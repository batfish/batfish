package org.batfish.specifier;

import java.util.Objects;

/** Identifies the {@link Location} of the link of an interface in the network. */
public class InterfaceLinkLocation implements Location {
  private final String _interfaceName;

  private final String _nodeName;

  public InterfaceLinkLocation(String nodeName, String interfaceName) {
    _nodeName = nodeName;
    _interfaceName = interfaceName;
  }

  @Override
  public <T> T accept(LocationVisitor<T> visitor) {
    return visitor.visitInterfaceLinkLocation(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InterfaceLinkLocation that = (InterfaceLinkLocation) o;
    return Objects.equals(_interfaceName, that._interfaceName)
        && Objects.equals(_nodeName, that._nodeName);
  }

  public String getInterfaceName() {
    return _interfaceName;
  }

  public String getNodeName() {
    return _nodeName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_interfaceName, _nodeName);
  }
}
