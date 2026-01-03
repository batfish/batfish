package org.batfish.grammar.flattener;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

public final class FlattenerLineMap {
  public static final int UNMAPPED_LINE_NUMBER = -1;

  /**
   * Map of new line number to word map, where word map is map of a word's starting-position in
   * new/flattened line to original line number
   */
  private NavigableMap<Integer, NavigableMap<Integer, Integer>> _lineMap;

  private final Map<Integer, Set<Integer>> _extraLines;

  public FlattenerLineMap() {
    _lineMap = new TreeMap<>();
    _extraLines = new HashMap<>();
  }

  /**
   * Return original line number corresponding to the character at the specified position on the
   * specified new/flattened line. If there is no entry covering the specified position, the last
   * original line number associated with this flattened line is returned.
   */
  public int getOriginalLine(@Nonnull Integer newLineNumber, @Nonnull Integer newStartingPosition) {
    NavigableMap<Integer, Integer> wordMap = _lineMap.get(newLineNumber);
    if (wordMap == null) {
      /*
       * Result from looking up an unmapped line, this handles lines like the header inserted after
       * flattening
       */
      return UNMAPPED_LINE_NUMBER;
    } else {
      Entry<Integer, Integer> originalLineEntry = wordMap.floorEntry(newStartingPosition);
      /*
       * Default to the last entry if there is no corresponding entry (e.g. looking up original
       * line for a word like 'set' that did not exist in the original config should still give
       * useful output)
       */
      return (originalLineEntry == null)
          ? wordMap.lastEntry().getValue()
          : originalLineEntry.getValue();
    }
  }

  /**
   * Set the original line corresponding to the word starting at the specified position on the
   * specified new/flattened line
   */
  public void setOriginalLine(
      @Nonnull Integer newLineNumber,
      @Nonnull Integer newStartingPosition,
      @Nonnull Integer originalLineNumber) {
    SortedMap<Integer, Integer> wordMap =
        _lineMap.computeIfAbsent(newLineNumber, l -> new TreeMap<>());
    wordMap.put(newStartingPosition, originalLineNumber);
  }

  /**
   * Extra original lines associated with an original line. This mapping may be both incomplete and
   * an overapproximation. For every structure definition whose lines cannot be determined from the
   * flattened parse tree, the keys should include the original line of at least one surviving token
   * of that structure definition. The corresponding value should include at minimum all original
   * lines of the corresponding structure definition that cannot be determined from the flattened
   * parse tree. There may be extra key and value lines, but they will not cause an
   * overapproximation of the original lines associated with any structure.
   */
  public @Nonnull Map<Integer, Set<Integer>> getExtraLines() {
    return _extraLines;
  }

  /**
   * Associate some original line with some extra original lines. If the original line is part of a
   * structure definition, all extra lines to associate must be part of that definition.
   *
   * <p>Should only be called during preprocessing.
   */
  public void setExtraLines(int originalLine, Set<Integer> extraLines) {
    _extraLines.put(originalLine, extraLines);
  }
}
