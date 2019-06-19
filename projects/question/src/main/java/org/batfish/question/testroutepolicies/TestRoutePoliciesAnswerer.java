package org.batfish.question.testroutepolicies;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.pojo.Node;
import org.batfish.datamodel.questions.BgpRouteDiffs;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
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

  private final Direction _direction;
  private final List<Bgpv4Route> _inputRoutes;
  private final String _nodes;
  private final String _policies;

  public TestRoutePoliciesAnswerer(TestRoutePoliciesQuestion question, IBatfish batfish) {
    super(question, batfish);
    _direction = question.getDirection();
    _inputRoutes =
        question.getInputRoutes().stream()
            .map(TestRoutePoliciesAnswerer::toDataplaneBgpRoute)
            .collect(Collectors.toList());
    _nodes = question.getNodes();
    _policies = question.getPolicies();
  }

  private SortedSet<RoutingPolicyId> resolvePolicies() {
    SpecifierContext ctxt = _batfish.specifierContext();
    NodeSpecifier nodeSpec =
        SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);

    RoutingPolicySpecifier policySpec =
        SpecifierFactories.getRoutingPolicySpecifierOrDefault(_policies, ALL_ROUTING_POLICIES);

    return nodeSpec.resolve(ctxt).stream()
        .flatMap(
            node ->
                policySpec.resolve(node, ctxt).stream()
                    .map(policy -> new RoutingPolicyId(node, policy.getName())))
        .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
  }

  private Stream<Result> testPolicy(RoutingPolicy policy) {
    return _inputRoutes.stream().map(route -> testPolicy(policy, route));
  }

  private Result testPolicy(RoutingPolicy policy, Bgpv4Route inputRoute) {

    Bgpv4Route.Builder outputRoute = inputRoute.toBuilder();

    boolean permit =
        policy.process(
            inputRoute, outputRoute, null, null, Configuration.DEFAULT_VRF_NAME, _direction);
    return new Result(
        new RoutingPolicyId(policy.getOwner().getHostname(), policy.getName()),
        inputRoute,
        permit ? PERMIT : DENY,
        permit ? outputRoute.build() : null);
  }

  @Override
  public AnswerElement answer() {

    SortedSet<RoutingPolicyId> policies = resolvePolicies();
    Multiset<Row> rows =
        getResults(policies)
            .flatMap(this::testPolicy)
            .map(TestRoutePoliciesAnswerer::toRow)
            .collect(ImmutableMultiset.toImmutableMultiset());

    TableAnswerElement answerElement = new TableAnswerElement(metadata());
    answerElement.postProcessAnswer(_question, rows);
    return answerElement;
  }

  @Nonnull
  private Stream<RoutingPolicy> getResults(SortedSet<RoutingPolicyId> policies) {
    Map<String, Configuration> configs = _batfish.loadConfigurations();
    return policies.stream()
        .map(
            policyId ->
                configs.get(policyId.getNode()).getRoutingPolicies().get(policyId.getPolicy()));
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
        .setOriginType(questionsBgpRoute.getOriginType())
        .setOriginatorIp(questionsBgpRoute.getOriginatorIp())
        .setMetric(questionsBgpRoute.getMetric())
        .setLocalPreference(questionsBgpRoute.getLocalPreference())
        .setWeight(questionsBgpRoute.getWeight())
        .setNetwork(questionsBgpRoute.getNetwork())
        .setCommunities(questionsBgpRoute.getCommunities())
        .setAsPath(questionsBgpRoute.getAsPath())
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
        .setNextHopIp(dataplaneBgpRoute.getNextHopIp())
        .setProtocol(dataplaneBgpRoute.getProtocol())
        .setSrcProtocol(dataplaneBgpRoute.getSrcProtocol())
        .setOriginType(dataplaneBgpRoute.getOriginType())
        .setOriginatorIp(dataplaneBgpRoute.getOriginatorIp())
        .setMetric(dataplaneBgpRoute.getMetric())
        .setLocalPreference(dataplaneBgpRoute.getLocalPreference())
        .setWeight(dataplaneBgpRoute.getWeight())
        .setNetwork(dataplaneBgpRoute.getNetwork())
        .setCommunities(dataplaneBgpRoute.getCommunities())
        .setAsPath(dataplaneBgpRoute.getAsPath())
        .build();
  }

  @Override
  public AnswerElement answerDiff() {
    _batfish.pushBaseSnapshot();
    SortedSet<RoutingPolicyId> basePolicies = resolvePolicies();
    _batfish.popSnapshot();

    _batfish.pushDeltaSnapshot();
    SortedSet<RoutingPolicyId> deltaPolicies = resolvePolicies();
    _batfish.popSnapshot();

    SortedSet<RoutingPolicyId> policies =
        Sets.intersection(basePolicies, deltaPolicies).stream()
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));

    _batfish.pushBaseSnapshot();
    Map<Result.Key, Result> baseResults =
        getResults(policies)
            .flatMap(this::testPolicy)
            .collect(ImmutableMap.toImmutableMap(Result::getKey, Function.identity()));
    _batfish.popSnapshot();
    _batfish.pushDeltaSnapshot();
    Map<Result.Key, Result> deltaResults =
        getResults(policies)
            .flatMap(this::testPolicy)
            .collect(ImmutableMap.toImmutableMap(Result::getKey, Function.identity()));
    _batfish.popSnapshot();

    checkState(
        baseResults.keySet().equals(deltaResults.keySet()),
        "base and delta results should have the same keySets");

    Multiset<Row> rows =
        baseResults.entrySet().stream()
            .map(
                baseEntry -> {
                  Result.Key key = baseEntry.getKey();
                  Result baseResult = baseEntry.getValue();
                  Result deltaResult = deltaResults.get(key);
                  return baseResult.equals(deltaResult) ? null : toDiffRow(baseResult, deltaResult);
                })
            .filter(Objects::nonNull)
            .collect(ImmutableMultiset.toImmutableMultiset());

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
    LineAction action = result.getAction();
    Bgpv4Route outputRoute = result.getOutputRoute();
    boolean permit = action == PERMIT;
    RoutingPolicyId policyId = result.getPolicyId();
    return Row.builder()
        .put(COL_NODE, new Node(policyId.getNode()))
        .put(COL_POLICY_NAME, policyId.getPolicy())
        .put(COL_INPUT_ROUTE, inputRoute)
        .put(COL_ACTION, action)
        .put(COL_OUTPUT_ROUTE, permit ? toQuestionsBgpRoute(outputRoute) : null)
        .put(
            COL_DIFF,
            permit
                ? new BgpRouteDiffs(routeDiffs(inputRoute, toQuestionsBgpRoute(outputRoute)))
                : null)
        .build();
  }

  private static Row toDiffRow(Result baseResult, Result deltaResult) {
    checkArgument(
        baseResult.getKey().equals(deltaResult.getKey()),
        "results must be for the same policy and input route");
    Bgpv4Route baseOutputRoute = baseResult.getOutputRoute();
    Bgpv4Route deltaOutputRoute = deltaResult.getOutputRoute();
    boolean equalAction = baseResult.getAction() == deltaResult.getAction();
    boolean equalOutputRoutes = Objects.equals(baseOutputRoute, deltaOutputRoute);
    checkArgument(
        !(equalAction && equalOutputRoutes), "Results must have different action or output route");

    // delta is reference, base is current. so show diffs from delta -> base
    BgpRouteDiffs routeDiffs =
        new BgpRouteDiffs(
            routeDiffs(
                toQuestionsBgpRoute(deltaOutputRoute), toQuestionsBgpRoute(baseOutputRoute)));

    RoutingPolicyId policyId = baseResult.getPolicyId();
    Bgpv4Route inputRoute = baseResult.getInputRoute();
    return Row.builder()
        .put(COL_NODE, new Node(policyId.getNode()))
        .put(COL_POLICY_NAME, policyId.getPolicy())
        .put(COL_INPUT_ROUTE, toQuestionsBgpRoute(inputRoute))
        .put(baseColumnName(COL_ACTION), baseResult.getAction())
        .put(deltaColumnName(COL_ACTION), deltaResult.getAction())
        .put(baseColumnName(COL_OUTPUT_ROUTE), toQuestionsBgpRoute(baseResult.getOutputRoute()))
        .put(deltaColumnName(COL_OUTPUT_ROUTE), toQuestionsBgpRoute(deltaResult.getOutputRoute()))
        .put(COL_DIFF, routeDiffs)
        .build();
  }
}
