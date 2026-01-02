package org.batfish.datamodel.pojo;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.InterfaceType;

public class Interface extends BfObject {
  private static final String PROP_NAME = "name";
  private static final String PROP_NODE_ID = "nodeId";
  private static final String PROP_TYPE = "type";

  private final @Nonnull String _name;

  private final @Nonnull String _nodeId;

  private @Nonnull InterfaceType _type;

  public Interface(String nodeId, String name) {
    this(nodeId, name, InterfaceType.UNKNOWN);
  }

  public Interface(String nodeId, String name, InterfaceType type) {
    this(getId(nodeId, name), nodeId, name, type);
  }

  @JsonCreator
  public Interface(
      @JsonProperty(PROP_ID) String id,
      @JsonProperty(PROP_NODE_ID) @Nonnull String nodeId,
      @JsonProperty(PROP_NAME) @Nonnull String name,
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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("_name", _name)
        .add("_nodeId", _nodeId)
        .add("_type", _type)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Interface)) {
      return false;
    }
    Interface that = (Interface) o;
    return _name.equals(that._name) && _nodeId.equals(that._nodeId) && _type == that._type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_name, _nodeId, _type.ordinal());
  }
}
