package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpMetricVersion;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;

/** Represents an internal EIGRP route */
public class EigrpInternalRoute extends EigrpRoute {

  private EigrpInternalRoute(
      int admin,
      long processAsn,
      @Nullable Prefix network,
      @Nonnull NextHop nextHop,
      @Nonnull EigrpMetric metric,
      @Nonnull EigrpMetricVersion metricVersion,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        admin, network, nextHop, metric, metricVersion, processAsn, tag, nonForwarding, nonRouting);
  }

  @JsonCreator
  private static EigrpInternalRoute create(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) @Nullable Integer admin,
      @JsonProperty(PROP_PROCESS_ASN) @Nullable Long processAsn,
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_EIGRP_METRIC) @Nullable EigrpMetric metric,
      @JsonProperty(PROP_EIGRP_METRIC_VERSION) @Nullable EigrpMetricVersion metricVersion,
      @JsonProperty(PROP_TAG) @Nullable Long tag) {
    checkArgument(admin != null, "EIGRP route: missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
    checkArgument(metricVersion != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC_VERSION);
    checkArgument(processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
    checkArgument(tag != null, "EIGRP route: missing %s", PROP_TAG);
    return new EigrpInternalRoute(
        admin,
        processAsn,
        network,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        metric,
        metricVersion,
        tag,
        false,
        false);
  }

  public static Builder builder() {
    return new Builder();
  }

  @VisibleForTesting
  public static Builder testBuilder() {
    return builder()
        .setNextHop(NextHopDiscard.instance())
        .setEigrpMetricVersion(EigrpMetricVersion.V1);
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.EIGRP;
  }

  public static class Builder extends EigrpRoute.Builder<Builder, EigrpInternalRoute> {

    private Builder() {}

    @Override
    public @Nonnull EigrpInternalRoute build() {
      checkArgument(getNetwork() != null, "EIGRP route: missing %s", PROP_NETWORK);
      checkArgument(_eigrpMetric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
      checkArgument(
          _eigrpMetricVersion != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC_VERSION);
      checkArgument(_processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
      checkArgument(
          getMetric() == 0, "EIGRP route: cannot set metric directly, use setEigrpMetric instead");
      checkArgument(_nextHop != null, "EIGRP route: missing next hop");
      return new EigrpInternalRoute(
          getAdmin(),
          _processAsn,
          getNetwork(),
          _nextHop,
          _eigrpMetric,
          _eigrpMetricVersion,
          getTag(),
          getNonForwarding(),
          getNonRouting());
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder()
        // AbstractRoute properties
        .setAdmin(getAdministrativeCost())
        .setNetwork(getNetwork())
        .setNextHop(getNextHop())
        // Skip setMetric since this builder ignores it in favor of setEigrpMetric
        .setTag(getTag())
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        // EigrpInternalRoute properties
        .setEigrpMetric(getEigrpMetric())
        .setEigrpMetricVersion(getEigrpMetricVersion())
        .setProcessAsn(getProcessAsn());
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
        // Skip #getMetric() since it is derived from EigrpMetric _metric
        && _network.equals(rhs._network)
        && _nextHop.equals(rhs._nextHop)
        && _tag == rhs._tag
        && _processAsn == rhs._processAsn
        && _metric.equals(rhs._metric)
        && _metricVersion == rhs._metricVersion
        && getNonForwarding() == rhs.getNonForwarding()
        && getNonRouting() == rhs.getNonRouting();
  }

  @Override
  public final int hashCode() {
    return Objects.hash(
        _admin,
        _network,
        _nextHop,
        _tag,
        _processAsn,
        _metric,
        _metricVersion,
        getNonForwarding(),
        getNonRouting());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_NETWORK, _network)
        .add(PROP_NEXT_HOP_IP, _nextHop)
        .add(PROP_PROCESS_ASN, _processAsn)
        .toString();
  }
}
