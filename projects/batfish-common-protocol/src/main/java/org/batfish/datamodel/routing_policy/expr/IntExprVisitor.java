package org.batfish.datamodel.routing_policy.expr;

/** A visitor of {@link IntExpr} that takes 1 generic argument and returns a generic value. */
public interface IntExprVisitor<T, U> {

  T visitLiteralInt(LiteralInt literalInt, U arg);

  T visitVarInt(VarInt varInt, U arg);
}
