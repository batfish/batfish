package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.minesweeper.aspath.RoutePolicyStatementMatchCollector;
import org.batfish.minesweeper.collectors.AsPathNameBooleanExprCollector;
import org.batfish.minesweeper.collectors.RoutingPolicyStatementCommunityCollector;
import org.batfish.minesweeper.collectors.RoutingPolicyStatementRouteFilterCollector;
import org.batfish.minesweeper.utils.Tuple;

/**
 * This class describes the diff of the context (prefix-lists, community-lists, as-path lists)
 * between two configurations. This is a purely syntactic diff.
 *
 * <p>It is meant to be used via the method {@link RoutingPolicyContextDiff#differ(RoutingPolicy)}
 * which "projects" the context difference on a given routing-policy: If there are no differences in
 * the definitions used by policy {@link RoutingPolicyContextDiff#differ(RoutingPolicy)} returns
 * false, even if there are differences in other definitions that do not appear in policy.
 *
 * <p>Note: {@link RoutingPolicyContextDiff#differ(RoutingPolicy)} does not perform a recursive
 * exploration of policy calls. If you want a full syntactic difference, including the context and
 * statements of a routing policy, you should use {@link SyntacticCompare}.
 */
public final class RoutingPolicyContextDiff {
  private final Configuration _currentConfig;

  /** The set of community lists that differ between the two configurations. */
  private final Set<String> _communityListsDiff;

  /** The set of as-path access lists that differ between the two configurations. */
  private final Set<String> _asPathAccessListsDiff;

  /** The set of prefix lists that differ between the two configurations. */
  private final Set<String> _routeFilterListsDiff;

  public RoutingPolicyContextDiff(Configuration currentConfig, Configuration referenceConfig) {
    this._currentConfig = currentConfig;
    this._communityListsDiff =
        mapDiffKeys(
            currentConfig.getCommunitySetMatchExprs(), referenceConfig.getCommunitySetMatchExprs());
    this._communityListsDiff.addAll(
        mapDiffKeys(currentConfig.getCommunitySetExprs(), referenceConfig.getCommunitySetExprs()));
    this._communityListsDiff.addAll(
        mapDiffKeys(
            currentConfig.getCommunityMatchExprs(), referenceConfig.getCommunityMatchExprs()));
    this._asPathAccessListsDiff =
        mapDiffKeys(currentConfig.getAsPathAccessLists(), referenceConfig.getAsPathAccessLists());
    this._routeFilterListsDiff =
        mapDiffKeys(currentConfig.getRouteFilterLists(), referenceConfig.getRouteFilterLists());
  }

  /**
   * @param first a map from K to V
   * @param second another map from K to V
   * @return the set of keys for which the value V differs between the two maps (including keys
   *     missing from one map).
   */
  private <K, V> Set<K> mapDiffKeys(Map<K, V> first, Map<K, V> second) {
    Set<K> diff = new HashSet<>();
    Set<K> keys = new HashSet<>(first.keySet());
    keys.addAll(second.keySet());
    for (K key : keys) {
      V fv = first.get(key);
      V sv = second.get(key);
      if (!Objects.equals(fv, sv)) {
        diff.add(key);
      }
    }
    return diff;
  }

  /**
   * This method only looks through the names used by the currentConfig (and the currentPolicy).
   * This is because it assumes that the referencePolicy and currentPolicy are syntactically equal,
   * hence there is no point in also looking for references used through the referencePolicy. Note
   * that if the given policy calls other routing policies, the contexts of these won't be
   * automatically recursively checked for equality by this method. One may separately check them by
   * another call to this method; class {@link SyntacticCompare} implements this logic.
   *
   * @param currentPolicy a routing policy
   * @return for the given routing policy, find if the context differs based on the references used
   *     in the routing policy.
   */
  public boolean differ(RoutingPolicy currentPolicy) {
    // Find all the references/names used by the routing policy
    Set<String> communityNames =
        new RoutingPolicyStatementCommunityCollector()
            .visitAll(
                currentPolicy.getStatements(),
                new Tuple<>(
                    new HashSet<>(Collections.singleton(currentPolicy.getName())),
                    this._currentConfig));
    Set<String> routeFilterNames =
        new RoutingPolicyStatementRouteFilterCollector()
            .visitAll(
                currentPolicy.getStatements(),
                new Tuple<>(
                    new HashSet<>(Collections.singleton(currentPolicy.getName())),
                    this._currentConfig));
    Set<String> asPathNames =
        new RoutePolicyStatementMatchCollector<>(new AsPathNameBooleanExprCollector())
            .visitAll(
                currentPolicy.getStatements(),
                new Tuple<>(
                    new HashSet<>(Collections.singleton(currentPolicy.getName())),
                    this._currentConfig));

    // Return true if any of the names referenced in the routing policy are in the set of diffs.
    return communityNames.stream().anyMatch(_communityListsDiff::contains)
        || routeFilterNames.stream().anyMatch(_routeFilterListsDiff::contains)
        || asPathNames.stream().anyMatch(_asPathAccessListsDiff::contains);
  }

  @VisibleForTesting
  public Set<String> getCommunityListsDiff() {
    return _communityListsDiff;
  }

  @VisibleForTesting
  public Set<String> getAsPathAccessListsDiff() {
    return _asPathAccessListsDiff;
  }

  @VisibleForTesting
  public Set<String> getRouteFilterListsDiff() {
    return _routeFilterListsDiff;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RoutingPolicyContextDiff that = (RoutingPolicyContextDiff) o;

    if (!_communityListsDiff.equals(that._communityListsDiff)) {
      return false;
    }
    if (!_asPathAccessListsDiff.equals(that._asPathAccessListsDiff)) {
      return false;
    }
    return _routeFilterListsDiff.equals(that._routeFilterListsDiff);
  }

  @Override
  public int hashCode() {
    int result = _communityListsDiff.hashCode();
    result = 31 * result + _asPathAccessListsDiff.hashCode();
    result = 31 * result + _routeFilterListsDiff.hashCode();
    return result;
  }
}
