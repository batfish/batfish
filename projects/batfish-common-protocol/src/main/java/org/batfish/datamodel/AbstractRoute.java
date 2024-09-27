package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.LegacyNextHops;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;

/**
 * A base class for all types of routes supported in the dataplane computation, making this the most
 * general route type available. "Main" non-protocol-specific RIBs store and reason about this type
 * of route.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@ParametersAreNonnullByDefault
public abstract class AbstractRoute implements AbstractRouteDecorator, Serializable {

  // unsigned 32-bit int max
  public static final long MAX_TAG = 0xFFFFFFFFL;

  // The maximum (VI) value for admin distance. Most OSes limit it to 255, and so do most networks,
  // but at least one OS (Junos) allows values up to 2^32-1.
  // TODO(https://github.com/batfish/batfish/issues/8808): update if we need to.
  public static final int MAX_ADMIN_DISTANCE = Integer.MAX_VALUE;

  static final String PROP_ADMINISTRATIVE_COST = "administrativeCost";
  public static final String PROP_METRIC = "metric";
  static final String PROP_NETWORK = "network";
  static final String PROP_NEXT_HOP_INTERFACE = "nextHopInterface";
  static final String PROP_NEXT_HOP_IP = "nextHopIp";
  static final String PROP_PROTOCOL = "protocol";
  static final String PROP_TAG = "tag";

  protected final @Nonnull Prefix _network;
  protected final int _admin;
  private final boolean _nonRouting;
  private final boolean _nonForwarding;
  protected final long _tag;
  protected @Nonnull NextHop _nextHop = NextHopDiscard.instance();

  /** Helper to check admin distance validity. */
  @SuppressWarnings("ComparisonOutOfRange")
  static void checkAdmin(int admin, int lower) {
    checkArgument(
        admin >= lower && admin <= MAX_ADMIN_DISTANCE,
        "Invalid admin distance %s is not in [%s,%s]",
        admin,
        lower,
        MAX_ADMIN_DISTANCE);
  }

  protected AbstractRoute(
      @Nullable Prefix network, int admin, long tag, boolean nonRouting, boolean nonForwarding) {
    checkArgument(network != null, "Cannot create a route without a %s", PROP_NETWORK);
    checkAdmin(admin, 0);
    _network = network;
    _admin = admin;
    _nonForwarding = nonForwarding;
    _nonRouting = nonRouting;
    _tag = tag;
  }

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();

  @JsonIgnore
  @Override
  public AbstractRoute getAbstractRoute() {
    return this;
  }

  public final int getAdministrativeCost() {
    return _admin;
  }

  @JsonIgnore
  public abstract long getMetric();

  /** IPV4 network of this route */
  @JsonProperty(PROP_NETWORK)
  @Override
  public final @Nonnull Prefix getNetwork() {
    return _network;
  }

  /**
   * Name of the next-hop interface for this route. If not known, {@link
   * Route#UNSET_NEXT_HOP_INTERFACE} must be returned.
   */
  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  public final @Nonnull String getNextHopInterface() {
    return LegacyNextHops.getNextHopInterface(_nextHop).orElse(Route.UNSET_NEXT_HOP_INTERFACE);
  }

  /**
   * Next hop IP for this route. If not known, {@link Route#UNSET_ROUTE_NEXT_HOP_IP} must be
   * returned.
   */
  @JsonProperty(PROP_NEXT_HOP_IP)
  public final @Nonnull Ip getNextHopIp() {
    return LegacyNextHops.getNextHopIp(_nextHop).orElse(Route.UNSET_ROUTE_NEXT_HOP_IP);
  }

  /**
   * The generic {@link NextHop} for this route. Preferred method to reason about next hops, as
   * opposed to the legacy {@link #getNextHopIp()}} or {@link #getNextHopInterface()} methods.
   */
  @JsonIgnore
  public final @Nonnull NextHop getNextHop() {
    return _nextHop;
  }

  /**
   * Returns {@code true} if this route is non-forwarding, i.e., it can be installed in the main RIB
   * but not the FIB.
   */
  @JsonIgnore
  public final boolean getNonForwarding() {
    return _nonForwarding;
  }

  /** Check if this route is "non-routing", i.e., should not be installed in the main RIB. */
  @JsonIgnore
  public final boolean getNonRouting() {
    return _nonRouting;
  }

  @JsonIgnore
  public abstract RoutingProtocol getProtocol();

  /** Return the route's tag or {@link Route#UNSET_ROUTE_TAG} if no tag is present */
  @JsonProperty(PROP_TAG)
  public final long getTag() {
    return _tag;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName()
        + "<"
        + _network
        + ",nhip:"
        + getNextHopIp()
        + ",nhint:"
        + getNextHopInterface()
        + ">";
  }

  /** Return a {@link AbstractRouteBuilder} pre-populated with the values for this route. */
  public abstract AbstractRouteBuilder<?, ?> toBuilder();
}
