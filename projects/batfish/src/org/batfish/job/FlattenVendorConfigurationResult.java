package org.batfish.job;

import java.io.File;
import java.util.Map;

import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;

public class FlattenVendorConfigurationResult extends
      BatfishJobResult<Map<File, String>> {

   private final String _flattenedText;

   private final BatfishLoggerHistory _history;

   private final File _outputFile;

   public FlattenVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, File outputFile, String flattenedText) {
      super(elapsedTime);
      _history = history;
      _outputFile = outputFile;
      _flattenedText = flattenedText;
   }

   public FlattenVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, File outputFile, Throwable failureCause) {
      super(elapsedTime, failureCause);
      _outputFile = outputFile;
      _history = history;
      _flattenedText = null;
   }

   private void appendHistory(BatfishLogger logger) {
      String terseLogLevelPrefix;
      if (logger.isActive(BatfishLogger.LEVEL_INFO)) {
         terseLogLevelPrefix = "";
      }
      else {
         terseLogLevelPrefix = _outputFile.getName() + ": ";
      }
      logger.append(_history, terseLogLevelPrefix);
   }

   @Override
   public void applyTo(Map<File, String> outputConfigurationData,
         BatfishLogger logger) {
      appendHistory(logger);
      outputConfigurationData.put(_outputFile, _flattenedText);
   }

   @Override
   public void explainFailure(BatfishLogger logger) {
      appendHistory(logger);
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
