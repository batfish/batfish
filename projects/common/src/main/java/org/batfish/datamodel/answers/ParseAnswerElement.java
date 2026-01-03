package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import org.batfish.common.ParseTreeSentences;

public abstract class ParseAnswerElement extends InitStepAnswerElement {

  protected static final String PROP_PARSE_STATUS = "parseStatus";
  protected static final String PROP_PARSE_TREES = "parseTrees";

  @JsonProperty(PROP_PARSE_STATUS)
  public abstract SortedMap<String, ParseStatus> getParseStatus();

  @JsonProperty(PROP_PARSE_TREES)
  public abstract SortedMap<String, ParseTreeSentences> getParseTrees();

  @JsonProperty(PROP_PARSE_STATUS)
  public abstract void setParseStatus(SortedMap<String, ParseStatus> parseStatus);

  @JsonProperty(PROP_PARSE_TREES)
  public abstract void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees);
}
