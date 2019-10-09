package org.batfish.representation.palo_alto;

public interface OspfAreaTypeSettings {
  <T> T accept(OspfAreaTypeSettingsVisitor<T> visitor);
}
