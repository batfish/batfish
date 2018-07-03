package org.batfish.representation.palo_alto;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.VendorConfiguration;

public final class PaloAltoConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  private Configuration _c;

  private String _dnsServerPrimary;

  private String _dnsServerSecondary;

  private String _hostname;

  private final SortedMap<String, Interface> _interfaces;

  private String _ntpServerPrimary;

  private String _ntpServerSecondary;

  private SortedMap<String, SortedMap<String, SyslogServer>> _syslogServerGroups;

  private transient Set<String> _unimplementedFeatures;

  private ConfigurationFormat _vendor;

  private final SortedMap<String, VirtualRouter> _virtualRouters;

  private final SortedMap<String, Zone> _zones;

  public PaloAltoConfiguration(Set<String> unimplementedFeatures) {
    _interfaces = new TreeMap<>();
    _syslogServerGroups = new TreeMap<>();
    _unimplementedFeatures = unimplementedFeatures;
    _virtualRouters = new TreeMap<>();
    _zones = new TreeMap<>();
  }

  private NavigableSet<String> getDnsServers() {
    NavigableSet<String> servers = new TreeSet<>();
    if (_dnsServerPrimary != null) {
      servers.add(_dnsServerPrimary);
    }
    if (_dnsServerSecondary != null) {
      servers.add(_dnsServerSecondary);
    }
    return servers;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public SortedMap<String, Interface> getInterfaces() {
    return _interfaces;
  }

  private NavigableSet<String> getNtpServers() {
    NavigableSet<String> servers = new TreeSet<>();
    if (_ntpServerPrimary != null) {
      servers.add(_ntpServerPrimary);
    }
    if (_ntpServerSecondary != null) {
      servers.add(_ntpServerSecondary);
    }
    return servers;
  }

  @Override
  public Set<String> getUnimplementedFeatures() {
    return _unimplementedFeatures;
  }

  public SortedMap<String, VirtualRouter> getVirtualRouters() {
    return _virtualRouters;
  }

  public SortedMap<String, Zone> getZones() {
    return _zones;
  }

  public void setDnsServerPrimary(String dnsServerPrimary) {
    _dnsServerPrimary = dnsServerPrimary;
  }

  public void setDnsServerSecondary(String dnsServerSecondary) {
    _dnsServerSecondary = dnsServerSecondary;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  public void setNtpServerPrimary(String ntpServerPrimary) {
    _ntpServerPrimary = ntpServerPrimary;
  }

  public void setNtpServerSecondary(String ntpServerSecondary) {
    _ntpServerSecondary = ntpServerSecondary;
  }

  /**
   * Returns a syslog server with the specified name in the specified server group. If a matching
   * server does not exist, one is created.
   */
  public SyslogServer getSyslogServer(String serverGroupName, String serverName) {
    SortedMap<String, SyslogServer> serverGroup =
        _syslogServerGroups.computeIfAbsent(serverGroupName, g -> new TreeMap<>());
    return serverGroup.computeIfAbsent(serverName, SyslogServer::new);
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  /** Convert Palo Alto specific interface into vendor independent model interface */
  private org.batfish.datamodel.Interface toInterface(Interface iface) {
    String name = iface.getName();
    org.batfish.datamodel.Interface newIface =
        new org.batfish.datamodel.Interface(name, _c, InterfaceType.PHYSICAL);
    Integer mtu = iface.getMtu();
    if (mtu != null) {
      newIface.setMtu(mtu);
    }
    newIface.setAddress(iface.getAddress());
    newIface.setAllAddresses(iface.getAllAddresses());
    newIface.setActive(iface.getActive());
    newIface.setDescription(iface.getComment());

    return newIface;
  }

  /** Convert Palo Alto specific virtual router into vendor independent model Vrf */
  private Vrf toVrf(VirtualRouter vr) {
    Vrf vrf = new Vrf(vr.getName());

    // Static routes
    for (Entry<String, StaticRoute> e : vr.getStaticRoutes().entrySet()) {
      StaticRoute sr = e.getValue();
      // Can only construct a static route if it has a destination
      if (sr.getDestination() != null) {
        vrf.getStaticRoutes()
            .add(
                org.batfish.datamodel.StaticRoute.builder()
                    .setNextHopInterface(sr.getNextHopInterface())
                    .setNextHopIp(sr.getNextHopIp())
                    .setAdministrativeCost(sr.getAdminDistance())
                    .setMetric(sr.getMetric())
                    .setNetwork(sr.getDestination())
                    .build());
      } else {
        _w.redFlag(
            String.format(
                "Cannot convert static route %s, as it does not have a destination.", e.getKey()));
      }
    }

    // Interfaces
    NavigableMap<String, org.batfish.datamodel.Interface> map = new TreeMap<>();
    for (String interfaceName : vr.getInterfaceNames()) {
      org.batfish.datamodel.Interface iface = _c.getInterfaces().get(interfaceName);
      if (iface != null) {
        map.put(interfaceName, iface);
      }
    }
    vrf.setInterfaces(map);

    return vrf;
  }

  /** Convert Palo Alto zone to vendor independent model zone */
  private org.batfish.datamodel.Zone toZone(Zone zone) {
    org.batfish.datamodel.Zone newZone = new org.batfish.datamodel.Zone(zone.getName());
    newZone.setInterfaces(zone.getInterfaceNames());
    return newZone;
  }

  @Override
  public Configuration toVendorIndependentConfiguration() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setDefaultCrossZoneAction(LineAction.REJECT);
    _c.setDefaultInboundAction(LineAction.ACCEPT);
    _c.setDnsServers(getDnsServers());
    _c.setNtpServers(getNtpServers());

    for (Entry<String, Interface> i : _interfaces.entrySet()) {
      _c.getInterfaces().put(i.getKey(), toInterface(i.getValue()));
    }

    NavigableSet<String> loggingServers = new TreeSet<>();
    _syslogServerGroups
        .values()
        .forEach(g -> g.values().forEach(s -> loggingServers.add(s.getAddress())));
    _c.setLoggingServers(loggingServers);

    for (Entry<String, VirtualRouter> vr : _virtualRouters.entrySet()) {
      _c.getVrfs().put(vr.getKey(), toVrf(vr.getValue()));
    }

    for (Entry<String, Zone> zone : _zones.entrySet()) {
      _c.getZones().put(zone.getKey(), toZone(zone.getValue()));
    }

    // Count and mark structure usages and identify undefined references
    markConcreteStructure(
        PaloAltoStructureType.INTERFACE,
        PaloAltoStructureUsage.VIRTUAL_ROUTER_INTERFACE,
        PaloAltoStructureUsage.ZONE_INTERFACE);
    return _c;
  }
}
