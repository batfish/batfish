package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;

/**
 * Enables specification a set of node properties.
 *
 * <p>Currently supported example specifier:
 *
 * <ul>
 *   <li>ntp-servers —> gets NTP servers using a configured Java function
 *   <li>ntp.* gets all properties that start with 'ntp'
 * </ul>
 *
 * <p>In the future, we might add other specifier types, e.g., those based on Json Path
 */
public class NodePropertySpecifier extends PropertySpecifier {

  public static Map<String, PropertyDescriptor<Configuration>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<Configuration>>()
          .put(
              "as-path-access-lists",
              new PropertyDescriptor<>(
                  Configuration::getAsPathAccessLists, Schema.set(Schema.STRING)))
          .put(
              "authentication-key-chains",
              new PropertyDescriptor<>(
                  Configuration::getAuthenticationKeyChains, Schema.set(Schema.STRING)))
          .put("canonical-ip", new PropertyDescriptor<>(Configuration::getCanonicalIp, Schema.IP))
          .put(
              "community-lists",
              new PropertyDescriptor<>(Configuration::getCommunityLists, Schema.set(Schema.STRING)))
          .put(
              "configuration-format",
              new PropertyDescriptor<>(Configuration::getConfigurationFormat, Schema.STRING))
          .put(
              "default-cross-zone-action",
              new PropertyDescriptor<>(Configuration::getDefaultCrossZoneAction, Schema.STRING))
          .put(
              "default-inbound-action",
              new PropertyDescriptor<>(Configuration::getDefaultInboundAction, Schema.STRING))
          .put("device-type", new PropertyDescriptor<>(Configuration::getDeviceType, Schema.STRING))
          .put(
              "dns-servers",
              new PropertyDescriptor<>(Configuration::getDnsServers, Schema.set(Schema.STRING)))
          .put(
              "dns-source-interface",
              new PropertyDescriptor<>(Configuration::getDnsSourceInterface, Schema.STRING))
          .put("domain-name", new PropertyDescriptor<>(Configuration::getDomainName, Schema.STRING))
          .put("hostname", new PropertyDescriptor<>(Configuration::getHostname, Schema.STRING))
          .put(
              "ike-gateways",
              new PropertyDescriptor<>(Configuration::getIkeGateways, Schema.set(Schema.STRING)))
          .put(
              "ike-policies",
              new PropertyDescriptor<>(Configuration::getIkePolicies, Schema.set(Schema.STRING)))
          .put(
              "interfaces",
              new PropertyDescriptor<>(Configuration::getInterfaces, Schema.set(Schema.STRING)))
          .put(
              "ip-access-lists",
              new PropertyDescriptor<>(Configuration::getIpAccessLists, Schema.set(Schema.STRING)))
          .put(
              "ip-spaces",
              new PropertyDescriptor<>(Configuration::getIpSpaces, Schema.set(Schema.STRING)))
          .put(
              "ip6-access-lists",
              new PropertyDescriptor<>(Configuration::getIp6AccessLists, Schema.set(Schema.STRING)))
          .put(
              "ipsec-policies",
              new PropertyDescriptor<>(Configuration::getIpsecPolicies, Schema.set(Schema.STRING)))
          .put(
              "ipsec-proposals",
              new PropertyDescriptor<>(Configuration::getIpsecProposals, Schema.set(Schema.STRING)))
          .put(
              "ipsec-vpns",
              new PropertyDescriptor<>(Configuration::getIpsecVpns, Schema.set(Schema.STRING)))
          .put(
              "logging-servers",
              new PropertyDescriptor<>(Configuration::getLoggingServers, Schema.set(Schema.STRING)))
          .put(
              "logging-source-interface",
              new PropertyDescriptor<>(Configuration::getLoggingSourceInterface, Schema.STRING))
          .put(
              "ntp-servers",
              new PropertyDescriptor<>(Configuration::getNtpServers, Schema.set(Schema.STRING)))
          .put(
              "ntp-source-interface",
              new PropertyDescriptor<>(Configuration::getNtpSourceInterface, Schema.STRING))
          .put(
              "route-filter-lists",
              new PropertyDescriptor<>(
                  Configuration::getRouteFilterLists, Schema.set(Schema.STRING)))
          .put(
              "route6-filter-lists",
              new PropertyDescriptor<>(
                  Configuration::getRoute6FilterLists, Schema.set(Schema.STRING)))
          .put(
              "routing-policies",
              new PropertyDescriptor<>(
                  Configuration::getRoutingPolicies, Schema.set(Schema.STRING)))
          .put(
              "snmp-source-interface",
              new PropertyDescriptor<>(Configuration::getSnmpSourceInterface, Schema.STRING))
          .put(
              "snmp-trap-servers",
              new PropertyDescriptor<>(
                  Configuration::getSnmpTrapServers, Schema.set(Schema.STRING)))
          .put(
              "tacacs-servers",
              new PropertyDescriptor<>(Configuration::getTacacsServers, Schema.set(Schema.STRING)))
          .put(
              "tacacs-source-interface",
              new PropertyDescriptor<>(Configuration::getTacacsSourceInterface, Schema.STRING))
          .put(
              "vendor-family",
              new PropertyDescriptor<>(Configuration::getVendorFamily, Schema.STRING))
          .put("vrfs", new PropertyDescriptor<>(Configuration::getVrfs, Schema.set(Schema.STRING)))
          .put(
              "zones", new PropertyDescriptor<>(Configuration::getZones, Schema.set(Schema.STRING)))
          .build();

  public static final NodePropertySpecifier ALL = new NodePropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public NodePropertySpecifier(String expression) {
    _expression = expression.trim().toLowerCase();
    _pattern = Pattern.compile(_expression.trim().toLowerCase()); // canonicalize
  }

  /**
   * Returns a list of suggestions based on the query. The current implementation treats the query
   * as a prefix of the property string.
   *
   * @param query The query to auto complete
   * @return The list of suggestions
   */
  public static List<AutocompleteSuggestion> autoComplete(String query) {
    return PropertySpecifier.baseAutoComplete(query, JAVA_MAP.keySet());
  }

  @Override
  public Set<String> getMatchingProperties() {
    return JAVA_MAP
        .keySet()
        .stream()
        .filter(prop -> _pattern.matcher(prop).matches())
        .collect(Collectors.toSet());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
