package org.batfish.minesweeper;

import static org.batfish.minesweeper.CommunityVar.Type.EXACT;

import com.google.common.base.MoreObjects;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.bgp.community.Community;

/**
 * Representation of a community variable for the symbolic encoding. Configuration languages allow
 * users match community values using either <b>exact matches</b> or <b>regular expression</b>
 * matches. For example, a regular expression match such as .*:65001 will match any community string
 * that ends with 65001.
 *
 * <p>To encode community semantics, the model introduces a single new boolean variable for every
 * exact match, and two new boolean variables for every regex match. The first variable says whether
 * there is a community value that matches the regex, but is not specified in the configuration
 * (e.g., came from a neighbor). The second variable says if the regex match is successful, which is
 * based on both the communities in the configuration as well as other communities possibly sent by
 * neighbors.
 *
 * @author Ryan Beckett
 */
@ParametersAreNonnullByDefault
public final class CommunityVar extends SymbolicRegex implements Comparable<CommunityVar> {

  public static final CommunityVar ALL_COMMUNITIES = CommunityVar.from(".*");

  private static final Comparator<CommunityVar> COMPARATOR =
      Comparator.comparing(CommunityVar::getType)
          .thenComparing(CommunityVar::getRegex)
          .thenComparing(
              CommunityVar::getLiteralValue, Comparator.nullsLast(Comparator.naturalOrder()));

  public enum Type {
    EXACT,
    REGEX,
    OTHER
  }

  @Nonnull private final Type _type;
  @Nullable private final Community _literalValue;

  @Nonnull private static final String NUM_REGEX = "(0|[1-9][0-9]*)";

  // a regex that represents the language of community literals supported by Batfish
  // see Community::matchString() and its implementations
  @Nonnull
  private static final String COMMUNITY_REGEX =
      // start-of-string character
      "^"
          +
          // standard and extended communities
          "("
          + String.join(":", NUM_REGEX, NUM_REGEX)
          + "|"
          // large communities
          + String.join(":", "large", NUM_REGEX, NUM_REGEX, NUM_REGEX)
          + ")"
          // end-of-string character
          + "$";

  /**
   * When converting a community variable to an automaton (see toAutomaton()), we intersect with
   * this automaton, which represents the language of community literals supported by Batfish. Doing
   * so serves two purposes. First, it is necessary for correctness of the symbolic analysis. For
   * example, a regex like ".*" does not actually match any possible string since communities cannot
   * be arbitrary strings. Second, it ensures that when we solve for community literals that match
   * regexes, we will get examples that are sensible and also able to be parsed by Batfish.
   */
  @Nonnull static final Automaton COMMUNITY_FSM = new RegExp(COMMUNITY_REGEX).toAutomaton();

  private CommunityVar(Type type, String regex, @Nullable Community literalValue) {
    super(regex);
    _type = type;
    _literalValue = literalValue;
  }

  /** Create a community var of type {@link Type#REGEX} */
  public static CommunityVar from(String regex) {
    return new CommunityVar(Type.REGEX, regex, null);
  }

  /**
   * Create a community var of type {@link Type#EXACT} based on a literal {@link Community} value
   */
  public static CommunityVar from(Community literalCommunity) {
    return new CommunityVar(Type.EXACT, literalCommunity.matchString(), literalCommunity);
  }

  /** Create a community var of type {@link Type#OTHER} based on a REGEX community var. */
  public static CommunityVar other(String regex) {
    return new CommunityVar(Type.OTHER, regex, null);
  }

  @Nonnull
  public Type getType() {
    return _type;
  }

  @Nullable
  public Community getLiteralValue() {
    return _literalValue;
  }

  /**
   * Convert this community variable into an equivalent finite-state automaton.
   *
   * @return the automaton
   */
  @Override
  public Automaton toAutomaton() {
    String regex = _regex;
    if (_type == EXACT) {
      // to turn a literal into a regex, add the start-of-string and end-of-string characters
      regex = "^" + "(" + regex + ")" + "$";
    } else {
      /**
       * A regex need only match a portion of a given community string. For example, the regex
       * "^40:" matches the community 40:11. But to properly relate community regexes to one
       * another, for example to find their intersection, we need regexes that match completely.
       *
       * <p>The simple approach below converts a possibly-partial regex into a complete one. It
       * works because below we intersect the resulting automaton with COMMUNITY_FSM, which notably
       * includes the start-of-string and end-of-string characters. Note that the automaton library
       * treats these as ordinary characters.
       *
       * <p>For example, the regex "^40:" becomes ".*(^40:).*", and the final automaton after
       * intersecting with COMMUNITY_FSM accepts the language of the regex "^40:[0-9]+$" as desired.
       */
      regex = ".*" + "(" + regex + ")" + ".*";
    }
    return new RegExp(regex).toAutomaton().intersection(COMMUNITY_FSM);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("type", _type)
        .add("regex", _regex)
        .add("literalValue", _literalValue)
        .toString();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CommunityVar)) {
      return false;
    }
    CommunityVar that = (CommunityVar) o;
    return _type == that._type
        && _regex.equals(that._regex)
        && Objects.equals(_literalValue, that._literalValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type.ordinal(), _regex, _literalValue);
  }

  @Override
  public int compareTo(CommunityVar that) {
    return COMPARATOR.compare(this, that);
  }
}
