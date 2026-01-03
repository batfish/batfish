package org.batfish.datamodel.routing_policy.expr;

/** A visitor of {@link LongExpr} that takes 1 generic argument and returns a generic value. */
public interface LongExprVisitor<T, U> {

  T visitAsnValue(AsnValue asnValue, U arg);

  T visitDecrementLocalPreference(DecrementLocalPreference decrementLocalPreference, U arg);

  T visitDecrementMetric(DecrementMetric decrementMetric, U arg);

  T visitIgpCost(IgpCost igpCost, U arg);

  T visitIncrementLocalPreference(IncrementLocalPreference incrementLocalPreference, U arg);

  T visitIncrementMetric(IncrementMetric incrementMetric, U arg);

  T visitLiteralLong(LiteralLong literalLong, U arg);

  T visitUint32HighLowExpr(Uint32HighLowExpr uint32HighLowExpr);

  T visitVarLong(VarLong varLong, U arg);
}
