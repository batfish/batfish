package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nonnull;

/** Concretely evaluates an {@link IntExpr}, resulting in an int. */
public final class IntExprEvaluator implements IntExprVisitor<Integer, Void> {

  public static @Nonnull IntExprEvaluator instance() {
    return INSTANCE;
  }

  @Override
  public Integer visitLiteralInt(LiteralInt literalInt, Void arg) {
    return literalInt.getValue();
  }

  @Override
  public Integer visitVarInt(VarInt varInt, Void arg) {
    // TODO: support integer variable evaluation
    throw new UnsupportedOperationException();
  }

  private static final IntExprEvaluator INSTANCE = new IntExprEvaluator();
}
