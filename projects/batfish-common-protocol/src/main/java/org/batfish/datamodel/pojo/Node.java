package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.DeviceType;

public class Node extends BfObject {

  @Nonnull private final String _name;

  @Nullable private DeviceType _type;

  @JsonCreator
  public Node(
      @JsonProperty("name") String name,
      @JsonProperty("id") String id,
      @JsonProperty("type") DeviceType type) {
    super(id);
    _name = name;
    _type = type;
  }

  public Node(String name) {
    this(name, getId(name), null);
  }

  public Node(String name, DeviceType type) {
    this(name, getId(name), type);
  }

  public static String getId(String name) {
    return "node-" + name;
  }

  public String getName() {
    return _name;
  }

  public DeviceType getType() {
    return _type;
  }
}
