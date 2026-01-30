package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Datamodel class representing high-availability configuration for a PaloAlto device. */
public final class HighAvailability implements Serializable {
  /** The high-availability device id (number) assigned to this device. */
  public @Nullable Integer getDeviceId() {
    return _deviceId;
  }

  public void setDeviceId(@Nullable Integer deviceId) {
    _deviceId = deviceId;
  }

  /** The high-availability group ID. */
  public @Nullable Integer getGroupId() {
    return _groupId;
  }

  public void setGroupId(@Nullable Integer groupId) {
    _groupId = groupId;
  }

  /** Whether high-availability is enabled. */
  public @Nullable Boolean getEnabled() {
    return _enabled;
  }

  public void setEnabled(@Nullable Boolean enabled) {
    _enabled = enabled;
  }

  // TODO: relocate this once we flesh out datamodel to better represent
  // active-active/active-passive hierarchy
  private @Nullable Integer _deviceId;

  private @Nullable Integer _groupId;

  private @Nullable Boolean _enabled;
}
