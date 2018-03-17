package org.batfish.job;

import java.util.HashMap;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.aws.AwsConfiguration;
import org.batfish.representation.host.HostConfiguration;
import org.batfish.representation.iptables.IptablesVendorConfiguration;
import org.batfish.vendor.VendorConfiguration;

public class ConvertConfigurationJob extends BatfishJob<ConvertConfigurationResult> {

  private Object _configObject;

  private String _name;

  private Warnings _warnings;

  public ConvertConfigurationJob(
      Settings settings, Object configObject, String name, Warnings warnings) {
    super(settings);
    _configObject = configObject;
    _name = name;
    _warnings = warnings;
  }

  @Override
  public ConvertConfigurationResult call() {
    long startTime = System.currentTimeMillis();
    long elapsedTime;
    _logger.infof("Processing: \"%s\"", _name);
    Map<String, Configuration> configurations = new HashMap<>();
    ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
    try {
      // We have only two options: AWS VPCs or router configs
      if (VendorConfiguration.class.isInstance(_configObject)) {
        VendorConfiguration vendorConfiguration = ((VendorConfiguration) _configObject);
        vendorConfiguration.setWarnings(_warnings);
        vendorConfiguration.setAnswerElement(answerElement);
        Configuration configuration = vendorConfiguration.toVendorIndependentConfiguration();
        if (configuration.getDefaultCrossZoneAction() == null) {
          throw new BatfishException(
              "Implementation error: missing default cross-zone action for host: '"
                  + configuration.getHostname()
                  + "'");
        }
        if (configuration.getDefaultInboundAction() == null) {
          throw new BatfishException(
              "Implementation error: missing default inbound action for host: '"
                  + configuration.getHostname()
                  + "'");
        }

        // get iptables if applicable
        IptablesVendorConfiguration iptablesConfig = null;
        VendorConfiguration ov = vendorConfiguration.getOverlayConfiguration();
        if (ov != null) {
          // apply overlay
          HostConfiguration oh = (HostConfiguration) ov;
          iptablesConfig = oh.getIptablesVendorConfig();
        } else if (vendorConfiguration instanceof HostConfiguration) {
          // TODO: To enable below, we need to reconcile overlay and non-overlay iptables semantics.
          // HostConfiguration oh = (HostConfiguration)vendorConfiguration;
          // iptablesConfig = oh.getIptablesVendorConfig();
        }
        if (iptablesConfig != null) {
          iptablesConfig.addAsIpAccessLists(configuration, vendorConfiguration, _warnings);
          iptablesConfig.applyAsOverlay(configuration, _warnings);
        }

        configurations.put(_name, configuration);
      } else {
        configurations = ((AwsConfiguration) _configObject).toConfigurations(_warnings, _logger);
      }
      _logger.info(" ...OK\n");
    } catch (Exception e) {
      String error = "Conversion error for node with hostname '" + _name + "'";
      elapsedTime = System.currentTimeMillis() - startTime;
      return new ConvertConfigurationResult(
          elapsedTime, _logger.getHistory(), _name, new BatfishException(error, e));
    } finally {
      Batfish.logWarnings(_logger, _warnings);
    }
    elapsedTime = System.currentTimeMillis() - startTime;
    return new ConvertConfigurationResult(
        elapsedTime, _logger.getHistory(), _warnings, _name, configurations, answerElement);
  }
}
