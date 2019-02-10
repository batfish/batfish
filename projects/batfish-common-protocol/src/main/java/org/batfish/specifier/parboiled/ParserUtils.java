package org.batfish.specifier.parboiled;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.matchers.AnyOfMatcher;
import org.parboiled.matchers.CharMatcher;
import org.parboiled.matchers.CharRangeMatcher;
import org.parboiled.matchers.StringMatcher;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.MatcherPath;

/** A helper class to interpret parser errors */
public final class ParserUtils {

  /** The label for the built in rule to create tokens from string literals */
  public static String STRING_LITERAL_LABEL = "fromStringLiteral";

  private static Set<String> _BUILT_IN_LABELS =
      ImmutableSet.of("Sequence", "FirstOf", "AnyOf", "ZeroOrMore", "OneOrMore", "Optional");

  /** Generates a friendly message to explain what might be wrong with parser input */
  public static String getErrorString(InvalidInputError error) {
    return getErrorString(error.getStartIndex(), getPartialMatches(error));
  }

  /** Generates a friendly message to explain what might be wrong with parser input */
  public static String getErrorString(int startIndex, Set<PartialMatch> partialMatches) {
    String retString = String.format("Error parsing input at index %d.", startIndex);
    if (!partialMatches.isEmpty()) {
      retString +=
          "Expected "
              + partialMatches.stream()
                  .map(pm -> getErrorString(pm))
                  .collect(Collectors.joining(", "));
    }
    return retString;
  }

  /** Generates a friendly message to explain what might be wrong with a particular partial match */
  @VisibleForTesting
  static String getErrorString(PartialMatch pm) {
    if (pm.getRuleLabel().equals(STRING_LITERAL_LABEL)) {
      return String.format("'%s'", pm.getMatchCompletion());
    }
    if (pm.getMatchPrefix().equals("")) {
      return String.format("%s", pm.getRuleLabel());
    } else {
      return String.format("%s starting with '%s'", pm.getRuleLabel(), pm.getMatchPrefix());
    }
  }

  /**
   * When parsing fails, given its error, this function returns the set of matches that could have
   * made things work.
   *
   * <p>This function has been tested with only {@link ReportingParseRunner}. When using {@link
   * RecoveringParseRunner}, there may be some additional complexity with respect to indices. See
   * https://github.com/sirthias/parboiled/blob/07b6e2b5c583c7e258599650157a3b0d2b63667a/parboiled-core/src/main/java/org/parboiled/errors/DefaultInvalidInputErrorFormatter.java#L60.
   */
  public static Set<PartialMatch> getPartialMatches(InvalidInputError error) {

    Set<PartialMatch> partialMatches = new HashSet<>();

    // For each path that failed to match we identify a useful point in the path that is closest to
    // the end and can provide the basis for error reporting and completion suggestions.
    for (MatcherPath path : error.getFailedMatchers()) {
      int level = path.length() - 1;
      for (; level >= 0; level--) {
        MatcherPath.Element element = path.getElementAtLevel(level);

        // Ignore paths that end in WhiteSpace -- nothing interesting to report there because
        // our grammar is not sensitive to whitespace
        if (element.matcher.getLabel().equals("WhiteSpace")) {
          break;
        }

        // Ignore points in the path that correspond to built-in and common rules
        if (element.matcher instanceof AnyOfMatcher
            || element.matcher instanceof CharMatcher
            || element.matcher instanceof CharRangeMatcher
            || element.matcher instanceof StringMatcher
            || _BUILT_IN_LABELS.contains(element.matcher.getLabel())
            || CommonParser.COMMON_LABELS.contains(element.matcher.getLabel())) {
          continue;
        }

        partialMatches.add(getPartialMatch(error, path, element, level));
        break;
      }
      if (level == -1) {
        throw new IllegalStateException(String.format("Useful matcher not found in path %s", path));
      }
    }

    return partialMatches;
  }

  private static PartialMatch getPartialMatch(
      InvalidInputError error, MatcherPath path, MatcherPath.Element element, int level) {

    String matchPrefix =
        error.getInputBuffer().extract(element.startIndex, path.element.startIndex);

    if (STRING_LITERAL_LABEL.equals(element.matcher.getLabel())) {
      String fullToken = path.getElementAtLevel(level + 1).matcher.getLabel();
      if (fullToken.length() >= 2) {
        fullToken = fullToken.substring(1, fullToken.length() - 1);
      }
      String matchCompletion = fullToken.substring(matchPrefix.length());

      return new PartialMatch(STRING_LITERAL_LABEL, matchPrefix, matchCompletion);

    } else {
      return new PartialMatch(element.matcher.getLabel(), matchPrefix, null);
    }
  }
}
