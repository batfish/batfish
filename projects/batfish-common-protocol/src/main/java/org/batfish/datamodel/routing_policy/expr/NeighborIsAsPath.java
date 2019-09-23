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
public final class NeighborIsAsPath extends BooleanExpr {
  private static final String PROP_EXACT = "exact";
  private static final String PROP_RANGE = "range";

  private boolean _exact;
  @Nonnull private final List<SubRangeExpr> _range;

  @JsonCreator
  private static NeighborIsAsPath jsonCreator(
      @Nullable @JsonProperty(PROP_EXACT) Boolean exact,
      @Nullable @JsonProperty(PROP_RANGE) List<SubRangeExpr> range) {
    checkArgument(exact != null, "%s must be provided", PROP_EXACT);
    return new NeighborIsAsPath(firstNonNull(range, ImmutableList.of()), exact);
  }

  public NeighborIsAsPath(List<SubRangeExpr> range, boolean exact) {
    _range = range;
    _exact = exact;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitNeighborIsAsPath(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    throw new BatfishException("No implementation for NeighborIsAsPath.evaluate()");
  }

  @JsonProperty(PROP_EXACT)
  public boolean getExact() {
    return _exact;
  }

  @JsonProperty(PROP_RANGE)
  @Nonnull
  public List<SubRangeExpr> getRange() {
    return _range;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof NeighborIsAsPath)) {
      return false;
    }
    NeighborIsAsPath other = (NeighborIsAsPath) obj;
    return _exact == other._exact && _range.equals(other._range);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_exact, _range);
  }
}
