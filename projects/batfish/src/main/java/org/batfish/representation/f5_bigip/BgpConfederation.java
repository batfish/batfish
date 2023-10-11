package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** BGP confederation settings */
public final class BgpConfederation implements Serializable {

  public BgpConfederation() {
    _peers = new ArrayList<>();
  }

  public @Nullable Long getId() {
    return _id;
  }

  public void setId(@Nullable Long id) {
    _id = id;
  }

  public @Nonnull List<Long> getPeers() {
    return _peers;
  }

  private @Nullable Long _id;
  private final @Nonnull List<Long> _peers;
}
