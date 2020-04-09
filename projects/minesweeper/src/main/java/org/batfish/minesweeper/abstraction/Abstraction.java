package org.batfish.minesweeper.abstraction;

import javax.annotation.Nullable;
import org.batfish.minesweeper.Graph;

public class Abstraction {

  private Graph _graph;

  private AbstractionMap _abstractionMap;

  public Abstraction(Graph graph, @Nullable AbstractionMap abstractionMap) {
    _graph = graph;
    _abstractionMap = abstractionMap;
  }

  public Graph getGraph() {
    return _graph;
  }

  public AbstractionMap getAbstractionMap() {
    return _abstractionMap;
  }
}
