package org.batfish.minesweeper;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;

@ParametersAreNonnullByDefault
public class SymbolicAsPathRegex extends SymbolicRegex implements Comparable<SymbolicAsPathRegex> {

  public static final SymbolicAsPathRegex ALL_AS_PATHS = new SymbolicAsPathRegex(".*");

  @Nonnull private static final String AS_NUM_REGEX = "(0|[1-9][0-9]*)";

  /**
   * A regex that represents the language of AS paths: a space-separated list of AS numbers,
   * starting and ending with the ^ (start-of-string) and $ (end-of-string) tokens respectively. For
   * non-empty AS-path regexes we require two ^ characters at the front. This is because of the way
   * that Juniper AS-path regexes are translated to Java. For example, "^80$" is translated to "^(^|
   * )80$". Because the automaton library treats ^ as a regular character we have to deal with the
   * fact that there can be two of them at the front. We will therefore arrange that there are
   * _always_ two of them at the front, regardless of what the original Java regex looks like (see
   * toAutomaton below), so we can properly compare regexes to one another.
   *
   * <p>Note: in general an AS path is a list of *sets* of AS numbers. but the format of regexes
   * over sets is apparently vendor-dependent. for now we do not support them.
   */
  @Nonnull
  private static final String AS_PATH_REGEX =
      // the empty AS-path
      "^^$"
          + "|"
          // non-empty AS-paths
          + "^^"
          + "("
          + AS_NUM_REGEX
          + " "
          + ")*"
          + AS_NUM_REGEX
          + "$";

  /**
   * When converting an AS path regex to an automaton (see toAutomaton()), we intersect with this
   * automaton, which represents the language of AS paths. Doing so serves several purposes. First,
   * it is necessary for correctness of the symbolic analysis. For example, a regex like ".*" does
   * not actually match any possible string since AS paths cannot be arbitrary strings. Second, it
   * addresses the issue of different formats for Java regexes mentioned above. Third, it ensures
   * that when we solve for AS paths that match regexes, we will get examples that are sensible and
   * also able to be parsed by Batfish.
   */
  @Nonnull private static final Automaton AS_PATH_FSM = new RegExp(AS_PATH_REGEX).toAutomaton();

  public SymbolicAsPathRegex(String regex) {
    super(regex);
  }

  /**
   * Construct a symbolic representation of an {@link AsSetsMatchingRanges} expression, which
   * represents certain common kinds of AS-path regexes.
   *
   * @param asExpr the AS-path expression
   */
  public SymbolicAsPathRegex(AsSetsMatchingRanges asExpr) {
    super(toRegex(asExpr));
  }

  // Produce a regex for the automaton library that is equivalent to the given AS path expression.
  private static String toRegex(AsSetsMatchingRanges asExpr) {
    boolean start = asExpr.getAnchorStart();
    boolean end = asExpr.getAnchorEnd();
    List<Range<Long>> ranges = asExpr.getAsRanges();
    String pre = start ? "^" : "(^| )";
    String post = end ? "$" : "( |$)";
    String asns =
        ranges.stream().map(SymbolicAsPathRegex::toRegex).collect(Collectors.joining(" "));
    return pre + asns + post;
  }

  // Produce a regex for the automaton library that is equivalent to the given range of longs.
  private static String toRegex(Range<Long> r) {
    checkArgument(
        r.hasLowerBound() && r.hasUpperBound(),
        "Unexpected unbounded ASN range in an AS-path expression");
    long lower = r.lowerBoundType() == BoundType.CLOSED ? r.lowerEndpoint() : r.lowerEndpoint() + 1;
    long upper = r.upperBoundType() == BoundType.CLOSED ? r.upperEndpoint() : r.upperEndpoint() - 1;
    return toRegex(lower, upper);
  }

  // Produce a regex for the automaton library that represents a closed range from lower to upper.
  // Since these numbers represent AS numbers, they are assumed to be no larger than 2^32 - 1.
  @VisibleForTesting
  static String toRegex(long lower, long upper) {
    if (lower == upper) {
      return String.valueOf(lower);
    } else if (upper <= Integer.MAX_VALUE) {
      return toRegexRange(String.valueOf(lower), String.valueOf(upper));
    } else {
      // handle the case when the given interval is outside the range of Java integers
      List<String> disjuncts = new ArrayList<>();
      long currLower = lower;
      if (currLower <= Integer.MAX_VALUE) {
        // make a range expression for the part of the interval that is in the range of Java
        // integers
        disjuncts.add(toRegexRange(String.valueOf(currLower), String.valueOf(Integer.MAX_VALUE)));
        currLower = Integer.MAX_VALUE + 1L;
      }
      // now lower and upper are both beyond the range of Java ints
      // therefore they also have the same length in decimal
      String lowerS = String.valueOf(currLower);
      String upperS = String.valueOf(upper);
      // create a separate range expression for each interval of values that all start with the same
      // decimal number
      while (lowerS.charAt(0) < upperS.charAt(0)) {
        disjuncts.add(
            lowerS.charAt(0)
                + toRegexRange(lowerS.substring(1), stringCopies("9", lowerS.length() - 1)));
        lowerS =
            (Character.getNumericValue(lowerS.charAt(0)) + 1)
                + stringCopies("0", lowerS.length() - 1);
      }
      // one last range expression for the remaining values up to and including upper
      disjuncts.add(lowerS.charAt(0) + toRegexRange(lowerS.substring(1), upperS.substring(1)));

      // OR all of these range expressions together
      return "("
          + disjuncts.stream().map(s -> "(" + s + ")").collect(Collectors.joining("|"))
          + ")";
    }
  }

  private static String toRegexRange(String lower, String upper) {
    return "<" + lower + "-" + upper + ">";
  }

  private static String stringCopies(String s, int n) {
    return IntStream.range(0, n).mapToObj(i -> s).collect(Collectors.joining());
  }

  /**
   * Construct a single symbolic as-path regex that represents the union of a given collection of
   * such regexes. The collection of regexes is assumed to be non-empty
   *
   * @param regexes the regexes to union
   * @return a regex representing the union of the given regexes
   */
  public static SymbolicAsPathRegex union(Collection<SymbolicAsPathRegex> regexes) {
    checkArgument(!regexes.isEmpty());
    String regex =
        regexes.stream().map(r -> "(" + r.getRegex() + ")").collect(Collectors.joining("|"));
    return new SymbolicAsPathRegex(regex);
  }

  /**
   * Convert this community variable into an equivalent finite-state automaton.
   *
   * @return the automaton
   */
  @Override
  public Automaton toAutomaton() {
    /**
     * A regex need only match a portion of a given AS-path string. For example, the regex "_40_"
     * matches AS paths that contain the AS number 40 anywhere. But to properly relate AS paths to
     * one another, for example to find their intersection, we need regexes that match completely.
     *
     * <p>The simple approach below converts a possibly-partial regex into a complete one. It works
     * because below we intersect the resulting automaton with AS_PATH_FSM, which notably includes
     * the start-of-string and end-of-string characters. Note that the automaton library treats
     * these as ordinary characters.
     */
    String regex = ".*" + "(" + _regex + ")" + ".*";
    return new RegExp(regex, RegExp.INTERVAL).toAutomaton().intersection(AS_PATH_FSM);
  }

  @Override
  public String toString() {
    return _regex;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SymbolicAsPathRegex)) {
      return false;
    }
    SymbolicAsPathRegex that = (SymbolicAsPathRegex) o;
    return _regex.equals(that._regex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_regex);
  }

  @Override
  public int compareTo(SymbolicAsPathRegex that) {
    return _regex.compareTo(that._regex);
  }
}
