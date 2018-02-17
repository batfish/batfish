package org.batfish.z3.state;

import java.util.Objects;

public class StateParameter {

  public enum Type {
    ACL,
    ACL_LINE,
    INTERFACE,
    NODE,
    QUERY_NUMBER,
    VRF
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

  public String getId() {
    return _id;
  }

  public Type getType() {
    return _type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, _type);
  }
}
