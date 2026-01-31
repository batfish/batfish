package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public final class Vrf implements Serializable {
  private @Nullable String _description;
  private @Nullable IsisProcess _isisProcess;
  private final @Nonnull Map<String, LoggingHost> _loggingHosts;
  private @Nullable String _loggingSourceInterface;
  private final @Nonnull String _name;
  private @Nonnull Map<String, OspfProcess> _ospfProcesses;
  private @Nullable RipProcess _ripProcess;
  private @Nullable RouteDistinguisher _routeDistinguisher;
  private @Nullable ExtendedCommunity _routeExportTarget;
  private @Nullable ExtendedCommunity _routeImportTarget;
  private boolean _shutdown;
  private final @Nonnull Map<Prefix, StaticRouteManager> _staticRoutes;
  private @Nullable Integer _vni;

  public Vrf(@Nonnull String name) {
    _name = name;
    _loggingHosts = new HashMap<>(0);
    // Ensure that processes are in insertion order.
    _ospfProcesses = new LinkedHashMap<>(0);
    _staticRoutes = new HashMap<>(0);
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nullable IsisProcess getIsisProcess() {
    return _isisProcess;
  }

  public @Nonnull Map<String, LoggingHost> getLoggingHosts() {
    return _loggingHosts;
  }

  public @Nullable String getLoggingSourceInterface() {
    return _loggingSourceInterface;
  }

  public void setLoggingSourceInterface(@Nullable String loggingSourceInterface) {
    _loggingSourceInterface = loggingSourceInterface;
  }

  public @Nonnull String getName() {
    return _name;
  }

  /** Return OSPF processes defined on this VRF. Guaranteed to be in insertion order */
  public @Nonnull Map<String, OspfProcess> getOspfProcesses() {
    return _ospfProcesses;
  }

  public @Nullable RipProcess getRipProcess() {
    return _ripProcess;
  }

  /**
   * The route distinguisher to attach to VPN originating from this VRF. Will be {@code null} if it
   * must be auto-derived.
   */
  public @Nullable RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  /**
   * The route target value to attach to VPN routes originating from this VRF. Will be {@code null}
   * if it must be auto-derived.
   */
  public @Nullable ExtendedCommunity getRouteExportTarget() {
    return _routeExportTarget;
  }

  /**
   * Routes that contain this route target community should be merged into this VRF. Will be {@code
   * null} if it must be auto-derived.
   */
  public @Nullable ExtendedCommunity getRouteImportTarget() {
    return _routeImportTarget;
  }

  /** Is this VRF shutdown (not used for routing/forwarding) */
  public boolean isShutdown() {
    return _shutdown;
  }

  /** Layer 3 VNI number associated with this VRF */
  public @Nullable Integer getVni() {
    return _vni;
  }

  public @Nonnull Map<Prefix, StaticRouteManager> getStaticRoutes() {
    return _staticRoutes;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public void setIsisProcess(@Nullable IsisProcess isisProcess) {
    _isisProcess = isisProcess;
  }

  public void setRipProcess(@Nullable RipProcess ripProcess) {
    _ripProcess = ripProcess;
  }

  public void setRouteDistinguisher(@Nullable RouteDistinguisher routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }

  public void setRouteExportTarget(@Nullable ExtendedCommunity routeExportTarget) {
    _routeExportTarget = routeExportTarget;
  }

  public void setRouteImportTarget(@Nullable ExtendedCommunity routeImportTarget) {
    _routeImportTarget = routeImportTarget;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }

  public void setVni(@Nullable Integer vni) {
    _vni = vni;
  }
}
