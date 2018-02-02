package org.batfish.symbolic.abstraction;

import javax.annotation.Nullable;
import org.batfish.symbolic.Graph;

public class Abstraction {

  private Graph _graph;

  private AbstractionMap _abstractionMap;

  public Abstraction(Graph graph, @Nullable AbstractionMap abstractionMap) {
    this._graph = graph;
    this._abstractionMap = abstractionMap;
  }

  public Graph getGraph() {
    return _graph;
  }

  public AbstractionMap getAbstractionMap() {
    return _abstractionMap;
  }
}
