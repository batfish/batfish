package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;

public class ParseVendorConfigurationAnswerElement extends ParseAnswerElement
    implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final String PROP_FILE_MAP = "fileMap";

  private static final String PROP_VERSION = "version";

  private SortedMap<String, BatfishException.BatfishStackTrace> _errors;

  /* Map of hostname to source filenames (e.g. "configs/foo.cfg") */
  private Multimap<String, String> _fileMap;

  private SortedMap<String, ErrorDetails> _errorDetails;

  private SortedMap<String, ParseStatus> _parseStatus;

  private SortedMap<String, ParseTreeSentences> _parseTrees;

  private String _version;

  /* Map of filename to warnings */
  private SortedMap<String, Warnings> _warnings;

  public ParseVendorConfigurationAnswerElement() {
    _fileMap = TreeMultimap.create();
    _parseStatus = new TreeMap<>();
    _parseTrees = new TreeMap<>();
    _warnings = new TreeMap<>();
    _errors = new TreeMap<>();
    _errorDetails = new TreeMap<>();
  }

  public void addRedFlagWarning(String name, Warning warning) {
    _warnings.computeIfAbsent(name, n -> new Warnings()).getRedFlagWarnings().add(warning);
  }

  public void addUnimplementedWarning(String name, Warning warning) {
    _warnings.computeIfAbsent(name, n -> new Warnings()).getUnimplementedWarnings().add(warning);
  }

  @Override
  public SortedMap<String, BatfishException.BatfishStackTrace> getErrors() {
    return _errors;
  }

  @Override
  public SortedMap<String, ErrorDetails> getErrorDetails() {
    return _errorDetails;
  }

  @JsonProperty(PROP_FILE_MAP)
  public Multimap<String, String> getFileMap() {
    return _fileMap;
  }

  @Override
  public SortedMap<String, ParseStatus> getParseStatus() {
    return _parseStatus;
  }

  @Override
  public SortedMap<String, ParseTreeSentences> getParseTrees() {
    return _parseTrees;
  }

  @JsonProperty(PROP_VERSION)
  public String getVersion() {
    return _version;
  }

  @Override
  public SortedMap<String, Warnings> getWarnings() {
    return _warnings;
  }

  @Override
  public String prettyPrint() {
    StringBuilder retString = new StringBuilder("Results of parsing vendor configurations\n");

    for (String name : _warnings.keySet()) {
      retString.append("\n  " + name + "[Parser warnings]\n");
      for (Warning warning : _warnings.get(name).getRedFlagWarnings()) {
        retString.append("    RedFlag " + warning.getTag() + " : " + warning.getText() + "\n");
      }
      for (Warning warning : _warnings.get(name).getUnimplementedWarnings()) {
        retString.append(
            "    Unimplemented " + warning.getTag() + " : " + warning.getText() + "\n");
      }
      for (Warning warning : _warnings.get(name).getPedanticWarnings()) {
        retString.append("    Pedantic " + warning.getTag() + " : " + warning.getText() + "\n");
      }
    }
    for (String name : _errors.keySet()) {
      retString.append("\n  " + name + "[Parser errors]\n");
      for (String line : _errors.get(name).getLineMap()) {
        retString.append("    " + line + "\n");
      }
    }
    for (String name : _parseTrees.keySet()) {
      retString.append("\n  " + name + " [Parse trees]\n");
      for (String sentence : _parseTrees.get(name).getSentences()) {
        retString.append("    ParseTreeSentence : " + sentence + "\n");
      }
    }

    return retString.toString();
  }

  @Override
  public void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors) {
    _errors = errors;
  }

  @JsonProperty(PROP_FILE_MAP)
  public void setFileMap(Multimap<String, String> fileMap) {
    _fileMap = fileMap;
  }

  @Override
  public void setParseStatus(SortedMap<String, ParseStatus> parseStatus) {
    _parseStatus = parseStatus;
  }

  @Override
  public void setParseTrees(SortedMap<String, ParseTreeSentences> parseTrees) {
    _parseTrees = parseTrees;
  }

  @JsonProperty(PROP_VERSION)
  public void setVersion(String version) {
    _version = version;
  }

  @Override
  public void setWarnings(SortedMap<String, Warnings> warnings) {
    _warnings = warnings;
  }
}
