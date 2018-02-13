package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class RuleExpr extends Statement {

  private final BooleanExpr _subExpression;

  public RuleExpr(BooleanExpr antecedent, StateExpr consequent) {
    _subExpression = new IfExpr(antecedent, consequent);
  }

  public RuleExpr(StateExpr subExpression) {
    _subExpression = subExpression;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitRuleExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_subExpression, ((RuleExpr) e)._subExpression);
  }

  public BooleanExpr getSubExpression() {
    return _subExpression;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_subExpression);
  }
}
