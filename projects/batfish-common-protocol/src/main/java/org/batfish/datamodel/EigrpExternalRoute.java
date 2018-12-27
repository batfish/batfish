package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
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
  private final long _destinationAsn;

  private EigrpExternalRoute(
      @Nullable Prefix network,
      int admin,
      long destinationAsn,
      @Nullable String nextHopInterface,
      @Nullable Ip nextHopIp,
      @Nonnull EigrpMetric metric,
      @Nonnull Long processAsn,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        admin, network, nextHopInterface, nextHopIp, metric, processAsn, nonForwarding, nonRouting);
    _destinationAsn = destinationAsn;
  }

  @JsonCreator
  private static EigrpExternalRoute create(
      @Nullable @JsonProperty(PROP_ADMINISTRATIVE_COST) Integer admin,
      @Nullable @JsonProperty(PROP_DESTINATION_ASN) Long destinationAsn,
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric,
      @Nullable @JsonProperty(PROP_PROCESS_ASN) Long processAsn) {
    checkArgument(admin != null, "EIGRP route: missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(destinationAsn != null, "EIGRP route: missing %s", PROP_DESTINATION_ASN);
    checkArgument(metric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
    checkArgument(processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
    return new EigrpExternalRoute(
        network,
        admin,
        destinationAsn,
        nextHopInterface,
        nextHopIp,
        metric,
        processAsn,
        false,
        false);
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
  @Nonnull
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

    @Override
    public EigrpExternalRoute build() {
      checkArgument(getNetwork() != null, "EIGRP route: missing %s", PROP_NETWORK);
      checkArgument(_destinationAsn != null, "EIGRP route: missing %s", PROP_DESTINATION_ASN);
      checkArgument(_eigrpMetric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
      checkArgument(_processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
      return new EigrpExternalRoute(
          getNetwork(),
          getAdmin(),
          _destinationAsn,
          _nextHopInterface,
          getNextHopIp(),
          requireNonNull(_eigrpMetric),
          _processAsn,
          getNonForwarding(),
          getNonRouting());
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setDestinationAsn(@Nonnull Long destinationAsn) {
      _destinationAsn = destinationAsn;
      return this;
    }

    public Builder setEigrpMetric(@Nonnull EigrpMetric metric) {
      _eigrpMetric = metric;
      return this;
    }

    public Builder setNextHopInterface(@Nullable String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
      return this;
    }

    public Builder setProcessAsn(@Nullable Long processAsn) {
      _processAsn = processAsn;
      return this;
    }
  }
}
