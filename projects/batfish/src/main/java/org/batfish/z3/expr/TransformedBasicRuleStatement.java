package org.batfish.z3.expr;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;

/**
 * A @{link RuleStatement} by which a postcondition state reached by a transformed header is
 * produced by a set of precondition state(s) reached by the untransformed header; a set of
 * precondition state(s) reached by the transformed header; and state-independent constraints
 */
public class TransformedBasicRuleStatement extends RuleStatement {

  private final BasicStateExpr _postconditionPostTransformationState;

  private final Set<BasicStateExpr> _preconditionPostTransformationStates;

  private final Set<BasicStateExpr> _preconditionPreTransformationStates;

  private final BooleanExpr _preconditionStateIndependentConstraints;

  private final Set<TransformationStateExpr> _preconditionTransformationStates;

  public TransformedBasicRuleStatement(
      BooleanExpr preconditionStateIndependentConstraints,
      Set<BasicStateExpr> preconditionPreTransformationStates,
      Set<BasicStateExpr> preconditionPostTransformationStates,
      Set<TransformationStateExpr> preconditionTransformationStates,
      BasicStateExpr postconditionPostTransformationState) {
    _preconditionStateIndependentConstraints = preconditionStateIndependentConstraints;
    _preconditionPreTransformationStates = preconditionPreTransformationStates;
    _preconditionPostTransformationStates = preconditionPostTransformationStates;
    _preconditionTransformationStates = preconditionTransformationStates;
    _postconditionPostTransformationState = postconditionPostTransformationState;
  }

  public TransformedBasicRuleStatement(
      TransformationStateExpr preconditionTransformationState,
      BasicStateExpr postconditionPostTransformationState) {
    this(
        TrueExpr.INSTANCE,
        ImmutableSet.of(),
        ImmutableSet.of(),
        ImmutableSet.of(preconditionTransformationState),
        postconditionPostTransformationState);
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitTransformedBasicRuleStatement(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitTransformedBasicRuleStatement(this);
  }

  public BasicStateExpr getPostconditionPostTransformationState() {
    return _postconditionPostTransformationState;
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
        _postconditionPostTransformationState,
        _preconditionPostTransformationStates,
        _preconditionPreTransformationStates,
        _preconditionStateIndependentConstraints,
        _preconditionTransformationStates);
  }

  @Override
  public boolean statementEquals(Statement e) {
    TransformedBasicRuleStatement rhs = (TransformedBasicRuleStatement) e;
    return Objects.equals(
            _postconditionPostTransformationState, rhs._postconditionPostTransformationState)
        && Objects.equals(
            _preconditionPostTransformationStates, rhs._preconditionPostTransformationStates)
        && Objects.equals(
            _preconditionPreTransformationStates, rhs._preconditionPreTransformationStates)
        && Objects.equals(
            _preconditionStateIndependentConstraints, rhs._preconditionStateIndependentConstraints)
        && Objects.equals(_preconditionTransformationStates, rhs._preconditionTransformationStates);
  }
}
