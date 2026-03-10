package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Matches a {@link CommunitySet} iff it has a {@link org.batfish.datamodel.bgp.community.Community}
 * that is matched by the provided {@link CommunityMatchExpr}.
 */
public final class HasCommunity extends CommunitySetMatchExpr {

  public HasCommunity(CommunityMatchExpr expr) {
    _expr = expr;
  }

  @JsonProperty(PROP_EXPR)
  public @Nonnull CommunityMatchExpr getExpr() {
    return _expr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof HasCommunity)) {
      return false;
    }
    return _expr.equals(((HasCommunity) obj)._expr);
  }

  @Override
  public int hashCode() {
    return _expr.hashCode();
  }

  @Override
  public <T, U> T accept(CommunitySetMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitHasCommunity(this, arg);
  }

  private static final String PROP_EXPR = "expr";

  @JsonCreator
  private static @Nonnull HasCommunity create(
      @JsonProperty(PROP_EXPR) @Nullable CommunityMatchExpr expr) {
    checkArgument(expr != null, "Missing %s", PROP_EXPR);
    return new HasCommunity(expr);
  }

  private final @Nonnull CommunityMatchExpr _expr;
}
