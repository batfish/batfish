package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.TrueExpr;
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
  public ReachabilityProgram getReachabilityProgram(SynthesizerInput input) {
    ReachabilityProgram.Builder builder = ReachabilityProgram.builder().setInput(input);
    ImmutableList.Builder<QueryStatement> queries = ImmutableList.builder();
    ImmutableList.Builder<RuleStatement> rules = ImmutableList.builder();
    for (int line = 0; line < _numLines; line++) {
      NumberedQuery query = new NumberedQuery(line);
      rules.add(
          new BasicRuleStatement(
              TrueExpr.INSTANCE,
              ImmutableSet.of(new AclLineMatch(_hostname, _aclName, line)),
              new NumberedQuery(line)));
      queries.add(new QueryStatement(query));
      _keys.add(new AclLine(_hostname, _aclName, line));
    }
    return builder.setInput(input).setQueries(queries.build()).setRules(rules.build()).build();
  }
}
