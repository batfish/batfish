package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;

/** Configuration for a {@link Device} within a {@link DeviceGroup}. */
public final class DeviceGroupDevice implements Serializable {

  public DeviceGroupDevice(String name) {
    _name = name;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public boolean getSetSyncLeader() {
    return _setSyncLeader;
  }

  public void setSetSyncLeader(boolean setSyncLeader) {
    _setSyncLeader = setSyncLeader;
  }

  private final @Nonnull String _name;
  private boolean _setSyncLeader;
}
