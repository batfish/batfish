package org.batfish.datamodel.answers;

import java.util.SortedMap;
import org.batfish.common.ParseTreeSentences;

public interface ParseAnswerElement extends InitStepAnswerElement {

  SortedMap<String, ParseStatus> getParseStatus();

  SortedMap<String, ParseTreeSentences> getParseTrees();

  void setParseStatus(SortedMap<String, ParseStatus> parseStatus);

  void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees);
}
