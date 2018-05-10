package org.batfish.datamodel.pojo;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;

public class Interface extends BfObject {

  public enum InterfaceType {
    PHYSICAL,
    UNKNOWN,
    VIRTUAL,
  }

  private static final String PROP_NAME = "name";

  private static final String PROP_NODE_ID = "nodeId";

  private static final String PROP_TYPE = "type";

  @Nonnull private final String _name;

  @Nonnull private final String _nodeId;

  @Nonnull private InterfaceType _type;

  public Interface(String nodeId, String name) {
    this(nodeId, name, InterfaceType.UNKNOWN);
  }

  public Interface(String nodeId, String name, InterfaceType type) {
    this(getId(nodeId, name), nodeId, name, type);
  }

  @JsonCreator
  public Interface(
      @JsonProperty(PROP_ID) String id,
      @Nonnull @JsonProperty(PROP_NODE_ID) String nodeId,
      @Nonnull @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_TYPE) InterfaceType type) {
    super(firstNonNull(id, getId(nodeId, name)));
    _nodeId = nodeId;
    _name = name;
    _type = firstNonNull(type, InterfaceType.UNKNOWN);
    if (nodeId == null) {
      throw new IllegalArgumentException("Cannot build interface: nodeId is null");
    }
    if (name == null) {
      throw new IllegalArgumentException("Cannot build interface: name is null");
    }
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
