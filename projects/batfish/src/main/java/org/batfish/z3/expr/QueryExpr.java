package org.batfish.z3.expr;

import java.util.Objects;
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

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_subExpression, ((QueryExpr) e)._subExpression);
  }

  public BooleanExpr getSubExpression() {
    return _subExpression;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_subExpression);
  }
}
