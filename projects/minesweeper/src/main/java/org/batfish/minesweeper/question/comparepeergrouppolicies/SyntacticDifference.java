package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import static org.batfish.question.TracingHintsStripper.TRACING_HINTS_STRIPPER;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.minesweeper.collectors.RoutePolicyStatementCallCollector;
import org.batfish.minesweeper.utils.Tuple;

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

  /**
   * Strips tracing hints from a list of statements. Useful for comparing routing policies across
   * different files.
   *
   * @param stmts A list of statements
   * @return The same list of statements but with tracing information stripped.
   */
  private List<Statement> stripStatements(List<Statement> stmts) {
    return stmts.stream()
        .map(st -> st.accept(TRACING_HINTS_STRIPPER, null))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * @param p1 the first policy
   * @param p2 the second policy
   * @return true if the two policies are syntactically equal, including any sub-policies they call.
   */
  private boolean routingPolicySyntaxEq(RoutingPolicy p1, RoutingPolicy p2) {
    List<Statement> p1Stripped = stripStatements(p1.getStatements());
    if (p1Stripped.equals(stripStatements(p2.getStatements()))) {
      Set<String> callees =
          new RoutePolicyStatementCallCollector()
              .visitAll(p1Stripped, new Tuple<>(new HashSet<>(), p1.getOwner()));
      for (String callee : callees) {
        if (!routingPolicySyntaxEq(
            p1.getOwner().getRoutingPolicies().get(callee),
            p2.getOwner().getRoutingPolicies().get(callee))) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param o the object to be compared.
   * @return This comparison is a bit strange, because Statements are not comparable. To determine
   *     the ordering of two potential differences we first do a syntactic comparison between the
   *     policy in the current snapshot, the policy in the reference snapshot, the routing contexts
   *     and the names of the route-maps. If all of these are equal, the two differences are equal.
   *     We require the names to be equal too, otherwise it would be difficult for a user to track
   *     down all the route-maps with the same content but different names. Otherwise if these are
   *     not equal, we simply order the differences by name and hostname. Note that we consider the
   *     full routing context of the device, even if some definitions are not used in this routing
   *     policy. This might lead to two seemingly equal policies being considered different, but
   *     this can only cause a performance issue by running CRP more than necessary. The results are
   *     anyway deduplicated.
   */
  @Override
  public int compareTo(SyntacticDifference o) {

    if (this.getCurrentPolicy().getName().equals(o.getCurrentPolicy().getName())
        && this.getReferencePolicy().getName().equals(o.getReferencePolicy().getName())
        && routingPolicySyntaxEq(this.getCurrentPolicy(), o.getCurrentPolicy())
        && routingPolicySyntaxEq(this.getReferencePolicy(), o.getReferencePolicy())
        && this.getContext().equals(o.getContext())) {
      return 0;
    } else {
      Comparator<RoutingPolicy> policyName = Comparator.comparing(RoutingPolicy::getName);
      Comparator<RoutingPolicy> ownerName = Comparator.comparing(r -> r.getOwner().getHostname());
      return policyName.thenComparing(ownerName).compare(this._currentPolicy, o._currentPolicy);
    }
  }
}
