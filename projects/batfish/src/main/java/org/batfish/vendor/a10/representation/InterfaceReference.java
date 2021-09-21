package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Combination of Interface {@link Interface.Type} and an Interface's number. Uniquely identifies an
 * interface.
 */
public class InterfaceReference implements Serializable {
  public Interface.Type getType() {
    return _type;
  }

  public int getNumber() {
    return _number;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof InterfaceReference)) {
      return false;
    }
    InterfaceReference that = (InterfaceReference) o;
    return _number == that._number && Objects.equals(_type, that._type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _number);
  }

  public InterfaceReference(Interface.Type type, int number) {
    _type = type;
    _number = number;
  }

  @Nonnull private final Interface.Type _type;
  private final int _number;
}
