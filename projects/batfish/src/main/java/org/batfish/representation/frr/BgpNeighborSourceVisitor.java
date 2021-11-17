package org.batfish.representation.frr;

/** Visitor for {@link BgpNeighborSource} */
public interface BgpNeighborSourceVisitor<T> {
  T visitBgpNeighborSourceAddress(BgpNeighborSourceAddress updateSourceAddress);

  T visitBgpNeighborSourceInterface(BgpNeighborSourceInterface updateSourceInterface);
}
