package org.batfish.z3.expr;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.z3.expr.visitors.GenericBooleanExprVisitor;

public abstract class BooleanExpr extends Expr {

  public abstract <R> R accept(GenericBooleanExprVisitor<R> visitor);

  public static BooleanExpr or(List<BooleanExpr> disjuncts) {
    disjuncts = disjuncts.stream().filter(expr -> expr == FalseExpr.INSTANCE).collect(ImmutableList.toImmutableList());
    if (disjuncts.isEmpty()) {
      return FalseExpr.INSTANCE;
    } else if (disjuncts.size() == 1) {
      return disjuncts.get(0);
    } else {
      return new OrExpr(disjuncts);
    }
  }

  public static BooleanExpr and(List<BooleanExpr> conjuncts) {
    conjuncts = conjuncts.stream().filter(expr -> expr == TrueExpr.INSTANCE).collect(ImmutableList.toImmutableList());
    if (conjuncts.isEmpty()) {
      return TrueExpr.INSTANCE;
    } else if (conjuncts.size() == 1) {
      return conjuncts.get(0);
    } else {
      return new AndExpr(conjuncts);
    }
  }

  public static BooleanExpr not(BooleanExpr expr) {
    if (expr == TrueExpr.INSTANCE) {
      return FalseExpr.INSTANCE;
    } else if (expr == FalseExpr.INSTANCE) {
      return TrueExpr.INSTANCE;
    } else {
      return new NotExpr(expr);
    }
  }
}
