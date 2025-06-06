package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.minesweeper.bdd.TransferBDD.isRelevantForDestination;
import static org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer.toRow;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import dk.brics.automaton.Automaton;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.NextHopBgpPeerAddress;
import org.batfish.datamodel.answers.NextHopSelf;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.minesweeper.AsPathRegexAtomicPredicates;
import org.batfish.minesweeper.CommunityVar;
import org.batfish.minesweeper.ConfigAtomicPredicates;
import org.batfish.minesweeper.SymbolicAsPathRegex;
import org.batfish.minesweeper.bdd.BDDDomain;
import org.batfish.minesweeper.bdd.BDDRoute;
import org.batfish.minesweeper.bdd.CommunityMatchExprToBDD;
import org.batfish.minesweeper.bdd.CommunitySetMatchExprToBDD;
import org.batfish.minesweeper.bdd.ModelGeneration;
import org.batfish.minesweeper.bdd.TransferBDD;
import org.batfish.minesweeper.bdd.TransferBDD.Context;
import org.batfish.minesweeper.bdd.TransferReturn;
import org.batfish.minesweeper.communities.CommunityMatchExprVarCollector;
import org.batfish.minesweeper.question.searchroutepolicies.SearchRoutePoliciesQuestion.PathOption;
import org.batfish.minesweeper.utils.RouteMapEnvironment;
import org.batfish.question.testroutepolicies.Result;
import org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** An answerer for {@link SearchRoutePoliciesQuestion}. */
@ParametersAreNonnullByDefault
public final class SearchRoutePoliciesAnswerer extends Answerer {

  private final @Nonnull Environment.Direction _direction;
  private final @Nonnull BgpRouteConstraints _inputConstraints;
  private final @Nonnull BgpRouteConstraints _outputConstraints;
  private final @Nonnull NodeSpecifier _nodeSpecifier;
  private final @Nonnull RoutingPolicySpecifier _policySpecifier;
  private final @Nonnull LineAction _action;

  private final PathOption _pathOption;

  private final @Nonnull Set<RegexConstraint> _communityRegexes;
  private final @Nonnull Set<RegexConstraint> _asPathRegexes;

  /** Helper class that contains both a row and and Bgpv4Route for a result */
  private static class RowAndRoute {
    public final Bgpv4Route _route;
    public final Row _row;

    public RowAndRoute(Bgpv4Route route, Row row) {
      this._route = route;
      this._row = row;
    }
  }

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
    _pathOption = question.getPathOption();

    // in the future, it may improve performance to combine all input community regexes
    // into a single regex representing their disjunction, and similarly for all output
    // community regexes, in order to minimize the number of atomic predicates that are
    // created and tracked by the analysis
    _communityRegexes =
        ImmutableSet.<RegexConstraint>builder()
            .addAll(_inputConstraints.getCommunities().getRegexConstraints())
            .addAll(_outputConstraints.getCommunities().getRegexConstraints())
            .build();
    _asPathRegexes =
        ImmutableSet.<RegexConstraint>builder()
            // AS-path output constraints are handled in a separate post-processing step, to
            // properly handle AS-path prepending
            .addAll(_inputConstraints.getAsPath().getRegexConstraints())
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
   * @param outputRoute the symbolic route produced by the analysis
   * @return an optional answer, which includes a concrete input route and (if the desired action is
   *     PERMIT) concrete output route
   */
  private Optional<RowAndRoute> constraintsToResult(
      BDD constraints,
      RoutingPolicy policy,
      ConfigAtomicPredicates configAPs,
      BDDRoute outputRoute) {
    if (constraints.isZero()) {
      return Optional.empty();
    } else {
      BDD model = ModelGeneration.constraintsToModel(constraints, configAPs);

      Bgpv4Route inRoute = ModelGeneration.satAssignmentToBgpInputRoute(model, configAPs);
      RouteMapEnvironment env = ModelGeneration.satAssignmentToEnvironment(model, configAPs);

      if (_action == PERMIT) {
        // the AS path on the produced route represents the AS path that will result after
        // all prepends along the execution path occur. to obtain the original AS path of the
        // input route, we simply remove those prepended ASes.
        List<AsSet> asSets = inRoute.getAsPath().getAsSets();
        AsPath newAspath =
            AsPath.ofAsSets(
                asSets
                    .subList(outputRoute.getPrependedASes().size(), asSets.size())
                    .toArray(new AsSet[0]));
        inRoute = inRoute.toBuilder().setAsPath(newAspath).build();
      }

      Result<BgpRoute, BgpRoute> result =
          simulatePolicy(policy, inRoute, _direction, env, outputRoute);

      // As a sanity check, compare the simulated result above with what the symbolic route
      // analysis predicts will happen.
      assert ModelGeneration.validateModel(
          model, outputRoute, configAPs, _action, _direction, result);

      return Optional.of(new RowAndRoute(inRoute, toRow(result)));
    }
  }

  /**
   * Produce the results of simulating the given route policy on the given input route.
   *
   * @param policy the route policy to simulate
   * @param inRoute the input route for the policy
   * @param direction whether the policy is used on import or export (IN or OUT)
   * @param env a pair of a predicate that indicates which tracks are successful and an optional
   *     name of the source VRF
   * @return the results of the simulation as a result for this question
   */
  public static Result<BgpRoute, BgpRoute> simulatePolicy(
      RoutingPolicy policy,
      Bgpv4Route inRoute,
      Environment.Direction direction,
      RouteMapEnvironment env,
      BDDRoute bddRoute) {
    Result<Bgpv4Route, Bgpv4Route> simResult =
        TestRoutePoliciesAnswerer.simulatePolicyWithBgpRoute(
            policy,
            inRoute,
            env.getSessionProperties(),
            direction,
            env.getSuccessfulTracks(),
            env.getSourceVrf());
    return toQuestionResult(simResult, bddRoute);
  }

  /**
   * Converts a simulation result that uses {@link Bgpv4Route} to represent the input and output
   * routes to an equivalent result that uses {@link BgpRoute} instead. The former class is used by
   * the Batfish route simulation, while the latter class is the format that is used in results by
   * {@link SearchRoutePoliciesQuestion}. This method differs from the same-named method in {@link
   * TestRoutePoliciesAnswerer} because results here sometimes use symbolic values instead of
   * concrete ones, for instance for the next-hop in a route.
   *
   * @param result the original simulation result
   * @return a version of the result suitable for output from this analysis
   */
  private static Result<BgpRoute, BgpRoute> toQuestionResult(
      Result<Bgpv4Route, Bgpv4Route> result, BDDRoute bddRoute) {
    Result<BgpRoute, BgpRoute> qResult = TestRoutePoliciesAnswerer.toQuestionResult(result);

    if (result.getAction() == PERMIT) {
      qResult =
          qResult.setOutputRoute(toSymbolicBgpOutputRoute(qResult.getOutputRoute(), bddRoute));
    }
    return qResult;
  }

  /**
   * Converts a concrete {@link BgpRoute} output route that comes from route-policy simulation into
   * a version of it that is a valid result from the symbolic route analysis questions. The symbolic
   * analysis uses symbolic placeholders for data that comes from the environment, such as the IP
   * address of the local BGP session.
   *
   * @param route a concrete BGP route
   * @param bddRoute the results of symbolic analysis
   * @return a BGP route that is a valid question result
   */
  public static BgpRoute toSymbolicBgpOutputRoute(@Nullable BgpRoute route, BDDRoute bddRoute) {

    if (route == null) {
      return null;
    }
    // update the output route's next-hop if it was set to the local or remote IP;
    // rather than producing a concrete IP we use a special class that indicates that the
    // local (remote) IP is used
    return switch (bddRoute.getNextHopType()) {
      case SELF -> route.toBuilder().setNextHop(NextHopSelf.instance()).build();
      case BGP_PEER_ADDRESS ->
          route.toBuilder().setNextHop(NextHopBgpPeerAddress.instance()).build();
      default -> route;
    };
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
    if (!optNextHopIp.isPresent()) {
      return r.getFactory().one();
    }
    BDD nextHopBDD = r.getNextHop().toBDD(optNextHopIp.get());
    if (outputRoute) {
      // handle special kinds of next hops that can be set by the route map
      switch (r.getNextHopType()) {
        case DISCARDED:
          // if the next hop is discarded it can't satisfy any prefix constraints
          return r.getFactory().zero();
        case SELF:
        case BGP_PEER_ADDRESS:
          // since the local and remote IPs could in principle be anything, we
          // consider any prefix constraints to be satisfied, erring on the side of producing
          // results
          return r.getFactory().one();
        default:
          if (_direction == Environment.Direction.OUT && !r.getNextHopSet()) {
            // in the OUT direction we can only use the next-hop IP in the route
            // if the route-map explicitly sets it
            return r.getFactory().zero();
          }
      }
    }
    return nextHopBDD;
  }

  /**
   * Converts a list of {@link RegexConstraint}s about the AS-path to a BDD.
   *
   * @param regexConstraints the regex constraints
   * @param configAPs information about the AS-path atomic predicates
   * @param route the symbolic route
   * @return the BDD
   */
  private BDD asPathRegexConstraintListToBDD(
      List<RegexConstraint> regexConstraints, ConfigAtomicPredicates configAPs, BDDRoute route) {
    return TransferBDD.asPathRegexesToBDD(
        regexConstraints.stream()
            .map(RegexConstraint::getRegex)
            .map(SymbolicAsPathRegex::new)
            .collect(Collectors.toSet()),
        configAPs.getAsPathRegexAtomicPredicates().getRegexAtomicPredicates(),
        route);
  }

  /**
   * Convert a community regex constraint to a BDD.
   *
   * @param regex the user-defined regex constraint
   * @param tbdd information about the symbolic route analysis
   * @param route the symbolic route
   * @return the constraint as a BDD
   */
  private BDD communityRegexConstraintToBDD(
      RegexConstraint regex, TransferBDD tbdd, BDDRoute route, TransferBDD.Context context) {
    return switch (regex.getRegexType()) {
      case REGEX ->
          tbdd.getFactory()
              .orAll(
                  tbdd
                      .getConfigAtomicPredicates()
                      .getStandardCommunityAtomicPredicates()
                      .getRegexAtomicPredicates()
                      .get(CommunityVar.from(regex.getRegex()))
                      .stream()
                      .map(i -> route.getCommunityAtomicPredicates()[i])
                      .collect(ImmutableSet.toImmutableSet()));
      case STRUCTURE_NAME -> {
        CommunityMatchExpr cme = context.config().getCommunityMatchExprs().get(regex.getRegex());
        yield cme.accept(
            new CommunityMatchExprToBDD(),
            new CommunitySetMatchExprToBDD.Arg(tbdd, route, context));
      }
    };
  }

  /**
   * Convert community regex constraints to a BDD.
   *
   * @param regexes the user-defined regex constraints
   * @param tbdd information about the symbolic route analysis
   * @param route the symbolic route
   * @return the overall constraint as a BDD
   */
  private BDD communityRegexConstraintsToBDD(
      RegexConstraints regexes, TransferBDD tbdd, BDDRoute route, Context context) {

    BDDFactory factory = tbdd.getFactory();

    /*
     * disjoin all positive regex constraints. special case: if there are no positive
     * constraints then treat the constraint as "true", i.e. no constraints.
     */
    BDD positiveConstraints =
        regexes.getPositiveRegexConstraints().isEmpty()
            ? factory.one()
            : factory.orAll(
                regexes.getPositiveRegexConstraints().stream()
                    .map(r -> communityRegexConstraintToBDD(r, tbdd, route, context))
                    .collect(ImmutableSet.toImmutableSet()));
    // disjoin all negative regex constraints, similarly
    BDD negativeConstraints =
        factory.orAll(
            regexes.getNegativeRegexConstraints().stream()
                .map(r -> communityRegexConstraintToBDD(r, tbdd, route, context))
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
    BDDDomain<Integer> apBDDs = r.getAsPathRegexAtomicPredicates();
    return r.getFactory()
        .orAll(
            apAutomata.keySet().stream()
                .filter(i -> !apAutomata.get(i).isEmpty())
                .map(apBDDs::value)
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
      TransferBDD tbdd,
      Context context) {

    ConfigAtomicPredicates configAPs = tbdd.getConfigAtomicPredicates();

    // make sure the model we end up getting corresponds to a valid route
    BDD result = r.wellFormednessConstraints(true);

    result.andWith(prefixSpaceToBDD(constraints.getPrefix(), r, constraints.getComplementPrefix()));
    result.andWith(longSpaceToBDD(constraints.getLocalPreference(), r.getLocalPref()));
    result.andWith(longSpaceToBDD(constraints.getMed(), r.getMed()));
    result.andWith(longSpaceToBDD(constraints.getTag(), r.getTag()));
    result.andWith(communityRegexConstraintsToBDD(constraints.getCommunities(), tbdd, r, context));
    if (outputRoute) {
      // AS-path constraints on the output route need to take any prepends into account
      result.andWith(
          outputAsPathConstraintsToBDDAndUpdatedAPs(constraints.getAsPath(), configAPs, r));
    } else {
      List<RegexConstraint> pos = constraints.getAsPath().getPositiveRegexConstraints();
      List<RegexConstraint> neg = constraints.getAsPath().getNegativeRegexConstraints();
      // convert the positive and negative constraints to BDDs and return their difference;
      // if the positive constraints are empty then treat it as logically true
      result.andWith(
          (pos.isEmpty() ? r.getFactory().one() : asPathRegexConstraintListToBDD(pos, configAPs, r))
              .diffWith(asPathRegexConstraintListToBDD(neg, configAPs, r)));
    }
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
    Context context = TransferBDD.Context.forPolicy(policy);
    try {
      tbdd = new TransferBDD(configAPs);
      paths = tbdd.computePaths(policy.getStatements(), context, true);
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
            .collect(Collectors.partitioningBy(tr -> tr.getOutputRoute().getUnsupported()));
    // consider the paths that do not encounter an unsupported feature first, to avoid the potential
    // for false positives as much as possible
    List<TransferReturn> relevantPaths = pathMap.get(false);
    relevantPaths.addAll(pathMap.get(true));
    Set<PrefixSpace> blockedPrefixes = new HashSet<>();
    BDD inConstraints =
        routeConstraintsToBDD(
            _inputConstraints, new BDDRoute(tbdd.getFactory(), configAPs), false, tbdd, context);
    ImmutableList.Builder<Row> builder = ImmutableList.builder();
    for (TransferReturn path : relevantPaths) {
      BDD pathAnnouncements = path.getInputConstraints();
      BDDRoute outputRoute = path.getOutputRoute();
      BDD intersection = pathAnnouncements.and(inConstraints);
      for (PrefixSpace blockedPrefix : blockedPrefixes) {
        intersection = intersection.andWith(prefixSpaceToBDD(blockedPrefix, outputRoute, true));
      }
      // make a copy of the config atomic predicates, since the process of creating the constraints
      // on the output route can modify them, in order to handle AS-path constraints in the presence
      // of AS prepending
      ConfigAtomicPredicates outConfigAPs = new ConfigAtomicPredicates(configAPs);
      if (_action == PERMIT) {
        // incorporate the constraints on the output route as well
        BDD outConstraints =
            routeConstraintsToBDD(
                _outputConstraints, outputRoute, true, new TransferBDD(outConfigAPs), context);
        intersection = intersection.and(outConstraints);
      }

      Optional<RowAndRoute> result =
          constraintsToResult(intersection, policy, outConfigAPs, outputRoute);
      if (result.isPresent()) {
        builder.add(result.get()._row);
        if (_pathOption == PathOption.SINGLE) {
          // return the first result we find
          break;
        } else if (_pathOption == PathOption.NON_OVERLAP) {
          // modify the input constraints to not include this route anymore
          PrefixSpace prefixSpace =
              new PrefixSpace(PrefixRange.fromPrefix(result.get()._route.getNetwork()));
          blockedPrefixes.add(prefixSpace);
        }
      }
    }
    return builder.build();
  }

  /**
   * Search all of the route policies of a particular node for behaviors of interest.
   *
   * @param config the node's configuration
   * @param policies all route policies in that node
   * @return all results from analyzing those route policies
   */
  private Stream<Row> searchPoliciesForNode(Configuration config, Set<RoutingPolicy> policies) {
    ConfigAtomicPredicates configAPs =
        new ConfigAtomicPredicates(
            ImmutableList.of(new SimpleImmutableEntry<>(config, policies)),
            _communityRegexes.stream()
                .flatMap(
                    rc -> {
                      String regex = rc.getRegex();
                      return switch (rc.getRegexType()) {
                        case REGEX -> ImmutableList.of(CommunityVar.from(regex)).stream();
                        case STRUCTURE_NAME ->
                            config
                                .getCommunityMatchExprs()
                                .get(regex)
                                .accept(new CommunityMatchExprVarCollector(), config)
                                .stream();
                      };
                    })
                .collect(ImmutableSet.toImmutableSet()),
            _asPathRegexes.stream()
                .map(RegexConstraint::getRegex)
                .collect(ImmutableSet.toImmutableSet()));

    return policies.stream().flatMap(policy -> searchPolicy(policy, configAPs).stream());
  }

  /**
   * Check that all community structure names that appear in user-provided community constraints
   * refer to actual structures in each node on which this question will be run.
   *
   * @param nodes the nodes on which this question will be run
   * @param context provides access to the nodes' configurations
   */
  public void validateCommunityConstraints(Set<String> nodes, SpecifierContext context) {
    Set<String> communityMatchExprNames =
        Stream.concat(
                _inputConstraints.getCommunities().getRegexConstraints().stream(),
                _outputConstraints.getCommunities().getRegexConstraints().stream())
            .distinct()
            .filter(rc -> rc.getRegexType() == RegexConstraint.RegexType.STRUCTURE_NAME)
            .map(RegexConstraint::getRegex)
            .collect(ImmutableSet.toImmutableSet());
    nodes.forEach(
        node ->
            communityMatchExprNames.forEach(
                cme ->
                    checkArgument(
                        context.getConfigs().get(node).getCommunityMatchExprs().containsKey(cme),
                        "Node %s has no CommunityMatchExpr named %s",
                        node,
                        cme)));
  }

  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {
    SpecifierContext context = _batfish.specifierContext(snapshot);
    Set<String> nodes = _nodeSpecifier.resolve(context);
    validateCommunityConstraints(_nodeSpecifier.resolve(context), context);
    List<Row> rows =
        nodes.stream()
            .flatMap(
                node ->
                    searchPoliciesForNode(
                        context.getConfigs().get(node), _policySpecifier.resolve(node, context)))
            .collect(ImmutableList.toImmutableList());

    TableAnswerElement answerElement = new TableAnswerElement(TestRoutePoliciesAnswerer.metadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  @VisibleForTesting
  @Nonnull
  NodeSpecifier getNodeSpecifier() {
    return _nodeSpecifier;
  }

  @VisibleForTesting
  @Nonnull
  RoutingPolicySpecifier getPolicySpecifier() {
    return _policySpecifier;
  }
}
