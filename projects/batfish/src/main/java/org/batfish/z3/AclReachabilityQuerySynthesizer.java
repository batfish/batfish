package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;
import java.util.List;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.DeclareRelStatement;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.visitors.BoolExprTransformer;
import org.batfish.z3.state.AclLineMatch;
import org.batfish.z3.state.NumberedQuery;

public final class AclReachabilityQuerySynthesizer extends SatQuerySynthesizer<AclLine> {

  private final String _aclName;

  private final String _hostname;

  private final int _numLines;

  public AclReachabilityQuerySynthesizer(String hostname, String aclName, int numLines) {
    _hostname = hostname;
    _aclName = aclName;
    _numLines = numLines;
  }

  @Override
  public NodProgram getNodProgram(SynthesizerInput input, NodProgram baseProgram)
      throws Z3Exception {
    Context ctx = baseProgram.getContext();
    NodProgram program = new NodProgram(ctx);
    for (int line = 0; line < _numLines; line++) {
      AclLineMatch matchAclLine = new AclLineMatch(_hostname, _aclName, line);
      AndExpr queryConditions = new AndExpr(ImmutableList.of(matchAclLine, SaneExpr.INSTANCE));
      NumberedQuery queryRel = new NumberedQuery(line);
      String queryRelName = BoolExprTransformer.getNodName(input, queryRel);
      DeclareRelStatement declaration = new DeclareRelStatement(queryRelName);
      baseProgram.getRelationDeclarations().put(queryRelName, declaration.toFuncDecl(ctx));
      RuleStatement queryRule = new RuleStatement(queryConditions, queryRel);
      List<BoolExpr> rules = program.getRules();
      rules.add(BoolExprTransformer.toBoolExpr(queryRule.getSubExpression(), input, baseProgram));
      QueryStatement query = new QueryStatement(queryRel);
      BoolExpr queryBoolExpr =
          BoolExprTransformer.toBoolExpr(query.getSubExpression(), input, baseProgram);
      program.getQueries().add(queryBoolExpr);
      _keys.add(new AclLine(_hostname, _aclName, line));
    }
    return program;
  }

  @Override
  public NodProgram synthesizeBaseProgram(Synthesizer synthesizer, Context ctx) {
    return synthesizer.synthesizeNodAclProgram(_hostname, _aclName, ctx);
  }
}
