package org.batfish.datamodel.packet_policy;

/** Evaluates {@link BoolExpr} */
public interface BoolExprVisitor<T> {

  default T visit(BoolExpr expr) {
    return expr.accept(this);
  }

  T visitPacketMatchExpr(PacketMatchExpr expr);

  T visitTrueExpr(TrueExpr expr);

  T visitFalseExpr(FalseExpr expr);

  T visitFibLookupOutgoingInterfaceIsOneOf(FibLookupOutgoingInterfaceIsOneOf expr);

  T visitConjunction(Conjunction expr);
}
