package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.io.Serializable;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.BatfishException;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConfigurationFormat;

public class ParseVendorConfigurationAnswerElement extends ParseAnswerElement
    implements Serializable {

  private static final String PROP_FILE_MAP = "fileMap";
  private static final String PROP_FILE_FORMATS = "fileFormats";
  private static final String PROP_VERSION = "version";

  private SortedMap<String, BatfishException.BatfishStackTrace> _errors;

  /* Map of hostname to source filenames (e.g. "configs/foo.cfg") */
  private Multimap<String, String> _fileMap;

  /** Map of filename to detected {@link ConfigurationFormat format}. */
  private SortedMap<String, ConfigurationFormat> _fileFormats;

  private SortedMap<String, ErrorDetails> _errorDetails;

  private SortedMap<String, ParseStatus> _parseStatus;

  private SortedMap<String, ParseTreeSentences> _parseTrees;

  private String _version;

  /* Map of filename to warnings */
  private SortedMap<String, Warnings> _warnings;

  public ParseVendorConfigurationAnswerElement() {
    _fileMap = TreeMultimap.create();
    _fileFormats = new TreeMap<>();
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

  @JsonProperty(PROP_FILE_FORMATS)
  public Map<String, ConfigurationFormat> getFileFormats() {
    return _fileFormats;
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
  public void setErrors(SortedMap<String, BatfishException.BatfishStackTrace> errors) {
    _errors = errors;
  }

  @JsonProperty(PROP_FILE_MAP)
  public void setFileMap(Multimap<String, String> fileMap) {
    _fileMap = fileMap;
  }

  @JsonProperty(PROP_FILE_FORMATS) // only for Jackson
  private void setFileFormats(SortedMap<String, ConfigurationFormat> fileFormats) {
    _fileFormats = fileFormats;
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
