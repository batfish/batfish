package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.eigrp.EigrpMetric;

/** Represents an EIGRP route, internal or external */
public abstract class EigrpRoute extends AbstractRoute {

  protected static final String PROP_ASN = "asn";
  protected static final String PROP_EIGRP_METRIC = "eigrp-metric";

  private static final long serialVersionUID = 1L;

  protected final int _admin;

  protected final long _asn;

  protected final EigrpMetric _metric;

  protected final String _nextHopInterface;

  protected final Ip _nextHopIp;

  @JsonCreator
  public EigrpRoute(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_ASN) long asn,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @Nullable @JsonProperty(PROP_NEXT_HOP_INTERFACE) String nextHopInterface,
      @Nullable @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_METRIC) EigrpMetric metric) {
    super(network);
    _admin = admin;
    _asn = asn;
    _metric = metric;
    _nextHopInterface = firstNonNull(nextHopInterface, Route.UNSET_NEXT_HOP_INTERFACE);
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EigrpRoute)) {
      return false;
    }
    EigrpRoute rhs = (EigrpRoute) obj;
    return _admin == rhs._admin
        && _asn == rhs._asn
        && Objects.equals(_network, rhs._network)
        && Objects.equals(_nextHopInterface, rhs._nextHopInterface)
        && Objects.equals(_nextHopIp, rhs._nextHopIp)
        && Objects.equals(_metric, rhs._metric);
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_ADMINISTRATIVE_COST)
  @Override
  public final int getAdministrativeCost() {
    return _admin;
  }

  @JsonProperty(PROP_ASN)
  public final long getAsNumber() {
    return _asn;
  }

  @JsonProperty(PROP_EIGRP_METRIC)
  public final EigrpMetric getEigrpMetric() {
    return _metric;
  }

  @Override
  public final Long getMetric() {
    return _metric.getCost();
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

  @Override
  public abstract RoutingProtocol getProtocol();

  @Override
  public int getTag() {
    // TODO support EIGRP route tags
    // https://github.com/batfish/batfish/issues/1945
    return NO_TAG;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(_admin, _asn, _metric.hashCode(), _network, _nextHopIp, _nextHopInterface);
  }

  @Override
  protected String protocolRouteString() {
    return _metric.prettyPrint();
  }
}
