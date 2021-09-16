package org.batfish.vendor.check_point_management;

import javax.annotation.Nonnull;

/** For any types of {@link NatHideBehind} that are not yet handled */
public class UnhandledNatHideBehind extends NatHideBehind {
  @Override
  <T> T accept(NatHideBehindVisitor<T> visitor) {
    return visitor.visitUnhandledNatHideBehind(this);
  }

  public UnhandledNatHideBehind(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  private final @Nonnull String _name;
}
