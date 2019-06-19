package org.batfish.job;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip6AccessList;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
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
   * Sanity checks the given map from name-of-thing to thing-with-name for name consistency. If the
   * names are not consistent, warns and does not convert them.
   *
   * <p>The created maps are sorted by key in ascending order.
   *
   * <p>Hopefully this will only happen during new parser development, and will help authors of
   * those new parsers get it right.
   */
  private static <T> ImmutableMap<String, T> verifyAndToImmutableMap(
      @Nullable Map<String, T> map, Function<T, String> keyFn, Warnings w) {
    if (map == null || map.isEmpty()) {
      return ImmutableMap.of();
    }
    return map.entrySet().stream()
        .filter(
            e -> {
              String key = keyFn.apply(e.getValue());
              if (key.equals(e.getKey())) {
                return true;
              }
              w.redFlag(
                  String.format(
                      "Batfish internal error: invalid entry %s -> %s named %s",
                      e.getKey(), e.getValue(), key));
              return false;
            })
        .sorted(Comparator.comparing(Entry::getKey)) /* ImmutableMap is insert ordered. */
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
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
    c.setAsPathAccessLists(
        verifyAndToImmutableMap(c.getAsPathAccessLists(), AsPathAccessList::getName, w));
    c.setCommunityLists(verifyAndToImmutableMap(c.getCommunityLists(), CommunityList::getName, w));
    c.setInterfaces(verifyAndToImmutableMap(c.getAllInterfaces(), Interface::getName, w));
    c.setIpAccessLists(verifyAndToImmutableMap(c.getIpAccessLists(), IpAccessList::getName, w));
    c.setIp6AccessLists(verifyAndToImmutableMap(c.getIp6AccessLists(), Ip6AccessList::getName, w));
    c.setRouteFilterLists(
        verifyAndToImmutableMap(c.getRouteFilterLists(), RouteFilterList::getName, w));
    c.setRoute6FilterLists(
        verifyAndToImmutableMap(c.getRoute6FilterLists(), Route6FilterList::getName, w));
    c.setRoutingPolicies(
        verifyAndToImmutableMap(c.getRoutingPolicies(), RoutingPolicy::getName, w));
    removeInvalidAcls(c, w);
  }

  /** Confirm assigned ACLs (e.g. interface's outgoing ACL) exist in config's IpAccessList map */
  private static void removeInvalidAcls(Configuration c, Warnings w) {
    for (Interface iface : c.getAllInterfaces().values()) {
      String ifaceName = iface.getName();
      String outName = iface.getOutgoingFilterName();
      if (outName != null && !c.getIpAccessLists().containsKey(outName)) {
        w.redFlag(
            String.format(
                "IpAccessList map is missing filter %s: outgoing filter for interface %s",
                outName, ifaceName));
        iface.setOutgoingFilter((IpAccessList) null);
      }

      String inName = iface.getIncomingFilterName();
      if (inName != null && !c.getIpAccessLists().containsKey(inName)) {
        w.redFlag(
            String.format(
                "IpAccessList map is missing filter %s: incoming filter for interface %s",
                inName, ifaceName));
        iface.setIncomingFilter(null);
      }

      String postTransformName = iface.getPostTransformationIncomingFilterName();
      if (postTransformName != null && !c.getIpAccessLists().containsKey(postTransformName)) {
        w.redFlag(
            String.format(
                "IpAccessList map is missing filter %s: post transformation incoming filter for interface %s",
                postTransformName, ifaceName));
        iface.setPostTransformationIncomingFilter((IpAccessList) null);
      }

      String preTransformName = iface.getPreTransformationOutgoingFilterName();
      if (preTransformName != null && !c.getIpAccessLists().containsKey(preTransformName)) {
        w.redFlag(
            String.format(
                "IpAccessList map is missing filter %s: pre transformation outgoing filter for interface %s",
                preTransformName, ifaceName));
        iface.setPreTransformationOutgoingFilter((IpAccessList) null);
      }
    }
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
