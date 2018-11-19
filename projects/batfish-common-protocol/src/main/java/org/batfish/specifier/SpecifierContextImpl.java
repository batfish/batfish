package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.topology.TopologyUtil;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.role.NodeRoleDimension;

/** Implementation of {@link SpecifierContext}. */
public class SpecifierContextImpl implements SpecifierContext {
  private final @Nonnull IBatfish _batfish;

  private final @Nonnull Map<String, Configuration> _configs;

  private final @Nonnull Map<String, Map<String, IpSpace>> _interfaceOwnedIps;

  private final @Nonnull IpSpace _snapshotDeviceOwnedIps;

  private final @Nonnull Map<String, Map<String, IpSpace>> _vrfOwnedIps;

  public SpecifierContextImpl(
      @Nonnull IBatfish batfish, @Nonnull Map<String, Configuration> configs) {
    _batfish = batfish;
    _configs = configs;
    Map<String, Set<Interface>> nodeInterfaces = TopologyUtil.computeNodeInterfaces(configs);

    /* Include inactive interfaces here so their IPs are considered part of the network (even though
     * they are unreachable). This means when ARP fails for those IPs we'll use NEIGHBOR_UNREACHABLE
     * or INSUFFICIENT_INFO dispositions rather than DELIVERED_TO_SUBNET or EXITS_NETWORK.
     */
    _snapshotDeviceOwnedIps =
        firstNonNull(
            AclIpSpace.union(
                TopologyUtil.computeIpInterfaceOwners(nodeInterfaces, false)
                    .keySet()
                    .stream()
                    .map(Ip::toIpSpace)
                    .collect(Collectors.toList())),
            EmptyIpSpace.INSTANCE);

    Map<Ip, Map<String, Set<String>>> ipInterfaceOwners =
        TopologyUtil.computeIpInterfaceOwners(nodeInterfaces, true);
    _interfaceOwnedIps = TopologyUtil.computeInterfaceOwnedIpSpaces(ipInterfaceOwners);
    _vrfOwnedIps =
        TopologyUtil.computeVrfOwnedIpSpaces(
            TopologyUtil.computeIpVrfOwners(ipInterfaceOwners, configs));
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

  @Override
  public Map<String, Map<String, IpSpace>> getInterfaceOwnedIps() {
    return _interfaceOwnedIps;
  }

  @Override
  public Map<String, Map<String, IpSpace>> getVrfOwnedIps() {
    return _vrfOwnedIps;
  }

  @Override
  @Nonnull
  public IpSpace getSnapshotDeviceOwnedIps() {
    return _snapshotDeviceOwnedIps;
  }
}
