package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import java.util.List;

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

  public List<BooleanExpr> getConjuncts() {
    return _conjuncts;
  }
}
