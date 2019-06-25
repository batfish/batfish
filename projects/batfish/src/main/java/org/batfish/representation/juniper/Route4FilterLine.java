package org.batfish.representation.juniper;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route6FilterList;

public abstract class Route4FilterLine extends RouteFilterLine {

  protected final Prefix _prefix;

  private final Set<PsThen> _thens;

  public Route4FilterLine(Prefix prefix) {
    _prefix = prefix;
    _thens = new HashSet<>();
  }

  @Override
  public final void applyTo(Route6FilterList rfl) {}

  @Override
  public Set<PsThen> getThens() {
    return _thens;
  }
}
