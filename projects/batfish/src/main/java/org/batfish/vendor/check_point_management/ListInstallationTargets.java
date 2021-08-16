package org.batfish.vendor.check_point_management;

import java.util.List;
import javax.annotation.Nonnull;

/** Indicates that package should be applied to listed installation targets. */
public final class ListInstallationTargets extends InstallationTargets {

  ListInstallationTargets(List<GatewayOrServer> installationTargets) {
    _installationTargets = installationTargets;
  }

  public @Nonnull List<GatewayOrServer> getInstallationTargets() {
    return _installationTargets;
  }

  private final @Nonnull List<GatewayOrServer> _installationTargets;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof ListInstallationTargets)) {
      return false;
    }
    ListInstallationTargets that = (ListInstallationTargets) o;
    return _installationTargets.equals(that._installationTargets);
  }

  @Override
  public int hashCode() {
    return _installationTargets.hashCode();
  }
}
