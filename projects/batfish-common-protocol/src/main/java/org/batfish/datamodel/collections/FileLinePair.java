package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;

public class FileLinePair extends Pair<String, Integer> {

  private static final String PROP_FILENAME = "filename";

  private static final String PROP_LINENUMBER = "linenumber";
  /** */
  private static final long serialVersionUID = 1L;

  @JsonCreator
  public FileLinePair(
      @JsonProperty(PROP_FILENAME) String filename,
      @JsonProperty(PROP_LINENUMBER) Integer linenumber) {
    super(filename, linenumber);
  }

  @JsonProperty(PROP_FILENAME)
  public String getFilename() {
    return _first;
  }

  @JsonProperty(PROP_LINENUMBER)
  public Integer getInterface() {
    return _second;
  }

  @Override
  public String toString() {
    return _first + ":" + _second;
  }
}
