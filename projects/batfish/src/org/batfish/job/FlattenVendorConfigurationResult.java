package org.batfish.job;

import java.io.File;

import org.batfish.main.BatfishLogger.BatfishLoggerHistory;

public class FlattenVendorConfigurationResult {

   private Throwable _failureCause;

   private String _flattenedText;

   private BatfishLoggerHistory _history;

   private File _outputFile;

   public FlattenVendorConfigurationResult(BatfishLoggerHistory history) {
      _history = history;
   }

   public FlattenVendorConfigurationResult(BatfishLoggerHistory history,
         File outputFile, String flattenedText) {
      _history = history;
      _outputFile = outputFile;
      _flattenedText = flattenedText;
   }

   public FlattenVendorConfigurationResult(BatfishLoggerHistory history,
         Throwable failureCause) {
      _history = history;
      _failureCause = failureCause;
   }

   public Throwable getFailureCause() {
      return _failureCause;
   }

   public String getFlattenedText() {
      return _flattenedText;
   }

   public BatfishLoggerHistory getHistory() {
      return _history;
   }

   public File getOutputFile() {
      return _outputFile;
   }

}
