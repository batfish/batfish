package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;

/** A {@link CommunityMember} representing a single literal {@link Community}. */
@ParametersAreNonnullByDefault
public final class LiteralCommunityMember implements CommunityMember {

  public LiteralCommunityMember(Community community) {
    _community = community;
  }

  @Override
  public <T> T accept(CommunityMemberVisitor<T> visitor) {
    return visitor.visitLiteralCommunityMember(this);
  }

  public @Nonnull Community getCommunity() {
    return _community;
  }

  private final @Nonnull Community _community;
}
