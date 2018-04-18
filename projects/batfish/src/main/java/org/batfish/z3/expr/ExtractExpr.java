package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.Field;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericIntExprVisitor;
import org.batfish.z3.expr.visitors.IntExprVisitor;

/**
 * Represents a projection of a bitvector to a bitvector of a lower dimension. The output consists
 * of a contiguous subvector of the original.
 */
public final class ExtractExpr extends IntExpr {

  public static IntExpr newExtractExpr(Field field, int low, int high) {
    return newExtractExpr(new VarIntExpr(field), low, high);
  }

  public static IntExpr newExtractExpr(IntExpr var, int low, int high) {
    int varSize = var.numBits();
    return newExtractExpr(var, varSize, low, high);
  }

  private static IntExpr newExtractExpr(IntExpr var, int varSize, int low, int high) {
    if (low == 0 && high == varSize - 1) {
      return var;
    } else {
      return new ExtractExpr(var, low, high);
    }
  }

  private final int _high;

  private final int _low;

  private final IntExpr _var;

  private ExtractExpr(IntExpr var, int low, int high) {
    _low = low;
    _high = high;
    _var = var;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitExtractExpr(this);
  }

  @Override
  public <R> R accept(GenericIntExprVisitor<R> visitor) {
    return visitor.visitExtractExpr(this);
  }

  @Override
  public void accept(IntExprVisitor visitor) {
    visitor.visitExtractExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    ExtractExpr other = (ExtractExpr) e;
    return Objects.equals(_high, other._high)
        && Objects.equals(_low, other._low)
        && Objects.equals(_var, other._var);
  }

  public int getHigh() {
    return _high;
  }

  public int getLow() {
    return _low;
  }

  public IntExpr getVar() {
    return _var;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_high, _low, _var);
  }

  @Override
  public int numBits() {
    return _high - _low + 1;
  }
}
