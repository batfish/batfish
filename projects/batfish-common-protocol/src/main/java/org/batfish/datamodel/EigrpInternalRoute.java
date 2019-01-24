package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;

/** Represents an internal EIGRP route */
public class EigrpInternalRoute extends EigrpRoute {

  private static final long serialVersionUID = 1L;

  private EigrpInternalRoute(
      int admin,
      long processAsn,
      @Nullable Prefix network,
      @Nullable String nextHopInterface,
      @Nullable Ip nextHopIp,
      @Nonnull EigrpMetric metric,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        admin, network, nextHopInterface, nextHopIp, metric, processAsn, nonForwarding, nonRouting);
  }

  @JsonCreator
  private static EigrpInternalRoute create(
      @Nullable @JsonProperty(PROP_ADMINISTRATIVE_COST) Integer admin,
      @Nullable @JsonProperty(PROP_PROCESS_ASN) Long processAsn,
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric) {
    checkArgument(admin != null, "EIGRP rooute: missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
    checkArgument(processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
    return new EigrpInternalRoute(
        admin, processAsn, network, nextHopInterface, nextHopIp, metric, false, false);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EigrpInternalRoute)) {
      return false;
    }
    EigrpInternalRoute rhs = (EigrpInternalRoute) obj;
    return _admin == rhs._admin
        && Objects.equals(_metric, rhs._metric)
        && Objects.equals(_network, rhs._network)
        && Objects.equals(_nextHopInterface, rhs._nextHopInterface)
        && Objects.equals(_nextHopIp, rhs._nextHopIp)
        && Objects.equals(_processAsn, rhs._processAsn);
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.EIGRP;
  }

  @Override
  public AbstractRouteBuilder<?, ?> toBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public final int hashCode() {
    return Objects.hash(_admin, _metric.hashCode(), _network, _nextHopIp, _nextHopInterface);
  }

  public static class Builder extends AbstractRouteBuilder<Builder, EigrpInternalRoute> {

    @Nullable private EigrpMetric _eigrpMetric;
    @Nullable String _nextHopInterface;
    @Nullable Long _processAsn;

    @Override
    @Nonnull
    public EigrpInternalRoute build() {
      checkArgument(getNetwork() != null, "EIGRP route: missing %s", PROP_NETWORK);
      checkArgument(_eigrpMetric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
      checkArgument(_processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
      return new EigrpInternalRoute(
          getAdmin(),
          _processAsn,
          getNetwork(),
          _nextHopInterface,
          getNextHopIp(),
          _eigrpMetric,
          getNonForwarding(),
          getNonRouting());
    }

    @Override
    protected Builder getThis() {
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

    public Builder setProcessAsn(@Nonnull Long processAsn) {
      _processAsn = processAsn;
      return this;
    }
  }
}
