package org.batfish.symbolic.ainterpreter;

import java.util.Objects;
import net.sf.javabdd.BDD;

public class RouteAclStateSetPair {

  private BDD _routes;

  private BDD _acls;

  public RouteAclStateSetPair(BDD routes, BDD acls) {
    this._routes = routes;
    this._acls = acls;
  }

  public BDD getRoutes() {
    return _routes;
  }

  public BDD getAcls() {
    return _acls;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RouteAclStateSetPair that = (RouteAclStateSetPair) o;
    return Objects.equals(_routes, that._routes) && Objects.equals(_acls, that._acls);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_routes, _acls);
  }
}
