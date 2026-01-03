package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;

public class ParseEnvironmentBgpTablesAnswerElement extends ParseAnswerElement
    implements Serializable {

  private SortedMap<String, BatfishException.BatfishStackTrace> _errors;

  private SortedMap<String, ErrorDetails> _errorDetails;

  private SortedMap<String, ParseStatus> _parseStatus;

  private SortedMap<String, ParseTreeSentences> _parseTrees;

  private String _version;

  private SortedMap<String, Warnings> _warnings;

  public ParseEnvironmentBgpTablesAnswerElement() {
    _errors = new TreeMap<>();
    _errorDetails = new TreeMap<>();
    _parseStatus = new TreeMap<>();
    _parseTrees = new TreeMap<>();
    _warnings = new TreeMap<>();
  }

  @Override
  public SortedMap<String, BatfishException.BatfishStackTrace> getErrors() {
    return _errors;
  }

  @Override
  public SortedMap<String, ErrorDetails> getErrorDetails() {
    return _errorDetails;
  }

  @Override
  public SortedMap<String, ParseStatus> getParseStatus() {
    return _parseStatus;
  }

  @Override
  public SortedMap<String, ParseTreeSentences> getParseTrees() {
    return _parseTrees;
  }

  public String getVersion() {
    return _version;
  }

  @Override
  public SortedMap<String, Warnings> getWarnings() {
    return _warnings;
  }

  @Override
  public void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors) {
    _errors = errors;
  }

  @Override
  public void setParseStatus(SortedMap<String, ParseStatus> parseStatus) {
    _parseStatus = parseStatus;
  }

  @Override
  public void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees) {
    _parseTrees = parseTrees;
  }

  public void setVersion(String version) {
    _version = version;
  }

  @Override
  public void setWarnings(SortedMap<String, Warnings> warnings) {
    _warnings = warnings;
  }
}
