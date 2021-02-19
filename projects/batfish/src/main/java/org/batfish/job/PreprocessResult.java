package org.batfish.job;

import java.nio.file.Path;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.datamodel.answers.AnswerElement;

/** Result storing output of {@link PreprocessJob}. */
@ParametersAreNonnullByDefault
public final class PreprocessResult extends BatfishJobResult<Map<Path, String>, AnswerElement> {

  private final @Nonnull Path _outputFile;
  private final @Nullable String _outputText;

  public PreprocessResult(
      long elapsedTime, BatfishLoggerHistory history, Path outputFile, String outputText) {
    super(elapsedTime, history);
    _outputFile = outputFile;
    _outputText = outputText;
  }

  public PreprocessResult(
      long elapsedTime, BatfishLoggerHistory history, Path outputFile, Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _outputFile = outputFile;
    _outputText = null;
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
      AnswerElement answerElement) {
    appendHistory(logger);
    outputConfigurationData.put(_outputFile, _outputText);
  }

  @Override
  public BatfishLoggerHistory getHistory() {
    return _history;
  }

  public Path getOutputFile() {
    return _outputFile;
  }

  public String getOutputText() {
    return _outputText;
  }
}
