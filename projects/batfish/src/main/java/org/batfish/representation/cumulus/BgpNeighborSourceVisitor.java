package org.batfish.representation.cumulus;

/** Visitor for {@link BgpNeighborSource} */
public interface BgpNeighborSourceVisitor<T> {
  T visitBgpNeighborSourceAddress(BgpNeighborSourceAddress updateSourceAddress);

  T visitBgpNeighborSourceInterface(BgpNeighborSourceInterface updateSourceInterface);
}
