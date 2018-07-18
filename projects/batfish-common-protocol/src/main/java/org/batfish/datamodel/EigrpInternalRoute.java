package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;

/** Represents an internal EIGRP route */
public class EigrpInternalRoute extends EigrpRoute {

  private static final long serialVersionUID = 1L;

  @JsonCreator
  public EigrpInternalRoute(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_ASN) long asn,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric) {
    super(admin, asn, network, nextHopInterface, nextHopIp, metric);
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.EIGRP;
  }

  @Override
  public int routeCompare(@Nonnull AbstractRoute rhs) {
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    EigrpInternalRoute castRhs = (EigrpInternalRoute) rhs;
    return Long.compare(_asn, castRhs._asn);
  }

  public static class Builder extends AbstractRouteBuilder<Builder, EigrpInternalRoute> {

    private Long _asn;

    private EigrpMetric _eigrpMetric;

    private String _nextHopInterface;

    @Override
    public EigrpInternalRoute build() {
      return new EigrpInternalRoute(
          getAdmin(),
          Objects.requireNonNull(_asn),
          getNetwork(),
          _nextHopInterface,
          getNextHopIp(),
          _eigrpMetric);
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setAsn(long asn) {
      _asn = asn;
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
