package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

public class DefinedStructureInfo implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_DEFINITION_LINES = "definitionLines";
  private static final String PROP_NUM_REFERRERS = "numReferrers";

  private SortedSet<Integer> _definitionLines;
  private int _numReferrers;

  @JsonCreator
  public DefinedStructureInfo(
      @JsonProperty(PROP_DEFINITION_LINES) SortedSet<Integer> definitionLines,
      @JsonProperty(PROP_NUM_REFERRERS) Integer numReferrers) {
    _definitionLines = MoreObjects.firstNonNull(definitionLines, new TreeSet<>());
    _numReferrers = MoreObjects.firstNonNull(numReferrers, -1);
  }

  @JsonProperty(PROP_DEFINITION_LINES)
  public SortedSet<Integer> getDefinitionLines() {
    return _definitionLines;
  }

  @JsonProperty(PROP_NUM_REFERRERS)
  public int getNumReferrers() {
    return _numReferrers;
  }
}
