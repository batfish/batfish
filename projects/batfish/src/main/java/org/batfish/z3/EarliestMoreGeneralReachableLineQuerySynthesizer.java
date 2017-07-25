package org.batfish.z3;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.z3.node.AndExpr;
import org.batfish.z3.node.BooleanExpr;
import org.batfish.z3.node.DeclareRelExpr;
import org.batfish.z3.node.NotExpr;
import org.batfish.z3.node.NumberedQueryExpr;
import org.batfish.z3.node.QueryExpr;
import org.batfish.z3.node.RuleExpr;
import org.batfish.z3.node.SaneExpr;

public class EarliestMoreGeneralReachableLineQuerySynthesizer
    extends FirstUnsatQuerySynthesizer<AclLine, Integer> {

  private String _aclName;

  private List<AclLine> _earlierReachableLines;

  private String _hostname;

  private IpAccessList _list;

  private AclLine _unreachableLine;

  public EarliestMoreGeneralReachableLineQuerySynthesizer(
      AclLine unreachableLine, List<AclLine> earlierReachableLines, IpAccessList list) {
    super(unreachableLine);
    _unreachableLine = unreachableLine;
    _earlierReachableLines = earlierReachableLines;
    _hostname = _unreachableLine.getHostname();
    _aclName = _unreachableLine.getAclName();
    _list = list;
  }

  @Override
  public NodProgram getNodProgram(NodProgram baseProgram) throws Z3Exception {
    Context ctx = baseProgram.getContext();
    NodProgram program = new NodProgram(ctx);
    int unreachableLineIndex = _unreachableLine.getLine();
    IpAccessListLine unreachableLine = _list.getLines().get(unreachableLineIndex);
    BooleanExpr matchUnreachableLineHeaderSpace = Synthesizer.matchHeaderSpace(unreachableLine);
    for (AclLine earlierReachableLine : _earlierReachableLines) {
      int earlierLineIndex = earlierReachableLine.getLine();
      IpAccessListLine earlierLine = _list.getLines().get(earlierLineIndex);
      BooleanExpr matchEarlierLineHeaderSpace = Synthesizer.matchHeaderSpace(earlierLine);
      AndExpr queryConditions = new AndExpr();
      queryConditions.addConjunct(new NotExpr(matchEarlierLineHeaderSpace));
      queryConditions.addConjunct(matchUnreachableLineHeaderSpace);
      queryConditions.addConjunct(SaneExpr.INSTANCE);
      NumberedQueryExpr queryRel = new NumberedQueryExpr(earlierLineIndex);
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
      _resultsByQueryIndex.add(earlierLineIndex);
    }
    return program;
  }

  @Override
  public NodProgram synthesizeBaseProgram(Synthesizer synthesizer, Context ctx) {
    return synthesizer.synthesizeNodAclProgram(_hostname, _aclName, ctx);
  }
}
