package org.batfish.symbolic.interpreter;

import java.util.Objects;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.smt.EdgeType;

public class EdgeTransformer {

  private GraphEdge _edge;

  private EdgeType _edgeType;

  private BDDTransferFunction _bgpTransfer;

  public EdgeTransformer(GraphEdge edge, EdgeType edgeType, BDDTransferFunction bgpTransfer) {
    this._edge = edge;
    this._edgeType = edgeType;
    this._bgpTransfer = bgpTransfer;
  }

  public GraphEdge getEdge() {
    return _edge;
  }

  public EdgeType getEdgeType() {
    return _edgeType;
  }

  public BDDTransferFunction getBgpTransfer() {
    return _bgpTransfer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EdgeTransformer that = (EdgeTransformer) o;
    return Objects.equals(_edge, that._edge)
        && _edgeType == that._edgeType
        && Objects.equals(_bgpTransfer, that._bgpTransfer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_edge, _edgeType, _bgpTransfer);
  }
}
