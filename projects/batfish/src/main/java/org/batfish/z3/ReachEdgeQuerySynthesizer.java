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
import org.batfish.z3.expr.QueryExpr;
import org.batfish.z3.expr.RuleExpr;
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
  public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
    NodProgram program = new NodProgram(baseProgram.getContext());
    BooleanExpr originate = OriginateVrf.expr(_originationNode, _ingressVrf);
    RuleExpr injectSymbolicPackets = new RuleExpr(originate);
    ImmutableList.Builder<BooleanExpr> queryConditionsBuilder =
        ImmutableList.<BooleanExpr>builder()
            .add(PreOutEdge.expr(_edge))
            .add(PreInInterface.expr(_edge.getNode2(), _edge.getInt2()))
            .add(new HeaderSpaceMatchExpr(_headerSpace));
    if (_requireAcceptance) {
      queryConditionsBuilder.add(Accept.EXPR);
    }
    queryConditionsBuilder.add(SaneExpr.INSTANCE);
    AndExpr queryConditions = new AndExpr(queryConditionsBuilder.build());
    RuleExpr queryRule = new RuleExpr(queryConditions, Query.EXPR);
    List<BoolExpr> rules = program.getRules();
    BoolExpr injectSymbolicPacketsBoolExpr =
        BoolExprTransformer.toBoolExpr(injectSymbolicPackets.getSubExpression(), baseProgram);
    rules.add(injectSymbolicPacketsBoolExpr);
    rules.add(BoolExprTransformer.toBoolExpr(queryRule.getSubExpression(), baseProgram));
    QueryExpr query = new QueryExpr(Query.EXPR);
    BoolExpr queryBoolExpr = BoolExprTransformer.toBoolExpr(query.getSubExpression(), baseProgram);
    program.getQueries().add(queryBoolExpr);
    return program;
  }
}
