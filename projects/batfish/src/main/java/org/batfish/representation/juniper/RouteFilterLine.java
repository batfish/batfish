package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class RouteFilterLine implements Serializable {

  private final Set<PsThen> _thens;

  public RouteFilterLine() {
    _thens = new HashSet<>();
  }

  @Override
  public abstract boolean equals(Object o);

  public Set<PsThen> getThens() {
    return _thens;
  }

  @Override
  public abstract int hashCode();
}
