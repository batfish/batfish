package org.batfish.z3.expr;

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

  public HeaderField getHeaderField() {
    return _headerField;
  }
}
