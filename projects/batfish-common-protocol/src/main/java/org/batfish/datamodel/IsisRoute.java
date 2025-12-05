package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;

/** IS-IS route */
public class IsisRoute extends AbstractRoute {

  public static class Builder extends AbstractRouteBuilder<Builder, IsisRoute> {

    private String _area;
    private boolean _attach;
    private boolean _down;
    private IsisLevel _level;
    private boolean _overload;
    private RoutingProtocol _protocol;
    private String _systemId;

    @Override
    public @Nonnull IsisRoute build() {
      return new IsisRoute(
          getAdmin(),
          requireNonNull(_area),
          _attach,
          _down,
          requireNonNull(_level),
          getMetric(),
          requireNonNull(getNetwork()),
          requireNonNull(_nextHop),
          _overload,
          requireNonNull(_protocol),
          requireNonNull(_systemId),
          getTag(),
          getNonForwarding(),
          getNonRouting());
    }

    @Override
    protected @Nonnull Builder getThis() {
      return this;
    }

    public Builder setArea(@Nonnull String area) {
      _area = area;
      return this;
    }

    public Builder setAttach(boolean attach) {
      _attach = attach;
      return this;
    }

    public Builder setDown(boolean down) {
      _down = down;
      return this;
    }

    public Builder setLevel(@Nonnull IsisLevel level) {
      _level = level;
      return this;
    }

    public Builder setOverload(boolean overload) {
      _overload = overload;
      return this;
    }

    public Builder setProtocol(@Nonnull RoutingProtocol protocol) {
      _protocol = protocol;
      return this;
    }

    public Builder setSystemId(@Nonnull String systemId) {
      _systemId = systemId;
      return this;
    }
  }

  /** Return a route builder with pre-filled mandatory values. To be used in tests only */
  @VisibleForTesting
  public static IsisRoute.Builder testBuilder() {
    return builder()
        .setNextHop(NextHopDiscard.instance())
        .setAdmin(115)
        .setArea("49.0001")
        .setSystemId("1921.6800.1077")
        .setProtocol(RoutingProtocol.ISIS_L1);
  }

  /** Default Isis route metric, unless one is explicitly specified */
  public static final long DEFAULT_METRIC = 10L;

  private static final String PROP_AREA = "area";
  private static final String PROP_ATTACH = "attach";
  private static final String PROP_DOWN = "down";
  private static final String PROP_LEVEL = "level";
  private static final String PROP_OVERLOAD = "overload";
  private static final String PROP_SYSTEM_ID = "systemId";

  @JsonCreator
  private static @Nonnull IsisRoute createIsisRoute(
      @JsonProperty(PROP_ADMINISTRATIVE_COST) long administrativeCost,
      @JsonProperty(PROP_AREA) String area,
      @JsonProperty(PROP_ATTACH) boolean attach,
      @JsonProperty(PROP_DOWN) boolean down,
      @JsonProperty(PROP_LEVEL) IsisLevel level,
      @JsonProperty(PROP_METRIC) long metric,
      @JsonProperty(PROP_NETWORK) Prefix network,
      @JsonProperty(PROP_NEXT_HOP_IP) Ip nextHopIp,
      @JsonProperty(PROP_OVERLOAD) boolean overload,
      @JsonProperty(PROP_PROTOCOL) RoutingProtocol protocol,
      @JsonProperty(PROP_SYSTEM_ID) String systemId,
      @JsonProperty(PROP_TAG) long tag) {
    return new IsisRoute(
        administrativeCost,
        requireNonNull(area),
        attach,
        down,
        requireNonNull(level),
        metric,
        requireNonNull(network),
        NextHop.legacyConverter(null, nextHopIp),
        overload,
        requireNonNull(protocol),
        requireNonNull(systemId),
        tag,
        false,
        false);
  }

  private final String _area;

  private final boolean _attach;

  private final boolean _down;

  private final IsisLevel _level;

  private final long _metric;

  private final boolean _overload;

  private final RoutingProtocol _protocol;

  private final String _systemId;

  private IsisRoute(
      long administrativeCost,
      @Nonnull String area,
      boolean attach,
      boolean down,
      @Nonnull IsisLevel level,
      long metric,
      @Nonnull Prefix network,
      @Nonnull NextHop nextHop,
      boolean overload,
      @Nonnull RoutingProtocol protocol,
      @Nonnull String systemId,
      long tag,
      boolean nonForwarding,
      boolean nonRouting) {
    super(network, administrativeCost, tag, nonRouting, nonForwarding);
    _area = area;
    _attach = attach;
    _down = down;
    _level = level;
    _metric = metric;
    _nextHop = nextHop;
    _overload = overload;
    _protocol = protocol;
    _systemId = systemId;
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonProperty(PROP_AREA)
  public @Nonnull String getArea() {
    return _area;
  }

  /** Attach bit is set on default route originated by L1L2 routers to L1 neighbors. */
  @JsonProperty(PROP_ATTACH)
  public boolean getAttach() {
    return _attach;
  }

  /**
   * A "down" bit indicating that this route has already been leaked from level 2 down to level 1.
   */
  @JsonProperty(PROP_DOWN)
  public boolean getDown() {
    return _down;
  }

  @JsonProperty(PROP_LEVEL)
  public @Nonnull IsisLevel getLevel() {
    return _level;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_METRIC)
  @Override
  public @Nonnull long getMetric() {
    return _metric;
  }

  /** Overload bit indicates this route came through an overloaded interface level. */
  @JsonProperty(PROP_OVERLOAD)
  public boolean getOverload() {
    return _overload;
  }

  @JsonIgnore(false)
  @JsonProperty(PROP_PROTOCOL)
  @Override
  public @Nonnull RoutingProtocol getProtocol() {
    return _protocol;
  }

  @JsonProperty(PROP_SYSTEM_ID)
  public @Nonnull String getSystemId() {
    return _systemId;
  }

  /////// Keep #toBuilder, #equals, and #hashCode in sync ////////

  @Override
  public Builder toBuilder() {
    return new Builder()
        .setAdmin(_admin)
        .setArea(_area)
        .setAttach(_attach)
        .setDown(_down)
        .setLevel(_level)
        .setMetric(_metric)
        .setNetwork(_network)
        .setNextHop(_nextHop)
        .setNonForwarding(getNonForwarding())
        .setNonRouting(getNonRouting())
        .setOverload(_overload)
        .setProtocol(_protocol)
        .setSystemId(_systemId)
        .setTag(_tag);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IsisRoute)) {
      return false;
    }
    IsisRoute rhs = (IsisRoute) o;
    return _admin == rhs._admin
        && _area.equals(rhs._area)
        && _attach == rhs._attach
        && _down == rhs._down
        && _level == rhs._level
        && _metric == rhs._metric
        && _network.equals(rhs._network)
        && _nextHop.equals(rhs._nextHop)
        && getNonForwarding() == rhs.getNonForwarding()
        && getNonRouting() == rhs.getNonRouting()
        && _overload == rhs._overload
        && _protocol == rhs._protocol
        && _systemId.equals(rhs._systemId)
        && _tag == rhs._tag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _admin,
        _area,
        _attach,
        _down,
        _level.ordinal(),
        _metric,
        _network,
        _nextHop,
        getNonForwarding(),
        getNonRouting(),
        _overload,
        _protocol.ordinal(),
        _systemId,
        _tag);
  }
}
