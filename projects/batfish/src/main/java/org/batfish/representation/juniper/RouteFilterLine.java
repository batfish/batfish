package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;

public abstract class RouteFilterLine implements Serializable {

  private final Set<PsThen> _thens;

  public RouteFilterLine() {
    _thens = new HashSet<>();
  }

  public abstract void applyTo(Route6FilterList rfl);

  public abstract void applyTo(RouteFilterList rfl);

  @Override
  public abstract boolean equals(Object o);

  public Set<PsThen> getThens() {
    return _thens;
  }

  @Override
  public abstract int hashCode();
}
