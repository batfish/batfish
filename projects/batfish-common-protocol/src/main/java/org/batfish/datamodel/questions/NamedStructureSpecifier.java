package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Schema;

/** Enables specification a set of named structures. */
public class NamedStructureSpecifier extends PropertySpecifier {

  public static final String AS_PATH_ACCESS_LIST = "AS_Path_Access_List";
  public static final String AUTHENTICATION_KEY_CHAIN = "Authentication_Key_Chain";
  public static final String COMMUNITY_LIST = "Community_List";
  public static final String IKE_PHASE1_KEYS = "IKE_Phase1_Keys";
  public static final String IKE_PHASE1_POLICIES = "IKE_Phase1_Policies";
  public static final String IKE_PHASE1_PROPOSALS = "IKE_Phase1_Proposals";
  public static final String IP_ACCESS_LIST = "IP_Access_List";
  public static final String IP_6_ACCESS_LIST = "IP6_Access_List";
  public static final String IPSEC_PEER_CONFIGS = "IPsec_Peer_Configs";
  public static final String IPSEC_PHASE2_POLICIES = "IPsec_Phase2_Policies";
  public static final String IPSEC_PHASE2_PROPOSALS = "IPsec_Phase2_Proposals";
  public static final String ROUTE_FILTER_LIST = "Route_Filter_List";
  public static final String ROUTE_6_FILTER_LIST = "Route6_Filter_List";
  public static final String ROUTING_POLICY = "Routing_Policy";
  public static final String VRF = "VRF";
  public static final String ZONE = "Zone";

  public static Map<String, PropertyDescriptor<Configuration>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<Configuration>>()
          .put(
              AS_PATH_ACCESS_LIST,
              new PropertyDescriptor<>(
                  Configuration::getAsPathAccessLists, Schema.OBJECT, "AS path access list"))
          .put(
              AUTHENTICATION_KEY_CHAIN,
              new PropertyDescriptor<>(
                  Configuration::getAuthenticationKeyChains,
                  Schema.OBJECT,
                  "Authentication keychain"))
          .put(
              COMMUNITY_LIST,
              new PropertyDescriptor<>(
                  Configuration::getCommunityLists, Schema.OBJECT, "Community list"))
          .put(
              IKE_PHASE1_KEYS,
              new PropertyDescriptor<>(
                  Configuration::getIkePhase1Keys, Schema.OBJECT, "IKE Phase 1 keys"))
          .put(
              IKE_PHASE1_POLICIES,
              new PropertyDescriptor<>(
                  Configuration::getIkePhase1Policies, Schema.OBJECT, "IKE Phase 1 policies"))
          .put(
              IKE_PHASE1_PROPOSALS,
              new PropertyDescriptor<>(
                  Configuration::getIkePhase1Proposals, Schema.OBJECT, "IKE Phase 1 proposals"))
          .put(
              IP_ACCESS_LIST,
              new PropertyDescriptor<>(
                  Configuration::getIpAccessLists,
                  Schema.OBJECT,
                  "IPv4 filter (ACL, firewall ruleset)"))
          .put(
              IP_6_ACCESS_LIST,
              new PropertyDescriptor<>(
                  Configuration::getIp6AccessLists,
                  Schema.OBJECT,
                  "IPv6 filter (ACL, firewall ruleset)"))
          .put(
              IPSEC_PEER_CONFIGS,
              new PropertyDescriptor<>(
                  Configuration::getIpsecPeerConfigs, Schema.OBJECT, "IPSec peer configs"))
          .put(
              IPSEC_PHASE2_POLICIES,
              new PropertyDescriptor<>(
                  Configuration::getIpsecPhase2Policies, Schema.OBJECT, "IPSec Phase 2 policies"))
          .put(
              IPSEC_PHASE2_PROPOSALS,
              new PropertyDescriptor<>(
                  Configuration::getIpsecPhase2Proposals, Schema.OBJECT, "IPSec Phase 2 proposals"))
          .put(
              ROUTE_FILTER_LIST,
              new PropertyDescriptor<>(
                  Configuration::getRouteFilterLists,
                  Schema.OBJECT,
                  "IPv4 route filter list (prefix list, ACLs etc. used to filter routes)"))
          .put(
              ROUTE_6_FILTER_LIST,
              new PropertyDescriptor<>(
                  Configuration::getRoute6FilterLists,
                  Schema.OBJECT,
                  "IPv6 route filter list (prefix list, ACLs etc. used to filter routes)"))
          .put(
              ROUTING_POLICY,
              new PropertyDescriptor<>(
                  Configuration::getRoutingPolicies,
                  Schema.OBJECT,
                  "Policy for route manipulation (e.g., route maps)"))
          .put(VRF, new PropertyDescriptor<>(Configuration::getVrfs, Schema.OBJECT, "VRF"))
          .put(ZONE, new PropertyDescriptor<>(Configuration::getZones, Schema.OBJECT, "Zone"))
          .build();

  public static final NamedStructureSpecifier ALL = new NamedStructureSpecifier(".*");

  private final String _expression;

  private final Pattern _pattern;

  @JsonCreator
  public NamedStructureSpecifier(String expression) {
    _expression = expression;
    _pattern = Pattern.compile(_expression.trim(), Pattern.CASE_INSENSITIVE); // canonicalize
  }

  public NamedStructureSpecifier(Collection<String> structureTypes) {
    // quote and join
    _expression =
        structureTypes.stream()
            .map(String::trim)
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));
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
