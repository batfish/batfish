package org.batfish.vendor.check_point_management;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpRange;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;

/** Create an {@link IpSpace} representing the visited {@link AddressSpace}. */
public class AddressSpaceToIpSpace implements AddressSpaceVisitor<IpSpace> {

  @Override
  public IpSpace visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
    return UniverseIpSpace.INSTANCE;
  }

  @Override
  public IpSpace visitAddressRange(AddressRange addressRange) {
    if (addressRange.getIpv4AddressFirst() == null || addressRange.getIpv4AddressLast() == null) {
      return EmptyIpSpace.INSTANCE;
    }
    return IpRange.range(addressRange.getIpv4AddressFirst(), addressRange.getIpv4AddressLast());
  }

  @Override
  public IpSpace visitGatewayOrServer(GatewayOrServer gatewayOrServer) {
    // TODO: Verify semantics.
    //       For now, we assume only the IPv4 address gets used, since the interface addresses do
    //       not show up in the show-objects schema version of gateways/servers.
    return Optional.ofNullable(gatewayOrServer.getIpv4Address())
        .<IpSpace>map(Ip::toIpSpace)
        .orElse(EmptyIpSpace.INSTANCE);
  }

  @Override
  public IpSpace visitGroup(Group group) {
    Set<Uid> allMembers = getDescendantObjects(group, new HashSet<>());
    List<IpSpace> memberIpSpaces =
        allMembers.stream()
            .map(_objs::get)
            .filter(AddressSpace.class::isInstance)
            .map(HasName::getName)
            .map(IpSpaceReference::new)
            .collect(ImmutableList.toImmutableList());
    IpSpace space = AclIpSpace.union(memberIpSpaces);
    return space == null ? EmptyIpSpace.INSTANCE : space;
  }

  /** Returns descendant objects for the specified {@link Group}. */
  private Set<Uid> getDescendantObjects(Group group, Set<Uid> alreadyTraversedMembers) {
    Uid groupUid = group.getUid();
    if (alreadyTraversedMembers.contains(groupUid)) {
      return ImmutableSet.of();
    }
    alreadyTraversedMembers.add(groupUid);

    Set<Uid> descendantObjects = new HashSet<>();
    for (Uid memberUid : group.getMembers()) {
      NamedManagementObject member = _objs.get(memberUid);
      if (member instanceof Group) {
        descendantObjects.addAll(getDescendantObjects((Group) member, alreadyTraversedMembers));
      } else if (member instanceof AddressSpace) {
        descendantObjects.add(memberUid);
      }
    }
    return descendantObjects;
  }

  @Override
  public IpSpace visitHost(Host host) {
    Ip hostV4Address = host.getIpv4Address();
    return hostV4Address == null ? EmptyIpSpace.INSTANCE : hostV4Address.toIpSpace();
  }

  @Override
  public IpSpace visitNetwork(Network network) {
    Ip networkAddress = network.getSubnet4();
    Ip subnetMask = network.getSubnetMask();
    if (networkAddress == null) {
      // IPv6
      return EmptyIpSpace.INSTANCE;
    }
    assert subnetMask != null;
    return Prefix.create(networkAddress, subnetMask).toIpSpace();
  }

  public AddressSpaceToIpSpace(Map<Uid, NamedManagementObject> objs) {
    _objs = objs;
  }

  private final Map<Uid, NamedManagementObject> _objs;
}
