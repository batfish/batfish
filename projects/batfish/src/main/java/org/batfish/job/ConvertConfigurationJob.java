package org.batfish.job;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimap;
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

  public ConvertConfigurationJob(Settings settings, Object configObject, String name) {
    super(settings);
    _configObject = configObject;
    _name = name;
  }

  /**
   * Applies sanity checks and finishing touches to the given {@link Configuration}.
   *
   * <p>Sanity checks such as asserting that required properties hold.
   *
   * <p>Finishing touches such as converting structures to their immutable forms.
   */
  private static void finalizeConfiguration(Configuration c, Warnings w) {
    String hostname = c.getHostname();
    if (c.getDefaultCrossZoneAction() == null) {
      throw new BatfishException(
          "Implementation error: missing default cross-zone action for host: '" + hostname + "'");
    }
    if (c.getDefaultInboundAction() == null) {
      throw new BatfishException(
          "Implementation error: missing default inbound action for host: '" + hostname + "'");
    }
    c.simplifyRoutingPolicies();
    c.computeRoutingPolicySources(w);
    c.setAsPathAccessLists(ImmutableSortedMap.copyOf(c.getAsPathAccessLists()));
    c.setCommunityLists(ImmutableSortedMap.copyOf(c.getCommunityLists()));
    c.setIpAccessLists(ImmutableSortedMap.copyOf(c.getIpAccessLists()));
    c.setIp6AccessLists(ImmutableSortedMap.copyOf(c.getIp6AccessLists()));
    c.setRouteFilterLists(ImmutableSortedMap.copyOf(c.getRouteFilterLists()));
    c.setRoute6FilterLists(ImmutableSortedMap.copyOf(c.getRoute6FilterLists()));
  }

  @Override
  public ConvertConfigurationResult call() {
    long startTime = System.currentTimeMillis();
    long elapsedTime;
    _logger.infof("Processing: \"%s\"", _name);
    Map<String, Configuration> configurations = new HashMap<>();
    Map<String, Warnings> warningsByHost = new HashMap<>();
    ConvertConfigurationAnswerElement answerElement = new ConvertConfigurationAnswerElement();
    Multimap<String, String> fileMap = answerElement.getFileMap();
    try {
      // We have only two options: AWS VPCs or router configs
      if (_configObject instanceof VendorConfiguration) {
        Warnings warnings = Batfish.buildWarnings(_settings);
        VendorConfiguration vendorConfiguration = ((VendorConfiguration) _configObject);
        String filename = vendorConfiguration.getFilename();
        vendorConfiguration.setWarnings(warnings);
        vendorConfiguration.setAnswerElement(answerElement);
        for (Configuration configuration :
            vendorConfiguration.toVendorIndependentConfigurations()) {

          // get iptables if applicable
          IptablesVendorConfiguration iptablesConfig = null;
          VendorConfiguration ov = vendorConfiguration.getOverlayConfiguration();
          if (ov != null) {
            // apply overlay
            HostConfiguration oh = (HostConfiguration) ov;
            iptablesConfig = oh.getIptablesVendorConfig();
          } else if (vendorConfiguration instanceof HostConfiguration) {
            // TODO: To enable below, we need to reconcile overlay and non-overlay iptables
            // semantics.
            // HostConfiguration oh = (HostConfiguration)vendorConfiguration;
            // iptablesConfig = oh.getIptablesVendorConfig();
          }
          if (iptablesConfig != null) {
            iptablesConfig.addAsIpAccessLists(configuration, vendorConfiguration, warnings);
            iptablesConfig.applyAsOverlay(configuration, warnings);
          }

          finalizeConfiguration(configuration, warnings);

          String hostname = configuration.getHostname();
          configurations.put(hostname, configuration);
          warningsByHost.put(hostname, warnings);
          fileMap.put(filename, hostname);
        }
      } else {
        configurations =
            ((AwsConfiguration) _configObject).toConfigurations(_settings, warningsByHost);
      }
      _logger.info(" ...OK\n");
    } catch (Exception e) {
      String error = "Conversion error for node with hostname '" + _name + "'";
      elapsedTime = System.currentTimeMillis() - startTime;
      return new ConvertConfigurationResult(
          elapsedTime, _logger.getHistory(), _name, new BatfishException(error, e));
    } finally {
      warningsByHost.forEach((hostname, warnings) -> Batfish.logWarnings(_logger, warnings));
    }
    elapsedTime = System.currentTimeMillis() - startTime;
    return new ConvertConfigurationResult(
        elapsedTime, _logger.getHistory(), warningsByHost, _name, configurations, answerElement);
  }
}
