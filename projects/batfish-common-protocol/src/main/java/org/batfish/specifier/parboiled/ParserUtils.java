package org.batfish.specifier.parboiled;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.parboiled.Completion.Type;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.MatcherPath.Element;
import org.parboiled.support.ParsingResult;
import scala.Tuple2;

/** A helper class to interpret parser errors */
@ParametersAreNonnullByDefault
final class ParserUtils {

  static AstNode getAst(ParsingResult<AstNode> result) {
    checkArgument(
        result.parseErrors.isEmpty(),
        "Cannot get AST because parsing failed for '%s'",
        result.inputBuffer);
    return Iterables.getOnlyElement(result.valueStack);
  }

  /** Generates a friendly message to explain what might be wrong with parser input */
  static String getErrorString(
      String input,
      String inputType,
      InvalidInputError error,
      Map<String, Completion.Type> completionTypes) {
    return getErrorString(
        input, inputType, error.getStartIndex(), getPartialMatches(error, completionTypes));
  }

  /** Generates a friendly message to explain what might be wrong with parser input */
  static String getErrorString(
      String input, String inputType, int startIndex, Set<PartialMatch> partialMatches) {
    StringBuilder retString =
        new StringBuilder(
            String.format(
                "Error parsing '%s' as %s after index %d. ", input, inputType, startIndex));
    if (!partialMatches.isEmpty()) {
      retString.append("Valid continuations are ");
      retString.append(
          partialMatches.stream()
              .map(ParserUtils::getErrorString)
              .collect(Collectors.joining(" or ")));
      retString.append(".");
    }
    return retString.toString();
  }

  /** Generates a friendly message to explain what might be wrong with a particular partial match */
  @VisibleForTesting
  static String getErrorString(PartialMatch pm) {
    if (pm.getCompletionType().equals(Completion.Type.STRING_LITERAL)) {
      return String.format("'%s'", pm.getMatchCompletion());
    }
    return String.format("%s", pm.getCompletionType());
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
      Tuple2<Integer, Completion.Type> usefulPoint =
          getUsefulLabel(path, path.length() - 1, completionTypes);

      if (usefulPoint == null) {
        /**
         * If you get here, that means the grammar is missing a Completion annotation on one or more
         * rules. For each path from the top-level rule to the leaf, there should be at least one
         * rule with a Completion annotation.
         */
        throw new IllegalStateException(
            String.format("Useful completion not found in path %s", path));
      }

      int level = usefulPoint._1;
      Completion.Type completionType = usefulPoint._2;

      // Ignore paths that end in WhiteSpace -- nothing interesting to report there because
      // our grammar is not sensitive to whitespace
      if (completionType.equals(Type.WhiteSpace)) {
        continue;
      }

      partialMatches.add(
          getPartialMatch(error, path, path.getElementAtLevel(level), completionType));
    }

    return partialMatches;
  }

  @Nonnull
  private static PartialMatch getPartialMatch(
      InvalidInputError error, MatcherPath path, Element element, Completion.Type type) {

    String matchPrefix =
        error.getInputBuffer().extract(element.startIndex, path.element.startIndex);

    if (type.equals(Type.STRING_LITERAL)) {
      String fullToken = element.matcher.getLabel();
      if (fullToken.length() >= 2) {
        fullToken = fullToken.substring(1, fullToken.length() - 1);
      }
      String matchCompletion = fullToken.substring(matchPrefix.length());

      return new PartialMatch(type, matchPrefix, matchCompletion);

    } else {
      return new PartialMatch(type, matchPrefix, null);
    }
  }

  /**
   * Working backwards from level, tries to find a useful label for error messages and auto
   * completion. Returns null if none is found.
   */
  @Nullable
  private static Tuple2<Integer, Completion.Type> getUsefulLabel(
      MatcherPath path, int level, Map<String, Completion.Type> completionTypes) {

    MatcherPath.Element element = path.getElementAtLevel(level);
    String label = element.matcher.getLabel();

    if (completionTypes.containsKey(label)) {
      return new Tuple2<>(level, completionTypes.get(label));
    } else if (isStringLiteralLabel(label)) {
      return new Tuple2<>(level, Type.STRING_LITERAL);
    } else if (isCharLiteralLabel(label)) {
      if (level == 0 || getUsefulLabel(path, level - 1, completionTypes) == null) {
        return new Tuple2<>(level, Type.STRING_LITERAL);
      }
    } else if (level == 0) {
      return null;
    }
    return getUsefulLabel(path, level - 1, completionTypes);
  }

  private static boolean isCharLiteralLabel(String label) {
    return label.startsWith("\'") && label.endsWith("\'");
  }

  private static boolean isStringLiteralLabel(String label) {
    return label.startsWith("\"") && label.endsWith("\"");
  }
}
