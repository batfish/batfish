package org.batfish.datamodel.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Interface extends BfObject {

  public enum InterfaceType {
    PHYSICAL,
    UNKNOWN,
    VIRTUAL,
  }

  private static final String PROP_NAME = "name";

  private static final String PROP_NODE_ID = "nodeId";

  private static final String PROP_TYPE = "type";

  private final String _name;

  private final String _nodeId;

  private InterfaceType _type;

  public Interface(String nodeId, String name, InterfaceType type) {
    super(getId(nodeId, name));
    _nodeId = nodeId;
    _name = name;
    _type = type;
  }

  @JsonCreator
  public Interface(
      @JsonProperty(PROP_NODE_ID) String nodeId, @JsonProperty(PROP_NAME) String name) {
    this(nodeId, name, InterfaceType.UNKNOWN);
  }

  public static String getId(String nodeId, String name) {
    return "interface-" + nodeId + "-" + name;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_NODE_ID)
  public String getNodeId() {
    return _nodeId;
  }

  @JsonProperty(PROP_TYPE)
  public InterfaceType getType() {
    return _type;
  }

  @JsonProperty(PROP_TYPE)
  public void setType(InterfaceType type) {
    _type = type;
  }
}
