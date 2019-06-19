package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Interface to be used for BGP unnumbered session */
public class BgpInterfaceNeighbor implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull String _name;
  private @Nullable Long _remoteAs;
  private @Nullable RemoteAsType _remoteAsType;

  public BgpInterfaceNeighbor(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
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
}
