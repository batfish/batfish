package org.batfish.vendor.check_point_management;

import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Container for all data corresponding to a given domain. */
public final class ManagementDomain extends NamedManagementObject {

  public ManagementDomain(
      Domain domain,
      Map<Uid, GatewayOrServer> gatewaysAndServers,
      Map<Uid, ManagementPackage> packages) {
    super(domain.getName(), domain.getUid());
    _gatewaysAndServers = gatewaysAndServers;
    _packages = packages;
  }

  public @Nonnull Map<Uid, GatewayOrServer> getGatewaysAndServers() {
    return _gatewaysAndServers;
  }

  public @Nonnull Map<Uid, ManagementPackage> getPackages() {
    return _packages;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    ManagementDomain that = (ManagementDomain) o;
    return _gatewaysAndServers.equals(that._gatewaysAndServers) && _packages.equals(that._packages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _gatewaysAndServers, _packages);
  }

  @Override
  public String toString() {
    return baseToStringHelper()
        .add("_gatewaysAndServers", _gatewaysAndServers)
        .add("_packages", _packages)
        .toString();
  }

  private final @Nonnull Map<Uid, GatewayOrServer> _gatewaysAndServers;
  private final @Nonnull Map<Uid, ManagementPackage> _packages;
}
