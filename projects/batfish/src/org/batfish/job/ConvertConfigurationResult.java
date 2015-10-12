package org.batfish.job;

import org.batfish.main.BatfishLogger.BatfishLoggerHistory;
import org.batfish.representation.Configuration;

public class ConvertConfigurationResult {

   private Configuration _configuration;

   private Throwable _failureCause;

   private BatfishLoggerHistory _history;

   private String _hostname;

   public ConvertConfigurationResult(BatfishLoggerHistory history,
         String hostname, Configuration configuration) {
      _history = history;
      _hostname = hostname;
      _configuration = configuration;
   }

   public ConvertConfigurationResult(BatfishLoggerHistory history,
         String hostname, Throwable failureCause) {
      _history = history;
      _hostname = hostname;
      _failureCause = failureCause;
   }

   public Configuration getConfiguration() {
      return _configuration;
   }

   public Throwable getFailureCause() {
      return _failureCause;
   }

   public BatfishLoggerHistory getHistory() {
      return _history;
   }

   public String getNodeName() {
      return _hostname;
   }

}
