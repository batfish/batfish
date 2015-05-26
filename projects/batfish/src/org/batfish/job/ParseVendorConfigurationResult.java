package org.batfish.job;

import org.batfish.main.BatfishLogger.BatfishLoggerHistory;
import org.batfish.representation.VendorConfiguration;

public class ParseVendorConfigurationResult {

   private Throwable _failureCause;

   private BatfishLoggerHistory _history;

   private VendorConfiguration _vc;

   public ParseVendorConfigurationResult(BatfishLoggerHistory history) {
      _history = history;
   }

   public ParseVendorConfigurationResult(BatfishLoggerHistory history,
         Throwable failureCause) {
      _history = history;
      _failureCause = failureCause;
   }

   public ParseVendorConfigurationResult(BatfishLoggerHistory history,
         VendorConfiguration vc) {
      _history = history;
      _vc = vc;
   }

   public Throwable getFailureCause() {
      return _failureCause;
   }

   public BatfishLoggerHistory getHistory() {
      return _history;
   }

   public VendorConfiguration getVendorConfiguration() {
      return _vc;
   }

}
