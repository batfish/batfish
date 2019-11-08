package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An element of a {@link CommunitySet} specifying a space of standard communities or standard
 * community attributes via a Cisco IOS-style regex.
 */
@ParametersAreNonnullByDefault
public class XrCommunitySetIosRegex implements XrCommunitySetElem {

  public XrCommunitySetIosRegex(String regex) {
    _regex = regex;
  }

  @Override
  public <T, U> T accept(XrCommunitySetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetIosRegex(this, arg);
  }

  public @Nonnull String getRegex() {
    return _regex;
  }

  private final @Nonnull String _regex;
}
