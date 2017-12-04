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

  private static final String PROP_NAME = "name";

  private List<Interface> _interfaces;

  private final String _name;

  private DeviceType _type;

  @JsonCreator
  public Node(@JsonProperty(PROP_NAME) String name) {
    super("node-" + name);
    _name = name;
    _type = DeviceType.UNKNOWN;
  }

  public Node(String name, DeviceType type) {
    this(name);
    _type = type;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }
}
