package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import org.batfish.z3.expr.visitors.ExprVisitor;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public final class OrExpr extends BooleanExpr {

  private List<BooleanExpr> _disjuncts;

  public OrExpr(List<BooleanExpr> disjuncts) {
    _disjuncts =
        disjuncts.isEmpty()
            ? ImmutableList.of(FalseExpr.INSTANCE)
            : ImmutableList.copyOf(disjuncts);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitOrExpr(this);
  }

  @Override
  public <R> R accept(GenericBooleanExprVisitor<R> visitor) {
    return visitor.visitOrExpr(this);
  }

  @Override
  protected boolean exprEquals(Expr e) {
    return Objects.equals(_disjuncts, ((OrExpr) e)._disjuncts);
  }

  public List<BooleanExpr> getDisjuncts() {
    return _disjuncts;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_disjuncts);
  }
}
