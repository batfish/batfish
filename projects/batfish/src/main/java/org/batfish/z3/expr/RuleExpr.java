package org.batfish.z3.expr;

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

  public BooleanExpr getSubExpression() {
    return _subExpression;
  }
}
