package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import static org.batfish.question.TracingHintsStripper.TRACING_HINTS_STRIPPER;

import com.google.common.collect.Comparators;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Captures pairs of current/reference routing policies that *may* differ, based on syntactic
 * equality and differences in community/as-path/prefix lists.
 */
@ParametersAreNonnullByDefault
public class SyntacticDifference implements Comparable<SyntacticDifference> {

  /** The policy for the current snapshot */
  private final RoutingPolicy _currentPolicy;

  /** The policy for the reference snapshot */
  private final RoutingPolicy _referencePolicy;

  /** The (potential) context-diff under which the two policies differ */
  private final Optional<RoutingPolicyContextDiff> _context;

  public SyntacticDifference(RoutingPolicy currentPolicy, RoutingPolicy referencePolicy) {
    this._currentPolicy = currentPolicy;
    this._referencePolicy = referencePolicy;
    this._context = Optional.empty();
  }

  public SyntacticDifference(
      RoutingPolicy currentPolicy,
      RoutingPolicy referencePolicy,
      RoutingPolicyContextDiff context) {
    this._currentPolicy = currentPolicy;
    this._referencePolicy = referencePolicy;
    this._context = Optional.of(context);
  }

  public RoutingPolicy getCurrentPolicy() {
    return _currentPolicy;
  }

  public RoutingPolicy getReferencePolicy() {
    return _referencePolicy;
  }

  /**
   * Strips tracing hints from a list of statements. Useful for comparing routing policies across
   * different files.
   *
   * @param stmts A list of statements
   * @return The same list of statements but with tracing information stripped.
   */
  private static List<Statement> stripStatements(List<Statement> stmts) {
    return stmts.stream()
        .map(st -> st.accept(TRACING_HINTS_STRIPPER, null))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Compares the strings of the statements of two routing policies. Note that this does not
   * recursively look into called statements; it assumes that if the top-level statements are equal
   * (e.g., the route-map names), any call statement is also equal. This allows for more aggressive
   * deduplication, especially in cases where there might be small differences in prefix-lists or
   * community-lists.
   */
  private static final Comparator<RoutingPolicy> ROUTING_POLICY_SYNTAX_COMPARATOR =
      (p1, p2) -> {
        List<Statement> p1Stripped = stripStatements(p1.getStatements());
        List<Statement> p2Stripped = stripStatements(p2.getStatements());
        return p1Stripped.toString().compareTo(p2Stripped.toString());
      };

  /**
   * @param o the object to be compared.
   * @return Compares the statements of the current routing policies, and if they are equal of the
   *     reference routing policies.
   */
  @Override
  public int compareTo(SyntacticDifference o) {
    return ComparisonChain.start()
        .compare(this.getCurrentPolicy(), o.getCurrentPolicy(), ROUTING_POLICY_SYNTAX_COMPARATOR)
        .compare(
            this.getReferencePolicy(), o.getReferencePolicy(), ROUTING_POLICY_SYNTAX_COMPARATOR)
        .compare(this._context, o._context, Comparators.emptiesLast(Comparator.naturalOrder()))
        .result();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SyntacticDifference that = (SyntacticDifference) o;
    return this.compareTo(that) == 0;
  }

  @Override
  public int hashCode() {
    int result = stripStatements(_currentPolicy.getStatements()).hashCode();
    result = 31 * result + stripStatements(_referencePolicy.getStatements()).hashCode();
    result = 31 * result + _context.hashCode();
    return result;
  }
}
