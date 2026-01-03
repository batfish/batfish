package org.batfish.specifier.parboiled;

import static org.batfish.specifier.parboiled.Anchor.Type.CHAR_LITERAL;
import static org.batfish.specifier.parboiled.Anchor.Type.ENUM_SET_VALUE;
import static org.batfish.specifier.parboiled.Anchor.Type.IGNORE;
import static org.batfish.specifier.parboiled.Anchor.Type.STRING_LITERAL;
import static org.batfish.specifier.parboiled.Anchor.Type.WHITESPACE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.Grammar;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;

/** A helper class to interpret parser errors */
@ParametersAreNonnullByDefault
final class ParserUtils {

  static AstNode getAst(ParsingResult<AstNode> result) {
    if (!result.parseErrors.isEmpty()) {
      throw new IllegalArgumentException(
          String.format(
              "Cannot get AST. Parsing failed for '%s' at index %s",
              result.inputBuffer.extract(0, Integer.MAX_VALUE),
              result.parseErrors.get(0).getStartIndex()));
    }
    return Iterables.getOnlyElement(result.valueStack);
  }

  /** Generates a friendly message to explain what might be wrong with parser input */
  static String getErrorString(
      String input,
      Grammar grammar,
      InvalidInputError error,
      Map<String, Anchor.Type> anchorTypes) {
    return String.format(
        "%s. See %s for valid grammar. %s",
        getErrorString(
            input,
            grammar.getFriendlyName(),
            error.getStartIndex(),
            getPotentialMatches(error, anchorTypes, true)),
        grammar.getFullUrl(),
        Grammar.GENERAL_NOTE);
  }

  /** Generates a friendly message to explain what might be wrong with parser input */
  static String getErrorString(
      String input, String inputType, int startIndex, Set<PotentialMatch> potentialMatches) {
    StringBuilder retString =
        new StringBuilder(
            String.format(
                "Error parsing '%s' as %s after index %d. ", input, inputType, startIndex));
    if (!potentialMatches.isEmpty()) {
      retString.append("Valid continuations are ");
      retString.append(
          potentialMatches.stream()
              .map(ParserUtils::getErrorString)
              .distinct()
              .collect(Collectors.joining(" or ")));
      retString.append(".");
    }
    return retString.toString();
  }

  /** Generates a friendly message to explain what might be wrong with a particular partial match */
  @VisibleForTesting
  static String getErrorString(PotentialMatch pm) {
    if (pm.getAnchorType() == Anchor.Type.STRING_LITERAL) {
      return String.format("'%s'", pm.getMatch());
    }
    return String.format("%s", pm.getAnchorType());
  }

  /**
   * When parsing fails, this function returns potential matches that could have made things work.
   *
   * <p>Potential matches are anchored around either elements in the path (rules) that correspond to
   * completion types or those that represent string/character literals.
   *
   * <p>The parameter {@code fromTop} determines if we find anchors from top of the rule hierarchy
   * or the bottom. The former is useful for error reporting and the latter for auto completion. For
   * example, if a potential matching path is input->ip_range->ip_address, we want to tell the user
   * that we expect an ip_range but when auto completing we want to use ip_address.
   *
   * <p>This function has been tested with only {@link
   * org.parboiled.parserunners.ReportingParseRunner}. When using {@link
   * org.parboiled.parserunners.RecoveringParseRunner}, there may be some additional complexity with
   * respect to indices. See
   * https://github.com/sirthias/parboiled/blob/07b6e2b5c583c7e258599650157a3b0d2b63667a/parboiled-core/src/main/java/org/parboiled/errors/DefaultInvalidInputErrorFormatter.java#L60.
   */
  static Set<PotentialMatch> getPotentialMatches(
      InvalidInputError error, Map<String, Anchor.Type> anchorTypes, boolean fromTop) {
    ImmutableSet.Builder<PotentialMatch> potentialMatches = ImmutableSet.builder();

    for (MatcherPath path : error.getFailedMatchers()) {
      // convert the parboiled path to a list of PathElement
      List<PathElement> pathElements =
          IntStream.range(0, path.length())
              .mapToObj(i -> PathElement.create(path.getElementAtLevel(i), anchorTypes))
              .collect(ImmutableList.toImmutableList());

      // at least one anchor should exist along every path
      if (!pathElements.stream().anyMatch(pe -> pe.getAnchorType() != null)) {
        throw new IllegalStateException(String.format("No anchor found for path %s", path));
      }

      // Do not consider IGNORE or DEPRECATED paths
      if (pathElements.stream()
          .anyMatch(
              e ->
                  e.getAnchorType() == Anchor.Type.DEPRECATED
                      || e.getAnchorType() == Anchor.Type.HIDDEN)) {
        continue;
      }

      Optional<PathElement> pathAnchorOpt =
          fromTop
              ? findPathAnchorFromTop(pathElements)
              : findPathAnchorFromBottom(pathElements, path.length() - 1);

      if (!pathAnchorOpt.isPresent() || pathAnchorOpt.get().getAnchorType() == WHITESPACE) {
        continue;
      }

      PathElement pathAnchor = pathAnchorOpt.get();
      String matchPrefix =
          error.getInputBuffer().extract(pathAnchor.getStartIndex(), path.element.startIndex);

      potentialMatches.add(new PotentialMatch(pathAnchor, matchPrefix, pathElements));
    }

    return potentialMatches.build();
  }

  /** Finds the anchor in the path, from bottom to top, starting at {@code level}. */
  @VisibleForTesting
  static Optional<PathElement> findPathAnchorFromBottom(List<PathElement> pathElements, int level) {

    // Return empty if our time is up
    if (level < 0) {
      return Optional.empty();
    }

    PathElement element = pathElements.get(level);

    // If we have descended from IGNORE go past that
    if (level > 0) {
      Optional<PathElement> ignoreAncestor = findAncestorOfType(pathElements, level - 1, IGNORE);
      if (ignoreAncestor.isPresent()) {
        return findPathAnchorFromBottom(pathElements, ignoreAncestor.get().getLevel() - 1);
      }
    }

    if (element.getAnchorType() == CHAR_LITERAL) {
      // if the parent label is STRING_LITERAL (e.g., "@specifier"), use that because we want to
      // autocomplete the entire string not just one of its characters
      if (level > 0) {
        if (pathElements.get(level - 1).getAnchorType() == STRING_LITERAL) {
          return findPathAnchorFromBottom(pathElements, level - 1);
        }
        Optional<PathElement> enumSetValueAncestor =
            findAncestorOfType(pathElements, level - 1, ENUM_SET_VALUE);
        if (enumSetValueAncestor.isPresent()) {
          return enumSetValueAncestor;
        }
        return Optional.of(element);
      }
    } else if (element.getAnchorType() != null && element.getAnchorType() != IGNORE) {
      return Optional.of(element);
    }
    return findPathAnchorFromBottom(pathElements, level - 1);
  }

  /**
   * Checks if an ancestor of element at {@code level} is of {@code ancestorType}. Returns the
   * ancestor if so; otherwise, returns an empty Optional.
   */
  private static Optional<PathElement> findAncestorOfType(
      List<PathElement> pathElements, int level, Anchor.Type ancestorType) {
    return IntStream.rangeClosed(0, level)
        .mapToObj(i -> pathElements.get(level - i)) // "level - i" to walk the list from bottom
        .filter(e -> e.getAnchorType() == ancestorType)
        .findFirst();
  }

  private static Optional<PathElement> findPathAnchorFromTop(List<PathElement> pathElements) {
    return pathElements.stream()
        .filter(
            e ->
                e.getAnchorType() != null
                    && e.getAnchorType() != IGNORE
                    && e.getAnchorType() != Anchor.Type.WHITESPACE)
        .findFirst();
  }

  @VisibleForTesting
  static boolean isCharLiteralLabel(String label) {
    return label.startsWith("\'") && label.endsWith("\'");
  }

  @VisibleForTesting
  static boolean isStringLiteralLabel(String label) {
    return label.startsWith("\"") && label.endsWith("\"");
  }
}
