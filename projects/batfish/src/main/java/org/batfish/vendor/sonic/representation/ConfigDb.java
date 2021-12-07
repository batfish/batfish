package org.batfish.vendor.sonic.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Prefix6;

/**
 * Represents ConfigDb for a one Sonic node.
 *
 * <p>See https://github.com/Azure/SONiC/wiki/Configuration
 */
public class ConfigDb implements Serializable {

  private static final String PROP_DEVICE_METADATA = "DEVICE_METADATA";
  private static final String PROP_INTERFACE = "INTERFACE";

  private final @Nonnull Map<String, DeviceMetadata> _deviceMetadata;
  private final @Nonnull Map<String, L3Interface> _interfaces;

  private ConfigDb(
      Map<String, DeviceMetadata> deviceMetadata, Map<String, L3Interface> interfaces) {
    _deviceMetadata = deviceMetadata;
    _interfaces = interfaces;
  }

  @JsonCreator
  private static ConfigDb create(
      @Nullable @JsonProperty(PROP_DEVICE_METADATA) Map<String, DeviceMetadata> deviceMetadata,
      @Nullable @JsonProperty(PROP_INTERFACE) Map<String, Object> interfacesMap) {
    return ConfigDb.builder()
        .setDeviceMetadata(deviceMetadata)
        .setInterfaces(
            createInterfaces(
                // all data is in the keys (see createInterfaces), so we just pass that in
                firstNonNull(interfacesMap, ImmutableMap.<String, Object>of()).keySet()))
        .build();
  }

  /**
   * Converts configdb's interface multi-level key encoding for interfaces, where keys are
   * "Ethernet", "Ethernet|1.1.1.1", to a map of interfaces.
   *
   * <p>In this encoding, the same interface may appear as key multiple times: by itself, with a v4
   * address, or with a v6 address.
   */
  @VisibleForTesting
  static Map<String, L3Interface> createInterfaces(Set<String> interfaceKeys) {
    Map<String, L3Interface> interfaces = new HashMap<>();
    for (String key : interfaceKeys) {
      String[] parts = key.split("\\|", 2);
      interfaces.computeIfAbsent(parts[0], i -> new L3Interface(null));
      if (parts.length == 2) {
        try {
          // if the interface appears with a v4 address, overwrite with that version
          ConcreteInterfaceAddress v4Address = ConcreteInterfaceAddress.parse(parts[1]);
          interfaces.put(parts[0], new L3Interface(v4Address));
        } catch (IllegalArgumentException e) {
          Prefix6.parse(parts[1]); // try to parse as v6; Will throw an exception upon failure
        }
      }
    }
    return ImmutableMap.copyOf(interfaces);
  }

  public @Nonnull Optional<String> getHostname() {
    return Optional.ofNullable(_deviceMetadata.get("localhost"))
        .flatMap(DeviceMetadata::getHostname)
        .map(String::toLowerCase);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConfigDb)) {
      return false;
    }
    ConfigDb other = (ConfigDb) o;
    return _deviceMetadata.equals(other._deviceMetadata) && _interfaces.equals(other._interfaces);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_deviceMetadata, _interfaces);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("deviceMetadata", _deviceMetadata)
        .add("interfaces", _interfaces)
        .toString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Map<String, DeviceMetadata> _deviceMetadata;
    private Map<String, L3Interface> _interfaces;

    private Builder() {}

    public @Nonnull Builder setDeviceMetadata(
        @Nullable Map<String, DeviceMetadata> deviceMetadata) {
      this._deviceMetadata = deviceMetadata;
      return this;
    }

    public @Nonnull Builder setInterfaces(@Nullable Map<String, L3Interface> interfaces) {
      this._interfaces = interfaces;
      return this;
    }

    public @Nonnull ConfigDb build() {
      return new ConfigDb(
          firstNonNull(ImmutableMap.copyOf(_deviceMetadata), ImmutableMap.of()),
          firstNonNull(ImmutableMap.copyOf(_interfaces), ImmutableMap.of()));
    }
  }
}
