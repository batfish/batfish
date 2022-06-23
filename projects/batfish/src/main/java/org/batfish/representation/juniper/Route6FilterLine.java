package org.batfish.representation.juniper;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Prefix6;

public abstract class Route6FilterLine extends RouteFilterLine {
  protected final Prefix6 _prefix6;

  private final Set<PsThen> _thens;

  public Route6FilterLine(Prefix6 prefix6) {
    _prefix6 = prefix6;
    _thens = new HashSet<>();
  }

  public final Prefix6 getPrefix6() {
    return _prefix6;
  }

  @Override
  public Set<PsThen> getThens() {
    return _thens;
  }
}
