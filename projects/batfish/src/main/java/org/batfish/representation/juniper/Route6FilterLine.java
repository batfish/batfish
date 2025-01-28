package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix6;

public abstract class Route6FilterLine extends RouteFilterLine {
  protected final Prefix6 _prefix6;

  public Route6FilterLine(Prefix6 prefix6) {
    _prefix6 = prefix6;
  }

  public final Prefix6 getPrefix6() {
    return _prefix6;
  }
}
