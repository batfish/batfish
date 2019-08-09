package org.batfish.representation.cisco_nxos;

public final class OspfAreaStub implements OspfAreaTypeSettings {

  @Override
  public <T> T accept(OspfAreaTypeSettingsVisitor<T> visitor) {
    return visitor.visitOspfAreaStub(this);
  }

  public boolean getNoSummary() {
    return _noSummary;
  }

  public void setNoSummary(boolean noSummary) {
    _noSummary = noSummary;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private boolean _noSummary;
}
