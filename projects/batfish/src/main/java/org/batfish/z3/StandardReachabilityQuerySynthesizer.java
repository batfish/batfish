package org.batfish.z3;

import static org.batfish.z3.AclLineMatchExprToBooleanExpr.NO_ACLS_NO_IP_SPACES_NO_SOURCES_ORIG_HEADERSPACE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.question.SrcNattedConstraint;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.DropAclIn;
import org.batfish.z3.state.DropAclOut;
import org.batfish.z3.state.DropNoRoute;
import org.batfish.z3.state.DropNullRoute;
import org.batfish.z3.state.NeighborUnreachableOrExitsNetwork;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.NodeNeighborUnreachableOrExitsNetwork;
import org.batfish.z3.state.Query;
import org.batfish.z3.state.visitors.DefaultTransitionGenerator;

public class StandardReachabilityQuerySynthesizer extends ReachabilityQuerySynthesizer {

  public static class Builder
      extends ReachabilityQuerySynthesizer.Builder<
          StandardReachabilityQuerySynthesizer, StandardReachabilityQuerySynthesizer.Builder> {

    protected Set<FlowDisposition> _actions;

    protected Set<String> _finalNodes;

    protected Builder() {
      _actions = ImmutableSet.of();
      _finalNodes = ImmutableSet.of();
    }

    @Override
    public StandardReachabilityQuerySynthesizer build() {
      return new StandardReachabilityQuerySynthesizer(
          _actions,
          _headerSpace,
          _finalNodes,
          _srcIpConstraints,
          _srcNatted,
          _requiredTransitNodes,
          _forbiddenTransitNodes);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public Builder setActions(Set<FlowDisposition> actions) {
      _actions = actions;
      return this;
    }

    @Override
    public Builder setFinalNodes(Set<String> finalNodes) {
      _finalNodes = finalNodes;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  protected final Set<FlowDisposition> _actions;

  protected final Set<String> _finalNodes;

  protected StandardReachabilityQuerySynthesizer(
      @Nonnull Set<FlowDisposition> actions,
      @Nonnull AclLineMatchExpr headerSpace,
      @Nonnull Set<String> finalNodes,
      @Nonnull Map<IngressLocation, BooleanExpr> srcIpConstraints,
      @Nonnull SrcNattedConstraint srcNatted,
      @Nonnull Set<String> transitNodes,
      @Nonnull Set<String> nonTransitNodes) {
    super(headerSpace, srcIpConstraints, srcNatted, transitNodes, nonTransitNodes);
    _actions = actions;
    _finalNodes = finalNodes;
  }

  /** Create query condition for action at final node(s) */
  protected List<StateExpr> computeFinalActions() {
    ImmutableList.Builder<StateExpr> finalActionsBuilder = ImmutableList.builder();
    for (FlowDisposition action : _actions) {
      switch (action) {
        case ACCEPTED:
          if (!_finalNodes.isEmpty()) {
            for (String finalNode : _finalNodes) {
              StateExpr accept = new NodeAccept(finalNode);
              finalActionsBuilder.add(accept);
            }
          } else {
            finalActionsBuilder.add(Accept.INSTANCE);
          }
          break;

        case DENIED_IN:
          if (!_finalNodes.isEmpty()) {
            for (String finalNode : _finalNodes) {
              StateExpr drop = new NodeDropAclIn(finalNode);
              finalActionsBuilder.add(drop);
            }
          } else {
            finalActionsBuilder.add(DropAclIn.INSTANCE);
          }
          break;

        case DENIED_OUT:
          if (!_finalNodes.isEmpty()) {
            for (String finalNode : _finalNodes) {
              StateExpr drop = new NodeDropAclOut(finalNode);
              finalActionsBuilder.add(drop);
            }
          } else {
            finalActionsBuilder.add(DropAclOut.INSTANCE);
          }
          break;

        case NO_ROUTE:
          if (!_finalNodes.isEmpty()) {
            for (String finalNode : _finalNodes) {
              StateExpr drop = new NodeDropNoRoute(finalNode);
              finalActionsBuilder.add(drop);
            }
          } else {
            finalActionsBuilder.add(DropNoRoute.INSTANCE);
          }
          break;

        case NULL_ROUTED:
          if (!_finalNodes.isEmpty()) {
            for (String finalNode : _finalNodes) {
              StateExpr drop = new NodeDropNullRoute(finalNode);
              finalActionsBuilder.add(drop);
            }
          } else {
            finalActionsBuilder.add(DropNullRoute.INSTANCE);
          }
          break;

        case NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK:
          if (!_finalNodes.isEmpty()) {
            for (String finalNode : _finalNodes) {
              StateExpr drop = new NodeNeighborUnreachableOrExitsNetwork(finalNode);
              finalActionsBuilder.add(drop);
            }
          } else {
            finalActionsBuilder.add(NeighborUnreachableOrExitsNetwork.INSTANCE);
          }
          break;
        default:
          throw new BatfishException("Unimplemented disposition: " + action.toString());
      }
    }
    return finalActionsBuilder.build();
  }

  @Override
  public ReachabilityProgram getReachabilityProgram(SynthesizerInput input) {
    ImmutableList.Builder<RuleStatement> rules = ImmutableList.builder();
    List<StateExpr> finalActions = computeFinalActions();
    finalActions
        .stream()
        .map(finalAction -> new BasicRuleStatement(ImmutableSet.of(finalAction), Query.INSTANCE))
        .forEach(rules::add);
    addOriginateRules(rules);

    /*
     * If transit nodes are specified, make sure one was transited.
     */
    BooleanExpr transitNodesConstraint =
        input.getTransitNodes().isEmpty()
            ? TrueExpr.INSTANCE
            : new EqExpr(
                new VarIntExpr(DefaultTransitionGenerator.TRANSITED_TRANSIT_NODES_FIELD),
                DefaultTransitionGenerator.TRANSITED);

    return ReachabilityProgram.builder()
        .setInput(input)
        .setQueries(ImmutableList.of(new QueryStatement(Query.INSTANCE)))
        .setRules(rules.build())
        .setSmtConstraint(
            new AndExpr(
                ImmutableList.of(
                    NO_ACLS_NO_IP_SPACES_NO_SOURCES_ORIG_HEADERSPACE.toBooleanExpr(_headerSpace),
                    getSrcNattedConstraint(),
                    transitNodesConstraint)))
        .build();
  }
}
