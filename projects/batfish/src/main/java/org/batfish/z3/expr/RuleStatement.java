package org.batfish.z3.expr;

/** A statement describing the preconditions necessary to reach a specified target state */
public abstract class RuleStatement extends Statement {

  //  private final BooleanExpr _subExpression;

  //  public RuleStatement(BooleanExpr antecedent, StateExpr consequent) {
  //    _subExpression = new IfExpr(antecedent, consequent);
  //  }

  //  public RuleStatement(StateExpr subExpression) {
  //    _subExpression = subExpression;
  //  }

  //  public BooleanExpr getSubExpression() {
  //    return _subExpression;
  //  }

  //  public abstract BooleanExpr getSubExpression();

  //  @Override
  //  public int hashCode() {
  //    return Objects.hash(_subExpression);
  //  }
  //
  //  @Override
  //  public boolean statementEquals(Statement e) {
  //    return Objects.equals(_subExpression, ((RuleStatement) e)._subExpression);
  //  }
}
