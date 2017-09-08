package org.batfish.smt;

import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.RoutingProtocol;

/**
 * Class representing the routing protocols supported. The Protocol wraps the type enum in order to
 * allow us to define a deterministic hashcode() for testing purposes.
 *
 * @author Ryan Beckett
 */
class Protocol {

  enum Type {
    BEST,
    OSPF,
    BGP,
    CONNECTED,
    STATIC
  }

  private Type _type;

  private Protocol(Type t) {
    _type = t;
  }

  static final Protocol BGP = new Protocol(Type.BGP);

  static final Protocol OSPF = new Protocol(Type.OSPF);

  static final Protocol STATIC = new Protocol(Type.STATIC);

  static final Protocol CONNECTED = new Protocol(Type.CONNECTED);

  static final Protocol BEST = new Protocol(Type.BEST);

  @Nullable
  static Protocol fromRoutingProtocol(RoutingProtocol p) {
    switch (p) {
      case CONNECTED:
        return Protocol.CONNECTED;
      case STATIC:
        return Protocol.STATIC;
      case BGP:
        return Protocol.BGP;
      case OSPF:
        return Protocol.OSPF;
      default:
        return null;
    }
  }

  static RoutingProtocol toRoutingProtocol(Protocol p) {
    switch (p._type) {
      case BGP:
        return RoutingProtocol.BGP;
      case OSPF:
        return RoutingProtocol.OSPF;
      case CONNECTED:
        return RoutingProtocol.CONNECTED;
      case STATIC:
        return RoutingProtocol.STATIC;
      default:
        throw new BatfishException("Error[toRoutingProtocol]: " + p.name());
    }
  }

  boolean isBgp() {
    return _type == Type.BGP;
  }

  boolean isOspf() {
    return _type == Type.OSPF;
  }

  boolean isConnected() {
    return _type == Type.CONNECTED;
  }

  boolean isStatic() {
    return _type == Type.STATIC;
  }

  boolean isBest() {
    return _type == Type.BEST;
  }

  String name() {
    return _type.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Protocol protocol = (Protocol) o;

    return _type == protocol._type;
  }

  @Override
  public int hashCode() {
    return _type != null ? _type.ordinal() : 0;
  }
}
