package org.batfish.vendor.check_point_management;

/** A visitor of {@link HasNatSettings} that returns a generic value. */
public interface HasNatSettingsVisitor<T> {
  default T visit(HasNatSettings hasNatSettings) {
    return hasNatSettings.accept(this);
  }

  T visitAddressRange(AddressRange addressRange);

  T visitHost(Host host);

  T visitNetwork(Network network);
}
