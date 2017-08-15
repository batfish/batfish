package org.batfish.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.datamodel.Interface;

/**
 * <p>An edge in the network graph. Contains the router and interface
 * as well as the peer router and interface. Each edge can also be
 * abstract, as in the case of iBGP, or concrete where it corresponds
 * to a particular edge in the topology.</p>
 *
 * @author Ryan Beckett
 */
public class GraphEdge {

    private static final String START_VAR = "start";

    private static final String END_VAR = "end";

    private static final String ROUTER_VAR = "router";

    private static final String PEER_VAR = "peer";

    private static final String ABSTRACT_VAR = "isAbstract";

    private Interface _start;

    private Interface _end;

    private String _router;

    private String _peer;

    private boolean _isAbstract;

    @JsonCreator
    public GraphEdge(
            @JsonProperty(START_VAR) Interface start, @JsonProperty(END_VAR) Interface end,
            @JsonProperty(ROUTER_VAR) String router, @JsonProperty(PEER_VAR) String peer,
            @JsonProperty(ABSTRACT_VAR) boolean isAbstract) {
        _start = start;
        _end = end;
        _router = router;
        _peer = peer;
        _isAbstract = isAbstract;
    }

    @JsonProperty(START_VAR)
    public Interface getStart() {
        return _start;
    }

    @JsonProperty(END_VAR)
    public Interface getEnd() {
        return _end;
    }

    @JsonProperty(ROUTER_VAR)
    public String getRouter() {
        return _router;
    }

    @JsonProperty(PEER_VAR)
    public String getPeer() {
        return _peer;
    }

    @JsonProperty(ABSTRACT_VAR)
    public boolean isAbstract() {
        return _isAbstract;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GraphEdge graphEdge = (GraphEdge) o;

        if (_isAbstract != graphEdge._isAbstract)
            return false;
        if (_start != null ? !_start.equals(graphEdge._start) : graphEdge._start != null)
            return false;
        if (_end != null ? !_end.equals(graphEdge._end) : graphEdge._end != null)
            return false;
        if (_router != null ? !_router.equals(graphEdge._router) : graphEdge._router != null)
            return false;
        return _peer != null ? _peer.equals(graphEdge._peer) : graphEdge._peer == null;
    }

    @Override
    public int hashCode() {
        int result = _start != null ? _start.getName().hashCode() : 0;
        result = 31 * result + (_end != null ? _end.getName().hashCode() : 0);
        result = 31 * result + (_router != null ? _router.hashCode() : 0);
        result = 31 * result + (_peer != null ? _peer.hashCode() : 0);
        result = 31 * result + (_isAbstract ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return _router + "," + _start.getName() + " --> " + (_peer == null ? "_" : _peer) + "," +
                (_end == null ? "_" : _end.getName());
    }

}
