package org.batfish.job;

import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.representation.Configuration;

public class ConvertConfigurationResult extends
      BatfishJobResult<Map<String, Configuration>> {

   private Configuration _configuration;

   private BatfishLoggerHistory _history;

   private String _hostname;

   public ConvertConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, String hostname,
         Configuration configuration) {
      super(elapsedTime);
      _history = history;
      _hostname = hostname;
      _configuration = configuration;
   }

   public ConvertConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, String hostname, Throwable failureCause) {
      super(elapsedTime, failureCause);
      _history = history;
      _hostname = hostname;
   }

   private void appendHistory(BatfishLogger logger) {
      String terseLogLevelPrefix;
      if (logger.isActive(BatfishLogger.LEVEL_INFO)) {
         terseLogLevelPrefix = "";
      }
      else {
         terseLogLevelPrefix = _hostname.toString() + ": ";
      }
      logger.append(_history, terseLogLevelPrefix);
   }

   @Override
   public void applyTo(Map<String, Configuration> configurations,
         BatfishLogger logger) {
      appendHistory(logger);
      if (_configuration != null) {
         String hostname = _configuration.getHostname();
         if (configurations.containsKey(hostname)) {
            throw new BatfishException("Duplicate hostname: " + hostname);
         }
         else {
            configurations.put(hostname, _configuration);
         }
      }
   }

   @Override
   public void explainFailure(BatfishLogger logger) {
      appendHistory(logger);
   }

   public Configuration getConfiguration() {
      return _configuration;
   }

   public BatfishLoggerHistory getHistory() {
      return _history;
   }

   public String getNodeName() {
      return _hostname;
   }

}
