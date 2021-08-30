package org.batfish.vendor.check_point_management;

/** Visitor for {@link ConcreteSrcOrDst} */
public interface ConcreteSrcOrDstVisitor<T> {
  default T visit(ConcreteSrcOrDst concreteSrcOrDst) {
    return concreteSrcOrDst.accept(this);
  }

  T visitAddressRange(AddressRange addressRange);

  T visitCpmiGatewayCluster(CpmiGatewayCluster cpmiGatewayCluster);

  T visitGroup(Group group);

  T visitHost(Host host);

  T visitNetwork(Network network);
}
