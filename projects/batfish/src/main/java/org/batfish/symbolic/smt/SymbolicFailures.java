package org.batfish.symbolic.smt;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.collections.Table2;

/**
 * A collection of symbolic routeVariables representing the possible link failures in the network.
 *
 * @author Ryan Beckett
 */
class SymbolicFailures {

  private ArithExpr _zero;

  private Table2<String, String, ArithExpr> _failedInternalLinks;

  private Map<GraphEdge, ArithExpr> _failedEdgeLinks;

  SymbolicFailures(Context ctx) {
    _zero = ctx.mkInt(0);
    _failedInternalLinks = new Table2<>();
    _failedEdgeLinks = new HashMap<>();
  }

  Table2<String, String, ArithExpr> getFailedInternalLinks() {
    return _failedInternalLinks;
  }

  Map<GraphEdge, ArithExpr> getFailedEdgeLinks() {
    return _failedEdgeLinks;
  }

  @Nullable
  ArithExpr getFailedVariable(GraphEdge ge) {
    if (ge.isAbstract()) {
      return _zero;
    }
    if (ge.getPeer() == null) {
      return _failedEdgeLinks.get(ge);
    }
    String router = ge.getRouter();
    String peer = ge.getPeer();
    return _failedInternalLinks.get(router, peer);
  }
}
