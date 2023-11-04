package org.batfish.question.routes;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.question.routes.RoutesQuestion.RibProtocol.MAIN;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.questions.Question;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.RoutingProtocolSpecifier;
import org.batfish.specifier.SpecifierFactories;

/** Returns computed routes after dataplane computation. */
@ParametersAreNonnullByDefault
public class RoutesQuestion extends Question {

  /** RIBs of these protocols are available for examining routes using {@link RoutesQuestion}. */
  public enum RibProtocol {
    MAIN("main"),
    BGP("bgp"),
    EVPN("evpn");

    private final String _protocolName;

    private static final Map<String, RibProtocol> _map = buildMap();

    private static Map<String, RibProtocol> buildMap() {
      return Arrays.stream(RibProtocol.values())
          .collect(
              ImmutableMap.toImmutableMap(p -> p._protocolName.toLowerCase(), Function.identity()));
    }

    @JsonCreator
    private static RibProtocol fromName(String name) {
      return _map.getOrDefault(name.toLowerCase(), MAIN);
    }

    RibProtocol(String protocol) {
      _protocolName = protocol;
    }
  }

  /** How the prefix should be matched. */
  public enum PrefixMatchType {
    /* exactly match the input network; returns at 0 or 1 matching prefix */
    EXACT,
    /* longest matching prefix; returns 0 or 1 matching prefix */
    LONGEST_PREFIX_MATCH,
    /* only matches prefixes in the input network; can return multiple prefixes */
    LONGER_PREFIXES,
    /* only matches prefixes that contain the input network; can return multiple prefixes */
    SHORTER_PREFIXES
  }

  private static final String PROP_BGP_ROUTE_STATUS = "bgpRouteStatus"; // for BGP and EVPN RIBs
  private static final String PROP_NETWORK = "network";
  private static final String PROP_PREFIX_MATCH_TYPE = "prefixMatchType";
  private static final String PROP_NODES = "nodes";
  private static final String PROP_PROTOCOLS = "protocols";
  private static final String PROP_RIB = "rib";
  private static final String PROP_VRFS = "vrfs";

  private static final String QUESTION_NAME = "routes";

  private final @Nullable String _bgpRouteStatus;

  private final PrefixMatchType _prefixMatchType;

  private final @Nullable Prefix _network;

  private final @Nullable String _nodes;

  private final @Nonnull String _protocols;

  private final @Nonnull RibProtocol _rib;

  private final @Nonnull String _vrfs;

  /**
   * Create a new question.
   *
   * @param nodes {@link NodeSpecifier} indicating which nodes' RIBs should be considered
   * @param vrfs a regex pattern indicating which VRFs should be considered
   * @param rib a specific protocol RIB to return routes from.
   * @param prefixMatchType what type of matching to use; relevant only when a network is specified
   */
  @VisibleForTesting
  public RoutesQuestion(
      @Nullable Prefix network,
      @Nullable String nodes,
      @Nullable String vrfs,
      @Nullable String protocols,
      @Nullable String bgpRouteStatus,
      @Nullable RibProtocol rib,
      @Nullable PrefixMatchType prefixMatchType) {
    _network = network;
    _nodes = nodes;
    _protocols = firstNonNull(protocols, RoutingProtocolSpecifier.ALL);
    _rib = firstNonNull(rib, MAIN);
    _vrfs = firstNonNull(vrfs, ".*");
    _bgpRouteStatus = bgpRouteStatus;
    _prefixMatchType = firstNonNull(prefixMatchType, PrefixMatchType.EXACT);
  }

  @JsonCreator
  private static @Nonnull RoutesQuestion create(
      @JsonProperty(PROP_NETWORK) @Nullable Prefix network,
      @JsonProperty(PROP_NODES) @Nullable String nodes,
      @JsonProperty(PROP_VRFS) @Nullable String vrfs,
      @JsonProperty(PROP_PROTOCOLS) @Nullable String protocols,
      @JsonProperty(PROP_BGP_ROUTE_STATUS) @Nullable String bgpRouteStatus,
      @JsonProperty(PROP_RIB) @Nullable RibProtocol rib,
      @JsonProperty(PROP_PREFIX_MATCH_TYPE) @Nullable PrefixMatchType prefixMatchType) {
    return new RoutesQuestion(
        network, nodes, vrfs, protocols, bgpRouteStatus, rib, prefixMatchType);
  }

  /** Create new routes question with default parameters. */
  public RoutesQuestion() {
    this(null, null, null, null, null, null, null);
  }

  @JsonProperty(PROP_BGP_ROUTE_STATUS)
  public @Nullable String getBgpRouteStatus() {
    return _bgpRouteStatus;
  }

  @Override
  public boolean getDataPlane() {
    return true;
  }

  @JsonProperty(PROP_PREFIX_MATCH_TYPE)
  public @Nonnull PrefixMatchType getPrefixMatchType() {
    return _prefixMatchType;
  }

  @Override
  public String getName() {
    return QUESTION_NAME;
  }

  @JsonProperty(PROP_NETWORK)
  public @Nullable Prefix getNetwork() {
    return _network;
  }

  @JsonProperty(PROP_NODES)
  public @Nullable String getNodes() {
    return _nodes;
  }

  @JsonIgnore
  public @Nonnull NodeSpecifier getNodeSpecifier() {
    return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
  }

  @JsonProperty(PROP_PROTOCOLS)
  public @Nonnull String getProtocols() {
    return _protocols;
  }

  @JsonIgnore
  public @Nonnull RoutingProtocolSpecifier getRoutingProtocolSpecifier() {
    return new RoutingProtocolSpecifier(_protocols);
  }

  @JsonProperty(PROP_RIB)
  public @Nonnull RibProtocol getRib() {
    return _rib;
  }

  @JsonProperty(PROP_VRFS)
  public @Nonnull String getVrfs() {
    return _vrfs;
  }
}
