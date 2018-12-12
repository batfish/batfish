package org.batfish.common.topology;

import static org.batfish.common.topology.TopologyUtil.computeIpInterfaceOwners;
import static org.batfish.common.topology.TopologyUtil.computeNodeInterfaces;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;

/** A utility class for working with IPs owned by network devices. */
public final class IpOwners {

  /** Mapping from a hostname to a set of all (including inactive) interfaces that node owns */
  private final Map<String, Set<Interface>> _allInterfaces;

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (including inactive
   * interfaces)
   */
  private final Map<Ip, Map<String, Set<String>>> _allDeviceOwnedIps;

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (for active interfaces
   * only)
   */
  private final Map<Ip, Map<String, Set<String>>> _activeDeviceOwnedIps;

  /** Mapping from hostname to interface name to IpSpace owned by that interface */
  private final Map<String, Map<String, IpSpace>> _hostToInterfaceToIpSpace;

  public IpOwners(Map<String, Configuration> configurations) {
    _allInterfaces = ImmutableMap.copyOf(computeNodeInterfaces(configurations));
    _allDeviceOwnedIps = ImmutableMap.copyOf(computeIpInterfaceOwners(_allInterfaces, false));
    _activeDeviceOwnedIps = ImmutableMap.copyOf(computeIpInterfaceOwners(_allInterfaces, true));
    _hostToInterfaceToIpSpace =
        ImmutableMap.copyOf(computeInterfaceOwnedIpSpaces(_activeDeviceOwnedIps));
  }

  /**
   * Invert a mapping from {@link Ip} to owner interfaces (Ip -&gt; hostname -&gt; interface name)
   * and convert the set of owned Ips into an IpSpace.
   */
  private static Map<String, Map<String, IpSpace>> computeInterfaceOwnedIpSpaces(
      Map<Ip, Map<String, Set<String>>> ipInterfaceOwners) {
    return CommonUtil.toImmutableMap(
        TopologyUtil.computeInterfaceOwnedIps(ipInterfaceOwners),
        Entry::getKey, /* host */
        hostEntry ->
            CommonUtil.toImmutableMap(
                hostEntry.getValue(),
                Entry::getKey, /* interface */
                ifaceEntry ->
                    AclIpSpace.union(
                        ifaceEntry
                            .getValue()
                            .stream()
                            .map(Ip::toIpSpace)
                            .collect(Collectors.toList()))));
  }

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (for active interfaces
   * only)
   */
  public Map<Ip, Map<String, Set<String>>> getActiveDeviceOwnedIps() {
    return _activeDeviceOwnedIps;
  }

  /**
   * Mapping from a IP to hostname to set of interfaces that own that IP (including inactive
   * interfaces)
   */
  public Map<Ip, Map<String, Set<String>>> getAllDeviceOwnedIps() {
    return _allDeviceOwnedIps;
  }

  /**
   * Returns a mapping from hostname to interface name to IpSpace owned by that interface, for
   * active interfaces only
   */
  public Map<String, Map<String, IpSpace>> getInterfaceOwnedIpSpaces() {
    return _hostToInterfaceToIpSpace;
  }
}
