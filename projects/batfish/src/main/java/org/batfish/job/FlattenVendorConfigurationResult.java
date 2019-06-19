package org.batfish.job;

import java.nio.file.Path;
import java.util.Map;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.datamodel.answers.FlattenVendorConfigurationAnswerElement;

public class FlattenVendorConfigurationResult
    extends BatfishJobResult<Map<Path, String>, FlattenVendorConfigurationAnswerElement> {

  private final String _flattenedText;

  private final Path _outputFile;

  public FlattenVendorConfigurationResult(
      long elapsedTime, BatfishLoggerHistory history, Path outputFile, String flattenedText) {
    super(elapsedTime, history);
    _outputFile = outputFile;
    _flattenedText = flattenedText;
  }

  public FlattenVendorConfigurationResult(
      long elapsedTime, BatfishLoggerHistory history, Path outputFile, Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _outputFile = outputFile;
    _flattenedText = null;
  }

  @Override
  public void appendHistory(BatfishLogger logger) {
    String terseLogLevelPrefix;
    if (logger.isActive(BatfishLogger.LEVEL_INFO)) {
      terseLogLevelPrefix = "";
    } else {
      terseLogLevelPrefix = _outputFile.getFileName() + ": ";
    }
    logger.append(_history, terseLogLevelPrefix);
  }

  @Override
  public void applyTo(
      Map<Path, String> outputConfigurationData,
      BatfishLogger logger,
      FlattenVendorConfigurationAnswerElement answerElement) {
    appendHistory(logger);
    outputConfigurationData.put(_outputFile, _flattenedText);
  }

  public String getFlattenedText() {
    return _flattenedText;
  }

  @Override
  public BatfishLoggerHistory getHistory() {
    return _history;
  }

  public Path getOutputFile() {
    return _outputFile;
  }
}
