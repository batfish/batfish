package org.batfish.representation.cumulus;

import java.io.Serializable;

public class BgpVrfNeighborAddressFamilyConfiguration implements Serializable {

  private boolean _nextHopSelf;

  public BgpVrfNeighborAddressFamilyConfiguration() {
    _nextHopSelf = false;
  }

  public Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }
}
