package org.batfish.representation.palo_alto;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

/** PAN datamodel component containing an address specifier for an interface. */
public final class InterfaceAddress implements Serializable {

  public enum Type {
    IP_ADDRESS,
    IP_PREFIX,
    REFERENCE
  }

  private final Type _type;

  private final String _value;

  public InterfaceAddress(Type type, String value) {
    _type = type;
    _value = value;
  }

  /** Type hint from lexer */
  public Type getType() {
    return _type;
  }

  public String getValue() {
    return _value;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("type", _type)
        .add("value", _value)
        .toString();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InterfaceAddress)) {
      return false;
    }
    InterfaceAddress o = (InterfaceAddress) obj;
    return _type == o._type && _value.equals(o._value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type.ordinal(), _value);
  }
}
