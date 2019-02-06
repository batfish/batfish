package org.batfish.question.initialization;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;

/**
 * Aggregate similar issues ({@link Warning}, {@link ParseWarning}, and {@link String}) across nodes
 * or files.
 */
@ParametersAreNonnullByDefault
final class IssueAggregation {

  /**
   * Class for holding aggregatable parse warnings. Similar to {@link ParseWarning}s, but ignoring
   * line numbers to allow useful aggregation.
   */
  static final class ParseWarningTriplet {
    final String _text;
    final String _parserContext;
    @Nullable final String _comment;

    ParseWarningTriplet(ParseWarning w) {
      this(w.getText(), w.getParserContext(), w.getComment());
    }

    ParseWarningTriplet(String text, String parserContext, @Nullable String comment) {
      _text = text;
      _parserContext = parserContext;
      _comment = comment;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ParseWarningTriplet)) {
        return false;
      }
      ParseWarningTriplet triplet = (ParseWarningTriplet) o;
      return Objects.equals(_text, triplet._text)
          && Objects.equals(_parserContext, triplet._parserContext)
          && Objects.equals(_comment, triplet._comment);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_text, _parserContext, _comment);
    }
  }

  /**
   * Aggregate same parse warnings across multiple files and lines.
   *
   * <p>Produces a map of {@link ParseWarningTriplet} to map of filename to lines.
   */
  @Nonnull
  @VisibleForTesting
  static Map<ParseWarningTriplet, Multimap<String, Integer>> aggregateDuplicateParseWarnings(
      Map<String, Warnings> fileWarnings) {
    Map<ParseWarningTriplet, Multimap<String, Integer>> map = new HashMap<>();
    fileWarnings.forEach(
        (filename, warnings) -> {
          for (ParseWarning w : warnings.getParseWarnings()) {
            ParseWarningTriplet triplet = new ParseWarningTriplet(w);
            map.computeIfAbsent(triplet, k -> HashMultimap.create()).put(filename, w.getLine());
          }
        });
    return map;
  }

  /**
   * Aggregate same warnings across multiple nodes. The specified warningFunc determines what {@link
   * List} of {@link Warning} within the supplied {@link Warnings} are evaluated.
   *
   * <p>Produces a map of {@link Warning} to nodes.
   */
  @Nonnull
  @VisibleForTesting
  static Map<Warning, SortedSet<String>> aggregateDuplicateWarnings(
      Map<String, Warnings> nodeToWarnings, Function<Warnings, List<Warning>> warningFunc) {
    Map<Warning, SortedSet<String>> map = new HashMap<>();
    nodeToWarnings.forEach(
        (node, warnings) -> {
          for (Warning warning : warningFunc.apply(warnings)) {
            map.computeIfAbsent(warning, w -> new TreeSet<>()).add(node);
          }
        });
    return map;
  }

  /**
   * Aggregate same strings (e.g. trimmed stack traces) across multiple nodes (or alternatively
   * files).
   *
   * <p>Produces a map of {@link String} to node names from a map of node names to string.
   */
  @Nonnull
  @VisibleForTesting
  static Map<String, SortedSet<String>> aggregateDuplicateStrings(
      Map<String, String> nodeToString) {
    Map<String, SortedSet<String>> map = new HashMap<>();
    nodeToString.forEach(
        (source, string) -> map.computeIfAbsent(string, t -> new TreeSet<>()).add(source));
    return map;
  }
}
