package org.batfish.job;

import java.io.File;
import java.util.Map;

import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;

public class FlattenVendorConfigurationResult extends
      BatfishJobResult<Map<File, String>> {

   private String _flattenedText;

   private BatfishLoggerHistory _history;

   private File _outputFile;

   public FlattenVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history) {
      super(elapsedTime);
      _history = history;
   }

   public FlattenVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, File outputFile, String flattenedText) {
      super(elapsedTime);
      _history = history;
      _outputFile = outputFile;
      _flattenedText = flattenedText;
   }

   public FlattenVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, Throwable failureCause) {
      super(elapsedTime, failureCause);
      _history = history;
   }

   @Override
   public void applyTo(Map<File, String> outputConfigurationData,
         BatfishLogger logger) {
      outputConfigurationData.put(_outputFile, _flattenedText);
   }

   @Override
   public void explainFailure(BatfishLogger logger) {
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
