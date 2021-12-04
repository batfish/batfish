package org.batfish.vendor.sonic.representation;

import static org.batfish.vendor.sonic.representation.ConfigDbObject.Type.DEVICE_METADATA;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.BatfishObjectMapper;

/**
 * Represents ConfigDb for a one Sonic node.
 *
 * <p>See https://github.com/Azure/SONiC/wiki/Configuration
 */
@ParametersAreNonnullByDefault
public class ConfigDb implements Serializable {
  private final @Nonnull Map<ConfigDbObject.Type, ConfigDbObject> _objects;

  public ConfigDb(Map<ConfigDbObject.Type, ConfigDbObject> objects) {
    _objects = ImmutableMap.copyOf(objects);
  }

  @JsonCreator
  private static ConfigDb create(Map<String, JsonNode> objects) throws JsonProcessingException {
    Map<ConfigDbObject.Type, ConfigDbObject> objectMap = new HashMap<>();
    for (String key : objects.keySet()) {
      try {
        ConfigDbObject.Type objectType = Enum.valueOf(ConfigDbObject.Type.class, key);
        switch (objectType) {
          case DEVICE_METADATA:
            objectMap.put(
                ConfigDbObject.Type.DEVICE_METADATA,
                BatfishObjectMapper.ignoreUnknownMapper()
                    .treeToValue(objects.get(key), DeviceMetadata.class));
            break;
          case INTERFACE:
            objectMap.put(
                ConfigDbObject.Type.INTERFACE,
                BatfishObjectMapper.ignoreUnknownMapper()
                    .treeToValue(objects.get(key), InterfaceDb.class));
            break;
          default:
            throw new UnsupportedOperationException(
                "Deserialization not implemented for " + objectType);
        }
      } catch (IllegalArgumentException e) {
        // ignore -- unknown object type
      }
    }
    return new ConfigDb(objectMap);
  }

  public Optional<String> getHostname() {
    return Optional.ofNullable(_objects.get(DEVICE_METADATA))
        .flatMap(dm -> ((DeviceMetadata) dm).getHostname())
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
    ConfigDb data = (ConfigDb) o;
    return Objects.equals(_objects, data._objects);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_objects);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("objects", _objects).toString();
  }
}
