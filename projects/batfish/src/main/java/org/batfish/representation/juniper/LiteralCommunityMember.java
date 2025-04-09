package org.batfish.representation.juniper;

import java.util.Objects;
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
    return Objects.equals(_community, lcm._community);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_community);
  }
}
