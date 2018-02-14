package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
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
  public NodProgram getNodProgram(SynthesizerInput input, NodProgram baseProgram)
      throws Z3Exception {
    NodProgram program = new NodProgram(baseProgram.getContext());
    OriginateVrf originate = new OriginateVrf(_hostname, _vrf);
    RuleStatement injectSymbolicPackets = new RuleStatement(originate);
    AndExpr queryConditions =
        new AndExpr(
            ImmutableList.of(
                Accept.INSTANCE,
                Drop.INSTANCE,
                SaneExpr.INSTANCE,
                new HeaderSpaceMatchExpr(_headerSpace)));
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
