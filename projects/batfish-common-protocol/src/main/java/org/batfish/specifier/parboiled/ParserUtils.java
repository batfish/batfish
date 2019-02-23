package org.batfish.specifier.parboiled;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.specifier.parboiled.Anchor.Type;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;

/** A helper class to interpret parser errors */
@ParametersAreNonnullByDefault
final class ParserUtils {

  /**
   * Captures anchors in potentially matching paths for anchor error reporting and auto completion.
   */
  private static class PathAnchor {
    private final Anchor.Type _anchorType;
    private final int _level;

    PathAnchor(Anchor.Type anchorType, int level) {
      this._anchorType = anchorType;
      this._level = level;
    }

    Anchor.Type getAnchorType() {
      return _anchorType;
    }

    int getLevel() {
      return _level;
    }
  }

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
        "%s. See %s for valid grammar.",
        getErrorString(
            input,
            grammar.getFriendlyName(),
            error.getStartIndex(),
            getPotentialMatches(error, anchorTypes, true)),
        grammar.getFullUrl());
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
              .collect(Collectors.joining(" or ")));
      retString.append(".");
    }
    return retString.toString();
  }

  /** Generates a friendly message to explain what might be wrong with a particular partial match */
  @VisibleForTesting
  static String getErrorString(PotentialMatch pm) {
    if (pm.getAnchorType().equals(Anchor.Type.STRING_LITERAL)) {
      return String.format("'%s'", pm.getMatchCompletion());
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
      PathAnchor pathAnchor =
          findPathAnchor(path, fromTop ? 0 : path.length() - 1, anchorTypes, fromTop);

      if (pathAnchor == null) {
        // Getting here means the grammar is missing a Anchor annotation. See Parser's JavaDoc.
        throw new IllegalStateException(
            String.format("Useful completion not found in path %s", path));
      }

      // Ignore paths whose anchor is WHITESPACE or IGNORE
      if (pathAnchor._anchorType.equals(Anchor.Type.WHITESPACE)
          || pathAnchor._anchorType.equals(Anchor.Type.IGNORE)) {
        continue;
      }

      /*
       The PotentialMatch object is straightforward given the anchor. Except that for string
       literals, we remove the quotes inserted by parboiled.
      */
      String matchPrefix =
          error
              .getInputBuffer()
              .extract(
                  path.getElementAtLevel(pathAnchor.getLevel()).startIndex,
                  path.element.startIndex);

      if (pathAnchor._anchorType.equals(Anchor.Type.STRING_LITERAL)) {
        String fullToken = path.getElementAtLevel(pathAnchor.getLevel()).matcher.getLabel();
        if (fullToken.length() >= 2) { // remove surrounding quotes
          fullToken = fullToken.substring(1, fullToken.length() - 1);
        }
        potentialMatches.add(
            new PotentialMatch(
                pathAnchor.getAnchorType(),
                matchPrefix,
                fullToken.substring(matchPrefix.length())));
      } else {
        potentialMatches.add(new PotentialMatch(pathAnchor.getAnchorType(), matchPrefix, null));
      }
    }

    return potentialMatches.build();
  }

  /** Finds the anchor in the path. Returns null if none is found. */
  @Nullable
  private static PathAnchor findPathAnchor(
      MatcherPath path, int level, Map<String, Anchor.Type> anchorTypes, boolean fromTop) {

    MatcherPath.Element element = path.getElementAtLevel(level);
    String label = element.matcher.getLabel();

    if (anchorTypes.containsKey(label)) {
      if (!fromTop && anchorTypes.get(label) == Type.IGNORE && level > 0) {
        return findPathAnchor(path, level - 1, anchorTypes, fromTop);
      }
      return new PathAnchor(anchorTypes.get(label), level);
    } else if (isStringLiteralLabel(label)) {
      // If we have descended from IGNORE go all the way up to IGNORE and return that
      if (!fromTop) {
        int ignoreLevel = descendedFromIgnore(path, level, anchorTypes);
        if (ignoreLevel > 0) {
          return findPathAnchor(path, ignoreLevel - 1, anchorTypes, fromTop);
        }
      }
      return new PathAnchor(Anchor.Type.STRING_LITERAL, level);
    } else if (isCharLiteralLabel(label)) {
      // char literals appear at the bottom; if we are searching from top, we've reached the end
      if (fromTop || level == 0) {
        return new PathAnchor(Anchor.Type.STRING_LITERAL, level);
      }
      // If we have descended from IGNORE, go past IGNORE
      int ignoreLevel = descendedFromIgnore(path, level, anchorTypes);
      if (ignoreLevel > 0) {
        return findPathAnchor(path, ignoreLevel - 1, anchorTypes, fromTop);
      }
      // if the parent label is a literal string (e.g., "@specifier"), use that because we want to
      // autocomplete the entire string not just one of its characters
      MatcherPath.Element parentElement = path.getElementAtLevel(level - 1);
      if (isStringLiteralLabel(parentElement.matcher.getLabel())) {
        return new PathAnchor(Anchor.Type.STRING_LITERAL, level - 1);
      }
      return new PathAnchor(Anchor.Type.STRING_LITERAL, level);
    } else if ((fromTop && level == path.length() - 1) || (!fromTop && level == 0)) {
      // we have reached the last element in the path
      return null;
    }
    return findPathAnchor(path, fromTop ? level + 1 : level - 1, anchorTypes, fromTop);
  }

  /**
   * Checks if an ancestor of element at {@code level) is of Anchor.Type == IGNORE. Returns the
   * level of the ancestor if so. Returns -1 otherwise.
   */
  private static int descendedFromIgnore(
      MatcherPath path, int level, Map<String, Anchor.Type> anchorTypes) {
    if (level <= 0) {
      return -1;
    }
    MatcherPath.Element parent = path.getElementAtLevel(level - 1);
    String label = parent.matcher.getLabel();
    if (anchorTypes.containsKey(label) && anchorTypes.get(label) == Type.IGNORE) {
      return level - 1;
    }
    return descendedFromIgnore(path, level - 1, anchorTypes);
  }

  private static boolean isCharLiteralLabel(String label) {
    return label.startsWith("\'") && label.endsWith("\'");
  }

  private static boolean isStringLiteralLabel(String label) {
    return label.startsWith("\"") && label.endsWith("\"");
  }
}
