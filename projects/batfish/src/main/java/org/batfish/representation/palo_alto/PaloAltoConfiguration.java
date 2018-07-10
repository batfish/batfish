package org.batfish.representation.palo_alto;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;
import java.util.Collections;
import java.util.Map;
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
import org.batfish.datamodel.DefinedStructureInfo;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Vrf;
import org.batfish.vendor.StructureUsage;
import org.batfish.vendor.VendorConfiguration;

public final class PaloAltoConfiguration extends VendorConfiguration {

  private static final long serialVersionUID = 1L;

  public static final String DEFAULT_VSYS_NAME = "vsys1";

  public static final String SHARED_VSYS_NAME = "~SHARED_VSYS~";

  private Configuration _c;

  private String _dnsServerPrimary;

  private String _dnsServerSecondary;

  private String _hostname;

  private final SortedMap<String, Interface> _interfaces;

  private String _ntpServerPrimary;

  private String _ntpServerSecondary;

  private transient Set<String> _unimplementedFeatures;

  private ConfigurationFormat _vendor;

  private final SortedMap<String, VirtualRouter> _virtualRouters;

  private final SortedMap<String, Vsys> _virtualSystems;

  public PaloAltoConfiguration(Set<String> unimplementedFeatures) {
    _interfaces = new TreeMap<>();
    _unimplementedFeatures = unimplementedFeatures;
    _virtualRouters = new TreeMap<>();
    _virtualSystems = new TreeMap<>();
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

  public SortedMap<String, Vsys> getVirtualSystems() {
    return _virtualSystems;
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

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  // Visible for testing
  /**
   * Generate unique object name (no collision across vsys namespaces) given a vsys name and
   * original object name
   */
  public static String computeObjectName(String vsysName, String objectName) {
    return String.format("%s~%s", vsysName, objectName);
  }

  /** Extract object name from an object name with an embedded namespace */
  private static String extractObjectName(String objectName) {
    String[] parts = objectName.split("~", -1);
    return parts[parts.length - 1];
  }

  /** Extract vsys name from an object name with an embedded namespace */
  private static String extractVsysName(String objectName) {
    return objectName.split("~", -1)[0];
  }

  /** Convert vsys components to vendor independent model */
  private void convertVirtualSystems() {
    NavigableSet<String> loggingServers = new TreeSet<>();

    for (Vsys vsys : _virtualSystems.values()) {
      loggingServers.addAll(vsys.getSyslogServerAddresses());
      for (Entry<String, Zone> zoneEntry : vsys.getZones().entrySet()) {
        Zone zone = zoneEntry.getValue();
        String zoneName = computeObjectName(vsys.getName(), zone.getName());
        _c.getZones().put(zoneName, toZone(zoneName, zone));
      }
    }
    _c.setLoggingServers(loggingServers);
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
  private org.batfish.datamodel.Zone toZone(String name, Zone zone) {
    org.batfish.datamodel.Zone newZone = new org.batfish.datamodel.Zone(name);
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

    for (Entry<String, VirtualRouter> vr : _virtualRouters.entrySet()) {
      _c.getVrfs().put(vr.getKey(), toVrf(vr.getValue()));
    }

    // Handle converting items within virtual systems
    convertVirtualSystems();

    // Count and mark structure usages and identify undefined references
    markConcreteStructure(
        PaloAltoStructureType.INTERFACE,
        PaloAltoStructureUsage.VIRTUAL_ROUTER_INTERFACE,
        PaloAltoStructureUsage.ZONE_INTERFACE);

    markServiceOrServiceGroup(PaloAltoStructureUsage.SERVICE_GROUP_MEMBER);
    return _c;
  }

  /** Mark services or service-groups in order of decreasing specificity */
  private void markServiceOrServiceGroup(PaloAltoStructureUsage... usages) {
    for (PaloAltoStructureUsage usage : usages) {
      Map<String, SortedMap<StructureUsage, SortedMultiset<Integer>>> references =
          firstNonNull(
              _structureReferences.get(PaloAltoStructureType.SERVICE_OR_SERVICE_GROUP),
              Collections.emptyMap());
      references.forEach(
          (name, byUsage) -> {
            Multiset<Integer> lines =
                MoreObjects.firstNonNull(byUsage.get(usage), TreeMultiset.create());
            String vsysName = extractVsysName(name);
            String objName = extractObjectName(name);

            /* Check:
             *    vsys service
             *    vsys service-group
             *    shared service
             *    shared service-group
             */
            DefinedStructureInfo info = null;
            if (_virtualSystems.get(vsysName).getServices().get(objName) != null) {
              info =
                  _structureDefinitions
                      .get(PaloAltoStructureType.SERVICE.getDescription())
                      .get(name);
              String debug2 = "test";
            } else if (_virtualSystems.get(vsysName).getServiceGroups().get(objName) != null) {
              // TODO
            } else if (_virtualSystems.get(SHARED_VSYS_NAME).getServices().get(objName) != null) {
              info =
                  _structureDefinitions
                      .get(PaloAltoStructureType.SERVICE.getDescription())
                      .get(computeObjectName(SHARED_VSYS_NAME, objName));
              String debug2 = "test";
            } else if (_virtualSystems.get(vsysName).getServiceGroups().get(objName) != null) {
              // TODO
            }

            if (info != null) {
              info.setNumReferrers(
                  info.getNumReferrers() == DefinedStructureInfo.UNKNOWN_NUM_REFERRERS
                      ? DefinedStructureInfo.UNKNOWN_NUM_REFERRERS
                      : info.getNumReferrers() + lines.size());
            }

            String debug = "test";
            name = debug;
          });

      /*Map<String, SortedMap<StructureUsage, SortedMultiset<Integer>>> references =
          firstNonNull(_structureReferences.get(type), Collections.emptyMap());
      references.forEach(
          (name, byUsage) -> {
            Multiset<Integer> lines = firstNonNull(byUsage.get(usage), TreeMultiset.create());

            List<DefinedStructureInfo> matchingStructures =
                structureTypesToCheck
                    .stream()
                    .map(t -> _structureDefinitions.get(t.getDescription()))
                    .filter(Objects::nonNull)
                    .map(m -> m.get(name))
                    .filter(Objects::nonNull)
                    .collect(ImmutableList.toImmutableList());
            if (matchingStructures.isEmpty()) {
              for (int line : lines) {
                undefined(type, name, usage, line);
              }
            } else {
              matchingStructures.forEach(
                  info ->
                      info.setNumReferrers(
                          info.getNumReferrers() == DefinedStructureInfo.UNKNOWN_NUM_REFERRERS
                              ? DefinedStructureInfo.UNKNOWN_NUM_REFERRERS
                              : info.getNumReferrers() + lines.size()));
            }
          });*/
    }
  }
}
