package org.batfish.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Warning extends Pair<String, String> {

  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_TAG = "tag";

  private static final String PROP_TEXT = "text";

  @JsonCreator
  public Warning(@JsonProperty(PROP_TEXT) String text, @JsonProperty(PROP_TAG) String tag) {
    super(text, tag);
  }

  @JsonProperty(PROP_TAG)
  public String getTag() {
    return _second;
  }

  @JsonProperty(PROP_TEXT)
  public String getText() {
    return _first;
  }
}
