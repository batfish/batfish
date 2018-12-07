package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ospf.OspfMetricType;

public abstract class OspfExternalRoute extends OspfRoute {

  public static final class Builder extends AbstractRouteBuilder<Builder, OspfExternalRoute> {

    private String _advertiser;

    private Long _area;

    private Long _costToAdvertiser;

    private Long _lsaMetric;

    private OspfMetricType _ospfMetricType;

    @Override
    public OspfExternalRoute build() {
      RoutingProtocol protocol = _ospfMetricType.toRoutingProtocol();
      OspfExternalRoute route;
      if (protocol == RoutingProtocol.OSPF_E1) {
        route =
            new OspfExternalType1Route(
                getNetwork(),
                getNextHopIp(),
                getAdmin(),
                getMetric(),
                _lsaMetric,
                _area,
                _costToAdvertiser,
                _advertiser,
                getNonForwarding(),
                getNonRouting());
      } else {
        route =
            new OspfExternalType2Route(
                getNetwork(),
                getNextHopIp(),
                getAdmin(),
                getMetric(),
                _lsaMetric,
                _area,
                _costToAdvertiser,
                _advertiser,
                getNonForwarding(),
                getNonRouting());
      }
      return route;
    }

    public String getAdvertiser() {
      return _advertiser;
    }

    public Long getCostToAdvertiser() {
      return _costToAdvertiser;
    }

    public OspfMetricType getOspfMetricType() {
      return _ospfMetricType;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    public Builder setAdvertiser(String advertiser) {
      _advertiser = advertiser;
      return getThis();
    }

    public Builder setArea(long area) {
      _area = area;
      return getThis();
    }

    public Builder setCostToAdvertiser(long costToAdvertiser) {
      _costToAdvertiser = costToAdvertiser;
      return getThis();
    }

    public Builder setLsaMetric(long lsaMetric) {
      _lsaMetric = lsaMetric;
      return getThis();
    }

    public Builder setOspfMetricType(OspfMetricType ospfMetricType) {
      _ospfMetricType = ospfMetricType;
      return getThis();
    }

    private Builder() {} // only for use by #builder()
  }

  protected static final String PROP_ADVERTISER = "advertiser";

  protected static final String PROP_COST_TO_ADVERTISER = "costToAdvertiser";

  protected static final String PROP_LSA_METRIC = "lsaMetric";

  protected static final String PROP_OSPF_METRIC_TYPE = "ospfMetricType";

  /** */
  private static final long serialVersionUID = 1L;

  private final String _advertiser;

  private final long _costToAdvertiser;

  private final long _lsaMetric;

  public static Builder builder() {
    return new Builder();
  }

  OspfExternalRoute(
      Prefix prefix,
      Ip nextHopIp,
      int admin,
      long metric,
      long lsaMetric,
      long area,
      String advertiser,
      long costToAdvertiser,
      boolean nonForwarding,
      boolean nonRouting) {
    super(prefix, nextHopIp, admin, metric, area);
    setNonForwarding(nonForwarding);
    setNonRouting(nonRouting);
    _advertiser = advertiser;
    _costToAdvertiser = costToAdvertiser;
    _lsaMetric = lsaMetric;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof OspfExternalRoute)) {
      return false;
    }
    OspfExternalRoute other = (OspfExternalRoute) o;
    if (!_network.equals(other._network)) {
      return false;
    }
    if (_nextHopIp == null) {
      if (other._nextHopIp != null) {
        return false;
      }
    } else if (!_nextHopIp.equals(other._nextHopIp)) {
      return false;
    }
    if (_admin != other._admin) {
      return false;
    }
    if (_lsaMetric != other._lsaMetric) {
      return false;
    }
    if (_metric != other._metric) {
      return false;
    }
    if (getOspfMetricType() != other.getOspfMetricType()) {
      return false;
    }
    return true;
  }

  @JsonProperty(PROP_ADVERTISER)
  public final String getAdvertiser() {
    return _advertiser;
  }

  @JsonProperty(PROP_COST_TO_ADVERTISER)
  public long getCostToAdvertiser() {
    return _costToAdvertiser;
  }

  @JsonProperty(PROP_LSA_METRIC)
  public long getLsaMetric() {
    return _lsaMetric;
  }

  @Nonnull
  @Override
  public String getNextHopInterface() {
    return Route.UNSET_NEXT_HOP_INTERFACE;
  }

  @JsonIgnore
  public abstract OspfMetricType getOspfMetricType();

  @Override
  public RoutingProtocol getProtocol() {
    return getOspfMetricType().toRoutingProtocol();
  }

  @Override
  public int getTag() {
    return NO_TAG;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _network.hashCode();
    result = prime * result + ((_nextHopIp == null) ? 0 : _nextHopIp.hashCode());
    result = prime * result + _admin;
    result = prime * result + Long.hashCode(_metric);
    result = prime * result + Long.hashCode(_lsaMetric);
    result = prime * result + ((getOspfMetricType() == null) ? 0 : getOspfMetricType().ordinal());
    return result;
  }

  protected abstract String ospfExternalRouteString();

  @Override
  protected final String protocolRouteString() {
    return " ospfMetricType:"
        + getOspfMetricType()
        + " lsaMetric:"
        + _lsaMetric
        + " advertiser:"
        + _advertiser
        + ospfExternalRouteString();
  }
}
