package org.batfish.symbolic.ainterpreter;

import javax.annotation.Nullable;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.smt.EdgeType;

public class EdgeTransformer {

  private GraphEdge _edge;

  private EdgeType _edgeType;

  private RoutingProtocol _protocol;

  private BDDTransferFunction _bddTransfer;

  @Nullable private BDDAcl _bddAcl;

  public EdgeTransformer(
      GraphEdge edge,
      EdgeType edgeType,
      RoutingProtocol proto,
      BDDTransferFunction bgpTransfer,
      @Nullable BDDAcl acl) {
    this._edge = edge;
    this._edgeType = edgeType;
    this._protocol = proto;
    this._bddTransfer = bgpTransfer;
    this._bddAcl = acl;
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

  @Nullable
  public BDDAcl getBddAcl() {
    return _bddAcl;
  }
}
