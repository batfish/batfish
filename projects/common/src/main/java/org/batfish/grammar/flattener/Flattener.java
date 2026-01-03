package org.batfish.grammar.flattener;

public interface Flattener {
  /** Return a string corresponding to the text of the flattened config */
  String getFlattenedConfigurationText();

  /** Return FlattenerLineMap for the flattened config */
  FlattenerLineMap getOriginalLineMap();
}
