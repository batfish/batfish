package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.vendor_family.f5_bigip.DeviceGroupType;

/** Centralized management device-group */
public final class DeviceGroup implements Serializable {

  public DeviceGroup(String name) {
    _name = name;
    _devices = new HashMap<>();
  }

  public @Nullable Boolean getAutoSync() {
    return _autoSync;
  }

  public void setAutoSync(@Nullable Boolean autoSync) {
    _autoSync = autoSync;
  }

  public @Nonnull Map<String, DeviceGroupDevice> getDevices() {
    return _devices;
  }

  public @Nullable Boolean getHidden() {
    return _hidden;
  }

  public void setHidden(@Nullable Boolean hidden) {
    _hidden = hidden;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Boolean getNetworkFailover() {
    return _networkFailover;
  }

  public void setNetworkFailover(@Nullable Boolean networkFailover) {
    _networkFailover = networkFailover;
  }

  public @Nullable DeviceGroupType getType() {
    return _type;
  }

  public void setType(@Nullable DeviceGroupType type) {
    _type = type;
  }

  private @Nullable Boolean _autoSync;
  private final @Nonnull Map<String, DeviceGroupDevice> _devices;
  private @Nullable Boolean _hidden;
  private final @Nonnull String _name;
  private @Nullable Boolean _networkFailover;
  private @Nullable DeviceGroupType _type;
}
