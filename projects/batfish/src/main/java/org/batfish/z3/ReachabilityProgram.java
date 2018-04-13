package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.visitors.Simplifier;

public class ReachabilityProgram {

  public static class Builder {

    private SynthesizerInput _input;

    private List<QueryStatement> _queries;

    private List<RuleStatement> _rules;

    private BooleanExpr _smtConstraint;

    private Builder() {
      _queries = ImmutableList.of();
      _rules = ImmutableList.of();
      _smtConstraint = TrueExpr.INSTANCE;
    }

    public ReachabilityProgram build() {
      return new ReachabilityProgram(_queries, _rules, _input, _smtConstraint);
    }

    public Builder setInput(SynthesizerInput input) {
      _input = input;
      return this;
    }

    public Builder setQueries(List<QueryStatement> queries) {
      _queries = ImmutableList.copyOf(queries);
      return this;
    }

    public Builder setRules(List<RuleStatement> rules) {
      _rules = ImmutableList.copyOf(rules);
      return this;
    }

    public Builder setSmtConstraint(BooleanExpr constraint) {
      _smtConstraint = constraint;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final SynthesizerInput _input;

  private final List<QueryStatement> _queries;

  private final List<RuleStatement> _rules;

  private final BooleanExpr _smtConstraint;

  private ReachabilityProgram(
      List<QueryStatement> queries,
      List<RuleStatement> rules,
      SynthesizerInput input,
      BooleanExpr smtConstraint) {
    _queries = ImmutableList.copyOf(queries);
    _input = input;
    _smtConstraint = smtConstraint;

    /*
     * Simplify rule statements if desired, and remove statements that simplify to trivial
     * statements that are no longer rules.
     */
    _rules =
        _input.getSimplify()
            ? rules
                .stream()
                .map(Simplifier::simplifyStatement)
                .filter(s -> s instanceof RuleStatement)
                .map(s -> (RuleStatement) s)
                .collect(ImmutableList.toImmutableList())
            : ImmutableList.copyOf(rules);
  }

  public SynthesizerInput getInput() {
    return _input;
  }

  public List<QueryStatement> getQueries() {
    return _queries;
  }

  public List<RuleStatement> getRules() {
    return _rules;
  }

  public BooleanExpr getSmtConstraint() {
    return _smtConstraint;
  }
}
