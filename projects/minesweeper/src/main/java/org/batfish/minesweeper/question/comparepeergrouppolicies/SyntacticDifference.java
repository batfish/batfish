package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import static org.batfish.question.TracingHintsStripper.TRACING_HINTS_STRIPPER;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.Statement;

/**
 * Captures pairs of current/reference routing policies that *may* differ, based on syntactic
 * equality and differences in community/as-path/prefix lists.
 */
public class SyntacticDifference implements Comparable<SyntacticDifference> {

  /** The policy for the current snapshot */
  private final RoutingPolicy _currentPolicy;

  /** The policy for the reference snapshot */
  private final RoutingPolicy _referencePolicy;

  public SyntacticDifference(RoutingPolicy currentPolicy, RoutingPolicy referencePolicy) {
    this._currentPolicy = currentPolicy;
    this._referencePolicy = referencePolicy;
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
  private List<Statement> stripStatements(List<Statement> stmts) {
    return stmts.stream()
        .map(st -> st.accept(TRACING_HINTS_STRIPPER, null))
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * @param p1 the first policy
   * @param p2 the second policy
   * @return the result of comparing the strings of the statements of two routing policies. Note
   *     that this does not recursively look into called statements; it assumes that if the
   *     top-level statements are equal (e.g., the route-map names), any call statement is also
   *     equal. This allows for more aggressive deduplication, especially in cases where there might
   *     be small differences in prefix-lists or community-lists.
   */
  private int routingPolicySyntaxComparison(RoutingPolicy p1, RoutingPolicy p2) {

    List<Statement> p1Stripped = stripStatements(p1.getStatements());
    List<Statement> p2Stripped = stripStatements(p2.getStatements());
    return p1Stripped.toString().compareTo(p2Stripped.toString());
  }

  /**
   * @param o the object to be compared.
   * @return Compares the statements of the current routing policies, and if they are equal of the
   *     reference routing policies.
   */
  @Override
  public int compareTo(SyntacticDifference o) {
    int current = routingPolicySyntaxComparison(this.getCurrentPolicy(), o.getCurrentPolicy());
    if (current != 0) {
      return current;
    } else {
      return routingPolicySyntaxComparison(this.getReferencePolicy(), o.getReferencePolicy());
    }
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
    return result;
  }
}
