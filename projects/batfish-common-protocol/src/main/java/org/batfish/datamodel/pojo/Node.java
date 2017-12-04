package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Node extends BfObject {

  // remove this class and point to the datamodel.devicetype in PR #661
  public enum DeviceType {
    HOST,
    ROUTER,
    SWITCH,
    UNKNOWN
  }

  private List<Interface> _interfaces;

  private final String _name;

  private DeviceType _type;

  @JsonCreator
  public Node(@JsonProperty("name") String name) {
    super("node-" + name);
    _name = name;
    _type = DeviceType.UNKNOWN;
  }

  public Node(String name, DeviceType type) {
    this(name);
    _type = type;
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
