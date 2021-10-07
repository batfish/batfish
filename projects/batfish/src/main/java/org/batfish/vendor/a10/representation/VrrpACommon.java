package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Common vrrp-a settings on ACOS5. */
public final class VrrpACommon implements Serializable {

  public @Nullable Integer getDeviceId() {
    return _deviceId;
  }

  public boolean getDisableDefaultVrid() {
    return _disableDefaultVrid;
  }

  public void setDisableDefaultVrid(boolean disableDefaultVrid) {
    _disableDefaultVrid = disableDefaultVrid;
  }

  public boolean getEnable() {
    return _enable;
  }

  public void setEnable(boolean enable) {
    _enable = enable;
  }

  public void setDeviceId(@Nullable Integer deviceId) {
    _deviceId = deviceId;
  }

  public @Nullable Integer getSetId() {
    return _setId;
  }

  public void setSetId(@Nullable Integer setId) {
    _setId = setId;
  }

  private @Nullable Integer _deviceId;
  private boolean _disableDefaultVrid;
  private boolean _enable;
  private @Nullable Integer _setId;
}
