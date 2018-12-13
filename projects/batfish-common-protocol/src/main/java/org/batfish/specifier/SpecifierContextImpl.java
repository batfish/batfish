package org.batfish.specifier;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.topology.IpOwners;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.EmptyIpSpace;
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

  public SpecifierContextImpl(@Nonnull IBatfish batfish, @Nonnull NetworkSnapshot networkSnapshot) {
    _batfish = batfish;
    _configs = _batfish.loadConfigurations(networkSnapshot);
    IpOwners ipOwners = _batfish.getTopologyProvider().getIpOwners(networkSnapshot);

    /* Include inactive interfaces here so their IPs are considered part of the network (even though
     * they are unreachable). This means when ARP fails for those IPs we'll use NEIGHBOR_UNREACHABLE
     * or INSUFFICIENT_INFO dispositions rather than DELIVERED_TO_SUBNET or EXITS_NETWORK.
     */
    _snapshotDeviceOwnedIps =
        firstNonNull(
            AclIpSpace.union(
                ipOwners
                    .getAllDeviceOwnedIps()
                    .keySet()
                    .stream()
                    .map(Ip::toIpSpace)
                    .collect(Collectors.toList())),
            EmptyIpSpace.INSTANCE);

    _interfaceOwnedIps = ipOwners.getInterfaceOwnedIpSpaces();
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

  @Override
  @Nonnull
  public IpSpace getSnapshotDeviceOwnedIps() {
    return _snapshotDeviceOwnedIps;
  }
}
