package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.Field;
import org.batfish.z3.HeaderField;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericIntExprVisitor;
import org.batfish.z3.expr.visitors.IntExprVisitor;

public class VarIntExpr extends IntExpr {

  private final Field _field;

  public VarIntExpr(HeaderField headerField) {
    _field = new Field(headerField.getName(), headerField.getSize());
  }

  public VarIntExpr(Field field) {
    _field = field;
  }

  public VarIntExpr(String name, Integer size) {
    _field = new Field(name, size);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitVarIntExpr(this);
  }

  @Override
  public <R> R accept(GenericIntExprVisitor<R> visitor) {
    return visitor.visitVarIntExpr(this);
  }

  @Override
  public void accept(IntExprVisitor visitor) {
    visitor.visitVarIntExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
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
