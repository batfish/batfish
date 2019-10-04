package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Configuration of a BGP Peer Group {@code network virtual-router NAME protocol bgp peer-group
 * NAME}.
 */
public class BgpPeerGroup implements Serializable {
  /** TODO From PAN admin UI - only shows in running config if checked (as yes). */
  private static final boolean DEFAULT_ENABLE = false;

  public BgpPeerGroup(String name) {
    _enable = DEFAULT_ENABLE;
    _name = name;
    _peers = new HashMap<>();
  }

  public boolean getEnable() {
    return _enable;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable BgpPeerGroupTypeAndOptions getTypeAndOptions() {
    return _typeAndOptions;
  }

  public @Nonnull EbgpPeerGroupType updateAndGetEbgpType() {
    if (!(_typeAndOptions instanceof EbgpPeerGroupType)) {
      _typeAndOptions = new EbgpPeerGroupType();
    }
    return (EbgpPeerGroupType) _typeAndOptions;
  }

  public @Nonnull IbgpPeerGroupType updateAndGetIbgpType() {
    if (!(_typeAndOptions instanceof IbgpPeerGroupType)) {
      _typeAndOptions = new IbgpPeerGroupType();
    }
    return (IbgpPeerGroupType) _typeAndOptions;
  }

  public @Nonnull BgpPeer getOrCreatePeerGroup(String name) {
    return _peers.computeIfAbsent(name, BgpPeer::new);
  }

  public @Nonnull Map<String, BgpPeer> getPeers() {
    return Collections.unmodifiableMap(_peers);
  }

  // private implementation details

  private boolean _enable;
  private final @Nonnull String _name;
  private @Nonnull final Map<String, BgpPeer> _peers;
  private @Nullable BgpPeerGroupTypeAndOptions _typeAndOptions;
}
