package org.batfish.z3;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.List;
import org.batfish.z3.node.AclMatchExpr;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.DeclareRelExpr;
import org.batfish.z3.node.NumberedQueryExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

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
  public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
    Context ctx = baseProgram.getContext();
    NodProgram program = new NodProgram(ctx);
    for (int line = 0; line < _numLines; line++) {
      AclMatchExpr matchAclLine = new AclMatchExpr(_hostname, _aclName, line);
      AndExpr queryConditions = new AndExpr();
      queryConditions.addConjunct(matchAclLine);
      queryConditions.addConjunct(SaneExpr.INSTANCE);
      NumberedQueryExpr queryRel = new NumberedQueryExpr(line);
      String queryRelName = queryRel.getRelations().toArray(new String[] {})[0];
      List<Integer> sizes = new ArrayList<>();
      sizes.addAll(Synthesizer.PACKET_VAR_SIZES.values());
      DeclareRelExpr declaration = new DeclareRelExpr(queryRelName, sizes);
      baseProgram.getRelationDeclarations().put(queryRelName, declaration.toFuncDecl(ctx));
      RuleExpr queryRule = new RuleExpr(queryConditions, queryRel);
      List<BoolExpr> rules = program.getRules();
      rules.add(queryRule.toBoolExpr(baseProgram));
      QueryExpr query = new QueryExpr(queryRel);
      BoolExpr queryBoolExpr = query.toBoolExpr(baseProgram);
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
