package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.RoutingProtocol.HMM;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopInterface;

/**
 * An NX-OS Host Mobility Manager (HMM) route. HMM routes are /32 routes whose prefix IP is an IP
 * address assigned on a neighbor interface of an HMM-enabled NX-OS interface.
 */
public final class HmmRoute extends AbstractRoute {

  public static final class Builder extends AbstractRouteBuilder<Builder, HmmRoute> {

    public Builder() {
      setAdmin(DEFAULT_ADMIN);
    }

    @Override
    public @Nonnull Builder setNonForwarding(boolean nonForwarding) {
      throw new UnsupportedOperationException("Cannot set nonForwarding an HmmRoute");
    }

    @Override
    public @Nonnull Builder setNonRouting(boolean nonRouting) {
      throw new UnsupportedOperationException("Cannot set nonForwarding an HmmRoute");
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    @Override
    public @Nonnull HmmRoute build() {
      checkArgument(getNetwork() != null, "Missing %s", PROP_NETWORK);
      checkArgument(_nextHop != null, "Missing %s", PROP_NEXT_HOP);

      checkArgument(
          getNetwork().getPrefixLength() == 32,
          "Invalid prefix length for HmmRoute: %s",
          getNetwork().getPrefixLength());
      checkArgument(
          _nextHop instanceof NextHopInterface,
          "NextHop type must be NextHopInterface, but was: %s",
          _nextHop.getClass().getSimpleName());
      checkArgument(
          ((NextHopInterface) _nextHop).getIp() == null,
          "Cannot set non-null next-hop IP for HmmRoute");
      checkArgument(
          getMetric() == 0L, "Cannot use non-zero metric for an HmmRoute: %s", getMetric());

      return new HmmRoute(getNetwork(), _nextHop, getAdmin(), getTag());
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  @Override
  public long getMetric() {
    return 0;
  }

  // TODO: Serailize NextHop instead of legacy fields in AbstractRoute
  @JsonProperty(PROP_NEXT_HOP)
  private @Nonnull NextHop getNextHopJson() {
    return _nextHop;
  }

  @Override
  public @Nonnull RoutingProtocol getProtocol() {
    return HMM;
  }

  @SuppressWarnings("unused")
  @JsonCreator
  private static @Nonnull HmmRoute create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NEXT_HOP) @Nullable NextHop nextHop,
      @JsonProperty(PROP_ADMINISTRATIVE_COST) int admin,
      @JsonProperty(PROP_TAG) long tag,
      // unused
      // TODO: stop dumping legacy fields via final JsonProperty getters
      @JsonProperty(PROP_NEXT_HOP_INTERFACE) @Nullable String nextHopInterface,
      @JsonProperty(PROP_NEXT_HOP_IP) @Nullable Ip nextHopIp) {
    checkArgument(network != null, "Missing %s", PROP_NETWORK);
    checkArgument(nextHop != null, "Missing %s", PROP_NEXT_HOP);
    return builder().setNetwork(network).setNextHop(nextHop).setAdmin(admin).setTag(tag).build();
  }

  private HmmRoute(Prefix network, NextHop nextHop, int admin, long tag) {
    super(network, admin, tag, false, false);
    _nextHop = nextHop;
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return builder().setNetwork(_network).setNextHop(_nextHop).setAdmin(_admin).setTag(_tag);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof HmmRoute)) {
      return false;
    }
    HmmRoute that = (HmmRoute) o;
    // Ordered by likelihood to mismatch
    return (_hashCode == that._hashCode || _hashCode == 0 || that._hashCode == 0)
        && _nextHop.equals(that._nextHop)
        && _network.equals(that._network)
        && _tag == that._tag
        && _admin == that._admin;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = _admin;
      h = h * 31 + _network.hashCode();
      h = h * 31 + _nextHop.hashCode();
      h = h * 31 + Long.hashCode(_tag);

      _hashCode = h;
    }
    return h;
  }

  // Seen in lab on NX-OS devices
  private static final int DEFAULT_ADMIN = 190;

  private static final String PROP_NEXT_HOP = "nextHop";

  /* Cache the hashcode */
  private transient int _hashCode = 0;
}
