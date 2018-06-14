package org.batfish.grammar.flattener;

import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlattenerLineMap {
  /**
   * Map of new line number to word map, where word map is map of starting-position in new/flattened
   * line to original line number
   */
  private SortedMap<Integer, SortedMap<Integer, Integer>> _lineMap;

  public FlattenerLineMap() {
    _lineMap = new TreeMap<>();
  }

  /**
   * Return original line number corresponding to the word starting at the specified position on the
   * specified new/flattened line. If there is no word starting at the specified position, the last
   * original line number associated with this flattened line is returned.
   */
  public @Nullable Integer getOriginalLine(
      @Nonnull Integer newLineNumber, @Nonnull Integer newStartingPosition) {
    SortedMap<Integer, Integer> wordMap = _lineMap.get(newLineNumber);
    // return (wordMap == null) ? null : wordMap.get(newStartingPosition);
    if (wordMap == null) {
      // Should never get here
      return null;
    } else {
      Integer originalLine = wordMap.get(newStartingPosition);
      return (originalLine == null) ? wordMap.get(wordMap.lastKey()) : originalLine;
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
}
