package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Centralized management device-group */
public final class DeviceGroup implements Serializable {

  public static final class Builder {

    public @Nonnull DeviceGroup build() {
      checkArgument(_name != null, "Missing %s", PROP_NAME);
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

  @JsonProperty(PROP_AUTO_SYNC)
  public @Nullable Boolean getAutoSync() {
    return _autoSync;
  }

  @JsonIgnore
  public @Nonnull Map<String, DeviceGroupDevice> getDevices() {
    return _devices;
  }

  @JsonProperty(PROP_DEVICES)
  private @Nonnull SortedMap<String, DeviceGroupDevice> getDevicesSorted() {
    return ImmutableSortedMap.copyOf(_devices);
  }

  @JsonProperty(PROP_HIDDEN)
  public @Nullable Boolean getHidden() {
    return _hidden;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_NETWORK_FAILOVER)
  public @Nullable Boolean getNetworkFailover() {
    return _networkFailover;
  }

  @JsonProperty(PROP_TYPE)
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

  private static final String PROP_AUTO_SYNC = "autoSync";
  private static final String PROP_DEVICES = "devices";
  private static final String PROP_HIDDEN = "hidden";
  private static final String PROP_NAME = "name";
  private static final String PROP_NETWORK_FAILOVER = "networkFailover";
  private static final String PROP_TYPE = "type";

  @JsonCreator
  private static @Nonnull DeviceGroup create(
      @JsonProperty(PROP_AUTO_SYNC) @Nullable Boolean autoSync,
      @JsonProperty(PROP_DEVICES) @Nullable Map<String, DeviceGroupDevice> devices,
      @JsonProperty(PROP_HIDDEN) @Nullable Boolean hidden,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_NETWORK_FAILOVER) @Nullable Boolean networkFailover,
      @JsonProperty(PROP_TYPE) @Nullable DeviceGroupType type) {
    Builder builder = builder().setAutoSync(autoSync);
    ofNullable(devices).ifPresent(d -> d.values().forEach(builder::addDevice));
    builder.setHidden(hidden);
    ofNullable(name).ifPresent(builder::setName);
    return builder.setNetworkFailover(networkFailover).setType(type).build();
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
