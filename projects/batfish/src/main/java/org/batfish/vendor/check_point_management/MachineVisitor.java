package org.batfish.vendor.check_point_management;

/** A visitor of {@link Machine} that returns a generic value. */
public interface MachineVisitor<T> {

  T visitGatewayOrServer(GatewayOrServer gatewayOrServer);

  T visitHost(Host host);
}
