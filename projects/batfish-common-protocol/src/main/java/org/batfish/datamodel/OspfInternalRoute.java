package org.batfish.datamodel;

/** A base class for OSPF inter-area and intra-area routes */
public abstract class OspfInternalRoute extends OspfRoute {

  private static final long serialVersionUID = 1L;

  public static final class Builder extends AbstractRouteBuilder<Builder, OspfInternalRoute> {

    private Long _area;
    private RoutingProtocol _protocol;

    @Override
    public OspfInternalRoute build() {
      if (_protocol == RoutingProtocol.OSPF) {
        return new OspfIntraAreaRoute(
            getNetwork(),
            getNextHopIp(),
            getAdmin(),
            getMetric(),
            _area,
            getNonForwarding(),
            getNonRouting());
      } else {
        return new OspfInterAreaRoute(
            getNetwork(),
            getNextHopIp(),
            getAdmin(),
            getMetric(),
            _area,
            getNonForwarding(),
            getNonRouting());
      }
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setArea(Long area) {
      _area = area;
      return this;
    }

    public Builder setProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
      return this;
    }

    private Builder() {} // Only used in this OspfInternalRoute.builder()
  }

  public static Builder builder() {
    return new Builder();
  }

  OspfInternalRoute(
      Prefix network,
      Ip nextHopIp,
      int admin,
      long metric,
      long area,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, nextHopIp, admin, metric, area, nonRouting, nonForwarding);
  }
}
