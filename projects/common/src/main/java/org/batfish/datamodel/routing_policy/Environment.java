package org.batfish.datamodel.routing_policy;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Predicates.alwaysFalse;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.Route.UNSET_ROUTE_NEXT_HOP_IP;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.eigrp.EigrpProcess;
import org.batfish.datamodel.routing_policy.as_path.AsPathExpr;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.trace.Tracer;

public class Environment {
  /**
   * Initializes an {@link Environment} builder using a {@link Configuration} as the source of
   * several fields.
   */
  public static Builder builder(@Nonnull Configuration c) {
    return new Builder()
        .setAsPathAccessLists(c.getAsPathAccessLists())
        .setAsPathExprs(c.getAsPathExprs())
        .setAsPathMatchExprs(c.getAsPathMatchExprs())
        .setCommunityMatchExprs(c.getCommunityMatchExprs())
        .setCommunitySetExprs(c.getCommunitySetExprs())
        .setCommunitySetMatchExprs(c.getCommunitySetMatchExprs())
        .setCommunitySets(c.getCommunitySets())
        .setIpAccessLists(c.getIpAccessLists())
        .setRouteFilterLists(c.getRouteFilterLists())
        .setRoutingPolicies(c.getRoutingPolicies())
        .setUseOutputAttributes(useOutputAttributesFor(c));
  }

  /**
   * Indicates whether simulation of route policies in the given {@link Configuration} should match
   * on the output route attributes instead of the original route attributes, based on the
   * configuration's format.
   */
  public static boolean useOutputAttributesFor(@Nonnull Configuration c) {
    ConfigurationFormat format = c.getConfigurationFormat();
    return format == ConfigurationFormat.JUNIPER
        || format == ConfigurationFormat.JUNIPER_SWITCH
        || format == ConfigurationFormat.FLAT_JUNIPER;
  }

  public enum Direction {
    IN,
    OUT
  }

  private final Map<String, AsPathAccessList> _asPathAccessLists;
  private final @Nonnull Map<String, AsPathExpr> _asPathExprs;
  private final @Nonnull Map<String, AsPathMatchExpr> _asPathMatchExprs;

  /**
   * If present, BGP properties are for this node. Aka, local properties are TAIL and remote
   * properties are HEAD.
   */
  private final @Nullable BgpSessionProperties _bgpSessionProperties;

  private boolean _callExprContext;
  private boolean _callStatementContext;
  private final Map<String, CommunityMatchExpr> _communityMatchExprs;
  private final Map<String, CommunitySetExpr> _communitySetExprs;
  private final Map<String, CommunitySetMatchExpr> _communitySetMatchExprs;
  private final Map<String, CommunitySet> _communitySets;
  private boolean _defaultAction;
  private String _defaultPolicy;
  private final @Nonnull Direction _direction;
  private final @Nullable EigrpProcess _eigrpProcess;
  private boolean _error;
  private BgpRoute.Builder<?, ?> _intermediateBgpAttributes;
  private final Map<String, IpAccessList> _ipAccessLists;
  private boolean _localDefaultAction;
  private boolean _tagExplicitlySet;

  private final @Nonnull Predicate<String> _successfulTrack;

  private final AbstractRoute _originalRoute;
  private final AbstractRouteBuilder<?, ?> _outputRoute;
  private final Map<String, RoutingPolicy> _routingPolicies;
  private boolean _readFromIntermediateBgpAttributes;
  private final Map<String, RouteFilterList> _routeFilterLists;
  private final @Nullable String _routeSourceVrf;
  private final boolean _useOutputAttributes;
  private boolean _writeToIntermediateBgpAttributes;
  private Boolean _suppressed;
  private final @Nullable Tracer _tracer;

  private Environment(
      Map<String, AsPathAccessList> asPathAccessLists,
      @Nonnull Map<String, AsPathExpr> asPathExprs,
      @Nonnull Map<String, AsPathMatchExpr> asPathMatchExprs,
      @Nullable BgpSessionProperties bgpSessionProperties,
      boolean callExprContext,
      boolean callStatementContext,
      Map<String, CommunityMatchExpr> communityMatchExprs,
      Map<String, CommunitySetExpr> communitySetExprs,
      Map<String, CommunitySetMatchExpr> communitySetMatchExprs,
      Map<String, CommunitySet> communitySets,
      boolean defaultAction,
      String defaultPolicy,
      @Nonnull Direction direction,
      @Nullable EigrpProcess eigrpProcess,
      boolean error,
      BgpRoute.Builder<?, ?> intermediateBgpAttributes,
      Map<String, IpAccessList> ipAccessLists,
      boolean localDefaultAction,
      Map<String, RoutingPolicy> routingPolicies,
      @Nonnull Predicate<String> successfulTrack,
      AbstractRouteDecorator originalRoute,
      AbstractRouteBuilder<?, ?> outputRoute,
      boolean readFromIntermediateBgpAttributes,
      Map<String, RouteFilterList> routeFilterLists,
      boolean useOutputAttributes,
      boolean writeToIntermediateBgpAttributes,
      @Nullable Tracer tracer) {
    _asPathAccessLists = asPathAccessLists;
    _asPathExprs = asPathExprs;
    _asPathMatchExprs = asPathMatchExprs;
    _bgpSessionProperties = bgpSessionProperties;
    _callExprContext = callExprContext;
    _callStatementContext = callStatementContext;
    _communityMatchExprs = communityMatchExprs;
    _communitySetExprs = communitySetExprs;
    _communitySetMatchExprs = communitySetMatchExprs;
    _communitySets = communitySets;
    _defaultAction = defaultAction;
    _defaultPolicy = defaultPolicy;
    _direction = direction;
    _eigrpProcess = eigrpProcess;
    _error = error;
    _intermediateBgpAttributes = intermediateBgpAttributes;
    _ipAccessLists = ipAccessLists;
    _localDefaultAction = localDefaultAction;
    _successfulTrack = successfulTrack;
    _routingPolicies = routingPolicies;
    _originalRoute = originalRoute == null ? null : originalRoute.getAbstractRoute();
    _outputRoute = outputRoute;
    _readFromIntermediateBgpAttributes = readFromIntermediateBgpAttributes;
    _routeFilterLists = routeFilterLists;
    _routeSourceVrf =
        originalRoute instanceof AnnotatedRoute
            ? ((AnnotatedRoute<?>) originalRoute).getSourceVrf()
            : null;
    _useOutputAttributes = useOutputAttributes;
    _writeToIntermediateBgpAttributes = writeToIntermediateBgpAttributes;
    _tracer = tracer;
  }

  public Map<String, AsPathAccessList> getAsPathAccessLists() {
    return _asPathAccessLists;
  }

  /**
   * The {@link BgpSessionProperties} representing the session <em>from</em> the remote node
   * <em>to</em> the node processing the policy. (Note direction is unintuitive for route exports.)
   */
  public @Nullable BgpSessionProperties getBgpSessionProperties() {
    return _bgpSessionProperties;
  }

  public @Nonnull Map<String, AsPathExpr> getAsPathExprs() {
    return _asPathExprs;
  }

  public @Nonnull Map<String, AsPathMatchExpr> getAsPathMatchExprs() {
    return _asPathMatchExprs;
  }

  public boolean getCallExprContext() {
    return _callExprContext;
  }

  public boolean getCallStatementContext() {
    return _callStatementContext;
  }

  public Map<String, CommunityMatchExpr> getCommunityMatchExprs() {
    return _communityMatchExprs;
  }

  public Map<String, CommunitySetExpr> getCommunitySetExprs() {
    return _communitySetExprs;
  }

  public Map<String, CommunitySetMatchExpr> getCommunitySetMatchExprs() {
    return _communitySetMatchExprs;
  }

  public Map<String, CommunitySet> getCommunitySets() {
    return _communitySets;
  }

  public boolean getDefaultAction() {
    return _defaultAction;
  }

  public String getDefaultPolicy() {
    return _defaultPolicy;
  }

  public @Nonnull Direction getDirection() {
    return _direction;
  }

  public @Nullable EigrpProcess getEigrpProcess() {
    return _eigrpProcess;
  }

  public boolean getError() {
    return _error;
  }

  public BgpRoute.Builder<?, ?> getIntermediateBgpAttributes() {
    return _intermediateBgpAttributes;
  }

  public Map<String, IpAccessList> getIpAccessLists() {
    return _ipAccessLists;
  }

  public boolean getLocalDefaultAction() {
    return _localDefaultAction;
  }

  /**
   * Returns the BGP local AS for the current environment. Returns {@link Optional#empty()} if there
   * are no {@link BgpSessionProperties}.
   */
  public Optional<Long> getLocalAs() {
    return Optional.ofNullable(_bgpSessionProperties).map(BgpSessionProperties::getLocalAs);
  }

  /**
   * Returns the BGP local IP for the current environment. Returns {@link Optional#empty()} if there
   * are no {@link BgpSessionProperties}.
   */
  public Optional<Ip> getLocalIp() {
    return Optional.ofNullable(_bgpSessionProperties).map(BgpSessionProperties::getLocalIp);
  }

  /**
   * Returns the BGP remote AS for the current environment. Returns {@link Optional#empty()} if
   * there are no {@link BgpSessionProperties}.
   */
  public Optional<Long> getRemoteAs() {
    return Optional.ofNullable(_bgpSessionProperties).map(BgpSessionProperties::getRemoteAs);
  }

  /**
   * Returns the BGP remote IP for the current environment. Returns {@link Optional#empty()} if
   * there are no {@link BgpSessionProperties}.
   */
  public Optional<Ip> getRemoteIp() {
    return Optional.ofNullable(_bgpSessionProperties).map(BgpSessionProperties::getRemoteIp);
  }

  /** Whether the output route's tag has been explicitly set in the current routing policy */
  public boolean getTagExplicitlySet() {
    return _tagExplicitlySet;
  }

  public Map<String, RoutingPolicy> getRoutingPolicies() {
    return _routingPolicies;
  }

  /**
   * Returns {@code true} iff the named track was indicated as successful according to the predicate
   * provided by the instantiator of this {@link Environment}.
   */
  public boolean isTrackSuccessful(String trackName) {
    return _successfulTrack.test(trackName);
  }

  public AbstractRoute getOriginalRoute() {
    return _originalRoute;
  }

  public AbstractRouteBuilder<?, ?> getOutputRoute() {
    return _outputRoute;
  }

  public boolean getReadFromIntermediateBgpAttributes() {
    return _readFromIntermediateBgpAttributes;
  }

  public Map<String, RouteFilterList> getRouteFilterLists() {
    return _routeFilterLists;
  }

  public @Nullable String getRouteSourceVrf() {
    return _routeSourceVrf;
  }

  public boolean getUseOutputAttributes() {
    return _useOutputAttributes;
  }

  public boolean getWriteToIntermediateBgpAttributes() {
    return _writeToIntermediateBgpAttributes;
  }

  public @Nullable Tracer getTracer() {
    return _tracer;
  }

  public void setCallExprContext(boolean callExprContext) {
    _callExprContext = callExprContext;
  }

  public void setCallStatementContext(boolean callStatementContext) {
    _callStatementContext = callStatementContext;
  }

  public void setDefaultAction(boolean defaultAction) {
    _defaultAction = defaultAction;
  }

  public void setDefaultPolicy(String defaultPolicy) {
    _defaultPolicy = defaultPolicy;
  }

  public void setError(boolean error) {
    _error = error;
  }

  public void setIntermediateBgpAttributes(BgpRoute.Builder<?, ?> intermediateBgpAttributes) {
    _intermediateBgpAttributes = intermediateBgpAttributes;
  }

  public void setLocalDefaultAction(boolean localDefaultAction) {
    _localDefaultAction = localDefaultAction;
  }

  public void setTagExplicitlySet(boolean tagExplicitlySet) {
    _tagExplicitlySet = tagExplicitlySet;
  }

  public void setReadFromIntermediateBgpAttributes(boolean readFromIntermediateBgpAttributes) {
    _readFromIntermediateBgpAttributes = readFromIntermediateBgpAttributes;
  }

  public void setWriteToIntermediateBgpAttributes(boolean writeToIntermediateBgpAttributes) {
    _writeToIntermediateBgpAttributes = writeToIntermediateBgpAttributes;
  }

  public static final class Builder {
    private Map<String, AsPathAccessList> _asPathAccessLists;
    private Map<String, AsPathExpr> _asPathExprs;
    private Map<String, AsPathMatchExpr> _asPathMatchExprs;
    private @Nullable BgpSessionProperties _bgpSessionProperties;
    private boolean _callExprContext;
    private boolean _callStatementContext;
    private Map<String, CommunityMatchExpr> _communityMatchExprs;
    private Map<String, CommunitySetExpr> _communitySetExprs;
    private Map<String, CommunitySetMatchExpr> _communitySetMatchExprs;
    private Map<String, CommunitySet> _communitySets;
    private boolean _defaultAction;
    private String _defaultPolicy;
    private @Nonnull Direction _direction = Direction.OUT;
    private @Nullable EigrpProcess _eigrpProcess;
    private boolean _error;
    private BgpRoute.Builder<?, ?> _intermediateBgpAttributes;
    private Map<String, IpAccessList> _ipAccessLists;
    private boolean _localDefaultAction;
    private Map<String, RoutingPolicy> _routingPolicies;
    private @Nullable Predicate<String> _successfulTrack;
    private AbstractRouteDecorator _originalRoute;
    private AbstractRouteBuilder<?, ?> _outputRoute;
    private boolean _readFromIntermediateBgpAttributes;
    private Map<String, RouteFilterList> _routeFilterLists;
    private boolean _useOutputAttributes;
    private boolean _writeToIntermediateBgpAttributes;
    @Nullable Tracer _tracer;

    private Builder() {}

    public Builder setAsPathAccessLists(Map<String, AsPathAccessList> asPathAccessLists) {
      _asPathAccessLists = toImmutableMap(asPathAccessLists);
      return this;
    }

    public @Nonnull Builder setAsPathExprs(Map<String, AsPathExpr> asPathExprs) {
      _asPathExprs = toImmutableMap(asPathExprs);
      return this;
    }

    public @Nonnull Builder setAsPathMatchExprs(Map<String, AsPathMatchExpr> asPathMatchExprs) {
      _asPathMatchExprs = toImmutableMap(asPathMatchExprs);
      return this;
    }

    /**
     * If populated, must be session properties for this node. Aka, local properties should be TAIL,
     * and remote properties should be HEAD.
     */
    public Builder setBgpSessionProperties(@Nullable BgpSessionProperties bgpSessionProperties) {
      _bgpSessionProperties = bgpSessionProperties;
      return this;
    }

    public Builder setCallExprContext(boolean callExprContext) {
      _callExprContext = callExprContext;
      return this;
    }

    public Builder setCallStatementContext(boolean callStatementContext) {
      _callStatementContext = callStatementContext;
      return this;
    }

    public Builder setCommunityMatchExprs(Map<String, CommunityMatchExpr> communityMatchExprs) {
      _communityMatchExprs = toImmutableMap(communityMatchExprs);
      return this;
    }

    public Builder setCommunitySetExprs(Map<String, CommunitySetExpr> communitySetExprs) {
      _communitySetExprs = toImmutableMap(communitySetExprs);
      return this;
    }

    public Builder setCommunitySetMatchExprs(
        Map<String, CommunitySetMatchExpr> communitySetMatchExprs) {
      _communitySetMatchExprs = toImmutableMap(communitySetMatchExprs);
      return this;
    }

    public Builder setCommunitySets(Map<String, CommunitySet> communitySets) {
      _communitySets = toImmutableMap(communitySets);
      return this;
    }

    public Builder setDefaultAction(boolean defaultAction) {
      _defaultAction = defaultAction;
      return this;
    }

    public Builder setDefaultPolicy(String defaultPolicy) {
      _defaultPolicy = defaultPolicy;
      return this;
    }

    public Builder setDirection(@Nonnull Direction direction) {
      _direction = direction;
      return this;
    }

    public Builder setEigrpProcess(@Nullable EigrpProcess eigrpProcess) {
      _eigrpProcess = eigrpProcess;
      return this;
    }

    public Builder setError(boolean error) {
      _error = error;
      return this;
    }

    public Builder setIntermediateBgpAttributes(BgpRoute.Builder<?, ?> intermediateBgpAttributes) {
      _intermediateBgpAttributes = intermediateBgpAttributes;
      return this;
    }

    public Builder setIpAccessLists(Map<String, IpAccessList> ipAccessLists) {
      _ipAccessLists = toImmutableMap(ipAccessLists);
      return this;
    }

    public Builder setLocalDefaultAction(boolean localDefaultAction) {
      _localDefaultAction = localDefaultAction;
      return this;
    }

    public Builder setRoutingPolicies(Map<String, RoutingPolicy> routingPolicies) {
      _routingPolicies = toImmutableMap(routingPolicies);
      return this;
    }

    public @Nonnull Builder setSuccessfulTrack(@Nonnull Predicate<String> successfulTrack) {
      _successfulTrack = successfulTrack;
      return this;
    }

    public Builder setOriginalRoute(AbstractRouteDecorator originalRoute) {
      _originalRoute = originalRoute;
      return this;
    }

    public Builder setOutputRoute(AbstractRouteBuilder<?, ?> outputRoute) {
      _outputRoute = outputRoute;
      return this;
    }

    public Builder setReadFromIntermediateBgpAttributes(boolean readFromIntermediateBgpAttributes) {
      _readFromIntermediateBgpAttributes = readFromIntermediateBgpAttributes;
      return this;
    }

    public Builder setWriteToIntermediateBgpAttributes(boolean writeToIntermediateBgpAttributes) {
      _writeToIntermediateBgpAttributes = writeToIntermediateBgpAttributes;
      return this;
    }

    public Builder setTracer(@Nullable Tracer tracer) {
      _tracer = tracer;
      return this;
    }

    public Environment build() {
      if (_originalRoute instanceof BgpRoute<?, ?>
          && _outputRoute instanceof BgpRoute.Builder<?, ?>
          && _direction == Direction.OUT) {
        assert _outputRoute.getNextHopIp() == UNSET_ROUTE_NEXT_HOP_IP;
      }
      return new Environment(
          firstNonNull(_asPathAccessLists, ImmutableMap.of()),
          firstNonNull(_asPathExprs, ImmutableMap.of()),
          firstNonNull(_asPathMatchExprs, ImmutableMap.of()),
          _bgpSessionProperties,
          _callExprContext,
          _callStatementContext,
          firstNonNull(_communityMatchExprs, ImmutableMap.of()),
          firstNonNull(_communitySetExprs, ImmutableMap.of()),
          firstNonNull(_communitySetMatchExprs, ImmutableMap.of()),
          firstNonNull(_communitySets, ImmutableMap.of()),
          _defaultAction,
          _defaultPolicy,
          _direction,
          _eigrpProcess,
          _error,
          _intermediateBgpAttributes,
          firstNonNull(_ipAccessLists, ImmutableMap.of()),
          _localDefaultAction,
          firstNonNull(_routingPolicies, ImmutableMap.of()),
          firstNonNull(_successfulTrack, alwaysFalse()),
          _originalRoute,
          _outputRoute,
          _readFromIntermediateBgpAttributes,
          firstNonNull(_routeFilterLists, ImmutableMap.of()),
          _useOutputAttributes,
          _writeToIntermediateBgpAttributes,
          _tracer);
    }

    public Builder setRouteFilterLists(Map<String, RouteFilterList> routeFilterLists) {
      _routeFilterLists = toImmutableMap(routeFilterLists);
      return this;
    }

    public Builder setUseOutputAttributes(boolean useOutputAttributes) {
      _useOutputAttributes = useOutputAttributes;
      return this;
    }
  }

  public @Nullable Boolean getSuppressed() {
    return _suppressed;
  }

  public void setSuppressed(@Nullable Boolean suppressed) {
    _suppressed = suppressed;
  }
}
