package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;

public final class IdExpr extends Expr {

  private String _id;

  public IdExpr(String id) {
    _id = id;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitIdExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return Objects.equals(_id, ((IdExpr) e)._id);
  }

  public String getId() {
    return _id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id);
  }
}
