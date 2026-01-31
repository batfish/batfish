package org.batfish.vendor.arista.representation;

import java.io.Serializable;

public class NssaSettings implements Serializable {

  private boolean _defaultInformationOriginate;

  private boolean _noRedistribution;

  private boolean _noSummary;

  public boolean getDefaultInformationOriginate() {
    return _defaultInformationOriginate;
  }

  public boolean getNoRedistribution() {
    return _noRedistribution;
  }

  public boolean getNoSummary() {
    return _noSummary;
  }

  public void setDefaultInformationOriginate(boolean defaultInformationOriginate) {
    _defaultInformationOriginate = defaultInformationOriginate;
  }

  public void setNoRedistribution(boolean noRedistribution) {
    _noRedistribution = noRedistribution;
  }

  public void setNoSummary(boolean noSummary) {
    _noSummary = noSummary;
  }
}
