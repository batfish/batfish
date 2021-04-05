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

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof XrWildcardCommunitySetElem;
  }

  @Override
  public int hashCode() {
    return 0xe37bb8a8; // randomly generated
  }

  private static final XrWildcardCommunitySetElem INSTANCE = new XrWildcardCommunitySetElem();

  private XrWildcardCommunitySetElem() {}
}
