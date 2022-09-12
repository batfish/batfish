package org.batfish.question.testroutepolicies;

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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.Schema;
import org.batfish.datamodel.pojo.Node;
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
  public static final String COL_INPUT_ROUTE = "Input_Route";
  public static final String COL_ACTION = "Action";
  public static final String COL_OUTPUT_ROUTE = "Output_Route";
  public static final String COL_DIFF = "Difference";
  public static final String COL_TRACE = "Trace";

  @Nonnull private final Direction _direction;
  @Nonnull private final List<Bgpv4Route> _inputRoutes;
  @Nonnull private final NodeSpecifier _nodeSpecifier;
  @Nonnull private final RoutingPolicySpecifier _policySpecifier;

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
  }

  private SortedSet<RoutingPolicyId> resolvePolicies(SpecifierContext context) {
    return _nodeSpecifier.resolve(context).stream()
        .flatMap(
            node ->
                _policySpecifier.resolve(node, context).stream()
                    .map(policy -> new RoutingPolicyId(node, policy.getName())))
        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
  }

  public Result getResult(SpecifierContext context, Result.Key key, Direction direction) {
    RoutingPolicyId policyId = key.getPolicyId();
    RoutingPolicy policy =
        context.getConfigs().get(policyId.getNode()).getRoutingPolicies().get(policyId.getPolicy());
    return testPolicy(policy, key.getInputRoute(), direction);
  }

  /**
   * Produce the results of simulating the given route policy on the given input route.
   *
   * @param policy the route policy to simulate
   * @param inputRoute the input route for the policy
   * @param direction whether the policy is used on import or export (IN or OUT)
   * @return a table row containing the results of the simulation
   */
  public static Row rowResultFor(RoutingPolicy policy, Bgpv4Route inputRoute, Direction direction) {
    return toRow(testPolicy(policy, inputRoute, direction));
  }

  private static Result testPolicy(
      RoutingPolicy policy, Bgpv4Route inputRoute, Direction direction) {

    Bgpv4Route.Builder outputRoute = inputRoute.toBuilder();
    if (direction == Direction.OUT) {
      // when simulating a route policy in the OUT direction, the output route's next hop IP must be
      // unset by default (checked by Environment::build)
      outputRoute.setNextHopIp(Route.UNSET_ROUTE_NEXT_HOP_IP);
    }
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    boolean permit = policy.process(inputRoute, outputRoute, direction, tracer);
    tracer.endSubTrace();
    return new Result(
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
    List<Result.Key> tasks =
        resolvePolicies(context).stream()
            .flatMap(rpid -> _inputRoutes.stream().map(r -> new Key(rpid, r)))
            .collect(ImmutableList.toImmutableList());

    List<Row> rows =
        tasks.parallelStream()
            .map(key -> getResult(context, key, _direction))
            .map(TestRoutePoliciesAnswerer::toRow)
            .collect(ImmutableList.toImmutableList());

    TableAnswerElement answerElement = new TableAnswerElement(metadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  @Nullable
  private static Bgpv4Route toDataplaneBgpRoute(
      @Nullable org.batfish.datamodel.questions.BgpRoute questionsBgpRoute) {
    if (questionsBgpRoute == null) {
      return null;
    }
    return Bgpv4Route.builder()
        .setWeight(questionsBgpRoute.getWeight())
        .setNextHopIp(questionsBgpRoute.getNextHopIp())
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
        .setCommunities(questionsBgpRoute.getCommunities())
        .setAsPath(questionsBgpRoute.getAsPath())
        .setReceivedFrom(ReceivedFromSelf.instance()) // TODO: support receivedFrom in input route
        .build();
  }

  @Nullable
  private static org.batfish.datamodel.questions.BgpRoute toQuestionsBgpRoute(
      @Nullable Bgpv4Route dataplaneBgpRoute) {
    if (dataplaneBgpRoute == null) {
      return null;
    }
    return org.batfish.datamodel.questions.BgpRoute.builder()
        .setWeight(dataplaneBgpRoute.getWeight())
        // TODO: The next-hop IP AUTO/NONE (Ip.AUTO) is used to denote multiple different things;
        // we should distinguish these uses clearly from one another in the results returned by this
        // question. If the simulated route map has direction OUT, AUTO/NONE indicates that the
        // route map does not explicitly set the next hop.  If the simulated route map has direction
        // IN, AUTO/NONE can indicate that the route is explicitly discarded by the route map, but
        // it is also used in other situations (see AbstractRoute::NEXT_HOP_IP_EXTRACTOR).
        .setNextHopIp(dataplaneBgpRoute.getNextHopIp())
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
    List<Result.Key> tasks =
        policiesToTest.stream()
            .flatMap(rpid -> _inputRoutes.stream().map(r -> new Key(rpid, r)))
            .collect(ImmutableList.toImmutableList());

    List<Row> rows =
        tasks.parallelStream()
            .map(
                key -> {
                  Result snapshotResult = getResult(context, key, _direction);
                  Result referenceResult = getResult(referenceCtx, key, _direction);
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

  private static Row toRow(Result result) {
    org.batfish.datamodel.questions.BgpRoute inputRoute =
        toQuestionsBgpRoute(result.getInputRoute());
    org.batfish.datamodel.questions.BgpRoute outputRoute =
        toQuestionsBgpRoute(result.getOutputRoute());
    LineAction action = result.getAction();
    boolean permit = action == PERMIT;
    RoutingPolicyId policyId = result.getPolicyId();
    return Row.builder()
        .put(COL_NODE, new Node(policyId.getNode()))
        .put(COL_POLICY_NAME, policyId.getPolicy())
        .put(COL_INPUT_ROUTE, inputRoute)
        .put(COL_ACTION, action)
        .put(COL_OUTPUT_ROUTE, permit ? outputRoute : null)
        .put(COL_DIFF, permit ? new BgpRouteDiffs(routeDiffs(inputRoute, outputRoute)) : null)
        .put(COL_TRACE, result.getTrace())
        .build();
  }

  private static @Nullable Row toDiffRow(Result snapshotResult, Result referenceResult) {
    assert snapshotResult.getKey().equals(referenceResult.getKey());

    if (snapshotResult.equals(referenceResult)) {
      return null;
    }

    org.batfish.datamodel.questions.BgpRoute snapshotOutputRoute =
        toQuestionsBgpRoute(snapshotResult.getOutputRoute());
    org.batfish.datamodel.questions.BgpRoute referenceOutputRoute =
        toQuestionsBgpRoute(referenceResult.getOutputRoute());

    boolean equalAction = snapshotResult.getAction() == referenceResult.getAction();
    boolean equalOutputRoutes = Objects.equals(snapshotOutputRoute, referenceOutputRoute);
    assert !(equalAction && equalOutputRoutes);

    BgpRouteDiffs routeDiffs =
        new BgpRouteDiffs(routeDiffs(referenceOutputRoute, snapshotOutputRoute));

    RoutingPolicyId policyId = snapshotResult.getPolicyId();
    Bgpv4Route inputRoute = snapshotResult.getInputRoute();
    return Row.builder()
        .put(COL_NODE, new Node(policyId.getNode()))
        .put(COL_POLICY_NAME, policyId.getPolicy())
        .put(COL_INPUT_ROUTE, toQuestionsBgpRoute(inputRoute))
        .put(baseColumnName(COL_ACTION), snapshotResult.getAction())
        .put(deltaColumnName(COL_ACTION), referenceResult.getAction())
        .put(baseColumnName(COL_OUTPUT_ROUTE), snapshotOutputRoute)
        .put(deltaColumnName(COL_OUTPUT_ROUTE), referenceOutputRoute)
        .put(COL_DIFF, routeDiffs)
        .build();
  }

  @Nonnull
  @VisibleForTesting
  RoutingPolicySpecifier getPolicySpecifier() {
    return _policySpecifier;
  }

  @Nonnull
  @VisibleForTesting
  NodeSpecifier getNodeSpecifier() {
    return _nodeSpecifier;
  }
}
