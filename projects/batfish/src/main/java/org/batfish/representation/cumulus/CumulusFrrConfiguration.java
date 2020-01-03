package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.vendor.VendorConfiguration;

/** A {@link VendorConfiguration} for the Cumulus NCLU configuration language. */
public class CumulusFrrConfiguration implements Serializable {

  private @Nullable BgpProcess _bgpProcess;
  private @Nullable OspfProcess _ospfProcess;
  private @Nonnull Map<String, FrrInterface> _interfaces;
  private final @Nonnull List<Ip> _ipv4Nameservers;
  private final @Nonnull List<Ip6> _ipv6Nameservers;
  private final @Nonnull Map<String, RouteMap> _routeMaps;
  private final @Nonnull Set<StaticRoute> _staticRoutes;
  private @Nonnull Map<String, Vrf> _vrfs;
  private final @Nonnull Map<String, IpAsPathAccessList> _ipAsPathAccessLists;
  private final @Nonnull Map<String, IpPrefixList> _ipPrefixLists;
  private final @Nonnull Map<String, IpCommunityList> _ipCommunityLists;

  public CumulusFrrConfiguration() {
    _interfaces = new HashMap<>();
    _ipAsPathAccessLists = new HashMap<>();
    _ipPrefixLists = new HashMap<>();
    _ipCommunityLists = new HashMap<>();
    _ipv4Nameservers = new LinkedList<>();
    _ipv6Nameservers = new LinkedList<>();
    _routeMaps = new HashMap<>();
    _staticRoutes = new HashSet<>();
    _vrfs = new HashMap<>();
  }

  @Nonnull
  public Vrf getOrCreateVrf(String vrfName) {
    return _vrfs.computeIfAbsent(vrfName, name -> new Vrf(vrfName));
  }

  @Nonnull
  public FrrInterface getOrCreateInterface(String ifaceName, @Nullable String vrfName) {
    return _interfaces.computeIfAbsent(ifaceName, name -> new FrrInterface(ifaceName, vrfName));
  }

  @Nonnull
  public FrrInterface getOrCreateInterface(String ifaceName) {
    return getOrCreateInterface(ifaceName, null);
  }

  @Nullable
  public BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public void setBgpProcess(@Nullable BgpProcess bgpProcess) {
    _bgpProcess = bgpProcess;
  }

  @Nullable
  public OspfProcess getOspfProcess() {
    return _ospfProcess;
  }

  public void setOspfProcess(@Nullable OspfProcess ospfProcess) {
    _ospfProcess = ospfProcess;
  }

  @Nonnull
  public Map<String, IpAsPathAccessList> getIpAsPathAccessLists() {
    return _ipAsPathAccessLists;
  }

  @Nonnull
  public Map<String, IpPrefixList> getIpPrefixLists() {
    return _ipPrefixLists;
  }

  @Nonnull
  public Map<String, IpCommunityList> getIpCommunityLists() {
    return _ipCommunityLists;
  }

  @Nonnull
  public Set<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  @Nonnull
  public Map<String, RouteMap> getRouteMaps() {
    return _routeMaps;
  }

  @Nonnull
  public List<Ip> getIpv4Nameservers() {
    return _ipv4Nameservers;
  }

  @Nonnull
  public List<Ip6> getIpv6Nameservers() {
    return _ipv6Nameservers;
  }

  @Nonnull
  public Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  @Nonnull
  public Map<String, FrrInterface> getInterfaces() {
    return _interfaces;
  }
}
