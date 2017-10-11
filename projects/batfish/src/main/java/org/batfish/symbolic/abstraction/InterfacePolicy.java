package org.batfish.symbolic.abstraction;

import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.StaticRoute;

public class InterfacePolicy {

  private BDD _acl;

  private BDDRecord _bgpPolicy;

  private BDDRecord _ospfPolicy;

  private Integer _ospfCost;

  private SortedSet<StaticRoute> _staticRoutes;

  // TODO: route reflectors etc

  public InterfacePolicy(
      BDD acl,
      BDDRecord bgpPolicy,
      @Nullable BDDRecord ospfPolicy,
      @Nullable Integer ospfCost,
      @Nullable SortedSet<StaticRoute> staticRoutes) {
    this._acl = acl;
    this._bgpPolicy = bgpPolicy;
    this._ospfPolicy = ospfPolicy;
    this._ospfCost = ospfCost;
    this._staticRoutes = staticRoutes;
  }

  public BDD getAcl() {
    return _acl;
  }

  public BDDRecord getBgpPolicy() {
    return _bgpPolicy;
  }

  public BDDRecord getOspfPolicy() {
    return _ospfPolicy;
  }

  public Integer getOspfCost() {
    return _ospfCost;
  }

  public SortedSet<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InterfacePolicy)) {
      return false;
    }
    InterfacePolicy other = (InterfacePolicy) o;
    return Objects.equals(_acl, other._acl)
        && Objects.equals(_bgpPolicy, other._bgpPolicy)
        && Objects.equals(_ospfCost, other._ospfCost)
        && Objects.equals(_ospfPolicy, other._ospfPolicy)
        && Objects.equals(_staticRoutes, other._staticRoutes);
  }

  @Override
  public int hashCode() {
    int result = _ospfCost != null ? _ospfCost.hashCode() : 0;
    result = 31 * result + (_acl != null ? _acl.hashCode() : 0);
    result = 31 * result + (_bgpPolicy != null ? _bgpPolicy.hashCode() : 0);
    result = 31 * result + (_ospfPolicy != null ? _ospfPolicy.hashCode() : 0);
    result = 31 * result + (_ospfCost != null ? _ospfCost.hashCode() : 0);
    result = 31 * result + (_staticRoutes != null ? _staticRoutes.hashCode() : 0);
    return result;
  }
}
