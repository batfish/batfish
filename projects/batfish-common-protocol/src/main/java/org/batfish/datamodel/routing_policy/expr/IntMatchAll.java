package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Matches an integer if it is matched by all of the provided {@link IntMatchExpr}s. If empty,
 * always matches.
 */
public final class IntMatchAll extends IntMatchExpr {

  public static @Nonnull IntMatchAll of(IntMatchExpr... exprs) {
    return of(ImmutableSet.copyOf(exprs));
  }

  public static @Nonnull IntMatchAll of(Iterable<IntMatchExpr> exprs) {
    return new IntMatchAll(ImmutableSet.copyOf(exprs));
  }

  @VisibleForTesting
  IntMatchAll(ImmutableSet<IntMatchExpr> exprs) {
    _exprs = exprs;
  }

  @Override
  public <T, U> T accept(IntMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitIntMatchAll(this, arg);
  }

  @JsonIgnore
  public Set<IntMatchExpr> getExprs() {
    return _exprs;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof IntMatchAll)) {
      return false;
    }
    return _exprs.equals(((IntMatchAll) obj)._exprs);
  }

  @Override
  public int hashCode() {
    return _exprs.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("exprs", _exprs).toString();
  }

  private static final String PROP_EXPRS = "exprs";

  @JsonCreator
  private static @Nonnull IntMatchAll create(
      @JsonProperty(PROP_EXPRS) @Nullable Iterable<IntMatchExpr> exprs) {
    return new IntMatchAll(ImmutableSet.copyOf(firstNonNull(exprs, ImmutableSet.of())));
  }

  @JsonProperty(PROP_EXPRS)
  private @Nonnull List<IntMatchExpr> getExprsList() {
    return ImmutableList.copyOf(_exprs);
  }

  private final @Nonnull Set<IntMatchExpr> _exprs;
}
