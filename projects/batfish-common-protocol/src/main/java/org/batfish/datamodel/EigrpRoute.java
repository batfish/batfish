package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;

/** Represents an EIGRP route, internal or external */
public abstract class EigrpRoute extends AbstractRoute {

  static final String PROP_EIGRP_METRIC = "eigrp-metric";
  static final String PROP_PROCESS_ASN = "process-asn";

  private static final long serialVersionUID = 1L;

  protected final int _admin;
  @Nonnull protected final EigrpMetric _metric;
  @Nonnull protected final Ip _nextHopIp;

  /** AS number of the EIGRP process that installed this route in the RIB */
  final long _processAsn;

  EigrpRoute(
      int admin,
      Prefix network,
      @Nullable Ip nextHopIp,
      @Nullable EigrpMetric metric,
      long processAsn,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, admin, nonRouting, nonForwarding);
    checkArgument(metric != null, "Cannot create EIGRP route: missing %s", PROP_EIGRP_METRIC);
    _admin = admin;
    _metric = metric;
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    _processAsn = processAsn;
  }

  @JsonIgnore
  public final long getCompositeCost() {
    return _metric.getCost();
  }

  @JsonProperty(PROP_EIGRP_METRIC)
  @Nonnull
  public final EigrpMetric getEigrpMetric() {
    return _metric;
  }

  @Override
  public final Long getMetric() {
    return _metric.getRibMetric();
  }

  @Nonnull
  @Override
  public String getNextHopInterface() {
    return Route.UNSET_NEXT_HOP_INTERFACE;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_IP)
  @Override
  public final Ip getNextHopIp() {
    return _nextHopIp;
  }

  @JsonProperty(PROP_PROCESS_ASN)
  public long getProcessAsn() {
    return _processAsn;
  }

  @Override
  public abstract RoutingProtocol getProtocol();

  @Override
  public int getTag() {
    // TODO support EIGRP route tags
    // https://github.com/batfish/batfish/issues/1945
    return NO_TAG;
  }
}
