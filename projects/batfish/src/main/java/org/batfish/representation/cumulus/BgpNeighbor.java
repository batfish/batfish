package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Parent for all BGP neighbors. */
public abstract class BgpNeighbor implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull String _name;
  private @Nullable String _description;
  private @Nullable String _peerGroup;

  // Inheritable properties
  private @Nullable Long _remoteAs;
  private @Nullable RemoteAsType _remoteAsType;

  // Whether this configuration has inherited from its parent.
  private boolean _inherited = false;

  public BgpNeighbor(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Nullable
  public String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  @Nullable
  public String getPeerGroup() {
    return _peerGroup;
  }

  public void setPeerGroup(@Nullable String peerGroup) {
    _peerGroup = peerGroup;
  }

  /**
   * Returns explicit remote-as number when {@link #getRemoteAsType} is {@link
   * RemoteAsType#EXPLICIT}, or else {@code null}.
   */
  public @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public @Nullable RemoteAsType getRemoteAsType() {
    return _remoteAsType;
  }

  public void setRemoteAs(@Nullable Long remoteAs) {
    _remoteAs = remoteAs;
  }

  public void setRemoteAsType(@Nullable RemoteAsType remoteAsType) {
    _remoteAsType = remoteAsType;
  }

  protected void inheritFrom(@Nonnull Map<String, BgpNeighbor> peers) {
    if (_inherited) {
      return;
    }
    _inherited = true;

    @Nullable BgpNeighbor other = _peerGroup == null ? null : peers.get(_peerGroup);
    if (other == null) {
      return;
    }

    // Do not inherit description.
    // Do not inherit name.
    // Do not inherit peer group.

    if (_remoteAs == null) {
      _remoteAs = other.getRemoteAs();
    }
    if (_remoteAsType == null) {
      _remoteAsType = other.getRemoteAsType();
    }
  }
}
