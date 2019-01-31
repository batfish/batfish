package org.batfish.common.util;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class ModellingUtils {

  public void addInternetAndIspNodes(
      @Nonnull Map<String, Configuration> configurations,
      @Nonnull List<NodeInterfacePair> interfacesConnectedToIsps,
      @Nullable List<Long> asNumOfIsps,
      @Nullable List<Ip> ipOfIsps,
      Warnings warnings) {
    Map<String, List<String>> interfaceListByNodes =
        interfacesConnectedToIsps.stream()
            .collect(
                Collectors.groupingBy(
                    NodeInterfacePair::getHostname,
                    Collectors.mapping(NodeInterfacePair::getInterface, Collectors.toList())));
    Map<Ip, Long> allIspsIpsAndAsns = new HashMap<>();
    for (Entry<String, List<String>> nodeWithInterfaces : interfaceListByNodes.entrySet()) {
      for()
    }
  }

  /**
   * Gets the local {@link Ip}s and local ASNs of the BGP configurations on the ISP routers
   * @param configuration {@link Configuration} the node containing all the interfaces
   * @param interfaces
   * @return
   */
  public Map<Ip, Long> getIpsAndAsnsOfIsps(
      @Nonnull Configuration configuration, @Nonnull List<String> interfaces) {
    Set<Ip> interfaceIps =
        interfaces.stream()
            .map(iface -> configuration.getAllInterfaces().get(iface))
            .filter(Objects::nonNull)
            .flatMap(iface -> iface.getAllAddresses().stream().map(InterfaceAddress::getIp))
            .collect(Collectors.toSet());

    return configuration.getVrfs().values().stream()
        .map(Vrf::getBgpProcess)
        .flatMap(bgpProcess -> bgpProcess.getActiveNeighbors().values().stream())
        .filter(
            bgpActivePeerConfig ->
                bgpActivePeerConfig.getLocalAs() != null
                    && bgpActivePeerConfig.getRemoteAs() != null
                    && bgpActivePeerConfig.getPeerAddress() != null
                    && bgpActivePeerConfig.getLocalIp() != null)
        .filter(
            bgpActivePeerConfig ->
                interfaceIps.contains(bgpActivePeerConfig.getLocalIp())
                    && !bgpActivePeerConfig.getLocalAs().equals(bgpActivePeerConfig.getRemoteAs()))
        .collect(
            ImmutableMap.toImmutableMap(
                BgpActivePeerConfig::getPeerAddress, BgpActivePeerConfig::getRemoteAs));
  }
}
