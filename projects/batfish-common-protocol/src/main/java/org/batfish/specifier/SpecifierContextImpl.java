package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.IpSpace;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRoleDimension;

/** Implementation of {@link SpecifierContext}. */
public class SpecifierContextImpl implements SpecifierContext {
  private final @Nonnull IBatfish _batfish;

  private final @Nonnull Map<String, Configuration> _configs;

  private final @Nonnull Map<String, Map<String, IpSpace>> _interfaceOwnedIps;

  private final @Nonnull Supplier<Map<String, Map<String, IpSpace>>> _interfaceLinkOwnedIps;

  public SpecifierContextImpl(@Nonnull IBatfish batfish, @Nonnull NetworkSnapshot networkSnapshot) {
    _batfish = batfish;
    _configs = _batfish.loadConfigurations(networkSnapshot);
    _interfaceOwnedIps =
        _batfish.getTopologyProvider().getIpOwners(networkSnapshot).getInterfaceOwnedIpSpaces();
    _interfaceLinkOwnedIps = Suppliers.memoize(this::computeInterfaceLinkOwnedIps);
  }

  private Map<String, Map<String, IpSpace>> computeInterfaceLinkOwnedIps() {
    ForwardingAnalysis forwardingAnalysis = _batfish.loadDataPlane().getForwardingAnalysis();

    Map<String, Map<String, Map<String, IpSpace>>> deliveredToSubnet =
        forwardingAnalysis.getDeliveredToSubnet();
    Map<String, Map<String, Map<String, IpSpace>>> exitsNetwork =
        forwardingAnalysis.getExitsNetwork();

    return Sets.union(deliveredToSubnet.keySet(), exitsNetwork.keySet()).stream()
        .map(
            node -> {
              Map<String, Map<String, IpSpace>> nodeDeliveredToSubnet =
                  deliveredToSubnet.getOrDefault(node, ImmutableMap.of());
              Map<String, Map<String, IpSpace>> nodeExitsNetwork =
                  exitsNetwork.getOrDefault(node, ImmutableMap.of());
              Map<String, IpSpace> ifaceIpSpaces =
                  Sets.union(nodeDeliveredToSubnet.keySet(), nodeExitsNetwork.keySet()).stream()
                      .flatMap(
                          vrf -> {
                            Map<String, IpSpace> vrfDeliveredToSubnet =
                                nodeDeliveredToSubnet.getOrDefault(vrf, ImmutableMap.of());
                            Map<String, IpSpace> vrfExitsNetwork =
                                nodeExitsNetwork.getOrDefault(vrf, ImmutableMap.of());
                            return Sets.union(
                                    vrfDeliveredToSubnet.keySet(), vrfExitsNetwork.keySet())
                                .stream()
                                .map(
                                    iface -> {
                                      IpSpace ifaceDeliveredToSubnet =
                                          vrfDeliveredToSubnet.get(iface);
                                      IpSpace ifaceExitsNetwork = vrfExitsNetwork.get(iface);
                                      return Maps.immutableEntry(
                                          iface,
                                          firstNonNull(
                                              AclIpSpace.union(
                                                  ifaceDeliveredToSubnet, ifaceExitsNetwork),
                                              EmptyIpSpace.INSTANCE));
                                    });
                          })
                      .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
              return Maps.immutableEntry(node, ifaceIpSpaces);
            })
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  @Nonnull
  @Override
  public Map<String, Configuration> getConfigs() {
    return _configs;
  }

  @Override
  public Optional<ReferenceBook> getReferenceBook(String bookName) {
    return _batfish.getReferenceLibraryData().getReferenceBook(bookName);
  }

  @Nonnull
  @Override
  public Optional<NodeRoleDimension> getNodeRoleDimension(String dimension) {
    return _batfish.getNodeRoleDimension(dimension);
  }

  @Nonnull
  @Override
  public Map<String, Map<String, IpSpace>> getInterfaceOwnedIps() {
    return _interfaceOwnedIps;
  }

  @Nonnull
  @Override
  public Map<String, Map<String, IpSpace>> getInterfaceLinkOwnedIps() {
    return _interfaceLinkOwnedIps.get();
  }
}
