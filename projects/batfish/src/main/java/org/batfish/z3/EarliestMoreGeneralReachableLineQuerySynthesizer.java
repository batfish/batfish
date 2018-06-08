package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.state.NumberedQuery;

/**
 * Synthesizer for a query program that finds the earliest of all reachable lines in an ACL
 * preceding a given unreachable line, such that its match condition is more general than that of
 * the unreachable line
 */
public class EarliestMoreGeneralReachableLineQuerySynthesizer
    extends FirstUnsatQuerySynthesizer<AclLine, Integer> {

  private List<AclLine> _earlierReachableLines;

  private IpAccessList _list;

  private Map<String, IpSpace> _namedIpSpaces;

  private final Map<String, IpAccessList> _nodeAcls;

  private Map<String, IntExpr> _sourceInterfaceFieldValues;

  private final Field _sourceInterfaceField;

  private AclLine _unreachableLine;

  public EarliestMoreGeneralReachableLineQuerySynthesizer(
      AclLine unreachableLine,
      List<AclLine> earlierReachableLines,
      IpAccessList list,
      Map<String, IpSpace> namedIpSpaces,
      Map<String, IpAccessList> nodeAcls,
      List<String> nodeInterfaces) {
    super(unreachableLine);
    _unreachableLine = unreachableLine;
    _earlierReachableLines = earlierReachableLines;
    _list = list;
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _nodeAcls = ImmutableMap.copyOf(nodeAcls);
    int fieldBits = Math.max(LongMath.log2(nodeInterfaces.size() + 1, RoundingMode.CEILING), 1);
    _sourceInterfaceField = new Field("SRC_INTERFACE", fieldBits);
    _sourceInterfaceFieldValues = computeSourceInterfaceFieldValues(nodeInterfaces);
  }

  private Map<String, IntExpr> computeSourceInterfaceFieldValues(List<String> nodeInterfaces) {
    ImmutableMap.Builder<String, IntExpr> sourceInterfaceFieldValues = ImmutableMap.builder();
    CommonUtil.forEachWithIndex(
        nodeInterfaces,
        (index, iface) ->
            sourceInterfaceFieldValues.put(
                iface, new LitIntExpr(index + 1, _sourceInterfaceField.getSize())));
    return sourceInterfaceFieldValues.build();
  }

  @Override
  public ReachabilityProgram getReachabilityProgram(SynthesizerInput input) {
    int unreachableLineIndex = _unreachableLine.getLine();
    IpAccessListLine unreachableLine = _list.getLines().get(unreachableLineIndex);
    AclLineMatchExprToBooleanExpr aclLineMatchExprToBooleanExpr =
        new AclLineMatchExprToBooleanExpr(
            _nodeAcls, _namedIpSpaces, _sourceInterfaceField, _sourceInterfaceFieldValues);
    BooleanExpr matchUnreachableLineHeaderSpace =
        aclLineMatchExprToBooleanExpr.toBooleanExpr(unreachableLine.getMatchCondition());
    ImmutableList.Builder<QueryStatement> queries = ImmutableList.builder();
    ImmutableList.Builder<RuleStatement> rules = ImmutableList.builder();
    for (AclLine earlierReachableLine : _earlierReachableLines) {
      int earlierLineIndex = earlierReachableLine.getLine();
      IpAccessListLine earlierLine = _list.getLines().get(earlierLineIndex);
      BooleanExpr matchEarlierLineHeaderSpace =
          aclLineMatchExprToBooleanExpr.toBooleanExpr(earlierLine.getMatchCondition());
      NumberedQuery queryRel = new NumberedQuery(earlierLineIndex);
      rules.add(
          new BasicRuleStatement(
              new AndExpr(
                  ImmutableList.of(
                      new NotExpr(matchEarlierLineHeaderSpace), matchUnreachableLineHeaderSpace)),
              queryRel));
      QueryStatement query = new QueryStatement(queryRel);
      queries.add(query);
      _resultsByQueryIndex.add(earlierLineIndex);
    }
    return ReachabilityProgram.builder()
        .setInput(input)
        .setQueries(queries.build())
        .setRules(rules.build())
        .build();
  }
}
