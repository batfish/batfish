package org.batfish.job;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Map;
import javax.annotation.Nonnull;
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

  private final String _filename;

  private ParseTreeSentences _parseTree;

  private final ParseStatus _status;

  private VendorConfiguration _vc;

  private Warnings _warnings;

  private static Multimap<String, String> _duplicateHostnames = HashMultimap.create();

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      String filename,
      @Nonnull Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _filename = filename;
    _status = ParseStatus.FAILED;
  }

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      String filename,
      VendorConfiguration vc,
      Warnings warnings,
      ParseTreeSentences parseTree) {
    super(elapsedTime, history);
    _filename = filename;
    _parseTree = parseTree;
    _vc = vc;
    _warnings = warnings;
    // parse status is determined from other fields
    _status = null;
  }

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      String filename,
      Warnings warnings,
      ParseStatus status) {
    super(elapsedTime, history);
    _filename = filename;
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
      terseLogLevelPrefix = _filename + ": ";
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
        /*
         * Modify the hostname of what is already in the vendorConfigurations map. Ideally, we'd add
         * a warning but the getWarnings object around here is null
         */
        VendorConfiguration oldVc = vendorConfigurations.get(hostname);
        String modifiedOldName = getModifiedName(hostname, oldVc.getFilename());
        oldVc.setHostname(modifiedOldName);
        vendorConfigurations.remove(hostname);
        vendorConfigurations.put(modifiedOldName, oldVc);
        _duplicateHostnames.put(hostname, modifiedOldName);
      }
      if (_duplicateHostnames.containsKey(hostname)) {
        String modifiedNewName = getModifiedName(hostname, _vc.getFilename());
        _warnings.redFlag(
            String.format("Duplicate hostname %s. Changed to %s", hostname, modifiedNewName));
        _vc.setHostname(modifiedNewName);
        _duplicateHostnames.put(hostname, modifiedNewName);
        hostname = modifiedNewName;
      }
      vendorConfigurations.put(hostname, _vc);
      answerElement.getFileMap().put(hostname, _filename);
      if (!_warnings.isEmpty()) {
        answerElement.getWarnings().put(hostname, _warnings);
      }
      if (!_parseTree.isEmpty()) {
        answerElement.getParseTrees().put(hostname, _parseTree);
      }
      if (_vc.getUnrecognized()) {
        answerElement.getParseStatus().put(_filename, ParseStatus.PARTIALLY_UNRECOGNIZED);
      } else {
        answerElement.getParseStatus().put(_filename, ParseStatus.PASSED);
      }

    } else {
      answerElement.getParseStatus().put(_filename, _status);
      if (_status == ParseStatus.FAILED) {
        assert _failureCause != null; // status == FAILED, failureCause must be non-null
        answerElement
            .getErrors()
            .put(_filename, ((BatfishException) _failureCause).getBatfishStackTrace());
      }
    }
  }

  public String getFilename() {
    return _filename;
  }

  @Override
  public BatfishLoggerHistory getHistory() {
    return _history;
  }

  private static String getModifiedName(String baseName, String filename) {
    String modifiedName = baseName + "__file__" + filename;
    int index = 0;
    while (_duplicateHostnames.containsEntry(baseName, modifiedName)) {
      modifiedName = baseName + "__file__" + filename + "." + index;
      index++;
    }
    return modifiedName;
  }

  public VendorConfiguration getVendorConfiguration() {
    return _vc;
  }

  @Override
  public String toString() {
    if (_vc == null) {
      return "<EMPTY OR UNSUPPORTED FORMAT>";
    } else if (_vc.getHostname() == null) {
      return "<File: \"" + _filename + "\" has indeterminate hostname>";
    } else {
      return "<" + _vc.getHostname() + ">";
    }
  }
}
