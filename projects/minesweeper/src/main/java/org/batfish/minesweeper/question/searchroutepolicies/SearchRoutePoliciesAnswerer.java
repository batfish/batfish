package org.batfish.minesweeper.question.searchroutepolicies;

import static org.batfish.datamodel.answers.Schema.BGP_ROUTE;
import static org.batfish.datamodel.answers.Schema.NODE;
import static org.batfish.datamodel.answers.Schema.STRING;
import static org.batfish.minesweeper.CommunityVar.Type.EXACT;
import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantFor;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.LargeCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.PolicyQuotient;
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

  @Nonnull private final RouteConstraints _inputConstraints;
  @Nonnull private final RouteConstraints _outputConstraints;
  @Nonnull private final String _nodes;
  @Nonnull private final String _policies;
  @Nonnull private final Action _action;

  @Nonnull private final Graph _g;
  @Nonnull private final PolicyQuotient _pq;

  private BDD _inputConstraintsBDD;

  public SearchRoutePoliciesAnswerer(SearchRoutePoliciesQuestion question, IBatfish batfish) {
    super(question, batfish);
    _inputConstraints = question.getInputConstraints();
    _outputConstraints = question.getOutputConstraints();
    _nodes = question.getNodes();
    _policies = question.getPolicies();
    _action = question.getAction();

    _g = new Graph(batfish, batfish.getSnapshot());
    _pq = new PolicyQuotient();

    initializeRouteConstraints();
  }

  private void initializeRouteConstraints() {
    // add any communities from the input and output constraints to the Graph so that
    // they will be represented symbolically in our BDD computation
    _g.addCommunities(_inputConstraints.getCommunities().getCommunities());
    _g.addCommunities(_outputConstraints.getCommunities().getCommunities());

    _inputConstraintsBDD =
        routeConstraintsToBDD(_inputConstraints, new BDDRoute(_g.getAllCommunities()));
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

  private static Automaton communityVarToAutomaton(CommunityVar cvar) {
    String regex = cvar.getRegex();
    if (cvar.getType() != EXACT) {
      // strip the leading ^ and trailing $
      if (regex.startsWith("^")) {
        regex = regex.substring(1);
      }
      if (regex.endsWith("$")) {
        regex = regex.substring(0, regex.length() - 1);
      }
    }
    return new RegExp(regex).toAutomaton();
  }

  /**
   * Given a bdd representing a single satisfying assignment to the constraints from symbolic route
   * analysis, and given a BDDRoute that represents the constraints on the input or output route,
   * produce either a set of communities for the route that are consistent with this assignment, or
   * a set of constraints (represented as a BDD) that are contradictory. The latter can happen
   * because the symbolic route analysis treats community regexes (mostly) as a black box, so the
   * constraints may be shown to be infeasible after properly interpreting the regexes.
   *
   * @param satAssignment the satisfying assignment
   * @param r the BDDRoute
   * @return a set of communities or a BDD representing an infeasible constraint
   */
  static ResultOrUnsat<Set<Community>> satAssignmentToCommunities(BDD satAssignment, BDDRoute r) {

    // first figure out which communities are (not) in the assignment
    Map<CommunityVar, BDD> communities = r.getCommunities();
    Set<CommunityVar> positiveCommunities = new TreeSet<>();
    Set<CommunityVar> negativeCommunities = new TreeSet<>();
    for (Entry<CommunityVar, BDD> commEntry : communities.entrySet()) {
      CommunityVar commVar = commEntry.getKey();
      BDD commBDD = commEntry.getValue();
      if (commBDD.andSat(satAssignment)) {
        // this community should exist according to the given model
        positiveCommunities.add(commVar);
      } else {
        // this community should not exist according to the given model
        negativeCommunities.add(commVar);
      }
    }

    ImmutableSet.Builder<Community> comms = new ImmutableSet.Builder<>();
    Set<String> commStrs = new TreeSet<>();

    // create an automaton representing all of the communities that are not in this model
    Automaton disallowed = new Automaton();
    for (CommunityVar negvar : negativeCommunities) {
      disallowed = disallowed.union(communityVarToAutomaton(negvar));
    }

    // try to produce concrete communities for each positive community literal/regex
    for (CommunityVar cvar : positiveCommunities) {
      Automaton cvarAutomaton = communityVarToAutomaton(cvar);
      Community example;
      Community lit = cvar.getLiteralValue();
      if (lit != null) {
        // since literals are tracked precisely by the symbolic analysis, we know that this
        // literal must exist in the route
        example = lit;
      } else {
        // this is a regex, so we need to take into account the other community literals/regexes
        // that are not in the model, to see if there is a contradiction
        Automaton automaton = cvarAutomaton.minus(disallowed);
        if (automaton.isEmpty()) {
          // the regex constraints are not satisfiable, so
          // compute the BDD corresponding to these constraints and return it
          BDD disallowedConstraints =
              r.getFactory()
                  .orAll(
                      negativeCommunities.stream()
                          .map(communities::get)
                          .collect(ImmutableList.toImmutableList()));
          return new ResultOrUnsat<>(communities.get(cvar).diff(disallowedConstraints));
        } else {
          // check if any existing community in the route already satisfies this regex
          if (commStrs.stream().anyMatch(automaton::run)) {
            continue;
          }
          // the constraints are satisfiable so get an example string
          String str = automaton.getShortestExample(true);
          Optional<Community> exampleOpt = stringToCommunity(str);
          if (exampleOpt.isPresent()) {
            example = exampleOpt.get();
          } else {
            throw new BatfishException(
                "Failed to produce a valid community matching regex " + cvar.getRegex());
          }
        }
      }
      comms.add(example);
      commStrs.add(example.toString());
    }
    return new ResultOrUnsat<>(comms.build());
  }

  /**
   * Given a bdd representing a single satisfying assignment to the constraints from symbolic route
   * analysis, and given a BDDRoute that represents the constraints on the input or output route,
   * produce either a concrete route that is consistent with this assignment, or a set of
   * constraints (represented as a BDD) that are contradictory. The latter can happen because the
   * symbolic route analysis treats community regexes (mostly) as a black box, so the constraints
   * may be shown to be infeasible after properly interpreting the regexes.
   *
   * @param satAssignment the satisfying assignment
   * @param r the BDDRoute
   * @return either a route or a BDD representing an infeasible constraint
   */
  private static ResultOrUnsat<Bgpv4Route> satAssignmentToRoute(BDD satAssignment, BDDRoute r) {
    Bgpv4Route.Builder builder =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.IGP)
            .setProtocol(RoutingProtocol.BGP);

    ResultOrUnsat<Set<Community>> commResult = satAssignmentToCommunities(satAssignment, r);
    Set<Community> communities = commResult.getResult();
    if (communities == null) {
      // the community constraints were not satisfiable, so we need to get a new
      // satisfying assignment to the symbolic route constraints
      return new ResultOrUnsat<>(commResult.getUnsatConstraints());
    } else {
      // the community constraints were satisfiable so we can build a concrete route
      builder.setCommunities(communities);
      Ip ip = Ip.create(r.getPrefix().satAssignmentToLong(satAssignment));
      long len = r.getPrefixLength().satAssignmentToLong(satAssignment);
      builder.setNetwork(Prefix.create(ip, (int) len));

      builder.setLocalPreference(r.getLocalPref().satAssignmentToLong(satAssignment));
      builder.setAdmin((int) (long) r.getAdminDist().satAssignmentToLong(satAssignment));
      // BDDRoute has a med and a metric, which appear to be treated identically
      // I'm ignoring the metric and using the med
      builder.setMetric(r.getMed().satAssignmentToLong(satAssignment));

      return new ResultOrUnsat<>(builder.build());
    }
  }

  /**
   * Convert the results of symbolic route analysis into an answer to this question.
   *
   * @param constraints intersection of the input and output constraints provided as part of the
   *     question and the constraints on a solution that come from the symbolic route analysis
   * @param outputRoute the symbolic output route that results from the route analysis
   * @param policy the route policy that was analyzed
   * @return an optional answer, which includes a concrete input route and (if the desired action is
   *     PERMIT) concrete output route
   */
  private Optional<Result> constraintsToResult(
      BDD constraints, BDDRoute outputRoute, RoutingPolicy policy) {
    if (constraints.isZero()) {
      return Optional.empty();
    } else {
      BDD model = constraints.fullSatOne();
      ResultOrUnsat<Bgpv4Route> inResult =
          satAssignmentToRoute(model, new BDDRoute(_g.getAllCommunities()));
      if (_action == Action.DENY) {
        return Optional.of(
            new Result(
                new RoutingPolicyId(policy.getOwner().getHostname(), policy.getName()),
                // the input route constraints are always satisfiable so will always
                // produce a route.
                // this could change later if we add more kinds of route constraints.
                inResult.getResult(),
                _action,
                null));
      } else {
        ResultOrUnsat<Bgpv4Route> outResult = satAssignmentToRoute(model, outputRoute);
        if (outResult.getResult() == null) {
          // we couldn't solve the community regex constraints in the current model, so
          // try to find another one.
          return constraintsToResult(
              constraints.diff(outResult.getUnsatConstraints()), outputRoute, policy);
        } else {
          return Optional.of(
              new Result(
                  new RoutingPolicyId(policy.getOwner().getHostname(), policy.getName()),
                  inResult.getResult(),
                  _action,
                  outResult.getResult()));
        }
      }
    }
  }

  private SortedSet<RoutingPolicyId> resolvePolicies(SpecifierContext context) {
    NodeSpecifier nodeSpec =
        SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);

    RoutingPolicySpecifier policySpec =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(_policies, ALL_ROUTING_POLICIES);

    return nodeSpec.resolve(context).stream()
        .flatMap(
            node ->
                policySpec.resolve(node, context).stream()
                    .map(policy -> new RoutingPolicyId(node, policy.getName())))
        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
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

  private BDD integerSpaceToBDD(IntegerSpace space, BDDInteger bddInt) {
    if (space.isEmpty()) {
      return bddInt.getFactory().one();
    } else {
      BDD result = bddInt.getFactory().zero();
      for (SubRange range : space.getSubRanges()) {
        result = result.or(bddInt.range(range.getStart(), range.getEnd()));
      }
      return result;
    }
  }

  private BDD communityConstraintsToBDD(
      CommunitySet communitySet,
      Map<CommunityVar, BDD> commMap,
      boolean complementCommunities,
      BDDFactory factory) {
    Set<Community> communities = communitySet.getCommunities();
    if (communities.isEmpty()) {
      return factory.one();
    } else {
      BDD result = factory.zero();
      for (Community c : communitySet.getCommunities()) {
        CommunityVar cvar = CommunityVar.from(c);
        BDD commBDD = commMap.get(cvar);
        result = result.or(commBDD);
      }
      if (complementCommunities) {
        result = result.not();
      }
      return result;
    }
  }

  private BDD routeConstraintsToBDD(RouteConstraints constraints, BDDRoute r) {
    BDD result =
        prefixSpaceToBDD(constraints.getPrefixSpace(), r, constraints.getComplementPrefixSpace());
    result = result.and(integerSpaceToBDD(constraints.getLocalPref(), r.getLocalPref()));
    result = result.and(integerSpaceToBDD(constraints.getMed(), r.getMed()));
    result =
        result.and(
            communityConstraintsToBDD(
                constraints.getCommunities(),
                r.getCommunities(),
                constraints.getComplementCommunities(),
                r.getFactory()));

    return result;
  }

  private Optional<Result> searchPolicy(RoutingPolicy policy) {
    TransferReturn result;
    try {
      TransferBDD tbdd = new TransferBDD(_g, policy.getOwner(), policy.getStatements(), _pq);
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
    if (_action == Action.PERMIT) {
      // incorporate the constraints on the output route as well
      BDD outConstraints = routeConstraintsToBDD(_outputConstraints, outputRoute);
      intersection = acceptedAnnouncements.and(_inputConstraintsBDD).and(outConstraints);
    } else {
      intersection = acceptedAnnouncements.not().and(_inputConstraintsBDD);
    }

    return constraintsToResult(intersection, outputRoute, policy);
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    SpecifierContext context = _batfish.specifierContext(snapshot);
    SortedSet<RoutingPolicyId> policies = resolvePolicies(context);
    Multiset<Row> rows =
        getPolicies(context, policies)
            .map(this::searchPolicy)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(SearchRoutePoliciesAnswerer::toRow)
            .collect(ImmutableMultiset.toImmutableMultiset());

    TableAnswerElement answerElement = new TableAnswerElement(metadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  @Nonnull
  private Stream<RoutingPolicy> getPolicies(
      SpecifierContext context, SortedSet<RoutingPolicyId> policies) {
    Map<String, Configuration> configs = context.getConfigs();
    return policies.stream()
        .map(
            policyId ->
                configs.get(policyId.getNode()).getRoutingPolicies().get(policyId.getPolicy()));
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
            new ColumnMetadata(COL_OUTPUT_ROUTE, BGP_ROUTE, "The input route", false, false));
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
        .build();
  }
}
