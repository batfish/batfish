package org.batfish.representation.juniper;

import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;

public abstract class Route4FilterLine extends RouteFilterLine {

  protected final Prefix _prefix;

  public Route4FilterLine(Prefix prefix) {
    _prefix = prefix;
  }

  public abstract void applyTo(RouteFilterList rfl);
}
