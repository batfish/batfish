package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;

public class DefinedStructureInfo implements Serializable {

  private static final String PROP_DEFINITION_LINES = "definitionLines";
  private static final String PROP_NUM_REFERRERS = "numReferrers";

  @Nonnull private IntegerSpace _definitionLines;
  private int _numReferrers;

  @JsonCreator
  public DefinedStructureInfo(
      @JsonProperty(PROP_DEFINITION_LINES) IntegerSpace definitionLines,
      @JsonProperty(PROP_NUM_REFERRERS) Integer numReferrers) {
    checkArgument(numReferrers != null, "Missing %s", PROP_NUM_REFERRERS);
    _definitionLines = firstNonNull(definitionLines, IntegerSpace.EMPTY);
    _numReferrers = numReferrers;
  }

  @JsonProperty(PROP_DEFINITION_LINES)
  public @Nonnull IntegerSpace getDefinitionLines() {
    return _definitionLines;
  }

  public void addDefinitionLines(int line) {
    if (_definitionLines.contains(line)) {
      return;
    }
    _definitionLines = _definitionLines.toBuilder().including(line).build();
  }

  public void addDefinitionLines(int... lines) {
    _definitionLines = _definitionLines.toBuilder().including(lines).build();
  }

  @JsonProperty(PROP_NUM_REFERRERS)
  public int getNumReferrers() {
    return _numReferrers;
  }

  public void setNumReferrers(int numReferrers) {
    _numReferrers = numReferrers;
  }
}
