package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
import org.batfish.datamodel.answers.Schema;

/** Enables specification a set of named structures. */
public class NamedStructureSpecifier extends PropertySpecifier {

  public static final String AS_PATH_ACCESS_LIST = "AS_Path_Access_List";
  public static final String AUTHENTICATION_KEY_CHAIN = "Authentication_Key_Chain";
  public static final String COMMUNITY_LIST = "Community_List";
  public static final String IKE_POLICIES = "IKE_Policies";
  public static final String IP_ACCESS_LIST = "IP_Access_List";
  public static final String IP_6_ACCESS_LIST = "IP6_Access_List";
  public static final String IPSEC_POLICY = "IPSec_Policy";
  public static final String IPSEC_PROPOSAL = "IPSec_Proposal";
  public static final String IPSEC_VPN = "IPSec_Vpn";
  public static final String ROUTE_FILTER_LIST = "Route_Filter_List";
  public static final String ROUTE_6_FILTER_LIST = "Route6_Filter_List";
  public static final String ROUTING_POLICY = "Routing_Policy";
  public static final String VRF = "VRF";
  public static final String ZONE = "Zone";

  public static Map<String, PropertyDescriptor<Configuration>> JAVA_MAP =
      new ImmutableMap.Builder<String, PropertyDescriptor<Configuration>>()
          .put(
              AS_PATH_ACCESS_LIST,
              new PropertyDescriptor<>(Configuration::getAsPathAccessLists, Schema.OBJECT))
          .put(
              AUTHENTICATION_KEY_CHAIN,
              new PropertyDescriptor<>(Configuration::getAuthenticationKeyChains, Schema.OBJECT))
          .put(
              COMMUNITY_LIST,
              new PropertyDescriptor<>(Configuration::getCommunityLists, Schema.OBJECT))
          .put(IKE_POLICIES, new PropertyDescriptor<>(Configuration::getIkePolicies, Schema.OBJECT))
          .put(
              IP_ACCESS_LIST,
              new PropertyDescriptor<>(Configuration::getIpAccessLists, Schema.OBJECT))
          .put(
              IP_6_ACCESS_LIST,
              new PropertyDescriptor<>(Configuration::getIp6AccessLists, Schema.OBJECT))
          .put(
              IPSEC_POLICY,
              new PropertyDescriptor<>(Configuration::getIpsecPolicies, Schema.OBJECT))
          .put(
              IPSEC_PROPOSAL,
              new PropertyDescriptor<>(Configuration::getIpsecProposals, Schema.OBJECT))
          .put(IPSEC_VPN, new PropertyDescriptor<>(Configuration::getIpsecVpns, Schema.OBJECT))
          .put(
              ROUTE_FILTER_LIST,
              new PropertyDescriptor<>(Configuration::getRouteFilterLists, Schema.OBJECT))
          .put(
              ROUTE_6_FILTER_LIST,
              new PropertyDescriptor<>(Configuration::getRoute6FilterLists, Schema.OBJECT))
          .put(
              ROUTING_POLICY,
              new PropertyDescriptor<>(Configuration::getRoutingPolicies, Schema.OBJECT))
          .put(VRF, new PropertyDescriptor<>(Configuration::getVrfs, Schema.OBJECT))
          .put(ZONE, new PropertyDescriptor<>(Configuration::getZones, Schema.OBJECT))
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
        structureTypes
            .stream()
            .map(String::trim)
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));
    _pattern = Pattern.compile(_expression, Pattern.CASE_INSENSITIVE);
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
