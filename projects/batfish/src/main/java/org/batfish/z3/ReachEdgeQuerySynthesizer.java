package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;
import java.util.List;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.visitors.BoolExprTransformer;
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
  public NodProgram getNodProgram(SynthesizerInput input, NodProgram baseProgram)
      throws Z3Exception {
    NodProgram program = new NodProgram(baseProgram.getContext());
    OriginateVrf originate = new OriginateVrf(_originationNode, _ingressVrf);
    RuleStatement injectSymbolicPackets = new RuleStatement(originate);
    ImmutableList.Builder<BooleanExpr> queryConditionsBuilder =
        ImmutableList.<BooleanExpr>builder()
            .add(new PreOutEdge(_edge))
            .add(new PreInInterface(_edge.getNode2(), _edge.getInt2()))
            .add(new HeaderSpaceMatchExpr(_headerSpace));
    if (_requireAcceptance) {
      queryConditionsBuilder.add(Accept.INSTANCE);
    }
    queryConditionsBuilder.add(SaneExpr.INSTANCE);
    AndExpr queryConditions = new AndExpr(queryConditionsBuilder.build());
    RuleStatement queryRule = new RuleStatement(queryConditions, Query.INSTANCE);
    List<BoolExpr> rules = program.getRules();
    BoolExpr injectSymbolicPacketsBoolExpr =
        BoolExprTransformer.toBoolExpr(
            injectSymbolicPackets.getSubExpression(), input, baseProgram);
    rules.add(injectSymbolicPacketsBoolExpr);
    rules.add(BoolExprTransformer.toBoolExpr(queryRule.getSubExpression(), input, baseProgram));
    QueryStatement query = new QueryStatement(Query.INSTANCE);
    BoolExpr queryBoolExpr =
        BoolExprTransformer.toBoolExpr(query.getSubExpression(), input, baseProgram);
    program.getQueries().add(queryBoolExpr);
    return program;
  }
}
