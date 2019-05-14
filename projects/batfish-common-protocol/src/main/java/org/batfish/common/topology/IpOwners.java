package org.batfish.common.topology;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.topology.TopologyUtil.computeIpInterfaceOwners;
import static org.batfish.common.topology.TopologyUtil.computeNodeInterfaces;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.opentracing.ActiveSpan;
import io.opentracing.util.GlobalTracer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.util.CollectionUtil;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

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

  /**
   * Mapping from hostname to interface name to host IP subnet.
   *
   * @see Prefix#toHostIpSpace()
   */
  private final Map<String, Map<String, IpSpace>> _allInterfaceHostIps;

  /**
   * Mapping from hostname to interface name to host IP subnet (for active interfaces only).
   *
   * @see Prefix#toHostIpSpace()
   */
  private final Map<String, Map<String, IpSpace>> _activeInterfaceHostIps;

  public IpOwners(Map<String, Configuration> configurations) {
    _allInterfaces = ImmutableMap.copyOf(computeNodeInterfaces(configurations));

    _allDeviceOwnedIps = ImmutableMap.copyOf(computeIpInterfaceOwners(_allInterfaces, false));
    _activeDeviceOwnedIps = ImmutableMap.copyOf(computeIpInterfaceOwners(_allInterfaces, true));

    _hostToInterfaceToIpSpace =
        ImmutableMap.copyOf(computeInterfaceOwnedIpSpaces(_activeDeviceOwnedIps));

    _allInterfaceHostIps = computeInterfaceHostSubnetIps(configurations, false);
    _activeInterfaceHostIps = computeInterfaceHostSubnetIps(configurations, true);
  }

  /**
   * Invert a mapping from {@link Ip} to owner interfaces (Ip -&gt; hostname -&gt; interface name)
   * and convert the set of owned Ips into an IpSpace.
   */
  private static Map<String, Map<String, IpSpace>> computeInterfaceOwnedIpSpaces(
      Map<Ip, Map<String, Set<String>>> ipInterfaceOwners) {
    return CollectionUtil.toImmutableMap(
        TopologyUtil.computeInterfaceOwnedIps(ipInterfaceOwners),
        Entry::getKey, /* host */
        hostEntry ->
            CollectionUtil.toImmutableMap(
                hostEntry.getValue(),
                Entry::getKey, /* interface */
                ifaceEntry ->
                    AclIpSpace.union(
                        ifaceEntry.getValue().stream()
                            .map(Ip::toIpSpace)
                            .collect(Collectors.toList()))));
  }

  @VisibleForTesting
  static Map<String, Map<String, IpSpace>> computeInterfaceHostSubnetIps(
      Map<String, Configuration> configs, boolean excludeInactive) {
    try (ActiveSpan span =
        GlobalTracer.get().buildSpan("IpOwners.computeInterfaceHostSubnetIps").startActive()) {
      assert span != null; // avoid unused warning
      return toImmutableMap(
          configs,
          Entry::getKey, /* hostname */
          nodeEntry ->
              toImmutableMap(
                  excludeInactive
                      ? nodeEntry.getValue().getActiveInterfaces()
                      : nodeEntry.getValue().getAllInterfaces(),
                  Entry::getKey, /* interface */
                  ifaceEntry ->
                      firstNonNull(
                          AclIpSpace.union(
                              ifaceEntry.getValue().getAllAddresses().stream()
                                  .map(InterfaceAddress::getPrefix)
                                  .map(Prefix::toHostIpSpace)
                                  .collect(ImmutableList.toImmutableList())),
                          EmptyIpSpace.INSTANCE)));
    }
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
   * Returns a mapping from hostname to interface name to the host {@link IpSpace} of that
   * interface, for active interfaces only.
   *
   * @see Prefix#toHostIpSpace()
   */
  public Map<String, Map<String, IpSpace>> getActiveInterfaceHostIps() {
    return _activeInterfaceHostIps;
  }

  /**
   * Returns a mapping from hostname to interface name to the host {@link IpSpace} of that
   * interface, including inactive interfaces.
   *
   * @see Prefix#toHostIpSpace()
   */
  public Map<String, Map<String, IpSpace>> getAllInterfaceHostIps() {
    return _allInterfaceHostIps;
  }

  /**
   * Returns a mapping from hostname to interface name to IpSpace owned by that interface, for
   * active interfaces only
   */
  public Map<String, Map<String, IpSpace>> getInterfaceOwnedIpSpaces() {
    return _hostToInterfaceToIpSpace;
  }
}
