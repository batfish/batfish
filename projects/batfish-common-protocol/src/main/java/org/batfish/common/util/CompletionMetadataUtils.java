package org.batfish.common.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.autocomplete.IpCompletionMetadata;
import org.batfish.common.autocomplete.IpCompletionRelevance;
import org.batfish.common.autocomplete.LocationCompletionMetadata;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;

/** Various functions useful for fetching data used in the creation of CompletionMetadata */
public final class CompletionMetadataUtils {

  private CompletionMetadataUtils() {}

  /** We will add these well-known IPs to assist with autocompletion */
  public static Map<Ip, String> WELL_KNOWN_IPS =
      ImmutableMap.of(
          Ip.parse("8.8.8.8"),
          "Google DNS",
          Ip.parse("1.1.1.1"),
          "Cloudflare DNS",
          Ip.parse("52.95.110.1"),
          "AWS Route53");

  public static Set<String> getFilterNames(Map<String, Configuration> configurations) {
    ImmutableSet.Builder<String> filterNames = ImmutableSet.builder();
    configurations
        .values()
        .forEach(configuration -> filterNames.addAll(configuration.getIpAccessLists().keySet()));
    return filterNames.build();
  }

  public static Set<NodeInterfacePair> getInterfaces(Map<String, Configuration> configurations) {
    ImmutableSet.Builder<NodeInterfacePair> interfaces = ImmutableSet.builder();
    configurations
        .values()
        .forEach(
            configuration ->
                configuration.getAllInterfaces().values().stream()
                    .map(NodeInterfacePair::of)
                    .forEach(interfaces::add));
    return interfaces.build();
  }

  @VisibleForTesting
  static String interfaceDisplayString(Configuration configuration, Interface iface) {
    String suffix =
        configuration.getHumanName() == null
            ? ""
            : String.format(" (%s)", configuration.getHumanName());
    return String.format("%s[%s]%s", configuration.getHostname(), iface.getName(), suffix);
  }

  public static Map<Ip, IpCompletionMetadata> getIps(Map<String, Configuration> configurations) {
    Map<Ip, IpCompletionMetadata> ips = new HashMap<>();
    configurations
        .values()
        .forEach(
            configuration -> {
              configuration
                  .getAllInterfaces()
                  .values()
                  .forEach(
                      iface ->
                          iface.getAllConcreteAddresses().stream()
                              .map(interfaceAddress -> interfaceAddress.getIp())
                              .forEach(
                                  ip ->
                                      ips.computeIfAbsent(ip, k -> new IpCompletionMetadata())
                                          .addRelevance(
                                              new IpCompletionRelevance(
                                                  interfaceDisplayString(configuration, iface),
                                                  configuration.getHumanName(),
                                                  configuration.getHostname(),
                                                  iface.getName()))));

              configuration
                  .getGeneratedReferenceBooks()
                  .values()
                  .forEach(
                      book ->
                          book.getAddressGroups()
                              .forEach(
                                  ag ->
                                      ag.getAddresses()
                                          // we are ignoring child groups; their IPs will be caught
                                          // when we process that group itself
                                          .forEach(
                                              a ->
                                                  addGeneratedRefBookAddress(
                                                      a,
                                                      configuration,
                                                      book.getName(),
                                                      ag.getName(),
                                                      ips))));
            });

    WELL_KNOWN_IPS.forEach(
        (ip, description) -> {
          if (!ips.containsKey(ip)) {
            ips.put(
                ip, new IpCompletionMetadata(new IpCompletionRelevance(description, description)));
          }
        });
    return ImmutableMap.copyOf(ips);
  }

  @VisibleForTesting
  static String addressGroupDisplayString(
      Configuration configuration, String bookName, String groupName) {
    String suffix =
        configuration.getHumanName() == null
            ? ""
            : String.format(" (%s)", configuration.getHumanName());
    if (bookName.equals(
        GeneratedRefBookUtils.getName(configuration.getHostname(), BookType.PoolAddresses))) {
      return String.format(
          "Pool address %s on %s%s", groupName, configuration.getHostname(), suffix);
    }
    if (bookName.equals(
        GeneratedRefBookUtils.getName(configuration.getHostname(), BookType.VirtualAddresses))) {
      return String.format(
          "Virtual address %s on %s%s", groupName, configuration.getHostname(), suffix);
    }
    if (bookName.equals(
        GeneratedRefBookUtils.getName(configuration.getHostname(), BookType.PublicIps))) {
      return String.format("%s public IP%s", configuration.getHostname(), suffix);
    }
    if (bookName.equals(
        GeneratedRefBookUtils.getName(configuration.getHostname(), BookType.AwsSeviceIps))) {
      return groupName;
    }
    // Don't know what type of address this is; use default value.
    return String.format(
        "%s in %s on %s%s", groupName, bookName, configuration.getHostname(), suffix);
  }

  private static void addGeneratedRefBookAddress(
      String ipString,
      Configuration configuration,
      String bookName,
      String groupName,
      Map<Ip, IpCompletionMetadata> ips) {
    Ip.tryParse(ipString)
        .ifPresent(
            ip ->
                ips.computeIfAbsent(ip, k -> new IpCompletionMetadata())
                    .addRelevance(
                        new IpCompletionRelevance(
                            addressGroupDisplayString(configuration, bookName, groupName),
                            configuration.getHostname(),
                            configuration.getHumanName(),
                            bookName,
                            groupName)));
  }

  public static Set<String> getMlagIds(Map<String, Configuration> configurations) {
    ImmutableSet.Builder<String> mlags = ImmutableSet.builder();
    configurations
        .values()
        .forEach(configuration -> configuration.getMlags().keySet().stream().forEach(mlags::add));
    return mlags.build();
  }

  public static Map<String, NodeCompletionMetadata> getNodes(
      Map<String, Configuration> configurations) {
    return configurations.values().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Configuration::getHostname,
                config -> new NodeCompletionMetadata(config.getHumanName())));
  }

  public static Set<String> getPrefixes(Map<String, Configuration> configurations) {
    ImmutableSet.Builder<String> prefixes = ImmutableSet.builder();
    configurations
        .values()
        .forEach(
            configuration -> {
              configuration
                  .getAllInterfaces()
                  .values()
                  .forEach(
                      iface ->
                          iface.getAllConcreteAddresses().stream()
                              .map(interfaceAddress -> interfaceAddress.getPrefix().toString())
                              .forEach(prefixes::add));

              configuration
                  .getGeneratedReferenceBooks()
                  .values()
                  .forEach(
                      book ->
                          book.getAddressGroups()
                              .forEach(
                                  ag ->
                                      ag.getAddresses()
                                          // we are ignoring child groups; their prefixes will be
                                          // caught when we process that group itself
                                          .forEach(
                                              a ->
                                                  Prefix.tryParse(a)
                                                      .ifPresent(prefix -> prefixes.add(a)))));
            });
    return prefixes.build();
  }

  public static Set<String> getRoutingPolicyNames(Map<String, Configuration> configurations) {
    ImmutableSet.Builder<String> routingPolicyNames = ImmutableSet.builder();
    configurations
        .values()
        .forEach(
            configuration ->
                routingPolicyNames.addAll(configuration.getRoutingPolicies().keySet()));
    return routingPolicyNames.build();
  }

  public static Set<LocationCompletionMetadata> getLocationCompletionMetadata(
      Map<Location, LocationInfo> locationInfo, Map<String, Configuration> configurations) {
    IpSpaceToBDD toBdd = new BDDPacket().getDstIpSpaceToBDD();
    return locationInfo.entrySet().stream()
        .map(
            entry -> {
              Location location = entry.getKey();
              LocationInfo info = entry.getValue();

              // must be a source per location info and must have a source IP
              boolean isSource = info.isSource() && !toBdd.visit(info.getSourceIps()).isZero();

              boolean isTracerouteSource = isTracerouteSource(location, configurations);

              if (isSource || isTracerouteSource) {
                return new LocationCompletionMetadata(location, isSource, isTracerouteSource);
              }
              return null;
            })
        .filter(Objects::nonNull)
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Judges if a location is a valid traceroute source. To be deemed a traceroute source, the
   * location must be active and, if it is an InterfaceLocation, must have a concrete
   * InterfaceAddress.
   *
   * <p>The function will return false for anything but an InterfaceLocation or
   * InterfaceLinkLocation.
   */
  // TODO: possible to share this logic with TR logic?
  @VisibleForTesting
  static boolean isTracerouteSource(Location location, Map<String, Configuration> configurations) {
    Configuration config = configurations.get(location.getNodeName());
    @Nullable
    String ifaceName =
        location instanceof InterfaceLocation
            ? ((InterfaceLocation) location).getInterfaceName()
            : location instanceof InterfaceLinkLocation
                ? ((InterfaceLinkLocation) location).getInterfaceName()
                : null;
    @Nullable Interface iface = ifaceName != null ? config.getAllInterfaces().get(ifaceName) : null;

    return iface != null
        && iface.getActive()
        && !iface.getSwitchport() // ignore L2 interfaces
        && ((location instanceof InterfaceLinkLocation
                // packets can enter (InterfaceLinkLocation) interfaces that are not loopback and
                // have either LLA or a concrete address with prefix size < 32
                && !iface.isLoopback()
                && (!iface.getAllLinkLocalAddresses().isEmpty()
                    || iface.getAllConcreteAddresses().stream()
                        .anyMatch(address -> address.getNetworkBits() < Prefix.MAX_PREFIX_LENGTH)))
            || (location instanceof InterfaceLocation
                // packets can start (InterfaceLocation) at any interfaces with an IP address
                && !iface.getAllAddresses().isEmpty()));
  }

  public static Set<String> getStructureNames(Map<String, Configuration> configurations) {
    ImmutableSet.Builder<String> structureNames = ImmutableSet.builder();
    configurations
        .values()
        .forEach(
            configuration ->
                NamedStructurePropertySpecifier.JAVA_MAP
                    .values()
                    .forEach(
                        type -> {
                          // fetch names of all defined structures of given type and configuration
                          Object namedStructuresMap = type.getGetter().apply(configuration);
                          // should be an instance of a Map
                          if (namedStructuresMap instanceof Map<?, ?>) {
                            ((Map<?, ?>) namedStructuresMap)
                                .keySet()
                                .forEach(
                                    key -> {
                                      if (key instanceof String) {
                                        structureNames.add((String) key);
                                      }
                                    });
                          }
                        }));
    return structureNames.build();
  }

  public static Set<String> getVrfs(Map<String, Configuration> configurations) {
    ImmutableSet.Builder<String> vrfs = ImmutableSet.builder();
    configurations
        .values()
        .forEach(
            configuration ->
                configuration.getAllInterfaces().values().stream()
                    .map(Interface::getVrfName)
                    .forEach(vrfs::add));
    return vrfs.build();
  }

  public static Set<String> getZones(Map<String, Configuration> configurations) {
    ImmutableSet.Builder<String> zones = ImmutableSet.builder();
    configurations
        .values()
        .forEach(configuration -> zones.addAll(configuration.getZones().keySet()));
    return zones.build();
  }
}
