package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;
import java.util.List;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.DeclareRelExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.QueryExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.visitors.BoolExprTransformer;
import org.batfish.z3.state.NumberedQuery;

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
  public NodProgram getNodProgram(SynthesizerInput input, NodProgram baseProgram)
      throws Z3Exception {
    Context ctx = baseProgram.getContext();
    NodProgram program = new NodProgram(ctx);
    int unreachableLineIndex = _unreachableLine.getLine();
    IpAccessListLine unreachableLine = _list.getLines().get(unreachableLineIndex);
    BooleanExpr matchUnreachableLineHeaderSpace = new HeaderSpaceMatchExpr(unreachableLine);
    for (AclLine earlierReachableLine : _earlierReachableLines) {
      int earlierLineIndex = earlierReachableLine.getLine();
      IpAccessListLine earlierLine = _list.getLines().get(earlierLineIndex);
      BooleanExpr matchEarlierLineHeaderSpace = new HeaderSpaceMatchExpr(earlierLine);
      AndExpr queryConditions =
          new AndExpr(
              ImmutableList.of(
                  new NotExpr(matchEarlierLineHeaderSpace),
                  matchUnreachableLineHeaderSpace,
                  SaneExpr.INSTANCE));
      NumberedQuery queryRel = new NumberedQuery(earlierLineIndex);
      String queryRelName = BoolExprTransformer.getNodName(input, queryRel);
      DeclareRelExpr declaration = new DeclareRelExpr(queryRelName);
      baseProgram.getRelationDeclarations().put(queryRelName, declaration.toFuncDecl(ctx));
      RuleExpr queryRule = new RuleExpr(queryConditions, queryRel);
      List<BoolExpr> rules = program.getRules();
      rules.add(BoolExprTransformer.toBoolExpr(queryRule.getSubExpression(), input, baseProgram));
      QueryExpr query = new QueryExpr(queryRel);
      BoolExpr queryBoolExpr =
          BoolExprTransformer.toBoolExpr(query.getSubExpression(), input, baseProgram);
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
