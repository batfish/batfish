package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import org.batfish.datamodel.Configuration;

/**
 * Enables specification a set of node properties.
 *
 * <p>Currently supported example specifier:
 *
 * <ul>
 *   <li>ntp-servers —> gets NTP servers using a configured Java function
 * </ul>
 *
 * <p>In the future, we might add other specifier types, e.g., those based on Json Path
 */
public class NodePropertySpecifier {

  public static final Map<String, Function<Configuration, Object>> JAVA_MAP =
      new ImmutableMap.Builder<String, Function<Configuration, Object>>()
          .put("as-path-access-lists", Configuration::getAsPathAccessLists)
          .put("authentication-key-chains", Configuration::getAuthenticationKeyChains)
          .put("canonical-ip", Configuration::getCanonicalIp)
          .put("community-lists", Configuration::getCommunityLists)
          .put("configuration-format", Configuration::getConfigurationFormat)
          .put("default-cross-zone-action", Configuration::getDefaultCrossZoneAction)
          .put("default-inbound-action", Configuration::getDefaultInboundAction)
          .put("default-vrf", Configuration::getDefaultVrf)
          .put("device-type", Configuration::getDeviceType)
          .put("dns-servers", Configuration::getDnsServers)
          .put("dns-source-interface", Configuration::getDnsSourceInterface)
          .put("domain-name", Configuration::getDomainName)
          .put("hostname", Configuration::getHostname)
          .put("ike-gateways", Configuration::getIkeGateways)
          .put("ike-policies", Configuration::getIkePolicies)
          .put("interfaces", Configuration::getInterfaces)
          .put("ip-access-lists", Configuration::getIpAccessLists)
          .put("ip-spaces", Configuration::getIpSpaces)
          .put("ip6-access-lists", Configuration::getIp6AccessLists)
          .put("ipsec-policies", Configuration::getIpsecPolicies)
          .put("ipsec-proposals", Configuration::getIpsecProposals)
          .put("ipsec-vpns", Configuration::getIpsecVpns)
          .put("logging-servers", Configuration::getLoggingServers)
          .put("logging-source-interface", Configuration::getLoggingSourceInterface)
          .put("ntp-servers", Configuration::getNtpServers)
          .put("ntp-source-interface", Configuration::getNtpSourceInterface)
          .put("route-filter-lists", Configuration::getRouteFilterLists)
          .put("route6-filter-lists", Configuration::getRoute6FilterLists)
          .put("routing-policies", Configuration::getRoutingPolicies)
          .put("snmp-source-interface", Configuration::getSnmpSourceInterface)
          .put("snmp-trap-servers", Configuration::getSnmpTrapServers)
          .put("tacacs-servers", Configuration::getTacacsServers)
          .put("tacacs-source-interface", Configuration::getTacacsSourceInterface)
          .put("vendor-family", Configuration::getVendorFamily)
          .put("vrfs", Configuration::getVrfs)
          .put("zones", Configuration::getZones)
          .build();

  private final String _expression;

  @JsonCreator
  public NodePropertySpecifier(String expression) {
    _expression = expression.trim();

    if (!JAVA_MAP.containsKey(expression.toLowerCase())) {
      throw new IllegalArgumentException(
          "Invalid node property specification: '" + expression + "'");
    }
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
