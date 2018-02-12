package org.batfish.geometry;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

public class Path implements Iterable<GraphLink> {

  private GraphNode _source;

  private GraphNode _target;

  private List<GraphLink> _links;

  public Path(List<GraphLink> elements, GraphNode source, GraphNode target) {
    this._links = elements;
    this._source = source;
    this._target = target;
    Collections.reverse(_links);
  }

  public GraphNode getSource() {
    return _source;
  }

  public GraphNode getDestination() {
    return _target;
  }

  public List<GraphLink> getLinks() {
    return _links;
  }

  @Nonnull
  @Override
  public Iterator<GraphLink> iterator() {
    return _links.iterator();
  }
}
