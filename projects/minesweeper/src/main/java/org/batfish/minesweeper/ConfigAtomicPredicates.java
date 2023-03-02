package org.batfish.minesweeper;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.aspath.RoutePolicyStatementAsPathCollector;
import org.batfish.minesweeper.communities.RoutePolicyStatementVarCollector;
import org.batfish.minesweeper.utils.Tuple;

/**
 * This class computes the community-regex and AS-path-regex atomic predicates for a single router
 * configuration.
 */
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
    this(batfish, snapshot, router, communities, asPathRegexes, null);
  }

  /**
   * Compute atomic predicates for the given router's configuration.
   *
   * @param batfish the batfish object
   * @param snapshot the current snapshot
   * @param router the name of the router whose configuration is being analyzed
   * @param communities additional community regexes to track, from user-defined constraints
   * @param asPathRegexes additional as-path regexes to track, from user-defined constraints
   * @param policies the set of policies to create AtomicPredicates for
   */
  public ConfigAtomicPredicates(
      IBatfish batfish,
      NetworkSnapshot snapshot,
      String router,
      @Nullable Set<CommunityVar> communities,
      @Nullable Set<String> asPathRegexes,
      @Nullable Collection<RoutingPolicy> policies) {
    this(
        batfish,
        snapshot,
        null,
        router,
        communities,
        asPathRegexes,
        policies == null
            ? batfish.loadConfigurations(snapshot).get(router).getRoutingPolicies().values()
            : policies,
        null);
  }

  /**
   * Compute atomic predicates for the given router's configuration.
   *
   * @param batfish the batfish object
   * @param snapshot the current snapshot
   * @param reference the reference snapshot - can be null if this is not called from a differential
   *     question, such as SRP.
   * @param router the name of the router whose configuration is being analyzed
   * @param communities additional community regexes to track, from user-defined constraints
   * @param asPathRegexes additional as-path regexes to track, from user-defined constraints
   * @param policies the set of policies to create AtomicPredicates for
   * @param referencePolicies the set of policies in the reference snapshot to create
   *     AtomicPredicates for
   */
  public ConfigAtomicPredicates(
      IBatfish batfish,
      NetworkSnapshot snapshot,
      @Nullable NetworkSnapshot reference,
      String router,
      @Nullable Set<CommunityVar> communities,
      @Nullable Set<String> asPathRegexes,
      @Nonnull Collection<RoutingPolicy> policies,
      @Nullable Collection<RoutingPolicy> referencePolicies) {
    Configuration configuration = batfish.loadConfigurations(snapshot).get(router);
    Configuration referenceConfiguration = null;
    if (reference != null) {
      referenceConfiguration = batfish.loadConfigurations(reference).get(router);
    }

    // Gather the communities from both (if differential) configs + any user provided communities.
    Set<CommunityVar> allCommunities = findAllCommunities(communities, policies, configuration);

    if (reference != null) {
      allCommunities.addAll(
          findAllCommunities(Collections.emptySet(), referencePolicies, referenceConfiguration));
    }

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
    _nonStandardCommunityLiterals = new HashMap<>();
    for (int i = 0; i < nonStandardCommunityVars.length; i++) {
      _nonStandardCommunityLiterals.put(i + numAPs, nonStandardCommunityVars[i]);
    }

    // Collect as path regexes from both (if differential) configs
    Set<SymbolicAsPathRegex> asPathAps =
        new HashSet<>(findAllAsPathRegexes(asPathRegexes, policies, configuration));
    if (reference != null) {
      asPathAps.addAll(
          findAllAsPathRegexes(Collections.emptySet(), referencePolicies, referenceConfiguration));
    }
    _asPathRegexAtomicPredicates = new AsPathRegexAtomicPredicates(ImmutableSet.copyOf(asPathAps));
  }

  public ConfigAtomicPredicates(ConfigAtomicPredicates other) {
    _standardCommunityAtomicPredicates =
        new RegexAtomicPredicates<>(other._standardCommunityAtomicPredicates);
    _nonStandardCommunityLiterals = new HashMap<>(other._nonStandardCommunityLiterals);
    _asPathRegexAtomicPredicates =
        new AsPathRegexAtomicPredicates(other._asPathRegexAtomicPredicates);
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
   * Identifies all of the AS-path regexes in the given routing policies. An optional set of
   * additional AS-path regexes is also included, which is used to support user-specified AS-path
   * constraints for symbolic analysis.
   */
  private static Set<SymbolicAsPathRegex> findAllAsPathRegexes(
      @Nullable Set<String> asPathRegexes,
      Collection<RoutingPolicy> policies,
      Configuration configuration) {
    ImmutableSet.Builder<SymbolicAsPathRegex> builder = ImmutableSet.builder();

    builder.addAll(findAsPathRegexes(policies, configuration));
    if (asPathRegexes != null) {
      builder.addAll(
          asPathRegexes.stream()
              .map(SymbolicAsPathRegex::new)
              .collect(ImmutableSet.toImmutableSet()));
    }
    return builder.build();
  }

  /**
   * Collect all AS-path regexes that appear in the given policy
   *
   * @param policy the policy to collect AS-path regexes from
   * @param configuration the batfish configuration
   * @return a set of symbolic AS path regexes.
   */
  private static Set<SymbolicAsPathRegex> findAsPathRegexes(
      RoutingPolicy policy, Configuration configuration) {
    Set<SymbolicAsPathRegex> asPathRegexes = new HashSet<>();
    List<Statement> stmts = policy.getStatements();
    stmts.forEach(
        stmt ->
            asPathRegexes.addAll(
                stmt.accept(
                    new RoutePolicyStatementAsPathCollector(),
                    new Tuple<>(
                        new HashSet<>(Collections.singleton(policy.getName())), configuration))));
    return asPathRegexes;
  }

  /**
   * Collect up all AS-path regexes that appear in the given policies.
   *
   * @param policies the set of policies to collect AS-path regexes from.
   * @param configuration the batfish configuration
   * @return a set of all AS-path regexes that appear
   */
  private static Set<SymbolicAsPathRegex> findAsPathRegexes(
      Collection<RoutingPolicy> policies, Configuration configuration) {
    Set<SymbolicAsPathRegex> asPathRegexes = new HashSet<>();

    // walk through every statement of every route policy
    policies.forEach(pol -> asPathRegexes.addAll(findAsPathRegexes(pol, configuration)));

    return asPathRegexes;
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
}
