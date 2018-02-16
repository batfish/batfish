package org.batfish.z3.expr;

import java.util.List;
import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class ListExpr extends Expr {

  private final List<Expr> _subExpressions;

  public ListExpr(List<Expr> subExpressions) {
    _subExpressions = subExpressions;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitListExpr(this);
  }

  @Override
  public final boolean exprEquals(Expr e) {
    return Objects.equals(_subExpressions, ((ListExpr) e)._subExpressions);
  }

  public List<Expr> getSubExpressions() {
    return _subExpressions;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(_subExpressions);
  }
}
