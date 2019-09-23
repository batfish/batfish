package org.batfish.datamodel.routing_policy.communities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** A {@link CommunitySetExpr} unconditionally representing a literal {@link CommunitySet}. */
public final class LiteralCommunitySet extends CommunitySetExpr {

  public LiteralCommunitySet(CommunitySet communitySet) {
    _communitySet = communitySet;
  }

  @Override
  public <T, U> T accept(CommunitySetExprVisitor<T, U> visitor, U arg) {
    return visitor.visitLiteralCommunitySet(this, arg);
  }

  @JsonProperty(PROP_COMMUNITY_SET)
  public @Nonnull CommunitySet getCommunitySet() {
    return _communitySet;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LiteralCommunitySet)) {
      return false;
    }
    return _communitySet.equals(((LiteralCommunitySet) obj)._communitySet);
  }

  @Override
  public int hashCode() {
    return _communitySet.hashCode();
  }

  private static final String PROP_COMMUNITY_SET = "communitySet";

  @JsonCreator
  private static @Nonnull LiteralCommunitySet create(
      @JsonProperty(PROP_COMMUNITY_SET) @Nullable CommunitySet communitySet) {
    return new LiteralCommunitySet(communitySet != null ? communitySet : CommunitySet.empty());
  }

  private final @Nonnull CommunitySet _communitySet;
}
