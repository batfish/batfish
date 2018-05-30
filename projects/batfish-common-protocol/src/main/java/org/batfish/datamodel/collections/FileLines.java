package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedSet;
import scala.Tuple2;

public class FileLines extends Tuple2<String, SortedSet<Integer>> {

  private static final String PROP_FILENAME = "filename";
  private static final String PROP_LINES = "lines";

  @JsonCreator
  public FileLines(
      @JsonProperty(PROP_FILENAME) String filename,
      @JsonProperty(PROP_LINES) SortedSet<Integer> lines) {
    super(filename, lines);
  }

  @JsonProperty(PROP_FILENAME)
  public String getFilename() {
    return _1;
  }

  @JsonProperty(PROP_LINES)
  public SortedSet<Integer> getLines() {
    return _2;
  }

  @Override
  public String toString() {
    return _1 + ":" + _2;
  }
}
