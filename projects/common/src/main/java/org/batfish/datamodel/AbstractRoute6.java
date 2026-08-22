package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Base class for IPv6 routes.
 *
 * <p>This model is intentionally separate from {@link AbstractRoute}, whose
 * network and next-hop APIs are IPv4-specific.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@ParametersAreNonnullByDefault
public abstract class AbstractRoute6 implements Serializable {

  public static final long MAX_ADMIN_DISTANCE = 0xFFFFFFFFL;
  public static final long MAX_TAG = 0xFFFFFFFFL;

  static final String PROP_ADMINISTRATIVE_COST = "administrativeCost";
  static final String PROP_METRIC = "metric";
  static final String PROP_NETWORK = "network";
  static final String PROP_NEXT_HOP_INTERFACE = "nextHopInterface";
  static final String PROP_NEXT_HOP_IP = "nextHopIp";
  static final String PROP_PROTOCOL = "protocol";
  static final String PROP_TAG = "tag";

  protected AbstractRoute6(
      Prefix6 network,
      long admin,
      long tag,
      boolean nonRouting,
      boolean nonForwarding,
      String nextHopInterface,
      @Nullable Ip6 nextHopIp) {
    checkArgument(
        admin >= 0 && admin <= MAX_ADMIN_DISTANCE,
        "Invalid admin distance %s",
        admin);
    checkArgument(
        tag == Route.UNSET_ROUTE_TAG || (tag >= 0 && tag <= MAX_TAG),
        "Invalid tag %s",
        tag);

    _network = network;
    _admin = (int) admin;
    _nonRouting = nonRouting;
    _nonForwarding = nonForwarding;
    _nextHopInterface = nextHopInterface;
    _nextHopIp = nextHopIp;
    _hasTag = tag != Route.UNSET_ROUTE_TAG;
    _tag = (int) tag;
  }

  @JsonProperty(PROP_ADMINISTRATIVE_COST)
  public final long getAdministrativeCost() {
    return Integer.toUnsignedLong(_admin);
  }

  @JsonIgnore
  public abstract long getMetric();

  @JsonProperty(PROP_NETWORK)
  public final @Nonnull Prefix6 getNetwork() {
    return _network;
  }

  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  public final @Nonnull String getNextHopInterface() {
    return _nextHopInterface;
  }

  @JsonProperty(PROP_NEXT_HOP_IP)
  public final @Nullable Ip6 getNextHopIp() {
    return _nextHopIp;
  }

  @JsonIgnore
  public final boolean getNonForwarding() {
    return _nonForwarding;
  }

  @JsonIgnore
  public final boolean getNonRouting() {
    return _nonRouting;
  }

  @JsonIgnore
  public abstract RoutingProtocol getProtocol();

  @JsonProperty(PROP_TAG)
  public final long getTag() {
    return _hasTag ? Integer.toUnsignedLong(_tag) : Route.UNSET_ROUTE_TAG;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "<"
        + _network
        + ",nhip:"
        + _nextHopIp
        + ",nhint:"
        + _nextHopInterface
        + ">";
  }

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();

  protected final @Nonnull Prefix6 _network;

  private final int _admin;
  private final boolean _nonRouting;
  private final boolean _nonForwarding;
  private final @Nonnull String _nextHopInterface;
  private final @Nullable Ip6 _nextHopIp;
  private final boolean _hasTag;
  private final int _tag;
}
