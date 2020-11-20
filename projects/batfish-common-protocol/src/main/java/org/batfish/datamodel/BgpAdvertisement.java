package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
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

    private long _localPreference;

    private long _med;

    private Prefix _network;

    private Ip _nextHopIp;

    private Ip _originatorIp;

    private OriginType _originType;

    private Ip _srcIp;

    private RoutingProtocol _srcProtocol;

    private BgpAdvertisementType _type;

    private int _weight;

    public BgpAdvertisement build() {
      return new BgpAdvertisement(
          _type,
          _network,
          _nextHopIp,
          _srcIp,
          _dstNode,
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

    public Builder setSrcProtocol(RoutingProtocol srcProtocol) {
      _srcProtocol = srcProtocol;
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
  private static final String PROP_LOCAL_PREFERENCE = "localPreference";
  private static final String PROP_MED = "med";
  private static final String PROP_NETWORK = "network";
  private static final String PROP_NEXT_HOP_IP = "nextHopIp";
  private static final String PROP_ORIGIN_TYPE = "originType";
  private static final String PROP_ORIGINATOR_IP = "originatorIp";
  private static final String PROP_SRC_IP = "srcIp";
  private static final String PROP_SRC_PROTOCOL = "srcProtocol";
  private static final String PROP_TYPE = "type";
  private static final String PROP_WEIGHT = "weight";

  // deprecated fields that exist only for backward compatibility
  private static final String PROP_DST_VRF = "dstVrf";
  private static final String PROP_SRC_VRF = "srcVrf";
  private static final String PROP_SRC_NODE = "srcNode";

  public static final long UNSET_LOCAL_PREFERENCE = 0;

  public static final Ip UNSET_ORIGINATOR_IP = Ip.AUTO;

  public static final int UNSET_WEIGHT = 0;

  private final AsPath _asPath;

  private final SortedSet<Long> _clusterList;

  private final SortedSet<Community> _communities;

  private final Ip _dstIp;

  private final String _dstNode;

  private final long _localPreference;

  private final long _med;

  private final Prefix _network;

  private final Ip _nextHopIp;

  private final Ip _originatorIp;

  private final OriginType _originType;

  private final Ip _srcIp;

  private final RoutingProtocol _srcProtocol;

  private final BgpAdvertisementType _type;

  private final int _weight;

  @JsonCreator
  private static BgpAdvertisement create(
      @JsonProperty(PROP_TYPE) BgpAdvertisementType type,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_SRC_NODE) String srcNode, // unused field (here for backward compatibility)
      @JsonProperty(PROP_SRC_VRF) String srcVrf, // unused field (here for backward compatibility)
      @JsonProperty(PROP_SRC_IP) Ip srcIp,
      @JsonProperty(PROP_DST_NODE) String dstNode,
      @JsonProperty(PROP_DST_VRF) String dstVrf, // unused field (here for backward compatibility)
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
        srcIp,
        dstNode,
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
      @Nonnull Ip srcIp,
      @Nonnull String dstNode,
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
    _srcIp = srcIp;
    _dstNode = dstNode.toLowerCase(); // canonicalize node name
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

  @Override
  public int compareTo(BgpAdvertisement rhs) {
    int ret;
    ret = _type.compareTo(rhs._type);
    if (ret != 0) {
      return ret;
    }
    ret = _dstNode.compareTo(rhs._dstNode);
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
    if (_srcProtocol != other._srcProtocol) {
      return false;
    }
    if (!_type.equals(other._type)) {
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

  @JsonProperty(PROP_SRC_PROTOCOL)
  public RoutingProtocol getSrcProtocol() {
    return _srcProtocol;
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
        _localPreference,
        _med,
        _network,
        _nextHopIp,
        _originType.ordinal(),
        _originatorIp,
        _srcIp,
        _srcProtocol.ordinal(),
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
        + _dstNode
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
