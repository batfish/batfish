package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.HasReadableClusterList;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that returns {@code true} iff the {@link Environment}'s route's BGP Cluster
 * List Length matches a provided {@link IntExpr} using a provided {@link IntComparator}.
 *
 * <p>Assumes a length of 0 if source route does not have a cluster list.
 */
@ParametersAreNonnullByDefault
public final class MatchClusterListLength extends BooleanExpr {
  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchClusterListLength(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    // Treat as 0 if undefined
    int effectiveClusterListLength;
    if (environment.getUseOutputAttributes()
        && environment.getOutputRoute() instanceof HasReadableClusterList) {
      effectiveClusterListLength =
          ((HasReadableClusterList) environment.getOutputRoute()).getClusterList().size();
    } else if (environment.getOriginalRoute() instanceof HasReadableClusterList) {
      effectiveClusterListLength =
          ((HasReadableClusterList) environment.getOriginalRoute()).getClusterList().size();
    } else {
      effectiveClusterListLength = 0;
    }
    int rhs = _rhs.evaluate(environment);
    return _comparator.apply(effectiveClusterListLength, rhs);
  }

  @JsonProperty(PROP_COMPARATOR)
  public @Nonnull IntComparator getComparator() {
    return _comparator;
  }

  @JsonProperty(PROP_RHS)
  public @Nonnull IntExpr getRhs() {
    return _rhs;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchClusterListLength)) {
      return false;
    }
    MatchClusterListLength other = (MatchClusterListLength) obj;
    return _comparator == other._comparator && Objects.equals(_rhs, other._rhs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_comparator.ordinal(), _rhs);
  }

  public static @Nonnull MatchClusterListLength of(IntComparator comparator, IntExpr rhs) {
    return new MatchClusterListLength(comparator, rhs);
  }

  @JsonCreator
  private static MatchClusterListLength create(
      @JsonProperty(PROP_COMPARATOR) @Nullable IntComparator comparator,
      @JsonProperty(PROP_RHS) @Nullable IntExpr rhs) {
    checkArgument(comparator != null, "%s must be provided", PROP_COMPARATOR);
    checkArgument(rhs != null, "%s must be provided", PROP_RHS);
    return new MatchClusterListLength(comparator, rhs);
  }

  private MatchClusterListLength(IntComparator comparator, IntExpr rhs) {
    _comparator = comparator;
    _rhs = rhs;
  }

  private static final String PROP_COMPARATOR = "comparator";
  private static final String PROP_RHS = "rhs";

  private final @Nonnull IntComparator _comparator;
  private final @Nonnull IntExpr _rhs;
}
