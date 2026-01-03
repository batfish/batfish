package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.AclIpSpace.difference;
import static org.batfish.datamodel.Prefix.HOST_SUBNET_MAX_PREFIX_LENGTH;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.IpOwners;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;

/** Utility methods for constructing {@link LocationInfo}. */
public final class LocationInfoUtils {
  private LocationInfoUtils() {}

  /**
   * @return the host IP space of connected host subnets (subnets shorter than {@link
   *     HOST_SUBNET_MAX_PREFIX_LENGTH}).
   */
  public static @Nonnull IpSpace connectedHostSubnetHostIps(Interface iface) {
    return firstNonNull(
        AclIpSpace.union(
            iface.getAllConcreteAddresses().stream()
                /*
                 * Only include addresses on networks that might have hosts.
                 */
                .filter(address -> address.getNetworkBits() <= HOST_SUBNET_MAX_PREFIX_LENGTH)
                .map(address -> address.getPrefix().toHostIpSpace())
                .collect(Collectors.toList())),
        EmptyIpSpace.INSTANCE);
  }

  /**
   * @return the host IP space of all connected subnets.
   */
  public static @Nonnull IpSpace connectedSubnetHostIps(Interface iface) {
    return firstNonNull(
        AclIpSpace.union(
            iface.getAllConcreteAddresses().stream()
                .map(address -> address.getPrefix().toHostIpSpace())
                .collect(Collectors.toList())),
        EmptyIpSpace.INSTANCE);
  }

  public static Map<Location, LocationInfo> computeLocationInfo(
      IpOwners ipOwners, Map<String, Configuration> configs) {
    /* Include inactive interfaces here so their IPs are considered part of the network (even though
     * they are unreachable). This means when ARP fails for those IPs we'll use NEIGHBOR_UNREACHABLE
     * or INSUFFICIENT_INFO dispositions rather than DELIVERED_TO_SUBNET or EXITS_NETWORK.
     */
    IpSpace snapshotDeviceOwnedIps =
        firstNonNull(
            AclIpSpace.union(
                ipOwners.getAllDeviceOwnedIps().keySet().stream()
                    .map(Ip::toIpSpace)
                    .collect(Collectors.toList())),
            EmptyIpSpace.INSTANCE);

    Map<String, Map<String, IpSpace>> interfaceOwnedIps = ipOwners.getInterfaceOwnedIpSpaces();
    return computeLocationInfo(snapshotDeviceOwnedIps, interfaceOwnedIps, configs);
  }

  @VisibleForTesting
  static Map<Location, LocationInfo> computeLocationInfo(
      IpSpace snapshotDeviceOwnedIps,
      Map<String, Map<String, IpSpace>> interfaceOwnedIps,
      Map<String, Configuration> configs) {

    return configs.values().stream()
        .flatMap(
            config -> {
              Map<Location, LocationInfo> locationInfo =
                  firstNonNull(config.getLocationInfo(), ImmutableMap.of());
              return config.getAllInterfaces().values().stream()
                  .flatMap(
                      iface -> {
                        String hostname = iface.getOwner().getHostname();
                        String ifaceName = iface.getName();

                        Location ifaceLocation = new InterfaceLocation(hostname, ifaceName);
                        Location linkLocation = new InterfaceLinkLocation(hostname, ifaceName);

                        if (!iface.getActive()) {
                          // Would get filtered out below, so just omit.
                          return Stream.of();
                        }

                        return Stream.of(
                            Maps.immutableEntry(
                                ifaceLocation,
                                getInterfaceLocationInfo(
                                    locationInfo.get(ifaceLocation),
                                    interfaceOwnedIps
                                        .getOrDefault(hostname, ImmutableMap.of())
                                        .getOrDefault(ifaceName, EmptyIpSpace.INSTANCE))),
                            Maps.immutableEntry(
                                linkLocation,
                                getInterfaceLinkLocationInfo(
                                    locationInfo.get(linkLocation),
                                    iface,
                                    snapshotDeviceOwnedIps)));
                      });
            })
        .filter(e -> !e.getValue().equals(LocationInfo.NOTHING))
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  private static LocationInfo getInterfaceLocationInfo(
      @Nullable LocationInfo vendorLocationInfo, IpSpace interfaceOwnedIps) {
    return firstNonNull(
        vendorLocationInfo,
        new LocationInfo(
            // assume the interface is infrastructure, and so not a source
            false,
            // when used as a source, pick one of its owned IPs
            interfaceOwnedIps,
            // interface locations do not have external ARP IPs
            EmptyIpSpace.INSTANCE));
  }

  private static LocationInfo getInterfaceLinkLocationInfo(
      @Nullable LocationInfo vendorLocationInfo, Interface iface, IpSpace snapshotOwnedIps) {
    if (vendorLocationInfo != null) {
      return subtractSnapshotOwnedIpsFromSourceIps(vendorLocationInfo, snapshotOwnedIps);
    }
    return new LocationInfo(
        true,
        firstNonNull(
            difference(connectedHostSubnetHostIps(iface), snapshotOwnedIps), EmptyIpSpace.INSTANCE),
        connectedSubnetHostIps(iface));
  }

  private static LocationInfo subtractSnapshotOwnedIpsFromSourceIps(
      LocationInfo locationInfo, IpSpace snapshotOwnedIps) {
    return new LocationInfo(
        locationInfo.isSource(),
        checkNotNull(difference(locationInfo.getSourceIps(), snapshotOwnedIps)),
        locationInfo.getArpIps());
  }

  public static IpSpace configuredIps(Interface iface) {
    return firstNonNull(
        AclIpSpace.union(
            iface.getAllConcreteAddresses().stream()
                .map(ConcreteInterfaceAddress::getIp)
                .map(Ip::toIpSpace)
                .collect(ImmutableList.toImmutableList())),
        EmptyIpSpace.INSTANCE);
  }
}
