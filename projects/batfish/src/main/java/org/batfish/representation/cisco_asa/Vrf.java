package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.RouteDistinguisher;

public final class Vrf implements Serializable {
  @Nonnull private final Map<Long, EigrpProcess> _eigrpProcesses;
  @Nullable private BgpProcess _bgpProcess;
  @Nullable private String _description;
  @Nullable private IsisProcess _isisProcess;
  @Nonnull private final String _name;
  @Nonnull private Map<String, OspfProcess> _ospfProcesses;
  @Nullable private RipProcess _ripProcess;
  @Nullable private RouteDistinguisher _routeDistinguisher;

  private boolean _shutdown;
  @Nonnull private final Set<StaticRoute> _staticRoutes;
  @Nullable private Integer _vni;

  @Nonnull private final Map<AddressFamilyType, VrfAddressFamily> _addressFamilies;
  @Nonnull private final VrfAddressFamily _genericAddressFamily;

  public Vrf(@Nonnull String name) {
    _addressFamilies = new HashMap<>(4);
    _genericAddressFamily = new VrfAddressFamily();
    _eigrpProcesses = new TreeMap<>();
    _name = name;
    // Ensure that processes are in insertion order.
    _ospfProcesses = new LinkedHashMap<>(0);
    _staticRoutes = new HashSet<>();
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

  /** Configuration available under address-family ipv4 (unicast). */
  @Nullable
  public VrfAddressFamily getIpv4UnicastAddressFamily() {
    return _addressFamilies.get(AddressFamilyType.IPV4_UNICAST);
  }

  @Nonnull
  public VrfAddressFamily getOrCreateIpv4UnicastAddressFamily() {
    return _addressFamilies.computeIfAbsent(
        AddressFamilyType.IPV4_UNICAST, k -> new VrfAddressFamily());
  }

  /**
   * This represents AF commands that can be typed at the VRF level. Individual AF configs can
   * inherit these if unset at the AF level. Note that not all commands can be typed at the VRF
   * level. We rely on the parser/extractor to avoid impossible configurations.
   */
  @Nonnull
  public VrfAddressFamily getGenericAddressFamilyConfig() {
    return _genericAddressFamily;
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

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }

  public void setVni(@Nullable Integer vni) {
    _vni = vni;
  }
}
