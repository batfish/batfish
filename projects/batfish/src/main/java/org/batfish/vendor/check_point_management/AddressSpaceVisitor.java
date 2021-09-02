package org.batfish.vendor.check_point_management;

/** A visitor of {@link AddressSpace} that returns a generic value. */
public interface AddressSpaceVisitor<T> {

  T visitAddressRange(AddressRange addressRange);

  T visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject);

  T visitGatewayOrServer(GatewayOrServer gatewayOrServer);

  T visitGroup(Group group);

  T visitHost(Host host);

  T visitNetwork(Network network);
}
