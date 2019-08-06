package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;

/** Represents an internal EIGRP route */
public class EigrpInternalRoute extends EigrpRoute {

  private EigrpInternalRoute(
      int admin,
      long processAsn,
      @Nullable Prefix network,
      @Nullable Ip nextHopIp,
      @Nonnull EigrpMetric metric,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(admin, network, nextHopIp, metric, processAsn, tag, nonForwarding, nonRouting);
  }

  @JsonCreator
  private static EigrpInternalRoute create(
      @Nullable @JsonProperty(PROP_ADMINISTRATIVE_COST) Integer admin,
      @Nullable @JsonProperty(PROP_PROCESS_ASN) Long processAsn,
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric,
      @Nullable @JsonProperty(PROP_TAG) Long tag) {
    checkArgument(admin != null, "EIGRP rooute: missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(metric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
    checkArgument(processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
    checkArgument(tag != null, "EIGRP route: missing %s", PROP_TAG);
    return new EigrpInternalRoute(admin, processAsn, network, nextHopIp, metric, tag, false, false);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.EIGRP;
  }

  public static class Builder extends EigrpRoute.Builder<Builder, EigrpInternalRoute> {

    private Builder() {}

    @Override
    @Nonnull
    public EigrpInternalRoute build() {
      checkArgument(getNetwork() != null, "EIGRP route: missing %s", PROP_NETWORK);
      checkArgument(_eigrpMetric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
      checkArgument(_processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
      checkArgument(
          getMetric() == 0, "EIGRP route: cannot set metric directly, use setEigrpMetric instead");
      return new EigrpInternalRoute(
          getAdmin(),
          _processAsn,
          getNetwork(),
          getNextHopIp(),
          _eigrpMetric,
          getTag(),
          getNonForwarding(),
          getNonRouting());
    }

    @Nonnull
    @Override
    protected Builder getThis() {
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
        .setNextHopIp(getNextHopIp())
        // Skip setMetric since this builder ignores it in favor of setEigrpMetric
        .setTag(getTag())
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        // EigrpInternalRoute properties
        .setEigrpMetric(getEigrpMetric())
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
        && _nextHopIp.equals(rhs._nextHopIp)
        && _tag == rhs._tag
        && _processAsn == rhs._processAsn
        && _metric.equals(rhs._metric)
        && getNonForwarding() == rhs.getNonForwarding()
        && getNonRouting() == rhs.getNonRouting();
  }

  @Override
  public final int hashCode() {
    return Objects.hash(
        _admin,
        _network,
        _nextHopIp,
        _tag,
        _processAsn,
        _metric,
        getNonForwarding(),
        getNonRouting());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_NETWORK, _network)
        .add(PROP_NEXT_HOP_IP, _nextHopIp)
        .add(PROP_PROCESS_ASN, _processAsn)
        .toString();
  }
}
