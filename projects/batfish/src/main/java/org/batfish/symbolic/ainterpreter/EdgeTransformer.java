package org.batfish.symbolic.ainterpreter;

import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.smt.EdgeType;

public class EdgeTransformer {

  private GraphEdge _edge;

  private EdgeType _edgeType;

  private RoutingProtocol _protocol;

  private BDDTransferFunction _bddTransfer;

  public EdgeTransformer(
      GraphEdge edge, EdgeType edgeType, RoutingProtocol proto, BDDTransferFunction bgpTransfer) {
    this._edge = edge;
    this._edgeType = edgeType;
    this._protocol = proto;
    this._bddTransfer = bgpTransfer;
  }

  public GraphEdge getEdge() {
    return _edge;
  }

  public EdgeType getEdgeType() {
    return _edgeType;
  }

  public RoutingProtocol getProtocol() {
    return _protocol;
  }

  public BDDTransferFunction getBddTransfer() {
    return _bddTransfer;
  }
}
