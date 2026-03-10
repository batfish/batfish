package org.batfish.representation.juniper;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public final class StaticRouteV4 extends StaticRoute<Ip> {
  private Prefix _prefix;

  public StaticRouteV4(Prefix prefix) {
    _prefix = prefix;
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
