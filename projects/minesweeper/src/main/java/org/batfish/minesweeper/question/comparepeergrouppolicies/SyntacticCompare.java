package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.minesweeper.collectors.CalledPolicyCollector;
import org.batfish.minesweeper.utils.Tuple;

/**
 * A class that performs a syntactic comparison of two routing policies by comparing their
 * statements and the list-definitions they use. This class will recursively compare any routing
 * policy called.
 */
public final class SyntacticCompare {
  /** The configuration of a device in the current snapshot. */
  private final Configuration _currentConfig;

  /** The configuration of the same device in the reference snapshot. */
  private final Configuration _referenceConfig;

  /**
   * A cache mapping tuples of (current policy, reference policy) to a boolean indicating whether
   * they are syntactically equal
   */
  private final Map<Tuple<String, String>, Boolean> _resultsCache;

  /** A cache mapping policies in the current config to the set of policies that they calL. */
  private final Map<String, Set<String>> _callees;

  /**
   * A class comparing the context (community-lists, prefix-lists, aspath-lists) for two given
   * routing policies.
   */
  private final RoutingPolicyContextDiff _contextDiff;

  public SyntacticCompare(
      @Nonnull Configuration currentConfig, @Nonnull Configuration referenceConfig) {
    _currentConfig = currentConfig;
    _referenceConfig = referenceConfig;
    _resultsCache = new HashMap<>();
    _callees = new HashMap<>();
    _contextDiff = new RoutingPolicyContextDiff(currentConfig, referenceConfig);
  }

  public boolean areEqual(String currentPolicy, String referencePolicy) {
    Boolean result = _resultsCache.get(new Tuple<>(currentPolicy, referencePolicy));
    if (result != null) {
      return result;
    }
    RoutingPolicy cpol = _currentConfig.getRoutingPolicies().get(currentPolicy);
    RoutingPolicy rpol = _referenceConfig.getRoutingPolicies().get(referencePolicy);
    if (cpol == null) {
      throw new BatfishException("Did not find policy " + currentPolicy + " in current snapshot.");
    }
    if (rpol == null) {
      throw new BatfishException(
          "Did not find policy " + referencePolicy + " in reference snapshot.");
    }

    // First compare the list of statements of the two policies syntactically,
    // then compare their contexts, and finally recursively compare the policies they call.
    // The recursive call will compare the statements of the callees as well as their contexts.

    return cpol.getStatements().equals(rpol.getStatements())
        && !_contextDiff.differ(cpol)
        && comparePoliciesSet(getCallees(currentPolicy, _currentConfig, _callees));
  }

  /**
   * @param currentPolicies a set of routing policies
   * @return true if they are all syntactically equal in the current and reference snapshot
   */
  private boolean comparePoliciesSet(Set<String> currentPolicies) {
    for (String callee : currentPolicies) {
      if (!areEqual(callee, callee)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param policy a policy name
   * @param config the config of the device
   * @param cache a cache mapping policies to the set of policies that they call
   * @return the set of policies that the given policy calls. Also updates the given cache.
   */
  private Set<String> getCallees(
      String policy, Configuration config, Map<String, Set<String>> cache) {
    Set<String> callees = cache.get(policy);
    if (callees == null) {
      RoutingPolicy pol = config.getRoutingPolicies().get(policy);
      callees =
          new CalledPolicyCollector()
              .visitAll(pol.getStatements(), new Tuple<>(new HashSet<>(), config));
      cache.put(policy, callees);
    }
    return callees;
  }

  public Configuration getCurrentConfig() {
    return _currentConfig;
  }

  public Configuration getReferenceConfig() {
    return _referenceConfig;
  }

  public RoutingPolicyContextDiff getContextDiff() {
    return _contextDiff;
  }
}
