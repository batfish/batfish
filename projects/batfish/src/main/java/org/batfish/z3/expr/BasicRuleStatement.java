package org.batfish.z3.expr;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;

/**
 * A @{link RuleStatement} by which a postcondition state is produced by a set of precondition
 * state(s) under state-independent constraints
 */
public class BasicRuleStatement extends RuleStatement {

  private final BasicStateExpr _postconditionState;

  private final BooleanExpr _preconditionStateIndependentConstraints;

  private final Set<BasicStateExpr> _preconditionStates;

  public BasicRuleStatement(
      BooleanExpr preconditionStateIndependentConstraints,
      Set<BasicStateExpr> preconditionStates,
      BasicStateExpr postconditionState) {
    _postconditionState = postconditionState;
    _preconditionStateIndependentConstraints = preconditionStateIndependentConstraints;
    _preconditionStates = preconditionStates;
  }

  public BasicRuleStatement(BasicStateExpr preconditionState, BasicStateExpr postconditionState) {
    this(TrueExpr.INSTANCE, ImmutableSet.of(preconditionState), postconditionState);
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitBasicRuleStatement(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitBasicRuleStatement(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _postconditionState, _preconditionStateIndependentConstraints, _preconditionStates);
  }

  @Override
  public boolean statementEquals(Statement e) {
    BasicRuleStatement rhs = (BasicRuleStatement) e;
    return Objects.equals(_postconditionState, rhs._postconditionState)
        && Objects.equals(
            _preconditionStateIndependentConstraints, rhs._preconditionStateIndependentConstraints)
        && Objects.equals(_preconditionStates, rhs._preconditionStates);
  }
}
