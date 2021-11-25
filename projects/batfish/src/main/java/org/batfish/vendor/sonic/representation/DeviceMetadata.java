package org.batfish.vendor.sonic.representation;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;

/** Represents device metadata: https://github.com/Azure/SONiC/wiki/Configuration#device-metadata */
public class DeviceMetadata implements ConfigDbObject {

  private @Nonnull final Map<String, String> _properties;

  public DeviceMetadata(Map<String, String> properties) {
    _properties = ImmutableMap.copyOf(properties);
  }

  public Optional<String> getHostname() {
    return Optional.ofNullable(_properties.get("hostname"));
  }

  @JsonCreator
  private static DeviceMetadata create(Map<String, Map<String, String>> properties) {
    checkArgument(properties.size() == 1, "DEVICE_METADATA should have only one property");
    checkArgument(properties.containsKey("localhost"), "'localhost' not found in %DEVICE_METADATA");
    return new DeviceMetadata(properties.get("localhost"));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DeviceMetadata)) {
      return false;
    }
    DeviceMetadata that = (DeviceMetadata) o;
    return _properties.equals(that._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_properties);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("properties", _properties).toString();
  }
}
