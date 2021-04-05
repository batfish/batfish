package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class XrWildcardCommunitySetElem implements XrCommunitySetElem {

  @Nonnull
  public static XrWildcardCommunitySetElem instance() {
    return INSTANCE;
  }

  @Override
  public <T, U> T accept(XrCommunitySetElemVisitor<T, U> visitor, U arg) {
    return visitor.visitWildcardCommunitySetElem(this);
  }

  private static final XrWildcardCommunitySetElem INSTANCE = new XrWildcardCommunitySetElem();

  private XrWildcardCommunitySetElem() {}
}
