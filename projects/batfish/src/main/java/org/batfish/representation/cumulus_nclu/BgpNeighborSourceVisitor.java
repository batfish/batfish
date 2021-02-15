package org.batfish.representation.cumulus_nclu;

/** Visitor for {@link BgpNeighborSource} */
public interface BgpNeighborSourceVisitor<T> {
  T visitBgpNeighborSourceAddress(BgpNeighborSourceAddress updateSourceAddress);

  T visitBgpNeighborSourceInterface(BgpNeighborSourceInterface updateSourceInterface);
}
