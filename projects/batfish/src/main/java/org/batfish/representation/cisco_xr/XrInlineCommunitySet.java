package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An inline {@link XrCommunitySetExpr} defined within a {@link RoutePolicy}. */
@ParametersAreNonnullByDefault
public final class XrInlineCommunitySet implements XrCommunitySetExpr {

  public XrInlineCommunitySet(XrCommunitySet communitySet) {
    _communitySet = communitySet;
  }

  @Override
  public <T, U> T accept(XrCommunitySetExprVisitor<T, U> visitor, U arg) {
    return visitor.visitInlineCommunitySet(this, arg);
  }

  public @Nonnull XrCommunitySet getCommunitySet() {
    return _communitySet;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof XrInlineCommunitySet)) {
      return false;
    }
    XrInlineCommunitySet that = (XrInlineCommunitySet) o;
    return _communitySet.equals(that._communitySet);
  }

  @Override
  public int hashCode() {
    return _communitySet.hashCode();
  }

  private final @Nonnull XrCommunitySet _communitySet;
}
