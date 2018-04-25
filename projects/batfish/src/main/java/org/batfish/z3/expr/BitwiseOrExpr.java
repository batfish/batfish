package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericIntExprVisitor;
import org.batfish.z3.expr.visitors.IntExprVisitor;

public class BitwiseOrExpr extends IntExpr {
  private final IntExpr _expr1;

  private final IntExpr _expr2;

  public BitwiseOrExpr(IntExpr expr1, IntExpr expr2) {
    assert expr1.numBits() == expr2.numBits();
    _expr1 = expr1;
    _expr2 = expr2;
  }

  @Override
  public <R> R accept(GenericIntExprVisitor<R> visitor) {
    return visitor.visitBitwiseOrExpr(this);
  }

  @Override
  public void accept(IntExprVisitor visitor) {
    visitor.visitBitwiseOrExpr(this);
  }

  @Override
  public int numBits() {
    return _expr1.numBits();
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitBitwiseOrExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    BitwiseOrExpr other = (BitwiseOrExpr) e;
    return Objects.equals(_expr1, other._expr1) && Objects.equals(_expr2, other._expr2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expr1, _expr2);
  }

  public IntExpr getExpr1() {
    return _expr1;
  }

  public IntExpr getExpr2() {
    return _expr2;
  }
}
