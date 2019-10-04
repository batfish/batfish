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
  /** From PAN admin UI - only shows in running config if checked (as yes). */
  private static final boolean DEFAULT_ENABLE = false;
  /** From PAN admin UI - only shows in running config if checked (as yes). */
  private static final boolean DEFAULT_INSTALL_ROUTE = false;
  /** From PAN admin UI - only shows in running config if unchecked (as no). */
  private static final boolean DEFAULT_REJECT_DEFAULT_ROUTE = true;

  public BgpVr() {
    _enable = DEFAULT_ENABLE;
    _installRoute = DEFAULT_INSTALL_ROUTE;
    _rejectDefaultRoute = DEFAULT_REJECT_DEFAULT_ROUTE;
    _routingOptions = new BgpVrRoutingOptions();
  }

  public boolean getEnable() {
    return _enable;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public boolean getInstallRoute() {
    return _installRoute;
  }

  public void setInstallRoute(boolean installRoute) {
    _installRoute = installRoute;
  }

  public @Nullable Long getLocalAs() {
    return _localAs;
  }

  public void setLocalAs(@Nullable Long localAs) {
    _localAs = localAs;
  }

  public boolean getRejectDefaultRoute() {
    return _rejectDefaultRoute;
  }

  public void setRejectDefaultRoute(boolean rejectDefaultRoute) {
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

  private boolean _enable;
  private boolean _installRoute;
  private @Nullable Long _localAs;
  private boolean _rejectDefaultRoute;
  private @Nullable Ip _routerId;
}
