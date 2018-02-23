package org.batfish.z3.expr;

import java.util.Objects;
import java.util.Set;

/**
 * A @{link RuleStatement} by which a postcondition state reached by a transformed header is
 * produced by a set of precondition state(s) reached by the untransformed header; a set of
 * precondition state(s) reached by the transformed header; and state-independent constraints
 */
public class TransformedBasicRuleStatement extends RuleStatement {

  private final BasicStateExpr _postconditionPostTransformationState;

  private final Set<BasicStateExpr> _preconditionPreTransformationStates;

  private final BooleanExpr _preconditionStateIndependent;

  private final Set<TransformationStateExpr> _preconditionTransformationStates;

  public TransformedBasicRuleStatement(
      BasicStateExpr postconditionPostTransformationState,
      Set<BasicStateExpr> preconditionPreTransformationStates,
      BooleanExpr preconditionStateIndependent,
      Set<TransformationStateExpr> preconditionTransformationStates) {
    _postconditionPostTransformationState = postconditionPostTransformationState;
    _preconditionPreTransformationStates = preconditionPreTransformationStates;
    _preconditionStateIndependent = preconditionStateIndependent;
    _preconditionTransformationStates = preconditionTransformationStates;
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitTransformedBasicRuleStatement(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitTransformedBasicRuleStatement(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _postconditionPostTransformationState,
        _preconditionPreTransformationStates,
        _preconditionStateIndependent,
        _preconditionTransformationStates);
  }

  @Override
  public boolean statementEquals(Statement e) {
    TransformedBasicRuleStatement rhs = (TransformedBasicRuleStatement) e;
    return Objects.equals(
            _postconditionPostTransformationState, rhs._postconditionPostTransformationState)
        && Objects.equals(
            _preconditionPreTransformationStates, rhs._postconditionPostTransformationState)
        && Objects.equals(_preconditionStateIndependent, rhs._preconditionStateIndependent)
        && Objects.equals(_preconditionTransformationStates, rhs._preconditionTransformationStates);
  }
}
