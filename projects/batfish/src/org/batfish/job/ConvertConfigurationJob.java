package org.batfish.job;

import java.util.concurrent.Callable;

import org.batfish.main.BatfishException;
import org.batfish.main.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.VendorConfiguration;

public class ConvertConfigurationJob implements
      Callable<ConvertConfigurationResult> {

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
      _logger.info("Processing: \"" + _hostname + "\"");
      Configuration configuration = null;
      try {
         configuration = _vc.toVendorIndependentConfiguration(_warnings);
         _logger.info(" ...OK\n");
      }
      catch (BatfishException e) {
         String error = "Conversion error";
         return new ConvertConfigurationResult(_logger.getHistory(), _hostname,
               new BatfishException(error, e));
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
      return new ConvertConfigurationResult(_logger.getHistory(), _hostname,
            configuration);
   }

}
