package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.io.Serializable;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;

public class DefinedStructureInfo implements Serializable {

  private static final String PROP_DEFINITION_LINES = "definitionLines";
  private static final String PROP_NUM_REFERRERS = "numReferrers";

  @Nonnull private final RangeSet<Integer> _definitionLines;
  private int _numReferrers;

  public DefinedStructureInfo() {
    this(TreeRangeSet.create(), 0);
  }

  public DefinedStructureInfo(@Nonnull RangeSet<Integer> lines, int numReferrers) {
    _definitionLines = lines;
    _numReferrers = numReferrers;
  }

  @JsonCreator
  private static DefinedStructureInfo jsonCreator(
      @JsonProperty(PROP_DEFINITION_LINES) IntegerSpace definitionLines,
      @JsonProperty(PROP_NUM_REFERRERS) Integer numReferrers) {
    checkArgument(numReferrers != null, "Missing %s", PROP_NUM_REFERRERS);
    return new DefinedStructureInfo(definitionLines.getRangeSet(), numReferrers);
  }

  @JsonProperty(PROP_DEFINITION_LINES)
  public @Nonnull IntegerSpace getDefinitionLines() {
    return IntegerSpace.of(_definitionLines);
  }

  public void addDefinitionLines(int line) {
    _definitionLines.add(Range.singleton(line));
  }

  public void addDefinitionLines(IntStream lines) {
    lines.forEach(l -> _definitionLines.add(Range.singleton(l)));
  }

  public void addDefinitionLines(Range<Integer> lines) {
    _definitionLines.add(lines);
  }

  @JsonProperty(PROP_NUM_REFERRERS)
  public int getNumReferrers() {
    return _numReferrers;
  }

  public void setNumReferrers(int numReferrers) {
    _numReferrers = numReferrers;
  }
}
