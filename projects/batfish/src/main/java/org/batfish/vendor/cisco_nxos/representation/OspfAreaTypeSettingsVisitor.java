package org.batfish.vendor.cisco_nxos.representation;

/** A visitor of {@link OspfAreaTypeSettings}. */
public interface OspfAreaTypeSettingsVisitor<T> {

  T visitOspfAreaNssa(OspfAreaNssa ospfAreaNssa);

  T visitOspfAreaStub(OspfAreaStub ospfAreaStub);
}
