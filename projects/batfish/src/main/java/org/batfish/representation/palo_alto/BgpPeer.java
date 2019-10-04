package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

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

  public @Nullable Ip getLocalAddress() {
    return _localAddress;
  }

  public void setLocalAddress(@Nullable Ip localAddress) {
    _localAddress = localAddress;
  }

  public @Nullable String getLocalInterface() {
    return _localInterface;
  }

  public void setLocalInterface(@Nullable String localInterface) {
    _localInterface = localInterface;
  }

  public @Nullable Ip getPeerAddress() {
    return _peerAddress;
  }

  public void setPeerAddress(@Nullable Ip peerAddress) {
    _peerAddress = peerAddress;
  }

  public @Nullable Long getPeerAs() {
    return _peerAs;
  }

  public void setPeerAs(@Nullable Long peerAs) {
    _peerAs = peerAs;
  }
  // private implementation details

  private boolean _enable;
  private @Nullable Ip _localAddress;
  private @Nullable String _localInterface;
  private @Nullable Ip _peerAddress;
  private @Nullable Long _peerAs;
}
