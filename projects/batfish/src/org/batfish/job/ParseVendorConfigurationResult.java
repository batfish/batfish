package org.batfish.job;

import java.io.File;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishLogger.BatfishLoggerHistory;
import org.batfish.representation.VendorConfiguration;

public class ParseVendorConfigurationResult extends
      BatfishJobResult<Map<String, VendorConfiguration>> {

   private final File _file;

   private final BatfishLoggerHistory _history;

   private VendorConfiguration _vc;

   public ParseVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, File file) {
      super(elapsedTime);
      _history = history;
      _file = file;
   }

   public ParseVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, File file, Throwable failureCause) {
      super(elapsedTime, failureCause);
      _history = history;
      _file = file;
   }

   public ParseVendorConfigurationResult(long elapsedTime,
         BatfishLoggerHistory history, File file, VendorConfiguration vc) {
      super(elapsedTime);
      _history = history;
      _file = file;
      _vc = vc;
   }

   private void appendHistory(BatfishLogger logger) {
      String terseLogLevelPrefix;
      if (logger.isActive(BatfishLogger.LEVEL_INFO)) {
         terseLogLevelPrefix = "";
      }
      else {
         terseLogLevelPrefix = _vc.getHostname().toString() + ": ";
      }
      logger.append(_history, terseLogLevelPrefix);
   }

   @Override
   public void applyTo(Map<String, VendorConfiguration> vendorConfigurations,
         BatfishLogger logger) {
      appendHistory(logger);
      if (_vc != null) {
         String hostname = _vc.getHostname();
         if (vendorConfigurations.containsKey(hostname)) {
            throw new BatfishException("Duplicate hostname: " + hostname);
         }
         else {
            vendorConfigurations.put(hostname, _vc);
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

}
