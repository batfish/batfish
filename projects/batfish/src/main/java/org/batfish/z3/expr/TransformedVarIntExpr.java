package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.Field;
import org.batfish.z3.expr.visitors.ExprVisitor;

public final class TransformedVarIntExpr extends IntExpr {

  private final Field _field;

  public TransformedVarIntExpr(Field field) {
    _field = field;
  }

  public TransformedVarIntExpr(String name, Integer size) {
    _field = new Field(name, size);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitTransformedVarIntExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return Objects.equals(_field, ((TransformedVarIntExpr) e)._field);
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
