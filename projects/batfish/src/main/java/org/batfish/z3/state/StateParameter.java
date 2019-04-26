package org.batfish.z3.state;

import java.util.Objects;

public class StateParameter {

  public enum Type {
    INTERFACE,
    NODE,
    VRF,
  }

  private final String _id;

  private final Type _type;

  public StateParameter(String id, Type type) {
    _id = id;
    _type = type;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof StateParameter)) {
      return false;
    }

    StateParameter other = (StateParameter) obj;
    return Objects.equals(_id, other._id) && Objects.equals(_type, other._type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, _type);
  }
}
