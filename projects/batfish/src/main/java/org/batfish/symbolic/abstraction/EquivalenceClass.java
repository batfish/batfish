package org.batfish.symbolic.abstraction;

import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.symbolic.Graph;

public class EquivalenceClass {

  private Graph _graph;

  private HeaderSpace _headerSpace;

  private Map<String, String> _abstraction;

  public EquivalenceClass(
      HeaderSpace headerSpace, Graph graph, @Nullable Map<String, String> abstraction) {
    this._headerSpace = headerSpace;
    this._graph = graph;
    this._abstraction = abstraction;
  }

  public Graph getGraph() {
    return _graph;
  }

  public HeaderSpace getHeaderSpace() {
    return _headerSpace;
  }

  public Map<String, String> getAbstraction() {
    return _abstraction;
  }
}
