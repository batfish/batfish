package org.batfish.vendor.cisco_nxos.representation;

import javax.annotation.Nullable;

public class OspfAreaNssa implements OspfAreaTypeSettings {

  @Override
  public <T> T accept(OspfAreaTypeSettingsVisitor<T> visitor) {
    return visitor.visitOspfAreaNssa(this);
  }

  public boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  public void setDefaultInformationOriginate(boolean defaultInformationOriginate) {
    _defaultInformationOriginate = defaultInformationOriginate;
  }

  public @Nullable String getDefaultInformationOriginateMap() {
    return _defaultInformationOriginateMap;
  }

  public void setDefaultInformationOriginateMap(@Nullable String defaultInformationOriginateMap) {
    _defaultInformationOriginateMap = defaultInformationOriginateMap;
  }

  public boolean getNoRedistribution() {
    return _noRedistribution;
  }

  public void setNoRedistribution(boolean noRedistribution) {
    _noRedistribution = noRedistribution;
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

  private boolean _defaultInformationOriginate;
  private @Nullable String _defaultInformationOriginateMap;
  private boolean _noRedistribution;
  private boolean _noSummary;
}
