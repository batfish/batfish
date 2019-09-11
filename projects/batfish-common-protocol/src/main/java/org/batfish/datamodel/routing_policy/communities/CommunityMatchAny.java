package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Matches a {@link org.batfish.datamodel.bgp.community.Community} if it is matched by any of the
 * provided {@link CommunityMatchExpr}s. If empty, never matches.
 */
public final class CommunityMatchAny extends CommunityMatchExpr {

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
  protected <T> T accept(CommunityMatchExprVisitor<T> visitor) {
    return visitor.visitCommunityMatchAny(this);
  }

  private static final String PROP_EXPRS = "exprs";

  @JsonCreator
  private static @Nonnull CommunityMatchAny create(
      @JsonProperty(PROP_EXPRS) @Nullable Iterable<CommunityMatchExpr> exprs) {
    return new CommunityMatchAny(ImmutableSet.copyOf(firstNonNull(exprs, ImmutableSet.of())));
  }

  private final @Nonnull Set<CommunityMatchExpr> _exprs;

  @JsonProperty(PROP_EXPRS)
  private @Nonnull SortedSet<CommunityMatchExpr> getExprsSorted() {
    // sorted for refs
    return ImmutableSortedSet.copyOf(_exprs);
  }
}
