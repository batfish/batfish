package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nonnull;

/**
 * Concretely evaluates an {@link LongMatchExpr} against the result of evaluating an {@link
 * LongExpr}, resulting in a boolean.
 */
public final class LongMatchExprEvaluator implements LongMatchExprVisitor<Boolean, LongExpr> {

  // TODO: support VarLong

  public static @Nonnull LongMatchExprEvaluator instance() {
    return INSTANCE;
  }

  @Override
  public @Nonnull Boolean visitLongComparison(LongComparison longComparison, LongExpr arg) {
    return longComparison
        .getComparator()
        .apply(
            arg.accept(LongExprEvaluator.instance(), null),
            longComparison.getExpr().accept(LongExprEvaluator.instance(), null))
        .getBooleanValue();
  }

  @Override
  public @Nonnull Boolean visitLongMatchAll(LongMatchAll longMatchAll, LongExpr arg) {
    return longMatchAll.getExprs().stream().allMatch(expr -> expr.accept(this, arg));
  }

  private static final LongMatchExprEvaluator INSTANCE = new LongMatchExprEvaluator();
}
