package org.batfish.representation.juniper;

import java.io.Serializable;

public class StubSettings implements Serializable {

  private boolean _noSummaries;

  public boolean getNoSummaries() {
    return _noSummaries;
  }

  public void setNoSummaries(boolean noSummaries) {
    _noSummaries = noSummaries;
  }
}
