package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;

/** Matches a {@link Community} if it is equal to the provided community. */
public final class CommunityIs extends CommunityMatchExpr {

  public CommunityIs(Community community) {
    _community = community;
  }

  @JsonProperty(PROP_COMMUNITY)
  public @Nonnull Community getCommunity() {
    return _community;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunityIs)) {
      return false;
    }
    return _community.equals(((CommunityIs) obj)._community);
  }

  @Override
  public int hashCode() {
    return _community.hashCode();
  }

  @Override
  public <T, U> T accept(CommunityMatchExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunityIs(this, arg);
  }

  private static final String PROP_COMMUNITY = "community";

  @JsonCreator
  private static @Nonnull CommunityIs create(
      @JsonProperty(PROP_COMMUNITY) @Nullable Community community) {
    checkArgument(community != null, "Missing %s", PROP_COMMUNITY);
    return new CommunityIs(community);
  }

  private final @Nonnull Community _community;
}
