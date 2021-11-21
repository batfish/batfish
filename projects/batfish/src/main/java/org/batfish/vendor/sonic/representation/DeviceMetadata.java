package org.batfish.vendor.sonic.representation;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
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
}
