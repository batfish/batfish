package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.route.nh.NextHopInterface;

/**
 * Represents directly connected routes. These are typically generated based on interface
 * adjacencies.
 */
@ParametersAreNonnullByDefault
public final class ConnectedRoute extends AbstractRoute {

  static final String PROP_EIGRP_METRIC = "eigrp-metric";
  static final String PROP_PROCESS_ASN = "process-asn";

  /** AS number of the EIGRP process that installed this route in the RIB */
  @Nullable final long _processAsn;

  @Nonnull final EigrpMetric _metric;

  @JsonCreator
  @SuppressWarnings("unused")
  private static ConnectedRoute create(
      @Nullable @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) String nextHopIp,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int adminCost,
      @JsonProperty(PROP_TAG) long tag,
      @Nullable @JsonProperty(PROP_EIGRP_METRIC) EigrpMetric metric,
      @Nullable @JsonProperty(PROP_PROCESS_ASN) Long processAsn) {
    checkArgument(network != null, "Cannot create connected route: missing %s", PROP_NETWORK);
    checkArgument(
        processAsn != null, "Cannot create connected route: missing %s", PROP_PROCESS_ASN);
    return new ConnectedRoute(
        network,
        firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE),
        adminCost,
        tag,
        metric,
        processAsn);
  }

  /** Create a connected route with admin cost of 0 */
  public ConnectedRoute(Prefix network, String nextHopInterface) {
    this(network, nextHopInterface, 0);
  }

  public ConnectedRoute(Prefix network, String nextHopInterface, int adminCost) {
    this(network, nextHopInterface, adminCost, Route.UNSET_ROUTE_TAG);
  }

  @JsonProperty(PROP_EIGRP_METRIC)
  @Nonnull
  public final EigrpMetric getEigrpMetric() {
    return _metric;
  }

  @JsonProperty(PROP_PROCESS_ASN)
  public long getProcessAsn() {
    return _processAsn;
  }

  @Override
  public String toString() {
    return "ConnectedRoute{"
        + "_network="
        + _network
        + ", _admin="
        + _admin
        + ", _tag="
        + _tag
        + ", _metric"
        + _metric
        + ", _processAsn="
        + _processAsn
        + '}';
  }

  public ConnectedRoute(Prefix network, String nextHopInterface, int adminCost, long tag) {
    this(network, nextHopInterface, adminCost, tag, null, -1);
  }

  public ConnectedRoute(
      Prefix network,
      String nextHopInterface,
      int adminCost,
      long tag,
      @Nullable EigrpMetric metric,
      long processAsn) {
    super(network, adminCost, tag, false, false);
    _metric = metric;
    _nextHop = NextHopInterface.of(nextHopInterface);
    _processAsn = processAsn;
  }

  @Override
  public long getMetric() {
    return 0L;
  }

  @Override
  public RoutingProtocol getProtocol() {
    return RoutingProtocol.CONNECTED;
  }

  /** Builder for {@link ConnectedRoute} */
  public static final class Builder extends AbstractRouteBuilder<Builder, ConnectedRoute> {
    @Nullable protected Long _processAsn;
    @Nullable protected EigrpMetric _eigrpMetric;

    @Nonnull
    @Override
    public ConnectedRoute build() {
      checkArgument(
          _nextHop != null && _nextHop instanceof NextHopInterface,
          "ConnectedRoute must have %s",
          PROP_NEXT_HOP_INTERFACE);
      if (_eigrpMetric == null || _processAsn == null) {
        return new ConnectedRoute(
            getNetwork(), ((NextHopInterface) _nextHop).getInterfaceName(), getAdmin(), getTag());
      }

      return new ConnectedRoute(
          getNetwork(),
          ((NextHopInterface) _nextHop).getInterfaceName(),
          getAdmin(),
          getTag(),
          _eigrpMetric,
          _processAsn);
    }

    public Builder setProcessAsn(@Nullable Long processAsn) {
      _processAsn = processAsn;
      return this;
    }

    public Builder setEigrpMetric(@Nonnull EigrpMetric metric) {
      _eigrpMetric = metric;
      return this;
    }

    @Nonnull
    @Override
    protected Builder getThis() {
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder()
        .setNetwork(getNetwork())
        .setAdmin(_admin)
        .setNextHop(_nextHop)
        .setNonRouting(getNonRouting())
        .setNonForwarding(getNonForwarding())
        .setTag(_tag);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ConnectedRoute)) {
      return false;
    }
    ConnectedRoute rhs = (ConnectedRoute) o;
    return _network.equals(rhs._network)
        && _admin == rhs._admin
        && getNonRouting() == rhs.getNonRouting()
        && getNonForwarding() == rhs.getNonForwarding()
        && _nextHop.equals(rhs._nextHop)
        && _tag == rhs._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_network, _admin, getNonRouting(), getNonForwarding(), _nextHop, _tag);
  }
}
