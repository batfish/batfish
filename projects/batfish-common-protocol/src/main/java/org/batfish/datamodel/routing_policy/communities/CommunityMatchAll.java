package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Matches a {@link org.batfish.datamodel.bgp.community.Community} if it is matched by all of the
 * provided {@link CommunityMatchExpr}s. If empty, always matches.
 */
public final class CommunityMatchAll extends CommunityMatchExpr {

  public CommunityMatchAll(Iterable<CommunityMatchExpr> exprs) {
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
    if (!(obj instanceof CommunityMatchAll)) {
      return false;
    }
    return _exprs.equals(((CommunityMatchAll) obj)._exprs);
  }

  @Override
  public int hashCode() {
    return _exprs.hashCode();
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunityMatchAll(this, arg);
  }

  private static final String PROP_EXPRS = "exprs";

  @JsonCreator
  private static @Nonnull CommunityMatchAll create(
      @JsonProperty(PROP_EXPRS) @Nullable Iterable<CommunityMatchExpr> exprs) {
    return new CommunityMatchAll(ImmutableSet.copyOf(firstNonNull(exprs, ImmutableSet.of())));
  }

  private final @Nonnull Set<CommunityMatchExpr> _exprs;

  @JsonProperty(PROP_EXPRS)
  private @Nonnull List<CommunityMatchExpr> getExprsSorted() {
    // ordered for refs
    return ImmutableList.copyOf(_exprs);
  }
}
