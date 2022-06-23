package org.batfish.representation.juniper;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;

public abstract class Route4FilterLine extends RouteFilterLine {

  protected final Prefix _prefix;

  private final Set<PsThen> _thens;

  public Route4FilterLine(Prefix prefix) {
    _prefix = prefix;
    _thens = new HashSet<>();
  }

  public abstract void applyTo(RouteFilterList rfl);

  @Override
  public Set<PsThen> getThens() {
    return _thens;
  }
}
