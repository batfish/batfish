package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;

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

  public static final String AS_PATH_ACCESS_LISTS = "AS_Path_Access_Lists";
  public static final String AUTHENTICATION_KEY_CHAINS = "Authentication_Key_Chains";
  public static final String CANONICAL_IP = "Canonical_IP";
  public static final String COMMUNITY_LISTS = "Community_Lists";
  public static final String CONFIGURATION_FORMAT = "Configuration_Format";
  public static final String DEFAULT_CROSS_ZONE_ACTION = "Default_Cross_Zone_Action";
  public static final String DEFAULT_INBOUND_ACTION = "Default_Inbound_Action";
  public static final String DEVICE_TYPE = "Device_Type";
  public static final String DNS_SERVERS = "DNS_Servers";
  public static final String DNS_SOURCE_INTERFACE = "DNS_Source_Interface";
  public static final String DOMAIN_NAME = "Domain_Name";
  public static final String HOSTNAME = "Hostname";
  public static final String IKE_PHASE1_KEYS = "IKE_Phase1_Keys";
  public static final String IKE_PHASE1_POLICIES = "IKE_Phase1_Policies";
  public static final String IKE_PHASE1_PROPOSALS = "IKE_Phase1_Proposals";
  public static final String INTERFACES = "Interfaces";
  public static final String IP_ACCESS_LISTS = "IP_Access_Lists";
  public static final String IP_SPACES = "IP_Spaces";
  public static final String IP_6_ACCESS_LISTS = "IP6_Access_Lists";
  public static final String IPSEC_PEER_CONFIGS = "IPsec_Peer_Configs";
  public static final String IPSEC_PHASE2_POLICIES = "IPsec_Phase2_Policies";
  public static final String IPSEC_PHASE2_PROPOSALS = "IPsec_Phase2_Proposals";
  public static final String IPSEC_VPNS = "IPSec_Vpns";
  public static final String LOGGING_SERVERS = "Logging_Servers";
  public static final String LOGGING_SOURCE_INTERFACE = "Logging_Source_Interface";
  public static final String NTP_SERVERS = "NTP_Servers";
  public static final String NTP_SOURCE_INTERFACE = "NTP_Source_Interface";
  public static final String ROUTE_FILTER_LISTS = "Route_Filter_Lists";
  public static final String ROUTE_6_FILTER_LISTS = "Route6_Filter_Lists";
  public static final String ROUTING_POLICIES = "Routing_Policies";
  public static final String SNMP_SOURCE_INTERFACE = "SNMP_Source_Interface";
  public static final String SNMP_TRAP_SERVERS = "SNMP_Trap_Servers";
  public static final String TACACS_SERVERS = "TACACS_Servers";
  public static final String TACACS_SOURCE_INTERFACE = "TACACS_Source_Interface";
  public static final String VENDOR_FAMILY = "Vendor_Family";
  public static final String VRFS = "VRFs";
  public static final String ZONES = "Zones";

  public static Map<String, PropertyDescriptor<Configuration>> JAVA_MAP =
      initPropertyMap(Configuration.class);

  //  public static Map<String, PropertyDescriptor<Configuration>> JAVA_MAP =
  //      new ImmutableMap.Builder<String, PropertyDescriptor<Configuration>>()
  //          .put(
  //              AS_PATH_ACCESS_LISTS,
  //              new PropertyDescriptor<>(
  //                  Configuration::getAsPathAccessLists, Schema.set(Schema.STRING)))
  //          .put(
  //              AUTHENTICATION_KEY_CHAINS,
  //              new PropertyDescriptor<>(
  //                  Configuration::getAuthenticationKeyChains, Schema.set(Schema.STRING)))
  //          .put(CANONICAL_IP, new PropertyDescriptor<>(Configuration::getCanonicalIp, Schema.IP))
  //          .put(
  //              COMMUNITY_LISTS,
  //              new PropertyDescriptor<>(Configuration::getCommunityLists,
  // Schema.set(Schema.STRING)))
  //          .put(
  //              CONFIGURATION_FORMAT,
  //              new PropertyDescriptor<>(Configuration::getConfigurationFormat, Schema.STRING))
  //          .put(
  //              DEFAULT_CROSS_ZONE_ACTION,
  //              new PropertyDescriptor<>(Configuration::getDefaultCrossZoneAction, Schema.STRING))
  //          .put(
  //              DEFAULT_INBOUND_ACTION,
  //              new PropertyDescriptor<>(Configuration::getDefaultInboundAction, Schema.STRING))
  //          .put(DEVICE_TYPE, new PropertyDescriptor<>(Configuration::getDeviceType,
  // Schema.STRING))
  //          .put(
  //              DNS_SERVERS,
  //              new PropertyDescriptor<>(Configuration::getDnsServers, Schema.set(Schema.STRING)))
  //          .put(
  //              DNS_SOURCE_INTERFACE,
  //              new PropertyDescriptor<>(Configuration::getDnsSourceInterface, Schema.STRING))
  //          .put(DOMAIN_NAME, new PropertyDescriptor<>(Configuration::getDomainName,
  // Schema.STRING))
  //          .put(HOSTNAME, new PropertyDescriptor<>(Configuration::getHostname, Schema.STRING))
  //          .put(
  //              IKE_PHASE1_KEYS,
  //              new PropertyDescriptor<>(Configuration::getIkePhase1Keys,
  // Schema.set(Schema.STRING)))
  //          .put(
  //              IKE_PHASE1_POLICIES,
  //              new PropertyDescriptor<>(
  //                  Configuration::getIkePhase1Policies, Schema.set(Schema.STRING)))
  //          .put(
  //              IKE_PHASE1_PROPOSALS,
  //              new PropertyDescriptor<>(
  //                  Configuration::getIkePhase1Proposals, Schema.set(Schema.STRING)))
  //          .put(
  //              INTERFACES,
  //              new PropertyDescriptor<>(Configuration::getAllInterfaces,
  // Schema.set(Schema.STRING)))
  //          .put(
  //              IP_ACCESS_LISTS,
  //              new PropertyDescriptor<>(Configuration::getIpAccessLists,
  // Schema.set(Schema.STRING)))
  //          .put(
  //              IP_SPACES,
  //              new PropertyDescriptor<>(Configuration::getIpSpaces, Schema.set(Schema.STRING)))
  //          .put(
  //              IP_6_ACCESS_LISTS,
  //              new PropertyDescriptor<>(Configuration::getIp6AccessLists,
  // Schema.set(Schema.STRING)))
  //          .put(
  //              IPSEC_PEER_CONFIGS,
  //              new PropertyDescriptor<>(
  //                  Configuration::getIpsecPeerConfigs, Schema.set(Schema.STRING)))
  //          .put(
  //              IPSEC_PHASE2_POLICIES,
  //              new PropertyDescriptor<>(
  //                  Configuration::getIpsecPhase2Policies, Schema.set(Schema.STRING)))
  //          .put(
  //              IPSEC_PHASE2_PROPOSALS,
  //              new PropertyDescriptor<>(
  //                  Configuration::getIpsecPhase2Proposals, Schema.set(Schema.STRING)))
  //          .put(
  //              LOGGING_SERVERS,
  //              new PropertyDescriptor<>(Configuration::getLoggingServers,
  // Schema.set(Schema.STRING)))
  //          .put(
  //              LOGGING_SOURCE_INTERFACE,
  //              new PropertyDescriptor<>(Configuration::getLoggingSourceInterface, Schema.STRING))
  //          .put(
  //              NTP_SERVERS,
  //              new PropertyDescriptor<>(Configuration::getNtpServers, Schema.set(Schema.STRING)))
  //          .put(
  //              NTP_SOURCE_INTERFACE,
  //              new PropertyDescriptor<>(Configuration::getNtpSourceInterface, Schema.STRING))
  //          .put(
  //              ROUTE_FILTER_LISTS,
  //              new PropertyDescriptor<>(
  //                  Configuration::getRouteFilterLists, Schema.set(Schema.STRING)))
  //          .put(
  //              ROUTE_6_FILTER_LISTS,
  //              new PropertyDescriptor<>(
  //                  Configuration::getRoute6FilterLists, Schema.set(Schema.STRING)))
  //          .put(
  //              ROUTING_POLICIES,
  //              new PropertyDescriptor<>(
  //                  Configuration::getRoutingPolicies, Schema.set(Schema.STRING)))
  //          .put(
  //              SNMP_SOURCE_INTERFACE,
  //              new PropertyDescriptor<>(Configuration::getSnmpSourceInterface, Schema.STRING))
  //          .put(
  //              SNMP_TRAP_SERVERS,
  //              new PropertyDescriptor<>(
  //                  Configuration::getSnmpTrapServers, Schema.set(Schema.STRING)))
  //          .put(
  //              TACACS_SERVERS,
  //              new PropertyDescriptor<>(Configuration::getTacacsServers,
  // Schema.set(Schema.STRING)))
  //          .put(
  //              TACACS_SOURCE_INTERFACE,
  //              new PropertyDescriptor<>(Configuration::getTacacsSourceInterface, Schema.STRING))
  //          .put(
  //              VENDOR_FAMILY,
  //              new PropertyDescriptor<>(Configuration::getVendorFamily, Schema.STRING))
  //          .put(VRFS, new PropertyDescriptor<>(Configuration::getVrfs,
  // Schema.set(Schema.STRING)))
  //          .put(ZONES, new PropertyDescriptor<>(Configuration::getZones,
  // Schema.set(Schema.STRING)))
  //          .build();

  public static final NodePropertySpecifier ALL = new NodePropertySpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public NodePropertySpecifier(String expression) {
    _expression = expression;
    _pattern = Pattern.compile(_expression.trim(), Pattern.CASE_INSENSITIVE);
  }

  public NodePropertySpecifier(Collection<String> properties) {
    // quote and join
    _expression =
        properties.stream().map(String::trim).map(Pattern::quote).collect(Collectors.joining("|"));
    _pattern = Pattern.compile(_expression, Pattern.CASE_INSENSITIVE);
  }

  @Override
  public List<String> getMatchingProperties() {
    return JAVA_MAP.keySet().stream()
        .filter(prop -> _pattern.matcher(prop).matches())
        .collect(ImmutableList.toImmutableList());
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
