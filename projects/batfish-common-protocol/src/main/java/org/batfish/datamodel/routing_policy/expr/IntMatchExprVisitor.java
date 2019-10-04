package org.batfish.datamodel.routing_policy.expr;

/** A visitor of {@link IntMatchExpr} that takes 1 generic argument and returns a generic value. */
public interface IntMatchExprVisitor<T, U> {

  T visitIntComparison(IntComparison intComparison, U arg);

  T visitIntMatchAll(IntMatchAll intMatchAll, U arg);
}
