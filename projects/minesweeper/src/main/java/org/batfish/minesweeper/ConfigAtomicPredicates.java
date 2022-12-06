package org.batfish.minesweeper;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.aspath.RoutePolicyStatementAsPathCollector;
import org.batfish.minesweeper.communities.RoutePolicyStatementVarCollector;

/**
 * This class computes the community-regex and AS-path-regex atomic predicates for a single router
 * configuration.
 */
public class ConfigAtomicPredicates {

  private final Configuration _configuration;

  /**
   * Atomic predicates for standard community literals and regexes that appear in the given
   * configuration.
   */
  private final RegexAtomicPredicates<CommunityVar> _standardCommunityAtomicPredicates;

  /**
   * Each extended/large community literal that appears in the given configuration is assigned a
   * unique atomic predicate.
   */
  private final Map<Integer, CommunityVar> _nonStandardCommunityLiterals = new HashMap<>();

  /** Atomic predicates for the AS-path regexes that appear in the given configuration. */
  private final RegexAtomicPredicates<SymbolicAsPathRegex> _asPathRegexAtomicPredicates;

  /**
   * Compute atomic predicates for the given router's configuration.
   *
   * @param batfish the batfish object
   * @param snapshot the current snapshot
   * @param router the name of the router whose configuration is being analyzed
   */
  public ConfigAtomicPredicates(IBatfish batfish, NetworkSnapshot snapshot, String router) {
    this(batfish, snapshot, router, null, null);
  }

  /**
   * Compute atomic predicates for the given router's configuration.
   *
   * @param batfish the batfish object
   * @param snapshot the current snapshot
   * @param router the name of the router whose configuration is being analyzed
   * @param communities additional community regexes to track, from user-defined constraints
   * @param asPathRegexes additional as-path regexes to track, from user-defined constraints
   */
  public ConfigAtomicPredicates(
      IBatfish batfish,
      NetworkSnapshot snapshot,
      String router,
      @Nullable Set<CommunityVar> communities,
      @Nullable Set<String> asPathRegexes) {
    _configuration = batfish.loadConfigurations(snapshot).get(router);

    Set<CommunityVar> allCommunities = findAllCommunities(communities);

    // currently we only support regex matching for standard communities
    Predicate<CommunityVar> isStandardCommunity =
        cvar ->
            cvar.getType() == CommunityVar.Type.REGEX
                || cvar.getLiteralValue() instanceof StandardCommunity;

    // compute atomic predicates for all regexes and standard community literals
    _standardCommunityAtomicPredicates =
        new RegexAtomicPredicates<>(
            allCommunities.stream()
                .filter(isStandardCommunity)
                .collect(ImmutableSet.toImmutableSet()),
            CommunityVar.ALL_STANDARD_COMMUNITIES);

    // assign an atomic predicate to each extended/large community literal
    CommunityVar[] nonStandardCommunityVars =
        allCommunities.stream().filter(isStandardCommunity.negate()).toArray(CommunityVar[]::new);
    int numAPs = _standardCommunityAtomicPredicates.getNumAtomicPredicates();
    for (int i = 0; i < nonStandardCommunityVars.length; i++) {
      _nonStandardCommunityLiterals.put(i + numAPs, nonStandardCommunityVars[i]);
    }

    _asPathRegexAtomicPredicates =
        new RegexAtomicPredicates<>(
            findAllAsPathRegexes(asPathRegexes), SymbolicAsPathRegex.ALL_AS_PATHS);
  }

  /**
   * Identifies all of the community literals and regexes in the given configurations. An optional
   * set of additional community literals and regexes is also included, which is used to support
   * user-specified community constraints for symbolic analysis.
   */
  private Set<CommunityVar> findAllCommunities(@Nullable Set<CommunityVar> communities) {
    Set<CommunityVar> allCommunities = findAllCommunities();
    if (communities != null) {
      allCommunities.addAll(communities);
    }
    return allCommunities;
  }

  /**
   * Finds all community literals and regexes in the configuration by walking over all of its route
   * policies
   */
  private Set<CommunityVar> findAllCommunities() {
    Set<CommunityVar> comms = new HashSet<>();
    Configuration conf = _configuration;

    // walk through every statement of every route policy
    for (RoutingPolicy pol : conf.getRoutingPolicies().values()) {
      for (Statement stmt : pol.getStatements()) {
        comms.addAll(stmt.accept(new RoutePolicyStatementVarCollector(), conf));
      }
    }
    return comms;
  }

  /**
   * Identifies all of the AS-path regexes in the given configuration. An optional set of additional
   * AS-path regexes is also included, which is used to support user-specified AS-path constraints
   * for symbolic analysis.
   */
  private Set<SymbolicAsPathRegex> findAllAsPathRegexes(@Nullable Set<String> asPathRegexes) {
    ImmutableSet.Builder<SymbolicAsPathRegex> builder = ImmutableSet.builder();

    builder.addAll(findAsPathRegexes());
    if (asPathRegexes != null) {
      builder.addAll(
          asPathRegexes.stream()
              .map(SymbolicAsPathRegex::new)
              .collect(ImmutableSet.toImmutableSet()));
    }
    return builder.build();
  }

  /**
   * Collect up all AS-path regexes that appear in the given router's configuration.
   *
   * @return a set of all AS-path regexes that appear
   */
  private Set<SymbolicAsPathRegex> findAsPathRegexes() {
    Set<SymbolicAsPathRegex> asPathRegexes = new HashSet<>();
    Configuration conf = _configuration;
    // walk through every statement of every route policy
    for (RoutingPolicy pol : conf.getRoutingPolicies().values()) {
      for (Statement stmt : pol.getStatements()) {
        asPathRegexes.addAll(stmt.accept(new RoutePolicyStatementAsPathCollector(), conf));
      }
    }
    return asPathRegexes;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }

  public RegexAtomicPredicates<CommunityVar> getStandardCommunityAtomicPredicates() {
    return _standardCommunityAtomicPredicates;
  }

  public Map<Integer, CommunityVar> getNonStandardCommunityLiterals() {
    return _nonStandardCommunityLiterals;
  }

  public RegexAtomicPredicates<SymbolicAsPathRegex> getAsPathRegexAtomicPredicates() {
    return _asPathRegexAtomicPredicates;
  }
}
