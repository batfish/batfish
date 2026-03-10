package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.vendor.StructureType;

public enum CiscoNxosStructureType implements StructureType {
  BGP_NEIGHBOR("bgp neighbor"),
  BGP_TEMPLATE_PEER("bgp template peer"),
  BGP_TEMPLATE_PEER_POLICY("bgp template peer-policy"),
  BGP_TEMPLATE_PEER_SESSION("bgp template peer-session"),
  CLASS_MAP_CONTROL_PLANE("class-map type control-plane"),
  CLASS_MAP_NETWORK_QOS("class-map type network-qos"),
  CLASS_MAP_QOS("class-map type qos"),
  CLASS_MAP_QUEUING("class-map type queuing"),
  FLOW_EXPORTER("flow exporter"),
  FLOW_MONITOR("flow monitor"),
  FLOW_RECORD("flow record"),
  INTERFACE("interface"),
  IP_ACCESS_LIST("ip access-list"),
  IP_ACCESS_LIST_LINE("ip access-list line"),
  IP_ACCESS_LIST_ABSTRACT_REF("ip[v4|v6] access-list"),
  IP_AS_PATH_ACCESS_LIST("ip as-path access-list"),
  IP_COMMUNITY_LIST_ABSTRACT_REF("ip community-list [expanded|standard]"),
  IP_COMMUNITY_LIST_EXPANDED("ip community-list expanded"),
  IP_COMMUNITY_LIST_STANDARD("ip community-list standard"),
  IP_OR_MAC_ACCESS_LIST_ABSTRACT_REF("[mac|ip|ipv6] access-list"),
  IP_PREFIX_LIST("ip prefix-list"),
  IPV6_ACCESS_LIST("ipv6 access-list"),
  IPV6_PREFIX_LIST("ipv6 prefix-list"),
  MAC_ACCESS_LIST("mac access-list"),
  NVE("nve"),
  OBJECT_GROUP_IP_ADDRESS("object-group ip address"),
  OBJECT_GROUP_IP_PORT("object-group ip port"),
  POLICY_MAP_CONTROL_PLANE("policy-map type control-plane"),
  POLICY_MAP_NETWORK_QOS("policy-map type network-qos"),
  POLICY_MAP_QOS("policy-map type qos"),
  POLICY_MAP_QUEUING("policy-map type queuing"),
  PORT_CHANNEL("port-channel"),
  ROUTE_MAP("route-map"),
  ROUTE_MAP_ENTRY("route-map entry"),
  ROUTER_EIGRP("router eigrp"),
  ROUTER_ISIS("router isis"),
  ROUTER_OSPF("router ospf"),
  ROUTER_OSPFV3("router ospfv3"),
  ROUTER_RIP("router rip"),
  TRACK("track"),
  VLAN("vlan"),
  VRF("vrf");

  public static final Multimap<CiscoNxosStructureType, CiscoNxosStructureType> ABSTRACT_STRUCTURES =
      ImmutableListMultimap.<CiscoNxosStructureType, CiscoNxosStructureType>builder()
          .putAll(IP_ACCESS_LIST_ABSTRACT_REF, IP_ACCESS_LIST, IPV6_ACCESS_LIST)
          .putAll(
              IP_COMMUNITY_LIST_ABSTRACT_REF,
              IP_COMMUNITY_LIST_EXPANDED,
              IP_COMMUNITY_LIST_STANDARD)
          .putAll(
              IP_OR_MAC_ACCESS_LIST_ABSTRACT_REF, IP_ACCESS_LIST, IPV6_ACCESS_LIST, MAC_ACCESS_LIST)
          .build();

  public static final Set<CiscoNxosStructureType> CONCRETE_STRUCTURES =
      ImmutableSet.copyOf(
          Sets.difference(ImmutableSet.copyOf(values()), ABSTRACT_STRUCTURES.keySet()));

  private final @Nonnull String _description;

  private CiscoNxosStructureType(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
