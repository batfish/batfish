package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.answers.Schema;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.Grammar;
import org.batfish.specifier.SpecifierFactories;

/**
 * Enables specification a set of node properties.
 *
 * <p>Currently supported example specifier:
 *
 * <ul>
 *   <li>ntp_servers -&gt; gets NTP servers using a configured Java function
 *   <li>ntp.* gets all properties that start with 'ntp'
 * </ul>
 *
 * <p>In the future, we might add other specifier types, e.g., those based on Json Path
 */
@ParametersAreNonnullByDefault
public class NodePropertySpecifier extends PropertySpecifier {

  public static final String AS_PATH_ACCESS_LISTS = "AS_Path_Access_Lists";
  public static final String AUTHENTICATION_KEY_CHAINS = "Authentication_Key_Chains";
  public static final String COMMUNITY_MATCH_EXPRS = "Community_Match_Exprs";
  public static final String COMMUNITY_SET_EXPRS = "Community_Set_Exprs";
  public static final String COMMUNITY_SET_MATCH_EXPRS = "Community_Set_Match_Exprs";
  public static final String COMMUNITY_SETS = "Community_Sets";
  public static final String CONFIGURATION_FORMAT = "Configuration_Format";
  public static final String DEFAULT_CROSS_ZONE_ACTION = "Default_Cross_Zone_Action";
  public static final String DEFAULT_INBOUND_ACTION = "Default_Inbound_Action";
  public static final String DNS_SERVERS = "DNS_Servers";
  public static final String DNS_SOURCE_INTERFACE = "DNS_Source_Interface";
  public static final String DOMAIN_NAME = "Domain_Name";
  public static final String HOSTNAME = "Hostname";
  public static final String IKE_PHASE1_KEYS = "IKE_Phase1_Keys";
  public static final String IKE_PHASE1_POLICIES = "IKE_Phase1_Policies";
  public static final String IKE_PHASE1_PROPOSALS = "IKE_Phase1_Proposals";
  public static final String INTERFACES = "Interfaces";
  public static final String IP_ACCESS_LISTS = "IP_Access_Lists";
  public static final String IP_6_ACCESS_LISTS = "IP6_Access_Lists";
  public static final String IPSEC_PEER_CONFIGS = "IPsec_Peer_Configs";
  public static final String IPSEC_PHASE2_POLICIES = "IPsec_Phase2_Policies";
  public static final String IPSEC_PHASE2_PROPOSALS = "IPsec_Phase2_Proposals";
  public static final String LOGGING_SERVERS = "Logging_Servers";
  public static final String LOGGING_SOURCE_INTERFACE = "Logging_Source_Interface";
  public static final String NTP_SERVERS = "NTP_Servers";
  public static final String NTP_SOURCE_INTERFACE = "NTP_Source_Interface";
  public static final String PBR_POLICIES = "PBR_Policies";
  public static final String ROUTE_FILTER_LISTS = "Route_Filter_Lists";
  public static final String ROUTE_6_FILTER_LISTS = "Route6_Filter_Lists";
  public static final String ROUTING_POLICIES = "Routing_Policies";
  public static final String SNMP_SOURCE_INTERFACE = "SNMP_Source_Interface";
  public static final String SNMP_TRAP_SERVERS = "SNMP_Trap_Servers";
  public static final String TACACS_SERVERS = "TACACS_Servers";
  public static final String TACACS_SOURCE_INTERFACE = "TACACS_Source_Interface";
  public static final String VRFS = "VRFs";
  public static final String ZONES = "Zones";

  private static final Map<String, PropertyDescriptor<Configuration>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<Configuration>>()
          .put(
              AS_PATH_ACCESS_LISTS,
              new PropertyDescriptor<>(
                  Configuration::getAsPathAccessLists,
                  Schema.set(Schema.STRING),
                  "Names of AS path access lists"))
          .put(
              AUTHENTICATION_KEY_CHAINS,
              new PropertyDescriptor<>(
                  Configuration::getAuthenticationKeyChains,
                  Schema.set(Schema.STRING),
                  "Names of authentication keychains"))
          .put(
              COMMUNITY_MATCH_EXPRS,
              new PropertyDescriptor<>(
                  Configuration::getCommunityMatchExprs,
                  Schema.set(Schema.STRING),
                  "Names of expressions for matching a community"))
          .put(
              COMMUNITY_SET_EXPRS,
              new PropertyDescriptor<>(
                  Configuration::getCommunitySetExprs,
                  Schema.set(Schema.STRING),
                  "Names of expressions representing a community-set"))
          .put(
              COMMUNITY_SET_MATCH_EXPRS,
              new PropertyDescriptor<>(
                  Configuration::getCommunitySetMatchExprs,
                  Schema.set(Schema.STRING),
                  "Names of expressions for matching a ommunity-set"))
          .put(
              COMMUNITY_SETS,
              new PropertyDescriptor<>(
                  Configuration::getCommunitySets,
                  Schema.set(Schema.STRING),
                  "Names of community-sets"))
          .put(
              CONFIGURATION_FORMAT,
              new PropertyDescriptor<>(
                  Configuration::getConfigurationFormat,
                  Schema.STRING,
                  "Configuration format of the node"))
          .put(
              DEFAULT_CROSS_ZONE_ACTION,
              new PropertyDescriptor<>(
                  Configuration::getDefaultCrossZoneAction,
                  Schema.STRING,
                  "Default action ("
                      + Arrays.stream(LineAction.values())
                          .map(Object::toString)
                          .collect(Collectors.joining(", "))
                      + ") for traffic that traverses firewall zones (null for non-firewall"
                      + " nodes)"))
          .put(
              DEFAULT_INBOUND_ACTION,
              new PropertyDescriptor<>(
                  Configuration::getDefaultInboundAction,
                  Schema.STRING,
                  "Default action ("
                      + Arrays.stream(LineAction.values())
                          .map(Object::toString)
                          .collect(Collectors.joining(", "))
                      + ") for traffic destined for this node"))
          .put(
              DNS_SERVERS,
              new PropertyDescriptor<>(
                  Configuration::getDnsServers,
                  Schema.set(Schema.STRING),
                  "Configured DNS servers"))
          .put(
              DNS_SOURCE_INTERFACE,
              new PropertyDescriptor<>(
                  Configuration::getDnsSourceInterface,
                  Schema.STRING,
                  "Source interface to use for communicating with DNS servers"))
          .put(
              DOMAIN_NAME,
              new PropertyDescriptor<>(
                  Configuration::getDomainName, Schema.STRING, "Domain name of the node"))
          .put(
              HOSTNAME,
              new PropertyDescriptor<>(
                  Configuration::getHostname, Schema.STRING, "Hostname of the node"))
          .put(
              IKE_PHASE1_KEYS,
              new PropertyDescriptor<>(
                  Configuration::getIkePhase1Keys,
                  Schema.set(Schema.STRING),
                  "Names of IKE Phase 1 keys"))
          .put(
              IKE_PHASE1_POLICIES,
              new PropertyDescriptor<>(
                  Configuration::getIkePhase1Policies,
                  Schema.set(Schema.STRING),
                  "Names of IKE Phase 1 policies"))
          .put(
              IKE_PHASE1_PROPOSALS,
              new PropertyDescriptor<>(
                  Configuration::getIkePhase1Proposals,
                  Schema.set(Schema.STRING),
                  "Names of IKE Phase 1 proposals"))
          .put(
              INTERFACES,
              new PropertyDescriptor<>(
                  Configuration::getAllInterfaces,
                  Schema.set(Schema.STRING),
                  "Names of interfaces"))
          .put(
              IP_ACCESS_LISTS,
              new PropertyDescriptor<>(
                  Configuration::getIpAccessLists,
                  Schema.set(Schema.STRING),
                  "Names of IPv4 filters (ACLs, firewall rule sets)"))
          .put(
              IP_6_ACCESS_LISTS,
              new PropertyDescriptor<>(
                  c -> ImmutableSet.of(),
                  Schema.set(Schema.STRING),
                  "(Deprecated) Names of IPv6 filters (ACLs, firewall rule sets)"))
          .put(
              IPSEC_PEER_CONFIGS,
              new PropertyDescriptor<>(
                  Configuration::getIpsecPeerConfigs,
                  Schema.set(Schema.STRING),
                  "Names of IPSec peers"))
          .put(
              IPSEC_PHASE2_POLICIES,
              new PropertyDescriptor<>(
                  Configuration::getIpsecPhase2Policies,
                  Schema.set(Schema.STRING),
                  "Names of IPSec Phase 2 policies"))
          .put(
              IPSEC_PHASE2_PROPOSALS,
              new PropertyDescriptor<>(
                  Configuration::getIpsecPhase2Proposals,
                  Schema.set(Schema.STRING),
                  "Names of IPSec Phase 2 proposals"))
          .put(
              LOGGING_SERVERS,
              new PropertyDescriptor<>(
                  Configuration::getLoggingServers,
                  Schema.set(Schema.STRING),
                  "Configured logging servers"))
          .put(
              LOGGING_SOURCE_INTERFACE,
              new PropertyDescriptor<>(
                  Configuration::getLoggingSourceInterface,
                  Schema.STRING,
                  "Source interface for communicating with logging servers"))
          .put(
              NTP_SERVERS,
              new PropertyDescriptor<>(
                  Configuration::getNtpServers,
                  Schema.set(Schema.STRING),
                  "Configured NTP servers"))
          .put(
              NTP_SOURCE_INTERFACE,
              new PropertyDescriptor<>(
                  Configuration::getNtpSourceInterface,
                  Schema.STRING,
                  "Source interface for communicating with NTP servers"))
          .put(
              PBR_POLICIES,
              new PropertyDescriptor<>(
                  Configuration::getPacketPolicies,
                  Schema.set(Schema.STRING),
                  "Names of policy-based routing (PBR) policies"))
          .put(
              ROUTE_FILTER_LISTS,
              new PropertyDescriptor<>(
                  Configuration::getRouteFilterLists,
                  Schema.set(Schema.STRING),
                  "Names of structures that filter IPv4 routes (e.g., prefix lists)"))
          .put(
              ROUTE_6_FILTER_LISTS,
              new PropertyDescriptor<>(
                  c -> ImmutableSet.of(),
                  Schema.set(Schema.STRING),
                  "(Deprecated) Names of structures that filter IPv6 routes (e.g., prefix lists)"))
          .put(
              ROUTING_POLICIES,
              new PropertyDescriptor<>(
                  Configuration::getRoutingPolicies,
                  Schema.set(Schema.STRING),
                  "Names of policies that manipulate routes (e.g., route maps)"))
          .put(
              SNMP_SOURCE_INTERFACE,
              new PropertyDescriptor<>(
                  Configuration::getSnmpSourceInterface,
                  Schema.STRING,
                  "Source interface to use for communicating with SNMP servers"))
          .put(
              SNMP_TRAP_SERVERS,
              new PropertyDescriptor<>(
                  Configuration::getSnmpTrapServers,
                  Schema.set(Schema.STRING),
                  "Configured SNMP trap servers"))
          .put(
              TACACS_SERVERS,
              new PropertyDescriptor<>(
                  Configuration::getTacacsServers,
                  Schema.set(Schema.STRING),
                  "Configured TACACS servers"))
          .put(
              TACACS_SOURCE_INTERFACE,
              new PropertyDescriptor<>(
                  Configuration::getTacacsSourceInterface,
                  Schema.STRING,
                  "Source interface to use for communicating with TACACS servers"))
          .put(
              VRFS,
              new PropertyDescriptor<>(
                  Configuration::getVrfs,
                  Schema.set(Schema.STRING),
                  "Names of VRFs present on the node"))
          .put(
              ZONES,
              new PropertyDescriptor<>(
                  Configuration::getZones,
                  Schema.set(Schema.STRING),
                  "Names of firewall zones on the node"))
          .build();

  /** Returns the property descriptor for {@code property} */
  public static PropertyDescriptor<Configuration> getPropertyDescriptor(String property) {
    checkArgument(JAVA_MAP.containsKey(property), "Property " + property + " does not exist");
    return JAVA_MAP.get(property);
  }

  public static final NodePropertySpecifier ALL = new NodePropertySpecifier(JAVA_MAP.keySet());

  private final @Nonnull List<String> _properties;

  /**
   * Create a node property specifier from provided expression. If the expression is null or empty,
   * a specifier with all properties is returned.
   */
  public static NodePropertySpecifier create(@Nullable String expression) {
    return new NodePropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression,
                Grammar.NODE_PROPERTY_SPECIFIER,
                new ConstantEnumSetSpecifier<>(JAVA_MAP.keySet()))
            .resolve());
  }

  public NodePropertySpecifier(Set<String> properties) {
    Set<String> diffSet = Sets.difference(properties, JAVA_MAP.keySet());
    checkArgument(
        diffSet.isEmpty(),
        "Invalid properties supplied: %s. Valid properties are %s",
        diffSet,
        JAVA_MAP.keySet());
    _properties = properties.stream().sorted().collect(ImmutableList.toImmutableList());
  }

  @Override
  public List<String> getMatchingProperties() {
    return _properties;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodePropertySpecifier)) {
      return false;
    }
    return _properties.equals(((NodePropertySpecifier) o)._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_properties);
  }
}
