package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.ExprVisitor;

public class RuleExpr extends Statement {

  private final BooleanExpr _subExpression;

  public RuleExpr(BooleanExpr subExpression) {
    _subExpression = subExpression;
  }

  public RuleExpr(BooleanExpr antecedent, BooleanExpr consequent) {
    _subExpression = new IfExpr(antecedent, consequent);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitRuleExpr(this);
  }

  public BooleanExpr getSubExpression() {
    return _subExpression;
  }
}
