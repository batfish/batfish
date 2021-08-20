package org.batfish.vendor.check_point_management;

import java.util.Map;
import javax.annotation.Nonnull;

public final class ManagementDomain extends NamedManagementObject {

  public ManagementDomain(
      Map<Uid, GatewayOrServer> gatewaysAndServers,
      String name,
      Map<Uid, Package> packages,
      Uid uid) {
    super(name, uid);
    _gatewaysAndServers = gatewaysAndServers;
    _packages = packages;
  }

  public @Nonnull Map<Uid, GatewayOrServer> getGatewaysAndServers() {
    return _gatewaysAndServers;
  }

  public @Nonnull Map<Uid, Package> getPackages() {
    return _packages;
  }

  private final @Nonnull Map<Uid, GatewayOrServer> _gatewaysAndServers;
  private final @Nonnull Map<Uid, Package> _packages;
}
