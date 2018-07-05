package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.state.NumberedQuery;

public final class AclReachabilityQuerySynthesizer extends SatQuerySynthesizer<AclLine> {

  private final int _lineNum;

  private final List<RuleStatement> _rules;

  public AclReachabilityQuerySynthesizer(
      List<RuleStatement> rules, String hostname, String aclName, int lineNum) {
    _rules = rules;
    _lineNum = lineNum;
    for (int i = 0; i <= lineNum; i++) {
      _keys.add(new AclLine(hostname, aclName, lineNum));
    }
  }

  @Override
  public ReachabilityProgram getReachabilityProgram(SynthesizerInput input) {
    return ReachabilityProgram.builder()
        .setInput(input)
        .setQueries(ImmutableList.of(new QueryStatement(new NumberedQuery(_lineNum))))
        .setRules(_rules)
        .build();
  }
}
