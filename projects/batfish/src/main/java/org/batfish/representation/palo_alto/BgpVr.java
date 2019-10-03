package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * Configuration of BGP within a virtual-router. Config at {@code network virtual-router NAME
 * protocol bgp}.
 */
public class BgpVr implements Serializable {

  public BgpVr() {
    _routingOptions = new BgpVrRoutingOptions();
  }

  public @Nullable Boolean getEnable() {
    return _enable;
  }

  public void setEnable(@Nullable Boolean enable) {
    _enable = enable;
  }

  public @Nullable Boolean getInstallRoute() {
    return _installRoute;
  }

  public void setInstallRoute(@Nullable Boolean installRoute) {
    _installRoute = installRoute;
  }

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  public @Nullable Boolean getRejectDefaultRoute() {
    return _rejectDefaultRoute;
  }

  public void setRejectDefaultRoute(@Nullable Boolean rejectDefaultRoute) {
    _rejectDefaultRoute = rejectDefaultRoute;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public @Nonnull BgpVrRoutingOptions getRoutingOptions() {
    return _routingOptions;
  }

  // private implementation details
  private final BgpVrRoutingOptions _routingOptions;

  private @Nullable Boolean _enable;
  private @Nullable Boolean _installRoute;
  private @Nullable Long _localAs;
  private @Nullable Boolean _rejectDefaultRoute;
  private @Nullable Ip _routerId;
}
