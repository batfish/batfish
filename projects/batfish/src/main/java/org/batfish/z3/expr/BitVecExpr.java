package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class BitVecExpr extends TypeExpr {

  private final int _size;

  public BitVecExpr(int size) {
    _size = size;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitBitVecExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_size, ((BitVecExpr) e)._size);
  }

  public int getSize() {
    return _size;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_size);
  }
}
