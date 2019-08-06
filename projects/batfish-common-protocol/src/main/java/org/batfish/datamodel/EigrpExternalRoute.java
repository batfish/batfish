package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;

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
      @Nullable Ip nextHopIp,
      @Nonnull EigrpMetric metric,
      long processAsn,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(admin, network, nextHopIp, metric, processAsn, tag, nonForwarding, nonRouting);
    _destinationAsn = destinationAsn;
  }

  @JsonCreator
  private static EigrpExternalRoute create(
      @Nullable @JsonProperty(PROP_ADMINISTRATIVE_COST) Integer admin,
      @Nullable @JsonProperty(PROP_DESTINATION_ASN) Long destinationAsn,
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @Nullable @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric,
      @Nullable @JsonProperty(PROP_PROCESS_ASN) Long processAsn,
      @Nullable @JsonProperty(PROP_TAG) Long tag) {
    checkArgument(admin != null, "EIGRP route: missing %s", PROP_ADMINISTRATIVE_COST);
    checkArgument(destinationAsn != null, "EIGRP route: missing %s", PROP_DESTINATION_ASN);
    checkArgument(metric != null, "EIGRP route: missing %s", PROP_EIGRP_METRIC);
    checkArgument(processAsn != null, "EIGRP route: missing %s", PROP_PROCESS_ASN);
    checkArgument(tag != null, "EIGRP route: missing %s", PROP_TAG);
    return new EigrpExternalRoute(
        network, admin, destinationAsn, nextHopIp, metric, processAsn, tag, false, false);
  }

  @JsonProperty(PROP_DESTINATION_ASN)
  public long getDestinationAsn() {
    return _destinationAsn;
  }

  @Override
  @Nonnull
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.EIGRP_EX;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends EigrpRoute.Builder<Builder, EigrpExternalRoute> {

    private Builder() {}

    @Nonnull
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
          getNextHopIp(),
          requireNonNull(_eigrpMetric),
          _processAsn,
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
        && _network.equals(rhs._network)
        && _nextHopIp.equals(rhs._nextHopIp)
        && _processAsn == rhs._processAsn
        && _tag == rhs._tag
        && getNonForwarding() == rhs.getNonForwarding()
        && getNonRouting() == rhs.getNonRouting();
  }

  @Override
  public final int hashCode() {
    return hash(
        _network,
        _nextHopIp,
        _admin,
        _tag,
        getNonForwarding(),
        getNonRouting(),
        _destinationAsn,
        _metric,
        _processAsn);
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
