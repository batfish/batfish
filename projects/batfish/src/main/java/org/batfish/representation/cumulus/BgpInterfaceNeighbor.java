package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BgpInterfaceNeighbor implements Serializable {

  private static final long serialVersionUID = 1L;

  private final @Nonnull String _name;
  private @Nullable RemoteAsType _remoteAsType;

  public BgpInterfaceNeighbor(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable RemoteAsType getRemoteAsType() {
    return _remoteAsType;
  }

  public void setRemoteAsType(@Nullable RemoteAsType remoteAsType) {
    _remoteAsType = remoteAsType;
  }
}
