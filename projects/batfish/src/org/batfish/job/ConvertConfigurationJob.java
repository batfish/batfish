package org.batfish.job;

import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.Settings;
import org.batfish.main.Warnings;
import org.batfish.representation.Configuration;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.aws_vpcs.AwsVpcConfiguration;

public class ConvertConfigurationJob extends
      BatfishJob<ConvertConfigurationResult> {

   private String _name;

   private BatfishLogger _logger;

   private Settings _settings;

   private Object _configObject;

   private Warnings _warnings;

   public ConvertConfigurationJob(Settings settings, Object configObject,
         String name, Warnings warnings) {
      _settings = settings;
      _configObject = configObject;
      _name = name;
      _warnings = warnings;
      _logger = new BatfishLogger(_settings.getLogLevel(),
            _settings.getTimestamp());
   }

   @Override
   public ConvertConfigurationResult call() throws Exception {
      long startTime = System.currentTimeMillis();
      long elapsedTime;
      _logger.info("Processing: \"" + _name + "\"");
      Map<String,Configuration> configurations = new HashMap<String, Configuration>();
      try {
         if (VendorConfiguration.class.isInstance(_configObject)) {
            Configuration configuration = ((VendorConfiguration) _configObject)
                  .toVendorIndependentConfiguration(_warnings);
            configurations.put(_name, configuration);
         }
         //so far we have only two options. its AWS VPCs or router configs
         else {
            configurations = ((AwsVpcConfiguration) _configObject)
                  .toConfigurations(_warnings);
         }
         _logger.info(" ...OK\n");
      }
      catch (BatfishException e) {
         String error = "Conversion error";
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ConvertConfigurationResult(elapsedTime,
               _logger.getHistory(), _name, new BatfishException(error, e));
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
            _name, configurations);
   }

}
