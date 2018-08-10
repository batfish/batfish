package org.batfish.specifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Identifies the {@link Location} of an interface in the network. */
public final class InterfaceLocation implements Location {
  private static final String PROP_INTERFACE_NAME = "interfaceName";

  private static final String PROP_NODE_NAME = "nodeName";

  private final @Nonnull String _interfaceName;

  private final @Nonnull String _nodeName;

  @JsonCreator
  public InterfaceLocation(
      @JsonProperty(PROP_NODE_NAME) @Nonnull String nodeName,
      @JsonProperty(PROP_INTERFACE_NAME) @Nonnull String interfaceName) {
    _nodeName = nodeName;
    _interfaceName = interfaceName;
  }

  @JsonProperty(PROP_INTERFACE_NAME)
  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

  @JsonProperty(PROP_NODE_NAME)
  public @Nonnull String getNodeName() {
    return _nodeName;
  }

  @Override
  public <T> T accept(LocationVisitor<T> visitor) {
    return visitor.visitInterfaceLocation(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceLocation)) {
      return false;
    }
    InterfaceLocation that = (InterfaceLocation) o;
    return Objects.equals(_interfaceName, that._interfaceName)
        && Objects.equals(_nodeName, that._nodeName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_interfaceName, _nodeName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_INTERFACE_NAME, _interfaceName)
        .add(PROP_NODE_NAME, _nodeName)
        .toString();
  }
}
