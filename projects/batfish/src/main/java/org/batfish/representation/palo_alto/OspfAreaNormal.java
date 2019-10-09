package org.batfish.representation.palo_alto;

/** An OSPF area which is neither STUB nor NSSA */
public class OspfAreaNormal implements OspfAreaTypeSettings {
  @Override
  public <T> T accept(OspfAreaTypeSettingsVisitor<T> visitor) {
    return visitor.visitOspfAreaNormal(this);
  }
}
