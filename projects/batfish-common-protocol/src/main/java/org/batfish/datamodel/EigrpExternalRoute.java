package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.hash;

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

/** Represents an external EIGRP route */
public class EigrpExternalRoute extends EigrpRoute {
  private static final String PROP_DESTINATION_ASN = "destination-asn";

  /**
   * AS number where the destination resides if the destination is EIGRP, or where it was learned if
   * the destination is another process
   */
  private final long _destinationAsn;

  private EigrpExternalRoute(
      @Nullable Prefix network,
      int admin,
      long destinationAsn,
      @Nonnull NextHop nextHop,
      @Nonnull EigrpMetric metric,
      @Nonnull EigrpMetricVersion metricVersion,
      long processAsn,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(
        admin, network, nextHop, metric, metricVersion, processAsn, tag, nonForwarding, nonRouting);
    _destinationAsn = destinationAsn;
  }

  @JsonCreator
  private static EigrpExternalRoute create(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) @Nullable Integer admin,
      @JsonProperty(PROP_DESTINATION_ASN) @Nullable Long destinationAsn,
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp,
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_EIGRP_METRIC) @Nullable EigrpMetric metric,
      @JsonProperty(PROP_EIGRP_METRIC_VERSION) @Nullable EigrpMetricVersion metricVersion,
      @JsonProperty(PROP_PROCESS_ASN) @Nullable Long processAsn,
      @JsonProperty(PROP_TAG) @Nullable Long tag) {
    checkArgument(admin != null, "EIGRP route: missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(destinationAsn != null, "EIGRP route: missing %s", PROP_DESTINATION_ASN);
    checkArgument(metric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
    checkArgument(metricVersion != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC_VERSION);
    checkArgument(processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
    checkArgument(tag != null, "EIGRP route: missing %s", PROP_TAG);
    return new EigrpExternalRoute(
        network,
        admin,
        destinationAsn,
        NextHop.legacyConverter(nextHopInterface, nextHopIp),
        metric,
        metricVersion,
        processAsn,
        tag,
        false,
        false);
  }

  @JsonProperty(PROP_DESTINATION_ASN)
  public long getDestinationAsn() {
    return _destinationAsn;
  }

  @Override
  public @Nonnull RoutingProtocol getProtocol() {
    return RoutingProtocol.EIGRP_EX;
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

  public static class Builder extends EigrpRoute.Builder<Builder, EigrpExternalRoute> {

    private Builder() {}

    @Override
    public @Nonnull EigrpExternalRoute build() {
      checkArgument(getNetwork() != null, "EIGRP route: missing %s", PROP_NETWORK);
      checkArgument(_destinationAsn != null, "EIGRP route: missing %s", PROP_DESTINATION_ASN);
      checkArgument(_eigrpMetric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
      checkArgument(
          _eigrpMetricVersion != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC_VERSION);
      checkArgument(_processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
      checkArgument(_nextHop != null, "EIGRP route: missing next hop");
      return new EigrpExternalRoute(
          getNetwork(),
          getAdmin(),
          _destinationAsn,
          _nextHop,
          _eigrpMetric,
          _eigrpMetricVersion,
          _processAsn,
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
        .setNetwork(getNetwork())
        .setNextHopIp(getNextHopIp())
        .setAdmin(getAdministrativeCost())
        // Skip setMetric since this builder ignores it in favor of setEigrpMetric
        .setTag(getTag())
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        // EigrpExternalRoute properties
        .setDestinationAsn(getDestinationAsn())
        .setEigrpMetric(getEigrpMetric())
        .setEigrpMetricVersion(getEigrpMetricVersion())
        .setProcessAsn(getProcessAsn());
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
        && _metric.equals(rhs._metric)
        && _metricVersion == rhs._metricVersion
        && _network.equals(rhs._network)
        && _nextHop.equals(rhs._nextHop)
        && _processAsn == rhs._processAsn
        && _tag == rhs._tag
        && getNonForwarding() == rhs.getNonForwarding()
        && getNonRouting() == rhs.getNonRouting();
  }

  @Override
  public final int hashCode() {
    return hash(
        _network,
        _nextHop,
        _admin,
        _tag,
        getNonForwarding(),
        getNonRouting(),
        _destinationAsn,
        _metric,
        _metricVersion,
        _processAsn);
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
