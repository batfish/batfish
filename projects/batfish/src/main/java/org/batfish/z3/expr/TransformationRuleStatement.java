package org.batfish.z3.expr;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;

/**
 * A @{link RuleStatement} by which a postcondition state reached by a pair of a header and a
 * transformed version it is produced by a set of precondition state(s) reached by the untransformed
 * header; a set of precondition state(s) reached by the transformed header; a set of precondition
 * states reached by the pair; and state-independent constraints
 */
public class TransformationRuleStatement extends RuleStatement {

  private final StateExpr _postconditionState;

  private final Set<StateExpr> _preconditionPostTransformationStates;

  private final Set<StateExpr> _preconditionPreTransformationStates;

  private final BooleanExpr _preconditionStateIndependentConstraints;

  public TransformationRuleStatement(
      BooleanExpr preconditionStateIndependentConstraints,
      Set<StateExpr> preconditionPreTransformationStates,
      Set<StateExpr> preconditionPostTransformationStates,
      StateExpr postconditionTransformationState) {
    _preconditionStateIndependentConstraints = preconditionStateIndependentConstraints;
    _preconditionPreTransformationStates = preconditionPreTransformationStates;
    _preconditionPostTransformationStates = preconditionPostTransformationStates;
    _postconditionState = postconditionTransformationState;
  }

  public TransformationRuleStatement(StateExpr postconditionTransformationState) {
    this(TrueExpr.INSTANCE, ImmutableSet.of(), ImmutableSet.of(), postconditionTransformationState);
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitTransformationRuleStatement(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitTransformationRuleStatement(this);
  }

  public StateExpr getPostconditionTransformationState() {
    return _postconditionState;
  }

  public Set<StateExpr> getPreconditionPostTransformationStates() {
    return _preconditionPostTransformationStates;
  }

  public Set<StateExpr> getPreconditionPreTransformationStates() {
    return _preconditionPreTransformationStates;
  }

  public BooleanExpr getPreconditionStateIndependentConstraints() {
    return _preconditionStateIndependentConstraints;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _postconditionState,
        _preconditionPostTransformationStates,
        _preconditionPreTransformationStates,
        _preconditionStateIndependentConstraints);
  }

  @Override
  public boolean statementEquals(Statement e) {
    TransformationRuleStatement rhs = (TransformationRuleStatement) e;
    return Objects.equals(_postconditionState, rhs._postconditionState)
        && Objects.equals(
            _preconditionPostTransformationStates, rhs._preconditionPostTransformationStates)
        && Objects.equals(
            _preconditionPreTransformationStates, rhs._preconditionPreTransformationStates)
        && Objects.equals(
            _preconditionStateIndependentConstraints, rhs._preconditionStateIndependentConstraints);
  }
}
