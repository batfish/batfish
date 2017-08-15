package org.batfish.smt;


/**
 * <p>A logical edge in the network graph. Wraps a graph edge
 * with additional information about the type of the edge
 * (Import/Export) as well as the record of symbolic variables.</p>
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        LogicalEdge that = (LogicalEdge) o;

        if (_edge != null ? !_edge.equals(that._edge) : that._edge != null)
            return false;
        if (_type != that._type)
            return false;
        return _symbolicRecord != null ? _symbolicRecord.equals(that._symbolicRecord) : that
                ._symbolicRecord == null;
    }

    @Override
    public int hashCode() {
        int result = _edge != null ? _edge.hashCode() : 0;
        result = 31 * result + (_type != null ? (_type == EdgeType.EXPORT ? 2 : 1) : 0);
        result = 31 * result + (_symbolicRecord != null ? _symbolicRecord.hashCode() : 0);
        return result;
    }
}

