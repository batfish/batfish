package org.batfish.datamodel.collections;

import java.util.Collection;
import java.util.TreeSet;
import org.batfish.datamodel.Edge;

public class EdgeSet extends TreeSet<Edge> {

  private static final long serialVersionUID = 1L;

  public EdgeSet() {}

  public EdgeSet(Collection<Edge> edges) {
    super(edges);
  }
}
