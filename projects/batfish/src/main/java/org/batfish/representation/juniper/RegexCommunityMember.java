package org.batfish.representation.juniper;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;

/** A {@link CommunityMember} representing a regex match condition for a {@link Community}. */
@ParametersAreNonnullByDefault
public final class RegexCommunityMember implements CommunityMember {

  public RegexCommunityMember(String regex) {
    _regex = regex;
  }

  @Override
  public <T> T accept(CommunityMemberVisitor<T> visitor) {
    return visitor.visitRegexCommunityMember(this);
  }

  public @Nonnull String getRegex() {
    return _regex;
  }

  private final @Nonnull String _regex;
}
