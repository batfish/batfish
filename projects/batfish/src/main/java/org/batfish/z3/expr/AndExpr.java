package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import org.batfish.z3.expr.visitors.BooleanExprVisitor;
import org.batfish.z3.expr.visitors.ExprVisitor;

public class AndExpr extends BooleanExpr {

  private List<BooleanExpr> _conjuncts;

  public AndExpr(List<BooleanExpr> conjuncts) {
    _conjuncts =
        conjuncts.isEmpty() ? ImmutableList.of(TrueExpr.INSTANCE) : ImmutableList.copyOf(conjuncts);
  }

  @Override
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitAndExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitAndExpr(this);
  }

  @Override
  public boolean exprEquals(Expr e) {
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
