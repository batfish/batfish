package org.batfish.atoms;

import java.util.Iterator;
import javax.annotation.Nonnull;
import org.batfish.symbolic.collections.PList;

public class Path implements Iterable<GraphLink> {

  private GraphNode _source;

  private GraphNode _target;

  private PList<GraphLink> _links;

  public Path(PList<GraphLink> elements, GraphNode source, GraphNode target) {
    this._links = elements;
    this._source = source;
    this._target = target;
  }

  public boolean containsNode(GraphNode node) {
    if (node.equals(_source)) {
      return true;
    }
    if (node.equals(_target)) {
      return true;
    }
    for (GraphLink link : _links) {
      if (node.equals(link.getSource())) {
        return true;
      }
      if (node.equals(link.getTarget())) {
        return true;
      }
    }
    return false;
  }

  public GraphNode getSource() {
    return _source;
  }

  public GraphNode getDestination() {
    return _target;
  }

  public PList<GraphLink> getLinks() {
    return _links;
  }

  @Nonnull
  @Override
  public Iterator<GraphLink> iterator() {
    return _links.iterator();
  }
}
