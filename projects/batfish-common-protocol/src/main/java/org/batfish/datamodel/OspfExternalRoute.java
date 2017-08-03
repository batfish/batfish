package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class OspfExternalRoute extends OspfRoute {

  public static class Builder extends AbstractRouteBuilder<Builder,OspfExternalRoute> {

    private String _advertiser;

    private Integer _costToAdvertiser;

    private OspfMetricType _ospfMetricType;

    @Override
    public OspfExternalRoute build() {
      RoutingProtocol protocol = _ospfMetricType.toRoutingProtocol();
      OspfExternalRoute route;
      if (protocol == RoutingProtocol.OSPF_E1) {
        route =
            new OspfExternalType1Route(
                this.getNetwork(),
                this.getNextHopIp(),
                this.getAdmin(),
                this.getMetric(),
                _costToAdvertiser,
                _advertiser);
      } else {
        route =
            new OspfExternalType2Route(
                this.getNetwork(),
                this.getNextHopIp(),
                this.getAdmin(),
                this.getMetric(),
                _costToAdvertiser,
                _advertiser);
      }
      return route;
    }

    @Override
    public Builder getThis() {
      return this;
    }

    public String getAdvertiser() {
      return _advertiser;
    }

    public Integer getCostToAdvertiser() {
      return _costToAdvertiser;
    }

    public OspfMetricType getOspfMetricType() {
      return _ospfMetricType;
    }

    public void setAdvertiser(String advertiser) {
      _advertiser = advertiser;
    }

    public void setCostToAdvertiser(int costToAdvertiser) {
      _costToAdvertiser = costToAdvertiser;
    }

    public void setOspfMetricType(OspfMetricType ospfMetricType) {
      _ospfMetricType = ospfMetricType;
    }
  }

  protected static final String ADVERTISER_VAR = "advertiser";

  protected static final String COST_TO_ADVERTISER_VAR = "costToAdvertiser";

  protected static final String OSPF_METRIC_TYPE_VAR = "ospfMetricType";

  /** */
  private static final long serialVersionUID = 1L;

  private final String _advertiser;

  private final int _costToAdvertiser;

  @JsonCreator
  public OspfExternalRoute(
      @JsonProperty(NETWORK_VAR) Prefix prefix,
      @JsonProperty(NEXT_HOP_IP_VAR) Ip nextHopIp,
      @JsonProperty(ADMINISTRATIVE_COST_VAR) int admin,
      int metric,
      @JsonProperty(ADVERTISER_VAR) String advertiser,
      @JsonProperty(COST_TO_ADVERTISER_VAR) int costToAdvertiser) {
    super(prefix, nextHopIp, admin, metric);
    _advertiser = advertiser;
    _costToAdvertiser = costToAdvertiser;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    OspfExternalRoute other = (OspfExternalRoute) obj;
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
    if (_metric != other._metric) {
      return false;
    }
    if (getOspfMetricType() != other.getOspfMetricType()) {
      return false;
    }
    return true;
  }

  @JsonProperty(ADVERTISER_VAR)
  public final String getAdvertiser() {
    return _advertiser;
  }

  @JsonProperty(COST_TO_ADVERTISER_VAR)
  public int getCostToAdvertiser() {
    return _costToAdvertiser;
  }

  @Override
  public String getNextHopInterface() {
    return null;
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
    result = prime * result + _metric;
    result = prime * result + ((getOspfMetricType() == null) ? 0 : getOspfMetricType().ordinal());
    return result;
  }

  protected abstract String ospfExternalRouteString();

  @Override
  protected final String protocolRouteString() {
    return " ospfMetricType:"
        + getOspfMetricType()
        + " advertiser:"
        + _advertiser
        + ospfExternalRouteString();
  }
}
