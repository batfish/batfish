package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import java.util.Comparator;
import org.batfish.datamodel.routing_policy.RoutingPolicy;

/**
 * Captures pairs of current/reference routing policies that *may* differ, based on syntactic
 * equality and differences in community/as-path/prefix lists.
 */
public class SyntacticDifference implements Comparable<SyntacticDifference> {

  /** The policy for the current snapshot */
  private final RoutingPolicy _currentPolicy;

  /** The policy for the reference snapshot */
  private final RoutingPolicy _referencePolicy;

  /** The (potential) context-diff under which the two policies differ */
  private final RoutingPolicyContextDiff _context;

  public SyntacticDifference(
      RoutingPolicy currentPolicy,
      RoutingPolicy referencePolicy,
      RoutingPolicyContextDiff context) {
    this._currentPolicy = currentPolicy;
    this._referencePolicy = referencePolicy;
    this._context = context;
  }

  public RoutingPolicy getCurrentPolicy() {
    return _currentPolicy;
  }

  public RoutingPolicy getReferencePolicy() {
    return _referencePolicy;
  }

  public RoutingPolicyContextDiff getContext() {
    return _context;
  }

  @Override
  public int compareTo(SyntacticDifference o) {
    Comparator<RoutingPolicy> policyName = Comparator.comparing(RoutingPolicy::getName);
    Comparator<RoutingPolicy> ownerName = Comparator.comparing(r -> r.getOwner().getHostname());
    return policyName.thenComparing(ownerName).compare(this._currentPolicy, o._currentPolicy);
  }
}
