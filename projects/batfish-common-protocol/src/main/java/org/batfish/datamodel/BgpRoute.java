package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.common.util.CommonUtil;

public class BgpRoute extends AbstractRoute {

  public static class Builder extends AbstractRouteBuilder<Builder,BgpRoute> {

    private List<SortedSet<Integer>> _asPath;

    private SortedSet<Long> _clusterList;

    private SortedSet<Long> _communities;

    private int _localPreference;

    private Ip _originatorIp;

    private OriginType _originType;

    private RoutingProtocol _protocol;

    private boolean _receivedFromRouteReflectorClient;

    private RoutingProtocol _srcProtocol;

    private int _weight;

    public Builder() {
      _asPath = new ArrayList<>();
      _communities = new TreeSet<>();
      _clusterList = new TreeSet<>();
    }

    @Override
    public BgpRoute build() {
      if (_originatorIp == null) {
        throw new BatfishException("Missing originatorIp");
      }
      if (_originType == null) {
        throw new BatfishException("Missing originType");
      }
      return new BgpRoute(
          this.getNetwork(),
          this.getNextHopIp(),
          this.getAdmin(),
          new AsPath(_asPath),
          _communities,
          _localPreference,
          this.getMetric(),
          _originatorIp,
          _clusterList,
          _receivedFromRouteReflectorClient,
          _originType,
          _protocol,
          _srcProtocol,
          _weight);
    }

    @Override
    public Builder getThis() {
      return this;
    }

    public List<SortedSet<Integer>> getAsPath() {
      return _asPath;
    }

    public SortedSet<Long> getClusterList() {
      return _clusterList;
    }

    public SortedSet<Long> getCommunities() {
      return _communities;
    }

    public int getLocalPreference() {
      return _localPreference;
    }

    public Ip getOriginatorIp() {
      return _originatorIp;
    }

    public OriginType getOriginType() {
      return _originType;
    }

    public RoutingProtocol getProtocol() {
      return _protocol;
    }

    public int getWeight() {
      return _weight;
    }

    public void setAsPath(List<SortedSet<Integer>> asPath) {
      _asPath = asPath;
    }

    public void setClusterList(SortedSet<Long> clusterList) {
      _clusterList = clusterList;
    }

    public void setCommunities(SortedSet<Long> communities) {
      _communities = communities;
    }

    public void setLocalPreference(int localPreference) {
      _localPreference = localPreference;
    }

    public void setOriginatorIp(Ip originatorIp) {
      _originatorIp = originatorIp;
    }

    public void setOriginType(OriginType originType) {
      _originType = originType;
    }

    public void setProtocol(RoutingProtocol protocol) {
      _protocol = protocol;
    }

    public void setReceivedFromRouteReflectorClient(boolean receivedFromRouteReflectorClient) {
      _receivedFromRouteReflectorClient = receivedFromRouteReflectorClient;
    }

    public void setSrcProtocol(RoutingProtocol srcProtocol) {
      _srcProtocol = srcProtocol;
    }

    public void setWeight(int weight) {
      _weight = weight;
    }
  }

  private static final String AS_PATH_VAR = "asPath";

  private static final String CLUSTER_LIST_VAR = "clusterList";

  private static final String COMMUNITIES_VAR = "communities";

  public static final int DEFAULT_LOCAL_PREFERENCE = 100;

  private static final String LOCAL_PREFERENCE_VAR = "localPreference";

  private static final String ORIGIN_TYPE_VAR = "originType";

  private static final String ORIGINATOR_IP_VAR = "originatorIp";

  private static final String RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT_VAR =
      "receivedFromRouteReflectorClient";

  /** */
  private static final long serialVersionUID = 1L;

  private static final String SRC_PROTOCOL_VAR = "srcProtocol";

  private static final String WEIGHT_VAR = "weight";

  private final int _admin;

  private final AsPath _asPath;

  private final SortedSet<Long> _clusterList;

  private final SortedSet<Long> _communities;

  private final int _localPreference;

  private final int _med;

  private final Ip _nextHopIp;

  private final Ip _originatorIp;

  private final OriginType _originType;

  private final RoutingProtocol _protocol;

  private final boolean _receivedFromRouteReflectorClient;

  private final RoutingProtocol _srcProtocol;

  private final int _weight;

  @JsonCreator
  public BgpRoute(
      @JsonProperty(NETWORK_VAR) Prefix network,
      @JsonProperty(NEXT_HOP_IP_VAR) Ip nextHopIp,
      @JsonProperty(ADMINISTRATIVE_COST_VAR) int admin,
      @JsonProperty(AS_PATH_VAR) AsPath asPath,
      @JsonProperty(COMMUNITIES_VAR) SortedSet<Long> communities,
      @JsonProperty(LOCAL_PREFERENCE_VAR) int localPreference,
      @JsonProperty(METRIC_VAR) int med,
      @JsonProperty(ORIGINATOR_IP_VAR) Ip originatorIp,
      @JsonProperty(CLUSTER_LIST_VAR) SortedSet<Long> clusterList,
      @JsonProperty(RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT_VAR)
          boolean receivedFromRouteReflectorClient,
      @JsonProperty(ORIGIN_TYPE_VAR) OriginType originType,
      @JsonProperty(PROTOCOL_VAR) RoutingProtocol protocol,
      @JsonProperty(SRC_PROTOCOL_VAR) RoutingProtocol srcProtocol,
      @JsonProperty(WEIGHT_VAR) int weight) {
    super(network);
    _admin = admin;
    _asPath = asPath;
    _clusterList = clusterList != null ? clusterList : new TreeSet<>();
    _communities = communities != null ? communities : new TreeSet<>();
    _localPreference = localPreference;
    _med = med;
    _nextHopIp = nextHopIp;
    _originatorIp = originatorIp;
    _originType = originType;
    _protocol = protocol;
    _receivedFromRouteReflectorClient = receivedFromRouteReflectorClient;
    _srcProtocol = srcProtocol;
    _weight = weight;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    BgpRoute other = (BgpRoute) obj;
    if (_admin != other._admin) {
      return false;
    }
    if (!_asPath.equals(other._asPath)) {
      return false;
    }
    if (!_clusterList.equals(other._clusterList)) {
      return false;
    }
    if (!_communities.equals(other._communities)) {
      return false;
    }
    if (_localPreference != other._localPreference) {
      return false;
    }
    if (_med != other._med) {
      return false;
    }
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
    if (_originType != other._originType) {
      return false;
    }
    if (_originatorIp == null) {
      if (other._originatorIp != null) {
        return false;
      }
    } else if (!_originatorIp.equals(other._originatorIp)) {
      return false;
    }
    if (_protocol != other._protocol) {
      return false;
    }
    if (_weight != other._weight) {
      return false;
    }
    return true;
  }

  @JsonIgnore(false)
  @JsonProperty(ADMINISTRATIVE_COST_VAR)
  @Override
  public int getAdministrativeCost() {
    return _admin;
  }

  @JsonProperty(AS_PATH_VAR)
  public AsPath getAsPath() {
    return _asPath;
  }

  @JsonProperty(CLUSTER_LIST_VAR)
  public SortedSet<Long> getClusterList() {
    return Collections.unmodifiableSortedSet(_clusterList);
  }

  @JsonProperty(COMMUNITIES_VAR)
  public SortedSet<Long> getCommunities() {
    return Collections.unmodifiableSortedSet(_communities);
  }

  @JsonProperty(LOCAL_PREFERENCE_VAR)
  public int getLocalPreference() {
    return _localPreference;
  }

  @JsonIgnore(false)
  @JsonProperty(METRIC_VAR)
  @Override
  public Integer getMetric() {
    return _med;
  }

  @Override
  public String getNextHopInterface() {
    return null;
  }

  @JsonIgnore(false)
  @JsonProperty(NEXT_HOP_IP_VAR)
  @Override
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @JsonProperty(ORIGINATOR_IP_VAR)
  public Ip getOriginatorIp() {
    return _originatorIp;
  }

  @JsonProperty(ORIGIN_TYPE_VAR)
  public OriginType getOriginType() {
    return _originType;
  }

  @JsonIgnore(false)
  @JsonProperty(PROTOCOL_VAR)
  @Override
  public RoutingProtocol getProtocol() {
    return _protocol;
  }

  @JsonProperty(RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT_VAR)
  public boolean getReceivedFromRouteReflectorClient() {
    return _receivedFromRouteReflectorClient;
  }

  @JsonProperty(SRC_PROTOCOL_VAR)
  public RoutingProtocol getSrcProtocol() {
    return _srcProtocol;
  }

  @Override
  public int getTag() {
    return NO_TAG;
  }

  @JsonProperty(WEIGHT_VAR)
  public int getWeight() {
    return _weight;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _admin;
    result = prime * result + ((_asPath == null) ? 0 : _asPath.hashCode());
    result = prime * result + _clusterList.hashCode();
    result = prime * result + ((_communities == null) ? 0 : _communities.hashCode());
    result = prime * result + _localPreference;
    result = prime * result + _med;
    result = prime * result + _network.hashCode();
    result = prime * result + ((_nextHopIp == null) ? 0 : _nextHopIp.hashCode());
    result = prime * result + ((_originType == null) ? 0 : _originType.ordinal());
    result = prime * result + ((_originatorIp == null) ? 0 : _originatorIp.hashCode());
    result = prime * result + ((_protocol == null) ? 0 : _protocol.ordinal());
    result = prime * result + _weight;
    return result;
  }

  @Override
  protected final String protocolRouteString() {
    return " asPath:"
        + _asPath.toString()
        + " clusterList:"
        + _clusterList.toString()
        + " communities:"
        + _communities.toString()
        + " localPreference:"
        + _localPreference
        + " med:"
        + _med
        + " originatorIp:"
        + _originatorIp
        + " originType:"
        + _originType
        + " srcProtocol:"
        + _srcProtocol
        + " weight:"
        + _weight;
  }

  @Override
  public int routeCompare(AbstractRoute rhs) {
    if (getClass() != rhs.getClass()) {
      return 0;
    }
    BgpRoute castRhs = (BgpRoute) rhs;
    int ret;
    ret = _asPath.compareTo(castRhs._asPath);
    if (ret != 0) {
      return ret;
    }
    ret = CommonUtil.compareCollection(_clusterList, castRhs._clusterList);
    if (ret != 0) {
      return ret;
    }
    ret = CommonUtil.compareCollection(_communities, castRhs._communities);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_localPreference, castRhs._localPreference);
    if (ret != 0) {
      return ret;
    }
    ret = _originType.compareTo(castRhs._originType);
    if (ret != 0) {
      return ret;
    }
    ret = _originatorIp.compareTo(castRhs._originatorIp);
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_weight, castRhs._weight);
    return ret;
  }
}
