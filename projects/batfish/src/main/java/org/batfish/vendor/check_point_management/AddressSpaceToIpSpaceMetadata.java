package org.batfish.vendor.check_point_management;

import org.batfish.datamodel.IpSpaceMetadata;

/** Create an {@link IpSpaceMetadata} representing the visited {@link AddressSpace}. */
public class AddressSpaceToIpSpaceMetadata implements AddressSpaceVisitor<IpSpaceMetadata> {
  public static IpSpaceMetadata toIpSpaceMetadata(AddressSpace addressSpace) {
    return addressSpace.accept(INSTANCE);
  }

  @Override
  public IpSpaceMetadata visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject) {
    // This shouldn't be needed; matching `Any` object shouldn't require a named IpSpace lookup
    return new IpSpaceMetadata(cpmiAnyObject.getName(), "network object", null);
  }

  @Override
  public IpSpaceMetadata visitAddressRange(AddressRange addressRange) {
    return new IpSpaceMetadata(addressRange.getName(), "address-range", null);
  }

  @Override
  public IpSpaceMetadata visitGatewayOrServer(GatewayOrServer gatewayOrServer) {
    return new IpSpaceMetadata(gatewayOrServer.getName(), "gateway or server", null);
  }

  @Override
  public IpSpaceMetadata visitGroup(Group group) {
    return new IpSpaceMetadata(group.getName(), "group", null);
  }

  @Override
  public IpSpaceMetadata visitHost(Host host) {
    return new IpSpaceMetadata(host.getName(), "host", null);
  }

  @Override
  public IpSpaceMetadata visitNetwork(Network network) {
    return new IpSpaceMetadata(network.getName(), "network", null);
  }

  private AddressSpaceToIpSpaceMetadata() {}

  private static final AddressSpaceToIpSpaceMetadata INSTANCE = new AddressSpaceToIpSpaceMetadata();
}
