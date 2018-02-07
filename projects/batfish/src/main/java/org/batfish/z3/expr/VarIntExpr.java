package org.batfish.z3.expr;

import org.batfish.z3.HeaderField;

public class VarIntExpr extends IntExpr {

  private HeaderField _headerField;

  public VarIntExpr(HeaderField headerField) {
    _headerField = headerField;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitVarIntExpr(this);
  }

  @Override
  public void accept(IntExprVisitor visitor) {
    visitor.visitVarIntExpr(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof VarIntExpr) {
      VarIntExpr rhs = (VarIntExpr) o;
      return _headerField == rhs._headerField;
    }
    return false;
  }

  public HeaderField getHeaderField() {
    return _headerField;
  }

  @Override
  public int hashCode() {
    return _headerField.hashCode();
  }
}
