package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Comparator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A base class for all types of routes supported in the dataplane computation, making this the most
 * general route type available. "Main" non-protocol-specific RIBs store and reason about this type
 * of route.
 *
 * <p><i>Note:</i> This class implements {@link Comparable} because we put AbstractRoute in ordered
 * collections all throughout the codebase. {@link #compareTo(AbstractRoute)} has <b>NO</b> impact
 * on route preference.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
@ParametersAreNonnullByDefault
public abstract class AbstractRoute implements Serializable, Comparable<AbstractRoute> {

  private static final long serialVersionUID = 1L;

  /** Indicates a route has no tag associated with it */
  public static final int NO_TAG = -1;

  static final String PROP_ADMINISTRATIVE_COST = "administrativeCost";
  static final String PROP_METRIC = "metric";
  static final String PROP_NETWORK = "network";
  static final String PROP_NEXT_HOP_INTERFACE = "nextHopInterface";
  static final String PROP_NEXT_HOP_IP = "nextHopIp";
  static final String PROP_PROTOCOL = "protocol";
  static final String PROP_TAG = "tag";

  @Nonnull protected final Prefix _network;
  private boolean _nonRouting;
  private boolean _nonForwarding;

  @JsonCreator
  protected AbstractRoute(@Nullable @JsonProperty(PROP_NETWORK) Prefix network) {
    _network = requireNonNull(network, "Cannot create a route without a network");
  }

  @Override
  public final int compareTo(AbstractRoute rhs) {
    return Comparator.comparing(AbstractRoute::getNetwork)
        .thenComparingInt(AbstractRoute::getAdministrativeCost)
        .thenComparing(AbstractRoute::getMetric)
        .thenComparing(AbstractRoute::routeCompare)
        .thenComparing(AbstractRoute::getNextHopIp)
        .thenComparing(AbstractRoute::getNextHopInterface)
        .thenComparingInt(AbstractRoute::getTag)
        .compare(this, rhs);
  }

  @Override
  public abstract boolean equals(Object o);

  @JsonIgnore
  public abstract int getAdministrativeCost();

  @JsonIgnore
  public abstract Long getMetric();

  /** IPV4 network of this route */
  @JsonProperty(PROP_NETWORK)
  @Nonnull
  public final Prefix getNetwork() {
    return _network;
  }

  /**
   * Name of the next-hop interface for this route. If not known, {@link
   * Route#UNSET_NEXT_HOP_INTERFACE} must be returned.
   */
  @JsonIgnore
  @Nonnull
  public abstract String getNextHopInterface();

  /**
   * Next hop IP for this route. If not known, {@link Route#UNSET_ROUTE_NEXT_HOP_IP} must be
   * returned.
   */
  @JsonIgnore
  @Nonnull
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
  public abstract int hashCode();

  protected abstract String protocolRouteString();

  /**
   * Helps implement the {@link Comparable} interface. Implement this function to establish ordering
   * for a particular type of route (presumably with more properties than only {@link #_network}).
   *
   * <p>Guiding principle: comparison with routes of a different type should return 0.
   *
   * <p><b>Note</b>: this does nothing for route preference computation, that's the job of a {@link
   * GenericRib}.
   */
  public abstract int routeCompare(@Nonnull AbstractRoute rhs);

  @JsonIgnore
  public final void setNonForwarding(boolean nonForwarding) {
    _nonForwarding = nonForwarding;
  }

  @JsonIgnore
  public final void setNonRouting(boolean nonRouting) {
    _nonRouting = nonRouting;
  }

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
}
