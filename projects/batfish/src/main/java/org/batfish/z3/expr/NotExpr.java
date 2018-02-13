package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.BooleanExprVisitor;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class NotExpr extends BooleanExpr {

  private final BooleanExpr _arg;

  public NotExpr(BooleanExpr arg) {
    _arg = arg;
  }

  @Override
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitNotExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitNotExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_arg, ((NotExpr) e)._arg);
  }

  public BooleanExpr getArg() {
    return _arg;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_arg);
  }
}
