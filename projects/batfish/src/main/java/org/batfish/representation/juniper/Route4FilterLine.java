package org.batfish.representation.juniper;

import java.util.List;
import org.batfish.datamodel.Prefix;

public abstract class Route4FilterLine extends RouteFilterLine {

  protected final Prefix _prefix;

  public Route4FilterLine(Prefix prefix) {
    _prefix = prefix;
  }

  /** Prefix used for longest-prefix lookup before evaluating the match type. */
  public Prefix getLookupPrefix() {
    return _prefix;
  }

  /**
   * Returns the vendor-independent {@link org.batfish.datamodel.RouteFilterLine}s for this line.
   */
  public abstract List<org.batfish.datamodel.RouteFilterLine> toRouteFilterLines();
}
