package org.batfish.representation.cisco;

import java.io.Serializable;

public class StubSettings implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean _noSummary;

  public boolean getNoSummary() {
    return _noSummary;
  }

  public void setNoSummary(boolean noSummary) {
    _noSummary = noSummary;
  }
}
