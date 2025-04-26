package org.batfish.minesweeper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.bgp.TunnelEncapsulationAttribute;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.aspath.AsPathRegexCollector;
import org.batfish.minesweeper.aspath.RoutingPolicyCollector;
import org.batfish.minesweeper.aspath.TunnelEncapsulationAttributeCollector;
import org.batfish.minesweeper.communities.RoutePolicyStatementVarCollector;
import org.batfish.minesweeper.env.PeerAddressCollector;
import org.batfish.minesweeper.env.SourceVrfCollector;
import org.batfish.minesweeper.env.TrackCollector;
import org.batfish.minesweeper.utils.Tuple;

/**
 * This class traverses a given router configuration to find the community literals/regexes, as-path
 * regexes, tracks (see {@link org.batfish.datamodel.routing_policy.expr.TrackSucceeded}), and
 * source VRFs (see {@link org.batfish.datamodel.routing_policy.expr.MatchSourceVrf}) that it
 * contains. It uses this information to compute atomic predicates for each, which are then
 * represented by unique BDD variables in a {@link org.batfish.minesweeper.bdd.BDDRoute} in order to
 * perform the symbolic route analysis. (We don't need to compute atomic predicates for the tracks
 * and source VRFs, because they are all independent of one another, so each gets a corresponding
 * BDD variable.)
 */
@ParametersAreNonnullByDefault
public final class ConfigAtomicPredicates {

  /**
   * Atomic predicates for standard community literals and regexes that appear in the given
   * configuration.
   */
  private final RegexAtomicPredicates<CommunityVar> _standardCommunityAtomicPredicates;

  /**
   * Each extended/large community literal that appears in the given configuration is assigned a
   * unique atomic predicate.
   */
  private final Map<Integer, CommunityVar> _nonStandardCommunityLiterals;

  /** Atomic predicates for the AS-path regexes that appear in the given configuration. */
  private final AsPathRegexAtomicPredicates _asPathRegexAtomicPredicates;

  /** The list of next-hop interface names that appear in the given configuration. */
  private final List<String> _nextHopInterfaces;

  /** The list of peer IP addresses that appear in the given configuration. */
  private final List<Ip> _peerAddresses;

  /** The list of "tracks" that appear in the given configuration. */
  private final List<String> _tracks;

  /** The list of source VRFs that appear in the given configuration. */
  private final List<String> _sourceVrfs;

  private final List<TunnelEncapsulationAttribute> _tunnelEncapsulationAttributes;

  private static boolean isStandardCommunity(CommunityVar var) {
    if (var.getType() == CommunityVar.Type.REGEX) {
      // assume all regexes are on standard communities
      return true;
    }
    assert var.getType() == CommunityVar.Type.EXACT;
    return var.getLiteralValue() instanceof StandardCommunity;
  }

  /**
   * Creates a {@link ConfigAtomicPredicates} that supports all relevant constructs referenced in
   * the given policies, also incorporating the given extra communities or AS-path regexes.
   *
   * @param extraCommunities additional community regexes to track, from user-defined constraints
   * @param extraAsPathRegexes additional as-path regexes to track, from user-defined constraints
   */
  public ConfigAtomicPredicates(
      List<Map.Entry<Configuration, Collection<RoutingPolicy>>> configAndPolicies,
      Set<CommunityVar> extraCommunities,
      Set<String> extraAsPathRegexes) {
    ImmutableSet.Builder<CommunityVar> allCommunitiesB = ImmutableSet.builder();
    ImmutableSet.Builder<SymbolicAsPathRegex> allAsPathRegexesB = ImmutableSet.builder();
    Set<String> allTrackNames = new TreeSet<>();
    Set<String> allNextHopInterfaceNames = new TreeSet<>();
    Set<Ip> allPeerAddresses = new TreeSet<>();
    Set<String> allSourceVrfNames = new TreeSet<>();
    ImmutableSet.Builder<TunnelEncapsulationAttribute> allTunnelEncapsulationAttributes =
        ImmutableSet.builder();
    allCommunitiesB.addAll(extraCommunities);
    extraAsPathRegexes.forEach(s -> allAsPathRegexesB.add(new SymbolicAsPathRegex(s)));

    for (Entry<Configuration, Collection<RoutingPolicy>> pair : configAndPolicies) {
      Configuration config = pair.getKey();
      Collection<RoutingPolicy> policies = pair.getValue();
      allCommunitiesB.addAll(findAllCommunities(ImmutableSet.of(), policies, config));
      allAsPathRegexesB.addAll(findAllAsPathRegexes(ImmutableSet.of(), policies, config));
      allTrackNames.addAll(findAllTracks(policies, config));
      allNextHopInterfaceNames.addAll(findAllNextHopInterfaces(policies, config));
      allPeerAddresses.addAll(findAllPeerAddresses(policies, config));
      allSourceVrfNames.addAll(findAllSourceVrfs(policies, config));
      allTunnelEncapsulationAttributes.addAll(findAllTunnelAttributes(policies, config));
    }
    Set<CommunityVar> allCommunities = allCommunitiesB.build();

    // compute atomic predicates for all regexes and standard community literals
    _standardCommunityAtomicPredicates =
        new RegexAtomicPredicates<>(
            allCommunities.stream()
                .filter(ConfigAtomicPredicates::isStandardCommunity)
                .collect(ImmutableSet.toImmutableSet()),
            CommunityVar.ALL_STANDARD_COMMUNITIES);

    // assign an atomic predicate to each extended/large community literal
    int numAPs = _standardCommunityAtomicPredicates.getNumAtomicPredicates();
    Map<Integer, CommunityVar> nonStandardCommunityLiterals = new HashMap<>();
    for (CommunityVar var : allCommunities) {
      if (isStandardCommunity(var)) {
        continue;
      }
      assert var.getType() == CommunityVar.Type.EXACT;
      nonStandardCommunityLiterals.put(nonStandardCommunityLiterals.size() + numAPs, var);
    }

    _nonStandardCommunityLiterals = ImmutableMap.copyOf(nonStandardCommunityLiterals);
    _asPathRegexAtomicPredicates = new AsPathRegexAtomicPredicates(allAsPathRegexesB.build());
    _nextHopInterfaces = ImmutableList.copyOf(allNextHopInterfaceNames);
    _peerAddresses = ImmutableList.copyOf(allPeerAddresses);
    _tracks = ImmutableList.copyOf(allTrackNames);
    _sourceVrfs = ImmutableList.copyOf(allSourceVrfNames);
    _tunnelEncapsulationAttributes = ImmutableList.copyOf(allTunnelEncapsulationAttributes.build());
  }

  public ConfigAtomicPredicates(ConfigAtomicPredicates other) {
    _standardCommunityAtomicPredicates =
        new RegexAtomicPredicates<>(other._standardCommunityAtomicPredicates);
    _nonStandardCommunityLiterals = new HashMap<>(other._nonStandardCommunityLiterals);
    _asPathRegexAtomicPredicates =
        new AsPathRegexAtomicPredicates(other._asPathRegexAtomicPredicates);
    _nextHopInterfaces = other._nextHopInterfaces;
    _peerAddresses = other._peerAddresses;
    _tracks = other._tracks;
    _sourceVrfs = other._sourceVrfs;
    _tunnelEncapsulationAttributes = other._tunnelEncapsulationAttributes;
  }

  /**
   * Identifies all of the community literals and regexes in the given routing policies. An optional
   * set of additional community literals and regexes is also included, which is used to support
   * user-specified community constraints for symbolic analysis.
   */
  private static Set<CommunityVar> findAllCommunities(
      @Nullable Set<CommunityVar> communities,
      Collection<RoutingPolicy> policies,
      Configuration configuration) {
    Set<CommunityVar> allCommunities = findAllCommunities(policies, configuration);
    if (communities != null) {
      allCommunities.addAll(communities);
    }
    return allCommunities;
  }

  /**
   * Collect all community vars that appear in the given policy
   *
   * @param policy the policy to collect community vars from
   * @param configuration the configuration based on a given snapshot
   * @return a set of community vars
   */
  private static Set<CommunityVar> findAllCommunities(
      RoutingPolicy policy, Configuration configuration) {
    Set<CommunityVar> comms = new HashSet<>();
    List<Statement> stmts = policy.getStatements();
    stmts.forEach(
        stmt ->
            comms.addAll(
                stmt.accept(
                    new RoutePolicyStatementVarCollector(),
                    new Tuple<>(
                        new HashSet<>(Collections.singleton(policy.getName())), configuration))));
    return comms;
  }

  /**
   * Finds all community literals and regexes in the given routing policies by walking over them
   *
   * @param policies the routing policies to retrieve the community literals/regexes from.
   * @param configuration the configuration based on a given snapshot
   */
  private static Set<CommunityVar> findAllCommunities(
      Collection<RoutingPolicy> policies, Configuration configuration) {
    Set<CommunityVar> comms = new HashSet<>();

    // walk through every statement of every route policy
    policies.forEach(pol -> comms.addAll(findAllCommunities(pol, configuration)));
    return comms;
  }

  /**
   * Identifies all items of a given kind in the match expressions of the given routing policies. A
   * {@link RoutingPolicyCollector} is provided that is specific to the particular items being
   * searched for. An optional set of additional items is also included, which is used to support
   * user-specified constraints for symbolic analysis.
   */
  private static <T> Set<T> findAllMatchItems(
      Set<T> items,
      Collection<RoutingPolicy> policies,
      Configuration configuration,
      RoutingPolicyCollector<T> collector) {
    ImmutableSet.Builder<T> builder = ImmutableSet.builder();
    policies.forEach(pol -> builder.addAll(findAllMatchItems(pol, configuration, collector)));
    builder.addAll(items);
    return builder.build();
  }

  /**
   * Identifies all items of a given kind in the match expressions of the given routing policy. A
   * {@link RoutingPolicyCollector} is provided that is specific to the particular items being
   * searched for. An optional set of additional items is also included, which is used to support
   * user-specified constraints for symbolic analysis.
   */
  private static <T> Set<T> findAllMatchItems(
      RoutingPolicy policy, Configuration configuration, RoutingPolicyCollector<T> collector) {
    Set<String> visited = new HashSet<>(Collections.singleton(policy.getName()));
    return collector.visitAll(policy.getStatements(), new Tuple<>(visited, configuration));
  }

  /**
   * Identifies all of the AS-path regexes in the given routing policies. An optional set of
   * additional AS-path regexes is also included, which is used to support user-specified AS-path
   * constraints for symbolic analysis.
   */
  private static Set<SymbolicAsPathRegex> findAllAsPathRegexes(
      Set<SymbolicAsPathRegex> asPathRegexes,
      Collection<RoutingPolicy> policies,
      Configuration configuration) {
    return findAllMatchItems(asPathRegexes, policies, configuration, new AsPathRegexCollector());
  }

  /**
   * Collect up all next-hop interface names that appear in the given policies.
   *
   * @param policies the set of policies to collect interface names from.
   * @param configuration the batfish configuration
   * @return a set of all next-hop interface names that appear
   */
  private static Set<String> findAllNextHopInterfaces(
      Collection<RoutingPolicy> policies, Configuration configuration) {
    return findAllMatchItems(
        ImmutableSet.of(), policies, configuration, new NextHopInterfaceCollector());
  }

  /**
   * Collect up all peer IP addresses names that appear in the given policies.
   *
   * @param policies the set of policies to collect interface names from.
   * @param configuration the batfish configuration
   * @return a set of all peer IP addresses that appear
   */
  private static Set<Ip> findAllPeerAddresses(
      Collection<RoutingPolicy> policies, Configuration configuration) {
    return findAllMatchItems(
        ImmutableSet.of(), policies, configuration, new PeerAddressCollector());
  }

  /**
   * Collect up all tracks that appear in the given policies.
   *
   * @param policies the set of policies to collect tracks from.
   * @param configuration the batfish configuration
   * @return a set of all tracks that appear
   */
  private static Set<String> findAllTracks(
      Collection<RoutingPolicy> policies, Configuration configuration) {
    return findAllMatchItems(ImmutableSet.of(), policies, configuration, new TrackCollector());
  }

  /**
   * Collect up all source VRFs that appear in the given policies.
   *
   * @param policies the set of policies to collect source VRFs from.
   * @param configuration the batfish configuration
   * @return a set of all source VRFs that appear
   */
  private static Set<String> findAllSourceVrfs(
      Collection<RoutingPolicy> policies, Configuration configuration) {
    return findAllMatchItems(ImmutableSet.of(), policies, configuration, new SourceVrfCollector());
  }

  private static Set<TunnelEncapsulationAttribute> findAllTunnelAttributes(
      Collection<RoutingPolicy> policies, Configuration configuration) {
    TunnelEncapsulationAttributeCollector collector =
        TunnelEncapsulationAttributeCollector.instance();
    ImmutableSet.Builder<TunnelEncapsulationAttribute> attributes = ImmutableSet.builder();
    for (RoutingPolicy p : policies) {
      attributes.addAll(
          collector.visitAll(p.getStatements(), new Tuple<>(new HashSet<>(), configuration)));
    }
    return attributes.build();
  }

  public RegexAtomicPredicates<CommunityVar> getStandardCommunityAtomicPredicates() {
    return _standardCommunityAtomicPredicates;
  }

  public Map<Integer, CommunityVar> getNonStandardCommunityLiterals() {
    return _nonStandardCommunityLiterals;
  }

  public AsPathRegexAtomicPredicates getAsPathRegexAtomicPredicates() {
    return _asPathRegexAtomicPredicates;
  }

  public List<String> getNextHopInterfaces() {
    return _nextHopInterfaces;
  }

  public List<Ip> getPeerAddresses() {
    return _peerAddresses;
  }

  public List<String> getTracks() {
    return _tracks;
  }

  public List<TunnelEncapsulationAttribute> getTunnelEncapsulationAttributes() {
    return _tunnelEncapsulationAttributes;
  }

  public List<String> getSourceVrfs() {
    return _sourceVrfs;
  }
}
