package org.batfish.symbolic.ainterpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.collections.Table3;

public class AbstractState<T> {

  private Map<String, AbstractRib<T>> _perRouterRoutes;

  private Table3<String, String, GraphEdge, T> _perNeighborRoutes;

  private Map<String, T> _nonDynamicRoutes;

  public AbstractState(
      Map<String, AbstractRib<T>> perRouter,
      Table3<String, String, GraphEdge, T> perNeighbor,
      Map<String, T> nonDynamic) {
    this._perRouterRoutes = perRouter;
    this._perNeighborRoutes = new Table3<>();
    this._nonDynamicRoutes = nonDynamic;
  }

  public AbstractState(AbstractState<T> other) {
    this._perRouterRoutes = new HashMap<>(other._perRouterRoutes);
    this._nonDynamicRoutes = new HashMap<>(other._nonDynamicRoutes);
    this._perNeighborRoutes = new Table3<>();
    other._perNeighborRoutes.forEach(
        (k1, k2, k3, v) -> {
          _perNeighborRoutes.put(k1, k2, k3, v);
        });
  }

  public Map<String, AbstractRib<T>> getPerRouterRoutes() {
    return _perRouterRoutes;
  }

  public Table3<String, String, GraphEdge, T> getPerNeighborRoutes() {
    return _perNeighborRoutes;
  }

  public Map<String, T> getNonDynamicRoutes() {
    return _nonDynamicRoutes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractState<?> that = (AbstractState<?>) o;
    return Objects.equals(_perRouterRoutes, that._perRouterRoutes)
        && Objects.equals(_perNeighborRoutes, that._perNeighborRoutes)
        && Objects.equals(_nonDynamicRoutes, that._nonDynamicRoutes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_perRouterRoutes, _perNeighborRoutes, _nonDynamicRoutes);
  }
}
