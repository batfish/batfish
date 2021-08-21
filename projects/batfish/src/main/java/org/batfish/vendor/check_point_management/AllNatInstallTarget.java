package org.batfish.vendor.check_point_management;

import javax.annotation.Nonnull;

/** Indicates that {@link NatRule} should be applied to all possible targets. */
public final class AllNatInstallTarget implements NatInstallTarget {

  public static @Nonnull AllNatInstallTarget instance() {
    return INSTANCE;
  }

  private static final AllNatInstallTarget INSTANCE = new AllNatInstallTarget();

  private AllNatInstallTarget() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof AllNatInstallTarget;
  }

  @Override
  public int hashCode() {
    return 0x5A480954; // randomly generated
  }
}
