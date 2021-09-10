package org.batfish.vendor.check_point_management;

/** Visitor for {@link NatTranslatedSource} */
public interface NatTranslatedSourceVisitor<T> {
  T visitAddressRange(AddressRange addressRange);

  T visitHost(Host host);

  T visitOriginal(Original original);
}
