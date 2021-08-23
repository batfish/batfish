package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.List;
import javax.annotation.Nonnull;

/** Indicates that package should be applied to listed installation targets. */
public final class ListInstallationTargets extends InstallationTargets {

  ListInstallationTargets(List<PackageInstallationTarget> installationTargets) {
    _installationTargets = installationTargets;
  }

  public @Nonnull List<PackageInstallationTarget> getInstallationTargets() {
    return _installationTargets;
  }

  private final @Nonnull List<PackageInstallationTarget> _installationTargets;

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

  @Override
  public String toString() {
    return toStringHelper(this).add("_installationTargets", _installationTargets).toString();
  }
}
