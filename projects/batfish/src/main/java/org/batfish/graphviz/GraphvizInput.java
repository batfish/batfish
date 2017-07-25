package org.batfish.graphviz;

public abstract class GraphvizInput {

  protected final String _name;

  public GraphvizInput(String name) {
    _name = name;
  }

  @Override
  public abstract String toString();
}
