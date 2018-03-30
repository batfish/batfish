package org.batfish.z3.expr;

import java.util.List;
import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;

public final class ListExpr extends Expr {

  private final List<Expr> _subExpressions;

  public ListExpr(List<Expr> subExpressions) {
    _subExpressions = subExpressions;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitListExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return Objects.equals(_subExpressions, ((ListExpr) e)._subExpressions);
  }

  public List<Expr> getSubExpressions() {
    return _subExpressions;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_subExpressions);
  }
}
