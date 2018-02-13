package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.HeaderField;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.IntExprVisitor;

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
  public boolean exprEquals(Expr e) {
    return Objects.equals(_headerField, ((VarIntExpr) e)._headerField);
  }

  public HeaderField getHeaderField() {
    return _headerField;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_headerField);
  }

  @Override
  public int numBits() {
    return _headerField.getSize();
  }
}
