package org.batfish.job;

import java.io.File;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;

public class ParseVendorConfigurationResult
      extends
      BatfishJobResult<Map<String, VendorConfiguration>, ParseVendorConfigurationAnswerElement> {

   private final File _file;

   private final BatfishLoggerHistory _history;

   private VendorConfiguration _vc;

   private Warnings _warnings;

   public ParseVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, File file, Throwable failureCause) {
      super(elapsedTime, failureCause);
      _history = history;
      _file = file;
   }

   public ParseVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, File file, VendorConfiguration vc,
         Warnings warnings) {
      super(elapsedTime);
      _history = history;
      _file = file;
      _vc = vc;
      _warnings = warnings;
   }

   public ParseVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, File file, Warnings warnings) {
      super(elapsedTime);
      _history = history;
      _file = file;
      _warnings = warnings;
   }

   private void appendHistory(BatfishLogger logger) {
      String terseLogLevelPrefix;
      if (logger.isActive(BatfishLogger.LEVEL_INFO)) {
         terseLogLevelPrefix = "";
      }
      else if (_vc != null) {
         terseLogLevelPrefix = _vc.getHostname().toString() + ": ";
      }
      else {
         terseLogLevelPrefix = _file.toString() + ": ";
      }
      logger.append(_history, terseLogLevelPrefix);
   }

   @Override
   public void applyTo(Map<String, VendorConfiguration> vendorConfigurations,
         BatfishLogger logger,
         ParseVendorConfigurationAnswerElement answerElement) {
      appendHistory(logger);
      if (_vc != null) {
         String hostname = _vc.getHostname();
         if (vendorConfigurations.containsKey(hostname)) {
            throw new BatfishException("Duplicate hostname: " + hostname);
         }
         else {
            vendorConfigurations.put(hostname, _vc);
            if (!_warnings.isEmpty()) {
               answerElement.getWarnings().put(hostname, _warnings);
            }
         }
      }
   }

   @Override
   public void explainFailure(BatfishLogger logger) {
      appendHistory(logger);
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

   @Override
   public String toString() {
      if (_vc == null) {
         return "<EMPTY OR UNSUPPORTED FORMAT>";
      }
      else if (_vc.getHostname() == null) {
         return "<File: \"" + _file.toString()
               + "\" has indeterminate hostname>";
      }
      else {
         return "<" + _vc.getHostname() + ">";
      }
   }

}
