package org.batfish.atoms;

/*
 * A graph node for the network model. Rather than using the
 * node name directly, this class associates a unique index with
 * each graph node starting from 0. For this reason, we can use
 * more efficient data structures like arrays instead of maps.
 */
public class GraphNode {

  private String _name;

  private int _index;

  GraphNode(String name, int index) {
    this._name = name;
    this._index = index;
  }

  public String getName() {
    return _name;
  }

  public int getIndex() {
    return _index;
  }

  public boolean isDropNode() {
    return _name.equals("(none)");
  }

  public GraphNode owner() {
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GraphNode graphNode = (GraphNode) o;

    return _index == graphNode._index;
  }

  @Override
  public int hashCode() {
    return _index;
  }

  @Override
  public String toString() {
    return "Node[" + _name + ']';
  }
}
