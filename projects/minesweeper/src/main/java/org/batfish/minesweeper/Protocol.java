package org.batfish.minesweeper;

import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.RoutingProtocol;

/**
 * Class representing the routing protocols supported. The Protocol wraps the type enum in order to
 * allow us to define a deterministic hashcode() for testing purposes.
 *
 * @author Ryan Beckett
 */
public class Protocol {

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

  public static final Protocol BGP = new Protocol(Type.BGP);

  public static final Protocol OSPF = new Protocol(Type.OSPF);

  public static final Protocol STATIC = new Protocol(Type.STATIC);

  public static final Protocol CONNECTED = new Protocol(Type.CONNECTED);

  public static final Protocol BEST = new Protocol(Type.BEST);

  @Nullable
  public static Protocol fromRoutingProtocol(RoutingProtocol p) {
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

  public static RoutingProtocol toRoutingProtocol(Protocol p) {
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

  public boolean isBgp() {
    return _type == Type.BGP;
  }

  public boolean isOspf() {
    return _type == Type.OSPF;
  }

  public boolean isConnected() {
    return _type == Type.CONNECTED;
  }

  public boolean isStatic() {
    return _type == Type.STATIC;
  }

  public boolean isBest() {
    return _type == Type.BEST;
  }

  public String name() {
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
