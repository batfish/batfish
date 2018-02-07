package org.batfish.z3.state;

public class StateParameter {

  enum Type {
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

  public String getId() {
    return _id;
  }

  public Type getType() {
    return _type;
  }
}
