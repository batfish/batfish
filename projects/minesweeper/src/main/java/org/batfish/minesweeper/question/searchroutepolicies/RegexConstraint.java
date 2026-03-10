package org.batfish.minesweeper.question.searchroutepolicies;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A class that represents a regular expression constraint for a {@link BgpRouteConstraints} object.
 * These are used for constraints on communities as well as on AS paths. Each regex is optionally
 * negated, meaning that the given route should _not_ have a community/AS-path that matches the
 * regex. Regex constraints are of two types: regex literals and structure names. Currently,
 * structure names are only supported for community constraints and must refer to a {@link
 * org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr} in the configuration.
 */
@ParametersAreNonnullByDefault
public class RegexConstraint {

  private static final String PROP_REGEX = "regex";
  private static final String PROP_NEGATED = "negated";
  private static final String PROP_REGEX_TYPE = "regexType";

  public enum RegexType {
    REGEX,
    STRUCTURE_NAME
  }

  private final @Nonnull String _regex;
  private final boolean _negated;

  private final RegexType _regexType;

  public RegexConstraint(String regex, boolean negated) {
    this(regex, negated, RegexType.REGEX);
  }

  @JsonCreator
  public RegexConstraint(
      @JsonProperty(PROP_REGEX) String regex,
      @JsonProperty(PROP_NEGATED) boolean negated,
      @JsonProperty(PROP_REGEX_TYPE) @Nullable RegexType type) {
    _regex = regex;
    _negated = negated;
    _regexType = firstNonNull(type, RegexType.REGEX);
  }

  /**
   * We allow a regex constraint to be expressed using the same syntax as enumSetTerm in Batfish's
   * syntax of specifiers (see
   * https://github.com/batfish/batfish/blob/master/questions/Parameters.md#set-of-enums-or-names).
   * Specifically, "val1" denotes a specific value, "!val1" denotes that val1 should not exist,
   * "/regex1/" denotes values that match the given Java regex, and "!/regex1/" denotes that values
   * that match the given regex should not exist.
   *
   * @param s the string representation of a RegexConstraint, using the above syntax
   * @return the corresponding RegexConstraint object
   */
  @JsonCreator
  public static @Nonnull RegexConstraint parse(@Nonnull String s) {
    String curr = s;
    // first check if this constraint should be negated
    boolean negated;
    if (curr.startsWith("!")) {
      negated = true;
      curr = curr.substring(1);
    } else {
      negated = false;
    }
    String regex;
    RegexType regexType;
    boolean startRegex = curr.startsWith("/");
    boolean endRegex = curr.endsWith("/");
    if (startRegex && endRegex && curr.length() > 1) {
      // valid syntax for a regex
      regex = curr.substring(1, curr.length() - 1);
      regexType = RegexType.REGEX;
    } else if (!startRegex && !endRegex) {
      // the constraint denotes a single value
      if (!curr.isEmpty() && Character.isLetter(curr.charAt(0))) {
        // since the regex starts with a letter, treat it as a structure name
        // TODO we could make a syntax for people to explicitly indicate that they are providing a
        // structure name
        regex = curr;
        regexType = RegexType.STRUCTURE_NAME;
      } else {
        // convert a string literal to a regex
        regex = "^" + curr + "$";
        regexType = RegexType.REGEX;
      }
    } else {
      throw new IllegalArgumentException("Invalid regex constraint: " + s);
    }
    return new RegexConstraint(regex, negated, regexType);
  }

  @JsonProperty(PROP_REGEX)
  public String getRegex() {
    return _regex;
  }

  @JsonProperty(PROP_NEGATED)
  public boolean getNegated() {
    return _negated;
  }

  @JsonProperty(PROP_REGEX_TYPE)
  public RegexType getRegexType() {
    return _regexType;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RegexConstraint)) {
      return false;
    }
    RegexConstraint that = (RegexConstraint) o;
    return _regex.equals(that._regex) && _negated == that._negated && _regexType == that._regexType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_regex, _negated, _regexType);
  }
}
