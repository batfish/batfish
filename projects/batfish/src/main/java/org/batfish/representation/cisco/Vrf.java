package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.RouteDistinguisher;

public final class Vrf implements Serializable {
  private final @Nonnull Map<Long, EigrpProcess> _eigrpProcesses;
  private @Nullable BgpProcess _bgpProcess;
  private @Nullable String _description;
  private @Nullable IsisProcess _isisProcess;
  private final @Nonnull String _name;
  private @Nonnull Map<String, OspfProcess> _ospfProcesses;
  private @Nullable RipProcess _ripProcess;
  private @Nullable RouteDistinguisher _routeDistinguisher;

  private boolean _shutdown;
  private final @Nonnull Set<StaticRoute> _staticRoutes;
  private @Nullable Integer _vni;

  private final @Nonnull Map<AddressFamilyType, VrfAddressFamily> _addressFamilies;
  private final @Nonnull VrfAddressFamily _genericAddressFamily;

  public Vrf(@Nonnull String name) {
    _addressFamilies = new EnumMap<>(AddressFamilyType.class);
    _genericAddressFamily = new VrfAddressFamily();
    _eigrpProcesses = new TreeMap<>();
    _name = name;
    // Ensure that processes are in insertion order.
    _ospfProcesses = new LinkedHashMap<>(0);
    _staticRoutes = new HashSet<>();
  }

  public @Nullable BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public @Nonnull Map<Long, EigrpProcess> getEigrpProcesses() {
    return _eigrpProcesses;
  }

  public @Nullable IsisProcess getIsisProcess() {
    return _isisProcess;
  }

  /** Configuration available under address-family ipv4 (unicast). */
  public @Nullable VrfAddressFamily getIpv4UnicastAddressFamily() {
    return _addressFamilies.get(AddressFamilyType.IPV4_UNICAST);
  }

  public @Nonnull VrfAddressFamily getOrCreateIpv4UnicastAddressFamily() {
    return _addressFamilies.computeIfAbsent(
        AddressFamilyType.IPV4_UNICAST, k -> new VrfAddressFamily());
  }

  /**
   * This represents AF commands that can be typed at the VRF level. Individual AF configs can
   * inherit these if unset at the AF level. Note that not all commands can be typed at the VRF
   * level. We rely on the parser/extractor to avoid impossible configurations.
   */
  public @Nonnull VrfAddressFamily getGenericAddressFamilyConfig() {
    return _genericAddressFamily;
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

  /** Is this VRF shutdown (not used for routing/forwarding) */
  public boolean isShutdown() {
    return _shutdown;
  }

  /** Layer 3 VNI number associated with this VRF */
  public @Nullable Integer getVni() {
    return _vni;
  }

  public @Nonnull Set<StaticRoute> getStaticRoutes() {
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
