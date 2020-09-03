package org.batfish.minesweeper;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SymbolicAsPathRegex extends SymbolicRegex implements Comparable<SymbolicAsPathRegex> {

  public static final SymbolicAsPathRegex ALL_AS_PATHS = new SymbolicAsPathRegex(".*");

  @Nonnull private static final String AS_NUM_REGEX = "(0|[1-9][0-9]*)";

  // a regex that represents the language of AS paths: a space-separated list of
  // AS numbers, starting and ending with the ^ (start-of-string) and $ (end-of-string)
  // tokens respectively.
  // Note: in general an AS path is a list of *sets* of AS numbers.  but the format of
  // regexes over sets is apparently vendor-dependent.  for now we do not support them.
  @Nonnull
  private static final String AS_PATH_REGEX =
      // the empty AS-path
      "^$"
          + "|"
          // non-empty AS-paths
          + "^"
          + "("
          + AS_NUM_REGEX
          + " "
          + ")*"
          + AS_NUM_REGEX
          + "$";

  /**
   * When converting an AS path regex to an automaton (see toAutomaton()), we intersect with this
   * automaton, which represents the language of AS paths. Doing so serves two purposes. First, it
   * is necessary for correctness of the symbolic analysis. For example, a regex like ".*" does not
   * actually match any possible string since AS paths cannot be arbitrary strings. Second, it
   * ensures that when we solve for AS paths that match regexes, we will get examples that are
   * sensible and also able to be parsed by Batfish.
   */
  @Nonnull static final Automaton AS_PATH_FSM = new RegExp(AS_PATH_REGEX).toAutomaton();

  public SymbolicAsPathRegex(String regex) {
    super(regex);
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
    return new RegExp(regex).toAutomaton().intersection(AS_PATH_FSM);
  }

  @Override
  public String toString() {
    return _regex.toString();
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
