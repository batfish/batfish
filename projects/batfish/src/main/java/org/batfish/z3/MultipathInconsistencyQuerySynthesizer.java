package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.QueryExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.visitors.BoolExprTransformer;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.Drop;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.Query;

public class MultipathInconsistencyQuerySynthesizer extends BaseQuerySynthesizer {

  private HeaderSpace _headerSpace;

  private String _hostname;

  private String _vrf;

  public MultipathInconsistencyQuerySynthesizer(
      String hostname, String vrf, HeaderSpace headerSpace) {
    _hostname = hostname;
    _vrf = vrf;
    _headerSpace = headerSpace;
  }

  @Override
  public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
    NodProgram program = new NodProgram(baseProgram.getContext());
    BooleanExpr originate = OriginateVrf.expr(_hostname, _vrf);
    RuleExpr injectSymbolicPackets = new RuleExpr(originate);
    AndExpr queryConditions =
        new AndExpr(
            ImmutableList.of(
                Accept.EXPR, Drop.EXPR, SaneExpr.INSTANCE, new HeaderSpaceMatchExpr(_headerSpace)));
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
