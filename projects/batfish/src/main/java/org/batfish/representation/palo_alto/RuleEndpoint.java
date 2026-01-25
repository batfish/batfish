package org.batfish.representation.palo_alto;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;

/** PAN datamodel component containing the source or destination of a rule */
public final class RuleEndpoint implements Serializable {

  public enum Type {
    Any,
    IP_ADDRESS,
    IP_PREFIX,
    IP_RANGE,
    REFERENCE,
    FQDN,
    IP_LOCATION
  }

  private final Type _type;

  private final String _value;

  public RuleEndpoint(Type type, String value) {
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
    if (!(obj instanceof RuleEndpoint)) {
      return false;
    }
    RuleEndpoint o = (RuleEndpoint) obj;
    return _type == o._type && Objects.equals(_value, o._value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type.ordinal(), _value);
  }
}
