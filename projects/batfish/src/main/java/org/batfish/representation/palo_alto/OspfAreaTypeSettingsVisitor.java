package org.batfish.representation.palo_alto;

/** A visitor of {@link OspfAreaTypeSettings}. */
public interface OspfAreaTypeSettingsVisitor<T> {

  T visitOspfAreaNssa(OspfAreaNssa ospfAreaNssa);

  T visitOspfAreaStub(OspfAreaStub ospfAreaStub);

  T visitOspfAreaNormal(OspfAreaNormal ospfAreaNormal);
}
