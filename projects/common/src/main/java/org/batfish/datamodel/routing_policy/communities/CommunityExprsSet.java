package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a {@link CommunitySet} containing the communities represented by the specified {@link
 * CommunityExpr}s.
 */
public final class CommunityExprsSet extends CommunitySetExpr {

  public static @Nonnull CommunityExprsSet of(CommunityExpr... exprs) {
    return of(ImmutableSet.copyOf(exprs));
  }

  public static @Nonnull CommunityExprsSet of(Iterable<CommunityExpr> exprs) {
    return new CommunityExprsSet(ImmutableSet.copyOf(exprs));
  }

  public CommunityExprsSet(ImmutableSet<CommunityExpr> exprs) {
    _exprs = exprs;
  }

  @Override
  public <T, U> T accept(CommunitySetExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunityExprsSet(this, arg);
  }

  @JsonIgnore
  public @Nonnull Set<CommunityExpr> getExprs() {
    return _exprs;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityExprsSet)) {
      return false;
    }
    return _exprs.equals(((CommunityExprsSet) obj)._exprs);
  }

  @Override
  public int hashCode() {
    return _exprs.hashCode();
  }

  private static final String PROP_EXPRS = "exprs";

  @JsonCreator
  private static @Nonnull CommunityExprsSet create(
      @JsonProperty(PROP_EXPRS) @Nullable Iterable<CommunityExpr> exprs) {
    return new CommunityExprsSet(ImmutableSet.copyOf(firstNonNull(exprs, ImmutableSet.of())));
  }

  @JsonProperty(PROP_EXPRS)
  private @Nonnull List<CommunityExpr> getExprsList() {
    return ImmutableList.copyOf(_exprs);
  }

  private final @Nonnull Set<CommunityExpr> _exprs;
}
