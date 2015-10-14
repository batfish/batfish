package org.batfish.job;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.VendorConfiguration;

public class ConvertConfigurationJob extends
      BatfishJob<ConvertConfigurationResult> {

   private String _hostname;

   private BatfishLogger _logger;

   private Settings _settings;

   private VendorConfiguration _vc;

   private Warnings _warnings;

   public ConvertConfigurationJob(Settings settings, VendorConfiguration vc,
         String hostname, Warnings warnings) {
      _settings = settings;
      _vc = vc;
      _hostname = hostname;
      _warnings = warnings;
      _logger = new BatfishLogger(_settings.getLogLevel(),
            _settings.getTimestamp());
   }

   @Override
   public ConvertConfigurationResult call() throws Exception {
      long startTime = System.currentTimeMillis();
      long elapsedTime;
      _logger.info("Processing: \"" + _hostname + "\"");
      Configuration configuration = null;
      try {
         configuration = _vc.toVendorIndependentConfiguration(_warnings);
         _logger.info(" ...OK\n");
      }
      catch (BatfishException e) {
         String error = "Conversion error";
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ConvertConfigurationResult(elapsedTime,
               _logger.getHistory(), _hostname, new BatfishException(error, e));
      }
      finally {
         for (String warning : _warnings.getRedFlagWarnings()) {
            _logger.redflag(warning);
         }
         for (String warning : _warnings.getUnimplementedWarnings()) {
            _logger.unimplemented(warning);
         }
         for (String warning : _warnings.getPedanticWarnings()) {
            _logger.pedantic(warning);
         }
      }
      elapsedTime = System.currentTimeMillis() - startTime;
      return new ConvertConfigurationResult(elapsedTime, _logger.getHistory(),
            _hostname, configuration);
   }

}
