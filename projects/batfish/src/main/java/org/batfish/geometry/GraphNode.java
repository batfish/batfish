package org.batfish.geometry;

public class GraphNode {

  private String _name;

  public GraphNode(String name) {
    this._name = name;
  }

  public String getName() {
    return _name;
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

    return _name != null ? _name.equals(graphNode._name) : graphNode._name == null;
  }

  @Override
  public int hashCode() {
    return _name != null ? _name.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "GraphNode{" + "_name='" + _name + '\'' + '}';
  }
}
