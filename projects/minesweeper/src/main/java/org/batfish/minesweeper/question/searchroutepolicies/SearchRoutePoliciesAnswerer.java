package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE_DIFFS;
import static org.batfish.datamodel.answers.Schema.NODE;
import static org.batfish.datamodel.answers.Schema.STRING;
import static org.batfish.datamodel.questions.BgpRouteDiff.routeDiffs;
import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantFor;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action.PERMIT;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Range;
import dk.brics.automaton.Automaton;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RegexCommunitySet;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRouteDiffs;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.Protocol;
import org.batfish.minesweeper.RegexAtomicPredicates;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.SymbolicRegex;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.TransferBDD;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** An answerer for {@link SearchRoutePoliciesQuestion}. */
@ParametersAreNonnullByDefault
public final class SearchRoutePoliciesAnswerer extends Answerer {
  public static final String COL_NODE = "Node";
  public static final String COL_POLICY_NAME = "Policy_Name";
  public static final String COL_INPUT_ROUTE = "Input_Route";
  public static final String COL_ACTION = "Action";
  public static final String COL_OUTPUT_ROUTE = "Output_Route";
  public static final String COL_DIFF = "Difference";

  @Nonnull private final BgpRouteConstraints _inputConstraints;
  @Nonnull private final BgpRouteConstraints _outputConstraints;
  @Nonnull private final NodeSpecifier _nodeSpecifier;
  @Nonnull private final RoutingPolicySpecifier _policySpecifier;
  @Nonnull private final Action _action;

  @Nonnull private final Set<String> _communityRegexes;
  @Nonnull private final Set<String> _asPathRegexes;

  public SearchRoutePoliciesAnswerer(SearchRoutePoliciesQuestion question, IBatfish batfish) {
    super(question, batfish);
    _inputConstraints = question.getInputConstraints();
    _outputConstraints = question.getOutputConstraints();
    _nodeSpecifier =
        SpecifierFactories.getNodeSpecifierOrDefault(
            question.getNodes(), AllNodesNodeSpecifier.INSTANCE);
    _policySpecifier =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(
            question.getPolicies(), ALL_ROUTING_POLICIES);
    _action = question.getAction();

    // in the future, it may improve performance to combine all input community regexes
    // into a single regex representing their disjunction, and similarly for all output
    // community regexes, in order to minimize the number of atomic predicates that are
    // created and tracked by the analysis
    _communityRegexes =
        ImmutableSet.<String>builder()
            .addAll(_inputConstraints.getCommunities().getAllRegexes())
            .addAll(_outputConstraints.getCommunities().getAllRegexes())
            .build();
    _asPathRegexes =
        ImmutableSet.<String>builder()
            .addAll(_inputConstraints.getAsPath().getAllRegexes())
            .addAll(_outputConstraints.getAsPath().getAllRegexes())
            .build();
  }

  private static Optional<Community> stringToCommunity(String str) {
    Optional<StandardCommunity> scomm = StandardCommunity.tryParse(str);
    if (scomm.isPresent()) {
      return Optional.of(scomm.get());
    }
    Optional<ExtendedCommunity> ecomm = ExtendedCommunity.tryParse(str);
    if (ecomm.isPresent()) {
      return Optional.of(ecomm.get());
    }
    Optional<LargeCommunity> lcomm = LargeCommunity.tryParse(str);
    if (lcomm.isPresent()) {
      return Optional.of(lcomm.get());
    }
    return Optional.empty();
  }

  /**
   * Given a single satisfying assignment to the constraints from symbolic route analysis, produce a
   * set of communities for a given symbolic route that is consistent with the assignment.
   *
   * @param fullModel a full model of the symbolic route constraints
   * @param r the symbolic route
   * @param g the Graph, which provides information about the community atomic predicates
   * @return a set of communities
   */
  static Set<Community> satAssignmentToCommunities(BDD fullModel, BDDRoute r, Graph g) {

    BDD[] aps = r.getCommunityAtomicPredicates();
    Map<Integer, Automaton> apAutomata =
        g.getCommunityAtomicPredicates().getAtomicPredicateAutomata();

    ImmutableSet.Builder<Community> comms = new ImmutableSet.Builder<>();
    for (int i = 0; i < aps.length; i++) {
      if (aps[i].andSat(fullModel)) {
        Automaton a = apAutomata.get(i);
        // community atomic predicates should always be non-empty;
        // see RegexAtomicPredicates::initAtomicPredicates
        checkState(!a.isEmpty(), "Cannot produce example string for empty automaton");
        String str = a.getShortestExample(true);
        // community automata should only accept strings with this property;
        // see CommunityVar::toAutomaton
        checkState(
            str.startsWith("^") && str.endsWith("$"),
            "Community example %s has an unexpected format",
            str);
        // strip off the leading ^ and trailing $
        str = str.substring(1, str.length() - 1);
        Optional<Community> exampleOpt = stringToCommunity(str);
        if (exampleOpt.isPresent()) {
          comms.add(exampleOpt.get());
        } else {
          throw new BatfishException("Failed to produce a valid community for answer");
        }
      }
    }
    return comms.build();
  }

  /**
   * Given a single satisfying assignment to the constraints from symbolic route analysis, produce
   * an AS-path for a given symbolic route that is consistent with the assignment.
   *
   * @param fullModel a full model of the symbolic route constraints
   * @param r the symbolic route
   * @param g the Graph, which provides information about the AS-path regex atomic predicates
   * @return an AsPath
   */
  static AsPath satAssignmentToAsPath(BDD fullModel, BDDRoute r, Graph g) {

    BDD[] aps = r.getAsPathRegexAtomicPredicates();
    Map<Integer, Automaton> apAutomata =
        g.getAsPathRegexAtomicPredicates().getAtomicPredicateAutomata();

    // find all atomic predicates that are required to be true in the given model
    List<Integer> trueAPs =
        IntStream.range(0, g.getAsPathRegexAtomicPredicates().getNumAtomicPredicates())
            .filter(i -> aps[i].andSat(fullModel))
            .boxed()
            .collect(Collectors.toList());

    // since atomic predicates are disjoint, at most one of them should be true in the model
    checkState(
        trueAPs.size() <= 1,
        "Error in symbolic AS-path analysis: at most one atomic predicate should be true");

    // create an automaton for the language of AS-paths that are true in the model
    Automaton asPathRegexAutomaton = SymbolicAsPathRegex.ALL_AS_PATHS.toAutomaton();
    for (Integer i : trueAPs) {
      asPathRegexAutomaton = asPathRegexAutomaton.intersection(apAutomata.get(i));
    }

    String asPathStr = asPathRegexAutomaton.getShortestExample(true);
    // As-path regex automata should only accept strings with this property;
    // see SymbolicAsPathRegex::toAutomaton
    checkState(
        asPathStr.startsWith("^") && asPathStr.endsWith("$"),
        "AS-path example %s has an unexpected format",
        asPathStr);
    // strip off the leading ^ and trailing $
    asPathStr = asPathStr.substring(1, asPathStr.length() - 1);
    // the string is a space-separated list of numbers; convert them to a list of numbers
    List<Long> asns;
    if (asPathStr.isEmpty()) {
      asns = ImmutableList.of();
    } else {
      try {
        asns =
            Arrays.stream(asPathStr.split(" "))
                .mapToLong(Long::valueOf)
                .boxed()
                .collect(Collectors.toList());
      } catch (NumberFormatException nfe) {
        throw new BatfishException("Failed to produce a valid AS path for answer");
      }
    }
    return AsPath.ofSingletonAsSets(asns);
  }

  /**
   * Given a satisfying assignment to the constraints from symbolic route analysis, produce a
   * concrete route for a given symbolic route that is consistent with the assignment.
   *
   * @param fullModel the satisfying assignment
   * @param r the symbolic route
   * @param g the Graph, which provides information about the community atomic predicates
   * @return either a route or a BDD representing an infeasible constraint
   */
  private static Bgpv4Route satAssignmentToRoute(BDD fullModel, BDDRoute r, Graph g) {
    Bgpv4Route.Builder builder =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setNextHop(NextHopDiscard.instance())
            .setProtocol(RoutingProtocol.BGP);

    Ip ip = Ip.create(r.getPrefix().satAssignmentToLong(fullModel));
    long len = r.getPrefixLength().satAssignmentToLong(fullModel);
    builder.setNetwork(Prefix.create(ip, (int) len));

    builder.setLocalPreference(r.getLocalPref().satAssignmentToLong(fullModel));
    builder.setAdmin((int) (long) r.getAdminDist().satAssignmentToLong(fullModel));
    // the BDDRoute also tracks a metric but I believe for BGP we should use the MED
    builder.setMetric(r.getMed().satAssignmentToLong(fullModel));

    Set<Community> communities = satAssignmentToCommunities(fullModel, r, g);
    builder.setCommunities(communities);

    AsPath asPath = satAssignmentToAsPath(fullModel, r, g);
    builder.setAsPath(asPath);

    return builder.build();
  }

  /**
   * Convert the results of symbolic route analysis into an answer to this question, if the
   * resulting constraints are satisfiable.
   *
   * @param constraints intersection of the input and output constraints provided as part of the
   *     question and the constraints on a solution that come from the symbolic route analysis
   * @param outputRoute the symbolic output route that results from the route analysis
   * @param policy the route policy that was analyzed
   * @param g the Graph, which provides information about the community atomic predicates
   * @return an optional answer, which includes a concrete input route and (if the desired action is
   *     PERMIT) concrete output route
   */
  private Optional<Result> constraintsToResult(
      BDD constraints, BDDRoute outputRoute, RoutingPolicy policy, Graph g) {
    if (constraints.isZero()) {
      return Optional.empty();
    } else {
      BDD fullModel = constraints.fullSatOne();
      Bgpv4Route inRoute = satAssignmentToRoute(fullModel, new BDDRoute(g), g);
      Bgpv4Route outRoute =
          _action == Action.DENY ? null : satAssignmentToRoute(fullModel, outputRoute, g);
      return Optional.of(
          new Result(
              new RoutingPolicyId(policy.getOwner().getHostname(), policy.getName()),
              inRoute,
              _action,
              outRoute));
    }
  }

  private BDD prefixSpaceToBDD(PrefixSpace space, BDDRoute r, boolean complementPrefixes) {
    BDDFactory factory = r.getPrefix().getFactory();
    if (space.isEmpty()) {
      return factory.one();
    } else {
      BDD result = factory.zero();
      for (PrefixRange range : space.getPrefixRanges()) {
        BDD rangeBDD = isRelevantFor(r, range);
        result = result.or(rangeBDD);
      }
      if (complementPrefixes) {
        result = result.not();
      }
      return result;
    }
  }

  // convert a possibly open range of longs to a closed one
  @VisibleForTesting
  static Range<Long> toClosedRange(Range<Long> r) {
    BoundType lowerType = r.lowerBoundType();
    Long lowerBound = r.lowerEndpoint();
    BoundType upperType = r.upperBoundType();
    Long upperBound = r.upperEndpoint();

    return Range.range(
        lowerType == BoundType.CLOSED ? lowerBound : lowerBound + 1,
        BoundType.CLOSED,
        upperType == BoundType.CLOSED ? upperBound : upperBound - 1,
        BoundType.CLOSED);
  }

  private BDD longSpaceToBDD(LongSpace space, BDDInteger bddInt) {
    if (space.isEmpty()) {
      return bddInt.getFactory().one();
    } else {
      BDD result = bddInt.getFactory().zero();
      for (Range<Long> range : space.getRanges()) {
        Range<Long> closedRange = toClosedRange(range);
        result = result.or(bddInt.range(closedRange.lowerEndpoint(), closedRange.upperEndpoint()));
      }
      return result;
    }
  }

  /**
   * Convert regex constraints from a {@link BgpRouteConstraints} object to a BDD.
   *
   * @param regexes the user-defined regex constraints
   * @param constructor function to convert a regex string into a symbolic regex object
   * @param atomicPredicates information about the atomic predicates corresponding to the regexes
   * @param atomicPredicateBDDs one BDD per atomic predicate, coming from a {@link BDDRoute} object
   * @param factory the BDD factory
   * @param <T> the particular type of symbolic regexes (community or AS-path)
   * @return the overall constraint as a BDD
   */
  private <T extends SymbolicRegex> BDD regexConstraintsToBDD(
      RegexConstraints regexes,
      Function<String, T> constructor,
      RegexAtomicPredicates<T> atomicPredicates,
      BDD[] atomicPredicateBDDs,
      BDDFactory factory) {
    /**
     * disjoin all positive regex constraints, each of which is itself logically represented as the
     * disjunction of its corresponding atomic predicates. special case: if there are no positive
     * constraints then treat the constraint as "true", i.e. no constraints.
     */
    BDD positiveConstraints =
        regexes.getPositiveRegexConstraints().isEmpty()
            ? factory.one()
            : factory.orAll(
                regexes.getPositiveRegexConstraints().stream()
                    .map(RegexConstraint::getRegex)
                    .map(constructor)
                    .flatMap(
                        regex -> atomicPredicates.getRegexAtomicPredicates().get(regex).stream())
                    .map(i -> atomicPredicateBDDs[i])
                    .collect(ImmutableSet.toImmutableSet()));
    // disjoin all negative regex constraints, similarly
    BDD negativeConstraints =
        factory.orAll(
            regexes.getNegativeRegexConstraints().stream()
                .map(RegexConstraint::getRegex)
                .map(constructor)
                .flatMap(regex -> atomicPredicates.getRegexAtomicPredicates().get(regex).stream())
                .map(i -> atomicPredicateBDDs[i])
                .collect(ImmutableSet.toImmutableSet()));

    return positiveConstraints.diffWith(negativeConstraints);
  }

  private BDD routeConstraintsToBDD(BgpRouteConstraints constraints, BDDRoute r, Graph g) {

    // make sure the model we end up getting corresponds to a valid route
    BDD result = r.wellFormednessConstraints();

    // require the protocol to be BGP
    result.andWith(r.getProtocolHistory().value(Protocol.BGP));
    result.andWith(prefixSpaceToBDD(constraints.getPrefix(), r, constraints.getComplementPrefix()));
    result.andWith(longSpaceToBDD(constraints.getLocalPreference(), r.getLocalPref()));
    result.andWith(longSpaceToBDD(constraints.getMed(), r.getMed()));
    result.andWith(
        regexConstraintsToBDD(
            constraints.getCommunities(),
            CommunityVar::from,
            g.getCommunityAtomicPredicates(),
            r.getCommunityAtomicPredicates(),
            r.getFactory()));
    result.andWith(
        regexConstraintsToBDD(
            constraints.getAsPath(),
            SymbolicAsPathRegex::new,
            g.getAsPathRegexAtomicPredicates(),
            r.getAsPathRegexAtomicPredicates(),
            r.getFactory()));

    return result;
  }

  /**
   * Search a particular route policy for behaviors of interest.
   *
   * @param policy the routing policy
   * @param g a Graph object providing information about the policy's owner configuration
   * @return an optional result, if a behavior of interest was found
   */
  private Optional<Result> searchPolicy(RoutingPolicy policy, Graph g) {
    TransferReturn result;
    try {
      TransferBDD tbdd = new TransferBDD(g, policy.getOwner(), policy.getStatements());
      result = tbdd.compute(ImmutableSet.of()).getReturnValue();
    } catch (Exception e) {
      throw new BatfishException(
          "Unsupported features in route policy "
              + policy.getName()
              + " in node "
              + policy.getOwner().getHostname(),
          e);
    }
    BDD acceptedAnnouncements = result.getSecond();
    BDDRoute outputRoute = result.getFirst();
    BDD intersection;
    BDD inConstraints = routeConstraintsToBDD(_inputConstraints, new BDDRoute(g), g);
    if (_action == PERMIT) {
      // incorporate the constraints on the output route as well
      BDD outConstraints = routeConstraintsToBDD(_outputConstraints, outputRoute, g);
      intersection = acceptedAnnouncements.and(inConstraints).and(outConstraints);
    } else {
      intersection = acceptedAnnouncements.not().and(inConstraints);
    }

    return constraintsToResult(intersection, outputRoute, policy, g);
  }

  /**
   * Search all of the route policies of a particular node for behaviors of interest.
   *
   * @param node the node
   * @param policies all route policies in that node
   * @return all results from analyzing those route policies
   */
  private Stream<Result> searchPoliciesForNode(String node, Set<RoutingPolicy> policies) {
    Graph g =
        new Graph(
            _batfish,
            _batfish.getSnapshot(),
            null,
            ImmutableSet.of(node),
            _communityRegexes.stream()
                .map(RegexCommunitySet::new)
                .collect(ImmutableSet.toImmutableSet()),
            _asPathRegexes);

    return policies.stream()
        .map(policy -> searchPolicy(policy, g))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    SpecifierContext context = _batfish.specifierContext(snapshot);
    Multiset<Row> rows =
        _nodeSpecifier.resolve(context).stream()
            .flatMap(node -> searchPoliciesForNode(node, _policySpecifier.resolve(node, context)))
            .map(SearchRoutePoliciesAnswerer::toRow)
            .collect(ImmutableMultiset.toImmutableMultiset());

    TableAnswerElement answerElement = new TableAnswerElement(metadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  @Nullable
  private static org.batfish.datamodel.questions.BgpRoute toQuestionsBgpRoute(
      @Nullable Bgpv4Route dataplaneBgpRoute) {
    if (dataplaneBgpRoute == null) {
      return null;
    }
    return org.batfish.datamodel.questions.BgpRoute.builder()
        .setWeight(dataplaneBgpRoute.getWeight())
        .setNextHopIp(dataplaneBgpRoute.getNextHopIp())
        .setProtocol(dataplaneBgpRoute.getProtocol())
        .setSrcProtocol(dataplaneBgpRoute.getSrcProtocol())
        .setOriginType(dataplaneBgpRoute.getOriginType())
        .setOriginatorIp(dataplaneBgpRoute.getOriginatorIp())
        .setMetric(dataplaneBgpRoute.getMetric())
        .setLocalPreference(dataplaneBgpRoute.getLocalPreference())
        .setWeight(dataplaneBgpRoute.getWeight())
        .setNetwork(dataplaneBgpRoute.getNetwork())
        .setCommunities(dataplaneBgpRoute.getCommunities().getCommunities())
        .setAsPath(dataplaneBgpRoute.getAsPath())
        .build();
  }

  public static TableMetadata metadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, NODE, "The node that has the policy", true, false),
            new ColumnMetadata(COL_POLICY_NAME, STRING, "The name of this policy", true, false),
            new ColumnMetadata(COL_INPUT_ROUTE, BGP_ROUTE, "The input route", true, false),
            new ColumnMetadata(
                COL_ACTION, STRING, "The action of the policy on the input route", false, true),
            new ColumnMetadata(COL_OUTPUT_ROUTE, BGP_ROUTE, "The output route", false, false),
            new ColumnMetadata(
                COL_DIFF,
                BGP_ROUTE_DIFFS,
                "The difference between the input and output routes",
                false,
                true));
    return new TableMetadata(
        columnMetadata, String.format("Results for policy ${%s}", COL_POLICY_NAME));
  }

  private static Row toRow(Result result) {
    org.batfish.datamodel.questions.BgpRoute inputRoute =
        toQuestionsBgpRoute(result.getInputRoute());
    org.batfish.datamodel.questions.BgpRoute outputRoute =
        toQuestionsBgpRoute(result.getOutputRoute());

    Action action = result.getAction();
    RoutingPolicyId policyId = result.getPolicyId();
    return Row.builder()
        .put(COL_NODE, new Node(policyId.getNode()))
        .put(COL_POLICY_NAME, policyId.getPolicy())
        .put(COL_INPUT_ROUTE, inputRoute)
        .put(COL_ACTION, action)
        .put(COL_OUTPUT_ROUTE, outputRoute)
        .put(
            COL_DIFF,
            action == PERMIT ? new BgpRouteDiffs(routeDiffs(inputRoute, outputRoute)) : null)
        .build();
  }

  @Nonnull
  @VisibleForTesting
  NodeSpecifier getNodeSpecifier() {
    return _nodeSpecifier;
  }

  @Nonnull
  @VisibleForTesting
  RoutingPolicySpecifier getPolicySpecifier() {
    return _policySpecifier;
  }
}
