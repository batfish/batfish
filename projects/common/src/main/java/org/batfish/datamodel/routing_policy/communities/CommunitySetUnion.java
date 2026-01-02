package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link CommunitySetExpr} representing the union of the sets represented by its constituent
 * expressions.
 */
public final class CommunitySetUnion extends CommunitySetExpr {

  public static @Nonnull CommunitySetUnion of(CommunitySetExpr... exprs) {
    return of(Arrays.asList(exprs));
  }

  public static @Nonnull CommunitySetUnion of(Iterable<CommunitySetExpr> exprs) {
    return new CommunitySetUnion(ImmutableSet.copyOf(exprs));
  }

  @Override
  public <T, U> T accept(CommunitySetExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetUnion(this, arg);
  }

  @JsonIgnore
  public @Nonnull Set<CommunitySetExpr> getExprs() {
    return _exprs;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySetUnion)) {
      return false;
    }
    CommunitySetUnion rhs = (CommunitySetUnion) obj;
    return _exprs.equals(rhs._exprs);
  }

  @Override
  public int hashCode() {
    return _exprs.hashCode();
  }

  private static final String PROP_EXPRS = "exprs";

  @JsonCreator
  private static @Nonnull CommunitySetUnion create(
      @JsonProperty(PROP_EXPRS) @Nullable Iterable<CommunitySetExpr> exprs) {
    return new CommunitySetUnion(ImmutableSet.copyOf(firstNonNull(exprs, ImmutableSet.of())));
  }

  private CommunitySetUnion(Set<CommunitySetExpr> exprs) {
    _exprs = exprs;
  }

  @JsonProperty(PROP_EXPRS)
  private @Nonnull List<CommunitySetExpr> getExprsList() {
    return ImmutableList.copyOf(_exprs);
  }

  private final @Nonnull Set<CommunitySetExpr> _exprs;
}
