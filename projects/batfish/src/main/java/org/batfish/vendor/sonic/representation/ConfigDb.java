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

  @ParametersAreNonnullByDefault
  public static class Data implements Serializable {
    private final @Nonnull Map<ConfigDbObject.Type, ConfigDbObject> _objects;

    public Data(Map<ConfigDbObject.Type, ConfigDbObject> objects) {
      _objects = ImmutableMap.copyOf(objects);
    }

    @JsonCreator
    private static Data create(Map<String, JsonNode> objects) throws JsonProcessingException {
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
      return new Data(objectMap);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Data)) {
        return false;
      }
      Data data = (Data) o;
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

  private final @Nonnull String _filename;
  private final @Nonnull Data _data;

  public ConfigDb(String filename, Data data) {
    _filename = filename;
    _data = data;
  }

  public Optional<String> getHostname() {
    if (_data._objects.containsKey(DEVICE_METADATA)) {
      return ((DeviceMetadata) _data._objects.get(DEVICE_METADATA))
          .getHostname()
          .map(String::toLowerCase);
    }
    return Optional.empty();
  }

  @Nonnull
  public String getFilename() {
    return _filename;
  }

  @Nonnull
  public Data getData() {
    return _data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConfigDb)) {
      return false;
    }
    ConfigDb configDb = (ConfigDb) o;
    return _filename.equals(configDb._filename) && _data.equals(configDb._data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_filename, _data);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("filename", _filename)
        .add("data", _data)
        .toString();
  }
}
