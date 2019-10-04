package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Configuration of a BGP Peer {@code network virtual-router NAME protocol bgp peer-group NAME peer
 * NAME}.
 */
public class BgpPeer implements Serializable {
  /** TODO From PAN admin UI - only shows in running config if checked (as yes). */
  private static final boolean DEFAULT_ENABLE = false;

  public BgpPeer() {
    _enable = DEFAULT_ENABLE;
  }

  public boolean getEnable() {
    return _enable;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public @Nullable Long getPeerAs() {
    return _peerAs;
  }

  public void setPeerAs(@Nullable Long peerAs) {
    _peerAs = peerAs;
  }
  // private implementation details

  private boolean _enable;
  private @Nullable Long _peerAs;
}
