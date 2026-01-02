package org.batfish.datamodel.routing_policy.as_path;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.expr.IntComparison;

/**
 * An {@link AsPathMatchExpr} matching the length of an {@link org.batfish.datamodel.AsPath} via an
 * {@link IntComparison}.
 */
public final class HasAsPathLength extends AsPathMatchExpr {

  public static @Nonnull HasAsPathLength of(IntComparison comparison) {
    return new HasAsPathLength(comparison);
  }

  @Override
  public <T, U> T accept(AsPathMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitHasAsPathLength(this, arg);
  }

  @JsonProperty(PROP_COMPARISON)
  public @Nonnull IntComparison getComparison() {
    return _comparison;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof HasAsPathLength)) {
      return false;
    }
    HasAsPathLength that = (HasAsPathLength) obj;
    return _comparison.equals(that._comparison);
  }

  @Override
  public int hashCode() {
    return _comparison.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("comparison", _comparison).toString();
  }

  private static final String PROP_COMPARISON = "comparison";

  @JsonCreator
  private static @Nonnull HasAsPathLength create(
      @JsonProperty(PROP_COMPARISON) @Nullable IntComparison comparison) {
    checkArgument(comparison != null, "Missing %s", PROP_COMPARISON);
    return of(comparison);
  }

  private final @Nonnull IntComparison _comparison;

  private HasAsPathLength(IntComparison comparison) {
    _comparison = comparison;
  }
}
