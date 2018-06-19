package org.batfish.grammar;

public interface Flattener {
  /** Return a string corresponding to the text of the flattened config */
  String getFlattenedConfigurationText();

  /**
   * Return map of new line number to word map, where word map is map of position in new line to
   * original line number.
   */
  FlattenerLineMap getOriginalLineMap();
}
