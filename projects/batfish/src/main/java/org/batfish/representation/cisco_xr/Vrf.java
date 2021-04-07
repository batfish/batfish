package org.batfish.representation.cisco_xr;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;

public final class Vrf implements Serializable {
  @Nonnull private final Map<Long, EigrpProcess> _eigrpProcesses;
  @Nullable private BgpProcess _bgpProcess;
  @Nullable private String _description;
  @Nullable private IsisProcess _isisProcess;
  @Nonnull private final String _name;
  @Nonnull private Map<String, OspfProcess> _ospfProcesses;
  @Nullable private RipProcess _ripProcess;
  @Nullable private RouteDistinguisher _routeDistinguisher;
  @Nonnull private List<ExtendedCommunity> _routeTargetExport;
  @Nonnull private List<ExtendedCommunity> _routeTargetImport;
  private boolean _shutdown;
  @Nonnull private final Set<StaticRoute> _staticRoutes;
  @Nullable private Integer _vni;

  public Vrf(@Nonnull String name) {
    _eigrpProcesses = new TreeMap<>();
    _name = name;
    // Ensure that processes are in insertion order.
    _ospfProcesses = new LinkedHashMap<>(0);
    _staticRoutes = new HashSet<>();
    _routeTargetExport = ImmutableList.of();
    _routeTargetImport = ImmutableList.of();
  }

  @Nullable
  public BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  @Nonnull
  public Map<Long, EigrpProcess> getEigrpProcesses() {
    return _eigrpProcesses;
  }

  @Nullable
  public IsisProcess getIsisProcess() {
    return _isisProcess;
  }

  @Nonnull
  public String getName() {
    return _name;
  }

  /** Return OSPF processes defined on this VRF. Guaranteed to be in insertion order */
  @Nonnull
  public Map<String, OspfProcess> getOspfProcesses() {
    return _ospfProcesses;
  }

  @Nullable
  public RipProcess getRipProcess() {
    return _ripProcess;
  }

  /**
   * The route distinguisher to attach to VPN originating from this VRF. Will be {@code null} if it
   * must be auto-derived.
   */
  @Nullable
  public RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  /**
   * The route target values to attach to VPN routes originating from this VRF. Will be empty if it
   * must be auto-derived.
   */
  @Nonnull
  public List<ExtendedCommunity> getRouteTargetExport() {
    return _routeTargetExport;
  }

  /**
   * Routes that contain any of these route target community should be merged into this VRF. Will be
   * empty if it must be auto-derived.
   */
  @Nonnull
  public List<ExtendedCommunity> getRouteTargetImport() {
    return _routeTargetImport;
  }

  /** Is this VRF shutdown (not used for routing/forwarding) */
  public boolean isShutdown() {
    return _shutdown;
  }

  /** Layer 3 VNI number associated with this VRF */
  @Nullable
  public Integer getVni() {
    return _vni;
  }

  @Nonnull
  public Set<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public void setBgpProcess(@Nullable BgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
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

  public void addRouteTargetExport(ExtendedCommunity routeTargetExport) {
    _routeTargetExport =
        ImmutableList.<ExtendedCommunity>builder()
            .addAll(_routeTargetExport)
            .add(routeTargetExport)
            .build();
  }

  public void addRouteTargetImport(ExtendedCommunity routeTargetImport) {
    _routeTargetImport =
        ImmutableList.<ExtendedCommunity>builder()
            .addAll(_routeTargetImport)
            .add(routeTargetImport)
            .build();
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }

  public void setVni(@Nullable Integer vni) {
    _vni = vni;
  }
}
