package org.batfish.representation.juniper;

import com.google.common.base.MoreObjects;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LiteralCommunityMember)) {
      return false;
    }
    LiteralCommunityMember lcm = (LiteralCommunityMember) o;
    return _community.equals(lcm._community);
  }

  @Override
  public int hashCode() {
    return _community.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("community", _community).toString();
  }
}
