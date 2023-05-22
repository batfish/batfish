package org.batfish.minesweeper.question.searchroutepolicies;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.minesweeper.AsPathRegexAtomicPredicates;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.RegexAtomicPredicates;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.SymbolicRegex;
import org.batfish.minesweeper.bdd.BDDDomain;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.ModelGeneration;
import org.batfish.minesweeper.bdd.TransferBDD;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.Action;
import org.batfish.minesweeper.utils.Tuple;
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

  private final boolean _perPath;

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
    _perPath = question.getPerPath();

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
            // AS-path output constraints are handled in a separate post-processing step, to
            // properly handle AS-path prepending
            .addAll(_inputConstraints.getAsPath().getAllRegexes())
            .build();
  }

  /**
   * Convert the results of symbolic route analysis into an answer to this question, if the
   * resulting constraints are satisfiable.
   *
   * @param constraints intersection of the input and output constraints provided as part of the
   *     question and the constraints on a solution that come from the symbolic route analysis
   * @param policy the route policy that was analyzed
   * @param configAPs an object that provides information about the community and as-path atomic
   *     predicates
   * @param prependedASes a list of the ASes that were prepended on the particular execution path
   *     being considered
   * @return an optional answer, which includes a concrete input route and (if the desired action is
   *     PERMIT) concrete output route
   */
  private Optional<Row> constraintsToResult(
      BDD constraints,
      RoutingPolicy policy,
      ConfigAtomicPredicates configAPs,
      List<Long> prependedASes) {
    if (constraints.isZero()) {
      return Optional.empty();
    } else {
      BDD fullModel = ModelGeneration.constraintsToModel(constraints, configAPs);

      Bgpv4Route inRoute = ModelGeneration.satAssignmentToInputRoute(fullModel, configAPs);
      Tuple<Predicate<String>, String> env =
          ModelGeneration.satAssignmentToEnvironment(fullModel, configAPs);
      Predicate<String> successfulTracks = env.getFirst();
      String sourceVrf = env.getSecond();

      if (_action == PERMIT) {
        // the AS path on the produced route represents the AS path that will result after
        // all prepends along the execution path occur. to obtain the original AS path of the
        // input route, we simply remove those prepended ASes.
        List<AsSet> asSets = inRoute.getAsPath().getAsSets();
        AsPath newAspath =
            AsPath.ofAsSets(
                asSets.subList(prependedASes.size(), asSets.size()).toArray(new AsSet[0]));
        inRoute = inRoute.toBuilder().setAsPath(newAspath).build();
      }

      Row result =
          TestRoutePoliciesAnswerer.rowResultFor(
              policy, inRoute, _direction, successfulTracks, sourceVrf);

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
      BDD nextHopBDD = r.getNextHop().toBDD(optNextHopIp.get());
      if (outputRoute) {
        // make sure that the next hop was not discarded by the route map
        if (r.getNextHopDiscarded()) {
          return r.getFactory().zero();
        }
        if (_direction == Environment.Direction.OUT && !r.getNextHopSet()) {
          // in the OUT direction we can only use the next-hop IP in the route
          // if the route-map explicitly sets it
          return r.getFactory().zero();
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
    /*
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

  /**
   * Updates the AS-path regex atomic predicates to incorporate the given AS-path constraints on the
   * output route. Then returns a BDD representing the AS-path regexes that satisfy these
   * constraints.
   *
   * @param asPathRegexes the user-defined regex constraints on the output AS path
   * @param configAPs object containing the AS-path atomic predicates; these atomic predicates are
   *     modified to represent only AS paths that satisfy the given regex constraints, also taking
   *     into account any AS prepending that occurs along the current execution path
   * @param r the {@link BDDRoute} representing the symbolic output route on the current execution
   *     path
   * @return a BDD representing atomic predicates that satisfy the given regex constraints
   */
  private BDD outputAsPathConstraintsToBDDAndUpdatedAPs(
      RegexConstraints asPathRegexes, ConfigAtomicPredicates configAPs, BDDRoute r) {
    // update the atomic predicates to include any prepended ASes and then to constrain them to
    // satisfy the given regex constraints
    AsPathRegexAtomicPredicates aps = configAPs.getAsPathRegexAtomicPredicates();
    aps.prependAPs(r.getPrependedASes());
    aps.constrainAPs(asPathRegexes);

    // produce the OR of all atomic predicates whose associated automata are non-empty
    // these are the atomic predicates that satisfy the given regex constraints
    Map<Integer, Automaton> apAutomata = aps.getAtomicPredicateAutomata();
    BDD[] apBDDs = r.getAsPathRegexAtomicPredicates();
    return r.getFactory()
        .orAll(
            apAutomata.keySet().stream()
                .filter(i -> !apAutomata.get(i).isEmpty())
                .map(i -> apBDDs[i])
                .collect(ImmutableSet.toImmutableSet()));
  }

  private <T> BDD setToBDD(Set<T> set, BDDRoute bddRoute, BDDDomain<T> bddDomain) {
    if (set.isEmpty()) {
      return bddRoute.getFactory().one();
    } else {
      return bddRoute.anyElementOf(set, bddDomain);
    }
  }

  // Produce a BDD that represents all truth assignments for the given BDDRoute r that satisfy the
  // given set of BgpRouteConstraints.  The way to represent next-hop constraints depends on whether
  // r is an input or output route, so the outputRoute flag distinguishes these cases.
  private BDD routeConstraintsToBDD(
      BgpRouteConstraints constraints,
      BDDRoute r,
      boolean outputRoute,
      ConfigAtomicPredicates configAPs) {

    // make sure the model we end up getting corresponds to a valid route
    BDD result = r.bgpWellFormednessConstraints();

    result.andWith(prefixSpaceToBDD(constraints.getPrefix(), r, constraints.getComplementPrefix()));
    result.andWith(longSpaceToBDD(constraints.getLocalPreference(), r.getLocalPref()));
    result.andWith(longSpaceToBDD(constraints.getMed(), r.getMed()));
    result.andWith(longSpaceToBDD(constraints.getTag(), r.getTag()));
    result.andWith(
        regexConstraintsToBDD(
            constraints.getCommunities(),
            CommunityVar::from,
            configAPs.getStandardCommunityAtomicPredicates(),
            r.getCommunityAtomicPredicates(),
            r.getFactory()));
    result.andWith(
        outputRoute
            ? outputAsPathConstraintsToBDDAndUpdatedAPs(constraints.getAsPath(), configAPs, r)
            : regexConstraintsToBDD(
                constraints.getAsPath(),
                SymbolicAsPathRegex::new,
                configAPs.getAsPathRegexAtomicPredicates(),
                r.getAsPathRegexAtomicPredicates(),
                r.getFactory()));
    result.andWith(nextHopIpConstraintsToBDD(constraints.getNextHopIp(), r, outputRoute));
    result.andWith(setToBDD(constraints.getOriginType(), r, r.getOriginType()));
    result.andWith(setToBDD(constraints.getProtocol(), r, r.getProtocolHistory()));

    return result;
  }

  /**
   * Search a particular route policy for behaviors of interest.
   *
   * @param policy the routing policy
   * @param configAPs an object providing the atomic predicates for the policy's owner configuration
   * @return an optional result, if a behavior of interest was found
   */
  private List<Row> searchPolicy(RoutingPolicy policy, ConfigAtomicPredicates configAPs) {
    List<TransferReturn> paths;
    TransferBDD tbdd;
    try {
      tbdd = new TransferBDD(configAPs, policy);
      paths = tbdd.computePaths(ImmutableSet.of());
    } catch (Exception e) {
      throw new BatfishException(
          "Unexpected error analyzing policy "
              + policy.getName()
              + " in node "
              + policy.getOwner().getHostname(),
          e);
    }

    Map<Boolean, List<TransferReturn>> pathMap =
        paths.stream()
            // consider only the subset of paths that have the desired action (permit or deny)
            .filter(p -> p.getAccepted() == (_action == PERMIT))
            // separate the paths that encountered an unsupported statement from the others
            .collect(Collectors.partitioningBy(tr -> tr.getFirst().getUnsupported()));
    // consider the paths that do not encounter an unsupported feature first, to avoid the potential
    // for false positives as much as possible
    List<TransferReturn> relevantPaths = pathMap.get(false);
    relevantPaths.addAll(pathMap.get(true));
    BDD inConstraints =
        routeConstraintsToBDD(
            _inputConstraints, new BDDRoute(tbdd.getFactory(), configAPs), false, configAPs);
    ImmutableList.Builder<Row> builder = ImmutableList.builder();
    for (TransferReturn path : relevantPaths) {
      BDD pathAnnouncements = path.getSecond();
      BDDRoute outputRoute = path.getFirst();
      BDD intersection = pathAnnouncements.and(inConstraints);
      // make a copy of the config atomic predicates, since the process of creating the constraints
      // on the output route can modify them, in order to handle AS-path constraints in the presence
      // of AS prepending
      ConfigAtomicPredicates outConfigAPs = new ConfigAtomicPredicates(configAPs);
      if (_action == PERMIT) {
        // incorporate the constraints on the output route as well
        BDD outConstraints =
            routeConstraintsToBDD(_outputConstraints, outputRoute, true, outConfigAPs);
        intersection = intersection.and(outConstraints);
      }

      Optional<Row> result =
          constraintsToResult(intersection, policy, outConfigAPs, outputRoute.getPrependedASes());
      if (result.isPresent()) {
        builder.add(result.get());
        if (!_perPath) {
          // return the first result we find
          break;
        }
      }
    }
    return builder.build();
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
    ConfigAtomicPredicates configAPs =
        new ConfigAtomicPredicates(
            _batfish,
            snapshot,
            node,
            _communityRegexes.stream()
                .map(CommunityVar::from)
                .collect(ImmutableSet.toImmutableSet()),
            _asPathRegexes,
            policies);

    return policies.stream().flatMap(policy -> searchPolicy(policy, configAPs).stream());
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
