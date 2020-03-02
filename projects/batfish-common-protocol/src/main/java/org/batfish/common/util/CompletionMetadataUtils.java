package org.batfish.common.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.batfish.common.autocomplete.IpCompletionMetadata;
import org.batfish.common.autocomplete.IpCompletionRelevance;
import org.batfish.common.autocomplete.NodeCompletionMetadata;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.questions.NamedStructurePropertySpecifier;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;

/** Various functions useful for fetching data used in the creation of CompletionMetadata */
public final class CompletionMetadataUtils {

  private CompletionMetadataUtils() {}

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
    return String.format(
        "%s[%s]",
        configuration.getHumanName() == null
            ? configuration.getHostname()
            : configuration.getHumanName(),
        iface.getName());
  }

  public static Map<String, IpCompletionMetadata> getIps(
      Map<String, Configuration> configurations) {
    Map<String, IpCompletionMetadata> ips = new HashMap<>();
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
                              .map(interfaceAddress -> interfaceAddress.getIp().toString())
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

    return ImmutableMap.copyOf(ips);
  }

  @VisibleForTesting
  static String addressGroupDisplayString(
      Configuration configuration, String bookName, String groupName) {
    if (bookName.equals(
        GeneratedRefBookUtils.getName(configuration.getHostname(), BookType.PoolAddresses))) {
      // include only group name (no hostname), which should be enough of a distinguisher
      return String.format("Pool address %s", groupName);
    }
    if (bookName.equals(
        GeneratedRefBookUtils.getName(configuration.getHostname(), BookType.VirtualAddresses))) {
      // include only group name (no hostname), which should be enough of a distinguisher
      return String.format("Virtual address %s", groupName);
    }
    if (bookName.equals(
        GeneratedRefBookUtils.getName(configuration.getHostname(), BookType.PublicIps))) {
      return String.format(
          "Public IP of %s",
          configuration.getHumanName() == null
              ? configuration.getHostname()
              : configuration.getHumanName());
    }
    // Don't know what type of address this is; use default value.
    return String.format("%s in %s", groupName, bookName);
  }

  private static void addGeneratedRefBookAddress(
      String ipString,
      Configuration configuration,
      String bookName,
      String groupName,
      Map<String, IpCompletionMetadata> ips) {
    Ip.tryParse(ipString)
        .ifPresent(
            ip ->
                ips.computeIfAbsent(ip.toString(), k -> new IpCompletionMetadata())
                    .addRelevance(
                        new IpCompletionRelevance(
                            addressGroupDisplayString(configuration, bookName, groupName),
                            configuration.getHostname(),
                            configuration.getHumanName(),
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
