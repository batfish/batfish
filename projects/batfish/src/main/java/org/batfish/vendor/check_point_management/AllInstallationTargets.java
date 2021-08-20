package org.batfish.vendor.check_point_management;

import javax.annotation.Nonnull;

/** Indicates that package should be applied to all possible installation targets. */
public class AllInstallationTargets extends InstallationTargets {

  public static @Nonnull AllInstallationTargets instance() {
    return INSTANCE;
  }

  private static final AllInstallationTargets INSTANCE = new AllInstallationTargets();

  private AllInstallationTargets() {}

  @Override
  public boolean equals(Object obj) {
    return this == obj || obj instanceof AllInstallationTargets;
  }

  @Override
  public int hashCode() {
    return 0xC30C4F08; // randomly generated
  }
}
