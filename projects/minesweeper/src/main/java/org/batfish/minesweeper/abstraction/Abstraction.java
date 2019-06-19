package org.batfish.minesweeper.abstraction;

import javax.annotation.Nullable;
import javax.validation.constraints.Null;

import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.GraphEdge;

import java.util.Map;

public class Abstraction {

  private Graph _graph;

  private AbstractionMap _abstractionMap;

  private Map<GraphEdge, Integer> _multiplicities;

  public Abstraction(Graph graph, @Nullable AbstractionMap abstractionMap, Map<GraphEdge, Integer> mult) {
    this._graph = graph;
    this._abstractionMap = abstractionMap;
    this._multiplicities = mult;
  }

  public Graph getGraph() {
    return _graph;
  }

  public AbstractionMap getAbstractionMap() {
    return _abstractionMap;
  }

  public Map<GraphEdge, Integer>  getMultiplicityMap() { return _multiplicities;}
}
