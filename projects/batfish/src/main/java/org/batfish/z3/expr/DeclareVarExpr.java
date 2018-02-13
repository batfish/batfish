package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.HeaderField;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class DeclareVarExpr extends Statement {

  private final HeaderField _headerField;

  public DeclareVarExpr(HeaderField headerField) {
    _headerField = headerField;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitDeclareVarExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
    return Objects.equals(_headerField, ((DeclareVarExpr) e)._headerField);
  }

  public HeaderField getHeaderField() {
    return _headerField;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_headerField);
  }
}
