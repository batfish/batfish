package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.CurrentIsOriginalExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.Query;

public class ReachEdgeQuerySynthesizer extends BaseQuerySynthesizer {

  private Edge _edge;

  private HeaderSpace _headerSpace;

  private String _ingressVrf;

  private String _originationNode;

  private boolean _requireAcceptance;

  public ReachEdgeQuerySynthesizer(
      String originationNode,
      String ingressVrf,
      Edge edge,
      boolean requireAcceptance,
      HeaderSpace headerSpace) {
    _originationNode = originationNode;
    _ingressVrf = ingressVrf;
    _edge = edge;
    _requireAcceptance = requireAcceptance;
    _headerSpace = headerSpace;
  }

  @Override
  public ReachabilityProgram getReachabilityProgram(SynthesizerInput input) {
    ImmutableSet.Builder<StateExpr> queryPreconditionPreTransformationStates =
        ImmutableSet.<StateExpr>builder()
            .add(new PreOutEdge(_edge))
            .add(new PreInInterface(_edge.getNode2(), _edge.getInt2()));
    if (_requireAcceptance) {
      queryPreconditionPreTransformationStates.add(Accept.INSTANCE);
    }
    return ReachabilityProgram.builder()
        .setInput(input)
        .setQueries(ImmutableList.of(new QueryStatement(Query.INSTANCE)))
        .setRules(
            ImmutableList.of(
                new BasicRuleStatement(
                    new AndExpr(
                        ImmutableList.of(
                            CurrentIsOriginalExpr.INSTANCE,
                            new HeaderSpaceMatchExpr(_headerSpace),
                            SaneExpr.INSTANCE)),
                    new OriginateVrf(_originationNode, _ingressVrf)),
                new BasicRuleStatement(
                    queryPreconditionPreTransformationStates.build(), Query.INSTANCE)))
        .build();
  }
}
