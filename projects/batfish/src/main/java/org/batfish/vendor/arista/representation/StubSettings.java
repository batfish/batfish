package org.batfish.vendor.arista.representation;

import java.io.Serializable;

public class StubSettings implements Serializable {

  private boolean _noSummary;

  public boolean getNoSummary() {
    return _noSummary;
  }

  public void setNoSummary(boolean noSummary) {
    _noSummary = noSummary;
  }
}
