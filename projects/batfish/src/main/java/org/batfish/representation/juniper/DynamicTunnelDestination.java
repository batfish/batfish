package org.batfish.representation.juniper;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class DynamicTunnelDestination implements Serializable {

  private final @Nullable Long _preference;

  public DynamicTunnelDestination(@Nullable Long preference) {
    _preference = preference;
  }

  public @Nullable Long getPreference() {
    return _preference;
  }
}
