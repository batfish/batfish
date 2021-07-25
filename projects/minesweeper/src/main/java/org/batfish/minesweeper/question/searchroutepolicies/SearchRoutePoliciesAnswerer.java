package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.answers.Schema.STRING;
import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantForDestination;
import static org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action.PERMIT;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
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
import org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** An answerer for {@link SearchRoutePoliciesQuestion}. */
@ParametersAreNonnullByDefault
public final class SearchRoutePoliciesAnswerer extends Answerer {

  @Nonnull private final Environment.Direction _direction;
  @Nonnull private final BgpRouteConstraints _inputConstraints;
  @Nonnull private final BgpRouteConstraints _outputConstraints;
  @Nonnull private final NodeSpecifier _nodeSpecifier;
  @Nonnull private final RoutingPolicySpecifier _policySpecifier;
  @Nonnull private final Action _action;

  @Nonnull private final Set<String> _communityRegexes;
  @Nonnull private final Set<String> _asPathRegexes;

  public SearchRoutePoliciesAnswerer(SearchRoutePoliciesQuestion question, IBatfish batfish) {
    super(question, batfish);
    _direction = question.getDirection();
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
   * concrete input route that is consistent with the assignment.
   *
   * @param fullModel the satisfying assignment
   * @param g the Graph, which provides information about the community atomic predicates
   * @return a route
   */
  private static Bgpv4Route satAssignmentToInputRoute(BDD fullModel, Graph g) {
    Bgpv4Route.Builder builder =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP);

    BDDRoute r = new BDDRoute(g);

    Ip ip = Ip.create(r.getPrefix().satAssignmentToLong(fullModel));
    long len = r.getPrefixLength().satAssignmentToLong(fullModel);
    builder.setNetwork(Prefix.create(ip, (int) len));

    builder.setLocalPreference(r.getLocalPref().satAssignmentToLong(fullModel));
    builder.setAdmin((int) (long) r.getAdminDist().satAssignmentToLong(fullModel));
    builder.setMetric(r.getMed().satAssignmentToLong(fullModel));
    builder.setTag(r.getTag().satAssignmentToLong(fullModel));

    Set<Community> communities = satAssignmentToCommunities(fullModel, r, g);
    builder.setCommunities(communities);

    AsPath asPath = satAssignmentToAsPath(fullModel, r, g);
    builder.setAsPath(asPath);

    // Note: this is the only part of the method that relies on the fact that we are solving for the
    // input route.  If we also want to produce the output route from the model, given the BDDRoute
    // that results from symbolic analysis, we need to consider the _direction as well as the values
    // of the two next-hop flags in the BDDRoute, in order to do it properly
    builder.setNextHop(NextHopIp.of(Ip.create(r.getNextHop().satAssignmentToLong(fullModel))));

    return builder.build();
  }

  /**
   * Convert the results of symbolic route analysis into an answer to this question, if the
   * resulting constraints are satisfiable.
   *
   * @param constraints intersection of the input and output constraints provided as part of the
   *     question and the constraints on a solution that come from the symbolic route analysis
   * @param policy the route policy that was analyzed
   * @param g the Graph, which provides information about the community and as-path atomic
   *     predicates
   * @return an optional answer, which includes a concrete input route and (if the desired action is
   *     PERMIT) concrete output route
   */
  private Optional<Row> constraintsToResult(BDD constraints, RoutingPolicy policy, Graph g) {
    if (constraints.isZero()) {
      return Optional.empty();
    } else {
      BDD fullModel = constraints.fullSatOne();
      Bgpv4Route inRoute = satAssignmentToInputRoute(fullModel, g);
      Row result = TestRoutePoliciesAnswerer.rowResultFor(policy, inRoute, _direction);
      // sanity check: make sure that the accept/deny status produced by TestRoutePolicies is
      // the same as what the user was asking for.  if this ever fails then either TRP or SRP
      // is modeling something incorrectly (or both).
      // TODO: We can also take this validation further by using a variant of
      // satAssignmentToInputRoute to produce the output route from our fullModel and the final
      // BDDRoute from the symbolic analysis (as we used to do) and then compare that to the TRP
      // result.
      assert result.get(TestRoutePoliciesAnswerer.COL_ACTION, STRING).equals(_action.toString());
      return Optional.of(result);
    }
  }

  private BDD prefixSpaceToBDD(PrefixSpace space, BDDRoute r, boolean complementPrefixes) {
    BDDFactory factory = r.getPrefix().getFactory();
    if (space.isEmpty()) {
      return factory.one();
    } else {
      BDD result = factory.zero();
      for (PrefixRange range : space.getPrefixRanges()) {
        BDD rangeBDD = isRelevantForDestination(r, range);
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

  private BDD nextHopIpConstraintsToBDD(
      Optional<Prefix> optNextHopIp, BDDRoute r, boolean outputRoute) {
    if (optNextHopIp.isPresent()) {
      BDD nextHopBDD = optNextHopIp.get().toIpSpace().accept(new IpSpaceToBDD(r.getNextHop()));
      if (outputRoute) {
        // make sure that the next hop was not discarded by the route map
        nextHopBDD = nextHopBDD.and(r.getNextHopDiscarded().not());
        if (_direction == Environment.Direction.OUT) {
          // in the OUT direction we can only use the next-hop IP in the route
          // if the route-map explicitly sets it
          nextHopBDD = nextHopBDD.and(r.getNextHopSet());
        }
      }
      return nextHopBDD;
    } else {
      return r.getFactory().one();
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

  // Produce a BDD that represents all truth assignments for the given BDDRoute r that satisfy the
  // given set of BgpRouteConstraints.  The way to represent next-hop constraints depends on whether
  // r is an input or output route, so the outputRoute flag distinguishes these cases.
  private BDD routeConstraintsToBDD(
      BgpRouteConstraints constraints, BDDRoute r, boolean outputRoute, Graph g) {

    // make sure the model we end up getting corresponds to a valid route
    BDD result = r.wellFormednessConstraints();

    // require the protocol to be BGP
    result.andWith(r.getProtocolHistory().value(Protocol.BGP));
    result.andWith(prefixSpaceToBDD(constraints.getPrefix(), r, constraints.getComplementPrefix()));
    result.andWith(longSpaceToBDD(constraints.getLocalPreference(), r.getLocalPref()));
    result.andWith(longSpaceToBDD(constraints.getMed(), r.getMed()));
    result.andWith(longSpaceToBDD(constraints.getTag(), r.getTag()));
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
    result.andWith(nextHopIpConstraintsToBDD(constraints.getNextHopIp(), r, outputRoute));

    return result;
  }

  /**
   * Search a particular route policy for behaviors of interest.
   *
   * @param policy the routing policy
   * @param g a Graph object providing information about the policy's owner configuration
   * @return an optional result, if a behavior of interest was found
   */
  private Optional<Row> searchPolicy(RoutingPolicy policy, Graph g) {
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
    BDD inConstraints = routeConstraintsToBDD(_inputConstraints, new BDDRoute(g), false, g);
    if (_action == PERMIT) {
      // incorporate the constraints on the output route as well
      BDD outConstraints = routeConstraintsToBDD(_outputConstraints, outputRoute, true, g);
      intersection = acceptedAnnouncements.and(inConstraints).and(outConstraints);
    } else {
      intersection = acceptedAnnouncements.not().and(inConstraints);
    }

    return constraintsToResult(intersection, policy, g);
  }

  /**
   * Search all of the route policies of a particular node for behaviors of interest.
   *
   * @param node the node
   * @param policies all route policies in that node
   * @return all results from analyzing those route policies
   */
  private Stream<Row> searchPoliciesForNode(
      String node, Set<RoutingPolicy> policies, NetworkSnapshot snapshot) {
    Graph g =
        new Graph(
            _batfish,
            snapshot,
            null,
            ImmutableSet.of(node),
            _communityRegexes.stream()
                .map(CommunityVar::from)
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
    List<Row> rows =
        _nodeSpecifier.resolve(context).stream()
            .flatMap(
                node ->
                    searchPoliciesForNode(node, _policySpecifier.resolve(node, context), snapshot))
            .collect(ImmutableList.toImmutableList());

    TableAnswerElement answerElement = new TableAnswerElement(TestRoutePoliciesAnswerer.metadata());
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
        .setNextHopIp(dataplaneBgpRoute.getNextHopIp())
        .setProtocol(dataplaneBgpRoute.getProtocol())
        .setSrcProtocol(dataplaneBgpRoute.getSrcProtocol())
        .setOriginType(dataplaneBgpRoute.getOriginType())
        .setOriginatorIp(dataplaneBgpRoute.getOriginatorIp())
        .setMetric(dataplaneBgpRoute.getMetric())
        .setLocalPreference(dataplaneBgpRoute.getLocalPreference())
        .setTag(dataplaneBgpRoute.getTag())
        .setWeight(dataplaneBgpRoute.getWeight())
        .setNetwork(dataplaneBgpRoute.getNetwork())
        .setCommunities(dataplaneBgpRoute.getCommunities().getCommunities())
        .setAsPath(dataplaneBgpRoute.getAsPath())
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
