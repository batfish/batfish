package org.batfish.job;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import java.io.File;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ErrorDetails;
import org.batfish.common.ParseTreeSentences;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.vendor.VendorConfiguration;

public class ParseVendorConfigurationResult
    extends BatfishJobResult<
        Map<String, VendorConfiguration>, ParseVendorConfigurationAnswerElement> {

  /** Information about duplicate hostnames is collected here */
  private Multimap<String, String> _duplicateHostnames;

  private final String _filename;
  private final @Nonnull ConfigurationFormat _format;

  @Nonnull private ParseTreeSentences _parseTree;

  private final ParseStatus _status;

  private VendorConfiguration _vc;

  @Nonnull private final Warnings _warnings;

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      String filename,
      @Nonnull ConfigurationFormat format,
      @Nonnull Warnings warnings,
      @Nonnull ParseTreeSentences parseTree,
      @Nonnull Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _filename = filename;
    _format = format;
    _parseTree = parseTree;
    _status = ParseStatus.FAILED;
    _warnings = warnings;
  }

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      String filename,
      @Nonnull ConfigurationFormat format,
      VendorConfiguration vc,
      @Nonnull Warnings warnings,
      @Nonnull ParseTreeSentences parseTree,
      @Nonnull ParseStatus status,
      @Nonnull Multimap<String, String> duplicateHostnames) {
    super(elapsedTime, history);
    _filename = filename;
    _format = format;
    _parseTree = parseTree;
    _vc = vc;
    _warnings = warnings;
    _status = status;
    _duplicateHostnames = duplicateHostnames;
  }

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      String filename,
      @Nonnull ConfigurationFormat format,
      @Nonnull Warnings warnings,
      @Nonnull ParseStatus status) {
    super(elapsedTime, history);
    _filename = filename;
    _format = format;
    _parseTree = new ParseTreeSentences();
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
    answerElement.getParseStatus().put(_filename, _status);
    answerElement.getFileFormats().put(_filename, _format);
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
        answerElement.getWarnings().put(_filename, _warnings);
      }
      if (!_parseTree.isEmpty()) {
        answerElement.getParseTrees().put(_filename, _parseTree);
      }
    } else if (_status == ParseStatus.FAILED) {
      assert _failureCause != null; // status == FAILED, failureCause must be non-null
      answerElement
          .getErrors()
          .put(_filename, ((BatfishException) _failureCause).getBatfishStackTrace());
      ErrorDetails errorDetails = _warnings.getErrorDetails();
      // Pass existing errorDetails through, if applicable (e.g. exception caught while walking
      // parse tree and details [including parser context] already populated)
      if (errorDetails != null) {
        answerElement.getErrorDetails().put(_filename, errorDetails);
      } else {
        answerElement
            .getErrorDetails()
            .put(
                _filename,
                new ErrorDetails(
                    Throwables.getStackTraceAsString(
                        firstNonNull(_failureCause.getCause(), _failureCause))));
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

  private String getModifiedName(String baseName, String filename) {
    String modifiedName = getModifiedNameBase(baseName, filename);
    int index = 0;
    while (_duplicateHostnames.containsEntry(baseName, modifiedName)) {
      modifiedName = getModifiedNameBase(baseName, filename) + "." + index;
      index++;
    }
    return modifiedName;
  }

  /** Returns a modified host name to use when duplicate hostnames are encountered */
  public static String getModifiedNameBase(String baseName, String filename) {
    return baseName + "__" + filename.replaceAll(File.separator, "__");
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
