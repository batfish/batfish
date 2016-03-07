package org.batfish.job;

import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.representation.Configuration;

public class ConvertConfigurationResult extends
      BatfishJobResult<Map<String, Configuration>> {

   private Map<String, Configuration> _configurations;

   private BatfishLoggerHistory _history;

   private String _name;

   public ConvertConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, String name,
         Map<String, Configuration> configurations) {
      super(elapsedTime);
      _history = history;
      _name = name;
      _configurations = configurations;
   }

   public ConvertConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, String name, Throwable failureCause) {
      super(elapsedTime, failureCause);
      _history = history;
      _name = name;
   }

   private void appendHistory(BatfishLogger logger) {
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
         BatfishLogger logger) {
      appendHistory(logger);
      if (_configurations != null) {
         for (String hostname : _configurations.keySet()) {
            Configuration config = _configurations.get(hostname);
            if (configurations.containsKey(hostname)) {
               throw new BatfishException("Duplicate hostname: " + hostname);
            }
            else {
               configurations.put(hostname, config);
            }
         }
      }
   }

   @Override
   public void explainFailure(BatfishLogger logger) {
      appendHistory(logger);
   }

   public Map<String, Configuration> getConfigurations() {
      return _configurations;
   }

   public BatfishLoggerHistory getHistory() {
      return _history;
   }

   public String getName() {
      return _name;
   }

}
