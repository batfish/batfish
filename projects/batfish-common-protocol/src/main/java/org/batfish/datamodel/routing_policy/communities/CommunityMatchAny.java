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
 * Matches a {@link org.batfish.datamodel.bgp.community.Community} if it is matched by any of the
 * provided {@link CommunityMatchExpr}s. If empty, never matches.
 */
public final class CommunityMatchAny extends CommunityMatchExpr {

  public static CommunityMatchExpr matchAny(Iterable<CommunityMatchExpr> exprs) {
    Set<CommunityMatchExpr> exprsSet = ImmutableSet.copyOf(exprs);
    if (exprsSet.size() == 1) {
      return Iterables.getOnlyElement(exprsSet);
    }
    return new CommunityMatchAny(exprsSet);
  }

  @VisibleForTesting
  public CommunityMatchAny(Iterable<CommunityMatchExpr> exprs) {
    _exprs = ImmutableSet.copyOf(exprs);
  }

  public @Nonnull Set<CommunityMatchExpr> getExprs() {
    return _exprs;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityMatchAny)) {
      return false;
    }
    return _exprs.equals(((CommunityMatchAny) obj)._exprs);
  }

  @Override
  public int hashCode() {
    return _exprs.hashCode();
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunityMatchAny(this, arg);
  }

  private static final String PROP_EXPRS = "exprs";

  @JsonCreator
  private static @Nonnull CommunityMatchAny create(
      @JsonProperty(PROP_EXPRS) @Nullable Iterable<CommunityMatchExpr> exprs) {
    return new CommunityMatchAny(ImmutableSet.copyOf(firstNonNull(exprs, ImmutableSet.of())));
  }

  private final @Nonnull Set<CommunityMatchExpr> _exprs;

  @JsonProperty(PROP_EXPRS)
  private @Nonnull List<CommunityMatchExpr> getExprsSorted() {
    // ordered for refs
    return ImmutableList.copyOf(_exprs);
  }
}
