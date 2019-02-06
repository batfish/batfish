package org.batfish.question.initialization;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;

final class IssueAggregation {
  static class WarningTriplet {
    String _text;
    String _parserContext;
    String _comment;

    WarningTriplet(ParseWarning w) {
      this(w.getText(), w.getParserContext(), w.getComment());
    }

    WarningTriplet(String text, String parserContext, String comment) {
      _text = text;
      _parserContext = parserContext;
      _comment = comment;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof WarningTriplet)) {
        return false;
      }
      return Objects.equals(_text, ((WarningTriplet) o)._text)
          && Objects.equals(_parserContext, ((WarningTriplet) o)._parserContext)
          && Objects.equals(_comment, ((WarningTriplet) o)._comment);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_text, _parserContext, _comment);
    }
  }

  /**
   * Aggregate same parse warnings across multiple files and lines.
   *
   * <p>Produces a map of {@link WarningTriplet} to map of filename to lines.
   */
  @Nonnull
  @VisibleForTesting
  static Map<WarningTriplet, Map<String, SortedSet<Integer>>> aggregateDuplicateParseWarnings(
      Map<String, Warnings> fileWarnings) {
    Map<WarningTriplet, Map<String, SortedSet<Integer>>> map = new HashMap<>();
    fileWarnings.forEach(
        (filename, warnings) -> {
          for (ParseWarning w : warnings.getParseWarnings()) {
            WarningTriplet triplet = new WarningTriplet(w);
            map.computeIfAbsent(triplet, k -> new HashMap<>())
                .computeIfAbsent(filename, k -> new TreeSet<>())
                .add(w.getLine());
          }
        });
    return map;
  }

  /**
   * Aggregate same redflag warnings across multiple nodes.
   *
   * <p>Produces a map of redflag {@link Warning} to nodes.
   */
  @Nonnull
  @VisibleForTesting
  static Map<Warning, SortedSet<String>> aggregateDuplicateRedflagWarnings(
      Map<String, Warnings> nodeToWarnings) {
    Map<Warning, SortedSet<String>> map = new HashMap<>();
    nodeToWarnings.forEach(
        (node, warnings) -> {
          for (Warning warning : warnings.getRedFlagWarnings()) {
            map.computeIfAbsent(warning, w -> new TreeSet<>()).add(node);
          }
        });
    return map;
  }

  /**
   * Aggregate same unimplemented warnings across multiple nodes.
   *
   * <p>Produces a map of unimplemented {@link Warning} to nodes.
   */
  @Nonnull
  @VisibleForTesting
  static Map<Warning, SortedSet<String>> aggregateDuplicateUnimplementedWarnings(
      Map<String, Warnings> nodeToWarnings) {
    Map<Warning, SortedSet<String>> map = new HashMap<>();
    nodeToWarnings.forEach(
        (node, warnings) -> {
          for (Warning warning : warnings.getUnimplementedWarnings()) {
            map.computeIfAbsent(warning, w -> new TreeSet<>()).add(node);
          }
        });
    return map;
  }

  /**
   * Aggregate same strings (e.g. trimmed stack traces) across multiple nodes or files.
   *
   * <p>Produces a map of {@link String} to node/file names from a map of node/file to string.
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
