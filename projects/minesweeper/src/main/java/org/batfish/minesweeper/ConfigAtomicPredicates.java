package org.batfish.minesweeper;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
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
   * Atomic predicates for community literals and regexes that appear in the given configuration.
   */
  private final RegexAtomicPredicates<CommunityVar> _communityAtomicPredicates;

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

    _communityAtomicPredicates =
        new RegexAtomicPredicates<>(
            findAllCommunities(communities), CommunityVar.ALL_STANDARD_COMMUNITIES);
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

  public RegexAtomicPredicates<CommunityVar> getCommunityAtomicPredicates() {
    return _communityAtomicPredicates;
  }

  public RegexAtomicPredicates<SymbolicAsPathRegex> getAsPathRegexAtomicPredicates() {
    return _asPathRegexAtomicPredicates;
  }
}
