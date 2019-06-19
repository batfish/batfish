package org.batfish.datamodel.routing_policy;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;

public class Environment {
  /**
   * Initalizes an {@link Environment} builder using a {@link Configuration} as the source of
   * several fields.
   */
  public static Builder builder(@Nonnull Configuration c) {
    ConfigurationFormat format = c.getConfigurationFormat();
    return new Builder()
        .setAsPathAccessLists(c.getAsPathAccessLists())
        .setCommunityLists(c.getCommunityLists())
        .setIpAccessLists(c.getIpAccessLists())
        .setIp6AccessLists(c.getIp6AccessLists())
        .setRouteFilterLists(c.getRouteFilterLists())
        .setRoute6FilterLists(c.getRoute6FilterLists())
        .setRoutingPolicies(c.getRoutingPolicies())
        .setUseOutputAttributes(
            format == ConfigurationFormat.JUNIPER
                || format == ConfigurationFormat.JUNIPER_SWITCH
                || format == ConfigurationFormat.FLAT_JUNIPER);
  }

  /**
   * Initalizes an {@link Environment} builder using a {@link Configuration} and a {@link
   * org.batfish.datamodel.Vrf} as the source of several fields. The vrf determines which {@link
   * BgpProcess} to put into the environment.
   */
  public static Builder builder(@Nonnull Configuration c, String vrf) {
    return builder(c).setBgpProcess(c.getVrfs().get(vrf).getBgpProcess());
  }

  public enum Direction {
    IN,
    OUT
  }

  private final Map<String, AsPathAccessList> _asPathAccessLists;

  private BgpProcess _bgpProcess;

  private boolean _buffered;

  private boolean _callExprContext;

  private boolean _callStatementContext;

  private final Map<String, CommunityList> _communityLists;

  private boolean _defaultAction;

  private String _defaultPolicy;

  private final Direction _direction;

  private boolean _error;

  private BgpRoute.Builder<?, ?> _intermediateBgpAttributes;

  private final Map<String, IpAccessList> _ipAccessLists;

  private final Map<String, Ip6AccessList> _ip6AccessLists;

  private boolean _localDefaultAction;

  private final AbstractRoute _originalRoute;

  @Nullable private final AbstractRoute6 _originalRoute6;

  private final AbstractRouteBuilder<?, ?> _outputRoute;

  private final Map<String, RoutingPolicy> _routingPolicies;

  @Nullable private final Ip _peerAddress;

  @Nullable private final Prefix _peerPrefix;

  private boolean _readFromIntermediateBgpAttributes;

  private final Map<String, Route6FilterList> _route6FilterLists;

  private final Map<String, RouteFilterList> _routeFilterLists;

  @Nullable private final String _routeSourceVrf;

  private final boolean _useOutputAttributes;

  private boolean _writeToIntermediateBgpAttributes;

  private Boolean _suppressed;

  private Environment(
      Map<String, AsPathAccessList> asPathAccessLists,
      BgpProcess bgpProcess,
      boolean buffered,
      boolean callExprContext,
      boolean callStatementContext,
      Map<String, CommunityList> communityLists,
      boolean defaultAction,
      String defaultPolicy,
      Direction direction,
      boolean error,
      BgpRoute.Builder<?, ?> intermediateBgpAttributes,
      Map<String, IpAccessList> ipAccessLists,
      Map<String, Ip6AccessList> ip6AccessLists,
      boolean localDefaultAction,
      Map<String, RoutingPolicy> routingPolicies,
      AbstractRouteDecorator originalRoute,
      @Nullable AbstractRoute6 originalRoute6,
      AbstractRouteBuilder<?, ?> outputRoute,
      @Nullable Ip peerAddress,
      @Nullable Prefix peerPrefix,
      boolean readFromIntermediateBgpAttributes,
      Map<String, Route6FilterList> route6FilterLists,
      Map<String, RouteFilterList> routeFilterLists,
      boolean useOutputAttributes,
      boolean writeToIntermediateBgpAttributes) {
    _asPathAccessLists = asPathAccessLists;
    _bgpProcess = bgpProcess;
    _buffered = buffered;
    _callExprContext = callExprContext;
    _callStatementContext = callStatementContext;
    _communityLists = communityLists;
    _defaultAction = defaultAction;
    _defaultPolicy = defaultPolicy;
    _direction = direction;
    _error = error;
    _intermediateBgpAttributes = intermediateBgpAttributes;
    _ipAccessLists = ipAccessLists;
    _ip6AccessLists = ip6AccessLists;
    _localDefaultAction = localDefaultAction;
    _routingPolicies = routingPolicies;
    _originalRoute = originalRoute == null ? null : originalRoute.getAbstractRoute();
    _originalRoute6 = originalRoute6;
    _outputRoute = outputRoute;
    _peerAddress = peerAddress;
    _peerPrefix = peerPrefix;
    _readFromIntermediateBgpAttributes = readFromIntermediateBgpAttributes;
    _route6FilterLists = route6FilterLists;
    _routeFilterLists = routeFilterLists;
    _routeSourceVrf =
        originalRoute instanceof AnnotatedRoute
            ? ((AnnotatedRoute<?>) originalRoute).getSourceVrf()
            : null;
    _useOutputAttributes = useOutputAttributes;
    _writeToIntermediateBgpAttributes = writeToIntermediateBgpAttributes;
  }

  public Map<String, AsPathAccessList> getAsPathAccessLists() {
    return _asPathAccessLists;
  }

  public BgpProcess getBgpProcess() {
    return _bgpProcess;
  }

  public boolean getBuffered() {
    return _buffered;
  }

  public boolean getCallExprContext() {
    return _callExprContext;
  }

  public boolean getCallStatementContext() {
    return _callStatementContext;
  }

  public Map<String, CommunityList> getCommunityLists() {
    return _communityLists;
  }

  public boolean getDefaultAction() {
    return _defaultAction;
  }

  public String getDefaultPolicy() {
    return _defaultPolicy;
  }

  public Direction getDirection() {
    return _direction;
  }

  public boolean getError() {
    return _error;
  }

  public BgpRoute.Builder<?, ?> getIntermediateBgpAttributes() {
    return _intermediateBgpAttributes;
  }

  public Map<String, Ip6AccessList> getIp6AccessLists() {
    return _ip6AccessLists;
  }

  public Map<String, IpAccessList> getIpAccessLists() {
    return _ipAccessLists;
  }

  public boolean getLocalDefaultAction() {
    return _localDefaultAction;
  }

  public Map<String, RoutingPolicy> getRoutingPolicies() {
    return _routingPolicies;
  }

  public AbstractRoute getOriginalRoute() {
    return _originalRoute;
  }

  @Nullable
  public AbstractRoute6 getOriginalRoute6() {
    return _originalRoute6;
  }

  public AbstractRouteBuilder<?, ?> getOutputRoute() {
    return _outputRoute;
  }

  @Nullable
  public Ip getPeerAddress() {
    return _peerAddress;
  }

  @Nullable
  public Prefix getPeerPrefix() {
    return _peerPrefix;
  }

  public boolean getReadFromIntermediateBgpAttributes() {
    return _readFromIntermediateBgpAttributes;
  }

  public Map<String, Route6FilterList> getRoute6FilterLists() {
    return _route6FilterLists;
  }

  public Map<String, RouteFilterList> getRouteFilterLists() {
    return _routeFilterLists;
  }

  public String getRouteSourceVrf() {
    return _routeSourceVrf;
  }

  public boolean getUseOutputAttributes() {
    return _useOutputAttributes;
  }

  public boolean getWriteToIntermediateBgpAttributes() {
    return _writeToIntermediateBgpAttributes;
  }

  public void setBuffered(boolean buffered) {
    _buffered = buffered;
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

  public void setReadFromIntermediateBgpAttributes(boolean readFromIntermediateBgpAttributes) {
    _readFromIntermediateBgpAttributes = readFromIntermediateBgpAttributes;
  }

  public void setWriteToIntermediateBgpAttributes(boolean writeToIntermediateBgpAttributes) {
    _writeToIntermediateBgpAttributes = writeToIntermediateBgpAttributes;
  }

  public static final class Builder {
    private Map<String, AsPathAccessList> _asPathAccessLists;
    private BgpProcess _bgpProcess;
    private boolean _buffered;
    private boolean _callExprContext;
    private boolean _callStatementContext;
    private Map<String, CommunityList> _communityLists;
    private boolean _defaultAction;
    private String _defaultPolicy;
    private Direction _direction;
    private boolean _error;
    private BgpRoute.Builder<?, ?> _intermediateBgpAttributes;
    private Map<String, Ip6AccessList> _ip6AccessLists;
    private Map<String, IpAccessList> _ipAccessLists;
    private boolean _localDefaultAction;
    private Map<String, RoutingPolicy> _routingPolicies;
    private AbstractRouteDecorator _originalRoute;
    private AbstractRoute6 _originalRoute6;
    private AbstractRouteBuilder<?, ?> _outputRoute;
    @Nullable private Ip _peerAddress;
    @Nullable private Prefix _peerPrefix;
    private boolean _readFromIntermediateBgpAttributes;
    private Map<String, Route6FilterList> _route6FilterLists;
    private Map<String, RouteFilterList> _routeFilterLists;
    private boolean _useOutputAttributes;
    private boolean _writeToIntermediateBgpAttributes;

    private Builder() {}

    public Builder setAsPathAccessLists(Map<String, AsPathAccessList> asPathAccessLists) {
      _asPathAccessLists = toImmutableMap(asPathAccessLists);
      return this;
    }

    public Builder setBgpProcess(BgpProcess bgpProcess) {
      _bgpProcess = bgpProcess;
      return this;
    }

    public Builder setBuffered(boolean buffered) {
      _buffered = buffered;
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

    public Builder setCommunityLists(Map<String, CommunityList> communityLists) {
      _communityLists = toImmutableMap(communityLists);
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

    public Builder setDirection(Direction direction) {
      _direction = direction;
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

    public Builder setIp6AccessLists(Map<String, Ip6AccessList> ip6AccessLists) {
      _ip6AccessLists = toImmutableMap(ip6AccessLists);
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

    public Builder setOriginalRoute(AbstractRouteDecorator originalRoute) {
      _originalRoute = originalRoute;
      return this;
    }

    public Builder setOriginalRoute6(AbstractRoute6 originalRoute6) {
      _originalRoute6 = originalRoute6;
      return this;
    }

    public Builder setOutputRoute(AbstractRouteBuilder<?, ?> outputRoute) {
      _outputRoute = outputRoute;
      return this;
    }

    public Builder setPeerAddress(@Nullable Ip peerAddress) {
      _peerAddress = peerAddress;
      return this;
    }

    public Builder setPeerPrefix(@Nullable Prefix peerPrefix) {
      _peerPrefix = peerPrefix;
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

    public Environment build() {
      return new Environment(
          firstNonNull(_asPathAccessLists, ImmutableMap.of()),
          _bgpProcess,
          _buffered,
          _callExprContext,
          _callStatementContext,
          firstNonNull(_communityLists, ImmutableMap.of()),
          _defaultAction,
          _defaultPolicy,
          _direction,
          _error,
          _intermediateBgpAttributes,
          firstNonNull(_ipAccessLists, ImmutableMap.of()),
          firstNonNull(_ip6AccessLists, ImmutableMap.of()),
          _localDefaultAction,
          firstNonNull(_routingPolicies, ImmutableMap.of()),
          _originalRoute,
          _originalRoute6,
          _outputRoute,
          _peerAddress,
          _peerPrefix,
          _readFromIntermediateBgpAttributes,
          firstNonNull(_route6FilterLists, ImmutableMap.of()),
          firstNonNull(_routeFilterLists, ImmutableMap.of()),
          _useOutputAttributes,
          _writeToIntermediateBgpAttributes);
    }

    public Builder setRoute6FilterLists(Map<String, Route6FilterList> route6FilterLists) {
      _route6FilterLists = toImmutableMap(route6FilterLists);
      return this;
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
