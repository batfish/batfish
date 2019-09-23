package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class OriginatesFromAsPath extends BooleanExpr {
  private static final String PROP_AS_RANGE = "asRange";
  private static final String PROP_EXACT = "exact";

  @Nonnull private final List<SubRangeExpr> _asRange;
  private final boolean _exact;

  @JsonCreator
  private static OriginatesFromAsPath jsonCreator(
      @Nullable @JsonProperty(PROP_AS_RANGE) List<SubRangeExpr> asRange,
      @Nullable @JsonProperty(PROP_EXACT) Boolean exact) {
    checkArgument(exact != null, "%s must be provided", PROP_EXACT);
    return new OriginatesFromAsPath(firstNonNull(asRange, ImmutableList.of()), exact);
  }

  public OriginatesFromAsPath(List<SubRangeExpr> asRange, boolean exact) {
    _asRange = asRange;
    _exact = exact;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitOriginatesFromAsPath(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new BatfishException("No implementation for OriginatesFromAsPath.evaluate()");
  }

  @JsonProperty(PROP_AS_RANGE)
  @Nonnull
  public List<SubRangeExpr> getAsRange() {
    return _asRange;
  }

  @JsonProperty(PROP_EXACT)
  public boolean getExact() {
    return _exact;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof OriginatesFromAsPath)) {
      return false;
    }

    OriginatesFromAsPath other = (OriginatesFromAsPath) obj;
    return Objects.equals(_asRange, other._asRange) && _exact == other._exact;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asRange, _exact);
  }
}
