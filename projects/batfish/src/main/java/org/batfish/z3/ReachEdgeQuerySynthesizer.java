package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.Query;

public class ReachEdgeQuerySynthesizer extends BaseQuerySynthesizer {

  private Edge _edge;

  private AclLineMatchExpr _headerSpace;

  private String _ingressVrf;

  private String _originationNode;

  private boolean _requireAcceptance;

  public ReachEdgeQuerySynthesizer(
      String originationNode,
      String ingressVrf,
      Edge edge,
      boolean requireAcceptance,
      AclLineMatchExpr headerSpace) {
    _edge = edge;
    _headerSpace = headerSpace;
    _ingressVrf = ingressVrf;
    _originationNode = originationNode;
    _requireAcceptance = requireAcceptance;
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
                            new EqExpr(
                                new VarIntExpr(Field.ORIG_SRC_IP), new VarIntExpr(Field.SRC_IP)),
                            _headerSpace == null
                                ? TrueExpr.INSTANCE
                                : AclLineMatchExprToBooleanExpr
                                    .NO_ACLS_NO_IP_SPACES_NO_SOURCES_ORIG_HEADERSPACE.toBooleanExpr(
                                    _headerSpace))),
                    new OriginateVrf(_originationNode, _ingressVrf)),
                new BasicRuleStatement(
                    queryPreconditionPreTransformationStates.build(), Query.INSTANCE)))
        .build();
  }
}
