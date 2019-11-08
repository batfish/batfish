package org.batfish.datamodel.routing_policy.expr;

/** A visitor of {@link LongMatchExpr} that takes 1 generic argument and returns a generic value. */
public interface LongMatchExprVisitor<T, U> {

  T visitLongComparison(LongComparison longComparison, U arg);

  T visitLongMatchAll(LongMatchAll longMatchAll, U arg);
}
