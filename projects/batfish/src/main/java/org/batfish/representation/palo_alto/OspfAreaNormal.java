package org.batfish.representation.palo_alto;

public class OspfAreaNormal implements OspfAreaTypeSettings {
  @Override
  public <T> T accept(OspfAreaTypeSettingsVisitor<T> visitor) {
    return visitor.visitOspfAreaNormal(this);
  }
}
