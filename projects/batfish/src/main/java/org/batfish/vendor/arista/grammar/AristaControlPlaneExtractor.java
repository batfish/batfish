package org.batfish.vendor.arista.grammar;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toCollection;
import static org.batfish.datamodel.ConfigurationFormat.ARISTA;
import static org.batfish.datamodel.Names.bgpNeighborStructureName;
import static org.batfish.datamodel.tracking.TrackMethods.interfaceActive;
import static org.batfish.vendor.arista.representation.AristaConfiguration.aclLineStructureName;
import static org.batfish.vendor.arista.representation.AristaConfiguration.computeRouteMapEntryName;
import static org.batfish.vendor.arista.representation.AristaStructureType.ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureType.AS_PATH_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureType.BFD_TEMPLATE;
import static org.batfish.vendor.arista.representation.AristaStructureType.BGP_LISTEN_RANGE;
import static org.batfish.vendor.arista.representation.AristaStructureType.BGP_NEIGHBOR;
import static org.batfish.vendor.arista.representation.AristaStructureType.BGP_PEER_GROUP;
import static org.batfish.vendor.arista.representation.AristaStructureType.CLASS_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureType.COMMUNITY_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureType.COMMUNITY_LIST_EXPANDED;
import static org.batfish.vendor.arista.representation.AristaStructureType.COMMUNITY_LIST_STANDARD;
import static org.batfish.vendor.arista.representation.AristaStructureType.CRYPTO_DYNAMIC_MAP_SET;
import static org.batfish.vendor.arista.representation.AristaStructureType.CRYPTO_MAP_SET;
import static org.batfish.vendor.arista.representation.AristaStructureType.INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureType.IPSEC_PROFILE;
import static org.batfish.vendor.arista.representation.AristaStructureType.IPSEC_TRANSFORM_SET;
import static org.batfish.vendor.arista.representation.AristaStructureType.IPV4_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureType.IPV4_ACCESS_LIST_EXTENDED;
import static org.batfish.vendor.arista.representation.AristaStructureType.IPV4_ACCESS_LIST_EXTENDED_LINE;
import static org.batfish.vendor.arista.representation.AristaStructureType.IPV6_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureType.IPV6_ACCESS_LIST_EXTENDED;
import static org.batfish.vendor.arista.representation.AristaStructureType.IPV6_ACCESS_LIST_STANDARD;
import static org.batfish.vendor.arista.representation.AristaStructureType.IP_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureType.IP_ACCESS_LIST_STANDARD;
import static org.batfish.vendor.arista.representation.AristaStructureType.IP_ACCESS_LIST_STANDARD_LINE;
import static org.batfish.vendor.arista.representation.AristaStructureType.ISAKMP_POLICY;
import static org.batfish.vendor.arista.representation.AristaStructureType.ISAKMP_PROFILE;
import static org.batfish.vendor.arista.representation.AristaStructureType.KEYRING;
import static org.batfish.vendor.arista.representation.AristaStructureType.L2TP_CLASS;
import static org.batfish.vendor.arista.representation.AristaStructureType.MAC_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureType.NAMED_RSA_PUB_KEY;
import static org.batfish.vendor.arista.representation.AristaStructureType.NAT_POOL;
import static org.batfish.vendor.arista.representation.AristaStructureType.PEER_FILTER;
import static org.batfish.vendor.arista.representation.AristaStructureType.POLICY_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureType.PREFIX6_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureType.PREFIX_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureType.ROUTE_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureType.ROUTE_MAP_ENTRY;
import static org.batfish.vendor.arista.representation.AristaStructureType.SERVICE_TEMPLATE;
import static org.batfish.vendor.arista.representation.AristaStructureType.TRACK;
import static org.batfish.vendor.arista.representation.AristaStructureType.VXLAN;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_AGGREGATE_ATTRIBUTE_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_AGGREGATE_MATCH_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_DEFAULT_ORIGINATE_ROUTE_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_INBOUND_PREFIX_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_INBOUND_ROUTE_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_LISTEN_RANGE_PEER_FILTER;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_LISTEN_RANGE_SELF_REF;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_NEIGHBOR_PEER_GROUP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_NEIGHBOR_SELF_REF;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_NETWORK_ORIGINATION_ROUTE_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_OUTBOUND_PREFIX_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_OUTBOUND_ROUTE_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_REDISTRIBUTE_ATTACHED_HOST_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_REDISTRIBUTE_CONNECTED_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_REDISTRIBUTE_DYNAMIC_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_REDISTRIBUTE_ISIS_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_REDISTRIBUTE_OSPFV3_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_REDISTRIBUTE_OSPF_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_REDISTRIBUTE_RIP_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_REDISTRIBUTE_STATIC_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.BGP_UPDATE_SOURCE_INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CLASS_MAP_ACCESS_GROUP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CLASS_MAP_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CLASS_MAP_ACTIVATED_SERVICE_TEMPLATE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CLASS_MAP_SERVICE_TEMPLATE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CONTROL_PLANE_ACCESS_GROUP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CONTROL_PLANE_SERVICE_POLICY_INPUT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CONTROL_PLANE_SERVICE_POLICY_OUTPUT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.COPS_LISTENER_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CRYPTO_DYNAMIC_MAP_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CRYPTO_DYNAMIC_MAP_ISAKMP_PROFILE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CRYPTO_DYNAMIC_MAP_TRANSFORM_SET;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CRYPTO_MAP_IPSEC_ISAKMP_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CRYPTO_MAP_IPSEC_ISAKMP_CRYPTO_DYNAMIC_MAP_SET;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CRYPTO_MAP_IPSEC_ISAKMP_ISAKMP_PROFILE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.CRYPTO_MAP_IPSEC_ISAKMP_TRANSFORM_SET;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.DOMAIN_LOOKUP_SOURCE_INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_BFD_TEMPLATE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_IGMP_ACCESS_GROUP_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_IGMP_HOST_PROXY_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_IGMP_STATIC_GROUP_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_IPV6_TRAFFIC_FILTER_IN;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_IPV6_TRAFFIC_FILTER_OUT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_IP_ACCESS_GROUP_IN;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_IP_ACCESS_GROUP_OUT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_IP_INBAND_ACCESS_GROUP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_IP_MULTICAST_BOUNDARY;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_MAC_ACCESS_GROUP_IN;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_MAC_ACCESS_GROUP_OUT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_PIM_NEIGHBOR_FILTER;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_SELF_REF;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.INTERFACE_SERVICE_POLICY;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IPSEC_PROFILE_ISAKMP_PROFILE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IPSEC_PROFILE_TRANSFORM_SET;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IPV4_ACCESS_LIST_EXTENDED_LINE_SELF_REFERENCE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IP_ACCESS_LIST_STANDARD_LINE_SELF_REFERENCE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IP_DOMAIN_LOOKUP_INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IP_NAT_DESTINATION_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IP_NAT_SOURCE_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IP_NAT_SOURCE_POOL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IP_NAT_SOURCE_STATIC_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IP_ROUTE_NHINT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.IP_TACACS_SOURCE_INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ISAKMP_POLICY_SELF_REF;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ISAKMP_PROFILE_KEYRING;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ISAKMP_PROFILE_SELF_REF;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ISIS_REDISTRIBUTE_CONNECTED_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ISIS_REDISTRIBUTE_STATIC_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.LOGGING_SOURCE_INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.MANAGEMENT_SSH_ACCESS_GROUP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.MANAGEMENT_TELNET_ACCESS_GROUP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.MLAG_CONFIGURATION_LOCAL_INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.MLAG_CONFIGURATION_PEER_LINK;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.MSDP_PEER_SA_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.NAMED_RSA_PUB_KEY_SELF_REF;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF6_DISTRIBUTE_LIST_PREFIX_LIST_IN;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF6_DISTRIBUTE_LIST_PREFIX_LIST_OUT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_AREA_FILTER_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_DEFAULT_ORIGINATE_ROUTE_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_DISTRIBUTE_LIST_ACCESS_LIST_IN;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_DISTRIBUTE_LIST_ACCESS_LIST_OUT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_DISTRIBUTE_LIST_PREFIX_LIST_IN;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_DISTRIBUTE_LIST_PREFIX_LIST_OUT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_DISTRIBUTE_LIST_ROUTE_MAP_IN;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_DISTRIBUTE_LIST_ROUTE_MAP_OUT;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_REDISTRIBUTE_BGP_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_REDISTRIBUTE_CONNECTED_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.OSPF_REDISTRIBUTE_STATIC_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.PIM_ACCEPT_REGISTER_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.PIM_ACCEPT_REGISTER_ROUTE_MAP;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.PIM_ACCEPT_RP_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.PIM_RP_ADDRESS_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.PIM_RP_ANNOUNCE_FILTER;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.PIM_RP_CANDIDATE_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.PIM_SEND_RP_ANNOUNCE_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.PIM_SPT_THRESHOLD_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.POLICY_MAP_CLASS;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.POLICY_MAP_CLASS_SERVICE_POLICY;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.POLICY_MAP_EVENT_CLASS;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.POLICY_MAP_EVENT_CLASS_ACTIVATE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.RIP_DISTRIBUTE_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTER_ISIS_DISTRIBUTE_LIST_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTER_PIM_RP_ADDRESS_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_CONTINUE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_ENTRY_AUTO_REF;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_MATCH_AS_PATH_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_MATCH_COMMUNITY_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_MATCH_IPV4_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_MATCH_IPV4_PREFIX_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_MATCH_IPV6_ACCESS_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_MATCH_IPV6_PREFIX_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_SET_COMMUNITY_COMMUNITY_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_SET_COMMUNITY_COMMUNITY_LIST_ADDITIVE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.ROUTE_MAP_SET_COMMUNITY_COMMUNITY_LIST_DELETE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.SERVICE_POLICY_GLOBAL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.SNMP_SERVER_COMMUNITY_ACL4;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.SNMP_SERVER_COMMUNITY_ACL6;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.SNMP_SERVER_FILE_TRANSFER_ACL;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.SNMP_SERVER_SOURCE_INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.SNMP_SERVER_TFTP_SERVER_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.SNMP_SERVER_TRAP_SOURCE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.SYSTEM_SERVICE_POLICY;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.TRACK_INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.TUNNEL_PROTECTION_IPSEC_PROFILE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.TUNNEL_SOURCE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.VXLAN_SELF_REF;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.VXLAN_SOURCE_INTERFACE;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.WCCP_GROUP_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.WCCP_REDIRECT_LIST;
import static org.batfish.vendor.arista.representation.AristaStructureUsage.WCCP_SERVICE_LIST;
import static org.batfish.vendor.arista.representation.eos.AristaRedistributeType.OSPF;
import static org.batfish.vendor.arista.representation.eos.AristaRedistributeType.OSPF_EXTERNAL;
import static org.batfish.vendor.arista.representation.eos.AristaRedistributeType.OSPF_INTERNAL;
import static org.batfish.vendor.arista.representation.eos.AristaRedistributeType.OSPF_NSSA_EXTERNAL;
import static org.batfish.vendor.arista.representation.eos.AristaRedistributeType.OSPF_NSSA_EXTERNAL_TYPE_1;
import static org.batfish.vendor.arista.representation.eos.AristaRedistributeType.OSPF_NSSA_EXTERNAL_TYPE_2;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AaaAuthenticationLoginList;
import org.batfish.datamodel.AuthenticationMethod;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.DscpType;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IntegerSpace.Builder;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ip6Wildcard;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.IpsecEncapsulationMode;
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
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.bgp.RouteDistinguisher;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfAreaSummary.SummaryRouteBehavior;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.AutoAs;
import org.batfish.datamodel.routing_policy.expr.DecrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.IgpCost;
import org.batfish.datamodel.routing_policy.expr.IncrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.LongExpr;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;
import org.batfish.datamodel.vendor_family.cisco.Aaa;
import org.batfish.datamodel.vendor_family.cisco.AaaAccounting;
import org.batfish.datamodel.vendor_family.cisco.AaaAccountingCommands;
import org.batfish.datamodel.vendor_family.cisco.AaaAccountingDefault;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthentication;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLogin;
import org.batfish.datamodel.vendor_family.cisco.L2tpClass;
import org.batfish.datamodel.vendor_family.cisco.Logging;
import org.batfish.datamodel.vendor_family.cisco.Ntp;
import org.batfish.datamodel.vendor_family.cisco.NtpServer;
import org.batfish.datamodel.vendor_family.cisco.Service;
import org.batfish.datamodel.vendor_family.cisco.Sntp;
import org.batfish.datamodel.vendor_family.cisco.SntpServer;
import org.batfish.datamodel.vendor_family.cisco.SshSettings;
import org.batfish.datamodel.vendor_family.cisco.User;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.BatfishParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_accountingContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_accounting_commands_lineContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_accounting_defaultContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_accounting_default_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_accounting_default_localContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_authenticationContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_authentication_list_methodContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_authentication_loginContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_authentication_login_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_authentication_login_privilege_modeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aaa_new_modelContext;
import org.batfish.vendor.arista.grammar.AristaParser.Access_list_actionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Access_list_ip6_rangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Access_list_ip_rangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Acl_extendedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Acl_ipv4_matchContext;
import org.batfish.vendor.arista.grammar.AristaParser.Acl_standardContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aclstd_action_lineContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aclstd_remark_lineContext;
import org.batfish.vendor.arista.grammar.AristaParser.Aclstd_seqContext;
import org.batfish.vendor.arista.grammar.AristaParser.Arista_configurationContext;
import org.batfish.vendor.arista.grammar.AristaParser.As_exprContext;
import org.batfish.vendor.arista.grammar.AristaParser.Bgp_asnContext;
import org.batfish.vendor.arista.grammar.AristaParser.Bgp_local_prefContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cd_match_addressContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cd_set_isakmp_profileContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cd_set_peerContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cd_set_pfsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cd_set_transform_setContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cip_profileContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cip_transform_setContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cipprf_set_isakmp_profileContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cipprf_set_pfsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cipprf_set_transform_setContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cipt_modeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cis_keyContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cis_policyContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cis_profileContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cispol_authenticationContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cispol_encryptionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cispol_encryption_arubaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cispol_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cispol_hashContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cispol_lifetimeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cisprf_keyringContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cisprf_local_addressContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cisprf_matchContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cisprf_self_identityContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ckp_named_keyContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ckpn_addressContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ckpn_key_stringContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ckr_local_addressContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ckr_pskContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cm_matchContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cmm_access_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cmm_access_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cmm_activated_service_templateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cmm_service_templateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Community_list_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.Continue_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Copsl_access_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cp_ip_access_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Cp_service_policyContext;
import org.batfish.vendor.arista.grammar.AristaParser.Crypto_dynamic_mapContext;
import org.batfish.vendor.arista.grammar.AristaParser.Crypto_keyringContext;
import org.batfish.vendor.arista.grammar.AristaParser.Crypto_mapContext;
import org.batfish.vendor.arista.grammar.AristaParser.Crypto_map_t_ii_match_addressContext;
import org.batfish.vendor.arista.grammar.AristaParser.Crypto_map_t_ii_set_isakmp_profileContext;
import org.batfish.vendor.arista.grammar.AristaParser.Crypto_map_t_ii_set_peerContext;
import org.batfish.vendor.arista.grammar.AristaParser.Crypto_map_t_ii_set_pfsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Crypto_map_t_ii_set_transform_setContext;
import org.batfish.vendor.arista.grammar.AristaParser.Crypto_map_t_ipsec_isakmpContext;
import org.batfish.vendor.arista.grammar.AristaParser.DecContext;
import org.batfish.vendor.arista.grammar.AristaParser.Description_lineContext;
import org.batfish.vendor.arista.grammar.AristaParser.Dh_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Distribute_list_is_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Domain_lookupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Domain_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.Domain_name_serverContext;
import org.batfish.vendor.arista.grammar.AristaParser.Dscp_typeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Enable_secretContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_as_rangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_as_range_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_bandwidth_specifierContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_bgp_communityContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_mlag_domainContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_mlag_local_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_mlag_peer_linkContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_mlag_reload_delayContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_mlag_shutdownContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_mpa_heartbeatContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_mpa_ipContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_neighbor_idContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_aa_modifiersContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_aa_v4Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_aa_v6Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_evpnContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_evpn_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_evpn_neighbor_nidContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_evpn_no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_flow_spec_ipv4Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_flow_spec_ipv6Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_ipv4_labeled_unicastContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_ipv4_multicastContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_ipv4_sr_teContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_ipv4_unicastContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_ipv6_labeled_unicastContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_ipv6_multicastContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_ipv6_sr_teContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_ipv6_unicastContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_vpn_v4Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_af_vpn_v6Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_afed_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_vab_vlanContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_vlanContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_vlan_aware_bundleContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_vlan_tail_rdContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_vlan_tail_redistributeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_vlan_tail_route_targetContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rb_vrfContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafbc_additional_pathsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafbc_next_hop_unchangedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafdnc_activateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbaffs4_default_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbaffs4_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbaffs4no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbaffs6_default_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbaffs6_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbaffs6no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4labu_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4labud_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4labuno_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4m_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4m_no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4md_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4srte_default_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4srte_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4srte_no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4u_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4u_no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4ub_next_hopContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4ub_redistribute_internalContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4ub_routeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv4ud_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6labu_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6labud_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6labuno_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6m_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6m_no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6md_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6srte_default_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6srte_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6srte_no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6u_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6u_no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafipv6ud_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnc_activateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnc_additional_pathsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnc_default_originateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnc_next_hop_unchangedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnc_prefix_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnc_route_mapContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnobc_additional_pathsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnobc_next_hop_unchangedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnonc_activateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnonc_additional_pathsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnonc_default_originateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnonc_next_hop_unchangedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafnonc_route_mapContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafvpn4_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafvpn4d_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafvpn4no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafvpn6_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafvpn6d_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbafvpn6no_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_default_metricContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_distanceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_maximum_pathsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_neighbor4Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_neighbor6Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_network4Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_network6Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_peer_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_router_idContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_shutdownContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbi_timersContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbib_additional_pathsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbib_advertise_inactiveContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbib_allowas_inContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbib_always_compare_medContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbib_client_to_clientContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbib_cluster_idContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbib_enforce_first_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbib_missing_policyContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbib_next_hop_unchangedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbibbp_tie_breakContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbibbpa_multipath_relaxContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbibconf_identifierContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbibconf_peersContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbibd_ipv4u_enabledContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbibl_limitContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbibl_rangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbibtrans_listen_portContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbid_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbidn_export_localprefContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbin_peer_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_additional_pathsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_allowas_inContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_default_originateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_descriptionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_dont_capability_negotiateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_ebgp_multihopContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_enforce_first_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_export_localprefContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_local_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_maximum_accepted_routesContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_maximum_routesContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_next_hop_peerContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_next_hop_selfContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_next_hop_unchangedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_passiveContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_remote_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_remove_private_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_route_reflector_clientContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_send_communityContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_shutdownContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinc_update_sourceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bc_identifierContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_advertise_inactiveContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_allowas_inContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_always_compare_medContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_bpa_multipath_relaxContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_bpt_cluster_list_lengthContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_bpt_router_idContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_client_to_clientContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_cluster_idContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_default_ipv4u_enabledContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_bgp_next_hop_unchangedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_default_metricContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_neighbor_neighborContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_router_idContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbino_shutdownContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_allowas_inContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_default_originateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_descriptionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_ebgp_multihopContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_enforce_first_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_export_localprefContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_local_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_next_hop_peerContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_next_hop_selfContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_next_hop_unchangedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_passiveContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_peer_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_remote_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_remove_private_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_route_reflector_clientContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_route_to_peerContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_send_communityContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_shutdownContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinon_update_sourceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinor_attached_hostContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinor_connectedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinor_isisContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinor_ospfContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinor_ospf_matchContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinor_ripContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbinor_staticContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbir_attached_hostContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbir_connectedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbir_dynamicContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbir_isisContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbir_ospf3Context;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbir_ospfContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbir_ripContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbir_staticContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbv_local_asContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbv_rdContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_rbv_route_targetContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vlan_idContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_arpContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_descriptionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_vxlan_floodContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_vxlan_multicast_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_vxlan_source_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_vxlan_udp_portContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_vxlan_vlanContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_vxlan_vlan_vniContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_vxlan_vlan_vni_rangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Eos_vxif_vxlan_vrfContext;
import org.batfish.vendor.arista.grammar.AristaParser.Extended_access_list_additional_featureContext;
import org.batfish.vendor.arista.grammar.AristaParser.Extended_access_list_tailContext;
import org.batfish.vendor.arista.grammar.AristaParser.Extended_ipv6_access_list_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Extended_ipv6_access_list_tailContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_autostateContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_bandwidthContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_bfd_templateContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_crypto_mapContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_descriptionContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_encapsulation_dot1q_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_eos_mlagContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_ip_helper_addressContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_ip_inband_access_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_ip_local_proxy_arp_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_ip_virtual_routerContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_ipv6_traffic_filterContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_isis_metricContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_member_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_mtuContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_autostateContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_bandwidthContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_channel_group_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_description_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_ip_local_proxy_arp_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_ip_proxy_arp_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_shutdown_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_speed_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_st_portfastContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_no_switchport_switchport_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_noswpt_allowed_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_noswpt_group_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_service_policyContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_shutdown_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_spanning_treeContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_speed_auto_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_speed_bw_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_speed_forced_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_st_portfastContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_switchport_accessContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_switchport_modeContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_switchport_switchportContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_switchport_trunk_allowed_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_switchport_trunk_group_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_switchport_trunk_native_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_vrf_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.If_vrrpContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifcg_num_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifcg_recirculation_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifigmp_access_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifigmphp_access_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifigmpsg_aclContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifip_access_group_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifip_address_address_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifip_address_virtual_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifip_proxy_arp_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipm_boundary_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipnd_staticContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipns_dynamicContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipns_staticContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipo_area_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipo_cost_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipo_dead_interval_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipo_hello_interval_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipo_network_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifipp_neighbor_filter_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifmac_access_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Iftunnel_destinationContext;
import org.batfish.vendor.arista.grammar.AristaParser.Iftunnel_modeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Iftunnel_protectionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Iftunnel_sourceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifvrrp_authenticationContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifvrrp_ipContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifvrrp_ipv4Context;
import org.batfish.vendor.arista.grammar.AristaParser.Ifvrrp_preemptContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifvrrp_priorityContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ifvrrp_priority_levelContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ike_encryptionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ike_encryption_arubaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Int_exprContext;
import org.batfish.vendor.arista.grammar.AristaParser.Interface_addressContext;
import org.batfish.vendor.arista.grammar.AristaParser.Interface_is_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Interface_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_community_list_expanded_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_community_list_standard_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_dhcp_relay_serverContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_domain_lookupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_domain_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_hostnameContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_nat_poolContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_nat_pool_rangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_prefixContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_prefix_list_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_prefix_list_tailContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_route_nexthopContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ip_ssh_versionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ipap_access_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ipap_originContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ipsec_authenticationContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ipsec_encryptionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ipsec_encryption_arubaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ipv6_prefix_list_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ipv6_prefix_list_tailContext;
import org.batfish.vendor.arista.grammar.AristaParser.Is_type_is_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Literal_standard_communityContext;
import org.batfish.vendor.arista.grammar.AristaParser.Logging_vrfContext;
import org.batfish.vendor.arista.grammar.AristaParser.Logging_vrf_hostContext;
import org.batfish.vendor.arista.grammar.AristaParser.Logging_vrf_source_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Mac_access_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Management_ssh_ip_access_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Management_telnet_ip_access_groupContext;
import org.batfish.vendor.arista.grammar.AristaParser.Match_as_path_access_list_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Match_community_list_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Match_ip_access_list_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Match_ip_prefix_list_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Match_ipv6_access_list_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Match_ipv6_prefix_list_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Match_semanticsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Match_source_protocol_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Match_tag_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Net_is_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.No_ip_prefix_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.No_ip_prefix_list_seqContext;
import org.batfish.vendor.arista.grammar.AristaParser.No_ip_prefix_list_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.No_ip_routeContext;
import org.batfish.vendor.arista.grammar.AristaParser.No_route_map_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.No_vlanContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ntp_serverContext;
import org.batfish.vendor.arista.grammar.AristaParser.Origin_expr_literalContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ospf_areaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Passive_iis_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Passive_interface_default_is_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Passive_interface_is_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Peer_filter_lineContext;
import org.batfish.vendor.arista.grammar.AristaParser.Peer_sa_filterContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pim_accept_registerContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pim_accept_rpContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pim_rp_addressContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pim_rp_announce_filterContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pim_rp_candidateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pim_send_rp_announceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pim_sm4_rp_addressContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pim_spt_thresholdContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pm_classContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pm_event_classContext;
import org.batfish.vendor.arista.grammar.AristaParser.Pmc_service_policyContext;
import org.batfish.vendor.arista.grammar.AristaParser.PortContext;
import org.batfish.vendor.arista.grammar.AristaParser.Port_numberContext;
import org.batfish.vendor.arista.grammar.AristaParser.Port_specifierContext;
import org.batfish.vendor.arista.grammar.AristaParser.ProtocolContext;
import org.batfish.vendor.arista.grammar.AristaParser.Protocol_distanceContext;
import org.batfish.vendor.arista.grammar.AristaParser.RangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Redistribute_connected_is_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Redistribute_static_is_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Rms_distanceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Rmsc_communitiesContext;
import org.batfish.vendor.arista.grammar.AristaParser.Rmsc_community_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Rmsc_noneContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro6_distribute_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_area_filterlistContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_area_nssaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_area_rangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_area_stubContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_auto_costContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_default_informationContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_default_metricContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_distribute_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_max_metricContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_maximum_pathsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_networkContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_passive_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_passive_interface_defaultContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_redistribute_bgp_aristaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_redistribute_connectedContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_redistribute_ripContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_redistribute_staticContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_rfc1583_compatibilityContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ro_router_idContext;
import org.batfish.vendor.arista.grammar.AristaParser.Route_distinguisherContext;
import org.batfish.vendor.arista.grammar.AristaParser.Route_map_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Route_targetContext;
import org.batfish.vendor.arista.grammar.AristaParser.Router_bgp_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Router_isis_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Rr_distribute_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Rr_networkContext;
import org.batfish.vendor.arista.grammar.AristaParser.Rr_passive_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Rr_passive_interface_defaultContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_aaaContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_access_lineContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_banner_eosContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_bfd_templateContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_class_mapContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_domain_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_eos_mlagContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_eos_vxlan_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_hostnameContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_default_gatewayContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_dhcpContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_domainContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_domain_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_name_serverContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_pimContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_routeContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_source_routeContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_sshContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_tacacs_source_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ip_virtual_routerContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_l2tp_classContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_loggingContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_no_router_ospfContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_ntpContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_peer_filterContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_policy_mapContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_router_multicastContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_router_ospfContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_router_pimContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_router_ripContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_serviceContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_service_policy_globalContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_service_templateContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_snmp_serverContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_sntpContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_spanning_treeContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_switchportContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_system_service_policyContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_tacacs_serverContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_trackContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_usernameContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_vlanContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_vlan_internalContext;
import org.batfish.vendor.arista.grammar.AristaParser.S_vrf_definitionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Sd_switchport_blankContext;
import org.batfish.vendor.arista.grammar.AristaParser.Sd_switchport_shutdownContext;
import org.batfish.vendor.arista.grammar.AristaParser.Set_as_path_prepend_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Set_local_preference_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Set_metric_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Set_metric_type_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Set_next_hop_peer_address_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Set_next_hop_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Set_origin_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Set_tag_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Set_weight_rm_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Sntp_serverContext;
import org.batfish.vendor.arista.grammar.AristaParser.Spanning_tree_portfastContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ss_communityContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ss_enable_trapsContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ss_file_transferContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ss_hostContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ss_source_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ss_tftp_server_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ss_trap_sourceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ssc_access_controlContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ssc_use_ipv4_aclContext;
import org.batfish.vendor.arista.grammar.AristaParser.Standard_access_list_additional_featureContext;
import org.batfish.vendor.arista.grammar.AristaParser.Standard_ipv6_access_list_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Standard_ipv6_access_list_tailContext;
import org.batfish.vendor.arista.grammar.AristaParser.SubrangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Summary_address_is_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Suppressed_iis_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Switching_mode_stanzaContext;
import org.batfish.vendor.arista.grammar.AristaParser.Switchport_trunk_encapsulationContext;
import org.batfish.vendor.arista.grammar.AristaParser.Track_interfaceContext;
import org.batfish.vendor.arista.grammar.AristaParser.Ts_hostContext;
import org.batfish.vendor.arista.grammar.AristaParser.U_passwordContext;
import org.batfish.vendor.arista.grammar.AristaParser.U_roleContext;
import org.batfish.vendor.arista.grammar.AristaParser.Uint16Context;
import org.batfish.vendor.arista.grammar.AristaParser.Uint32Context;
import org.batfish.vendor.arista.grammar.AristaParser.Uint8Context;
import org.batfish.vendor.arista.grammar.AristaParser.VariableContext;
import org.batfish.vendor.arista.grammar.AristaParser.Variable_access_listContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_d_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_d_stateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_d_trunkContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_idContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_no_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_no_stateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_no_trunkContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_stateContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_subrangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vlan_trunkContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vni_numberContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vni_subrangeContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vrf_nameContext;
import org.batfish.vendor.arista.grammar.AristaParser.Vrfd_descriptionContext;
import org.batfish.vendor.arista.grammar.AristaParser.Wccp_idContext;
import org.batfish.vendor.arista.representation.AccessListAddressSpecifier;
import org.batfish.vendor.arista.representation.AccessListServiceSpecifier;
import org.batfish.vendor.arista.representation.AnyAddressSpecifier;
import org.batfish.vendor.arista.representation.AristaConfiguration;
import org.batfish.vendor.arista.representation.AristaDestinationStaticNat;
import org.batfish.vendor.arista.representation.AristaDynamicSourceNat;
import org.batfish.vendor.arista.representation.AristaStaticSourceNat;
import org.batfish.vendor.arista.representation.AristaStructureType;
import org.batfish.vendor.arista.representation.AristaStructureUsage;
import org.batfish.vendor.arista.representation.CryptoMapEntry;
import org.batfish.vendor.arista.representation.CryptoMapSet;
import org.batfish.vendor.arista.representation.DistributeList;
import org.batfish.vendor.arista.representation.DistributeList.DistributeListFilterType;
import org.batfish.vendor.arista.representation.ExpandedCommunityList;
import org.batfish.vendor.arista.representation.ExpandedCommunityListLine;
import org.batfish.vendor.arista.representation.ExtendedAccessList;
import org.batfish.vendor.arista.representation.ExtendedAccessListLine;
import org.batfish.vendor.arista.representation.ExtendedIpv6AccessList;
import org.batfish.vendor.arista.representation.ExtendedIpv6AccessListLine;
import org.batfish.vendor.arista.representation.Interface;
import org.batfish.vendor.arista.representation.IpAsPathAccessList;
import org.batfish.vendor.arista.representation.IpAsPathAccessListLine;
import org.batfish.vendor.arista.representation.IpsecProfile;
import org.batfish.vendor.arista.representation.IpsecTransformSet;
import org.batfish.vendor.arista.representation.IsakmpKey;
import org.batfish.vendor.arista.representation.IsakmpPolicy;
import org.batfish.vendor.arista.representation.IsakmpProfile;
import org.batfish.vendor.arista.representation.IsisProcess;
import org.batfish.vendor.arista.representation.IsisRedistributionPolicy;
import org.batfish.vendor.arista.representation.Keyring;
import org.batfish.vendor.arista.representation.LoggingHost;
import org.batfish.vendor.arista.representation.MatchSemantics;
import org.batfish.vendor.arista.representation.MlagConfiguration;
import org.batfish.vendor.arista.representation.NamedRsaPubKey;
import org.batfish.vendor.arista.representation.NatPool;
import org.batfish.vendor.arista.representation.NatProtocol;
import org.batfish.vendor.arista.representation.NssaSettings;
import org.batfish.vendor.arista.representation.OspfNetwork;
import org.batfish.vendor.arista.representation.OspfNetworkType;
import org.batfish.vendor.arista.representation.OspfProcess;
import org.batfish.vendor.arista.representation.OspfRedistributionPolicy;
import org.batfish.vendor.arista.representation.Prefix6List;
import org.batfish.vendor.arista.representation.Prefix6ListLine;
import org.batfish.vendor.arista.representation.PrefixList;
import org.batfish.vendor.arista.representation.PrefixListLine;
import org.batfish.vendor.arista.representation.RedistributionSourceProtocol;
import org.batfish.vendor.arista.representation.RipProcess;
import org.batfish.vendor.arista.representation.RouteMap;
import org.batfish.vendor.arista.representation.RouteMapClause;
import org.batfish.vendor.arista.representation.RouteMapContinue;
import org.batfish.vendor.arista.representation.RouteMapMatchAsPathAccessListLine;
import org.batfish.vendor.arista.representation.RouteMapMatchCommunity;
import org.batfish.vendor.arista.representation.RouteMapMatchIpAccessListLine;
import org.batfish.vendor.arista.representation.RouteMapMatchIpPrefixListLine;
import org.batfish.vendor.arista.representation.RouteMapMatchIpv6AccessListLine;
import org.batfish.vendor.arista.representation.RouteMapMatchIpv6PrefixListLine;
import org.batfish.vendor.arista.representation.RouteMapMatchSourceProtocolLine;
import org.batfish.vendor.arista.representation.RouteMapMatchTagLine;
import org.batfish.vendor.arista.representation.RouteMapSetAsPathPrependLine;
import org.batfish.vendor.arista.representation.RouteMapSetCommunity;
import org.batfish.vendor.arista.representation.RouteMapSetCommunityDelete;
import org.batfish.vendor.arista.representation.RouteMapSetCommunityList;
import org.batfish.vendor.arista.representation.RouteMapSetCommunityListDelete;
import org.batfish.vendor.arista.representation.RouteMapSetCommunityNone;
import org.batfish.vendor.arista.representation.RouteMapSetDistanceLine;
import org.batfish.vendor.arista.representation.RouteMapSetLine;
import org.batfish.vendor.arista.representation.RouteMapSetLocalPreferenceLine;
import org.batfish.vendor.arista.representation.RouteMapSetMetricLine;
import org.batfish.vendor.arista.representation.RouteMapSetNextHopLine;
import org.batfish.vendor.arista.representation.RouteMapSetNextHopPeerAddress;
import org.batfish.vendor.arista.representation.RouteMapSetOriginTypeLine;
import org.batfish.vendor.arista.representation.RouteMapSetTagLine;
import org.batfish.vendor.arista.representation.RouteMapSetWeightLine;
import org.batfish.vendor.arista.representation.SimpleExtendedAccessListServiceSpecifier;
import org.batfish.vendor.arista.representation.StandardAccessList;
import org.batfish.vendor.arista.representation.StandardAccessListActionLine;
import org.batfish.vendor.arista.representation.StandardAccessListLine;
import org.batfish.vendor.arista.representation.StandardAccessListRemarkLine;
import org.batfish.vendor.arista.representation.StandardCommunityList;
import org.batfish.vendor.arista.representation.StandardCommunityListLine;
import org.batfish.vendor.arista.representation.StandardIpv6AccessList;
import org.batfish.vendor.arista.representation.StandardIpv6AccessListLine;
import org.batfish.vendor.arista.representation.StaticRoute;
import org.batfish.vendor.arista.representation.StaticRoute.NextHop;
import org.batfish.vendor.arista.representation.StaticRouteManager;
import org.batfish.vendor.arista.representation.StubSettings;
import org.batfish.vendor.arista.representation.Tunnel;
import org.batfish.vendor.arista.representation.Tunnel.TunnelMode;
import org.batfish.vendor.arista.representation.UnimplementedAccessListServiceSpecifier;
import org.batfish.vendor.arista.representation.Vlan;
import org.batfish.vendor.arista.representation.Vlan.State;
import org.batfish.vendor.arista.representation.Vrf;
import org.batfish.vendor.arista.representation.VrrpGroup;
import org.batfish.vendor.arista.representation.VrrpInterface;
import org.batfish.vendor.arista.representation.WildcardAddressSpecifier;
import org.batfish.vendor.arista.representation.eos.AristaBgpAdditionalPathsConfig;
import org.batfish.vendor.arista.representation.eos.AristaBgpAdditionalPathsConfig.SendType;
import org.batfish.vendor.arista.representation.eos.AristaBgpAggregateNetwork;
import org.batfish.vendor.arista.representation.eos.AristaBgpBestpathTieBreaker;
import org.batfish.vendor.arista.representation.eos.AristaBgpDefaultOriginate;
import org.batfish.vendor.arista.representation.eos.AristaBgpHasPeerGroup;
import org.batfish.vendor.arista.representation.eos.AristaBgpNeighbor;
import org.batfish.vendor.arista.representation.eos.AristaBgpNeighbor.RemovePrivateAsMode;
import org.batfish.vendor.arista.representation.eos.AristaBgpNeighborAddressFamily;
import org.batfish.vendor.arista.representation.eos.AristaBgpNetworkConfiguration;
import org.batfish.vendor.arista.representation.eos.AristaBgpPeerFilter;
import org.batfish.vendor.arista.representation.eos.AristaBgpPeerFilterLine;
import org.batfish.vendor.arista.representation.eos.AristaBgpPeerGroupNeighbor;
import org.batfish.vendor.arista.representation.eos.AristaBgpProcess;
import org.batfish.vendor.arista.representation.eos.AristaBgpV4DynamicNeighbor;
import org.batfish.vendor.arista.representation.eos.AristaBgpV4Neighbor;
import org.batfish.vendor.arista.representation.eos.AristaBgpV6Neighbor;
import org.batfish.vendor.arista.representation.eos.AristaBgpVlan;
import org.batfish.vendor.arista.representation.eos.AristaBgpVlanAwareBundle;
import org.batfish.vendor.arista.representation.eos.AristaBgpVlanBase;
import org.batfish.vendor.arista.representation.eos.AristaBgpVrf;
import org.batfish.vendor.arista.representation.eos.AristaBgpVrfAddressFamily;
import org.batfish.vendor.arista.representation.eos.AristaBgpVrfIpv4UnicastAddressFamily;
import org.batfish.vendor.arista.representation.eos.AristaEosVxlan;
import org.batfish.vendor.arista.representation.eos.AristaRedistributeType;

public class AristaControlPlaneExtractor extends AristaParserBaseListener
    implements SilentSyntaxListener, ControlPlaneExtractor {
  private static final IntegerSpace ADMIN_DISTANCE_SPACE = IntegerSpace.of(Range.closed(1, 255));
  private static final IntegerSpace VLAN_RANGE = IntegerSpace.of(Range.closed(1, 4094));
  private static final IntegerSpace VNI_RANGE = IntegerSpace.of(Range.closed(1, 16777215));

  @Override
  public String getInputText() {
    return _text;
  }

  @Override
  public BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Override
  public Warnings getWarnings() {
    return _w;
  }

  private static String getDescription(Description_lineContext ctx) {
    return ctx.text != null ? ctx.text.getText().trim() : "";
  }

  private static Ip6Wildcard getIpWildcard(Access_list_ip6_rangeContext ctx) {
    if (ctx.prefix6 != null) {
      return Ip6Wildcard.parse(ctx.prefix6.getText());
    } else if (ctx.ANY() != null) {
      return Ip6Wildcard.ANY;
    } else {
      assert ctx.ipv6 != null;
      return Ip6Wildcard.parse(ctx.ipv6.getText());
    }
  }

  private static int toInteger(DecContext ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private Optional<Integer> toInteger(ParserRuleContext messageCtx, Protocol_distanceContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, ADMIN_DISTANCE_SPACE, "admin distance");
  }

  private static int toInteger(Uint16Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private static int toInteger(Uint8Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private static int toInteger(Token t) {
    return Integer.parseInt(t.getText());
  }

  private @Nonnull Optional<Integer> toInteger(ParserRuleContext messageCtx, Vlan_idContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, VLAN_RANGE, "VLAN number");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Vni_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, VNI_RANGE, "VNI number");
  }

  private static String toInterfaceName(Interface_nameContext ctx) {
    StringBuilder name =
        new StringBuilder(
            AristaConfiguration.getCanonicalInterfaceNamePrefix(ctx.name_prefix_alpha.getText()));
    for (Token part : ctx.name_middle_parts) {
      name.append(part.getText());
    }
    if (ctx.range().range_list.size() != 1) {
      throw new BatfishException(
          "got interface range where single interface was expected: '" + ctx.getText() + "'");
    }
    name.append(ctx.range().getText());
    return name.toString();
  }

  private static long toAsNum(Bgp_asnContext ctx) {
    if (ctx.asn != null) {
      return toLong(ctx.asn);
    }
    String[] parts = ctx.asn4b.getText().split("\\.");
    return (Long.parseLong(parts[0]) << 16) + Long.parseLong(parts[1]);
  }

  private static @Nonnull LongSpace toAsSpace(Eos_as_rangeContext rangeContext) {
    if (rangeContext.hi == null) {
      return LongSpace.of(toAsNum(rangeContext.lo));
    } else {
      return LongSpace.of(Range.closed(toAsNum(rangeContext.lo), toAsNum(rangeContext.hi)));
    }
  }

  private static @Nonnull LongSpace toAsSpace(Eos_as_range_listContext asns) {
    LongSpace.Builder builder = LongSpace.builder();
    for (Eos_as_rangeContext rangeContext : asns.aslist) {
      builder.including(toAsSpace(rangeContext));
    }
    return builder.build();
  }

  private static @Nonnull IntegerSpace toIntegerSpace(Eos_vlan_idContext ctx) {
    return ctx.vlan_ids.stream()
        .map(innerctx -> IntegerSpace.of(toSubRange(innerctx)))
        .reduce(IntegerSpace::union)
        .get();
  }

  private static Ip toIp(TerminalNode t) {
    return Ip.parse(t.getText());
  }

  private static Ip toIp(Token t) {
    return Ip.parse(t.getText());
  }

  private static Ip6 toIp6(Token t) {
    return Ip6.parse(t.getText());
  }

  private static Prefix toPrefix(Token t) {
    return Prefix.parse(t.getText());
  }

  private static Prefix toPrefix(Ip_prefixContext ctx) {
    if (ctx.prefix != null) {
      return Prefix.parse(ctx.prefix.getText());
    }
    assert ctx.address != null && ctx.mask != null;
    Ip addr = toIp(ctx.address);
    Ip mask = toIp(ctx.mask);
    return Prefix.create(addr, mask);
  }

  private static List<SubRange> toRange(RangeContext ctx) {
    List<SubRange> range = new ArrayList<>();
    for (SubrangeContext sc : ctx.range_list) {
      SubRange sr = toSubRange(sc);
      range.add(sr);
    }
    return range;
  }

  private static SubRange toSubRange(SubrangeContext ctx) {
    int low = toInteger(ctx.low);
    if (ctx.DASH() != null) {
      int high = toInteger(ctx.high);
      return new SubRange(low, high);
    } else {
      return SubRange.singleton(low);
    }
  }

  private @Nonnull Optional<SubRange> toSubRange(
      ParserRuleContext messageCtx, Vlan_subrangeContext ctx) {
    Optional<Integer> maybeLow = toInteger(messageCtx, ctx.low);
    if (!maybeLow.isPresent()) {
      // already warned
      return Optional.empty();
    }
    int low = maybeLow.get();
    if (ctx.high == null) {
      return Optional.of(SubRange.singleton(low));
    }
    Optional<Integer> maybeHigh = toInteger(messageCtx, ctx.high);
    if (!maybeHigh.isPresent()) {
      // already warned
      return Optional.empty();
    }
    int high = maybeHigh.get();
    if (low > high) {
      warn(
          messageCtx,
          String.format("Invalid VLAN range with high VLAN < low VLAN: %s", getFullText(ctx)));
      return Optional.empty();
    }
    return Optional.of(new SubRange(low, high));
  }

  private @Nonnull Optional<SubRange> toSubRange(
      ParserRuleContext messageCtx, Vni_subrangeContext ctx) {
    Optional<Integer> maybeLow = toInteger(messageCtx, ctx.low);
    if (!maybeLow.isPresent()) {
      // already warned
      return Optional.empty();
    }
    int low = maybeLow.get();
    if (ctx.high == null) {
      return Optional.of(SubRange.singleton(low));
    }
    Optional<Integer> maybeHigh = toInteger(messageCtx, ctx.high);
    if (!maybeHigh.isPresent()) {
      // already warned
      return Optional.empty();
    }
    int high = maybeHigh.get();
    if (low > high) {
      warn(
          messageCtx,
          String.format("Invalid VNI range with high VNI < low VNI: %s", getFullText(ctx)));
      return Optional.empty();
    }
    return Optional.of(new SubRange(low, high));
  }

  private static String unquote(String text) {
    if (text.isEmpty()) {
      return text;
    }
    char firstChar = text.charAt(0);
    if (firstChar != '"' && firstChar != '\'') {
      return text;
    }
    char lastChar = text.charAt(text.length() - 1);
    if ((firstChar == '"' && lastChar == '"') || (firstChar == '\'' && lastChar == '\'')) {
      return text.substring(1, text.length() - 1);
    } else {
      throw new BatfishException("Improperly-quoted string");
    }
  }

  private AristaConfiguration _configuration;

  private AaaAuthenticationLoginList _currentAaaAuthenticationLoginList;

  private Long _currentAclSeq;

  private AristaBgpAggregateNetwork _currentAristaBgpAggregateNetwork;
  private AristaBgpNeighbor _currentAristaBgpNeighbor;
  private boolean _currentAristaBgpNeighborIsBrandNew;
  private AristaBgpNeighborAddressFamily _currentAristaBgpNeighborAddressFamily;
  private AristaBgpProcess _currentAristaBgpProcess;
  private AristaBgpVlanBase _currentAristaBgpVlan;
  private AristaBgpVrf _currentAristaBgpVrf;
  private AristaBgpVrfAddressFamily _currentAristaBgpVrfAf;

  private CryptoMapEntry _currentCryptoMapEntry;

  private String _currentCryptoMapName;

  private Integer _currentCryptoMapSequenceNum;

  private NamedRsaPubKey _currentNamedRsaPubKey;

  private ExtendedAccessList _currentExtendedAcl;

  private ExtendedIpv6AccessList _currentExtendedIpv6Acl;

  private List<Interface> _currentInterfaces;

  private IsakmpPolicy _currentIsakmpPolicy;

  private IsakmpProfile _currentIsakmpProfile;

  private IpsecTransformSet _currentIpsecTransformSet;

  private IpsecProfile _currentIpsecProfile;

  private Interface _currentIsisInterface;

  private IsisProcess _currentIsisProcess;

  private Keyring _currentKeyring;

  private OspfProcess _currentOspfProcess;

  private @Nullable AristaBgpPeerFilter _currentPeerFilter;

  private Prefix6List _currentPrefix6List;

  private PrefixList _currentPrefixList;

  private RipProcess _currentRipProcess;

  private RouteMap _currentRouteMap;

  private RouteMapClause _currentRouteMapClause;

  private SnmpCommunity _currentSnmpCommunity;

  private StandardAccessList _currentStandardAcl;

  private StandardIpv6AccessList _currentStandardIpv6Acl;

  private User _currentUser;

  private @Nonnull List<Vlan> _currentVlans = ImmutableList.of();

  private Integer _currentVxlanVlanNum;

  private String _currentVrf;

  private Integer _currentVrrpGroupNum;

  private final ConfigurationFormat _format;

  private boolean _no;

  private final AristaCombinedParser _parser;

  private final String _text;

  private final Warnings _w;

  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  private String _currentTrackingGroup;

  private AristaEosVxlan _eosVxlan;

  public AristaControlPlaneExtractor(
      String text,
      AristaCombinedParser parser,
      ConfigurationFormat format,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _text = text;
    _parser = parser;
    _format = format;
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  private Interface addInterface(String name, Interface_nameContext ctx, boolean explicit) {
    Interface newInterface = _configuration.getInterfaces().get(name);
    if (newInterface == null) {
      newInterface = new Interface(name, _configuration);
      _configuration.getInterfaces().put(name, newInterface);
      initInterface(newInterface, ctx);
    } else {
      _w.pedantic("Interface: '" + name + "' altered more than once");
    }
    newInterface.setDeclaredNames(
        new ImmutableSortedSet.Builder<String>(naturalOrder())
            .addAll(newInterface.getDeclaredNames())
            .add(ctx.getText())
            .build());
    if (explicit) {
      _currentInterfaces.add(newInterface);
    }
    return newInterface;
  }

  private Interface getOrAddInterface(Interface_nameContext ctx) {
    String canonicalIfaceName = getCanonicalInterfaceName(ctx.getText());
    Interface iface = _configuration.getInterfaces().get(canonicalIfaceName);
    if (iface == null) {
      iface = addInterface(canonicalIfaceName, ctx, false);
    }
    return iface;
  }

  private String convErrorMessage(Class<?> type, ParserRuleContext ctx) {
    return String.format("Could not convert to %s: %s", type.getSimpleName(), getFullText(ctx));
  }

  private BatfishException convError(Class<?> type, ParserRuleContext ctx) {
    return new BatfishException(convErrorMessage(type, ctx));
  }

  private Vrf currentVrf() {
    return initVrf(_currentVrf);
  }

  @Override
  public void enterArista_configuration(Arista_configurationContext ctx) {
    _configuration = new AristaConfiguration();
    _configuration.setVendor(_format);
    _currentVrf = AristaConfiguration.DEFAULT_VRF_NAME;
  }

  @Override
  public void enterAaa_accounting(Aaa_accountingContext ctx) {
    if (_configuration.getCf().getAaa().getAccounting() == null) {
      _configuration.getCf().getAaa().setAccounting(new AaaAccounting());
    }
  }

  @Override
  public void enterAaa_accounting_commands_line(Aaa_accounting_commands_lineContext ctx) {
    Map<String, AaaAccountingCommands> commands =
        _configuration.getCf().getAaa().getAccounting().getCommands();
    Set<String> levels = new TreeSet<>();
    if (ctx.levels != null) {
      List<SubRange> range = toRange(ctx.levels);
      for (SubRange subRange : range) {
        for (int i = subRange.getStart(); i <= subRange.getEnd(); i++) {
          String level = Integer.toString(i);
          levels.add(level);
        }
      }
    } else {
      levels.add(AaaAccounting.DEFAULT_COMMANDS);
    }
    List<AaaAccountingCommands> currentAaaAccountingCommands = new ArrayList<>();
    for (String level : levels) {
      AaaAccountingCommands c = commands.computeIfAbsent(level, k -> new AaaAccountingCommands());
      currentAaaAccountingCommands.add(c);
    }
  }

  @Override
  public void enterAaa_accounting_default(Aaa_accounting_defaultContext ctx) {
    AaaAccounting accounting = _configuration.getCf().getAaa().getAccounting();
    if (accounting.getDefault() == null) {
      accounting.setDefault(new AaaAccountingDefault());
    }
  }

  @Override
  public void enterAaa_authentication(Aaa_authenticationContext ctx) {
    if (_configuration.getCf().getAaa().getAuthentication() == null) {
      _configuration.getCf().getAaa().setAuthentication(new AaaAuthentication());
    }
  }

  @Override
  public void enterAaa_authentication_login(Aaa_authentication_loginContext ctx) {
    if (_configuration.getCf().getAaa().getAuthentication().getLogin() == null) {
      _configuration.getCf().getAaa().getAuthentication().setLogin(new AaaAuthenticationLogin());
    }
  }

  @Override
  public void enterAaa_authentication_login_list(Aaa_authentication_login_listContext ctx) {
    AaaAuthenticationLogin login = _configuration.getCf().getAaa().getAuthentication().getLogin();
    String name;
    if (ctx.DEFAULT() != null) {
      name = AaaAuthenticationLogin.DEFAULT_LIST_NAME;
    } else if (ctx.variable() != null) {
      name = ctx.variable().getText();
    } else {
      throw new BatfishException("Unsupported mode");
    }

    List<AuthenticationMethod> methods = new ArrayList<>();

    for (Aaa_authentication_list_methodContext method : ctx.aaa_authentication_list_method()) {
      methods.add(AuthenticationMethod.toAuthenticationMethod(method.getText()));
    }

    _currentAaaAuthenticationLoginList =
        login.getLists().computeIfAbsent(name, k -> new AaaAuthenticationLoginList(methods));

    // apply the list to each line
    SortedMap<String, Line> lines = _configuration.getCf().getLines();
    for (Line line : lines.values()) {
      if (name.equals(AaaAuthenticationLogin.DEFAULT_LIST_NAME)) {
        // only apply default list if no other list applied
        if (line.getAaaAuthenticationLoginList() == null) {
          line.setAaaAuthenticationLoginList(_currentAaaAuthenticationLoginList);
          line.setLoginAuthentication(name);
        }
      } else if (line.getLoginAuthentication() != null
          && line.getLoginAuthentication().equals(name)) {
        // if not the default list, apply it to lines that have specified this list as it's login
        // list
        line.setAaaAuthenticationLoginList(_currentAaaAuthenticationLoginList);
      }
    }
  }

  @Override
  public void enterAcl_extended(Acl_extendedContext ctx) {
    String name = ctx.name.getText();
    _currentExtendedAcl =
        _configuration.getExtendedAcls().computeIfAbsent(name, ExtendedAccessList::new);
    _configuration.defineStructure(IPV4_ACCESS_LIST_EXTENDED, name, ctx);
  }

  @Override
  public void exitAcl_extended(Acl_extendedContext ctx) {
    _currentExtendedAcl = null;
  }

  @Override
  public void enterAcl_standard(Acl_standardContext ctx) {
    String name = ctx.name.getText();
    _currentStandardAcl =
        _configuration.getStandardAcls().computeIfAbsent(name, StandardAccessList::new);
    _configuration.defineStructure(IP_ACCESS_LIST_STANDARD, name, ctx);
  }

  @Override
  public void exitAcl_standard(Acl_standardContext ctx) {
    _currentStandardAcl = null;
  }

  @Override
  public void enterAclstd_seq(Aclstd_seqContext ctx) {
    if (ctx.seq != null) {
      _currentAclSeq = toLong(ctx.seq);
    } else {
      SortedMap<Long, StandardAccessListLine> lines = _currentStandardAcl.getLines();
      long lastKey = lines.isEmpty() ? 0L : lines.lastKey();
      _currentAclSeq = lastKey + 10;
    }
    // structure definition tracking
    String lineName = getFullText(ctx).trim();
    String structName = aclLineStructureName(_currentStandardAcl.getName(), lineName);
    int configLine = ctx.start.getLine();
    _configuration.defineSingleLineStructure(IP_ACCESS_LIST_STANDARD_LINE, structName, configLine);
    _configuration.referenceStructure(
        IP_ACCESS_LIST_STANDARD_LINE,
        structName,
        IP_ACCESS_LIST_STANDARD_LINE_SELF_REFERENCE,
        configLine);
  }

  @Override
  public void exitAclstd_seq(Aclstd_seqContext ctx) {
    _currentAclSeq = null;
  }

  public @Nonnull IpWildcard toIpWildcard(Acl_ipv4_matchContext ctx) {
    if (ctx.host != null) {
      return IpWildcard.create(toIp(ctx.host));
    } else if (ctx.prefix != null) {
      return IpWildcard.create(toPrefix(ctx.prefix));
    } else if (ctx.wildcard_ip != null) {
      assert ctx.wildcard_mask != null;
      // Note: mask in config is bits that matter, so invert for Batfish IpWildcard mask.
      return IpWildcard.ipWithWildcardMask(
          toIp(ctx.wildcard_ip), toIp(ctx.wildcard_mask).inverted());
    } else {
      assert ctx.ANY() != null;
      return IpWildcard.ANY;
    }
  }

  @Override
  public void exitAclstd_action_line(Aclstd_action_lineContext ctx) {
    LineAction action = toLineAction(ctx.action);
    IpWildcard srcAddress = toIpWildcard(ctx.source);
    String name = getFullText(ctx.getParent()).trim();
    StandardAccessListLine line =
        new StandardAccessListActionLine(_currentAclSeq, action, name, srcAddress);
    _currentStandardAcl.addLine(line);
  }

  @Override
  public void exitAclstd_remark_line(Aclstd_remark_lineContext ctx) {
    String remark = ctx.text.getText().trim();
    StandardAccessListLine line = new StandardAccessListRemarkLine(_currentAclSeq, remark);
    _currentStandardAcl.addLine(line);
  }

  @Override
  public void enterCip_profile(Cip_profileContext ctx) {
    if (_currentIpsecProfile != null) {
      throw new BatfishException("IpsecProfile should be null!");
    }
    _currentIpsecProfile = new IpsecProfile(ctx.name.getText());
    _configuration.defineStructure(IPSEC_PROFILE, ctx.name.getText(), ctx);
  }

  @Override
  public void enterCip_transform_set(Cip_transform_setContext ctx) {
    if (_currentIpsecTransformSet != null) {
      throw new BatfishException("IpsecTransformSet should be null!");
    }
    _currentIpsecTransformSet = new IpsecTransformSet(ctx.name.getText());
    _configuration.defineStructure(IPSEC_TRANSFORM_SET, ctx.name.getText(), ctx);
    if (ctx.ipsec_encryption() != null) {
      _currentIpsecTransformSet.setEncryptionAlgorithm(
          toEncryptionAlgorithm(ctx.ipsec_encryption()));
    } else if (ctx.ipsec_encryption_aruba() != null) {
      _currentIpsecTransformSet.setEncryptionAlgorithm(
          toEncryptionAlgorithm(ctx.ipsec_encryption_aruba()));
    }
    // If any encryption algorithm was set then ESP protocol is used
    if (_currentIpsecTransformSet.getEncryptionAlgorithm() != null) {
      _currentIpsecTransformSet.getProtocols().add(IpsecProtocol.ESP);
    }

    if (ctx.ipsec_authentication() != null) {
      _currentIpsecTransformSet.setAuthenticationAlgorithm(
          toIpsecAuthenticationAlgorithm(ctx.ipsec_authentication()));
      _currentIpsecTransformSet.getProtocols().add(toProtocol(ctx.ipsec_authentication()));
    }
  }

  @Override
  public void enterCkp_named_key(Ckp_named_keyContext ctx) {
    String keyName = ctx.name.getText();
    _currentNamedRsaPubKey =
        _configuration.getCryptoNamedRsaPubKeys().computeIfAbsent(keyName, NamedRsaPubKey::new);
    _configuration.defineStructure(NAMED_RSA_PUB_KEY, keyName, ctx);
    /* RSA pub keys are dynamically matched and not explicitly referenced, so adding a
    self-reference here */
    _configuration.referenceStructure(
        NAMED_RSA_PUB_KEY, keyName, NAMED_RSA_PUB_KEY_SELF_REF, ctx.name.start.getLine());
  }

  @Override
  public void enterCkpn_address(Ckpn_addressContext ctx) {
    if (ctx.NO() != null) {
      return;
    }
    _currentNamedRsaPubKey.setAddress(toIp(ctx.ip_address));
  }

  @Override
  public void enterCkpn_key_string(Ckpn_key_stringContext ctx) {
    if (ctx.NO() != null) {
      return;
    }
    _currentNamedRsaPubKey.setKey(
        CommonUtil.sha256Digest(ctx.certificate().getText() + CommonUtil.salt()));
  }

  @Override
  public void exitCkp_named_key(Ckp_named_keyContext ctx) {
    _currentNamedRsaPubKey = null;
  }

  private IpsecProtocol toProtocol(Ipsec_authenticationContext ctx) {
    if (ctx.ESP_MD5_HMAC() != null
        || ctx.ESP_SHA256_HMAC() != null
        || ctx.ESP_SHA512_HMAC() != null
        || ctx.ESP_SHA_HMAC() != null) {
      return IpsecProtocol.ESP;
    } else if (ctx.AH_SHA_HMAC() != null || ctx.AH_MD5_HMAC() != null) {
      return IpsecProtocol.AH;
    } else {
      throw convError(IpsecProtocol.class, ctx);
    }
  }

  @Override
  public void exitCipprf_set_isakmp_profile(Cipprf_set_isakmp_profileContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.getStart().getLine();
    _currentIpsecProfile.setIsakmpProfile(name);
    _configuration.referenceStructure(ISAKMP_PROFILE, name, IPSEC_PROFILE_ISAKMP_PROFILE, line);
  }

  @Override
  public void exitCipprf_set_pfs(Cipprf_set_pfsContext ctx) {
    _currentIpsecProfile.setPfsGroup(toDhGroup(ctx.dh_group()));
  }

  @Override
  public void exitCipprf_set_transform_set(Cipprf_set_transform_setContext ctx) {
    for (VariableContext transform : ctx.transforms) {
      int line = transform.getStart().getLine();
      String name = transform.getText();
      _currentIpsecProfile.getTransformSets().add(name);
      _configuration.referenceStructure(
          IPSEC_TRANSFORM_SET, name, IPSEC_PROFILE_TRANSFORM_SET, line);
    }
  }

  @Override
  public void exitCipt_mode(Cipt_modeContext ctx) {
    if (ctx.TRANSPORT() != null) {
      _currentIpsecTransformSet.setIpsecEncapsulationMode(IpsecEncapsulationMode.TRANSPORT);
    } else if (ctx.TUNNEL() != null) {
      _currentIpsecTransformSet.setIpsecEncapsulationMode(IpsecEncapsulationMode.TUNNEL);
    } else {
      throw new BatfishException("Unsupported mode " + ctx.getText());
    }
  }

  @Override
  public void enterCis_key(Cis_keyContext ctx) {
    int encType = ctx.dec() != null ? toInteger(ctx.dec()) : 0;
    IkeKeyType ikeKeyType;
    if (encType == 0) {
      ikeKeyType = IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED;
    } else if (encType == 6) {
      ikeKeyType = IkeKeyType.PRE_SHARED_KEY_ENCRYPTED;
    } else {
      _w.addWarning(
          ctx,
          getFullText(ctx),
          _parser,
          String.format("%s is not a valid encryption type", encType));
      return;
    }
    String psk = ctx.key.getText();
    Ip wildCardMask = ctx.wildcard_mask == null ? Ip.MAX : toIp(ctx.wildcard_mask);
    _configuration
        .getIsakmpKeys()
        .add(
            new IsakmpKey(
                IpWildcard.ipWithWildcardMask(toIp(ctx.ip_address), wildCardMask.inverted())
                    .toIpSpace(),
                ikeKeyType == IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED
                    ? CommonUtil.sha256Digest(psk + CommonUtil.salt())
                    : psk,
                ikeKeyType));
  }

  @Override
  public void enterCis_policy(Cis_policyContext ctx) {
    Integer priority = toInteger(ctx.priority);
    _currentIsakmpPolicy = new IsakmpPolicy(priority);
    _currentIsakmpPolicy.setHashAlgorithm(IkeHashingAlgorithm.SHA1);
    _currentIsakmpPolicy.setEncryptionAlgorithm(EncryptionAlgorithm.THREEDES_CBC);
    _currentIsakmpPolicy.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    _currentIsakmpPolicy.setDiffieHellmanGroup(DiffieHellmanGroup.GROUP2);

    _currentIsakmpPolicy.setLifetimeSeconds(86400);
    _configuration.defineStructure(ISAKMP_POLICY, priority.toString(), ctx);
    /* Isakmp policies are checked in order not explicitly referenced, so add a self-reference
    here */
    _configuration.referenceStructure(
        ISAKMP_POLICY,
        priority.toString(),
        ISAKMP_POLICY_SELF_REF,
        ctx.priority.getStart().getLine());
  }

  @Override
  public void enterCis_profile(Cis_profileContext ctx) {
    _currentIsakmpProfile = new IsakmpProfile(ctx.name.getText());
    _configuration.defineStructure(ISAKMP_PROFILE, ctx.name.getText(), ctx);
    /* Isakmp profiles are checked against for matches not explicitly referenced, so add a
    self-reference here */
    _configuration.referenceStructure(
        ISAKMP_PROFILE, ctx.name.getText(), ISAKMP_PROFILE_SELF_REF, ctx.name.start.getLine());
  }

  @Override
  public void exitCispol_authentication(Cispol_authenticationContext ctx) {
    if (ctx.PRE_SHARE() != null) {
      _currentIsakmpPolicy.setAuthenticationMethod(IkeAuthenticationMethod.PRE_SHARED_KEYS);
    } else if (ctx.RSA_ENCR() != null) {
      _currentIsakmpPolicy.setAuthenticationMethod(IkeAuthenticationMethod.RSA_ENCRYPTED_NONCES);
    } else if (ctx.RSA_SIG() != null) {
      _currentIsakmpPolicy.setAuthenticationMethod(IkeAuthenticationMethod.RSA_SIGNATURES);
    } else {
      throw new BatfishException("Unsupported authentication method in " + ctx.getText());
    }
  }

  @Override
  public void exitCispol_encryption(Cispol_encryptionContext ctx) {
    _currentIsakmpPolicy.setEncryptionAlgorithm(toEncryptionAlgorithm(ctx.ike_encryption()));
  }

  @Override
  public void exitCispol_encryption_aruba(Cispol_encryption_arubaContext ctx) {
    _currentIsakmpPolicy.setEncryptionAlgorithm(toEncryptionAlgorithm(ctx.ike_encryption_aruba()));
  }

  @Override
  public void exitCispol_group(Cispol_groupContext ctx) {
    int group = toInteger(ctx.dec());
    _currentIsakmpPolicy.setDiffieHellmanGroup(DiffieHellmanGroup.fromGroupNumber(group));
  }

  @Override
  public void exitCispol_hash(Cispol_hashContext ctx) {
    if (ctx.MD5() != null) {
      _currentIsakmpPolicy.setHashAlgorithm(IkeHashingAlgorithm.MD5);
    } else if (ctx.SHA() != null) {
      _currentIsakmpPolicy.setHashAlgorithm(IkeHashingAlgorithm.SHA1);
    } else if (ctx.SHA2_256_128() != null) {
      _currentIsakmpPolicy.setHashAlgorithm(IkeHashingAlgorithm.SHA_256);
    } else {
      throw new BatfishException("Unsupported authentication method in " + ctx.getText());
    }
  }

  @Override
  public void exitCispol_lifetime(Cispol_lifetimeContext ctx) {
    _currentIsakmpPolicy.setLifetimeSeconds(toInteger(ctx.dec()));
  }

  @Override
  public void exitCisprf_keyring(Cisprf_keyringContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.getStart().getLine();
    _currentIsakmpProfile.setKeyring(name);
    _configuration.referenceStructure(KEYRING, name, ISAKMP_PROFILE_KEYRING, line);
  }

  @Override
  public void exitCisprf_match(Cisprf_matchContext ctx) {
    Ip mask = (ctx.mask == null) ? Ip.parse("255.255.255.255") : toIp(ctx.mask);
    _currentIsakmpProfile.setMatchIdentity(
        IpWildcard.ipWithWildcardMask(toIp(ctx.address), mask.inverted()));
  }

  @Override
  public void exitCisprf_self_identity(Cisprf_self_identityContext ctx) {
    _currentIsakmpProfile.setSelfIdentity(toIp(ctx.IP_ADDRESS()));
  }

  @Override
  public void exitCisprf_local_address(Cisprf_local_addressContext ctx) {
    if (ctx.IP_ADDRESS() != null) {
      _currentIsakmpProfile.setLocalAddress(toIp(ctx.IP_ADDRESS()));
    } else {
      _currentIsakmpProfile.setLocalInterfaceName(ctx.iname.getText());
    }
  }

  @Override
  public void exitCkr_local_address(Ckr_local_addressContext ctx) {
    if (ctx.IP_ADDRESS() != null) {
      _currentKeyring.setLocalAddress(toIp(ctx.IP_ADDRESS()));
    } else {
      _currentKeyring.setLocalInterfaceName(ctx.iname.getText());
    }
  }

  @Override
  public void exitCkr_psk(Ckr_pskContext ctx) {
    Ip wildCardMask = ctx.wildcard_mask == null ? Ip.MAX : toIp(ctx.wildcard_mask);
    _currentKeyring.setKey(
        CommonUtil.sha256Digest(ctx.variable_permissive().getText() + CommonUtil.salt()));
    _currentKeyring.setRemoteIdentity(
        IpWildcard.ipWithWildcardMask(toIp(ctx.ip_address), wildCardMask.inverted()));
  }

  @Override
  public void enterCrypto_keyring(Crypto_keyringContext ctx) {
    if (_currentKeyring != null) {
      throw new BatfishException("Keyring should be null!");
    }
    _currentKeyring = new Keyring(ctx.name.getText());
    _configuration.defineStructure(KEYRING, ctx.name.getText(), ctx);
  }

  @Override
  public void enterCrypto_map(Crypto_mapContext ctx) {
    _currentCryptoMapName = ctx.name.getText();
    if (ctx.seq_num != null) {
      _currentCryptoMapSequenceNum = toInteger(ctx.seq_num);
    }
  }

  @Override
  public void enterCrypto_dynamic_map(Crypto_dynamic_mapContext ctx) {
    String name = ctx.name.getText();

    _currentCryptoMapEntry = new CryptoMapEntry(name, toInteger(ctx.seq_num));
    _currentCryptoMapEntry.setDynamic(true);

    CryptoMapSet cryptoMapSet = _configuration.getCryptoMapSets().get(name);
    // if this is the first crypto map entry in the crypto map set
    if (cryptoMapSet == null) {
      cryptoMapSet = new CryptoMapSet();
      cryptoMapSet.setDynamic(true);
      _configuration.getCryptoMapSets().put(name, cryptoMapSet);
      _configuration.defineStructure(CRYPTO_DYNAMIC_MAP_SET, name, ctx);
    } else if (!cryptoMapSet.getDynamic()) {
      warn(
          ctx,
          String.format("Cannot add dynamic crypto map entry %s to a static crypto map set", name));
      return;
    }
    cryptoMapSet.getCryptoMapEntries().add(_currentCryptoMapEntry);
  }

  @Override
  public void enterCrypto_map_t_ipsec_isakmp(Crypto_map_t_ipsec_isakmpContext ctx) {
    _currentCryptoMapEntry =
        new CryptoMapEntry(_currentCryptoMapName, _currentCryptoMapSequenceNum);

    CryptoMapSet cryptoMapSet = _configuration.getCryptoMapSets().get(_currentCryptoMapName);
    // if this is the first crypto map entry in the crypto map set
    if (cryptoMapSet == null) {
      cryptoMapSet = new CryptoMapSet();
      _configuration.getCryptoMapSets().put(_currentCryptoMapName, cryptoMapSet);
      _configuration.defineStructure(CRYPTO_MAP_SET, _currentCryptoMapName, ctx);
    } else if (cryptoMapSet.getDynamic()) {
      warn(
          ctx,
          String.format(
              "Cannot add static crypto map entry %s to a dynamic crypto map set",
              _currentCryptoMapName));
      return;
    }

    if (ctx.crypto_dynamic_map_name != null) {
      String name = ctx.crypto_dynamic_map_name.getText();
      _currentCryptoMapEntry.setReferredDynamicMapSet(name);
      _configuration.referenceStructure(
          CRYPTO_DYNAMIC_MAP_SET,
          name,
          CRYPTO_MAP_IPSEC_ISAKMP_CRYPTO_DYNAMIC_MAP_SET,
          ctx.getStart().getLine());
    }

    cryptoMapSet.getCryptoMapEntries().add(_currentCryptoMapEntry);
  }

  @Override
  public void enterEos_rb_aa_v4(Eos_rb_aa_v4Context ctx) {
    Prefix prefix =
        ctx.prefix != null
            ? Prefix.parse(ctx.prefix.getText())
            : Prefix.create(toIp(ctx.ip), toIp(ctx.mask));
    _currentAristaBgpAggregateNetwork = new AristaBgpAggregateNetwork();
    _currentAristaBgpVrf.getV4aggregates().put(prefix, _currentAristaBgpAggregateNetwork);
  }

  @Override
  public void exitEos_rb_aa_v4(Eos_rb_aa_v4Context ctx) {
    _currentAristaBgpAggregateNetwork = null;
  }

  @Override
  public void enterEos_rb_aa_v6(Eos_rb_aa_v6Context ctx) {
    Prefix6 prefix = Prefix6.parse(ctx.prefix.getText());
    _currentAristaBgpAggregateNetwork = new AristaBgpAggregateNetwork();
    _currentAristaBgpVrf.getV6aggregates().put(prefix, _currentAristaBgpAggregateNetwork);
  }

  @Override
  public void exitEos_rb_aa_v6(Eos_rb_aa_v6Context ctx) {
    _currentAristaBgpAggregateNetwork = null;
  }

  @Override
  public void exitEos_rb_aa_modifiers(Eos_rb_aa_modifiersContext ctx) {
    if (ctx.ADVERTISE_ONLY() != null) {
      _currentAristaBgpAggregateNetwork.setAdvertiseOnly(true);
    }
    if (ctx.AS_SET() != null) {
      _currentAristaBgpAggregateNetwork.setAsSet(true);
    }
    if (ctx.ATTRIBUTE_MAP() != null) {
      String routeMapName = ctx.attr_map.getText();
      _currentAristaBgpAggregateNetwork.setAttributeMap(routeMapName);
      _configuration.referenceStructure(
          ROUTE_MAP, routeMapName, BGP_AGGREGATE_ATTRIBUTE_MAP, ctx.getStart().getLine());
    }
    if (ctx.MATCH_MAP() != null) {
      String routeMapName = ctx.match_map.getText();
      _currentAristaBgpAggregateNetwork.setMatchMap(routeMapName);
      _configuration.referenceStructure(
          ROUTE_MAP, routeMapName, BGP_AGGREGATE_MATCH_MAP, ctx.getStart().getLine());
    }
    if (ctx.SUMMARY_ONLY() != null) {
      _currentAristaBgpAggregateNetwork.setSummaryOnly(true);
    }
  }

  @Override
  public void exitEos_rbafbc_additional_paths(Eos_rbafbc_additional_pathsContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEos_rbafbc_next_hop_unchanged(Eos_rbafbc_next_hop_unchangedContext ctx) {
    _currentAristaBgpVrfAf.setNextHopUnchanged(true);
  }

  @Override
  public void exitEos_rbafdnc_activate(Eos_rbafdnc_activateContext ctx) {
    _currentAristaBgpNeighborAddressFamily.setActivate(null);
  }

  @Override
  public void enterEos_rb_af_evpn(Eos_rb_af_evpnContext ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateEvpnAf();
  }

  @Override
  public void exitEos_rb_af_evpn(Eos_rb_af_evpnContext ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_afed_neighbor(Eos_rb_afed_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rb_afed_neighbor(Eos_rb_afed_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rb_af_evpn_neighbor_nid(Eos_rb_af_evpn_neighbor_nidContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rb_af_evpn_neighbor(Eos_rb_af_evpn_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rb_af_evpn_no_neighbor(Eos_rb_af_evpn_no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rb_af_evpn_no_neighbor(Eos_rb_af_evpn_no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rb_af_flow_spec_ipv4(Eos_rb_af_flow_spec_ipv4Context ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateFlowSpecV4Af();
  }

  @Override
  public void exitEos_rb_af_flow_spec_ipv4(Eos_rb_af_flow_spec_ipv4Context ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_flow_spec_ipv6(Eos_rb_af_flow_spec_ipv6Context ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateFlowSpecV6Af();
  }

  @Override
  public void exitEos_rb_af_flow_spec_ipv6(Eos_rb_af_flow_spec_ipv6Context ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_ipv4_unicast(Eos_rb_af_ipv4_unicastContext ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateV4UnicastAf();
  }

  @Override
  public void exitEos_rb_af_ipv4_unicast(Eos_rb_af_ipv4_unicastContext ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_ipv4_labeled_unicast(Eos_rb_af_ipv4_labeled_unicastContext ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateV4LabeledUnicastAf();
  }

  @Override
  public void exitEos_rb_af_ipv4_labeled_unicast(Eos_rb_af_ipv4_labeled_unicastContext ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_ipv4_multicast(Eos_rb_af_ipv4_multicastContext ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateV4MulticastAf();
  }

  @Override
  public void exitEos_rb_af_ipv4_multicast(Eos_rb_af_ipv4_multicastContext ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_ipv4_sr_te(Eos_rb_af_ipv4_sr_teContext ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateV4SrTeAf();
  }

  @Override
  public void exitEos_rb_af_ipv4_sr_te(Eos_rb_af_ipv4_sr_teContext ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_ipv6_labeled_unicast(Eos_rb_af_ipv6_labeled_unicastContext ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateV6LabeledUnicastAf();
  }

  @Override
  public void exitEos_rb_af_ipv6_labeled_unicast(Eos_rb_af_ipv6_labeled_unicastContext ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_ipv6_multicast(Eos_rb_af_ipv6_multicastContext ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateV6MulticastAf();
  }

  @Override
  public void exitEos_rb_af_ipv6_multicast(Eos_rb_af_ipv6_multicastContext ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_ipv6_sr_te(Eos_rb_af_ipv6_sr_teContext ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateV6SrTeAf();
  }

  @Override
  public void exitEos_rb_af_ipv6_sr_te(Eos_rb_af_ipv6_sr_teContext ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_ipv6_unicast(Eos_rb_af_ipv6_unicastContext ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateV6UnicastAf();
  }

  @Override
  public void exitEos_rb_af_ipv6_unicast(Eos_rb_af_ipv6_unicastContext ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_vpn_v4(Eos_rb_af_vpn_v4Context ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateVpnV4Af();
  }

  @Override
  public void exitEos_rb_af_vpn_v4(Eos_rb_af_vpn_v4Context ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rb_af_vpn_v6(Eos_rb_af_vpn_v6Context ctx) {
    _currentAristaBgpVrfAf = _currentAristaBgpVrf.getOrCreateVpnV6Af();
  }

  @Override
  public void exitEos_rb_af_vpn_v6(Eos_rb_af_vpn_v6Context ctx) {
    _currentAristaBgpVrfAf = null;
  }

  @Override
  public void enterEos_rbaffs4_default_neighbor(Eos_rbaffs4_default_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbaffs4_default_neighbor(Eos_rbaffs4_default_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbaffs4_neighbor(Eos_rbaffs4_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbaffs4_neighbor(Eos_rbaffs4_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbaffs4no_neighbor(Eos_rbaffs4no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbaffs4no_neighbor(Eos_rbaffs4no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbaffs6_default_neighbor(Eos_rbaffs6_default_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbaffs6_default_neighbor(Eos_rbaffs6_default_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbaffs6_neighbor(Eos_rbaffs6_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbaffs6_neighbor(Eos_rbaffs6_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbaffs6no_neighbor(Eos_rbaffs6no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbaffs6no_neighbor(Eos_rbaffs6no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void exitEos_rbafipv4ub_next_hop(Eos_rbafipv4ub_next_hopContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEos_rbafipv4ub_redistribute_internal(
      Eos_rbafipv4ub_redistribute_internalContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEos_rbafipv4ub_route(Eos_rbafipv4ub_routeContext ctx) {
    todo(ctx);
  }

  @Override
  public void enterEos_rbafipv4u_neighbor(Eos_rbafipv4u_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4u_neighbor(Eos_rbafipv4u_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  /** Common logic for setting the current BGP neighbor in the current VRF>AF config. */
  private void enterCurrentBgpNeighborAddressFamily(
      ParserRuleContext ctx, Eos_neighbor_idContext nid) {
    if (nid.v4 != null) {
      Ip address = toIp(nid.v4);
      _currentAristaBgpVrf.getOrCreateV4Neighbor(address); // ensure peer exists
      _currentAristaBgpNeighborAddressFamily = _currentAristaBgpVrfAf.getOrCreateNeighbor(address);
    } else if (nid.pg != null) {
      String name = nid.pg.getText();
      _currentAristaBgpProcess.getOrCreatePeerGroup(name); // ensure peer exists
      _currentAristaBgpNeighborAddressFamily = _currentAristaBgpVrfAf.getOrCreatePeerGroup(name);
      _configuration.referenceStructure(
          BGP_PEER_GROUP, name, BGP_NEIGHBOR_PEER_GROUP, ctx.getStart().getLine());
    } else if (nid.v6 != null) {
      _currentAristaBgpNeighborAddressFamily =
          _currentAristaBgpVrfAf.getOrCreateNeighbor(toIp6(nid.v6));
    } else {
      throw new IllegalStateException(
          String.format("Unknown neighbor type in %s", getFullText(ctx)));
    }
  }

  @Override
  public void enterEos_rbafipv4ud_neighbor(Eos_rbafipv4ud_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4ud_neighbor(Eos_rbafipv4ud_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv4labud_neighbor(Eos_rbafipv4labud_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4labud_neighbor(Eos_rbafipv4labud_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv4labu_neighbor(Eos_rbafipv4labu_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4labu_neighbor(Eos_rbafipv4labu_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv4labuno_neighbor(Eos_rbafipv4labuno_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4labuno_neighbor(Eos_rbafipv4labuno_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv4md_neighbor(Eos_rbafipv4md_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4md_neighbor(Eos_rbafipv4md_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv4m_neighbor(Eos_rbafipv4m_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4m_neighbor(Eos_rbafipv4m_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv4m_no_neighbor(Eos_rbafipv4m_no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4m_no_neighbor(Eos_rbafipv4m_no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv4srte_default_neighbor(Eos_rbafipv4srte_default_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4srte_default_neighbor(Eos_rbafipv4srte_default_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv4srte_neighbor(Eos_rbafipv4srte_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4srte_neighbor(Eos_rbafipv4srte_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv4srte_no_neighbor(Eos_rbafipv4srte_no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4srte_no_neighbor(Eos_rbafipv4srte_no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6labud_neighbor(Eos_rbafipv6labud_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6labud_neighbor(Eos_rbafipv6labud_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6labu_neighbor(Eos_rbafipv6labu_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6labu_neighbor(Eos_rbafipv6labu_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6labuno_neighbor(Eos_rbafipv6labuno_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6labuno_neighbor(Eos_rbafipv6labuno_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6md_neighbor(Eos_rbafipv6md_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6md_neighbor(Eos_rbafipv6md_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6m_neighbor(Eos_rbafipv6m_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6m_neighbor(Eos_rbafipv6m_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6m_no_neighbor(Eos_rbafipv6m_no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6m_no_neighbor(Eos_rbafipv6m_no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6srte_default_neighbor(Eos_rbafipv6srte_default_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6srte_default_neighbor(Eos_rbafipv6srte_default_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6srte_neighbor(Eos_rbafipv6srte_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6srte_neighbor(Eos_rbafipv6srte_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6srte_no_neighbor(Eos_rbafipv6srte_no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6srte_no_neighbor(Eos_rbafipv6srte_no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6u_neighbor(Eos_rbafipv6u_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6u_neighbor(Eos_rbafipv6u_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6ud_neighbor(Eos_rbafipv6ud_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6ud_neighbor(Eos_rbafipv6ud_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafvpn4d_neighbor(Eos_rbafvpn4d_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafvpn4d_neighbor(Eos_rbafvpn4d_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafvpn4_neighbor(Eos_rbafvpn4_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafvpn4_neighbor(Eos_rbafvpn4_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafvpn4no_neighbor(Eos_rbafvpn4no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafvpn4no_neighbor(Eos_rbafvpn4no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafvpn6d_neighbor(Eos_rbafvpn6d_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafvpn6d_neighbor(Eos_rbafvpn6d_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafvpn6_neighbor(Eos_rbafvpn6_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafvpn6_neighbor(Eos_rbafvpn6_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafvpn6no_neighbor(Eos_rbafvpn6no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafvpn6no_neighbor(Eos_rbafvpn6no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void exitEos_rbafnc_activate(Eos_rbafnc_activateContext ctx) {
    _currentAristaBgpNeighborAddressFamily.setActivate(true);
  }

  @Override
  public void exitEos_rbafnc_additional_paths(Eos_rbafnc_additional_pathsContext ctx) {
    AristaBgpAdditionalPathsConfig addPaths =
        _currentAristaBgpNeighborAddressFamily.getOrCreateAdditionalPaths();
    if (ctx.RECEIVE() != null) {
      addPaths.setReceive(true);
    }
    if (ctx.SEND() != null) {
      addPaths.setSend(SendType.ANY);
    }
  }

  @Override
  public void exitEos_rbafnc_default_originate(Eos_rbafnc_default_originateContext ctx) {
    boolean always = ctx.ALWAYS() != null;
    @Nullable String routeMap = null;
    if (ctx.rm != null) {
      routeMap = ctx.rm.getText();
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_DEFAULT_ORIGINATE_ROUTE_MAP, ctx.getStart().getLine());
    }
    AristaBgpDefaultOriginate defaultOriginate =
        AristaBgpDefaultOriginate.enabled(always, routeMap);
    _currentAristaBgpNeighborAddressFamily.setDefaultOriginate(defaultOriginate);
  }

  @Override
  public void exitEos_rbafnc_next_hop_unchanged(Eos_rbafnc_next_hop_unchangedContext ctx) {
    _currentAristaBgpNeighborAddressFamily.setNextHopUnchanged(true);
  }

  @Override
  public void exitEos_rbafnc_prefix_list(Eos_rbafnc_prefix_listContext ctx) {
    String name = ctx.name.getText();
    if (ctx.IN() != null) {
      _currentAristaBgpNeighborAddressFamily.setPrefixListIn(name);
      _configuration.referenceStructure(
          PREFIX_LIST, name, BGP_INBOUND_PREFIX_LIST, ctx.getStart().getLine());
    } else {
      assert ctx.OUT() != null;
      _currentAristaBgpNeighborAddressFamily.setPrefixListOut(name);
      _configuration.referenceStructure(
          PREFIX_LIST, name, BGP_OUTBOUND_PREFIX_LIST, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitEos_rbafnc_route_map(Eos_rbafnc_route_mapContext ctx) {
    String name = ctx.name.getText();
    if (ctx.IN() != null) {
      _currentAristaBgpNeighborAddressFamily.setRouteMapIn(name);
      _configuration.referenceStructure(
          ROUTE_MAP, name, BGP_INBOUND_ROUTE_MAP, ctx.getStart().getLine());
    } else {
      assert ctx.OUT() != null;
      _currentAristaBgpNeighborAddressFamily.setRouteMapOut(name);
      _configuration.referenceStructure(
          ROUTE_MAP, name, BGP_OUTBOUND_ROUTE_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void enterEos_rbafipv4u_no_neighbor(Eos_rbafipv4u_no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv4u_no_neighbor(Eos_rbafipv4u_no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbafipv6u_no_neighbor(Eos_rbafipv6u_no_neighborContext ctx) {
    enterCurrentBgpNeighborAddressFamily(ctx, ctx.nid);
  }

  @Override
  public void exitEos_rbafipv6u_no_neighbor(Eos_rbafipv6u_no_neighborContext ctx) {
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void exitEos_rbafnobc_additional_paths(Eos_rbafnobc_additional_pathsContext ctx) {
    AristaBgpAdditionalPathsConfig addPaths = _currentAristaBgpVrfAf.getOrCreateAdditionalPaths();
    if (ctx.INSTALL() != null) {
      addPaths.setInstall(false);
    }
    if (ctx.RECEIVE() != null) {
      addPaths.setReceive(false);
    }
    if (ctx.SEND() != null) {
      addPaths.setSend(SendType.NONE);
    }
  }

  @Override
  public void exitEos_rbafnobc_next_hop_unchanged(Eos_rbafnobc_next_hop_unchangedContext ctx) {
    _currentAristaBgpVrfAf.setNextHopUnchanged(false);
  }

  @Override
  public void exitEos_rbafnonc_activate(Eos_rbafnonc_activateContext ctx) {
    _currentAristaBgpNeighborAddressFamily.setActivate(false);
  }

  @Override
  public void exitEos_rbafnonc_additional_paths(Eos_rbafnonc_additional_pathsContext ctx) {
    AristaBgpAdditionalPathsConfig addPaths =
        _currentAristaBgpNeighborAddressFamily.getOrCreateAdditionalPaths();
    if (ctx.SEND() != null) {
      addPaths.setSend(SendType.NONE);
    }
    if (ctx.RECEIVE() != null) {
      addPaths.setReceive(false);
    }
  }

  @Override
  public void exitEos_rbafnonc_default_originate(Eos_rbafnonc_default_originateContext ctx) {
    _currentAristaBgpNeighborAddressFamily.setDefaultOriginate(
        AristaBgpDefaultOriginate.disabled());
  }

  @Override
  public void exitEos_rbafnonc_next_hop_unchanged(Eos_rbafnonc_next_hop_unchangedContext ctx) {
    _currentAristaBgpNeighborAddressFamily.setNextHopUnchanged(false);
  }

  @Override
  public void exitEos_rbafnonc_route_map(Eos_rbafnonc_route_mapContext ctx) {
    if (ctx.IN() != null) {
      _currentAristaBgpNeighborAddressFamily.setRouteMapIn(null);
    } else {
      assert ctx.OUT() != null;
      _currentAristaBgpNeighborAddressFamily.setRouteMapOut(null);
    }
  }

  @Override
  public void exitEos_rbib_additional_paths(Eos_rbib_additional_pathsContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEos_rbib_advertise_inactive(Eos_rbib_advertise_inactiveContext ctx) {
    _currentAristaBgpVrf.setAdvertiseInactive(true);
  }

  @Override
  public void enterEos_rbib_allowas_in(Eos_rbib_allowas_inContext ctx) {
    _currentAristaBgpVrf.setAllowAsIn(ctx.num == null ? Integer.MAX_VALUE : toInteger(ctx.num));
  }

  @Override
  public void exitEos_rbib_always_compare_med(Eos_rbib_always_compare_medContext ctx) {
    todo(ctx); // No support for this in VI.
    _currentAristaBgpVrf.setAlwaysCompareMed(true);
  }

  @Override
  public void exitEos_rbib_cluster_id(Eos_rbib_cluster_idContext ctx) {
    Ip clusterId = toIp(ctx.ip);
    if (Ip.ZERO.equals(clusterId)) {
      // 0.0.0.0 is the same as no bgp cluster-id
      _currentAristaBgpVrf.setClusterId(null);
    } else {
      _currentAristaBgpVrf.setClusterId(clusterId);
    }
  }

  @Override
  public void exitEos_rbibconf_identifier(Eos_rbibconf_identifierContext ctx) {
    _currentAristaBgpVrf.setConfederationIdentifier(toAsNum(ctx.asn));
  }

  @Override
  public void exitEos_rbibconf_peers(Eos_rbibconf_peersContext ctx) {
    _currentAristaBgpVrf.setConfederationPeers(toAsSpace(ctx.asns));
  }

  @Override
  public void exitEos_rbibd_ipv4u_enabled(Eos_rbibd_ipv4u_enabledContext ctx) {
    _currentAristaBgpVrf.setDefaultIpv4Unicast(true);
  }

  @Override
  public void exitEos_rbib_client_to_client(Eos_rbib_client_to_clientContext ctx) {
    _currentAristaBgpVrf.setClientToClientReflection(true);
  }

  @Override
  public void exitEos_rbib_enforce_first_as(Eos_rbib_enforce_first_asContext ctx) {
    _currentAristaBgpVrf.setEnforceFirstAs(true);
  }

  @Override
  public void exitEos_rbibbpa_multipath_relax(Eos_rbibbpa_multipath_relaxContext ctx) {
    _currentAristaBgpVrf.setBestpathAsPathMultipathRelax(true);
  }

  @Override
  public void exitEos_rbibbp_tie_break(Eos_rbibbp_tie_breakContext ctx) {
    if (ctx.ROUTER_ID() != null) {
      _currentAristaBgpVrf.setBestpathTieBreaker(AristaBgpBestpathTieBreaker.ROUTER_ID);
    } else if (ctx.CLUSTER_LIST_LENGTH() != null) {
      _currentAristaBgpVrf.setBestpathTieBreaker(AristaBgpBestpathTieBreaker.CLUSTER_LIST_LENGTH);
    } else {
      throw new IllegalStateException(
          String.format("Unrecognized 'bgp bestpath tie-break' value: %s", getFullText(ctx)));
    }
  }

  @Override
  public void exitEos_rbibl_limit(Eos_rbibl_limitContext ctx) {
    _currentAristaBgpVrf.setListenLimit(toInteger(ctx.num));
  }

  @Override
  public void exitEos_rbib_missing_policy(Eos_rbib_missing_policyContext ctx) {
    if (ctx.PERMIT() != null) {
      // this is batfish's default
      return;
    }
    todo(ctx);
  }

  @Override
  public void exitEos_rbibl_range(Eos_rbibl_rangeContext ctx) {
    Prefix prefix;
    if (ctx.prefix != null) {
      prefix = toPrefix(ctx.prefix);
    } else if (ctx.ip != null) {
      prefix = Prefix.create(toIp(ctx.ip), toIp(ctx.mask));
    } else {
      // IPv6, currently ignored.
      return;
    }
    AristaBgpV4DynamicNeighbor neighbor = _currentAristaBgpVrf.getOrCreateV4DynamicNeighbor(prefix);
    assert ctx.pg != null; // parser guarantee
    neighbor.setPeerGroup(ctx.pg.getText());
    if (ctx.asn != null) {
      neighbor.setRemoteAs(toAsNum(ctx.asn));
    }
    if (ctx.peer_filter != null) {
      String peerFilterName = ctx.peer_filter.getText();
      neighbor.setPeerFilter(peerFilterName);
      _configuration.referenceStructure(
          PEER_FILTER, peerFilterName, BGP_LISTEN_RANGE_PEER_FILTER, ctx.getStart().getLine());
    }
    String bgpNeighborStructName =
        bgpNeighborStructureName(prefix.toString(), _currentAristaBgpVrf.getName());
    _configuration.defineStructure(BGP_LISTEN_RANGE, bgpNeighborStructName, ctx);
    _configuration.referenceStructure(
        BGP_LISTEN_RANGE, bgpNeighborStructName, BGP_LISTEN_RANGE_SELF_REF, ctx.prefix.getLine());
  }

  @Override
  public void exitEos_rbib_next_hop_unchanged(Eos_rbib_next_hop_unchangedContext ctx) {
    _currentAristaBgpVrf.setNextHopUnchanged(true);
  }

  @Override
  public void exitEos_rbibtrans_listen_port(Eos_rbibtrans_listen_portContext ctx) {
    todo(ctx);
  }

  @Override
  public void enterEos_rbid_neighbor(Eos_rbid_neighborContext ctx) {
    if (ctx.nid.v4 != null) {
      Ip address = toIp(ctx.nid.v4);
      _currentAristaBgpNeighbor =
          _currentAristaBgpVrf.getOrCreateV4Neighbor(address); // ensure peer exists
    } else if (ctx.nid.pg != null) {
      String name = ctx.nid.pg.getText();
      _currentAristaBgpNeighbor =
          _currentAristaBgpProcess.getOrCreatePeerGroup(name); // ensure peer exists
      _configuration.referenceStructure(
          BGP_PEER_GROUP, name, BGP_NEIGHBOR_PEER_GROUP, ctx.getStart().getLine());
    } else if (ctx.nid.v6 != null) {
      // TODO: v6 neighbors aren't tracked and most properties aren't modeled.
      Ip6 ip = toIp6(ctx.nid.v6);
      _currentAristaBgpNeighbor = new AristaBgpV6Neighbor(ip);
    } else {
      throw new IllegalStateException(
          String.format("Unknown neighbor type in %s", getFullText(ctx)));
    }
    _currentAristaBgpNeighborAddressFamily = _currentAristaBgpNeighbor.getGenericAddressFamily();
  }

  @Override
  public void exitEos_rbid_neighbor(Eos_rbid_neighborContext ctx) {
    _currentAristaBgpNeighbor = null;
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void exitEos_rbidn_export_localpref(Eos_rbidn_export_localprefContext ctx) {
    _currentAristaBgpNeighbor.setExportLocalPref(null);
  }

  @Override
  public void exitEos_rbi_default_metric(Eos_rbi_default_metricContext ctx) {
    _currentAristaBgpVrf.setDefaultMetric(toLong(ctx.metric));
  }

  @Override
  public void exitEos_rbi_distance(Eos_rbi_distanceContext ctx) {
    _currentAristaBgpVrf.setEbgpAdminDistance(toInteger(ctx.external));
    if (ctx.internal != null) {
      _currentAristaBgpVrf.setIbgpAdminDistance(toInteger(ctx.internal));
    }
    if (ctx.local != null) {
      _currentAristaBgpVrf.setLocalAdminDistance(toInteger(ctx.local));
    }
  }

  @Override
  public void exitEos_rbi_maximum_paths(Eos_rbi_maximum_pathsContext ctx) {
    _currentAristaBgpVrf.setMaxPaths(toInteger(ctx.num));
    if (ctx.ecmp != null) {
      _currentAristaBgpVrf.setMaxPathsEcmp(toInteger(ctx.ecmp));
    }
  }

  @Override
  public void enterEos_rbi_neighbor4(Eos_rbi_neighbor4Context ctx) {
    Ip ip = toIp(ctx.name);
    _currentAristaBgpNeighbor =
        _currentAristaBgpVrf.getV4neighbors().computeIfAbsent(ip, AristaBgpV4Neighbor::new);
    _currentAristaBgpNeighborAddressFamily = _currentAristaBgpNeighbor.getGenericAddressFamily();
    String bgpNeighborStructName =
        bgpNeighborStructureName(ip.toString(), _currentAristaBgpVrf.getName());
    _configuration.defineStructure(BGP_NEIGHBOR, bgpNeighborStructName, ctx);
    _configuration.referenceStructure(
        BGP_NEIGHBOR, bgpNeighborStructName, BGP_NEIGHBOR_SELF_REF, ctx.name.getLine());
  }

  @Override
  public void exitEos_rbi_neighbor4(Eos_rbi_neighbor4Context ctx) {
    _currentAristaBgpNeighbor = null;
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void enterEos_rbi_neighbor6(Eos_rbi_neighbor6Context ctx) {
    // Create a dummy v4 neighbor instead
    _currentAristaBgpNeighbor = new AristaBgpV4Neighbor(Ip.ZERO);
    _currentAristaBgpNeighborAddressFamily = _currentAristaBgpNeighbor.getGenericAddressFamily();
  }

  @Override
  public void exitEos_rbi_neighbor6(Eos_rbi_neighbor6Context ctx) {
    _currentAristaBgpNeighbor = null;
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void exitEos_rbinc_additional_paths(Eos_rbinc_additional_pathsContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEos_rbinc_allowas_in(Eos_rbinc_allowas_inContext ctx) {
    int num = ctx.num != null ? toInteger(ctx.num) : Integer.MAX_VALUE;
    _currentAristaBgpNeighbor.setAllowAsIn(num);
  }

  @Override
  public void exitEos_rbinc_default_originate(Eos_rbinc_default_originateContext ctx) {
    boolean always = ctx.ALWAYS() != null;
    @Nullable String routeMap = null;
    if (ctx.rm != null) {
      routeMap = ctx.rm.getText();
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_DEFAULT_ORIGINATE_ROUTE_MAP, ctx.getStart().getLine());
    }
    AristaBgpDefaultOriginate defaultOriginate =
        AristaBgpDefaultOriginate.enabled(always, routeMap);
    _currentAristaBgpNeighbor.setDefaultOriginate(defaultOriginate);
  }

  @Override
  public void exitEos_rbinc_description(Eos_rbinc_descriptionContext ctx) {
    _currentAristaBgpNeighbor.setDescription(ctx.desc.getText().trim());
  }

  @Override
  public void exitEos_rbinc_dont_capability_negotiate(
      Eos_rbinc_dont_capability_negotiateContext ctx) {
    _currentAristaBgpNeighbor.setDontCapabilityNegotiate(true);
  }

  @Override
  public void exitEos_rbinc_ebgp_multihop(Eos_rbinc_ebgp_multihopContext ctx) {
    int num = ctx.num != null ? toInteger(ctx.num) : Integer.MAX_VALUE;
    _currentAristaBgpNeighbor.setEbgpMultihop(num);
  }

  @Override
  public void exitEos_rbinc_enforce_first_as(Eos_rbinc_enforce_first_asContext ctx) {
    _currentAristaBgpNeighbor.setEnforceFirstAs(true);
  }

  @Override
  public void exitEos_rbinc_export_localpref(Eos_rbinc_export_localprefContext ctx) {
    long localpref = toLong(ctx.value);
    _currentAristaBgpNeighbor.setExportLocalPref(localpref);
  }

  @Override
  public void exitEos_rbinc_local_as(Eos_rbinc_local_asContext ctx) {
    _currentAristaBgpNeighbor.setLocalAs(toAsNum(ctx.asn));
  }

  @Override
  public void exitEos_rbinc_maximum_accepted_routes(Eos_rbinc_maximum_accepted_routesContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEos_rbinc_maximum_routes(Eos_rbinc_maximum_routesContext ctx) {
    if (!ctx.WARNING_ONLY().isEmpty()) {
      // This is not a limit, Batfish's behavior is correct.
      return;
    }
    int limit = toInteger(ctx.num);
    if (limit > 0) {
      warn(ctx, "Batfish does not limit the number of routes received over BGP.");
    }
  }

  @Override
  public void exitEos_rbinc_next_hop_peer(Eos_rbinc_next_hop_peerContext ctx) {
    _currentAristaBgpNeighbor.setNextHopPeer(true);
    todo(ctx);
  }

  @Override
  public void enterEos_rbinc_next_hop_self(Eos_rbinc_next_hop_selfContext ctx) {
    _currentAristaBgpNeighbor.setNextHopSelf(true);
  }

  @Override
  public void exitEos_rbinc_next_hop_unchanged(Eos_rbinc_next_hop_unchangedContext ctx) {
    _currentAristaBgpNeighbor.setNextHopUnchanged(true);
  }

  @Override
  public void exitEos_rbinc_passive(Eos_rbinc_passiveContext ctx) {
    _currentAristaBgpNeighbor.setPassive(true);
    todo(ctx);
  }

  @Override
  public void exitEos_rbinc_remote_as(Eos_rbinc_remote_asContext ctx) {
    _currentAristaBgpNeighbor.setRemoteAs(toAsNum(ctx.asn));
  }

  @Override
  public void exitEos_rbinc_remove_private_as(Eos_rbinc_remove_private_asContext ctx) {
    if (ctx.REPLACE_AS() != null) {
      _currentAristaBgpNeighbor.setRemovePrivateAsMode(
          AristaBgpNeighbor.RemovePrivateAsMode.REPLACE_AS);
    } else if (ctx.ALL() != null) {
      _currentAristaBgpNeighbor.setRemovePrivateAsMode(AristaBgpNeighbor.RemovePrivateAsMode.ALL);
    } else {
      _currentAristaBgpNeighbor.setRemovePrivateAsMode(AristaBgpNeighbor.RemovePrivateAsMode.BASIC);
    }
  }

  @Override
  public void exitEos_rbinc_route_reflector_client(Eos_rbinc_route_reflector_clientContext ctx) {
    _currentAristaBgpNeighbor.setRouteReflectorClient(true);
  }

  @Override
  public void exitEos_rbinc_send_community(Eos_rbinc_send_communityContext ctx) {
    if (ctx.ADD() != null) {
      if (ctx.comm.STANDARD() != null) {
        _currentAristaBgpNeighbor.setSendCommunity(true);
      } else if (ctx.comm.EXTENDED() != null) {
        _currentAristaBgpNeighbor.setSendExtendedCommunity(true);
      }
    } else if (ctx.REMOVE() != null) {
      if (ctx.comm.STANDARD() != null) {
        _currentAristaBgpNeighbor.setSendCommunity(false);
      } else if (ctx.comm.EXTENDED() != null) {
        _currentAristaBgpNeighbor.setSendExtendedCommunity(false);
      }
    } else if (!ctx.communities.isEmpty()) {
      for (Eos_bgp_communityContext community : ctx.communities) {
        if (community.STANDARD() != null) {
          _currentAristaBgpNeighbor.setSendCommunity(true);
        } else if (community.EXTENDED() != null) {
          _currentAristaBgpNeighbor.setSendExtendedCommunity(true);
        }
      }
    } else {
      _currentAristaBgpNeighbor.setSendCommunity(true);
      _currentAristaBgpNeighbor.setSendExtendedCommunity(true);
    }
  }

  @Override
  public void exitEos_rbinc_shutdown(Eos_rbinc_shutdownContext ctx) {
    _currentAristaBgpNeighbor.setShutdown(true);
  }

  @Override
  public void exitEos_rbinc_update_source(Eos_rbinc_update_sourceContext ctx) {
    String iface = toInterfaceName(ctx.iface);
    _currentAristaBgpNeighbor.setUpdateSource(iface);
    _configuration.referenceStructure(
        INTERFACE, iface, BGP_UPDATE_SOURCE_INTERFACE, ctx.iface.getStart().getLine());
  }

  @Override
  public void exitEos_rbi_network4(Eos_rbi_network4Context ctx) {
    AristaBgpVrfIpv4UnicastAddressFamily af = _currentAristaBgpVrf.getOrCreateV4UnicastAf();
    Prefix network =
        ctx.prefix != null
            ? Prefix.parse(ctx.prefix.getText())
            : Prefix.create(toIp(ctx.ip), toIp(ctx.mask));
    AristaBgpNetworkConfiguration conf = af.getOrCreateNetwork(network);
    String routeMapName = ctx.rm == null ? null : ctx.rm.getText();
    if (routeMapName != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, routeMapName, BGP_NETWORK_ORIGINATION_ROUTE_MAP, ctx.getStart().getLine());
      conf.setRouteMap(routeMapName);
    }
  }

  @Override
  public void exitEos_rbi_network6(Eos_rbi_network6Context ctx) {
    if (ctx.rm != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, ctx.rm.getText(), BGP_NETWORK_ORIGINATION_ROUTE_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitEos_rbino_bgp_advertise_inactive(Eos_rbino_bgp_advertise_inactiveContext ctx) {
    _currentAristaBgpVrf.setAdvertiseInactive(false);
  }

  @Override
  public void exitEos_rbino_bgp_allowas_in(Eos_rbino_bgp_allowas_inContext ctx) {
    _currentAristaBgpVrf.setAllowAsIn(0);
  }

  @Override
  public void exitEos_rbino_bgp_always_compare_med(Eos_rbino_bgp_always_compare_medContext ctx) {
    _currentAristaBgpVrf.setAlwaysCompareMed(false);
  }

  @Override
  public void exitEos_rbino_bgp_bpa_multipath_relax(Eos_rbino_bgp_bpa_multipath_relaxContext ctx) {
    _currentAristaBgpVrf.setBestpathAsPathMultipathRelax(false);
  }

  @Override
  public void exitEos_rbino_bgp_bpt_cluster_list_length(
      Eos_rbino_bgp_bpt_cluster_list_lengthContext ctx) {
    if (_currentAristaBgpVrf.getBestpathTieBreaker()
        == AristaBgpBestpathTieBreaker.CLUSTER_LIST_LENGTH) {
      _currentAristaBgpVrf.setBestpathTieBreaker(null);
    }
  }

  @Override
  public void exitEos_rbino_bgp_bpt_router_id(Eos_rbino_bgp_bpt_router_idContext ctx) {
    if (_currentAristaBgpVrf.getBestpathTieBreaker() == AristaBgpBestpathTieBreaker.ROUTER_ID) {
      _currentAristaBgpVrf.setBestpathTieBreaker(null);
    }
  }

  @Override
  public void exitEos_rbino_bgp_client_to_client(Eos_rbino_bgp_client_to_clientContext ctx) {
    _currentAristaBgpVrf.setClientToClientReflection(false);
  }

  @Override
  public void exitEos_rbino_bgp_cluster_id(Eos_rbino_bgp_cluster_idContext ctx) {
    _currentAristaBgpVrf.setClusterId(null);
  }

  @Override
  public void exitEos_rbino_bc_identifier(Eos_rbino_bc_identifierContext ctx) {
    _currentAristaBgpVrf.setConfederationIdentifier(null);
  }

  @Override
  public void exitEos_rbino_bgp_default_ipv4u_enabled(
      Eos_rbino_bgp_default_ipv4u_enabledContext ctx) {
    _currentAristaBgpVrf.setDefaultIpv4Unicast(false);
  }

  @Override
  public void exitEos_rbino_bgp_next_hop_unchanged(Eos_rbino_bgp_next_hop_unchangedContext ctx) {
    _currentAristaBgpVrf.setNextHopUnchanged(false);
  }

  @Override
  public void exitEos_rbino_default_metric(Eos_rbino_default_metricContext ctx) {
    _currentAristaBgpVrf.setDefaultMetric(null);
  }

  @Override
  public void enterEos_rbino_neighbor(Eos_rbino_neighborContext ctx) {
    if (ctx.nid.v4 != null) {
      Ip address = toIp(ctx.nid.v4);
      _currentAristaBgpNeighborIsBrandNew =
          !_currentAristaBgpVrf.getV4neighbors().containsKey(address);
      _currentAristaBgpNeighbor =
          _currentAristaBgpVrf.getOrCreateV4Neighbor(address); // ensure peer exists
    } else if (ctx.nid.pg != null) {
      String name = ctx.nid.pg.getText();
      _currentAristaBgpNeighborIsBrandNew =
          !_currentAristaBgpProcess.getPeerGroups().containsKey(name);
      _currentAristaBgpNeighbor =
          _currentAristaBgpProcess.getOrCreatePeerGroup(name); // ensure peer exists
      _configuration.referenceStructure(
          BGP_PEER_GROUP, name, BGP_NEIGHBOR_PEER_GROUP, ctx.getStart().getLine());
    } else if (ctx.nid.v6 != null) {
      // TODO: v6 neighbors
      _currentAristaBgpNeighbor = new AristaBgpPeerGroupNeighbor("dummy");
    } else {
      throw new IllegalStateException(
          String.format("Unknown neighbor type in %s", getFullText(ctx)));
    }
    _currentAristaBgpNeighborAddressFamily = _currentAristaBgpNeighbor.getGenericAddressFamily();
  }

  @Override
  public void exitEos_rbino_neighbor(Eos_rbino_neighborContext ctx) {
    _currentAristaBgpNeighbor = null;
    _currentAristaBgpNeighborAddressFamily = null;
    _currentAristaBgpNeighborIsBrandNew = false;
  }

  @Override
  public void exitEos_rbino_neighbor_neighbor(Eos_rbino_neighbor_neighborContext ctx) {
    if (_currentAristaBgpNeighbor instanceof AristaBgpPeerGroupNeighbor) {
      String name = ((AristaBgpPeerGroupNeighbor) _currentAristaBgpNeighbor).getName();
      warn(
          ctx,
          String.format("Incomplete: need peer group suffix to delete BGP peer group %s", name));
      if (_currentAristaBgpNeighborIsBrandNew) {
        // Still need to delete it so it doesn't end up created.
        _currentAristaBgpProcess.deletePeerGroup(name);
      }
    } else if (_currentAristaBgpNeighbor instanceof AristaBgpV4Neighbor) {
      Ip ip = ((AristaBgpV4Neighbor) _currentAristaBgpNeighbor).getIp();
      _currentAristaBgpVrf.getV4neighbors().remove(ip);
      if (_currentAristaBgpNeighborIsBrandNew) {
        warn(
            ctx,
            String.format(
                "No BGP neighbor %s to delete in vrf %s", ip, _currentAristaBgpVrf.getName()));
      }
    } else {
      assert _currentAristaBgpNeighbor instanceof AristaBgpV6Neighbor;
      // TODO v6 neighbors
    }
  }

  @Override
  public void exitEos_rbinon_allowas_in(Eos_rbinon_allowas_inContext ctx) {
    _currentAristaBgpNeighbor.setAllowAsIn(0);
  }

  @Override
  public void exitEos_rbinon_default_originate(Eos_rbinon_default_originateContext ctx) {
    _currentAristaBgpNeighbor.setDefaultOriginate(AristaBgpDefaultOriginate.disabled());
  }

  @Override
  public void exitEos_rbinon_description(Eos_rbinon_descriptionContext ctx) {
    _currentAristaBgpNeighbor.setDescription(null);
  }

  @Override
  public void exitEos_rbinon_ebgp_multihop(Eos_rbinon_ebgp_multihopContext ctx) {
    _currentAristaBgpNeighbor.setEbgpMultihop(0);
  }

  @Override
  public void exitEos_rbinon_enforce_first_as(Eos_rbinon_enforce_first_asContext ctx) {
    _currentAristaBgpNeighbor.setEnforceFirstAs(false);
  }

  @Override
  public void exitEos_rbinon_export_localpref(Eos_rbinon_export_localprefContext ctx) {
    // No means "set it to the default value" ; default means clear it and inherit as appropriate
    _currentAristaBgpNeighbor.setExportLocalPref(AristaBgpNeighbor.SYSTEM_DEFAULT_LOCALPREF);
  }

  @Override
  public void exitEos_rbinon_local_as(Eos_rbinon_local_asContext ctx) {
    _currentAristaBgpNeighbor.setLocalAs(null);
  }

  @Override
  public void exitEos_rbinon_next_hop_peer(Eos_rbinon_next_hop_peerContext ctx) {
    _currentAristaBgpNeighbor.setNextHopPeer(false);
  }

  @Override
  public void exitEos_rbinon_next_hop_self(Eos_rbinon_next_hop_selfContext ctx) {
    _currentAristaBgpNeighbor.setNextHopSelf(false);
  }

  @Override
  public void exitEos_rbinon_next_hop_unchanged(Eos_rbinon_next_hop_unchangedContext ctx) {
    _currentAristaBgpNeighbor.setNextHopUnchanged(false);
  }

  @Override
  public void exitEos_rbinon_passive(Eos_rbinon_passiveContext ctx) {
    _currentAristaBgpNeighbor.setPassive(false);
  }

  @Override
  public void exitEos_rbinon_peer_group(Eos_rbinon_peer_groupContext ctx) {
    if (_currentAristaBgpNeighbor instanceof AristaBgpHasPeerGroup) {
      // clearing the peer group for a concrete neighbor.
      ((AristaBgpHasPeerGroup) _currentAristaBgpNeighbor).setPeerGroup(null);
    } else {
      assert _currentAristaBgpNeighbor instanceof AristaBgpPeerGroupNeighbor;
      String name = ((AristaBgpPeerGroupNeighbor) _currentAristaBgpNeighbor).getName();
      if (_currentAristaBgpNeighborIsBrandNew) {
        warn(ctx, String.format("No BGP peer-group %s to delete", name));
        // still have to delete it, since we created it above in case of defalt creation
      }
      _currentAristaBgpProcess.deletePeerGroup(name);
    }
  }

  @Override
  public void exitEos_rbinon_remote_as(Eos_rbinon_remote_asContext ctx) {
    _currentAristaBgpNeighbor.setRemoteAs(null);
  }

  @Override
  public void exitEos_rbinon_remove_private_as(Eos_rbinon_remove_private_asContext ctx) {
    _currentAristaBgpNeighbor.setRemovePrivateAsMode(RemovePrivateAsMode.NONE);
  }

  @Override
  public void exitEos_rbinon_route_reflector_client(Eos_rbinon_route_reflector_clientContext ctx) {
    _currentAristaBgpNeighbor.setRouteReflectorClient(false);
  }

  @Override
  public void exitEos_rbinon_route_to_peer(Eos_rbinon_route_to_peerContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEos_rbinon_send_community(Eos_rbinon_send_communityContext ctx) {
    _currentAristaBgpNeighbor.setSendCommunity(false);
    _currentAristaBgpNeighbor.setSendExtendedCommunity(false);
  }

  @Override
  public void exitEos_rbinon_shutdown(Eos_rbinon_shutdownContext ctx) {
    _currentAristaBgpNeighbor.setShutdown(false);
  }

  @Override
  public void exitEos_rbinon_update_source(Eos_rbinon_update_sourceContext ctx) {
    _currentAristaBgpNeighbor.setUpdateSource(null);
  }

  @Override
  public void exitEos_rbinor_attached_host(Eos_rbinor_attached_hostContext ctx) {
    _currentAristaBgpVrf.removeRedistributionPolicy(AristaRedistributeType.ATTACHED_HOST);
  }

  @Override
  public void exitEos_rbinor_connected(Eos_rbinor_connectedContext ctx) {
    _currentAristaBgpVrf.removeRedistributionPolicy(AristaRedistributeType.CONNECTED);
  }

  @Override
  public void exitEos_rbinor_isis(Eos_rbinor_isisContext ctx) {
    _currentAristaBgpVrf.removeRedistributionPolicy(AristaRedistributeType.ISIS);
    _currentAristaBgpVrf.removeRedistributionPolicy(AristaRedistributeType.ISIS_L1);
    _currentAristaBgpVrf.removeRedistributionPolicy(AristaRedistributeType.ISIS_L2);
    _currentAristaBgpVrf.removeRedistributionPolicy(AristaRedistributeType.ISIS_L1_L2);
  }

  @Override
  public void exitEos_rbinor_ospf(Eos_rbinor_ospfContext ctx) {
    _currentAristaBgpVrf.removeRedistributionPolicy(OSPF);
    _currentAristaBgpVrf.removeRedistributionPolicy(OSPF_EXTERNAL);
    _currentAristaBgpVrf.removeRedistributionPolicy(OSPF_NSSA_EXTERNAL);
    _currentAristaBgpVrf.removeRedistributionPolicy(OSPF_NSSA_EXTERNAL_TYPE_1);
    _currentAristaBgpVrf.removeRedistributionPolicy(OSPF_NSSA_EXTERNAL_TYPE_2);
    _currentAristaBgpVrf.removeRedistributionPolicy(OSPF_INTERNAL);
  }

  @Override
  public void exitEos_rbinor_ospf_match(Eos_rbinor_ospf_matchContext ctx) {
    if (ctx.EXTERNAL() != null) {
      _currentAristaBgpVrf.removeRedistributionPolicy(OSPF_EXTERNAL);
    }
    if (ctx.INTERNAL() != null) {
      _currentAristaBgpVrf.removeRedistributionPolicy(OSPF_INTERNAL);
    }
    if (ctx.NSSA_EXTERNAL() != null) {
      _currentAristaBgpVrf.removeRedistributionPolicy(OSPF_NSSA_EXTERNAL);
    }
  }

  @Override
  public void exitEos_rbinor_rip(Eos_rbinor_ripContext ctx) {
    _currentAristaBgpVrf.removeRedistributionPolicy(AristaRedistributeType.RIP);
  }

  @Override
  public void exitEos_rbinor_static(Eos_rbinor_staticContext ctx) {
    _currentAristaBgpVrf.removeRedistributionPolicy(AristaRedistributeType.STATIC);
  }

  @Override
  public void exitEos_rbino_router_id(Eos_rbino_router_idContext ctx) {
    _currentAristaBgpVrf.setRouterId(null);
  }

  @Override
  public void exitEos_rbino_shutdown(Eos_rbino_shutdownContext ctx) {
    _currentAristaBgpVrf.setShutdown(false);
  }

  @Override
  public void exitEos_rbin_peer_group(Eos_rbin_peer_groupContext ctx) {
    assert _currentAristaBgpNeighbor instanceof AristaBgpHasPeerGroup;
    String peerGroupName = ctx.name.getText();
    ((AristaBgpHasPeerGroup) _currentAristaBgpNeighbor).setPeerGroup(peerGroupName);
    _configuration.referenceStructure(
        BGP_PEER_GROUP, peerGroupName, BGP_NEIGHBOR_PEER_GROUP, ctx.getStart().getLine());
  }

  @Override
  public void enterEos_rbi_peer_group(Eos_rbi_peer_groupContext ctx) {
    String peerGroupName = ctx.name.getText();
    _currentAristaBgpNeighbor =
        _currentAristaBgpProcess
            .getPeerGroups()
            .computeIfAbsent(peerGroupName, AristaBgpPeerGroupNeighbor::new);
    _currentAristaBgpNeighborAddressFamily = _currentAristaBgpNeighbor.getGenericAddressFamily();
    _configuration.defineStructure(BGP_PEER_GROUP, peerGroupName, ctx);
  }

  @Override
  public void exitEos_rbi_peer_group(Eos_rbi_peer_groupContext ctx) {
    _currentAristaBgpNeighbor = null;
    _currentAristaBgpNeighborAddressFamily = null;
  }

  @Override
  public void exitEos_rbir_attached_host(Eos_rbir_attached_hostContext ctx) {
    todo(ctx);
    String routeMap = ctx.rm == null ? null : ctx.rm.getText();
    if (routeMap != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_REDISTRIBUTE_ATTACHED_HOST_MAP, ctx.getStart().getLine());
    }
    _currentAristaBgpVrf.addRedistributionPolicy(AristaRedistributeType.ATTACHED_HOST, routeMap);
  }

  @Override
  public void exitEos_rbir_connected(Eos_rbir_connectedContext ctx) {
    String routeMap = ctx.rm == null ? null : ctx.rm.getText();
    if (routeMap != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_REDISTRIBUTE_CONNECTED_MAP, ctx.getStart().getLine());
    }
    _currentAristaBgpVrf.addRedistributionPolicy(AristaRedistributeType.CONNECTED, routeMap);
  }

  @Override
  public void exitEos_rbir_dynamic(Eos_rbir_dynamicContext ctx) {
    todo(ctx);
    String routeMap = ctx.rm == null ? null : ctx.rm.getText();
    if (routeMap != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_REDISTRIBUTE_DYNAMIC_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitEos_rbir_isis(Eos_rbir_isisContext ctx) {
    todo(ctx);
    String routeMap = ctx.rm == null ? null : ctx.rm.getText();
    if (routeMap != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_REDISTRIBUTE_ISIS_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitEos_rbir_ospf(Eos_rbir_ospfContext ctx) {
    String routeMap = ctx.rm == null ? null : ctx.rm.getText();
    if (routeMap != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_REDISTRIBUTE_OSPF_MAP, ctx.getStart().getLine());
    }
    if (ctx.MATCH() == null) {
      _currentAristaBgpVrf.addRedistributionPolicy(OSPF, routeMap);
    } else {
      if (ctx.INTERNAL() != null) {
        _currentAristaBgpVrf.addRedistributionPolicy(OSPF_INTERNAL, routeMap);
      } else if (ctx.EXTERNAL() != null) {
        _currentAristaBgpVrf.addRedistributionPolicy(OSPF_EXTERNAL, routeMap);
      } else if (ctx.NSSA_EXTERNAL() != null) {
        if (ctx.nssa_type == null) {
          _currentAristaBgpVrf.addRedistributionPolicy(OSPF_NSSA_EXTERNAL, routeMap);
        } else if (toInteger(ctx.nssa_type) == 1) {
          _currentAristaBgpVrf.addRedistributionPolicy(OSPF_NSSA_EXTERNAL_TYPE_1, routeMap);
        } else if (toInteger(ctx.nssa_type) == 2) {
          _currentAristaBgpVrf.addRedistributionPolicy(OSPF_NSSA_EXTERNAL_TYPE_2, routeMap);
        } else {
          _w.addWarning(
              ctx,
              getFullText(ctx),
              _parser,
              String.format("Unknown OSPF nssa-external route type: %s", ctx.nssa_type.getText()));
        }
      }
    }
  }

  @Override
  public void exitEos_rbir_ospf3(Eos_rbir_ospf3Context ctx) {
    todo(ctx);
    String routeMap = ctx.rm == null ? null : ctx.rm.getText();
    if (routeMap != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_REDISTRIBUTE_OSPFV3_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitEos_rbir_rip(Eos_rbir_ripContext ctx) {
    todo(ctx);
    String routeMap = ctx.rm == null ? null : ctx.rm.getText();
    if (routeMap != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_REDISTRIBUTE_RIP_MAP, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitEos_rbir_static(Eos_rbir_staticContext ctx) {
    String routeMap = ctx.rm == null ? null : ctx.rm.getText();
    if (routeMap != null) {
      _configuration.referenceStructure(
          ROUTE_MAP, routeMap, BGP_REDISTRIBUTE_STATIC_MAP, ctx.getStart().getLine());
    }
    _currentAristaBgpVrf.addRedistributionPolicy(AristaRedistributeType.STATIC, routeMap);
  }

  @Override
  public void exitEos_rbi_router_id(Eos_rbi_router_idContext ctx) {
    _currentAristaBgpVrf.setRouterId(toIp(ctx.id));
  }

  @Override
  public void exitEos_rbi_timers(Eos_rbi_timersContext ctx) {
    _currentAristaBgpVrf.setHoldTimer(toInteger(ctx.hold));
    _currentAristaBgpVrf.setKeepAliveTimer(toInteger(ctx.keepalive));
  }

  @Override
  public void exitEos_rbi_shutdown(Eos_rbi_shutdownContext ctx) {
    _currentAristaBgpVrf.setShutdown(true);
  }

  @Override
  public void exitEos_rb_vab_vlan(Eos_rb_vab_vlanContext ctx) {
    // Enforced by the grammar
    assert _currentAristaBgpVlan instanceof AristaBgpVlanAwareBundle;
    ((AristaBgpVlanAwareBundle) _currentAristaBgpVlan).setVlans(toIntegerSpace(ctx.vlans));
  }

  @Override
  public void enterEos_rb_vlan(Eos_rb_vlanContext ctx) {
    int vlan = toInteger(ctx.id);
    _currentAristaBgpVlan =
        _currentAristaBgpProcess.getVlans().computeIfAbsent(vlan, AristaBgpVlan::new);
  }

  @Override
  public void exitEos_rb_vlan(Eos_rb_vlanContext ctx) {
    _currentAristaBgpVlan = null;
  }

  @Override
  public void exitEos_rb_vlan_tail_rd(Eos_rb_vlan_tail_rdContext ctx) {
    _currentAristaBgpVlan.setRd(toRouteDistinguisher(ctx.rd));
  }

  @Override
  public void exitEos_rb_vlan_tail_redistribute(Eos_rb_vlan_tail_redistributeContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitEos_rb_vlan_tail_route_target(Eos_rb_vlan_tail_route_targetContext ctx) {
    ExtendedCommunity rt = toRouteTarget(ctx.rt);
    if (ctx.IMPORT() != null || ctx.BOTH() != null) {
      _currentAristaBgpVlan.setRtImport(rt);
    }
    if (ctx.EXPORT() != null || ctx.BOTH() != null) {
      _currentAristaBgpVlan.setRtExport(rt);
    }
  }

  @Override
  public void enterEos_rb_vlan_aware_bundle(Eos_rb_vlan_aware_bundleContext ctx) {
    _currentAristaBgpVlan =
        _currentAristaBgpProcess
            .getVlanAwareBundles()
            .computeIfAbsent(ctx.name.getText(), AristaBgpVlanAwareBundle::new);
  }

  @Override
  public void exitEos_rb_vlan_aware_bundle(Eos_rb_vlan_aware_bundleContext ctx) {
    _currentAristaBgpVlan = null;
  }

  @Override
  public void enterEos_rb_vrf(Eos_rb_vrfContext ctx) {
    String name = ctx.name.getText();
    _currentAristaBgpVrf =
        _currentAristaBgpProcess.getVrfs().computeIfAbsent(name, AristaBgpVrf::new);
  }

  @Override
  public void exitEos_rb_vrf(Eos_rb_vrfContext ctx) {
    _currentAristaBgpVrf = _currentAristaBgpProcess.getDefaultVrf();
  }

  @Override
  public void exitEos_rbv_local_as(Eos_rbv_local_asContext ctx) {
    _currentAristaBgpVrf.setLocalAs(toAsNum(ctx.asn));
  }

  @Override
  public void exitEos_rbv_rd(Eos_rbv_rdContext ctx) {
    _currentAristaBgpVrf.setRouteDistinguisher(toRouteDistinguisher(ctx.rd));
  }

  @Override
  public void exitEos_rbv_route_target(Eos_rbv_route_targetContext ctx) {
    if (ctx.EVPN() == null && ctx.p != null) {
      warn(ctx, "Only EVPN is supported");
      return;
    }
    if (ctx.EXPORT() != null) {
      _currentAristaBgpVrf.setExportRouteTarget(toRouteTarget(ctx.rt));
    }
    if (ctx.IMPORT() != null) {
      _currentAristaBgpVrf.setImportRouteTarget(toRouteTarget(ctx.rt));
    }
  }

  @Override
  public void enterS_vlan(S_vlanContext ctx) {
    IntegerSpace vlans = toIntegerSpace(ctx.eos_vlan_id());
    _currentVlans =
        vlans
            .intStream()
            .mapToObj(_configuration::getOrCreateVlan)
            .collect(ImmutableList.toImmutableList());
  }

  @Override
  public void enterS_eos_vxlan_interface(S_eos_vxlan_interfaceContext ctx) {
    String canonicalVxlanName = getCanonicalInterfaceName(ctx.iname.getText());
    if (_eosVxlan == null) {
      _eosVxlan = new AristaEosVxlan(canonicalVxlanName);
    } else if (!_eosVxlan.getInterfaceName().equals(canonicalVxlanName)) {
      warn(ctx, "Only one VXLAN interface may be defined, appending to existing interface");
    }
    _configuration.setEosVxlan(_eosVxlan);
    _configuration.defineStructure(VXLAN, canonicalVxlanName, ctx);
    _configuration.referenceStructure(
        VXLAN, canonicalVxlanName, VXLAN_SELF_REF, ctx.iname.getStart().getLine());
  }

  @Override
  public void exitEos_vxif_arp(Eos_vxif_arpContext ctx) {
    _eosVxlan.setArpReplyRelay(true);
  }

  @Override
  public void exitEos_vxif_description(Eos_vxif_descriptionContext ctx) {
    _eosVxlan.setDescription(getDescription(ctx.description_line()));
  }

  @Override
  public void exitEos_vxif_vxlan_flood(Eos_vxif_vxlan_floodContext ctx) {
    SortedSet<Ip> floodAddresses = _eosVxlan.getFloodAddresses();
    if (_currentVxlanVlanNum != null) {
      floodAddresses =
          _eosVxlan
              .getVlanFloodAddresses()
              .computeIfAbsent(_currentVxlanVlanNum, n -> new TreeSet<>());
    }

    if (ctx.REMOVE() != null) {
      for (Token host : ctx.hosts) {
        floodAddresses.remove(toIp(host));
      }
      return;
    }

    if (ctx.ADD() == null) {
      // Replace existing addresses instead of adding
      floodAddresses.clear();
    }
    for (Token host : ctx.hosts) {
      floodAddresses.add(toIp(host));
    }
  }

  @Override
  public void exitEos_vxif_vxlan_multicast_group(Eos_vxif_vxlan_multicast_groupContext ctx) {
    _eosVxlan.setMulticastGroup(toIp(ctx.group));
  }

  @Override
  public void exitEos_vxif_vxlan_source_interface(Eos_vxif_vxlan_source_interfaceContext ctx) {
    String ifaceName = getCanonicalInterfaceName(ctx.iface.getText());
    _eosVxlan.setSourceInterface(ifaceName);
    _configuration.referenceStructure(
        INTERFACE, ifaceName, VXLAN_SOURCE_INTERFACE, ctx.iface.getStart().getLine());
  }

  @Override
  public void exitEos_vxif_vxlan_udp_port(Eos_vxif_vxlan_udp_portContext ctx) {
    _eosVxlan.setUdpPort(toInteger(ctx.num));
  }

  @Override
  public void enterEos_vxif_vxlan_vlan_vni_range(Eos_vxif_vxlan_vlan_vni_rangeContext ctx) {
    ImmutableList.Builder<Integer> vlansBuilder = ImmutableList.builder();
    for (Vlan_subrangeContext rangeCtx : ctx.vlans.vlan_range_list) {
      Optional<SubRange> maybeRange = toSubRange(ctx, rangeCtx);
      if (!maybeRange.isPresent()) {
        // already warned
        return;
      }
      maybeRange.get().asStream().forEach(vlansBuilder::add);
    }
    List<Integer> vlans = vlansBuilder.build();

    ImmutableList.Builder<Integer> vnisBuilder = ImmutableList.builder();
    for (Vni_subrangeContext rangeCtx : ctx.vnis.vni_range_list) {
      Optional<SubRange> maybeRange = toSubRange(ctx, rangeCtx);
      if (!maybeRange.isPresent()) {
        // already warned
        return;
      }
      maybeRange.get().asStream().forEach(vnisBuilder::add);
    }
    List<Integer> vnis = vnisBuilder.build();

    if (vlans.size() != vnis.size()) {
      warn(ctx, "Need to have 1:1 mapping of vlan to vni");
      return;
    }

    for (int i = 0; i < vnis.size(); i++) {
      _eosVxlan.getVlanVnis().put(vlans.get(i), vnis.get(i));
    }
  }

  @Override
  public void enterEos_vxif_vxlan_vlan(Eos_vxif_vxlan_vlanContext ctx) {
    _currentVxlanVlanNum = toInteger(ctx.num);
  }

  @Override
  public void exitEos_vxif_vxlan_vlan(Eos_vxif_vxlan_vlanContext ctx) {
    _currentVxlanVlanNum = null;
  }

  @Override
  public void exitEos_vxif_vxlan_vlan_vni(Eos_vxif_vxlan_vlan_vniContext ctx) {
    _eosVxlan.getVlanVnis().computeIfAbsent(_currentVxlanVlanNum, n -> toInteger(ctx.num));
  }

  @Override
  public void exitEos_vxif_vxlan_vrf(Eos_vxif_vxlan_vrfContext ctx) {
    _eosVxlan.getVrfToVni().put(ctx.vrf.getText(), toInteger(ctx.vni));
  }

  @Override
  public void enterExtended_ipv6_access_list_stanza(Extended_ipv6_access_list_stanzaContext ctx) {
    String name;
    if (ctx.name != null) {
      name = ctx.name.getText();
    } else {
      throw new BatfishException("Could not determine acl name");
    }
    _currentExtendedIpv6Acl =
        _configuration.getExtendedIpv6Acls().computeIfAbsent(name, ExtendedIpv6AccessList::new);
    _configuration.defineStructure(IPV6_ACCESS_LIST_EXTENDED, name, ctx);
  }

  @Override
  public void enterIf_description(If_descriptionContext ctx) {
    String description = getDescription(ctx.description_line());
    for (Interface currentInterface : _currentInterfaces) {
      currentInterface.setDescription(description);
    }
  }

  @Override
  public void enterIf_vrrp(If_vrrpContext ctx) {
    _currentVrrpGroupNum = toInteger(ctx.groupnum);
  }

  @Override
  public void enterInterface_is_stanza(Interface_is_stanzaContext ctx) {
    Interface iface = getOrAddInterface(ctx.iname);
    iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
    _currentIsisInterface = iface;
  }

  @Override
  public void exitIp_community_list_expanded_stanza(Ip_community_list_expanded_stanzaContext ctx) {
    LineAction action = toLineAction(ctx.action);
    String regex = ctx.regexp.getText();

    Optional<ExpandedCommunityListLine> maybeLine = ExpandedCommunityListLine.create(action, regex);
    if (!maybeLine.isPresent()) {
      warn(ctx, "Invalid regex");
      return;
    }
    ExpandedCommunityListLine line = maybeLine.get();

    String name = ctx.name.getText();
    ExpandedCommunityList list =
        _configuration
            .getExpandedCommunityLists()
            .computeIfAbsent(name, ExpandedCommunityList::new);

    list.addLine(line);
    _configuration.defineStructure(COMMUNITY_LIST_EXPANDED, name, ctx);
  }

  @Override
  public void exitIp_community_list_standard_stanza(Ip_community_list_standard_stanzaContext ctx) {
    String name = ctx.name_cl.getText();
    StandardCommunityList list =
        _configuration
            .getStandardCommunityLists()
            .computeIfAbsent(name, StandardCommunityList::new);
    _configuration.defineStructure(COMMUNITY_LIST_STANDARD, name, ctx);

    StandardCommunityListLine line =
        new StandardCommunityListLine(
            toLineAction(ctx.action),
            ctx.communities.stream()
                .map(AristaControlPlaneExtractor::toStandardCommunity)
                .collect(ImmutableList.toImmutableList()));
    list.getLines().add(line);
  }

  @Override
  public void enterIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
    String name = ctx.name.getText();
    _currentPrefixList = _configuration.getPrefixLists().computeIfAbsent(name, PrefixList::new);
    _configuration.defineStructure(PREFIX_LIST, name, ctx);
  }

  @Override
  public void enterIpv6_prefix_list_stanza(Ipv6_prefix_list_stanzaContext ctx) {
    String name = ctx.name.getText();
    _currentPrefix6List = _configuration.getPrefix6Lists().computeIfAbsent(name, Prefix6List::new);
    _configuration.defineStructure(PREFIX6_LIST, name, ctx);
  }

  @Override
  public void enterIs_type_is_stanza(Is_type_is_stanzaContext ctx) {
    IsisProcess proc = currentVrf().getIsisProcess();
    if (ctx.LEVEL_1() != null) {
      proc.setLevel(IsisLevel.LEVEL_1);
    } else if (ctx.LEVEL_2_ONLY() != null || ctx.LEVEL_2() != null) {
      proc.setLevel(IsisLevel.LEVEL_2);
    } else {
      throw new BatfishException("Unsupported is-type");
    }
  }

  @Override
  public void enterNet_is_stanza(Net_is_stanzaContext ctx) {
    IsisProcess proc = currentVrf().getIsisProcess();
    IsoAddress isoAddress = new IsoAddress(ctx.ISO_ADDRESS().getText());
    proc.setNetAddress(isoAddress);
  }

  @Override
  public void enterRoute_map_stanza(Route_map_stanzaContext ctx) {
    String name = ctx.name.getText();
    RouteMap routeMap = _configuration.getRouteMaps().computeIfAbsent(name, RouteMap::new);
    _currentRouteMap = routeMap;
    int num = ctx.num != null ? toInteger(ctx.num) : 10;
    LineAction action = ctx.rmt != null ? toLineAction(ctx.rmt) : LineAction.PERMIT;
    RouteMapClause clause = _currentRouteMap.getClauses().get(num);
    if (clause == null) {
      clause = new RouteMapClause(action, name, num);
      routeMap.getClauses().put(num, clause);
    } else {
      warn(
          ctx,
          String.format(
              "Route map '%s' already contains clause numbered '%d'. Duplicate clause will be"
                  + " merged with original clause.",
              _currentRouteMap.getName(), num));
      // Yes, action can change if the line is reconfigured.
      clause.setAction(action);
    }
    _currentRouteMapClause = clause;
    _configuration.defineStructure(ROUTE_MAP, name, ctx);
    // Declare and auto-reference the entry too.
    String entryName = computeRouteMapEntryName(name, num);
    _configuration.defineStructure(ROUTE_MAP_ENTRY, entryName, ctx);
    _configuration.referenceStructure(
        ROUTE_MAP_ENTRY, entryName, ROUTE_MAP_ENTRY_AUTO_REF, ctx.name.getStart().getLine());
  }

  @Override
  public void enterRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
    _currentAristaBgpProcess = _configuration.getAristaBgp();
    long asn = toAsNum(ctx.bgp_asn());
    if (_currentAristaBgpProcess == null) {
      _currentAristaBgpProcess = new AristaBgpProcess(asn);
      _configuration.setAristaBgp(_currentAristaBgpProcess);
    } else if (asn != _currentAristaBgpProcess.getAsn()) {
      // Create a dummy node
      _currentAristaBgpProcess = new AristaBgpProcess(asn);
      _w.addWarning(ctx, getFullText(ctx), _parser, "Ignoring bgp configuration for invalid ASN");
    }
    _currentAristaBgpVrf = _currentAristaBgpProcess.getDefaultVrf();
  }

  @Override
  public void exitRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
    _currentAristaBgpProcess = null;
    _currentAristaBgpVrf = null;
  }

  @Override
  public void enterRouter_isis_stanza(Router_isis_stanzaContext ctx) {
    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setLevel(IsisLevel.LEVEL_1_2);
    currentVrf().setIsisProcess(isisProcess);
    _currentIsisProcess = isisProcess;
  }

  @Override
  public void enterS_aaa(S_aaaContext ctx) {
    if (_configuration.getCf().getAaa() == null) {
      _configuration.getCf().setAaa(new Aaa());
    }
  }

  @Override
  public void enterS_access_line(S_access_lineContext ctx) {
    String name = ctx.linetype.getText();
    _configuration.getCf().getLines().computeIfAbsent(name, Line::new);
  }

  @Override
  public void enterS_bfd_template(S_bfd_templateContext ctx) {
    _configuration.defineStructure(BFD_TEMPLATE, ctx.name.getText(), ctx);
  }

  @Override
  public void enterS_class_map(S_class_mapContext ctx) {
    // TODO: do something with this.
    String name = ctx.name.getText();
    _configuration.defineStructure(CLASS_MAP, name, ctx);
  }

  @Override
  public void enterS_eos_mlag(S_eos_mlagContext ctx) {
    if (_configuration.getEosMlagConfiguration() == null) {
      _configuration.setEosMlagConfiguration(new MlagConfiguration());
    }
  }

  @Override
  public void enterS_interface(S_interfaceContext ctx) {
    String nameAlpha = ctx.iname.name_prefix_alpha.getText();
    String canonicalNamePrefix;
    try {
      canonicalNamePrefix = AristaConfiguration.getCanonicalInterfaceNamePrefix(nameAlpha);
    } catch (BatfishException e) {
      warn(ctx, "Error fetching interface name: " + e.getMessage());
      _currentInterfaces = ImmutableList.of();
      return;
    }
    StringBuilder namePrefix = new StringBuilder(canonicalNamePrefix);
    for (Token part : ctx.iname.name_middle_parts) {
      namePrefix.append(part.getText());
    }
    _currentInterfaces = new ArrayList<>();
    if (ctx.iname.range() != null) {
      List<SubRange> ranges = toRange(ctx.iname.range());
      for (SubRange range : ranges) {
        for (int i = range.getStart(); i <= range.getEnd(); i++) {
          String name = namePrefix.toString() + i;
          addInterface(name, ctx.iname, true);
          _configuration.defineStructure(INTERFACE, name, ctx);
          _configuration.referenceStructure(
              INTERFACE, name, INTERFACE_SELF_REF, ctx.getStart().getLine());
        }
      }
    } else {
      addInterface(namePrefix.toString(), ctx.iname, true);
    }
    if (ctx.MULTIPOINT() != null) {
      todo(ctx);
    }
  }

  @Override
  public void enterS_ip_dhcp(S_ip_dhcpContext ctx) {
    _no = (ctx.NO() != null);
  }

  @Override
  public void enterS_ip_domain(S_ip_domainContext ctx) {
    _no = ctx.NO() != null;
  }

  private @Nonnull Optional<NextHop> toNextHop(Ip_route_nexthopContext ctx) {
    if (ctx.null0 != null) {
      return Optional.of(new NextHop(null, null, true));
    } else if (ctx.nexthopint == null) {
      assert ctx.nexthopip != null; // guaranteed by grammar.
      return Optional.of(new NextHop(null, toIp(ctx.nexthopip), false));
    } else {
      String nextHopInterface;
      try {
        nextHopInterface = getCanonicalInterfaceName(ctx.nexthopint.getText());
      } catch (BatfishException e) {
        warn(ctx, "Error fetching interface name: " + e.getMessage());
        return Optional.empty();
      }
      _configuration.referenceStructure(
          INTERFACE, nextHopInterface, IP_ROUTE_NHINT, ctx.nexthopint.getStart().getLine());
      if (ctx.nexthopip != null) {
        // NHINT + IP.
        return Optional.of(new NextHop(nextHopInterface, toIp(ctx.nexthopip), false));
      } else {
        // NHINT only.
        return Optional.of(new NextHop(nextHopInterface, null, false));
      }
    }
  }

  @Override
  public void exitS_ip_route(S_ip_routeContext ctx) {
    Vrf vrf = _configuration.getVrfs().get(AristaConfiguration.DEFAULT_VRF_NAME);
    if (ctx.vrf != null) {
      String vrfName = toString(ctx.vrf);
      vrf = _configuration.getVrfs().get(vrfName);
      if (vrf == null) {
        warn(ctx.vrf, "VRF " + vrfName + " is not yet defined");
        return;
      }
    }
    @Nullable Integer distance = null;
    if (ctx.distance != null) {
      Optional<Integer> maybeDistance = toInteger(ctx, ctx.distance);
      if (maybeDistance.isPresent()) {
        distance = maybeDistance.get();
      } else {
        // already warned.
        return;
      }
    }
    Optional<NextHop> maybeNextHop = toNextHop(ctx.nh);
    if (!maybeNextHop.isPresent()) {
      // Already warned.
      return;
    }
    NextHop nextHop = maybeNextHop.get();
    Prefix prefix = toPrefix(ctx.prefix);
    long tag = ctx.tag != null ? toLong(ctx.tag) : 0L;
    boolean track = ctx.track != null;
    StaticRoute route = new StaticRoute(nextHop, distance, track);
    if (track) {
      todo(ctx.track);
    }
    StaticRouteManager srm =
        vrf.getStaticRoutes().computeIfAbsent(prefix, p -> new StaticRouteManager());
    Optional<String> maybeProblem = srm.addVariant(route);
    if (maybeProblem.isPresent()) {
      warn(ctx, maybeProblem.get());
      return;
    }
    srm.setTag(tag);
  }

  @Override
  public void exitNo_ip_route(No_ip_routeContext ctx) {
    Vrf vrf = _configuration.getVrfs().get(AristaConfiguration.DEFAULT_VRF_NAME);
    if (ctx.vrf != null) {
      String vrfName = toString(ctx.vrf);
      vrf = _configuration.getVrfs().get(vrfName);
      if (vrf == null) {
        warn(ctx.vrf, "VRF " + vrfName + " is not yet defined");
        return;
      }
    }
    Prefix p = toPrefix(ctx.prefix);
    StaticRouteManager removeFrom = vrf.getStaticRoutes().get(p);
    if (removeFrom == null) {
      warn(ctx.prefix, "No static routes for network " + p + " in vrf " + vrf.getName());
      return;
    }

    if (ctx.nh == null) {
      // Without a next-hop, none of the config is compared to the route.
      vrf.getStaticRoutes().remove(p);
      return;
    }

    Optional<NextHop> maybeNextHop = toNextHop(ctx.nh);
    if (!maybeNextHop.isPresent()) {
      // already warned.
      return;
    }

    @Nullable Integer distance = null;
    if (ctx.distance != null) {
      Optional<Integer> maybeDistance = toInteger(ctx, ctx.distance);
      if (maybeDistance.isPresent()) {
        distance = maybeDistance.get();
      } else {
        // already warned.
        return;
      }
    }
    NextHop nextHop = maybeNextHop.get();
    boolean removedSomething = removeFrom.removeVariant(nextHop, distance);
    if (!removedSomething) {
      warn(ctx.getParent(), "No static routes found to remove");
    }

    if (removeFrom.getVariants().isEmpty()) {
      // If there are no more routes, remove the prefix too.
      vrf.getStaticRoutes().remove(p);
    }
  }

  @Override
  public void enterS_ip_ssh(S_ip_sshContext ctx) {
    if (_configuration.getCf().getSsh() == null) {
      _configuration.getCf().setSsh(new SshSettings());
    }
  }

  @Override
  public void enterS_l2tp_class(S_l2tp_classContext ctx) {
    String name = ctx.name.getText();
    _configuration.getCf().getL2tpClasses().computeIfAbsent(name, L2tpClass::new);
    _configuration.defineStructure(L2TP_CLASS, name, ctx);
  }

  @Override
  public void enterS_logging(S_loggingContext ctx) {
    if (_configuration.getCf().getLogging() == null) {
      _configuration.getCf().setLogging(new Logging());
    }
  }

  @Override
  public void enterS_ntp(S_ntpContext ctx) {
    if (_configuration.getCf().getNtp() == null) {
      _configuration.getCf().setNtp(new Ntp());
    }
  }

  @Override
  public void enterS_policy_map(S_policy_mapContext ctx) {
    // TODO: do something with this.
    if (ctx.mapname != null) {
      String name = ctx.mapname.getText();
      _configuration.defineStructure(POLICY_MAP, name, ctx);
    }
  }

  @Override
  public void enterS_peer_filter(S_peer_filterContext ctx) {
    String name = ctx.name.getText();
    _currentPeerFilter =
        _configuration.getPeerFilters().computeIfAbsent(name, AristaBgpPeerFilter::new);
    _configuration.defineStructure(PEER_FILTER, name, ctx);
  }

  @Override
  public void exitS_peer_filter(S_peer_filterContext ctx) {
    _currentPeerFilter = null;
  }

  @Override
  public void exitPeer_filter_line(Peer_filter_lineContext ctx) {
    assert _currentPeerFilter != null;
    AristaBgpPeerFilterLine.Action action;
    if (ctx.ACCEPT() != null) {
      action = AristaBgpPeerFilterLine.Action.ACCEPT;
    } else if (ctx.REJECT() != null) {
      action = AristaBgpPeerFilterLine.Action.REJECT;
    } else {
      throw new IllegalStateException("peer-filter line without known action");
    }
    LongSpace asSpace = toAsSpace(ctx.asn_range);
    if (ctx.seq == null) {
      _currentPeerFilter.addLine(asSpace, action);
    } else {
      _currentPeerFilter.addLine(toInteger(ctx.seq), asSpace, action);
    }
  }

  @Override
  public void exitPim_sm4_rp_address(Pim_sm4_rp_addressContext ctx) {
    String aclName = ctx.name.getText();
    _configuration.referenceStructure(
        IPV4_ACCESS_LIST,
        aclName,
        ROUTER_PIM_RP_ADDRESS_ACCESS_LIST,
        ctx.name.getStart().getLine());
  }

  @Override
  public void enterS_router_ospf(S_router_ospfContext ctx) {
    String procName = toString(ctx.name);
    if (ctx.vrf != null) {
      _currentVrf = ctx.vrf.getText();
    }
    _currentOspfProcess =
        currentVrf()
            .getOspfProcesses()
            .computeIfAbsent(procName, (pName) -> new OspfProcess(pName));
  }

  @Override
  public void enterS_service_template(S_service_templateContext ctx) {
    // TODO: do something with this.
    String name = ctx.name.getText();
    _configuration.defineStructure(SERVICE_TEMPLATE, name, ctx);
  }

  @Override
  public void exitRo_auto_cost(Ro_auto_costContext ctx) {
    long referenceBandwidthDec = toLong(ctx.refbw);
    long referenceBandwidth;
    if (ctx.MBPS() != null) {
      referenceBandwidth = referenceBandwidthDec * 1_000_000;
    } else if (ctx.GBPS() != null) {
      referenceBandwidth = referenceBandwidthDec * 1_000_000_000;
    } else {
      referenceBandwidth = referenceBandwidthDec * 1_000_000;
    }
    _currentOspfProcess.setReferenceBandwidth(referenceBandwidth);
  }

  @Override
  public void exitRo_max_metric(Ro_max_metricContext ctx) {
    if (ctx.on_startup != null || !ctx.WAIT_FOR_BGP().isEmpty()) {
      return;
    }
    _currentOspfProcess.setMaxMetricRouterLsa(true);
    _currentOspfProcess.setMaxMetricIncludeStub(ctx.stub != null);
    if (ctx.external_lsa != null) {
      _currentOspfProcess.setMaxMetricExternalLsa(
          ctx.external != null
              ? toLong(ctx.external)
              : OspfProcess.DEFAULT_MAX_METRIC_EXTERNAL_LSA);
    }
    if (ctx.summary_lsa != null) {
      _currentOspfProcess.setMaxMetricSummaryLsa(
          ctx.summary != null ? toLong(ctx.summary) : OspfProcess.DEFAULT_MAX_METRIC_SUMMARY_LSA);
    }
  }

  @Override
  public void enterS_router_rip(S_router_ripContext ctx) {
    RipProcess proc = new RipProcess();
    currentVrf().setRipProcess(proc);
    _currentRipProcess = proc;
  }

  @Override
  public void enterS_snmp_server(S_snmp_serverContext ctx) {
    if (_configuration.getSnmpServer() == null) {
      SnmpServer snmpServer = new SnmpServer();
      snmpServer.setVrf(AristaConfiguration.DEFAULT_VRF_NAME);
      _configuration.setSnmpServer(snmpServer);
    }
  }

  @Override
  public void enterS_sntp(S_sntpContext ctx) {
    if (_configuration.getCf().getSntp() == null) {
      _configuration.getCf().setSntp(new Sntp());
    }
  }

  @Override
  public void enterS_spanning_tree(S_spanning_treeContext ctx) {
    _no = ctx.NO() != null;
  }

  @Override
  public void enterS_tacacs_server(S_tacacs_serverContext ctx) {
    _no = ctx.NO() != null;
  }

  @Override
  public void enterS_track(S_trackContext ctx) {
    String name = ctx.name.getText();
    _currentTrackingGroup = name;
    _configuration.defineStructure(TRACK, name, ctx);
  }

  @Override
  public void exitS_track(S_trackContext ctx) {
    _currentTrackingGroup = null;
  }

  @Override
  public void exitTrack_interface(Track_interfaceContext ctx) {
    String name = toInterfaceName(ctx.interface_name());
    _configuration.referenceStructure(
        INTERFACE, name, TRACK_INTERFACE, ctx.interface_name().getStart().getLine());
    // TODO: create and use vendor classes for tracking.
    _configuration.getTrackingGroups().put(_currentTrackingGroup, interfaceActive(name));
  }

  @Override
  public void enterS_username(S_usernameContext ctx) {
    String username;
    if (ctx.user != null) {
      username = ctx.user.getText();
    } else {
      username = unquote(ctx.quoted_user.getText());
    }
    _currentUser = _configuration.getCf().getUsers().computeIfAbsent(username, User::new);
  }

  @Override
  public void enterS_vrf_definition(S_vrf_definitionContext ctx) {
    _currentVrf = ctx.name.getText();
    initVrf(_currentVrf);
  }

  @Override
  public void enterSs_community(Ss_communityContext ctx) {
    String name = ctx.name.getText();
    Map<String, SnmpCommunity> communities = _configuration.getSnmpServer().getCommunities();
    _currentSnmpCommunity = communities.computeIfAbsent(name, SnmpCommunity::new);
  }

  @Override
  public void enterSs_host(Ss_hostContext ctx) {
    String hostname;
    if (ctx.ip4 != null) {
      hostname = ctx.ip4.getText();
    } else if (ctx.ip6 != null) {
      hostname = ctx.ip6.getText();
    } else if (ctx.host != null) {
      hostname = ctx.host.getText();
    } else {
      throw new BatfishException("Invalid host");
    }
    Map<String, SnmpHost> hosts = _configuration.getSnmpServer().getHosts();
    hosts.computeIfAbsent(hostname, SnmpHost::new);
  }

  @Override
  public void enterStandard_ipv6_access_list_stanza(Standard_ipv6_access_list_stanzaContext ctx) {
    String name;
    if (ctx.name != null) {
      name = ctx.name.getText();
    } else {
      throw new BatfishException("Invalid standard access-list name");
    }
    _currentStandardIpv6Acl =
        _configuration.getStandardIpv6Acls().computeIfAbsent(name, StandardIpv6AccessList::new);
    _configuration.defineStructure(IPV6_ACCESS_LIST_STANDARD, name, ctx);
  }

  @Override
  public void enterTs_host(Ts_hostContext ctx) {
    String hostname = ctx.hostname.getText();
    if (!_no) {
      _configuration.getTacacsServers().add(hostname);
    }
  }

  @Override
  public void exitAaa_accounting_default_group(Aaa_accounting_default_groupContext ctx) {
    List<String> groups =
        ctx.groups.stream().map(RuleContext::getText).collect(Collectors.toList());
    _configuration.getCf().getAaa().getAccounting().getDefault().setGroups(groups);
  }

  @Override
  public void exitAaa_accounting_default_local(Aaa_accounting_default_localContext ctx) {
    _configuration.getCf().getAaa().getAccounting().getDefault().setLocal(true);
  }

  @Override
  public void exitAaa_authentication_login_list(Aaa_authentication_login_listContext ctx) {
    _currentAaaAuthenticationLoginList = null;
  }

  @Override
  public void exitAaa_authentication_login_privilege_mode(
      Aaa_authentication_login_privilege_modeContext ctx) {
    _configuration.getCf().getAaa().getAuthentication().getLogin().setPrivilegeMode(true);
  }

  @Override
  public void exitAaa_new_model(Aaa_new_modelContext ctx) {
    _configuration.getCf().getAaa().setNewModel(!_no);
  }

  @Override
  public void exitS_banner_eos(S_banner_eosContext ctx) {
    String bannerType = ctx.type.getText();
    String body = ctx.body != null ? ctx.body.getText() : "";
    _configuration.getCf().getBanners().put(bannerType, body);
  }

  @Override
  public void exitCip_profile(Cip_profileContext ctx) {
    _configuration.getIpsecProfiles().put(_currentIpsecProfile.getName(), _currentIpsecProfile);
    _currentIpsecProfile = null;
  }

  @Override
  public void exitCip_transform_set(Cip_transform_setContext ctx) {
    _configuration
        .getIpsecTransformSets()
        .put(_currentIpsecTransformSet.getName(), _currentIpsecTransformSet);
    _currentIpsecTransformSet = null;
  }

  @Override
  public void exitCis_policy(Cis_policyContext ctx) {
    _configuration.getIsakmpPolicies().put(_currentIsakmpPolicy.getName(), _currentIsakmpPolicy);
    _currentIsakmpPolicy = null;
  }

  @Override
  public void exitCis_profile(Cis_profileContext ctx) {
    _configuration.getIsakmpProfiles().put(_currentIsakmpProfile.getName(), _currentIsakmpProfile);
    _currentIsakmpProfile = null;
  }

  private MatchSemantics toMatchSemantics(Match_semanticsContext ctx) {
    if (ctx.MATCH_ALL() != null) {
      return MatchSemantics.MATCH_ALL;
    } else if (ctx.MATCH_ANY() != null) {
      return MatchSemantics.MATCH_ANY;
    } else {
      throw convError(MatchSemantics.class, ctx);
    }
  }

  @Override
  public void exitCd_match_address(Cd_match_addressContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _currentCryptoMapEntry.setAccessList(name);
    _configuration.referenceStructure(IP_ACCESS_LIST, name, CRYPTO_DYNAMIC_MAP_ACL, line);
  }

  @Override
  public void exitCd_set_isakmp_profile(Cd_set_isakmp_profileContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _currentCryptoMapEntry.setIsakmpProfile(name);
    _configuration.referenceStructure(
        ISAKMP_PROFILE, name, CRYPTO_DYNAMIC_MAP_ISAKMP_PROFILE, line);
  }

  @Override
  public void exitCd_set_peer(Cd_set_peerContext ctx) {
    _currentCryptoMapEntry.setPeer(toIp(ctx.address));
  }

  @Override
  public void exitCd_set_pfs(Cd_set_pfsContext ctx) {
    _currentCryptoMapEntry.setPfsKeyGroup(toDhGroup(ctx.dh_group()));
  }

  @Override
  public void exitCd_set_transform_set(Cd_set_transform_setContext ctx) {
    for (VariableContext transform : ctx.transforms) {
      int line = transform.getStart().getLine();
      String name = transform.getText();
      _currentCryptoMapEntry.getTransforms().add(name);
      _configuration.referenceStructure(
          IPSEC_TRANSFORM_SET, name, CRYPTO_DYNAMIC_MAP_TRANSFORM_SET, line);
    }
  }

  @Override
  public void exitCm_match(Cm_matchContext ctx) {
    if (ctx.NOT() != null) {
      // TODO: https://github.com/batfish/batfish/issues/1835
      todo(ctx);
    }
  }

  @Override
  public void exitCmm_access_group(Cmm_access_groupContext ctx) {
    String name;
    int line;
    if (ctx.name != null) {
      name = ctx.name.getText();
      line = ctx.name.getStart().getLine();
    } else {
      name = ctx.num.getText();
      line = ctx.num.getStart().getLine();
    }
    _configuration.referenceStructure(ACCESS_LIST, name, CLASS_MAP_ACCESS_GROUP, line);
  }

  @Override
  public void exitCmm_access_list(Cmm_access_listContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _configuration.referenceStructure(ACCESS_LIST, name, CLASS_MAP_ACCESS_LIST, line);
  }

  @Override
  public void exitCmm_activated_service_template(Cmm_activated_service_templateContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _configuration.referenceStructure(
        SERVICE_TEMPLATE, name, CLASS_MAP_ACTIVATED_SERVICE_TEMPLATE, line);
  }

  @Override
  public void exitCmm_service_template(Cmm_service_templateContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _configuration.referenceStructure(SERVICE_TEMPLATE, name, CLASS_MAP_SERVICE_TEMPLATE, line);
  }

  @Override
  public void exitContinue_rm_stanza(Continue_rm_stanzaContext ctx) {
    Integer target = null;
    if (ctx.dec() != null) {
      target = toInteger(ctx.dec());
      if (_currentRouteMapClause.getSeqNum() >= target) {
        warn(
            ctx,
            String.format(
                "Route-map %s entry %d: continue %d introduces a loop",
                _currentRouteMap.getName(), _currentRouteMapClause.getSeqNum(), target));
        return;
      }
      String targetName = computeRouteMapEntryName(_currentRouteMap.getName(), target);
      _configuration.referenceStructure(
          ROUTE_MAP_ENTRY, targetName, ROUTE_MAP_CONTINUE, ctx.getStart().getLine());
    }
    RouteMapContinue continueLine = new RouteMapContinue(target);
    _currentRouteMapClause.setContinueLine(continueLine);
  }

  @Override
  public void exitCopsl_access_list(Copsl_access_listContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.getStart().getLine();
    _configuration.referenceStructure(IP_ACCESS_LIST, name, COPS_LISTENER_ACCESS_LIST, line);
  }

  @Override
  public void exitCp_ip_access_group(Cp_ip_access_groupContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _configuration.referenceStructure(IPV4_ACCESS_LIST, name, CONTROL_PLANE_ACCESS_GROUP, line);
  }

  @Override
  public void exitCp_service_policy(Cp_service_policyContext ctx) {
    AristaStructureUsage usage =
        ctx.INPUT() != null
            ? CONTROL_PLANE_SERVICE_POLICY_INPUT
            : CONTROL_PLANE_SERVICE_POLICY_OUTPUT;
    _configuration.referenceStructure(
        POLICY_MAP, ctx.name.getText(), usage, ctx.name.getStart().getLine());
  }

  @Override
  public void exitCrypto_keyring(Crypto_keyringContext ctx) {
    _configuration.getKeyrings().put(_currentKeyring.getName(), _currentKeyring);
    _currentKeyring = null;
  }

  @Override
  public void exitCrypto_map_t_ii_match_address(Crypto_map_t_ii_match_addressContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _currentCryptoMapEntry.setAccessList(name);
    _configuration.referenceStructure(IP_ACCESS_LIST, name, CRYPTO_MAP_IPSEC_ISAKMP_ACL, line);
  }

  @Override
  public void exitCrypto_map_t_ii_set_isakmp_profile(
      Crypto_map_t_ii_set_isakmp_profileContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _currentCryptoMapEntry.setIsakmpProfile(name);
    _configuration.referenceStructure(
        ISAKMP_PROFILE, name, CRYPTO_MAP_IPSEC_ISAKMP_ISAKMP_PROFILE, line);
  }

  @Override
  public void exitCrypto_map_t_ii_set_peer(Crypto_map_t_ii_set_peerContext ctx) {
    _currentCryptoMapEntry.setPeer(toIp(ctx.address));
  }

  @Override
  public void exitCrypto_map_t_ii_set_pfs(Crypto_map_t_ii_set_pfsContext ctx) {
    _currentCryptoMapEntry.setPfsKeyGroup(toDhGroup(ctx.dh_group()));
  }

  @Override
  public void exitCrypto_map_t_ii_set_transform_set(Crypto_map_t_ii_set_transform_setContext ctx) {
    for (VariableContext transform : ctx.transforms) {
      int line = transform.getStart().getLine();
      String name = transform.getText();
      _currentCryptoMapEntry.getTransforms().add(name);
      _configuration.referenceStructure(
          IPSEC_TRANSFORM_SET, name, CRYPTO_MAP_IPSEC_ISAKMP_TRANSFORM_SET, line);
    }
  }

  @Override
  public void exitCrypto_map_t_ipsec_isakmp(Crypto_map_t_ipsec_isakmpContext ctx) {
    _currentCryptoMapName = null;
    _currentCryptoMapSequenceNum = null;
  }

  @Override
  public void exitDistribute_list_is_stanza(Distribute_list_is_stanzaContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.getStart().getLine();
    _configuration.referenceStructure(IP_ACCESS_LIST, name, ROUTER_ISIS_DISTRIBUTE_LIST_ACL, line);
  }

  @Override
  public void exitDomain_lookup(Domain_lookupContext ctx) {
    if (ctx.iname != null) {
      String ifaceName = getCanonicalInterfaceName(ctx.iname.getText());
      _configuration.referenceStructure(
          INTERFACE, ifaceName, DOMAIN_LOOKUP_SOURCE_INTERFACE, ctx.iname.getStart().getLine());
      _configuration.setDnsSourceInterface(ifaceName);
    }
  }

  @Override
  public void exitDomain_name(Domain_nameContext ctx) {
    String domainName = ctx.hostname.getText();
    _configuration.setDomainName(domainName);
  }

  @Override
  public void exitDomain_name_server(Domain_name_serverContext ctx) {
    Set<String> dnsServers = _configuration.getDnsServers();
    String hostname = ctx.hostname.getText();
    dnsServers.add(hostname);
  }

  @Override
  public void exitEnable_secret(Enable_secretContext ctx) {
    String password;
    if (ctx.double_quoted_string() != null) {
      password = unquote(ctx.double_quoted_string().getText());
    } else {
      password = ctx.pass.getText() + CommonUtil.salt();
    }
    String passwordRehash = CommonUtil.sha256Digest(password);
    _configuration.getCf().setEnableSecret(passwordRehash);
  }

  @Override
  public void exitExtended_access_list_tail(Extended_access_list_tailContext ctx) {
    LineAction action = toLineAction(ctx.ala);
    AccessListAddressSpecifier srcAddressSpecifier = toAccessListAddressSpecifier(ctx.srcipr);
    AccessListAddressSpecifier dstAddressSpecifier = toAccessListAddressSpecifier(ctx.dstipr);
    AccessListServiceSpecifier serviceSpecifier = computeExtendedAccessListServiceSpecifier(ctx);
    String name = getFullText(ctx).trim();
    ExtendedAccessListLine line =
        ExtendedAccessListLine.builder()
            .setAction(action)
            .setDstAddressSpecifier(dstAddressSpecifier)
            .setName(name)
            .setServiceSpecifier(serviceSpecifier)
            .setSrcAddressSpecifier(srcAddressSpecifier)
            .build();
    _currentExtendedAcl.addLine(line);

    // structure definition tracking
    String structName = aclLineStructureName(_currentExtendedAcl.getName(), name);
    int configLine = ctx.start.getLine();
    _configuration.defineSingleLineStructure(
        IPV4_ACCESS_LIST_EXTENDED_LINE, structName, configLine);
    _configuration.referenceStructure(
        IPV4_ACCESS_LIST_EXTENDED_LINE,
        structName,
        IPV4_ACCESS_LIST_EXTENDED_LINE_SELF_REFERENCE,
        configLine);
  }

  private AccessListServiceSpecifier computeExtendedAccessListServiceSpecifier(
      Extended_access_list_tailContext ctx) {
    if (ctx.prot != null) {
      @Nullable IpProtocol protocol = toIpProtocol(ctx.prot);
      List<SubRange> srcPortRanges =
          ctx.alps_src != null ? toPortRanges(ctx.alps_src) : Collections.emptyList();
      List<SubRange> dstPortRanges =
          ctx.alps_dst != null ? toPortRanges(ctx.alps_dst) : Collections.emptyList();
      Integer icmpType = null;
      Integer icmpCode = null;
      List<TcpFlagsMatchConditions> tcpFlags = new ArrayList<>();
      Set<Integer> dscps = new TreeSet<>();
      Set<Integer> ecns = new TreeSet<>();
      for (Extended_access_list_additional_featureContext feature : ctx.features) {
        if (feature.ACK() != null) {
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setAck(true).build())
                  .setUseAck(true)
                  .build());
        } else if (feature.ADMINISTRATIVELY_PROHIBITED() != null) {
          icmpType = IcmpCode.COMMUNICATION_ADMINISTRATIVELY_PROHIBITED.getType();
          icmpCode = IcmpCode.COMMUNICATION_ADMINISTRATIVELY_PROHIBITED.getCode();
        } else if (feature.ALTERNATE_ADDRESS() != null) {
          icmpType = IcmpType.ALTERNATE_ADDRESS;
        } else if (feature.CWR() != null) {
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setCwr(true).build())
                  .setUseCwr(true)
                  .build());
        } else if (feature.DOD_HOST_PROHIBITED() != null) {
          icmpType = IcmpCode.DESTINATION_HOST_PROHIBITED.getType();
          icmpCode = IcmpCode.DESTINATION_HOST_PROHIBITED.getCode();
        } else if (feature.DOD_NET_PROHIBITED() != null) {
          icmpType = IcmpCode.DESTINATION_NETWORK_PROHIBITED.getType();
          icmpCode = IcmpCode.DESTINATION_NETWORK_PROHIBITED.getCode();
        } else if (feature.DSCP() != null) {
          int dscpType = toDscpType(feature.dscp_type());
          dscps.add(dscpType);
        } else if (feature.ECE() != null) {
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setEce(true).build())
                  .setUseEce(true)
                  .build());
        } else if (feature.ECHO_REPLY() != null) {
          icmpType = IcmpType.ECHO_REPLY;
        } else if (feature.ECHO() != null) {
          icmpType = IcmpType.ECHO_REQUEST;
        } else if (feature.ECN() != null) {
          int ecn = toInteger(feature.ecn);
          ecns.add(ecn);
        } else if (feature.ESTABLISHED() != null) {
          // must contain ACK or RST
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setAck(true).build())
                  .setUseAck(true)
                  .build());
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setRst(true).build())
                  .setUseRst(true)
                  .build());
        } else if (feature.FIN() != null) {
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setFin(true).build())
                  .setUseFin(true)
                  .build());
        } else if (feature.GENERAL_PARAMETER_PROBLEM() != null) {
          icmpType = IcmpCode.INVALID_IP_HEADER.getType();
          icmpCode = IcmpCode.INVALID_IP_HEADER.getCode();
        } else if (feature.HOST_ISOLATED() != null) {
          icmpType = IcmpCode.SOURCE_HOST_ISOLATED.getType();
          icmpCode = IcmpCode.SOURCE_HOST_ISOLATED.getCode();
        } else if (feature.HOST_PRECEDENCE_UNREACHABLE() != null) {
          icmpType = IcmpCode.HOST_PRECEDENCE_VIOLATION.getType();
          icmpCode = IcmpCode.HOST_PRECEDENCE_VIOLATION.getCode();
        } else if (feature.HOST_REDIRECT() != null) {
          icmpType = IcmpCode.HOST_ERROR.getType();
          icmpCode = IcmpCode.HOST_ERROR.getCode();
        } else if (feature.HOST_TOS_REDIRECT() != null) {
          icmpType = IcmpCode.TOS_AND_HOST_ERROR.getType();
          icmpCode = IcmpCode.TOS_AND_HOST_ERROR.getCode();
        } else if (feature.HOST_TOS_UNREACHABLE() != null) {
          icmpType = IcmpCode.HOST_UNREACHABLE_FOR_TOS.getType();
          icmpCode = IcmpCode.HOST_UNREACHABLE_FOR_TOS.getCode();
        } else if (feature.HOST_UNKNOWN() != null) {
          icmpType = IcmpCode.DESTINATION_HOST_UNKNOWN.getType();
          icmpCode = IcmpCode.DESTINATION_HOST_UNKNOWN.getCode();
        } else if (feature.HOST_UNREACHABLE() != null) {
          icmpType = IcmpCode.HOST_UNREACHABLE.getType();
          icmpCode = IcmpCode.HOST_UNREACHABLE.getCode();
        } else if (feature.INFORMATION_REPLY() != null) {
          icmpType = IcmpType.INFO_REPLY;
        } else if (feature.INFORMATION_REQUEST() != null) {
          icmpType = IcmpType.INFO_REQUEST;
        } else if (feature.LOG() != null) {
          // Do nothing.
        } else if (feature.MASK_REPLY() != null) {
          icmpType = IcmpType.MASK_REPLY;
        } else if (feature.MASK_REQUEST() != null) {
          icmpType = IcmpType.MASK_REQUEST;
        } else if (feature.MOBILE_HOST_REDIRECT() != null) {
          icmpType = IcmpType.MOBILE_REDIRECT;
        } else if (feature.NET_REDIRECT() != null) {
          icmpType = IcmpCode.NETWORK_ERROR.getType();
          icmpCode = IcmpCode.NETWORK_ERROR.getCode();
        } else if (feature.NET_TOS_REDIRECT() != null) {
          icmpType = IcmpCode.TOS_AND_NETWORK_ERROR.getType();
          icmpCode = IcmpCode.TOS_AND_NETWORK_ERROR.getCode();
        } else if (feature.NET_TOS_UNREACHABLE() != null) {
          icmpType = IcmpCode.NETWORK_UNREACHABLE_FOR_TOS.getType();
          icmpCode = IcmpCode.NETWORK_UNREACHABLE_FOR_TOS.getCode();
        } else if (feature.NET_UNREACHABLE() != null) {
          icmpType = IcmpCode.NETWORK_UNREACHABLE.getType();
          icmpCode = IcmpCode.NETWORK_UNREACHABLE.getCode();
        } else if (feature.NETWORK_UNKNOWN() != null) {
          icmpType = IcmpCode.DESTINATION_NETWORK_UNKNOWN.getType();
          icmpCode = IcmpCode.DESTINATION_NETWORK_UNKNOWN.getCode();
        } else if (feature.NO_ROOM_FOR_OPTION() != null) {
          icmpType = IcmpCode.BAD_LENGTH.getType();
          icmpCode = IcmpCode.BAD_LENGTH.getCode();
        } else if (feature.OPTION_MISSING() != null) {
          icmpType = IcmpCode.REQUIRED_OPTION_MISSING.getType();
          icmpCode = IcmpCode.REQUIRED_OPTION_MISSING.getCode();
        } else if (feature.PACKET_TOO_BIG() != null) {
          icmpType = IcmpCode.FRAGMENTATION_NEEDED.getType();
          icmpCode = IcmpCode.FRAGMENTATION_NEEDED.getCode();
        } else if (feature.PARAMETER_PROBLEM() != null) {
          icmpType = IcmpType.PARAMETER_PROBLEM;
        } else if (feature.PORT_UNREACHABLE() != null) {
          icmpType = IcmpCode.PORT_UNREACHABLE.getType();
          icmpCode = IcmpCode.PORT_UNREACHABLE.getCode();
        } else if (feature.PSH() != null) {
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setPsh(true).build())
                  .setUsePsh(true)
                  .build());
        } else if (feature.REDIRECT() != null) {
          icmpType = IcmpType.REDIRECT_MESSAGE;
        } else if (feature.ROUTER_ADVERTISEMENT() != null) {
          icmpType = IcmpType.ROUTER_ADVERTISEMENT;
        } else if (feature.ROUTER_SOLICITATION() != null) {
          icmpType = IcmpType.ROUTER_SOLICITATION;
        } else if (feature.RST() != null) {
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setRst(true).build())
                  .setUseRst(true)
                  .build());
        } else if (feature.SOURCE_QUENCH() != null) {
          icmpType = IcmpType.SOURCE_QUENCH;
        } else if (feature.SOURCE_ROUTE_FAILED() != null) {
          icmpType = IcmpCode.SOURCE_ROUTE_FAILED.getType();
          icmpCode = IcmpCode.SOURCE_ROUTE_FAILED.getCode();
        } else if (feature.SYN() != null) {
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setSyn(true).build())
                  .setUseSyn(true)
                  .build());
        } else if (feature.TIME_EXCEEDED() != null) {
          icmpType = IcmpType.TIME_EXCEEDED;
        } else if (feature.TIMESTAMP_REPLY() != null) {
          icmpType = IcmpType.TIMESTAMP_REPLY;
        } else if (feature.TIMESTAMP_REQUEST() != null) {
          icmpType = IcmpType.TIMESTAMP_REQUEST;
        } else if (feature.TRACEROUTE() != null) {
          icmpType = IcmpType.TRACEROUTE;
        } else if (feature.TTL_EXCEEDED() != null) {
          icmpType = IcmpCode.TTL_EQ_ZERO_DURING_TRANSIT.getType();
          icmpCode = IcmpCode.TTL_EQ_ZERO_DURING_TRANSIT.getCode();
        } else if (feature.UNREACHABLE() != null) {
          icmpType = IcmpType.DESTINATION_UNREACHABLE;
        } else if (feature.URG() != null) {
          tcpFlags.add(
              TcpFlagsMatchConditions.builder()
                  .setTcpFlags(TcpFlags.builder().setUrg(true).build())
                  .setUseUrg(true)
                  .build());
        } else if (feature.icmp_message_type != null) {
          icmpType = toInteger(feature.icmp_message_type);
          if (feature.icmp_message_code != null) {
            icmpCode = toInteger(feature.icmp_message_code);
          }
        } else if (feature.TTL() != null) {
          // special case this warning to reduce user confusion
          // "ttl eq 255" is a common pattern to limit traffic to neighboring routers
          warn(ctx, "Batfish does not model packet TTLs");
          return UnimplementedAccessListServiceSpecifier.INSTANCE;
        } else {
          warn(ctx, "Unsupported clause in extended access list: " + getFullText(feature));
          return UnimplementedAccessListServiceSpecifier.INSTANCE;
        }
      }
      return SimpleExtendedAccessListServiceSpecifier.builder()
          .setDscps(dscps)
          .setDstPortRanges(dstPortRanges)
          .setEcns(ecns)
          .setIcmpCode(icmpCode)
          .setIcmpType(icmpType)
          .setProtocol(protocol)
          .setSrcPortRanges(srcPortRanges)
          .setTcpFlags(tcpFlags)
          .build();
    } else {
      return convProblem(
          AccessListServiceSpecifier.class, ctx, UnimplementedAccessListServiceSpecifier.INSTANCE);
    }
  }

  private @Nonnull AccessListAddressSpecifier toAccessListAddressSpecifier(
      Access_list_ip_rangeContext ctx) {
    if (ctx.ip != null) {
      if (ctx.wildcard != null) {
        // IP and mask
        Ip wildcard = toIp(ctx.wildcard);
        return new WildcardAddressSpecifier(IpWildcard.ipWithWildcardMask(toIp(ctx.ip), wildcard));
      } else {
        // Just IP. Same as if 'host' was specified
        return new WildcardAddressSpecifier(IpWildcard.create(toIp(ctx.ip)));
      }
    } else if (ctx.ANY() != null) {
      return AnyAddressSpecifier.INSTANCE;
    } else if (ctx.prefix != null) {
      return new WildcardAddressSpecifier(IpWildcard.create(Prefix.parse(ctx.prefix.getText())));
    } else {
      throw convError(AccessListAddressSpecifier.class, ctx);
    }
  }

  @Override
  public void exitExtended_ipv6_access_list_stanza(Extended_ipv6_access_list_stanzaContext ctx) {
    _currentExtendedIpv6Acl = null;
  }

  @Override
  public void exitExtended_ipv6_access_list_tail(Extended_ipv6_access_list_tailContext ctx) {
    LineAction action = toLineAction(ctx.ala);
    IpProtocol protocol = toIpProtocol(ctx.prot);
    Ip6Wildcard srcIps = getIpWildcard(ctx.srcipr);
    Ip6Wildcard dstIps = getIpWildcard(ctx.dstipr);
    List<SubRange> srcPortRanges =
        ctx.alps_src != null ? toPortRanges(ctx.alps_src) : Collections.emptyList();
    List<SubRange> dstPortRanges =
        ctx.alps_dst != null ? toPortRanges(ctx.alps_dst) : Collections.emptyList();
    Integer icmpType = null;
    Integer icmpCode = null;
    List<TcpFlagsMatchConditions> tcpFlags = new ArrayList<>();
    Set<Integer> dscps = new TreeSet<>();
    Set<Integer> ecns = new TreeSet<>();
    for (Extended_access_list_additional_featureContext feature : ctx.features) {
      if (feature.ACK() != null) {
        tcpFlags.add(
            TcpFlagsMatchConditions.builder()
                .setTcpFlags(TcpFlags.builder().setAck(true).build())
                .setUseAck(true)
                .build());
      } else if (feature.DSCP() != null) {
        int dscpType = toDscpType(feature.dscp_type());
        dscps.add(dscpType);
      } else if (feature.ECE() != null) {
        tcpFlags.add(
            TcpFlagsMatchConditions.builder()
                .setTcpFlags(TcpFlags.builder().setEce(true).build())
                .setUseEce(true)
                .build());
      } else if (feature.ECHO_REPLY() != null) {
        icmpType = IcmpCode.ECHO_REPLY.getType();
        icmpCode = IcmpCode.ECHO_REPLY.getCode();
      } else if (feature.ECHO() != null) {
        icmpType = IcmpCode.ECHO_REQUEST.getType();
        icmpCode = IcmpCode.ECHO_REQUEST.getCode();
      } else if (feature.ECN() != null) {
        int ecn = toInteger(feature.ecn);
        ecns.add(ecn);
      } else if (feature.ESTABLISHED() != null) {
        // must contain ACK or RST
        tcpFlags.add(
            TcpFlagsMatchConditions.builder()
                .setTcpFlags(TcpFlags.builder().setAck(true).build())
                .setUseAck(true)
                .build());
        tcpFlags.add(
            TcpFlagsMatchConditions.builder()
                .setTcpFlags(TcpFlags.builder().setRst(true).build())
                .setUseRst(true)
                .build());
      } else if (feature.FIN() != null) {
        tcpFlags.add(
            TcpFlagsMatchConditions.builder()
                .setTcpFlags(TcpFlags.builder().setFin(true).build())
                .setUseFin(true)
                .build());
      } else if (feature.HOST_UNKNOWN() != null) {
        icmpType = IcmpCode.DESTINATION_HOST_UNKNOWN.getType();
        icmpCode = IcmpCode.DESTINATION_HOST_UNKNOWN.getCode();
      } else if (feature.HOST_UNREACHABLE() != null) {
        icmpType = IcmpCode.HOST_UNREACHABLE.getType();
        icmpCode = IcmpCode.HOST_UNREACHABLE.getCode();
      } else if (feature.LOG() != null) {
        // Do nothing.
      } else if (feature.NETWORK_UNKNOWN() != null) {
        icmpType = IcmpCode.DESTINATION_NETWORK_UNKNOWN.getType();
        icmpCode = IcmpCode.DESTINATION_NETWORK_UNKNOWN.getCode();
      } else if (feature.NET_UNREACHABLE() != null) {
        icmpType = IcmpCode.NETWORK_UNREACHABLE.getType();
        icmpCode = IcmpCode.NETWORK_UNREACHABLE.getCode();
      } else if (feature.PARAMETER_PROBLEM() != null) {
        icmpType = IcmpType.PARAMETER_PROBLEM;
      } else if (feature.PORT_UNREACHABLE() != null) {
        icmpType = IcmpCode.PORT_UNREACHABLE.getType();
        icmpCode = IcmpCode.PORT_UNREACHABLE.getCode();
      } else if (feature.PSH() != null) {
        tcpFlags.add(
            TcpFlagsMatchConditions.builder()
                .setTcpFlags(TcpFlags.builder().setPsh(true).build())
                .setUsePsh(true)
                .build());
      } else if (feature.REDIRECT() != null) {
        icmpType = IcmpType.REDIRECT_MESSAGE;
      } else if (feature.RST() != null) {
        tcpFlags.add(
            TcpFlagsMatchConditions.builder()
                .setTcpFlags(TcpFlags.builder().setRst(true).build())
                .setUseRst(true)
                .build());
      } else if (feature.SOURCE_QUENCH() != null) {
        icmpType = IcmpCode.SOURCE_QUENCH.getType();
        icmpCode = IcmpCode.SOURCE_QUENCH.getCode();
      } else if (feature.SYN() != null) {
        tcpFlags.add(
            TcpFlagsMatchConditions.builder()
                .setTcpFlags(TcpFlags.builder().setSyn(true).build())
                .setUseSyn(true)
                .build());
      } else if (feature.TIME_EXCEEDED() != null) {
        icmpType = IcmpType.TIME_EXCEEDED;
      } else if (feature.TTL_EXCEEDED() != null) {
        icmpType = IcmpType.TIME_EXCEEDED;
      } else if (feature.TRACEROUTE() != null) {
        icmpType = IcmpType.TRACEROUTE;
      } else if (feature.UNREACHABLE() != null) {
        icmpType = IcmpType.DESTINATION_UNREACHABLE;
      } else if (feature.URG() != null) {
        tcpFlags.add(
            TcpFlagsMatchConditions.builder()
                .setTcpFlags(TcpFlags.builder().setUrg(true).build())
                .setUseUrg(true)
                .build());
      } else {
        // warn(ctx, "Unsupported clause in IPv6 extended access list: " + getFullText(feature));
      }
    }
    String name = getFullText(ctx).trim();
    ExtendedIpv6AccessListLine line =
        new ExtendedIpv6AccessListLine(
            name,
            action,
            protocol,
            srcIps,
            dstIps,
            srcPortRanges,
            dstPortRanges,
            dscps,
            ecns,
            icmpType,
            icmpCode,
            tcpFlags);
    _currentExtendedIpv6Acl.addLine(line);
  }

  @Override
  public void exitIf_autostate(If_autostateContext ctx) {
    _currentInterfaces.forEach(currentInterface -> currentInterface.setAutoState(true));
  }

  @Override
  public void exitIf_bandwidth(If_bandwidthContext ctx) {
    double newBandwidthBps = toLong(ctx.bw) * 1000.0D;
    _currentInterfaces.forEach(i -> i.setBandwidth(newBandwidthBps));
  }

  @Override
  public void exitIf_bfd_template(If_bfd_templateContext ctx) {
    _configuration.referenceStructure(
        BFD_TEMPLATE, ctx.name.getText(), INTERFACE_BFD_TEMPLATE, ctx.name.getStart().getLine());
  }

  @Override
  public void exitIfcg_num_eos(Ifcg_num_eosContext ctx) {
    int num = toInteger(ctx.num);
    String name = computeAggregatedInterfaceName(num);
    _currentInterfaces.forEach(i -> i.setChannelGroup(name));
  }

  @Override
  public void exitIfcg_recirculation_eos(Ifcg_recirculation_eosContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitIf_crypto_map(If_crypto_mapContext ctx) {
    _currentInterfaces.forEach(i -> i.setCryptoMap(ctx.name.getText()));
  }

  @Override
  public void exitIf_encapsulation_dot1q_eos(If_encapsulation_dot1q_eosContext ctx) {
    int vlanId = toInteger(ctx.id);
    _currentInterfaces.forEach(i -> i.setEncapsulationVlan(vlanId));
  }

  private @Nullable String computeAggregatedInterfaceName(int num) {
    return String.format("Port-Channel%d", num);
  }

  @Override
  public void exitIfip_access_group_eos(Ifip_access_group_eosContext ctx) {
    String name = ctx.name.getText();
    AristaStructureUsage usage;
    if (ctx.IN() != null) {
      usage = INTERFACE_IP_ACCESS_GROUP_IN;
      _currentInterfaces.forEach(currentInterface -> currentInterface.setIncomingFilter(name));
    } else {
      assert ctx.OUT() != null;
      usage = INTERFACE_IP_ACCESS_GROUP_OUT;
      _currentInterfaces.forEach(currentInterface -> currentInterface.setOutgoingFilter(name));
    }
    _configuration.referenceStructure(IPV4_ACCESS_LIST, name, usage, ctx.name.getStart().getLine());
  }

  @Override
  public void exitIfip_address_address_eos(Ifip_address_address_eosContext ctx) {
    ConcreteInterfaceAddress addr = toAddress(ctx.addr);
    _currentInterfaces.forEach(
        i -> {
          if (i.getSwitchport()) {
            warn(ctx, String.format("Ignoring IP address for switchport %s", i.getName()));
            return;
          }
          if (ctx.SECONDARY() != null) {
            i.getSecondaryAddresses().add(addr);
          } else {
            i.setAddress(addr);
          }
        });
  }

  @Override
  public void exitIfip_address_virtual_eos(Ifip_address_virtual_eosContext ctx) {
    // TODO: this should be handled differently, since virtual is present.
    ConcreteInterfaceAddress addr = toAddress(ctx.addr);
    _currentInterfaces.forEach(
        i -> {
          if (i.getSwitchport()) {
            warn(ctx, String.format("Ignoring IP address for switchport %s", i.getName()));
            return;
          }
          if (ctx.SECONDARY() != null) {
            i.getSecondaryAddresses().add(addr);
          } else {
            i.setAddress(addr);
          }
        });
  }

  @Override
  public void exitIf_ip_local_proxy_arp_eos(If_ip_local_proxy_arp_eosContext ctx) {
    todo(ctx);
    _currentInterfaces.forEach(i -> i.setLocalProxyArp(true));
  }

  @Override
  public void exitIfipo_area_eos(Ifipo_area_eosContext ctx) {
    long area = toLong(ctx.area);
    for (Interface iface : _currentInterfaces) {
      iface.setOspfArea(area);
    }
  }

  @Override
  public void exitIfipo_cost_eos(Ifipo_cost_eosContext ctx) {
    int cost = toInteger(ctx.cost);
    _currentInterfaces.forEach(i -> i.setOspfCost(cost));
  }

  @Override
  public void exitIfipo_dead_interval_eos(Ifipo_dead_interval_eosContext ctx) {
    int seconds = toInteger(ctx.seconds);
    for (Interface currentInterface : _currentInterfaces) {
      currentInterface.setOspfDeadInterval(seconds);
      currentInterface.setOspfHelloMultiplier(0);
    }
  }

  @Override
  public void exitIfipo_hello_interval_eos(Ifipo_hello_interval_eosContext ctx) {
    int seconds = toInteger(ctx.seconds);
    _currentInterfaces.forEach(i -> i.setOspfHelloInterval(seconds));
  }

  @Override
  public void exitIfipo_network_eos(Ifipo_network_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setOspfNetworkType(OspfNetworkType.POINT_TO_POINT));
  }

  @Override
  public void exitIfipp_neighbor_filter_eos(Ifipp_neighbor_filter_eosContext ctx) {
    String acl = ctx.acl.getText();
    int line = ctx.acl.getStart().getLine();
    _configuration.referenceStructure(IPV4_ACCESS_LIST, acl, INTERFACE_PIM_NEIGHBOR_FILTER, line);
  }

  @Override
  public void exitIfip_proxy_arp_eos(Ifip_proxy_arp_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setProxyArp(true));
  }

  @Override
  public void exitIf_ip_helper_address(If_ip_helper_addressContext ctx) {
    for (Interface iface : _currentInterfaces) {
      Ip dhcpRelayAddress = toIp(ctx.address);
      iface.getDhcpRelayAddresses().add(dhcpRelayAddress);
    }
  }

  @Override
  public void exitIf_ip_inband_access_group(If_ip_inband_access_groupContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.getStart().getLine();
    _configuration.referenceStructure(IP_ACCESS_LIST, name, INTERFACE_IP_INBAND_ACCESS_GROUP, line);
  }

  @Override
  public void exitIfipm_boundary_eos(Ifipm_boundary_eosContext ctx) {
    if (ctx.name != null) {
      String name = ctx.name.getText();
      _configuration.referenceStructure(
          IP_ACCESS_LIST_STANDARD, name, INTERFACE_IP_MULTICAST_BOUNDARY, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitIfipnd_static(Ifipnd_staticContext ctx) {
    if ((ctx.original_port == null) != (ctx.tx_port == null)) {
      warn(ctx, "For port translation, must specify both original and translated ports.");
      return;
    }
    Optional<Integer> originalPort =
        Optional.ofNullable(ctx.original_port).flatMap(p -> toPort(ctx, p));
    Optional<Integer> translatedPort =
        Optional.ofNullable(ctx.tx_port).flatMap(p -> toPort(ctx, p));
    if (ctx.original_port != null && (!originalPort.isPresent() || !translatedPort.isPresent())) {
      // already warned.
      return;
    }
    NatProtocol protocol =
        ctx.UDP() != null ? NatProtocol.UDP : ctx.TCP() != null ? NatProtocol.TCP : NatProtocol.ANY;
    String aclName;
    if (ctx.acl != null) {
      aclName = ctx.acl.getText();
      _configuration.referenceStructure(
          IPV4_ACCESS_LIST, aclName, IP_NAT_DESTINATION_ACCESS_LIST, ctx.acl.getStart().getLine());
      warn(ctx, "NAT based on access-list is not supported, all packets will be translated.");
    } else {
      aclName = null;
    }
    AristaDestinationStaticNat nat =
        new AristaDestinationStaticNat(
            toIp(ctx.original_ip),
            originalPort.orElse(null),
            toIp(ctx.tx_ip),
            translatedPort.orElse(null),
            aclName,
            protocol);
    for (Interface iface : _currentInterfaces) {
      iface.addDestinationStaticNat(nat);
    }
  }

  @Override
  public void exitIfipns_dynamic(Ifipns_dynamicContext ctx) {
    String acl = null;
    String pool = null;
    if (ctx.acl != null) {
      acl = ctx.acl.getText();
      int aclLine = ctx.acl.getStart().getLine();
      _configuration.referenceStructure(IPV4_ACCESS_LIST, acl, IP_NAT_SOURCE_ACCESS_LIST, aclLine);
    }
    if (ctx.pool != null) {
      pool = ctx.pool.getText();
      int poolLine = ctx.pool.getStart().getLine();
      _configuration.referenceStructure(NAT_POOL, pool, IP_NAT_SOURCE_POOL, poolLine);
    }
    boolean overload = ctx.OVERLOAD() != null;

    if (acl == null) {
      // incomplete definition. ignore
      warn(ctx, "Arista dynamic source nat missing required ACL.");
      return;
    }
    if (pool == null && !overload) {
      // incomplete definition. ignore
      warn(ctx, "Arista dynamic source nat must have a pool or be overload.");
      return;
    }

    AristaDynamicSourceNat nat = new AristaDynamicSourceNat(acl, pool, overload);

    for (Interface iface : _currentInterfaces) {
      if (iface.getDynamicSourceNats() == null) {
        iface.setDynamicSourceNats(new ArrayList<>(1));
      }
      iface.getDynamicSourceNats().add(nat);
    }
  }

  @Override
  public void exitIfipns_static(Ifipns_staticContext ctx) {
    if ((ctx.original_port == null) != (ctx.tx_port == null)) {
      warn(ctx, "For port translation, must specify both original and translated ports.");
      return;
    }
    Optional<Integer> originalPort =
        Optional.ofNullable(ctx.original_port).flatMap(p -> toPort(ctx, p));
    Optional<Integer> translatedPort =
        Optional.ofNullable(ctx.tx_port).flatMap(p -> toPort(ctx, p));
    if (ctx.original_port != null && (!originalPort.isPresent() || !translatedPort.isPresent())) {
      // already warned.
      return;
    }
    NatProtocol protocol =
        ctx.UDP() != null ? NatProtocol.UDP : ctx.TCP() != null ? NatProtocol.TCP : NatProtocol.ANY;
    String aclName;
    if (ctx.acl != null) {
      aclName = ctx.acl.getText();
      // Standard ACLs cannot be used to express filtering on destination IPs.
      _configuration.referenceStructure(
          IPV4_ACCESS_LIST_EXTENDED,
          aclName,
          IP_NAT_SOURCE_STATIC_ACCESS_LIST,
          ctx.acl.getStart().getLine());
    } else {
      aclName = null;
    }
    AristaStaticSourceNat nat =
        new AristaStaticSourceNat(
            toIp(ctx.original_ip),
            originalPort.orElse(null),
            toIp(ctx.tx_ip),
            translatedPort.orElse(null),
            aclName,
            protocol);
    for (Interface iface : _currentInterfaces) {
      if (iface.getStaticSourceNats() == null) {
        iface.setStaticSourceNats(new ArrayList<>(1));
      }
      iface.getStaticSourceNats().add(nat);
    }
  }

  @Override
  public void exitIf_ipv6_traffic_filter(If_ipv6_traffic_filterContext ctx) {
    AristaStructureUsage usage =
        ctx.IN() != null ? INTERFACE_IPV6_TRAFFIC_FILTER_IN : INTERFACE_IPV6_TRAFFIC_FILTER_OUT;
    _configuration.referenceStructure(
        IPV6_ACCESS_LIST, ctx.acl.getText(), usage, ctx.acl.getStart().getLine());
  }

  @Override
  public void exitIfmac_access_group(Ifmac_access_groupContext ctx) {
    String name = ctx.name.getText();
    AristaStructureUsage usage;
    if (ctx.IN() != null) {
      usage = INTERFACE_MAC_ACCESS_GROUP_IN;
    } else {
      assert ctx.OUT() != null;
      usage = INTERFACE_MAC_ACCESS_GROUP_OUT;
    }
    _configuration.referenceStructure(MAC_ACCESS_LIST, name, usage, ctx.name.getStart().getLine());
  }

  @Override
  public void exitIf_isis_metric(If_isis_metricContext ctx) {
    long metric = toLong(ctx.metric);
    for (Interface iface : _currentInterfaces) {
      iface.setIsisCost(metric);
    }
  }

  @Override
  public void exitIf_eos_mlag(If_eos_mlagContext ctx) {
    int mlagId = toInteger(ctx.dec());
    _currentInterfaces.forEach(iface -> iface.setMlagId(mlagId));
  }

  @Override
  public void exitIf_mtu(If_mtuContext ctx) {
    int mtu = toInteger(ctx.dec());
    for (Interface currentInterface : _currentInterfaces) {
      currentInterface.setMtu(mtu);
    }
  }

  @Override
  public void exitIf_member_interface(If_member_interfaceContext ctx) {
    if (_currentInterfaces.size() != 1) {
      warn(ctx, "member-interfaces can only be configured in single-interface context");
      return;
    }
    String member = toInterfaceName(ctx.name);
    _configuration.referenceStructure(
        INTERFACE,
        member,
        AristaStructureUsage.INTERFACE_MEMBER_INTERFACE,
        ctx.name.getStart().getLine());
    _currentInterfaces.get(0).getMemberInterfaces().add(member);
  }

  @Override
  public void exitIf_no_autostate(If_no_autostateContext ctx) {
    _currentInterfaces.forEach(currentInterface -> currentInterface.setAutoState(false));
  }

  @Override
  public void exitIf_no_bandwidth(If_no_bandwidthContext ctx) {
    _currentInterfaces.forEach(i -> i.setBandwidth(null));
  }

  @Override
  public void exitIf_no_channel_group_eos(If_no_channel_group_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setChannelGroup(null));
  }

  @Override
  public void exitIf_no_description_eos(If_no_description_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setDescription(null));
  }

  @Override
  public void exitIf_no_ip_local_proxy_arp_eos(If_no_ip_local_proxy_arp_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setLocalProxyArp(false));
  }

  @Override
  public void exitIf_no_ip_proxy_arp_eos(If_no_ip_proxy_arp_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setProxyArp(false));
  }

  @Override
  public void exitIf_no_shutdown_eos(If_no_shutdown_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setShutdown(false));
  }

  @Override
  public void exitIf_no_speed_eos(If_no_speed_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setSpeed(null));
  }

  @Override
  public void exitIf_no_st_portfast(If_no_st_portfastContext ctx) {
    _currentInterfaces.forEach(i -> i.setSpanningTreePortfast(false));
  }

  @Override
  public void exitIf_no_switchport_switchport_eos(If_no_switchport_switchport_eosContext ctx) {
    _currentInterfaces.forEach(
        i -> {
          i.setSwitchport(false);
          i.setSwitchportMode(SwitchportMode.NONE);
        });
  }

  @Override
  public void exitIf_noswpt_allowed_eos(If_noswpt_allowed_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setAllowedVlans(null));
  }

  @Override
  public void exitIf_noswpt_group_eos(If_noswpt_group_eosContext ctx) {
    if (ctx.name == null) {
      _currentInterfaces.forEach(Interface::clearVlanTrunkGroups);
    } else {
      String groupName = ctx.name.getText();
      _currentInterfaces.forEach(i -> i.removeVlanTrunkGroup(groupName));
    }
  }

  @Override
  public void exitIf_service_policy(If_service_policyContext ctx) {
    // TODO: do something with this.
    String mapname = ctx.policy_map.getText();
    _configuration.referenceStructure(
        POLICY_MAP, mapname, INTERFACE_SERVICE_POLICY, ctx.getStart().getLine());
  }

  @Override
  public void exitIf_shutdown_eos(If_shutdown_eosContext ctx) {
    _currentInterfaces.forEach(i -> i.setShutdown(true));
  }

  @Override
  public void exitIf_spanning_tree(If_spanning_treeContext ctx) {
    _no = false;
  }

  @Override
  public void exitIf_speed_auto_eos(If_speed_auto_eosContext ctx) {
    if (ctx.eos_bandwidth_specifier() != null) {
      double speed = toBandwidth(ctx.eos_bandwidth_specifier());
      _currentInterfaces.forEach(i -> i.setSpeed(speed));
    }
  }

  @Override
  public void exitIf_speed_bw_eos(If_speed_bw_eosContext ctx) {
    double speed = toBandwidth(ctx.eos_bandwidth_specifier());
    _currentInterfaces.forEach(i -> i.setSpeed(speed));
  }

  @Override
  public void exitIf_speed_forced_eos(If_speed_forced_eosContext ctx) {
    double speed = toBandwidth(ctx.eos_bandwidth_specifier());
    _currentInterfaces.forEach(i -> i.setSpeed(speed));
  }

  private double toBandwidth(Eos_bandwidth_specifierContext ctx) {
    if (ctx.FORTYG_FULL() != null) {
      return 40E9D;
    } else if (ctx.ONE_HUNDRED_FULL() != null) {
      return 100E6D;
    } else if (ctx.ONE_THOUSAND_FULL() != null) {
      return 1E9D;
    } else if (ctx.ONE_HUNDREDG_FULL() != null) {
      return 100E9D;
    } else if (ctx.TEN_THOUSAND_FULL() != null) {
      return 10E9D;
    } else if (ctx.TWENTY_FIVEG_FULL() != null) {
      return 25E9D;
    } else {
      throw convError(Double.class, ctx);
    }
  }

  @Override
  public void exitIf_st_portfast(If_st_portfastContext ctx) {
    boolean spanningTreePortfast = ctx.disable == null;
    _currentInterfaces.forEach(i -> i.setSpanningTreePortfast(spanningTreePortfast));
  }

  @Override
  public void exitIf_switchport_switchport(If_switchport_switchportContext ctx) {
    for (Interface iface : _currentInterfaces) {
      iface.setSwitchport(true);
      // setting the switch port mode only if it is not already set
      if (iface.getSwitchportMode() == null || iface.getSwitchportMode() == SwitchportMode.NONE) {
        SwitchportMode defaultSwitchportMode = _configuration.getCf().getDefaultSwitchportMode();
        iface.setSwitchportMode(
            (defaultSwitchportMode == SwitchportMode.NONE || defaultSwitchportMode == null)
                ? Interface.getUndeclaredDefaultSwitchportMode()
                : defaultSwitchportMode);
      }
    }
  }

  @Override
  public void exitIf_switchport_access(If_switchport_accessContext ctx) {
    if (ctx.vlan != null) {
      int vlan = toInteger(ctx.vlan);
      for (Interface currentInterface : _currentInterfaces) {
        currentInterface.setSwitchport(true);
        currentInterface.setSwitchportMode(SwitchportMode.ACCESS);
        currentInterface.setAccessVlan(vlan);
      }
    } else {
      for (Interface currentInterface : _currentInterfaces) {
        currentInterface.setSwitchport(true);
        currentInterface.setSwitchportMode(SwitchportMode.ACCESS);
        currentInterface.setSwitchportAccessDynamic(true);
      }
    }
  }

  @Override
  public void exitIf_switchport_mode(If_switchport_modeContext ctx) {
    if (ctx.if_switchport_mode_monitor() != null) {
      // This does not actually change the switchport mode, rather it just
      // configures buffer settings. See, e.g.,
      // https://www.cisco.com/c/en/us/td/docs/switches/datacenter/nexus3000/sw/system_mgmt/503_U5_1/b_3k_System_Mgmt_Config_503_u5_1/b_3k_System_Mgmt_Config_503_u5_1_chapter_010000.html
      return;
    }

    SwitchportMode mode;
    if (ctx.ACCESS() != null) {
      mode = SwitchportMode.ACCESS;
    } else if (ctx.DOT1Q_TUNNEL() != null) {
      mode = SwitchportMode.DOT1Q_TUNNEL;
    } else if (ctx.DYNAMIC() != null && ctx.AUTO() != null) {
      mode = SwitchportMode.DYNAMIC_AUTO;
    } else if (ctx.DYNAMIC() != null && ctx.DESIRABLE() != null) {
      mode = SwitchportMode.DYNAMIC_DESIRABLE;
    } else if (ctx.FEX_FABRIC() != null) {
      mode = SwitchportMode.FEX_FABRIC;
    } else if (ctx.TAP() != null) {
      mode = SwitchportMode.TAP;
    } else if (ctx.TRUNK() != null) {
      mode = SwitchportMode.TRUNK;
    } else if (ctx.TOOL() != null) {
      mode = SwitchportMode.TOOL;
    } else {
      throw new BatfishException("Unhandled switchport mode");
    }
    _currentInterfaces.forEach(iface -> iface.setSwitchport(true));
    _currentInterfaces.forEach(iface -> iface.setSwitchportMode(mode));
  }

  @Override
  public void exitIf_switchport_trunk_allowed_eos(If_switchport_trunk_allowed_eosContext ctx) {
    if (ctx.ALL() != null) {
      _currentInterfaces.forEach(iface -> iface.setAllowedVlans(Interface.ALL_VLANS));
      return;
    } else if (ctx.NONE() != null) {
      _currentInterfaces.forEach(iface -> iface.setAllowedVlans(IntegerSpace.EMPTY));
      return;
    }

    // The result is dependent on the provided vlans.
    IntegerSpace allowed = IntegerSpace.builder().includingAllSubRanges(toRange(ctx.r)).build();
    if (ctx.EXCEPT() != null) {
      IntegerSpace except = Interface.ALL_VLANS.difference(allowed);
      _currentInterfaces.forEach(iface -> iface.setAllowedVlans(except));
      return;
    } else if (ctx.ADD() == null && ctx.REMOVE() == null) {
      _currentInterfaces.forEach(iface -> iface.setAllowedVlans(allowed));
      return;
    }

    // The result is dependent on the already-configured VLANs.
    for (Interface currentInterface : _currentInterfaces) {
      IntegerSpace current = firstNonNull(currentInterface.getAllowedVlans(), Interface.ALL_VLANS);
      if (ctx.ADD() != null) {
        currentInterface.setAllowedVlans(IntegerSpace.unionOf(allowed, current));
      } else {
        assert ctx.REMOVE() != null;
        currentInterface.setAllowedVlans(current.difference(allowed));
      }
    }
  }

  @Override
  public void exitIf_switchport_trunk_group_eos(If_switchport_trunk_group_eosContext ctx) {
    String groupName = ctx.name.getText();
    for (Interface currentInterface : _currentInterfaces) {
      currentInterface.addVlanTrunkGroup(groupName);
    }
  }

  @Override
  public void exitIf_switchport_trunk_native_eos(If_switchport_trunk_native_eosContext ctx) {
    int vlan = toInteger(ctx.vlan);
    for (Interface currentInterface : _currentInterfaces) {
      currentInterface.setNativeVlan(vlan);
    }
  }

  @Override
  public void exitIf_vrf_name(If_vrf_nameContext ctx) {
    String name = ctx.name.getText();
    for (Interface currentInterface : _currentInterfaces) {
      currentInterface.setVrf(name);
      initVrf(name);
    }
  }

  @Override
  public void exitIf_vrrp(If_vrrpContext ctx) {
    _currentVrrpGroupNum = null;
  }

  @Override
  public void exitIfigmp_access_group(Ifigmp_access_groupContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.getStart().getLine();
    _configuration.referenceStructure(IP_ACCESS_LIST, name, INTERFACE_IGMP_ACCESS_GROUP_ACL, line);
  }

  @Override
  public void exitIfigmphp_access_list(Ifigmphp_access_listContext ctx) {
    _configuration.referenceStructure(
        IP_ACCESS_LIST,
        ctx.name.getText(),
        INTERFACE_IGMP_HOST_PROXY_ACCESS_LIST,
        ctx.getStart().getLine());
  }

  @Override
  public void exitIfigmpsg_acl(Ifigmpsg_aclContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _configuration.referenceStructure(
        IPV4_ACCESS_LIST, name, INTERFACE_IGMP_STATIC_GROUP_ACL, line);
  }

  @Override
  public void exitIftunnel_destination(Iftunnel_destinationContext ctx) {
    Ip destination = toIp(ctx.IP_ADDRESS());
    for (Interface iface : _currentInterfaces) {
      iface.getTunnelInitIfNull().setDestination(destination);
    }
  }

  @Override
  public void exitIftunnel_mode(Iftunnel_modeContext ctx) {
    for (Interface iface : _currentInterfaces) {
      Tunnel tunnel = iface.getTunnelInitIfNull();
      if (ctx.gre_id != null) {
        tunnel.setMode(TunnelMode.GRE_MULTIPOINT);
        todo(ctx);
      } else if (ctx.gre_ipv4 != null) {
        tunnel.setMode(TunnelMode.GRE_MULTIPOINT);
        todo(ctx);
      } else if (ctx.gre_multipoint != null) {
        tunnel.setMode(TunnelMode.GRE_MULTIPOINT);
        todo(ctx);
      } else if (ctx.ipsec_ipv4 != null) {
        tunnel.setMode(TunnelMode.IPSEC_IPV4);
      } else if (ctx.ipv6ip != null) {
        tunnel.setMode(TunnelMode.IPV6_IP);
        todo(ctx);
      } else {
        todo(ctx);
      }
    }
  }

  @Override
  public void exitIftunnel_protection(Iftunnel_protectionContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.getStart().getLine();
    _configuration.referenceStructure(IPSEC_PROFILE, name, TUNNEL_PROTECTION_IPSEC_PROFILE, line);
    for (Interface iface : _currentInterfaces) {
      Tunnel tunnel = iface.getTunnelInitIfNull();
      tunnel.setIpsecProfileName(name);
      tunnel.setMode(TunnelMode.IPSEC_IPV4);
    }
  }

  @Override
  public void exitIftunnel_source(Iftunnel_sourceContext ctx) {
    if (ctx.IP_ADDRESS() != null) {
      Ip sourceAddress = toIp(ctx.IP_ADDRESS());
      for (Interface iface : _currentInterfaces) {
        iface.getTunnelInitIfNull().setSourceAddress(sourceAddress);
      }
    } else if (ctx.iname != null) {
      String sourceInterfaceName = getCanonicalInterfaceName(ctx.iname.getText());
      _configuration.referenceStructure(
          INTERFACE, sourceInterfaceName, TUNNEL_SOURCE, ctx.iname.getStart().getLine());
      for (Interface iface : _currentInterfaces) {
        iface.getTunnelInitIfNull().setSourceInterfaceName(sourceInterfaceName);
      }
    } else if (ctx.DYNAMIC() != null) {
      for (Interface iface : _currentInterfaces) {
        iface.getTunnelInitIfNull().setSourceInterfaceName(null);
      }
    }
  }

  @Override
  public void exitIfvrrp_authentication(Ifvrrp_authenticationContext ctx) {
    String hashedAuthenticationText =
        CommonUtil.sha256Digest(ctx.text.getText() + CommonUtil.salt());
    for (Interface iface : _currentInterfaces) {
      String ifaceName = iface.getName();
      VrrpGroup vrrpGroup =
          _configuration
              .getVrrpGroups()
              .computeIfAbsent(ifaceName, n -> new VrrpInterface())
              .getVrrpGroups()
              .computeIfAbsent(_currentVrrpGroupNum, VrrpGroup::new);
      vrrpGroup.setAuthenticationTextHash(hashedAuthenticationText);
    }
  }

  @Override
  public void exitIfvrrp_ip(Ifvrrp_ipContext ctx) {
    Ip ip = toIp(ctx.ip);
    if (ctx.SECONDARY() != null) {
      todo(ctx); // Batfish VI model does not yet handle secondary VRRP addresses.
      return;
    }
    for (Interface iface : _currentInterfaces) {
      String ifaceName = iface.getName();
      VrrpGroup vrrpGroup =
          _configuration
              .getVrrpGroups()
              .computeIfAbsent(ifaceName, n -> new VrrpInterface())
              .getVrrpGroups()
              .computeIfAbsent(_currentVrrpGroupNum, VrrpGroup::new);
      vrrpGroup.setVirtualAddress(ip);
    }
  }

  @Override
  public void exitIfvrrp_ipv4(Ifvrrp_ipv4Context ctx) {
    Ip ip = toIp(ctx.ip);
    for (Interface iface : _currentInterfaces) {
      String ifaceName = iface.getName();
      VrrpGroup vrrpGroup =
          _configuration
              .getVrrpGroups()
              .computeIfAbsent(ifaceName, n -> new VrrpInterface())
              .getVrrpGroups()
              .computeIfAbsent(_currentVrrpGroupNum, VrrpGroup::new);
      vrrpGroup.setVirtualAddress(ip);
    }
  }

  @Override
  public void exitIfvrrp_preempt(Ifvrrp_preemptContext ctx) {
    for (Interface iface : _currentInterfaces) {
      String ifaceName = iface.getName();
      VrrpGroup vrrpGroup =
          _configuration
              .getVrrpGroups()
              .computeIfAbsent(ifaceName, n -> new VrrpInterface())
              .getVrrpGroups()
              .computeIfAbsent(_currentVrrpGroupNum, VrrpGroup::new);
      vrrpGroup.setPreempt(true);
    }
  }

  @Override
  public void exitIfvrrp_priority(Ifvrrp_priorityContext ctx) {
    int priority = toInteger(ctx.priority);
    for (Interface iface : _currentInterfaces) {
      String ifaceName = iface.getName();
      VrrpGroup vrrpGroup =
          _configuration
              .getVrrpGroups()
              .computeIfAbsent(ifaceName, n -> new VrrpInterface())
              .getVrrpGroups()
              .computeIfAbsent(_currentVrrpGroupNum, VrrpGroup::new);
      vrrpGroup.setPriority(priority);
    }
  }

  @Override
  public void exitIfvrrp_priority_level(Ifvrrp_priority_levelContext ctx) {
    int priority = toInteger(ctx.priority);
    for (Interface iface : _currentInterfaces) {
      String ifaceName = iface.getName();
      VrrpGroup vrrpGroup =
          _configuration
              .getVrrpGroups()
              .computeIfAbsent(ifaceName, n -> new VrrpInterface())
              .getVrrpGroups()
              .computeIfAbsent(_currentVrrpGroupNum, VrrpGroup::new);
      vrrpGroup.setPriority(priority);
    }
  }

  @Override
  public void exitInterface_is_stanza(Interface_is_stanzaContext ctx) {
    _currentIsisInterface = null;
  }

  @Override
  public void exitIpap_access_list(Ipap_access_listContext ctx) {
    String name = ctx.name.getText();
    String regex = ctx.regex.getText().trim();
    LineAction action = toLineAction(ctx.action);
    IpAsPathAccessListLine.OriginType origin = toOriginType(ctx.origin);
    if (origin != IpAsPathAccessListLine.OriginType.ANY) {
      warn(ctx, "Origin type other than ANY is not yet supported");
    }
    IpAsPathAccessListLine line = new IpAsPathAccessListLine(action, regex, origin);
    IpAsPathAccessList list =
        _configuration.getAsPathAccessLists().computeIfAbsent(name, IpAsPathAccessList::new);
    list.addLine(line);
    _configuration.defineStructure(AS_PATH_ACCESS_LIST, name, ctx);
  }

  @Override
  public void exitIp_dhcp_relay_server(Ip_dhcp_relay_serverContext ctx) {
    if (!_no && ctx.ip != null) {
      Ip ip = toIp(ctx.ip);
      _configuration.getDhcpRelayServers().add(ip);
    }
  }

  @Override
  public void exitIp_domain_lookup(Ip_domain_lookupContext ctx) {
    if (ctx.iname != null) {
      String ifaceName = getCanonicalInterfaceName(ctx.iname.getText());
      _configuration.referenceStructure(
          INTERFACE,
          ifaceName,
          IP_DOMAIN_LOOKUP_INTERFACE,
          ctx.interface_name().getStart().getLine());
      _configuration.setDnsSourceInterface(ifaceName);
    }
  }

  @Override
  public void exitIp_domain_name(Ip_domain_nameContext ctx) {
    if (!_no) {
      String domainName = ctx.hostname.getText();
      _configuration.setDomainName(domainName);
    } else {
      _configuration.setDomainName(null);
    }
  }

  @Override
  public void exitIp_nat_pool(Ip_nat_poolContext ctx) {
    String name = ctx.name.getText();
    Ip first = toIp(ctx.first);
    Ip last = toIp(ctx.last);
    if (ctx.mask != null) {
      Prefix subnet = IpWildcard.ipWithWildcardMask(first, toIp(ctx.mask).inverted()).toPrefix();
      createNatPool(name, first, last, subnet, ctx);
    } else if (ctx.prefix_length != null) {
      Prefix subnet = Prefix.create(first, Integer.parseInt(ctx.prefix_length.getText()));
      createNatPool(name, first, last, subnet, ctx);
    } else {
      _configuration.getNatPools().put(name, new NatPool(first, last));
    }
    _configuration.defineStructure(NAT_POOL, name, ctx);
  }

  @Override
  public void exitIp_nat_pool_range(Ip_nat_pool_rangeContext ctx) {
    String name = ctx.name.getText();
    Ip first = toIp(ctx.first);
    Ip last = toIp(ctx.last);
    if (ctx.prefix_length != null) {
      Prefix subnet = Prefix.create(first, Integer.parseInt(ctx.prefix_length.getText()));
      createNatPool(name, first, last, subnet, ctx);
    } else {
      _configuration.getNatPools().put(name, new NatPool(first, last));
    }
    _configuration.defineStructure(NAT_POOL, name, ctx);
  }

  /**
   * Check that the pool IPs are contained in the subnet, and warn if not. Then create the pool,
   * while excluding the network/broadcast IPs. This means that if specified first pool IP is
   * numerically less than the first host IP in the subnet, use the first host IP instead.
   * Similarly, if the specified last pool IP is greater than the last host IP in the subnet, use
   * the last host IP instead.
   */
  private void createNatPool(String name, Ip first, Ip last, Prefix subnet, ParserRuleContext ctx) {
    if (!subnet.containsIp(first)) {
      warn(ctx, String.format("Subnet of NAT pool %s does not contain first pool IP", name));
    }
    if (!subnet.containsIp(last)) {
      warn(ctx, String.format("Subnet of NAT pool %s does not contain last pool IP", name));
    }

    NatPool pool;
    if (first.equals(last)) {
      // Arista ignores the prefix-length when the pool is a single IP.
      pool = new NatPool(first, last);
    } else {
      // Enforce prefix-length by removing the network and broadcast addresses, if present.
      Ip firstHostIp = subnet.getFirstHostIp();
      Ip lastHostIp = subnet.getLastHostIp();
      pool =
          new NatPool(
              first.asLong() < firstHostIp.asLong() ? firstHostIp : first,
              last.asLong() > lastHostIp.asLong() ? lastHostIp : last);
    }

    _configuration.getNatPools().put(name, pool);
  }

  @Override
  public void exitIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
    _currentPrefixList = null;
  }

  @Override
  public void exitIp_prefix_list_tail(Ip_prefix_list_tailContext ctx) {
    LineAction action = toLineAction(ctx.action);
    Prefix prefix = Prefix.parse(ctx.prefix.getText());
    int prefixLength = prefix.getPrefixLength();
    int minLen = prefixLength;
    int maxLen = prefixLength;
    if (ctx.minpl != null) {
      minLen = toInteger(ctx.minpl);
      maxLen = Prefix.MAX_PREFIX_LENGTH;
    }
    if (ctx.maxpl != null) {
      maxLen = toInteger(ctx.maxpl);
    }
    if (ctx.eqpl != null) {
      minLen = toInteger(ctx.eqpl);
      maxLen = toInteger(ctx.eqpl);
    }
    SubRange lengthRange = new SubRange(minLen, maxLen);
    long seq = ctx.seqnum != null ? toLong(ctx.seqnum) : _currentPrefixList.getNextSeq();
    PrefixListLine line = new PrefixListLine(seq, action, prefix, lengthRange);
    _currentPrefixList.addLine(line);
  }

  @Override
  public void exitIp_ssh_version(Ip_ssh_versionContext ctx) {
    int version = toInteger(ctx.version);
    if (version < 1 || version > 2) {
      throw new BatfishException("Invalid ssh version: " + version);
    }
    _configuration.getCf().getSsh().setVersion(version);
  }

  @Override
  public void exitIpv6_prefix_list_stanza(Ipv6_prefix_list_stanzaContext ctx) {
    _currentPrefix6List = null;
  }

  @Override
  public void exitIpv6_prefix_list_tail(Ipv6_prefix_list_tailContext ctx) {
    LineAction action = toLineAction(ctx.action);
    Prefix6 prefix6 = Prefix6.parse(ctx.prefix6.getText());
    int prefixLength = prefix6.getPrefixLength();
    int minLen = prefixLength;
    int maxLen = prefixLength;
    if (ctx.minpl != null) {
      minLen = toInteger(ctx.minpl);
      maxLen = Prefix6.MAX_PREFIX_LENGTH;
    }
    if (ctx.maxpl != null) {
      maxLen = toInteger(ctx.maxpl);
    }
    if (ctx.eqpl != null) {
      minLen = toInteger(ctx.eqpl);
      maxLen = toInteger(ctx.eqpl);
    }
    SubRange lengthRange = new SubRange(minLen, maxLen);
    Prefix6ListLine line = new Prefix6ListLine(action, prefix6, lengthRange);
    _currentPrefix6List.addLine(line);
  }

  @Override
  public void enterLogging_vrf(Logging_vrfContext ctx) {
    _currentVrf = ctx.name.getText();
  }

  @Override
  public void exitLogging_vrf(Logging_vrfContext ctx) {
    _currentVrf = AristaConfiguration.DEFAULT_VRF_NAME;
  }

  @Override
  public void exitLogging_vrf_host(Logging_vrf_hostContext ctx) {
    String name = ctx.host.getText();
    Vrf v = initVrf(_currentVrf);
    v.getLoggingHosts().computeIfAbsent(name, LoggingHost::new);
  }

  @Override
  public void exitLogging_vrf_source_interface(Logging_vrf_source_interfaceContext ctx) {
    String name = toInterfaceName(ctx.name);
    Vrf v = initVrf(_currentVrf);
    v.setLoggingSourceInterface(name);
    _configuration.referenceStructure(
        INTERFACE, name, LOGGING_SOURCE_INTERFACE, ctx.name.getStart().getLine());
  }

  @Override
  public void enterMac_access_list(Mac_access_listContext ctx) {
    String name = ctx.name.getText();
    _configuration.defineStructure(MAC_ACCESS_LIST, name, ctx);
  }

  @Override
  public void exitManagement_ssh_ip_access_group(Management_ssh_ip_access_groupContext ctx) {
    String acl = ctx.acl.getText();
    int line = ctx.acl.getStart().getLine();
    _configuration.referenceStructure(IPV4_ACCESS_LIST, acl, MANAGEMENT_SSH_ACCESS_GROUP, line);
  }

  @Override
  public void exitManagement_telnet_ip_access_group(Management_telnet_ip_access_groupContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _configuration.referenceStructure(IPV4_ACCESS_LIST, name, MANAGEMENT_TELNET_ACCESS_GROUP, line);
  }

  @Override
  public void exitMatch_as_path_access_list_rm_stanza(
      Match_as_path_access_list_rm_stanzaContext ctx) {
    Set<String> names = new TreeSet<>();
    for (VariableContext name : ctx.name_list) {
      names.add(name.getText());
      _configuration.referenceStructure(
          AS_PATH_ACCESS_LIST, name.getText(),
          ROUTE_MAP_MATCH_AS_PATH_ACCESS_LIST, name.getStart().getLine());
    }
    RouteMapMatchAsPathAccessListLine line = new RouteMapMatchAsPathAccessListLine(names);
    _currentRouteMapClause.addMatchLine(line);
  }

  @Override
  public void exitMatch_community_list_rm_stanza(Match_community_list_rm_stanzaContext ctx) {
    Set<String> names = new TreeSet<>();
    for (VariableContext name : ctx.name_list) {
      names.add(name.getText());
      _configuration.referenceStructure(
          COMMUNITY_LIST,
          name.getText(),
          ROUTE_MAP_MATCH_COMMUNITY_LIST,
          name.getStart().getLine());
    }
    RouteMapMatchCommunity line = new RouteMapMatchCommunity(names);
    _currentRouteMapClause.setMatchCommunity(line);
  }

  @Override
  public void exitMatch_ip_access_list_rm_stanza(Match_ip_access_list_rm_stanzaContext ctx) {
    Set<String> names = new TreeSet<>();
    for (Variable_access_listContext v : ctx.name_list) {
      names.add(v.getText());
      _configuration.referenceStructure(
          IPV4_ACCESS_LIST, v.getText(),
          ROUTE_MAP_MATCH_IPV4_ACCESS_LIST, v.getStart().getLine());
    }
    RouteMapMatchIpAccessListLine line = new RouteMapMatchIpAccessListLine(names);
    _currentRouteMapClause.addMatchLine(line);
  }

  @Override
  public void exitMatch_ip_prefix_list_rm_stanza(Match_ip_prefix_list_rm_stanzaContext ctx) {
    Set<String> names = new TreeSet<>();
    for (VariableContext t : ctx.name_list) {
      names.add(t.getText());
      _configuration.referenceStructure(
          PREFIX_LIST, t.getText(), ROUTE_MAP_MATCH_IPV4_PREFIX_LIST, t.getStart().getLine());
    }
    RouteMapMatchIpPrefixListLine line = new RouteMapMatchIpPrefixListLine(names);
    _currentRouteMapClause.addMatchLine(line);
  }

  @Override
  public void exitMatch_ipv6_access_list_rm_stanza(Match_ipv6_access_list_rm_stanzaContext ctx) {
    Set<String> names = new TreeSet<>();
    for (Variable_access_listContext v : ctx.name_list) {
      names.add(v.getText());
      _configuration.referenceStructure(
          IPV6_ACCESS_LIST, v.getText(), ROUTE_MAP_MATCH_IPV6_ACCESS_LIST, v.getStart().getLine());
    }
    RouteMapMatchIpv6AccessListLine line = new RouteMapMatchIpv6AccessListLine(names);
    _currentRouteMapClause.addMatchLine(line);
  }

  @Override
  public void exitMatch_ipv6_prefix_list_rm_stanza(Match_ipv6_prefix_list_rm_stanzaContext ctx) {
    Set<String> names = new TreeSet<>();
    for (VariableContext t : ctx.name_list) {
      names.add(t.getText());
      _configuration.referenceStructure(
          PREFIX6_LIST, t.getText(), ROUTE_MAP_MATCH_IPV6_PREFIX_LIST, t.getStart().getLine());
    }
    RouteMapMatchIpv6PrefixListLine line = new RouteMapMatchIpv6PrefixListLine(names);
    _currentRouteMapClause.addMatchLine(line);
  }

  @Override
  public void exitMatch_source_protocol_rm_stanza(Match_source_protocol_rm_stanzaContext ctx) {
    List<RoutingProtocol> rps = new LinkedList<>();
    boolean todo = false;
    if (!ctx.BGP().isEmpty()) {
      todo = true;
    }
    if (!ctx.CONNECTED().isEmpty()) {
      rps.add(RoutingProtocol.CONNECTED);
    }
    if (!ctx.ISIS().isEmpty()) {
      todo = true;
    }
    if (!ctx.OSPF().isEmpty()) {
      todo = true;
    }
    if (!ctx.STATIC().isEmpty()) {
      rps.add(RoutingProtocol.STATIC);
    }
    if (todo) {
      todo(ctx);
      // Do nothing, since we have some unsupported protocols.
      return;
    }
    if (rps.isEmpty()) {
      // This should not be possible.
      warn(ctx, "Unexpected: empty routing protocol list to match against.");
      return;
    }
    _currentRouteMapClause.addMatchLine(new RouteMapMatchSourceProtocolLine(rps));
  }

  @Override
  public void exitMatch_tag_rm_stanza(Match_tag_rm_stanzaContext ctx) {
    Set<Long> tags = new TreeSet<>();
    for (Uint32Context t : ctx.tag_list) {
      tags.add(toLong(t));
    }
    RouteMapMatchTagLine line = new RouteMapMatchTagLine(tags);
    _currentRouteMapClause.addMatchLine(line);
  }

  @Override
  public void exitEos_mlag_domain(Eos_mlag_domainContext ctx) {
    _configuration.getEosMlagConfiguration().setDomainId(ctx.id.getText());
  }

  @Override
  public void exitEos_mlag_local_interface(Eos_mlag_local_interfaceContext ctx) {
    String iface = getCanonicalInterfaceName(ctx.iface.getText());
    _configuration.getEosMlagConfiguration().setLocalInterface(iface);
    _configuration.referenceStructure(
        INTERFACE, iface, MLAG_CONFIGURATION_LOCAL_INTERFACE, ctx.getStart().getLine());
  }

  @Override
  public void exitEos_mpa_ip(Eos_mpa_ipContext ctx) {
    _configuration.getEosMlagConfiguration().setPeerAddress(toIp(ctx.ip));
  }

  @Override
  public void exitEos_mpa_heartbeat(Eos_mpa_heartbeatContext ctx) {
    _configuration.getEosMlagConfiguration().setPeerAddressHeartbeat(toIp(ctx.ip));
    if (ctx.vrf_name() != null) {
      _configuration.getEosMlagConfiguration().setPeerAddressHeartbeatVrf(toString(ctx.vrf_name()));
    }
  }

  @Override
  public void exitEos_mlag_peer_link(Eos_mlag_peer_linkContext ctx) {
    String iface = getCanonicalInterfaceName(ctx.iface.getText());
    _configuration.getEosMlagConfiguration().setPeerLink(iface);
    _configuration.referenceStructure(
        INTERFACE, iface, MLAG_CONFIGURATION_PEER_LINK, ctx.getStart().getLine());
  }

  @Override
  public void exitEos_mlag_reload_delay(Eos_mlag_reload_delayContext ctx) {
    Integer period = ctx.INFINITY() != null ? Integer.MAX_VALUE : toInteger(ctx.period);
    if (ctx.MLAG() != null) {
      _configuration.getEosMlagConfiguration().setReloadDelayMlag(period);
    } else if (ctx.NON_MLAG() != null) {
      _configuration.getEosMlagConfiguration().setReloadDelayNonMlag(period);
    } else {
      _configuration.getEosMlagConfiguration().setReloadDelayMlag(period);
      _configuration.getEosMlagConfiguration().setReloadDelayNonMlag(period);
    }
  }

  @Override
  public void exitEos_mlag_shutdown(Eos_mlag_shutdownContext ctx) {
    _configuration.getEosMlagConfiguration().setShutdown(ctx.NO() == null);
  }

  @Override
  public void exitVlan_name(Vlan_nameContext ctx) {
    String name = ctx.name.getText();
    _currentVlans.forEach(v -> v.setName(name));
  }

  @Override
  public void exitVlan_d_name(Vlan_d_nameContext ctx) {
    _currentVlans.forEach(v -> v.setName(null));
  }

  @Override
  public void exitVlan_d_state(Vlan_d_stateContext ctx) {
    _currentVlans.forEach(v -> v.setState(null));
  }

  @Override
  public void exitVlan_d_trunk(Vlan_d_trunkContext ctx) {
    _currentVlans.forEach(v -> v.setTrunkGroup(null));
  }

  @Override
  public void exitVlan_no_name(Vlan_no_nameContext ctx) {
    _currentVlans.forEach(v -> v.setName(null));
  }

  @Override
  public void exitVlan_no_state(Vlan_no_stateContext ctx) {
    _currentVlans.forEach(v -> v.setState(null));
  }

  @Override
  public void exitVlan_no_trunk(Vlan_no_trunkContext ctx) {
    _currentVlans.forEach(v -> v.setTrunkGroup(null));
  }

  @Override
  public void exitVlan_state(Vlan_stateContext ctx) {
    State state = ctx.ACTIVE() != null ? State.ACTIVE : State.SUSPEND;
    _currentVlans.forEach(v -> v.setState(state));
  }

  @Override
  public void exitVlan_trunk(Vlan_trunkContext ctx) {
    String groupName = ctx.name.getText();
    _currentVlans.forEach(v -> v.setTrunkGroup(groupName));
  }

  @Override
  public void exitS_vlan_internal(S_vlan_internalContext ctx) {
    todo(ctx);
  }

  @Override
  public void enterNo_ip_prefix_list(No_ip_prefix_listContext ctx) {
    String name = ctx.name.getText();
    _currentPrefixList = _configuration.getPrefixLists().get(name);
    if (_currentPrefixList == null) {
      warn(ctx, "No ip prefix-list named " + name + " to delete");
      _currentPrefixList = new PrefixList(name);
    }
  }

  @Override
  public void exitNo_ip_prefix_list(No_ip_prefix_listContext ctx) {
    _currentPrefixList = null;
  }

  @Override
  public void exitNo_ip_prefix_list_stanza(No_ip_prefix_list_stanzaContext ctx) {
    // Okay if not present, we already warned.
    _configuration.getPrefixLists().remove(_currentPrefixList.getName());
  }

  @Override
  public void exitNo_ip_prefix_list_seq(No_ip_prefix_list_seqContext ctx) {
    long seq = toLong(ctx.seqnum);
    if (!_configuration.getPrefixLists().containsKey(_currentPrefixList.getName())) {
      // This non-existent prefix list we already warned on, exit early.
      return;
    }
    boolean removed = _currentPrefixList.getLines().remove(seq) != null;
    if (!removed) {
      warn(
          ctx,
          String.format(
              "ip prefix-list %s does not have sequence %d to delete",
              _currentPrefixList.getName(), seq));
    }
  }

  @Override
  public void exitNo_route_map_stanza(No_route_map_stanzaContext ctx) {
    String mapName = ctx.name.getText();
    if (!_configuration.getRouteMaps().containsKey(mapName)) {
      warn(ctx, "Undefined route-map: " + mapName);
      return;
    }
    _configuration.getRouteMaps().remove(mapName);
  }

  @Override
  public void exitNo_vlan(No_vlanContext ctx) {
    IntegerSpace vlans = toIntegerSpace(ctx.eos_vlan_id());
    vlans.intStream().forEach(_configuration::removeVlan);
  }

  @Override
  public void exitNtp_server(Ntp_serverContext ctx) {
    Ntp ntp = _configuration.getCf().getNtp();
    String hostname = ctx.hostname.getText();
    NtpServer server = ntp.getServers().computeIfAbsent(hostname, NtpServer::new);
    if (ctx.vrf != null) {
      String vrfName = ctx.vrf.getText();
      server.setVrf(vrfName);
      initVrf(vrfName);
    }
    if (ctx.PREFER() != null) {
      // TODO: implement
    }
  }

  @Override
  public void exitPassive_iis_stanza(Passive_iis_stanzaContext ctx) {
    _currentIsisInterface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
  }

  @Override
  public void exitPassive_interface_default_is_stanza(
      Passive_interface_default_is_stanzaContext ctx) {
    for (Interface iface : _configuration.getInterfaces().values()) {
      iface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
    }
  }

  @Override
  public void exitPassive_interface_is_stanza(Passive_interface_is_stanzaContext ctx) {
    Interface iface = getOrAddInterface(ctx.name);
    if (ctx.NO() == null) {
      iface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
    } else {
      iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
    }
  }

  @Override
  public void exitPeer_sa_filter(Peer_sa_filterContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _configuration.referenceStructure(IPV4_ACCESS_LIST, name, MSDP_PEER_SA_LIST, line);
  }

  @Override
  public void exitPim_accept_register(Pim_accept_registerContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    if (ctx.LIST() != null) {
      _configuration.referenceStructure(IPV4_ACCESS_LIST, name, PIM_ACCEPT_REGISTER_ACL, line);
    } else if (ctx.ROUTE_MAP() != null) {
      _configuration.referenceStructure(ROUTE_MAP, name, PIM_ACCEPT_REGISTER_ROUTE_MAP, line);
    }
  }

  @Override
  public void exitPim_accept_rp(Pim_accept_rpContext ctx) {
    if (ctx.name != null) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.referenceStructure(IPV4_ACCESS_LIST, name, PIM_ACCEPT_RP_ACL, line);
    }
  }

  @Override
  public void exitPim_rp_address(Pim_rp_addressContext ctx) {
    if (ctx.name != null) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.referenceStructure(
          IPV4_ACCESS_LIST, name,
          PIM_RP_ADDRESS_ACL, line);
    }
  }

  @Override
  public void exitPim_rp_announce_filter(Pim_rp_announce_filterContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _configuration.referenceStructure(IPV4_ACCESS_LIST, name, PIM_RP_ANNOUNCE_FILTER, line);
  }

  @Override
  public void exitPim_rp_candidate(Pim_rp_candidateContext ctx) {
    if (ctx.name != null) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.referenceStructure(IPV4_ACCESS_LIST, name, PIM_RP_CANDIDATE_ACL, line);
    }
  }

  @Override
  public void exitPim_send_rp_announce(Pim_send_rp_announceContext ctx) {
    if (ctx.name != null) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.referenceStructure(IPV4_ACCESS_LIST, name, PIM_SEND_RP_ANNOUNCE_ACL, line);
    }
  }

  @Override
  public void exitPim_spt_threshold(Pim_spt_thresholdContext ctx) {
    if (ctx.name != null) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.referenceStructure(IPV4_ACCESS_LIST, name, PIM_SPT_THRESHOLD_ACL, line);
    }
  }

  @Override
  public void enterPm_class(Pm_classContext ctx) {
    // TODO: do something with this.
    if (ctx.BUILT_IN() != null) {
      // configuration for a built-in class, no need to add ref
      return;
    }
    if (ctx.CLASS_DEFAULT() != null) {
      // configuration for the "switch default" class, no need to add ref
      return;
    }
    assert ctx.name != null;
    String name = ctx.name.getText();
    if (name.startsWith("copp-system-")) {
      // built-in control plane policing class, no need to add ref
      return;
    }
    _configuration.referenceStructure(CLASS_MAP, name, POLICY_MAP_CLASS, ctx.getStart().getLine());
  }

  @Override
  public void exitPm_event_class(Pm_event_classContext ctx) {
    if (ctx.classname != null) {
      _configuration.referenceStructure(
          CLASS_MAP, ctx.classname.getText(), POLICY_MAP_EVENT_CLASS, ctx.getStart().getLine());
    }
    if (ctx.stname != null) {
      _configuration.referenceStructure(
          SERVICE_TEMPLATE,
          ctx.stname.getText(),
          POLICY_MAP_EVENT_CLASS_ACTIVATE,
          ctx.stname.getStart().getLine());
    }
  }

  @Override
  public void exitPmc_service_policy(Pmc_service_policyContext ctx) {
    _configuration.referenceStructure(
        POLICY_MAP,
        ctx.name.getText(),
        POLICY_MAP_CLASS_SERVICE_POLICY,
        ctx.name.getStart().getLine());
  }

  @Override
  public void exitRedistribute_connected_is_stanza(Redistribute_connected_is_stanzaContext ctx) {
    IsisProcess proc = currentVrf().getIsisProcess();
    RedistributionSourceProtocol sourceProtocol = RedistributionSourceProtocol.CONNECTED;
    IsisRedistributionPolicy r = new IsisRedistributionPolicy(sourceProtocol);
    proc.getRedistributionPolicies().put(sourceProtocol, r);
    if (ctx.metric != null) {
      int metric = toInteger(ctx.metric);
      r.setMetric(metric);
    }
    if (ctx.map != null) {
      String map = ctx.map.getText();
      r.setMap(map);
      _configuration.referenceStructure(
          ROUTE_MAP, map, ISIS_REDISTRIBUTE_CONNECTED_MAP, ctx.map.getLine());
    }
    if (ctx.LEVEL_1() != null) {
      r.setLevel(IsisLevel.LEVEL_1);
    } else if (ctx.LEVEL_2() != null) {
      r.setLevel(IsisLevel.LEVEL_2);
    } else if (ctx.LEVEL_1_2() != null) {
      r.setLevel(IsisLevel.LEVEL_1_2);
    } else {
      r.setLevel(IsisRedistributionPolicy.DEFAULT_LEVEL);
    }
  }

  @Override
  public void exitRedistribute_static_is_stanza(Redistribute_static_is_stanzaContext ctx) {
    IsisProcess proc = currentVrf().getIsisProcess();
    RedistributionSourceProtocol sourceProtocol = RedistributionSourceProtocol.STATIC;
    IsisRedistributionPolicy r = new IsisRedistributionPolicy(sourceProtocol);
    proc.getRedistributionPolicies().put(sourceProtocol, r);
    if (ctx.metric != null) {
      int metric = toInteger(ctx.metric);
      r.setMetric(metric);
    }
    if (ctx.map != null) {
      String map = ctx.map.getText();
      r.setMap(map);
      _configuration.referenceStructure(
          ROUTE_MAP, map, ISIS_REDISTRIBUTE_STATIC_MAP, ctx.map.getLine());
    }
    if (ctx.LEVEL_1() != null) {
      r.setLevel(IsisLevel.LEVEL_1);
    } else if (ctx.LEVEL_2() != null) {
      r.setLevel(IsisLevel.LEVEL_2);
    } else if (ctx.LEVEL_1_2() != null) {
      r.setLevel(IsisLevel.LEVEL_1_2);
    } else {
      r.setLevel(IsisRedistributionPolicy.DEFAULT_LEVEL);
    }
  }

  @Override
  public void exitRms_distance(Rms_distanceContext ctx) {
    int distance = toInteger(ctx.distance);
    RouteMapSetLine line = new RouteMapSetDistanceLine(distance);
    _currentRouteMapClause.addSetLine(line);
  }

  @Override
  public void exitRo_area_filterlist(Ro_area_filterlistContext ctx) {
    String prefixListName = ctx.list.getText();
    _configuration.referenceStructure(
        PREFIX_LIST, prefixListName, OSPF_AREA_FILTER_LIST, ctx.list.getStart().getLine());
    todo(ctx);
  }

  @Override
  public void exitRo_area_nssa(Ro_area_nssaContext ctx) {
    OspfProcess proc = _currentOspfProcess;
    long area = toLong(ctx.area);
    NssaSettings settings = proc.getNssas().computeIfAbsent(area, a -> new NssaSettings());
    if (ctx.default_information_originate != null) {
      settings.setDefaultInformationOriginate(true);
    }
    if (ctx.no_redistribution != null) {
      settings.setNoRedistribution(true);
    }
    if (ctx.no_summary != null) {
      settings.setNoSummary(true);
    }
  }

  @Override
  public void exitRo_area_range(Ro_area_rangeContext ctx) {
    long areaNum = toLong(ctx.area);
    Prefix prefix;
    if (ctx.area_prefix != null) {
      prefix = Prefix.parse(ctx.area_prefix.getText());
    } else {
      prefix = Prefix.create(toIp(ctx.area_ip), toIp(ctx.area_subnet));
    }
    boolean advertise = ctx.NOT_ADVERTISE() == null;
    Long cost = ctx.cost == null ? null : toLong(ctx.cost);

    Map<Prefix, OspfAreaSummary> area =
        _currentOspfProcess.getSummaries().computeIfAbsent(areaNum, k -> new TreeMap<>());
    area.put(
        prefix,
        new OspfAreaSummary(
            advertise
                ? SummaryRouteBehavior.ADVERTISE_AND_INSTALL_DISCARD
                : SummaryRouteBehavior.NOT_ADVERTISE_AND_NO_DISCARD,
            cost));
  }

  @Override
  public void exitRo_area_stub(Ro_area_stubContext ctx) {
    OspfProcess proc = _currentOspfProcess;
    long area = toLong(ctx.area);
    StubSettings settings = proc.getStubs().computeIfAbsent(area, a -> new StubSettings());
    if (ctx.no_summary != null) {
      settings.setNoSummary(true);
    }
  }

  @Override
  public void exitRo_default_information(Ro_default_informationContext ctx) {
    OspfProcess proc = _currentOspfProcess;
    proc.setDefaultInformationOriginate(true);
    boolean always = !ctx.ALWAYS().isEmpty();
    proc.setDefaultInformationOriginateAlways(always);
    if (ctx.metric != null) {
      int metric = toInteger(ctx.metric);
      proc.setDefaultInformationMetric(metric);
    }
    if (ctx.metric_type != null) {
      int metricTypeInt = toInteger(ctx.metric_type);
      OspfMetricType metricType = OspfMetricType.fromInteger(metricTypeInt);
      proc.setDefaultInformationMetricType(metricType);
    }
    if (ctx.map != null) {
      String map = ctx.map.getText();
      proc.setDefaultInformationOriginateMap(map);
      _configuration.referenceStructure(
          ROUTE_MAP, map, OSPF_DEFAULT_ORIGINATE_ROUTE_MAP, ctx.map.getLine());
    }
  }

  @Override
  public void exitRo_default_metric(Ro_default_metricContext ctx) {
    OspfProcess proc = _currentOspfProcess;
    if (ctx.NO() != null) {
      proc.setDefaultMetric(null);
    } else {
      long metric = toLong(ctx.metric);
      proc.setDefaultMetric(metric);
    }
  }

  @Override
  public void exitRo_distribute_list(Ro_distribute_listContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    boolean in = ctx.IN() != null;
    AristaStructureType type;
    AristaStructureUsage usage;
    DistributeListFilterType filterType;
    if (ctx.PREFIX() != null) {
      type = PREFIX_LIST;
      filterType = DistributeListFilterType.PREFIX_LIST;
      usage = in ? OSPF_DISTRIBUTE_LIST_PREFIX_LIST_IN : OSPF_DISTRIBUTE_LIST_PREFIX_LIST_OUT;
    } else if (ctx.ROUTE_MAP() != null) {
      filterType = DistributeListFilterType.ROUTE_MAP;
      type = ROUTE_MAP;
      usage = in ? OSPF_DISTRIBUTE_LIST_ROUTE_MAP_IN : OSPF_DISTRIBUTE_LIST_ROUTE_MAP_OUT;
      todo(ctx);
    } else {
      filterType = DistributeListFilterType.ACCESS_LIST;
      type = IP_ACCESS_LIST;
      usage = in ? OSPF_DISTRIBUTE_LIST_ACCESS_LIST_IN : OSPF_DISTRIBUTE_LIST_ACCESS_LIST_OUT;
      todo(ctx);
    }
    _configuration.referenceStructure(type, name, usage, line);

    DistributeList distributeList = new DistributeList(name, filterType);
    if (ctx.iname != null) {
      String ifaceName = getCanonicalInterfaceName(ctx.iname.getText());
      _configuration.referenceStructure(INTERFACE, ifaceName, usage, line);
      if (in) {
        _currentOspfProcess.getInboundInterfaceDistributeLists().put(ifaceName, distributeList);
      } else {
        _currentOspfProcess.getOutboundInterfaceDistributeLists().put(ifaceName, distributeList);
      }
    } else {
      if (in) {
        _currentOspfProcess.setInboundGlobalDistributeList(distributeList);
      } else {
        _currentOspfProcess.setOutboundGlobalDistributeList(distributeList);
      }
    }
  }

  @Override
  public void exitRo_maximum_paths(Ro_maximum_pathsContext ctx) {
    todo(ctx);
    /*
     * Note that this is very difficult to enforce, and may not help the
     * analysis without major changes
     */
  }

  @Override
  public void exitRo_network(Ro_networkContext ctx) {
    Prefix prefix;
    long area = toLong(ctx.area);
    if (ctx.prefix != null) {
      prefix = Prefix.parse(ctx.prefix.getText());
    } else {
      Ip address = toIp(ctx.ip);
      Ip wildcard = toIp(ctx.wildcard);
      Ip mask = wildcard.inverted();
      if (!mask.isValidNetmask1sLeading()) {
        // Tested on 4.18 and 4.23
        warn(
            ctx,
            String.format(
                "Invalid wildcard mask (expecting the inverse of a subnet mask): %s", wildcard));
        return;
      }
      prefix = Prefix.create(address, mask);
    }
    OspfNetwork network = new OspfNetwork(prefix, area);
    _currentOspfProcess.getNetworks().add(network);
  }

  @Override
  public void exitRo_passive_interface(Ro_passive_interfaceContext ctx) {
    boolean passive = ctx.NO() == null;
    String iname = toInterfaceName(ctx.i);
    OspfProcess proc = _currentOspfProcess;
    if (passive ^ proc.getPassiveInterfaceDefault()) {
      proc.getNonDefaultInterfaces().add(iname);
    }
  }

  @Override
  public void exitRo_passive_interface_default(Ro_passive_interface_defaultContext ctx) {
    boolean passive = ctx.NO() == null;
    OspfProcess proc = _currentOspfProcess;
    proc.setPassiveInterfaceDefault(passive);
  }

  @Override
  public void exitRo_redistribute_bgp_arista(Ro_redistribute_bgp_aristaContext ctx) {
    OspfProcess proc = _currentOspfProcess;
    RedistributionSourceProtocol sourceProtocol = RedistributionSourceProtocol.BGP_ANY;
    OspfRedistributionPolicy r = new OspfRedistributionPolicy(sourceProtocol);
    proc.getRedistributionPolicies().put(sourceProtocol, r);
    if (ctx.map != null) {
      String map = ctx.map.getText();
      r.setRouteMap(map);
      _configuration.referenceStructure(
          ROUTE_MAP, map, OSPF_REDISTRIBUTE_BGP_MAP, ctx.map.getLine());
    }
    r.setOspfMetricType(OspfRedistributionPolicy.DEFAULT_METRIC_TYPE);
  }

  @Override
  public void exitRo_redistribute_connected(Ro_redistribute_connectedContext ctx) {
    OspfProcess proc = _currentOspfProcess;
    RedistributionSourceProtocol sourceProtocol = RedistributionSourceProtocol.CONNECTED;
    OspfRedistributionPolicy r = new OspfRedistributionPolicy(sourceProtocol);
    proc.getRedistributionPolicies().put(sourceProtocol, r);
    if (ctx.metric != null) {
      int metric = toInteger(ctx.metric);
      r.setMetric(metric);
    }
    if (ctx.map != null) {
      String map = ctx.map.getText();
      r.setRouteMap(map);
      _configuration.referenceStructure(
          ROUTE_MAP, map, OSPF_REDISTRIBUTE_CONNECTED_MAP, ctx.map.getLine());
    }
    if (ctx.type != null) {
      int typeInt = toInteger(ctx.type);
      OspfMetricType type = OspfMetricType.fromInteger(typeInt);
      r.setOspfMetricType(type);
    } else {
      r.setOspfMetricType(OspfRedistributionPolicy.DEFAULT_METRIC_TYPE);
    }
    if (ctx.tag != null) {
      long tag = toLong(ctx.tag);
      r.setTag(tag);
    }
  }

  @Override
  public void exitRo_redistribute_rip(Ro_redistribute_ripContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitRo_redistribute_static(Ro_redistribute_staticContext ctx) {
    OspfProcess proc = _currentOspfProcess;
    RedistributionSourceProtocol sourceProtocol = RedistributionSourceProtocol.STATIC;
    OspfRedistributionPolicy r = new OspfRedistributionPolicy(sourceProtocol);
    proc.getRedistributionPolicies().put(sourceProtocol, r);
    if (ctx.metric != null) {
      int metric = toInteger(ctx.metric);
      r.setMetric(metric);
    }
    if (ctx.map != null) {
      String map = ctx.map.getText();
      r.setRouteMap(map);
      _configuration.referenceStructure(
          ROUTE_MAP, map, OSPF_REDISTRIBUTE_STATIC_MAP, ctx.map.getLine());
    }
    if (ctx.type != null) {
      int typeInt = toInteger(ctx.type);
      OspfMetricType type = OspfMetricType.fromInteger(typeInt);
      r.setOspfMetricType(type);
    } else {
      r.setOspfMetricType(OspfRedistributionPolicy.DEFAULT_METRIC_TYPE);
    }
    if (ctx.tag != null) {
      long tag = toLong(ctx.tag);
      r.setTag(tag);
    }
  }

  @Override
  public void exitRo_rfc1583_compatibility(Ro_rfc1583_compatibilityContext ctx) {
    _currentOspfProcess.setRfc1583Compatible(ctx.NO() == null);
  }

  @Override
  public void exitRo_router_id(Ro_router_idContext ctx) {
    Ip routerId = toIp(ctx.ip);
    _currentOspfProcess.setRouterId(routerId);
  }

  @Override
  public void exitRo6_distribute_list(Ro6_distribute_listContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    boolean in = ctx.IN() != null;
    AristaStructureUsage usage =
        in ? OSPF6_DISTRIBUTE_LIST_PREFIX_LIST_IN : OSPF6_DISTRIBUTE_LIST_PREFIX_LIST_OUT;
    _configuration.referenceStructure(PREFIX6_LIST, name, usage, line);

    if (ctx.iname != null) {
      String ifaceName = getCanonicalInterfaceName(ctx.iname.getText());
      _configuration.referenceStructure(INTERFACE, ifaceName, usage, line);
    }
  }

  @Override
  public void exitRoute_map_stanza(Route_map_stanzaContext ctx) {
    _currentRouteMap = null;
    _currentRouteMapClause = null;
  }

  @Override
  public void exitRouter_isis_stanza(Router_isis_stanzaContext ctx) {
    _currentIsisProcess = null;
  }

  @Override
  public void exitRr_distribute_list(Rr_distribute_listContext ctx) {
    RipProcess proc = _currentRipProcess;
    int line = ctx.getStart().getLine();
    boolean in = ctx.IN() != null;
    String name;
    boolean acl;
    if (ctx.acl != null) {
      name = ctx.acl.getText();
      acl = true;
      _configuration.referenceStructure(IP_ACCESS_LIST, name, RIP_DISTRIBUTE_LIST, line);
    } else {
      name = ctx.prefix_list.getText();
      acl = false;
      _configuration.referenceStructure(PREFIX_LIST, name, RIP_DISTRIBUTE_LIST, line);
    }
    if (in) {
      proc.setDistributeListIn(name);
      proc.setDistributeListInAcl(acl);
    } else {
      proc.setDistributeListOut(name);
      proc.setDistributeListOutAcl(acl);
    }
  }

  @Override
  public void exitRr_network(Rr_networkContext ctx) {
    Ip networkAddress = toIp(ctx.network);
    Ip mask = networkAddress.getClassMask();
    Prefix network = Prefix.create(networkAddress, mask);
    _currentRipProcess.getNetworks().add(network);
  }

  @Override
  public void exitRr_passive_interface(Rr_passive_interfaceContext ctx) {
    boolean passive = ctx.NO() == null;
    String iname = ctx.iname.getText();
    RipProcess proc = _currentRipProcess;
    if (passive) {
      proc.getPassiveInterfaceList().add(iname);
    } else {
      proc.getActiveInterfaceList().add(iname);
    }
  }

  @Override
  public void exitRr_passive_interface_default(Rr_passive_interface_defaultContext ctx) {
    boolean no = ctx.NO() != null;
    _currentRipProcess.setPassiveInterfaceDefault(!no);
  }

  @Override
  public void exitS_domain_name(S_domain_nameContext ctx) {
    String domainName = ctx.hostname.getText();
    _configuration.setDomainName(domainName);
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    String hostname;
    if (ctx.quoted_name != null) {
      hostname = unquote(ctx.quoted_name.getText());
    } else {
      StringBuilder sb = new StringBuilder();
      for (Token namePart : ctx.name_parts) {
        sb.append(namePart.getText());
      }
      hostname = sb.toString();
    }
    _configuration.setHostname(hostname);
    _configuration.getCf().setHostname(hostname);
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    _currentInterfaces = null;
  }

  @Override
  public void exitS_ip_default_gateway(S_ip_default_gatewayContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitS_ip_dhcp(S_ip_dhcpContext ctx) {
    _no = false;
  }

  @Override
  public void exitS_ip_domain(S_ip_domainContext ctx) {
    _no = false;
  }

  @Override
  public void exitS_ip_domain_name(S_ip_domain_nameContext ctx) {
    String domainName = ctx.hostname.getText();
    _configuration.setDomainName(domainName);
  }

  @Override
  public void exitS_ip_name_server(S_ip_name_serverContext ctx) {
    Set<String> dnsServers = _configuration.getDnsServers();
    for (Ip_hostnameContext ipCtx : ctx.hostnames) {
      String domainName = ipCtx.getText();
      dnsServers.add(domainName);
    }
  }

  @Override
  public void exitS_ip_pim(S_ip_pimContext ctx) {
    _no = false;
  }

  @Override
  public void exitS_ip_source_route(S_ip_source_routeContext ctx) {
    boolean enabled = ctx.NO() == null;
    _configuration.getCf().setSourceRoute(enabled);
  }

  @Override
  public void exitS_ip_tacacs_source_interface(S_ip_tacacs_source_interfaceContext ctx) {
    String ifaceName = getCanonicalInterfaceName(ctx.iname.getText());
    _configuration.setTacacsSourceInterface(ifaceName);
    _configuration.referenceStructure(
        INTERFACE, ifaceName, IP_TACACS_SOURCE_INTERFACE, ctx.iname.getStart().getLine());
  }

  @Override
  public void exitS_logging(S_loggingContext ctx) {
    _no = false;
  }

  @Override
  public void exitS_no_router_ospf(S_no_router_ospfContext ctx) {
    String procName = toString(ctx.name);
    String vrfName = AristaConfiguration.DEFAULT_VRF_NAME;
    if (ctx.vrf != null) {
      vrfName = toString(ctx.vrf);
      if (!_configuration.getVrfs().containsKey(vrfName)) {
        warn(ctx, "Undefined VRF: " + vrfName);
        return;
      }
    }
    Vrf vrf = _configuration.getVrfs().get(vrfName);
    if (!vrf.getOspfProcesses().containsKey(procName)) {
      warn(ctx, "Undefined OSPF instance: " + procName);
      return;
    }
    vrf.getOspfProcesses().remove(procName);
  }

  @Override
  public void enterS_router_multicast(S_router_multicastContext ctx) {
    warn(ctx, ctx.MULTICAST(), "Batfish does not model multicast");
  }

  @Override
  public void exitS_router_ospf(S_router_ospfContext ctx) {
    _currentOspfProcess = null;
    _currentVrf = AristaConfiguration.DEFAULT_VRF_NAME;
  }

  @Override
  public void enterS_router_pim(S_router_pimContext ctx) {
    warn(ctx, ctx.PIM(), "Batfish does not model multicast");
  }

  @Override
  public void exitS_router_rip(S_router_ripContext ctx) {
    _currentRipProcess = null;
  }

  @Override
  public void exitS_service(S_serviceContext ctx) {
    List<String> words = ctx.words.stream().map(RuleContext::getText).collect(Collectors.toList());
    boolean enabled = ctx.NO() == null;
    Iterator<String> i = words.iterator();
    SortedMap<String, Service> currentServices = _configuration.getCf().getServices();
    while (i.hasNext()) {
      String name = i.next();
      Service s = currentServices.computeIfAbsent(name, k -> new Service());
      if (enabled) {
        s.setEnabled(true);
      } else if (!i.hasNext()) {
        s.disable();
      }
      currentServices = s.getSubservices();
    }
  }

  @Override
  public void exitS_service_policy_global(S_service_policy_globalContext ctx) {
    _configuration.referenceStructure(
        POLICY_MAP, ctx.name.getText(), SERVICE_POLICY_GLOBAL, ctx.name.getStart().getLine());
  }

  @Override
  public void exitS_spanning_tree(S_spanning_treeContext ctx) {
    _no = false;
  }

  @Override
  public void exitS_system_service_policy(S_system_service_policyContext ctx) {
    _configuration.referenceStructure(
        POLICY_MAP,
        ctx.policy_map.getText(),
        SYSTEM_SERVICE_POLICY,
        ctx.policy_map.getStart().getLine());
  }

  @Override
  public void exitS_switchport(S_switchportContext ctx) {
    if (ctx.ACCESS() != null) {
      _configuration.getCf().setDefaultSwitchportMode(SwitchportMode.ACCESS);
    } else if (ctx.ROUTED() != null) {
      _configuration.getCf().setDefaultSwitchportMode(SwitchportMode.NONE);
    } else {
      throw new BatfishException("Unsupported top-level switchport statement: " + ctx.getText());
    }
  }

  @Override
  public void exitS_tacacs_server(S_tacacs_serverContext ctx) {
    _no = false;
  }

  @Override
  public void exitS_username(S_usernameContext ctx) {
    _currentUser = null;
  }

  @Override
  public void exitS_vlan(S_vlanContext ctx) {
    _currentVlans = ImmutableList.of();
  }

  @Override
  public void exitS_vrf_definition(S_vrf_definitionContext ctx) {
    _currentVrf = AristaConfiguration.DEFAULT_VRF_NAME;
  }

  @Override
  public void exitSd_switchport_blank(Sd_switchport_blankContext ctx) {
    _configuration.getCf().setDefaultSwitchportMode(SwitchportMode.ACCESS);
  }

  @Override
  public void exitSd_switchport_shutdown(Sd_switchport_shutdownContext ctx) {
    _configuration.getCf().setDefaultSwitchportMode(SwitchportMode.NONE);
  }

  @Override
  public void exitSet_as_path_prepend_rm_stanza(Set_as_path_prepend_rm_stanzaContext ctx) {
    List<AsExpr> asList = new ArrayList<>();
    for (As_exprContext asx : ctx.as_list) {
      AsExpr as = toAsExpr(asx);
      asList.add(as);
    }
    RouteMapSetAsPathPrependLine line = new RouteMapSetAsPathPrependLine(asList);
    _currentRouteMapClause.addSetLine(line);
  }

  @Override
  public void exitRmsc_community_list(Rmsc_community_listContext ctx) {
    List<String> names =
        ctx.names.stream()
            .map(AristaControlPlaneExtractor::toString)
            .collect(ImmutableList.toImmutableList());
    if (ctx.DELETE() != null) {
      extractSetCommunityListDelete(ctx, names);
    } else if (ctx.ADDITIVE() != null) {
      extractSetCommunityListAdditive(ctx, names);
    } else {
      RouteMapSetCommunityListDelete del = _currentRouteMapClause.getSetCommunityListDelete();
      boolean add = _currentRouteMapClause.getAdditive();
      if (add && del != null) {
        // list delete is set, and either list or non-list additive is set
        warn(ctx, "set community community-list ambiguous; specify either additive or delete");
        return;
      }
      if (add) {
        // list or non-list additive is set
        extractSetCommunityListAdditive(ctx, names);
      } else if (del != null) {
        // delete is set
        extractSetCommunityListDelete(ctx, names);
      } else {
        extractSetCommunityList(ctx, names);
      }
    }
  }

  private void extractSetCommunityListAdditive(Rmsc_community_listContext ctx, List<String> names) {
    _currentRouteMapClause.clearNonAdditiveSetCommunity();
    _currentRouteMapClause.setSetCommunityList(new RouteMapSetCommunityList(names, true));
    names.forEach(
        name ->
            _configuration.referenceStructure(
                COMMUNITY_LIST,
                name,
                ROUTE_MAP_SET_COMMUNITY_COMMUNITY_LIST_ADDITIVE,
                ctx.getStart().getLine()));
  }

  private void extractSetCommunityListDelete(Rmsc_community_listContext ctx, List<String> names) {
    _currentRouteMapClause.clearNonAdditiveSetCommunity();
    _currentRouteMapClause.setSetCommunityDelete(null);
    _currentRouteMapClause.setSetCommunityListDelete(new RouteMapSetCommunityListDelete(names));
    names.forEach(
        name ->
            _configuration.referenceStructure(
                COMMUNITY_LIST,
                name,
                ROUTE_MAP_SET_COMMUNITY_COMMUNITY_LIST_DELETE,
                ctx.getStart().getLine()));
  }

  private void extractSetCommunityList(Rmsc_community_listContext ctx, List<String> names) {
    _currentRouteMapClause.clearSetCommunity();
    _currentRouteMapClause.setSetCommunityList(new RouteMapSetCommunityList(names, false));
    names.forEach(
        name ->
            _configuration.referenceStructure(
                COMMUNITY_LIST,
                name,
                ROUTE_MAP_SET_COMMUNITY_COMMUNITY_LIST,
                ctx.getStart().getLine()));
  }

  private static @Nonnull String toString(Community_list_nameContext ctx) {
    return ctx.getText();
  }

  private @Nonnull String toString(VariableContext ctx) {
    return ctx.getText();
  }

  private static @Nonnull String toString(Vrf_nameContext ctx) {
    return ctx.getText();
  }

  @Override
  public void exitRmsc_none(Rmsc_noneContext ctx) {
    _currentRouteMapClause.clearSetCommunity();
    _currentRouteMapClause.setSetCommunityNone(RouteMapSetCommunityNone.instance());
  }

  @Override
  public void exitRmsc_communities(Rmsc_communitiesContext ctx) {
    List<StandardCommunity> communities =
        ctx.communities.stream()
            .map(AristaControlPlaneExtractor::toStandardCommunity)
            .collect(ImmutableList.toImmutableList());
    if (ctx.DELETE() != null) {
      extractSetCommunityDelete(communities);
    } else if (ctx.ADDITIVE() != null) {
      extractSetCommunityAdditive(communities);
    } else {
      RouteMapSetCommunityDelete del = _currentRouteMapClause.getSetCommunityDelete();
      boolean add = _currentRouteMapClause.getAdditive();
      if (add && del != null) {
        // delete is set, and either list or non-list additive is set
        warn(ctx, "set community ambiguous; specify either additive or delete");
        return;
      }
      if (add) {
        // list or non-list additive is set
        extractSetCommunityAdditive(communities);
      } else if (del != null) {
        // delete is set
        extractSetCommunityDelete(communities);
      } else {
        extractSetCommunity(communities);
      }
    }
  }

  private void extractSetCommunityAdditive(List<StandardCommunity> communities) {
    List<StandardCommunity> finalCommunities = communities;
    RouteMapSetCommunity old = _currentRouteMapClause.getSetCommunity();
    if (old != null) {
      finalCommunities =
          ImmutableList.<StandardCommunity>builder()
              .addAll(finalCommunities)
              .addAll(old.getCommunities())
              .build();
    }
    _currentRouteMapClause.clearNonDeleteSetCommunity();
    _currentRouteMapClause.setSetCommunity(new RouteMapSetCommunity(finalCommunities, true));
  }

  private void extractSetCommunityDelete(List<StandardCommunity> communities) {
    _currentRouteMapClause.clearNonAdditiveSetCommunity();
    _currentRouteMapClause.setSetCommunityListDelete(null);
    _currentRouteMapClause.setSetCommunityDelete(new RouteMapSetCommunityDelete(communities));
  }

  private void extractSetCommunity(List<StandardCommunity> communities) {
    List<StandardCommunity> finalCommunities = communities;
    RouteMapSetCommunity old = _currentRouteMapClause.getSetCommunity();
    if (old != null) {
      finalCommunities =
          ImmutableList.<StandardCommunity>builder()
              .addAll(finalCommunities)
              .addAll(old.getCommunities())
              .build();
    }
    _currentRouteMapClause.clearSetCommunity();
    _currentRouteMapClause.setSetCommunity(new RouteMapSetCommunity(finalCommunities, false));
  }

  @Override
  public void exitSet_local_preference_rm_stanza(Set_local_preference_rm_stanzaContext ctx) {
    LongExpr localPreference = toLocalPreferenceLongExpr(ctx.pref);
    RouteMapSetLocalPreferenceLine line = new RouteMapSetLocalPreferenceLine(localPreference);
    _currentRouteMapClause.addSetLine(line);
  }

  @Override
  public void exitSet_metric_rm_stanza(Set_metric_rm_stanzaContext ctx) {
    LongExpr metric = toMetricLongExpr(ctx.metric);
    RouteMapSetMetricLine line = new RouteMapSetMetricLine(metric);
    _currentRouteMapClause.addSetLine(line);
  }

  @Override
  public void exitSet_metric_type_rm_stanza(Set_metric_type_rm_stanzaContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitSet_next_hop_peer_address_stanza(Set_next_hop_peer_address_stanzaContext ctx) {
    RouteMapSetNextHopPeerAddress line = new RouteMapSetNextHopPeerAddress();
    _currentRouteMapClause.addSetLine(line);
  }

  @Override
  public void exitSet_next_hop_rm_stanza(Set_next_hop_rm_stanzaContext ctx) {
    List<Ip> nextHops = new ArrayList<>();
    for (Token t : ctx.nexthop_list) {
      Ip nextHop = toIp(t);
      nextHops.add(nextHop);
    }
    RouteMapSetNextHopLine line = new RouteMapSetNextHopLine(nextHops);
    _currentRouteMapClause.addSetLine(line);
  }

  @Override
  public void exitSet_origin_rm_stanza(Set_origin_rm_stanzaContext ctx) {
    OriginExpr originExpr = toOriginExpr(ctx.origin_expr_literal());
    RouteMapSetLine line = new RouteMapSetOriginTypeLine(originExpr);
    _currentRouteMapClause.addSetLine(line);
  }

  @Override
  public void exitSet_tag_rm_stanza(Set_tag_rm_stanzaContext ctx) {
    long tag = toLong(ctx.tag);
    _currentRouteMapClause.addSetLine(new RouteMapSetTagLine(tag));
  }

  @Override
  public void exitSet_weight_rm_stanza(Set_weight_rm_stanzaContext ctx) {
    RouteMapSetWeightLine line = new RouteMapSetWeightLine(toInteger(ctx.dec()));
    _currentRouteMapClause.addSetLine(line);
  }

  @Override
  public void exitSntp_server(Sntp_serverContext ctx) {
    Sntp sntp = _configuration.getCf().getSntp();
    String hostname = ctx.hostname.getText();
    SntpServer server = sntp.getServers().computeIfAbsent(hostname, SntpServer::new);
    if (ctx.version != null) {
      int version = toInteger(ctx.version);
      server.setVersion(version);
    }
  }

  @Override
  public void exitSpanning_tree_portfast(Spanning_tree_portfastContext ctx) {
    if (ctx.defaultLiteral != null) {
      _configuration.setSpanningTreePortfastDefault(true);
    }
  }

  @Override
  public void exitSs_community(Ss_communityContext ctx) {
    _currentSnmpCommunity = null;
  }

  @Override
  public void exitSs_enable_traps(Ss_enable_trapsContext ctx) {
    if (ctx.snmp_trap_type != null) {
      String trapName = ctx.snmp_trap_type.getText();
      SortedSet<String> subfeatureNames =
          ctx.subfeature.stream().map(RuleContext::getText).collect(toCollection(TreeSet::new));
      SortedMap<String, SortedSet<String>> traps = _configuration.getSnmpServer().getTraps();
      SortedSet<String> subfeatures = traps.get(trapName);
      if (subfeatures == null) {
        traps.put(trapName, subfeatureNames);
      } else {
        subfeatures.addAll(subfeatureNames);
      }
    }
  }

  @Override
  public void exitSs_file_transfer(Ss_file_transferContext ctx) {
    String acl = ctx.acl.getText();
    int line = ctx.acl.getStart().getLine();
    _configuration.referenceStructure(IP_ACCESS_LIST, acl, SNMP_SERVER_FILE_TRANSFER_ACL, line);
  }

  @Override
  public void exitSs_source_interface(Ss_source_interfaceContext ctx) {
    String ifaceName = getCanonicalInterfaceName(ctx.iname.getText());
    _configuration.setSnmpSourceInterface(ifaceName);
    _configuration.referenceStructure(
        INTERFACE, ifaceName, SNMP_SERVER_SOURCE_INTERFACE, ctx.iname.getStart().getLine());
  }

  @Override
  public void exitSs_tftp_server_list(Ss_tftp_server_listContext ctx) {
    String acl = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _configuration.referenceStructure(IP_ACCESS_LIST, acl, SNMP_SERVER_TFTP_SERVER_LIST, line);
  }

  @Override
  public void exitSs_trap_source(Ss_trap_sourceContext ctx) {
    String ifaceName = getCanonicalInterfaceName(ctx.iname.getText());
    _configuration.setSnmpSourceInterface(ifaceName);
    _configuration.referenceStructure(
        IP_ACCESS_LIST, ifaceName, SNMP_SERVER_TRAP_SOURCE, ctx.iname.getStart().getLine());
  }

  @Override
  public void exitSsc_access_control(Ssc_access_controlContext ctx) {
    int line;
    if (ctx.acl4 != null) {
      String name = ctx.acl4.getText();
      line = ctx.acl4.getStart().getLine();
      _currentSnmpCommunity.setAccessList(name);
      _configuration.referenceStructure(IPV4_ACCESS_LIST, name, SNMP_SERVER_COMMUNITY_ACL4, line);
    }
    if (ctx.acl6 != null) {
      String name = ctx.acl6.getText();
      line = ctx.acl6.getStart().getLine();
      _currentSnmpCommunity.setAccessList6(name);
      _configuration.referenceStructure(IPV6_ACCESS_LIST, name, SNMP_SERVER_COMMUNITY_ACL6, line);
    }
    if (ctx.readonly != null) {
      _currentSnmpCommunity.setRo(true);
    }
    if (ctx.readwrite != null) {
      _currentSnmpCommunity.setRw(true);
    }
  }

  @Override
  public void exitSsc_use_ipv4_acl(Ssc_use_ipv4_aclContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.name.getStart().getLine();
    _currentSnmpCommunity.setAccessList(name);
    _configuration.referenceStructure(IPV4_ACCESS_LIST, name, SNMP_SERVER_COMMUNITY_ACL4, line);
  }

  @Override
  public void exitStandard_ipv6_access_list_stanza(Standard_ipv6_access_list_stanzaContext ctx) {
    _currentStandardIpv6Acl = null;
  }

  @Override
  public void exitStandard_ipv6_access_list_tail(Standard_ipv6_access_list_tailContext ctx) {
    LineAction action = toLineAction(ctx.ala);
    Ip6Wildcard srcIps = getIpWildcard(ctx.ipr);
    Set<Integer> dscps = new TreeSet<>();
    Set<Integer> ecns = new TreeSet<>();
    for (Standard_access_list_additional_featureContext feature : ctx.features) {
      if (feature.DSCP() != null) {
        int dscpType = toDscpType(feature.dscp_type());
        dscps.add(dscpType);
      } else if (feature.ECN() != null) {
        int ecn = toInteger(feature.ecn);
        ecns.add(ecn);
      }
    }
    String name;
    if (ctx.num != null) {
      name = ctx.num.getText();
    } else {
      name = getFullText(ctx).trim();
    }
    StandardIpv6AccessListLine line =
        new StandardIpv6AccessListLine(name, action, srcIps, dscps, ecns);
    _currentStandardIpv6Acl.addLine(line);
  }

  @Override
  public void exitSummary_address_is_stanza(Summary_address_is_stanzaContext ctx) {
    Ip ip = toIp(ctx.ip);
    Ip mask = toIp(ctx.mask);
    Prefix prefix = Prefix.create(ip, mask);
    RedistributionSourceProtocol sourceProtocol = RedistributionSourceProtocol.ISIS_L1;
    IsisRedistributionPolicy r = new IsisRedistributionPolicy(sourceProtocol);
    r.setSummaryPrefix(prefix);
    _currentIsisProcess.getRedistributionPolicies().put(sourceProtocol, r);
    if (ctx.metric != null) {
      int metric = toInteger(ctx.metric);
      r.setMetric(metric);
    }
    if (!ctx.LEVEL_1().isEmpty()) {
      r.setLevel(IsisLevel.LEVEL_1);
    } else if (!ctx.LEVEL_2().isEmpty()) {
      r.setLevel(IsisLevel.LEVEL_2);
    } else if (!ctx.LEVEL_1_2().isEmpty()) {
      r.setLevel(IsisLevel.LEVEL_1_2);
    } else {
      r.setLevel(IsisRedistributionPolicy.DEFAULT_LEVEL);
    }
  }

  @Override
  public void exitSuppressed_iis_stanza(Suppressed_iis_stanzaContext ctx) {
    if (ctx.NO() != null) {
      _currentIsisInterface.setIsisInterfaceMode(IsisInterfaceMode.SUPPRESSED);
    }
  }

  @Override
  public void exitSwitching_mode_stanza(Switching_mode_stanzaContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitU_password(U_passwordContext ctx) {
    String passwordString;
    if (ctx.up_arista_md5() != null) {
      passwordString = ctx.up_arista_md5().pass.getText();
    } else if (ctx.up_arista_sha512() != null) {
      passwordString = ctx.up_arista_sha512().pass.getText();
    } else if (ctx.NOPASSWORD() != null) {
      passwordString = "";
    } else {
      throw new BatfishException("Missing username password handling");
    }
    String passwordRehash = CommonUtil.sha256Digest(passwordString + CommonUtil.salt());
    _currentUser.setPassword(passwordRehash);
  }

  @Override
  public void exitU_role(U_roleContext ctx) {
    String role = ctx.role.getText();
    _currentUser.setRole(role);
  }

  @Override
  public void exitVrfd_description(Vrfd_descriptionContext ctx) {
    currentVrf().setDescription(getDescription(ctx.description_line()));
  }

  @Override
  public void exitWccp_id(Wccp_idContext ctx) {
    if (ctx.group_list != null) {
      String name = ctx.group_list.getText();
      int line = ctx.group_list.getStart().getLine();
      _configuration.referenceStructure(IP_ACCESS_LIST, name, WCCP_GROUP_LIST, line);
    }
    if (ctx.redirect_list != null) {
      String name = ctx.redirect_list.getText();
      int line = ctx.redirect_list.getStart().getLine();
      _configuration.referenceStructure(IP_ACCESS_LIST, name, WCCP_REDIRECT_LIST, line);
    }
    if (ctx.service_list != null) {
      String name = ctx.service_list.getText();
      int line = ctx.service_list.getStart().getLine();
      _configuration.referenceStructure(IP_ACCESS_LIST, name, WCCP_SERVICE_LIST, line);
    }
  }

  @Override
  public void exitIf_ip_virtual_router(If_ip_virtual_routerContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitS_ip_virtual_router(S_ip_virtual_routerContext ctx) {
    todo(ctx);
  }

  private String getCanonicalInterfaceName(String ifaceName) {
    return _configuration.canonicalizeInterfaceName(ifaceName);
  }

  private int getPortNumber(PortContext ctx) {
    if (ctx.dec() != null) {
      return toInteger(ctx.dec());
    } else {
      NamedPort namedPort = toNamedPort(ctx);
      return namedPort.number();
    }
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  private void initInterface(Interface iface, Interface_nameContext ctx) {
    String nameAlpha = ctx.name_prefix_alpha.getText();
    String canonicalNamePrefix = AristaConfiguration.getCanonicalInterfaceNamePrefix(nameAlpha);
    String vrf =
        canonicalNamePrefix.equals(AristaConfiguration.MANAGEMENT_INTERFACE_PREFIX)
            ? AristaConfiguration.MANAGEMENT_VRF_NAME
            : AristaConfiguration.DEFAULT_VRF_NAME;
    int mtu = Interface.getDefaultMtu();
    iface.setVrf(vrf);
    initVrf(vrf);
    iface.setMtu(mtu);
  }

  private Vrf initVrf(String vrfName) {
    return _configuration.getVrfs().computeIfAbsent(vrfName, Vrf::new);
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
    walker.walk(this, tree);
  }

  private AsExpr toAsExpr(As_exprContext ctx) {
    if (ctx.asn != null) {
      long as = toAsNum(ctx.asn);
      return new ExplicitAs(as);
    } else if (ctx.AUTO() != null) {
      return AutoAs.instance();
    } else {
      throw convError(AsExpr.class, ctx);
    }
  }

  private IpsecAuthenticationAlgorithm toIpsecAuthenticationAlgorithm(
      Ipsec_authenticationContext ctx) {
    if (ctx.ESP_MD5_HMAC() != null || ctx.AH_MD5_HMAC() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_MD5_96;
    } else if (ctx.ESP_SHA_HMAC() != null || ctx.AH_SHA_HMAC() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_SHA1_96;
    } else if (ctx.ESP_SHA256_HMAC() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_SHA_256_128;
    } else if (ctx.ESP_SHA512_HMAC() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_SHA_512;
    } else {
      throw convError(IpsecAuthenticationAlgorithm.class, ctx);
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
    } else if (ctx.GROUP21() != null) {
      return DiffieHellmanGroup.GROUP21;
    } else if (ctx.GROUP24() != null) {
      return DiffieHellmanGroup.GROUP24;
    } else if (ctx.GROUP5() != null) {
      return DiffieHellmanGroup.GROUP5;
    } else {
      throw convError(DiffieHellmanGroup.class, ctx);
    }
  }

  private int toDscpType(Dscp_typeContext ctx) {
    int val;
    if (ctx.dec() != null) {
      val = toInteger(ctx.dec());
    } else if (ctx.AF11() != null) {
      val = DscpType.AF11.number();
    } else if (ctx.AF12() != null) {
      val = DscpType.AF12.number();
    } else if (ctx.AF13() != null) {
      val = DscpType.AF13.number();
    } else if (ctx.AF21() != null) {
      val = DscpType.AF21.number();
    } else if (ctx.AF22() != null) {
      val = DscpType.AF22.number();
    } else if (ctx.AF23() != null) {
      val = DscpType.AF23.number();
    } else if (ctx.AF31() != null) {
      val = DscpType.AF31.number();
    } else if (ctx.AF32() != null) {
      val = DscpType.AF32.number();
    } else if (ctx.AF33() != null) {
      val = DscpType.AF33.number();
    } else if (ctx.AF41() != null) {
      val = DscpType.AF41.number();
    } else if (ctx.AF42() != null) {
      val = DscpType.AF42.number();
    } else if (ctx.AF43() != null) {
      val = DscpType.AF43.number();
    } else if (ctx.CS1() != null) {
      val = DscpType.CS1.number();
    } else if (ctx.CS2() != null) {
      val = DscpType.CS2.number();
    } else if (ctx.CS3() != null) {
      val = DscpType.CS3.number();
    } else if (ctx.CS4() != null) {
      val = DscpType.CS4.number();
    } else if (ctx.CS5() != null) {
      val = DscpType.CS5.number();
    } else if (ctx.CS6() != null) {
      val = DscpType.CS6.number();
    } else if (ctx.CS7() != null) {
      val = DscpType.CS7.number();
    } else if (ctx.DEFAULT() != null) {
      val = DscpType.DEFAULT.number();
    } else if (ctx.EF() != null) {
      val = DscpType.EF.number();
    } else {
      throw convError(DscpType.class, ctx);
    }
    return val;
  }

  private SwitchportEncapsulationType toEncapsulation(Switchport_trunk_encapsulationContext ctx) {
    if (ctx.DOT1Q() != null) {
      return SwitchportEncapsulationType.DOT1Q;
    } else if (ctx.ISL() != null) {
      return SwitchportEncapsulationType.ISL;
    } else if (ctx.NEGOTIATE() != null) {
      return SwitchportEncapsulationType.NEGOTIATE;
    } else {
      throw convError(SwitchportEncapsulationType.class, ctx);
    }
  }

  private EncryptionAlgorithm toEncryptionAlgorithm(Ike_encryptionContext ctx) {
    if (ctx.AES() != null) {
      int strength = ctx.strength == null ? 128 : toInteger(ctx.strength);
      switch (strength) {
        case 128:
          return EncryptionAlgorithm.AES_128_CBC;
        case 192:
          return EncryptionAlgorithm.AES_192_CBC;
        case 256:
          return EncryptionAlgorithm.AES_256_CBC;
        default:
          throw convError(EncryptionAlgorithm.class, ctx);
      }
    } else if (ctx.DES() != null) {
      return EncryptionAlgorithm.DES_CBC;
    } else if (ctx.THREE_DES() != null) {
      return EncryptionAlgorithm.THREEDES_CBC;
    } else {
      throw convError(EncryptionAlgorithm.class, ctx);
    }
  }

  private EncryptionAlgorithm toEncryptionAlgorithm(Ike_encryption_arubaContext ctx) {
    if (ctx.AES128() != null) {
      return EncryptionAlgorithm.AES_128_CBC;
    } else if (ctx.AES192() != null) {
      return EncryptionAlgorithm.AES_192_CBC;
    } else if (ctx.AES256() != null) {
      return EncryptionAlgorithm.AES_256_CBC;
    } else if (ctx.DES() != null) {
      return EncryptionAlgorithm.DES_CBC;
    } else if (ctx.THREE_DES() != null) {
      return EncryptionAlgorithm.THREEDES_CBC;
    } else {
      throw convError(EncryptionAlgorithm.class, ctx);
    }
  }

  private EncryptionAlgorithm toEncryptionAlgorithm(Ipsec_encryptionContext ctx) {
    if (ctx.ESP_AES() != null) {
      int strength = ctx.strength == null ? 128 : toInteger(ctx.strength);
      switch (strength) {
        case 128:
          return EncryptionAlgorithm.AES_128_CBC;
        case 192:
          return EncryptionAlgorithm.AES_192_CBC;
        case 256:
          return EncryptionAlgorithm.AES_256_CBC;
        default:
          throw convError(EncryptionAlgorithm.class, ctx);
      }
    } else if (ctx.ESP_DES() != null) {
      return EncryptionAlgorithm.DES_CBC;
    } else if (ctx.ESP_3DES() != null) {
      return EncryptionAlgorithm.THREEDES_CBC;
    } else if (ctx.ESP_GCM() != null) {
      int strength = ctx.strength == null ? 128 : toInteger(ctx.strength);
      switch (strength) {
        case 128:
          return EncryptionAlgorithm.AES_128_GCM;
        case 192:
          return EncryptionAlgorithm.AES_192_GCM;
        case 256:
          return EncryptionAlgorithm.AES_256_GCM;
        default:
          throw convError(EncryptionAlgorithm.class, ctx);
      }
    } else if (ctx.ESP_GMAC() != null) {
      int strength = ctx.strength == null ? 128 : toInteger(ctx.strength);
      switch (strength) {
        case 128:
          return EncryptionAlgorithm.AES_128_GMAC;
        case 192:
          return EncryptionAlgorithm.AES_192_GMAC;
        case 256:
          return EncryptionAlgorithm.AES_256_GMAC;
        default:
          throw convError(EncryptionAlgorithm.class, ctx);
      }
    } else if (ctx.ESP_NULL() != null) {
      return EncryptionAlgorithm.NULL;
    } else if (ctx.ESP_SEAL() != null) {
      return EncryptionAlgorithm.SEAL_160;
    } else {
      throw convError(EncryptionAlgorithm.class, ctx);
    }
  }

  private EncryptionAlgorithm toEncryptionAlgorithm(Ipsec_encryption_arubaContext ctx) {
    if (ctx.ESP_AES128() != null) {
      return EncryptionAlgorithm.AES_128_CBC;
    } else if (ctx.ESP_AES128_GCM() != null) {
      return EncryptionAlgorithm.AES_128_GCM;
    } else if (ctx.ESP_AES192() != null) {
      return EncryptionAlgorithm.AES_192_CBC;
    } else if (ctx.ESP_AES256() != null) {
      return EncryptionAlgorithm.AES_256_CBC;
    } else if (ctx.ESP_AES256_GCM() != null) {
      return EncryptionAlgorithm.AES_256_GCM;
    } else if (ctx.ESP_DES() != null) {
      return EncryptionAlgorithm.DES_CBC;
    } else if (ctx.ESP_3DES() != null) {
      return EncryptionAlgorithm.THREEDES_CBC;
    } else {
      throw convError(EncryptionAlgorithm.class, ctx);
    }
  }

  /** Returns the given IPv4 protocol, or {@code null} if none is specified. */
  private @Nullable IpProtocol toIpProtocol(ProtocolContext ctx) {
    if (ctx.dec() != null) {
      int num = toInteger(ctx.dec());
      if (num < 0 || num > 255) {
        return convProblem(IpProtocol.class, ctx, null);
      }
      return IpProtocol.fromNumber(num);
    } else if (ctx.AH() != null || ctx.AHP() != null) {
      // Different Cisco variants use `ahp` or `ah` to mean the IPSEC authentication header protocol
      return IpProtocol.AHP;
    } else if (ctx.ESP() != null) {
      return IpProtocol.ESP;
    } else if (ctx.GRE() != null) {
      return IpProtocol.GRE;
    } else if (ctx.ICMP() != null) {
      return IpProtocol.ICMP;
    } else if (ctx.ICMP6() != null || ctx.ICMPV6() != null) {
      return IpProtocol.IPV6_ICMP;
    } else if (ctx.IGMP() != null) {
      return IpProtocol.IGMP;
    } else if (ctx.IP() != null) {
      return null;
    } else if (ctx.IPINIP() != null) {
      return IpProtocol.IPINIP;
    } else if (ctx.IPV4() != null) {
      return null;
    } else if (ctx.IPV6() != null) {
      return IpProtocol.IPV6;
    } else if (ctx.ND() != null) {
      return IpProtocol.IPV6_ICMP;
    } else if (ctx.OSPF() != null) {
      return IpProtocol.OSPF;
    } else if (ctx.PIM() != null) {
      return IpProtocol.PIM;
    } else if (ctx.SCTP() != null) {
      return IpProtocol.SCTP;
    } else if (ctx.TCP() != null) {
      return IpProtocol.TCP;
    } else if (ctx.UDP() != null) {
      return IpProtocol.UDP;
    } else if (ctx.VRRP() != null) {
      return IpProtocol.VRRP;
    } else {
      throw convError(IpProtocol.class, ctx);
    }
  }

  private LineAction toLineAction(Access_list_actionContext ctx) {
    if (ctx.PERMIT() != null) {
      return LineAction.PERMIT;
    } else if (ctx.DENY() != null) {
      return LineAction.DENY;
    } else {
      throw convError(LineAction.class, ctx);
    }
  }

  private IpAsPathAccessListLine.OriginType toOriginType(Ipap_originContext ctx) {
    if (ctx == null || ctx.ANY() != null) {
      return IpAsPathAccessListLine.OriginType.ANY;
    } else if (ctx.EGP() != null) {
      return IpAsPathAccessListLine.OriginType.EGP;
    } else if (ctx.IGP() != null) {
      return IpAsPathAccessListLine.OriginType.IGP;
    } else {
      assert ctx.INCOMPLETE() != null;
      return IpAsPathAccessListLine.OriginType.INCOMPLETE;
    }
  }

  private LongExpr toLocalPreferenceLongExpr(Int_exprContext ctx) {
    if (ctx.dec() != null) {
      long val = toLong(ctx.dec());
      if (ctx.PLUS() != null) {
        return new IncrementLocalPreference(val);
      } else if (ctx.DASH() != null) {
        return new DecrementLocalPreference(val);
      } else {
        return new LiteralLong(val);
      }
    } else {
      /*
       * Unsupported local-preference integer expression - do not add cases
       * unless you know what you are doing
       */
      throw convError(LongExpr.class, ctx);
    }
  }

  private static @Nonnull StandardCommunity toStandardCommunity(
      Literal_standard_communityContext ctx) {
    if (ctx.u32 != null) {
      return StandardCommunity.of(toLong(ctx.u32));
    } else if (ctx.lo != null) {
      assert ctx.hi != null;
      int lo = toInteger(ctx.lo);
      int hi = toInteger(ctx.hi);
      return StandardCommunity.of(hi, lo);
    } else if (ctx.GSHUT() != null) {
      return StandardCommunity.GRACEFUL_SHUTDOWN;
    } else if (ctx.INTERNET() != null) {
      return StandardCommunity.INTERNET;
    } else if (ctx.LOCAL_AS() != null) {
      // Cisco LOCAL_AS is interpreted as RFC1997 NO_EXPORT_SUBCONFED: internet forums.
      return StandardCommunity.NO_EXPORT_SUBCONFED;
    } else if (ctx.NO_ADVERTISE() != null) {
      return StandardCommunity.NO_ADVERTISE;
    } else {
      assert ctx.NO_EXPORT() != null;
      return StandardCommunity.NO_EXPORT;
    }
  }

  /**
   * Convert a {@link ParserRuleContext} whose text is guaranteed to represent a valid signed 64-bit
   * decimal integer to a {@link Long} if it is contained in the provided {@code space}, or else
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

  private @Nullable Long toLong(DecContext ctx) {
    try {
      long val = Long.parseLong(ctx.getText());
      checkArgument(0 <= val && val <= 0xFFFFFFFFL);
      return val;
    } catch (IllegalArgumentException e) {
      return convProblem(Long.class, ctx, null);
    }
  }

  private static long toLong(Uint32Context ctx) {
    return Long.parseLong(ctx.getText());
  }

  private LongExpr toMetricLongExpr(Int_exprContext ctx) {
    if (ctx.dec() != null) {
      long val = toLong(ctx.dec());
      if (ctx.PLUS() != null) {
        return new IncrementMetric(val);
      } else if (ctx.DASH() != null) {
        return new DecrementMetric(val);
      } else {
        return new LiteralLong(val);
      }
    } else if (ctx.IGP_COST() != null) {
      return new IgpCost();
    } else {
      /*
       * Unsupported metric long expression - do not add cases unless you
       * know what you are doing
       */
      throw convError(LongExpr.class, ctx);
    }
  }

  private NamedPort toNamedPort(PortContext ctx) {
    if (ctx.ACAP() != null) {
      return NamedPort.ACAP;
    } else if (ctx.ACR_NEMA() != null) {
      return NamedPort.ACR_NEMA;
    } else if (ctx.AFPOVERTCP() != null) {
      return NamedPort.AFPOVERTCP;
    } else if (ctx.AOL() != null) {
      return NamedPort.AOL;
    } else if (ctx.ARNS() != null) {
      return NamedPort.ARNS;
    } else if (ctx.ASF_RMCP() != null) {
      return NamedPort.ASF_RMCP;
    } else if (ctx.ASIP_WEBADMIN() != null) {
      return NamedPort.ASIP_WEBADMIN;
    } else if (ctx.AT_RTMP() != null) {
      return NamedPort.AT_RTMP;
    } else if (ctx.AURP() != null) {
      return NamedPort.AURP;
    } else if (ctx.AUTH() != null) {
      return NamedPort.IDENT;
    } else if (ctx.BFD() != null) {
      return NamedPort.BFD_CONTROL;
    } else if (ctx.BFD_ECHO() != null) {
      return NamedPort.BFD_ECHO;
    } else if (ctx.BFTP() != null) {
      return NamedPort.BFTP;
    } else if (ctx.BGMP() != null) {
      return NamedPort.BGMP;
    } else if (ctx.BGP() != null) {
      return NamedPort.BGP;
    } else if (ctx.BIFF() != null) {
      return NamedPort.BIFFudp_OR_EXECtcp;
    } else if (ctx.BOOTPC() != null) {
      return NamedPort.BOOTPC;
    } else if (ctx.BOOTPS() != null) {
      return NamedPort.BOOTPS_OR_DHCP;
    } else if (ctx.CHARGEN() != null) {
      return NamedPort.CHARGEN;
    } else if (ctx.CIFS() != null) {
      return NamedPort.CIFS;
    } else if (ctx.CISCO_TDP() != null) {
      return NamedPort.CISCO_TDP;
    } else if (ctx.CITADEL() != null) {
      return NamedPort.CITADEL;
    } else if (ctx.CITRIX_ICA() != null) {
      return NamedPort.CITRIX_ICA;
    } else if (ctx.CLEARCASE() != null) {
      return NamedPort.CLEARCASE;
    } else if (ctx.CMD() != null) {
      return NamedPort.CMDtcp_OR_SYSLOGudp;
    } else if (ctx.COMMERCE() != null) {
      return NamedPort.COMMERCE;
    } else if (ctx.CSNET_NS() != null) {
      return NamedPort.CSNET_NS;
    } else if (ctx.CTIQBE() != null) {
      return NamedPort.CTIQBE;
    } else if (ctx.CVX() != null) {
      return NamedPort.CVX;
    } else if (ctx.CVX_CLUSTER() != null) {
      return NamedPort.CVX_CLUSTER;
    } else if (ctx.CVX_LICENSE() != null) {
      return NamedPort.CVX_LICENSE;
    } else if (ctx.DAYTIME() != null) {
      return NamedPort.DAYTIME;
    } else if (ctx.DHCP_FAILOVER2() != null) {
      return NamedPort.DHCP_FAILOVER2;
    } else if (ctx.DHCPV6_CLIENT() != null) {
      return NamedPort.DHCPV6_CLIENT;
    } else if (ctx.DHCPV6_SERVER() != null) {
      return NamedPort.DHCPV6_SERVER;
    } else if (ctx.DISCARD() != null) {
      return NamedPort.DISCARD;
    } else if (ctx.DNSIX() != null) {
      return NamedPort.DNSIX;
    } else if (ctx.DOMAIN() != null) {
      return NamedPort.DOMAIN;
    } else if (ctx.DSP() != null) {
      return NamedPort.DSP;
    } else if (ctx.ECHO() != null) {
      return NamedPort.ECHO;
    } else if (ctx.EFS() != null) {
      return NamedPort.EFStcp_OR_RIPudp;
    } else if (ctx.EPP() != null) {
      return NamedPort.EPP;
    } else if (ctx.ESRO_GEN() != null) {
      return NamedPort.ESRO_GEN;
    } else if (ctx.EXEC() != null) {
      return NamedPort.BIFFudp_OR_EXECtcp;
    } else if (ctx.FINGER() != null) {
      return NamedPort.FINGER;
    } else if (ctx.FTP() != null) {
      return NamedPort.FTP;
    } else if (ctx.FTP_DATA() != null) {
      return NamedPort.FTP_DATA;
    } else if (ctx.FTPS() != null) {
      return NamedPort.FTPS;
    } else if (ctx.FTPS_DATA() != null) {
      return NamedPort.FTPS_DATA;
    } else if (ctx.GODI() != null) {
      return NamedPort.GODI;
    } else if (ctx.GOPHER() != null) {
      return NamedPort.GOPHER;
    } else if (ctx.GTP_C() != null) {
      return NamedPort.GPRS_GTP_C;
    } else if (ctx.GTP_PRIME() != null) {
      return NamedPort.GPRS_GTP_V0;
    } else if (ctx.GTP_U() != null) {
      return NamedPort.GPRS_GTP_U;
    } else if (ctx.H323() != null) {
      return NamedPort.H323;
    } else if (ctx.HA_CLUSTER() != null) {
      return NamedPort.HA_CLUSTER;
    } else if (ctx.HOSTNAME() != null) {
      return NamedPort.HOSTNAME;
    } else if (ctx.HP_ALARM_MGR() != null) {
      return NamedPort.HP_ALARM_MGR;
    } else if (ctx.HTTP() != null) {
      return NamedPort.HTTP;
    } else if (ctx.HTTP_ALT() != null) {
      return NamedPort.HTTP_ALT;
    } else if (ctx.HTTP_MGMT() != null) {
      return NamedPort.HTTP_MGMT;
    } else if (ctx.HTTP_RPC_EPMAP() != null) {
      return NamedPort.HTTP_RPC_EPMAP;
    } else if (ctx.HTTPS() != null) {
      return NamedPort.HTTPS;
    } else if (ctx.IDENT() != null) {
      return NamedPort.IDENT;
    } else if (ctx.IMAP() != null) {
      return NamedPort.IMAP;
    } else if (ctx.IMAP3() != null) {
      return NamedPort.IMAP3;
    } else if (ctx.IMAP4() != null) {
      return NamedPort.IMAP;
    } else if (ctx.IMAPS() != null) {
      return NamedPort.IMAPS;
    } else if (ctx.IPP() != null) {
      return NamedPort.IPP;
    } else if (ctx.IPX() != null) {
      return NamedPort.IPX;
    } else if (ctx.IRC() != null) {
      return NamedPort.IRC;
    } else if (ctx.IRIS_BEEP() != null) {
      return NamedPort.IRIS_BEEP;
    } else if (ctx.ISAKMP() != null) {
      return NamedPort.ISAKMP;
    } else if (ctx.ISCSI() != null) {
      return NamedPort.ISCSI;
    } else if (ctx.ISI_GL() != null) {
      return NamedPort.ISI_GL;
    } else if (ctx.ISO_TSAP() != null) {
      return NamedPort.ISO_TSAP;
    } else if (ctx.KERBEROS() != null) {
      if (_format == ARISTA) {
        return NamedPort.KERBEROS_SEC;
      } else {
        return NamedPort.KERBEROS;
      }
    } else if (ctx.KERBEROS_ADM() != null) {
      return NamedPort.KERBEROS_ADM;
    } else if (ctx.KLOGIN() != null) {
      return NamedPort.KLOGIN;
    } else if (ctx.KPASSWD() != null) {
      return NamedPort.KPASSWDV5;
    } else if (ctx.KSHELL() != null) {
      return NamedPort.KSHELL;
    } else if (ctx.L2TP() != null) {
      return NamedPort.L2TP;
    } else if (ctx.LA_MAINT() != null) {
      return NamedPort.LA_MAINT;
    } else if (ctx.LANZ() != null) {
      return NamedPort.LANZ;
    } else if (ctx.LDAP() != null) {
      return NamedPort.LDAP;
    } else if (ctx.LDAPS() != null) {
      return NamedPort.LDAPS;
    } else if (ctx.LDP() != null) {
      return NamedPort.LDP;
    } else if (ctx.LMP() != null) {
      return NamedPort.LMP;
    } else if (ctx.LPD() != null) {
      return NamedPort.LPD;
    } else if (ctx.LOGIN() != null) {
      return NamedPort.LOGINtcp_OR_WHOudp;
    } else if (ctx.LOTUSNOTES() != null) {
      return NamedPort.LOTUSNOTES;
    } else if (ctx.MAC_SRVR_ADMIN() != null) {
      return NamedPort.MAC_SRVR_ADMIN;
    } else if (ctx.MATIP_TYPE_A() != null) {
      return NamedPort.MATIP_TYPE_A;
    } else if (ctx.MATIP_TYPE_B() != null) {
      return NamedPort.MATIP_TYPE_B;
    } else if (ctx.MICRO_BFD() != null) {
      return NamedPort.BFD_LAG;
    } else if (ctx.MICROSOFT_DS() != null) {
      return NamedPort.MICROSOFT_DS;
    } else if (ctx.MLAG() != null) {
      return NamedPort.MLAG;
    } else if (ctx.MOBILE_IP() != null) {
      return NamedPort.MOBILE_IP_AGENT;
    } else if (ctx.MPP() != null) {
      return NamedPort.MPP;
    } else if (ctx.MS_SQL_M() != null) {
      return NamedPort.MS_SQL_M;
    } else if (ctx.MS_SQL_S() != null) {
      return NamedPort.MS_SQL;
    } else if (ctx.MSDP() != null) {
      return NamedPort.MSDP;
    } else if (ctx.MSEXCH_ROUTING() != null) {
      return NamedPort.MSEXCH_ROUTING;
    } else if (ctx.MSG_ICP() != null) {
      return NamedPort.MSG_ICP;
    } else if (ctx.MSP() != null) {
      return NamedPort.MSP;
    } else if (ctx.MSRPC() != null) {
      return NamedPort.MSRPC;
    } else if (ctx.NAS() != null) {
      return NamedPort.NAS;
    } else if (ctx.NAT() != null) {
      return NamedPort.NAT;
    } else if (ctx.NAMESERVER() != null) {
      return NamedPort.NAMESERVER;
    } else if (ctx.NCP() != null) {
      return NamedPort.NCP;
    } else if (ctx.NETBIOS_DGM() != null) {
      return NamedPort.NETBIOS_DGM;
    } else if (ctx.NETBIOS_NS() != null) {
      return NamedPort.NETBIOS_NS;
    } else if (ctx.NETBIOS_SS() != null) {
      return NamedPort.NETBIOS_SSN;
    } else if (ctx.NETBIOS_SSN() != null) {
      return NamedPort.NETBIOS_SSN;
    } else if (ctx.NETRJS_1() != null) {
      return NamedPort.NETRJS_1;
    } else if (ctx.NETRJS_2() != null) {
      return NamedPort.NETRJS_2;
    } else if (ctx.NETRJS_3() != null) {
      return NamedPort.NETRJS_3;
    } else if (ctx.NETRJS_4() != null) {
      return NamedPort.NETRJS_4;
    } else if (ctx.NETWALL() != null) {
      return NamedPort.NETWALL;
    } else if (ctx.NETWNEWS() != null) {
      return NamedPort.NETWNEWS;
    } else if (ctx.NEW_RWHO() != null) {
      return NamedPort.NEW_RWHO;
    } else if (ctx.NFS() != null) {
      return NamedPort.NFSD;
    } else if (ctx.NNTP() != null) {
      return NamedPort.NNTP;
    } else if (ctx.NNTPS() != null) {
      return NamedPort.NNTPS;
    } else if (ctx.NON500_ISAKMP() != null) {
      return NamedPort.NON500_ISAKMP;
    } else if (ctx.NSW_FE() != null) {
      return NamedPort.NSW_FE;
    } else if (ctx.NTP() != null) {
      return NamedPort.NTP;
    } else if (ctx.ODMR() != null) {
      return NamedPort.ODMR;
    } else if (ctx.OPENVPN() != null) {
      return NamedPort.OPENVPN;
    } else if (ctx.PCANYWHERE_DATA() != null) {
      return NamedPort.PCANYWHERE_DATA;
    } else if (ctx.PCANYWHERE_STATUS() != null) {
      return NamedPort.PCANYWHERE_STATUS;
    } else if (ctx.PIM_AUTO_RP() != null) {
      return NamedPort.PIM_AUTO_RP;
    } else if (ctx.PKIX_TIMESTAMP() != null) {
      return NamedPort.PKIX_TIMESTAMP;
    } else if (ctx.PKT_KRB_IPSEC() != null) {
      return NamedPort.IPSEC;
    } else if (ctx.OLSR() != null) {
      return NamedPort.OLSR;
    } else if (ctx.POP2() != null) {
      return NamedPort.POP2;
    } else if (ctx.POP3() != null) {
      return NamedPort.POP3;
    } else if (ctx.POP3S() != null) {
      return NamedPort.POP3S;
    } else if (ctx.PPTP() != null) {
      return NamedPort.PPTP;
    } else if (ctx.PRINT_SRV() != null) {
      return NamedPort.PRINT_SRV;
    } else if (ctx.PTP_EVENT() != null) {
      return NamedPort.PTP_EVENT;
    } else if (ctx.PTP_GENERAL() != null) {
      return NamedPort.PTP_GENERAL;
    } else if (ctx.QMTP() != null) {
      return NamedPort.QMTP;
    } else if (ctx.QOTD() != null) {
      return NamedPort.QOTD;
    } else if (ctx.RADIUS() != null) {
      if (_format == ARISTA) {
        return NamedPort.RADIUS_2_AUTH;
      } else {
        return NamedPort.RADIUS_1_AUTH;
      }
    } else if (ctx.RADIUS_ACCT() != null) {
      if (_format == ARISTA) {
        return NamedPort.RADIUS_2_ACCT;
      } else {
        return NamedPort.RADIUS_1_ACCT;
      }
    } else if (ctx.RE_MAIL_CK() != null) {
      return NamedPort.RE_MAIL_CK;
    } else if (ctx.REMOTEFS() != null) {
      return NamedPort.REMOTEFS;
    } else if (ctx.REPCMD() != null) {
      return NamedPort.REPCMD;
    } else if (ctx.RIP() != null) {
      return NamedPort.EFStcp_OR_RIPudp;
    } else if (ctx.RJE() != null) {
      return NamedPort.RJE;
    } else if (ctx.RLP() != null) {
      return NamedPort.RLP;
    } else if (ctx.RLZDBASE() != null) {
      return NamedPort.RLZDBASE;
    } else if (ctx.RMC() != null) {
      return NamedPort.RMC;
    } else if (ctx.RMONITOR() != null) {
      return NamedPort.RMONITOR;
    } else if (ctx.RPC2PORTMAP() != null) {
      return NamedPort.RPC2PORTMAP;
    } else if (ctx.RSH() != null) {
      return NamedPort.CMDtcp_OR_SYSLOGudp;
    } else if (ctx.RSYNC() != null) {
      return NamedPort.RSYNC;
    } else if (ctx.RTELNET() != null) {
      return NamedPort.RTELNET;
    } else if (ctx.RTSP() != null) {
      return NamedPort.RTSP;
    } else if (ctx.SECUREID_UDP() != null) {
      return NamedPort.SECUREID_UDP;
    } else if (ctx.SGMP() != null) {
      return NamedPort.SGMP;
    } else if (ctx.SILC() != null) {
      return NamedPort.SILC;
    } else if (ctx.SIP() != null) {
      return NamedPort.SIP_5060;
    } else if (ctx.SMTP() != null) {
      return NamedPort.SMTP;
    } else if (ctx.SMUX() != null) {
      return NamedPort.SMUX;
    } else if (ctx.SNAGAS() != null) {
      return NamedPort.SNAGAS;
    } else if (ctx.SNMP() != null) {
      return NamedPort.SNMP;
    } else if (ctx.SNMP_TRAP() != null) {
      return NamedPort.SNMPTRAP;
    } else if (ctx.SNMPTRAP() != null) {
      return NamedPort.SNMPTRAP;
    } else if (ctx.SNPP() != null) {
      return NamedPort.SNPP;
    } else if (ctx.SQLNET() != null) {
      return NamedPort.SQLNET;
    } else if (ctx.SQLSERV() != null) {
      return NamedPort.SQLSERV;
    } else if (ctx.SQLSRV() != null) {
      return NamedPort.SQLSRV;
    } else if (ctx.SSH() != null) {
      return NamedPort.SSH;
    } else if (ctx.SUBMISSION() != null) {
      return NamedPort.SUBMISSION;
    } else if (ctx.SUNRPC() != null) {
      return NamedPort.SUNRPC;
    } else if (ctx.SVRLOC() != null) {
      return NamedPort.SVRLOC;
    } else if (ctx.SYSLOG() != null) {
      return NamedPort.CMDtcp_OR_SYSLOGudp;
    } else if (ctx.SYSTAT() != null) {
      return NamedPort.SYSTAT;
    } else if (ctx.TACACS() != null) {
      return NamedPort.TACACS;
    } else if (ctx.TACACS_DS() != null) {
      return NamedPort.TACACS_DS;
    } else if (ctx.TALK() != null) {
      return NamedPort.TALK;
    } else if (ctx.TBRPF() != null) {
      return NamedPort.TBRPF;
    } else if (ctx.TCPMUX() != null) {
      return NamedPort.TCPMUX;
    } else if (ctx.TCPNETHASPSRV() != null) {
      return NamedPort.TCPNETHASPSRV;
    } else if (ctx.TELNET() != null) {
      return NamedPort.TELNET;
    } else if (ctx.TFTP() != null) {
      return NamedPort.TFTP;
    } else if (ctx.TIME() != null) {
      return NamedPort.TIME;
    } else if (ctx.TIMED() != null) {
      return NamedPort.TIMED;
    } else if (ctx.TUNNEL() != null) {
      return NamedPort.TUNNEL;
    } else if (ctx.UPS() != null) {
      return NamedPort.UPS;
    } else if (ctx.UUCP() != null) {
      return NamedPort.UUCP;
    } else if (ctx.UUCP_PATH() != null) {
      return NamedPort.UUCP_PATH;
    } else if (ctx.VMNET() != null) {
      return NamedPort.VMNET;
    } else if (ctx.VXLAN() != null) {
      return NamedPort.VXLAN;
    } else if (ctx.WHO() != null) {
      return NamedPort.LOGINtcp_OR_WHOudp;
    } else if (ctx.WHOIS() != null) {
      return NamedPort.WHOIS;
    } else if (ctx.WWW() != null) {
      return NamedPort.HTTP;
    } else if (ctx.XDMCP() != null) {
      return NamedPort.XDMCP;
    } else if (ctx.XNS_CH() != null) {
      return NamedPort.XNS_CH;
    } else if (ctx.XNS_MAIL() != null) {
      return NamedPort.XNS_MAIL;
    } else if (ctx.XNS_TIME() != null) {
      return NamedPort.XNS_TIME;
    } else if (ctx.Z39_50() != null) {
      return NamedPort.Z39_50;
    } else {
      throw convError(NamedPort.class, ctx);
    }
  }

  private OriginExpr toOriginExpr(Origin_expr_literalContext ctx) {
    OriginType originType;
    Long asNum = null;
    LiteralOrigin originExpr;
    if (ctx.IGP() != null) {
      originType = OriginType.IGP;
    } else if (ctx.INCOMPLETE() != null) {
      originType = OriginType.INCOMPLETE;
    } else if (ctx.bgp_asn() != null) {
      asNum = toAsNum(ctx.bgp_asn());
      originType = OriginType.IGP;
    } else {
      throw convError(OriginExpr.class, ctx);
    }
    originExpr = new LiteralOrigin(originType, asNum);
    return originExpr;
  }

  private List<SubRange> toPortRanges(Port_specifierContext ps) {
    Builder builder = IntegerSpace.builder();
    if (ps.EQ() != null) {
      ps.args.forEach(p -> builder.including(getPortNumber(p)));
    } else if (ps.GT() != null) {
      int port = getPortNumber(ps.arg);
      builder.including(new SubRange(port + 1, 65535));
    } else if (ps.NEQ() != null) {
      builder.including(IntegerSpace.PORTS);
      ps.args.forEach(p -> builder.excluding(getPortNumber(p)));
    } else if (ps.LT() != null) {
      int port = getPortNumber(ps.arg);
      builder.including(new SubRange(0, port - 1));
    } else if (ps.RANGE() != null) {
      int lowPort = getPortNumber(ps.arg1);
      int highPort = getPortNumber(ps.arg2);
      builder.including(new SubRange(lowPort, highPort));
    } else {
      throw convError(List.class, ps);
    }
    return builder.build().getSubRanges().stream()
        .sorted() // for output/ref determinism
        .collect(ImmutableList.toImmutableList());
  }

  private <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, U defaultReturnValue) {
    warn(ctx, convErrorMessage(returnType, ctx));
    return defaultReturnValue;
  }

  private static @Nonnull ConcreteInterfaceAddress toAddress(Interface_addressContext ctx) {
    if (ctx.prefix != null) {
      return ConcreteInterfaceAddress.parse(ctx.prefix.getText());
    } else {
      Ip ip = toIp(ctx.ip);
      Ip mask = toIp(ctx.subnet);
      return ConcreteInterfaceAddress.create(ip, mask);
    }
  }

  private static long toLong(Bgp_local_prefContext ctx) {
    return toLong(ctx.p);
  }

  private static long toLong(Ospf_areaContext ctx) {
    if (ctx.id_ip != null) {
      return toIp(ctx.id_ip).asLong();
    } else {
      return toLong(ctx.id);
    }
  }

  private static final IntegerSpace PORT_NUMBER_RANGE = IntegerSpace.of(Range.closed(1, 65535));

  private Optional<Integer> toPort(ParserRuleContext messageCtx, Port_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx, PORT_NUMBER_RANGE, "port number");
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

  private @Nonnull RouteDistinguisher toRouteDistinguisher(Route_distinguisherContext ctx) {
    long dec = toInteger(ctx.uint16());
    if (ctx.IP_ADDRESS() != null) {
      checkArgument(dec <= 0xFFFFL, "Invalid route distinguisher %s", ctx.getText());
      return RouteDistinguisher.from(toIp(ctx.IP_ADDRESS()), (int) dec);
    }
    return RouteDistinguisher.from(toAsNum(ctx.bgp_asn()), dec);
  }

  private @Nonnull ExtendedCommunity toRouteTarget(Route_targetContext ctx) {
    long la = toInteger(ctx.uint16());
    if (ctx.IP_ADDRESS() != null) {
      return ExtendedCommunity.target(toIp(ctx.IP_ADDRESS()).asLong(), la);
    }
    return ExtendedCommunity.target(toAsNum(ctx.bgp_asn()), la);
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
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
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }
}
