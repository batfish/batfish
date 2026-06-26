package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

public final class StaticRouteV4 extends StaticRoute<Ip> {
  private Prefix _prefix;

  public StaticRouteV4(Prefix prefix) {
    this(prefix, null);
  }

  public StaticRouteV4(Prefix prefix, @Nullable StaticRouteV4 defaults) {
    super(defaults);
    _prefix = prefix;
  }

  public Prefix getPrefix() {
    return _prefix;
  }
}
