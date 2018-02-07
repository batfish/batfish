package org.batfish.z3.expr;

import org.batfish.datamodel.Ip;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.IntExprVisitor;

public class LitIntExpr extends IntExpr {

  private final int _bits;

  private final long _num;

  public LitIntExpr(Ip ip) {
    _num = ip.asLong();
    _bits = 32;
  }

  public LitIntExpr(long num, int bits) {
    _num = num;
    _bits = bits;
  }

  public LitIntExpr(long num, int low, int high) {
    _bits = high - low + 1;
    _num = num >> low;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitLitIntExpr(this);
  }

  @Override
  public void accept(IntExprVisitor visitor) {
    visitor.visitLitIntExpr(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof LitIntExpr) {
      LitIntExpr rhs = (LitIntExpr) o;
      return _bits == rhs._bits && _num == rhs._num;
    }
    return false;
  }

  public int getBits() {
    return _bits;
  }

  public long getNum() {
    return _num;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + _bits;
    result = prime * result + Long.hashCode(_num);
    return result;
  }
}
