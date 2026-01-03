package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nonnull;

/**
 * Concretely evaluates an {@link IntMatchExpr} against the result of evaluating an {@link IntExpr},
 * resulting in a boolean.
 */
public final class IntMatchExprEvaluator implements IntMatchExprVisitor<Boolean, IntExpr> {

  // TODO: support VarInt

  public static @Nonnull IntMatchExprEvaluator instance() {
    return INSTANCE;
  }

  @Override
  public @Nonnull Boolean visitIntComparison(IntComparison intComparison, IntExpr arg) {
    return intComparison
        .getComparator()
        .apply(
            arg.accept(IntExprEvaluator.instance(), null),
            intComparison.getExpr().accept(IntExprEvaluator.instance(), null))
        .getBooleanValue();
  }

  @Override
  public @Nonnull Boolean visitIntMatchAll(IntMatchAll intMatchAll, IntExpr arg) {
    return intMatchAll.getExprs().stream().allMatch(expr -> expr.accept(this, arg));
  }

  private static final IntMatchExprEvaluator INSTANCE = new IntMatchExprEvaluator();
}
