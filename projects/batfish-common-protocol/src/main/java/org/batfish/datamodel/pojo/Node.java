package org.batfish.datamodel.pojo;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.DeviceType;

public class Node extends BfObject {
  private static final String PROP_NAME = "name";
  private static final String PROP_TYPE = "type";

  @Nonnull private final String _name;

  @Nullable private DeviceType _type;

  @VisibleForTesting
  Node(@Nonnull String name, @Nullable String id, @Nullable DeviceType type) {
    super(firstNonNull(id, makeId(name.toLowerCase())));
    _name = name.toLowerCase();
    _type = type;
  }

  @JsonCreator
  private static Node jsonCreator(
      @Nullable @JsonProperty(PROP_NAME) String name,
      @Nullable @JsonProperty(PROP_ID) String id,
      @Nullable @JsonProperty(PROP_TYPE) DeviceType type) {
    checkArgument(name != null, "Missing: %s", PROP_NAME);
    return new Node(name, id, type);
  }

  public Node(String name) {
    this(name, null, null);
  }

  public Node(String name, DeviceType type) {
    this(name, null, type);
  }

  public static String makeId(String name) {
    return "node-" + name.toLowerCase();
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
