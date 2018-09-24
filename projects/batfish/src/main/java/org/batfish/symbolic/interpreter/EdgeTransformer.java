package org.batfish.symbolic.interpreter;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.smt.EdgeType;

public class EdgeTransformer {

  private GraphEdge _edge;

  private EdgeType _edgeType;

  private RoutingProtocol _protocol;

  @Nullable private Integer _cost;

  @Nullable private Ip _nextHopIp;

  public EdgeTransformer(
      GraphEdge edge,
      EdgeType edgeType,
      RoutingProtocol proto,
      @Nullable Integer cost,
      @Nullable Ip nextHopIp) {
    this._edge = edge;
    this._edgeType = edgeType;
    this._protocol = proto;
    this._cost = cost;
    this._nextHopIp = nextHopIp;
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
  public Integer getCost() {
    return _cost;
  }

  @Nullable
  public Ip getNextHopIp() {
    return _nextHopIp;
  }
}