package org.batfish.datamodel.questions;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.Schema;
import org.batfish.specifier.ConstantEnumSetSpecifier;
import org.batfish.specifier.SpecifierFactories;
import org.batfish.specifier.parboiled.Grammar;

/** Enables specification a set of named structures. */
@ParametersAreNonnullByDefault
public class NamedStructurePropertySpecifier extends PropertySpecifier {

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
  public static final String PBR_POLICY = "PBR_Policy";
  public static final String ROUTE_FILTER_LIST = "Route_Filter_List";
  public static final String ROUTE_6_FILTER_LIST = "Route6_Filter_List";
  public static final String ROUTING_POLICY = "Routing_Policy";
  public static final String VRF = "VRF";
  public static final String ZONE = "Zone";

  public static final Map<String, PropertyDescriptor<Configuration>> JAVA_MAP =
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
                  Configuration::getCommunityLists, Schema.OBJECT, "BGP community list"))
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
              PBR_POLICY,
              new PropertyDescriptor<>(
                  Configuration::getPacketPolicies,
                  Schema.OBJECT,
                  "Policy-based routing (PBR) policy"))
          .put(
              ROUTE_FILTER_LIST,
              new PropertyDescriptor<>(
                  Configuration::getRouteFilterLists,
                  Schema.OBJECT,
                  "IPv4 route filter list (prefix list, ACL etc. used to filter routes)"))
          .put(
              ROUTE_6_FILTER_LIST,
              new PropertyDescriptor<>(
                  Configuration::getRoute6FilterLists,
                  Schema.OBJECT,
                  "IPv6 route filter list (prefix list, ACL etc. used to filter routes)"))
          .put(
              ROUTING_POLICY,
              new PropertyDescriptor<>(
                  Configuration::getRoutingPolicies,
                  Schema.OBJECT,
                  "Policy for route manipulation (e.g., route maps)"))
          .put(VRF, new PropertyDescriptor<>(Configuration::getVrfs, Schema.OBJECT, "VRF"))
          .put(
              ZONE,
              new PropertyDescriptor<>(
                  Configuration::getZones, Schema.OBJECT, "Firewall security zone"))
          .build();

  /** A specifier for all properties */
  public static final NamedStructurePropertySpecifier ALL =
      new NamedStructurePropertySpecifier(JAVA_MAP.keySet());

  @Nonnull private final List<String> _properties;

  /** Creates the specifier object from the grammar expression */
  public static NamedStructurePropertySpecifier create(String expression) {
    return new NamedStructurePropertySpecifier(
        SpecifierFactories.getEnumSetSpecifierOrDefault(
                expression,
                Grammar.NAMED_STRUCTURE_SPECIFIER,
                new ConstantEnumSetSpecifier<>(JAVA_MAP.keySet()))
            .resolve());
  }

  /** Returns a specifier object that maps to the specified properties */
  public NamedStructurePropertySpecifier(Set<String> properties) {
    Set<String> diffSet = Sets.difference(properties, JAVA_MAP.keySet());
    checkArgument(
        diffSet.isEmpty(),
        "Invalid properties supplied: %s. Valid properties are %s",
        diffSet,
        JAVA_MAP.keySet());
    _properties = properties.stream().sorted().collect(ImmutableList.toImmutableList());
  }

  @Override
  @Nonnull
  public List<String> getMatchingProperties() {
    return _properties;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NamedStructurePropertySpecifier)) {
      return false;
    }
    return _properties.equals(((NamedStructurePropertySpecifier) o)._properties);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_properties);
  }
}
