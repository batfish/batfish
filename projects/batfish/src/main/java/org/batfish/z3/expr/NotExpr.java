package org.batfish.z3.expr;

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

  public BooleanExpr getArg() {
    return _arg;
  }
}
