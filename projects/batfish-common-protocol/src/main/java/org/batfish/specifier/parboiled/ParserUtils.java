package org.batfish.specifier.parboiled;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.parboiled.Completion.Type;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.MatcherPath.Element;

/** A helper class to interpret parser errors */
@ParametersAreNonnullByDefault
final class ParserUtils {

  /** Generates a friendly message to explain what might be wrong with parser input */
  static String getErrorString(
      InvalidInputError error, Map<String, Completion.Type> completionTypes) {
    return getErrorString(error.getStartIndex(), getPartialMatches(error, completionTypes));
  }

  /** Generates a friendly message to explain what might be wrong with parser input */
  static String getErrorString(int startIndex, Set<PartialMatch> partialMatches) {
    String retString = String.format("Error parsing input at index %d.", startIndex);
    if (!partialMatches.isEmpty()) {
      retString +=
          "Expected "
              + partialMatches.stream()
                  .map(ParserUtils::getErrorString)
                  .collect(Collectors.joining(", "));
    }
    return retString;
  }

  /** Generates a friendly message to explain what might be wrong with a particular partial match */
  @VisibleForTesting
  static String getErrorString(PartialMatch pm) {
    if (pm.getCompletionType().equals(Completion.Type.STRING_LITERAL)) {
      return String.format("'%s'", pm.getMatchCompletion());
    }
    if (pm.getMatchPrefix().equals("")) {
      return String.format("%s", pm.getCompletionType());
    } else {
      return String.format("%s starting with '%s'", pm.getCompletionType(), pm.getMatchPrefix());
    }
  }

  /**
   * When parsing fails, given its error, this function returns the set of matches that could have
   * made things work.
   *
   * <p>This function has been tested with only {@link
   * org.parboiled.parserunners.ReportingParseRunner}. When using {@link
   * org.parboiled.parserunners.RecoveringParseRunner}, there may be some additional complexity with
   * respect to indices. See
   * https://github.com/sirthias/parboiled/blob/07b6e2b5c583c7e258599650157a3b0d2b63667a/parboiled-core/src/main/java/org/parboiled/errors/DefaultInvalidInputErrorFormatter.java#L60.
   */
  static Set<PartialMatch> getPartialMatches(
      InvalidInputError error, Map<String, Completion.Type> completionTypes) {

    Set<PartialMatch> partialMatches = new HashSet<>();

    // For each path that failed to match we identify a useful point in the path that is closest to
    // the end and can provide the basis for error reporting and completion suggestions.
    for (MatcherPath path : error.getFailedMatchers()) {
      int level = path.length() - 1;
      for (; level >= 0; level--) {
        MatcherPath.Element element = path.getElementAtLevel(level);
        String label = element.matcher.getLabel();

        // Ignore paths that end in WhiteSpace -- nothing interesting to report there because
        // our grammar is not sensitive to whitespace
        if (label.equals("WhiteSpace")) {
          break;
        }

        if (completionTypes.containsKey(label)) {
          partialMatches.add(
              getPartialMatch(error, path, element, level, completionTypes.get(label)));
          break;
        }
      }
      if (level == -1) {
        /**
         * If you get here, that means the grammar is missing a Completion annotation on one or more
         * rules. For each path from the top-level rule to the leaf, there should be at least one
         * rule with a Completion annotation.
         */
        throw new IllegalStateException(
            String.format("Useful completion not found in path %s", path));
      }
    }

    return partialMatches;
  }

  @Nonnull
  private static PartialMatch getPartialMatch(
      InvalidInputError error, MatcherPath path, Element element, int level, Completion.Type type) {

    String matchPrefix =
        error.getInputBuffer().extract(element.startIndex, path.element.startIndex);

    if (type.equals(Type.STRING_LITERAL)) {
      String fullToken = path.getElementAtLevel(level + 1).matcher.getLabel();
      if (fullToken.length() >= 2) {
        fullToken = fullToken.substring(1, fullToken.length() - 1);
      }
      String matchCompletion = fullToken.substring(matchPrefix.length());

      return new PartialMatch(type, matchPrefix, matchCompletion);

    } else {
      return new PartialMatch(type, matchPrefix, null);
    }
  }
}
