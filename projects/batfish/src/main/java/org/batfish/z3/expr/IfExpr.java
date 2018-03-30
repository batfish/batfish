package org.batfish.z3.expr;

import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public final class IfExpr extends BooleanExpr {

  private final BooleanExpr _antecedent;

  private final BooleanExpr _consequent;

  public IfExpr(BooleanExpr antecedent, BooleanExpr consequent) {
    _antecedent = antecedent;
    _consequent = consequent;
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitIfExpr(this);
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    return visitor.visitIfExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    IfExpr other = (IfExpr) e;
    return Objects.equals(_antecedent, other._antecedent)
        && Objects.equals(_consequent, other._consequent);
  }

  public BooleanExpr getAntecedent() {
    return _antecedent;
  }

  public BooleanExpr getConsequent() {
    return _consequent;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_antecedent, _consequent);
  }
}
