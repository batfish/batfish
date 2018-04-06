package org.batfish.job;

import java.nio.file.Path;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.vendor.VendorConfiguration;

public class ParseVendorConfigurationResult
    extends BatfishJobResult<
        Map<String, VendorConfiguration>, ParseVendorConfigurationAnswerElement> {

  private final Path _file;

  private ParseTreeSentences _parseTree;

  private final ParseStatus _status;

  private VendorConfiguration _vc;

  private Warnings _warnings;

  public ParseVendorConfigurationResult(
      long elapsedTime, BatfishLoggerHistory history, Path file, Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _file = file;
    _status = ParseStatus.FAILED;
  }

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      Path file,
      VendorConfiguration vc,
      Warnings warnings,
      ParseTreeSentences parseTree) {
    super(elapsedTime, history);
    _file = file;
    _parseTree = parseTree;
    _vc = vc;
    _warnings = warnings;
    // parse status is determined from other fields
    _status = null;
  }

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      Path file,
      Warnings warnings,
      ParseStatus status) {
    super(elapsedTime, history);
    _file = file;
    _status = status;
    _warnings = warnings;
  }

  @Override
  public void appendHistory(BatfishLogger logger) {
    String terseLogLevelPrefix;
    if (logger.isActive(BatfishLogger.LEVEL_INFO)) {
      terseLogLevelPrefix = "";
    } else if (_vc != null) {
      terseLogLevelPrefix = _vc.getHostname() + ": ";
    } else {
      terseLogLevelPrefix = _file + ": ";
    }
    logger.append(_history, terseLogLevelPrefix);
  }

  @Override
  public void applyTo(
      Map<String, VendorConfiguration> vendorConfigurations,
      BatfishLogger logger,
      ParseVendorConfigurationAnswerElement answerElement) {
    appendHistory(logger);
    if (_vc != null) {
      String hostname = _vc.getHostname();
      if (vendorConfigurations.containsKey(hostname)) {
        int index = 1;
        do {
          hostname = _vc.getHostname() + ".dupname" + index;
          index++;
        } while (vendorConfigurations.containsKey(hostname));
        _warnings.redFlag(
            String.format(
                "Found duplicate hostname %s. Changed to %s", _vc.getHostname(), hostname));
        _vc.setHostname(hostname);
      }
      vendorConfigurations.put(hostname, _vc);
      if (!_warnings.isEmpty()) {
        answerElement.getWarnings().put(hostname, _warnings);
      }
      if (!_parseTree.isEmpty()) {
        answerElement.getParseTrees().put(hostname, _parseTree);
      }
      if (_vc.getUnrecognized()) {
        answerElement.getParseStatus().put(hostname, ParseStatus.PARTIALLY_UNRECOGNIZED);
      } else {
        answerElement.getParseStatus().put(hostname, ParseStatus.PASSED);
        answerElement.getFileMap().put(hostname, _file.getFileName().toString());
      }

    } else {
      String filename = _file.getFileName().toString();
      answerElement.getParseStatus().put(filename, _status);
      if (_status == ParseStatus.FAILED) {
        answerElement
            .getErrors()
            .put(filename, ((BatfishException) _failureCause).getBatfishStackTrace());
      }
    }
  }

  public Path getFile() {
    return _file;
  }

  @Override
  public BatfishLoggerHistory getHistory() {
    return _history;
  }

  public VendorConfiguration getVendorConfiguration() {
    return _vc;
  }

  @Override
  public String toString() {
    if (_vc == null) {
      return "<EMPTY OR UNSUPPORTED FORMAT>";
    } else if (_vc.getHostname() == null) {
      return "<File: \"" + _file + "\" has indeterminate hostname>";
    } else {
      return "<" + _vc.getHostname() + ">";
    }
  }
}
