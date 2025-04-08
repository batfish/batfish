package org.batfish.representation.juniper;

import javax.annotation.Nullable;

public class CommunityMemberParseResult {

  private final CommunityMember _member;
  private final @Nullable String _warning;

  public CommunityMemberParseResult(CommunityMember member, @Nullable String warning) {
    _member = member;
    _warning = warning;
  }

  public CommunityMember getMember() {
    return _member;
  }

  public @Nullable String getWarning() {
    return _warning;
  }
}
