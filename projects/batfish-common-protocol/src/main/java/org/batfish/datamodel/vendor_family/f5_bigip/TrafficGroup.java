package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.MacAddress;

/**
 * Centralized management traffic-group that may float among {@link Device}s within a {@link
 * DeviceGroup}.
 */
public final class TrafficGroup implements Serializable {

  public static final class Builder {

    public @Nonnull TrafficGroup build() {
      checkArgument(_name != null, "Missing name");
      return new TrafficGroup(_haGroup, _mac, _name, _unitId);
    }

    public @Nonnull Builder setHaGroup(@Nullable String haGroup) {
      _haGroup = haGroup;
      return this;
    }

    public @Nonnull Builder setMac(@Nullable MacAddress mac) {
      _mac = mac;
      return this;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setUnitId(@Nullable Integer unitId) {
      _unitId = unitId;
      return this;
    }

    private @Nullable String _haGroup;
    private @Nullable MacAddress _mac;
    private @Nullable String _name;
    private @Nullable Integer _unitId;

    private Builder() {}
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public @Nullable String getHaGroup() {
    return _haGroup;
  }

  public @Nullable MacAddress getMac() {
    return _mac;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Integer getUnitId() {
    return _unitId;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TrafficGroup)) {
      return false;
    }
    TrafficGroup rhs = (TrafficGroup) obj;
    return Objects.equals(_haGroup, rhs._haGroup)
        && Objects.equals(_mac, rhs._mac)
        && _name.equals(rhs._name)
        && Objects.equals(_unitId, rhs._unitId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_haGroup, _mac, _name, _unitId);
  }

  private final @Nullable String _haGroup;
  private final @Nullable MacAddress _mac;
  private final @Nonnull String _name;
  private final @Nullable Integer _unitId;

  private TrafficGroup(
      @Nullable String haGroup, @Nullable MacAddress mac, String name, @Nullable Integer unitId) {
    _haGroup = haGroup;
    _mac = mac;
    _name = name;
    _unitId = unitId;
  }
}
