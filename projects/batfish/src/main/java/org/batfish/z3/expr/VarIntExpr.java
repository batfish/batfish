package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.Field;
import org.batfish.z3.expr.visitors.ExprVisitor;

public final class VarIntExpr extends IntExpr {

  private final Field _field;

  public VarIntExpr(Field field) {
    _field = field;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitVarIntExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return Objects.equals(_field, ((VarIntExpr) e)._field);
  }

  public Field getField() {
    return _field;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_field);
  }

  @Override
  public int numBits() {
    return _field.getSize();
  }
}
