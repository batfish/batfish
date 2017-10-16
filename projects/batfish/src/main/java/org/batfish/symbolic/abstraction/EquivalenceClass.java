package org.batfish.symbolic.abstraction;

import java.util.List;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.Graph;

public class EquivalenceClass {

  private Graph _graph;

  private List<Prefix> _slice;

  public EquivalenceClass(List<Prefix> slice, Graph graph) {
    this._slice = slice;
    this._graph = graph;
  }

  public Graph getGraph() {
    return _graph;
  }

  public List<Prefix> getSlice() {
    return _slice;
  }
}
