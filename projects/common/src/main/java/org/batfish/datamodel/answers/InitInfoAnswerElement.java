package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException.BatfishStackTrace;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;

public class InitInfoAnswerElement extends AnswerElement {
  private static final String PROP_ERRORS = "errors";
  private static final String PROP_PARSE_STATUS = "parseStatus";
  private static final String PROP_PARSE_TREES = "parseTrees";
  private static final String PROP_WARNINGS = "warnings";

  private SortedMap<String, List<BatfishStackTrace>> _errors;

  private SortedMap<String, ParseStatus> _parseStatus;

  private SortedMap<String, ParseTreeSentences> _parseTrees;

  private SortedMap<String, Warnings> _warnings;

  @JsonCreator
  public InitInfoAnswerElement() {
    _parseStatus = new TreeMap<>();
    _warnings = new TreeMap<>();
    _errors = new TreeMap<>();
  }

  @JsonProperty(PROP_ERRORS)
  public SortedMap<String, List<BatfishStackTrace>> getErrors() {
    return _errors;
  }

  @JsonProperty(PROP_PARSE_STATUS)
  public SortedMap<String, ParseStatus> getParseStatus() {
    return _parseStatus;
  }

  @JsonProperty(PROP_PARSE_TREES)
  public SortedMap<String, ParseTreeSentences> getParseTrees() {
    return _parseTrees;
  }

  @JsonProperty(PROP_WARNINGS)
  public SortedMap<String, Warnings> getWarnings() {
    return _warnings;
  }

  @JsonProperty(PROP_ERRORS)
  public void setErrors(SortedMap<String, List<BatfishStackTrace>> errors) {
    _errors = errors;
  }

  @JsonProperty(PROP_PARSE_STATUS)
  public void setParseStatus(SortedMap<String, ParseStatus> parseStatus) {
    _parseStatus = parseStatus;
  }

  @JsonProperty(PROP_PARSE_TREES)
  public void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees) {
    _parseTrees = parseTrees;
  }

  @JsonProperty(PROP_WARNINGS)
  public void setWarnings(SortedMap<String, Warnings> warnings) {
    _warnings = warnings;
  }
}
