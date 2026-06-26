package org.batfish.representation.juniper;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;

public final class StaticRouteV6 extends StaticRoute<Ip6> {
  private Prefix6 _prefix6;

  public StaticRouteV6(Prefix6 prefix6) {
    this(prefix6, null);
  }

  public StaticRouteV6(Prefix6 prefix6, @Nullable StaticRouteV6 defaults) {
    super(defaults);
    _prefix6 = prefix6;
  }

  public Prefix6 getPrefix6() {
    return _prefix6;
  }
}
