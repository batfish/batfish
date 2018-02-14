package org.batfish.z3.expr;

import java.util.Objects;

public class RuleStatement extends Statement {

  private final BooleanExpr _subExpression;

  public RuleStatement(BooleanExpr antecedent, StateExpr consequent) {
    _subExpression = new IfExpr(antecedent, consequent);
  }

  public RuleStatement(StateExpr subExpression) {
    _subExpression = subExpression;
  }

  @Override
  public <T> T accept(GenericStatementVisitor<T> visitor) {
    return visitor.visitRuleStatement(this);
  }

  @Override
  public void accept(VoidStatementVisitor visitor) {
    visitor.visitRuleStatement(this);
  }

  @Override
  public boolean statementEquals(Statement e) {
    return Objects.equals(_subExpression, ((RuleStatement) e)._subExpression);
  }

  public BooleanExpr getSubExpression() {
    return _subExpression;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_subExpression);
  }
}
