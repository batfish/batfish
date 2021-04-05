package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An element of a {@link XrCommunitySet} specifying a space of standard communities or standard
 * community attributes via a Cisco IOS-XR DFA regex.
 */
@ParametersAreNonnullByDefault
public class XrCommunitySetDfaRegex implements XrCommunitySetElem {

  public XrCommunitySetDfaRegex(String regex) {
    _regex = regex;
  }

  @Override
  public <T, U> T accept(XrCommunitySetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetDfaRegex(this, arg);
  }

  public @Nonnull String getRegex() {
    return _regex;
  }

  private final @Nonnull String _regex;
}
