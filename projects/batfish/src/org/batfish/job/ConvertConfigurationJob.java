package org.batfish.job;

import java.util.HashMap;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.Settings;
import org.batfish.common.Warnings;
import org.batfish.representation.aws_vpcs.AwsVpcConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class ConvertConfigurationJob
      extends BatfishJob<ConvertConfigurationResult> {

   private Object _configObject;

   private String _name;

   private Warnings _warnings;

   public ConvertConfigurationJob(Settings settings, Object configObject,
         String name, Warnings warnings) {
      super(settings);
      _configObject = configObject;
      _name = name;
      _warnings = warnings;
   }

   @Override
   public ConvertConfigurationResult call() throws Exception {
      long startTime = System.currentTimeMillis();
      long elapsedTime;
      _logger.info("Processing: \"" + _name + "\"");
      Map<String, Configuration> configurations = new HashMap<>();
      ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
      try {
         if (VendorConfiguration.class.isInstance(_configObject)) {
            VendorConfiguration vendorConfiguration = ((VendorConfiguration) _configObject);
            vendorConfiguration.setWarnings(_warnings);
            vendorConfiguration.setAnswerElement(answerElement);
            Configuration configuration = vendorConfiguration
                  .toVendorIndependentConfiguration();
            if (configuration.getDefaultCrossZoneAction() == null) {
               throw new BatfishException(
                     "Implementation error: missing default cross-zone action for host: '"
                           + configuration.getHostname() + "'");
            }
            if (configuration.getDefaultInboundAction() == null) {
               throw new BatfishException(
                     "Implementation error: missing default inbound action for host: '"
                           + configuration.getHostname() + "'");
            }
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
            _warnings, _name, configurations, answerElement);
   }
}
