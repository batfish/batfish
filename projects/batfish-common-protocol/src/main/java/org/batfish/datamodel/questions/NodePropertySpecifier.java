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
 *   <li>ntp-servers -&gt; gets NTP servers using a configured Java function
 *   <li>ntp.* gets all properties that start with 'ntp'
 * </ul>
 *
 * <p>In the future, we might add other specifier types, e.g., those based on Json Path
 */
public class NodePropertySpecifier extends PropertySpecifier {

  public static Map<String, PropertyDescriptor<Configuration>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<Configuration>>()
          .put(
              "As_Path_Access_Lists",
              new PropertyDescriptor<>(
                  Configuration::getAsPathAccessLists, Schema.set(Schema.STRING)))
          .put(
              "Authentication_Key_Chains",
              new PropertyDescriptor<>(
                  Configuration::getAuthenticationKeyChains, Schema.set(Schema.STRING)))
          .put("Canonical_Ip", new PropertyDescriptor<>(Configuration::getCanonicalIp, Schema.IP))
          .put(
              "Community_Lists",
              new PropertyDescriptor<>(Configuration::getCommunityLists, Schema.set(Schema.STRING)))
          .put(
              "Configuration_Format",
              new PropertyDescriptor<>(Configuration::getConfigurationFormat, Schema.STRING))
          .put(
              "Default_Cross_Zone_Action",
              new PropertyDescriptor<>(Configuration::getDefaultCrossZoneAction, Schema.STRING))
          .put(
              "Default_Inbound_Action",
              new PropertyDescriptor<>(Configuration::getDefaultInboundAction, Schema.STRING))
          .put("Device_Type", new PropertyDescriptor<>(Configuration::getDeviceType, Schema.STRING))
          .put(
              "Dns_Servers",
              new PropertyDescriptor<>(Configuration::getDnsServers, Schema.set(Schema.STRING)))
          .put(
              "Dns_Source_Interface",
              new PropertyDescriptor<>(Configuration::getDnsSourceInterface, Schema.STRING))
          .put("Domain_Name", new PropertyDescriptor<>(Configuration::getDomainName, Schema.STRING))
          .put("Hostname", new PropertyDescriptor<>(Configuration::getHostname, Schema.STRING))
          .put(
              "Ike_Gateways",
              new PropertyDescriptor<>(Configuration::getIkeGateways, Schema.set(Schema.STRING)))
          .put(
              "Ike_Policies",
              new PropertyDescriptor<>(Configuration::getIkePolicies, Schema.set(Schema.STRING)))
          .put(
              "Interfaces",
              new PropertyDescriptor<>(Configuration::getInterfaces, Schema.set(Schema.STRING)))
          .put(
              "Ip_Access_Lists",
              new PropertyDescriptor<>(Configuration::getIpAccessLists, Schema.set(Schema.STRING)))
          .put(
              "Ip_Spaces",
              new PropertyDescriptor<>(Configuration::getIpSpaces, Schema.set(Schema.STRING)))
          .put(
              "Ip6_Access_Lists",
              new PropertyDescriptor<>(Configuration::getIp6AccessLists, Schema.set(Schema.STRING)))
          .put(
              "Ipsec_Policies",
              new PropertyDescriptor<>(Configuration::getIpsecPolicies, Schema.set(Schema.STRING)))
          .put(
              "Ipsec_Proposals",
              new PropertyDescriptor<>(Configuration::getIpsecProposals, Schema.set(Schema.STRING)))
          .put(
              "Ipsec_Vpns",
              new PropertyDescriptor<>(Configuration::getIpsecVpns, Schema.set(Schema.STRING)))
          .put(
              "Logging_Servers",
              new PropertyDescriptor<>(Configuration::getLoggingServers, Schema.set(Schema.STRING)))
          .put(
              "Logging_Source_Interface",
              new PropertyDescriptor<>(Configuration::getLoggingSourceInterface, Schema.STRING))
          .put(
              "Ntp_Servers",
              new PropertyDescriptor<>(Configuration::getNtpServers, Schema.set(Schema.STRING)))
          .put(
              "Ntp_Source_Interface",
              new PropertyDescriptor<>(Configuration::getNtpSourceInterface, Schema.STRING))
          .put(
              "Route_Filter_Lists",
              new PropertyDescriptor<>(
                  Configuration::getRouteFilterLists, Schema.set(Schema.STRING)))
          .put(
              "Route6_Filter_Lists",
              new PropertyDescriptor<>(
                  Configuration::getRoute6FilterLists, Schema.set(Schema.STRING)))
          .put(
              "Routing_Policies",
              new PropertyDescriptor<>(
                  Configuration::getRoutingPolicies, Schema.set(Schema.STRING)))
          .put(
              "Snmp_Source_Interface",
              new PropertyDescriptor<>(Configuration::getSnmpSourceInterface, Schema.STRING))
          .put(
              "Snmp_Trap_Servers",
              new PropertyDescriptor<>(
                  Configuration::getSnmpTrapServers, Schema.set(Schema.STRING)))
          .put(
              "Tacacs_Servers",
              new PropertyDescriptor<>(Configuration::getTacacsServers, Schema.set(Schema.STRING)))
          .put(
              "Tacacs_Source_Interface",
              new PropertyDescriptor<>(Configuration::getTacacsSourceInterface, Schema.STRING))
          .put(
              "Vendor_Family",
              new PropertyDescriptor<>(Configuration::getVendorFamily, Schema.STRING))
          .put("Vrfs", new PropertyDescriptor<>(Configuration::getVrfs, Schema.set(Schema.STRING)))
          .put(
              "Zones", new PropertyDescriptor<>(Configuration::getZones, Schema.set(Schema.STRING)))
          .build();

  public static final NodePropertySpecifier ALL = new NodePropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public NodePropertySpecifier(String expression) {
    _expression = expression;
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
        .filter(prop -> _pattern.matcher(prop.toLowerCase()).matches())
        .collect(Collectors.toSet());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
