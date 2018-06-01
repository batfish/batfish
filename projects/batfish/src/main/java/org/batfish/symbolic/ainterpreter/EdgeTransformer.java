package org.batfish.symbolic.ainterpreter;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.smt.EdgeType;

public class EdgeTransformer {

  private GraphEdge _edge;

  private EdgeType _edgeType;

  private RoutingProtocol _protocol;

  @Nullable private BDDTransferFunction _bddTransfer;

  @Nullable private Integer _cost;

  @Nullable private Ip _nextHopIp;

  @Nullable private BDDAcl _bddAcl;

  public EdgeTransformer(
      GraphEdge edge,
      EdgeType edgeType,
      RoutingProtocol proto,
      @Nullable BDDTransferFunction bddTransfer,
      @Nullable Integer cost,
      @Nullable Ip nextHopIp,
      @Nullable BDDAcl acl) {
    this._edge = edge;
    this._edgeType = edgeType;
    this._protocol = proto;
    this._bddTransfer = bddTransfer;
    this._cost = cost;
    this._nextHopIp = nextHopIp;
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

  @Nullable
  public BDDTransferFunction getBddTransfer() {
    return _bddTransfer;
  }

  @Nullable
  public Integer getCost() {
    return _cost;
  }

  @Nullable
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Nullable
  public BDDAcl getBddAcl() {
    return _bddAcl;
  }
}
