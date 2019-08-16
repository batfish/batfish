package org.batfish.representation.cumulus;

import java.io.Serializable;

public class BgpVrfNeighborAddressFamilyConfiguration implements Serializable {

  private Boolean _nextHopSelf;

  public BgpVrfNeighborAddressFamilyConfiguration() {
    _nextHopSelf = false;
  }

  public Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(Boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }
}
