package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Centralized management device-group */
public final class DeviceGroup implements Serializable {

  public static final class Builder {

    public @Nonnull DeviceGroup build() {
      checkArgument(_name != null, "Missing name");
      return new DeviceGroup(_autoSync, _devices.build(), _hidden, _name, _networkFailover, _type);
    }

    public @Nonnull Builder setAutoSync(@Nullable Boolean autoSync) {
      _autoSync = autoSync;
      return this;
    }

    public @Nonnull Builder addDevice(DeviceGroupDevice device) {
      _devices.put(device.getName(), device);
      return this;
    }

    public @Nonnull Builder setDevices(Map<String, DeviceGroupDevice> devices) {
      _devices = ImmutableMap.<String, DeviceGroupDevice>builder().putAll(devices);
      return this;
    }

    public @Nonnull Builder setHidden(@Nullable Boolean hidden) {
      _hidden = hidden;
      return this;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setNetworkFailover(@Nullable Boolean networkFailover) {
      _networkFailover = networkFailover;
      return this;
    }

    public @Nonnull Builder setType(@Nullable DeviceGroupType type) {
      _type = type;
      return this;
    }

    private @Nullable Boolean _autoSync;
    private @Nonnull ImmutableMap.Builder<String, DeviceGroupDevice> _devices;
    private @Nullable Boolean _hidden;
    private @Nullable String _name;
    private @Nullable Boolean _networkFailover;
    private @Nullable DeviceGroupType _type;

    private Builder() {
      _devices = ImmutableMap.builder();
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public @Nullable Boolean getAutoSync() {
    return _autoSync;
  }

  public @Nonnull Map<String, DeviceGroupDevice> getDevices() {
    return _devices;
  }

  public @Nullable Boolean getHidden() {
    return _hidden;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nullable Boolean getNetworkFailover() {
    return _networkFailover;
  }

  public @Nullable DeviceGroupType getType() {
    return _type;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DeviceGroup)) {
      return false;
    }
    DeviceGroup rhs = (DeviceGroup) obj;
    return Objects.equals(_autoSync, rhs._autoSync)
        && _devices.equals(rhs._devices)
        && Objects.equals(_hidden, rhs._hidden)
        && _name.equals(rhs._name)
        && Objects.equals(_networkFailover, rhs._networkFailover)
        && Objects.equals(_type, rhs._type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _autoSync,
        _devices,
        _hidden,
        _name,
        _networkFailover,
        _type != null ? _type.ordinal() : null);
  }

  private DeviceGroup(
      @Nullable Boolean autoSync,
      Map<String, DeviceGroupDevice> devices,
      @Nullable Boolean hidden,
      String name,
      @Nullable Boolean networkFailover,
      @Nullable DeviceGroupType type) {
    _autoSync = autoSync;
    _devices = devices;
    _hidden = hidden;
    _name = name;
    _networkFailover = networkFailover;
    _type = type;
  }

  private final @Nullable Boolean _autoSync;
  private final @Nonnull Map<String, DeviceGroupDevice> _devices;
  private final @Nullable Boolean _hidden;
  private final @Nonnull String _name;
  private final @Nullable Boolean _networkFailover;
  private final @Nullable DeviceGroupType _type;
}
