package org.batfish.z3.expr;

public class IdExpr extends Expr {

  private String _id;

  public IdExpr(String id) {
    _id = id;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitIdExpr(this);
  }

  public String getId() {
    return _id;
  }
}
