package org.batfish.question.testroutepolicies;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.BgpRouteUtil.convertNonBgpRouteToBgpRoute;
import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE_DIFFS;
import static org.batfish.datamodel.answers.Schema.NODE;
import static org.batfish.datamodel.answers.Schema.STRING;
import static org.batfish.datamodel.questions.BgpRouteDiff.routeDiffs;
import static org.batfish.datamodel.table.TableDiff.baseColumnName;
import static org.batfish.datamodel.table.TableDiff.deltaColumnName;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.NextHopConcrete;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpRouteDiffs;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.trace.Tracer;
import org.batfish.question.testroutepolicies.Result.Key;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoutingPolicySpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

/** An answerer for {@link TestRoutePoliciesQuestion}. */
@ParametersAreNonnullByDefault
public final class TestRoutePoliciesAnswerer extends Answerer {
  public static final String COL_NODE = "Node";
  public static final String COL_POLICY_NAME = "Policy_Name";
  public static final String COL_REFERENCE_POLICY_NAME = "Reference_Policy_Name";
  public static final String COL_INPUT_ROUTE = "Input_Route";
  public static final String COL_ACTION = "Action";
  public static final String COL_OUTPUT_ROUTE = "Output_Route";
  public static final String COL_DIFF = "Difference";
  public static final String COL_TRACE = "Trace";

  private final @Nonnull Direction _direction;
  private final @Nonnull List<Bgpv4Route> _inputRoutes;
  private final @Nonnull NodeSpecifier _nodeSpecifier;
  private final @Nonnull RoutingPolicySpecifier _policySpecifier;

  private final @Nullable BgpSessionProperties _bgpSessionProperties;

  public TestRoutePoliciesAnswerer(TestRoutePoliciesQuestion question, IBatfish batfish) {
    super(question, batfish);
    _direction = question.getDirection();
    _inputRoutes =
        question.getInputRoutes().stream()
            .map(TestRoutePoliciesAnswerer::toDataplaneBgpRoute)
            .collect(Collectors.toList());
    _nodeSpecifier =
        SpecifierFactories.getNodeSpecifierOrDefault(
            question.getNodes(), AllNodesNodeSpecifier.INSTANCE);
    _policySpecifier =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(
            question.getPolicies(), ALL_ROUTING_POLICIES);
    org.batfish.datamodel.questions.BgpSessionProperties properties =
        question.getBgpSessionProperties();
    _bgpSessionProperties =
        properties == null
            ? null
            : BgpSessionProperties.builder()
                .setLocalAs(properties.getLocalAs())
                .setRemoteAs(properties.getRemoteAs())
                .setLocalIp(properties.getLocalIp())
                .setRemoteIp(properties.getRemoteIp())
                .build();
  }

  private SortedSet<RoutingPolicyId> resolvePolicies(SpecifierContext context) {
    return _nodeSpecifier.resolve(context).stream()
        .flatMap(
            node ->
                _policySpecifier.resolve(node, context).stream()
                    .map(policy -> new RoutingPolicyId(node, policy.getName())))
        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
  }

  public Result<Bgpv4Route, Bgpv4Route> getResult(
      SpecifierContext context, Result.Key<Bgpv4Route> key, Direction direction) {
    RoutingPolicyId policyId = key.getPolicyId();
    RoutingPolicy policy =
        context.getConfigs().get(policyId.getNode()).getRoutingPolicies().get(policyId.getPolicy());
    return simulatePolicyWithBgpRoute(policy, key.getInputRoute(), direction);
  }

  /**
   * Produce the results of simulating the given route policy on the given route.
   *
   * @param policy the route policy to simulate
   * @param inputRoute the input route for the policy
   * @param properties the properties of the Bgp session being simulated
   * @param direction whether the policy is used on import or export (IN or OUT)
   * @param successfulTrack a predicate that indicates which tracks are successful
   * @param sourceVrf an optional name of the source VRF
   * @return the results of the simulation
   */
  public static Result<? extends AbstractRoute, Bgpv4Route> simulatePolicy(
      RoutingPolicy policy,
      AbstractRoute inputRoute,
      @Nullable BgpSessionProperties properties,
      Direction direction,
      @Nullable Predicate<String> successfulTrack,
      @Nullable String sourceVrf) {
    switch (inputRoute.getProtocol()) {
      case STATIC:
        return simulatePolicyWithStaticRoute(
            policy, (StaticRoute) inputRoute, direction, successfulTrack, sourceVrf);
      case AGGREGATE:
      case BGP:
      case IBGP:
        return simulatePolicyWithBgpRoute(
            policy, (Bgpv4Route) inputRoute, properties, direction, successfulTrack, sourceVrf);
      default:
        throw new IllegalArgumentException("Unexpected route protocol " + inputRoute.getProtocol());
    }
  }

  /**
   * Produce the results of simulating the given route policy on the given BGP route.
   *
   * @param policy the route policy to simulate
   * @param inputRoute the input route for the policy
   * @param direction whether the policy is used on import or export (IN or OUT)
   * @return the results of the simulation
   */
  private Result<Bgpv4Route, Bgpv4Route> simulatePolicyWithBgpRoute(
      RoutingPolicy policy, Bgpv4Route inputRoute, Direction direction) {
    return simulatePolicyWithBgpRoute(
        policy, inputRoute, _bgpSessionProperties, direction, null, null);
  }

  /**
   * Produce the results of simulating the given route policy on the given BGP route.
   *
   * @param policy the route policy to simulate
   * @param inputRoute the input route for the policy
   * @param properties the properties of the Bgp session being simulated
   * @param direction whether the policy is used on import or export (IN or OUT)
   * @param successfulTrack a predicate that indicates which tracks are successful
   * @param sourceVrf an optional name of the source VRF
   * @return the results of the simulation
   */
  public static Result<Bgpv4Route, Bgpv4Route> simulatePolicyWithBgpRoute(
      RoutingPolicy policy,
      Bgpv4Route inputRoute,
      @Nullable BgpSessionProperties properties,
      Direction direction,
      @Nullable Predicate<String> successfulTrack,
      @Nullable String sourceVrf) {
    return processPolicy(
        policy,
        inputRoute,
        inputRoute.toBuilder(),
        properties,
        direction,
        successfulTrack,
        sourceVrf);
  }

  /**
   * Produce the results of simulating the given route policy on the given static route.
   *
   * @param policy the route policy to simulate
   * @param inputRoute the input route for the policy
   * @param direction whether the policy is used on import or export (IN or OUT)
   * @param successfulTrack a predicate that indicates which tracks are successful
   * @param sourceVrf an optional name of the source VRF
   * @return the results of the simulation
   */
  public static Result<StaticRoute, Bgpv4Route> simulatePolicyWithStaticRoute(
      RoutingPolicy policy,
      StaticRoute inputRoute,
      Direction direction,
      @Nullable Predicate<String> successfulTrack,
      @Nullable String sourceVrf) {

    /*
     * TODO: Using default values for these parameters; if the results of simulation depends on them
     * then we may require additional information from the caller of our method.
     * A few other notes: 1) We are using the static route's next hop IP as the next hop IP for the BGP route.
     * 2) The local preference of the BGP route will be set to the default value of 100.
     */
    Bgpv4Route.Builder outputRoute =
        convertNonBgpRouteToBgpRoute(
            inputRoute,
            Ip.ZERO,
            inputRoute.getNextHopIp(),
            0,
            RoutingProtocol.BGP,
            OriginMechanism.NETWORK);

    return processPolicy(
        policy, inputRoute, outputRoute, null, direction, successfulTrack, sourceVrf);
  }

  /**
   * Produce the results of simulating the given route policy on the given input route.
   *
   * @param <I> the type of the input route
   * @param policy the route policy to simulate
   * @param inputRoute the input route for the simulation
   * @param outputRoute the output route builder for the simulation
   * @param properties the properties of the BGP session being simulated
   * @param direction whether the policy is used on import or export (IN or OUT)
   * @param successfulTrack a predicate that indicates which tracks are successful
   * @param sourceVrf an optional name of the source VRF
   * @return the results of the simulation
   */
  private static <I extends AbstractRoute> Result<I, Bgpv4Route> processPolicy(
      RoutingPolicy policy,
      I inputRoute,
      Bgpv4Route.Builder outputRoute,
      @Nullable BgpSessionProperties properties,
      Direction direction,
      @Nullable Predicate<String> successfulTrack,
      @Nullable String sourceVrf) {

    if (direction == Direction.OUT) {
      // when simulating a route policy in the OUT direction, the output route's next hop IP must be
      // unset by default (checked by Environment::build)
      outputRoute.setNextHopIp(Route.UNSET_ROUTE_NEXT_HOP_IP);
    }
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    boolean permit =
        policy.process(
            // include the source VRF in the route if it is not null
            sourceVrf == null ? inputRoute : new AnnotatedRoute<>(inputRoute, sourceVrf),
            outputRoute,
            properties,
            direction,
            successfulTrack,
            tracer);
    tracer.endSubTrace();
    return new Result<>(
        new RoutingPolicyId(policy.getOwner().getHostname(), policy.getName()),
        inputRoute,
        permit ? PERMIT : DENY,
        permit ? outputRoute.build() : null,
        tracer.getTrace());
  }

  @Override
  public TableAnswerElement answer(NetworkSnapshot snapshot) {
    SpecifierContext context = _batfish.specifierContext(snapshot);

    // Materialized for efficient parallelism.
    List<Result.Key<Bgpv4Route>> tasks =
        resolvePolicies(context).stream()
            .flatMap(rpid -> _inputRoutes.stream().map(r -> new Key<>(rpid, r)))
            .collect(ImmutableList.toImmutableList());

    List<Row> rows =
        tasks.parallelStream()
            .map(key -> getResult(context, key, _direction))
            .map(TestRoutePoliciesAnswerer::toQuestionResult)
            .map(TestRoutePoliciesAnswerer::toRow)
            .collect(ImmutableList.toImmutableList());

    TableAnswerElement answerElement = new TableAnswerElement(metadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  private static @Nullable Bgpv4Route toDataplaneBgpRoute(
      @Nullable org.batfish.datamodel.questions.BgpRoute questionsBgpRoute) {
    if (questionsBgpRoute == null) {
      return null;
    }
    checkArgument(
        questionsBgpRoute.getNextHop() instanceof NextHopConcrete,
        "Unexpected next-hop: " + questionsBgpRoute.getNextHop());

    // We set the administrative distance to the default value chosen by BgpRoute. The more
    // principled thing to do would be to use the value based on the vendor but since this is only
    // used to convert the input route to the internal representation and AD is never matched as a
    // field the default value does not really matter.
    return Bgpv4Route.builder()
        .setAdmin(firstNonNull(questionsBgpRoute.getAdminDist(), BgpRoute.DEFAULT_AD))
        .setWeight(questionsBgpRoute.getWeight())
        .setProtocol(questionsBgpRoute.getProtocol())
        .setSrcProtocol(questionsBgpRoute.getSrcProtocol())
        .setOriginMechanism(questionsBgpRoute.getOriginMechanism())
        .setOriginType(questionsBgpRoute.getOriginType())
        .setOriginatorIp(questionsBgpRoute.getOriginatorIp())
        .setPathId(questionsBgpRoute.getPathId())
        .setMetric(questionsBgpRoute.getMetric())
        .setLocalPreference(questionsBgpRoute.getLocalPreference())
        .setTag(questionsBgpRoute.getTag())
        .setTunnelEncapsulationAttribute(questionsBgpRoute.getTunnelEncapsulationAttribute())
        .setNetwork(questionsBgpRoute.getNetwork())
        .setNextHop(((NextHopConcrete) questionsBgpRoute.getNextHop()).getNextHop())
        .setCommunities(questionsBgpRoute.getCommunities())
        .setAsPath(questionsBgpRoute.getAsPath())
        .setReceivedFrom(ReceivedFromSelf.instance()) // TODO: support receivedFrom in input route
        .build();
  }

  /**
   * Convert a {@link Bgpv4Route} to an equivalent {@link BgpRoute}. The former class is used by the
   * Batfish route simulation, while the latter class is the format that is used in results by
   * {@link TestRoutePoliciesQuestion} and other route-policy analysis questions.
   *
   * @param dataplaneBgpRoute the original route
   * @return a version of the route suitable for output from this analysis
   */
  public static @Nullable org.batfish.datamodel.questions.BgpRoute toQuestionBgpRoute(
      @Nullable Bgpv4Route dataplaneBgpRoute) {
    if (dataplaneBgpRoute == null) {
      return null;
    }
    return org.batfish.datamodel.questions.BgpRoute.builder()
        .setAdminDist(dataplaneBgpRoute.getAdministrativeCost())
        .setWeight(dataplaneBgpRoute.getWeight())
        // TODO: The class NextHopDiscard is used to denote multiple different things;
        // we should distinguish these uses clearly from one another in the results returned by this
        // question. If the simulated route map has direction OUT, AUTO/NONE indicates that the
        // route map does not explicitly set the next hop.  If the simulated route map has direction
        // IN, AUTO/NONE can indicate that the route is explicitly discarded by the route map, but
        // it is also used in other situations (see AbstractRoute::NEXT_HOP_IP_EXTRACTOR).
        .setNextHopConcrete(dataplaneBgpRoute.getNextHop())
        .setProtocol(dataplaneBgpRoute.getProtocol())
        .setSrcProtocol(dataplaneBgpRoute.getSrcProtocol())
        .setOriginMechanism(dataplaneBgpRoute.getOriginMechanism())
        .setOriginType(dataplaneBgpRoute.getOriginType())
        .setOriginatorIp(dataplaneBgpRoute.getOriginatorIp())
        .setPathId(dataplaneBgpRoute.getPathId())
        .setMetric(dataplaneBgpRoute.getMetric())
        .setLocalPreference(dataplaneBgpRoute.getLocalPreference())
        .setTag(dataplaneBgpRoute.getTag())
        .setTunnelEncapsulationAttribute(dataplaneBgpRoute.getTunnelEncapsulationAttribute())
        .setNetwork(dataplaneBgpRoute.getNetwork())
        .setCommunities(dataplaneBgpRoute.getCommunities().getCommunities())
        .setClusterList(dataplaneBgpRoute.getClusterList())
        .setAsPath(dataplaneBgpRoute.getAsPath())
        .build();
  }

  @Override
  public AnswerElement answerDiff(NetworkSnapshot snapshot, NetworkSnapshot reference) {
    SpecifierContext context = _batfish.specifierContext(snapshot);
    SpecifierContext referenceCtx = _batfish.specifierContext(reference);

    // Only test policies that exist in both snapshots.
    Set<RoutingPolicyId> policiesToTest =
        Sets.intersection(resolvePolicies(context), resolvePolicies(referenceCtx));

    // Materialized for efficient parallelism.
    List<Result.Key<Bgpv4Route>> tasks =
        policiesToTest.stream()
            .flatMap(rpid -> _inputRoutes.stream().map(r -> new Key<>(rpid, r)))
            .collect(ImmutableList.toImmutableList());

    List<Row> rows =
        tasks.parallelStream()
            .map(
                key -> {
                  Result<Bgpv4Route, Bgpv4Route> snapshotResult =
                      getResult(context, key, _direction);
                  Result<Bgpv4Route, Bgpv4Route> referenceResult =
                      getResult(referenceCtx, key, _direction);
                  return toDiffRow(snapshotResult, referenceResult);
                })
            .filter(Objects::nonNull)
            .collect(ImmutableList.toImmutableList());

    TableAnswerElement answerElement = new TableAnswerElement(diffMetadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  public static TableMetadata metadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, NODE, "The node that has the policy", true, false),
            new ColumnMetadata(COL_POLICY_NAME, STRING, "The name of this policy", true, false),
            new ColumnMetadata(COL_INPUT_ROUTE, BGP_ROUTE, "The input route", true, false),
            new ColumnMetadata(
                COL_ACTION, STRING, "The action of the policy on the input route", false, true),
            new ColumnMetadata(
                COL_OUTPUT_ROUTE, BGP_ROUTE, "The output route, if any", false, true),
            new ColumnMetadata(
                COL_DIFF,
                BGP_ROUTE_DIFFS,
                "The difference between the input and output routes, if any",
                false,
                true),
            new ColumnMetadata(
                COL_TRACE,
                Schema.list(Schema.TRACE_TREE),
                "Route policy trace that shows which clauses/terms matched the input route. If the"
                    + " trace is empty, either nothing matched or tracing is not yet been"
                    + " implemented for this policy type. This is an experimental feature whose"
                    + " content and format is subject to change.",
                false,
                true));
    return new TableMetadata(
        columnMetadata, String.format("Results for route ${%s}", COL_INPUT_ROUTE));
  }

  public static TableMetadata diffMetadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, NODE, "The node that has the policy", true, false),
            new ColumnMetadata(COL_POLICY_NAME, STRING, "The name of this policy", true, false),
            new ColumnMetadata(COL_INPUT_ROUTE, BGP_ROUTE, "The input route", true, false),
            new ColumnMetadata(
                baseColumnName(COL_ACTION),
                STRING,
                "The action of the policy on the input route",
                false,
                true),
            new ColumnMetadata(
                deltaColumnName(COL_ACTION),
                STRING,
                "The action of the policy on the input route",
                false,
                true),
            new ColumnMetadata(
                baseColumnName(COL_OUTPUT_ROUTE),
                BGP_ROUTE,
                "The output route, if any",
                false,
                true),
            new ColumnMetadata(
                deltaColumnName(COL_OUTPUT_ROUTE),
                BGP_ROUTE,
                "The output route, if any",
                false,
                true),
            new ColumnMetadata(
                COL_DIFF,
                BGP_ROUTE_DIFFS,
                "The difference between the output routes",
                false,
                true));
    return new TableMetadata(
        columnMetadata, String.format("Results for route ${%s}", COL_INPUT_ROUTE));
  }

  public static TableMetadata compareMetadata() {
    List<ColumnMetadata> columnMetadata =
        ImmutableList.of(
            new ColumnMetadata(COL_NODE, NODE, "The node that has the policy", true, false),
            new ColumnMetadata(COL_POLICY_NAME, STRING, "The name of this policy", true, false),
            new ColumnMetadata(
                COL_REFERENCE_POLICY_NAME, STRING, "The name of the proposed policy", true, false),
            new ColumnMetadata(COL_INPUT_ROUTE, BGP_ROUTE, "The input route", true, false),
            new ColumnMetadata(
                baseColumnName(COL_ACTION),
                STRING,
                "The action of the policy on the input route",
                false,
                true),
            new ColumnMetadata(
                deltaColumnName(COL_ACTION),
                STRING,
                "The action of the policy on the input route",
                false,
                true),
            new ColumnMetadata(
                baseColumnName(COL_OUTPUT_ROUTE),
                BGP_ROUTE,
                "The output route, if any",
                false,
                true),
            new ColumnMetadata(
                deltaColumnName(COL_OUTPUT_ROUTE),
                BGP_ROUTE,
                "The output route, if any",
                false,
                true),
            new ColumnMetadata(
                baseColumnName(COL_TRACE),
                Schema.list(Schema.TRACE_TREE),
                "Route policy trace that shows which clauses/terms matched the input route. If the"
                    + " trace is empty, either nothing matched or tracing is not yet been"
                    + " implemented for this policy type. This is an experimental feature whose"
                    + " content and format is subject to change.",
                false,
                true),
            new ColumnMetadata(
                deltaColumnName(COL_TRACE),
                Schema.list(Schema.TRACE_TREE),
                "Route policy trace that shows which clauses/terms matched the input route. If the"
                    + " trace is empty, either nothing matched or tracing is not yet been"
                    + " implemented for this policy type. This is an experimental feature whose"
                    + " content and format is subject to change.",
                false,
                true),
            new ColumnMetadata(
                COL_DIFF,
                BGP_ROUTE_DIFFS,
                "The difference between the output routes",
                false,
                true));
    return new TableMetadata(
        columnMetadata, String.format("Results for route ${%s}", COL_INPUT_ROUTE));
  }

  /**
   * Converts a simulation result that uses {@link Bgpv4Route} to represent the input and output
   * routes to an equivalent result that uses {@link BgpRoute} instead. The former class is used by
   * the Batfish route simulation, while the latter class is the format that is used in results by
   * {@link TestRoutePoliciesQuestion}.
   *
   * @param result the original simulation result
   * @return a version of the result suitable for output from this analysis
   */
  public static Result<BgpRoute, BgpRoute> toQuestionResult(Result<Bgpv4Route, Bgpv4Route> result) {
    return new Result<>(
        result.getPolicyId(),
        toQuestionBgpRoute(result.getInputRoute()),
        result.getAction(),
        toQuestionBgpRoute(result.getOutputRoute()),
        result.getTrace());
  }

  public static Row toRow(Result<BgpRoute, BgpRoute> result) {
    org.batfish.datamodel.questions.BgpRoute inputRoute = result.getInputRoute();
    org.batfish.datamodel.questions.BgpRoute outputRoute = result.getOutputRoute();
    LineAction action = result.getAction();
    boolean permit = action == PERMIT;
    RoutingPolicyId policyId = result.getPolicyId();
    return Row.builder()
        .put(COL_NODE, new Node(policyId.getNode()))
        .put(COL_POLICY_NAME, policyId.getPolicy())
        .put(COL_INPUT_ROUTE, inputRoute)
        .put(COL_ACTION, action)
        .put(COL_OUTPUT_ROUTE, permit ? outputRoute : null)
        .put(COL_DIFF, permit ? routeDiffs(inputRoute, outputRoute) : null)
        .put(COL_TRACE, result.getTrace())
        .build();
  }

  /**
   * Returns a {@link Row} describing the difference between the two results, in the case where the
   * actual outcome of the policy is different, or {@code null} otherwise.
   *
   * <p>Note that no difference in the outcome includes the case when, e.g., only the name of a
   * policy changed but the action and output attributes are identical across the two snapshots.
   */
  @VisibleForTesting
  static @Nullable Row toDiffRow(
      Result<Bgpv4Route, Bgpv4Route> snapshotResult,
      Result<Bgpv4Route, Bgpv4Route> referenceResult) {
    assert snapshotResult.getKey().equals(referenceResult.getKey());

    if (snapshotResult.equals(referenceResult)) {
      return null;
    }

    org.batfish.datamodel.questions.BgpRoute snapshotOutputRoute =
        toQuestionBgpRoute(snapshotResult.getOutputRoute());
    org.batfish.datamodel.questions.BgpRoute referenceOutputRoute =
        toQuestionBgpRoute(referenceResult.getOutputRoute());

    boolean equalAction = snapshotResult.getAction() == referenceResult.getAction();
    boolean equalOutputRoutes = Objects.equals(snapshotOutputRoute, referenceOutputRoute);
    if (equalAction && equalOutputRoutes) {
      // This can happen if the trace is different.
      return null;
    }

    BgpRouteDiffs routeDiffs = routeDiffs(referenceOutputRoute, snapshotOutputRoute);

    RoutingPolicyId policyId = snapshotResult.getPolicyId();
    Bgpv4Route inputRoute = snapshotResult.getInputRoute();
    return Row.builder()
        .put(COL_NODE, new Node(policyId.getNode()))
        .put(COL_POLICY_NAME, policyId.getPolicy())
        .put(COL_INPUT_ROUTE, toQuestionBgpRoute(inputRoute))
        .put(baseColumnName(COL_ACTION), snapshotResult.getAction())
        .put(deltaColumnName(COL_ACTION), referenceResult.getAction())
        .put(baseColumnName(COL_OUTPUT_ROUTE), snapshotOutputRoute)
        .put(deltaColumnName(COL_OUTPUT_ROUTE), referenceOutputRoute)
        .put(COL_DIFF, routeDiffs)
        .build();
  }

  /**
   * @param referenceResult the result from the reference snapshot
   * @param snapshotResult the result from the current snapshot.
   * @return A row that includes the comparison of the two results.
   */
  public static @Nullable Row toCompareRow(
      Result<BgpRoute, BgpRoute> snapshotResult, Result<BgpRoute, BgpRoute> referenceResult) {

    if (referenceResult.equals(snapshotResult)) {
      return null;
    }

    org.batfish.datamodel.questions.BgpRoute referenceOutputRoute =
        referenceResult.getOutputRoute();
    org.batfish.datamodel.questions.BgpRoute snapshotOutputRoute = snapshotResult.getOutputRoute();

    boolean equalAction = referenceResult.getAction() == snapshotResult.getAction();
    boolean equalOutputRoutes = Objects.equals(referenceOutputRoute, snapshotOutputRoute);
    assert !(equalAction && equalOutputRoutes);

    BgpRouteDiffs routeDiffs = routeDiffs(referenceOutputRoute, snapshotOutputRoute);

    RoutingPolicyId referencePolicyId = referenceResult.getPolicyId();
    RoutingPolicyId policyId = snapshotResult.getPolicyId();
    BgpRoute inputRoute = referenceResult.getInputRoute();
    return Row.builder()
        .put(COL_NODE, new Node(policyId.getNode()))
        .put(COL_POLICY_NAME, policyId.getPolicy())
        .put(COL_REFERENCE_POLICY_NAME, referencePolicyId.getPolicy())
        .put(COL_INPUT_ROUTE, inputRoute)
        .put(deltaColumnName(COL_ACTION), referenceResult.getAction())
        .put(baseColumnName(COL_ACTION), snapshotResult.getAction())
        .put(deltaColumnName(COL_OUTPUT_ROUTE), referenceOutputRoute)
        .put(baseColumnName(COL_OUTPUT_ROUTE), snapshotOutputRoute)
        .put(deltaColumnName(COL_TRACE), referenceResult.getTrace())
        .put(baseColumnName(COL_TRACE), snapshotResult.getTrace())
        .put(COL_DIFF, routeDiffs)
        .build();
  }

  @VisibleForTesting
  @Nonnull
  RoutingPolicySpecifier getPolicySpecifier() {
    return _policySpecifier;
  }

  @VisibleForTesting
  @Nonnull
  NodeSpecifier getNodeSpecifier() {
    return _nodeSpecifier;
  }
}
