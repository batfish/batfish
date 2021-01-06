package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;

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

  static final String PROP_ADMINISTRATIVE_COST = "administrativeCost";
  public static final String PROP_METRIC = "metric";
  static final String PROP_NETWORK = "network";
  static final String PROP_NEXT_HOP_INTERFACE = "nextHopInterface";
  static final String PROP_NEXT_HOP_IP = "nextHopIp";
  static final String PROP_PROTOCOL = "protocol";
  static final String PROP_TAG = "tag";

  @Nonnull protected final Prefix _network;
  protected final int _admin;
  private final boolean _nonRouting;
  private final boolean _nonForwarding;
  protected final long _tag;
  @Nonnull protected NextHop _nextHop = NextHopDiscard.instance();

  @JsonCreator
  protected AbstractRoute(
      @Nullable Prefix network, int admin, long tag, boolean nonRouting, boolean nonForwarding) {
    checkArgument(network != null, "Cannot create a route without a %s", PROP_NETWORK);
    checkArgument(admin >= 0, "Invalid admin distance for a route: %s", admin);
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
  public abstract Long getMetric();

  /** IPV4 network of this route */
  @JsonProperty(PROP_NETWORK)
  @Override
  @Nonnull
  public final Prefix getNetwork() {
    return _network;
  }

  /**
   * Name of the next-hop interface for this route. If not known, {@link
   * Route#UNSET_NEXT_HOP_INTERFACE} must be returned.
   */
  @JsonProperty(PROP_NEXT_HOP_INTERFACE)
  @Nonnull
  public final String getNextHopInterface() {
    return nextHopInterfaceExtractor().visit(_nextHop);
  }

  /**
   * Next hop IP for this route. If not known, {@link Route#UNSET_ROUTE_NEXT_HOP_IP} must be
   * returned.
   */
  @JsonProperty(PROP_NEXT_HOP_IP)
  @Nonnull
  public final Ip getNextHopIp() {
    return nextHopIpExtractor().visit(_nextHop);
  }

  /**
   * The generic {@link NextHop} for this route. Preferred method to reason about next hops, as
   * opposed to the legacy {@link #getNextHopIp()}} or {@link #getNextHopInterface()} methods.
   */
  @JsonIgnore
  @Nonnull
  public final NextHop getNextHop() {
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

  // Private implementation

  // Helper package methods
  @Nonnull
  static NextHopVisitor<Ip> nextHopIpExtractor() {
    return NEXT_HOP_IP_EXTRACTOR;
  }

  @Nonnull
  static NextHopVisitor<String> nextHopInterfaceExtractor() {
    return NEXT_HOP_INTERFACE_EXTRACTOR;
  }

  private static final NextHopVisitor<Ip> NEXT_HOP_IP_EXTRACTOR =
      new NextHopVisitor<Ip>() {

        @Override
        public Ip visitNextHopIp(NextHopIp nextHopIp) {
          return nextHopIp.getIp();
        }

        @Override
        public Ip visitNextHopInterface(NextHopInterface nextHopInterface) {
          return firstNonNull(nextHopInterface.getIp(), Route.UNSET_ROUTE_NEXT_HOP_IP);
        }

        @Override
        public Ip visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          return Route.UNSET_ROUTE_NEXT_HOP_IP;
        }

        @Override
        public Ip visitNextHopVrf(NextHopVrf nextHopVrf) {
          return Route.UNSET_ROUTE_NEXT_HOP_IP;
        }
      };

  private static final NextHopVisitor<String> NEXT_HOP_INTERFACE_EXTRACTOR =
      new NextHopVisitor<String>() {
        @Override
        public String visitNextHopIp(NextHopIp nextHopIp) {
          return Route.UNSET_NEXT_HOP_INTERFACE;
        }

        @Override
        public String visitNextHopInterface(NextHopInterface nextHopInterface) {
          return nextHopInterface.getInterfaceName();
        }

        @Override
        public String visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          return Interface.NULL_INTERFACE_NAME;
        }

        @Override
        public String visitNextHopVrf(NextHopVrf nextHopVrf) {
          return Route.UNSET_NEXT_HOP_INTERFACE;
        }
      };

  /** Returns the name of next VRF for a given route or {@code null} otherwise. */
  public static final NextHopVisitor<String> NEXT_VRF_EXTRACTOR =
      new NextHopVisitor<String>() {

        @Override
        @Nullable
        public String visitNextHopIp(NextHopIp nextHopIp) {
          return null;
        }

        @Override
        @Nullable
        public String visitNextHopInterface(NextHopInterface nextHopInterface) {
          return null;
        }

        @Override
        @Nullable
        public String visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
          return null;
        }

        @Override
        public String visitNextHopVrf(NextHopVrf nextHopVrf) {
          return nextHopVrf.getVrfName();
        }
      };
}
