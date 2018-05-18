package org.batfish.symbolic.bdd;

import java.util.Objects;
import net.sf.javabdd.BDD;
import org.batfish.symbolic.bdd.BDDNetFactory.BDDRoute;

public class BDDTransferFunction {

  private BDDRoute _route;

  private BDD _filter;

  BDDTransferFunction(BDDRoute r, BDD b) {
    _route = r;
    _filter = b;
  }

  public BDDRoute getRoute() {
    return _route;
  }

  public BDD getFilter() {
    return _filter;
  }

  public String debug() {
    return this.getRoute().dot(this.getFilter());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BDDTransferFunction that = (BDDTransferFunction) o;
    return Objects.equals(_route, that._route) && Objects.equals(_filter, that._filter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_route, _filter);
  }
}
