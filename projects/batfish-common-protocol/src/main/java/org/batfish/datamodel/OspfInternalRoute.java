package org.batfish.datamodel;

import java.util.Objects;

public abstract class OspfInternalRoute extends OspfRoute {

  /** */
  private static final long serialVersionUID = 1L;

  public OspfInternalRoute(Prefix network, Ip nextHopIp, int admin, long metric, long area) {
    super(network, nextHopIp, admin, metric, area);
  }

  @Override
  protected final String protocolRouteString() {
    return " area:" + _area;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(_admin, _area, _metric, _network, _nextHopIp);
  }
}
