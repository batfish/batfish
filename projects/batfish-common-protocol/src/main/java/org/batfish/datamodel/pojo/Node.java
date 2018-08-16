package org.batfish.datamodel.pojo;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.DeviceType;

public class Node extends BfObject {

  private static final String PROP_NAME = "name";
  private static final String PROP_TYPE = "type";

  @Nonnull private final String _name;

  @Nullable private DeviceType _type;

  @JsonCreator
  public Node(
      @JsonProperty(PROP_NAME) String name,
      @JsonProperty(PROP_ID) String id,
      @JsonProperty(PROP_TYPE) DeviceType type) {
    super(firstNonNull(id, makeId(name)));
    _name = name;
    _type = type;
    if (name == null) {
      throw new IllegalArgumentException("Cannot build Node: name is null");
    }
  }

  public Node(String name) {
    this(name, makeId(name), null);
  }

  public Node(String name, DeviceType type) {
    this(name, makeId(name), type);
  }

  public static String makeId(String name) {
    return "node-" + name;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }

  @JsonProperty(PROP_TYPE)
  public DeviceType getType() {
    return _type;
  }
}
