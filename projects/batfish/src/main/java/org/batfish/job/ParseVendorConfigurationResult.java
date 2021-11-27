package org.batfish.job;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Throwables;
import com.google.common.collect.Multimap;
import com.ibm.icu.impl.locale.XCldrStub.ImmutableMap;
import java.io.File;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ErrorDetails;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.job.ParseVendorConfigurationJob.FileResult;
import org.batfish.vendor.VendorConfiguration;

public class ParseVendorConfigurationResult
    extends BatfishJobResult<
        Map<String, VendorConfiguration>, ParseVendorConfigurationAnswerElement> {

  /** Information about duplicate hostnames is collected here */
  private Multimap<String, String> _duplicateHostnames;

  private final Map<String, FileResult> _fileResults;
  private final String _representativeFilename;

  private final @Nonnull ConfigurationFormat _format;

  private final ParseStatus _status;

  private VendorConfiguration _vc;

  @Nonnull private final Warnings _warnings;

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      @Nonnull Map<String, FileResult> fileResults,
      @Nonnull String representativeFilename,
      @Nonnull ConfigurationFormat format,
      @Nonnull Warnings warnings,
      @Nonnull Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    checkArgument(
        fileResults.containsKey(representativeFilename),
        "Representative filename %s is not present in fileResults",
        representativeFilename);
    _fileResults = ImmutableMap.copyOf(fileResults);
    _representativeFilename = representativeFilename;
    _format = format;
    _status = ParseStatus.FAILED;
    _warnings = warnings;
  }

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      @Nonnull Map<String, FileResult> fileResults,
      @Nonnull String representativeFilename,
      @Nonnull ConfigurationFormat format,
      VendorConfiguration vc,
      @Nonnull Warnings warnings,
      @Nonnull ParseStatus status,
      @Nonnull Multimap<String, String> duplicateHostnames) {
    super(elapsedTime, history);
    checkArgument(
        fileResults.containsKey(representativeFilename),
        "Representative filename %s is not present in fileResults",
        representativeFilename);
    _fileResults = fileResults;
    _representativeFilename = representativeFilename;
    _format = format;
    _vc = vc;
    _warnings = warnings;
    _status = status;
    _duplicateHostnames = duplicateHostnames;
  }

  public ParseVendorConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      @Nonnull Map<String, FileResult> fileResults,
      @Nonnull String representativeFilename,
      @Nonnull ConfigurationFormat format,
      @Nonnull Warnings warnings,
      @Nonnull ParseStatus status) {
    super(elapsedTime, history);
    checkArgument(
        fileResults.containsKey(representativeFilename),
        "Representative filename %s is not present in fileResults",
        representativeFilename);
    _fileResults = fileResults;
    _representativeFilename = representativeFilename;
    _format = format;
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
      terseLogLevelPrefix = _fileResults + ": ";
    }
    logger.append(_history, terseLogLevelPrefix);
  }

  @Override
  public void applyTo(
      Map<String, VendorConfiguration> vendorConfigurations,
      BatfishLogger logger,
      ParseVendorConfigurationAnswerElement answerElement) {
    appendHistory(logger);
    _fileResults
        .keySet()
        .forEach(
            name -> {
              answerElement.getParseStatus().put(name, _status);
              answerElement.getFileFormats().put(name, _format);
            });
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
      answerElement.getFileMap().putAll(hostname, _fileResults.keySet());
      if (!_warnings.isEmpty()) {
        answerElement.getWarnings().put(_representativeFilename, _warnings);
      }
      _fileResults.forEach(
          (name, result) -> {
            if (!result.getParseTreeSentences().isEmpty()) {
              answerElement.getParseTrees().put(name, result.getParseTreeSentences());
            }
          });
    } else if (_status == ParseStatus.FAILED) {
      assert _failureCause != null; // status == FAILED, failureCause must be non-null
      answerElement
          .getErrors()
          .put(_representativeFilename, ((BatfishException) _failureCause).getBatfishStackTrace());
      ErrorDetails errorDetails = _warnings.getErrorDetails();
      // Pass existing errorDetails through, if applicable (e.g. exception caught while walking
      // parse tree and details [including parser context] already populated)
      if (errorDetails != null) {
        answerElement.getErrorDetails().put(_representativeFilename, errorDetails);
      } else {
        answerElement
            .getErrorDetails()
            .put(
                _representativeFilename,
                new ErrorDetails(
                    Throwables.getStackTraceAsString(
                        firstNonNull(_failureCause.getCause(), _failureCause))));
      }
    }
  }

  public Map<String, FileResult> getFileResults() {
    return _fileResults;
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

  @Nonnull
  public ParseStatus getStatus() {
    return _status;
  }

  /** Returns a modified host name to use when duplicate hostnames are encountered */
  public static String getModifiedNameBase(String baseName, String filename) {
    return baseName + "__" + filename.replaceAll(File.separator, "__");
  }

  public VendorConfiguration getVendorConfiguration() {
    return _vc;
  }

  @Nonnull
  public ConfigurationFormat getConfigurationFormat() {
    return _format;
  }

  @Override
  public String toString() {
    if (_vc == null) {
      return "<EMPTY OR UNSUPPORTED FORMAT>";
    } else if (_vc.getHostname() == null) {
      return "<Indeterminate hostname in " + _fileResults + ">";
    } else {
      return "<" + _vc.getHostname() + ">";
    }
  }
}
