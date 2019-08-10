package org.batfish.representation.cisco_nxos;

/** A visitor of {@link OspfAreaTypeSettings}. */
public interface OspfAreaTypeSettingsVisitor<T> {

  T visitOspfAreaNssa(OspfAreaNssa ospfAreaNssa);

  T visitOspfAreaStub(OspfAreaStub ospfAreaStub);
}
