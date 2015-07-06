package org.batfish.job;

import java.io.File;

import org.batfish.main.BatfishLogger.BatfishLoggerHistory;
import org.batfish.representation.VendorConfiguration;

public class ParseVendorConfigurationResult {

   private Throwable _failureCause;

   private final File _file;

   private final BatfishLoggerHistory _history;

   private VendorConfiguration _vc;

   public ParseVendorConfigurationResult(BatfishLoggerHistory history, File file) {
      _history = history;
      _file = file;
   }

   public ParseVendorConfigurationResult(BatfishLoggerHistory history,
         File file, Throwable failureCause) {
      _history = history;
      _file = file;
      _failureCause = failureCause;
   }

   public ParseVendorConfigurationResult(BatfishLoggerHistory history,
         File file, VendorConfiguration vc) {
      _history = history;
      _file = file;
      _vc = vc;
   }

   public Throwable getFailureCause() {
      return _failureCause;
   }

   public File getFile() {
      return _file;
   }

   public BatfishLoggerHistory getHistory() {
      return _history;
   }

   public VendorConfiguration getVendorConfiguration() {
      return _vc;
   }

}
