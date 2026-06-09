package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An SR-OS routing instance (e.g. {@code router "Base"}), keyed by router-name. {@code Base} is the
 * full-featured instance. Holds the autonomous-system, interfaces, static routes, and BGP
 * configuration.
 */
public final class Router implements Serializable {

  public Router(String name) {
    _name = name;
    _interfaces = new HashMap<>();
    _staticRoutes = new ArrayList<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** The {@code autonomous-system}, or {@code null} if unset. */
  public @Nullable Long getAutonomousSystem() {
    return _autonomousSystem;
  }

  public void setAutonomousSystem(@Nullable Long autonomousSystem) {
    _autonomousSystem = autonomousSystem;
  }

  /** Interfaces in this router, keyed by interface-name. */
  public @Nonnull Map<String, RouterInterface> getInterfaces() {
    return _interfaces;
  }

  /** The {@code static-routes route} entries in this router, in configuration order. */
  public @Nonnull List<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  /** The BGP process for this router, or {@code null} if {@code bgp} is not configured. */
  public @Nullable BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public void setBgpProcess(@Nullable BgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  /** The OSPF process for this router, or {@code null} if {@code ospf} is not configured. */
  public @Nullable OspfProcess getOspfProcess() {
    return _ospfProcess;
  }

  public void setOspfProcess(@Nullable OspfProcess ospfProcess) {
    _ospfProcess = ospfProcess;
  }

  private final @Nonnull String _name;
  private @Nullable Long _autonomousSystem;
  private final @Nonnull Map<String, RouterInterface> _interfaces;
  private final @Nonnull List<StaticRoute> _staticRoutes;
  private @Nullable BgpProcess _bgpProcess;
  private @Nullable OspfProcess _ospfProcess;
}
