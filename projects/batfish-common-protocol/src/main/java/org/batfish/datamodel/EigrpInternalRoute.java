package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

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
      @Nonnull Prefix network,
      @Nullable String nextHopInterface,
      @Nullable Ip nextHopIp,
      @Nonnull EigrpMetric metric) {
    super(admin, network, nextHopInterface, nextHopIp, metric, processAsn);
  }

  @JsonCreator
  private static EigrpInternalRoute create(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_PROCESS_ASN) long processAsn,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric) {
    return new EigrpInternalRoute(admin, processAsn, network, nextHopInterface, nextHopIp, metric);
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
  public final int hashCode() {
    return Objects.hash(_admin, _metric.hashCode(), _network, _nextHopIp, _nextHopInterface);
  }

  public static class Builder extends AbstractRouteBuilder<Builder, EigrpInternalRoute> {

    @Nullable private EigrpMetric _eigrpMetric;

    @Nullable String _nextHopInterface;

    @Nullable Long _processAsn;

    @Nullable
    @Override
    public EigrpInternalRoute build() {
      if (getNetwork() == null || _eigrpMetric == null || _processAsn == null) {
        return null;
      }
      return new EigrpInternalRoute(
          getAdmin(),
          _processAsn,
          getNetwork(),
          _nextHopInterface,
          getNextHopIp(),
          // Metric required to build route
          requireNonNull(_eigrpMetric));
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

    public Builder setProcessAsn(Long processAsn) {
      _processAsn = processAsn;
      return this;
    }
  }
}
