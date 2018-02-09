package org.batfish.geometry;

public class GraphNode {

  private String _name;

  private int _index;

  public GraphNode(String name, int index) {
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

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GraphNode graphNode = (GraphNode) o;

    return _index == graphNode._index;
  }

  @Override public int hashCode() {
    return _index;
  }

  @Override
  public String toString() {
    return "GraphNode{" + "_name='" + _name + '\'' + '}';
  }
}
