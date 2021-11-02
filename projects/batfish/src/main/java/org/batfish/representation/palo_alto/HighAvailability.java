package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Datamodel class representing high-availability configuration for a PaloAlto device. */
public final class HighAvailability implements Serializable {
  /** The high-availability device id (number) assigned to this device. */
  @Nullable
  public Integer getDeviceId() {
    return _deviceId;
  }

  public void setDeviceId(@Nullable Integer deviceId) {
    _deviceId = deviceId;
  }

  // TODO: relocate this once we flesh out datamodel to better represent
  // active-active/active-passive hierarchy
  @Nullable private Integer _deviceId;
}
