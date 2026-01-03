package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Matches a {@link CommunitySet} if it is matched by all of the provided {@link
 * CommunitySetMatchExpr}s. If empty, always matches.
 */
public final class CommunitySetMatchAll extends CommunitySetMatchExpr {

  public static CommunitySetMatchExpr matchAll(Iterable<CommunitySetMatchExpr> exprs) {
    Set<CommunitySetMatchExpr> exprsSet = ImmutableSet.copyOf(exprs);
    if (exprsSet.size() == 1) {
      return Iterables.getOnlyElement(exprsSet);
    }
    return new CommunitySetMatchAll(exprsSet);
  }

  @VisibleForTesting
  public CommunitySetMatchAll(Iterable<CommunitySetMatchExpr> exprs) {
    _exprs = ImmutableSet.copyOf(exprs);
  }

  public @Nonnull Set<CommunitySetMatchExpr> getExprs() {
    return _exprs;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySetMatchAll)) {
      return false;
    }
    return _exprs.equals(((CommunitySetMatchAll) obj)._exprs);
  }

  @Override
  public int hashCode() {
    return _exprs.hashCode();
  }

  @Override
  public <T, U> T accept(CommunitySetMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetMatchAll(this, arg);
  }

  private static final String PROP_EXPRS = "exprs";

  @JsonCreator
  private static @Nonnull CommunitySetMatchAll create(
      @JsonProperty(PROP_EXPRS) @Nullable Iterable<CommunitySetMatchExpr> exprs) {
    return new CommunitySetMatchAll(ImmutableSet.copyOf(firstNonNull(exprs, ImmutableSet.of())));
  }

  private final @Nonnull Set<CommunitySetMatchExpr> _exprs;

  @JsonProperty(PROP_EXPRS)
  private @Nonnull List<CommunitySetMatchExpr> getExprsSorted() {
    // ordered for refs
    return ImmutableList.copyOf(_exprs);
  }
}
