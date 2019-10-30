package org.batfish.datamodel.vendor_family.f5_bigip;

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

public final class F5BigipFamily implements Serializable {

  public static final class Builder {

    private Builder() {
      _devices = ImmutableMap.builder();
      _deviceGroups = ImmutableMap.builder();
      _haGroups = ImmutableMap.builder();
      _pools = ImmutableMap.builder();
      _trafficGroups = ImmutableMap.builder();
      _virtuals = ImmutableMap.builder();
      _virtualAddresses = ImmutableMap.builder();
    }

    public @Nonnull F5BigipFamily build() {
      return new F5BigipFamily(
          _devices.build(),
          _deviceGroups.build(),
          _haGroups.build(),
          _pools.build(),
          _trafficGroups.build(),
          _virtuals.build(),
          _virtualAddresses.build());
    }

    public @Nonnull Builder addDevice(Device device) {
      _devices.put(device.getName(), device);
      return this;
    }

    public @Nonnull Builder setDevices(Map<String, Device> devices) {
      _devices = ImmutableMap.<String, Device>builder().putAll(devices);
      return this;
    }

    public @Nonnull Builder addDeviceGroup(DeviceGroup deviceGroup) {
      _deviceGroups.put(deviceGroup.getName(), deviceGroup);
      return this;
    }

    public @Nonnull Builder setDeviceGroups(Map<String, DeviceGroup> deviceGroups) {
      _deviceGroups = ImmutableMap.<String, DeviceGroup>builder().putAll(deviceGroups);
      return this;
    }

    public @Nonnull Builder addHaGroup(HaGroup haGroup) {
      _haGroups.put(haGroup.getName(), haGroup);
      return this;
    }

    public @Nonnull Builder setHaGroups(Map<String, HaGroup> haGroups) {
      _haGroups = ImmutableMap.<String, HaGroup>builder().putAll(haGroups);
      return this;
    }

    public @Nonnull Builder addPool(Pool pool) {
      _pools.put(pool.getName(), pool);
      return this;
    }

    public @Nonnull Builder setPools(Map<String, Pool> pools) {
      _pools = ImmutableMap.<String, Pool>builder().putAll(pools);
      return this;
    }

    public @Nonnull Builder addTrafficGroup(TrafficGroup trafficGroup) {
      _trafficGroups.put(trafficGroup.getName(), trafficGroup);
      return this;
    }

    public @Nonnull Builder setTrafficGroups(Map<String, TrafficGroup> trafficGroups) {
      _trafficGroups = ImmutableMap.<String, TrafficGroup>builder().putAll(trafficGroups);
      return this;
    }

    public @Nonnull Builder addVirtualAddress(VirtualAddress virtualAddress) {
      _virtualAddresses.put(virtualAddress.getName(), virtualAddress);
      return this;
    }

    public @Nonnull Builder setVirtualAddresses(Map<String, VirtualAddress> virtualAddresses) {
      _virtualAddresses = ImmutableMap.<String, VirtualAddress>builder().putAll(virtualAddresses);
      return this;
    }

    public @Nonnull Builder addVirtual(Virtual virtual) {
      _virtuals.put(virtual.getName(), virtual);
      return this;
    }

    public @Nonnull Builder setVirtuals(Map<String, Virtual> virtuals) {
      _virtuals = ImmutableMap.<String, Virtual>builder().putAll(virtuals);
      return this;
    }

    private @Nonnull ImmutableMap.Builder<String, Device> _devices;
    private @Nonnull ImmutableMap.Builder<String, DeviceGroup> _deviceGroups;
    private @Nonnull ImmutableMap.Builder<String, HaGroup> _haGroups;
    private @Nonnull ImmutableMap.Builder<String, Pool> _pools;
    private @Nonnull ImmutableMap.Builder<String, TrafficGroup> _trafficGroups;
    private @Nonnull ImmutableMap.Builder<String, VirtualAddress> _virtualAddresses;
    private @Nonnull ImmutableMap.Builder<String, Virtual> _virtuals;
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @JsonIgnore
  public @Nonnull Map<String, Device> getDevices() {
    return _devices;
  }

  @JsonProperty(PROP_DEVICES)
  private @Nonnull SortedMap<String, Device> getDevicesSorted() {
    return ImmutableSortedMap.copyOf(_devices);
  }

  @JsonIgnore
  public @Nonnull Map<String, DeviceGroup> getDeviceGroups() {
    return _deviceGroups;
  }

  @JsonProperty(PROP_DEVICE_GROUPS)
  private @Nonnull SortedMap<String, DeviceGroup> getDeviceGroupsSorted() {
    return ImmutableSortedMap.copyOf(_deviceGroups);
  }

  @JsonIgnore
  public @Nonnull Map<String, HaGroup> getHaGroups() {
    return _haGroups;
  }

  @JsonProperty(PROP_HA_GROUPS)
  private @Nonnull SortedMap<String, HaGroup> getHaGroupsSorted() {
    return ImmutableSortedMap.copyOf(_haGroups);
  }

  @JsonIgnore
  public @Nonnull Map<String, Pool> getPools() {
    return _pools;
  }

  @JsonProperty(PROP_POOLS)
  private @Nonnull SortedMap<String, Pool> getPoolsSorted() {
    return ImmutableSortedMap.copyOf(_pools);
  }

  @JsonIgnore
  public @Nonnull Map<String, TrafficGroup> getTrafficGroups() {
    return _trafficGroups;
  }

  @JsonProperty(PROP_TRAFFIC_GROUPS)
  private @Nonnull SortedMap<String, TrafficGroup> getTrafficGroupsSorted() {
    return ImmutableSortedMap.copyOf(_trafficGroups);
  }

  @JsonIgnore
  public @Nonnull Map<String, VirtualAddress> getVirtualAddresses() {
    return _virtualAddresses;
  }

  @JsonProperty(PROP_VIRTUAL_ADDRESSES)
  private @Nonnull SortedMap<String, VirtualAddress> getVirtualAddressesSorted() {
    return ImmutableSortedMap.copyOf(_virtualAddresses);
  }

  @JsonIgnore
  public @Nonnull Map<String, Virtual> getVirtuals() {
    return _virtuals;
  }

  @JsonProperty(PROP_VIRTUALS)
  public @Nonnull SortedMap<String, Virtual> getVirtualsSorted() {
    return ImmutableSortedMap.copyOf(_virtuals);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof F5BigipFamily)) {
      return false;
    }
    F5BigipFamily rhs = (F5BigipFamily) obj;
    return _devices.equals(rhs._devices)
        && _deviceGroups.equals(rhs._deviceGroups)
        && _haGroups.equals(rhs._haGroups)
        && _pools.equals(rhs._pools)
        && _trafficGroups.equals(rhs._trafficGroups)
        && _virtualAddresses.equals(rhs._virtualAddresses)
        && _virtuals.equals(rhs._virtuals);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _devices, _deviceGroups, _haGroups, _pools, _trafficGroups, _virtualAddresses, _virtuals);
  }

  private static final String PROP_DEVICES = "devices";
  private static final String PROP_DEVICE_GROUPS = "deviceGroups";
  private static final String PROP_HA_GROUPS = "haGroups";
  private static final String PROP_POOLS = "pools";
  private static final String PROP_TRAFFIC_GROUPS = "trafficGroups";
  private static final String PROP_VIRTUAL_ADDRESSES = "virtualAddresses";
  private static final String PROP_VIRTUALS = "virtuals";

  @JsonCreator
  private static final @Nonnull F5BigipFamily create(
      @JsonProperty(PROP_DEVICES) @Nullable Map<String, Device> devices,
      @JsonProperty(PROP_DEVICE_GROUPS) @Nullable Map<String, DeviceGroup> deviceGroups,
      @JsonProperty(PROP_HA_GROUPS) @Nullable Map<String, HaGroup> haGroups,
      @JsonProperty(PROP_POOLS) @Nullable Map<String, Pool> pools,
      @JsonProperty(PROP_TRAFFIC_GROUPS) @Nullable Map<String, TrafficGroup> trafficGroups,
      @JsonProperty(PROP_VIRTUALS) @Nullable Map<String, Virtual> virtuals,
      @JsonProperty(PROP_VIRTUAL_ADDRESSES) @Nullable
          Map<String, VirtualAddress> virtualAddresses) {
    Builder builder = builder();
    ofNullable(devices).ifPresent(builder::setDevices);
    ofNullable(deviceGroups).ifPresent(builder::setDeviceGroups);
    ofNullable(haGroups).ifPresent(builder::setHaGroups);
    ofNullable(pools).ifPresent(builder::setPools);
    ofNullable(trafficGroups).ifPresent(builder::setTrafficGroups);
    ofNullable(virtuals).ifPresent(builder::setVirtuals);
    ofNullable(virtualAddresses).ifPresent(builder::setVirtualAddresses);
    return builder.build();
  }

  private final @Nonnull Map<String, Device> _devices;
  private final @Nonnull Map<String, DeviceGroup> _deviceGroups;
  private final @Nonnull Map<String, HaGroup> _haGroups;
  private final @Nonnull Map<String, Pool> _pools;
  private final @Nonnull Map<String, TrafficGroup> _trafficGroups;
  private final @Nonnull Map<String, VirtualAddress> _virtualAddresses;
  private final @Nonnull Map<String, Virtual> _virtuals;

  private F5BigipFamily(
      Map<String, Device> devices,
      Map<String, DeviceGroup> deviceGroups,
      Map<String, HaGroup> haGroups,
      Map<String, Pool> pools,
      Map<String, TrafficGroup> trafficGroups,
      Map<String, Virtual> virtuals,
      Map<String, VirtualAddress> virtualAddresses) {
    _devices = devices;
    _deviceGroups = deviceGroups;
    _haGroups = haGroups;
    _pools = pools;
    _trafficGroups = trafficGroups;
    _virtuals = virtuals;
    _virtualAddresses = virtualAddresses;
  }
}
