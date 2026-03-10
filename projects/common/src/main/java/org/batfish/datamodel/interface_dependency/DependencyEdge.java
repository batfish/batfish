package org.batfish.datamodel.interface_dependency;

import org.batfish.datamodel.Interface;
import org.jgrapht.graph.DefaultEdge;

final class DependencyEdge extends DefaultEdge {
  private final Interface.DependencyType _dependencyType;

  DependencyEdge(Interface.DependencyType dependencyType) {
    _dependencyType = dependencyType;
  }

  public Interface.DependencyType getDependencyType() {
    return _dependencyType;
  }
}
