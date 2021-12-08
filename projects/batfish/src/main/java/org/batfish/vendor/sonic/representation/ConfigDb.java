package org.batfish.vendor.sonic.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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

  public @Nonnull Map<String, DeviceMetadata> getDeviceMetadata() {
    return _deviceMetadata;
  }

  public @Nonnull Map<String, L3Interface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull Map<String, L3Interface> getLoopbacks() {
    return _loopbacks;
  }

  public @Nonnull Set<String> getNtpServers() {
    return _ntpServers;
  }

  public @Nonnull Map<String, Port> getPorts() {
    return _ports;
  }

  public @Nonnull Set<String> getSyslogServers() {
    return _syslogServers;
  }

  private static final String PROP_DEVICE_METADATA = "DEVICE_METADATA";
  private static final String PROP_INTERFACE = "INTERFACE";
  private static final String PROP_LOOPBACK = "LOOPBACK";
  private static final String PROP_PORT = "PORT";
  private static final String PROP_NTP_SERVER = "NTP_SERVER";
  private static final String PROP_SYSLOG_SERVER = "SYSLOG_SERVER";

  private final @Nonnull Map<String, DeviceMetadata> _deviceMetadata;
  private final @Nonnull Map<String, L3Interface> _interfaces;
  private final @Nonnull Map<String, L3Interface> _loopbacks;
  private final @Nonnull Set<String> _ntpServers;
  private final @Nonnull Map<String, Port> _ports;
  private final @Nonnull Set<String> _syslogServers;

  private ConfigDb(
      Map<String, DeviceMetadata> deviceMetadata,
      Map<String, L3Interface> interfaces,
      Map<String, L3Interface> loopbacks,
      Set<String> ntpServers,
      Map<String, Port> ports,
      Set<String> syslogServers) {
    _deviceMetadata = deviceMetadata;
    _interfaces = interfaces;
    _loopbacks = loopbacks;
    _ntpServers = ntpServers;
    _ports = ports;
    _syslogServers = syslogServers;
  }

  @JsonCreator
  private static ConfigDb create(
      @Nullable @JsonProperty(PROP_DEVICE_METADATA) Map<String, DeviceMetadata> deviceMetadata,
      @Nullable @JsonProperty(PROP_INTERFACE) Map<String, Object> interfacesMap,
      @Nullable @JsonProperty(PROP_LOOPBACK) Map<String, Object> loopbackMap,
      @Nullable @JsonProperty(PROP_NTP_SERVER) Map<String, Object> ntpServersMap,
      @Nullable @JsonProperty(PROP_PORT) Map<String, Port> ports,
      @Nullable @JsonProperty(PROP_SYSLOG_SERVER) Map<String, Object> syslogServersMap) {

    // in many cases below, all data is embedded in the key of the map, the value is empty.
    // that is why we are using only the keys

    return ConfigDb.builder()
        .setDeviceMetadata(deviceMetadata)
        .setInterfaces(
            createInterfaces(
                firstNonNull(interfacesMap, ImmutableMap.<String, Object>of()).keySet()))
        .setLoopbacks(
            createInterfaces(firstNonNull(loopbackMap, ImmutableMap.<String, Object>of()).keySet()))
        .setNtpServers(firstNonNull(ntpServersMap, ImmutableMap.<String, Object>of()).keySet())
        .setPorts(ports)
        .setSyslogServers(
            firstNonNull(syslogServersMap, ImmutableMap.<String, Object>of()).keySet())
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
    return _deviceMetadata.equals(other._deviceMetadata)
        && _interfaces.equals(other._interfaces)
        && _loopbacks.equals(other._loopbacks)
        && _ntpServers.equals(other._ntpServers)
        && _ports.equals(other._ports)
        && _syslogServers.equals(other._syslogServers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _deviceMetadata, _interfaces, _loopbacks, _ntpServers, _ports, _syslogServers);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Map<String, DeviceMetadata> _deviceMetadata;
    private Map<String, L3Interface> _interfaces;
    private Map<String, L3Interface> _loopbacks;
    private Set<String> _ntpServers;
    private Map<String, Port> _ports;
    private Set<String> _syslogServers;

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

    public @Nonnull Builder setLoopbacks(@Nullable Map<String, L3Interface> loopbacks) {
      this._loopbacks = loopbacks;
      return this;
    }

    public @Nonnull Builder setNtpServers(@Nullable Set<String> ntpServers) {
      this._ntpServers = ntpServers;
      return this;
    }

    public @Nonnull Builder setPorts(@Nullable Map<String, Port> ports) {
      this._ports = ports;
      return this;
    }

    public @Nonnull Builder setSyslogServers(@Nullable Set<String> syslogServers) {
      this._syslogServers = syslogServers;
      return this;
    }

    public @Nonnull ConfigDb build() {
      return new ConfigDb(
          ImmutableMap.copyOf(firstNonNull(_deviceMetadata, ImmutableMap.of())),
          ImmutableMap.copyOf(firstNonNull(_interfaces, ImmutableMap.of())),
          ImmutableMap.copyOf(firstNonNull(_loopbacks, ImmutableMap.of())),
          ImmutableSet.copyOf(firstNonNull(_ntpServers, ImmutableSet.of())),
          ImmutableMap.copyOf(firstNonNull(_ports, ImmutableMap.of())),
          ImmutableSet.copyOf(firstNonNull(_syslogServers, ImmutableSet.of())));
    }
  }
}
