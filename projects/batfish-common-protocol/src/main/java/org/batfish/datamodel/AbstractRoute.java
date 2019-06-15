package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A base class for all types of routes supported in the dataplane computation, making this the most
 * general route type available. "Main" non-protocol-specific RIBs store and reason about this type
 * of route.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@ParametersAreNonnullByDefault
public abstract class AbstractRoute implements AbstractRouteDecorator, Serializable {

  private static final long serialVersionUID = 1L;

  /** Indicates a route has no tag associated with it */
  public static final int NO_TAG = -1;

  static final String PROP_ADMINISTRATIVE_COST = "administrativeCost";
  public static final String PROP_METRIC = "metric";
  static final String PROP_NETWORK = "network";
  static final String PROP_NEXT_HOP_INTERFACE = "nextHopInterface";
  static final String PROP_NEXT_HOP_IP = "nextHopIp";
  static final String PROP_NON_ROUTING = "nonRouting";
  static final String PROP_NON_FORWARDING = "nonForwarding";
  static final String PROP_PROTOCOL = "protocol";
  static final String PROP_TAG = "tag";

  @Nonnull protected final Prefix _network;
  protected final int _admin;
  private final boolean _nonRouting;
  private final boolean _nonForwarding;

  @JsonCreator
  protected AbstractRoute(
      @Nullable Prefix network, int admin, boolean nonRouting, boolean nonForwarding) {
    checkArgument(network != null, "Cannot create a route without a %s", PROP_NETWORK);
    checkArgument(admin >= 0, "Invalid admin distance for a route: %d", admin);
    _network = network;
    _admin = admin;
    _nonForwarding = nonForwarding;
    _nonRouting = nonRouting;
  }

  /** Backwards compatible API */
  protected AbstractRoute(@Nonnull Prefix network) {
    this(network, 1, false, false);
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
  @JsonIgnore
  public abstract String getNextHopInterface();

  /**
   * Next hop IP for this route. If not known, {@link Route#UNSET_ROUTE_NEXT_HOP_IP} must be
   * returned.
   */
  @JsonIgnore
  public abstract Ip getNextHopIp();

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

  /** Return the route's tag or {@link #NO_TAG} if no tag is present */
  @JsonIgnore
  public abstract int getTag();

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
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
