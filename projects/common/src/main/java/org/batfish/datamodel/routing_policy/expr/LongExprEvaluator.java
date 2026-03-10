package org.batfish.datamodel.routing_policy.expr;

import javax.annotation.Nonnull;

/** Concretely evaluates a {@link LongExpr}, resulting in a long. */
public final class LongExprEvaluator implements LongExprVisitor<Long, Void> {

  public static @Nonnull LongExprEvaluator instance() {
    return INSTANCE;
  }

  @Override
  public Long visitAsnValue(AsnValue asnValue, Void arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long visitDecrementLocalPreference(
      DecrementLocalPreference decrementLocalPreference, Void arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long visitDecrementMetric(DecrementMetric decrementMetric, Void arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long visitIgpCost(IgpCost igpCost, Void arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long visitIncrementLocalPreference(
      IncrementLocalPreference incrementLocalPreference, Void arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long visitIncrementMetric(IncrementMetric incrementMetric, Void arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long visitLiteralLong(LiteralLong literalLong, Void arg) {
    return literalLong.getValue();
  }

  @Override
  public Long visitUint32HighLowExpr(Uint32HighLowExpr uint32HighLowExpr) {
    long high = uint32HighLowExpr.getHighExpr().accept(IntExprEvaluator.instance(), null);
    assert 0L <= high && high <= 0xFFFFL;
    long low = uint32HighLowExpr.getLowExpr().accept(IntExprEvaluator.instance(), null);
    assert 0L <= low && low <= 0xFFFFL;
    return (high << 16) | low;
  }

  @Override
  public Long visitVarLong(VarLong varLong, Void arg) {
    // TODO: support long variable evaluation
    throw new UnsupportedOperationException();
  }

  private static final LongExprEvaluator INSTANCE = new LongExprEvaluator();
}
