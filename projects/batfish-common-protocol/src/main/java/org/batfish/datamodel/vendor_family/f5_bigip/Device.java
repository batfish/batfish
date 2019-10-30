package org.batfish.datamodel.vendor_family.f5_bigip;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;

/** Centralized management device */
public final class Device implements Serializable {

  public static final class Builder {

    public @Nonnull Device build() {
      checkArgument(_name != null, "Missing %s", PROP_NAME);
      return new Device(
          _baseMac,
          _configSyncIp,
          _hostname,
          _managementIp,
          _name,
          _selfDevice,
          _unicastAddresses.build());
    }

    public @Nonnull Builder setBaseMac(@Nullable MacAddress baseMac) {
      _baseMac = baseMac;
      return this;
    }

    public @Nonnull Builder setConfigSyncIp(@Nullable Ip configSyncIp) {
      _configSyncIp = configSyncIp;
      return this;
    }

    public @Nonnull Builder setHostname(@Nullable String hostname) {
      _hostname = hostname;
      return this;
    }

    public @Nonnull Builder setManagementIp(@Nullable Ip managementIp) {
      _managementIp = managementIp;
      return this;
    }

    public @Nonnull Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setSelfDevice(@Nullable Boolean selfDevice) {
      _selfDevice = selfDevice;
      return this;
    }

    public @Nonnull Builder addUnicastAddress(UnicastAddress unicastAddress) {
      _unicastAddresses.add(unicastAddress);
      return this;
    }

    public @Nonnull Builder setUnicastAddresses(Iterable<UnicastAddress> unicastAddresses) {
      _unicastAddresses = ImmutableList.<UnicastAddress>builder().addAll(unicastAddresses);
      return this;
    }

    private @Nullable MacAddress _baseMac;
    private @Nullable Ip _configSyncIp;
    private @Nullable String _hostname;
    private @Nullable Ip _managementIp;
    private @Nullable String _name;
    private @Nullable Boolean _selfDevice;
    private @Nonnull ImmutableList.Builder<UnicastAddress> _unicastAddresses;

    private Builder() {
      _unicastAddresses = ImmutableList.builder();
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  public Device(
      @Nullable MacAddress baseMac,
      @Nullable Ip configSyncIp,
      @Nullable String hostname,
      @Nullable Ip managementIp,
      String name,
      @Nullable Boolean selfDevice,
      List<UnicastAddress> unicastAddresses) {
    _baseMac = baseMac;
    _configSyncIp = configSyncIp;
    _hostname = hostname;
    _managementIp = managementIp;
    _name = name;
    _selfDevice = selfDevice;
    _unicastAddresses = unicastAddresses;
  }

  @JsonProperty(PROP_BASE_MAC)
  public @Nullable MacAddress getBaseMac() {
    return _baseMac;
  }

  @JsonProperty(PROP_CONFIG_SYNC_IP)
  public @Nullable Ip getConfigSyncIp() {
    return _configSyncIp;
  }

  @JsonProperty(PROP_HOSTNAME)
  public @Nullable String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_MANAGEMENT_IP)
  public @Nullable Ip getManagementIp() {
    return _managementIp;
  }

  @JsonProperty(PROP_NAME)
  public @Nonnull String getName() {
    return _name;
  }

  @JsonProperty(PROP_SELF_DEVICE)
  public @Nullable Boolean getSelfDevice() {
    return _selfDevice;
  }

  @JsonProperty(PROP_UNICAST_ADDRESSES)
  public @Nonnull List<UnicastAddress> getUnicastAddresses() {
    return _unicastAddresses;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Device)) {
      return false;
    }
    Device rhs = (Device) obj;
    return Objects.equals(_baseMac, rhs._baseMac)
        && Objects.equals(_configSyncIp, rhs._configSyncIp)
        && Objects.equals(_hostname, rhs._hostname)
        && Objects.equals(_managementIp, rhs._managementIp)
        && _name.equals(rhs._name)
        && Objects.equals(_selfDevice, rhs._selfDevice)
        && _unicastAddresses.equals(rhs._unicastAddresses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _baseMac, _configSyncIp, _hostname, _managementIp, _name, _selfDevice, _unicastAddresses);
  }

  private static final String PROP_BASE_MAC = "baseMac";
  private static final String PROP_CONFIG_SYNC_IP = "configSyncIp";
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_MANAGEMENT_IP = "managementIp";
  private static final String PROP_NAME = "name";
  private static final String PROP_SELF_DEVICE = "selfDevice";
  private static final String PROP_UNICAST_ADDRESSES = "unicastAddresses";

  @JsonCreator
  private static @Nonnull Device create(
      @JsonProperty(PROP_BASE_MAC) @Nullable MacAddress baseMac,
      @JsonProperty(PROP_CONFIG_SYNC_IP) @Nullable Ip configSyncIp,
      @JsonProperty(PROP_HOSTNAME) @Nullable String hostname,
      @JsonProperty(PROP_MANAGEMENT_IP) @Nullable Ip managementIp,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_SELF_DEVICE) @Nullable Boolean selfDevice,
      @JsonProperty(PROP_UNICAST_ADDRESSES) @Nullable List<UnicastAddress> unicastAddresses) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    Device.Builder builder =
        Device.builder()
            .setBaseMac(baseMac)
            .setConfigSyncIp(configSyncIp)
            .setHostname(hostname)
            .setManagementIp(managementIp);
    ofNullable(name).ifPresent(builder::setName);
    builder.setSelfDevice(selfDevice);
    ofNullable(unicastAddresses).ifPresent(ua -> ua.forEach(builder::addUnicastAddress));
    return builder.build();
  }

  private final @Nullable MacAddress _baseMac;
  private final @Nullable Ip _configSyncIp;
  private final @Nullable String _hostname;
  private final @Nullable Ip _managementIp;
  private final @Nonnull String _name;
  private final @Nullable Boolean _selfDevice;
  private final @Nonnull List<UnicastAddress> _unicastAddresses;
}
