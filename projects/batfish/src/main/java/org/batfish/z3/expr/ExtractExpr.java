package org.batfish.z3.expr;

import org.batfish.z3.HeaderField;

public class ExtractExpr extends IntExpr {

  public static IntExpr newExtractExpr(HeaderField var, int low, int high) {
    int varSize = var.getSize();
    return newExtractExpr(var, varSize, low, high);
  }

  private static IntExpr newExtractExpr(HeaderField var, int varSize, int low, int high) {
    if (low == 0 && high == varSize - 1) {
      return new VarIntExpr(var);
    } else {
      return new ExtractExpr(var, low, high);
    }
  }

  private final int _high;

  private final int _low;

  private final VarIntExpr _var;

  private ExtractExpr(HeaderField var, int low, int high) {
    _low = low;
    _high = high;
    _var = new VarIntExpr(var);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitExtractExpr(this);
  }

  @Override
  public void accept(IntExprVisitor visitor) {
    visitor.visitExtractExpr(this);
  }

  public int getHigh() {
    return _high;
  }

  public int getLow() {
    return _low;
  }

  public VarIntExpr getVar() {
    return _var;
  }
}
