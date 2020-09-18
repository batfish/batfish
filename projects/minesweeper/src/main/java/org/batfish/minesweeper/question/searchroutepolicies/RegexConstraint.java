package org.batfish.minesweeper.question.searchroutepolicies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;

/**
 * A class that represents a regular expression constraint for a {@link BgpRouteConstraints} object.
 * These are used for constraints on communities as well as on AS paths. Each regex is optionally
 * negated, meaning that the given route should _not_ have a community/AS-path that matches the
 * regex.
 */
@ParametersAreNonnullByDefault
public class RegexConstraint {

  private static final String PROP_REGEX = "regex";
  private static final String PROP_NEGATED = "negated";

  @Nonnull private final String _regex;
  private final boolean _negated;

  @JsonCreator
  public RegexConstraint(
      @JsonProperty(PROP_REGEX) String regex, @JsonProperty(PROP_NEGATED) boolean negated) {
    _regex = regex;
    _negated = negated;
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
  @Nonnull
  public static RegexConstraint parse(@Nonnull String s) {
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
    boolean startRegex = curr.startsWith("/");
    boolean endRegex = curr.endsWith("/");
    if (startRegex && endRegex && curr.length() > 1) {
      // valid syntax for a regex
      regex = curr.substring(1, curr.length() - 1);
    } else if (!startRegex && !endRegex) {
      // the constraint denotes a single value; convert it to a regex
      regex = "^" + curr + "$";
    } else {
      throw new BatfishException("Invalid regex constraint: " + s);
    }
    return new RegexConstraint(regex, negated);
  }

  @JsonProperty(PROP_REGEX)
  public String getRegex() {
    return _regex;
  }

  @JsonProperty(PROP_NEGATED)
  public boolean getNegated() {
    return _negated;
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
    return _regex.equals(that._regex) && _negated == that._negated;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_regex, _negated);
  }
}
