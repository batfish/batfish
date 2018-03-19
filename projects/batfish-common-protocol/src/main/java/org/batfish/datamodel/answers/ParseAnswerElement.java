package org.batfish.datamodel.answers;

import java.util.SortedMap;
import org.batfish.common.ParseTreeSentences;

public abstract class ParseAnswerElement extends InitStepAnswerElement {

  public abstract SortedMap<String, ParseStatus> getParseStatus();

  public abstract SortedMap<String, ParseTreeSentences> getParseTrees();

  public abstract void setParseStatus(SortedMap<String, ParseStatus> parseStatus);

  public abstract void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees);
}
