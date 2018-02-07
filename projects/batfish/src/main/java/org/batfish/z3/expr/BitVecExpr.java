package org.batfish.z3.expr;

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

  public int getSize() {
    return _size;
  }
}
