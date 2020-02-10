package org.batfish.minesweeper.abstraction;

import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.minesweeper.Graph;

public class EquivalenceClass {

  private Graph _graph;

  private HeaderSpace _headerSpace;

  private AbstractionMap _abstraction;

  public EquivalenceClass(
      HeaderSpace headerSpace, Graph graph, @Nullable AbstractionMap abstraction) {
    _headerSpace = headerSpace;
    _graph = graph;
    _abstraction = abstraction;
  }

  public Graph getGraph() {
    return _graph;
  }

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  public AbstractionMap getAbstraction() {
    return _abstraction;
  }
}
