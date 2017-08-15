package org.batfish.smt;


import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.smt.collections.Table2;

import java.util.*;

/**
 * <p>A logical graph that wraps the network graph and adds
 * additional information about the routing protocols actually
 * using the underlying network graph.</p>
 *
 * @author Ryan Beckett
 */
class LogicalGraph {

    private Graph _graph;

    private Map<LogicalEdge, LogicalEdge> _otherEnd;

    private Table2<String, Protocol, List<ArrayList<LogicalEdge>>> _logicalEdges;

    private Table2<String, Protocol, Set<Protocol>> _redistributedProtocols;

    private Map<LogicalEdge, SymbolicRecord> _environmentVars;

    LogicalGraph(Graph g) {
        _graph = g;
        _logicalEdges = new Table2<>();
        _redistributedProtocols = new Table2<>();
        _otherEnd = new HashMap<>();
        _environmentVars = new HashMap<>();
    }

    /*
     * Find the variables for the opposite edge of a
     * logical edge.
     */
    SymbolicRecord findOtherVars(LogicalEdge e) {
        LogicalEdge other = _otherEnd.get(e);
        if (other != null) {
            return other.getSymbolicRecord();
        }
        return _environmentVars.get(e);
    }

    /*
     * Check if a logical edge is used for a particular protocol.
     */
    boolean isEdgeUsed(Configuration conf, Protocol proto, LogicalEdge e) {
        GraphEdge ge = e.getEdge();
        return _graph.isEdgeUsed(conf, proto, ge);
    }

    /*
     * Find the router Id for the neighbor corresponding to a logical edge.
     */
    Long findRouterId(LogicalEdge e, Protocol proto) {
        LogicalEdge eOther = _otherEnd.get(e);
        if (eOther != null) {
            String peer = eOther.getEdge().getRouter();
            Configuration peerConf = getGraph().getConfigurations().get(peer);
            return routerId(peerConf, proto);
        }

        if (proto.isConnected() || proto.isStatic()) {
            return 0L;
        }

        BgpNeighbor n = getGraph().findBgpNeighbor(e.getEdge());

        if (n != null && n.getAddress() != null) {
            return n.getAddress().asLong();
        }
        return null;
    }

    /*
     * Find the router Id for a router and a protocol.
     */
    private long routerId(Configuration conf, Protocol proto) {
        if (proto.isBgp()) {
            return conf.getDefaultVrf().getBgpProcess().getRouterId().asLong();
        }
        if (proto.isOspf()) {
            return conf.getDefaultVrf().getOspfProcess().getRouterId().asLong();
        } else {
            return 0;
        }
    }

    /*
     * Getters and setters
     */

    Graph getGraph() {
        return _graph;
    }

    Map<LogicalEdge, LogicalEdge> getOtherEnd() {
        return _otherEnd;
    }

    Map<LogicalEdge, SymbolicRecord> getEnvironmentVars() {
        return _environmentVars;
    }

    Table2<String, Protocol, List<ArrayList<LogicalEdge>>> getLogicalEdges() {
        return _logicalEdges;
    }

    Table2<String, Protocol, Set<Protocol>> getRedistributedProtocols() {
        return _redistributedProtocols;
    }

}
