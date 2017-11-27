package org.batfish.datamodel;

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
    final int prime = 31;
    int result = 1;
    result = prime * result + _admin;
    result = prime * result + (int) (_area ^ (_area >>> 32));
    result = prime * result + Long.hashCode(_metric);
    result = prime * result + _network.hashCode();
    result = prime * result + (_nextHopIp == null ? 0 : _nextHopIp.hashCode());
    return result;
  }
}
