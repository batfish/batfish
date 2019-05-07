package org.batfish.minesweeper.smt;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.Context;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.batfish.minesweeper.GraphEdge;
import org.batfish.minesweeper.collections.Table2;

/**
 * A collection of symbolic variables representing the possible link failures in the network.
 *
 * @author Ryan Beckett
 */
class SymbolicFailures {

  private ArithExpr _zero;

  private Table2<String, String, ArithExpr> _failedInternalLinks;

  private Map<GraphEdge, ArithExpr> _failedEdgeLinks;

  private Map<String, ArithExpr> _failedNodes;

  SymbolicFailures(Context ctx) {
    _zero = ctx.mkInt(0);
    _failedInternalLinks = new Table2<>();
    _failedEdgeLinks = new HashMap<>();
    _failedNodes = new HashMap<>();
  }

  Table2<String, String, ArithExpr> getFailedInternalLinks() {
    return _failedInternalLinks;
  }

  Map<GraphEdge, ArithExpr> getFailedEdgeLinks() {
    return _failedEdgeLinks;
  }

  Map<String, ArithExpr> getFailedNodes() {
    return _failedNodes;
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

  ArithExpr getFailedStartVariable(GraphEdge ge) {
    String router = ge.getRouter();
    return _failedNodes.get(router);
  }

  Optional<ArithExpr> getFailedPeerVariable(GraphEdge ge) {
    if (ge.getPeer() == null) {
      return Optional.empty();
    }
    String router = ge.getPeer();
    return Optional.of(_failedNodes.get(router));
  }
}
