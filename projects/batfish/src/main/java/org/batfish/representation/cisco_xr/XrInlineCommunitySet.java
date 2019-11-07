package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** An inline {@link XrCommunitySetExpr} defined within a {@link RoutePolicy}. */
@ParametersAreNonnullByDefault
public class XrInlineCommunitySet implements XrCommunitySetExpr {

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

  private final @Nonnull XrCommunitySet _communitySet;
}
