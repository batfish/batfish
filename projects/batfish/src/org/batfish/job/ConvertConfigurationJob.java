package org.batfish.job;

import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;
import org.batfish.main.Settings;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.aws_vpcs.AwsVpcConfiguration;

public class ConvertConfigurationJob extends
      BatfishJob<ConvertConfigurationResult> {

   private Object _configObject;

   private BatfishLogger _logger;

   private String _name;

   private Settings _settings;

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
      Map<String, Configuration> configurations = new HashMap<String, Configuration>();
      try {
         if (VendorConfiguration.class.isInstance(_configObject)) {
            Configuration configuration = ((VendorConfiguration) _configObject)
                  .toVendorIndependentConfiguration(_warnings);
            configurations.put(_name, configuration);
         }
         // so far we have only two options. its AWS VPCs or router configs
         else {
            configurations = ((AwsVpcConfiguration) _configObject)
                  .toConfigurations(_warnings);
         }
         _logger.info(" ...OK\n");
      }
      catch (Exception e) {
         String error = "Conversion error for node with hostname '" + _name
               + "'";
         elapsedTime = System.currentTimeMillis() - startTime;
         return new ConvertConfigurationResult(elapsedTime,
               _logger.getHistory(), _name, new BatfishException(error, e));
      }
      finally {
         Batfish.logWarnings(_logger, _warnings);
      }
      elapsedTime = System.currentTimeMillis() - startTime;
      return new ConvertConfigurationResult(elapsedTime, _logger.getHistory(),
            _warnings, _name, configurations);
   }
}
