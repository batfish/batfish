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
  private final Set<CommunityVar> _allCommunities;

  /**
   * In order to track community literals and regexes in the BDD-based analysis, we compute a set of
   * "atomic predicates" for them.
   */
  private final RegexAtomicPredicates<CommunityVar> _communityAtomicPredicates;

  /**
   * We also compute a set of atomic predicates for the AS-path regexes that appear in the given
   * configuration.
   */
  private final RegexAtomicPredicates<SymbolicAsPathRegex> _asPathRegexAtomicPredicates;

  public ConfigAtomicPredicates(IBatfish batfish, NetworkSnapshot snapshot, String router) {
    this(batfish, snapshot, router, null, null);
  }

  /**
   * Create a graph, specifying an additional set of community literals/regexes and AS-path regexes
   * to be tracked. This is used by the BDD-based analyses to support user-defined constraints on
   * symbolic route analysis (e.g., the user is interested only in routes tagged with a particular
   * community).
   */
  public ConfigAtomicPredicates(
      IBatfish batfish,
      NetworkSnapshot snapshot,
      String router,
      @Nullable Set<CommunityVar> communities,
      @Nullable Set<String> asPathRegexes) {
    _configuration = batfish.loadConfigurations(snapshot).get(router);

    _allCommunities = new HashSet<>();
    initAllCommunities(communities);
    // compute atomic predicates for the BDD-based analysis
    Set<CommunityVar> comms = _allCommunities.stream().collect(ImmutableSet.toImmutableSet());
    _communityAtomicPredicates =
        new RegexAtomicPredicates<>(comms, CommunityVar.ALL_STANDARD_COMMUNITIES);
    _asPathRegexAtomicPredicates =
        new RegexAtomicPredicates<>(
            findAllAsPathRegexes(asPathRegexes), SymbolicAsPathRegex.ALL_AS_PATHS);
  }

  /**
   * Identifies all of the AS-path regexes in the given configurations. An optional set of
   * additional AS-path regexes is also included, which is used to support user-specified AS-path
   * constraints for symbolic analysis.
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

  public RegexAtomicPredicates<CommunityVar> getCommunityAtomicPredicates() {
    return _communityAtomicPredicates;
  }

  public RegexAtomicPredicates<SymbolicAsPathRegex> getAsPathRegexAtomicPredicates() {
    return _asPathRegexAtomicPredicates;
  }

  /**
   * Identifies all of the community literals and regexes in the given configurations. An optional
   * set of additional community literals and regexes is also included, which is used to support
   * user-specified community constraints for symbolic analysis.
   */
  private void initAllCommunities(@Nullable Set<CommunityVar> communities) {
    _allCommunities.addAll(findAllCommunities());
    if (communities != null) {
      _allCommunities.addAll(communities);
    }
  }

  /*
   * Finds all uniquely mentioned community matches
   * in the configuration by walking over all of its route policies
   */
  public Set<CommunityVar> findAllCommunities() {
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
}
