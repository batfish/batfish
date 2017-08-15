package org.batfish.smt;


import com.microsoft.z3.ArithExpr;
import org.batfish.smt.collections.Table2;

import java.util.HashMap;
import java.util.Map;


/**
 * <p>A collection of symbolic variables representing
 * the possible link failures in the network.</p>
 *
 * @author Ryan Beckett
 */
class SymbolicFailures {

    private Table2<String, String, ArithExpr> _failedInternalLinks;

    private Map<GraphEdge, ArithExpr> _failedEdgeLinks;

    SymbolicFailures() {
        _failedInternalLinks = new Table2<>();
        _failedEdgeLinks = new HashMap<>();
    }

    Table2<String, String, ArithExpr> getFailedInternalLinks() {
        return _failedInternalLinks;
    }

    Map<GraphEdge, ArithExpr> getFailedEdgeLinks() {
        return _failedEdgeLinks;
    }

    ArithExpr getFailedVariable(GraphEdge ge) {
        if (ge.getPeer() == null) {
            return _failedEdgeLinks.get(ge);
        }
        String router = ge.getRouter();
        String peer = ge.getPeer();
        return _failedInternalLinks.get(router, peer);
    }

}
