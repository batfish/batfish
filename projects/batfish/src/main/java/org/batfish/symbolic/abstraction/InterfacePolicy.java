package org.batfish.symbolic.abstraction;

import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;

public class InterfacePolicy {

  private AclBDD _acl;

  private BDDRecord _bgpPolicy;

  private Integer _ospfCost;

  private SortedSet<StaticRoute> _staticRoutes;

  // TODO: route reflectors etc

  public InterfacePolicy(
      @Nullable AclBDD acl,
      @Nullable BDDRecord bgpPolicy,
      @Nullable Integer ospfCost,
      @Nullable SortedSet<StaticRoute> staticRoutes) {
    this._acl = acl;
    this._bgpPolicy = bgpPolicy;
    this._ospfCost = ospfCost;
    this._staticRoutes = staticRoutes;
  }

  public InterfacePolicy(InterfacePolicy other) {
    this._acl = other._acl;
    this._bgpPolicy = other._bgpPolicy;
    this._ospfCost = other._ospfCost;
    this._staticRoutes = other._staticRoutes;
  }

  public AclBDD getAcl() {
    return _acl;
  }

  public BDDRecord getBgpPolicy() {
    return _bgpPolicy;
  }

  public Integer getOspfCost() {
    return _ospfCost;
  }

  public SortedSet<StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  @Override
  public int hashCode() {
    int result = _ospfCost != null ? _ospfCost.hashCode() : 0;
    result = 31 * result + (_acl != null ? _acl.hashCode() : 0);
    result = 31 * result + (_bgpPolicy != null ? _bgpPolicy.hashCode() : 0);
    result = 31 * result + (_staticRoutes != null ? _staticRoutes.hashCode() : 0);
    return result;
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
        && Objects.equals(_staticRoutes, other._staticRoutes);
  }

  public InterfacePolicy restrict(Prefix pfx) {
    InterfacePolicy pol = new InterfacePolicy(this);
    if (pol._bgpPolicy != null) {
      pol._bgpPolicy = pol._bgpPolicy.restrict(pfx);
    }
    if (pol._acl != null) {
      pol._acl = pol._acl.restrict(pfx);
    }
    return pol;
  }
}
