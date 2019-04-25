package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A generic BGP route containing the common properties among different types of BGP routes */
@ParametersAreNonnullByDefault
public abstract class BgpRoute extends AbstractRoute {

  private static final long serialVersionUID = 1L;

  /** Default local preference for a BGP route if one is not set explicitly */
  public static final long DEFAULT_LOCAL_PREFERENCE = 100L;

  public static final String PROP_AS_PATH = "asPath";

  protected static final String PROP_CLUSTER_LIST = "clusterList";

  public static final String PROP_COMMUNITIES = "communities";

  protected static final String PROP_DISCARD = "discard";

  public static final String PROP_LOCAL_PREFERENCE = "localPreference";

  protected static final String PROP_ORIGIN_TYPE = "originType";

  protected static final String PROP_ORIGINATOR_IP = "originatorIp";

  protected static final String PROP_RECEIVED_FROM_IP = "receivedFromIp";

  protected static final String PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT =
      "receivedFromRouteReflectorClient";

  protected static final String PROP_SRC_PROTOCOL = "srcProtocol";

  protected static final String PROP_WEIGHT = "weight";

  @Nonnull protected final AsPath _asPath;
  @Nonnull protected final SortedSet<Long> _clusterList;
  @Nonnull protected final SortedSet<Long> _communities;
  protected final boolean _discard;
  protected final long _localPreference;
  protected final long _med;
  @Nonnull protected final String _nextHopInterface;
  @Nonnull protected final Ip _nextHopIp;
  @Nonnull protected final Ip _originatorIp;
  @Nonnull protected final OriginType _originType;
  @Nonnull protected final RoutingProtocol _protocol;
  @Nullable protected final Ip _receivedFromIp;
  protected final boolean _receivedFromRouteReflectorClient;
  @Nullable protected final RoutingProtocol _srcProtocol;
  /* NOTE: Cisco-only attribute */
  protected final int _weight;

  protected BgpRoute(
      @Nullable Prefix network,
      @Nullable Ip nextHopIp,
      int admin,
      @Nullable AsPath asPath,
      @Nullable SortedSet<Long> communities,
      boolean discard,
      long localPreference,
      long med,
      String nextHopInterface,
      Ip originatorIp,
      @Nullable SortedSet<Long> clusterList,
      boolean receivedFromRouteReflectorClient,
      OriginType originType,
      RoutingProtocol protocol,
      @Nullable Ip receivedFromIp,
      @Nullable RoutingProtocol srcProtocol,
      int weight,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, admin, nonRouting, nonForwarding);
    checkArgument(
        protocol == RoutingProtocol.BGP
            || protocol == RoutingProtocol.IBGP
            || protocol == RoutingProtocol.AGGREGATE,
        "Invalid Bgpv4Route protocol");
    _asPath = firstNonNull(asPath, AsPath.empty());
    _clusterList =
        clusterList == null ? ImmutableSortedSet.of() : ImmutableSortedSet.copyOf(clusterList);
    _communities =
        communities == null ? ImmutableSortedSet.of() : ImmutableSortedSet.copyOf(communities);
    _discard = discard;
    _localPreference = localPreference;
    _med = med;
    _nextHopInterface = nextHopInterface;
    _nextHopIp = firstNonNull(nextHopIp, Route.UNSET_ROUTE_NEXT_HOP_IP);
    _originatorIp = originatorIp;
    _originType = originType;
    _protocol = protocol;
    _receivedFromIp = receivedFromIp;
    _receivedFromRouteReflectorClient = receivedFromRouteReflectorClient;
    _srcProtocol = srcProtocol;
    _weight = weight;
  }

  @Nonnull
  @JsonProperty(PROP_AS_PATH)
  public AsPath getAsPath() {
    return _asPath;
  }

  @Nonnull
  @JsonProperty(PROP_CLUSTER_LIST)
  public SortedSet<Long> getClusterList() {
    return _clusterList;
  }

  @Nonnull
  @JsonProperty(PROP_COMMUNITIES)
  public SortedSet<Long> getCommunities() {
    return _communities;
  }

  @JsonProperty(PROP_DISCARD)
  public boolean getDiscard() {
    return _discard;
  }

  @JsonProperty(PROP_LOCAL_PREFERENCE)
  public long getLocalPreference() {
    return _localPreference;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public Long getMetric() {
    return _med;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  @Nonnull
  @Override
  public String getNextHopInterface() {
    return _nextHopInterface;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_NEXT_HOP_IP)
  @Override
  public Ip getNextHopIp() {
    return _nextHopIp;
  }

  @Nonnull
  @JsonProperty(PROP_ORIGINATOR_IP)
  public Ip getOriginatorIp() {
    return _originatorIp;
  }

  @Nonnull
  @JsonProperty(PROP_ORIGIN_TYPE)
  public OriginType getOriginType() {
    return _originType;
  }

  @Nonnull
  @JsonIgnore(false)
  @JsonProperty(PROP_PROTOCOL)
  @Override
  public RoutingProtocol getProtocol() {
    return _protocol;
  }

  @Nullable
  @JsonProperty(PROP_RECEIVED_FROM_IP)
  public Ip getReceivedFromIp() {
    return _receivedFromIp;
  }

  @JsonProperty(PROP_RECEIVED_FROM_ROUTE_REFLECTOR_CLIENT)
  public boolean getReceivedFromRouteReflectorClient() {
    return _receivedFromRouteReflectorClient;
  }

  @Nullable
  @JsonProperty(PROP_SRC_PROTOCOL)
  public RoutingProtocol getSrcProtocol() {
    return _srcProtocol;
  }

  @Override
  public int getTag() {
    return NO_TAG;
  }

  @JsonProperty(PROP_WEIGHT)
  public int getWeight() {
    return _weight;
  }
}
