package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.state.AclLineIndependentMatch;
import org.batfish.z3.state.NumberedQuery;

/**
 * Provides a query that is satisfied when the match condition of a particular ACL line is
 * satisfiable, considered independently from the ACL lines that may precede it.
 */
public class AclLineIndependentSatisfiabilityQuerySynthesizer extends SatQuerySynthesizer<AclLine> {

  private final String _aclName;

  private final String _hostname;

  private final int _lineNumber;

  public AclLineIndependentSatisfiabilityQuerySynthesizer(
      String hostname, String aclName, int lineNumber) {
    _hostname = hostname;
    _aclName = aclName;
    _lineNumber = lineNumber;
  }

  @Override
  public ReachabilityProgram getReachabilityProgram(SynthesizerInput input) {
    ReachabilityProgram.Builder builder = ReachabilityProgram.builder().setInput(input);
    ImmutableList.Builder<QueryStatement> queries = ImmutableList.builder();
    ImmutableList.Builder<RuleStatement> rules = ImmutableList.builder();
    NumberedQuery query = new NumberedQuery(_lineNumber);
    rules.add(
        new BasicRuleStatement(
            TrueExpr.INSTANCE,
            ImmutableSet.of(new AclLineIndependentMatch(_hostname, _aclName, _lineNumber)),
            new NumberedQuery(_lineNumber)));
    queries.add(new QueryStatement(query));
    _keys.add(new AclLine(_hostname, _aclName, _lineNumber));
    return builder.setInput(input).setQueries(queries.build()).setRules(rules.build()).build();
  }
}
