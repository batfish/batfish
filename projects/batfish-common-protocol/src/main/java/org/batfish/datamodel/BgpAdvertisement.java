package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.bgp.community.Community;

/**
 * Instances of this class represent hypothetical BGP advertisements used for testing, or where the
 * config of an advertising border router is unavailable
 *
 * @author arifogel
 */
public class BgpAdvertisement implements Comparable<BgpAdvertisement>, Serializable {

  public enum BgpAdvertisementType {
    EBGP_ORIGINATED,
    EBGP_RECEIVED,
    EBGP_SENT,
    IBGP_ORIGINATED,
    IBGP_RECEIVED,
    IBGP_SENT;

    /** Returns true if this advertisement is from an ebgp peer. */
    public boolean isEbgp() {
      switch (this) {
        case EBGP_ORIGINATED:
        case EBGP_RECEIVED:
        case EBGP_SENT:
          return true;
        default:
          return false;
      }
    }

    /**
     * Returns true if this advertisement is from a device's local view of the route, aka, it has
     * already been processed/filtered by import policy. These routes are installed directly in the
     * RIB.
     */
    public boolean isReceived() {
      switch (this) {
        case EBGP_RECEIVED:
        case IBGP_RECEIVED:
          return true;
        case EBGP_SENT:
        case IBGP_SENT:
          return false;
        default:
          // include originated.
          throw new BatfishException("Missing or invalid bgp advertisement type");
      }
    }

    private static final Map<String, BgpAdvertisementType> _map = buildMap();

    private static Map<String, BgpAdvertisementType> buildMap() {
      ImmutableMap.Builder<String, BgpAdvertisementType> map = ImmutableMap.builder();
      for (BgpAdvertisementType bgpAdvertisementType : BgpAdvertisementType.values()) {
        String name = bgpAdvertisementType.toString().toLowerCase();
        map.put(name, bgpAdvertisementType);
      }
      return map.build();
    }

    @JsonCreator
    public static BgpAdvertisementType fromName(String name) {
      String lName = name.toLowerCase();
      BgpAdvertisementType bgpAdvertisementType = _map.get(lName);
      if (bgpAdvertisementType == null) {
        throw new BatfishException("Invalid name: \"" + name + "\"");
      }
      return bgpAdvertisementType;
    }

    @JsonValue
    public String getName() {
      return name().toLowerCase();
    }
  }

  public static class Builder {

    private AsPath _asPath;

    private SortedSet<Long> _clusterList;

    private SortedSet<Community> _communities;

    private Ip _dstIp;

    private String _dstNode;

    private String _dstVrf;

    private long _localPreference;

    private long _med;

    private Prefix _network;

    private Ip _nextHopIp;

    private Ip _originatorIp;

    private OriginType _originType;

    private Ip _srcIp;

    private String _srcNode;

    private RoutingProtocol _srcProtocol;

    private String _srcVrf;

    private BgpAdvertisementType _type;

    private int _weight;

    public BgpAdvertisement build() {
      return new BgpAdvertisement(
          _type,
          _network,
          _nextHopIp,
          _srcNode,
          _srcVrf,
          _srcIp,
          _dstNode,
          _dstVrf,
          _dstIp,
          _srcProtocol,
          _originType,
          _localPreference,
          _med,
          _originatorIp,
          _asPath,
          _communities,
          _clusterList,
          _weight);
    }

    public Builder setAsPath(AsPath asPath) {
      _asPath = asPath;
      return this;
    }

    public Builder setClusterList(SortedSet<Long> clusterList) {
      _clusterList = clusterList;
      return this;
    }

    public Builder setCommunities(SortedSet<Community> communities) {
      _communities = communities;
      return this;
    }

    public Builder setDstIp(Ip dstIp) {
      _dstIp = dstIp;
      return this;
    }

    public Builder setDstNode(String dstNode) {
      _dstNode = dstNode;
      return this;
    }

    public Builder setDstVrf(String dstVrf) {
      _dstVrf = dstVrf;
      return this;
    }

    public Builder setLocalPreference(long localPreference) {
      _localPreference = localPreference;
      return this;
    }

    public Builder setMed(long med) {
      _med = med;
      return this;
    }

    public Builder setNetwork(Prefix network) {
      _network = network;
      return this;
    }

    public Builder setNextHopIp(Ip nextHopIp) {
      _nextHopIp = nextHopIp;
      return this;
    }

    public Builder setOriginatorIp(Ip originatorIp) {
      _originatorIp = originatorIp;
      return this;
    }

    public Builder setOriginType(OriginType originType) {
      _originType = originType;
      return this;
    }

    public Builder setSrcIp(Ip srcIp) {
      _srcIp = srcIp;
      return this;
    }

    public Builder setSrcNode(String srcNode) {
      _srcNode = srcNode;
      return this;
    }

    public Builder setSrcProtocol(RoutingProtocol srcProtocol) {
      _srcProtocol = srcProtocol;
      return this;
    }

    public Builder setSrcVrf(String srcVrf) {
      _srcVrf = srcVrf;
      return this;
    }

    public Builder setType(BgpAdvertisementType type) {
      _type = type;
      return this;
    }

    public Builder setWeight(int weight) {
      _weight = weight;
      return this;
    }
  }

  private static final String PROP_AS_PATH = "asPath";
  private static final String PROP_CLUSTER_LIST = "clusterList";
  private static final String PROP_COMMUNITIES = "communities";
  private static final String PROP_DST_IP = "dstIp";
  private static final String PROP_DST_NODE = "dstNode";
  private static final String PROP_DST_VRF = "dstVrf";
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MED = "med";
  private static final String PROP_NETWORK = "network";
  private static final String PROP_NEXT_HOP_IP = "nextHopIp";
  private static final String PROP_ORIGIN_TYPE = "originType";
  private static final String PROP_ORIGINATOR_IP = "originatorIp";
  private static final String PROP_SRC_IP = "srcIp";
  private static final String PROP_SRC_NODE = "srcNode";
  private static final String PROP_SRC_PROTOCOL = "srcProtocol";
  private static final String PROP_SRC_VRF = "srcVrf";
  private static final String PROP_TYPE = "type";
  private static final String PROP_WEIGHT = "weight";

  public static final long UNSET_LOCAL_PREFERENCE = 0;

  public static final Ip UNSET_ORIGINATOR_IP = Ip.AUTO;

  public static final int UNSET_WEIGHT = 0;

  private final AsPath _asPath;

  private final SortedSet<Long> _clusterList;

  private final SortedSet<Community> _communities;

  private final Ip _dstIp;

  private final String _dstNode;

  private final String _dstVrf;

  private final long _localPreference;

  private final long _med;

  private final Prefix _network;

  private final Ip _nextHopIp;

  private final Ip _originatorIp;

  private final OriginType _originType;

  private final Ip _srcIp;

  private final @Nullable String _srcNode;

  private final RoutingProtocol _srcProtocol;

  private final String _srcVrf;

  private final BgpAdvertisementType _type;

  private final int _weight;

  @JsonCreator
  private static BgpAdvertisement create(
      @JsonProperty(PROP_TYPE) BgpAdvertisementType type,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_SRC_NODE) String srcNode,
      @JsonProperty(PROP_SRC_VRF) String srcVrf,
      @JsonProperty(PROP_SRC_IP) Ip srcIp,
      @JsonProperty(PROP_DST_NODE) String dstNode,
      @JsonProperty(PROP_DST_VRF) String dstVrf,
      @JsonProperty(PROP_DST_IP) Ip dstIp,
      @JsonProperty(PROP_SRC_PROTOCOL) RoutingProtocol srcProtocol,
      @JsonProperty(PROP_ORIGIN_TYPE) OriginType originType,
      @JsonProperty(PROP_LOCAL_PREFERENCE) Long localPreference,
      @JsonProperty(PROP_MED) Long med,
      @JsonProperty(PROP_ORIGINATOR_IP) Ip originatorIp,
      @JsonProperty(PROP_AS_PATH) AsPath asPath,
      @JsonProperty(PROP_COMMUNITIES) SortedSet<Community> communities,
      @JsonProperty(PROP_CLUSTER_LIST) SortedSet<Long> clusterList,
      @JsonProperty(PROP_WEIGHT) Integer weight) {
    checkArgument(type != null, "type must be specified for BgpAdvertisement");
    checkArgument(network != null, "network must be specified for BgpAdvertisement");
    checkArgument(nextHopIp != null, "nextHopIp must be specified for BgpAdvertisement");
    checkArgument(srcIp != null, "srcIp must be specified for BgpAdvertisement");
    checkArgument(dstNode != null, "dstNode must be specified for BgpAdvertisement");
    checkArgument(dstIp != null, "dstIp must be specified for BgpAdvertisement");
    checkArgument(srcProtocol != null, "srcProtocol must be specified for BgpAdvertisement");
    checkArgument(originType != null, "originType must be specified for BgpAdvertisement");
    checkArgument(asPath != null, "asPath must be specified for BgpAdvertisement");
    return new BgpAdvertisement(
        type,
        network,
        nextHopIp,
        srcNode,
        firstNonNull(srcVrf, DEFAULT_VRF_NAME),
        srcIp,
        dstNode,
        firstNonNull(dstVrf, DEFAULT_VRF_NAME),
        dstIp,
        srcProtocol,
        originType,
        firstNonNull(localPreference, UNSET_LOCAL_PREFERENCE),
        firstNonNull(med, 0L),
        firstNonNull(originatorIp, UNSET_ORIGINATOR_IP),
        asPath,
        firstNonNull(communities, ImmutableSortedSet.of()),
        firstNonNull(clusterList, ImmutableSortedSet.of()),
        firstNonNull(weight, UNSET_WEIGHT));
  }

  public BgpAdvertisement(
      @Nonnull BgpAdvertisementType type,
      @Nonnull Prefix network,
      @Nonnull Ip nextHopIp,
      @Nullable String srcNode,
      @Nonnull String srcVrf,
      @Nonnull Ip srcIp,
      @Nonnull String dstNode,
      @Nonnull String dstVrf,
      @Nonnull Ip dstIp,
      @Nonnull RoutingProtocol srcProtocol,
      @Nonnull OriginType originType,
      long localPreference,
      long med,
      @Nonnull Ip originatorIp,
      @Nonnull AsPath asPath,
      @Nonnull SortedSet<Community> communities,
      @Nonnull SortedSet<Long> clusterList,
      int weight) {
    _type = type;
    _network = network;
    _nextHopIp = nextHopIp;
    _srcNode = srcNode == null ? null : srcNode.toLowerCase(); // canonicalize node name
    _srcVrf = srcVrf;
    _srcIp = srcIp;
    _dstNode = dstNode.toLowerCase(); // canonicalize node name
    _dstVrf = dstVrf;
    _dstIp = dstIp;
    _srcProtocol = srcProtocol;
    _originType = originType;
    _localPreference = localPreference;
    _med = med;
    _originatorIp = originatorIp;
    _asPath = asPath;
    _communities = ImmutableSortedSet.copyOf(communities);
    _clusterList = ImmutableSortedSet.copyOf(clusterList);
    _weight = weight;
  }

  public static Builder builder() {
    return new Builder();
  }

  @VisibleForTesting
  static int nullSafeCompareTo(@Nullable String left, @Nullable String right) {
    return left == null ? right == null ? 0 : -1 : right == null ? 1 : left.compareTo(right);
  }

  @Override
  public int compareTo(BgpAdvertisement rhs) {
    int ret;
    ret = _type.compareTo(rhs._type);
    if (ret != 0) {
      return ret;
    }
    ret = nullSafeCompareTo(_srcNode, rhs._srcNode);
    if (ret != 0) {
      return ret;
    }
    ret = _srcVrf.compareTo(rhs._srcVrf);
    if (ret != 0) {
      return ret;
    }
    ret = _dstNode.compareTo(rhs._dstNode);
    if (ret != 0) {
      return ret;
    }
    if (_dstVrf == null) {
      if (rhs._dstVrf != null) {
        ret = -1;
      } else {
        ret = 0;
      }
    } else if (rhs._dstVrf == null) {
      ret = 1;
    } else {
      ret = _dstVrf.compareTo(rhs._dstVrf);
    }
    if (ret != 0) {
      return ret;
    }
    ret = _dstNode.compareTo(rhs._dstNode);
    if (ret != 0) {
      return ret;
    }
    ret = _network.compareTo(rhs._network);
    if (ret != 0) {
      return ret;
    }
    ret = Long.compare(_localPreference, rhs._localPreference);
    if (ret != 0) {
      return ret;
    }
    ret = Long.compare(_med, rhs._med);
    if (ret != 0) {
      return ret;
    }
    ret = _nextHopIp.compareTo(rhs._nextHopIp);
    if (ret != 0) {
      return ret;
    }
    ret = _originatorIp.compareTo(rhs._originatorIp);
    if (ret != 0) {
      return ret;
    }
    ret = _originType.compareTo(rhs._originType);
    if (ret != 0) {
      return ret;
    }
    ret = _srcProtocol.compareTo(rhs._srcProtocol);
    if (ret != 0) {
      return ret;
    }
    ret = _asPath.toString().compareTo(rhs._asPath.toString());
    if (ret != 0) {
      return ret;
    }
    ret = _communities.toString().compareTo(rhs._communities.toString());
    if (ret != 0) {
      return ret;
    }
    ret = _clusterList.toString().compareTo(rhs._clusterList.toString());
    if (ret != 0) {
      return ret;
    }
    ret = Integer.compare(_weight, rhs._weight);
    if (ret != 0) {
      return ret;
    }
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BgpAdvertisement)) {
      return false;
    }
    BgpAdvertisement other = (BgpAdvertisement) o;
    if (!_network.equals(other._network)) {
      return false;
    }
    if (!_asPath.toString().equals(other._asPath.toString())) {
      return false;
    }
    if (!_clusterList.toString().equals(other._clusterList.toString())) {
      return false;
    }
    if (!_communities.toString().equals(other._communities.toString())) {
      return false;
    }
    if (!_dstIp.equals(other._dstIp)) {
      return false;
    }
    if (!_dstNode.equals(other._dstNode)) {
      return false;
    }
    if (_dstVrf == null) {
      if (other._dstVrf != null) {
        return false;
      }
    } else if (!_dstVrf.equals(other._dstVrf)) {
      return false;
    }
    if (_localPreference != other._localPreference) {
      return false;
    }
    if (_med != other._med) {
      return false;
    }
    if (!_nextHopIp.equals(other._nextHopIp)) {
      return false;
    }
    if (_originType != other._originType) {
      return false;
    }
    if (!_originatorIp.equals(other._originatorIp)) {
      return false;
    }
    if (!_srcIp.equals(other._srcIp)) {
      return false;
    }
    if (!Objects.equals(_srcNode, other._srcNode)) {
      return false;
    }
    if (_srcProtocol != other._srcProtocol) {
      return false;
    }
    if (!_srcVrf.equals(other._srcVrf)) {
      return false;
    }
    if (_type != other._type) {
      return false;
    }
    if (_weight != other._weight) {
      return false;
    }
    return true;
  }

  @JsonProperty(PROP_AS_PATH)
  public AsPath getAsPath() {
    return _asPath;
  }

  @JsonProperty(PROP_CLUSTER_LIST)
  public SortedSet<Long> getClusterList() {
    return _clusterList;
  }

  @JsonProperty(PROP_COMMUNITIES)
  public SortedSet<Community> getCommunities() {
    return _communities;
  }

  @JsonProperty(PROP_DST_IP)
  public Ip getDstIp() {
    return _dstIp;
  }

  @JsonProperty(PROP_DST_NODE)
  public String getDstNode() {
    return _dstNode;
  }

  @JsonProperty(PROP_DST_VRF)
  public String getDstVrf() {
    return _dstVrf;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public long getLocalPreference() {
    return _localPreference;
  }

  @JsonProperty(PROP_MED)
  public long getMed() {
    return _med;
  }

  @JsonProperty(PROP_NETWORK)
  public Prefix getNetwork() {
    return _network;
  }

  @JsonProperty(PROP_NEXT_HOP_IP)
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @JsonProperty(PROP_ORIGINATOR_IP)
  public Ip getOriginatorIp() {
    return _originatorIp;
  }

  @JsonProperty(PROP_ORIGIN_TYPE)
  public OriginType getOriginType() {
    return _originType;
  }

  @JsonProperty(PROP_SRC_IP)
  public Ip getSrcIp() {
    return _srcIp;
  }

  @JsonProperty(PROP_SRC_NODE)
  public @Nullable String getSrcNode() {
    return _srcNode;
  }

  @JsonProperty(PROP_SRC_PROTOCOL)
  public RoutingProtocol getSrcProtocol() {
    return _srcProtocol;
  }

  @JsonProperty(PROP_SRC_VRF)
  public String getSrcVrf() {
    return _srcVrf;
  }

  @JsonProperty(PROP_TYPE)
  public BgpAdvertisementType getType() {
    return _type;
  }

  @JsonProperty(PROP_WEIGHT)
  public int getWeight() {
    return _weight;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _asPath,
        _clusterList,
        _communities,
        _dstIp,
        _dstNode,
        _dstVrf,
        _localPreference,
        _med,
        _network,
        _nextHopIp,
        _originType.ordinal(),
        _originatorIp,
        _srcIp,
        _srcNode,
        _srcProtocol.ordinal(),
        _srcVrf,
        _type.ordinal(),
        _weight);
  }

  @Override
  public String toString() {
    String originatorIp =
        _originatorIp.equals(UNSET_ORIGINATOR_IP) ? "N/A" : _originatorIp.toString();
    return "BgpAdvert<"
        + _type
        + ", "
        + _network
        + ", "
        + _nextHopIp
        + ", "
        + _srcIp
        + ", "
        + _dstIp
        + ", "
        + _srcProtocol
        + ", "
        + _srcNode
        + ", "
        + _srcVrf
        + ", "
        + _dstNode
        + ", "
        + _dstVrf
        + ", "
        + _localPreference
        + ", "
        + _med
        + ", "
        + originatorIp
        + ", "
        + _originType
        + ">";
  }
}
