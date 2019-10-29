package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.MacAddress;

/**
 * Centralized management traffic-group that may float among {@link Device}s within a {@link
 * DeviceGroup}.
 */
public final class TrafficGroup implements Serializable {

  public TrafficGroup(String name) {
    _name = name;
  }

  public @Nullable String getHaGroup() {
    return _haGroup;
  }

  public void setHaGroup(@Nullable String haGroup) {
    _haGroup = haGroup;
  }

  public @Nullable MacAddress getMac() {
    return _mac;
  }

  public void setMac(@Nullable MacAddress mac) {
    _mac = mac;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getUnitId() {
    return _unitId;
  }

  public void setUnitId(@Nullable Integer unitId) {
    _unitId = unitId;
  }

  private @Nullable String _haGroup;
  private @Nullable MacAddress _mac;
  private final @Nonnull String _name;
  private @Nullable Integer _unitId;
}
