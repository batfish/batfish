package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An element of a {@link XrCommunitySet} specifying a space of standard communities or standard
 * community attributes via a Cisco IOS-style regex.
 */
@ParametersAreNonnullByDefault
public final class XrCommunitySetIosRegex implements XrCommunitySetElem {

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    XrCommunitySetIosRegex that = (XrCommunitySetIosRegex) o;
    return _regex.equals(that._regex);
  }

  @Override
  public int hashCode() {
    return _regex.hashCode();
  }

  private final @Nonnull String _regex;
}
