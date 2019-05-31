package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.or;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.symbolic.state.StateExpr;

public final class BDDReachabilityGraph {
  public static final class Builder {
    // preState -> postState -> transition function
    private final Table<StateExpr, StateExpr, Transition> _edges;

    public Builder() {
      _edges = HashBasedTable.create();
    }

    public Builder(Table<StateExpr, StateExpr, Transition> edges) {
      _edges = HashBasedTable.create(edges);
    }

    public void addEdge(Edge edge) {
      StateExpr preState = edge.getPreState();
      StateExpr postState = edge.getPostState();
      Transition transition = edge.getTransition();
      Transition oldTransition = _edges.put(preState, postState, transition);
      if (oldTransition != null) {
        _edges.put(preState, postState, or(oldTransition, edge.getTransition()));
      }
    }

    public @Nullable Transition replaceEdge(Edge edge) {
      return _edges.put(edge.getPreState(), edge.getPostState(), edge.getTransition());
    }
  }

  // preState -> postState -> transition function
  private final Table<StateExpr, StateExpr, Transition> _edges;

  public BDDReachabilityGraph(Table<StateExpr, StateExpr, Transition> edges) {
    _edges = ImmutableTable.copyOf(edges);
  }

  public static Builder builder() {
    return new Builder();
  }

  public Stream<Edge> getEdges() {
    return _edges.cellSet().stream()
        .map(cell -> new Edge(cell.getRowKey(), cell.getColumnKey(), cell.getValue()));
  }

  public Builder toBuilder() {
    return new Builder(_edges);
  }
}
