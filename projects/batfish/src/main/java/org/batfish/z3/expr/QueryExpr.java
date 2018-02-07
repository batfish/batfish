package org.batfish.z3.expr;

import org.batfish.z3.expr.visitors.ExprVisitor;

public class QueryExpr extends Statement {

  private final BooleanExpr _subExpression;

  public QueryExpr(BooleanExpr expr) {
    _subExpression = expr;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitQueryExpr(this);
  }

  public BooleanExpr getSubExpression() {
    return _subExpression;
  }
}
