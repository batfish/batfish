package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class XrCommunitySetParameterReference implements XrCommunitySetExpr {

  public XrCommunitySetParameterReference(String name) {
    _name = name;
  }

  @Override
  public <T, U> T accept(XrCommunitySetExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetParameterReference(this);
  }

  public @Nonnull String getName() {
    return _name;
  }

  private final @Nonnull String _name;
}
