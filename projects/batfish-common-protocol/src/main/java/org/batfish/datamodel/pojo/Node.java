package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.DeviceType;

public class Node extends BfObject {

  private final String _name;

  private DeviceType _type;

  @JsonCreator
  public Node(@JsonProperty("name") String name) {
    super(getId(name));
    _name = name;
    _type = DeviceType.UNKNOWN;
  }

  public Node(String name, DeviceType type) {
    this(name);
    _type = type;
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

  public void setType(DeviceType type) {
    _type = type;
  }
}
