package org.batfish.symbolic.cinterpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.batfish.datamodel.Prefix;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.collections.Table3;

public class ConcreteState<T> {

  private Map<String, ConcreteRib<T>> _perRouterRoutes;

  private Table3<String, String, GraphEdge, T> _perNeighborRoutes;

  public ConcreteState(
      Map<String, ConcreteRib<T>> perRouter,
      Table3<String, String, GraphEdge, Map<Prefix, T>> perNeighbor) {
    this._perRouterRoutes = perRouter;
    this._perNeighborRoutes = new Table3<>();
  }

  public ConcreteState(ConcreteState<T> other) {
    this._perRouterRoutes = new HashMap<>(other._perRouterRoutes);
    this._perNeighborRoutes = new Table3<>();
    other._perNeighborRoutes.forEach(
        (k1, k2, k3, v) -> {
          _perNeighborRoutes.put(k1, k2, k3, v);
        });
  }

  public Map<String, ConcreteRib<T>> getPerRouterRoutes() {
    return _perRouterRoutes;
  }

  public Table3<String, String, GraphEdge, T> getPerNeighborRoutes() {
    return _perNeighborRoutes;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConcreteState<?> that = (ConcreteState<?>) o;
    return Objects.equals(_perRouterRoutes, that._perRouterRoutes)
        && Objects.equals(_perNeighborRoutes, that._perNeighborRoutes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_perRouterRoutes, _perNeighborRoutes);
  }
}
