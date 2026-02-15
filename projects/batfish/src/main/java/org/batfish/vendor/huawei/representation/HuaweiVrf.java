package org.batfish.vendor.huawei.representation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** VRF/VPN instance configuration for Huawei device. */
public class HuaweiVrf {

  private final @Nonnull String _name;
  private @Nullable String _routeDistinguisher;

  public HuaweiVrf(@Nonnull String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable String getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  public void setRouteDistinguisher(@Nullable String routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }
}
