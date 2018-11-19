package org.batfish.specifier;

import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.IpSpace;

/**
 * An {@link IpSpaceSpecifier} that specifies the {@link IpSpace} owned by each {@link Location}.
 */
public final class InferFromLocationIpSpaceSpecifier implements IpSpaceSpecifier {
  public static final InferFromLocationIpSpaceSpecifier INSTANCE =
      new InferFromLocationIpSpaceSpecifier();
  /**
   * /32s are loopback interfaces -- no hosts are connected.
   *
   * <p>/31s are point-to-point connections between nodes -- again, no hosts.
   *
   * <p>/30s could have hosts, but usually do not. Historically, each subnet was required to reserve
   * two addresses: one identifying the network itself, and a broadcast address. This made /31s
   * invalid, since there were no usable IPs left over. A /30 had 2 usable IPs, so was used for
   * point-to-point connections. Eventually /31s were allowed, but we assume here that any /30s are
   * hold-over point-to-point connections in the legacy model.
   */
  private static final int HOST_SUBNET_MAX_PREFIX_LENGTH = 29;

  /** A {@link LocationVisitor} that returns the {@link IpSpace} owned by that {@link Location}. */
  class IpSpaceLocationVisitor implements LocationVisitor<IpSpace> {
    private final SpecifierContext _specifierContext;

    IpSpaceLocationVisitor(SpecifierContext specifierContext) {
      _specifierContext = specifierContext;
    }

    private Set<InterfaceAddress> interfaceAddresses(String node, String iface) {
      return _specifierContext
          .getConfigs()
          .get(node)
          .getAllInterfaces()
          .get(iface)
          .getAllAddresses();
    }

    @Override
    public IpSpace visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation) {
      String node = interfaceLinkLocation.getNodeName();
      String iface = interfaceLinkLocation.getInterfaceName();

      @Nullable
      IpSpace linkIpSpace =
          AclIpSpace.union(
              interfaceAddresses(node, iface)
                  .stream()
                  /*
                   * Only include addresses on networks that might have hosts.
                   */
                  .filter(address -> address.getNetworkBits() <= HOST_SUBNET_MAX_PREFIX_LENGTH)
                  .map(address -> address.getPrefix().toHostIpSpace())
                  .collect(Collectors.toList()));

      return linkIpSpace == null
          ? EmptyIpSpace.INSTANCE
          : AclIpSpace.difference(linkIpSpace, _specifierContext.getSnapshotDeviceOwnedIps());
    }

    @Override
    public IpSpace visitInterfaceLocation(InterfaceLocation interfaceLocation) {
      return _specifierContext.getInterfaceOwnedIps(
          interfaceLocation.getNodeName(), interfaceLocation.getInterfaceName());
    }
  }

  private InferFromLocationIpSpaceSpecifier() {}

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    IpSpaceLocationVisitor ipSpaceLocationVisitor = new IpSpaceLocationVisitor(ctxt);
    IpSpaceAssignment.Builder builder = IpSpaceAssignment.builder();
    locations.forEach(
        location -> builder.assign(location, location.accept(ipSpaceLocationVisitor)));
    return builder.build();
  }
}
