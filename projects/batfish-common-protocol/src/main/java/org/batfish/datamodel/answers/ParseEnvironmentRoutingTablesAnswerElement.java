package org.batfish.datamodel.answers;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;

public class ParseEnvironmentRoutingTablesAnswerElement extends ParseAnswerElement
    implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private SortedMap<String, BatfishException.BatfishStackTrace> _errors;

  private SortedMap<String, ParseStatus> _parseStatus;

  private SortedMap<String, ParseTreeSentences> _parseTrees;

  private String _version;

  private SortedMap<String, Warnings> _warnings;

  public ParseEnvironmentRoutingTablesAnswerElement() {
    _errors = new TreeMap<>();
    _parseStatus = new TreeMap<>();
    _parseTrees = new TreeMap<>();
    _warnings = new TreeMap<>();
  }

  public SortedMap<String, BatfishException.BatfishStackTrace> getErrors() {
    return _errors;
  }

  public SortedMap<String, ParseStatus> getParseStatus() {
    return _parseStatus;
  }

  public SortedMap<String, ParseTreeSentences> getParseTrees() {
    return _parseTrees;
  }

  public String getVersion() {
    return _version;
  }

  public SortedMap<String, Warnings> getWarnings() {
    return _warnings;
  }

  public void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors) {
    _errors = errors;
  }

  public void setParseStatus(SortedMap<String, ParseStatus> parseStatus) {
    _parseStatus = parseStatus;
  }

  public void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees) {
    _parseTrees = parseTrees;
  }

  public void setVersion(String version) {
    _version = version;
  }

  public void setWarnings(SortedMap<String, Warnings> warnings) {
    _warnings = warnings;
  }
}
