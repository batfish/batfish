package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.datamodel.Ip;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericIntExprVisitor;
import org.batfish.z3.expr.visitors.IntExprVisitor;

public final class LitIntExpr extends IntExpr {

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
  public <R> R accept(GenericIntExprVisitor<R> visitor) {
    return visitor.visitLitIntExpr(this);
  }

  @Override
  public void accept(IntExprVisitor visitor) {
    visitor.visitLitIntExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    LitIntExpr other = (LitIntExpr) e;
    return Objects.equals(_bits, other._bits) && Objects.equals(_num, other._num);
  }

  public int getBits() {
    return _bits;
  }

  public long getNum() {
    return _num;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_bits, _num);
  }

  @Override
  public int numBits() {
    return _bits;
  }
}
