package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Prefix.HOST_SUBNET_MAX_PREFIX_LENGTH;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.topology.IpOwners;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;

/** Utility methods for constructing {@link LocationInfo}. */
public final class LocationInfoUtils {
  private LocationInfoUtils() {}

  @Nonnull
  public static IpSpace connectedSubnetIps(Interface iface) {

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
    Map<String, Map<String, IpSpace>> activeInterfaceHostIps = ipOwners.getActiveInterfaceHostIps();

    return configs.values().stream()
        .flatMap(config -> config.getAllInterfaces().values().stream())
        .flatMap(
            iface -> {
              String hostname = iface.getOwner().getHostname();
              String ifaceName = iface.getName();

              Location ifaceLocation = new InterfaceLocation(hostname, ifaceName);
              Location linkLocation = new InterfaceLinkLocation(hostname, ifaceName);

              if (!iface.getActive()) {
                LocationInfo info =
                    new LocationInfo(false, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE);
                return Stream.of(
                    Maps.immutableEntry(ifaceLocation, info),
                    Maps.immutableEntry(linkLocation, info));
              }

              LocationInfo ifaceLocationInfo =
                  new LocationInfo(
                      true,
                      interfaceOwnedIps
                          .getOrDefault(hostname, ImmutableMap.of())
                          .getOrDefault(ifaceName, EmptyIpSpace.INSTANCE),
                      EmptyIpSpace.INSTANCE);

              /* TODO this is very similar to but slightly different than activeInterfaceHostIps.
               * double-check whether that subtle difference is important, or if we can consolidate.
               */
              LocationInfo linkLocationInfo =
                  new LocationInfo(
                      true,
                      firstNonNull(
                          AclIpSpace.difference(connectedSubnetIps(iface), snapshotDeviceOwnedIps),
                          EmptyIpSpace.INSTANCE),
                      activeInterfaceHostIps.get(hostname).get(ifaceName));

              return Stream.of(
                  Maps.immutableEntry(ifaceLocation, ifaceLocationInfo),
                  Maps.immutableEntry(linkLocation, linkLocationInfo));
            })
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }
}
