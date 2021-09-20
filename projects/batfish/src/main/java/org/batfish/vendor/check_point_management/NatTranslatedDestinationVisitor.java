package org.batfish.vendor.check_point_management;

/** Visitor for {@link NatTranslatedDestination} */
public interface NatTranslatedDestinationVisitor<T> {
  default T visit(NatTranslatedDestination natTranslatedDestination) {
    return natTranslatedDestination.accept(this);
  }

  T visitAddressRange(AddressRange addressRange);

  T visitHost(Host host);

  T visitOriginal(Original original);
}
