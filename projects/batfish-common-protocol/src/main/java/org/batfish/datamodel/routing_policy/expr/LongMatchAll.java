package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Matches a long if it is matched by all of the provided {@link LongMatchExpr}s. If empty, always
 * matches.
 */
public final class LongMatchAll extends LongMatchExpr {

  public static @Nonnull LongMatchAll of(LongMatchExpr... exprs) {
    return of(ImmutableSet.copyOf(exprs));
  }

  public static @Nonnull LongMatchAll of(Iterable<LongMatchExpr> exprs) {
    return new LongMatchAll(ImmutableSet.copyOf(exprs));
  }

  @VisibleForTesting
  LongMatchAll(ImmutableSet<LongMatchExpr> exprs) {
    _exprs = exprs;
  }

  @Override
  public <T, U> T accept(LongMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitLongMatchAll(this, arg);
  }

  @JsonIgnore
  public Set<LongMatchExpr> getExprs() {
    return _exprs;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LongMatchAll)) {
      return false;
    }
    return _exprs.equals(((LongMatchAll) obj)._exprs);
  }

  @Override
  public int hashCode() {
    return _exprs.hashCode();
  }

  private static final String PROP_EXPRS = "exprs";

  @JsonCreator
  private static @Nonnull LongMatchAll create(
      @JsonProperty(PROP_EXPRS) @Nullable Iterable<LongMatchExpr> exprs) {
    return new LongMatchAll(ImmutableSet.copyOf(firstNonNull(exprs, ImmutableSet.of())));
  }

  @JsonProperty(PROP_EXPRS)
  private @Nonnull List<LongMatchExpr> getExprsList() {
    return ImmutableList.copyOf(_exprs);
  }

  private final @Nonnull Set<LongMatchExpr> _exprs;
}
