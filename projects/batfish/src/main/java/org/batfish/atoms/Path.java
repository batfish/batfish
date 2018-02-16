package org.batfish.atoms;

import java.util.Iterator;
import javax.annotation.Nonnull;
import org.batfish.symbolic.collections.PList;

/*
 * A path through the <tt>NetworkModel</tt>. Generally, a path
 * is a collection of nodes represented by a list of links
 * between nodes. The link representation is useful to find the
 * interfaces used between nodes. However, to represent the path
 * with only a single node we also store the path endpoints.
 *
 * <p>In order allow efficient reuse of paths in the <tt>NetworkModel</tt>,
 * the list of links is represented using a persistent list.</p>
 */
public class Path {

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
}
