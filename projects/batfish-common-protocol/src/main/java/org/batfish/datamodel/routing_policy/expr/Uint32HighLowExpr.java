package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;

/**
 * An expression for an unsigned 32-bit integer given by a pair of expressions for its high and low
 * 16-bits.
 */
@ParametersAreNonnullByDefault
public final class Uint32HighLowExpr extends LongExpr {

  public Uint32HighLowExpr(IntExpr highExpr, IntExpr lowExpr) {
    _highExpr = highExpr;
    _lowExpr = lowExpr;
  }

  @Override
  public <T, U> T accept(LongExprVisitor<T, U> visitor, U arg) {
    return visitor.visitUint32HighLowExpr(this);
  }

  @Deprecated
  @Override
  public long evaluate(Environment environment) {
    throw new UnsupportedOperationException();
  }

  @JsonProperty(PROP_HIGH_EXPR)
  public @Nonnull IntExpr getHighExpr() {
    return _highExpr;
  }

  @JsonProperty(PROP_LOW_EXPR)
  public @Nonnull IntExpr getLowExpr() {
    return _lowExpr;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Uint32HighLowExpr)) {
      return false;
    }
    Uint32HighLowExpr rhs = (Uint32HighLowExpr) obj;
    return _highExpr.equals(rhs._highExpr) && _lowExpr.equals(rhs._lowExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_highExpr, _lowExpr);
  }

  private static final String PROP_HIGH_EXPR = "highExpr";
  private static final String PROP_LOW_EXPR = "lowExpr";

  @JsonCreator
  private static @Nonnull Uint32HighLowExpr create(
      @JsonProperty(PROP_HIGH_EXPR) @Nullable IntExpr highExpr,
      @JsonProperty(PROP_LOW_EXPR) @Nullable IntExpr lowExpr) {
    checkArgument(highExpr != null, "Missing %s", PROP_HIGH_EXPR);
    checkArgument(highExpr != null, "Missing %s", PROP_LOW_EXPR);
    return new Uint32HighLowExpr(highExpr, lowExpr);
  }

  private final @Nonnull IntExpr _highExpr;
  private final @Nonnull IntExpr _lowExpr;
}
