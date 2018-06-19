package org.batfish.representation.juniper;

import java.io.Serializable;

public class StubSettings implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean _noSummaries;

  public boolean getNoSummaries() {
    return _noSummaries;
  }

  public void setNoSummaries(boolean noSummaries) {
    _noSummaries = noSummaries;
  }
}
