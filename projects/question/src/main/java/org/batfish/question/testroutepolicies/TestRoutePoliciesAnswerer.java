package org.batfish.question.testroutepolicies;

import static org.batfish.datamodel.LineAction.DENY;
import static org.batfish.datamodel.LineAction.PERMIT;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE;
import static org.batfish.datamodel.answers.Schema.BGP_ROUTE_DIFFS;
import static org.batfish.datamodel.answers.Schema.NODE;
import static org.batfish.datamodel.answers.Schema.STRING;
import static org.batfish.specifier.NameRegexRoutingPolicySpecifier.ALL_ROUTING_POLICIES;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Stream;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpRouteDiff;
import org.batfish.datamodel.BgpRouteDiffs;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.pojo.Node;
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
public final class TestRoutePoliciesAnswerer extends Answerer {
  public static final String COL_NODE = "Node";
  public static final String COL_POLICY_NAME = "Policy_Name";
  public static final String COL_INPUT_ROUTE = "Input_Route";
  public static final String COL_ACTION = "Action";
  public static final String COL_OUTPUT_ROUTE = "Output_Route";
  public static final String COL_DIFF = "Difference";

  private final Direction _direction;
  private final List<BgpRoute> _inputRoutes;
  private final String _nodes;
  private final String _policies;

  public TestRoutePoliciesAnswerer(TestRoutePoliciesQuestion question, IBatfish batfish) {
    super(question, batfish);
    _direction = question.getDirection();
    _inputRoutes = question.getInputRoutes();
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

  private Stream<Row> testPolicy(RoutingPolicy policy) {
    return _inputRoutes.stream().map(route -> testPolicy(policy, route));
  }

  private Row testPolicy(RoutingPolicy policy, BgpRoute inputRoute) {
    BgpRoute.Builder outputRoute = inputRoute.toBuilder();

    boolean permit =
        policy.process(
            inputRoute, outputRoute, null, null, Configuration.DEFAULT_VRF_NAME, _direction);

    return row(
        policy.getOwner().getHostname(),
        policy.getName(),
        inputRoute,
        permit ? PERMIT : DENY,
        outputRoute.build());
  }

  @Override
  public AnswerElement answer() {
    Map<String, Configuration> configs = _batfish.loadConfigurations();

    SortedSet<RoutingPolicyId> policies = resolvePolicies();
    Multiset<Row> rows =
        policies.stream()
            .map(
                policyId ->
                    configs.get(policyId.getNode()).getRoutingPolicies().get(policyId.getPolicy()))
            .flatMap(this::testPolicy)
            .collect(ImmutableMultiset.toImmutableMultiset());

    TableAnswerElement answerElement = new TableAnswerElement(metadata());
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

  private static Row row(
      String node, String policy, BgpRoute inputRoute, LineAction action, BgpRoute outputRoute) {
    boolean permit = action == PERMIT;
    return Row.builder()
        .put(COL_NODE, new Node(node))
        .put(COL_POLICY_NAME, policy)
        .put(COL_INPUT_ROUTE, inputRoute)
        .put(COL_ACTION, action)
        .put(COL_OUTPUT_ROUTE, permit ? outputRoute : null)
        .put(
            COL_DIFF,
            permit ? new BgpRouteDiffs(BgpRouteDiff.routeDiffs(inputRoute, outputRoute)) : null)
        .build();
  }
}
