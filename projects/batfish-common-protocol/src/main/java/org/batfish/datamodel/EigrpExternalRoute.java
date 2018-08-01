package org.batfish.datamodel;

import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;

/** Represents an external EIGRP route */
public class EigrpExternalRoute extends EigrpRoute {

  private static final String PROP_ASN = "asn";

  private static final long serialVersionUID = 1L;

  /**
   * AS number where the destination resides. Only used if a single router has multiple EIGRP
   * routing processes
   */
  @Nullable private final Long _asn;

  @JsonCreator
  private EigrpExternalRoute(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @Nullable @JsonProperty(PROP_ASN) Long asn,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric) {
    super(admin, network, nextHopInterface, nextHopIp, metric);
    _asn = asn;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EigrpExternalRoute)) {
      return false;
    }
    EigrpExternalRoute rhs = (EigrpExternalRoute) obj;
    return _admin == rhs._admin
        && Objects.equals(_asn, rhs._asn)
        && Objects.equals(_network, rhs._network)
        && Objects.equals(_nextHopInterface, rhs._nextHopInterface)
        && Objects.equals(_nextHopIp, rhs._nextHopIp)
        && Objects.equals(_metric, rhs._metric);
  }

  @JsonProperty(PROP_ASN)
  @Nullable
  public Long getAsn() {
    return _asn;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.EIGRP_EX;
  }

  @Override
  public final int hashCode() {
    return hash(_admin, _asn, _metric.hashCode(), _network, _nextHopIp, _nextHopInterface);
  }

  /** Type-specific comparison after administrative distance and metric */
  @Override
  public int routeCompare(@Nonnull AbstractRoute rhs) {
    if (getClass() != rhs.getClass() || _asn == null) {
      return 0;
    }
    EigrpExternalRoute castRhs = (EigrpExternalRoute) rhs;
    if (castRhs._asn == null) {
      return 0;
    }
    return Long.compare(_asn, castRhs._asn);
  }

  public static class Builder extends AbstractRouteBuilder<Builder, EigrpExternalRoute> {

    @Nullable String _nextHopInterface;
    @Nullable private Long _asn;
    @Nullable private EigrpMetric _eigrpMetric;

    @Override
    public EigrpExternalRoute build() {
      return new EigrpExternalRoute(
          getAdmin(),
          _asn,
          getNetwork(),
          _nextHopInterface,
          getNextHopIp(),
          requireNonNull(_eigrpMetric));
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setAsn(@Nullable Long asn) {
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
