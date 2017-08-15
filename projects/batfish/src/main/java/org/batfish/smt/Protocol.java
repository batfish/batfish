package org.batfish.smt;


import org.batfish.common.BatfishException;
import org.batfish.datamodel.RoutingProtocol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>Class representing the routing protocols supported.
 * The Protocol wraps the type enum in order to allow
 * us to define a deterministic hashcode() for testing purposes.</p>
 *
 * @author Ryan Beckett
 */
class Protocol {

    enum Type {
        BEST, OSPF, BGP, CONNECTED, STATIC
    }

    private Type _type;

    private Protocol(Type t) {
        _type = t;
    }

    final static Protocol BGP = new Protocol(Type.BGP);

    final static Protocol OSPF = new Protocol(Type.OSPF);

    final static Protocol STATIC = new Protocol(Type.STATIC);

    final static Protocol CONNECTED = new Protocol(Type.CONNECTED);

    final static Protocol BEST = new Protocol(Type.BEST);

    static final List<Protocol.Type> values =
            Collections.unmodifiableList(Arrays.asList(Type.BEST, Type.OSPF, Type.BGP, Type.CONNECTED, Type.STATIC));

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
        switch(p._type) {
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Protocol protocol = (Protocol) o;

        return _type == protocol._type;
    }

    @Override
    public int hashCode() {
        return _type != null ? _type.ordinal() : 0;
    }
}
