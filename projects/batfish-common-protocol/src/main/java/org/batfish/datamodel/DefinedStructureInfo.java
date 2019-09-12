package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;

public class DefinedStructureInfo implements Serializable {

  private static final String PROP_DEFINITION_LINES = "definitionLines";
  private static final String PROP_NUM_REFERRERS = "numReferrers";

  @Nonnull private SortedSet<Integer> _definitionLines;
  private int _numReferrers;

  @JsonCreator
  public DefinedStructureInfo(
      @JsonProperty(PROP_DEFINITION_LINES) SortedSet<Integer> definitionLines,
      @JsonProperty(PROP_NUM_REFERRERS) Integer numReferrers) {
    checkArgument(numReferrers != null, "Missing %s", PROP_NUM_REFERRERS);
    _definitionLines = firstNonNull(definitionLines, new TreeSet<>());
    _numReferrers = numReferrers;
  }

  @JsonProperty(PROP_DEFINITION_LINES)
  public SortedSet<Integer> getDefinitionLines() {
    return _definitionLines;
  }

  @JsonProperty(PROP_NUM_REFERRERS)
  public int getNumReferrers() {
    return _numReferrers;
  }

  public void setNumReferrers(int numReferrers) {
    _numReferrers = numReferrers;
  }
}
