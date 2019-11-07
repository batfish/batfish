package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A named reference to a {@link XrCommunitySetExpr}. */
@ParametersAreNonnullByDefault
public class XrCommunitySetReference implements XrCommunitySetExpr {

  public XrCommunitySetReference(String name) {
    _name = name;
  }

  @Override
  public <T, U> T accept(XrCommunitySetExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetReference(this, arg);
  }

  public @Nonnull String getName() {
    return _name;
  }

  private final @Nonnull String _name;
}
