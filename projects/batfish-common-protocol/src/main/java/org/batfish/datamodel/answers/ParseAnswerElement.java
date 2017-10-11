package org.batfish.datamodel.answers;

import java.util.SortedMap;
import org.batfish.common.BatfishException;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;

public interface ParseAnswerElement extends AnswerElement {

  SortedMap<String, BatfishException.BatfishStackTrace> getErrors();

  SortedMap<String, ParseStatus> getParseStatus();

  SortedMap<String, ParseTreeSentences> getParseTrees();

  SortedMap<String, Warnings> getWarnings();

  void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors);

  void setParseStatus(SortedMap<String, ParseStatus> parseStatus);

  void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees);

  void setWarnings(SortedMap<String, Warnings> warnings);
}
