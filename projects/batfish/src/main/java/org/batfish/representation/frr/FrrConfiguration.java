package org.batfish.representation.frr;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.representation.frr.BgpProcess.BGP_UNNUMBERED_IP;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LinkLocalAddress;
import org.batfish.vendor.VendorConfiguration;

/** A {@link VendorConfiguration} specifically for {@code /etc/frr/frr.conf}. */
public class FrrConfiguration implements Serializable {

  public static final String LOOPBACK_INTERFACE_NAME = "lo";
  public static final @Nonnull LinkLocalAddress LINK_LOCAL_ADDRESS =
      LinkLocalAddress.of(BGP_UNNUMBERED_IP);

  private @Nullable BgpProcess _bgpProcess;
  private @Nullable OspfProcess _ospfProcess;
  private @Nonnull Map<String, FrrInterface> _interfaces;
  private List<String> _interfaceInitOrder;
  private final @Nonnull Map<String, RouteMap> _routeMaps;
  private final @Nonnull Set<StaticRoute> _staticRoutes;
  private @Nonnull Map<String, Vrf> _vrfs;
  private final @Nonnull Map<String, BgpAsPathAccessList> _bgpAsPathAccessLists;
  private final @Nonnull Map<String, IpPrefixList> _ipPrefixLists;
  private final @Nonnull Map<String, Ipv6PrefixList> _ipv6PrefixLists;
  private final @Nonnull Map<String, BgpCommunityList> _bgpCommunityLists;

  public FrrConfiguration() {
    _interfaces = new HashMap<>();
    _bgpAsPathAccessLists = new HashMap<>();
    _ipPrefixLists = new HashMap<>();
    _ipv6PrefixLists = new HashMap<>();
    _bgpCommunityLists = new HashMap<>();
    _routeMaps = new HashMap<>();
    _staticRoutes = new HashSet<>();
    _vrfs = new HashMap<>();
  }

  public @Nonnull Vrf getOrCreateVrf(String vrfName) {
    return _vrfs.computeIfAbsent(vrfName, name -> new Vrf(vrfName));
  }

  public @Nonnull FrrInterface getOrCreateInterface(String ifaceName, @Nullable String vrfName) {
    return _interfaces.computeIfAbsent(ifaceName, name -> new FrrInterface(ifaceName, vrfName));
  }

  public @Nonnull FrrInterface getOrCreateInterface(String ifaceName) {
    return getOrCreateInterface(ifaceName, null);
  }

  public @Nullable BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public void setBgpProcess(@Nullable BgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  public @Nonnull OspfProcess getOrCreateOspfProcess() {
    if (_ospfProcess == null) {
      _ospfProcess = new OspfProcess();
    }
    return _ospfProcess;
  }

  public @Nullable OspfProcess getOspfProcess() {
    return _ospfProcess;
  }

  public void setOspfProcess(@Nullable OspfProcess ospfProcess) {
    _ospfProcess = ospfProcess;
  }

  public @Nonnull Map<String, BgpAsPathAccessList> getBgpAsPathAccessLists() {
    return _bgpAsPathAccessLists;
  }

  public @Nonnull Map<String, IpPrefixList> getIpPrefixLists() {
    return _ipPrefixLists;
  }

  public @Nonnull Map<String, Ipv6PrefixList> getIpv6PrefixLists() {
    return _ipv6PrefixLists;
  }

  public @Nonnull Map<String, BgpCommunityList> getBgpCommunityLists() {
    return _bgpCommunityLists;
  }

  public @Nonnull Set<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  public @Nonnull Map<String, RouteMap> getRouteMaps() {
    return _routeMaps;
  }

  public @Nonnull Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  public @Nonnull Map<String, FrrInterface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull Collection<String> getInterfaceInitOrder() {
    return firstNonNull(
        _interfaceInitOrder,
        // for ease of testing
        Collections.unmodifiableSet(_interfaces.keySet()));
  }

  public void setInterfaceInitOrder(List<String> interfaceInitOrder) {
    _interfaceInitOrder = ImmutableList.copyOf(interfaceInitOrder);
  }
}
