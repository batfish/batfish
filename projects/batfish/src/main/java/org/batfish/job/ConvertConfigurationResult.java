package org.batfish.job;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Throwables;
import java.util.Map;
import java.util.Map.Entry;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.ErrorDetails;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.answers.ConvertStatus;

public class ConvertConfigurationResult
    extends BatfishJobResult<Map<String, Configuration>, ConvertConfigurationAnswerElement> {

  private ConvertConfigurationAnswerElement _answerElement;

  private Map<String, Configuration> _configurations;

  private String _name;

  private Map<String, Warnings> _warningsByHost;

  public ConvertConfigurationResult(
      long elapsedTime, BatfishLoggerHistory history, String name, Throwable failureCause) {
    super(elapsedTime, history, failureCause);
    _name = name;
  }

  public ConvertConfigurationResult(
      long elapsedTime,
      BatfishLoggerHistory history,
      Map<String, Warnings> warningsByHost,
      String name,
      Map<String, Configuration> configurations,
      ConvertConfigurationAnswerElement answerElement) {
    super(elapsedTime, history);
    _name = name;
    _warningsByHost = warningsByHost;
    _configurations = configurations;
    _answerElement = answerElement;
  }

  @Override
  public void appendHistory(BatfishLogger logger) {
    String terseLogLevelPrefix;
    if (logger.isActive(BatfishLogger.LEVEL_INFO)) {
      terseLogLevelPrefix = "";
    } else {
      terseLogLevelPrefix = _name + ": ";
    }
    logger.append(_history, terseLogLevelPrefix);
  }

  @Override
  public void applyTo(
      Map<String, Configuration> configurations,
      BatfishLogger logger,
      ConvertConfigurationAnswerElement answerElement) {
    appendHistory(logger);
    if (_configurations != null) {
      for (Entry<String, Configuration> hostConfig : _configurations.entrySet()) {
        String hostname = hostConfig.getKey();
        Configuration config = hostConfig.getValue();
        if (configurations.containsKey(hostname)) {
          throw new BatfishException("Duplicate hostname: " + hostname);
        } else {
          configurations.put(hostname, config);
          if (_warningsByHost.containsKey(hostname) && !_warningsByHost.get(hostname).isEmpty()) {
            answerElement.getWarnings().put(hostname, _warningsByHost.get(hostname));
            answerElement.getConvertStatus().put(_name, ConvertStatus.WARNINGS);
          } else {
            answerElement.getConvertStatus().put(_name, ConvertStatus.PASSED);
          }
          answerElement.getDefinedStructures().putAll(_answerElement.getDefinedStructures());
          answerElement.getUndefinedReferences().putAll(_answerElement.getUndefinedReferences());
          answerElement.getReferencedStructures().putAll(_answerElement.getReferencedStructures());
          answerElement.getFileMap().putAll(_answerElement.getFileMap());
        }
      }
    } else {
      answerElement.getConvertStatus().put(_name, ConvertStatus.FAILED);
      answerElement
          .getErrors()
          .put(_name, ((BatfishException) _failureCause).getBatfishStackTrace());
      answerElement
          .getErrorDetails()
          .put(
              _name,
              new ErrorDetails(
                  Throwables.getStackTraceAsString(
                      firstNonNull(_failureCause.getCause(), _failureCause))));
    }
  }

  public Map<String, Configuration> getConfigurations() {
    return _configurations;
  }

  @Override
  public BatfishLoggerHistory getHistory() {
    return _history;
  }

  public String getName() {
    return _name;
  }

  @Override
  public String toString() {
    if (_configurations != null) {
      return _configurations.keySet().toString();
    } else {
      return "<EMPTY>";
    }
  }
}
