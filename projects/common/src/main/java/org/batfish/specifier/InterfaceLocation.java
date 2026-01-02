package org.batfish.specifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
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

  @Override
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
    return _interfaceName.equals(that._interfaceName) && _nodeName.equals(that._nodeName);
  }

  @Override
  public int hashCode() {
    return 31 * 31 * getClass().hashCode() + 31 * _interfaceName.hashCode() + _nodeName.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_NODE_NAME, _nodeName)
        .add(PROP_INTERFACE_NAME, _interfaceName)
        .toString();
  }
}
