package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;

/** Represents an internal EIGRP route */
public class EigrpInternalRoute extends EigrpRoute {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  private EigrpInternalRoute(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric) {
    super(admin, network, nextHopInterface, nextHopIp, metric);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.EIGRP;
  }

  @Override
  public int routeCompare(@Nonnull AbstractRoute rhs) {
    return 0;
  }

  public static class Builder extends AbstractRouteBuilder<Builder, EigrpInternalRoute> {

    private EigrpMetric _eigrpMetric;

    private String _nextHopInterface;

    @Override
    public EigrpInternalRoute build() {
      return new EigrpInternalRoute(
          getAdmin(), getNetwork(), _nextHopInterface, getNextHopIp(), _eigrpMetric);
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setEigrpMetric(EigrpMetric metric) {
      _eigrpMetric = metric;
      return this;
    }

    public Builder setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
      return this;
    }
  }
}
