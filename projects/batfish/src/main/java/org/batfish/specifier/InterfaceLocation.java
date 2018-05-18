package org.batfish.specifier;

import java.util.Objects;
import javax.annotation.Nonnull;

public class InterfaceLocation implements Location {
  private final @Nonnull String _interfaceName;

  private final @Nonnull String _nodeName;

  public InterfaceLocation(@Nonnull String nodeName, @Nonnull String interfaceName) {
    _nodeName = nodeName;
    _interfaceName = interfaceName;
  }

  public @Nonnull String getInterfaceName() {
    return _interfaceName;
  }

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
    if (o == null || getClass() != o.getClass()) {
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
}
