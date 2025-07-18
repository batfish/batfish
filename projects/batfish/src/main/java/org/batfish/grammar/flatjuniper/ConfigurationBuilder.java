package org.batfish.grammar.flatjuniper;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.datamodel.Names.bgpNeighborStructureName;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.batfish.representation.juniper.CommunityMemberParseResult.parseCommunityMember;
import static org.batfish.representation.juniper.JuniperConfiguration.ACL_NAME_GLOBAL_POLICY;
import static org.batfish.representation.juniper.JuniperConfiguration.computeFirewallFilterTermName;
import static org.batfish.representation.juniper.JuniperConfiguration.computePolicyStatementTermName;
import static org.batfish.representation.juniper.JuniperConfiguration.computeSecurityPolicyTermName;
import static org.batfish.representation.juniper.JuniperStructureType.ADDRESS_BOOK;
import static org.batfish.representation.juniper.JuniperStructureType.ADMIN_GROUP;
import static org.batfish.representation.juniper.JuniperStructureType.APPLICATION;
import static org.batfish.representation.juniper.JuniperStructureType.APPLICATION_OR_APPLICATION_SET;
import static org.batfish.representation.juniper.JuniperStructureType.APPLICATION_SET;
import static org.batfish.representation.juniper.JuniperStructureType.AS_PATH;
import static org.batfish.representation.juniper.JuniperStructureType.AS_PATH_GROUP;
import static org.batfish.representation.juniper.JuniperStructureType.AS_PATH_GROUP_AS_PATH;
import static org.batfish.representation.juniper.JuniperStructureType.AUTHENTICATION_KEY_CHAIN;
import static org.batfish.representation.juniper.JuniperStructureType.BGP_GROUP;
import static org.batfish.representation.juniper.JuniperStructureType.BRIDGE_DOMAIN;
import static org.batfish.representation.juniper.JuniperStructureType.CLASS_OF_SERVICE_CODE_POINT_ALIAS;
import static org.batfish.representation.juniper.JuniperStructureType.COMMUNITY;
import static org.batfish.representation.juniper.JuniperStructureType.CONDITION;
import static org.batfish.representation.juniper.JuniperStructureType.DHCP_RELAY_SERVER_GROUP;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_FILTER;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_FILTER_TERM;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_INET6_FILTER;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_INTERFACE_SET;
import static org.batfish.representation.juniper.JuniperStructureType.IKE_GATEWAY;
import static org.batfish.representation.juniper.JuniperStructureType.IKE_POLICY;
import static org.batfish.representation.juniper.JuniperStructureType.IKE_PROPOSAL;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureType.IPSEC_POLICY;
import static org.batfish.representation.juniper.JuniperStructureType.IPSEC_PROPOSAL;
import static org.batfish.representation.juniper.JuniperStructureType.LOGICAL_SYSTEM;
import static org.batfish.representation.juniper.JuniperStructureType.MPLS_PATH;
import static org.batfish.representation.juniper.JuniperStructureType.NAT_POOL;
import static org.batfish.representation.juniper.JuniperStructureType.NAT_RULE;
import static org.batfish.representation.juniper.JuniperStructureType.NAT_RULE_SET;
import static org.batfish.representation.juniper.JuniperStructureType.POLICY_STATEMENT;
import static org.batfish.representation.juniper.JuniperStructureType.POLICY_STATEMENT_TERM;
import static org.batfish.representation.juniper.JuniperStructureType.PREFIX_LIST;
import static org.batfish.representation.juniper.JuniperStructureType.RIB_GROUP;
import static org.batfish.representation.juniper.JuniperStructureType.ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureType.RTF_PREFIX_LIST;
import static org.batfish.representation.juniper.JuniperStructureType.SECURITY_POLICY;
import static org.batfish.representation.juniper.JuniperStructureType.SECURITY_POLICY_TERM;
import static org.batfish.representation.juniper.JuniperStructureType.SECURITY_PROFILE;
import static org.batfish.representation.juniper.JuniperStructureType.SNMP_CLIENT_LIST;
import static org.batfish.representation.juniper.JuniperStructureType.SNMP_CLIENT_LIST_OR_PREFIX_LIST;
import static org.batfish.representation.juniper.JuniperStructureType.SRLG;
import static org.batfish.representation.juniper.JuniperStructureType.TUNNEL_ATTRIBUTE;
import static org.batfish.representation.juniper.JuniperStructureType.VLAN;
import static org.batfish.representation.juniper.JuniperStructureUsage.ADDRESS_BOOK_ATTACH_ZONE;
import static org.batfish.representation.juniper.JuniperStructureUsage.ADD_PATH_SEND_PREFIX_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.AGGREGATE_ROUTE_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.APPLICATION_SET_MEMBER_APPLICATION;
import static org.batfish.representation.juniper.JuniperStructureUsage.APPLICATION_SET_MEMBER_APPLICATION_SET;
import static org.batfish.representation.juniper.JuniperStructureUsage.AS_PATH_GROUP_AS_PATH_SELF_REFERENCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.AUTHENTICATION_KEY_CHAINS_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.BGP_ALLOW;
import static org.batfish.representation.juniper.JuniperStructureUsage.BGP_EXPORT_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.BGP_FAMILY_INET_UNICAST_RIB_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.BGP_IMPORT_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.BGP_NEIGHBOR;
import static org.batfish.representation.juniper.JuniperStructureUsage.BGP_NEIGHBOR_SELF_REFERENCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.BRIDGE_DOMAINS_ROUTING_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.BRIDGE_DOMAIN_SELF_REF;
import static org.batfish.representation.juniper.JuniperStructureUsage.DHCP_RELAY_GROUP_ACTIVE_SERVER_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_DESTINATION_PREFIX_LIST;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_DSCP;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_PREFIX_LIST;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_SOURCE_PREFIX_LIST;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_TERM_DEFINITION;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_TERM_FROM_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_TERM_FROM_INTERFACE_SET;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_FILTER_THEN_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.FIREWALL_INTERFACE_SET_MEMBER;
import static org.batfish.representation.juniper.JuniperStructureUsage.FORWARDING_OPTIONS_DHCP_RELAY_GROUP_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.FORWARDING_TABLE_EXPORT_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.GENERATED_ROUTE_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.IKE_GATEWAY_EXTERNAL_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.IKE_GATEWAY_IKE_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.IKE_POLICY_IKE_PROPOSAL;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_FILTER;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_INCOMING_FILTER;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_INCOMING_FILTER_LIST;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_OUTGOING_FILTER;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_OUTGOING_FILTER_LIST;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_ROUTING_OPTIONS;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_SELF_REFERENCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_VLAN;
import static org.batfish.representation.juniper.JuniperStructureUsage.IPSEC_POLICY_IPSEC_PROPOSAL;
import static org.batfish.representation.juniper.JuniperStructureUsage.IPSEC_VPN_BIND_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.IPSEC_VPN_IKE_GATEWAY;
import static org.batfish.representation.juniper.JuniperStructureUsage.IPSEC_VPN_IPSEC_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.ISIS_EXPORT_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.ISIS_IMPORT_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.ISIS_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_INTERFACE_ADMIN_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_INTERFACE_SRLG;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_ADMIN_GROUP_EXCLUDE;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_ADMIN_GROUP_INCLUDE_ALL;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_ADMIN_GROUP_INCLUDE_ANY;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_PRIMARY_PATH;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_SECONDARY_ADMIN_GROUP_EXCLUDE;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ALL;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ANY;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_SECONDARY_PATH;
import static org.batfish.representation.juniper.JuniperStructureUsage.NAT_DESTINATION_RULE_SET_RULE_THEN;
import static org.batfish.representation.juniper.JuniperStructureUsage.NAT_RULE_SET_FROM_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.NAT_RULE_SET_FROM_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.NAT_RULE_SET_TO_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.NAT_RULE_SET_TO_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.NAT_SOURCE_RULE_SET_RULE_THEN;
import static org.batfish.representation.juniper.JuniperStructureUsage.NAT_STATIC_RULE_SET_RULE_THEN;
import static org.batfish.representation.juniper.JuniperStructureUsage.NTP_SERVER_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.NTP_SOURCE_ADDRESS_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.OSPF_AREA_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.OSPF_EXPORT_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_FROM_AS_PATH;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_FROM_AS_PATH_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_FROM_COMMUNITY;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_FROM_CONDITION;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_FROM_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_FROM_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_PREFIX_LIST;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_PREFIX_LIST_FILTER;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_RTF_PREFIX_LIST;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_TERM_DEFINITION;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_THEN_ADD_COMMUNITY;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_THEN_DELETE_COMMUNITY;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_THEN_SET_COMMUNITY;
import static org.batfish.representation.juniper.JuniperStructureUsage.POLICY_STATEMENT_THEN_TUNNEL_ATTRIBUTE;
import static org.batfish.representation.juniper.JuniperStructureUsage.RESOLUTION_RIB_IMPORT_POLICY;
import static org.batfish.representation.juniper.JuniperStructureUsage.ROUTING_INSTANCE_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.ROUTING_INSTANCE_SELF_REFERENCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.ROUTING_INSTANCE_VRF_EXPORT;
import static org.batfish.representation.juniper.JuniperStructureUsage.ROUTING_INSTANCE_VRF_IMPORT;
import static org.batfish.representation.juniper.JuniperStructureUsage.ROUTING_OPTIONS_INSTANCE_IMPORT;
import static org.batfish.representation.juniper.JuniperStructureUsage.SECURITY_POLICY_DEFINITION;
import static org.batfish.representation.juniper.JuniperStructureUsage.SECURITY_POLICY_MATCH_APPLICATION;
import static org.batfish.representation.juniper.JuniperStructureUsage.SECURITY_POLICY_TERM_DEFINITION;
import static org.batfish.representation.juniper.JuniperStructureUsage.SECURITY_PROFILE_LOGICAL_SYSTEM;
import static org.batfish.representation.juniper.JuniperStructureUsage.SECURITY_ZONES_SECURITY_ZONES_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.SNMP_COMMUNITY_CLIENT_LIST_NAME;
import static org.batfish.representation.juniper.JuniperStructureUsage.SNMP_COMMUNITY_LOGICAL_SYSTEM;
import static org.batfish.representation.juniper.JuniperStructureUsage.SNMP_COMMUNITY_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.STATIC_ROUTE_NEXT_HOP_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.SYSLOG_HOST_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.TACPLUS_SERVER_ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.VLAN_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.VLAN_L3_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.VTEP_SOURCE_INTERFACE;
import static org.batfish.representation.juniper.Nat.Type.SOURCE;
import static org.batfish.representation.juniper.RoutingInformationBase.RIB_IPV4_UNICAST;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.util.JuniperUtils;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.AuthenticationMethod;
import org.batfish.datamodel.BgpAuthenticationAlgorithm;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecProtocol;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.Line;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SnmpCommunity;
import org.batfish.datamodel.SnmpHost;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.isis.IsisAuthenticationAlgorithm;
import org.batfish.datamodel.isis.IsisHelloAuthenticationType;
import org.batfish.datamodel.isis.IsisOption;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfAreaSummary.SummaryRouteBehavior;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.StubType;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.vendor_family.juniper.TacplusServer;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.A_applicationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.A_application_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Aa_termContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Aas_applicationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Aas_application_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Aat_destination_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Aat_icmp_codeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Aat_icmp_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Aat_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Aat_source_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Address_specifierContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Address_specifier_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.As_path_expand_countContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.As_path_exprContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.As_unitContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_advertise_externalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_advertise_inactiveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_advertise_peer_asContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_allowContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_authentication_algorithmContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_authentication_keyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_authentication_key_chainContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_clusterContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_descriptionContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_disableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_enableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_enforce_first_asContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_exportContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_importContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_keepContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_local_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_multihopContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_multipathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_neighborContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_preferenceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_remove_privateContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.B_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.BandwidthContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bd_routing_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bd_vlan_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bf_route_targetContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bfiu_add_pathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bfiu_loopsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bfiu_rib_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bfiua_receiveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bfiua_sendContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bfiuas_multipathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bfiuas_path_countContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bfiuas_path_selection_modeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bfiuas_prefix_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bgp_asnContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bl_aliasContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bl_loopsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bl_no_prepend_global_asContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bl_numberContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bl_privateContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bpa_asContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bps_always_compare_medContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bps_external_router_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Bridge_domain_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.DecContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.DescriptionContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Dh_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.DirectionContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.E_encapsulationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.E_extended_vni_listContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.E_multicast_modeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.E_vni_optionsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Encryption_algorithmContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Eo8023ad_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Eo_redundant_parentContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Evo_vrf_targetContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.F_familyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.F_filterContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ff_termContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_destination_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_destination_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_destination_port_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_destination_prefix_listContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_dscpContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_first_fragmentContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_fragment_offsetContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_fragment_offset_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_icmp_codeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_icmp_code_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_icmp_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_icmp_type_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_ip_optionsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_ip_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_is_fragmentContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_packet_lengthContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_packet_length_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_port_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_prefix_listContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_source_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_source_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_source_port_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_source_prefix_listContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_tcp_establishedContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_tcp_flagsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftf_tcp_initialContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftfa_address_mask_prefixContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftt_acceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftt_decapsulateContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftt_discardContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftt_next_ipContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftt_next_termContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftt_nopContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftt_rejectContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fftt_routing_instanceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.FilterContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Filter_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fo_dhcp_relayContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fo_vxlan_routingContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fod_active_server_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fod_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fod_server_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fodg_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fods_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fragment_offsetContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Fragment_offset_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Hello_authentication_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Hib_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Hib_system_serviceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_descriptionContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_disableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_enableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_flexible_vlan_taggingContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_input_vlan_mapContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_mtuContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_native_vlan_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_output_vlan_mapContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_unitContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_vlan_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.I_vlan_taggingContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Icmp_codeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Icmp_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.If_ethernet_switchingContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.If_primaryContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ife_filterContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ife_interface_modeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ife_native_vlan_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ife_port_modeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ife_vlanContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifi6_destination_udp_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifi6f_inputContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifi_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifi_destination_udp_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifi_filterContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifi_tcp_mssContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifia_arpContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifia_preferredContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifia_primaryContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifia_vrrp_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifiav_no_preemptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifiav_preemptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifiav_priorityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifiav_virtual_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifiso_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifiso_destination_udp_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ifm_destination_udp_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ike_authentication_algorithmContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ike_authentication_methodContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Inet6_rib_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Inet_rib_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Int_interface_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Int_namedContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Interface_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Intir_memberContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Intir_member_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ip_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ip_address_and_maskContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ip_optionContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ip_prefixContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ip_prefix_default_32Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ip_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ipsec_authentication_algorithmContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ipsec_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ipv6_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ipv6_prefixContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ipv6_prefix_default_128Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Is_exportContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Is_ignore_attached_bitContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Is_importContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Is_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Is_levelContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Is_no_ipv4_routingContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Is_overloadContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Is_reference_bandwidthContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isi_disableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isi_levelContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isi_passiveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isi_point_to_pointContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isib_minimum_intervalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isib_multiplierContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isil_disableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isil_hello_authentication_keyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isil_hello_authentication_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isil_hello_intervalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isil_hold_timeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isil_metricContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isil_passiveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isil_priorityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isil_te_metricContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isl_disableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Isl_wide_metrics_onlyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Iso_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Iso_advertise_high_metricsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Iso_rib_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ist_credibility_protocol_preferenceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ist_family_shortcutsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Junos_applicationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Junos_application_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Junos_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Line_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mpls_admin_groupsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mpls_pathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mpls_rib_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplsi_admin_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplsi_srlgContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplslsp_primaryContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplslsp_secondaryContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplslspag_excludeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplslspag_include_allContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplslspag_include_anyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplslspsag_excludeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplslspsag_include_allContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Mplslspsag_include_anyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Name_or_ipContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Named_icmp_codeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Named_icmp_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Named_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Nat_poolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Nat_pool_default_port_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Nat_rule_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Natp_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Natp_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Natp_routing_instanceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.O_areaContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.O_disableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.O_enableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.O_exportContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.O_prefix_export_limitContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.O_reference_bandwidthContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oa_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oa_nssaContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oa_stubContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oai_dead_intervalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oai_disableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oai_enableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oai_hello_intervalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oai_interface_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oai_neighborContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oai_passiveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oan_default_lsaContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oan_no_summariesContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oand_metric_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oand_type_7Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oas_default_metricContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Oas_no_summariesContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ospf_interface_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.P_bgpContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.P_evpnContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Po_as_pathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Po_as_path_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Po_communityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Po_conditionContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Po_policy_statementContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Po_prefix_listContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Po_rtf_prefix_listContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Poapg_as_pathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Poc_invert_matchContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Poc_membersContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Poc_members_memberContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Pocond_if_route_existsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Pocondiaf_cccContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Pocondiafi_prefix6Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Pocondiafi_prefixContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Pocondiafi_tableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Policy_expressionContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Poplt_network6Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Poplt_networkContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Pops_commonContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Pops_termContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_areaContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_as_pathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_as_path_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_colorContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_communityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_community_countContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_externalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_familyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_instanceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_levelContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_local_preferenceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_metric2Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_metricContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_neighborContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_next_hopContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_originContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_prefix_listContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_prefix_list_filterContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_ribContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_route_filterContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_route_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_rtf_prefix_listContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_source_address_filterContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_tag2Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsf_tagContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsfrf_address_maskContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsfrf_exactContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsfrf_longerContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsfrf_orlongerContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsfrf_prefix_length_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsfrf_thenContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsfrf_throughContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsfrf_uptoContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_acceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_as_path_expandContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_as_path_prependContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_bgp_output_queue_priorityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_color2Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_colorContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_community_addContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_community_deleteContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_community_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_default_action_acceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_default_action_rejectContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_externalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_local_preferenceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_metricContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_metric_addContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_multipath_resolveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_next_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_next_termContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_originContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_preferenceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_rejectContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_tag2Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popst_tagContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popstnh_discardContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popstnh_ipv4Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popstnh_ipv6Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popstnh_peer_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popstnh_rejectContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popstnh_selfContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsto_levelContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Popsto_ribContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Port_numberContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Port_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Proposal_set_typeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.RangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ri_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ri_named_routing_instanceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ri_vrf_exportContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ri_vrf_importContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ri_vtep_source_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rib_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ro_autonomous_systemContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ro_confederationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ro_instance_importContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ro_maximum_prefixesContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ro_resolutionContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ro_rib_groupsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ro_router_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ro_srlgContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ro_staticContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roa_activeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roa_communityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roa_defaultsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roa_discardContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roa_passiveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roa_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roa_preferenceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roa_routeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roa_tagContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roaa_pathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roas_loopsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rof_exportContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rog_activeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rog_communityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rog_defaultsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rog_discardContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rog_metricContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rog_passiveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rog_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rog_routeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rog_tagContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roi_rib_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roifie_lanContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roifie_point_to_pointContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ror_export_ribContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ror_import_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ror_import_ribContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ror_inet6Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ror_inetContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ror_isoContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ror_mplsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rores_ribContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roresr_importContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ros_route4Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Ros_route6Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roslrg_srlg_costContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Roslrg_srlg_valueContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_activeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_as_pathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_communityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_discardContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_installContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_metricContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_next_hopContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_next_tableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_no_installContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_no_readvertiseContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_no_retainContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_passiveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_preferenceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_qualified_next_hop6Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_qualified_next_hopContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_readvertiseContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_rejectContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_resolveContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_retainContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_tag2Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosr_tagContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosrqnhc_metricContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosrqnhc_preferenceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rosrqnhc_tagContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Route_distinguisherContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rs_packet_locationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rs_ruleContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrm_destination_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrm_destination_address_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrm_destination_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrm_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrm_source_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrm_source_address_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrm_source_portContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrt_nat_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrt_nat_offContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrt_nat_poolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrtstp_prefixContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Rsrtstp_prefix_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_bridge_domainsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_firewallContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_logical_systemsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_routing_optionsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_snmpContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_switch_optionsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_vlans_namedContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sc_literalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sc_namedContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Scos_code_point_aliasesContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Se_address_bookContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Se_authentication_key_chainContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Se_zonesContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sea_keyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sea_toleranceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sead_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sead_address_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sead_attachContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seada_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seada_address_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seak_algorithmContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seak_optionsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seak_secretContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seak_start_timeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Secret_stringContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seik_gatewayContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seik_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seik_proposalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikg_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikg_external_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikg_ike_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikg_local_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikp_pre_shared_keyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikp_proposal_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikp_proposalsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikpr_authentication_algorithmContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikpr_authentication_methodContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikpr_dh_groupContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikpr_encryption_algorithmContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seikpr_lifetime_secondsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seip_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seip_proposalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seip_vpnContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seipp_perfect_forward_secrecyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seipp_proposal_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seipp_proposalsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seippr_authentication_algorithmContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seippr_encryption_algorithmContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seippr_lifetime_kilobytesContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seippr_lifetime_secondsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seippr_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seipv_bind_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seipvi_gatewayContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Seipvi_ipsec_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sen_destinationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sen_sourceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sen_staticContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Send_path_countContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sep_default_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sep_from_zoneContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sep_globalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctx_policyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctxpm_applicationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctxpm_destination_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctxpm_destination_address_excludedContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctxpm_source_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctxpm_source_address_excludedContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctxpm_source_identityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctxpt_denyContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctxpt_permitContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sepctxptp_application_servicesContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesoi_fragmentContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesoi_largeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesoi_ping_deathContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesop_block_fragContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesop_spoofingContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesop_unknown_protocolContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesot_fin_no_ackContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesot_landContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesot_syn_finContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesot_syn_fragContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesot_tcp_no_flagContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sesot_winnukeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sez_security_zoneContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sezs_address_bookContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sezs_host_inbound_trafficContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sezs_interfacesContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sezsa_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sezsa_address_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sezsaad_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sezsaad_address_setContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sezsh_protocolsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sezsh_system_servicesContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Snmp_client_listContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Snmp_communityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Snmpc_authorizationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Snmpc_client_list_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Snmpc_logical_systemContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Snmpcl_networkContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Snmpcls_routing_instanceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Snmptg_targetsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.So_route_distinguisherContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.So_vtep_source_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Srlg_costContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Srlg_valueContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Standard_communityContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.SubrangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_authentication_methodContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_authentication_orderContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_default_address_selectionContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_domain_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_host_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_name_serverContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_portsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_porttypeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_security_profileContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_services_linetypeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sy_tacplus_serverContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Syn_serverContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Syn_server_routing_instanceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Syn_source_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Syp_disableContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Syr_encrypted_passwordContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sys_hostContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sysh_routing_instanceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Sysp_logical_systemContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Syt_routing_instanceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Syt_secretContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Syt_source_addressContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Tacplus_server_hostContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Tcp_flagsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Tcp_flags_alternativeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Tcp_flags_atomContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Tcp_flags_literalContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Uint16Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Uint16_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Uint32Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Uint32lContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Uint8Context;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Uint8_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Vlan_numberContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Vlan_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Vlt_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Vlt_l3_interfaceContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Vlt_vlan_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Vlt_vni_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Vni_numberContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Vni_rangeContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.ZoneContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.juniper.AddressAddressBookEntry;
import org.batfish.representation.juniper.AddressBook;
import org.batfish.representation.juniper.AddressBookEntry;
import org.batfish.representation.juniper.AddressFamily;
import org.batfish.representation.juniper.AddressRangeAddressBookEntry;
import org.batfish.representation.juniper.AddressSetAddressBookEntry;
import org.batfish.representation.juniper.AddressSetEntry;
import org.batfish.representation.juniper.AdminGroup;
import org.batfish.representation.juniper.AggregateRoute;
import org.batfish.representation.juniper.AllVlans;
import org.batfish.representation.juniper.ApplicationOrApplicationSetReference;
import org.batfish.representation.juniper.ApplicationSet;
import org.batfish.representation.juniper.ApplicationSetMemberReference;
import org.batfish.representation.juniper.ApplicationSetReference;
import org.batfish.representation.juniper.AsPathGroup;
import org.batfish.representation.juniper.BaseApplication;
import org.batfish.representation.juniper.BaseApplication.Term;
import org.batfish.representation.juniper.BgpGroup;
import org.batfish.representation.juniper.BgpGroup.BgpGroupType;
import org.batfish.representation.juniper.BridgeDomain;
import org.batfish.representation.juniper.BridgeDomainVlanId;
import org.batfish.representation.juniper.BridgeDomainVlanIdAll;
import org.batfish.representation.juniper.BridgeDomainVlanIdNone;
import org.batfish.representation.juniper.BridgeDomainVlanIdNumber;
import org.batfish.representation.juniper.CommunityMember;
import org.batfish.representation.juniper.CommunityMemberParseResult;
import org.batfish.representation.juniper.ConcreteFirewallFilter;
import org.batfish.representation.juniper.Condition;
import org.batfish.representation.juniper.DhcpRelayGroup;
import org.batfish.representation.juniper.DhcpRelayServerGroup;
import org.batfish.representation.juniper.DscpUtil;
import org.batfish.representation.juniper.Evpn;
import org.batfish.representation.juniper.EvpnEncapsulation;
import org.batfish.representation.juniper.Family;
import org.batfish.representation.juniper.FirewallFilter;
import org.batfish.representation.juniper.FwFrom;
import org.batfish.representation.juniper.FwFromAddress;
import org.batfish.representation.juniper.FwFromApplicationOrApplicationSet;
import org.batfish.representation.juniper.FwFromDestinationAddress;
import org.batfish.representation.juniper.FwFromDestinationAddressBookEntry;
import org.batfish.representation.juniper.FwFromDestinationAddressExcept;
import org.batfish.representation.juniper.FwFromDestinationPort;
import org.batfish.representation.juniper.FwFromDestinationPrefixList;
import org.batfish.representation.juniper.FwFromDestinationPrefixListExcept;
import org.batfish.representation.juniper.FwFromDscp;
import org.batfish.representation.juniper.FwFromFragmentOffset;
import org.batfish.representation.juniper.FwFromIcmpCode;
import org.batfish.representation.juniper.FwFromIcmpCodeExcept;
import org.batfish.representation.juniper.FwFromIcmpType;
import org.batfish.representation.juniper.FwFromIcmpTypeExcept;
import org.batfish.representation.juniper.FwFromInterface;
import org.batfish.representation.juniper.FwFromInterfaceSet;
import org.batfish.representation.juniper.FwFromIpOptions;
import org.batfish.representation.juniper.FwFromJunosApplication;
import org.batfish.representation.juniper.FwFromJunosApplicationSet;
import org.batfish.representation.juniper.FwFromPacketLength;
import org.batfish.representation.juniper.FwFromPort;
import org.batfish.representation.juniper.FwFromPrefixList;
import org.batfish.representation.juniper.FwFromProtocol;
import org.batfish.representation.juniper.FwFromSourceAddress;
import org.batfish.representation.juniper.FwFromSourceAddressBookEntry;
import org.batfish.representation.juniper.FwFromSourceAddressExcept;
import org.batfish.representation.juniper.FwFromSourcePort;
import org.batfish.representation.juniper.FwFromSourcePrefixList;
import org.batfish.representation.juniper.FwFromSourcePrefixListExcept;
import org.batfish.representation.juniper.FwFromTcpFlags;
import org.batfish.representation.juniper.FwTerm;
import org.batfish.representation.juniper.FwThenAccept;
import org.batfish.representation.juniper.FwThenDiscard;
import org.batfish.representation.juniper.FwThenNextIp;
import org.batfish.representation.juniper.FwThenNextTerm;
import org.batfish.representation.juniper.FwThenNop;
import org.batfish.representation.juniper.FwThenRoutingInstance;
import org.batfish.representation.juniper.GeneratedRoute;
import org.batfish.representation.juniper.HostProtocol;
import org.batfish.representation.juniper.HostSystemService;
import org.batfish.representation.juniper.IcmpLarge;
import org.batfish.representation.juniper.IkeGateway;
import org.batfish.representation.juniper.IkePolicy;
import org.batfish.representation.juniper.IkeProposal;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.Interface.VlanTaggingMode;
import org.batfish.representation.juniper.InterfaceOspfNeighbor;
import org.batfish.representation.juniper.InterfaceRange;
import org.batfish.representation.juniper.InterfaceRangeMember;
import org.batfish.representation.juniper.InterfaceRangeMemberRange;
import org.batfish.representation.juniper.InterfaceSet;
import org.batfish.representation.juniper.IpBgpGroup;
import org.batfish.representation.juniper.IpOptions;
import org.batfish.representation.juniper.IpUnknownProtocol;
import org.batfish.representation.juniper.IpsecPolicy;
import org.batfish.representation.juniper.IpsecProposal;
import org.batfish.representation.juniper.IpsecVpn;
import org.batfish.representation.juniper.IsisInterfaceLevelSettings;
import org.batfish.representation.juniper.IsisInterfaceSettings;
import org.batfish.representation.juniper.IsisLevelSettings;
import org.batfish.representation.juniper.IsisSettings;
import org.batfish.representation.juniper.JuniperAuthenticationKey;
import org.batfish.representation.juniper.JuniperAuthenticationKeyChain;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.JuniperStructureType;
import org.batfish.representation.juniper.JuniperStructureUsage;
import org.batfish.representation.juniper.JunosApplication;
import org.batfish.representation.juniper.JunosApplicationReference;
import org.batfish.representation.juniper.JunosApplicationSet;
import org.batfish.representation.juniper.JunosApplicationSetReference;
import org.batfish.representation.juniper.LiteralCommunityMember;
import org.batfish.representation.juniper.LogicalSystem;
import org.batfish.representation.juniper.MulticastModeOptions;
import org.batfish.representation.juniper.NamedAsPath;
import org.batfish.representation.juniper.NamedBgpGroup;
import org.batfish.representation.juniper.NamedCommunity;
import org.batfish.representation.juniper.Nat;
import org.batfish.representation.juniper.NatPacketLocation;
import org.batfish.representation.juniper.NatPool;
import org.batfish.representation.juniper.NatRule;
import org.batfish.representation.juniper.NatRuleMatchDstAddr;
import org.batfish.representation.juniper.NatRuleMatchDstAddrName;
import org.batfish.representation.juniper.NatRuleMatchDstPort;
import org.batfish.representation.juniper.NatRuleMatchProtocol;
import org.batfish.representation.juniper.NatRuleMatchSrcAddr;
import org.batfish.representation.juniper.NatRuleMatchSrcAddrName;
import org.batfish.representation.juniper.NatRuleMatchSrcPort;
import org.batfish.representation.juniper.NatRuleSet;
import org.batfish.representation.juniper.NatRuleThenInterface;
import org.batfish.representation.juniper.NatRuleThenOff;
import org.batfish.representation.juniper.NatRuleThenPool;
import org.batfish.representation.juniper.NatRuleThenPrefix;
import org.batfish.representation.juniper.NatRuleThenPrefixName;
import org.batfish.representation.juniper.NextHop;
import org.batfish.representation.juniper.NoPortTranslation;
import org.batfish.representation.juniper.NodeDevice;
import org.batfish.representation.juniper.NssaSettings;
import org.batfish.representation.juniper.OspfArea;
import org.batfish.representation.juniper.OspfInterfaceSettings;
import org.batfish.representation.juniper.OspfInterfaceSettings.OspfInterfaceType;
import org.batfish.representation.juniper.PatPool;
import org.batfish.representation.juniper.PathSelectionMode;
import org.batfish.representation.juniper.PolicyStatement;
import org.batfish.representation.juniper.PrefixList;
import org.batfish.representation.juniper.PsFromAsPath;
import org.batfish.representation.juniper.PsFromAsPathGroup;
import org.batfish.representation.juniper.PsFromColor;
import org.batfish.representation.juniper.PsFromCommunity;
import org.batfish.representation.juniper.PsFromCommunityCount;
import org.batfish.representation.juniper.PsFromCommunityCount.Mode;
import org.batfish.representation.juniper.PsFromCondition;
import org.batfish.representation.juniper.PsFromExternal;
import org.batfish.representation.juniper.PsFromFamily;
import org.batfish.representation.juniper.PsFromInstance;
import org.batfish.representation.juniper.PsFromInterface;
import org.batfish.representation.juniper.PsFromLocalPreference;
import org.batfish.representation.juniper.PsFromMetric;
import org.batfish.representation.juniper.PsFromNeighbor;
import org.batfish.representation.juniper.PsFromNextHop;
import org.batfish.representation.juniper.PsFromNextHop.Hop;
import org.batfish.representation.juniper.PsFromPolicyStatement;
import org.batfish.representation.juniper.PsFromPolicyStatementConjunction;
import org.batfish.representation.juniper.PsFromPrefixList;
import org.batfish.representation.juniper.PsFromPrefixListFilterLonger;
import org.batfish.representation.juniper.PsFromPrefixListFilterOrLonger;
import org.batfish.representation.juniper.PsFromProtocol;
import org.batfish.representation.juniper.PsFromRouteFilter;
import org.batfish.representation.juniper.PsFromRouteType;
import org.batfish.representation.juniper.PsFromTag;
import org.batfish.representation.juniper.PsFromUnsupported;
import org.batfish.representation.juniper.PsFroms;
import org.batfish.representation.juniper.PsTerm;
import org.batfish.representation.juniper.PsThen;
import org.batfish.representation.juniper.PsThenAccept;
import org.batfish.representation.juniper.PsThenAsPathExpand;
import org.batfish.representation.juniper.PsThenAsPathExpandAsList;
import org.batfish.representation.juniper.PsThenAsPathExpandLastAs;
import org.batfish.representation.juniper.PsThenAsPathPrepend;
import org.batfish.representation.juniper.PsThenCommunityAdd;
import org.batfish.representation.juniper.PsThenCommunityDelete;
import org.batfish.representation.juniper.PsThenCommunitySet;
import org.batfish.representation.juniper.PsThenDefaultActionAccept;
import org.batfish.representation.juniper.PsThenDefaultActionReject;
import org.batfish.representation.juniper.PsThenExternal;
import org.batfish.representation.juniper.PsThenLocalPreference;
import org.batfish.representation.juniper.PsThenMetric;
import org.batfish.representation.juniper.PsThenMetricAdd;
import org.batfish.representation.juniper.PsThenNextHopDiscard;
import org.batfish.representation.juniper.PsThenNextHopIp;
import org.batfish.representation.juniper.PsThenNextHopPeerAddress;
import org.batfish.representation.juniper.PsThenNextHopReject;
import org.batfish.representation.juniper.PsThenNextHopSelf;
import org.batfish.representation.juniper.PsThenNextPolicy;
import org.batfish.representation.juniper.PsThenOrigin;
import org.batfish.representation.juniper.PsThenPreference;
import org.batfish.representation.juniper.PsThenReject;
import org.batfish.representation.juniper.PsThenTag;
import org.batfish.representation.juniper.PsThenTunnelAttributeRemove;
import org.batfish.representation.juniper.PsThenTunnelAttributeSet;
import org.batfish.representation.juniper.PsThens;
import org.batfish.representation.juniper.QualifiedNextHop;
import org.batfish.representation.juniper.Resolution;
import org.batfish.representation.juniper.ResolutionRib;
import org.batfish.representation.juniper.RibGroup;
import org.batfish.representation.juniper.Route4FilterLine;
import org.batfish.representation.juniper.Route4FilterLineAddressMask;
import org.batfish.representation.juniper.Route4FilterLineExact;
import org.batfish.representation.juniper.Route4FilterLineLengthRange;
import org.batfish.representation.juniper.Route4FilterLineLonger;
import org.batfish.representation.juniper.Route4FilterLineOrLonger;
import org.batfish.representation.juniper.Route4FilterLineThrough;
import org.batfish.representation.juniper.Route4FilterLineUpTo;
import org.batfish.representation.juniper.Route6FilterLine;
import org.batfish.representation.juniper.Route6FilterLineAddressMask;
import org.batfish.representation.juniper.Route6FilterLineExact;
import org.batfish.representation.juniper.Route6FilterLineLengthRange;
import org.batfish.representation.juniper.Route6FilterLineLonger;
import org.batfish.representation.juniper.Route6FilterLineOrLonger;
import org.batfish.representation.juniper.Route6FilterLineThrough;
import org.batfish.representation.juniper.Route6FilterLineUpTo;
import org.batfish.representation.juniper.RouteFilter;
import org.batfish.representation.juniper.RoutingInformationBase;
import org.batfish.representation.juniper.RoutingInstance;
import org.batfish.representation.juniper.Screen;
import org.batfish.representation.juniper.ScreenAction;
import org.batfish.representation.juniper.Srlg;
import org.batfish.representation.juniper.StaticRoute;
import org.batfish.representation.juniper.StaticRouteV4;
import org.batfish.representation.juniper.StaticRouteV6;
import org.batfish.representation.juniper.StubSettings;
import org.batfish.representation.juniper.TcpFinNoAck;
import org.batfish.representation.juniper.TcpNoFlag;
import org.batfish.representation.juniper.TcpSynFin;
import org.batfish.representation.juniper.TunnelAttribute;
import org.batfish.representation.juniper.Vlan;
import org.batfish.representation.juniper.VlanRange;
import org.batfish.representation.juniper.VlanReference;
import org.batfish.representation.juniper.VrrpGroup;
import org.batfish.representation.juniper.Zone;

public class ConfigurationBuilder extends FlatJuniperParserBaseListener
    implements SilentSyntaxListener {

  private static final boolean DEFAULT_VRRP_PREEMPT = true;

  private static final int DEFAULT_VRRP_PRIORITY = 100;

  private static final AggregateRoute DUMMY_AGGREGATE_ROUTE = new AggregateRoute(Prefix.ZERO);

  private static final BgpGroup DUMMY_BGP_GROUP = new BgpGroup();

  private static final IntegerSpace OSPF_HELLO_INTERVAL_RANGE =
      IntegerSpace.of(new SubRange(1, 255));
  private static final IntegerSpace OSPF_DEAD_INTERVAL_RANGE =
      IntegerSpace.of(new SubRange(1, 65535));
  private static final IntegerSpace SRLG_COST_RANGE = IntegerSpace.of(new SubRange(1, 65535));
  private static final LongSpace SRLG_VALUE_RANGE = LongSpace.of(Range.closed(1L, 4294967295L));
  private static final IntegerSpace VNI_NUMBER_RANGE = IntegerSpace.of(new SubRange(0, 16777215));

  private static final IntegerSpace VLAN_RANGE = IntegerSpace.of(new SubRange(1, 4094));

  private static final IntegerSpace ADMIN_GROUP_RANGE = IntegerSpace.of(new SubRange(0, 31));

  private static final IntegerSpace FRAGMENT_OFFSET_RANGE = IntegerSpace.of(new SubRange(0, 8191));

  private static final IntegerSpace ADD_PATH_SEND_PATH_COUNT_RANGE =
      IntegerSpace.of(new SubRange(2, 64));

  private static final IntegerSpace OSPF_EXTERNAL_TYPE_RANGE = IntegerSpace.of(new SubRange(1, 2));

  private <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, U defaultReturnValue) {
    _w.redFlagf("Could not convert to %s: %s", returnType.getSimpleName(), getFullText(ctx));
    return defaultReturnValue;
  }

  /** Return original line number for the specified token */
  private int getLine(Token t) {
    return _parser.getLine(t);
  }

  public static @Nonnull NamedPort getNamedPort(Named_portContext ctx) {
    if (ctx.AFS() != null) {
      return NamedPort.AFS;
    } else if (ctx.BGP() != null) {
      return NamedPort.BGP;
    } else if (ctx.BIFF() != null) {
      return NamedPort.BIFFudp_OR_EXECtcp;
    } else if (ctx.BOOTPC() != null) {
      return NamedPort.BOOTPC;
    } else if (ctx.BOOTPS() != null) {
      return NamedPort.BOOTPS_OR_DHCP;
    } else if (ctx.CMD() != null) {
      return NamedPort.CMDtcp_OR_SYSLOGudp;
    } else if (ctx.CVSPSERVER() != null) {
      return NamedPort.CVSPSERVER;
    } else if (ctx.DHCP() != null) {
      return NamedPort.BOOTPS_OR_DHCP;
    } else if (ctx.DOMAIN() != null) {
      return NamedPort.DOMAIN;
    } else if (ctx.EKLOGIN() != null) {
      return NamedPort.EKLOGIN;
    } else if (ctx.EKSHELL() != null) {
      return NamedPort.EKSHELL;
    } else if (ctx.EXEC() != null) {
      return NamedPort.BIFFudp_OR_EXECtcp;
    } else if (ctx.FINGER() != null) {
      return NamedPort.FINGER;
    } else if (ctx.FTP() != null) {
      return NamedPort.FTP;
    } else if (ctx.FTP_DATA() != null) {
      return NamedPort.FTP_DATA;
    } else if (ctx.HTTP() != null) {
      return NamedPort.HTTP;
    } else if (ctx.HTTPS() != null) {
      return NamedPort.HTTPS;
    } else if (ctx.IDENT() != null) {
      return NamedPort.IDENT;
    } else if (ctx.IMAP() != null) {
      return NamedPort.IMAP;
    } else if (ctx.KERBEROS_SEC() != null) {
      return NamedPort.KERBEROS_SEC;
    } else if (ctx.KLOGIN() != null) {
      return NamedPort.KLOGIN;
    } else if (ctx.KPASSWD() != null) {
      return NamedPort.KPASSWDV4;
    } else if (ctx.KRB_PROP() != null) {
      return NamedPort.KRB_PROP;
    } else if (ctx.KRBUPDATE() != null) {
      return NamedPort.KRBUPDATE;
    } else if (ctx.KSHELL() != null) {
      return NamedPort.KSHELL;
    } else if (ctx.LDAP() != null) {
      return NamedPort.LDAP;
    } else if (ctx.LDP() != null) {
      return NamedPort.LDP;
    } else if (ctx.LOGIN() != null) {
      return NamedPort.LOGINtcp_OR_WHOudp;
    } else if (ctx.MOBILEIP_AGENT() != null) {
      return NamedPort.MOBILE_IP_AGENT;
    } else if (ctx.MOBILIP_MN() != null) {
      return NamedPort.MOBILE_IP_MN;
    } else if (ctx.MSDP() != null) {
      return NamedPort.MSDP;
    } else if (ctx.NETBIOS_DGM() != null) {
      return NamedPort.NETBIOS_DGM;
    } else if (ctx.NETBIOS_NS() != null) {
      return NamedPort.NETBIOS_NS;
    } else if (ctx.NETBIOS_SSN() != null) {
      return NamedPort.NETBIOS_SSN;
    } else if (ctx.NFSD() != null) {
      return NamedPort.NFSD;
    } else if (ctx.NNTP() != null) {
      return NamedPort.NNTP;
    } else if (ctx.NTALK() != null) {
      return NamedPort.NTALK;
    } else if (ctx.NTP() != null) {
      return NamedPort.NTP;
    } else if (ctx.POP3() != null) {
      return NamedPort.POP3;
    } else if (ctx.PPTP() != null) {
      return NamedPort.PPTP;
    } else if (ctx.PRINTER() != null) {
      return NamedPort.LPD;
    } else if (ctx.RADACCT() != null) {
      return NamedPort.RADIUS_2_AUTH;
    } else if (ctx.RADIUS() != null) {
      return NamedPort.RADIUS_2_AUTH;
    } else if (ctx.RIP() != null) {
      return NamedPort.EFStcp_OR_RIPudp;
    } else if (ctx.RKINIT() != null) {
      return NamedPort.RKINIT;
    } else if (ctx.SMTP() != null) {
      return NamedPort.SMTP;
    } else if (ctx.SNMP() != null) {
      return NamedPort.SNMP;
    } else if (ctx.SNMPTRAP() != null) {
      return NamedPort.SNMPTRAP;
    } else if (ctx.SNPP() != null) {
      return NamedPort.SNPP;
    } else if (ctx.SOCKS() != null) {
      return NamedPort.SOCKS;
    } else if (ctx.SSH() != null) {
      return NamedPort.SSH;
    } else if (ctx.SUNRPC() != null) {
      return NamedPort.SUNRPC;
    } else if (ctx.SYSLOG() != null) {
      return NamedPort.CMDtcp_OR_SYSLOGudp;
    } else if (ctx.TACACS() != null) {
      return NamedPort.TACACS;
    } else if (ctx.TACACS_DS() != null) {
      return NamedPort.TACACS_DS;
    } else if (ctx.TALK() != null) {
      return NamedPort.TALK;
    } else if (ctx.TELNET() != null) {
      return NamedPort.TELNET;
    } else if (ctx.TFTP() != null) {
      return NamedPort.TFTP;
    } else if (ctx.TIMED() != null) {
      return NamedPort.TIMED;
    } else if (ctx.WHO() != null) {
      return NamedPort.LOGINtcp_OR_WHOudp;
    } else if (ctx.XDMCP() != null) {
      return NamedPort.XDMCP;
    } else {
      throw new BatfishException("missing port-number mapping for port: \"" + ctx.getText() + "\"");
    }
  }

  private static long toBandwidth(BandwidthContext ctx) {
    long base = toLong(ctx.base);
    if (ctx.C() != null) {
      // https://www.juniper.net/documentation/en_US/junos/topics/task/configuration/interfaces-configuring-the-interface-bandwidth.html
      return base * 384L;
    } else if (ctx.K() != null) {
      return base * 1000L;
    } else if (ctx.M() != null) {
      return base * 1000000L;
    } else if (ctx.G() != null) {
      return base * 1000000000L;
    } else {
      return base;
    }
  }

  private IsisHelloAuthenticationType toIsisHelloAuthenticationType(
      Hello_authentication_typeContext ctx) {
    if (ctx.MD5() != null) {
      return IsisHelloAuthenticationType.MD5;
    } else {
      assert ctx.SIMPLE() != null;
      return IsisHelloAuthenticationType.SIMPLE;
    }
  }

  private JunosApplication toJunosApplication(Junos_applicationContext ctx) {
    if (ctx.ANY() != null) {
      return JunosApplication.ANY;
    } else if (ctx.JUNOS_AOL() != null) {
      return JunosApplication.JUNOS_AOL;
    } else if (ctx.JUNOS_BGP() != null) {
      return JunosApplication.JUNOS_BGP;
    } else if (ctx.JUNOS_BIFF() != null) {
      return JunosApplication.JUNOS_BIFF;
    } else if (ctx.JUNOS_BOOTPC() != null) {
      return JunosApplication.JUNOS_BOOTPC;
    } else if (ctx.JUNOS_BOOTPS() != null) {
      return JunosApplication.JUNOS_BOOTPS;
    } else if (ctx.JUNOS_CHARGEN() != null) {
      return JunosApplication.JUNOS_CHARGEN;
    } else if (ctx.JUNOS_CVSPSERVER() != null) {
      return JunosApplication.JUNOS_CVSPSERVER;
    } else if (ctx.JUNOS_DHCP_CLIENT() != null) {
      return JunosApplication.JUNOS_DHCP_CLIENT;
    } else if (ctx.JUNOS_DHCP_RELAY() != null) {
      return JunosApplication.JUNOS_DHCP_RELAY;
    } else if (ctx.JUNOS_DHCP_SERVER() != null) {
      return JunosApplication.JUNOS_DHCP_SERVER;
    } else if (ctx.JUNOS_DISCARD() != null) {
      return JunosApplication.JUNOS_DISCARD;
    } else if (ctx.JUNOS_DNS_TCP() != null) {
      return JunosApplication.JUNOS_DNS_TCP;
    } else if (ctx.JUNOS_DNS_UDP() != null) {
      return JunosApplication.JUNOS_DNS_UDP;
    } else if (ctx.JUNOS_ECHO() != null) {
      return JunosApplication.JUNOS_ECHO;
    } else if (ctx.JUNOS_FINGER() != null) {
      return JunosApplication.JUNOS_FINGER;
    } else if (ctx.JUNOS_FTP() != null) {
      return JunosApplication.JUNOS_FTP;
    } else if (ctx.JUNOS_FTP_DATA() != null) {
      return JunosApplication.JUNOS_FTP_DATA;
    } else if (ctx.JUNOS_GNUTELLA() != null) {
      return JunosApplication.JUNOS_GNUTELLA;
    } else if (ctx.JUNOS_GOPHER() != null) {
      return JunosApplication.JUNOS_GOPHER;
    } else if (ctx.JUNOS_GPRS_GTP_C() != null) {
      return JunosApplication.JUNOS_GPRS_GTP_C;
    } else if (ctx.JUNOS_GPRS_GTP_U() != null) {
      return JunosApplication.JUNOS_GPRS_GTP_U;
    } else if (ctx.JUNOS_GPRS_GTP_V0() != null) {
      return JunosApplication.JUNOS_GPRS_GTP_V0;
    } else if (ctx.JUNOS_GPRS_SCTP() != null) {
      return JunosApplication.JUNOS_GPRS_SCTP;
    } else if (ctx.JUNOS_GRE() != null) {
      return JunosApplication.JUNOS_GRE;
    } else if (ctx.JUNOS_GTP() != null) {
      return JunosApplication.JUNOS_GTP;
    } else if (ctx.JUNOS_H323() != null) {
      return JunosApplication.JUNOS_H323;
    } else if (ctx.JUNOS_HTTP() != null) {
      return JunosApplication.JUNOS_HTTP;
    } else if (ctx.JUNOS_HTTP_EXT() != null) {
      return JunosApplication.JUNOS_HTTP_EXT;
    } else if (ctx.JUNOS_HTTPS() != null) {
      return JunosApplication.JUNOS_HTTPS;
    } else if (ctx.JUNOS_ICMP_ALL() != null) {
      return JunosApplication.JUNOS_ICMP_ALL;
    } else if (ctx.JUNOS_ICMP_PING() != null) {
      return JunosApplication.JUNOS_ICMP_PING;
    } else if (ctx.JUNOS_ICMP6_ALL() != null) {
      return JunosApplication.JUNOS_ICMP6_ALL;
    } else if (ctx.JUNOS_ICMP6_DST_UNREACH_ADDR() != null) {
      return JunosApplication.JUNOS_ICMP6_DST_UNREACH_ADDR;
    } else if (ctx.JUNOS_ICMP6_DST_UNREACH_ADMIN() != null) {
      return JunosApplication.JUNOS_ICMP6_DST_UNREACH_ADMIN;
    } else if (ctx.JUNOS_ICMP6_DST_UNREACH_BEYOND() != null) {
      return JunosApplication.JUNOS_ICMP6_DST_UNREACH_BEYOND;
    } else if (ctx.JUNOS_ICMP6_DST_UNREACH_PORT() != null) {
      return JunosApplication.JUNOS_ICMP6_DST_UNREACH_PORT;
    } else if (ctx.JUNOS_ICMP6_DST_UNREACH_ROUTE() != null) {
      return JunosApplication.JUNOS_ICMP6_DST_UNREACH_ROUTE;
    } else if (ctx.JUNOS_ICMP6_ECHO_REPLY() != null) {
      return JunosApplication.JUNOS_ICMP6_ECHO_REPLY;
    } else if (ctx.JUNOS_ICMP6_ECHO_REQUEST() != null) {
      return JunosApplication.JUNOS_ICMP6_ECHO_REQUEST;
    } else if (ctx.JUNOS_ICMP6_PACKET_TOO_BIG() != null) {
      return JunosApplication.JUNOS_ICMP6_PACKET_TOO_BIG;
    } else if (ctx.JUNOS_ICMP6_PARAM_PROB_HEADER() != null) {
      return JunosApplication.JUNOS_ICMP6_PARAM_PROB_HEADER;
    } else if (ctx.JUNOS_ICMP6_PARAM_PROB_NEXTHDR() != null) {
      return JunosApplication.JUNOS_ICMP6_PARAM_PROB_NEXTHDR;
    } else if (ctx.JUNOS_ICMP6_PARAM_PROB_OPTION() != null) {
      return JunosApplication.JUNOS_ICMP6_PARAM_PROB_OPTION;
    } else if (ctx.JUNOS_ICMP6_TIME_EXCEED_REASSEMBLY() != null) {
      return JunosApplication.JUNOS_ICMP6_TIME_EXCEED_REASSEMBLY;
    } else if (ctx.JUNOS_ICMP6_TIME_EXCEED_TRANSIT() != null) {
      return JunosApplication.JUNOS_ICMP6_TIME_EXCEED_TRANSIT;
    } else if (ctx.JUNOS_IDENT() != null) {
      return JunosApplication.JUNOS_IDENT;
    } else if (ctx.JUNOS_IKE() != null) {
      return JunosApplication.JUNOS_IKE;
    } else if (ctx.JUNOS_IKE_NAT() != null) {
      return JunosApplication.JUNOS_IKE_NAT;
    } else if (ctx.JUNOS_IMAP() != null) {
      return JunosApplication.JUNOS_IMAP;
    } else if (ctx.JUNOS_IMAPS() != null) {
      return JunosApplication.JUNOS_IMAPS;
    } else if (ctx.JUNOS_INTERNET_LOCATOR_SERVICE() != null) {
      return JunosApplication.JUNOS_INTERNET_LOCATOR_SERVICE;
    } else if (ctx.JUNOS_IRC() != null) {
      return JunosApplication.JUNOS_IRC;
    } else if (ctx.JUNOS_L2TP() != null) {
      return JunosApplication.JUNOS_L2TP;
    } else if (ctx.JUNOS_LDAP() != null) {
      return JunosApplication.JUNOS_LDAP;
    } else if (ctx.JUNOS_LDP_TCP() != null) {
      return JunosApplication.JUNOS_LDP_TCP;
    } else if (ctx.JUNOS_LDP_UDP() != null) {
      return JunosApplication.JUNOS_LDP_UDP;
    } else if (ctx.JUNOS_LPR() != null) {
      return JunosApplication.JUNOS_LPR;
    } else if (ctx.JUNOS_MAIL() != null) {
      return JunosApplication.JUNOS_MAIL;
    } else if (ctx.JUNOS_MGCP_CA() != null) {
      return JunosApplication.JUNOS_MGCP_CA;
    } else if (ctx.JUNOS_MGCP_UA() != null) {
      return JunosApplication.JUNOS_MGCP_UA;
    } else if (ctx.JUNOS_MS_RPC_EPM() != null) {
      return JunosApplication.JUNOS_MS_RPC_EPM;
    } else if (ctx.JUNOS_MS_RPC_IIS_COM_1() != null) {
      return JunosApplication.JUNOS_MS_RPC_IIS_COM_1;
    } else if (ctx.JUNOS_MS_RPC_IIS_COM_ADMINBASE() != null) {
      return JunosApplication.JUNOS_MS_RPC_IIS_COM_ADMINBASE;
    } else if (ctx.JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP() != null) {
      return JunosApplication.JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP;
    } else if (ctx.JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR() != null) {
      return JunosApplication.JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR;
    } else if (ctx.JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE() != null) {
      return JunosApplication.JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE;
    } else if (ctx.JUNOS_MS_RPC_TCP() != null) {
      return JunosApplication.JUNOS_MS_RPC_TCP;
    } else if (ctx.JUNOS_MS_RPC_UDP() != null) {
      return JunosApplication.JUNOS_MS_RPC_UDP;
    } else if (ctx.JUNOS_MS_RPC_UUID_ANY_TCP() != null) {
      return JunosApplication.JUNOS_MS_RPC_UUID_ANY_TCP;
    } else if (ctx.JUNOS_MS_RPC_UUID_ANY_UDP() != null) {
      return JunosApplication.JUNOS_MS_RPC_UUID_ANY_UDP;
    } else if (ctx.JUNOS_MS_RPC_WMIC_ADMIN() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_ADMIN;
    } else if (ctx.JUNOS_MS_RPC_WMIC_ADMIN2() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_ADMIN2;
    } else if (ctx.JUNOS_MS_RPC_WMIC_MGMT() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_MGMT;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_CALLRESULT() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_CALLRESULT;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_CLASSOBJECT() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_CLASSOBJECT;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_LOGIN_CLIENTID() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_LOGIN_CLIENTID;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_LOGIN_HELPER() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_LOGIN_HELPER;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_OBJECTSINK() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_OBJECTSINK;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_REFRESHING_SERVICES() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_REFRESHING_SERVICES;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_REMOTE_REFRESHER() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_REMOTE_REFRESHER;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_SERVICES() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_SERVICES;
    } else if (ctx.JUNOS_MS_RPC_WMIC_WEBM_SHUTDOWN() != null) {
      return JunosApplication.JUNOS_MS_RPC_WMIC_WEBM_SHUTDOWN;
    } else if (ctx.JUNOS_MS_SQL() != null) {
      return JunosApplication.JUNOS_MS_SQL;
    } else if (ctx.JUNOS_MSN() != null) {
      return JunosApplication.JUNOS_MSN;
    } else if (ctx.JUNOS_NBDS() != null) {
      return JunosApplication.JUNOS_NBDS;
    } else if (ctx.JUNOS_NBNAME() != null) {
      return JunosApplication.JUNOS_NBNAME;
    } else if (ctx.JUNOS_NETBIOS_SESSION() != null) {
      return JunosApplication.JUNOS_NETBIOS_SESSION;
    } else if (ctx.JUNOS_NFS() != null) {
      return JunosApplication.JUNOS_NFS;
    } else if (ctx.JUNOS_NFSD_TCP() != null) {
      return JunosApplication.JUNOS_NFSD_TCP;
    } else if (ctx.JUNOS_NFSD_UDP() != null) {
      return JunosApplication.JUNOS_NFSD_UDP;
    } else if (ctx.JUNOS_NNTP() != null) {
      return JunosApplication.JUNOS_NNTP;
    } else if (ctx.JUNOS_NS_GLOBAL() != null) {
      return JunosApplication.JUNOS_NS_GLOBAL;
    } else if (ctx.JUNOS_NS_GLOBAL_PRO() != null) {
      return JunosApplication.JUNOS_NS_GLOBAL_PRO;
    } else if (ctx.JUNOS_NSM() != null) {
      return JunosApplication.JUNOS_NSM;
    } else if (ctx.JUNOS_NTALK() != null) {
      return JunosApplication.JUNOS_NTALK;
    } else if (ctx.JUNOS_NTP() != null) {
      return JunosApplication.JUNOS_NTP;
    } else if (ctx.JUNOS_OSPF() != null) {
      return JunosApplication.JUNOS_OSPF;
    } else if (ctx.JUNOS_PC_ANYWHERE() != null) {
      return JunosApplication.JUNOS_PC_ANYWHERE;
    } else if (ctx.JUNOS_PERSISTENT_NAT() != null) {
      return JunosApplication.JUNOS_PERSISTENT_NAT;
    } else if (ctx.JUNOS_PING() != null) {
      return JunosApplication.JUNOS_PING;
    } else if (ctx.JUNOS_PINGV6() != null) {
      return JunosApplication.JUNOS_PINGV6;
    } else if (ctx.JUNOS_POP3() != null) {
      return JunosApplication.JUNOS_POP3;
    } else if (ctx.JUNOS_PPTP() != null) {
      return JunosApplication.JUNOS_PPTP;
    } else if (ctx.JUNOS_PRINTER() != null) {
      return JunosApplication.JUNOS_PRINTER;
    } else if (ctx.JUNOS_R2CP() != null) {
      return JunosApplication.JUNOS_R2CP;
    } else if (ctx.JUNOS_RADACCT() != null) {
      return JunosApplication.JUNOS_RADACCT;
    } else if (ctx.JUNOS_RADIUS() != null) {
      return JunosApplication.JUNOS_RADIUS;
    } else if (ctx.JUNOS_REALAUDIO() != null) {
      return JunosApplication.JUNOS_REALAUDIO;
    } else if (ctx.JUNOS_RDP() != null) {
      return JunosApplication.JUNOS_RDP;
    } else if (ctx.JUNOS_RIP() != null) {
      return JunosApplication.JUNOS_RIP;
    } else if (ctx.JUNOS_RSH() != null) {
      return JunosApplication.JUNOS_RSH;
    } else if (ctx.JUNOS_RTSP() != null) {
      return JunosApplication.JUNOS_RTSP;
    } else if (ctx.JUNOS_SCCP() != null) {
      return JunosApplication.JUNOS_SCCP;
    } else if (ctx.JUNOS_SCTP_ANY() != null) {
      return JunosApplication.JUNOS_SCTP_ANY;
    } else if (ctx.JUNOS_SIP() != null) {
      return JunosApplication.JUNOS_SIP;
    } else if (ctx.JUNOS_SMB() != null) {
      return JunosApplication.JUNOS_SMB;
    } else if (ctx.JUNOS_SMB_SESSION() != null) {
      return JunosApplication.JUNOS_SMB_SESSION;
    } else if (ctx.JUNOS_SMTP() != null) {
      return JunosApplication.JUNOS_SMTP;
    } else if (ctx.JUNOS_SMTPS() != null) {
      return JunosApplication.JUNOS_SMTPS;
    } else if (ctx.JUNOS_SNMP_AGENTX() != null) {
      return JunosApplication.JUNOS_SNMP_AGENTX;
    } else if (ctx.JUNOS_SNPP() != null) {
      return JunosApplication.JUNOS_SNPP;
    } else if (ctx.JUNOS_SQL_MONITOR() != null) {
      return JunosApplication.JUNOS_SQL_MONITOR;
    } else if (ctx.JUNOS_SQLNET_V1() != null) {
      return JunosApplication.JUNOS_SQLNET_V1;
    } else if (ctx.JUNOS_SQLNET_V2() != null) {
      return JunosApplication.JUNOS_SQLNET_V2;
    } else if (ctx.JUNOS_SSH() != null) {
      return JunosApplication.JUNOS_SSH;
    } else if (ctx.JUNOS_STUN_TCP() != null) {
      return JunosApplication.JUNOS_STUN_TCP;
    } else if (ctx.JUNOS_STUN_UDP() != null) {
      return JunosApplication.JUNOS_STUN_UDP;
    } else if (ctx.JUNOS_SUN_RPC_ANY_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_ANY_TCP;
    } else if (ctx.JUNOS_SUN_RPC_ANY_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_ANY_UDP;
    } else if (ctx.JUNOS_SUN_RPC_MOUNTD_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_MOUNTD_TCP;
    } else if (ctx.JUNOS_SUN_RPC_MOUNTD_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_MOUNTD_UDP;
    } else if (ctx.JUNOS_SUN_RPC_NFS_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_NFS_TCP;
    } else if (ctx.JUNOS_SUN_RPC_NFS_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_NFS_UDP;
    } else if (ctx.JUNOS_SUN_RPC_NLOCKMGR_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_NLOCKMGR_TCP;
    } else if (ctx.JUNOS_SUN_RPC_NLOCKMGR_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_NLOCKMGR_UDP;
    } else if (ctx.JUNOS_SUN_RPC_PORTMAP_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_PORTMAP_TCP;
    } else if (ctx.JUNOS_SUN_RPC_PORTMAP_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_PORTMAP_UDP;
    } else if (ctx.JUNOS_SUN_RPC_RQUOTAD_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_RQUOTAD_TCP;
    } else if (ctx.JUNOS_SUN_RPC_RQUOTAD_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_RQUOTAD_UDP;
    } else if (ctx.JUNOS_SUN_RPC_RUSERD_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_RUSERD_TCP;
    } else if (ctx.JUNOS_SUN_RPC_RUSERD_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_RUSERD_UDP;
    } else if (ctx.JUNOS_SUN_RPC_SADMIND_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_SADMIND_TCP;
    } else if (ctx.JUNOS_SUN_RPC_SADMIND_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_SADMIND_UDP;
    } else if (ctx.JUNOS_SUN_RPC_SPRAYD_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_SPRAYD_TCP;
    } else if (ctx.JUNOS_SUN_RPC_SPRAYD_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_SPRAYD_UDP;
    } else if (ctx.JUNOS_SUN_RPC_STATUS_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_STATUS_TCP;
    } else if (ctx.JUNOS_SUN_RPC_STATUS_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_STATUS_UDP;
    } else if (ctx.JUNOS_SUN_RPC_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_TCP;
    } else if (ctx.JUNOS_SUN_RPC_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_UDP;
    } else if (ctx.JUNOS_SUN_RPC_WALLD_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_WALLD_TCP;
    } else if (ctx.JUNOS_SUN_RPC_WALLD_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_WALLD_UDP;
    } else if (ctx.JUNOS_SUN_RPC_YPBIND_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_YPBIND_TCP;
    } else if (ctx.JUNOS_SUN_RPC_YPBIND_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_YPBIND_UDP;
    } else if (ctx.JUNOS_SUN_RPC_YPSERV_TCP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_YPSERV_TCP;
    } else if (ctx.JUNOS_SUN_RPC_YPSERV_UDP() != null) {
      return JunosApplication.JUNOS_SUN_RPC_YPSERV_UDP;
    } else if (ctx.JUNOS_SYSLOG() != null) {
      return JunosApplication.JUNOS_SYSLOG;
    } else if (ctx.JUNOS_TACACS() != null) {
      return JunosApplication.JUNOS_TACACS;
    } else if (ctx.JUNOS_TACACS_DS() != null) {
      return JunosApplication.JUNOS_TACACS_DS;
    } else if (ctx.JUNOS_TALK() != null) {
      return JunosApplication.JUNOS_TALK;
    } else if (ctx.JUNOS_TCP_ANY() != null) {
      return JunosApplication.JUNOS_TCP_ANY;
    } else if (ctx.JUNOS_TELNET() != null) {
      return JunosApplication.JUNOS_TELNET;
    } else if (ctx.JUNOS_TFTP() != null) {
      return JunosApplication.JUNOS_TFTP;
    } else if (ctx.JUNOS_UDP_ANY() != null) {
      return JunosApplication.JUNOS_UDP_ANY;
    } else if (ctx.JUNOS_UUCP() != null) {
      return JunosApplication.JUNOS_UUCP;
    } else if (ctx.JUNOS_VDO_LIVE() != null) {
      return JunosApplication.JUNOS_VDO_LIVE;
    } else if (ctx.JUNOS_VNC() != null) {
      return JunosApplication.JUNOS_VNC;
    } else if (ctx.JUNOS_WAIS() != null) {
      return JunosApplication.JUNOS_WAIS;
    } else if (ctx.JUNOS_WHO() != null) {
      return JunosApplication.JUNOS_WHO;
    } else if (ctx.JUNOS_WHOIS() != null) {
      return JunosApplication.JUNOS_WHOIS;
    } else if (ctx.JUNOS_WINFRAME() != null) {
      return JunosApplication.JUNOS_WINFRAME;
    } else if (ctx.JUNOS_WXCONTROL() != null) {
      return JunosApplication.JUNOS_WXCONTROL;
    } else if (ctx.JUNOS_X_WINDOWS() != null) {
      return JunosApplication.JUNOS_X_WINDOWS;
    } else if (ctx.JUNOS_XNM_CLEAR_TEXT() != null) {
      return JunosApplication.JUNOS_XNM_CLEAR_TEXT;
    } else if (ctx.JUNOS_XNM_SSL() != null) {
      return JunosApplication.JUNOS_XNM_SSL;
    } else {
      assert ctx.JUNOS_YMSG() != null;
      return JunosApplication.JUNOS_YMSG;
    }
  }

  private JunosApplicationSet toJunosApplicationSet(Junos_application_setContext ctx) {
    if (ctx.JUNOS_CIFS() != null) {
      return JunosApplicationSet.JUNOS_CIFS;
    } else if (ctx.JUNOS_MGCP() != null) {
      return JunosApplicationSet.JUNOS_MGCP;
    } else if (ctx.JUNOS_MS_RPC() != null) {
      return JunosApplicationSet.JUNOS_MS_RPC;
    } else if (ctx.JUNOS_MS_RPC_ANY() != null) {
      return JunosApplicationSet.JUNOS_MS_RPC_ANY;
    } else if (ctx.JUNOS_MS_RPC_IIS_COM() != null) {
      return JunosApplicationSet.JUNOS_MS_RPC_IIS_COM;
    } else if (ctx.JUNOS_MS_RPC_MSEXCHANGE() != null) {
      return JunosApplicationSet.JUNOS_MS_RPC_MSEXCHANGE;
    } else if (ctx.JUNOS_MS_RPC_WMIC() != null) {
      return JunosApplicationSet.JUNOS_MS_RPC_WMIC;
    } else if (ctx.JUNOS_ROUTING_INBOUND() != null) {
      return JunosApplicationSet.JUNOS_ROUTING_INBOUND;
    } else if (ctx.JUNOS_STUN() != null) {
      return JunosApplicationSet.JUNOS_STUN;
    } else if (ctx.JUNOS_SUN_RPC() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC;
    } else if (ctx.JUNOS_SUN_RPC_ANY() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_ANY;
    } else if (ctx.JUNOS_SUN_RPC_MOUNTD() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_MOUNTD;
    } else if (ctx.JUNOS_SUN_RPC_NFS() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_NFS;
    } else if (ctx.JUNOS_SUN_RPC_NFS_ACCESS() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_NFS_ACCESS;
    } else if (ctx.JUNOS_SUN_RPC_NLOCKMGR() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_NLOCKMGR;
    } else if (ctx.JUNOS_SUN_RPC_PORTMAP() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_PORTMAP;
    } else if (ctx.JUNOS_SUN_RPC_RQUOTAD() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_RQUOTAD;
    } else if (ctx.JUNOS_SUN_RPC_RUSERD() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_RUSERD;
    } else if (ctx.JUNOS_SUN_RPC_SADMIND() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_SADMIND;
    } else if (ctx.JUNOS_SUN_RPC_SPRAYD() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_SPRAYD;
    } else if (ctx.JUNOS_SUN_RPC_STATUS() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_STATUS;
    } else if (ctx.JUNOS_SUN_RPC_WALLD() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_WALLD;
    } else if (ctx.JUNOS_SUN_RPC_YPBIND() != null) {
      return JunosApplicationSet.JUNOS_SUN_RPC_YPBIND;
    } else {
      assert ctx.JUNOS_SUN_RPC_YPSERV() != null;
      return JunosApplicationSet.JUNOS_SUN_RPC_YPSERV;
    }
  }

  private static @Nonnull StandardCommunity toStandardCommunity(Sc_literalContext ctx) {
    String text = ctx.STANDARD_COMMUNITY().getText();
    return StandardCommunity.parse(text);
  }

  private @Nonnull StandardCommunity toStandardCommunity(Sc_namedContext ctx) {
    if (ctx.NO_ADVERTISE() != null) {
      return StandardCommunity.NO_ADVERTISE;
    } else if (ctx.NO_EXPORT() != null) {
      return StandardCommunity.NO_EXPORT;
    } else {
      assert ctx.NO_EXPORT_SUBCONFED() != null;
      return StandardCommunity.NO_EXPORT_SUBCONFED;
    }
  }

  private @Nonnull StandardCommunity toStandardCommunity(Standard_communityContext ctx) {
    if (ctx.sc_literal() != null) {
      return toStandardCommunity(ctx.sc_literal());
    } else {
      assert ctx.sc_named() != null;
      return toStandardCommunity(ctx.sc_named());
    }
  }

  private static EncryptionAlgorithm toEncryptionAlgorithm(Encryption_algorithmContext ctx) {
    if (ctx.THREEDES_CBC() != null) {
      return EncryptionAlgorithm.THREEDES_CBC;
    } else if (ctx.AES_128_CBC() != null) {
      return EncryptionAlgorithm.AES_128_CBC;
    } else if (ctx.AES_128_GCM() != null) {
      return EncryptionAlgorithm.AES_128_GCM;
    } else if (ctx.AES_192_CBC() != null) {
      return EncryptionAlgorithm.AES_192_CBC;
    } else if (ctx.AES_192_GCM() != null) {
      return EncryptionAlgorithm.AES_192_GCM;
    } else if (ctx.AES_256_CBC() != null) {
      return EncryptionAlgorithm.AES_256_CBC;
    } else if (ctx.AES_256_GCM() != null) {
      return EncryptionAlgorithm.AES_256_GCM;
    } else if (ctx.DES_CBC() != null) {
      return EncryptionAlgorithm.DES_CBC;
    } else {
      throw new BatfishException("Invalid encryption algorithm: " + ctx.getText());
    }
  }

  private static HostProtocol toHostProtocol(Hib_protocolContext ctx) {
    if (ctx.ALL() != null) {
      return HostProtocol.ALL;
    } else if (ctx.BFD() != null) {
      return HostProtocol.BFD;
    } else if (ctx.BGP() != null) {
      return HostProtocol.BGP;
    } else if (ctx.DVMRP() != null) {
      return HostProtocol.DVMRP;
    } else if (ctx.IGMP() != null) {
      return HostProtocol.IGMP;
    } else if (ctx.LDP() != null) {
      return HostProtocol.LDP;
    } else if (ctx.MSDP() != null) {
      return HostProtocol.MSDP;
    } else if (ctx.NHRP() != null) {
      return HostProtocol.NHRP;
    } else if (ctx.OSPF() != null) {
      return HostProtocol.OSPF;
    } else if (ctx.OSPF3() != null) {
      return HostProtocol.OSPF3;
    } else if (ctx.PGM() != null) {
      return HostProtocol.PGM;
    } else if (ctx.PIM() != null) {
      return HostProtocol.PIM;
    } else if (ctx.RIP() != null) {
      return HostProtocol.RIP;
    } else if (ctx.RIPNG() != null) {
      return HostProtocol.RIPNG;
    } else if (ctx.ROUTER_DISCOVERY() != null) {
      return HostProtocol.ROUTER_DISCOVERY;
    } else if (ctx.RSVP() != null) {
      return HostProtocol.RSVP;
    } else if (ctx.SAP() != null) {
      return HostProtocol.SAP;
    } else if (ctx.VRRP() != null) {
      return HostProtocol.VRRP;
    } else {
      throw new BatfishException("Invalid host protocol");
    }
  }

  private static HostSystemService toHostSystemService(Hib_system_serviceContext ctx) {
    if (ctx.ALL() != null) {
      return HostSystemService.ALL;
    } else if (ctx.ANY_SERVICE() != null) {
      return HostSystemService.ANY_SERVICE;
    } else if (ctx.DHCP() != null) {
      return HostSystemService.DHCP;
    } else if (ctx.DNS() != null) {
      return HostSystemService.DNS;
    } else if (ctx.FINGER() != null) {
      return HostSystemService.FINGER;
    } else if (ctx.FTP() != null) {
      return HostSystemService.FTP;
    } else if (ctx.HTTP() != null) {
      return HostSystemService.HTTP;
    } else if (ctx.HTTPS() != null) {
      return HostSystemService.HTTPS;
    } else if (ctx.IDENT_RESET() != null) {
      return HostSystemService.IDENT_RESET;
    } else if (ctx.IKE() != null) {
      return HostSystemService.IKE;
    } else if (ctx.LSPING() != null) {
      return HostSystemService.LSPING;
    } else if (ctx.NETCONF() != null) {
      return HostSystemService.NETCONF;
    } else if (ctx.NTP() != null) {
      return HostSystemService.NTP;
    } else if (ctx.PING() != null) {
      return HostSystemService.PING;
    } else if (ctx.R2CP() != null) {
      return HostSystemService.R2CP;
    } else if (ctx.REVERSE_SSH() != null) {
      return HostSystemService.REVERSE_SSH;
    } else if (ctx.REVERSE_TELNET() != null) {
      return HostSystemService.REVERSE_TELNET;
    } else if (ctx.RLOGIN() != null) {
      return HostSystemService.RLOGIN;
    } else if (ctx.RPM() != null) {
      return HostSystemService.RPM;
    } else if (ctx.RSH() != null) {
      return HostSystemService.RSH;
    } else if (ctx.SIP() != null) {
      return HostSystemService.SIP;
    } else if (ctx.SNMP() != null) {
      return HostSystemService.SNMP;
    } else if (ctx.SNMP_TRAP() != null) {
      return HostSystemService.SNMP_TRAP;
    } else if (ctx.SSH() != null) {
      return HostSystemService.SSH;
    } else if (ctx.TELNET() != null) {
      return HostSystemService.TELNET;
    } else if (ctx.TFTP() != null) {
      return HostSystemService.TFTP;
    } else if (ctx.TRACEROUTE() != null) {
      return HostSystemService.TRACEROUTE;
    } else if (ctx.XNM_CLEAR_TEXT() != null) {
      return HostSystemService.XNM_CLEAR_TEXT;
    } else if (ctx.XNM_SSL() != null) {
      return HostSystemService.XNM_SSL;
    } else {
      throw new BatfishException("Invalid host system service");
    }
  }

  /**
   * Maps a user-friendly name for an ICMP Code to an integer, or returns {@code null} and warns for
   * unknown strings. See
   * https://www.juniper.net/documentation/en_US/junos/topics/usage-guidelines/services-configuring-application-protocol-properties.html#id-10141121
   */
  private @Nonnull Optional<IcmpCode> toIcmpCode(
      ParserRuleContext messageCtx, Named_icmp_codeContext ctx) {
    if (ctx.COMMUNICATION_PROHIBITED_BY_FILTERING() != null) {
      return Optional.of(IcmpCode.COMMUNICATION_ADMINISTRATIVELY_PROHIBITED);
    } else if (ctx.DESTINATION_HOST_PROHIBITED() != null) {
      return Optional.of(IcmpCode.DESTINATION_HOST_PROHIBITED);
    } else if (ctx.DESTINATION_HOST_UNKNOWN() != null) {
      return Optional.of(IcmpCode.DESTINATION_HOST_UNKNOWN);
    } else if (ctx.FRAGMENTATION_NEEDED() != null) {
      return Optional.of(IcmpCode.FRAGMENTATION_NEEDED);
    } else if (ctx.HOST_PRECEDENCE_VIOLATION() != null) {
      return Optional.of(IcmpCode.HOST_PRECEDENCE_VIOLATION);
    } else if (ctx.HOST_UNREACHABLE() != null) {
      return Optional.of(IcmpCode.HOST_UNREACHABLE);
    } else if (ctx.HOST_UNREACHABLE_FOR_TOS() != null) {
      return Optional.of(IcmpCode.HOST_UNREACHABLE_FOR_TOS);
    } else if (ctx.IP_HEADER_BAD() != null) {
      return Optional.of(IcmpCode.INVALID_IP_HEADER);
    } else if (ctx.NETWORK_UNREACHABLE() != null) {
      return Optional.of(IcmpCode.NETWORK_UNREACHABLE);
    } else if (ctx.NETWORK_UNREACHABLE_FOR_TOS() != null) {
      return Optional.of(IcmpCode.NETWORK_UNREACHABLE_FOR_TOS);
    } else if (ctx.PORT_UNREACHABLE() != null) {
      return Optional.of(IcmpCode.PORT_UNREACHABLE);
    } else if (ctx.PRECEDENCE_CUTOFF_IN_EFFECT() != null) {
      return Optional.of(IcmpCode.PRECEDENCE_CUTOFF_IN_EFFECT);
    } else if (ctx.PROTOCOL_UNREACHABLE() != null) {
      return Optional.of(IcmpCode.PROTOCOL_UNREACHABLE);
    } else if (ctx.REDIRECT_FOR_HOST() != null) {
      return Optional.of(IcmpCode.HOST_ERROR);
    } else if (ctx.REDIRECT_FOR_NETWORK() != null) {
      return Optional.of(IcmpCode.NETWORK_ERROR);
    } else if (ctx.REDIRECT_FOR_TOS_AND_HOST() != null) {
      return Optional.of(IcmpCode.TOS_AND_HOST_ERROR);
    } else if (ctx.REDIRECT_FOR_TOS_AND_NET() != null) {
      return Optional.of(IcmpCode.TOS_AND_NETWORK_ERROR);
    } else if (ctx.REQUIRED_OPTION_MISSING() != null) {
      return Optional.of(IcmpCode.REQUIRED_OPTION_MISSING);
    } else if (ctx.SOURCE_HOST_ISOLATED() != null) {
      return Optional.of(IcmpCode.SOURCE_HOST_ISOLATED);
    } else if (ctx.SOURCE_ROUTE_FAILED() != null) {
      return Optional.of(IcmpCode.SOURCE_ROUTE_FAILED);
    } else if (ctx.TTL_EQ_ZERO_DURING_REASSEMBLY() != null) {
      return Optional.of(IcmpCode.TIME_EXCEEDED_DURING_FRAGMENT_REASSEMBLY);
    } else if (ctx.TTL_EQ_ZERO_DURING_TRANSIT() != null) {
      return Optional.of(IcmpCode.TTL_EQ_ZERO_DURING_TRANSIT);
    } else {
      warn(messageCtx, String.format("Missing mapping for icmp-code: '%s'", ctx.getText()));
      return Optional.empty();
    }
  }

  /**
   * Maps a user-friendly name for an ICMP Type to an integer, or returns {@code null} and warns for
   * unknown strings. See
   * https://www.juniper.net/documentation/en_US/junos/topics/usage-guidelines/services-configuring-application-protocol-properties.html#id-10141121
   */
  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Named_icmp_typeContext ctx) {
    if (ctx.DESTINATION_UNREACHABLE() != null) {
      return Optional.of(IcmpType.DESTINATION_UNREACHABLE);
    } else if (ctx.ECHO_REPLY() != null) {
      return Optional.of(IcmpType.ECHO_REPLY);
    } else if (ctx.ECHO_REQUEST() != null) {
      return Optional.of(IcmpType.ECHO_REQUEST);
    } else if (ctx.INFO_REPLY() != null) {
      return Optional.of(IcmpType.INFO_REPLY);
    } else if (ctx.INFO_REQUEST() != null) {
      return Optional.of(IcmpType.INFO_REQUEST);
    } else if (ctx.PARAMETER_PROBLEM() != null) {
      return Optional.of(IcmpType.PARAMETER_PROBLEM);
    } else if (ctx.REDIRECT() != null) {
      return Optional.of(IcmpType.REDIRECT_MESSAGE);
    } else if (ctx.ROUTER_ADVERTISEMENT() != null) {
      return Optional.of(IcmpType.ROUTER_ADVERTISEMENT);
    } else if (ctx.ROUTER_SOLICIT() != null) {
      return Optional.of(IcmpType.ROUTER_SOLICITATION);
    } else if (ctx.SOURCE_QUENCH() != null) {
      return Optional.of(IcmpType.SOURCE_QUENCH);
    } else if (ctx.TIME_EXCEEDED() != null) {
      return Optional.of(IcmpType.TIME_EXCEEDED);
    } else if (ctx.TIMESTAMP() != null) {
      return Optional.of(IcmpType.TIMESTAMP_REQUEST);
    } else if (ctx.TIMESTAMP_REPLY() != null) {
      return Optional.of(IcmpType.TIMESTAMP_REPLY);
    } else if (ctx.UNREACHABLE() != null) {
      return Optional.of(IcmpType.DESTINATION_UNREACHABLE);
    } else {
      warn(messageCtx, String.format("Missing mapping for icmp-type: '%s'", ctx.getText()));
      return Optional.empty();
    }
  }

  private static IkeHashingAlgorithm toIkeHashingAlgorithm(
      Ike_authentication_algorithmContext ctx) {
    if (ctx.MD5() != null) {
      return IkeHashingAlgorithm.MD5;
    } else if (ctx.SHA1() != null) {
      return IkeHashingAlgorithm.SHA1;
    } else if (ctx.SHA_256() != null) {
      return IkeHashingAlgorithm.SHA_256;
    } else if (ctx.SHA_384() != null) {
      return IkeHashingAlgorithm.SHA_384;
    } else {
      throw new BatfishException("invalid ike authentication algorithm: " + ctx.getText());
    }
  }

  private static IkeAuthenticationMethod toIkeAuthenticationMethod(
      Ike_authentication_methodContext ctx) {
    if (ctx.DSA_SIGNATURES() != null) {
      return IkeAuthenticationMethod.DSA_SIGNATURES;
    } else if (ctx.PRE_SHARED_KEYS() != null) {
      return IkeAuthenticationMethod.PRE_SHARED_KEYS;
    } else if (ctx.RSA_SIGNATURES() != null) {
      return IkeAuthenticationMethod.RSA_SIGNATURES;
    } else {
      throw new BatfishException("Invalid ike authentication method: " + ctx.getText());
    }
  }

  private OspfInterfaceType toOspfInterfaceType(Ospf_interface_typeContext ctx) {
    if (ctx.NBMA() != null) {
      return OspfInterfaceType.NBMA;
    } else if (ctx.P2MP() != null) {
      return OspfInterfaceType.P2MP;
    } else if (ctx.P2MP_OVER_LAN() != null) {
      return OspfInterfaceType.P2MP_OVER_LAN;
    } else if (ctx.P2P() != null) {
      return OspfInterfaceType.P2P;
    } else {
      return convProblem(OspfInterfaceType.class, ctx, null);
    }
  }

  private long toAsNum(Bgp_asnContext ctx) {
    if (ctx.asn != null) {
      return toLong(ctx.asn);
    } else {
      assert ctx.asn4hi != null;
      long hi = toLong(ctx.asn4hi);
      long lo = toLong(ctx.asn4lo);
      return (hi << 16) + lo;
    }
  }

  private static int toInt(DecContext ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private static int toInt(Port_numberContext ctx) {
    // TODO: validate value is from 1-65535, handle in callers
    return Integer.parseInt(ctx.getText());
  }

  private static int toInt(Named_portContext ctx) {
    return getNamedPort(ctx).number();
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Vni_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, VNI_NUMBER_RANGE, "vni");
  }

  private static @Nonnull IpOptions toIpOptions(Ip_optionContext ctx) {
    if (ctx.LOOSE_SOURCE_ROUTE() != null) {
      return IpOptions.LOOSE_SOURCE_ROUTE;
    } else if (ctx.ROUTE_RECORD() != null) {
      return IpOptions.ROUTE_RECORD;
    } else if (ctx.ROUTER_ALERT() != null) {
      return IpOptions.ROUTER_ALERT;
    } else if (ctx.SECURITY() != null) {
      return IpOptions.SECURITY;
    } else if (ctx.STREAM_ID() != null) {
      return IpOptions.STREAM_ID;
    } else if (ctx.STRICT_SOURCE_ROUTE() != null) {
      return IpOptions.STRICT_SOURCE_ROUTE;
    } else if (ctx.TIMESTAMP() != null) {
      return IpOptions.TIMESTAMP;
    }
    throw new IllegalArgumentException("unsupported");
  }

  private static long toLong(DecContext ctx) {
    return Long.parseLong(ctx.getText());
  }

  private static long toLong(Uint32Context ctx) {
    return Long.parseLong(ctx.getText());
  }

  private static long toLong(Uint32lContext ctx) {
    return Long.parseLong(ctx.getText().substring(0, ctx.getText().length() - 1));
  }

  private static long toLong(Uint16Context ctx) {
    return Long.parseLong(ctx.getText());
  }

  private @Nonnull Optional<Long> toLong(ParserRuleContext messageCtx, Srlg_valueContext ctx) {
    return toLongInSpace(messageCtx, ctx, SRLG_VALUE_RANGE, "srlg-value");
  }

  private static IpProtocol toIpProtocol(Ip_protocolContext ctx) {
    if (ctx.dec() != null) {
      int protocolNum = toInt(ctx.dec());
      return IpProtocol.fromNumber(protocolNum);
    } else if (ctx.AH() != null) {
      return IpProtocol.AHP;
    } else if (ctx.EGP() != null) {
      return IpProtocol.EGP;
    } else if (ctx.ESP() != null) {
      return IpProtocol.ESP;
    } else if (ctx.GRE() != null) {
      return IpProtocol.GRE;
    } else if (ctx.HOP_BY_HOP() != null) {
      return IpProtocol.HOPOPT;
    } else if (ctx.ICMP() != null) {
      return IpProtocol.ICMP;
    } else if (ctx.IGMP() != null) {
      return IpProtocol.IGMP;
    } else if (ctx.IPIP() != null) {
      return IpProtocol.IPINIP;
    } else if (ctx.IPV6() != null) {
      return IpProtocol.IPV6;
    } else if (ctx.OSPF() != null) {
      return IpProtocol.OSPF;
    } else if (ctx.PIM() != null) {
      return IpProtocol.PIM;
    } else if (ctx.RSVP() != null) {
      return IpProtocol.RSVP;
    } else if (ctx.SCTP() != null) {
      return IpProtocol.SCTP;
    } else if (ctx.TCP() != null) {
      return IpProtocol.TCP;
    } else if (ctx.UDP() != null) {
      return IpProtocol.UDP;
    } else if (ctx.VRRP() != null) {
      return IpProtocol.VRRP;
    } else {
      throw new BatfishException(
          "missing protocol-enum mapping for protocol: \"" + ctx.getText() + "\"");
    }
  }

  private static IpsecAuthenticationAlgorithm toIpsecAuthenticationAlgorithm(
      Ipsec_authentication_algorithmContext ctx) {
    if (ctx.HMAC_MD5_96() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_MD5_96;
    } else if (ctx.HMAC_SHA1_96() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_SHA1_96;
    } else if (ctx.HMAC_SHA_256_128() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_SHA_256_128;
    } else {
      throw new BatfishException("invalid ipsec authentication algorithm: " + ctx.getText());
    }
  }

  private static SortedSet<IpsecProtocol> toIpsecProtocol(Ipsec_protocolContext ctx) {
    if (ctx.AH() != null) {
      return ImmutableSortedSet.of(IpsecProtocol.AH);
    } else if (ctx.ESP() != null) {
      return ImmutableSortedSet.of(IpsecProtocol.ESP);
    } else if (ctx.BUNDLE() != null) {
      return ImmutableSortedSet.of(IpsecProtocol.AH, IpsecProtocol.ESP);
    } else {
      throw new BatfishException("invalid ipsec protocol: " + ctx.getText());
    }
  }

  private static List<SubRange> toRange(RangeContext ctx) {
    List<SubRange> range = new ArrayList<>();
    for (SubrangeContext sc : ctx.range_list) {
      SubRange sr = toSubRange(sc);
      range.add(sr);
    }
    return range;
  }

  private static RouteDistinguisher toRouteDistinguisher(Route_distinguisherContext ctx) {
    if (ctx.rd_ip_address_colon_id() != null) {
      String[] rd_ip = ctx.rd_ip_address_colon_id().getText().split(":");
      return RouteDistinguisher.from(Ip.parse(rd_ip[0]), Integer.parseInt(rd_ip[1]));
    } else {
      assert ctx.rd_asn_colon_id() != null;
      if (ctx.rd_asn_colon_id().high32 != null) {
        // If "L" appended to the number it's a 4-byte ASN.
        return RouteDistinguisher.from(
            toLong(ctx.rd_asn_colon_id().high32), toInteger(ctx.rd_asn_colon_id().low16));
      }
      return RouteDistinguisher.from(
          toInteger(ctx.rd_asn_colon_id().high16), toLong(ctx.rd_asn_colon_id().low32));
    }
  }

  private @Nonnull Optional<SubRange> toSubRange(
      ParserRuleContext messageCtx, Vlan_rangeContext ctx) {
    Optional<Integer> rangeStart = toInteger(messageCtx, ctx.start);
    if (!rangeStart.isPresent()) {
      return Optional.empty();
    }
    Optional<Integer> rangeEnd = ctx.end == null ? rangeStart : toInteger(messageCtx, ctx.end);
    if (!rangeEnd.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(new SubRange(rangeStart.get(), rangeEnd.get()));
  }

  private static SubRange toSubRange(SubrangeContext ctx) {
    int low = toInt(ctx.low);
    int high = (ctx.high != null) ? toInt(ctx.high) : low;
    return new SubRange(low, high);
  }

  private static @Nonnull SubRange toSubRange(Port_rangeContext ctx) {
    if (ctx.named_port() != null) {
      return SubRange.singleton(toInt(ctx.named));
    } else {
      assert ctx.start != null;
      int start = toInt(ctx.start);
      return ctx.end != null ? new SubRange(start, toInt(ctx.end)) : SubRange.singleton(start);
    }
  }

  private @Nonnull Optional<SubRange> toSubRange(
      ParserRuleContext messageCtx, Vni_rangeContext ctx) {
    Optional<Integer> rangeStart = toInteger(messageCtx, ctx.start);
    if (!rangeStart.isPresent()) {
      return Optional.empty();
    }
    Optional<Integer> rangeEnd = ctx.end == null ? rangeStart : toInteger(messageCtx, ctx.end);
    if (!rangeEnd.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(new SubRange(rangeStart.get(), rangeEnd.get()));
  }

  private static TcpFlagsMatchConditions toTcpFlags(Tcp_flags_alternativeContext ctx) {
    TcpFlagsMatchConditions.Builder tcpFlagsConditionsBuilder = TcpFlagsMatchConditions.builder();
    TcpFlags.Builder tcpFlagsBuilder = TcpFlags.builder();
    for (Tcp_flags_literalContext literalCtx : ctx.literals) {
      boolean value = literalCtx.BANG() == null;
      Tcp_flags_atomContext atom = literalCtx.tcp_flags_atom();
      if (atom.ACK() != null) {
        tcpFlagsConditionsBuilder.setUseAck(true);
        tcpFlagsBuilder.setAck(value);
      } else if (atom.CWR() != null) {
        tcpFlagsConditionsBuilder.setUseCwr(true);
        tcpFlagsBuilder.setCwr(value);
      } else if (atom.ECE() != null) {
        tcpFlagsConditionsBuilder.setUseEce(true);
        tcpFlagsBuilder.setEce(value);
      } else if (atom.FIN() != null) {
        tcpFlagsConditionsBuilder.setUseFin(true);
        tcpFlagsBuilder.setFin(value);
      } else if (atom.PSH() != null) {
        tcpFlagsConditionsBuilder.setUsePsh(true);
        tcpFlagsBuilder.setPsh(value);
      } else if (atom.RST() != null) {
        tcpFlagsConditionsBuilder.setUseRst(true);
        tcpFlagsBuilder.setRst(value);
      } else if (atom.SYN() != null) {
        tcpFlagsConditionsBuilder.setUseSyn(true);
        tcpFlagsBuilder.setSyn(value);
      } else if (atom.URG() != null) {
        tcpFlagsConditionsBuilder.setUseUrg(true);
        tcpFlagsBuilder.setUrg(value);
      } else {
        throw new BatfishException("Invalid tcp-flags atom: " + atom.getText());
      }
    }
    return tcpFlagsConditionsBuilder.setTcpFlags(tcpFlagsBuilder.build()).build();
  }

  private static List<TcpFlagsMatchConditions> toTcpFlags(Tcp_flagsContext ctx) {
    List<TcpFlagsMatchConditions> tcpFlagsList = new ArrayList<>();
    for (Tcp_flags_alternativeContext alternativeCtx : ctx.alternatives) {
      TcpFlagsMatchConditions tcpFlags = toTcpFlags(alternativeCtx);
      tcpFlagsList.add(tcpFlags);
    }
    return tcpFlagsList;
  }

  static @Nonnull Optional<String> unquote(String text) {
    if (text.isEmpty()) {
      return Optional.of(text);
    } else if (text.charAt(0) != '"') {
      return Optional.of(text);
    } else if (text.length() < 2 || text.charAt(text.length() - 1) != '"') {
      // Invalid quoting
      return Optional.empty();
    } else {
      return Optional.of(text.substring(1, text.length() - 1));
    }
  }

  private @Nonnull String unquote(String text, ParserRuleContext ctx) {
    Optional<String> unquoted = unquote(text);
    if (!unquoted.isPresent()) {
      warn(ctx, "Improperly-quoted string");
      return text;
    }
    return unquoted.get();
  }

  private JuniperConfiguration _configuration;

  private int _conjunctionPolicyIndex;

  private AddressBook _currentAddressBook;

  private AddressSetAddressBookEntry _currentAddressSetAddressBookEntry;

  private AggregateRoute _currentAggregateRoute;

  private BaseApplication _currentApplication;

  private Term _currentApplicationTerm;

  private OspfArea _currentArea;

  private @Nullable Long _currentAreaRangeMetric;

  private Prefix _currentAreaRangePrefix;

  private boolean _currentAreaRangeRestrict;

  private JuniperAuthenticationKey _currentAuthenticationKey;

  private JuniperAuthenticationKeyChain _currentAuthenticationKeyChain;

  private AaaAuthenticationLoginList _currentAuthenticationOrder;

  private BgpGroup _currentBgpGroup;

  private NamedCommunity _currentCommunityList;

  private DhcpRelayGroup _currentDhcpRelayGroup;
  private DhcpRelayServerGroup _currentDhcpRelayServerGroup;

  // TODO: separate firewall filter and security-policy
  private ConcreteFirewallFilter _currentFilter;
  private String _currentSecurityPolicyName; // Follows _currentFilter, but with correct name.

  private Family _currentFirewallFamily;

  private Zone _currentFromZone;

  private FwTerm _currentFwTerm;

  private GeneratedRoute _currentGeneratedRoute;

  private Screen _currentScreen;

  private IkeGateway _currentIkeGateway;

  private IkePolicy _currentIkePolicy;

  private IkeProposal _currentIkeProposal;

  private Interface _currentInterfaceOrRange;

  private ConcreteInterfaceAddress _currentInterfaceAddress;

  private IpsecPolicy _currentIpsecPolicy;

  private IpsecProposal _currentIpsecProposal;

  private IpsecVpn _currentIpsecVpn;

  private IsisInterfaceSettings _currentIsisInterfaceSettings;

  private IsisInterfaceLevelSettings _currentIsisInterfaceLevelSettings;

  private IsisLevelSettings _currentIsisLevelSettings;

  private Line _currentLine;

  private Interface _currentMasterInterface;

  private OspfInterfaceSettings _currentOspfSettings;

  private Nat _currentNat;

  private NatPool _currentNatPool;

  private NatRule _currentNatRule;

  private NatRuleSet _currentNatRuleSet;

  private Condition _currentCondition;

  private PolicyStatement _currentPolicyStatement;

  private PrefixList _currentPrefixList;

  private PsTerm _currentPsTerm;

  private PsThens _currentPsThens;

  private void addPsThen(PsThen then, ParserRuleContext ctx) {
    List<String> cleared = _currentPsThens.addPsThen(then);
    if (!cleared.isEmpty()) {
      warnRisky(
          ctx,
          String.format(
              "Overwriting existing %s %s",
              cleared.size() > 1 ? "thens" : "then", String.join(", ", cleared)));
    }
  }

  private QualifiedNextHop _currentQualifiedNextHop;

  private RoutingInformationBase _currentRib;

  private RibGroup _currentRibGroup;

  private Route6FilterLine _currentRoute6FilterLine;

  private Prefix6 _currentRoute6FilterPrefix;

  private RouteFilter _currentRouteFilter;

  private Route4FilterLine _currentRouteFilterLine;

  private Prefix _currentRouteFilterPrefix;

  private RoutingInstance _currentRoutingInstance;

  private SnmpCommunity _currentSnmpCommunity;

  private SnmpServer _currentSnmpServer;

  private Srlg _currentSrlg;

  private StaticRoute _currentStaticRoute;

  private TacplusServer _currentTacplusServer;

  private Zone _currentToZone;

  private VrrpGroup _currentVrrpGroup;

  private Vlan _currentNamedVlan;

  private Zone _currentZone;

  private ConcreteFirewallFilter _currentZoneInboundFilter;

  private String _currentZoneInterface;

  private LineAction _defaultCrossZoneAction;

  private int _disjunctionPolicyIndex;

  private boolean _hasZones;

  private FlatJuniperCombinedParser _parser;

  private final Map<PsTerm, RouteFilter> _termRouteFilters;

  private final String _text;

  private final Warnings _w;

  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  private ApplicationSet _currentApplicationSet;

  private NssaSettings _currentNssaSettings;

  private StubSettings _currentStubSettings;

  private AsPathGroup _currentAsPathGroup;

  private LogicalSystem _currentLogicalSystem;

  private final Map<Token, String> _tokenInputs;

  private Resolution _currentResolution;

  private ResolutionRib _currentResolutionRib;

  private BridgeDomain _currentBridgeDomain;

  private TunnelAttribute _currentTunnelAttribute;

  public ConfigurationBuilder(
      FlatJuniperCombinedParser parser,
      String text,
      Warnings warnings,
      Map<Token, String> tokenInputs,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _configuration = new JuniperConfiguration();
    _configuration.setExtraLines(_parser.getExtraLines());
    setLogicalSystem(_configuration.getMasterLogicalSystem());
    _termRouteFilters = new HashMap<>();
    _w = warnings;
    _conjunctionPolicyIndex = 0;
    _disjunctionPolicyIndex = 0;
    _tokenInputs = tokenInputs;
    _silentSyntax = silentSyntax;
  }

  private void setLogicalSystem(LogicalSystem logicalSystem) {
    _currentLogicalSystem = logicalSystem;
    _currentRoutingInstance = _currentLogicalSystem.getDefaultRoutingInstance();
  }

  @Override
  public void enterA_application(A_applicationContext ctx) {
    String name = toString(ctx.name);
    _currentApplication =
        _currentLogicalSystem
            .getApplications()
            .computeIfAbsent(name, n -> new BaseApplication(name, false));
    _currentApplicationTerm = _currentApplication.getMainTerm();
    _configuration.defineFlattenedStructure(APPLICATION, name, ctx, _parser);
  }

  @Override
  public void enterA_application_set(A_application_setContext ctx) {
    String name = toString(ctx.name);
    _currentApplicationSet =
        _currentLogicalSystem
            .getApplicationSets()
            .computeIfAbsent(name, n -> new ApplicationSet(name, false));
    _configuration.defineFlattenedStructure(APPLICATION_SET, name, ctx, _parser);
  }

  @Override
  public void exitA_application_set(A_application_setContext ctx) {
    _currentApplicationSet = null;
  }

  @Override
  public void exitAas_application(Aas_applicationContext ctx) {
    if (ctx.junos_application() != null) {
      JunosApplication application = toJunosApplication(ctx.junos_application());
      if (!application.hasDefinition()) {
        _w.redFlagf(
            "unimplemented pre-defined junos application: '%s'", ctx.junos_application().getText());
        return;
      }
      _currentApplicationSet.setMembers(
          ImmutableList.<ApplicationSetMemberReference>builder()
              .addAll(_currentApplicationSet.getMembers())
              .add(new JunosApplicationReference(application))
              .build());
    } else {
      String name = toString(ctx.name);
      int line = getLine(ctx.name.getStart());
      _currentApplicationSet.setMembers(
          ImmutableList.<ApplicationSetMemberReference>builder()
              .addAll(_currentApplicationSet.getMembers())
              .add(new ApplicationOrApplicationSetReference(name))
              .build());
      // only mark the structure as referenced if we know it's not a pre-defined application
      _configuration.referenceStructure(
          APPLICATION_OR_APPLICATION_SET, name, APPLICATION_SET_MEMBER_APPLICATION, line);
    }
  }

  @Override
  public void exitAas_application_set(Aas_application_setContext ctx) {
    if (ctx.junos_application_set() != null) {
      JunosApplicationSet junosApplicationSet = toJunosApplicationSet(ctx.junos_application_set());
      if (!junosApplicationSet.hasDefinition()) {
        _w.redFlagf(
            "unimplemented pre-defined junos application-set: '%s'",
            ctx.junos_application_set().getText());
        return;
      }
      _currentApplicationSet.setMembers(
          ImmutableList.<ApplicationSetMemberReference>builder()
              .addAll(_currentApplicationSet.getMembers())
              .add(new JunosApplicationSetReference(junosApplicationSet))
              .build());
    } else {
      String name = toString(ctx.name);
      int line = getLine(ctx.name.getStart());
      _currentApplicationSet.setMembers(
          ImmutableList.<ApplicationSetMemberReference>builder()
              .addAll(_currentApplicationSet.getMembers())
              .add(new ApplicationSetReference(name))
              .build());
      // only mark the structure as referenced if we know it's not a pre-defined application-set
      _configuration.referenceStructure(
          APPLICATION_SET, name, APPLICATION_SET_MEMBER_APPLICATION_SET, line);
    }
  }

  @Override
  public void enterAa_term(Aa_termContext ctx) {
    String name = toString(ctx.name);
    _currentApplicationTerm =
        _currentApplication.getTerms().computeIfAbsent(name, n -> new Term(name));
  }

  @Override
  public void enterB_allow(B_allowContext ctx) {
    if (_currentBgpGroup.getGroupName() != null) {
      _configuration.referenceStructure(
          BGP_GROUP, _currentBgpGroup.getGroupName(), BGP_ALLOW, getLine(ctx.getStart()));
    }
    if (ctx.ipv6_prefix() != null) {
      _currentBgpGroup.setIpv6(true);
      _currentBgpGroup = DUMMY_BGP_GROUP;
      // not supported for now
      return;
    }
    Prefix remotePrefix = Prefix.ZERO; // equivalent to ALL
    if (ctx.ip_prefix() != null) {
      remotePrefix = toPrefix(ctx.ip_prefix());
    }
    Map<Prefix, IpBgpGroup> ipBgpGroups = _currentRoutingInstance.getIpBgpGroups();
    IpBgpGroup ipBgpGroup = ipBgpGroups.get(remotePrefix);
    if (ipBgpGroup == null) {
      ipBgpGroup = new IpBgpGroup(remotePrefix);
      ipBgpGroup.setParent(_currentBgpGroup);
      ipBgpGroups.put(remotePrefix, ipBgpGroup);
      ipBgpGroup.setDynamic(true);
    }
    _currentBgpGroup = ipBgpGroup;
  }

  @Override
  public void enterB_group(B_groupContext ctx) {
    String name = toString(ctx.name);
    Map<String, NamedBgpGroup> namedBgpGroups = _currentRoutingInstance.getNamedBgpGroups();
    NamedBgpGroup namedBgpGroup = namedBgpGroups.get(name);
    if (namedBgpGroup == null) {
      namedBgpGroup = new NamedBgpGroup(name);
      namedBgpGroup.setParent(_currentBgpGroup);
      namedBgpGroups.put(name, namedBgpGroup);
    }
    _currentBgpGroup = namedBgpGroup;
    _configuration.defineFlattenedStructure(BGP_GROUP, name, ctx, _parser);
  }

  @Override
  public void enterB_neighbor(B_neighborContext ctx) {
    if (_currentBgpGroup.getGroupName() != null) {
      _configuration.referenceStructure(
          BGP_GROUP,
          _currentBgpGroup.getGroupName(),
          BGP_NEIGHBOR,
          getLine(ctx.NEIGHBOR().getSymbol()));
    }
    if (ctx.ip_address() != null) {
      Ip remoteIp = toIp(ctx.ip_address());
      Prefix remotePrefix = remoteIp.toPrefix();
      Map<Prefix, IpBgpGroup> ipBgpGroups = _currentRoutingInstance.getIpBgpGroups();
      IpBgpGroup ipBgpGroup = ipBgpGroups.get(remotePrefix);
      if (ipBgpGroup == null) {
        ipBgpGroup = new IpBgpGroup(remotePrefix);
        ipBgpGroup.setParent(_currentBgpGroup);
        ipBgpGroups.put(remotePrefix, ipBgpGroup);
      }
      _currentBgpGroup = ipBgpGroup;
      String bgpNeighborStructureName =
          bgpNeighborStructureName(remoteIp.toString(), _currentRoutingInstance.getName());
      _configuration.defineFlattenedStructure(
          JuniperStructureType.BGP_NEIGHBOR, bgpNeighborStructureName, ctx, _parser);
      _configuration.referenceStructure(
          JuniperStructureType.BGP_NEIGHBOR,
          bgpNeighborStructureName,
          BGP_NEIGHBOR_SELF_REFERENCE,
          getLine(ctx.start));
    } else if (ctx.ipv6_address() != null) {
      _currentBgpGroup.setIpv6(true);
      _currentBgpGroup = DUMMY_BGP_GROUP;
      String bgpNeighborStructureName =
          bgpNeighborStructureName(
              toIp6(ctx.ipv6_address()).toString(), _currentRoutingInstance.getName());
      _configuration.defineFlattenedStructure(
          JuniperStructureType.BGP_NEIGHBOR, bgpNeighborStructureName, ctx, _parser);
      _configuration.referenceStructure(
          JuniperStructureType.BGP_NEIGHBOR,
          bgpNeighborStructureName,
          BGP_NEIGHBOR_SELF_REFERENCE,
          getLine(ctx.start));
    }
  }

  @Override
  public void enterB_keep(B_keepContext ctx) {
    BgpGroup.BgpKeepType keep =
        ctx.NONE() != null ? BgpGroup.BgpKeepType.NONE : BgpGroup.BgpKeepType.ALL;
    _currentBgpGroup.setKeep(keep);
  }

  @Override
  public void enterF_family(F_familyContext ctx) {
    _currentFirewallFamily = toFamily(ctx);
  }

  @Override
  public void enterF_filter(F_filterContext ctx) {
    if (_currentFirewallFamily == null) {
      _currentFirewallFamily = Family.INET;
    }
    String name = toString(ctx.name);
    Map<String, FirewallFilter> filters = _currentLogicalSystem.getFirewallFilters();
    _currentFilter = (ConcreteFirewallFilter) filters.get(name);
    if (_currentFilter == null) {
      _currentFilter = new ConcreteFirewallFilter(name, _currentFirewallFamily);
      filters.put(name, _currentFilter);
    }
    JuniperStructureType filterType =
        _currentFirewallFamily == Family.INET6 ? FIREWALL_INET6_FILTER : FIREWALL_FILTER;
    _configuration.defineFlattenedStructure(filterType, name, ctx, _parser);
  }

  @Override
  public void enterFf_term(Ff_termContext ctx) {
    String name = toString(ctx.name);
    String defName = computeFirewallFilterTermName(_currentFilter.getName(), name);
    Map<String, FwTerm> terms = _currentFilter.getTerms();
    _currentFwTerm = terms.computeIfAbsent(name, FwTerm::new);
    _configuration.defineFlattenedStructure(FIREWALL_FILTER_TERM, defName, ctx, _parser);
    _configuration.referenceStructure(
        FIREWALL_FILTER_TERM, defName, FIREWALL_FILTER_TERM_DEFINITION, getLine(ctx.name.start));
  }

  @Override
  public void exitF_interface_set(FlatJuniperParser.F_interface_setContext ctx) {
    String interfaceSetName = toString(ctx.set_name);
    _configuration.defineFlattenedStructure(FIREWALL_INTERFACE_SET, interfaceSetName, ctx, _parser);
    InterfaceSet interfaceSet =
        _currentLogicalSystem
            .getInterfaceSets()
            .computeIfAbsent(interfaceSetName, k -> new InterfaceSet());
    if (ctx.iface_name != null) {
      String interfaceName = getInterfaceFullName(ctx.iface_name);
      interfaceSet.addInterface(interfaceName);
      _configuration.referenceStructure(
          INTERFACE,
          interfaceName,
          FIREWALL_INTERFACE_SET_MEMBER,
          getLine(ctx.iface_name.getStop()));
    } else {
      assert ctx.iface_wildcard != null;
      warn(ctx, "Interface wildcards are not yet supported in interface-set");
    }
  }

  @Override
  public void exitFo_vxlan_routing(Fo_vxlan_routingContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitFftt_decapsulate(Fftt_decapsulateContext ctx) {
    todo(ctx);
  }

  @Override
  public void enterFo_dhcp_relay(Fo_dhcp_relayContext ctx) {
    _currentDhcpRelayGroup =
        _currentRoutingInstance
            .getDhcpRelayGroups()
            .computeIfAbsent(
                DhcpRelayGroup.MASTER_DHCP_RELAY_GROUP_NAME, n -> new DhcpRelayGroup());
  }

  @Override
  public void enterFod_group(Fod_groupContext ctx) {
    String name = toString(ctx.name);
    _currentDhcpRelayGroup =
        _currentRoutingInstance
            .getDhcpRelayGroups()
            .computeIfAbsent(name, n -> new DhcpRelayGroup());
  }

  @Override
  public void enterFod_server_group(Fod_server_groupContext ctx) {
    String name = toString(ctx.name);
    _currentDhcpRelayServerGroup =
        _currentRoutingInstance
            .getDhcpRelayServerGroups()
            .computeIfAbsent(name, n -> new DhcpRelayServerGroup());
    _configuration.defineFlattenedStructure(DHCP_RELAY_SERVER_GROUP, name, ctx, _parser);
  }

  @Override
  public void exitFod_server_group(Fod_server_groupContext ctx) {
    _currentDhcpRelayServerGroup = null;
  }

  @Override
  public void exitFods_address(Fods_addressContext ctx) {
    _currentDhcpRelayServerGroup.getServers().add(toIp(ctx.address));
  }

  @Override
  public void enterI_unit(I_unitContext ctx) {
    String unit = ctx.num.getText();
    String unitFullName = _currentMasterInterface.getName() + "." + unit;
    Map<String, Interface> units = _currentMasterInterface.getUnits();
    _currentInterfaceOrRange = units.get(unitFullName);
    if (_currentInterfaceOrRange == null) {
      _currentInterfaceOrRange = new Interface(unitFullName);
      _currentInterfaceOrRange.setRoutingInstance(
          _currentLogicalSystem.getDefaultRoutingInstance());
      _currentInterfaceOrRange.setParent(_currentMasterInterface);
      units.put(unitFullName, _currentInterfaceOrRange);
    }
    _currentInterfaceOrRange.setDefined(true);
    _configuration.defineFlattenedStructure(INTERFACE, unitFullName, ctx, _parser);
    _configuration.referenceStructure(
        INTERFACE, unitFullName, INTERFACE_SELF_REFERENCE, getLine(ctx.num.start));
  }

  @Override
  public void enterIf_ethernet_switching(If_ethernet_switchingContext ctx) {
    _currentInterfaceOrRange.initEthernetSwitching();
  }

  @Override
  public void enterIf_primary(If_primaryContext ctx) {
    if (_currentInterfaceOrRange instanceof InterfaceRange) {
      warn(ctx, "Interface ranges cannot be declared primary");
      return;
    }
    Optional<Interface> currentPrimary =
        _currentLogicalSystem.getInterfaces().values().stream()
            .flatMap(iface -> iface.getUnits().values().stream())
            .filter(Interface::getPrimary)
            .findAny();
    if (currentPrimary.isPresent()
        && !currentPrimary.get().getName().equals(_currentInterfaceOrRange.getName())) {
      warn(
          ctx,
          String.format(
              "Cannot make %s as the primary interface. %s is already configured as primary.",
              _currentInterfaceOrRange.getName(), currentPrimary.get().getName()));
      return;
    }
    _currentInterfaceOrRange.setPrimary(true);
  }

  @Override
  public void enterIfi_address(Ifi_addressContext ctx) {
    Set<ConcreteInterfaceAddress> allAddresses = _currentInterfaceOrRange.getAllAddresses();
    ConcreteInterfaceAddress address;
    if (ctx.ip_prefix() != null) {
      address = toConcreteInterfaceAddress(ctx.ip_prefix());
    } else if (ctx.ip_address() != null) {
      Ip ip = toIp(ctx.ip_address());
      address = ConcreteInterfaceAddress.create(ip, Prefix.MAX_PREFIX_LENGTH);
    } else {
      throw new BatfishException("Invalid or missing address");
    }
    _currentInterfaceAddress = address;
    if (_currentInterfaceOrRange.getPrimaryAddress() == null) {
      _currentInterfaceOrRange.setPrimaryAddress(address);
    }
    if (_currentInterfaceOrRange.getPreferredAddress() == null) {
      _currentInterfaceOrRange.setPreferredAddress(address);
    }
    allAddresses.add(address);
    Ip ip = address.getIp();
    _currentInterfaceOrRange.getAllAddressIps().add(ip);
  }

  @Override
  public void enterIfia_vrrp_group(Ifia_vrrp_groupContext ctx) {
    int vrid = toInt(ctx.number);
    VrrpGroup currentVrrpGroup = _currentInterfaceOrRange.getVrrpGroups().get(vrid);
    if (currentVrrpGroup == null) {
      currentVrrpGroup = new VrrpGroup(_currentInterfaceAddress);
      currentVrrpGroup.setPreempt(DEFAULT_VRRP_PREEMPT);
      currentVrrpGroup.setPriority(DEFAULT_VRRP_PRIORITY);
      _currentInterfaceOrRange.getVrrpGroups().put(vrid, currentVrrpGroup);
    } else if (!currentVrrpGroup.getSourceAddress().equals(_currentInterfaceAddress)) {
      // Note that we are keeping the VRRP configuration for the first source-address we encounter
      // using this VRID.
      _w.fatalRedFlag(
          "Multiple inet addresses with the same VRRP VRID %d on interface '%s'",
          vrid, _currentInterfaceOrRange.getName());
      currentVrrpGroup = new VrrpGroup(_currentInterfaceAddress); // dummy
    }
    _currentVrrpGroup = currentVrrpGroup;
  }

  @Override
  public void enterInt_interface_range(Int_interface_rangeContext ctx) {
    String name = toString(ctx.irange);
    InterfaceRange currentInterfaceRange = _currentLogicalSystem.getInterfaceRanges().get(name);
    if (currentInterfaceRange == null) {
      currentInterfaceRange =
          _currentLogicalSystem.getInterfaceRanges().computeIfAbsent(name, InterfaceRange::new);
      currentInterfaceRange.setRoutingInstance(_currentLogicalSystem.getDefaultRoutingInstance());
      currentInterfaceRange.setParent(_currentLogicalSystem.getGlobalMasterInterface());
    }
    currentInterfaceRange.setDefined(true);
    _currentInterfaceOrRange = currentInterfaceRange;
  }

  @Override
  public void exitInt_interface_range(Int_interface_rangeContext ctx) {
    _currentInterfaceOrRange = null;
  }

  @Override
  public void enterInt_named(Int_namedContext ctx) {
    Interface currentInterface;
    if (ctx.interface_id() == null) {
      currentInterface = _currentLogicalSystem.getGlobalMasterInterface();
    } else {
      InterfaceId interfaceId = toInterfaceId(ctx.interface_id());
      String ifaceName = interfaceId.getParentName();
      Map<String, Interface> interfaces;
      String nodeDevicePrefix = "";
      if (interfaceId.getNode() == null) {
        interfaces = _currentLogicalSystem.getInterfaces();
      } else {
        String nodeDeviceName = interfaceId.getNode();
        nodeDevicePrefix = nodeDeviceName + ":";
        NodeDevice nodeDevice =
            _configuration.getNodeDevices().computeIfAbsent(nodeDeviceName, n -> new NodeDevice());
        interfaces = nodeDevice.getInterfaces();
      }
      currentInterface = interfaces.get(ifaceName);
      if (currentInterface == null) {
        String fullIfaceName = nodeDevicePrefix + ifaceName;
        currentInterface = new Interface(fullIfaceName);
        currentInterface.setRoutingInstance(_currentLogicalSystem.getDefaultRoutingInstance());
        currentInterface.setParent(_currentLogicalSystem.getGlobalMasterInterface());
        interfaces.put(fullIfaceName, currentInterface);
      }
      currentInterface.setDefined(true);
      _configuration.defineFlattenedStructure(INTERFACE, currentInterface.getName(), ctx, _parser);
      _configuration.referenceStructure(
          INTERFACE, currentInterface.getName(), INTERFACE_SELF_REFERENCE, getLine(ctx.getStart()));
    }
    _currentInterfaceOrRange = currentInterface;
    _currentMasterInterface = currentInterface;
  }

  @Override
  public void exitSepctxptp_application_services(Sepctxptp_application_servicesContext ctx) {
    todo(ctx);
  }

  @Override
  public void enterIs_interface(Is_interfaceContext ctx) {
    if (ctx.ALL() != null) {
      _currentIsisInterfaceSettings = _currentRoutingInstance.getInterfaceAllIsisSettings();
      if (_currentIsisInterfaceSettings == null) {
        _currentIsisInterfaceSettings = new IsisInterfaceSettings();
        _currentRoutingInstance.setInterfaceAllIsisSettings(_currentIsisInterfaceSettings);
      }
    } else {
      Interface iface = initRoutingInterface(ctx.id);
      _currentIsisInterfaceSettings = iface.getOrInitIsisSettings();
      _configuration.referenceStructure(
          INTERFACE, iface.getName(), ISIS_INTERFACE, getLine(ctx.id.getStop()));
    }
  }

  @Override
  public void enterIs_level(Is_levelContext ctx) {
    IsisSettings isisSettings = _currentRoutingInstance.getIsisSettings();
    int level = toInt(ctx.dec());
    switch (level) {
      case 1:
        _currentIsisLevelSettings = isisSettings.getLevel1Settings();
        break;
      case 2:
        _currentIsisLevelSettings = isisSettings.getLevel2Settings();
        break;
      default:
        throw new BatfishException("invalid level: " + level);
    }
  }

  @Override
  public void enterIsi_level(Isi_levelContext ctx) {
    int level = toInt(ctx.dec());
    switch (level) {
      case 1:
        _currentIsisInterfaceLevelSettings = _currentIsisInterfaceSettings.getLevel1Settings();
        break;
      case 2:
        _currentIsisInterfaceLevelSettings = _currentIsisInterfaceSettings.getLevel2Settings();
        break;
      default:
        throw new BatfishException("invalid IS-IS level: " + level);
    }
  }

  @Override
  public void enterNat_pool(Nat_poolContext ctx) {
    String poolName = toString(ctx.name);
    _currentNatPool = _currentNat.getPools().computeIfAbsent(poolName, p -> new NatPool());
    _configuration.defineFlattenedStructure(NAT_POOL, poolName, ctx, _parser);
  }

  @Override
  public void exitNat_pool(Nat_poolContext ctx) {
    _currentNatPool = null;
  }

  @Override
  public void enterNat_rule_set(Nat_rule_setContext ctx) {
    String rulesetName = toString(ctx.name);
    _currentNatRuleSet =
        _currentNat.getRuleSets().computeIfAbsent(rulesetName, k -> new NatRuleSet(rulesetName));
    _configuration.defineFlattenedStructure(NAT_RULE_SET, rulesetName, ctx, _parser);
  }

  @Override
  public void exitNat_rule_set(Nat_rule_setContext ctx) {
    _currentNatRuleSet = null;
  }

  @Override
  public void enterO_area(O_areaContext ctx) {
    long area;
    if (ctx.area_int != null) {
      area = toLong(ctx.area_int);
    } else if (ctx.area_ip != null) {
      area = toIp(ctx.area_ip).asLong();
    } else {
      throw new BatfishException("Missing area");
    }
    Map<Long, OspfArea> areas = _currentRoutingInstance.getOspfAreas();
    _currentArea = areas.computeIfAbsent(area, OspfArea::new);
  }

  @Override
  public void enterOa_area_range(FlatJuniperParser.Oa_area_rangeContext ctx) {
    // Set up defaults: no overridden metric, routes advertised.
    _currentAreaRangeMetric = null;
    _currentAreaRangePrefix = null;
    _currentAreaRangeRestrict = false;

    if (ctx.prefix != null) {
      _currentAreaRangePrefix = toPrefix(ctx.prefix);
    } else {
      todo(ctx);
    }
  }

  @Override
  public void enterOa_interface(Oa_interfaceContext ctx) {
    Map<String, Interface> interfaces = _currentLogicalSystem.getInterfaces();
    String ospfInterfaceName = null;
    if (ctx.ALL() != null) {
      ospfInterfaceName = "all";
      _currentOspfSettings = _currentRoutingInstance.getInterfaceAllOspfSettings();
      if (_currentOspfSettings == null) {
        _currentOspfSettings = new OspfInterfaceSettings(Ip.create(_currentArea.getName()));
        _currentRoutingInstance.setInterfaceAllOspfSettings(_currentOspfSettings);
      }
    } else if (ctx.ip != null) {
      Ip ip = toIp(ctx.ip);
      for (Interface iface : interfaces.values()) {
        for (Interface unit : iface.getUnits().values()) {
          if (unit.getAllAddressIps().contains(ip)) {
            _currentOspfSettings = unit.getOspfSettings();
            if (_currentOspfSettings == null) {
              _currentOspfSettings = new OspfInterfaceSettings(Ip.create(_currentArea.getName()));
              unit.setOspfSettings(_currentOspfSettings);
            }
            ospfInterfaceName = unit.getName();
          }
        }
      }
      if (_currentOspfSettings == null) {
        warn(ctx, "Could not find interface with ip address: " + ip);
        // create dummy object to store what follows; this dangling object will be ignored
        _currentOspfSettings = new OspfInterfaceSettings(Ip.ZERO);
        return;
      }
    } else {
      Interface iface = initRoutingInterface(ctx.id);
      _currentOspfSettings = iface.getOspfSettings();
      if (_currentOspfSettings == null) {
        _currentOspfSettings = new OspfInterfaceSettings(Ip.create(_currentArea.getName()));
        iface.setOspfSettings(_currentOspfSettings);
      }
      ospfInterfaceName = iface.getName();
      _configuration.referenceStructure(
          INTERFACE, ospfInterfaceName, OSPF_AREA_INTERFACE, getLine(ctx.id.getStop()));
    }
    Ip currentArea = Ip.create(_currentArea.getName());
    Ip currentInterfaceArea = _currentOspfSettings.getOspfArea();
    if (!currentArea.equals(currentInterfaceArea)) {
      _w.fatalRedFlag("Interface \"%s\" assigned to multiple areas", ospfInterfaceName);
      return;
    }
  }

  @Override
  public void enterOa_nssa(Oa_nssaContext ctx) {
    _currentNssaSettings = _currentArea.getNssaSettings();
    if (_currentNssaSettings == null) {
      _currentNssaSettings = new NssaSettings();
      _currentArea.setNssaSettings(_currentNssaSettings);
    }
    _currentArea.setStubType(StubType.NSSA);
  }

  @Override
  public void exitOa_nssa(Oa_nssaContext ctx) {
    _currentNssaSettings = null;
  }

  @Override
  public void enterOa_stub(Oa_stubContext ctx) {
    _currentStubSettings = _currentArea.getStubSettings();
    if (_currentStubSettings == null) {
      _currentStubSettings = new StubSettings();
      _currentArea.setStubSettings(_currentStubSettings);
    }
    _currentArea.setStubType(StubType.STUB);
  }

  @Override
  public void exitOa_stub(Oa_stubContext ctx) {
    _currentStubSettings = null;
  }

  @Override
  public void exitOai_passive(Oai_passiveContext ctx) {
    _currentOspfSettings.setOspfPassive(true);
  }

  @Override
  public void enterOan_default_lsa(Oan_default_lsaContext ctx) {
    if (_currentNssaSettings.getDefaultLsaType() == OspfDefaultOriginateType.NONE) {
      _currentNssaSettings.setDefaultLsaType(OspfDefaultOriginateType.INTER_AREA);
    }
  }

  @Override
  public void exitOand_metric_type(Oand_metric_typeContext ctx) {
    if (ctx.METRIC_TYPE_1() != null) {
      _currentNssaSettings.setDefaultLsaType(OspfDefaultOriginateType.EXTERNAL_TYPE1);
    }
    if (ctx.METRIC_TYPE_2() != null) {
      _currentNssaSettings.setDefaultLsaType(OspfDefaultOriginateType.EXTERNAL_TYPE2);
    }
  }

  @Override
  public void exitOand_type_7(Oand_type_7Context ctx) {
    if (_currentNssaSettings.getDefaultLsaType() != OspfDefaultOriginateType.EXTERNAL_TYPE1) {
      _currentNssaSettings.setDefaultLsaType(OspfDefaultOriginateType.EXTERNAL_TYPE2);
    }
  }

  @Override
  public void exitOan_no_summaries(Oan_no_summariesContext ctx) {
    _currentNssaSettings.setNoSummaries(true);
  }

  @Override
  public void exitOas_no_summaries(Oas_no_summariesContext ctx) {
    _currentStubSettings.setNoSummaries(true);
  }

  @Override
  public void exitOas_default_metric(Oas_default_metricContext ctx) {
    _currentArea.setInjectDefaultRoute(true);
    _currentArea.setMetricOfDefaultRoute(toInt(ctx.dec()));
  }

  @Override
  public void enterP_bgp(P_bgpContext ctx) {
    _currentBgpGroup = _currentRoutingInstance.getMasterBgpGroup();
  }

  @Override
  public void enterP_evpn(P_evpnContext ctx) {
    if (_currentLogicalSystem.getEvpn() == null) {
      _currentLogicalSystem.setEvpn(new Evpn());
    }
  }

  @Override
  public void exitPo_as_path(Po_as_pathContext ctx) {
    String name = toString(ctx.name);
    _configuration.defineFlattenedStructure(AS_PATH, name, ctx, _parser);
    _currentLogicalSystem
        .getAsPaths()
        .put(
            name, new org.batfish.representation.juniper.AsPath(unquote(ctx.regex.getText(), ctx)));
  }

  @Override
  public void enterPo_as_path_group(Po_as_path_groupContext ctx) {
    String name = toString(ctx.name);
    _configuration.defineFlattenedStructure(AS_PATH_GROUP, name, ctx, _parser);
    _currentAsPathGroup =
        _currentLogicalSystem.getAsPathGroups().computeIfAbsent(name, AsPathGroup::new);
  }

  @Override
  public void exitPo_as_path_group(Po_as_path_groupContext ctx) {
    _currentAsPathGroup = null;
  }

  @Override
  public void exitPoapg_as_path(Poapg_as_pathContext ctx) {
    String name = toString(ctx.name);
    _configuration.defineFlattenedStructure(AS_PATH_GROUP_AS_PATH, name, ctx, _parser);
    _configuration.referenceStructure(
        AS_PATH_GROUP_AS_PATH,
        name,
        AS_PATH_GROUP_AS_PATH_SELF_REFERENCE,
        getLine(ctx.name.getStart()));
    String asPathStr = unquote(ctx.regex.getText(), ctx);
    // intentional overwrite
    _currentAsPathGroup.getAsPaths().put(name, new NamedAsPath(name, asPathStr));
  }

  @Override
  public void enterPo_community(Po_communityContext ctx) {
    String name = toString(ctx.name);
    _configuration.defineFlattenedStructure(COMMUNITY, name, ctx, _parser);
    Map<String, NamedCommunity> communityLists = _currentLogicalSystem.getNamedCommunities();
    _currentCommunityList = communityLists.computeIfAbsent(name, NamedCommunity::new);
  }

  @Override
  public void enterPo_policy_statement(Po_policy_statementContext ctx) {
    String name = toString(ctx.name);
    Map<String, PolicyStatement> policyStatements = _currentLogicalSystem.getPolicyStatements();
    _currentPolicyStatement = policyStatements.computeIfAbsent(name, PolicyStatement::new);
    _currentPsTerm = _currentPolicyStatement.getDefaultTerm();
    _currentPsThens = _currentPsTerm.getThens();
    _configuration.defineFlattenedStructure(POLICY_STATEMENT, name, ctx, _parser);
  }

  @Override
  public void enterPo_prefix_list(Po_prefix_listContext ctx) {
    String name = toString(ctx.name);
    Map<String, PrefixList> prefixLists = _currentLogicalSystem.getPrefixLists();
    _currentPrefixList = prefixLists.computeIfAbsent(name, PrefixList::new);
    _configuration.defineFlattenedStructure(PREFIX_LIST, name, ctx, _parser);
  }

  @Override
  public void enterPo_rtf_prefix_list(Po_rtf_prefix_listContext ctx) {
    String name = toString(ctx.name);
    _configuration.defineFlattenedStructure(RTF_PREFIX_LIST, name, ctx, _parser);
    todo(ctx);
  }

  @Override
  public void enterPops_term(Pops_termContext ctx) {
    String name = toString(ctx.name);
    Map<String, PsTerm> terms = _currentPolicyStatement.getTerms();
    _currentPsTerm = terms.computeIfAbsent(name, PsTerm::new);
    _currentPsThens = _currentPsTerm.getThens();

    String termName = computePolicyStatementTermName(_currentPolicyStatement.getName(), name);
    _configuration.defineFlattenedStructure(POLICY_STATEMENT_TERM, termName, ctx, _parser);
    _configuration.referenceStructure(
        POLICY_STATEMENT_TERM, termName, POLICY_STATEMENT_TERM_DEFINITION, getLine(ctx.name.start));
  }

  @Override
  public void enterPopsf_route_filter(Popsf_route_filterContext ctx) {
    _currentRouteFilter = _termRouteFilters.get(_currentPsTerm);
    if (_currentRouteFilter == null) {
      String rfName = _currentPolicyStatement.getName() + ":" + _currentPsTerm.getName();
      _currentRouteFilter = new RouteFilter();
      _termRouteFilters.put(_currentPsTerm, _currentRouteFilter);
      _currentLogicalSystem.getRouteFilters().put(rfName, _currentRouteFilter);
      PsFromRouteFilter from = new PsFromRouteFilter(rfName);
      _currentPsTerm.getFroms().addFromRouteFilter(from);
    }
    if (ctx.prefix != null) {
      _currentRouteFilterPrefix = toPrefix(ctx.prefix);
      _currentRouteFilter.setIpv4(true);
    } else if (ctx.prefix6 != null) {
      _currentRoute6FilterPrefix = toPrefix6(ctx.prefix6);
      _currentRouteFilter.setIpv6(true);
    }
  }

  @Override
  public void exitPopsfrf_address_mask(Popsfrf_address_maskContext ctx) {
    if (_currentRouteFilterPrefix != null) { // ipv4
      if (ctx.ip_address() != null) {
        Route4FilterLine line =
            new Route4FilterLineAddressMask(
                _currentRouteFilterPrefix, toIp(ctx.ip_address()).inverted());
        _currentRouteFilterLine = _currentRouteFilter.insertLine(line, Route4FilterLine.class);
      } else {
        _w.redFlagf(
            "Route filter mask does not match version for prefix %s", _currentRouteFilterPrefix);
      }
    } else if (_currentRoute6FilterPrefix != null) { // ipv6
      if (ctx.ipv6_address() != null) {
        Route6FilterLine line =
            new Route6FilterLineAddressMask(
                _currentRoute6FilterPrefix, toIp6(ctx.ipv6_address()).inverted());
        _currentRoute6FilterLine = _currentRouteFilter.insertLine(line, Route6FilterLine.class);
      } else {
        _w.redFlagf(
            "Route filter mask does not match version for prefix %s", _currentRouteFilterPrefix);
      }
    }
  }

  @Override
  public void enterPopsfrf_exact(Popsfrf_exactContext ctx) {
    if (_currentRouteFilterPrefix != null) { // ipv4
      Route4FilterLine line = new Route4FilterLineExact(_currentRouteFilterPrefix);
      _currentRouteFilterLine = _currentRouteFilter.insertLine(line, Route4FilterLine.class);
    } else if (_currentRoute6FilterPrefix != null) { // ipv6
      Route6FilterLine line = new Route6FilterLineExact(_currentRoute6FilterPrefix);
      _currentRoute6FilterLine = _currentRouteFilter.insertLine(line, Route6FilterLine.class);
    }
  }

  @Override
  public void enterPopsfrf_longer(Popsfrf_longerContext ctx) {
    if (_currentRouteFilterPrefix != null) { // ipv4
      Route4FilterLine line = new Route4FilterLineLonger(_currentRouteFilterPrefix);
      _currentRouteFilterLine = _currentRouteFilter.insertLine(line, Route4FilterLine.class);
    } else if (_currentRoute6FilterPrefix != null) { // ipv6
      Route6FilterLine line = new Route6FilterLineLonger(_currentRoute6FilterPrefix);
      _currentRoute6FilterLine = _currentRouteFilter.insertLine(line, Route6FilterLine.class);
    }
  }

  @Override
  public void enterPopsfrf_orlonger(Popsfrf_orlongerContext ctx) {
    if (_currentRouteFilterPrefix != null) { // ipv4
      Route4FilterLine line = new Route4FilterLineOrLonger(_currentRouteFilterPrefix);
      _currentRouteFilterLine = _currentRouteFilter.insertLine(line, Route4FilterLine.class);
    } else if (_currentRoute6FilterPrefix != null) { // ipv6
      Route6FilterLine line = new Route6FilterLineOrLonger(_currentRoute6FilterPrefix);
      _currentRoute6FilterLine = _currentRouteFilter.insertLine(line, Route6FilterLine.class);
    }
  }

  @Override
  public void enterPopsfrf_prefix_length_range(Popsfrf_prefix_length_rangeContext ctx) {
    int minPrefixLength = toInt(ctx.low);
    int maxPrefixLength = toInt(ctx.high);
    if (_currentRouteFilterPrefix != null) { // ipv4
      Route4FilterLine line =
          new Route4FilterLineLengthRange(
              _currentRouteFilterPrefix, minPrefixLength, maxPrefixLength);
      _currentRouteFilterLine = _currentRouteFilter.insertLine(line, Route4FilterLine.class);
    } else if (_currentRoute6FilterPrefix != null) { // ipv6
      Route6FilterLine line =
          new Route6FilterLineLengthRange(
              _currentRoute6FilterPrefix, minPrefixLength, maxPrefixLength);
      _currentRoute6FilterLine = _currentRouteFilter.insertLine(line, Route6FilterLine.class);
    }
  }

  @Override
  public void enterPopsfrf_then(Popsfrf_thenContext ctx) {
    if (_currentRouteFilterPrefix != null) { // ipv4
      Route4FilterLine line = _currentRouteFilterLine;
      _currentPsThens = line.getThens();
    } else if (_currentRoute6FilterPrefix != null) { // ipv6
      Route6FilterLine line = _currentRoute6FilterLine;
      _currentPsThens = line.getThens();
    }
  }

  @Override
  public void enterPopsfrf_through(Popsfrf_throughContext ctx) {
    if (_currentRouteFilterPrefix != null) { // ipv4
      Prefix throughPrefix = toPrefix(ctx.prefix);
      Route4FilterLine line = new Route4FilterLineThrough(_currentRouteFilterPrefix, throughPrefix);
      _currentRouteFilterLine = _currentRouteFilter.insertLine(line, Route4FilterLine.class);
    } else if (_currentRoute6FilterPrefix != null) { // ipv6
      Prefix6 throughPrefix6 = toPrefix6(ctx.prefix6);
      Route6FilterLine line =
          new Route6FilterLineThrough(_currentRoute6FilterPrefix, throughPrefix6);
      _currentRoute6FilterLine = _currentRouteFilter.insertLine(line, Route6FilterLine.class);
    }
  }

  @Override
  public void enterPopsfrf_upto(Popsfrf_uptoContext ctx) {
    int maxPrefixLength = toInt(ctx.high);
    if (_currentRouteFilterPrefix != null) { // ipv4
      Route4FilterLine line = new Route4FilterLineUpTo(_currentRouteFilterPrefix, maxPrefixLength);
      _currentRouteFilterLine = _currentRouteFilter.insertLine(line, Route4FilterLine.class);
    } else if (_currentRoute6FilterPrefix != null) { // ipv6
      Route6FilterLine line = new Route6FilterLineUpTo(_currentRoute6FilterPrefix, maxPrefixLength);
      _currentRoute6FilterLine = _currentRouteFilter.insertLine(line, Route6FilterLine.class);
    }
  }

  @Override
  public void enterRi_named_routing_instance(Ri_named_routing_instanceContext ctx) {
    String name = toString(ctx.name);
    _currentRoutingInstance =
        _currentLogicalSystem.getRoutingInstances().computeIfAbsent(name, RoutingInstance::new);
    _currentRoutingInstance.getGlobalMasterInterface().setParent(_currentMasterInterface);
    _configuration.defineFlattenedStructure(ROUTING_INSTANCE, name, ctx, _parser);
    _configuration.referenceStructure(
        ROUTING_INSTANCE, name, ROUTING_INSTANCE_SELF_REFERENCE, getLine(ctx.name.getStart()));
  }

  @Override
  public void enterRoa_defaults(Roa_defaultsContext ctx) {
    _currentAggregateRoute = _currentRoutingInstance.getAggregateRouteDefaults();
  }

  @Override
  public void exitRoa_defaults(Roa_defaultsContext ctx) {
    _currentAggregateRoute = null;
  }

  @Override
  public void exitRoa_passive(Roa_passiveContext ctx) {
    _currentAggregateRoute.setActive(false);
  }

  @Override
  public void enterRoa_route(Roa_routeContext ctx) {
    if (ctx.prefix != null) {
      Prefix prefix = toPrefix(ctx.prefix);
      Map<Prefix, AggregateRoute> aggregateRoutes = _currentRib.getAggregateRoutes();
      _currentAggregateRoute = aggregateRoutes.computeIfAbsent(prefix, AggregateRoute::new);
    } else {
      _currentAggregateRoute = DUMMY_AGGREGATE_ROUTE;
    }
  }

  @Override
  public void exitRoa_route(Roa_routeContext ctx) {
    _currentAggregateRoute = null;
  }

  @Override
  public void enterRog_defaults(Rog_defaultsContext ctx) {
    _currentGeneratedRoute = _currentRoutingInstance.getGeneratedRouteDefaults();
  }

  @Override
  public void exitRog_defaults(Rog_defaultsContext ctx) {
    _currentGeneratedRoute = null;
  }

  @Override
  public void exitRog_discard(Rog_discardContext ctx) {
    _currentGeneratedRoute.setDrop(true);
  }

  @Override
  public void exitRog_passive(Rog_passiveContext ctx) {
    _currentGeneratedRoute.setActive(false);
  }

  @Override
  public void enterRog_route(Rog_routeContext ctx) {
    if (ctx.prefix != null) {
      Prefix prefix = toPrefix(ctx.prefix);
      Map<Prefix, GeneratedRoute> generatedRoutes = _currentRib.getGeneratedRoutes();
      _currentGeneratedRoute = generatedRoutes.computeIfAbsent(prefix, GeneratedRoute::new);
    } else if (ctx.prefix6 != null) {
      // dummy generated route not added to configuration
      _currentGeneratedRoute = new GeneratedRoute(null);
      todo(ctx);
    }
  }

  @Override
  public void exitRog_route(Rog_routeContext ctx) {
    _currentGeneratedRoute = null;
  }

  @Override
  public void enterRor_inet(Ror_inetContext ctx) {
    String name = toString(ctx.name);
    Map<String, RoutingInformationBase> ribs = _currentRoutingInstance.getRibs();
    _currentRib = ribs.computeIfAbsent(name, RoutingInformationBase::new);
  }

  @Override
  public void enterRor_inet6(Ror_inet6Context ctx) {
    String name = toString(ctx.name);
    Map<String, RoutingInformationBase> ribs = _currentRoutingInstance.getRibs();
    _currentRib = ribs.computeIfAbsent(name, RoutingInformationBase::new);
  }

  @Override
  public void enterRor_iso(Ror_isoContext ctx) {
    String name = toString(ctx.name);
    Map<String, RoutingInformationBase> ribs = _currentRoutingInstance.getRibs();
    _currentRib = ribs.computeIfAbsent(name, RoutingInformationBase::new);
  }

  @Override
  public void enterRor_mpls(Ror_mplsContext ctx) {
    String name = toString(ctx.name);
    Map<String, RoutingInformationBase> ribs = _currentRoutingInstance.getRibs();
    _currentRib = ribs.computeIfAbsent(name, RoutingInformationBase::new);
  }

  @Override
  public void enterRo_rib_groups(Ro_rib_groupsContext ctx) {
    String name = toString(ctx.name);
    _currentRibGroup = _currentLogicalSystem.getRibGroups().computeIfAbsent(name, RibGroup::new);
    _configuration.defineFlattenedStructure(RIB_GROUP, name, ctx, _parser);
  }

  @Override
  public void exitRo_rib_groups(Ro_rib_groupsContext ctx) {
    _currentRibGroup = null;
  }

  @Override
  public void exitRor_export_rib(Ror_export_ribContext ctx) {
    _currentRibGroup.setExportRib(toString(ctx.rib));
  }

  @Override
  public void exitRor_import_rib(Ror_import_ribContext ctx) {
    _currentRibGroup.addImportRib(toString(ctx.rib));
  }

  @Override
  public void exitRor_import_policy(Ror_import_policyContext ctx) {
    _currentRibGroup.addImportPolicy(
        toComplexPolicyStatement(
            ctx.expr, JuniperStructureUsage.ROUTING_OPTIONS_RIB_GROUPS_IMPORT_POLICY));
  }

  @Override
  public void enterRo_resolution(Ro_resolutionContext ctx) {
    _currentResolution = _currentRoutingInstance.getOrCreateResolution();
  }

  @Override
  public void exitRo_resolution(Ro_resolutionContext ctx) {
    _currentResolution = null;
  }

  @Override
  public void enterRores_rib(Rores_ribContext ctx) {
    String name = toString(ctx.name);
    if (!name.equals(RIB_IPV4_UNICAST)) {
      warn(ctx, "Resolution ribs other than inet.0 are currently unsupported");
    }
    _currentResolutionRib = _currentResolution.getOrReplaceRib(name);
  }

  @Override
  public void exitRores_rib(Rores_ribContext ctx) {
    _currentResolutionRib = null;
  }

  @Override
  public void exitRoresr_import(Roresr_importContext ctx) {
    _currentResolutionRib.addImportPolicy(
        toComplexPolicyStatement(ctx.expr, RESOLUTION_RIB_IMPORT_POLICY));
  }

  @Override
  public void enterRos_route4(Ros_route4Context ctx) {
    Prefix prefix = toPrefix(ctx.prefix);
    Map<Prefix, StaticRouteV4> staticRoutes = _currentRib.getStaticRoutes();
    _currentStaticRoute = staticRoutes.computeIfAbsent(prefix, StaticRouteV4::new);
  }

  @Override
  public void enterRos_route6(Ros_route6Context ctx) {
    Prefix6 prefix = toPrefix6(ctx.prefix);
    Map<Prefix6, StaticRouteV6> staticRoutes = _currentRib.getStaticRoutesV6();
    _currentStaticRoute = staticRoutes.computeIfAbsent(prefix, StaticRouteV6::new);
  }

  @Override
  public void enterRo_srlg(Ro_srlgContext ctx) {
    String name = toString(ctx.name);
    _configuration.defineFlattenedStructure(SRLG, name, ctx, _parser);
    _currentSrlg = _currentLogicalSystem.getOrCreateSrlg(name);
  }

  @Override
  public void exitRo_srlg(Ro_srlgContext ctx) {
    _currentSrlg = null;
  }

  @Override
  public void exitRoslrg_srlg_cost(Roslrg_srlg_costContext ctx) {
    toInteger(ctx, ctx.cost).ifPresent(_currentSrlg::setCost);
  }

  @Override
  public void exitRoslrg_srlg_value(Roslrg_srlg_valueContext ctx) {
    toLong(ctx, ctx.value).ifPresent(_currentSrlg::setValue);
  }

  @Override
  public void enterRs_rule(Rs_ruleContext ctx) {
    String name = toString(ctx.name);
    List<NatRule> currentNatRules = _currentNatRuleSet.getRules();
    _currentNatRule =
        currentNatRules.isEmpty() ? null : currentNatRules.get(currentNatRules.size() - 1);
    if (_currentNatRule == null || !name.equals(_currentNatRule.getName())) {
      _currentNatRule = new NatRule(name);
      currentNatRules.add(_currentNatRule);
    }
    _configuration.defineFlattenedStructure(NAT_RULE, name, ctx, _parser);
  }

  @Override
  public void exitRs_rule(Rs_ruleContext ctx) {
    _currentNatRule = null;
  }

  @Override
  public void enterS_firewall(S_firewallContext ctx) {
    _currentFirewallFamily = Family.INET;
  }

  @Override
  public void enterS_logical_systems(S_logical_systemsContext ctx) {
    String name = toString(ctx.name);
    _configuration.defineFlattenedStructure(
        JuniperStructureType.LOGICAL_SYSTEM, name, ctx, _parser);
    setLogicalSystem(_configuration.getLogicalSystems().computeIfAbsent(name, LogicalSystem::new));
  }

  @Override
  public void exitS_logical_systems(S_logical_systemsContext ctx) {
    setLogicalSystem(_configuration.getMasterLogicalSystem());
  }

  @Override
  public void enterS_routing_options(S_routing_optionsContext ctx) {
    _currentRib = _currentRoutingInstance.getRibs().get(RoutingInformationBase.RIB_IPV4_UNICAST);
  }

  @Override
  public void enterS_snmp(S_snmpContext ctx) {
    SnmpServer snmpServer = _currentRoutingInstance.getSnmpServer();
    if (snmpServer == null) {
      snmpServer = new SnmpServer();
      snmpServer.setVrf(_currentRoutingInstance.getName());
      _currentRoutingInstance.setSnmpServer(snmpServer);
    }
    _currentSnmpServer = snmpServer;
  }

  @Override
  public void enterS_switch_options(S_switch_optionsContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitSo_route_distinguisher(So_route_distinguisherContext ctx) {
    RouteDistinguisher rd = toRouteDistinguisher(ctx.route_distinguisher());
    _currentLogicalSystem.getOrInitSwitchOptions().setRouteDistinguisher(rd);
  }

  @Override
  public void exitSo_vtep_source_interface(So_vtep_source_interfaceContext ctx) {
    // TODO: add reference
    String ifaceName = getInterfaceFullName(ctx.iface);
    _currentLogicalSystem.getOrInitSwitchOptions().setVtepSourceInterface(ifaceName);
  }

  @Override
  public void enterSe_address_book(Se_address_bookContext ctx) {
    String name = toString(ctx.name);
    _currentAddressBook =
        _currentLogicalSystem
            .getAddressBooks()
            .computeIfAbsent(
                name, n -> new AddressBook(n, _currentLogicalSystem.getGlobalAddressBook()));
    if (!_currentAddressBook.getName().equals(LogicalSystem.GLOBAL_ADDRESS_BOOK_NAME)) {
      _configuration.defineFlattenedStructure(
          JuniperStructureType.ADDRESS_BOOK, _currentAddressBook.getName(), ctx, _parser);
    }
  }

  @Override
  public void exitSe_address_book(Se_address_bookContext ctx) {
    _currentAddressBook = null;
  }

  @Override
  public void enterSes_ids_option(FlatJuniperParser.Ses_ids_optionContext ctx) {
    String name = toString(ctx.name);
    _currentScreen =
        _currentLogicalSystem.getScreens().computeIfAbsent(name, n -> new Screen(name));
  }

  @Override
  public void exitSes_ids_option(FlatJuniperParser.Ses_ids_optionContext ctx) {
    _currentScreen = null;
  }

  @Override
  public void exitSeso_alarm(FlatJuniperParser.Seso_alarmContext ctx) {
    _currentScreen.setAction(ScreenAction.ALARM_WITHOUT_DROP);
  }

  @Override
  public void exitSesoi_fragment(Sesoi_fragmentContext ctx) {
    // Batfish does not currently model the IP fragmentation bits.
    warn(ctx, "Unsupported netscreen ICMP option");
  }

  @Override
  public void exitSesoi_large(Sesoi_largeContext ctx) {
    _currentScreen.getScreenOptions().add(IcmpLarge.INSTANCE);
  }

  @Override
  public void exitSesoi_ping_death(Sesoi_ping_deathContext ctx) {
    // Batfish does not currently model the IP fragmentation bits needed for this.
    warn(ctx, "Unsupported netscreen ICMP option");
  }

  @Override
  public void exitSesop_block_frag(Sesop_block_fragContext ctx) {
    // Batfish does not currently model the IP fragmentation bits.
    warn(ctx, "Unsupported netscreen IP option");
  }

  @Override
  public void exitSesop_spoofing(Sesop_spoofingContext ctx) {
    // Need to plumb into reachability and dataplane to support.
    warn(ctx, "Unsupported netscreen IP option");
  }

  @Override
  public void exitSesop_unknown_protocol(Sesop_unknown_protocolContext ctx) {
    _currentScreen.getScreenOptions().add(IpUnknownProtocol.INSTANCE);
  }

  @Override
  public void exitSesot_fin_no_ack(Sesot_fin_no_ackContext ctx) {
    _currentScreen.getScreenOptions().add(TcpFinNoAck.INSTANCE);
  }

  @Override
  public void exitSesot_land(Sesot_landContext ctx) {
    // Batfish has no way to express SourceIp == DestIp.
    warn(ctx, "Unsupported netscreen TCP option");
  }

  @Override
  public void exitSesot_syn_fin(Sesot_syn_finContext ctx) {
    _currentScreen.getScreenOptions().add(TcpSynFin.INSTANCE);
  }

  @Override
  public void exitSesot_syn_frag(Sesot_syn_fragContext ctx) {
    // Batfish does not currently model the IP fragmentation bits.
    warn(ctx, "Unsupported netscreen TCP option");
  }

  @Override
  public void exitSesot_tcp_no_flag(Sesot_tcp_no_flagContext ctx) {
    _currentScreen.getScreenOptions().add(TcpNoFlag.INSTANCE);
  }

  @Override
  public void exitSesot_winnuke(Sesot_winnukeContext ctx) {
    // Batfish does not currently support transformation in filters.
    warn(ctx, "Unsupported netscreen TCP option");
  }

  @Override
  public void enterSead_address_set(Sead_address_setContext ctx) {
    String name = toString(ctx.name);
    AddressBookEntry entry =
        _currentAddressBook.getEntries().computeIfAbsent(name, AddressSetAddressBookEntry::new);
    try {
      _currentAddressSetAddressBookEntry = (AddressSetAddressBookEntry) entry;
    } catch (ClassCastException e) {
      _w.redFlag(
          "Cannot create address-set address-book entry \""
              + name
              + "\" because a different type of address-book entry with that name already exists");
      // Create a throw-away entry so deeper parsing does not fail for this entry
      _currentAddressSetAddressBookEntry = new AddressSetAddressBookEntry(name);
    }
  }

  @Override
  public void exitSead_address(Sead_addressContext ctx) {
    String name = toString(ctx.name);
    if (ctx.ip_and_mask != null) {
      IpWildcard ipWildcard = toIpWildcard(ctx.ip_and_mask);
      AddressBookEntry addressEntry = new AddressAddressBookEntry(name, ipWildcard);
      _currentAddressBook.getEntries().put(name, addressEntry);
    } else if (ctx.address != null) {
      IpWildcard ipWildcard = toIpWildcard(ctx.address);
      AddressBookEntry addressEntry = new AddressAddressBookEntry(name, ipWildcard);
      _currentAddressBook.getEntries().put(name, addressEntry);
    } else if (ctx.prefix != null) {
      IpWildcard ipWildcard = toIpWildcard(ctx.prefix);
      AddressBookEntry addressEntry = new AddressAddressBookEntry(name, ipWildcard);
      _currentAddressBook.getEntries().put(name, addressEntry);
    } else if (ctx.RANGE_ADDRESS() != null) {
      Ip lower = toIp(ctx.lower_limit);
      Ip upper = toIp(ctx.upper_limit);
      if (lower.compareTo(upper) > 0) {
        // Lower is bigger, Juniper will reject this.
        _w.fatalRedFlag("Range must be from low to high: %s", getFullText(ctx));
        return;
      }
      AddressBookEntry addressEntry = new AddressRangeAddressBookEntry(name, lower, upper);
      _currentAddressBook.getEntries().put(name, addressEntry);
    } else {
      assert ctx.description() != null;
      /* TODO - data model doesn't have a place to put this yet. */
    }
  }

  private static @Nonnull IpWildcard toIpWildcard(Ip_address_and_maskContext ctx) {
    String[] ips = ctx.getText().split("/");
    Ip address = Ip.parse(ips[0]);
    Ip invertedMask = Ip.parse(ips[1]);
    return IpWildcard.ipWithWildcardMask(address, invertedMask.inverted());
  }

  @Override
  public void exitSead_address_set(Sead_address_setContext ctx) {
    _currentAddressSetAddressBookEntry = null;
  }

  @Override
  public void exitSeada_address(Seada_addressContext ctx) {
    String name = toString(ctx.name);
    _currentAddressSetAddressBookEntry
        .getEntries()
        .put(name, new AddressSetEntry(name, _currentAddressBook));
  }

  @Override
  public void exitSead_attach(Sead_attachContext ctx) {
    String zoneName = toString(ctx.name);
    Zone zone = _currentLogicalSystem.getOrCreateZone(zoneName);
    switch (zone.getAddressBookType()) {
      case GLOBAL:
        zone.attachAddressBook(_currentAddressBook);
        _configuration.referenceStructure(
            ADDRESS_BOOK,
            _currentAddressBook.getName(),
            ADDRESS_BOOK_ATTACH_ZONE,
            getLine(ctx.name.getStart()));
        break;
      case ATTACHED:
        _w.redFlagf(
            "Two address books are attached to zone %s: %s and %s. Ignoring the first one",
            zone.getName(), zone.getAddressBook().getName(), _currentAddressBook.getName());
        zone.attachAddressBook(_currentAddressBook);
        _configuration.referenceStructure(
            ADDRESS_BOOK,
            _currentAddressBook.getName(),
            ADDRESS_BOOK_ATTACH_ZONE,
            getLine(ctx.name.getStart()));
        break;
      case INLINED:
        _w.redFlagf(
            "Not attaching the address book %s to zone %s because an inline address book is"
                + " defined",
            _currentAddressBook.getName(), zone.getName());
        break;
    }
  }

  @Override
  public void exitSeada_address_set(Seada_address_setContext ctx) {
    String name = toString(ctx.name);
    _currentAddressSetAddressBookEntry
        .getEntries()
        .put(name, new AddressSetEntry(name, _currentAddressBook));
  }

  @Override
  public void enterSe_authentication_key_chain(Se_authentication_key_chainContext ctx) {
    String name = toString(ctx.name);
    int line = getLine(ctx.getStart());
    JuniperAuthenticationKeyChain authenticationkeyChain =
        _currentLogicalSystem
            .getAuthenticationKeyChains()
            .computeIfAbsent(name, n -> new JuniperAuthenticationKeyChain(n, line));
    _currentAuthenticationKeyChain = authenticationkeyChain;
    _configuration.defineFlattenedStructure(AUTHENTICATION_KEY_CHAIN, name, ctx, _parser);
  }

  @Override
  public void enterSea_key(Sea_keyContext ctx) {
    String name = toString(ctx.name);
    JuniperAuthenticationKey authenticationkey =
        _currentAuthenticationKeyChain
            .getKeys()
            .computeIfAbsent(name, JuniperAuthenticationKey::new);
    _currentAuthenticationKey = authenticationkey;
  }

  @Override
  public void enterSeik_gateway(Seik_gatewayContext ctx) {
    String name = toString(ctx.name);
    _currentIkeGateway =
        _currentLogicalSystem.getIkeGateways().computeIfAbsent(name, IkeGateway::new);
    _configuration.defineFlattenedStructure(IKE_GATEWAY, name, ctx, _parser);
  }

  @Override
  public void enterSeik_policy(Seik_policyContext ctx) {
    String name = toString(ctx.name);
    _currentIkePolicy =
        _currentLogicalSystem.getIkePolicies().computeIfAbsent(name, IkePolicy::new);
    _configuration.defineFlattenedStructure(IKE_POLICY, name, ctx, _parser);
  }

  @Override
  public void enterSeik_proposal(Seik_proposalContext ctx) {
    String name = toString(ctx.name);
    _currentIkeProposal =
        _currentLogicalSystem.getIkeProposals().computeIfAbsent(name, IkeProposal::new);
    _configuration.defineFlattenedStructure(IKE_PROPOSAL, name, ctx, _parser);
  }

  @Override
  public void enterSeip_policy(Seip_policyContext ctx) {
    String name = toString(ctx.name);
    _currentIpsecPolicy =
        _currentLogicalSystem.getIpsecPolicies().computeIfAbsent(name, IpsecPolicy::new);
    _configuration.defineFlattenedStructure(IPSEC_POLICY, name, ctx, _parser);
  }

  @Override
  public void enterSeip_proposal(Seip_proposalContext ctx) {
    String name = toString(ctx.name);
    _currentIpsecProposal =
        _currentLogicalSystem.getIpsecProposals().computeIfAbsent(name, IpsecProposal::new);
    _configuration.defineFlattenedStructure(IPSEC_PROPOSAL, name, ctx, _parser);
  }

  @Override
  public void enterSeip_vpn(Seip_vpnContext ctx) {
    String name = toString(ctx.name);
    _currentIpsecVpn = _currentLogicalSystem.getIpsecVpns().computeIfAbsent(name, IpsecVpn::new);
  }

  @Override
  public void enterSen_destination(Sen_destinationContext ctx) {
    _currentNat = _currentLogicalSystem.getOrCreateNat(Nat.Type.DESTINATION);
  }

  @Override
  public void exitSen_destination(Sen_destinationContext ctx) {
    _currentNat = null;
  }

  @Override
  public void enterSen_source(Sen_sourceContext ctx) {
    _currentNat = _currentLogicalSystem.getOrCreateNat(SOURCE);
  }

  @Override
  public void exitSen_source(Sen_sourceContext ctx) {
    _currentNat = null;
  }

  @Override
  public void enterSen_static(Sen_staticContext ctx) {
    _currentNat = _currentLogicalSystem.getOrCreateNat(Nat.Type.STATIC);
  }

  @Override
  public void exitSen_static(Sen_staticContext ctx) {
    _currentNat = null;
  }

  @Override
  public void enterSep_from_zone(Sep_from_zoneContext ctx) {
    if (ctx.from.JUNOS_HOST() != null && ctx.to.JUNOS_HOST() != null) {
      _w.redFlag("Cannot create security policy from junos-host to junos-host");
      _currentFilter =
          new ConcreteFirewallFilter(
              "invalid security-policy from junos-host to itself", Family.INET);
      _currentSecurityPolicyName = "invalid security-policy from junos-host to itself";
      return;
    }

    String fromName = toString(ctx.from);
    String toName = toString(ctx.to);
    String policyName = zoneToZoneFilter(fromName, toName);
    _configuration.defineFlattenedStructure(SECURITY_POLICY, policyName, ctx, _parser);
    _configuration.referenceStructure(
        SECURITY_POLICY, policyName, SECURITY_POLICY_DEFINITION, getLine(ctx.start));
    _currentSecurityPolicyName = policyName;
    if (ctx.from.JUNOS_HOST() == null) {
      _currentFromZone = _currentLogicalSystem.getOrCreateZone(fromName);
    }

    if (ctx.to.JUNOS_HOST() == null) {
      _currentToZone = _currentLogicalSystem.getOrCreateZone(toName);
    }

    if (ctx.from.JUNOS_HOST() != null) {
      // Policy for traffic originating from this device
      _currentFilter = _currentToZone.getFromHostFilter();
      if (_currentFilter == null) {
        _currentFilter = new ConcreteFirewallFilter(policyName, Family.INET);
        _currentLogicalSystem.getSecurityPolicies().put(policyName, _currentFilter);
        _currentToZone.setFromHostFilter(_currentFilter);
      }
    } else if (ctx.to.JUNOS_HOST() != null) {
      // Policy for traffic destined for this device
      _currentFilter = _currentFromZone.getToHostFilter();
      if (_currentFilter == null) {
        _currentFilter = new ConcreteFirewallFilter(policyName, Family.INET);
        _currentLogicalSystem.getSecurityPolicies().put(policyName, _currentFilter);
        _currentFromZone.setToHostFilter(_currentFilter);
      }
    } else {
      // Policy for thru traffic
      _currentFilter = _currentFromZone.getToZonePolicies().get(toName);
      if (_currentFilter == null) {
        _currentFilter = new ConcreteFirewallFilter(policyName, Family.INET);
        _currentLogicalSystem.getSecurityPolicies().put(policyName, _currentFilter);
        _currentFromZone.getToZonePolicies().put(toName, _currentFilter);
      }
      // Add this filter to the to-zone for easy combination with egress ACL
      _currentToZone.getFromZonePolicies().put(policyName, _currentFilter);
    }

    /*
     * Need to keep track of the from-zone for this filter to apply srcInterface filter to the
     * firewallFilter
     */
    if (_currentFromZone != null) {
      _currentFilter.setFromZone(_currentFromZone.getName());
    }
  }

  @Override
  public void exitSep_from_zone(Sep_from_zoneContext ctx) {
    _currentFilter = null;
    _currentSecurityPolicyName = null;
  }

  @Override
  public void enterSep_global(Sep_globalContext ctx) {
    _currentFilter =
        _currentLogicalSystem
            .getSecurityPolicies()
            .computeIfAbsent(
                ACL_NAME_GLOBAL_POLICY, n -> new ConcreteFirewallFilter(n, Family.INET));
    _currentSecurityPolicyName = ACL_NAME_GLOBAL_POLICY;
    _configuration.defineFlattenedStructure(SECURITY_POLICY, ACL_NAME_GLOBAL_POLICY, ctx, _parser);
    _configuration.referenceStructure(
        SECURITY_POLICY, ACL_NAME_GLOBAL_POLICY, SECURITY_POLICY_DEFINITION, getLine(ctx.start));
  }

  @Override
  public void exitSep_global(Sep_globalContext ctx) {
    _currentFilter = null;
    _currentSecurityPolicyName = null;
  }

  @Override
  public void enterSepctx_policy(Sepctx_policyContext ctx) {
    String termName = toString(ctx.name);
    _currentFwTerm = _currentFilter.getTerms().computeIfAbsent(termName, FwTerm::new);
    String defName = computeSecurityPolicyTermName(_currentSecurityPolicyName, termName);
    _configuration.defineFlattenedStructure(SECURITY_POLICY_TERM, defName, ctx, _parser);
    _configuration.referenceStructure(
        SECURITY_POLICY_TERM,
        defName,
        SECURITY_POLICY_TERM_DEFINITION,
        getLine(ctx.name.getStart()));
  }

  @Override
  public void enterSez_security_zone(Sez_security_zoneContext ctx) {
    String zoneName = toString(ctx.zone());
    _currentZone = _currentLogicalSystem.getOrCreateZone(zoneName);
    _currentZoneInboundFilter = _currentZone.getInboundFilter();
  }

  @Override
  public void enterSezs_address_book(Sezs_address_bookContext ctx) {
    switch (_currentZone.getAddressBookType()) {
      case GLOBAL:
        _currentAddressBook =
            _currentZone.initInlinedAddressBook(_currentLogicalSystem.getGlobalAddressBook());
        return;
      case INLINED:
        _currentAddressBook = _currentZone.getAddressBook();
        return;
      case ATTACHED:
        _w.redFlagf(
            "Ignoring attached address book %s to zone %s because an inline address book is"
                + " defined",
            _currentZone.getAddressBook().getName(), _currentZone.getName());
        _currentAddressBook =
            _currentZone.initInlinedAddressBook(_currentLogicalSystem.getGlobalAddressBook());
        break;
    }
  }

  @Override
  public void enterSezs_host_inbound_traffic(Sezs_host_inbound_trafficContext ctx) {
    if (_currentZoneInterface != null) {
      _currentZoneInboundFilter =
          _currentZone.getInboundInterfaceFilters().get(_currentZoneInterface);
      if (_currentZoneInboundFilter == null) {
        String name =
            "~ZONE_INTERFACE_FILTER~"
                + _currentZone.getName()
                + "~INTERFACE~"
                + _currentZoneInterface;
        _currentZoneInboundFilter = new ConcreteFirewallFilter(name, Family.INET);
        _currentLogicalSystem.getSecurityPolicies().put(name, _currentZoneInboundFilter);
        _currentZone
            .getInboundInterfaceFilters()
            .put(_currentZoneInterface, _currentZoneInboundFilter);
      }
    }
  }

  @Override
  public void enterSezs_interfaces(Sezs_interfacesContext ctx) {
    _currentZoneInterface = getInterfaceFullName(ctx.interface_id());
    _currentZone.getInterfaces().add(_currentZoneInterface);
    _currentLogicalSystem.getInterfaceZones().put(_currentZoneInterface, _currentZone);
    _configuration.referenceStructure(
        INTERFACE,
        _currentZoneInterface,
        SECURITY_ZONES_SECURITY_ZONES_INTERFACE,
        getLine(ctx.interface_id().getStop()));
  }

  @Override
  public void exitSezs_screen(FlatJuniperParser.Sezs_screenContext ctx) {
    String name =
        ctx.UNTRUST_SCREEN() == null ? toString(ctx.name) : ctx.UNTRUST_SCREEN().getText();
    _currentZone.getScreens().add(name);
  }

  @Override
  public void enterSezsa_address_set(Sezsa_address_setContext ctx) {
    String name = toString(ctx.name);
    AddressBookEntry entry =
        _currentAddressBook.getEntries().computeIfAbsent(name, AddressSetAddressBookEntry::new);
    try {
      _currentAddressSetAddressBookEntry = (AddressSetAddressBookEntry) entry;
    } catch (ClassCastException e) {
      _w.redFlag(
          "Cannot create address-set address-book entry \""
              + name
              + "\" because a different type of address-book entry with that name already exists");
      // Create a throw-away entry so deeper parsing does not fail for this entry
      _currentAddressSetAddressBookEntry = new AddressSetAddressBookEntry(name);
    }
  }

  @Override
  public void enterSnmp_community(Snmp_communityContext ctx) {
    String community = toString(ctx.comm);
    SnmpCommunity snmpCommunity =
        _currentSnmpServer.getCommunities().computeIfAbsent(community, SnmpCommunity::new);
    _currentSnmpCommunity = snmpCommunity;
  }

  @Override
  public void exitSnmp_community(Snmp_communityContext ctx) {
    _currentSnmpCommunity = null;
  }

  @Override
  public void enterSnmpc_logical_system(Snmpc_logical_systemContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        JuniperStructureType.LOGICAL_SYSTEM,
        name,
        SNMP_COMMUNITY_LOGICAL_SYSTEM,
        getLine(ctx.name.getStart()));
    LogicalSystem ls = _configuration.getLogicalSystems().get(name);
    if (ls == null) {
      // dummy, deliberately not hooked up.
      ls = new LogicalSystem(name);
    }
    setLogicalSystem(ls);
  }

  @Override
  public void exitSnmpc_logical_system(Snmpc_logical_systemContext ctx) {
    setLogicalSystem(_configuration.getMasterLogicalSystem());
  }

  @Override
  public void enterSnmpcls_routing_instance(Snmpcls_routing_instanceContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ROUTING_INSTANCE, name, SNMP_COMMUNITY_ROUTING_INSTANCE, getLine(ctx.name.getStart()));
    RoutingInstance ri = _currentLogicalSystem.getRoutingInstances().get(name);
    if (ri != null) {
      // dummy, deliberately not hooked up.
      ri = new RoutingInstance(name);
    }
    _currentRoutingInstance = ri;
  }

  @Override
  public void exitSnmpcls_routing_instance(Snmpcls_routing_instanceContext ctx) {
    _currentRoutingInstance = _currentLogicalSystem.getDefaultRoutingInstance();
  }

  @Override
  public void enterSy_authentication_order(Sy_authentication_orderContext ctx) {
    if (_currentLine != null) {
      // in system services/ports hierarchy
      _currentAuthenticationOrder = _currentLine.getAaaAuthenticationLoginList();
      if (_currentAuthenticationOrder == null || _currentAuthenticationOrder.isDefault()) {
        // if the line already has a default authentication order, give it a new non-default one
        _currentAuthenticationOrder = new AaaAuthenticationLoginList(new ArrayList<>(), false);
        _currentLine.setAaaAuthenticationLoginList(_currentAuthenticationOrder);
      }
    } else {
      // in system hierarchy
      _currentAuthenticationOrder = _currentLogicalSystem.getJf().getSystemAuthenticationOrder();
      if (_currentAuthenticationOrder == null || _currentAuthenticationOrder.isDefault()) {
        // if system already has a default authentication order, give it a new non-default one
        _currentAuthenticationOrder = new AaaAuthenticationLoginList(new ArrayList<>(), false);
        _currentLogicalSystem.getJf().setSystemAuthenticationOrder(_currentAuthenticationOrder);
      }
    }

    // _currentAuthenticationOrder = authenticationOrder.getMethods();
  }

  @Override
  public void enterSy_ports(Sy_portsContext ctx) {
    String name = toString(ctx.porttype);
    // aux and console ports should already exist unless they've been disabled, if disabled don't
    // add it to juniperFamily's lines
    _currentLine = firstNonNull(_currentLogicalSystem.getJf().getLines().get(name), new Line(name));
  }

  @Override
  public void enterSy_security_profile(Sy_security_profileContext ctx) {
    _configuration.defineFlattenedStructure(SECURITY_PROFILE, toString(ctx.name), ctx, _parser);
  }

  @Override
  public void enterSy_services_linetype(Sy_services_linetypeContext ctx) {
    String name = toString(ctx.linetype);
    _currentLogicalSystem.getJf().getLines().computeIfAbsent(name, Line::new);
    _currentLine = _currentLogicalSystem.getJf().getLines().get(name);

    // if system authentication order defined, set the current line's authentication login list to
    // the system authentication order
    if (_currentLogicalSystem.getJf().getSystemAuthenticationOrder() != null
        && _currentLine.getAaaAuthenticationLoginList() == null) {
      _currentLine.setAaaAuthenticationLoginList(
          new AaaAuthenticationLoginList(
              _currentLogicalSystem.getJf().getSystemAuthenticationOrder().getMethods(), true));
    }
  }

  @Override
  public void enterSy_tacplus_server(Sy_tacplus_serverContext ctx) {
    String hostname = toString(ctx.tacplus_server_host());
    _currentLogicalSystem.getTacplusServers().add(hostname);
    _currentTacplusServer =
        _currentLogicalSystem
            .getJf()
            .getTacplusServers()
            .computeIfAbsent(hostname, TacplusServer::new);
  }

  @Override
  public void enterSyn_server(Syn_serverContext ctx) {
    String hostname = toString(ctx.hostname);
    _currentLogicalSystem.getNtpServers().add(hostname);
  }

  @Override
  public void exitSyn_server_routing_instance(Syn_server_routing_instanceContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ROUTING_INSTANCE, name, NTP_SERVER_ROUTING_INSTANCE, getLine(ctx.getStart()));
  }

  @Override
  public void exitSyn_source_address(Syn_source_addressContext ctx) {
    if (ctx.ri != null) {
      String name = toString(ctx.ri);
      _configuration.referenceStructure(
          ROUTING_INSTANCE, name, NTP_SOURCE_ADDRESS_ROUTING_INSTANCE, getLine(ctx.getStart()));
    }
  }

  @Override
  public void enterSyp_disable(Syp_disableContext ctx) {
    // line is disabled so remove it from list of lines
    _currentLogicalSystem.getJf().getLines().remove(_currentLine.getName());
  }

  @Override
  public void enterSys_host(Sys_hostContext ctx) {
    String hostname = toString(ctx.hostname);
    _currentLogicalSystem.getSyslogHosts().add(hostname);
  }

  @Override
  public void enterS_vlans_named(S_vlans_namedContext ctx) {
    String name = toString(ctx.name);
    _currentNamedVlan = _currentLogicalSystem.getNamedVlans().computeIfAbsent(name, Vlan::new);
    _configuration.defineFlattenedStructure(VLAN, name, ctx, _parser);
  }

  @Override
  public void exitA_application(A_applicationContext ctx) {
    _currentApplication = null;
    _currentApplicationTerm = null;
  }

  @Override
  public void exitAa_term(Aa_termContext ctx) {
    _currentApplicationTerm = _currentApplication.getMainTerm();
  }

  @Override
  public void exitAat_destination_port(Aat_destination_portContext ctx) {
    SubRange subrange = toSubRange(ctx.port_range());
    HeaderSpace oldHeaderSpace = _currentApplicationTerm.getHeaderSpace();
    _currentApplicationTerm.setHeaderSpace(
        oldHeaderSpace.toBuilder()
            .setDstPorts(
                ImmutableSet.<SubRange>builder()
                    .addAll(oldHeaderSpace.getDstPorts())
                    .add(subrange)
                    .build())
            .build());
  }

  @Override
  public void exitAat_icmp_code(Aat_icmp_codeContext ctx) {
    Optional<Integer> maybeIcmpCode = toInteger(ctx, ctx.icmp_code());
    if (!maybeIcmpCode.isPresent()) {
      return;
    }
    HeaderSpace oldHeaderSpace = _currentApplicationTerm.getHeaderSpace();
    _currentApplicationTerm.setHeaderSpace(
        oldHeaderSpace.toBuilder().setIcmpCodes(maybeIcmpCode.get()).build());
  }

  private @Nonnull Optional<Integer> toInteger(ParserRuleContext messageCtx, Icmp_codeContext ctx) {
    if (ctx.uint8() != null) {
      return Optional.of(toInteger(ctx.uint8()));
    } else {
      assert ctx.named_icmp_code() != null;
      // NB: JunOS requires users to set both code AND type separately, so this does not affect
      // type.
      return toIcmpCode(messageCtx, ctx.named_icmp_code()).map(IcmpCode::getCode);
    }
  }

  private @Nonnull Optional<Integer> toInteger(ParserRuleContext messageCtx, Icmp_typeContext ctx) {
    if (ctx.uint8() != null) {
      return Optional.of(toInteger(ctx.uint8()));
    } else {
      assert ctx.named_icmp_type() != null;
      return toInteger(messageCtx, ctx.named_icmp_type());
    }
  }

  @Override
  public void exitAat_icmp_type(Aat_icmp_typeContext ctx) {
    Optional<Integer> maybeIcmpType = toInteger(ctx, ctx.icmp_type());
    if (!maybeIcmpType.isPresent()) {
      return;
    }
    HeaderSpace oldHeaderSpace = _currentApplicationTerm.getHeaderSpace();
    _currentApplicationTerm.setHeaderSpace(
        oldHeaderSpace.toBuilder().setIcmpTypes(maybeIcmpType.get()).build());
  }

  @Override
  public void exitAat_protocol(Aat_protocolContext ctx) {
    IpProtocol protocol = toIpProtocol(ctx.ip_protocol());
    HeaderSpace oldHeaderSpace = _currentApplicationTerm.getHeaderSpace();
    _currentApplicationTerm.setHeaderSpace(
        oldHeaderSpace.toBuilder()
            .setIpProtocols(
                ImmutableSet.<IpProtocol>builder()
                    .addAll(oldHeaderSpace.getIpProtocols())
                    .add(protocol)
                    .build())
            .build());
  }

  @Override
  public void exitAat_source_port(Aat_source_portContext ctx) {
    SubRange subrange = toSubRange(ctx.port_range());
    HeaderSpace oldHeaderSpace = _currentApplicationTerm.getHeaderSpace();
    _currentApplicationTerm.setHeaderSpace(
        oldHeaderSpace.toBuilder()
            .setSrcPorts(
                ImmutableSet.<SubRange>builder()
                    .addAll(oldHeaderSpace.getSrcPorts())
                    .add(subrange)
                    .build())
            .build());
  }

  @Override
  public void exitB_advertise_external(B_advertise_externalContext ctx) {
    _currentBgpGroup.setAdvertiseExternal(true);
  }

  @Override
  public void exitB_advertise_inactive(B_advertise_inactiveContext ctx) {
    _currentBgpGroup.setAdvertiseInactive(true);
  }

  @Override
  public void exitB_advertise_peer_as(B_advertise_peer_asContext ctx) {
    _currentBgpGroup.setAdvertisePeerAs(true);
  }

  @Override
  public void exitB_authentication_algorithm(B_authentication_algorithmContext ctx) {
    if (ctx.AES_128_CMAC_96() != null) {
      _currentBgpGroup.setAuthenticationAlgorithm(BgpAuthenticationAlgorithm.AES_128_CMAC_96);
    } else if (ctx.MD5() != null) {
      _currentBgpGroup.setAuthenticationAlgorithm(BgpAuthenticationAlgorithm.TCP_ENHANCED_MD5);
    } else {
      _currentBgpGroup.setAuthenticationAlgorithm(BgpAuthenticationAlgorithm.HMAC_SHA_1_96);
    }
  }

  @Override
  public void exitB_authentication_key(B_authentication_keyContext ctx) {
    _currentBgpGroup.setAuthenticationKey(applySecret(ctx.key));
  }

  @Override
  public void exitB_authentication_key_chain(B_authentication_key_chainContext ctx) {
    String name = toString(ctx.name);
    _currentBgpGroup.setAuthenticationKeyChainName(name);
    int line = getLine(ctx.getStart());
    _configuration.referenceStructure(
        AUTHENTICATION_KEY_CHAIN, name, AUTHENTICATION_KEY_CHAINS_POLICY, line);
  }

  @Override
  public void exitB_cluster(B_clusterContext ctx) {
    Ip clusterId = toIp(ctx.id);
    _currentBgpGroup.setClusterId(clusterId);
  }

  @Override
  public void exitB_description(B_descriptionContext ctx) {
    _currentBgpGroup.setDescription(toString(ctx.description()));
  }

  @Override
  public void exitB_disable(B_disableContext ctx) {
    _currentBgpGroup.setDisable(true);
  }

  @Override
  public void exitB_enable(B_enableContext ctx) {
    _currentBgpGroup.setDisable(false);
  }

  @Override
  public void exitB_enforce_first_as(B_enforce_first_asContext ctx) {
    _currentBgpGroup.setEnforceFirstAs(true);
  }

  @Override
  public void exitB_export(B_exportContext ctx) {
    Policy_expressionContext expr = ctx.expr;
    _currentBgpGroup.getExportPolicies().add(toComplexPolicyStatement(expr, BGP_EXPORT_POLICY));
  }

  @Override
  public void exitB_import(B_importContext ctx) {
    Policy_expressionContext expr = ctx.expr;
    _currentBgpGroup.getImportPolicies().add(toComplexPolicyStatement(expr, BGP_IMPORT_POLICY));
  }

  @Override
  public void exitB_local_address(B_local_addressContext ctx) {
    if (ctx.ip_address() != null) {
      Ip localAddress = toIp(ctx.ip_address());
      _currentBgpGroup.setLocalAddress(localAddress);
    }
  }

  @Override
  public void exitB_multihop(B_multihopContext ctx) {
    _currentBgpGroup.setEbgpMultihop(true);
  }

  @Override
  public void exitB_multipath(B_multipathContext ctx) {
    _currentBgpGroup.setMultipath(true);
    if (ctx.MULTIPLE_AS() != null) {
      _currentBgpGroup.setMultipathMultipleAs(true);
    }
  }

  @Override
  public void exitB_preference(B_preferenceContext ctx) {
    int preference = toInt(ctx.pref);
    _currentBgpGroup.setPreference(preference);
  }

  @Override
  public void exitB_remove_private(B_remove_privateContext ctx) {
    _currentBgpGroup.setRemovePrivate(true);
  }

  @Override
  public void exitB_type(B_typeContext ctx) {
    if (ctx.INTERNAL() != null) {
      _currentBgpGroup.setType(BgpGroupType.INTERNAL);
    } else if (ctx.EXTERNAL() != null) {
      _currentBgpGroup.setType(BgpGroupType.EXTERNAL);
    }
  }

  @Override
  public void exitBfiu_loops(Bfiu_loopsContext ctx) {
    _currentBgpGroup.setLoops(toInteger(ctx.count));
  }

  @Override
  public void enterBfiu_rib_group(Bfiu_rib_groupContext ctx) {
    String groupName = toString(ctx.name);
    _configuration.referenceStructure(
        RIB_GROUP, groupName, BGP_FAMILY_INET_UNICAST_RIB_GROUP, getLine(ctx.name.getStart()));
    _currentBgpGroup.setRibGroup(groupName);
  }

  @Override
  public void exitBl_alias(Bl_aliasContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitBl_loops(Bl_loopsContext ctx) {
    todo(ctx);
    int loops = toInteger(ctx.count);
    _currentBgpGroup.setLoops(loops);
  }

  @Override
  public void exitBl_no_prepend_global_as(Bl_no_prepend_global_asContext ctx) {
    _currentBgpGroup.setNoPrependGlobalAs(true);
  }

  @Override
  public void exitBl_number(Bl_numberContext ctx) {
    long localAs = toAsNum(ctx.asn);
    _currentBgpGroup.setLocalAs(localAs);
  }

  @Override
  public void exitBl_private(Bl_privateContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitBpa_as(Bpa_asContext ctx) {
    long peerAs = toAsNum(ctx.asn);
    _currentBgpGroup.setPeerAs(peerAs);
  }

  @Override
  public void exitBps_always_compare_med(Bps_always_compare_medContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitBps_external_router_id(Bps_external_router_idContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEo8023ad_interface(Eo8023ad_interfaceContext ctx) {
    // TODO: handle node
    String interfaceName = getInterfaceFullName(ctx.interface_id());
    _currentInterfaceOrRange.set8023adInterface(interfaceName);
  }

  @Override
  public void exitE_encapsulation(E_encapsulationContext ctx) {
    if (ctx.VXLAN() != null) {
      _currentLogicalSystem.getEvpn().setEncapsulation(EvpnEncapsulation.VXLAN);
    } else {
      assert ctx.MPLS() != null;
      _currentLogicalSystem.getEvpn().setEncapsulation(EvpnEncapsulation.MPLS);
    }
  }

  @Override
  public void exitE_extended_vni_list(E_extended_vni_listContext ctx) {
    if (ctx.ALL() != null) {
      _currentLogicalSystem.getEvpn().setExtendedVniAll(Boolean.TRUE);
    } else {
      for (Vni_rangeContext vni : ctx.vni_range()) {
        Optional<SubRange> maybeVniRange = toSubRange(ctx, vni);
        Evpn evpn = _currentLogicalSystem.getEvpn();
        if (evpn.getExtendedVniList() != null) {
          evpn.setExtendedVniList(
              IntegerSpace.builder()
                  .including(evpn.getExtendedVniList())
                  .including(maybeVniRange.get())
                  .build());
        } else {
          evpn.setExtendedVniList(IntegerSpace.of(maybeVniRange.get()));
        }
      }
    }
  }

  @Override
  public void exitE_multicast_mode(E_multicast_modeContext ctx) {
    if (ctx.INGRESS_REPLICATION() != null) {
      _currentLogicalSystem.getEvpn().setMulticastMode(MulticastModeOptions.INGRESS_REPLICATION);
    } else {
      assert ctx.CLIENT() != null;
      _currentLogicalSystem.getEvpn().setMulticastMode(MulticastModeOptions.CLIENT);
    }
  }

  @Override
  public void exitE_vni_options(E_vni_optionsContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEo_redundant_parent(Eo_redundant_parentContext ctx) {
    String interfaceName = toInterfaceId(ctx.name).getFullName();
    _currentInterfaceOrRange.setRedundantParentInterface(interfaceName);
  }

  @Override
  public void exitEvo_vrf_target(Evo_vrf_targetContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitF_filter(F_filterContext ctx) {
    _currentFilter = null;
  }

  @Override
  public void exitFf_term(Ff_termContext ctx) {
    _currentFwTerm = null;
  }

  @Override
  public void exitFftf_address(Fftf_addressContext ctx) {
    FwFrom from;
    IpWildcard ipWildcard = formIpWildCard(ctx.fftfa_address_mask_prefix());
    if (ipWildcard != null) {
      String text = getFullText(ctx.fftfa_address_mask_prefix());
      from = new FwFromAddress(ipWildcard, text);
      _currentFwTerm.getFroms().add(from);
    }
  }

  @Override
  public void exitFftf_destination_address(Fftf_destination_addressContext ctx) {
    FwFrom from;
    IpWildcard ipWildcard = formIpWildCard(ctx.fftfa_address_mask_prefix());
    if (ipWildcard != null) {
      String text = getFullText(ctx.fftfa_address_mask_prefix());
      from =
          ctx.EXCEPT() != null
              ? new FwFromDestinationAddressExcept(ipWildcard, text)
              : new FwFromDestinationAddress(ipWildcard, text);
      _currentFwTerm.getFroms().add(from);
    }
  }

  @Override
  public void exitFftf_destination_port(Fftf_destination_portContext ctx) {
    SubRange ports = toSubRange(ctx.port_range());
    FwFrom from = new FwFromDestinationPort(ports);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_destination_port_except(Fftf_destination_port_exceptContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitFftf_destination_prefix_list(Fftf_destination_prefix_listContext ctx) {
    String name = toString(ctx.name);
    FwFrom from;
    if (ctx.EXCEPT() != null) {
      from = new FwFromDestinationPrefixListExcept(name);
    } else {
      from = new FwFromDestinationPrefixList(name);
    }
    _currentFwTerm.getFroms().add(from);
    _configuration.referenceStructure(
        PREFIX_LIST, name, FIREWALL_FILTER_DESTINATION_PREFIX_LIST, getLine(ctx.name.start));
  }

  @Override
  public void exitFftf_dscp(Fftf_dscpContext ctx) {
    String dscpSpec = toString(ctx.name);
    FwFromDscp from = new FwFromDscp(dscpSpec);
    Integer value = Ints.tryParse(dscpSpec);
    if (value == null) {
      // not a number, so must be an alias
      // if it is not a builtin alias or it has been overridden, we add a reference
      // TODO: this is not quite right because the builtin override could happen later
      if (!DscpUtil.defaultValue(dscpSpec).isPresent()
          || _currentLogicalSystem.getDscpAliases().containsKey(dscpSpec)) {
        _configuration.referenceStructure(
            CLASS_OF_SERVICE_CODE_POINT_ALIAS,
            dscpSpec,
            FIREWALL_FILTER_DSCP,
            getLine(ctx.name.getStart()));
      }
    }
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_first_fragment(Fftf_first_fragmentContext ctx) {
    SubRange subRange = SubRange.singleton(0);
    FwFrom from = new FwFromFragmentOffset(subRange, false);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_fragment_offset(Fftf_fragment_offsetContext ctx) {
    Optional<SubRange> maybeRange = toSubRange(ctx, ctx.fragment_offset_range());
    if (!maybeRange.isPresent()) {
      return;
    }
    FwFrom from = new FwFromFragmentOffset(maybeRange.get(), false);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_fragment_offset_except(Fftf_fragment_offset_exceptContext ctx) {
    Optional<SubRange> maybeRange = toSubRange(ctx, ctx.fragment_offset_range());
    if (!maybeRange.isPresent()) {
      return;
    }
    FwFrom from = new FwFromFragmentOffset(maybeRange.get(), true);
    _currentFwTerm.getFroms().add(from);
  }

  private @Nonnull Optional<SubRange> toSubRange(
      ParserRuleContext messageCtx, Fragment_offset_rangeContext ctx) {
    Optional<Integer> maybeStart = toInteger(messageCtx, ctx.start);
    if (!maybeStart.isPresent()) {
      return Optional.empty();
    }
    int start = maybeStart.get();
    if (ctx.end == null) {
      return Optional.of(SubRange.singleton(start));
    }
    return toInteger(messageCtx, ctx.end).map(end -> new SubRange(start, end));
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Fragment_offsetContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, FRAGMENT_OFFSET_RANGE, "fragment offset");
  }

  @Override
  public void exitFftf_interface(FlatJuniperParser.Fftf_interfaceContext ctx) {
    String interfaceName = getInterfaceFullName(ctx.interface_id());
    _currentFwTerm.getFroms().add(new FwFromInterface(interfaceName));
    _configuration.referenceStructure(
        INTERFACE,
        interfaceName,
        FIREWALL_FILTER_TERM_FROM_INTERFACE,
        getLine(ctx.interface_id().getStop()));
  }

  @Override
  public void exitFftf_interface_set(FlatJuniperParser.Fftf_interface_setContext ctx) {
    String interfaceSetName = toString(ctx.name);
    _currentFwTerm.getFroms().add(new FwFromInterfaceSet(interfaceSetName));
    _configuration.referenceStructure(
        FIREWALL_INTERFACE_SET,
        interfaceSetName,
        FIREWALL_FILTER_TERM_FROM_INTERFACE_SET,
        getLine(ctx.name.getStop()));
  }

  @Override
  public void exitFftf_icmp_code(Fftf_icmp_codeContext ctx) {
    if (_currentFirewallFamily == Family.INET6) {
      // TODO: support icmpv6
      return;
    }
    SubRange icmpCodeRange;
    if (ctx.uint8_range() != null) {
      icmpCodeRange = toSubRange(ctx.uint8_range());
    } else {
      assert ctx.icmp_code() != null;
      Optional<Integer> maybeIcmpCode = toInteger(ctx, ctx.icmp_code());
      if (!maybeIcmpCode.isPresent()) {
        return;
      }
      icmpCodeRange = SubRange.singleton(maybeIcmpCode.get());
    }
    FwFrom from = new FwFromIcmpCode(icmpCodeRange);
    _currentFwTerm.getFroms().add(from);
  }

  private static @Nonnull SubRange toSubRange(Uint8_rangeContext ctx) {
    int start = toInteger(ctx.start);
    return ctx.end != null ? new SubRange(start, toInteger(ctx.end)) : SubRange.singleton(start);
  }

  private static int toInteger(Uint8Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  @Override
  public void exitFftf_icmp_code_except(Fftf_icmp_code_exceptContext ctx) {
    if (_currentFirewallFamily == Family.INET6) {
      // TODO: support icmpv6
      return;
    }
    SubRange icmpCodeRange;
    if (ctx.uint8_range() != null) {
      icmpCodeRange = toSubRange(ctx.uint8_range());
    } else {
      assert ctx.icmp_code() != null;
      Optional<Integer> maybeIcmpCode = toInteger(ctx, ctx.icmp_code());
      if (!maybeIcmpCode.isPresent()) {
        return;
      }
      icmpCodeRange = SubRange.singleton(maybeIcmpCode.get());
    }
    FwFrom from = new FwFromIcmpCodeExcept(icmpCodeRange);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_icmp_type(Fftf_icmp_typeContext ctx) {
    if (_currentFirewallFamily == Family.INET6) {
      // TODO: support icmpv6
      return;
    }
    SubRange icmpTypeRange;
    if (ctx.uint8_range() != null) {
      icmpTypeRange = toSubRange(ctx.uint8_range());
    } else if (ctx.icmp_type() != null) {
      Optional<Integer> maybeIcmpType = toInteger(ctx, ctx.icmp_type());
      if (!maybeIcmpType.isPresent()) {
        return;
      }
      icmpTypeRange = SubRange.singleton(maybeIcmpType.get());
    } else {
      assert ctx.icmp6_only_type() != null;
      todo(ctx);
      return;
    }
    _currentFwTerm.getFroms().add(new FwFromIcmpType(icmpTypeRange));
  }

  @Override
  public void exitFftf_icmp_type_except(Fftf_icmp_type_exceptContext ctx) {
    if (_currentFirewallFamily == Family.INET6) {
      // TODO: support icmpv6
      return;
    }
    SubRange icmpTypeRange;
    if (ctx.uint8_range() != null) {
      icmpTypeRange = toSubRange(ctx.uint8_range());
    } else if (ctx.icmp_type() != null) {
      Optional<Integer> maybeIcmpType = toInteger(ctx, ctx.icmp_type());
      if (!maybeIcmpType.isPresent()) {
        return;
      }
      icmpTypeRange = SubRange.singleton(maybeIcmpType.get());
    } else {
      assert ctx.icmp6_only_type() != null;
      todo(ctx);
      return;
    }
    _currentFwTerm.getFroms().add(new FwFromIcmpTypeExcept(icmpTypeRange));
  }

  @Override
  public void exitFftf_ip_options(Fftf_ip_optionsContext ctx) {
    todo(ctx);
    FwFromIpOptions from = _currentFwTerm.getOrCreateFromIpOptions();
    if (ctx.option.ANY() != null) {
      from.getOptions().addAll(Arrays.asList(IpOptions.values()));
    } else {
      from.getOptions().add(toIpOptions(ctx.option));
    }
  }

  @Override
  public void exitFftf_ip_protocol(Fftf_ip_protocolContext ctx) {
    IpProtocol protocol = toIpProtocol(ctx.ip_protocol());
    FwFrom from = new FwFromProtocol(protocol);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_is_fragment(Fftf_is_fragmentContext ctx) {
    SubRange subRange = SubRange.singleton(0);
    FwFrom from = new FwFromFragmentOffset(subRange, true);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_packet_length(Fftf_packet_lengthContext ctx) {
    SubRange range = toSubRange(ctx.uint16_range());
    FwFrom from = new FwFromPacketLength(range, false);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_packet_length_except(Fftf_packet_length_exceptContext ctx) {
    SubRange range = toSubRange(ctx.uint16_range());
    FwFrom from = new FwFromPacketLength(range, true);
    _currentFwTerm.getFroms().add(from);
  }

  private static @Nonnull SubRange toSubRange(Uint16_rangeContext ctx) {
    int start = toInteger(ctx.start);
    return ctx.end != null ? new SubRange(start, toInteger(ctx.end)) : SubRange.singleton(start);
  }

  private static int toInteger(Uint16Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  @Override
  public void exitFftf_port(Fftf_portContext ctx) {
    SubRange ports = toSubRange(ctx.port_range());
    FwFrom from = new FwFromPort(ports);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_port_except(Fftf_port_exceptContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitFftf_prefix_list(Fftf_prefix_listContext ctx) {
    String name = toString(ctx.name);
    FwFromPrefixList from = new FwFromPrefixList(name);
    _currentFwTerm.getFroms().add(from);
    _configuration.referenceStructure(
        PREFIX_LIST, name, FIREWALL_FILTER_PREFIX_LIST, getLine(ctx.name.start));
  }

  @Override
  public void exitFftf_protocol(Fftf_protocolContext ctx) {
    IpProtocol protocol = toIpProtocol(ctx.ip_protocol());
    FwFrom from = new FwFromProtocol(protocol);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_source_address(Fftf_source_addressContext ctx) {
    FwFrom from;
    IpWildcard ipWildcard = formIpWildCard(ctx.fftfa_address_mask_prefix());
    if (ipWildcard != null) {
      String text = getFullText(ctx.fftfa_address_mask_prefix());
      from =
          ctx.EXCEPT() != null
              ? new FwFromSourceAddressExcept(ipWildcard, text)
              : new FwFromSourceAddress(ipWildcard, text);
      _currentFwTerm.getFroms().add(from);
    }
  }

  @Override
  public void exitFftf_source_port(Fftf_source_portContext ctx) {
    SubRange ports = toSubRange(ctx.port_range());
    FwFrom from = new FwFromSourcePort(ports);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_source_port_except(Fftf_source_port_exceptContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitFftf_source_prefix_list(Fftf_source_prefix_listContext ctx) {
    String name = toString(ctx.name);
    FwFrom from;
    if (ctx.EXCEPT() != null) {
      from = new FwFromSourcePrefixListExcept(name);
    } else {
      from = new FwFromSourcePrefixList(name);
    }
    _currentFwTerm.getFroms().add(from);
    _configuration.referenceStructure(
        PREFIX_LIST, name, FIREWALL_FILTER_SOURCE_PREFIX_LIST, getLine(ctx.name.start));
  }

  @Override
  public void exitFftf_tcp_established(Fftf_tcp_establishedContext ctx) {
    _currentFwTerm.getFroms().add(FwFromTcpFlags.TCP_ESTABLISHED);
  }

  @Override
  public void exitFftf_tcp_flags(Fftf_tcp_flagsContext ctx) {
    List<TcpFlagsMatchConditions> tcpFlags = toTcpFlags(ctx.tcp_flags());
    FwFrom from = FwFromTcpFlags.fromTcpFlags(tcpFlags);
    _currentFwTerm.getFroms().add(from);
  }

  @Override
  public void exitFftf_tcp_initial(Fftf_tcp_initialContext ctx) {
    _currentFwTerm.getFroms().add(FwFromTcpFlags.TCP_INITIAL);
  }

  @Override
  public void exitFftt_accept(Fftt_acceptContext ctx) {
    _currentFwTerm.getThens().add(FwThenAccept.INSTANCE);
  }

  @Override
  public void exitFftt_discard(Fftt_discardContext ctx) {
    _currentFwTerm.getThens().add(FwThenDiscard.INSTANCE);
  }

  @Override
  public void exitFftt_next_ip(Fftt_next_ipContext ctx) {
    FwThenNextIp then = new FwThenNextIp(toPrefix(ctx.prefix));
    _currentFwTerm.getThens().add(then);
    _currentFwTerm.getThens().add(FwThenAccept.INSTANCE);
    _currentFilter.setUsedForFBF(true);
    warn(ctx, "Filter-based forwarding with next-hop-ip is not currently supported");
  }

  @Override
  public void exitFftt_next_term(Fftt_next_termContext ctx) {
    _currentFwTerm.getThens().add(FwThenNextTerm.INSTANCE);
  }

  @Override
  public void exitFftt_nop(Fftt_nopContext ctx) {
    _currentFwTerm.getThens().add(FwThenNop.INSTANCE);
  }

  @Override
  public void exitFftt_reject(Fftt_rejectContext ctx) {
    _currentFwTerm.getThens().add(FwThenDiscard.INSTANCE);
  }

  @Override
  public void exitFftt_routing_instance(Fftt_routing_instanceContext ctx) {
    String name = toString(ctx.name);
    _currentFwTerm.getThens().add(new FwThenRoutingInstance(name));
    _currentFilter.setUsedForFBF(true);
    _configuration.referenceStructure(
        ROUTING_INSTANCE, name, FIREWALL_FILTER_THEN_ROUTING_INSTANCE, getLine(ctx.getStart()));
  }

  @Override
  public void exitFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    if (_hasZones) {
      if (_defaultCrossZoneAction == null) {
        _defaultCrossZoneAction = LineAction.DENY;
      }
      _currentLogicalSystem.setDefaultCrossZoneAction(_defaultCrossZoneAction);
      _currentLogicalSystem.setDefaultInboundAction(LineAction.DENY);
    } else {
      _currentLogicalSystem.setDefaultInboundAction(LineAction.PERMIT);
    }
    _configuration.setExtraLines(null);
  }

  @Override
  public void exitFo_dhcp_relay(Fo_dhcp_relayContext ctx) {
    _currentDhcpRelayGroup = null;
  }

  @Override
  public void exitFod_active_server_group(Fod_active_server_groupContext ctx) {
    String name = toString(ctx.name);
    _currentDhcpRelayGroup.setActiveServerGroup(name);
    _configuration.referenceStructure(
        DHCP_RELAY_SERVER_GROUP,
        name,
        DHCP_RELAY_GROUP_ACTIVE_SERVER_GROUP,
        getLine(ctx.name.getStart()));
  }

  @Override
  public void exitFod_group(Fod_groupContext ctx) {
    _currentDhcpRelayGroup = null;
  }

  @Override
  public void exitFodg_interface(Fodg_interfaceContext ctx) {
    if (ctx.ALL() != null) {
      _currentDhcpRelayGroup.setAllInterfaces(true);
    } else {
      String interfaceName = getInterfaceFullName(ctx.interface_id());
      _currentDhcpRelayGroup.getInterfaces().add(interfaceName);
      _configuration.referenceStructure(
          INTERFACE,
          interfaceName,
          FORWARDING_OPTIONS_DHCP_RELAY_GROUP_INTERFACE,
          getLine(ctx.interface_id().getStop()));
    }
  }

  @Override
  public void exitI_bandwidth(FlatJuniperParser.I_bandwidthContext ctx) {
    long bandwidth = toBandwidth(ctx.bandwidth());
    _currentInterfaceOrRange.setBandwidth((double) bandwidth);
  }

  @Override
  public void exitI_description(I_descriptionContext ctx) {
    String text = toString(ctx.description());
    _currentInterfaceOrRange.setDescription(text);
  }

  @Override
  public void exitI_disable(I_disableContext ctx) {
    _currentInterfaceOrRange.setActive(false);
  }

  @Override
  public void exitI_enable(I_enableContext ctx) {
    _currentInterfaceOrRange.setActive(true);
  }

  @Override
  public void exitI_flexible_vlan_tagging(I_flexible_vlan_taggingContext ctx) {
    if (!_currentInterfaceOrRange.isPhysicalLike()) {
      warn(ctx, "VLAN tagging cannot be configured on " + _currentInterfaceOrRange.getName());
      return;
    }
    _currentInterfaceOrRange.setVlanTagging(VlanTaggingMode.FLEXIBLE_VLAN_TAGGING);
  }

  @Override
  public void exitI_input_vlan_map(I_input_vlan_mapContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitI_output_vlan_map(I_output_vlan_mapContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitI_mtu(I_mtuContext ctx) {
    int size = toInteger(ctx.bytes);
    _currentInterfaceOrRange.setMtu(size);
  }

  @Override
  public void exitI_native_vlan_id(I_native_vlan_idContext ctx) {
    _currentInterfaceOrRange.setNativeVlan(toInt(ctx.id));
  }

  @Override
  public void exitI_unit(I_unitContext ctx) {
    _currentInterfaceOrRange = _currentMasterInterface;
  }

  @Override
  public void exitI_vlan_id(I_vlan_idContext ctx) {
    if (!_currentInterfaceOrRange.isUnit()) {
      warn(ctx, "Expecting vlan-id to be configured on a unit");
      return;
    }
    if (!_currentInterfaceOrRange.getParent().isPhysicalLike()) {
      warn(ctx, "Expecting vlan-id to be configured on a unit of a physical interface");
      return;
    }
    _currentInterfaceOrRange.setVlanId(toInt(ctx.dec()));
  }

  @Override
  public void exitI_vlan_tagging(I_vlan_taggingContext ctx) {
    if (!_currentInterfaceOrRange.isPhysicalLike()) {
      warn(ctx, "VLAN tagging cannot be configured on " + _currentInterfaceOrRange.getName());
      return;
    }
    _currentInterfaceOrRange.setVlanTagging(VlanTaggingMode.VLAN_TAGGING);
  }

  @Override
  public void exitIfe_filter(Ife_filterContext ctx) {
    FilterContext filter = ctx.filter();
    if (filter.direction() == null) {
      return;
    }
    String name = toString(filter.name);
    int line = getLine(filter.name.getStart());
    _configuration.referenceStructure(FIREWALL_FILTER, name, INTERFACE_FILTER, line);
  }

  @Override
  public void exitIfe_interface_mode(Ife_interface_modeContext ctx) {
    if (!_currentInterfaceOrRange.getName().endsWith(".0")) {
      warn(ctx, "Interface mode can only be configured on unit 0");
      return;
    }
    if (ctx.ACCESS() != null) {
      _currentInterfaceOrRange.getEthernetSwitching().setSwitchportMode(SwitchportMode.ACCESS);
    } else if (ctx.TRUNK() != null) {
      _currentInterfaceOrRange.getEthernetSwitching().setSwitchportMode(SwitchportMode.TRUNK);
    } else {
      todo(ctx);
    }
  }

  @Override
  public void exitIfe_native_vlan_id(Ife_native_vlan_idContext ctx) {
    _currentInterfaceOrRange.getEthernetSwitching().setNativeVlan(toInt(ctx.id));
  }

  @Override
  public void exitIfe_port_mode(Ife_port_modeContext ctx) {
    if (ctx.TRUNK() != null) {
      _currentInterfaceOrRange.getEthernetSwitching().setSwitchportMode(SwitchportMode.TRUNK);
    }
  }

  @Override
  public void exitIfe_vlan(Ife_vlanContext ctx) {
    if (!_currentInterfaceOrRange.getName().endsWith(".0")) {
      warn(ctx, "Vlan members can only be configured on unit 0");
      return;
    }
    if (ctx.range() != null) {
      IntegerSpace range = IntegerSpace.unionOf(toRange(ctx.range()).toArray(new SubRange[] {}));
      _currentInterfaceOrRange.getEthernetSwitching().getVlanMembers().add(new VlanRange(range));
    } else if (ctx.name != null) {
      String name = toString(ctx.name);
      _configuration.referenceStructure(VLAN, name, INTERFACE_VLAN, getLine(ctx.name.getStart()));
      _currentInterfaceOrRange.getEthernetSwitching().getVlanMembers().add(new VlanReference(name));
    } else if (ctx.ALL() != null) {
      _currentInterfaceOrRange.getEthernetSwitching().getVlanMembers().add(AllVlans.instance());
    } else if (ctx.DEFAULT() != null) {
      _currentInterfaceOrRange.getEthernetSwitching().getVlanMembers().clear();
    } else {
      todo(ctx);
    }
  }

  @Override
  public void exitIfi_address(Ifi_addressContext ctx) {
    _currentInterfaceAddress = null;
  }

  @Override
  public void exitIfi_destination_udp_port(Ifi_destination_udp_portContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitIfi_filter(Ifi_filterContext ctx) {
    FilterContext filter = ctx.filter();
    if (filter.direction() == null) {
      return;
    }
    String name = toString(filter.name);
    JuniperStructureUsage usage = INTERFACE_FILTER;
    if (filter.direction() != null) {
      DirectionContext direction = filter.direction();
      if (direction.INPUT() != null) {
        _currentInterfaceOrRange.setIncomingFilter(name);
        usage = INTERFACE_INCOMING_FILTER;
      } else if (direction.INPUT_LIST() != null) {
        _currentInterfaceOrRange.addIncomingFilterList(name);
        usage = INTERFACE_INCOMING_FILTER_LIST;
      } else if (direction.OUTPUT() != null) {
        _currentInterfaceOrRange.setOutgoingFilter(name);
        usage = INTERFACE_OUTGOING_FILTER;
      } else if (direction.OUTPUT_LIST() != null) {
        _currentInterfaceOrRange.addOutgoingFilterList(name);
        usage = INTERFACE_OUTGOING_FILTER_LIST;
      } else {
        // Should be unreachable.
        warn(ctx, "Unhandled filter direction");
      }
    }
    _configuration.referenceStructure(
        FIREWALL_FILTER, name, usage, getLine(filter.name.getStart()));
  }

  @Override
  public void exitIfi_tcp_mss(Ifi_tcp_mssContext ctx) {
    int tcpMss = toInt(ctx.size);
    _currentInterfaceOrRange.setTcpMss(tcpMss);
    todo(ctx);
  }

  @Override
  public void exitIfia_arp(Ifia_arpContext ctx) {
    Ip ip = toIp(ctx.ip);
    _currentInterfaceOrRange.setAdditionalArpIps(
        ImmutableSet.<Ip>builder()
            .addAll(_currentInterfaceOrRange.getAdditionalArpIps())
            .add(ip)
            .build());
  }

  @Override
  public void exitIfia_preferred(Ifia_preferredContext ctx) {
    _currentInterfaceOrRange.setPreferredAddress(_currentInterfaceAddress);
  }

  @Override
  public void exitIfia_primary(Ifia_primaryContext ctx) {
    _currentInterfaceOrRange.setPrimaryAddress(_currentInterfaceAddress);
  }

  @Override
  public void exitIfia_vrrp_group(Ifia_vrrp_groupContext ctx) {
    _currentVrrpGroup = null;
  }

  @Override
  public void exitIfiav_preempt(Ifiav_preemptContext ctx) {
    _currentVrrpGroup.setPreempt(true);
  }

  @Override
  public void exitIfiav_no_preempt(Ifiav_no_preemptContext ctx) {
    _currentVrrpGroup.setPreempt(false);
  }

  @Override
  public void exitIfiav_priority(Ifiav_priorityContext ctx) {
    int priority = toInt(ctx.priority);
    _currentVrrpGroup.setPriority(priority);
  }

  @Override
  public void exitIfiav_virtual_address(Ifiav_virtual_addressContext ctx) {
    Ip virtualAddress = toIp(ctx.ip_address());
    if (!_currentInterfaceAddress.getPrefix().containsIp(virtualAddress)) {
      _w.fatalRedFlag(
          "Cannot assign virtual-address %s outside of subnet for inet address %s",
          virtualAddress, _currentInterfaceAddress);
      return;
    }
    _currentVrrpGroup.addVirtualAddress(virtualAddress);
  }

  @Override
  public void exitIfiso_address(Ifiso_addressContext ctx) {
    IsoAddress address = toIsoAddress(ctx.iso_address());
    _currentInterfaceOrRange.setIsoAddress(address);
  }

  private static @Nonnull IsoAddress toIsoAddress(Iso_addressContext ctx) {
    return new IsoAddress(ctx.getText());
  }

  @Override
  public void exitIfiso_destination_udp_port(Ifiso_destination_udp_portContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitIntir_member(Intir_memberContext ctx) {
    String member =
        unquote(
            (ctx.interface_id() == null ? ctx.DOUBLE_QUOTED_STRING() : ctx.interface_id())
                .getText(),
            ctx);
    try {
      InterfaceRangeMember mc = new InterfaceRangeMember(member);
      ((InterfaceRange) _currentInterfaceOrRange).getMembers().add(mc);
    } catch (IllegalArgumentException e) {
      _w.redFlagf(
          "Could not include member '%s' in interface range '%s': %s",
          member, _currentInterfaceOrRange.getName(), e.getMessage());
    }
  }

  @Override
  public void exitIfm_destination_udp_port(Ifm_destination_udp_portContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitIfi6_destination_udp_port(Ifi6_destination_udp_portContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitIfi6f_input(Ifi6f_inputContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        FIREWALL_INET6_FILTER, name, INTERFACE_FILTER, getLine(ctx.name.start));
  }

  @Override
  public void exitIntir_member_range(Intir_member_rangeContext ctx) {
    String from = ctx.from_i.getText();
    String to = ctx.to_i.getText();
    try {
      InterfaceRangeMemberRange range = new InterfaceRangeMemberRange(from, to);
      ((InterfaceRange) _currentInterfaceOrRange).getMemberRanges().add(range);
    } catch (IllegalArgumentException e) {
      _w.redFlagf(
          "Could not include member range '%s to %s' in interface-range '%s': %s",
          from, to, _currentInterfaceOrRange.getName(), e.getMessage());
    }
  }

  @Override
  public void exitInt_named(Int_namedContext ctx) {
    _currentInterfaceOrRange = null;
    _currentMasterInterface = null;
  }

  @Override
  public void exitIs_export(Is_exportContext ctx) {
    _currentRoutingInstance
        .getIsisSettings()
        .getExportPolicies()
        .add(toComplexPolicyStatement(ctx.expr, ISIS_EXPORT_POLICY));
  }

  @Override
  public void exitIs_ignore_attached_bit(Is_ignore_attached_bitContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitIs_import(Is_importContext ctx) {
    _configuration.referenceStructure(
        POLICY_STATEMENT, toString(ctx.name), ISIS_IMPORT_POLICY, getLine(ctx.name.getStart()));
    todo(ctx);
  }

  @Override
  public void exitIs_interface(Is_interfaceContext ctx) {
    _currentIsisInterfaceSettings = null;
  }

  @Override
  public void exitIs_level(Is_levelContext ctx) {
    _currentIsisLevelSettings = null;
  }

  @Override
  public void exitIs_no_ipv4_routing(Is_no_ipv4_routingContext ctx) {
    _currentRoutingInstance.getIsisSettings().setNoIpv4Routing(true);
  }

  @Override
  public void exitIs_overload(Is_overloadContext ctx) {
    _currentRoutingInstance.getIsisSettings().setOverload(true);
    if (ctx.iso_timeout() != null) {
      _currentRoutingInstance.getIsisSettings().setOverloadTimeout(toInt(ctx.iso_timeout().dec()));
    }
  }

  @Override
  public void exitIs_reference_bandwidth(Is_reference_bandwidthContext ctx) {
    long referenceBandwidth = toBandwidth(ctx.bandwidth());
    _currentRoutingInstance.getIsisSettings().setReferenceBandwidth((double) referenceBandwidth);
  }

  @Override
  public void exitIsi_disable(Isi_disableContext ctx) {
    _currentIsisInterfaceSettings.setEnabled(false);
  }

  @Override
  public void exitIsi_level(Isi_levelContext ctx) {
    _currentIsisInterfaceLevelSettings = null;
  }

  @Override
  public void exitIsi_passive(Isi_passiveContext ctx) {
    _currentIsisInterfaceSettings.setPassive(true);
  }

  @Override
  public void exitIsi_point_to_point(Isi_point_to_pointContext ctx) {
    _currentIsisInterfaceSettings.setPointToPoint(true);
  }

  @Override
  public void exitIsib_minimum_interval(Isib_minimum_intervalContext ctx) {
    _currentIsisInterfaceSettings.setBfdLivenessDetectionMinimumInterval(toInt(ctx.dec()));
  }

  @Override
  public void exitIsib_multiplier(Isib_multiplierContext ctx) {
    _currentIsisInterfaceSettings.setBfdLivenessDetectionMultiplier(toInt(ctx.dec()));
  }

  @Override
  public void exitIsil_disable(Isil_disableContext ctx) {
    _currentIsisInterfaceLevelSettings.setEnabled(false);
  }

  @Override
  public void exitIsil_hello_authentication_key(Isil_hello_authentication_keyContext ctx) {
    String key = applySecret(ctx.key);
    _currentIsisInterfaceLevelSettings.setHelloAuthenticationKey(key);
  }

  @Override
  public void exitIsil_hello_authentication_type(Isil_hello_authentication_typeContext ctx) {
    _currentIsisInterfaceLevelSettings.setHelloAuthenticationType(
        toIsisHelloAuthenticationType(ctx.hello_authentication_type()));
  }

  @Override
  public void exitIsil_hello_interval(Isil_hello_intervalContext ctx) {
    _currentIsisInterfaceLevelSettings.setHelloInterval(toInt(ctx.dec()));
  }

  @Override
  public void exitIsil_hold_time(Isil_hold_timeContext ctx) {
    _currentIsisInterfaceLevelSettings.setHoldTime(toInt(ctx.dec()));
  }

  @Override
  public void exitIsil_metric(Isil_metricContext ctx) {
    int metric = toInt(ctx.dec());
    _currentIsisInterfaceLevelSettings.setMetric(metric);
  }

  @Override
  public void exitIsil_passive(Isil_passiveContext ctx) {
    _currentIsisInterfaceLevelSettings.setPassive(true);
  }

  @Override
  public void exitIsil_priority(Isil_priorityContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitIsil_te_metric(Isil_te_metricContext ctx) {
    int teMetric = toInt(ctx.dec());
    _currentIsisInterfaceLevelSettings.setTeMetric(teMetric);
  }

  @Override
  public void exitIsl_disable(Isl_disableContext ctx) {
    _currentIsisLevelSettings.setEnabled(false);
  }

  @Override
  public void exitIsl_wide_metrics_only(Isl_wide_metrics_onlyContext ctx) {
    _currentIsisLevelSettings.setWideMetricsOnly(true);
  }

  @Override
  public void exitIst_credibility_protocol_preference(
      Ist_credibility_protocol_preferenceContext ctx) {
    _currentRoutingInstance
        .getIsisSettings()
        .setTrafficEngineeringCredibilityProtocolPreference(true);
  }

  @Override
  public void exitIst_family_shortcuts(Ist_family_shortcutsContext ctx) {
    if (ctx.INET6() != null) {
      todo(ctx);
    } else { // ipv4
      _currentRoutingInstance.getIsisSettings().setTrafficEngineeringShortcuts(true);
    }
  }

  @Override
  public void exitMplsi_admin_group(Mplsi_admin_groupContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ADMIN_GROUP, name, MPLS_INTERFACE_ADMIN_GROUP, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitMplsi_srlg(Mplsi_srlgContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        SRLG, name, MPLS_INTERFACE_SRLG, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitNatp_address(Natp_addressContext ctx) {
    if (ctx.from_prefix != null) {
      // from IP_PREFIX to IP_PREFIX
      // Juniper will treat IP_PREFIX as IP ADDRESS
      _currentNatPool.setFromAddress(toConcreteInterfaceAddress(ctx.from_prefix).getIp());
      _currentNatPool.setToAddress(toConcreteInterfaceAddress(ctx.to_prefix).getIp());
    } else if (ctx.from_address != null) {
      // from IP_ADDRESS to IP_ADDRESS
      _currentNatPool.setFromAddress(toIp(ctx.from_address));
      _currentNatPool.setToAddress(toIp(ctx.to_address));
    } else if (ctx.prefix != null) {
      // address IP_PREFIX
      Prefix prefix = toPrefix(ctx.prefix);
      _currentNatPool.setFromAddress(prefix.getFirstHostIp());
      _currentNatPool.setToAddress(prefix.getLastHostIp());
      if (ctx.port_num != null) {
        int port = toInt(ctx.port_num);
        _currentNatPool.setPortAddressTranslation(new PatPool(port, port));
      }
    } else {
      // this command can only happen for destination nat, and when port is given we need to enable
      // port translation
      Ip ip = toIp(ctx.ip);
      _currentNatPool.setFromAddress(ip);
      _currentNatPool.setToAddress(ip);
      if (ctx.port_num != null) {
        int port = toInt(ctx.port_num);
        _currentNatPool.setPortAddressTranslation(new PatPool(port, port));
      }
    }
  }

  private static @Nonnull ConcreteInterfaceAddress toConcreteInterfaceAddress(
      Ip_prefixContext ctx) {
    return ConcreteInterfaceAddress.parse(ctx.getText());
  }

  private static @Nonnull Ip toIp(Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private static @Nonnull Ip6 toIp6(Ipv6_addressContext ctx) {
    return Ip6.parse(ctx.getText());
  }

  @Override
  public void exitNatp_port(Natp_portContext ctx) {
    if (ctx.NO_TRANSLATION() != null) {
      _currentNatPool.setPortAddressTranslation(NoPortTranslation.INSTANCE);
    } else if (ctx.RANGE() != null) {
      int fromPort = toInt(ctx.from);
      int toPort = toInt(ctx.to);
      _currentNatPool.setPortAddressTranslation(new PatPool(fromPort, toPort));
    } else {
      _w.redFlag(ctx.getText() + " cannot be recognized");
    }
  }

  @Override
  public void exitNatp_routing_instance(Natp_routing_instanceContext ctx) {
    String ri = toString(ctx.name);
    _currentNatPool.setOwner(ri);
    todo(ctx);
  }

  @Override
  public void exitNat_pool_default_port_range(Nat_pool_default_port_rangeContext ctx) {
    _currentNat.setDefaultFromPort(toInt(ctx.low));
    if (ctx.TO() != null) {
      _currentNat.setDefaultToPort(toInt(ctx.high));
    }
  }

  @Override
  public void exitO_area(O_areaContext ctx) {
    _currentArea = null;
  }

  @Override
  public void exitO_disable(O_disableContext ctx) {
    _currentRoutingInstance.setOspfDisable(true);
  }

  @Override
  public void exitO_enable(O_enableContext ctx) {
    _currentRoutingInstance.setOspfDisable(false);
  }

  @Override
  public void exitO_export(O_exportContext ctx) {
    _currentRoutingInstance
        .getOspfExportPolicies()
        .add(toComplexPolicyStatement(ctx.expr, OSPF_EXPORT_POLICY));
  }

  @Override
  public void exitO_prefix_export_limit(O_prefix_export_limitContext ctx) {
    warn(ctx, "Batfish does not limit prefix-export-limit");
  }

  @Override
  public void exitO_reference_bandwidth(O_reference_bandwidthContext ctx) {
    long referenceBandwidth = toBandwidth(ctx.bandwidth());
    _currentRoutingInstance.setOspfReferenceBandwidth((double) referenceBandwidth);
  }

  @Override
  public void exitOa_area_range(FlatJuniperParser.Oa_area_rangeContext ctx) {
    if (_currentAreaRangePrefix != null) {
      OspfAreaSummary summary =
          new OspfAreaSummary(
              _currentAreaRangeRestrict
                  ? SummaryRouteBehavior.NOT_ADVERTISE_AND_INSTALL_DISCARD
                  : SummaryRouteBehavior.ADVERTISE_AND_INSTALL_DISCARD,
              _currentAreaRangeMetric);
      _currentArea.getSummaries().put(_currentAreaRangePrefix, summary);
    } else {
      todo(ctx);
    }
  }

  @Override
  public void exitOa_interface(Oa_interfaceContext ctx) {
    _currentOspfSettings = null;
  }

  @Override
  public void exitOaa_override_metric(FlatJuniperParser.Oaa_override_metricContext ctx) {
    _currentAreaRangeMetric = toLong(ctx.dec());
  }

  @Override
  public void exitOaa_restrict(FlatJuniperParser.Oaa_restrictContext ctx) {
    _currentAreaRangeRestrict = true;
  }

  @Override
  public void exitOai_disable(Oai_disableContext ctx) {
    _currentOspfSettings.setOspfDisable(true);
  }

  @Override
  public void exitOai_enable(Oai_enableContext ctx) {
    _currentOspfSettings.setOspfDisable(false);
  }

  @Override
  public void exitOai_interface_type(Oai_interface_typeContext ctx) {
    OspfInterfaceType type = toOspfInterfaceType(ctx.type);
    if (type != null) {
      _currentOspfSettings.setOspfInterfaceType(toOspfInterfaceType(ctx.type));
    }
  }

  @Override
  public void exitOai_dead_interval(Oai_dead_intervalContext ctx) {
    toIntegerInSpace(ctx, ctx.uint16(), OSPF_DEAD_INTERVAL_RANGE, "OSPF dead interval")
        .ifPresent(_currentOspfSettings::setOspfDeadInterval);
  }

  @Override
  public void exitOai_hello_interval(Oai_hello_intervalContext ctx) {
    toIntegerInSpace(ctx, ctx.uint8(), OSPF_HELLO_INTERVAL_RANGE, "OSPF hello interval")
        .ifPresent(_currentOspfSettings::setOspfHelloInterval);
  }

  @Override
  public void exitOai_neighbor(Oai_neighborContext ctx) {
    Ip neighborIp = toIp(ctx.ip_address());
    InterfaceOspfNeighbor neighbor = new InterfaceOspfNeighbor(neighborIp);
    if (ctx.ELIGIBLE() != null) {
      neighbor.setDesignated(true);
    }
    _currentOspfSettings.getOspfNeighbors().add(neighbor);
  }

  @Override
  public void exitOai_metric(FlatJuniperParser.Oai_metricContext ctx) {
    int ospfCost = toInt(ctx.dec());
    _currentOspfSettings.setOspfCost(ospfCost);
  }

  @Override
  public void exitP_bgp(P_bgpContext ctx) {
    _currentBgpGroup = null;
  }

  @Override
  public void exitPo_community(Po_communityContext ctx) {
    _currentCommunityList = null;
  }

  @Override
  public void exitPo_policy_statement(Po_policy_statementContext ctx) {
    _currentPolicyStatement = null;
  }

  @Override
  public void exitPo_prefix_list(Po_prefix_listContext ctx) {
    _currentPrefixList = null;
  }

  @Override
  public void exitPoc_invert_match(Poc_invert_matchContext ctx) {
    _currentCommunityList.setInvertMatch(true);
  }

  @Override
  public void exitPoc_members(Poc_membersContext ctx) {
    _currentCommunityList.getMembers().add(toCommunityMember(ctx.member));
  }

  private @Nonnull CommunityMember toCommunityMember(Poc_members_memberContext ctx) {
    String text = ctx.getText();
    if (ctx.literal_or_regex_community() != null) {
      CommunityMemberParseResult result = parseCommunityMember(text);
      if (result.getWarning() != null) {
        warnRisky(ctx, result.getWarning());
      }
      return result.getMember();
    } else {
      assert ctx.sc_named() != null;
      return new LiteralCommunityMember(toStandardCommunity(ctx.sc_named()));
    }
  }

  @Override
  public void exitPoplt_network(Poplt_networkContext ctx) {
    Prefix prefix = toPrefix(ctx.network);
    _currentPrefixList.getPrefixes().add(prefix);
  }

  @Override
  public void exitPoplt_network6(Poplt_network6Context ctx) {
    _currentPrefixList.setHasIpv6(true);
  }

  @Override
  public void exitPops_common(Pops_commonContext ctx) {
    _currentPsTerm = null;
    _currentPsThens = null;
  }

  @Override
  public void exitPopsf_area(Popsf_areaContext ctx) {
    todo(ctx);
    _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
  }

  @Override
  public void exitPopsf_as_path(Popsf_as_pathContext ctx) {
    String name = toString(ctx.name);
    _currentPsTerm.getFroms().addFromAsPath(new PsFromAsPath(name));
    _configuration.referenceStructure(
        AS_PATH, name, POLICY_STATEMENT_FROM_AS_PATH, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitPopsf_as_path_group(Popsf_as_path_groupContext ctx) {
    String name = toString(ctx.name);
    _currentPsTerm.getFroms().addFromAsPathGroup(new PsFromAsPathGroup(name));
    _configuration.referenceStructure(
        AS_PATH_GROUP, name, POLICY_STATEMENT_FROM_AS_PATH_GROUP, getLine(ctx.getStart()));
  }

  @Override
  public void exitPopsf_color(Popsf_colorContext ctx) {
    long color = toLong(ctx.color);
    _currentPsTerm.getFroms().setFromColor(new PsFromColor(color));
  }

  @Override
  public void exitPopsf_community(Popsf_communityContext ctx) {
    String name = toString(ctx.name);
    _currentPsTerm.getFroms().addFromCommunity(new PsFromCommunity(name));
    _configuration.referenceStructure(
        COMMUNITY, name, POLICY_STATEMENT_FROM_COMMUNITY, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitPopsf_community_count(Popsf_community_countContext ctx) {
    int count = toInteger(ctx.n.uint16());
    Mode mode = Mode.EXACT;
    if (ctx.ORHIGHER() != null) {
      mode = Mode.ORHIGHER;
    } else if (ctx.ORLOWER() != null) {
      mode = Mode.ORLOWER;
    }
    PsFromCommunityCount from = new PsFromCommunityCount(count, mode);
    _currentPsTerm.getFroms().setFromCommunityCount(from);
  }

  @Override
  public void exitPopsf_condition(FlatJuniperParser.Popsf_conditionContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        CONDITION, name, POLICY_STATEMENT_FROM_CONDITION, getLine(ctx.getStart()));
    _currentPsTerm.getFroms().addFromCondition(new PsFromCondition(name));
  }

  @Override
  public void exitPopsf_family(Popsf_familyContext ctx) {
    if (ctx.INET() != null) {
      _currentPsTerm.getFroms().setFromFamily(new PsFromFamily(AddressFamily.IPV4));
    } else if (ctx.INET6() != null) {
      _currentPsTerm.getFroms().setFromFamily(new PsFromFamily(AddressFamily.IPV6));
    } else {
      todo(ctx);
      _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
    }
  }

  @Override
  public void exitPopsf_instance(Popsf_instanceContext ctx) {
    String instanceName = toString(ctx.name);
    _configuration.referenceStructure(
        ROUTING_INSTANCE,
        instanceName,
        POLICY_STATEMENT_FROM_INSTANCE,
        getLine(ctx.name.getStart()));
    _currentPsTerm.getFroms().setFromInstance(new PsFromInstance(instanceName));
  }

  @Override
  public void exitPopsf_interface(Popsf_interfaceContext ctx) {
    String ifaceName = getInterfaceFullName(ctx.id);
    _currentPsTerm.getFroms().addFromInterface(new PsFromInterface(ifaceName));
    _configuration.referenceStructure(
        INTERFACE, ifaceName, POLICY_STATEMENT_FROM_INTERFACE, getLine(ctx.id.getStop()));
  }

  @Override
  public void exitPopsf_level(Popsf_levelContext ctx) {
    todo(ctx);
    _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
  }

  @Override
  public void exitPopsf_local_preference(Popsf_local_preferenceContext ctx) {
    todo(ctx.getParent());
    _currentPsTerm
        .getFroms()
        .setFromLocalPreference(new PsFromLocalPreference(toLong(ctx.localpref)));
  }

  @Override
  public void exitPopsf_metric(Popsf_metricContext ctx) {
    long metric = toLong(ctx.metric);
    _currentPsTerm.getFroms().setFromMetric(new PsFromMetric(metric));
  }

  @Override
  public void exitPopsf_metric2(Popsf_metric2Context ctx) {
    todo(ctx);
    _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
  }

  @Override
  public void exitPopsf_neighbor(Popsf_neighborContext ctx) {
    PsFromNeighbor.Neighbor neighbor;
    if (ctx.v4 != null) {
      neighbor = PsFromNeighbor.Neighbor.of(toIp(ctx.v4));
    } else {
      assert ctx.v6 != null;
      neighbor = PsFromNeighbor.Neighbor.of(toIp6(ctx.v6));
    }
    _currentPsTerm.getFroms().addFromNeighbor(new PsFromNeighbor(neighbor));
  }

  @Override
  public void exitPopsf_next_hop(Popsf_next_hopContext ctx) {
    Hop hop;
    if (ctx.v4 != null) {
      hop = Hop.of(toIp(ctx.v4));
    } else {
      assert ctx.v6 != null;
      hop = Hop.of(toIp6(ctx.v6));
    }
    _currentPsTerm.getFroms().addFromNextHop(new PsFromNextHop(hop));
  }

  @Override
  public void exitPopsf_origin(Popsf_originContext ctx) {
    todo(ctx);
    _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
  }

  @Override
  public void exitPopsf_policy(Popsf_policyContext ctx) {
    String policyName = toComplexPolicyStatement(ctx.policy_expression(), POLICY_STATEMENT_POLICY);
    _currentPsTerm.getFroms().addFromPolicyStatement(new PsFromPolicyStatement(policyName));
  }

  @Override
  public void exitPopsf_prefix_list(Popsf_prefix_listContext ctx) {
    String name = toString(ctx.name);
    _currentPsTerm.getFroms().addFromPrefixList(new PsFromPrefixList(name));
    _configuration.referenceStructure(
        PREFIX_LIST, name, POLICY_STATEMENT_PREFIX_LIST, getLine(ctx.name.start));
  }

  @Override
  public void exitPopsf_rtf_prefix_list(Popsf_rtf_prefix_listContext ctx) {
    todo(ctx);
    String name = toString(ctx.name);
    _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
    _configuration.referenceStructure(
        RTF_PREFIX_LIST, name, POLICY_STATEMENT_RTF_PREFIX_LIST, getLine(ctx.name.start));
  }

  @Override
  public void exitPopsf_prefix_list_filter(Popsf_prefix_list_filterContext ctx) {
    String name = toString(ctx.name);
    PsFroms currentFroms = _currentPsTerm.getFroms();
    if (ctx.popsfpl_exact() != null) {
      currentFroms.addFromPrefixList(new PsFromPrefixList(name));
    } else if (ctx.popsfpl_longer() != null) {
      currentFroms.addFromPrefixListFilterLonger(new PsFromPrefixListFilterLonger(name));
    } else if (ctx.popsfpl_orlonger() != null) {
      currentFroms.addFromPrefixListFilterOrLonger(new PsFromPrefixListFilterOrLonger(name));
    } else {
      throw new BatfishException("Invalid prefix-list-filter length specification");
    }
    _configuration.referenceStructure(
        PREFIX_LIST, name, POLICY_STATEMENT_PREFIX_LIST_FILTER, getLine(ctx.name.start));
  }

  @Override
  public void exitPopsf_protocol(Popsf_protocolContext ctx) {
    RoutingProtocol protocol;
    if (ctx.AGGREGATE() != null) {
      protocol = RoutingProtocol.AGGREGATE;
    } else if (ctx.BGP() != null) {
      protocol = RoutingProtocol.BGP;
    } else if (ctx.DIRECT() != null) {
      protocol = RoutingProtocol.CONNECTED;
    } else if (ctx.ISIS() != null) {
      protocol = RoutingProtocol.ISIS_ANY;
    } else if (ctx.EVPN() != null) {
      protocol = RoutingProtocol.EVPN;
    } else if (ctx.LDP() != null) {
      protocol = RoutingProtocol.LDP;
    } else if (ctx.LOCAL() != null) {
      protocol = RoutingProtocol.LOCAL;
    } else if (ctx.OSPF() != null) {
      protocol = RoutingProtocol.OSPF;
    } else if (ctx.OSPF3() != null) {
      protocol = RoutingProtocol.OSPF3;
    } else if (ctx.RSVP() != null) {
      protocol = RoutingProtocol.RSVP;
    } else if (ctx.STATIC() != null) {
      protocol = RoutingProtocol.STATIC;
    } else {
      todo(ctx);
      _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
      return;
    }
    _currentPsTerm.getFroms().addFromProtocol(new PsFromProtocol(protocol));
  }

  @Override
  public void exitPopsf_rib(Popsf_ribContext ctx) {
    todo(ctx);
    _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
  }

  @Override
  public void exitPopsf_route_filter(Popsf_route_filterContext ctx) {
    _currentRouteFilterPrefix = null;
    _currentRoute6FilterPrefix = null;
    _currentRouteFilter = null;
    _currentRouteFilterLine = null;
    _currentRoute6FilterLine = null;
  }

  @Override
  public void exitPopsf_external(Popsf_externalContext ctx) {
    if (ctx.ospf_type() == null) {
      // No type specified - match all external routes
      _currentPsTerm.getFroms().setFromExternal(new PsFromExternal(null));
    } else {
      Optional<Integer> maybeType =
          toIntegerInSpace(
              ctx, ctx.ospf_type().uint8(), OSPF_EXTERNAL_TYPE_RANGE, "OSPF external type");
      if (maybeType.isPresent()) {
        int type = maybeType.get();
        if (type == 1) {
          _currentPsTerm.getFroms().setFromExternal(new PsFromExternal(OspfMetricType.E1));
        } else if (type == 2) {
          _currentPsTerm.getFroms().setFromExternal(new PsFromExternal(OspfMetricType.E2));
        }
      }
    }
  }

  @Override
  public void exitPopsf_route_type(Popsf_route_typeContext ctx) {
    if (ctx.EXTERNAL() != null) {
      _currentPsTerm
          .getFroms()
          .setFromRouteType(new PsFromRouteType(PsFromRouteType.Type.EXTERNAL));
    } else {
      assert ctx.INTERNAL() != null;
      _currentPsTerm
          .getFroms()
          .setFromRouteType(new PsFromRouteType(PsFromRouteType.Type.INTERNAL));
    }
  }

  @Override
  public void exitPopsf_source_address_filter(Popsf_source_address_filterContext ctx) {
    todo(ctx);
    _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
  }

  @Override
  public void exitPopsf_tag(Popsf_tagContext ctx) {
    long tag = toLong(ctx.uint32());
    _currentPsTerm.getFroms().addFromTag(new PsFromTag(tag));
  }

  @Override
  public void exitPopsf_tag2(Popsf_tag2Context ctx) {
    todo(ctx);
    _currentPsTerm.getFroms().setFromUnsupported(new PsFromUnsupported());
  }

  @Override
  public void exitPopsto_level(Popsto_levelContext ctx) {
    todo(ctx);
    _currentPsTerm.setHasToConditions(true);
  }

  @Override
  public void exitPopsto_rib(Popsto_ribContext ctx) {
    todo(ctx);
    _currentPsTerm.setHasToConditions(true);
  }

  @Override
  public void exitPopsfrf_then(Popsfrf_thenContext ctx) {
    _currentPsThens = _currentPsTerm.getThens();
  }

  @Override
  public void exitPopst_accept(Popst_acceptContext ctx) {
    addPsThen(PsThenAccept.INSTANCE, ctx);
  }

  @Override
  public void exitPopst_as_path_prepend(Popst_as_path_prependContext ctx) {
    List<Long> asPaths =
        ctx.bgp_asn().stream().map(this::toAsNum).collect(ImmutableList.toImmutableList());
    addPsThen(new PsThenAsPathPrepend(asPaths), ctx);
  }

  @Override
  public void exitPopst_as_path_expand(Popst_as_path_expandContext ctx) {
    PsThenAsPathExpand expand;
    if (ctx.LAST_AS() != null) {
      Optional<Integer> maybeCount = ctx.count != null ? toInteger(ctx, ctx.count) : Optional.of(1);
      if (!maybeCount.isPresent()) {
        return;
      }
      expand = new PsThenAsPathExpandLastAs(maybeCount.get());
    } else {
      assert !ctx.asns.isEmpty();
      List<Long> asPaths =
          ctx.bgp_asn().stream().map(this::toAsNum).collect(ImmutableList.toImmutableList());
      expand = new PsThenAsPathExpandAsList(asPaths);
    }
    addPsThen(expand, ctx);
  }

  private static final IntegerSpace AS_PATH_EXPAND_LAST_AS_COUNT_RANGE =
      IntegerSpace.of(new SubRange(1, 32));

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, As_path_expand_countContext ctx) {
    return toIntegerInSpace(
        messageCtx, ctx, AS_PATH_EXPAND_LAST_AS_COUNT_RANGE, "as-path-expand last-as count");
  }

  /**
   * Convert a {@link ParserRuleContext} whose text is guaranteed to represent a valid signed 32-bit
   * decimal integer to an {@link Integer} if it is contained in the provided {@code space}, or else
   * {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, ParserRuleContext ctx, IntegerSpace space, String name) {
    int num = Integer.parseInt(ctx.getText());
    if (!space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  /**
   * Convert a {@link ParserRuleContext} whose text is guaranteed to represent a valid signed 64-bit
   * decimal integer to an {@link Long} if it is contained in the provided {@code space}, or else
   * {@link Optional#empty}.
   */
  private @Nonnull Optional<Long> toLongInSpace(
      ParserRuleContext messageCtx, ParserRuleContext ctx, LongSpace space, String name) {
    long num = Long.parseLong(ctx.getText());
    if (!space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  @Override
  public void exitPopst_bgp_output_queue_priority(Popst_bgp_output_queue_priorityContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitPopst_color(Popst_colorContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitIso_advertise_high_metrics(Iso_advertise_high_metricsContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitPopst_color2(Popst_color2Context ctx) {
    todo(ctx);
  }

  @Override
  public void exitPopst_community_add(Popst_community_addContext ctx) {
    String name = toString(ctx.name);
    PsThenCommunityAdd then = new PsThenCommunityAdd(name);
    addPsThen(then, ctx);
    _configuration.referenceStructure(
        COMMUNITY, name, POLICY_STATEMENT_THEN_ADD_COMMUNITY, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitPopst_community_delete(Popst_community_deleteContext ctx) {
    String name = toString(ctx.name);
    addPsThen(new PsThenCommunityDelete(name), ctx);
    _configuration.referenceStructure(
        COMMUNITY, name, POLICY_STATEMENT_THEN_DELETE_COMMUNITY, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitPopst_community_set(Popst_community_setContext ctx) {
    String name = toString(ctx.name);
    PsThenCommunitySet then = new PsThenCommunitySet(name);
    addPsThen(then, ctx);
    _configuration.referenceStructure(
        COMMUNITY, name, POLICY_STATEMENT_THEN_SET_COMMUNITY, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitPopst_default_action_accept(Popst_default_action_acceptContext ctx) {
    addPsThen(new PsThenDefaultActionAccept(), ctx);
  }

  @Override
  public void exitPopst_default_action_reject(Popst_default_action_rejectContext ctx) {
    addPsThen(new PsThenDefaultActionReject(), ctx);
  }

  @Override
  public void exitPopst_external(Popst_externalContext ctx) {
    int type = toInt(ctx.dec());
    if (type == 1) {
      addPsThen(new PsThenExternal(OspfMetricType.E1), ctx);
    } else if (type == 2) {
      addPsThen(new PsThenExternal(OspfMetricType.E2), ctx);
    } else {
      _w.redFlagf("unimplemented: then %s", getFullText(ctx));
    }
  }

  @Override
  public void exitPopst_local_preference(Popst_local_preferenceContext ctx) {
    PsThenLocalPreference.Operator op =
        ctx.ADD() != null
            ? PsThenLocalPreference.Operator.ADD
            : (ctx.SUBTRACT() != null
                ? PsThenLocalPreference.Operator.SUBTRACT
                : PsThenLocalPreference.Operator.SET);
    addPsThen(new PsThenLocalPreference(toLong(ctx.localpref), op), ctx);
  }

  @Override
  public void exitPopst_metric(Popst_metricContext ctx) {
    int metric = toInt(ctx.metric);
    PsThenMetric then = new PsThenMetric(metric);
    addPsThen(then, ctx);
  }

  @Override
  public void exitPopst_metric_add(Popst_metric_addContext ctx) {
    int metric = toInt(ctx.metric);
    addPsThen(new PsThenMetricAdd(metric), ctx);
  }

  @Override
  public void exitPopst_multipath_resolve(Popst_multipath_resolveContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitPopstnh_discard(Popstnh_discardContext ctx) {
    addPsThen(PsThenNextHopDiscard.INSTANCE, ctx);
  }

  @Override
  public void exitPopstnh_ipv4(Popstnh_ipv4Context ctx) {
    Ip nextHopIp = toIp(ctx.ip_address());
    addPsThen(new PsThenNextHopIp(nextHopIp), ctx);
  }

  @Override
  public void exitPopstnh_ipv6(Popstnh_ipv6Context ctx) {
    todo(ctx);
  }

  @Override
  public void exitPopstnh_peer_address(Popstnh_peer_addressContext ctx) {
    addPsThen(PsThenNextHopPeerAddress.INSTANCE, ctx);
  }

  @Override
  public void exitPopstnh_reject(Popstnh_rejectContext ctx) {
    addPsThen(PsThenNextHopReject.INSTANCE, ctx);
  }

  @Override
  public void exitPopstnh_self(Popstnh_selfContext ctx) {
    addPsThen(PsThenNextHopSelf.INSTANCE, ctx);
  }

  @Override
  public void exitPopst_next_policy(Popst_next_policyContext ctx) {
    addPsThen(PsThenNextPolicy.INSTANCE, ctx);
  }

  @Override
  public void exitPopst_next_term(Popst_next_termContext ctx) {
    // The next-term action itself is a no-op in Batfish, so we do not model this behavior.
    //
    // TODO(https://github.com/batfish/batfish/issues/1551): need to implement next_term replacing
    // any existing flow control action.
  }

  @Override
  public void exitPopst_origin(Popst_originContext ctx) {
    OriginType origin;
    if (ctx.EGP() != null) {
      origin = OriginType.EGP;
    } else if (ctx.IGP() != null) {
      origin = OriginType.IGP;
    } else if (ctx.INCOMPLETE() != null) {
      origin = OriginType.INCOMPLETE;
    } else {
      _w.redFlagf("unimplemented origin type: %s", getFullText(ctx));
      return;
    }
    addPsThen(new PsThenOrigin(origin), ctx);
  }

  @Override
  public void exitPopst_preference(Popst_preferenceContext ctx) {
    int preference = toInt(ctx.preference);
    PsThenPreference then = new PsThenPreference(preference);
    addPsThen(then, ctx);
  }

  @Override
  public void exitPopst_reject(Popst_rejectContext ctx) {
    addPsThen(PsThenReject.INSTANCE, ctx);
  }

  @Override
  public void exitPopst_tag(Popst_tagContext ctx) {
    addPsThen(new PsThenTag(toLong(ctx.uint32())), ctx);
  }

  @Override
  public void exitPopst_tag2(Popst_tag2Context ctx) {
    todo(ctx);
  }

  @Override
  public void exitPopstta_remove(FlatJuniperParser.Popstta_removeContext ctx) {
    if (ctx.ALL() != null) {
      addPsThen(PsThenTunnelAttributeRemove.INSTANCE, ctx);
    } else {
      assert ctx.name != null;
      warn(ctx, "Removing specific tunnel-attributes is not yet supported");
      _configuration.referenceStructure(
          TUNNEL_ATTRIBUTE,
          toString(ctx.name),
          POLICY_STATEMENT_THEN_TUNNEL_ATTRIBUTE,
          getLine(ctx.name.getStop()));
    }
  }

  @Override
  public void exitPopstta_set(FlatJuniperParser.Popstta_setContext ctx) {
    String tunnelAttrName = toString(ctx.name);
    addPsThen(new PsThenTunnelAttributeSet(tunnelAttrName), ctx);
    _configuration.referenceStructure(
        TUNNEL_ATTRIBUTE,
        tunnelAttrName,
        POLICY_STATEMENT_THEN_TUNNEL_ATTRIBUTE,
        getLine(ctx.name.getStop()));
  }

  @Override
  public void enterPo_tunnel_attribute(FlatJuniperParser.Po_tunnel_attributeContext ctx) {
    String name = toString(ctx.name);
    _currentTunnelAttribute =
        _currentLogicalSystem
            .getTunnelAttributes()
            .computeIfAbsent(name, k -> new TunnelAttribute());
    _configuration.defineFlattenedStructure(TUNNEL_ATTRIBUTE, name, ctx, _parser);
  }

  @Override
  public void exitPo_tunnel_attribute(FlatJuniperParser.Po_tunnel_attributeContext ctx) {
    _currentTunnelAttribute = null;
  }

  @Override
  public void exitPota_remote_end_point(FlatJuniperParser.Pota_remote_end_pointContext ctx) {
    _currentTunnelAttribute.setRemoteEndPoint(toIp(ctx.ip_address()));
  }

  @Override
  public void exitPota_tunnel_type(FlatJuniperParser.Pota_tunnel_typeContext ctx) {
    // only known type is IPIP
    assert ctx.IPIP() != null;
    _currentTunnelAttribute.setType(TunnelAttribute.Type.IPIP);
  }

  @Override
  public void exitRi_interface(Ri_interfaceContext ctx) {
    Interface iface = initRoutingInterface(ctx.id);
    iface.setRoutingInstance(_currentRoutingInstance);
    // iface.getOspfSettings().setParent(_currentRoutingInstance.getOspfSettings());
    _configuration.referenceStructure(
        INTERFACE, iface.getName(), ROUTING_INSTANCE_INTERFACE, getLine(ctx.id.getStop()));
  }

  @Override
  public void exitRi_named_routing_instance(Ri_named_routing_instanceContext ctx) {
    _currentRoutingInstance = _currentLogicalSystem.getDefaultRoutingInstance();
  }

  @Override
  public void exitRi_vrf_export(Ri_vrf_exportContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        POLICY_STATEMENT, name, ROUTING_INSTANCE_VRF_EXPORT, getLine(ctx.name.getStart()));
    todo(ctx);
  }

  @Override
  public void exitRi_vrf_import(Ri_vrf_importContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        POLICY_STATEMENT, name, ROUTING_INSTANCE_VRF_IMPORT, getLine(ctx.name.getStart()));
    todo(ctx);
  }

  @Override
  public void exitRi_vtep_source_interface(Ri_vtep_source_interfaceContext ctx) {
    String ifaceName = getInterfaceFullName(ctx.iface);
    _configuration.referenceStructure(
        INTERFACE, ifaceName, VTEP_SOURCE_INTERFACE, getLine(ctx.iface.getStart()));
    todo(ctx);
  }

  @Override
  public void exitRo_autonomous_system(Ro_autonomous_systemContext ctx) {
    if (ctx.asn != null) {
      long as = toAsNum(ctx.asn);
      _currentRoutingInstance.setAs(as);
    }
  }

  @Override
  public void exitRo_confederation(Ro_confederationContext ctx) {
    if (ctx.num != null) {
      _currentRoutingInstance.setConfederation(toLong(ctx.num));
    }
    // Note that Juniper will not allow commit with declared members unless confederation number
    // above is eventually set, even though members can be declared separately from confederation
    // number. So confederation members should not make it into data model when confederation number
    // is not set.
    ctx.member.forEach(mctx -> _currentRoutingInstance.getConfederationMembers().add(toLong(mctx)));
  }

  @Override
  public void exitRo_instance_import(Ro_instance_importContext ctx) {
    String policyName = toString(ctx.name);
    _configuration.referenceStructure(
        POLICY_STATEMENT,
        policyName,
        ROUTING_OPTIONS_INSTANCE_IMPORT,
        getLine(ctx.name.getStart()));
    _currentRoutingInstance.getInstanceImports().add(policyName);
  }

  @Override
  public void exitRo_maximum_prefixes(Ro_maximum_prefixesContext ctx) {
    warn(ctx, "Batfish does not limit maximum-prefixes");
  }

  @Override
  public void exitRo_router_id(Ro_router_idContext ctx) {
    Ip id = toIp(ctx.id);
    _currentRoutingInstance.setRouterId(id);
  }

  @Override
  public void exitRo_static(Ro_staticContext ctx) {
    _currentStaticRoute = null;
  }

  @Override
  public void exitRoa_active(Roa_activeContext ctx) {
    _currentAggregateRoute.setActive(true);
  }

  @Override
  public void exitRoa_community(Roa_communityContext ctx) {
    // TODO: should this rule allow more than literal standard communities?
    StandardCommunity community = StandardCommunity.parse(ctx.STANDARD_COMMUNITY().getText());
    _currentAggregateRoute.getCommunities().add(community);
  }

  @Override
  public void exitRoa_discard(Roa_discardContext ctx) {
    _currentAggregateRoute.setDrop(true);
  }

  @Override
  public void exitRoa_policy(Roa_policyContext ctx) {
    _currentAggregateRoute
        .getPolicies()
        .add(toComplexPolicyStatement(ctx.expr, AGGREGATE_ROUTE_POLICY));
  }

  @Override
  public void exitRoa_preference(Roa_preferenceContext ctx) {
    int preference = toInt(ctx.preference);
    _currentAggregateRoute.setPreference(preference);
  }

  @Override
  public void exitRoa_tag(Roa_tagContext ctx) {
    long tag = toLong(ctx.tag);
    _currentAggregateRoute.setTag(tag);
  }

  @Override
  public void exitRoaa_path(Roaa_pathContext ctx) {
    AsPath asPath = toAsPath(ctx.path);
    _currentAggregateRoute.setAsPath(asPath);
  }

  @Override
  public void exitRoas_loops(Roas_loopsContext ctx) {
    if (ctx.dec() != null) {
      _currentRoutingInstance.setLoops(toInt(ctx.dec()));
      todo(ctx);
    }
  }

  @Override
  public void exitRof_export(Rof_exportContext ctx) {
    _currentLogicalSystem
        .getDefaultRoutingInstance()
        .setForwardingTableExportPolicy(
            toComplexPolicyStatement(ctx.expr, FORWARDING_TABLE_EXPORT_POLICY));
    todo(ctx);
  }

  @Override
  public void exitRog_active(Rog_activeContext ctx) {
    _currentGeneratedRoute.setActive(true);
  }

  @Override
  public void exitRog_community(Rog_communityContext ctx) {
    _currentGeneratedRoute.getCommunities().add(toStandardCommunity(ctx.standard_community()));
  }

  @Override
  public void exitRog_metric(Rog_metricContext ctx) {
    int metric = toInt(ctx.metric);
    _currentGeneratedRoute.setMetric(metric);
  }

  @Override
  public void exitRog_policy(Rog_policyContext ctx) {
    if (_currentGeneratedRoute != null) { // not ipv6
      _currentGeneratedRoute
          .getPolicies()
          .add(toComplexPolicyStatement(ctx.expr, GENERATED_ROUTE_POLICY));
    }
  }

  @Override
  public void exitRog_tag(Rog_tagContext ctx) {
    _currentGeneratedRoute.setTag(toLong(ctx.tag));
  }

  @Override
  public void enterRoifie_lan(Roifie_lanContext ctx) {
    _currentRoutingInstance.setExportLocalRoutesLan(true);
  }

  @Override
  public void exitRoifie_point_to_point(Roifie_point_to_pointContext ctx) {
    _currentRoutingInstance.setExportLocalRoutesPointToPoint(true);
  }

  @Override
  public void exitRoi_rib_group(Roi_rib_groupContext ctx) {
    String groupName = toString(ctx.name);
    _configuration.referenceStructure(
        RIB_GROUP, groupName, INTERFACE_ROUTING_OPTIONS, getLine(ctx.name.getStart()));
    if (ctx.INET() != null) {
      _currentRoutingInstance.applyRibGroup(RoutingProtocol.CONNECTED, groupName);
      _currentRoutingInstance.applyRibGroup(RoutingProtocol.LOCAL, groupName);
    }
    if (ctx.INET6() != null) {
      todo(ctx);
    }
  }

  @Override
  public void exitRosr_active(Rosr_activeContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitRosr_as_path(Rosr_as_pathContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitRosr_community(Rosr_communityContext ctx) {
    _currentStaticRoute.getCommunities().add(toStandardCommunity(ctx.standard_community()));
  }

  @Override
  public void exitRosr_discard(Rosr_discardContext ctx) {
    _currentStaticRoute.setDrop();
  }

  @Override
  public void exitRosr_install(Rosr_installContext ctx) {
    _currentStaticRoute.setInstall(true);
  }

  @Override
  public void exitRosr_metric(Rosr_metricContext ctx) {
    int metric = toInt(ctx.metric);
    _currentStaticRoute.setMetric(metric);
  }

  @Override
  public void exitRosr_next_hop(Rosr_next_hopContext ctx) {
    if (ctx.ip_address() != null) {
      Ip nextHopIp = toIp(ctx.ip_address());
      if (!(_currentStaticRoute instanceof StaticRouteV4)) {
        warn(ctx, "Only IPv4 routes can have IPv4 next hops");
        return;
      }
      _currentStaticRoute.addNextHopIp(nextHopIp);
      return;
    } else if (ctx.ipv6_address() != null) {
      if (!(_currentStaticRoute instanceof StaticRouteV6)) {
        warn(ctx, "Only IPv6 routes can have IPv6 next hops");
        return;
      }
      Ip6 nextHopIp = toIp6(ctx.ipv6_address());
      _currentStaticRoute.addNextHopIp(nextHopIp);
      return;
    }
    assert ctx.interface_id() != null;
    String ifaceName = getInterfaceFullName(ctx.interface_id());
    _currentStaticRoute.addNextHopInterface(ifaceName);
    _configuration.referenceStructure(
        INTERFACE,
        ifaceName,
        STATIC_ROUTE_NEXT_HOP_INTERFACE,
        getLine(ctx.interface_id().getStop()));
  }

  @Override
  public void exitRosr_next_table(Rosr_next_tableContext ctx) {
    String name = toString(ctx.name);
    _currentStaticRoute.setNextTable(name);
  }

  @Override
  public void exitRosr_no_install(Rosr_no_installContext ctx) {
    _currentStaticRoute.setInstall(false);
  }

  @Override
  public void exitRosr_no_readvertise(Rosr_no_readvertiseContext ctx) {
    todo(ctx);
    _currentStaticRoute.setReadvertise(false);
  }

  @Override
  public void exitRosr_no_retain(Rosr_no_retainContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitRosr_passive(Rosr_passiveContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitRosr_preference(Rosr_preferenceContext ctx) {
    _currentStaticRoute.setDistance(toInt(ctx.pref));
  }

  @Override
  public void exitRosr_readvertise(Rosr_readvertiseContext ctx) {
    _currentStaticRoute.setReadvertise(true);
  }

  @Override
  public void exitRosr_reject(Rosr_rejectContext ctx) {
    _currentStaticRoute.setDrop();
  }

  @Override
  public void exitRosr_resolve(Rosr_resolveContext ctx) {
    _currentStaticRoute.setResolve(true);
  }

  @Override
  public void exitRosr_retain(Rosr_retainContext ctx) {
    todo(ctx);
  }

  @Override
  public void enterRosr_qualified_next_hop(Rosr_qualified_next_hopContext ctx) {
    if (ctx.ip_address() != null) {
      Ip ip = toIp(ctx.ip_address());
      _currentQualifiedNextHop = _currentStaticRoute.getOrCreateQualifiedNextHop(new NextHop(ip));
      return;
    }
    assert ctx.interface_id() != null;
    String ifaceName = getInterfaceFullName(ctx.interface_id());
    _currentQualifiedNextHop =
        _currentStaticRoute.getOrCreateQualifiedNextHop(new NextHop(ifaceName));
    _configuration.referenceStructure(
        INTERFACE,
        ifaceName,
        STATIC_ROUTE_NEXT_HOP_INTERFACE,
        getLine(ctx.interface_id().getStop()));
  }

  @Override
  public void enterRosr_qualified_next_hop6(Rosr_qualified_next_hop6Context ctx) {
    if (ctx.ipv6_address() != null) {
      Ip6 ip = toIp6(ctx.ipv6_address());
      _currentQualifiedNextHop = _currentStaticRoute.getOrCreateQualifiedNextHop(new NextHop(ip));
      return;
    }
    assert ctx.interface_id() != null;
    String ifaceName = getInterfaceFullName(ctx.interface_id());
    _currentQualifiedNextHop =
        _currentStaticRoute.getOrCreateQualifiedNextHop(new NextHop(ifaceName));
    _configuration.referenceStructure(
        INTERFACE,
        ifaceName,
        STATIC_ROUTE_NEXT_HOP_INTERFACE,
        getLine(ctx.interface_id().getStop()));
  }

  @Override
  public void exitRosrqnhc_metric(Rosrqnhc_metricContext ctx) {
    _currentQualifiedNextHop.setMetric(toInt(ctx.metric));
  }

  @Override
  public void exitRosrqnhc_preference(Rosrqnhc_preferenceContext ctx) {
    _currentQualifiedNextHop.setPreference(toInt(ctx.pref));
  }

  @Override
  public void exitRosrqnhc_tag(Rosrqnhc_tagContext ctx) {
    _currentQualifiedNextHop.setTag(toLong(ctx.tag));
  }

  @Override
  public void exitRosr_tag(Rosr_tagContext ctx) {
    long tag = toLong(ctx.tag);
    _currentStaticRoute.setTag(tag);
  }

  @Override
  public void exitRosr_tag2(Rosr_tag2Context ctx) {
    long tag = toLong(ctx.tag);
    _currentStaticRoute.setTag2(tag);
    todo(ctx);
  }

  @Override
  public void exitRs_packet_location(Rs_packet_locationContext ctx) {
    NatPacketLocation packetLocation;
    if (ctx.FROM() != null) {
      packetLocation = _currentNatRuleSet.getFromLocation();
    } else { // TO
      if (_currentNat.getType() != SOURCE) {
        _w.addWarning(
            ctx,
            getFullText(ctx),
            _parser,
            "'to' is illegal for non-source NATs. Ignoring statement.");
        return;
      }
      packetLocation = _currentNatRuleSet.getToLocation();
    }
    if (ctx.rs_interface() != null) {
      String iface = getInterfaceFullName(ctx.rs_interface().name);
      packetLocation.setInterface(iface);
      JuniperStructureUsage usage =
          ctx.FROM() != null ? NAT_RULE_SET_FROM_INTERFACE : NAT_RULE_SET_TO_INTERFACE;
      _configuration.referenceStructure(
          INTERFACE, iface, usage, getLine(ctx.rs_interface().name.start));
    } else if (ctx.rs_routing_instance() != null) {
      String ri = toString(ctx.rs_routing_instance().name);
      packetLocation.setRoutingInstance(ri);
      JuniperStructureUsage usage =
          ctx.FROM() != null
              ? NAT_RULE_SET_FROM_ROUTING_INSTANCE
              : NAT_RULE_SET_TO_ROUTING_INSTANCE;
      _configuration.referenceStructure(
          ROUTING_INSTANCE, ri, usage, getLine(ctx.rs_routing_instance().name.start));
    } else if (ctx.rs_zone() != null) {
      packetLocation.setZone(toString(ctx.rs_zone().name));
    }
  }

  @Override
  public void exitRsrm_destination_address(Rsrm_destination_addressContext ctx) {
    Prefix prefix = toPrefix(ctx.ip_prefix());
    _currentNatRule.getMatches().add(new NatRuleMatchDstAddr(prefix));
  }

  @Override
  public void exitRsrm_destination_address_name(Rsrm_destination_address_nameContext ctx) {
    String name = toString(ctx.name);
    _currentNatRule.getMatches().add(new NatRuleMatchDstAddrName(name));
  }

  @Override
  public void exitRsrm_destination_port(Rsrm_destination_portContext ctx) {
    int fromPort = toInt(ctx.from);
    int toPort = ctx.TO() != null ? toInt(ctx.to) : fromPort;
    _currentNatRule.getMatches().add(new NatRuleMatchDstPort(fromPort, toPort));
  }

  @Override
  public void exitRsrm_protocol(Rsrm_protocolContext ctx) {
    IpProtocol protocol = toIpProtocol(ctx.p);
    _currentNatRule.getMatches().add(new NatRuleMatchProtocol(protocol));
  }

  @Override
  public void exitRsrm_source_address(Rsrm_source_addressContext ctx) {
    Prefix prefix = toPrefix(ctx.ip_prefix());
    _currentNatRule.getMatches().add(new NatRuleMatchSrcAddr(prefix));
  }

  @Override
  public void exitRsrm_source_address_name(Rsrm_source_address_nameContext ctx) {
    String name = toString(ctx.name);
    _currentNatRule.getMatches().add(new NatRuleMatchSrcAddrName(name));
  }

  @Override
  public void exitRsrm_source_port(Rsrm_source_portContext ctx) {
    int fromPort = toInt(ctx.from);
    int toPort = ctx.TO() != null ? toInt(ctx.to) : fromPort;
    _currentNatRule.getMatches().add(new NatRuleMatchSrcPort(fromPort, toPort));
  }

  @Override
  public void exitRsrt_nat_off(Rsrt_nat_offContext ctx) {
    _currentNatRule.setThen(NatRuleThenOff.INSTANCE);
  }

  @Override
  public void exitRsrt_nat_pool(Rsrt_nat_poolContext ctx) {
    String name = toString(ctx.name);
    _currentNatRule.setThen(new NatRuleThenPool(name));
    JuniperStructureUsage usage =
        switch (_currentNat.getType()) {
          case DESTINATION -> NAT_DESTINATION_RULE_SET_RULE_THEN;
          case SOURCE -> NAT_SOURCE_RULE_SET_RULE_THEN;
          case STATIC -> NAT_STATIC_RULE_SET_RULE_THEN;
        };
    _configuration.referenceStructure(NAT_POOL, name, usage, getLine(ctx.name.start));
  }

  @Override
  public void exitRsrt_nat_interface(Rsrt_nat_interfaceContext ctx) {
    _currentNatRule.setThen(NatRuleThenInterface.INSTANCE);
  }

  @Override
  public void exitRsrtstp_prefix(Rsrtstp_prefixContext ctx) {
    Prefix prefix = toPrefix(ctx.ip_prefix());
    _currentNatRule.setThen(new NatRuleThenPrefix(prefix, IpField.DESTINATION));
  }

  @Override
  public void exitRsrtstp_prefix_name(Rsrtstp_prefix_nameContext ctx) {
    String prefixName = toString(ctx.name);
    _currentNatRule.setThen(new NatRuleThenPrefixName(prefixName, IpField.DESTINATION));
  }

  @Override
  public void exitS_firewall(S_firewallContext ctx) {
    _currentFirewallFamily = null;
  }

  @Override
  public void exitS_routing_options(S_routing_optionsContext ctx) {
    _currentRib = null;
  }

  @Override
  public void exitS_snmp(S_snmpContext ctx) {
    _currentSnmpServer = null;
  }

  @Override
  public void exitS_vlans_named(S_vlans_namedContext ctx) {
    _currentNamedVlan = null;
  }

  @Override
  public void exitScos_code_point_aliases(Scos_code_point_aliasesContext ctx) {
    String aliasName = toString(ctx.name);
    int value;
    try {
      value = Integer.parseInt(ctx.dec().getText(), 2);
    } catch (NumberFormatException ignored) {
      warn(
          ctx,
          String.format(
              "%s is not a legal code-point. Must be of form xxxxxx, where x is 1 or 0.",
              ctx.dec().getText()));
      return;
    }
    if (value > 63) {
      warn(
          ctx,
          String.format(
              "%s is not a legal code-point. Must be of form xxxxxx, where x is 1 or 0.",
              ctx.dec().getText()));
      return;
    }
    _currentLogicalSystem.getDscpAliases().put(aliasName, value);
    _configuration.defineFlattenedStructure(
        CLASS_OF_SERVICE_CODE_POINT_ALIAS, aliasName, ctx, _parser);
  }

  @Override
  public void exitSe_zones(Se_zonesContext ctx) {
    _hasZones = true;
  }

  @Override
  public void exitSea_tolerance(Sea_toleranceContext ctx) {
    _currentAuthenticationKeyChain.setTolerance(toInt(ctx.dec()));
  }

  @Override
  public void exitSeak_algorithm(Seak_algorithmContext ctx) {
    _currentAuthenticationKey.setIsisAuthenticationAlgorithm(
        ctx.HMAC_SHA1() != null
            ? IsisAuthenticationAlgorithm.HMAC_SHA_1
            : IsisAuthenticationAlgorithm.MD5);
  }

  @Override
  public void exitSeak_options(Seak_optionsContext ctx) {
    _currentAuthenticationKey.setIsisOption(
        ctx.ISIS_ENHANCED() != null ? IsisOption.ISIS_ENHANCED : IsisOption.BASIC);
  }

  @Override
  public void exitSeak_secret(Seak_secretContext ctx) {
    _currentAuthenticationKey.setSecret(applySecret(ctx.key));
  }

  @Override
  public void exitSeak_start_time(Seak_start_timeContext ctx) {
    _currentAuthenticationKey.setStartTime(getFullText(ctx.time));
  }

  @Override
  public void exitSeik_gateway(Seik_gatewayContext ctx) {
    _currentIkeGateway = null;
  }

  @Override
  public void exitSeik_policy(Seik_policyContext ctx) {
    _currentIkePolicy = null;
  }

  @Override
  public void exitSeik_proposal(Seik_proposalContext ctx) {
    _currentIkeProposal = null;
  }

  @Override
  public void exitSeikg_address(Seikg_addressContext ctx) {
    Ip ip = toIp(ctx.ip_address());
    _currentIkeGateway.setAddress(ip);
  }

  @Override
  public void exitSeikg_external_interface(Seikg_external_interfaceContext ctx) {
    String ifaceName = getInterfaceFullName(ctx.interface_id());
    _currentIkeGateway.setExternalInterface(ifaceName);
    _configuration.referenceStructure(
        INTERFACE,
        ifaceName,
        IKE_GATEWAY_EXTERNAL_INTERFACE,
        getLine(ctx.interface_id().getStart()));
  }

  @Override
  public void exitSeikg_ike_policy(Seikg_ike_policyContext ctx) {
    String name = toString(ctx.name);
    _currentIkeGateway.setIkePolicy(name);
    _configuration.referenceStructure(
        IKE_POLICY, name, IKE_GATEWAY_IKE_POLICY, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitSeikg_local_address(Seikg_local_addressContext ctx) {
    Ip ip = toIp(ctx.ip_address());
    _currentIkeGateway.setLocalAddress(ip);
  }

  @Override
  public void exitSeikp_pre_shared_key(Seikp_pre_shared_keyContext ctx) {
    if (ctx.ASCII_TEXT() != null || ctx.HEXADECIMAL() != null) {
      String key = unquote(ctx.key.getText(), ctx);
      // TODO: this 'if needed' probably should handle hexadecimal differently
      String decodedKeyHash = decryptIfNeededAndHash(key, getLine(ctx.key));
      _currentIkePolicy.setPreSharedKeyHash(decodedKeyHash);
    }
  }

  @Override
  public void exitSeikp_proposal_set(Seikp_proposal_setContext ctx) {
    Set<String> proposalsInSet = initIkeProposalSet(ctx.proposal_set_type());
    for (String proposal : proposalsInSet) {
      _currentIkePolicy.getProposals().add(proposal);
    }
  }

  @Override
  public void exitSeikp_proposals(Seikp_proposalsContext ctx) {
    for (Junos_nameContext proposal : ctx.junos_name_list().junos_name()) {
      String name = toString(proposal);
      _currentIkePolicy.getProposals().add(name);
      _configuration.referenceStructure(
          IKE_PROPOSAL, name, IKE_POLICY_IKE_PROPOSAL, getLine(proposal.getStart()));
    }
  }

  @Override
  public void exitSeikpr_authentication_algorithm(Seikpr_authentication_algorithmContext ctx) {
    IkeHashingAlgorithm alg = toIkeHashingAlgorithm(ctx.ike_authentication_algorithm());
    _currentIkeProposal.setAuthenticationAlgorithm(alg);
  }

  @Override
  public void exitSeikpr_authentication_method(Seikpr_authentication_methodContext ctx) {
    IkeAuthenticationMethod authenticationMethod =
        toIkeAuthenticationMethod(ctx.ike_authentication_method());
    _currentIkeProposal.setAuthenticationMethod(authenticationMethod);
  }

  @Override
  public void exitSeikpr_dh_group(Seikpr_dh_groupContext ctx) {
    DiffieHellmanGroup group = toDhGroup(ctx.dh_group());
    _currentIkeProposal.setDiffieHellmanGroup(group);
  }

  @Override
  public void exitSeikpr_encryption_algorithm(Seikpr_encryption_algorithmContext ctx) {
    EncryptionAlgorithm alg = toEncryptionAlgorithm(ctx.encryption_algorithm());
    _currentIkeProposal.setEncryptionAlgorithm(alg);
  }

  @Override
  public void exitSeikpr_lifetime_seconds(Seikpr_lifetime_secondsContext ctx) {
    int lifetimeSeconds = toInt(ctx.seconds);
    _currentIkeProposal.setLifetimeSeconds(lifetimeSeconds);
  }

  @Override
  public void exitSeip_policy(Seip_policyContext ctx) {
    _currentIpsecPolicy = null;
  }

  @Override
  public void exitSeip_proposal(Seip_proposalContext ctx) {
    _currentIpsecProposal = null;
  }

  @Override
  public void exitSeip_vpn(Seip_vpnContext ctx) {
    _currentIpsecVpn = null;
  }

  @Override
  public void exitSeipp_perfect_forward_secrecy(Seipp_perfect_forward_secrecyContext ctx) {
    DiffieHellmanGroup dhGroup = toDhGroup(ctx.dh_group());
    _currentIpsecPolicy.setPfsKeyGroup(dhGroup);
  }

  @Override
  public void exitSeipp_proposal_set(Seipp_proposal_setContext ctx) {
    for (String proposal : initIpsecProposalSet(ctx.proposal_set_type())) {
      _currentIpsecPolicy.getProposals().add(proposal);
    }
  }

  @Override
  public void exitSeipp_proposals(Seipp_proposalsContext ctx) {
    for (Junos_nameContext proposal : ctx.junos_name_list().junos_name()) {
      String name = toString(proposal);
      _currentIpsecPolicy.getProposals().add(name);
      _configuration.referenceStructure(
          IPSEC_PROPOSAL, name, IPSEC_POLICY_IPSEC_PROPOSAL, getLine(proposal.getStart()));
    }
  }

  @Override
  public void exitSeippr_authentication_algorithm(Seippr_authentication_algorithmContext ctx) {
    IpsecAuthenticationAlgorithm alg =
        toIpsecAuthenticationAlgorithm(ctx.ipsec_authentication_algorithm());
    _currentIpsecProposal.setAuthenticationAlgorithm(alg);
  }

  @Override
  public void exitSeippr_encryption_algorithm(Seippr_encryption_algorithmContext ctx) {
    EncryptionAlgorithm alg = toEncryptionAlgorithm(ctx.encryption_algorithm());
    _currentIpsecProposal.setEncryptionAlgorithm(alg);
  }

  @Override
  public void exitSeippr_lifetime_kilobytes(Seippr_lifetime_kilobytesContext ctx) {
    int lifetimeKilobytes = toInt(ctx.kilobytes);
    _currentIpsecProposal.setLifetimeKilobytes(lifetimeKilobytes);
  }

  @Override
  public void exitSeippr_lifetime_seconds(Seippr_lifetime_secondsContext ctx) {
    int lifetimeSeconds = toInt(ctx.seconds);
    _currentIpsecProposal.setLifetimeSeconds(lifetimeSeconds);
  }

  @Override
  public void exitSeippr_protocol(Seippr_protocolContext ctx) {
    _currentIpsecProposal.setProtocols(toIpsecProtocol(ctx.ipsec_protocol()));
  }

  @Override
  public void exitSeipv_bind_interface(Seipv_bind_interfaceContext ctx) {
    String iface = getInterfaceFullName(ctx.interface_id());
    _currentIpsecVpn.setBindInterface(iface);
    _configuration.referenceStructure(
        INTERFACE, iface, IPSEC_VPN_BIND_INTERFACE, getLine(ctx.interface_id().getStart()));
  }

  @Override
  public void exitSeipvi_gateway(Seipvi_gatewayContext ctx) {
    String name = toString(ctx.name);
    _currentIpsecVpn.setGateway(name);
    _configuration.referenceStructure(
        IKE_GATEWAY, name, IPSEC_VPN_IKE_GATEWAY, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitSeipvi_ipsec_policy(Seipvi_ipsec_policyContext ctx) {
    String name = toString(ctx.name);
    _currentIpsecVpn.setIpsecPolicy(name);
    _configuration.referenceStructure(
        IPSEC_POLICY, name, IPSEC_VPN_IPSEC_POLICY, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitSep_default_policy(Sep_default_policyContext ctx) {
    if (ctx.PERMIT_ALL() != null) {
      _defaultCrossZoneAction = LineAction.PERMIT;
    } else if (ctx.DENY_ALL() != null) {
      _defaultCrossZoneAction = LineAction.DENY;
    }
  }

  @Override
  public void exitSepctx_policy(Sepctx_policyContext ctx) {
    _currentFwTerm = null;
  }

  @Override
  public void exitSepctxpm_application(Sepctxpm_applicationContext ctx) {
    if (ctx.junos_application() != null) {
      JunosApplication application = toJunosApplication(ctx.junos_application());
      if (!application.hasDefinition()) {
        _w.redFlagf(
            "unimplemented pre-defined junos application: '%s'", ctx.junos_application().getText());
        return;
      }
      if (application.getIpv6()) {
        _currentFwTerm.setIpv6(true);
      } else {
        FwFromJunosApplication from = new FwFromJunosApplication(application);
        _currentFwTerm.getFromApplicationSetMembers().add(from);
      }
    } else if (ctx.junos_application_set() != null) {
      JunosApplicationSet applicationSet = toJunosApplicationSet(ctx.junos_application_set());
      if (!applicationSet.hasDefinition()) {
        _w.redFlagf(
            "unimplemented pre-defined junos application-set: '%s'",
            ctx.junos_application_set().getText());
        return;
      }
      FwFromJunosApplicationSet from = new FwFromJunosApplicationSet(applicationSet);
      _currentFwTerm.getFromApplicationSetMembers().add(from);
    } else {
      String name = toString(ctx.name);
      _configuration.referenceStructure(
          APPLICATION_OR_APPLICATION_SET,
          name,
          SECURITY_POLICY_MATCH_APPLICATION,
          getLine(ctx.name.getStart()));
      FwFromApplicationOrApplicationSet from = new FwFromApplicationOrApplicationSet(name);
      _currentFwTerm.getFromApplicationSetMembers().add(from);
    }
  }

  @Override
  public void exitSepctxpm_destination_address(Sepctxpm_destination_addressContext ctx) {
    Address_specifierContext addressSpecifierContext = ctx.address_specifier();
    if (addressSpecifierContext.ANY() != null) {
      FwFromDestinationAddress match = new FwFromDestinationAddress(IpWildcard.ANY, "any");
      _currentFwTerm.getFroms().add(match);
    } else if (addressSpecifierContext.ANY_IPV4() != null) {
      FwFromDestinationAddress match = new FwFromDestinationAddress(IpWildcard.ANY, "any-ipv4");
      _currentFwTerm.getFroms().add(match);
    } else if (addressSpecifierContext.ANY_IPV6() != null) {
      _currentFwTerm.setIpv6(true);
    } else {
      assert addressSpecifierContext.name != null;
      FwFrom match =
          new FwFromDestinationAddressBookEntry(
              _currentToZone,
              _currentLogicalSystem.getGlobalAddressBook(),
              toString(addressSpecifierContext.name));
      _currentFwTerm.getFroms().add(match);
    }
  }

  @Override
  public void exitSepctxpm_destination_address_excluded(
      Sepctxpm_destination_address_excludedContext ctx) {
    // This should change the meaning of the from dstIp clause to deny all that match.
    todo(ctx);
  }

  @Override
  public void exitSepctxpm_source_address(Sepctxpm_source_addressContext ctx) {
    if (ctx.address_specifier().ANY() != null) {
      FwFromSourceAddress match = new FwFromSourceAddress(IpWildcard.ANY, "any");
      _currentFwTerm.getFroms().add(match);
    } else if (ctx.address_specifier().ANY_IPV4() != null) {
      FwFromSourceAddress match = new FwFromSourceAddress(IpWildcard.ANY, "any-ipv4");
      _currentFwTerm.getFroms().add(match);
    } else if (ctx.address_specifier().ANY_IPV6() != null) {
      _currentFwTerm.setIpv6(true);
    } else {
      assert ctx.address_specifier().name != null;
      String addressBookEntryName = toString(ctx.address_specifier().name);
      FwFrom match =
          new FwFromSourceAddressBookEntry(
              _currentFromZone, _currentLogicalSystem.getGlobalAddressBook(), addressBookEntryName);
      _currentFwTerm.getFroms().add(match);
    }
  }

  @Override
  public void exitSepctxpm_source_address_excluded(Sepctxpm_source_address_excludedContext ctx) {
    // This should change the meaning of the from srcIp clause to deny all that match.
    todo(ctx);
  }

  @Override
  public void exitSepctxpm_source_identity(Sepctxpm_source_identityContext ctx) {
    if (ctx.ANY() != null) {
      return;
    } else {
      throw new UnsupportedOperationException("no implementation for generated method");
    }
  }

  @Override
  public void exitSepctxpt_deny(Sepctxpt_denyContext ctx) {
    _currentFwTerm.getThens().add(FwThenDiscard.INSTANCE);
  }

  @Override
  public void exitSepctxpt_permit(Sepctxpt_permitContext ctx) {
    if (ctx.sepctxptp_tunnel() != null) {
      // Used for dynamic VPNs (no bind interface tied to a zone)
      // TODO: change from deny to appropriate action when implemented
      todo(ctx);
      _currentFwTerm.getThens().add(FwThenDiscard.INSTANCE);
    } else {
      _currentFwTerm.getThens().add(FwThenAccept.INSTANCE);
    }
  }

  @Override
  public void exitSez_security_zone(Sez_security_zoneContext ctx) {
    _currentZone = null;
    _currentZoneInboundFilter = null;
  }

  @Override
  public void exitSezs_address_book(Sezs_address_bookContext ctx) {
    _currentAddressBook = null;
  }

  @Override
  public void exitSezs_interfaces(Sezs_interfacesContext ctx) {
    _currentZoneInterface = null;
  }

  @Override
  public void exitSezsa_address(Sezsa_addressContext ctx) {
    String name = toString(ctx.name);
    IpWildcard ipWildcard = null;
    if (ctx.ip_and_mask != null) {
      ipWildcard = toIpWildcard(ctx.ip_and_mask);
    } else if (ctx.address != null) {
      ipWildcard = toIpWildcard(ctx.address);
    } else {
      assert ctx.prefix != null;
      ipWildcard = toIpWildcard(ctx.prefix);
    }
    AddressBookEntry addressEntry = new AddressAddressBookEntry(name, ipWildcard);
    _currentZone.getAddressBook().getEntries().put(name, addressEntry);
  }

  private static @Nonnull IpWildcard toIpWildcard(Ip_addressContext ctx) {
    return IpWildcard.create(toIp(ctx));
  }

  private static @Nonnull IpWildcard toIpWildcard(Ip_prefixContext ctx) {
    return IpWildcard.create(toPrefix(ctx));
  }

  @Override
  public void exitSezsa_address_set(Sezsa_address_setContext ctx) {
    _currentAddressSetAddressBookEntry = null;
  }

  @Override
  public void exitSezsaad_address(Sezsaad_addressContext ctx) {
    String name = toString(ctx.name);
    _currentAddressSetAddressBookEntry
        .getEntries()
        .put(name, new AddressSetEntry(name, _currentAddressBook));
  }

  @Override
  public void exitSezsaad_address_set(Sezsaad_address_setContext ctx) {
    String name = toString(ctx.name);
    _currentAddressSetAddressBookEntry
        .getEntries()
        .put(name, new AddressSetEntry(name, _currentAddressBook));
  }

  @Override
  public void exitSezsh_protocols(Sezsh_protocolsContext ctx) {
    HostProtocol protocol = toHostProtocol(ctx.hib_protocol());
    String termName = protocol.name();
    FwTerm newTerm = new FwTerm(termName);
    _currentZoneInboundFilter.getTerms().put(termName, newTerm);
    newTerm.getFromHostProtocols().add(protocol);
    newTerm.getThens().add(FwThenAccept.INSTANCE);

    String defName = computeSecurityPolicyTermName(_currentZoneInboundFilter.getName(), termName);
    _configuration.defineFlattenedStructure(SECURITY_POLICY_TERM, defName, ctx, _parser);
    _configuration.referenceStructure(
        SECURITY_POLICY_TERM, defName, SECURITY_POLICY_TERM_DEFINITION, getLine(ctx.getStart()));
  }

  @Override
  public void exitSezsh_system_services(Sezsh_system_servicesContext ctx) {
    HostSystemService service = toHostSystemService(ctx.hib_system_service());
    String termName = service.name();
    FwTerm newTerm = new FwTerm(termName);
    _currentZoneInboundFilter.getTerms().put(termName, newTerm);
    newTerm.getFromHostServices().add(service);
    newTerm.getThens().add(FwThenAccept.INSTANCE);

    String defName = computeSecurityPolicyTermName(_currentZoneInboundFilter.getName(), termName);
    _configuration.defineFlattenedStructure(SECURITY_POLICY_TERM, defName, ctx, _parser);
    _configuration.referenceStructure(
        SECURITY_POLICY_TERM, defName, SECURITY_POLICY_TERM_DEFINITION, getLine(ctx.getStart()));
  }

  @Override
  public void enterSnmp_client_list(Snmp_client_listContext ctx) {
    String name = toString(ctx.name);
    _configuration.defineFlattenedStructure(SNMP_CLIENT_LIST, name, ctx, _parser);
    Map<String, PrefixList> clientLists = _currentLogicalSystem.getSnmpClientLists();
    _currentPrefixList = clientLists.computeIfAbsent(name, PrefixList::new);
  }

  @Override
  public void exitSnmp_client_list(Snmp_client_listContext ctx) {
    _currentPrefixList = null;
  }

  @Override
  public void exitSnmpcl_network(Snmpcl_networkContext ctx) {
    Prefix prefix = toPrefix(ctx.prefix);
    _currentPrefixList.getPrefixes().add(prefix);
  }

  @Override
  public void exitSnmpc_authorization(Snmpc_authorizationContext ctx) {
    if (ctx.READ_ONLY() != null) {
      _currentSnmpCommunity.setRo(true);
    } else if (ctx.READ_WRITE() != null) {
      _currentSnmpCommunity.setRw(true);
    } else {
      throw new BatfishException("Invalid authorization");
    }
  }

  @Override
  public void exitSnmpc_client_list_name(Snmpc_client_list_nameContext ctx) {
    String list = toString(ctx.name);
    _currentSnmpCommunity.setAccessList(list);
    _configuration.referenceStructure(
        SNMP_CLIENT_LIST_OR_PREFIX_LIST,
        list,
        SNMP_COMMUNITY_CLIENT_LIST_NAME,
        getLine(ctx.name.getStart()));
    // TODO: verify whether both ipv4 and ipv6 list should be set with this
    // command
    _currentSnmpCommunity.setAccessList6(list);
  }

  @Override
  public void exitSnmptg_targets(Snmptg_targetsContext ctx) {
    Ip ip = toIp(ctx.target);
    String name = ip.toString();
    _currentSnmpServer.getHosts().computeIfAbsent(name, k -> new SnmpHost(ip.toString()));
  }

  @Override
  public void exitSy_authentication_method(Sy_authentication_methodContext ctx) {
    if (_currentAuthenticationOrder != null) {
      _currentAuthenticationOrder.addMethod(
          AuthenticationMethod.toAuthenticationMethod(ctx.method.getText()));
    }
  }

  @Override
  public void exitSy_authentication_order(Sy_authentication_orderContext ctx) {
    if (_currentLine == null) {
      // in system hierarchy
      for (Line line : _currentLogicalSystem.getJf().getLines().values()) {
        if (line.getAaaAuthenticationLoginList() == null
            || line.getAaaAuthenticationLoginList().isDefault()) {
          // line has no login list or has default login list, give it the system's login list
          line.setAaaAuthenticationLoginList(
              new AaaAuthenticationLoginList(_currentAuthenticationOrder.getMethods(), true));
        }
      }
    }

    _currentAuthenticationOrder = null;
  }

  @Override
  public void exitSy_default_address_selection(Sy_default_address_selectionContext ctx) {
    _currentLogicalSystem.setDefaultAddressSelection(true);
  }

  @Override
  public void exitSy_domain_name(Sy_domain_nameContext ctx) {
    _currentRoutingInstance.setDomainName(toString(ctx.name));
  }

  @Override
  public void exitSy_host_name(Sy_host_nameContext ctx) {
    _currentRoutingInstance.setHostname(toString(ctx.name));
  }

  @Override
  public void exitSy_name_server(Sy_name_serverContext ctx) {
    Set<String> dnsServers = _currentLogicalSystem.getDnsServers();
    String hostname = toIp(ctx.server).toString();
    dnsServers.add(hostname);
  }

  @Override
  public void exitSy_ports(Sy_portsContext ctx) {
    _currentLine = null;
  }

  @Override
  public void exitSy_services_linetype(Sy_services_linetypeContext ctx) {
    _currentLine = null;
  }

  @Override
  public void exitSy_tacplus_server(Sy_tacplus_serverContext ctx) {
    _currentTacplusServer = null;
  }

  @Override
  public void exitSyr_encrypted_password(Syr_encrypted_passwordContext ctx) {
    String hash = ctx.password.getText();
    String rehashedPassword = CommonUtil.sha256Digest(hash + CommonUtil.salt());
    _currentLogicalSystem.getJf().setRootAuthenticationEncryptedPassword(rehashedPassword);
  }

  @Override
  public void exitSyt_secret(Syt_secretContext ctx) {
    _currentTacplusServer.setSecret(applySecret(ctx.secret_string()));
  }

  private @Nonnull String applySecret(@Nonnull Secret_stringContext ctx) {
    String text = ctx.getText();
    if (ctx.SCRUBBED() != null) {
      return saltAndHash(text);
    }
    return decryptIfNeededAndHash(unquote(text, ctx), getLine(ctx.start));
  }

  private @Nonnull String saltAndHash(@Nonnull String text) {
    return CommonUtil.sha256Digest(String.format("%s%s", text, CommonUtil.salt()));
  }

  private @Nonnull String decryptIfNeededAndHash(@Nonnull String text, int line) {
    if (JuniperUtils.isJuniper9CipherText(text)) {
      return JuniperUtils.decryptAndHashJuniper9CipherText(text);
    } else {
      _w.redFlagf("Unencrypted key stored at line: %d", line);
      return saltAndHash(text);
    }
  }

  @Override
  public void exitSysh_routing_instance(Sysh_routing_instanceContext ctx) {
    String name = toString(ctx.ri);
    _configuration.referenceStructure(
        ROUTING_INSTANCE, name, SYSLOG_HOST_ROUTING_INSTANCE, getLine(ctx.ri.getStart()));
  }

  @Override
  public void exitSysp_logical_system(Sysp_logical_systemContext ctx) {
    _configuration.referenceStructure(
        LOGICAL_SYSTEM,
        toString(ctx.name),
        SECURITY_PROFILE_LOGICAL_SYSTEM,
        getLine(ctx.name.getStart()));
  }

  @Override
  public void exitSyt_source_address(Syt_source_addressContext ctx) {
    Ip sourceAddress = toIp(ctx.address);
    _currentTacplusServer.setSourceAddress(sourceAddress);
  }

  @Override
  public void exitSyt_routing_instance(Syt_routing_instanceContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ROUTING_INSTANCE, name, TACPLUS_SERVER_ROUTING_INSTANCE, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitVlt_vlan_id(Vlt_vlan_idContext ctx) {
    Optional<Integer> vlan = toInteger(ctx, ctx.id);
    vlan.ifPresent(_currentNamedVlan::setVlanId);
  }

  @Override
  public void exitVlt_vlan_id_list(FlatJuniperParser.Vlt_vlan_id_listContext ctx) {
    Optional<SubRange> vlanRange = toSubRange(ctx, ctx.vlan_range());
    vlanRange.ifPresent(_currentNamedVlan::addVlanIdListSubrange);
  }

  @Override
  public void exitVlt_vni_id(Vlt_vni_idContext ctx) {
    Optional<Integer> maybeVniNumber = toInteger(ctx, ctx.vni_number());
    if (!maybeVniNumber.isPresent()) {
      // already warned
      return;
    }
    int vni = maybeVniNumber.get();
    _currentNamedVlan.setVniId(vni);
  }

  @Override
  public void exitVlt_interface(Vlt_interfaceContext ctx) {
    String name = getInterfaceFullName(ctx.interface_id());
    _configuration.referenceStructure(
        INTERFACE, name, VLAN_INTERFACE, getLine(ctx.interface_id().getStart()));
    _currentNamedVlan.addInterface(name);
  }

  @Override
  public void exitVlt_l3_interface(Vlt_l3_interfaceContext ctx) {
    String name = getInterfaceFullName(ctx.interface_id());
    _configuration.referenceStructure(
        INTERFACE, name, VLAN_L3_INTERFACE, getLine(ctx.interface_id().getStart()));
    _currentNamedVlan.setL3Interface(name);
  }

  @Override
  public void enterPo_condition(Po_conditionContext ctx) {
    String name = toString(ctx.name);
    _currentCondition = _currentLogicalSystem.getConditions().computeIfAbsent(name, Condition::new);
    _configuration.defineFlattenedStructure(CONDITION, name, ctx, _parser);
  }

  @Override
  public void exitPo_condition(Po_conditionContext ctx) {
    _currentCondition = null;
  }

  @Override
  public void enterPocond_if_route_exists(Pocond_if_route_existsContext ctx) {
    _currentCondition.getOrCreateIfRouteExists();
  }

  @Override
  public void exitPocondiafi_prefix(Pocondiafi_prefixContext ctx) {
    _currentCondition.getIfRouteExists().setPrefix(toPrefix(ctx.prefix));
  }

  @Override
  public void exitPocondiafi_prefix6(Pocondiafi_prefix6Context ctx) {
    _currentCondition.getIfRouteExists().setPrefix6(toPrefix6(ctx.prefix));
  }

  @Override
  public void exitPocondiafi_table(Pocondiafi_tableContext ctx) {
    _currentCondition.getIfRouteExists().setTable(toString(ctx.name));
  }

  @Override
  public void exitPocondiaf_ccc(Pocondiaf_cccContext ctx) {
    todo(ctx);
  }

  private static @Nonnull Prefix toPrefix(Ip_prefixContext ctx) {
    return Prefix.parse(ctx.getText());
  }

  private static @Nonnull Prefix toPrefix(Ip_prefix_default_32Context ctx) {
    if (ctx.IP_PREFIX() != null) {
      return Prefix.parse(ctx.getText());
    }
    assert ctx.IP_ADDRESS() != null;
    return Ip.parse(ctx.getText()).toPrefix();
  }

  private static @Nonnull Prefix6 toPrefix6(Ipv6_prefixContext ctx) {
    return Prefix6.parse(ctx.getText());
  }

  private static @Nonnull Prefix6 toPrefix6(Ipv6_prefix_default_128Context ctx) {
    if (ctx.IPV6_PREFIX() != null) {
      return Prefix6.parse(ctx.getText());
    }
    assert ctx.IPV6_ADDRESS() != null;
    return Ip6.parse(ctx.getText()).toPrefix6();
  }

  @Override
  public void enterS_bridge_domains(S_bridge_domainsContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.name);
    if (!maybeName.isPresent()) {
      _currentBridgeDomain = new BridgeDomain();
      return;
    }
    String name = maybeName.get();
    _currentBridgeDomain = _currentRoutingInstance.getOrAddBridgeDomain(name);
    _configuration.referenceStructure(
        BRIDGE_DOMAIN, name, BRIDGE_DOMAIN_SELF_REF, getLine(ctx.getStart()));
    _configuration.defineFlattenedStructure(BRIDGE_DOMAIN, name, ctx, _parser);
    // until we support bridge domains generally
    todo(ctx);
  }

  @Override
  public void exitS_bridge_domains(S_bridge_domainsContext ctx) {
    _currentBridgeDomain = null;
  }

  @Override
  public void exitBd_routing_interface(Bd_routing_interfaceContext ctx) {
    String iface = toInterfaceId(ctx.interface_id()).getFullName();
    if (!iface.startsWith("irb")) {
      warn(ctx, "Only IRB interfaces are supported for routing-interface");
      return;
    }
    _configuration.referenceStructure(
        INTERFACE, iface, BRIDGE_DOMAINS_ROUTING_INTERFACE, getLine(ctx.getStart()));
    _currentBridgeDomain.setRoutingInterface(iface);
  }

  @Override
  public void exitBf_route_target(Bf_route_targetContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitBd_vlan_id(Bd_vlan_idContext ctx) {
    BridgeDomainVlanId vlanId;
    if (ctx.ALL() != null) {
      vlanId = BridgeDomainVlanIdAll.instance();
    } else if (ctx.NONE() != null) {
      vlanId = BridgeDomainVlanIdNone.instance();
    } else {
      assert ctx.vlan_number() != null;
      Optional<Integer> maybeNum = toInteger(ctx, ctx.vlan_number());
      if (!maybeNum.isPresent()) {
        return;
      }
      vlanId = BridgeDomainVlanIdNumber.of(maybeNum.get());
    }
    _currentBridgeDomain.setVlanId(vlanId);
  }

  @Override
  public void enterBfiu_add_path(Bfiu_add_pathContext ctx) {
    todo(ctx);
    _currentBgpGroup.getOrInitAddPath();
  }

  @Override
  public void exitBfiua_receive(Bfiua_receiveContext ctx) {
    _currentBgpGroup.getOrInitAddPath().setReceive(true);
  }

  @Override
  public void enterBfiua_send(Bfiua_sendContext ctx) {
    _currentBgpGroup.getAddPath().getOrInitSend();
  }

  @Override
  public void exitBfiuas_multipath(Bfiuas_multipathContext ctx) {
    _currentBgpGroup.getAddPath().getSend().setMultipath(true);
  }

  @Override
  public void exitBfiuas_path_count(Bfiuas_path_countContext ctx) {
    toInteger(ctx, ctx.count).ifPresent(_currentBgpGroup.getAddPath().getSend()::setPathCount);
  }

  @Override
  public void exitBfiuas_path_selection_mode(Bfiuas_path_selection_modeContext ctx) {
    PathSelectionMode mode;
    if (ctx.ALL_PATHS() != null) {
      mode = PathSelectionMode.ALL_PATHS;
    } else {
      assert ctx.EQUAL_COST_PATHS() != null;
      mode = PathSelectionMode.EQUAL_COST_PATHS;
    }
    _currentBgpGroup.getAddPath().getSend().setPathSelectionMode(mode);
  }

  @Override
  public void exitBfiuas_prefix_policy(Bfiuas_prefix_policyContext ctx) {
    String name = toString(ctx.policy);
    _configuration.referenceStructure(
        POLICY_STATEMENT, name, ADD_PATH_SEND_PREFIX_POLICY, getLine(ctx.getStart()));
    _currentBgpGroup.getAddPath().getSend().setPrefixPolicy(name);
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Send_path_countContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, ADD_PATH_SEND_PATH_COUNT_RANGE, "path-count");
  }

  private @Nonnull Optional<Integer> toInteger(ParserRuleContext messageCtx, Srlg_costContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, SRLG_COST_RANGE, "srlg-cost");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Vlan_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, VLAN_RANGE, "vlan number");
  }

  @Override
  public void exitMpls_admin_groups(Mpls_admin_groupsContext ctx) {
    String name = toString(ctx.name);

    // Validate admin-group value (0-31)
    Optional<Integer> maybeValue =
        toIntegerInSpace(ctx, ctx.number, ADMIN_GROUP_RANGE, "admin-group");
    if (!maybeValue.isPresent()) {
      return;
    }

    int value = maybeValue.get();
    _currentLogicalSystem.getAdminGroups().put(name, new AdminGroup(name, value));
    _configuration.defineFlattenedStructure(ADMIN_GROUP, name, ctx, _parser);
  }

  @Override
  public void exitMpls_path(Mpls_pathContext ctx) {
    String name = toString(ctx.name);
    _configuration.defineFlattenedStructure(MPLS_PATH, name, ctx, _parser);
  }

  @Override
  public void exitMplslspag_exclude(Mplslspag_excludeContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ADMIN_GROUP, name, MPLS_LSP_ADMIN_GROUP_EXCLUDE, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitMplslspag_include_all(Mplslspag_include_allContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ADMIN_GROUP, name, MPLS_LSP_ADMIN_GROUP_INCLUDE_ALL, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitMplslspag_include_any(Mplslspag_include_anyContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ADMIN_GROUP, name, MPLS_LSP_ADMIN_GROUP_INCLUDE_ANY, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitMplslspsag_exclude(Mplslspsag_excludeContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ADMIN_GROUP, name, MPLS_LSP_SECONDARY_ADMIN_GROUP_EXCLUDE, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitMplslspsag_include_all(Mplslspsag_include_allContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ADMIN_GROUP,
        name,
        MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ALL,
        getLine(ctx.name.getStart()));
  }

  @Override
  public void exitMplslspsag_include_any(Mplslspsag_include_anyContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        ADMIN_GROUP,
        name,
        MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ANY,
        getLine(ctx.name.getStart()));
  }

  @Override
  public void exitMplslsp_primary(Mplslsp_primaryContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        MPLS_PATH, name, MPLS_LSP_PRIMARY_PATH, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitMplslsp_secondary(Mplslsp_secondaryContext ctx) {
    String name = toString(ctx.name);
    _configuration.referenceStructure(
        MPLS_PATH, name, MPLS_LSP_SECONDARY_PATH, getLine(ctx.name.getStart()));
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Bridge_domain_nameContext ctx) {
    String name = toString(ctx.junos_name());
    if (name.contains("/")) {
      warn(messageCtx, "bridge-domain name cannot contain '/'");
      return Optional.empty();
    }
    return Optional.of(name);
  }

  private @Nullable IpWildcard formIpWildCard(Fftfa_address_mask_prefixContext ctx) {
    if (ctx == null) {
      return null;
    } else if (ctx.ip != null) {
      return toIpWildcard(ctx.ip);
    } else if (ctx.ip_and_mask != null) {
      return toIpWildcard(ctx.ip_and_mask);
    } else {
      assert ctx.prefix != null;
      return toIpWildcard(ctx.prefix);
    }
  }

  public JuniperConfiguration getConfiguration() {
    return _configuration;
  }

  @Override
  public @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String text = _tokenInputs.getOrDefault(ctx.getStart(), _text).substring(start, end + 1);
    return text;
  }

  @Override
  public @Nonnull String getInputText() {
    return _text;
  }

  @Override
  public @Nonnull BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Override
  public @Nonnull Warnings getWarnings() {
    return _w;
  }

  @Override
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  private static class InterfaceId {
    private static final Pattern DIGITS = Pattern.compile("[0-9]+");

    private InterfaceId(String fullText) {
      String[] parentAndUnit = fullText.split("\\.", -1);
      assert parentAndUnit.length == 1 || parentAndUnit.length == 2;
      String exceptUnit = parentAndUnit[0];
      if (parentAndUnit.length == 2) {
        _unit = parentAndUnit[1];
      } else {
        _unit = null;
      }
      String[] parentParts = exceptUnit.split(":", -1);
      assert 1 <= parentParts.length && parentParts.length <= 3;
      if (parentParts.length == 1) {
        _parentName = exceptUnit;
        _node = null;
      } else if (parentParts.length == 2) {
        // could be node:name or name:channel or node:name:channel
        if (DIGITS.matcher(parentParts[parentParts.length - 1]).matches()) {
          // name:channel
          _parentName = exceptUnit;
          _node = null;
        } else {
          // node:name
          _node = parentParts[0];
          _parentName = parentParts[1];
        }
      } else {
        // 3 parts
        _parentName = exceptUnit.substring(parentParts[0].length());
        _node = parentParts[0];
      }
    }

    private @Nullable String getNode() {
      return _node;
    }

    private @Nonnull String getParentName() {
      return _parentName;
    }

    private @Nonnull String getFullName() {
      return _unit != null ? String.format("%s.%s", _parentName, _unit) : _parentName;
    }

    public @Nullable String getUnit() {
      return _unit;
    }

    private final @Nullable String _node;
    private final @Nonnull String _parentName;
    private final @Nullable String _unit;
  }

  private String getParentInterfaceName(Interface_idContext id) {
    return toInterfaceId(id).getParentName();
  }

  private String getInterfaceFullName(Interface_idContext id) {
    return toInterfaceId(id).getFullName();
  }

  private String initIkeProposal(IkeProposal proposal) {
    String name = proposal.getName();
    _currentLogicalSystem.getIkeProposals().put(name, proposal);
    return name;
  }

  private Set<String> initIkeProposalSet(Proposal_set_typeContext ctx) {
    Set<String> proposals = new HashSet<>();

    // the proposal-sets have been defined as per the specifications in the link:
    // https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/security-edit-proposal-set-ike.html
    if (ctx.BASIC() != null) {
      IkeProposal proposal1 = new IkeProposal("PSK_DES_DH1_SHA1");
      IkeProposal proposal2 = new IkeProposal("PSK_DES_DH1_MD5");

      proposals.add(
          initIkeProposal(
              proposal1
                  .setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC)
                  .setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS)
                  .setDiffieHellmanGroup(DiffieHellmanGroup.GROUP1)
                  .setAuthenticationAlgorithm(IkeHashingAlgorithm.SHA1)));
      proposals.add(
          initIkeProposal(
              proposal2
                  .setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC)
                  .setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS)
                  .setDiffieHellmanGroup(DiffieHellmanGroup.GROUP1)
                  .setAuthenticationAlgorithm(IkeHashingAlgorithm.MD5)));
    } else if (ctx.COMPATIBLE() != null) {
      IkeProposal proposal1 = new IkeProposal("PSK_3DES_DH2_MD5");
      IkeProposal proposal2 = new IkeProposal("PSK_DES_DH2_SHA1");
      IkeProposal proposal3 = new IkeProposal("PSK_DES_DH2_MD5");

      proposals.add(
          initIkeProposal(
              proposal1
                  .setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS)
                  .setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2)
                  .setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC)
                  .setAuthenticationAlgorithm(IkeHashingAlgorithm.MD5)));
      proposals.add(
          initIkeProposal(
              proposal2
                  .setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS)
                  .setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2)
                  .setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC)
                  .setAuthenticationAlgorithm(IkeHashingAlgorithm.SHA1)));
      proposals.add(
          initIkeProposal(
              proposal3
                  .setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS)
                  .setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2)
                  .setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC)
                  .setAuthenticationAlgorithm(IkeHashingAlgorithm.MD5)));

    } else if (ctx.STANDARD() != null) {
      IkeProposal proposal1 = new IkeProposal("PSK_3DES_DH2_SHA1");
      IkeProposal proposal2 = new IkeProposal("PSK_AES128_DH2_SHA1");

      proposals.add(
          initIkeProposal(
              proposal1
                  .setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS)
                  .setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2)
                  .setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC)
                  .setAuthenticationAlgorithm(IkeHashingAlgorithm.SHA1)));
      proposals.add(
          initIkeProposal(
              proposal2
                  .setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS)
                  .setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2)
                  .setEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC)
                  .setAuthenticationAlgorithm(IkeHashingAlgorithm.SHA1)));
    }
    return proposals;
  }

  /**
   * Returns a logical interface mentioned in an OSPF or ISIS routing context. The physical and
   * logical interfaces are created if they doesn't already exist. If the unit is not explicit in
   * the configuration text, it is considered to be zero (tested in lab for both OSPF and ISIS).
   */
  private @Nonnull Interface initRoutingInterface(Interface_idContext id) {
    Map<String, Interface> interfaces;
    InterfaceId interfaceId = toInterfaceId(id);
    if (interfaceId.getNode() != null) {
      String nodeDeviceName = interfaceId.getNode();
      NodeDevice nodeDevice =
          _configuration.getNodeDevices().computeIfAbsent(nodeDeviceName, n -> new NodeDevice());
      interfaces = nodeDevice.getInterfaces();
    } else {
      interfaces = _currentLogicalSystem.getInterfaces();
    }
    String name = getParentInterfaceName(id);

    Interface iface = interfaces.get(name);
    if (iface == null) {
      // TODO: this is not ideal, interface should not be created here as we are not sure if the
      // interface was defined
      iface = new Interface(name);
      iface.setRoutingInstance(_currentLogicalSystem.getDefaultRoutingInstance());
      interfaces.put(name, iface);
    }
    String unit = firstNonNull(interfaceId.getUnit(), "0");
    String unitFullName = name + "." + unit;
    Map<String, Interface> units = iface.getUnits();
    Interface unitIface = units.get(unitFullName);
    if (unitIface == null) {
      // TODO: this is not ideal, interface should not be created here as we are not sure if the
      // interface was defined
      unitIface = new Interface(unitFullName);
      unitIface.setRoutingInstance(_currentLogicalSystem.getDefaultRoutingInstance());
      units.put(unitFullName, unitIface);
      unitIface.setParent(iface);
    }
    return unitIface;
  }

  private String initIpsecProposal(IpsecProposal proposal) {
    String name = proposal.getName();
    _currentLogicalSystem.getIpsecProposals().put(name, proposal);
    return name;
  }

  private List<String> initIpsecProposalSet(Proposal_set_typeContext ctx) {
    List<String> proposals = new ArrayList<>();
    if (ctx.BASIC() != null) {
      IpsecProposal proposal1 = new IpsecProposal("NOPFS_ESP_DES_SHA");
      IpsecProposal proposal2 = new IpsecProposal("NOPFS_ESP_DES_MD5");
      proposals.add(
          initIpsecProposal(
              proposal1
                  .setProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP))
                  .setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC)
                  .setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_SHA1_96)));
      proposals.add(
          initIpsecProposal(
              proposal2
                  .setProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP))
                  .setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC)
                  .setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_MD5_96)));
    } else if (ctx.COMPATIBLE() != null) {
      IpsecProposal proposal1 = new IpsecProposal("NOPFS_ESP_3DES_SHA");
      IpsecProposal proposal2 = new IpsecProposal("NOPFS_ESP_3DES_MD5");
      IpsecProposal proposal3 = new IpsecProposal("NOPFS_ESP_DES_SHA");
      IpsecProposal proposal4 = new IpsecProposal("NOPFS_ESP_DES_MD5");
      proposals.add(
          initIpsecProposal(
              proposal1
                  .setProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP))
                  .setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC)
                  .setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_SHA1_96)));
      proposals.add(
          initIpsecProposal(
              proposal2
                  .setProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP))
                  .setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC)
                  .setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_MD5_96)));
      proposals.add(
          initIpsecProposal(
              proposal3
                  .setProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP))
                  .setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC)
                  .setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_SHA1_96)));
      proposals.add(
          initIpsecProposal(
              proposal4
                  .setProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP))
                  .setEncryptionAlgorithm(EncryptionAlgorithm.DES_CBC)
                  .setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_MD5_96)));
    } else if (ctx.STANDARD() != null) {
      IpsecProposal proposal1 = new IpsecProposal("G2_ESP_3DES_SHA");
      IpsecProposal proposal2 = new IpsecProposal("G2_ESP_AES128_SHA");
      proposals.add(
          initIpsecProposal(
              proposal1
                  .setProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP))
                  .setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC)
                  .setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_SHA1_96)));
      proposals.add(
          initIpsecProposal(
              proposal2
                  .setProtocols(ImmutableSortedSet.of(IpsecProtocol.ESP))
                  .setEncryptionAlgorithm(EncryptionAlgorithm.AES_128_CBC)
                  .setAuthenticationAlgorithm(IpsecAuthenticationAlgorithm.HMAC_SHA1_96)));
      _currentIpsecPolicy.setPfsKeyGroup(DiffieHellmanGroup.GROUP2);
    }
    return proposals;
  }

  private AsSet toAsSet(As_unitContext ctx) {
    if (ctx.bgp_asn() != null) {
      return AsSet.of(toAsNum(ctx.bgp_asn()));
    } else {
      return AsSet.of(ctx.as_set().items.stream().mapToLong(this::toAsNum).toArray());
    }
  }

  private AsPath toAsPath(As_path_exprContext path) {
    List<AsSet> asPath =
        path.items.stream().map(this::toAsSet).collect(ImmutableList.toImmutableList());
    return AsPath.of(asPath);
  }

  private @Nonnull InterfaceId toInterfaceId(Interface_idContext ctx) {
    String text = unquote(ctx.getText(), ctx);
    return new InterfaceId(text);
  }

  private @Nonnull String toString(Address_specifier_nameContext ctx) {
    return unquote(ctx.getText(), ctx);
  }

  private @Nonnull String toString(Junos_nameContext ctx) {
    return unquote(ctx.getText(), ctx);
  }

  private @Nonnull String toString(ZoneContext ctx) {
    return unquote(ctx.getText(), ctx);
  }

  private @Nonnull String toString(Sy_porttypeContext ctx) {
    return unquote(ctx.getText(), ctx);
  }

  private static @Nonnull String toString(Line_typeContext ctx) {
    return ctx.getText();
  }

  private @Nonnull String toString(DescriptionContext ctx) {
    return unquote(ctx.text.getText(), ctx);
  }

  private @Nonnull String toString(Filter_nameContext ctx) {
    return unquote(ctx.getText(), ctx);
  }

  private static @Nonnull String toString(Tacplus_server_hostContext ctx) {
    return ctx.getText();
  }

  private @Nonnull String toString(Name_or_ipContext ctx) {
    return ctx.getText();
  }

  private @Nonnull String toString(Inet_rib_nameContext ctx) {
    return ctx.getText();
  }

  private @Nonnull String toString(Inet6_rib_nameContext ctx) {
    return ctx.getText();
  }

  private @Nonnull String toString(Iso_rib_nameContext ctx) {
    return ctx.getText();
  }

  private @Nonnull String toString(Mpls_rib_nameContext ctx) {
    return ctx.getText();
  }

  private @Nonnull String toString(Rib_nameContext ctx) {
    return ctx.getText();
  }

  private String toComplexPolicyStatement(
      Policy_expressionContext expr, JuniperStructureUsage usage) {
    if (expr.pe_nested() != null) {
      return toComplexPolicyStatement(expr.pe_nested().policy_expression(), usage);
    } else if (expr.junos_name() != null) {
      String name = toString(expr.junos_name());
      _configuration.referenceStructure(
          POLICY_STATEMENT, name, usage, getLine(expr.junos_name().getStart()));
      return name;
    } else if (expr.pe_conjunction() != null) {
      Set<String> conjuncts = new LinkedHashSet<>();
      for (Policy_expressionContext conjunctCtx : expr.pe_conjunction().policy_expression()) {
        String conjunctName = toComplexPolicyStatement(conjunctCtx, usage);
        conjuncts.add(conjunctName);
      }
      String conjunctionPolicyName = "~CONJUNCTION_POLICY_" + _conjunctionPolicyIndex + "~";
      _conjunctionPolicyIndex++;
      PolicyStatement conjunctionPolicy = new PolicyStatement(conjunctionPolicyName);
      PsTerm conjunctionPolicyTerm = conjunctionPolicy.getDefaultTerm();
      PsFromPolicyStatementConjunction from = new PsFromPolicyStatementConjunction(conjuncts);
      conjunctionPolicyTerm.getFroms().addFromPolicyStatementConjunction(from);
      conjunctionPolicyTerm.getThens().addPsThen(PsThenAccept.INSTANCE);
      _currentLogicalSystem.getPolicyStatements().put(conjunctionPolicyName, conjunctionPolicy);
      return conjunctionPolicyName;
    } else if (expr.pe_disjunction() != null) {
      Set<String> disjuncts = new LinkedHashSet<>();
      for (Policy_expressionContext disjunctCtx : expr.pe_disjunction().policy_expression()) {
        String disjunctName = toComplexPolicyStatement(disjunctCtx, usage);
        disjuncts.add(disjunctName);
      }
      String disjunctionPolicyName = "~DISJUNCTION_POLICY_" + _disjunctionPolicyIndex + "~";
      _disjunctionPolicyIndex++;
      PolicyStatement disjunctionPolicy = new PolicyStatement(disjunctionPolicyName);
      PsTerm disjunctionPolicyTerm = disjunctionPolicy.getDefaultTerm();
      for (String disjunct : disjuncts) {
        PsFromPolicyStatement from = new PsFromPolicyStatement(disjunct);
        disjunctionPolicyTerm.getFroms().addFromPolicyStatement(from);
      }
      disjunctionPolicyTerm.getThens().addPsThen(PsThenAccept.INSTANCE);
      _currentLogicalSystem.getPolicyStatements().put(disjunctionPolicyName, disjunctionPolicy);
      return disjunctionPolicyName;
    } else {
      throw new BatfishException("Invalid policy expression");
    }
  }

  private DiffieHellmanGroup toDhGroup(Dh_groupContext ctx) {
    if (ctx.GROUP1() != null) {
      return DiffieHellmanGroup.GROUP1;
    } else if (ctx.GROUP14() != null) {
      return DiffieHellmanGroup.GROUP14;
    } else if (ctx.GROUP15() != null) {
      return DiffieHellmanGroup.GROUP15;
    } else if (ctx.GROUP16() != null) {
      return DiffieHellmanGroup.GROUP16;
    } else if (ctx.GROUP19() != null) {
      return DiffieHellmanGroup.GROUP19;
    } else if (ctx.GROUP2() != null) {
      return DiffieHellmanGroup.GROUP2;
    } else if (ctx.GROUP20() != null) {
      return DiffieHellmanGroup.GROUP20;
    } else if (ctx.GROUP24() != null) {
      return DiffieHellmanGroup.GROUP24;
    } else if (ctx.GROUP5() != null) {
      return DiffieHellmanGroup.GROUP5;
    } else {
      throw new BatfishException("invalid dh-group");
    }
  }

  private Family toFamily(F_familyContext ctx) {
    if (ctx.ANY() != null) {
      return Family.ANY;
    } else if (ctx.BRIDGE() != null) {
      return Family.BRIDGE;
    } else if (ctx.CCC() != null) {
      return Family.CCC;
    } else if (ctx.ETHERNET_SWITCHING() != null) {
      return Family.ETHERNET_SWITCHING;
    } else if (ctx.INET() != null) {
      return Family.INET;
    } else if (ctx.INET6() != null) {
      return Family.INET6;
    } else {
      assert ctx.MPLS() != null;
      return Family.MPLS;
    }
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    int line = getLine(token);
    _configuration.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      String msg = String.format("Unrecognized Line: %d: %s", line, lineText);
      _w.redFlag(msg + " SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY");
    }
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }
}
