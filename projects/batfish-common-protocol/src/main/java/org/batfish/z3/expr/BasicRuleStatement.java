package org.batfish.z3.expr;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;

/**
 * A @{link RuleStatement} by which a postcondition state is produced by a set of precondition
 * state(s) under state-independent constraints
 */
public class BasicRuleStatement extends RuleStatement {

  private final StateExpr _postconditionState;

  private final BooleanExpr _preconditionStateIndependentConstraints;

  private final Set<StateExpr> _preconditionStates;

  private int _hashCode;

  public BasicRuleStatement(StateExpr postconditionState) {
    this(TrueExpr.INSTANCE, ImmutableSet.of(), postconditionState);
  }

  public BasicRuleStatement(StateExpr preconditionState, StateExpr postconditionState) {
    this(TrueExpr.INSTANCE, ImmutableSet.of(preconditionState), postconditionState);
  }

  public BasicRuleStatement(
      BooleanExpr preconditionStateIndependentConstraints, StateExpr postconditionState) {
    this(preconditionStateIndependentConstraints, ImmutableSet.of(), postconditionState);
  }

  public BasicRuleStatement(
      BooleanExpr preconditionStateIndependentConstraints,
      StateExpr preconditionState,
      StateExpr postconditionState) {
    _postconditionState = postconditionState;
    _preconditionStateIndependentConstraints = preconditionStateIndependentConstraints;
    _preconditionStates = ImmutableSet.of(preconditionState);
  }

  public BasicRuleStatement(
      BooleanExpr preconditionStateIndependentConstraints,
      Set<StateExpr> preconditionStates,
      StateExpr postconditionState) {
    _postconditionState = postconditionState;
    _preconditionStateIndependentConstraints = preconditionStateIndependentConstraints;
    _preconditionStates = preconditionStates;
  }

  public BasicRuleStatement(Set<StateExpr> preconditionStates, StateExpr postconditionState) {
    this(TrueExpr.INSTANCE, preconditionStates, postconditionState);
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
  public StateExpr getPostconditionState() {
    return _postconditionState;
  }

  public BooleanExpr getPreconditionStateIndependentConstraints() {
    return _preconditionStateIndependentConstraints;
  }

  @Override
  public Set<StateExpr> getPreconditionStates() {
    return _preconditionStates;
  }

  @Override
  public int hashCode() {
    if (_hashCode == 0) {
      _hashCode =
          Objects.hash(
              _postconditionState, _preconditionStateIndependentConstraints, _preconditionStates);
    }
    return _hashCode;
  }

  @Override
  public boolean statementEquals(Statement e) {
    BasicRuleStatement rhs = (BasicRuleStatement) e;

    if (_hashCode != 0 && rhs._hashCode != 0 && _hashCode != rhs._hashCode) {
      return false;
    }

    return Objects.equals(_postconditionState, rhs._postconditionState)
        && Objects.equals(
            _preconditionStateIndependentConstraints, rhs._preconditionStateIndependentConstraints)
        && Objects.equals(_preconditionStates, rhs._preconditionStates);
  }
}
