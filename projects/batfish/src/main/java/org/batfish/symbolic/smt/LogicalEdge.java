package org.batfish.symbolic.smt;

import java.util.Objects;
import org.batfish.symbolic.GraphEdge;

/**
 * A logical edge in the network graph. Wraps a graph edge with additional information about the
 * type of the edge (Import/Export) as well as the record of symbolic variables.
 *
 * @author Ryan Beckett
 */
class LogicalEdge {

  private GraphEdge _edge;

  private EdgeType _type;

  private SymbolicRecord _symbolicRecord;

  LogicalEdge(GraphEdge edge, EdgeType type, SymbolicRecord symbolicRecord) {
    _edge = edge;
    _type = type;
    _symbolicRecord = symbolicRecord;
  }

  EdgeType getEdgeType() {
    return _type;
  }

  SymbolicRecord getSymbolicRecord() {
    return _symbolicRecord;
  }

  GraphEdge getEdge() {
    return _edge;
  }

  boolean isAbstract() {
    return _edge.isAbstract();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LogicalEdge)) {
      return false;
    }
    LogicalEdge other = (LogicalEdge) o;
    return _type == other._type
        && Objects.equals(_edge, other._edge)
        && Objects.equals(_symbolicRecord, other._symbolicRecord);
  }

  @Override
  public int hashCode() {
    int result = _edge != null ? _edge.hashCode() : 0;
    result = 31 * result + (_type != null ? (_type == EdgeType.EXPORT ? 2 : 1) : 0);
    result = 31 * result + (_symbolicRecord != null ? _symbolicRecord.hashCode() : 0);
    return result;
  }
}
