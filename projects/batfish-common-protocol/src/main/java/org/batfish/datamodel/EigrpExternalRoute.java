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

  private static final String PROP_DESTINATION_ASN = "destination-asn";

  private static final long serialVersionUID = 1L;

  /**
   * AS number where the destination resides if the destination is EIGRP, or where it was learned if
   * the destination is another process
   */
  @Nonnull private final Long _destinationAsn;

  private EigrpExternalRoute(
      int admin,
      @Nonnull Long destinationAsn,
      Prefix network,
      @Nullable String nextHopInterface,
      @Nullable Ip nextHopIp,
      @Nonnull EigrpMetric metric,
      long processAsn) {
    super(admin, network, nextHopInterface, nextHopIp, metric, processAsn);
    _destinationAsn = destinationAsn;
  }

  @JsonCreator
  private static EigrpExternalRoute create(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @Nonnull @JsonProperty(PROP_DESTINATION_ASN) Long destinationAsn,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric,
      @JsonProperty(PROP_PROCESS_ASN) long processAsn) {
    return new EigrpExternalRoute(
        admin, destinationAsn, network, nextHopInterface, nextHopIp, metric, processAsn);
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
        && Objects.equals(_destinationAsn, rhs._destinationAsn)
        && Objects.equals(_metric, rhs._metric)
        && Objects.equals(_network, rhs._network)
        && Objects.equals(_nextHopInterface, rhs._nextHopInterface)
        && Objects.equals(_nextHopIp, rhs._nextHopIp)
        && Objects.equals(_processAsn, rhs._processAsn);
  }

  @JsonProperty(PROP_DESTINATION_ASN)
  @Nonnull
  public Long getDestinationAsn() {
    return _destinationAsn;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.EIGRP_EX;
  }

  @Override
  public final int hashCode() {
    return hash(
        _admin, _destinationAsn, _metric.hashCode(), _network, _nextHopIp, _nextHopInterface);
  }

  public static class Builder extends AbstractRouteBuilder<Builder, EigrpExternalRoute> {

    @Nullable private Long _destinationAsn;

    @Nullable private EigrpMetric _eigrpMetric;

    @Nullable String _nextHopInterface;

    @Nullable Long _processAsn;

    @Nullable
    @Override
    public EigrpExternalRoute build() {
      if (_destinationAsn == null || _eigrpMetric == null || _processAsn == null) {
        return null;
      }
      return new EigrpExternalRoute(
          getAdmin(),
          _destinationAsn,
          getNetwork(),
          _nextHopInterface,
          getNextHopIp(),
          requireNonNull(_eigrpMetric),
          _processAsn);
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setDestinationAsn(@Nullable Long destinationAsn) {
      _destinationAsn = destinationAsn;
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

    public Builder setProcessAsn(Long processAsn) {
      _processAsn = processAsn;
      return this;
    }
  }
}
