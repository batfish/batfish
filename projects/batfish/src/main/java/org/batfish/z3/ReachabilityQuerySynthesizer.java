package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.CurrentIsOriginalExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.TransformationRuleStatement;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.Debug;
import org.batfish.z3.state.Drop;
import org.batfish.z3.state.DropAcl;
import org.batfish.z3.state.DropAclIn;
import org.batfish.z3.state.DropAclOut;
import org.batfish.z3.state.DropNoRoute;
import org.batfish.z3.state.DropNullRoute;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDrop;
import org.batfish.z3.state.NodeDropAcl;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.NodeTransit;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.Query;

public class ReachabilityQuerySynthesizer extends BaseQuerySynthesizer {

  private Set<ForwardingAction> _actions;

  private Set<String> _finalNodes;

  private HeaderSpace _headerSpace;

  private Map<String, Set<String>> _ingressNodeVrfs;

  @SuppressWarnings("unused")
  private Set<String> _notTransitNodes;

  private Set<String> _transitNodes;

  public ReachabilityQuerySynthesizer(
      Set<ForwardingAction> actions,
      HeaderSpace headerSpace,
      Set<String> finalNodes,
      Map<String, Set<String>> ingressNodeVrfs,
      Set<String> transitNodes,
      Set<String> notTransitNodes) {
    _actions = actions;
    _finalNodes = finalNodes;
    _headerSpace = headerSpace;
    _ingressNodeVrfs = ingressNodeVrfs;
    _transitNodes = transitNodes;
    _notTransitNodes = notTransitNodes;
  }

  @Override
  public ReachabilityProgram getReachabilityProgram(SynthesizerInput input) {
    ImmutableList.Builder<BooleanExpr> queryConditionsBuilder = ImmutableList.builder();

    // create query condition for action at final node(s)
    ImmutableList.Builder<BasicStateExpr> finalActions = ImmutableList.builder();
    ImmutableList.Builder<BasicStateExpr> queryPreconditionPreTransformationStatesBuilder =
        ImmutableList.builder();
    for (ForwardingAction action : _actions) {
      switch (action) {
        case ACCEPT:
          if (_finalNodes.size() > 0) {
            for (String finalNode : _finalNodes) {
              BasicStateExpr accept = new NodeAccept(finalNode);
              finalActions.add(accept);
            }
          } else {
            finalActions.add(Accept.INSTANCE);
          }
          break;

        case DEBUG:
          finalActions.add(Debug.INSTANCE);
          break;

        case DROP:
          if (_finalNodes.size() > 0) {
            for (String finalNode : _finalNodes) {
              BasicStateExpr drop = new NodeDrop(finalNode);
              finalActions.add(drop);
            }
          } else {
            finalActions.add(Drop.INSTANCE);
          }
          break;

        case DROP_ACL:
          if (_finalNodes.size() > 0) {
            for (String finalNode : _finalNodes) {
              BasicStateExpr drop = new NodeDropAcl(finalNode);
              finalActions.add(drop);
            }
          } else {
            finalActions.add(DropAcl.INSTANCE);
          }
          break;

        case DROP_ACL_IN:
          if (_finalNodes.size() > 0) {
            for (String finalNode : _finalNodes) {
              BasicStateExpr drop = new NodeDropAclIn(finalNode);
              finalActions.add(drop);
            }
          } else {
            finalActions.add(DropAclIn.INSTANCE);
          }
          break;

        case DROP_ACL_OUT:
          if (_finalNodes.size() > 0) {
            for (String finalNode : _finalNodes) {
              BasicStateExpr drop = new NodeDropAclOut(finalNode);
              finalActions.add(drop);
            }
          } else {
            finalActions.add(DropAclOut.INSTANCE);
          }
          break;

        case DROP_NO_ROUTE:
          if (_finalNodes.size() > 0) {
            for (String finalNode : _finalNodes) {
              BasicStateExpr drop = new NodeDropNoRoute(finalNode);
              finalActions.add(drop);
            }
          } else {
            finalActions.add(DropNoRoute.INSTANCE);
          }
          break;

        case DROP_NULL_ROUTE:
          if (_finalNodes.size() > 0) {
            for (String finalNode : _finalNodes) {
              BasicStateExpr drop = new NodeDropNullRoute(finalNode);
              finalActions.add(drop);
            }
          } else {
            finalActions.add(DropNullRoute.INSTANCE);
          }
          break;

        case FORWARD:
        default:
          throw new BatfishException("unsupported action");
      }
    }
    queryConditionsBuilder.add(SaneExpr.INSTANCE);

    // check transit constraints (unordered)
    _transitNodes
        .stream()
        .map(NodeTransit::new)
        .forEach(queryPreconditionPreTransformationStatesBuilder::add);

    /* TODO: re-enable notTransitNodes via stratified negation */
    //    _notTransitNodes
    //        .stream()
    //        .map(NodeTransit::new)
    //        .map(NotExpr::new)
    //        .forEach(queryPreconditionPreTransformationStates::add);

    // add headerSpace constraints
    BooleanExpr matchHeaderSpace = new HeaderSpaceMatchExpr(_headerSpace);
    queryConditionsBuilder.add(matchHeaderSpace);

    ImmutableList.Builder<RuleStatement> rules = ImmutableList.builder();
    // create rules for injecting symbolic packets into ingress node(s)
    for (String ingressNode : _ingressNodeVrfs.keySet()) {
      for (String ingressVrf : _ingressNodeVrfs.get(ingressNode)) {
        rules.add(
            new BasicRuleStatement(
                CurrentIsOriginalExpr.INSTANCE, new OriginateVrf(ingressNode, ingressVrf)));
      }
    }
    List<BasicStateExpr> queryPreconditionPreTransformationStates =
        queryPreconditionPreTransformationStatesBuilder.build();
    BooleanExpr queryConditions = new AndExpr(queryConditionsBuilder.build());
    finalActions
        .build()
        .stream()
        .map(
            finalAction ->
                new TransformationRuleStatement(
                    queryConditions,
                    ImmutableSet.<BasicStateExpr>builder()
                        .add(finalAction)
                        .addAll(queryPreconditionPreTransformationStates)
                        .build(),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    Query.INSTANCE))
        .forEach(rules::add);
    return ReachabilityProgram.builder()
        .setInput(input)
        .setQueries(ImmutableList.of(new QueryStatement(Query.INSTANCE)))
        .setRules(rules.build())
        .build();
  }
}
