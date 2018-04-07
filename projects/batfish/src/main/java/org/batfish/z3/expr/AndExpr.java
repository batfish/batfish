package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public final class AndExpr extends BooleanExpr {

  private List<BooleanExpr> _conjuncts;

  public AndExpr(List<BooleanExpr> conjuncts) {
    _conjuncts =
        conjuncts.isEmpty() ? ImmutableList.of(TrueExpr.INSTANCE) : ImmutableList.copyOf(conjuncts);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitAndExpr(this);
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    return visitor.visitAndExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return Objects.equals(_conjuncts, ((AndExpr) e)._conjuncts);
  }

  public List<BooleanExpr> getConjuncts() {
    return _conjuncts;
  }

  @Override
  public int hashCode() {
    return _conjuncts.hashCode();
  }
}
