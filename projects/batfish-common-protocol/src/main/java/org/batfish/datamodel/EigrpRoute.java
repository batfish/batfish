package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
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

  final int _admin;

  final EigrpMetric _metric;

  final String _nextHopInterface;

  final Ip _nextHopIp;

  /** AS number of the EIGRP process that installed this route in the RIB */
  final long _processAsn;

  @JsonCreator
  protected EigrpRoute(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_METRIC) EigrpMetric metric,
      @JsonProperty long processAsn) {
    super(network);
    _admin = admin;
    _metric = requireNonNull(metric);
    _nextHopInterface = firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE);
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    _processAsn = processAsn;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_ADMINISTRATIVE_COST)
  @Override
  public final int getAdministrativeCost() {
    return _admin;
  }

  @JsonIgnore
  public final long getCompositeCost() {
    return _metric.getCost();
  }

  @JsonProperty(PROP_EIGRP_METRIC)
  public final EigrpMetric getEigrpMetric() {
    return _metric;
  }

  @Override
  public final Long getMetric() {
    return _metric.getRibMetric();
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  @Override
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

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

  @Override
  protected String protocolRouteString() {
    return _metric.prettyPrint();
  }

  @Override
  public int routeCompare(@Nonnull AbstractRoute rhs) {
    return 0;
  }
}
