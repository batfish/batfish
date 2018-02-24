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

  private final TransformationStateExpr _postconditionTransformationState;

  private final Set<BasicStateExpr> _preconditionPostTransformationStates;

  private final Set<BasicStateExpr> _preconditionPreTransformationStates;

  private final BooleanExpr _preconditionStateIndependentConstraints;

  private final Set<TransformationStateExpr> _preconditionTransformationStates;

  public TransformationRuleStatement(
      BooleanExpr preconditionStateIndependentConstraints,
      Set<BasicStateExpr> preconditionPreTransformationStates,
      Set<BasicStateExpr> preconditionPostTransformationStates,
      Set<TransformationStateExpr> preconditionTransformationStates,
      TransformationStateExpr postconditionTransformationState) {
    _preconditionStateIndependentConstraints = preconditionStateIndependentConstraints;
    _preconditionPreTransformationStates = preconditionPreTransformationStates;
    _preconditionPostTransformationStates = preconditionPostTransformationStates;
    _preconditionTransformationStates = preconditionTransformationStates;
    _postconditionTransformationState = postconditionTransformationState;
  }

  public TransformationRuleStatement(TransformationStateExpr postconditionTransformationState) {
    this(
        TrueExpr.INSTANCE,
        ImmutableSet.of(),
        ImmutableSet.of(),
        ImmutableSet.of(),
        postconditionTransformationState);
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitTransformationRuleStatement(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitTransformationRuleStatement(this);
  }

  public TransformationStateExpr getPostconditionTransformationState() {
    return _postconditionTransformationState;
  }

  public Set<BasicStateExpr> getPreconditionPostTransformationStates() {
    return _preconditionPostTransformationStates;
  }

  public Set<BasicStateExpr> getPreconditionPreTransformationStates() {
    return _preconditionPreTransformationStates;
  }

  public BooleanExpr getPreconditionStateIndependentConstraints() {
    return _preconditionStateIndependentConstraints;
  }

  public Set<TransformationStateExpr> getPreconditionTransformationStates() {
    return _preconditionTransformationStates;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _postconditionTransformationState,
        _preconditionPostTransformationStates,
        _preconditionPreTransformationStates,
        _preconditionStateIndependentConstraints,
        _preconditionTransformationStates);
  }

  @Override
  public boolean statementEquals(Statement e) {
    TransformationRuleStatement rhs = (TransformationRuleStatement) e;
    return Objects.equals(_postconditionTransformationState, rhs._postconditionTransformationState)
        && Objects.equals(
            _preconditionPostTransformationStates, rhs._preconditionPostTransformationStates)
        && Objects.equals(
            _preconditionPreTransformationStates, rhs._preconditionPreTransformationStates)
        && Objects.equals(
            _preconditionStateIndependentConstraints, rhs._preconditionStateIndependentConstraints)
        && Objects.equals(_preconditionTransformationStates, rhs._preconditionTransformationStates);
  }
}
