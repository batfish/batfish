package org.batfish.job;

import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;

public class ConvertConfigurationResult extends
      BatfishJobResult<Map<String, Configuration>, ConvertConfigurationAnswerElement> {

   private ConvertConfigurationAnswerElement _answerElement;

   private Map<String, Configuration> _configurations;

   private String _name;

   private Warnings _warnings;

   public ConvertConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, String name, Throwable failureCause) {
      super(elapsedTime, history, failureCause);
      _name = name;
   }

   public ConvertConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, Warnings warnings, String name,
         Map<String, Configuration> configurations,
         ConvertConfigurationAnswerElement answerElement) {
      super(elapsedTime, history);
      _name = name;
      _warnings = warnings;
      _configurations = configurations;
      _answerElement = answerElement;
   }

   @Override
   public void appendHistory(BatfishLogger logger) {
      String terseLogLevelPrefix;
      if (logger.isActive(BatfishLogger.LEVEL_INFO)) {
         terseLogLevelPrefix = "";
      }
      else {
         terseLogLevelPrefix = _name.toString() + ": ";
      }
      logger.append(_history, terseLogLevelPrefix);
   }

   @Override
   public void applyTo(Map<String, Configuration> configurations,
         BatfishLogger logger,
         ConvertConfigurationAnswerElement answerElement) {
      appendHistory(logger);
      if (_configurations != null) {
         for (String hostname : _configurations.keySet()) {
            Configuration config = _configurations.get(hostname);
            if (configurations.containsKey(hostname)) {
               throw new BatfishException("Duplicate hostname: " + hostname);
            }
            else {
               configurations.put(hostname, config);
               if (!_warnings.isEmpty()) {
                  answerElement.getWarnings().put(hostname, _warnings);
               }
               if (!_answerElement.getUnusedStructures().isEmpty()) {
                  answerElement.getUnusedStructures()
                        .putAll(_answerElement.getUnusedStructures());
               }
               if (!_answerElement.getUndefinedReferences().isEmpty()) {
                  answerElement.getUndefinedReferences()
                        .putAll(_answerElement.getUndefinedReferences());
               }
            }
         }
      }
      else {
         answerElement.getFailed().add(_name);
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
      }
      else {
         return "<EMPTY>";
      }
   }

}
