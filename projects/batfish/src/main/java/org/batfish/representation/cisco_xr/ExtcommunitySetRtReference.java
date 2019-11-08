package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** A named reference to a {@link ExtcommunitySetRtExpr}. */
@ParametersAreNonnullByDefault
public class ExtcommunitySetRtReference implements ExtcommunitySetRtExpr {

  public ExtcommunitySetRtReference(String name) {
    _name = name;
  }

  @Override
  public <T, U> T accept(ExtcommunitySetRtExprVisitor<T, U> visitor, U arg) {
    return visitor.visitExtcommunitySetRtReference(this, arg);
  }

  public @Nonnull String getName() {
    return _name;
  }

  private final @Nonnull String _name;
}
