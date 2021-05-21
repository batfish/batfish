package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An as-path-set element that evaluates to true when an as-path is matched by a given IOS-style
 * regex.
 */
@ParametersAreNonnullByDefault
public final class IosRegexAsPathSetElem implements AsPathSetElem {

  public IosRegexAsPathSetElem(String regex) {
    _regex = regex;
  }

  @Override
  public <T, U> T accept(AsPathSetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitIosRegexAsPathSetElem(this, arg);
  }

  public @Nonnull String getRegex() {
    return _regex;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IosRegexAsPathSetElem)) {
      return false;
    }
    IosRegexAsPathSetElem that = (IosRegexAsPathSetElem) o;
    return _regex.equals(that._regex);
  }

  @Override
  public int hashCode() {
    return _regex.hashCode();
  }

  private final @Nonnull String _regex;
}
