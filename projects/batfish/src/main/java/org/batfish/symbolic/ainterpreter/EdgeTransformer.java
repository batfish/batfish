package org.batfish.symbolic.ainterpreter;

import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.smt.EdgeType;

public class EdgeTransformer {

  private GraphEdge _edge;

  private EdgeType _edgeType;

  private Protocol _protocol;

  private BDDTransferFunction _bddTransfer;

  public EdgeTransformer(
      GraphEdge edge, EdgeType edgeType, Protocol proto, BDDTransferFunction bgpTransfer) {
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

  public Protocol getProtocol() {
    return _protocol;
  }

  public BDDTransferFunction getBddTransfer() {
    return _bddTransfer;
  }
}
