package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link RdSetElem} that matches the string representation of a route distinguisher via an
 * IOS-style regex.
 */
@ParametersAreNonnullByDefault
public final class RdSetIosRegex implements RdSetElem {

  public RdSetIosRegex(String regex) {
    _regex = regex;
  }

  @Override
  public <T, U> T accept(RdSetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitRdSetIosRegex(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RdSetIosRegex)) {
      return false;
    }
    RdSetIosRegex that = (RdSetIosRegex) o;
    return _regex.equals(that._regex);
  }

  @Override
  public int hashCode() {
    return _regex.hashCode();
  }

  @Nonnull private final String _regex;
}
