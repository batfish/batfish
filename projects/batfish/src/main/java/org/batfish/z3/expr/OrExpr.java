package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import java.util.List;

public class OrExpr extends BooleanExpr {

  private List<BooleanExpr> _disjuncts;

  public OrExpr(List<BooleanExpr> disjuncts) {
    _disjuncts =
        disjuncts.isEmpty()
            ? ImmutableList.of(FalseExpr.INSTANCE)
            : ImmutableList.copyOf(disjuncts);
  }

  @Override
  public void accept(BooleanExprVisitor visitor) {
    visitor.visitOrExpr(this);
  }

  @Override
  public void accept(ExprVisitor visitor) {
    visitor.visitOrExpr(this);
  }

  public List<BooleanExpr> getDisjuncts() {
    return _disjuncts;
  }
}
