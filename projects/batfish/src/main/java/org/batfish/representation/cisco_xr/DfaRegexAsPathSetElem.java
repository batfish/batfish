package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An as-path-set element that evaluates to true when an as-path is matched by a given DFA-style
 * regex.
 */
@ParametersAreNonnullByDefault
public final class DfaRegexAsPathSetElem implements AsPathSetElem {

  public DfaRegexAsPathSetElem(String regex) {
    _regex = regex;
  }

  @Override
  public <T> T accept(AsPathSetElemVisitor<T> visitor) {
    return visitor.visitDfaRegexAsPathSetElem(this);
  }

  public @Nonnull String getRegex() {
    return _regex;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DfaRegexAsPathSetElem)) {
      return false;
    }
    DfaRegexAsPathSetElem that = (DfaRegexAsPathSetElem) o;
    return _regex.equals(that._regex);
  }

  @Override
  public int hashCode() {
    return _regex.hashCode();
  }

  private final @Nonnull String _regex;
}
