package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;

/**
 * Matches a {@link Community} if it is in the set represented by the provided {@link
 * CommunitySetExpr}.
 */
public final class CommunityIn extends CommunityMatchExpr {

  public CommunityIn(CommunitySetExpr communitySetExpr) {
    _communitySetExpr = communitySetExpr;
  }

  @JsonProperty(PROP_COMMUNITY_SET_EXPR)
  public @Nonnull CommunitySetExpr getCommunitySetExpr() {
    return _communitySetExpr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityIn)) {
      return false;
    }
    return _communitySetExpr.equals(((CommunityIn) obj)._communitySetExpr);
  }

  @Override
  public int hashCode() {
    return _communitySetExpr.hashCode();
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunityIn(this, arg);
  }

  private static final String PROP_COMMUNITY_SET_EXPR = "communitySetExpr";

  @JsonCreator
  private static @Nonnull CommunityIn create(
      @JsonProperty(PROP_COMMUNITY_SET_EXPR) @Nullable CommunitySetExpr communitySetExpr) {
    checkArgument(communitySetExpr != null, "Missing %s", PROP_COMMUNITY_SET_EXPR);
    return new CommunityIn(communitySetExpr);
  }

  private final @Nonnull CommunitySetExpr _communitySetExpr;
}
