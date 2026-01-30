package org.batfish.grammar.palo_alto;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.CATCHALL_APPLICATION_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.CATCHALL_CATEGORY_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.CATCHALL_ZONE_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.DEFAULT_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.PANORAMA_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.SHARED_VSYS_NAME;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeObjectName;
import static org.batfish.representation.palo_alto.PaloAltoConfiguration.isBuiltInApp;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.ADDRESS_GROUP;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.ADDRESS_LIKE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.ADDRESS_LIKE_OR_NONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.ADDRESS_OBJECT;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.ADDRESS_OBJECT_OR_NONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.APPLICATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.APPLICATION_GROUP;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.APPLICATION_GROUP_OR_APPLICATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.APPLICATION_GROUP_OR_APPLICATION_OR_NONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.APPLICATION_OR_NONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.APPLICATION_OVERRIDE_RULE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.CUSTOM_URL_CATEGORY;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.EXTERNAL_LIST;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.NAT_RULE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.REDIST_PROFILE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SECURITY_RULE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SERVICE_GROUP;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SERVICE_OR_SERVICE_GROUP;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SERVICE_OR_SERVICE_GROUP_OR_NONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.SHARED_GATEWAY;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.TEMPLATE;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.VIRTUAL_ROUTER;
import static org.batfish.representation.palo_alto.PaloAltoStructureType.ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.ADDRESS_GROUP_STATIC;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.APPLICATION_GROUP_MEMBERS;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.APPLICATION_OVERRIDE_RULE_APPLICATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.APPLICATION_OVERRIDE_RULE_DESTINATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.APPLICATION_OVERRIDE_RULE_FROM_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.APPLICATION_OVERRIDE_RULE_SELF_REF;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.APPLICATION_OVERRIDE_RULE_SOURCE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.APPLICATION_OVERRIDE_RULE_TO_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.BGP_PEER_LOCAL_ADDRESS_INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.ETHERNET_AGGREGATE_GROUP;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.IMPORT_INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.LAYER2_INTERFACE_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.LAYER3_INTERFACE_ADDRESS;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.LAYER3_INTERFACE_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.LOOPBACK_INTERFACE_ADDRESS;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.NAT_RULE_DESTINATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.NAT_RULE_DESTINATION_TRANSLATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.NAT_RULE_FROM_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.NAT_RULE_SELF_REF;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.NAT_RULE_SERVICE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.NAT_RULE_SOURCE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.NAT_RULE_SOURCE_TRANSLATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.NAT_RULE_TO_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.REDIST_RULE_REDIST_PROFILE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SECURITY_RULE_APPLICATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SECURITY_RULE_CATEGORY;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SECURITY_RULE_DESTINATION;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SECURITY_RULE_FROM_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SECURITY_RULE_SELF_REF;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SECURITY_RULE_SERVICE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SECURITY_RULE_SOURCE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SECURITY_RULE_TO_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.SERVICE_GROUP_MEMBER;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.STATIC_ROUTE_INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.STATIC_ROUTE_NEXT_VR;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.TAP_INTERFACE_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.TEMPLATE_STACK_TEMPLATES;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.VIRTUAL_ROUTER_INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.VIRTUAL_ROUTER_SELF_REFERENCE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.VIRTUAL_WIRE_INTERFACE_ZONE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.VSYS_IMPORT_INTERFACE;
import static org.batfish.representation.palo_alto.PaloAltoStructureUsage.ZONE_INTERFACE;
import static org.batfish.representation.palo_alto.Zone.Type.EXTERNAL;
import static org.batfish.representation.palo_alto.Zone.Type.LAYER2;
import static org.batfish.representation.palo_alto.Zone.Type.LAYER3;
import static org.batfish.representation.palo_alto.Zone.Type.TAP;
import static org.batfish.representation.palo_alto.Zone.Type.VIRTUAL_WIRE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.DiffieHellmanGroup;
import org.batfish.datamodel.EncryptionAlgorithm;
import org.batfish.datamodel.IkeHashingAlgorithm;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpsecAuthenticationAlgorithm;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_asnContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_install_routeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_local_asContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_local_prefContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_peer_group_nameContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_peer_nameContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_policy_ruleContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_reject_default_routeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgp_router_idContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgpp_exportContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgpp_importContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppg_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppg_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppg_peerContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_co_multihopContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_coi_allowContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_coi_remote_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_coo_allowContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_coo_local_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_enable_sender_side_loop_detectionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_la_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_la_ipContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_max_prefixesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_peer_addressContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_peer_asContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgp_reflector_clientContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgt_ebgpContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgt_ibgpContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgte_export_nexthopContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgte_import_nexthopContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgppgte_remove_private_asContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgpro_as_formatContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgpro_default_local_preferenceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgpro_reflector_cluster_idContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgproa_aggregate_medContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprog_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprom_always_compare_medContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprom_deterministic_med_comparisonContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprr_ip_addressContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprr_prefixContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprr_profile_nameContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprrg_address_family_identifierContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprrg_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprrg_route_tableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Bgprrg_set_originContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Cp_authenticationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Cp_dh_groupContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Cp_encryptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Cp_encryption_algoContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Cp_hashContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Cp_lifetimeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.If_commentContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.If_tagContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Interface_addressContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Interface_address_or_referenceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ip_addressContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ip_address_or_slash32Context;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ip_prefixContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ip_prefix_listContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ip_rangeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospf_areaContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospf_area_metricContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospf_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospf_graceful_restartContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospf_interface_metricContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospf_reject_default_routeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospf_router_idContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfa_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfai_dead_countsContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfai_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfai_hello_intervalContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfai_link_typeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfai_metricContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfai_passiveContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfai_priorityContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfai_retransmit_intervalContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfai_transit_delayContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfat_normalContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfat_nssaContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfat_stubContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfatn_accept_summaryContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfatndr_disableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfatndra_metricContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfatndra_typeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfats_accept_summaryContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfatsdr_advertise_metricContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Ospfatsdr_disableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Palo_alto_configurationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Port_numberContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Port_or_rangeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Pr_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Pr_used_byContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Pra_allowContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Pra_denyContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Praau_medContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Praau_originContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Praau_weightContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Prm_address_prefixContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Prm_from_peerContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Protocol_adContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_address_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_address_group_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_application_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_external_listContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_policy_panoramaContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_post_rulebaseContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_pre_rulebaseContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_profilesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_rulebaseContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_service_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_service_group_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_sharedContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_tagContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_vsys_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.S_zone_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sa_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sa_fqdnContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sa_ip_netmaskContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sa_ip_rangeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sa_tagContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sag_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sag_staticContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sag_tagContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sagd_filterContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sapp_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sapp_ignoredContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sappg_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sappg_membersContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdg_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdg_devicesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdg_parent_dgContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdgd_vsysContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_default_gatewayContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_domainContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_hostnameContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_ip_addressContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_netmaskContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_ntp_serversContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sds_snmp_settingContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdsd_serversContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdsn_ntp_server_addressContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdss_access_settingContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdss_snmp_systemContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sdssa_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Selt_ipContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_line_config_devicesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_line_device_groupContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_line_templateContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Set_line_template_stackContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sl_syslogContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sls_serverContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_and_also_toContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_certificate_profileContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_communityContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_facilityContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_formatContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_fromContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_gatewayContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_managerContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_serverContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_toContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_transportContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Slss_versionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sn_shared_gateway_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sni_aggregate_ethernet_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sni_ethernet_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sni_loopbackContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sni_tunnelContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sni_vlanContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snicp_global_protectContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snicp_ike_crypto_profilesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snicp_ipsec_crypto_profilesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snie_aggregate_groupContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snie_haContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snie_link_stateContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel2_unitContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel3_ipContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel3_mtuContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniel3_unitContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snil_ipContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snil_unitContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snit_unitContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sniv_unitContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snsg_display_nameContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snsg_zone_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snsgi_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Snsgzn_layer3Context;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_applicationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_disabledContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_fromContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_negate_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_negate_sourceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_protocolContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_sourceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_tagContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srao_toContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Src_or_dst_list_itemContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srespr_devicesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sresprd_hostnameContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_active_active_device_bindingContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_destination_translationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_disabledContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_fromContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_serviceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_sourceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srn_toContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srndt_translated_addressContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srndt_translated_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srnst_dynamic_ip_and_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sro_device_groupContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_actionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_applicationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_destination_hipContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_disabledContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_fromContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_hip_profilesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_negate_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_negate_sourceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_rule_typeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_serviceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_sourceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_source_hipContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_source_userContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_tagContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Srs_toContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sserv_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sserv_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sserv_protocolContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sserv_source_portContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sserv_tagContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sservgrp_membersContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.St_commentsContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.St_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sts_descriptionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sts_devicesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Sts_templatesContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Svi_visible_vsysContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Svin_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Szn_externalContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Szn_layer2Context;
import org.batfish.grammar.palo_alto.PaloAltoParser.Szn_layer3Context;
import org.batfish.grammar.palo_alto.PaloAltoParser.Szn_tapContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Szn_virtual_wireContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Tcp_or_udpContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Translated_address_list_itemContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Uint16Context;
import org.batfish.grammar.palo_alto.PaloAltoParser.Uint32Context;
import org.batfish.grammar.palo_alto.PaloAltoParser.Uint8Context;
import org.batfish.grammar.palo_alto.PaloAltoParser.ValueContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.VariableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Variable_listContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Variable_list_itemContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vlan_tagContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vr_definitionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vr_ecmp_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vr_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vr_routing_tableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrad_ebgpContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrad_ibgpContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrad_ospf_extContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrad_ospf_intContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrad_ospfv3_extContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrad_ospfv3_intContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrad_ripContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrad_staticContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrad_static_ipv6Context;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrp_bgpContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrp_ospfContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrp_redist_profileContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrprp_actionContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrprp_priorityContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrprpf_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrprpf_typeContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrrt_admin_distContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrrt_destinationContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrrt_interfaceContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrrt_metricContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrrtn_discardContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrrtn_ipContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrrtn_next_vrContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Vrrtpm_enableContext;
import org.batfish.grammar.palo_alto.PaloAltoParser.Yes_or_noContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.palo_alto.AddressGroup;
import org.batfish.representation.palo_alto.AddressObject;
import org.batfish.representation.palo_alto.AddressPrefix;
import org.batfish.representation.palo_alto.Application;
import org.batfish.representation.palo_alto.ApplicationGroup;
import org.batfish.representation.palo_alto.ApplicationOverrideRule;
import org.batfish.representation.palo_alto.ApplicationOverrideRule.Protocol;
import org.batfish.representation.palo_alto.BgpPeer;
import org.batfish.representation.palo_alto.BgpPeer.ReflectorClient;
import org.batfish.representation.palo_alto.BgpPeerGroup;
import org.batfish.representation.palo_alto.BgpPeerGroupTypeAndOptions;
import org.batfish.representation.palo_alto.BgpVr;
import org.batfish.representation.palo_alto.BgpVrRoutingOptions.AsFormat;
import org.batfish.representation.palo_alto.CryptoProfile;
import org.batfish.representation.palo_alto.CryptoProfile.Type;
import org.batfish.representation.palo_alto.CustomUrlCategory;
import org.batfish.representation.palo_alto.DestinationTranslation;
import org.batfish.representation.palo_alto.DeviceGroup;
import org.batfish.representation.palo_alto.DynamicIpAndPort;
import org.batfish.representation.palo_alto.EbgpPeerGroupType;
import org.batfish.representation.palo_alto.EbgpPeerGroupType.ExportNexthopMode;
import org.batfish.representation.palo_alto.EbgpPeerGroupType.ImportNexthopMode;
import org.batfish.representation.palo_alto.HighAvailability;
import org.batfish.representation.palo_alto.Interface;
import org.batfish.representation.palo_alto.InterfaceAddress;
import org.batfish.representation.palo_alto.Ip6Prefix;
import org.batfish.representation.palo_alto.IpPrefix;
import org.batfish.representation.palo_alto.NatRule;
import org.batfish.representation.palo_alto.OspfArea;
import org.batfish.representation.palo_alto.OspfAreaNormal;
import org.batfish.representation.palo_alto.OspfAreaNssa;
import org.batfish.representation.palo_alto.OspfAreaNssa.DefaultRouteType;
import org.batfish.representation.palo_alto.OspfAreaStub;
import org.batfish.representation.palo_alto.OspfInterface;
import org.batfish.representation.palo_alto.OspfInterface.LinkType;
import org.batfish.representation.palo_alto.OspfVr;
import org.batfish.representation.palo_alto.PaloAltoConfiguration;
import org.batfish.representation.palo_alto.PaloAltoStructureType;
import org.batfish.representation.palo_alto.PaloAltoStructureUsage;
import org.batfish.representation.palo_alto.PolicyRule;
import org.batfish.representation.palo_alto.PolicyRule.Action;
import org.batfish.representation.palo_alto.PolicyRuleMatchFromPeerSet;
import org.batfish.representation.palo_alto.PolicyRuleUpdateMetric;
import org.batfish.representation.palo_alto.PolicyRuleUpdateOrigin;
import org.batfish.representation.palo_alto.PolicyRuleUpdateWeight;
import org.batfish.representation.palo_alto.RedistProfile;
import org.batfish.representation.palo_alto.RedistRule;
import org.batfish.representation.palo_alto.RedistRule.AddressFamilyIdentifier;
import org.batfish.representation.palo_alto.RedistRule.RouteTableType;
import org.batfish.representation.palo_alto.RedistRuleRefNameOrPrefix;
import org.batfish.representation.palo_alto.RuleEndpoint;
import org.batfish.representation.palo_alto.Rulebase;
import org.batfish.representation.palo_alto.SecurityRule;
import org.batfish.representation.palo_alto.SecurityRule.RuleType;
import org.batfish.representation.palo_alto.Service;
import org.batfish.representation.palo_alto.ServiceBuiltIn;
import org.batfish.representation.palo_alto.ServiceGroup;
import org.batfish.representation.palo_alto.ServiceOrServiceGroupReference;
import org.batfish.representation.palo_alto.SnmpAccessSetting;
import org.batfish.representation.palo_alto.SnmpSetting;
import org.batfish.representation.palo_alto.SnmpSystem;
import org.batfish.representation.palo_alto.SourceTranslation;
import org.batfish.representation.palo_alto.StaticRoute;
import org.batfish.representation.palo_alto.SyslogServer;
import org.batfish.representation.palo_alto.Tag;
import org.batfish.representation.palo_alto.Template;
import org.batfish.representation.palo_alto.TemplateStack;
import org.batfish.representation.palo_alto.VirtualRouter;
import org.batfish.representation.palo_alto.Vsys;
import org.batfish.representation.palo_alto.Vsys.NamespaceType;
import org.batfish.representation.palo_alto.Zone;
import org.batfish.vendor.StructureType;
import org.batfish.vendor.StructureUsage;

public class PaloAltoConfigurationBuilder extends PaloAltoParserBaseListener
    implements SilentSyntaxListener {

  @Override
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  /** Indicates which rulebase that new rules go into. */
  private enum RulebaseId {
    /** Indicates default rulebase ({@link Vsys#getRulebase}) */
    DEFAULT,
    /** Indicates pre-rulebase ({@link Vsys#getPreRulebase}) */
    PRE,
    /** Indicates post-rulebase ({@link Vsys#getPostRulebase}) */
    POST
  }

  private PaloAltoConfiguration _mainConfiguration;
  private Vsys _defaultVsys;
  private PaloAltoCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  /** Should file at most one warning about ignored application statements */
  private boolean _filedWarningApplicationStatementIgnored = false;

  private AddressGroup _currentAddressGroup;
  private AddressObject _currentAddressObject;
  private Application _currentApplication;
  private ApplicationGroup _currentApplicationGroup;
  private BgpPeer _currentBgpPeer;
  private BgpPeerGroup _currentBgpPeerGroup;
  private BgpVr _currentBgpVr;
  private PaloAltoConfiguration _currentConfiguration;
  private CryptoProfile _currentCrytoProfile;
  private CustomUrlCategory _currentCustomUrlCategory;
  private DeviceGroup _currentDeviceGroup;
  private String _currentDeviceGroupVsys;
  private String _currentDeviceName;
  private String _currentExternalListName;
  private HighAvailability _currentHighAvailability;
  private Interface _currentInterface;
  private ApplicationOverrideRule _currentApplicationOverrideRule;
  private NatRule _currentNatRule;
  private DestinationTranslation _currentNatRuleDestinationTranslation;
  private boolean _currentNtpServerPrimary;
  private SnmpAccessSetting _currentSnmpAccessSetting;
  private SnmpSystem _currentSnmpSystem;
  private Interface _currentParentInterface;
  private PolicyRule _currentPolicyRule;
  private boolean _currentPolicyRuleImport;
  private OspfArea _currentOspfArea;
  private OspfInterface _currentOspfInterface;
  private OspfAreaStub _currentOspfStubAreaType;
  private OspfAreaNssa _currentOspfNssaAreaType;
  private OspfVr _currentOspfVr;
  private RedistProfile _currentRedistProfile;
  private RedistRule _currentRedistRule;
  private String _currentResultDeviceName;
  private RulebaseId _currentRuleScope;
  private SecurityRule _currentSecurityRule;
  private Service _currentService;
  private ServiceGroup _currentServiceGroup;
  private StaticRoute _currentStaticRoute;
  private SyslogServer _currentSyslogServer;
  private String _currentSyslogServerGroupName;
  private Tag _currentTag;
  private Template _currentTemplate;
  private TemplateStack _currentTemplateStack;
  private Optional<String> _currentTemplateVariableName;
  private VirtualRouter _currentVirtualRouter;
  private Vsys _currentVsys;
  private Zone _currentZone;

  public PaloAltoConfigurationBuilder(
      PaloAltoCombinedParser parser,
      String text,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  @SuppressWarnings("unused")
  private BatfishException convError(Class<?> type, ParserRuleContext ctx) {
    String typeName = type.getSimpleName();
    String txt = getFullText(ctx);
    return new BatfishException("Could not convert to " + typeName + ": " + txt);
  }

  private static String convErrorMessage(Class<?> type) {
    return String.format("Could not convert to %s", type.getSimpleName());
  }

  private <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, U defaultReturnValue) {
    warn(ctx, convErrorMessage(returnType));
    return defaultReturnValue;
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

  /** Return original line number for specified token */
  private int getLine(Token t) {
    return _parser.getLine(t);
  }

  /** Return token text with enclosing quotes removed, if applicable */
  private String getText(ParserRuleContext ctx) {
    return unquote(ctx.getText());
  }

  /** Return token text with enclosing quotes removed, if applicable */
  private String getText(TerminalNode t) {
    return unquote(t.getText());
  }

  /** Return token text with enclosing quotes removed, if applicable */
  private String getText(Token t) {
    return unquote(t.getText());
  }

  /** Return text corresponding to value context with enclosing quotes removed, if applicable */
  private String getText(ValueContext ctx) {
    return unquote(ctx.getText());
  }

  /**
   * Shallow wrapper to define structures in the correct configuration.
   *
   * @deprecated use {@link #defineFlattenedStructure}.
   */
  @Deprecated
  private void defineStructure(StructureType type, String name, ParserRuleContext ctx) {
    _mainConfiguration.defineStructure(type, name, ctx);
  }

  /** Shallow wrapper to define flattened structures in the correct configuration. */
  private void defineFlattenedStructure(StructureType type, String name, ParserRuleContext ctx) {
    _mainConfiguration.defineFlattenedStructure(type, name, ctx, _parser);
  }

  /** Shallow wrapper to add structure references to the correct configuration. */
  private void referenceStructure(StructureType type, String name, StructureUsage usage, int line) {
    _mainConfiguration.referenceStructure(type, name, usage, line);
  }

  /**
   * Add interface address reference. Adds a different type of reference based on the format of the
   * address (e.g. something that looks like an IP address might but does not have to refer to an
   * object, however something that looks like an object name must refer to an object).
   */
  private void referenceInterfaceAddress(
      Interface_address_or_referenceContext ctx, PaloAltoStructureUsage usage) {
    String uniqueName = computeObjectName(_currentVsys, getText(ctx));
    if (ctx.reference != null) {
      referenceStructure(ADDRESS_OBJECT, uniqueName, usage, getLine(ctx.start));
    } else {
      /* Interface addresses that look like concrete addresses might still be referring to an object
       * with an ambiguous name. */
      referenceStructure(ADDRESS_OBJECT_OR_NONE, uniqueName, usage, getLine(ctx.start));
    }
  }

  /**
   * Helper function to add the correct service reference type for a given reference. For references
   * that may be pointing to built-in services, this is needed to make sure we don't create false
   * positive undefined references.
   */
  private void referenceService(Variable_list_itemContext var, PaloAltoStructureUsage usage) {
    referenceService(getText(var), getLine(var.start), usage);
  }

  private void referenceService(VariableContext var, PaloAltoStructureUsage usage) {
    referenceService(getText(var), getLine(var.start), usage);
  }

  private void referenceService(String serviceName, int start, PaloAltoStructureUsage usage) {
    // Use constructed object name so same-named refs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, serviceName);

    if (Arrays.stream(ServiceBuiltIn.values()).anyMatch(n -> serviceName.equals(n.getName()))) {
      // Built-in services can be overridden, so add optional object reference
      referenceStructure(SERVICE_OR_SERVICE_GROUP_OR_NONE, uniqueName, usage, start);
    } else {
      referenceStructure(SERVICE_OR_SERVICE_GROUP, uniqueName, usage, start);
    }
  }

  private DiffieHellmanGroup toDiffieHellmanGroup(Cp_dh_groupContext ctx) {
    if (ctx.GROUP1() != null) {
      return DiffieHellmanGroup.GROUP1;
    } else if (ctx.GROUP2() != null) {
      return DiffieHellmanGroup.GROUP2;
    } else if (ctx.GROUP5() != null) {
      return DiffieHellmanGroup.GROUP5;
    } else if (ctx.GROUP14() != null) {
      return DiffieHellmanGroup.GROUP14;
    } else if (ctx.GROUP19() != null) {
      return DiffieHellmanGroup.GROUP19;
    } else if (ctx.GROUP20() != null) {
      return DiffieHellmanGroup.GROUP20;
    } else {
      return convProblem(DiffieHellmanGroup.class, ctx, null);
    }
  }

  private EncryptionAlgorithm toEncryptionAlgo(Cp_encryption_algoContext ctx) {
    if (ctx.AES_128_CBC() != null) {
      return EncryptionAlgorithm.AES_128_CBC;
    } else if (ctx.AES_128_GCM() != null) {
      return EncryptionAlgorithm.AES_128_GCM;
    } else if (ctx.AES_192_CBC() != null) {
      return EncryptionAlgorithm.AES_192_CBC;
    } else if (ctx.AES_256_CBC() != null) {
      return EncryptionAlgorithm.AES_256_CBC;
    } else if (ctx.AES_256_GCM() != null) {
      return EncryptionAlgorithm.AES_256_GCM;
    } else if (ctx.DES() != null) {
      return EncryptionAlgorithm.DES_CBC;
    } else if (ctx.THREE_DES() != null) {
      return EncryptionAlgorithm.THREEDES_CBC;
    } else if (ctx.NULL() != null) {
      return EncryptionAlgorithm.NULL;
    }
    return convProblem(EncryptionAlgorithm.class, ctx, null);
  }

  private IkeHashingAlgorithm toIkeHashingAlgorithm(Cp_hashContext ctx) {
    if (ctx.MD5() != null) {
      return IkeHashingAlgorithm.MD5;
    } else if (ctx.SHA1() != null) {
      return IkeHashingAlgorithm.SHA1;
    } else if (ctx.SHA256() != null) {
      return IkeHashingAlgorithm.SHA_256;
    } else if (ctx.SHA384() != null) {
      return IkeHashingAlgorithm.SHA_384;
    } else if (ctx.SHA512() != null) {
      return IkeHashingAlgorithm.SHA_512;
    }
    return convProblem(IkeHashingAlgorithm.class, ctx, null);
  }

  private IpsecAuthenticationAlgorithm toIpsecAuthenticationAlgorithm(
      Cp_authenticationContext ctx) {
    if (ctx.MD5() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_MD5_96;
    } else if (ctx.SHA1() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_SHA1_96;
    } else if (ctx.SHA256() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_SHA_256_128;
    } else if (ctx.SHA384() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_SHA_384;
    } else if (ctx.SHA512() != null) {
      return IpsecAuthenticationAlgorithm.HMAC_SHA_512;
    } else if (ctx.NONE() != null) {
      return null;
    }
    return convProblem(IpsecAuthenticationAlgorithm.class, ctx, null);
  }

  /** Convert address or reference into an InterfaceAddress */
  private InterfaceAddress toInterfaceAddress(Interface_address_or_referenceContext ctx) {
    String text = getText(ctx);
    if (ctx.addr != null) {
      return new InterfaceAddress(InterfaceAddress.Type.IP_ADDRESS, text);
    } else if (ctx.addr_with_mask != null) {
      return new InterfaceAddress(InterfaceAddress.Type.IP_PREFIX, text);
    }
    return new InterfaceAddress(InterfaceAddress.Type.REFERENCE, text);
  }

  /** Convert source or destination list item into an appropriate IpSpace */
  private RuleEndpoint toRuleEndpoint(Src_or_dst_list_itemContext ctx) {
    String text = getText(ctx);
    if (ctx.any != null) {
      return new RuleEndpoint(RuleEndpoint.Type.Any, text);
    } else if (ctx.address != null) {
      return new RuleEndpoint(RuleEndpoint.Type.IP_ADDRESS, text);
    } else if (ctx.prefix != null) {
      return new RuleEndpoint(RuleEndpoint.Type.IP_PREFIX, text);
    } else if (ctx.range != null) {
      return new RuleEndpoint(RuleEndpoint.Type.IP_RANGE, text);
    }
    // Check if this is a geolocation code (2-letter ISO country code or special codes)
    if (isGeolocationCode(text)) {
      return new RuleEndpoint(RuleEndpoint.Type.IP_LOCATION, text);
    }
    // Check if this looks like an FQDN (contains dots and domain-like characters)
    if (isFqdn(text)) {
      return new RuleEndpoint(RuleEndpoint.Type.FQDN, text);
    }
    return new RuleEndpoint(RuleEndpoint.Type.REFERENCE, text);
  }

  /** Convert translated-address list item into an appropriate IpSpace */
  private RuleEndpoint toRuleEndpoint(Translated_address_list_itemContext ctx) {
    String text = getText(ctx);
    if (ctx.address != null) {
      return new RuleEndpoint(RuleEndpoint.Type.IP_ADDRESS, text);
    } else if (ctx.prefix != null) {
      return new RuleEndpoint(RuleEndpoint.Type.IP_PREFIX, text);
    } else if (ctx.range != null) {
      return new RuleEndpoint(RuleEndpoint.Type.IP_RANGE, text);
    }
    // Check if this is a geolocation code (2-letter ISO country code or special codes)
    if (isGeolocationCode(text)) {
      return new RuleEndpoint(RuleEndpoint.Type.IP_LOCATION, text);
    }
    // Check if this looks like an FQDN (contains dots and domain-like characters)
    if (isFqdn(text)) {
      return new RuleEndpoint(RuleEndpoint.Type.FQDN, text);
    }
    return new RuleEndpoint(RuleEndpoint.Type.REFERENCE, text);
  }

  private String unquote(String text) {
    if (text.length() < 2) {
      return text;
    }
    char leading = text.charAt(0);
    char trailing = text.charAt(text.length() - 1);
    if (leading == '\'' || leading == '"') {
      if (leading == trailing) {
        return text.substring(1, text.length() - 1);
      } else {
        _w.redFlag("Improperly-quoted string: " + text);
      }
    }
    return text;
  }

  /**
   * Check if a string is a valid geolocation code (ISO 3166-1 alpha-2 country code or special codes
   * like A1, A2, EU, AP, etc.)
   */
  private boolean isGeolocationCode(String text) {
    if (text == null || text.length() != 2) {
      return false;
    }
    // Check for special geolocation codes
    if ("A1".equals(text)
        || "A2".equals(text)
        || "EU".equals(text)
        || "AP".equals(text)
        || "CE".equals(text)
        || "DN".equals(text)
        || "LN".equals(text)) {
      return true;
    }
    // Check for ISO 3166-1 alpha-2 country code pattern (two uppercase letters)
    return text.length() == 2
        && Character.isUpperCase(text.charAt(0))
        && Character.isUpperCase(text.charAt(1));
  }

  /**
   * Check if a string looks like an FQDN (contains dots and domain-like characters). This is a
   * heuristic check - real FQDNs would be validated by the firewall.
   */
  private boolean isFqdn(String text) {
    if (text == null || text.isEmpty()) {
      return false;
    }
    // FQDNs typically contain dots and alphanumeric characters with hyphens
    // This is a simple heuristic - valid FQDNs can be more complex
    return text.contains(".")
        && text.matches("^[a-zA-Z0-9.-]+$")
        && !text.startsWith(".")
        && !text.endsWith(".");
  }

  /** Return the rulebase based on the {@link #_currentRuleScope current rulebase scope} */
  private @Nonnull Rulebase getRulebase() {
    Rulebase rulebase;
    if (_currentRuleScope == RulebaseId.DEFAULT) {
      rulebase = _currentVsys.getRulebase();
    } else if (_currentRuleScope == RulebaseId.PRE) {
      rulebase = _currentVsys.getPreRulebase();
    } else {
      assert _currentRuleScope == RulebaseId.POST;
      rulebase = _currentVsys.getPostRulebase();
    }
    return rulebase;
  }

  /** A helper function to extract all variables from an optional list. */
  private static List<Variable_list_itemContext> variables(@Nullable Variable_listContext ctx) {
    if (ctx == null || ctx.variable_list_item() == null) {
      return ImmutableList.of();
    }
    return ctx.variable_list_item();
  }

  /** A helper function to extract all variables from an optional list of prefixes. */
  private static List<Ip_prefixContext> prefixes(@Nullable Ip_prefix_listContext ctx) {
    if (ctx == null || ctx.ip_prefix() == null) {
      return ImmutableList.of();
    }
    return ctx.ip_prefix();
  }

  @Override
  public void exitBgp_enable(Bgp_enableContext ctx) {
    _currentBgpVr.setEnable(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgp_install_route(Bgp_install_routeContext ctx) {
    _currentBgpVr.setInstallRoute(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgp_local_as(Bgp_local_asContext ctx) {
    toAsn(ctx, ctx.asn, _currentBgpVr.getRoutingOptions().getAsFormat())
        .ifPresent(_currentBgpVr::setLocalAs);
  }

  @Override
  public void enterBgpp_export(Bgpp_exportContext ctx) {
    _currentPolicyRuleImport = false;
  }

  @Override
  public void enterBgpp_import(Bgpp_importContext ctx) {
    _currentPolicyRuleImport = true;
  }

  @Override
  public void enterBgp_policy_rule(Bgp_policy_ruleContext ctx) {
    _currentPolicyRule =
        _currentPolicyRuleImport
            ? _currentBgpVr.getOrCreateImportPolicyRule(getText(ctx.name))
            : _currentBgpVr.getOrCreateExportPolicyRule(getText(ctx.name));
  }

  @Override
  public void exitBgpp_export(Bgpp_exportContext ctx) {
    _currentPolicyRule = null;
  }

  @Override
  public void exitBgpp_import(Bgpp_importContext ctx) {
    _currentPolicyRule = null;
  }

  @Override
  public void exitPr_enable(Pr_enableContext ctx) {
    _currentPolicyRule.setEnable(true);
  }

  @Override
  public void exitPr_used_by(Pr_used_byContext ctx) {
    _currentPolicyRule.addUsedBy(getText(ctx.name));
  }

  @Override
  public void exitPra_allow(Pra_allowContext ctx) {
    _currentPolicyRule.setAction(Action.ALLOW);
  }

  @Override
  public void exitPra_deny(Pra_denyContext ctx) {
    _currentPolicyRule.setAction(Action.DENY);
  }

  @Override
  public void exitPraau_origin(Praau_originContext ctx) {
    OriginType originType;
    if (ctx.EGP() != null) {
      originType = OriginType.EGP;
    } else if (ctx.IGP() != null) {
      originType = OriginType.IGP;
    } else {
      assert ctx.INCOMPLETE() != null;
      originType = OriginType.INCOMPLETE;
    }
    _currentPolicyRule.setUpdateOrigin(new PolicyRuleUpdateOrigin(originType));
  }

  @Override
  public void exitPraau_med(Praau_medContext ctx) {
    _currentPolicyRule.setUpdateMetric(new PolicyRuleUpdateMetric(toLong(ctx.val.uint32())));
  }

  @Override
  public void exitPraau_weight(Praau_weightContext ctx) {
    _currentPolicyRule.setUpdateWeight(new PolicyRuleUpdateWeight(toInteger(ctx.val.uint16())));
  }

  @Override
  public void exitPrm_from_peer(Prm_from_peerContext ctx) {
    ImmutableSet.Builder<String> peers = ImmutableSet.builder();
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String peerName = getText(var);
      peers.add(peerName);
    }
    _currentPolicyRule.setMatchFromPeerSet(new PolicyRuleMatchFromPeerSet(peers.build()));
  }

  @Override
  public void exitPrm_address_prefix(Prm_address_prefixContext ctx) {
    _currentPolicyRule
        .getOrCreateMatchAddressPrefixSet()
        .getAddressPrefixes()
        .add(new AddressPrefix(toPrefix(ctx.ip_prefix()), toBoolean(ctx.yn)));
  }

  @Override
  public void enterBgppg_definition(Bgppg_definitionContext ctx) {
    _currentBgpPeerGroup =
        toString(ctx, ctx.name)
            .map(_currentBgpVr::getOrCreatePeerGroup)
            .orElseGet(() -> new BgpPeerGroup("dummy")); // create dummy if the name is invalid.
  }

  @Override
  public void exitBgppg_definition(Bgppg_definitionContext ctx) {
    _currentBgpPeerGroup = null;
  }

  @Override
  public void exitBgppg_enable(Bgppg_enableContext ctx) {
    _currentBgpPeerGroup.setEnable(toBoolean(ctx.yn));
  }

  @Override
  public void enterBgppg_peer(Bgppg_peerContext ctx) {
    _currentBgpPeer =
        toString(ctx, ctx.name)
            .map(_currentBgpPeerGroup::getOrCreatePeerGroup)
            .orElseGet(() -> new BgpPeer("dummy")); // create dummy if the name is invalid.
  }

  @Override
  public void exitBgppgp_coi_allow(Bgppgp_coi_allowContext ctx) {
    _currentBgpPeer.getConnectionOptions().setIncomingAllow(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgppgp_coi_remote_port(Bgppgp_coi_remote_portContext ctx) {
    _currentBgpPeer.getConnectionOptions().setRemotePort(toInteger(ctx.p));
  }

  @Override
  public void enterBgppgp_co_multihop(Bgppgp_co_multihopContext ctx) {
    _currentBgpPeer.setMultihop(toInteger(ctx.num));
  }

  @Override
  public void exitBgppgp_coo_allow(Bgppgp_coo_allowContext ctx) {
    _currentBgpPeer.getConnectionOptions().setOutgoingAllow(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgppgp_coo_local_port(Bgppgp_coo_local_portContext ctx) {
    _currentBgpPeer.getConnectionOptions().setLocalPort(toInteger(ctx.p));
  }

  @Override
  public void exitBgppgp_enable(Bgppgp_enableContext ctx) {
    _currentBgpPeer.setEnable(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgppgp_enable_sender_side_loop_detection(
      Bgppgp_enable_sender_side_loop_detectionContext ctx) {
    _currentBgpPeer.setEnableSenderSideLoopDetection(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgppgp_la_interface(Bgppgp_la_interfaceContext ctx) {
    String name = getText(ctx.name);
    _currentBgpPeer.setLocalInterface(name);
    referenceStructure(INTERFACE, name, BGP_PEER_LOCAL_ADDRESS_INTERFACE, getLine(ctx.name.start));
  }

  @Override
  public void exitBgppgp_la_ip(Bgppgp_la_ipContext ctx) {
    ConcreteInterfaceAddress address = toConcreteInterfaceAddress(ctx.interface_address());
    _currentBgpPeer.setLocalAddress(address.getIp());
  }

  @Override
  public void exitBgppgp_max_prefixes(Bgppgp_max_prefixesContext ctx) {
    warn(ctx, "Batfish does not limit the number of prefixes received over BGP");
  }

  @Override
  public void exitBgppgp_peer_address(Bgppgp_peer_addressContext ctx) {
    toIp(ctx, ctx.addr, "BGP peer-address").ifPresent(_currentBgpPeer::setPeerAddress);
  }

  @Override
  public void exitBgppgp_peer_as(Bgppgp_peer_asContext ctx) {
    toAsn(ctx, ctx.asn, _currentBgpVr.getRoutingOptions().getAsFormat())
        .ifPresent(_currentBgpPeer::setPeerAs);
  }

  @Override
  public void exitBgppg_peer(Bgppg_peerContext ctx) {
    _currentBgpPeer = null;
  }

  @Override
  public void exitBgppgp_reflector_client(Bgppgp_reflector_clientContext ctx) {
    if (ctx.CLIENT() != null) {
      _currentBgpPeer.setReflectorClient(ReflectorClient.CLIENT);
    } else if (ctx.MESHED_CLIENT() != null) {
      _currentBgpPeer.setReflectorClient(ReflectorClient.MESHED_CLIENT);
    } else {
      assert ctx.NON_CLIENT() != null;
      _currentBgpPeer.setReflectorClient(ReflectorClient.NON_CLIENT);
    }
  }

  @Override
  public void enterBgppgt_ebgp(Bgppgt_ebgpContext ctx) {
    _currentBgpPeerGroup.updateAndGetEbgpType();
  }

  @Override
  public void exitBgppgte_export_nexthop(Bgppgte_export_nexthopContext ctx) {
    BgpPeerGroupTypeAndOptions options = _currentBgpPeerGroup.getTypeAndOptions();
    assert options instanceof EbgpPeerGroupType;
    EbgpPeerGroupType ebgp = (EbgpPeerGroupType) options;
    ebgp.setExportNexthop(
        ctx.RESOLVE() != null ? ExportNexthopMode.RESOLVE : ExportNexthopMode.USE_SELF);
  }

  @Override
  public void exitBgppgte_import_nexthop(Bgppgte_import_nexthopContext ctx) {
    BgpPeerGroupTypeAndOptions options = _currentBgpPeerGroup.getTypeAndOptions();
    assert options instanceof EbgpPeerGroupType;
    EbgpPeerGroupType ebgp = (EbgpPeerGroupType) options;
    ebgp.setImportNexthop(
        ctx.ORIGINAL() != null ? ImportNexthopMode.ORIGINAL : ImportNexthopMode.USE_PEER);
  }

  @Override
  public void exitBgppgte_remove_private_as(Bgppgte_remove_private_asContext ctx) {
    BgpPeerGroupTypeAndOptions options = _currentBgpPeerGroup.getTypeAndOptions();
    assert options instanceof EbgpPeerGroupType;
    EbgpPeerGroupType ebgp = (EbgpPeerGroupType) options;
    ebgp.setRemovePrivateAs(toBoolean(ctx.yn));
  }

  @Override
  public void enterBgppgt_ibgp(Bgppgt_ibgpContext ctx) {
    _currentBgpPeerGroup.updateAndGetIbgpType();
  }

  @Override
  public void exitBgp_reject_default_route(Bgp_reject_default_routeContext ctx) {
    _currentBgpVr.setRejectDefaultRoute(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgproa_aggregate_med(Bgproa_aggregate_medContext ctx) {
    _currentBgpVr.getRoutingOptions().setAggregateMed(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgpro_as_format(Bgpro_as_formatContext ctx) {
    if (ctx.TWO_BYTE() != null) {
      _currentBgpVr.getRoutingOptions().setAsFormat(AsFormat.TWO_BYTE_AS);
    } else {
      assert ctx.FOUR_BYTE() != null;
      _currentBgpVr.getRoutingOptions().setAsFormat(AsFormat.FOUR_BYTE_AS);
    }
  }

  @Override
  public void exitBgpro_default_local_preference(Bgpro_default_local_preferenceContext ctx) {
    _currentBgpVr.getRoutingOptions().setDefaultLocalPreference(toLong(ctx.pref));
  }

  @Override
  public void exitBgpro_reflector_cluster_id(Bgpro_reflector_cluster_idContext ctx) {
    _currentBgpVr.getRoutingOptions().setReflectorClusterId(toIp(ctx.id));
  }

  @Override
  public void exitBgprog_enable(Bgprog_enableContext ctx) {
    _currentBgpVr.getRoutingOptions().setGracefulRestartEnable(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgprom_always_compare_med(Bgprom_always_compare_medContext ctx) {
    _currentBgpVr.getRoutingOptions().setAlwaysCompareMed(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgprom_deterministic_med_comparison(
      Bgprom_deterministic_med_comparisonContext ctx) {
    _currentBgpVr.getRoutingOptions().setDeterministicMedComparison(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgp_router_id(Bgp_router_idContext ctx) {
    toIp(ctx, ctx.addr, "BGP router-id").ifPresent(_currentBgpVr::setRouterId);
  }

  @Override
  public void enterBgprr_prefix(Bgprr_prefixContext ctx) {
    _currentRedistRule =
        _currentBgpVr.getOrCreateRedistRule(new RedistRuleRefNameOrPrefix(toPrefix(ctx.prefix)));
  }

  @Override
  public void enterBgprr_ip_address(Bgprr_ip_addressContext ctx) {
    _currentRedistRule =
        _currentBgpVr.getOrCreateRedistRule(
            new RedistRuleRefNameOrPrefix(toIp(ctx.ip_address()).toPrefix()));
  }

  @Override
  public void enterBgprr_profile_name(Bgprr_profile_nameContext ctx) {
    String name = getText(ctx.name);
    _currentRedistRule = _currentBgpVr.getOrCreateRedistRule(new RedistRuleRefNameOrPrefix(name));
    referenceStructure(
        REDIST_PROFILE,
        computeObjectName(_currentVirtualRouter.getName(), name),
        REDIST_RULE_REDIST_PROFILE,
        getLine(ctx.name.start));
  }

  @Override
  public void exitBgprr_prefix(Bgprr_prefixContext ctx) {
    _currentRedistRule = null;
  }

  @Override
  public void exitBgprr_ip_address(Bgprr_ip_addressContext ctx) {
    _currentRedistRule = null;
  }

  @Override
  public void exitBgprr_profile_name(Bgprr_profile_nameContext ctx) {
    _currentRedistRule = null;
  }

  @Override
  public void exitBgprrg_address_family_identifier(Bgprrg_address_family_identifierContext ctx) {
    if (ctx.IPV4() != null) {
      _currentRedistRule.setAddressFamilyIdentifier(AddressFamilyIdentifier.IPV4);
    } else {
      assert ctx.IPV6() != null;
      _currentRedistRule.setAddressFamilyIdentifier(AddressFamilyIdentifier.IPV6);
    }
  }

  @Override
  public void exitBgprrg_route_table(Bgprrg_route_tableContext ctx) {
    if (ctx.BOTH() != null) {
      _currentRedistRule.setRouteTableType(RouteTableType.BOTH);
    } else if (ctx.MULTICAST() != null) {
      _currentRedistRule.setRouteTableType(RouteTableType.MULTICAST);
    } else {
      assert ctx.UNICAST() != null;
      _currentRedistRule.setRouteTableType(RouteTableType.UNICAST);
    }
  }

  @Override
  public void exitBgprrg_enable(Bgprrg_enableContext ctx) {
    _currentRedistRule.setEnable(toBoolean(ctx.yn));
  }

  @Override
  public void exitBgprrg_set_origin(Bgprrg_set_originContext ctx) {
    if (ctx.EGP() != null) {
      _currentRedistRule.setOrigin(OriginType.EGP);
    } else if (ctx.IGP() != null) {
      _currentRedistRule.setOrigin(OriginType.IGP);
    } else {
      assert ctx.INCOMPLETE() != null;
      _currentRedistRule.setOrigin(OriginType.INCOMPLETE);
    }
  }

  @Override
  public void exitCp_authentication(Cp_authenticationContext ctx) {
    if (_currentCrytoProfile.getType() == Type.IKE) {
      warn(ctx, "'authentication' is illegal for ike-crypto-profile");
      return;
    }
    IpsecAuthenticationAlgorithm algo = toIpsecAuthenticationAlgorithm(ctx);
    if (algo != null) {
      _currentCrytoProfile.setAuthAlgorithm(algo);
    }
  }

  @Override
  public void exitCp_dh_group(Cp_dh_groupContext ctx) {
    if (_currentCrytoProfile.getType() == Type.GLOBAL_PROTECT_APP) {
      warn(ctx, "'dh-group' is illegal for global-proptect-app-crypto-profile");
      return;
    }
    DiffieHellmanGroup dhGroup = toDiffieHellmanGroup(ctx);
    if (dhGroup != null) {
      _currentCrytoProfile.setDhGroup(dhGroup);
    }
  }

  @Override
  public void exitCp_encryption(Cp_encryptionContext ctx) {
    List<EncryptionAlgorithm> algos =
        ctx.algo.stream().map(this::toEncryptionAlgo).collect(Collectors.toList());
    _currentCrytoProfile.setEncryptionAlgorithms(algos);
  }

  @Override
  public void exitCp_hash(Cp_hashContext ctx) {
    if (_currentCrytoProfile.getType() != Type.IKE) {
      warn(ctx, "'hash' is illegal for non-Ike crypto profiles");
      return;
    }
    IkeHashingAlgorithm algo = toIkeHashingAlgorithm(ctx);
    if (algo != null) {
      _currentCrytoProfile.setHashAlgorithm(algo);
    }
  }

  @Override
  public void exitCp_lifetime(Cp_lifetimeContext ctx) {
    if (_currentCrytoProfile.getType() == Type.GLOBAL_PROTECT_APP) {
      warn(ctx, "'lifetime' is illegal for global-protect-app-crypto profile");
      return;
    }
    int val = toInteger(ctx.val);
    if (ctx.DAYS() != null) {
      val *= 24 * 60 * 60;
    } else if (ctx.HOURS() != null) {
      val *= 60 * 60;
    } else if (ctx.MINUTES() != null) {
      val *= 60;
    }
    _currentCrytoProfile.setLifetimeSeconds(val);
  }

  @Override
  public void exitIf_comment(If_commentContext ctx) {
    _currentInterface.setComment(getText(ctx.text));
  }

  @Override
  public void exitIf_tag(If_tagContext ctx) {
    toInteger(ctx, ctx.tag).ifPresent(_currentInterface::setTag);
  }

  @Override
  public void enterOspf_area(Ospf_areaContext ctx) {
    _currentOspfArea =
        toIp(ctx, ctx.addr, "OSPF area-id")
            .map(_currentOspfVr::getOrCreateOspfArea)
            // dummy area with Ip MAX
            .orElseGet(() -> new OspfArea(Ip.MAX));
  }

  @Override
  public void exitOspf_area(Ospf_areaContext ctx) {
    _currentOspfArea = null;
  }

  @Override
  public void enterOspfa_interface(Ospfa_interfaceContext ctx) {
    _currentOspfInterface = _currentOspfArea.getOrCreateOspfInterface(getText(ctx.name));
  }

  @Override
  public void exitOspfai_enable(Ospfai_enableContext ctx) {
    _currentOspfInterface.setEnable(toBoolean(ctx.yn));
  }

  @Override
  public void exitOspfai_hello_interval(Ospfai_hello_intervalContext ctx) {
    _currentOspfInterface.setHelloInterval(toInteger(ctx.hello_interval.uint16()));
  }

  @Override
  public void exitOspfai_link_type(Ospfai_link_typeContext ctx) {
    if (ctx.BROADCAST() != null) {
      _currentOspfInterface.setLinkType(LinkType.BROADCAST);
    } else if (ctx.P2P() != null) {
      _currentOspfInterface.setLinkType(LinkType.P2P);
    } else if (ctx.P2MP() != null) {
      _currentOspfInterface.setLinkType(LinkType.P2MP);
    }
  }

  @Override
  public void exitOspfai_metric(Ospfai_metricContext ctx) {
    _currentOspfInterface.setMetric(toInteger(ctx.metric));
  }

  @Override
  public void exitOspfai_passive(Ospfai_passiveContext ctx) {
    _currentOspfInterface.setPassive(toBoolean(ctx.yn));
  }

  @Override
  public void exitOspfai_priority(Ospfai_priorityContext ctx) {
    _currentOspfInterface.setPriority(toInteger(ctx.priority.uint8()));
  }

  @Override
  public void exitOspfai_retransmit_interval(Ospfai_retransmit_intervalContext ctx) {
    _currentOspfInterface.setRetransmitInterval(toInteger(ctx.retransmit_interval.uint16()));
  }

  @Override
  public void exitOspfai_transit_delay(Ospfai_transit_delayContext ctx) {
    _currentOspfInterface.setTransitDelay(toInteger(ctx.transit_delay.uint16()));
  }

  @Override
  public void exitOspfa_interface(Ospfa_interfaceContext ctx) {
    _currentOspfInterface = null;
  }

  @Override
  public void exitOspfai_dead_counts(Ospfai_dead_countsContext ctx) {
    _currentOspfInterface.setDeadCounts(toInteger(ctx.dead_counts.uint8()));
  }

  @Override
  public void enterOspfat_stub(Ospfat_stubContext ctx) {
    _currentOspfStubAreaType =
        Optional.ofNullable(_currentOspfArea.getTypeSettings())
            .filter(OspfAreaStub.class::isInstance)
            .map(OspfAreaStub.class::cast)
            .orElseGet(
                () -> {
                  // overwrite if missing or a different area type
                  OspfAreaStub newStubArea = new OspfAreaStub();
                  _currentOspfArea.setTypeSettings(newStubArea);
                  return newStubArea;
                });
  }

  @Override
  public void exitOspfat_stub(Ospfat_stubContext ctx) {
    _currentOspfStubAreaType = null;
  }

  @Override
  public void exitOspfat_normal(Ospfat_normalContext ctx) {
    _currentOspfArea.setTypeSettings(new OspfAreaNormal());
  }

  @Override
  public void exitOspfatsdr_disable(Ospfatsdr_disableContext ctx) {
    _currentOspfStubAreaType.setDefaultRouteDisable(true);
  }

  @Override
  public void exitOspfatsdr_advertise_metric(Ospfatsdr_advertise_metricContext ctx) {
    toInteger(ctx, ctx.metric).ifPresent(_currentOspfStubAreaType::setDefaultRouteMetric);
  }

  @Override
  public void exitOspfats_accept_summary(Ospfats_accept_summaryContext ctx) {
    _currentOspfStubAreaType.setAcceptSummary(toBoolean(ctx.yn));
  }

  @Override
  public void enterOspfat_nssa(Ospfat_nssaContext ctx) {
    _currentOspfNssaAreaType =
        Optional.ofNullable(_currentOspfArea.getTypeSettings())
            .filter(OspfAreaNssa.class::isInstance)
            .map(OspfAreaNssa.class::cast)
            .orElseGet(
                () -> {
                  // overwrite if missing or a different area type
                  OspfAreaNssa newNssaArea = new OspfAreaNssa();
                  _currentOspfArea.setTypeSettings(newNssaArea);
                  return newNssaArea;
                });
  }

  @Override
  public void exitOspfat_nssa(Ospfat_nssaContext ctx) {
    _currentOspfNssaAreaType = null;
  }

  @Override
  public void exitOspfatndr_disable(Ospfatndr_disableContext ctx) {
    _currentOspfNssaAreaType.setDefaultRouteDisable(true);
  }

  @Override
  public void exitOspfatndra_metric(Ospfatndra_metricContext ctx) {
    toInteger(ctx, ctx.metric).ifPresent(_currentOspfNssaAreaType::setDefaultRouteMetric);
  }

  @Override
  public void exitOspfatndra_type(Ospfatndra_typeContext ctx) {
    if (ctx.EXT_1() != null) {
      _currentOspfNssaAreaType.setDefaultRouteType(DefaultRouteType.EXT_1);
      return;
    }
    _currentOspfNssaAreaType.setDefaultRouteType(DefaultRouteType.EXT_2);
  }

  @Override
  public void exitOspfatn_accept_summary(Ospfatn_accept_summaryContext ctx) {
    _currentOspfNssaAreaType.setAcceptSummary(toBoolean(ctx.yn));
  }

  @Override
  public void exitOspf_enable(Ospf_enableContext ctx) {
    if (_currentOspfVr == null) {
      return;
    }
    _currentOspfVr.setEnable(toBoolean(ctx.yn));
  }

  @Override
  public void exitOspf_reject_default_route(Ospf_reject_default_routeContext ctx) {
    _currentOspfVr.setRejectDefaultRoute(toBoolean(ctx.yn));
  }

  @Override
  public void exitOspf_router_id(Ospf_router_idContext ctx) {
    toIp(ctx, ctx.addr, "OSPF router-id").ifPresent(_currentOspfVr::setRouterId);
  }

  @Override
  public void exitOspf_graceful_restart(Ospf_graceful_restartContext ctx) {
    // OSPF graceful restart is a protocol feature that only affects restart behavior,
    // not final routing state, so it's intentionally ignored (_null rules)
  }

  @Override
  public void enterPalo_alto_configuration(Palo_alto_configurationContext ctx) {
    _mainConfiguration = new PaloAltoConfiguration();
    _currentConfiguration = _mainConfiguration;
    _defaultVsys =
        _mainConfiguration.getVirtualSystems().computeIfAbsent(DEFAULT_VSYS_NAME, Vsys::new);
    _currentVsys = _defaultVsys;
    _currentDeviceGroup = null;
  }

  @Override
  public void enterS_pre_rulebase(S_pre_rulebaseContext ctx) {
    _currentRuleScope = RulebaseId.PRE;
  }

  @Override
  public void exitS_pre_rulebase(S_pre_rulebaseContext ctx) {
    _currentRuleScope = null;
  }

  @Override
  public void enterS_post_rulebase(S_post_rulebaseContext ctx) {
    _currentRuleScope = RulebaseId.POST;
  }

  @Override
  public void exitS_post_rulebase(S_post_rulebaseContext ctx) {
    _currentRuleScope = null;
  }

  @Override
  public void enterS_address_definition(S_address_definitionContext ctx) {
    String name = getText(ctx.name);
    if (_currentVsys.getAddressGroups().get(name) != null) {
      warn(
          ctx,
          String.format(
              "Cannot have an address object and group with the same name '%s'. Ignoring the"
                  + " object definition.",
              name));
      _currentAddressObject = new AddressObject(name);
    } else {
      _currentAddressObject =
          _currentVsys.getAddressObjects().computeIfAbsent(name, AddressObject::new);

      // Use constructed name so same-named defs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, name);
      defineFlattenedStructure(ADDRESS_OBJECT, uniqueName, ctx);
    }
  }

  @Override
  public void exitS_address_definition(S_address_definitionContext ctx) {
    _currentAddressObject = null;
  }

  @Override
  public void exitSa_tag(Sa_tagContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String tag = getText(var);
      _currentAddressObject.getTags().add(tag);
    }
  }

  @Override
  public void exitSag_tag(Sag_tagContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String tag = getText(var);
      _currentAddressGroup.getTags().add(tag);
    }
  }

  @Override
  public void exitSagd_filter(Sagd_filterContext ctx) {
    _currentAddressGroup.setFilter(getText(ctx.filter));
  }

  @Override
  public void enterS_tag(S_tagContext ctx) {
    String name = getText(ctx.name);
    _currentTag = _currentVsys.getTags().computeIfAbsent(name, Tag::new);
  }

  @Override
  public void exitS_tag(S_tagContext ctx) {
    _currentTag = null;
  }

  @Override
  public void exitSt_comments(St_commentsContext ctx) {
    _currentTag.setComments(getText(ctx.comments));
  }

  @Override
  public void enterS_address_group_definition(S_address_group_definitionContext ctx) {
    String name = getText(ctx.name);
    if (_currentVsys.getAddressObjects().get(name) != null) {
      warn(
          ctx,
          String.format(
              "Cannot have an address object and group with the same name '%s'. Ignoring the group"
                  + " definition.",
              name));
      _currentAddressGroup = new AddressGroup(name);
    } else {
      _currentAddressGroup =
          _currentVsys.getAddressGroups().computeIfAbsent(name, AddressGroup::new);

      // Use constructed name so same-named defs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, name);
      defineFlattenedStructure(ADDRESS_GROUP, uniqueName, ctx);
    }
  }

  @Override
  public void exitS_address_group_definition(S_address_group_definitionContext ctx) {
    _currentAddressGroup = null;
  }

  @Override
  public void enterS_application_definition(S_application_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentApplication =
        _currentVsys
            .getApplications()
            .computeIfAbsent(name, n -> Application.builder(name).build());
    // Use constructed name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, name);
    defineFlattenedStructure(APPLICATION, uniqueName, ctx);
  }

  @Override
  public void exitS_application_definition(S_application_definitionContext ctx) {
    _currentApplication = null;
  }

  @Override
  public void enterSappg_definition(Sappg_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentApplicationGroup =
        _currentVsys.getApplicationGroups().computeIfAbsent(name, ApplicationGroup::new);
    // Use constructed name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, name);
    defineFlattenedStructure(APPLICATION_GROUP, uniqueName, ctx);
  }

  @Override
  public void exitSappg_definition(Sappg_definitionContext ctx) {
    _currentApplicationGroup = null;
  }

  @Override
  public void exitSappg_members(Sappg_membersContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentApplicationGroup.getMembers().add(name);
      String uniqueName = computeObjectName(_currentVsys, name);
      referenceApplicationOrGroupLike(name, uniqueName, APPLICATION_GROUP_MEMBERS, var);
    }
  }

  @Override
  public void exitSapp_description(Sapp_descriptionContext ctx) {
    _currentApplication.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSapp_ignored(Sapp_ignoredContext ctx) {
    fileWarningApplicationStatementIgnored(ctx);
  }

  @Override
  public void enterS_zone_definition(S_zone_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentZone = _currentVsys.getZones().computeIfAbsent(name, n -> new Zone(n, _currentVsys));

    // Use constructed zone name so same-named zone defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, name);
    defineFlattenedStructure(ZONE, uniqueName, ctx);
  }

  @Override
  public void exitS_zone_definition(S_zone_definitionContext ctx) {
    _currentZone = null;
  }

  @Override
  public void exitSa_description(Sa_descriptionContext ctx) {
    _currentAddressObject.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSa_fqdn(Sa_fqdnContext ctx) {
    String fqdn = getText(ctx.null_rest_of_line());
    if (fqdn != null && !fqdn.trim().isEmpty()) {
      _currentAddressObject.setFqdn(fqdn.trim());
    }
  }

  @Override
  public void exitSa_ip_netmask(Sa_ip_netmaskContext ctx) {
    applyIpNetmask(_currentAddressObject, ctx.ip_netmask());
  }

  @Override
  public void exitSa_ip_range(Sa_ip_rangeContext ctx) {
    String rangeStr = ctx.ip_range().getText();
    if (rangeStr.contains(":")) {
      String[] ips = rangeStr.split("-");
      Ip6 lowIp = Ip6.parse(stripZoneIndex(ips[0]));
      Ip6 highIp = Ip6.parse(stripZoneIndex(ips[1]));
      if (lowIp.compareTo(highIp) >= 0) {
        warn(ctx, "Invalid IPv6 address range");
      } else {
        _currentAddressObject.setIpRange6(Range.closed(lowIp, highIp));
      }
    } else {
      toIpRange(ctx.ip_range()).ifPresent(range -> _currentAddressObject.setIpRange(range));
    }
  }

  @Override
  public void exitSag_description(Sag_descriptionContext ctx) {
    _currentAddressGroup.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSag_static(Sag_staticContext ctx) {
    for (VariableContext var : ctx.variable()) {
      String objectName = getText(var);
      if (objectName.equals(_currentAddressGroup.getName())) {
        warn(ctx, String.format("The address group '%s' cannot contain itself", objectName));
      } else {
        _currentAddressGroup.addMember(objectName);

        // Use constructed name so same-named defs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys, objectName);
        referenceStructure(ADDRESS_LIKE, uniqueName, ADDRESS_GROUP_STATIC, getLine(var.start));
      }
    }
  }

  @Override
  public void enterSds_default_gateway(Sds_default_gatewayContext ctx) {
    _currentConfiguration.setMgmtIfaceGateway(toIp(ctx.ip_address()));
  }

  @Override
  public void exitSds_domain(Sds_domainContext ctx) {
    _currentConfiguration.setDomain(getText(ctx.name));
  }

  @Override
  public void exitSds_hostname(Sds_hostnameContext ctx) {
    _currentConfiguration.setHostname(getText(ctx.name));
  }

  @Override
  public void enterSds_ip_address(Sds_ip_addressContext ctx) {
    _currentConfiguration.setMgmtIfaceAddress(toIp(ctx.ip_address()));
  }

  @Override
  public void enterSds_netmask(Sds_netmaskContext ctx) {
    _currentConfiguration.setMgmtIfaceNetmask(toIp(ctx.ip_address()));
  }

  @Override
  public void enterSds_ntp_servers(Sds_ntp_serversContext ctx) {
    _currentNtpServerPrimary = ctx.PRIMARY_NTP_SERVER() != null;
  }

  @Override
  public void exitSdsn_ntp_server_address(Sdsn_ntp_server_addressContext ctx) {
    if (_currentNtpServerPrimary) {
      _currentConfiguration.setNtpServerPrimary(getText(ctx.address));
    } else {
      _currentConfiguration.setNtpServerSecondary(getText(ctx.address));
    }
  }

  @Override
  public void exitSdsd_servers(Sdsd_serversContext ctx) {
    if (ctx.primary_name != null) {
      _currentConfiguration.setDnsServerPrimary(getText(ctx.primary_name));
    } else if (ctx.secondary_name != null) {
      _currentConfiguration.setDnsServerSecondary(getText(ctx.secondary_name));
    }
  }

  @Override
  public void enterSds_snmp_setting(Sds_snmp_settingContext ctx) {
    if (_currentConfiguration.getSnmpSetting() == null) {
      _currentConfiguration.setSnmpSetting(new SnmpSetting());
    }
  }

  @Override
  public void enterSdss_access_setting(Sdss_access_settingContext ctx) {
    // Will create a new access setting for each version encountered
  }

  @Override
  public void enterSdssa_definition(Sdssa_definitionContext ctx) {
    if (ctx.VERSION() != null) {
      String version = getText(ctx.variable());
      _currentSnmpAccessSetting = new SnmpAccessSetting(version);
    } else if (ctx.SNMP_COMMUNITY_STRING() != null) {
      if (_currentSnmpAccessSetting != null) {
        String communityString = getText(ctx.variable());
        _currentSnmpAccessSetting.addCommunityString(communityString);
      }
    }
  }

  @Override
  public void exitSdssa_definition(Sdssa_definitionContext ctx) {
    if (ctx.VERSION() != null && _currentSnmpAccessSetting != null) {
      _currentConfiguration.getSnmpSetting().addAccessSetting(_currentSnmpAccessSetting);
    }
  }

  @Override
  public void enterSdss_snmp_system(Sdss_snmp_systemContext ctx) {
    _currentSnmpSystem = new SnmpSystem();
    if (_currentConfiguration.getSnmpSetting() != null) {
      _currentConfiguration.getSnmpSetting().setSnmpSystem(_currentSnmpSystem);
    }
  }

  @Override
  public void exitSdss_snmp_system(Sdss_snmp_systemContext ctx) {
    if (_currentSnmpSystem != null) {
      if (ctx.SEND_EVENT_SPECIFIC_TRAPS() != null) {
        _currentSnmpSystem.setSendEventSpecificTraps(toBoolean(ctx.yes_or_no()));
      }
      if (ctx.CONTACT() != null && ctx.variable() != null) {
        _currentSnmpSystem.setContact(getText(ctx.variable()));
      }
      if (ctx.LOCATION() != null && ctx.variable() != null) {
        _currentSnmpSystem.setLocation(getText(ctx.variable()));
      }
    }
  }

  @Override
  public void enterS_external_list(S_external_listContext ctx) {
    if (ctx.name == null) {
      return;
    }
    _currentExternalListName = getFullText(ctx.name);
  }

  @Override
  public void exitS_external_list(S_external_listContext ctx) {
    _currentExternalListName = null;
  }

  @Override
  public void exitSelt_ip(Selt_ipContext ctx) {
    defineFlattenedStructure(
        EXTERNAL_LIST, computeObjectName(_currentVsys, _currentExternalListName), ctx);
  }

  @Override
  public void enterSn_shared_gateway_definition(Sn_shared_gateway_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentVsys = _currentConfiguration.getSharedGateways().computeIfAbsent(name, Vsys::new);
    defineFlattenedStructure(SHARED_GATEWAY, name, ctx);
  }

  @Override
  public void exitSn_shared_gateway_definition(Sn_shared_gateway_definitionContext ctx) {
    _currentVsys = null;
  }

  @Override
  public void exitSnsg_display_name(Snsg_display_nameContext ctx) {
    _currentVsys.setDisplayName(getText(ctx.name));
  }

  @Override
  public void exitSnsgi_interface(Snsgi_interfaceContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentVsys.getImportedInterfaces().add(name);
      referenceStructure(INTERFACE, name, IMPORT_INTERFACE, getLine(var.start));
    }
  }

  @Override
  public void enterSnsg_zone_definition(Snsg_zone_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentZone = _currentVsys.getZones().computeIfAbsent(name, n -> new Zone(n, _currentVsys));

    // Use constructed zone name so same-named zone defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, name);
    defineFlattenedStructure(ZONE, uniqueName, ctx);
  }

  @Override
  public void exitSnsg_zone_definition(Snsg_zone_definitionContext ctx) {
    _currentZone = null;
  }

  @Override
  public void exitSnsgzn_layer3(Snsgzn_layer3Context ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      if (_currentVsys.getImportedInterfaces().contains(name)) {
        _currentZone.getInterfaceNames().add(name);
      } else {
        warn(ctx, "Cannot add an interface to a shared-gateway zone before it is imported");
      }
      referenceStructure(INTERFACE, name, ZONE_INTERFACE, getLine(var.start));
      // Mark reference to zone when it has an interface in it (since the zone if effectively used
      // at this point)
      // Use constructed object name so same-named refs across vsys are unique
      String zoneName = computeObjectName(_currentVsys, _currentZone.getName());
      referenceStructure(ZONE, zoneName, LAYER3_INTERFACE_ZONE, getLine(var.start));
    }
  }

  @Override
  public void enterSet_line_config_devices(Set_line_config_devicesContext ctx) {
    if (ctx.name != null) {
      String deviceName = getText(ctx.name);
      _currentDeviceName = firstNonNull(_currentDeviceName, deviceName);
      if (!_currentDeviceName.equals(deviceName)) {
        /*
         * Do not currently handle multiple device names, which presumably happens only
         * if multiple
         * physical devices are configured from a single config
         */
        warn(ctx, "Multiple devices encountered: " + deviceName);
      }
    }
  }

  @Override
  public void enterSet_line_device_group(Set_line_device_groupContext ctx) {
    String deviceGroupName = getText(ctx.name);
    _currentDeviceGroup = _mainConfiguration.getOrCreateDeviceGroup(deviceGroupName);
    _currentConfiguration = _currentDeviceGroup;
    _currentVsys = getOrCreatePanoramaVsys();
  }

  @Override
  public void exitSet_line_device_group(Set_line_device_groupContext ctx) {
    _currentDeviceGroup = null;
    _currentConfiguration = _mainConfiguration;
    _currentVsys = _defaultVsys;
  }

  @Override
  public void exitSdg_description(Sdg_descriptionContext ctx) {
    _currentDeviceGroup.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSdg_devices(Sdg_devicesContext ctx) {
    if (_currentDeviceGroupVsys == null) {
      _currentDeviceGroup.addDevice(getText(ctx.device));
    } else {
      _currentDeviceGroup.addVsys(getText(ctx.device), _currentDeviceGroupVsys);
    }
    _currentDeviceGroupVsys = null;
  }

  @Override
  public void enterSrespr_devices(Srespr_devicesContext ctx) {
    _currentResultDeviceName = getText(ctx.name);
  }

  @Override
  public void exitSrespr_devices(Srespr_devicesContext ctx) {
    _currentResultDeviceName = null;
  }

  @Override
  public void exitSresprd_hostname(Sresprd_hostnameContext ctx) {
    _currentConfiguration.addHostnameMapping(_currentResultDeviceName, getText(ctx.hostname));
  }

  @Override
  public void enterSro_device_group(Sro_device_groupContext ctx) {
    String deviceGroupName = getText(ctx.name);
    _currentDeviceGroup = _mainConfiguration.getOrCreateDeviceGroup(deviceGroupName);
    _currentConfiguration = _currentDeviceGroup;
    _currentVsys = getOrCreatePanoramaVsys();
  }

  @Override
  public void exitSro_device_group(Sro_device_groupContext ctx) {
    _currentDeviceGroup = null;
    _currentConfiguration = _mainConfiguration;
    _currentVsys = _defaultVsys;
  }

  @Override
  public void exitSdg_parent_dg(Sdg_parent_dgContext ctx) {
    _currentDeviceGroup.setParentDg(getText(ctx.name));
  }

  @Override
  public void exitSdgd_vsys(Sdgd_vsysContext ctx) {
    _currentDeviceGroupVsys = getText(ctx.name);
  }

  @Override
  public void enterSet_line_template(Set_line_templateContext ctx) {
    String templateName = getText(ctx.name);
    defineFlattenedStructure(TEMPLATE, templateName, ctx);
    _currentTemplate = _mainConfiguration.getOrCreateTemplate(templateName);
    _currentConfiguration = _currentTemplate;
    _currentVsys = null;
  }

  @Override
  public void exitSet_line_template(Set_line_templateContext ctx) {
    _currentTemplate = null;
    _currentConfiguration = _mainConfiguration;
    _currentVsys = _defaultVsys;
  }

  @Override
  public void exitSt_description(St_descriptionContext ctx) {
    _currentTemplate.setDescription(getText(ctx.description));
  }

  @Override
  public void enterSt_variable(PaloAltoParser.St_variableContext ctx) {
    _currentTemplateVariableName = toString(ctx, ctx.variable_name());
  }

  @Override
  public void exitSt_variable(PaloAltoParser.St_variableContext ctx) {
    _currentTemplateVariableName = null;
  }

  @Override
  public void exitStvt_ip_netmask(PaloAltoParser.Stvt_ip_netmaskContext ctx) {
    _currentTemplateVariableName.ifPresent(
        name -> {
          // Store the variable as an address object, since that is how it is used
          AddressObject addr = new AddressObject(name);
          applyIpNetmask(addr, ctx.ip_netmask());
          getOrCreateDefaultVsys(_currentTemplate).getAddressObjects().put(name, addr);

          defineFlattenedStructure(ADDRESS_OBJECT, computeObjectName(_currentVsys, name), ctx);
        });
  }

  /** Apply the ip-netmask (ip addr or prefix) to the specified {@link AddressObject}. */
  private void applyIpNetmask(AddressObject addressObject, PaloAltoParser.Ip_netmaskContext ctx) {
    if (ctx.ip_address() != null) {
      String ipStr = ctx.ip_address().getText();
      if (ipStr.contains(":")) {
        // Strip zone index if present
        int percentIdx = ipStr.indexOf('%');
        String baseIp = percentIdx != -1 ? ipStr.substring(0, percentIdx) : ipStr;
        addressObject.setIp(Ip6.parse(baseIp));
      } else {
        addressObject.setIp(Ip.parse(ipStr));
      }
      return;
    }
    assert ctx.ip_prefix() != null;
    String prefixStr = ctx.ip_prefix().getText();
    if (prefixStr.contains(":")) {
      // IPv6 Prefix. Splitting on / to handle base IP possibly having a zone index
      String[] parts = prefixStr.split("/", 2);
      String baseIp = parts[0];
      int percentIdx = baseIp.indexOf('%');
      String strippedBaseIp = percentIdx != -1 ? baseIp.substring(0, percentIdx) : baseIp;
      String strippedPrefixStr = strippedBaseIp + (parts.length > 1 ? "/" + parts[1] : "");
      addressObject.setPrefix(Ip6Prefix.parse(strippedPrefixStr));
    } else {
      addressObject.setPrefix(toIpPrefix(ctx.ip_prefix()));
    }
  }

  @Override
  public void exitStvt_ip_range(PaloAltoParser.Stvt_ip_rangeContext ctx) {
    _currentTemplateVariableName.ifPresent(
        name -> {
          toIpRange(ctx.ip_range())
              .ifPresent(
                  range -> {
                    // Store the variable as an address object, since that is how it is used
                    AddressObject addr = new AddressObject(name);
                    addr.setIpRange(range);
                    getOrCreateDefaultVsys(_currentTemplate).getAddressObjects().put(name, addr);

                    defineFlattenedStructure(
                        ADDRESS_OBJECT, computeObjectName(_currentVsys, name), ctx);
                  });
        });
  }

  @Override
  public void enterSet_line_template_stack(Set_line_template_stackContext ctx) {
    String templateStackName = getText(ctx.name);
    _currentTemplateStack = _mainConfiguration.getOrCreateTemplateStack(templateStackName);
  }

  @Override
  public void exitSet_line_template_stack(Set_line_template_stackContext ctx) {
    _currentTemplateStack = null;
  }

  @Override
  public void exitSts_description(Sts_descriptionContext ctx) {
    _currentTemplateStack.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSts_devices(Sts_devicesContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      _currentTemplateStack.addDevice(getText(var));
    }
  }

  @Override
  public void exitSts_templates(Sts_templatesContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String templateName = getText(var);
      referenceStructure(TEMPLATE, templateName, TEMPLATE_STACK_TEMPLATES, getLine(ctx.start));
      _currentTemplateStack.addTemplate(templateName);
    }
  }

  @Override
  public void enterS_policy_panorama(S_policy_panoramaContext ctx) {
    _currentVsys = getOrCreatePanoramaVsys();
  }

  @Override
  public void exitS_policy_panorama(S_policy_panoramaContext ctx) {
    _currentVsys = _defaultVsys;
  }

  @Override
  public void enterVr_definition(Vr_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentVirtualRouter =
        _currentConfiguration.getVirtualRouters().computeIfAbsent(name, VirtualRouter::new);
    defineFlattenedStructure(VIRTUAL_ROUTER, name, ctx);
    referenceStructure(
        VIRTUAL_ROUTER, name, VIRTUAL_ROUTER_SELF_REFERENCE, getLine(ctx.name.getStart()));
  }

  @Override
  public void exitVr_definition(Vr_definitionContext ctx) {
    _currentVirtualRouter = null;
  }

  @Override
  public void exitVr_ecmp_enable(Vr_ecmp_enableContext ctx) {
    if (!toBoolean(ctx.yes_or_no())) {
      warn(ctx, "Disabling of ECMP for IGP is not supported");
    }
  }

  @Override
  public void enterSni_aggregate_ethernet_definition(Sni_aggregate_ethernet_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentParentInterface =
        _currentConfiguration
            .getInterfaces()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.AGGREGATED_ETHERNET));
    _currentInterface = _currentParentInterface;
    defineFlattenedStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSni_aggregate_ethernet_definition(Sni_aggregate_ethernet_definitionContext ctx) {
    _currentParentInterface = null;
    _currentInterface = null;
  }

  @Override
  public void enterSni_ethernet_definition(Sni_ethernet_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentParentInterface =
        _currentConfiguration
            .getInterfaces()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.PHYSICAL));
    _currentInterface = _currentParentInterface;
    defineFlattenedStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSni_ethernet_definition(Sni_ethernet_definitionContext ctx) {
    _currentParentInterface = null;
    _currentInterface = null;
  }

  @Override
  public void enterSni_loopback(Sni_loopbackContext ctx) {
    String name = getText(ctx.LOOPBACK());
    _currentParentInterface =
        _currentConfiguration
            .getInterfaces()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.LOOPBACK));
    _currentInterface = _currentParentInterface;
    defineFlattenedStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSni_loopback(Sni_loopbackContext ctx) {
    _currentParentInterface = null;
    _currentInterface = null;
  }

  @Override
  public void enterSni_tunnel(Sni_tunnelContext ctx) {
    String name = getText(ctx.TUNNEL());
    _currentParentInterface =
        _currentConfiguration
            .getInterfaces()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.TUNNEL));
    _currentInterface = _currentParentInterface;
    defineFlattenedStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSni_tunnel(Sni_tunnelContext ctx) {
    _currentParentInterface = null;
    _currentInterface = null;
  }

  @Override
  public void enterSni_vlan(Sni_vlanContext ctx) {
    String name = getText(ctx.VLAN());
    _currentParentInterface =
        _currentConfiguration
            .getInterfaces()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.VLAN));
    _currentInterface = _currentParentInterface;
    defineFlattenedStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSni_vlan(Sni_vlanContext ctx) {
    _currentParentInterface = null;
    _currentInterface = null;
  }

  @Override
  public void enterSnicp_global_protect(Snicp_global_protectContext ctx) {
    String name = getText(ctx.name);
    _currentCrytoProfile =
        _currentConfiguration.getCryptoProfileOrCreate(name, Type.GLOBAL_PROTECT_APP);
  }

  @Override
  public void exitSnicp_global_protect(Snicp_global_protectContext ctx) {
    _currentCrytoProfile = null;
  }

  @Override
  public void enterSnicp_ike_crypto_profiles(Snicp_ike_crypto_profilesContext ctx) {
    String name = getText(ctx.name);
    _currentCrytoProfile = _currentConfiguration.getCryptoProfileOrCreate(name, Type.IKE);
  }

  @Override
  public void exitSnicp_ike_crypto_profiles(Snicp_ike_crypto_profilesContext ctx) {
    _currentCrytoProfile = null;
  }

  @Override
  public void enterSnicp_ipsec_crypto_profiles(Snicp_ipsec_crypto_profilesContext ctx) {
    String name = getText(ctx.name);
    _currentCrytoProfile = _currentConfiguration.getCryptoProfileOrCreate(name, Type.IPSEC);
  }

  @Override
  public void exitSnicp_ipsec_crypto_profiles(Snicp_ipsec_crypto_profilesContext ctx) {
    _currentCrytoProfile = null;
  }

  @Override
  public void exitSnie_aggregate_group(Snie_aggregate_groupContext ctx) {
    String aeName = getText(ctx.group);
    _currentInterface.setAggregateGroup(aeName);
    referenceStructure(INTERFACE, aeName, ETHERNET_AGGREGATE_GROUP, getLine(ctx.group.start));
  }

  @Override
  public void exitSnie_ha(Snie_haContext ctx) {
    _currentInterface.setHa(true);
  }

  @Override
  public void exitSnie_link_state(Snie_link_stateContext ctx) {
    _currentInterface.setActive((ctx.DOWN() == null));
  }

  @Override
  public void enterSniel2_unit(Sniel2_unitContext ctx) {
    String name = getText(ctx.name);
    _currentInterface =
        _currentParentInterface
            .getUnits()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.LAYER2));
    _currentInterface.setParent(_currentParentInterface);
    defineFlattenedStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSniel2_unit(Sniel2_unitContext ctx) {
    _currentInterface = _currentParentInterface;
  }

  @Override
  public void exitSniel3_ip(Sniel3_ipContext ctx) {
    InterfaceAddress address = toInterfaceAddress(ctx.address);
    _currentInterface.addAddress(address);
    referenceInterfaceAddress(ctx.address, LAYER3_INTERFACE_ADDRESS);
  }

  @Override
  public void exitSniel3_mtu(Sniel3_mtuContext ctx) {
    _currentInterface.setMtu(Integer.parseInt(getText(ctx.mtu)));
  }

  @Override
  public void enterSniel3_unit(Sniel3_unitContext ctx) {
    String name = getText(ctx.name);
    _currentInterface =
        _currentParentInterface
            .getUnits()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.LAYER3));
    _currentInterface.setParent(_currentParentInterface);
    defineFlattenedStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSniel3_unit(Sniel3_unitContext ctx) {
    _currentInterface = _currentParentInterface;
  }

  @Override
  public void exitSnil_ip(Snil_ipContext ctx) {
    InterfaceAddress address = toInterfaceAddress(ctx.address);
    _currentInterface.addAddress(address);
    referenceInterfaceAddress(ctx.address, LOOPBACK_INTERFACE_ADDRESS);
  }

  @Override
  public void enterSnil_unit(Snil_unitContext ctx) {
    String name = getText(ctx.name);
    _currentInterface =
        _currentParentInterface
            .getUnits()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.LOOPBACK));
    _currentInterface.setParent(_currentParentInterface);
    defineFlattenedStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSnil_unit(Snil_unitContext ctx) {
    _currentInterface = _currentParentInterface;
  }

  @Override
  public void enterSnit_unit(Snit_unitContext ctx) {
    String name = getText(ctx.name);
    _currentInterface =
        _currentParentInterface
            .getUnits()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.TUNNEL_UNIT));
    _currentInterface.setParent(_currentParentInterface);
    defineFlattenedStructure(INTERFACE, name, ctx);
  }

  @Override
  public void exitSnit_unit(Snit_unitContext ctx) {
    _currentInterface = _currentParentInterface;
  }

  @Override
  public void enterSniv_unit(Sniv_unitContext ctx) {
    String name = getText(ctx.name);
    _currentInterface =
        _currentParentInterface
            .getUnits()
            .computeIfAbsent(name, n -> new Interface(n, Interface.Type.VLAN_UNIT));
    _currentInterface.setParent(_currentParentInterface);
    defineFlattenedStructure(INTERFACE, name, ctx);
    // Extract VLAN tag from unit name (e.g., "vlan.1" -> tag=1)
    try {
      String[] parts = name.split("\\.");
      if (parts.length == 2) {
        int vlanTag = Integer.parseInt(parts[1]);
        _currentInterface.setTag(vlanTag);
      }
    } catch (NumberFormatException e) {
      // If VLAN tag cannot be parsed, interface will be created without tag
    }
  }

  @Override
  public void enterSp_custom_url_category(PaloAltoParser.Sp_custom_url_categoryContext ctx) {
    Optional<String> maybeName = toString(ctx, ctx.custom_url_category_name());
    if (maybeName.isPresent()) {
      String name = maybeName.get();
      _currentCustomUrlCategory = _currentVsys.getOrCreateCustomUrlCategory(name);
      defineFlattenedStructure(
          CUSTOM_URL_CATEGORY, computeObjectName(_currentVsys.getName(), name), ctx);
    } else {
      _currentCustomUrlCategory = new CustomUrlCategory(getText(ctx.custom_url_category_name()));
    }
  }

  @Override
  public void exitSp_custom_url_category(PaloAltoParser.Sp_custom_url_categoryContext ctx) {
    _currentCustomUrlCategory = null;
  }

  @Override
  public void exitSpc_definition(PaloAltoParser.Spc_definitionContext ctx) {
    // Individual handlers (exitSpc_description, exitSpc_list, exitSpc_type) process the content
  }

  @Override
  public void exitSpc_description(PaloAltoParser.Spc_descriptionContext ctx) {
    _currentCustomUrlCategory.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSpc_list(PaloAltoParser.Spc_listContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String url = getText(var);
      if (PaloAltoConfiguration.unexpectedUnboundedCustomUrlWildcard(url)) {
        warn(
            ctx,
            String.format(
                "Did you mean '%s/'? Without the trailing slash, the url will match additional"
                    + " trailing domains, such as '%s.evil'.",
                url, url));
      }
      _currentCustomUrlCategory.addToList(url);
    }
  }

  @Override
  public void exitSpc_type(PaloAltoParser.Spc_typeContext ctx) {
    if (!getText(ctx.type).equals(CustomUrlCategory.TYPE_URL_LIST)) {
      warn(
          ctx,
          String.format(
              "Currently only '%s' custom-url-category type is supported by Batfish.",
              CustomUrlCategory.TYPE_URL_LIST));
    }
  }

  @Override
  public void exitSniv_unit(Sniv_unitContext ctx) {
    _currentInterface = _currentParentInterface;
  }

  @Override
  public void exitVrad_ebgp(Vrad_ebgpContext ctx) {
    toInteger(ctx, ctx.ad).ifPresent(_currentVirtualRouter.getAdminDists()::setEbgp);
  }

  @Override
  public void exitVrad_ibgp(Vrad_ibgpContext ctx) {
    toInteger(ctx, ctx.ad).ifPresent(_currentVirtualRouter.getAdminDists()::setIbgp);
  }

  @Override
  public void exitVrad_ospf_ext(Vrad_ospf_extContext ctx) {
    toInteger(ctx, ctx.ad).ifPresent(_currentVirtualRouter.getAdminDists()::setOspfExt);
  }

  @Override
  public void exitVrad_ospf_int(Vrad_ospf_intContext ctx) {
    toInteger(ctx, ctx.ad).ifPresent(_currentVirtualRouter.getAdminDists()::setOspfInt);
  }

  @Override
  public void exitVrad_ospfv3_ext(Vrad_ospfv3_extContext ctx) {
    toInteger(ctx, ctx.ad).ifPresent(_currentVirtualRouter.getAdminDists()::setOspfV3Ext);
  }

  @Override
  public void exitVrad_ospfv3_int(Vrad_ospfv3_intContext ctx) {
    toInteger(ctx, ctx.ad).ifPresent(_currentVirtualRouter.getAdminDists()::setOspfV3Int);
  }

  @Override
  public void exitVrad_rip(Vrad_ripContext ctx) {
    toInteger(ctx, ctx.ad).ifPresent(_currentVirtualRouter.getAdminDists()::setRip);
  }

  @Override
  public void exitVrad_static(Vrad_staticContext ctx) {
    toInteger(ctx, ctx.ad).ifPresent(_currentVirtualRouter.getAdminDists()::setStatic);
  }

  @Override
  public void exitVrad_static_ipv6(Vrad_static_ipv6Context ctx) {
    toInteger(ctx, ctx.ad).ifPresent(_currentVirtualRouter.getAdminDists()::setStaticv6);
  }

  @Override
  public void enterVr_routing_table(Vr_routing_tableContext ctx) {
    _currentStaticRoute =
        _currentVirtualRouter
            .getStaticRoutes()
            .computeIfAbsent(getText(ctx.name), StaticRoute::new);
  }

  @Override
  public void exitVr_interface(Vr_interfaceContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentVirtualRouter.getInterfaceNames().add(name);
      referenceStructure(INTERFACE, name, VIRTUAL_ROUTER_INTERFACE, getLine(var.start));
    }
  }

  @Override
  public void exitVr_routing_table(Vr_routing_tableContext ctx) {
    _currentStaticRoute = null;
  }

  @Override
  public void enterVrp_bgp(Vrp_bgpContext ctx) {
    _currentBgpVr = _currentVirtualRouter.getOrCreateBgp();
  }

  @Override
  public void enterVrp_ospf(Vrp_ospfContext ctx) {
    _currentOspfVr = _currentVirtualRouter.getOrCreateOspf();
  }

  @Override
  public void exitVrp_ospf(Vrp_ospfContext ctx) {
    _currentOspfVr = null;
  }

  @Override
  public void enterVrp_redist_profile(Vrp_redist_profileContext ctx) {
    String name = getText(ctx.name);
    _currentRedistProfile = _currentVirtualRouter.getOrCreateRedistProfile(name);
    defineFlattenedStructure(
        REDIST_PROFILE, computeObjectName(_currentVirtualRouter.getName(), name), ctx);
  }

  @Override
  public void exitVrp_redist_profile(Vrp_redist_profileContext ctx) {
    _currentRedistProfile = null;
  }

  @Override
  public void exitVrprp_action(Vrprp_actionContext ctx) {
    if (ctx.REDIST() != null) {
      _currentRedistProfile.setAction(RedistProfile.Action.REDIST);
    } else {
      assert ctx.NO_REDIST() != null;
      _currentRedistProfile.setAction(RedistProfile.Action.NO_REDIST);
    }
  }

  @Override
  public void exitVrprp_priority(Vrprp_priorityContext ctx) {
    _currentRedistProfile.setPriority(toInteger(ctx.redist_profile_priority().uint8()));
  }

  @Override
  public void exitVrprpf_destination(Vrprpf_destinationContext ctx) {
    for (Ip_prefixContext prefix : prefixes(ctx.ip_prefix_list())) {
      _currentRedistProfile.getOrCreateFilter().getDestinationPrefixes().add(toPrefix(prefix));
    }
  }

  @Override
  public void exitVrprpf_type(Vrprpf_typeContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String protocol = getText(var);
      RoutingProtocol routingProtocol = null;
      switch (protocol) {
        case "bgp":
          routingProtocol = RoutingProtocol.BGP;
          break;
        case "ospf":
          routingProtocol = RoutingProtocol.OSPF;
          break;
        case "static":
          routingProtocol = RoutingProtocol.STATIC;
          break;
        case "rip":
          routingProtocol = RoutingProtocol.RIP;
          break;
        case "connect":
          routingProtocol = RoutingProtocol.CONNECTED;
          break;
        default:
          _w.redFlagf(
              "type = %s is not valid under redist-profile %s",
              protocol, _currentRedistProfile.getName());
          break;
      }
      if (routingProtocol != null) {
        _currentRedistProfile.getOrCreateFilter().getRoutingProtocols().add(routingProtocol);
      }
    }
  }

  @Override
  public void exitVrp_bgp(Vrp_bgpContext ctx) {
    _currentBgpVr = null;
  }

  @Override
  public void exitVrrt_admin_dist(Vrrt_admin_distContext ctx) {
    toInteger(ctx, ctx.distance).ifPresent(_currentStaticRoute::setAdminDistance);
  }

  @Override
  public void exitVrrt_destination(Vrrt_destinationContext ctx) {
    _currentStaticRoute.setDestination(toPrefix(ctx.destination));
  }

  @Override
  public void exitVrrt_interface(Vrrt_interfaceContext ctx) {
    String name = getText(ctx.iface);
    _currentStaticRoute.setNextHopInterface(name);
    referenceStructure(INTERFACE, name, STATIC_ROUTE_INTERFACE, getLine(ctx.iface.start));
  }

  @Override
  public void exitVrrt_metric(Vrrt_metricContext ctx) {
    _currentStaticRoute.setMetric(Integer.parseInt(getText(ctx.metric)));
  }

  @Override
  public void exitVrrtn_discard(Vrrtn_discardContext ctx) {
    _currentStaticRoute.setNextHopDiscard();
  }

  @Override
  public void exitVrrtn_ip(Vrrtn_ipContext ctx) {
    toIp(ctx, ctx.addr, "static route nexthop ip-address")
        .ifPresent(_currentStaticRoute::setNextHopIp);
  }

  @Override
  public void exitVrrtn_next_vr(Vrrtn_next_vrContext ctx) {
    String name = getText(ctx.name);
    _currentStaticRoute.setNextVr(name);
    referenceStructure(VIRTUAL_ROUTER, name, STATIC_ROUTE_NEXT_VR, getLine(ctx.name.start));
  }

  @Override
  public void exitVrrtpm_enable(Vrrtpm_enableContext ctx) {
    boolean enabled = toBoolean(ctx.yn);
    if (enabled) {
      warn(ctx, "Static route path monitoring is not implemented");
    }
  }

  @Override
  public void enterSrao_definition(Srao_definitionContext ctx) {
    String name = getText(ctx.name);
    Rulebase rulebase = getRulebase();
    _currentApplicationOverrideRule =
        rulebase.getApplicationOverrideRules().computeIfAbsent(name, ApplicationOverrideRule::new);

    // Use constructed name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, name);
    defineFlattenedStructure(APPLICATION_OVERRIDE_RULE, uniqueName, ctx);
    referenceStructure(
        APPLICATION_OVERRIDE_RULE,
        uniqueName,
        APPLICATION_OVERRIDE_RULE_SELF_REF,
        getLine(ctx.name.start));
  }

  @Override
  public void exitSrao_definition(Srao_definitionContext ctx) {
    _currentApplicationOverrideRule = null;
  }

  @Override
  public void exitSrao_application(Srao_applicationContext ctx) {
    String application = getText(ctx.application);
    _currentApplicationOverrideRule.setApplication(application);
    String uniqueName = computeObjectName(_currentVsys, application);
    referenceApplicationLike(application, uniqueName, APPLICATION_OVERRIDE_RULE_APPLICATION, ctx);
  }

  @Override
  public void exitSrao_description(Srao_descriptionContext ctx) {
    _currentApplicationOverrideRule.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSrao_disabled(Srao_disabledContext ctx) {
    _currentApplicationOverrideRule.setDisabled(toBoolean(ctx.yn));
  }

  @Override
  public void exitSrao_port(Srao_portContext ctx) {
    for (Port_or_rangeContext item : ctx.variable_port_list().port_or_range()) {
      if (item.port != null) {
        _currentApplicationOverrideRule.addPort(toInteger(item.port));
      } else {
        assert item.range != null;
        _currentApplicationOverrideRule.addPorts(new SubRange(getText(item.range)));
      }
    }
  }

  @Override
  public void exitSrao_protocol(Srao_protocolContext ctx) {
    _currentApplicationOverrideRule.setProtocol(toApplicationOverrideProtocol(ctx.protocol));
  }

  @Override
  public void exitSrao_destination(Srao_destinationContext ctx) {
    for (Src_or_dst_list_itemContext var : ctx.src_or_dst_list().src_or_dst_list_item()) {
      RuleEndpoint endpoint = toRuleEndpoint(var);
      _currentApplicationOverrideRule.getDestination().add(endpoint);

      // Use constructed object name so same-named refs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, endpoint.getValue());

      // At this time, don't know if something that looks like a constant (e.g. IP address) is a
      // reference or not.  So mark a reference to a very permissive abstract structure type.
      PaloAltoStructureType type = ADDRESS_LIKE_OR_NONE;
      if (endpoint.getType() == RuleEndpoint.Type.REFERENCE) {
        // We know this reference doesn't look like a valid constant, so it must be pointing to an
        // object
        type = ADDRESS_LIKE;
      }
      referenceStructure(
          type, uniqueName, APPLICATION_OVERRIDE_RULE_DESTINATION, getLine(var.start));
    }
  }

  @Override
  public void exitSrao_negate_destination(Srao_negate_destinationContext ctx) {
    _currentApplicationOverrideRule.setNegateDestination(toBoolean(ctx.yn));
  }

  @Override
  public void exitSrao_source(Srao_sourceContext ctx) {
    for (Src_or_dst_list_itemContext var : ctx.src_or_dst_list().src_or_dst_list_item()) {
      RuleEndpoint endpoint = toRuleEndpoint(var);
      _currentApplicationOverrideRule.getSource().add(endpoint);
      // Use constructed object name so same-named refs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, endpoint.getValue());

      // At this time, don't know if something that looks like a constant (e.g. IP address) is a
      // reference or not.  So mark a reference to a very permissive abstract structure type.
      PaloAltoStructureType type = ADDRESS_LIKE_OR_NONE;
      if (endpoint.getType() == RuleEndpoint.Type.REFERENCE) {
        type = ADDRESS_LIKE;
      }
      referenceStructure(type, uniqueName, APPLICATION_OVERRIDE_RULE_SOURCE, getLine(var.start));
    }
  }

  @Override
  public void exitSrao_negate_source(Srao_negate_sourceContext ctx) {
    _currentApplicationOverrideRule.setNegateSource(toBoolean(ctx.yn));
  }

  @Override
  public void exitSrao_tag(Srao_tagContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      _currentApplicationOverrideRule.getTags().add(getText(var));
    }
  }

  @Override
  public void exitSrao_from(Srao_fromContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String zoneName = getText(var);
      _currentApplicationOverrideRule.getFrom().add(zoneName);

      if (!zoneName.equals(CATCHALL_ZONE_NAME)) {
        // Use constructed object name so same-named refs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys, zoneName);
        referenceStructure(
            ZONE, uniqueName, APPLICATION_OVERRIDE_RULE_FROM_ZONE, getLine(var.start));
      }
    }
  }

  @Override
  public void exitSrao_to(Srao_toContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String zoneName = getText(var);
      _currentApplicationOverrideRule.getTo().add(zoneName);

      if (!zoneName.equals(CATCHALL_ZONE_NAME)) {
        // Use constructed object name so same-named refs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys, zoneName);
        referenceStructure(ZONE, uniqueName, APPLICATION_OVERRIDE_RULE_TO_ZONE, getLine(var.start));
      }
    }
  }

  @Override
  public void enterSd_high_availability(PaloAltoParser.Sd_high_availabilityContext ctx) {
    _currentHighAvailability = _currentConfiguration.getOrCreateHighAvailability();
  }

  @Override
  public void exitSd_high_availability(PaloAltoParser.Sd_high_availabilityContext ctx) {
    _currentHighAvailability = null;
  }

  @Override
  public void exitSdhagmaa_device_id(PaloAltoParser.Sdhagmaa_device_idContext ctx) {
    toInteger(ctx, ctx.active_active_device_id()).ifPresent(_currentHighAvailability::setDeviceId);
  }

  @Override
  public void exitSrn_active_active_device_binding(Srn_active_active_device_bindingContext ctx) {
    toActiveActiveDeviceBinding(ctx, ctx.bind)
        .ifPresent(_currentNatRule::setActiveActiveDeviceBinding);
  }

  private @Nonnull Optional<NatRule.ActiveActiveDeviceBinding> toActiveActiveDeviceBinding(
      ParserRuleContext ctx, PaloAltoParser.Active_active_device_binding_valContext deviceBinding) {
    if (deviceBinding.PRIMARY() != null) {
      return Optional.of(NatRule.ActiveActiveDeviceBinding.PRIMARY);
    } else if (deviceBinding.BOTH() != null) {
      return Optional.of(NatRule.ActiveActiveDeviceBinding.BOTH);
    } else if (deviceBinding.uint8() != null) {
      int val = Integer.parseInt(deviceBinding.uint8().getText());
      if (val == 0) {
        return Optional.of(NatRule.ActiveActiveDeviceBinding.ZERO);
      } else if (val == 1) {
        return Optional.of(NatRule.ActiveActiveDeviceBinding.ONE);
      }
      warn(ctx, "Invalid active-active-device-binding value: " + val);
      return Optional.empty();
    } else {
      warn(ctx, "Invalid active-active-device-binding value");
      return Optional.empty();
    }
  }

  private static final IntegerSpace HA_ACTIVE_ACTIVE_DEVICE_ID_SPACE =
      IntegerSpace.of(Range.closed(0, 1));

  private Optional<Integer> toInteger(
      ParserRuleContext ctx, PaloAltoParser.Active_active_device_idContext deviceId) {
    return toIntegerInSpace(
        ctx,
        deviceId,
        HA_ACTIVE_ACTIVE_DEVICE_ID_SPACE,
        "high-availability active-active device-id");
  }

  private static final IntegerSpace ACTIVE_ACTIVE_DEVICE_BINDING_SPACE =
      IntegerSpace.of(Range.closed(0, 1));

  private Optional<Integer> toInteger(
      ParserRuleContext ctx, PaloAltoParser.Active_active_device_binding_valContext deviceBinding) {
    return toIntegerInSpace(
        ctx, deviceBinding, ACTIVE_ACTIVE_DEVICE_BINDING_SPACE, "active-active-device-binding");
  }

  @Override
  public void enterSrn_definition(Srn_definitionContext ctx) {
    String name = getText(ctx.name);
    Rulebase rulebase = getRulebase();
    _currentNatRule = rulebase.getNatRules().computeIfAbsent(name, NatRule::new);

    // Use constructed name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, name);
    defineFlattenedStructure(NAT_RULE, uniqueName, ctx);
    referenceStructure(NAT_RULE, uniqueName, NAT_RULE_SELF_REF, getLine(ctx.name.start));
  }

  @Override
  public void exitSrn_definition(Srn_definitionContext ctx) {
    _currentNatRule = null;
  }

  @Override
  public void enterSrn_destination_translation(Srn_destination_translationContext ctx) {
    _currentNatRuleDestinationTranslation =
        Optional.ofNullable(_currentNatRule.getDestinationTranslation())
            .orElseGet(DestinationTranslation::new);
  }

  @Override
  public void exitSrn_destination_translation(Srn_destination_translationContext ctx) {
    _currentNatRule.setDestinationTranslation(_currentNatRuleDestinationTranslation);
    _currentNatRuleDestinationTranslation = null;
  }

  @Override
  public void exitSrn_disabled(Srn_disabledContext ctx) {
    _currentNatRule.setDisabled(toBoolean(ctx.yn));
  }

  @Override
  public void exitSrn_description(Srn_descriptionContext ctx) {
    _currentNatRule.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSrndt_translated_address(Srndt_translated_addressContext ctx) {
    RuleEndpoint translatedAddress = toRuleEndpoint(ctx.translated_address_list_item());
    _currentNatRuleDestinationTranslation.setTranslatedAddress(translatedAddress);

    // Add reference
    String uniqueName = computeObjectName(_currentVsys, translatedAddress.getValue());
    // At this time, don't know if something that looks like a constant (e.g. IP address) is a
    // reference or not.  So mark a reference to a very permissive abstract structure type.
    PaloAltoStructureType type = ADDRESS_LIKE_OR_NONE;
    if (translatedAddress.getType() == RuleEndpoint.Type.REFERENCE) {
      // We know this reference doesn't look like a valid constant, so it must be pointing to an
      // object/group
      type = ADDRESS_LIKE;
    }
    referenceStructure(type, uniqueName, NAT_RULE_DESTINATION_TRANSLATION, getLine(ctx.start));
  }

  @Override
  public void exitSrndt_translated_port(Srndt_translated_portContext ctx) {
    _currentNatRuleDestinationTranslation.setTranslatedPort(toInteger(ctx.port));
  }

  @Override
  public void exitSrnst_dynamic_ip_and_port(Srnst_dynamic_ip_and_portContext ctx) {
    SourceTranslation sourceTranslation = _currentNatRule.getSourceTranslation();
    if (sourceTranslation == null) {
      sourceTranslation = new SourceTranslation();
      _currentNatRule.setSourceTranslation(sourceTranslation);
    }
    DynamicIpAndPort dynamicIpAndPort = sourceTranslation.getDynamicIpAndPort();
    if (dynamicIpAndPort == null) {
      dynamicIpAndPort = new DynamicIpAndPort();
      sourceTranslation.setDynamicIpAndPort(dynamicIpAndPort);
    }
    for (Translated_address_list_itemContext var :
        ctx.srnst_translated_address().translated_address_list().translated_address_list_item()) {
      RuleEndpoint translatedAddress = toRuleEndpoint(var);
      dynamicIpAndPort.addTranslatedAddress(translatedAddress);

      // Add reference
      String uniqueName = computeObjectName(_currentVsys, translatedAddress.getValue());
      // At this time, don't know if something that looks like a constant (e.g. IP address) is a
      // reference or not.  So mark a reference to a very permissive abstract structure type.
      PaloAltoStructureType type = ADDRESS_LIKE_OR_NONE;
      if (translatedAddress.getType() == RuleEndpoint.Type.REFERENCE) {
        // We know this reference doesn't look like a valid constant, so it must be pointing to an
        // object/group
        type = ADDRESS_LIKE;
      }
      referenceStructure(type, uniqueName, NAT_RULE_SOURCE_TRANSLATION, getLine(var.start));
    }
  }

  @Override
  public void exitSrn_destination(Srn_destinationContext ctx) {
    for (Src_or_dst_list_itemContext var : ctx.src_or_dst_list().src_or_dst_list_item()) {
      RuleEndpoint endpoint = toRuleEndpoint(var);
      _currentNatRule.getDestination().add(endpoint);

      // Use constructed object name so same-named refs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, endpoint.getValue());

      // At this time, don't know if something that looks like a constant (e.g. IP address) is a
      // reference or not.  So mark a reference to a very permissive abstract structure type.
      PaloAltoStructureType type = ADDRESS_LIKE_OR_NONE;
      if (endpoint.getType() == RuleEndpoint.Type.REFERENCE) {
        // We know this reference doesn't look like a valid constant, so it must be pointing to an
        // object/group
        type = ADDRESS_LIKE;
      }
      referenceStructure(type, uniqueName, NAT_RULE_DESTINATION, getLine(var.start));
    }
  }

  @Override
  public void exitSrn_source(Srn_sourceContext ctx) {
    for (Src_or_dst_list_itemContext var : ctx.src_or_dst_list().src_or_dst_list_item()) {
      RuleEndpoint endpoint = toRuleEndpoint(var);
      _currentNatRule.getSource().add(endpoint);
      // Use constructed object name so same-named refs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, endpoint.getValue());

      // At this time, don't know if something that looks like a constant (e.g. IP address) is a
      // reference or not.  So mark a reference to a very permissive abstract structure type.
      PaloAltoStructureType type = ADDRESS_LIKE_OR_NONE;
      if (endpoint.getType() == RuleEndpoint.Type.REFERENCE) {
        type = ADDRESS_LIKE;
      }
      referenceStructure(type, uniqueName, NAT_RULE_SOURCE, getLine(var.start));
    }
  }

  @Override
  public void exitSrn_service(Srn_serviceContext ctx) {
    String serviceName = getText(ctx.service);
    _currentNatRule.setService(new ServiceOrServiceGroupReference(serviceName));
    referenceService(ctx.service, NAT_RULE_SERVICE);
  }

  @Override
  public void exitSrn_from(Srn_fromContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String zoneName = getText(var);
      _currentNatRule.getFrom().add(zoneName);

      if (!zoneName.equals(CATCHALL_ZONE_NAME)) {
        // Use constructed object name so same-named refs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys, zoneName);
        referenceStructure(ZONE, uniqueName, NAT_RULE_FROM_ZONE, getLine(var.start));
      }
    }
  }

  @Override
  public void exitSrn_to(Srn_toContext ctx) {
    String zoneName = getText(ctx.zone);
    _currentNatRule.setTo(zoneName);

    if (!zoneName.equals(CATCHALL_ZONE_NAME)) {
      // Use constructed object name so same-named refs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, zoneName);
      referenceStructure(ZONE, uniqueName, NAT_RULE_TO_ZONE, getLine(ctx.zone.start));
    }
  }

  @Override
  public void enterSrs_definition(Srs_definitionContext ctx) {
    String name = getText(ctx.name);
    Rulebase rulebase = getRulebase();
    _currentSecurityRule =
        rulebase.getSecurityRules().computeIfAbsent(name, n -> new SecurityRule(n, _currentVsys));

    // Use constructed name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, name);
    defineFlattenedStructure(SECURITY_RULE, uniqueName, ctx);
    referenceStructure(SECURITY_RULE, uniqueName, SECURITY_RULE_SELF_REF, getLine(ctx.name.start));
  }

  @Override
  public void exitSrs_category(PaloAltoParser.Srs_categoryContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentSecurityRule.addCategory(name);
      if (!name.equals(CATCHALL_CATEGORY_NAME)) {
        // Use constructed object name so same-named refs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys, name);
        referenceStructure(
            CUSTOM_URL_CATEGORY, uniqueName, SECURITY_RULE_CATEGORY, getLine(var.start));
      }
    }
  }

  @Override
  public void exitSrs_definition(Srs_definitionContext ctx) {
    _currentSecurityRule = null;
  }

  @Override
  public void exitSrs_action(Srs_actionContext ctx) {
    if (ctx.ALLOW() != null) {
      _currentSecurityRule.setAction(LineAction.PERMIT);
    } else {
      _currentSecurityRule.setAction(LineAction.DENY);
    }
  }

  @Override
  public void exitSrs_rule_type(Srs_rule_typeContext ctx) {
    if (ctx.INTERZONE() != null) {
      _currentSecurityRule.setRuleType(RuleType.INTERZONE);
    } else if (ctx.INTRAZONE() != null) {
      if (_currentSecurityRule.getFrom().equals(_currentSecurityRule.getTo())) {
        _currentSecurityRule.setRuleType(RuleType.INTRAZONE);
      } else {
        warn(
            ctx,
            "Error: Cannot set 'rule-type intrazone' for security rule with different source and"
                + " destination zones.");
      }
    } else if (ctx.UNIVERSAL() != null) {
      _currentSecurityRule.setRuleType(RuleType.UNIVERSAL);
    } else {
      warn(ctx, "Unsupported security rule-type");
    }
  }

  @Override
  public void exitSrs_group_tag(PaloAltoParser.Srs_group_tagContext ctx) {
    String groupTag = getText((ParserRuleContext) ctx.getChild(1));
    _currentSecurityRule.setGroupTag(groupTag);
  }

  private void referenceApplicationOrGroupLike(
      String name, String uniqueName, PaloAltoStructureUsage usage, ParserRuleContext var) {
    PaloAltoStructureType type =
        name.equals(CATCHALL_APPLICATION_NAME) || isBuiltInApp(name)
            /*
             * Since the name matches a builtin, we'll add a reference if the user defined
             * over the builtin, but it's okay if they did not.
             */
            ? APPLICATION_GROUP_OR_APPLICATION_OR_NONE
            /* This is not a pre-defined name, the application must be defined in config. */
            : APPLICATION_GROUP_OR_APPLICATION;
    referenceStructure(type, uniqueName, usage, getLine(var.start));
  }

  private void referenceApplicationLike(
      String name, String uniqueName, PaloAltoStructureUsage usage, ParserRuleContext var) {
    PaloAltoStructureType type =
        name.equals(CATCHALL_APPLICATION_NAME) || isBuiltInApp(name)
            /*
             * Since the name matches a builtin, we'll add a reference if the user defined
             * over the builtin, but it's okay if they did not.
             */
            ? APPLICATION_OR_NONE
            /* This is not a pre-defined name, the application must be defined in config. */
            : APPLICATION;
    referenceStructure(type, uniqueName, usage, getLine(var.start));
  }

  @Override
  public void exitSrs_application(Srs_applicationContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentSecurityRule.addApplication(name);
      // Use constructed object name so same-named refs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, name);
      referenceApplicationOrGroupLike(name, uniqueName, SECURITY_RULE_APPLICATION, var);
    }
  }

  @Override
  public void exitSrs_description(Srs_descriptionContext ctx) {
    _currentSecurityRule.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSrs_destination(Srs_destinationContext ctx) {
    for (Src_or_dst_list_itemContext var : ctx.src_or_dst_list().src_or_dst_list_item()) {
      RuleEndpoint endpoint = toRuleEndpoint(var);
      _currentSecurityRule.addDestination(endpoint);

      // Use constructed object name so same-named refs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, endpoint.getValue());

      // At this time, don't know if something that looks like a constant (e.g. IP address) is a
      // reference or not.  So mark a reference to a very permissive abstract structure type.
      PaloAltoStructureType type = ADDRESS_LIKE_OR_NONE;
      if (endpoint.getType() == RuleEndpoint.Type.REFERENCE) {
        // We know this reference doesn't look like a valid constant, so it must be pointing to an
        // object/group
        type = ADDRESS_LIKE;
      }
      referenceStructure(type, uniqueName, SECURITY_RULE_DESTINATION, getLine(var.start));
    }
  }

  @Override
  public void exitSrs_destination_hip(Srs_destination_hipContext ctx) {
    if (ctx.any != null) {
      return;
    }
    for (Variable_list_itemContext var : variables(ctx.names)) {
      _currentSecurityRule.addDestinationHip(getText(var));
    }
  }

  @Override
  public void exitSrs_disabled(Srs_disabledContext ctx) {
    _currentSecurityRule.setDisabled(toBoolean(ctx.yn));
  }

  @Override
  public void exitSrs_from(Srs_fromContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String zoneName = getText(var);
      _currentSecurityRule.getFrom().add(zoneName);

      if (!zoneName.equals(CATCHALL_ZONE_NAME)) {
        // Use constructed object name so same-named refs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys, zoneName);
        referenceStructure(ZONE, uniqueName, SECURITY_RULE_FROM_ZONE, getLine(var.start));
      }
    }
  }

  @Override
  public void exitSrs_hip_profiles(Srs_hip_profilesContext ctx) {
    if (ctx.any != null) {
      return;
    }
    for (Variable_list_itemContext var : variables(ctx.names)) {
      _currentSecurityRule.addHipProfile(getText(var));
    }
  }

  @Override
  public void exitSrs_negate_destination(Srs_negate_destinationContext ctx) {
    _currentSecurityRule.setNegateDestination(toBoolean(ctx.yn));
  }

  @Override
  public void exitSrs_negate_source(Srs_negate_sourceContext ctx) {
    _currentSecurityRule.setNegateSource(toBoolean(ctx.yn));
  }

  @Override
  public void exitSrs_service(Srs_serviceContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String serviceName = getText(var);
      _currentSecurityRule.getService().add(new ServiceOrServiceGroupReference(serviceName));
      referenceService(var, SECURITY_RULE_SERVICE);
    }
  }

  @Override
  public void exitSrs_source(Srs_sourceContext ctx) {
    for (Src_or_dst_list_itemContext var : ctx.src_or_dst_list().src_or_dst_list_item()) {
      RuleEndpoint endpoint = toRuleEndpoint(var);
      _currentSecurityRule.addSource(endpoint);
      // Use constructed object name so same-named refs across vsys are unique
      String uniqueName = computeObjectName(_currentVsys, endpoint.getValue());

      // At this time, don't know if something that looks like a constant (e.g. IP address) is a
      // reference or not.  So mark a reference to a very permissive abstract structure type.
      PaloAltoStructureType type = ADDRESS_LIKE_OR_NONE;
      if (endpoint.getType() == RuleEndpoint.Type.REFERENCE) {
        type = ADDRESS_LIKE;
      }
      referenceStructure(type, uniqueName, SECURITY_RULE_SOURCE, getLine(var.start));
    }
  }

  @Override
  public void exitSrs_source_hip(Srs_source_hipContext ctx) {
    if (ctx.any != null) {
      return;
    }
    for (Variable_list_itemContext var : variables(ctx.names)) {
      _currentSecurityRule.addSourceHip(getText(var));
    }
  }

  @Override
  public void exitSrs_source_user(Srs_source_userContext ctx) {
    if (ctx.any != null) {
      return;
    }
    if (ctx.names != null) {
      for (Variable_list_itemContext var : variables(ctx.names)) {
        _currentSecurityRule.addSourceUser(getText(var));
      }
    }
  }

  @Override
  public void exitSrs_tag(Srs_tagContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String tag = getText(var);
      _currentSecurityRule.getTags().add(tag);
    }
  }

  @Override
  public void exitSrs_to(Srs_toContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String zoneName = getText(var);
      _currentSecurityRule.getTo().add(zoneName);

      if (!zoneName.equals(CATCHALL_ZONE_NAME)) {
        // Use constructed object name so same-named refs across vsys are unique
        String uniqueName = computeObjectName(_currentVsys, zoneName);
        referenceStructure(ZONE, uniqueName, SECURITY_RULE_TO_ZONE, getLine(var.start));
      }
    }
  }

  @Override
  public void enterS_rulebase(S_rulebaseContext ctx) {
    _currentRuleScope = RulebaseId.DEFAULT;
  }

  @Override
  public void exitS_rulebase(S_rulebaseContext ctx) {
    _currentRuleScope = null;
  }

  @Override
  public void exitS_profiles(S_profilesContext ctx) {
    // Silently consume security profile settings
  }

  @Override
  public void enterS_service_definition(S_service_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentService = _currentVsys.getServices().computeIfAbsent(name, Service::new);

    // Use constructed service name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, name);
    defineFlattenedStructure(PaloAltoStructureType.SERVICE, uniqueName, ctx);
  }

  @Override
  public void exitS_service_definition(S_service_definitionContext ctx) {
    _currentService = null;
  }

  @Override
  public void exitSserv_description(Sserv_descriptionContext ctx) {
    _currentService.setDescription(getText(ctx.description));
  }

  @Override
  public void exitSserv_port(Sserv_portContext ctx) {
    for (Port_or_rangeContext item : ctx.variable_port_list().port_or_range()) {
      if (item.port != null) {
        _currentService.addPort(toInteger(item.port));
      } else {
        assert item.range != null;
        _currentService.addPorts(new SubRange(getText(item.range)));
      }
    }
  }

  @Override
  public void exitSserv_protocol(Sserv_protocolContext ctx) {
    if (ctx.SCTP() != null) {
      _currentService.setProtocol(IpProtocol.SCTP);
    } else if (ctx.TCP() != null) {
      _currentService.setProtocol(IpProtocol.TCP);
    } else if (ctx.UDP() != null) {
      _currentService.setProtocol(IpProtocol.UDP);
    }
  }

  @Override
  public void exitSserv_source_port(Sserv_source_portContext ctx) {
    for (Port_or_rangeContext item : ctx.variable_port_list().port_or_range()) {
      if (item.port != null) {
        _currentService.addSourcePort(toInteger(item.port));
      } else {
        assert item.range != null;
        _currentService.addSourcePorts(new SubRange(getText(item.range)));
      }
    }
  }

  @Override
  public void enterSserv_tag(Sserv_tagContext ctx) {
    for (Variable_list_itemContext tag : variables(ctx.tags)) {
      _currentService.addTag(getText(tag));
    }
  }

  @Override
  public void enterS_service_group_definition(S_service_group_definitionContext ctx) {
    String name = getText(ctx.name);
    _currentServiceGroup = _currentVsys.getServiceGroups().computeIfAbsent(name, ServiceGroup::new);

    // Use constructed service-group name so same-named defs across vsys are unique
    String uniqueName = computeObjectName(_currentVsys, name);
    defineFlattenedStructure(SERVICE_GROUP, uniqueName, ctx);
  }

  @Override
  public void exitS_service_group_definition(S_service_group_definitionContext ctx) {
    _currentServiceGroup = null;
  }

  @Override
  public void exitSservgrp_members(Sservgrp_membersContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentServiceGroup.getReferences().add(new ServiceOrServiceGroupReference(name));
      referenceService(var, SERVICE_GROUP_MEMBER);
    }
  }

  @Override
  public void enterS_shared(S_sharedContext ctx) {
    _currentVsys = _currentConfiguration.getShared();
    if (_currentVsys == null) {
      _currentVsys = new Vsys(SHARED_VSYS_NAME, NamespaceType.SHARED);
      _currentConfiguration.setShared(_currentVsys);
    }
  }

  @Override
  public void exitS_shared(S_sharedContext ctx) {
    _currentVsys = _defaultVsys;
  }

  @Override
  public void enterSl_syslog(Sl_syslogContext ctx) {
    _currentSyslogServerGroupName = getText(ctx.name);
  }

  @Override
  public void exitSl_syslog(Sl_syslogContext ctx) {
    _currentSyslogServerGroupName = null;
  }

  @Override
  public void enterSls_server(Sls_serverContext ctx) {
    if (_currentSyslogServerGroupName == null) {
      return;
    }
    _currentSyslogServer =
        _currentVsys.getSyslogServer(_currentSyslogServerGroupName, getText(ctx.name));
  }

  @Override
  public void exitSls_server(Sls_serverContext ctx) {
    _currentSyslogServer = null;
  }

  @Override
  public void exitSlss_server(Slss_serverContext ctx) {
    if (_currentSyslogServer == null) {
      return;
    }
    _currentSyslogServer.setAddress(getText(ctx.address));
  }

  @Override
  public void exitSlss_certificate_profile(Slss_certificate_profileContext ctx) {
    // Silently consume certificate-profile - not used by Batfish
  }

  @Override
  public void exitSlss_format(Slss_formatContext ctx) {
    // Silently consume format settings - not used by Batfish
  }

  @Override
  public void exitSlss_from(Slss_fromContext ctx) {
    // Silently consume email from settings - not used by Batfish
  }

  @Override
  public void exitSlss_to(Slss_toContext ctx) {
    // Silently consume email to settings - not used by Batfish
  }

  @Override
  public void exitSlss_gateway(Slss_gatewayContext ctx) {
    // Silently consume gateway settings - not used by Batfish
  }

  @Override
  public void exitSlss_and_also_to(Slss_and_also_toContext ctx) {
    // Silently consume and-also-to settings - not used by Batfish
  }

  @Override
  public void exitSlss_transport(Slss_transportContext ctx) {
    // Silently consume transport settings - not used by Batfish
  }

  @Override
  public void exitSlss_facility(Slss_facilityContext ctx) {
    // Silently consume facility settings - not used by Batfish
  }

  @Override
  public void exitSlss_port(Slss_portContext ctx) {
    // Silently consume port settings - not used by Batfish
  }

  @Override
  public void exitSlss_community(Slss_communityContext ctx) {
    // Silently consume SNMP community settings - not used by Batfish
  }

  @Override
  public void exitSlss_manager(Slss_managerContext ctx) {
    // Silently consume SNMP manager settings - not used by Batfish
  }

  @Override
  public void exitSlss_version(Slss_versionContext ctx) {
    // Silently consume version settings - not used by Batfish
  }

  @Override
  public void enterS_vsys_definition(S_vsys_definitionContext ctx) {
    _currentVsys =
        _currentConfiguration.getVirtualSystems().computeIfAbsent(getText(ctx.name), Vsys::new);
  }

  @Override
  public void exitS_vsys_definition(S_vsys_definitionContext ctx) {
    _currentVsys = _defaultVsys;
  }

  @Override
  public void exitSvi_visible_vsys(Svi_visible_vsysContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentVsys.getImportedVsyses().add(name);
    }
  }

  @Override
  public void exitSvin_interface(Svin_interfaceContext ctx) {
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentVsys.getImportedInterfaces().add(name);
      referenceStructure(INTERFACE, name, VSYS_IMPORT_INTERFACE, getLine(var.start));
    }
  }

  @Override
  public void exitSzn_external(Szn_externalContext ctx) {
    _currentZone.setType(EXTERNAL);
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentZone.getExternalNames().add(name);
    }
  }

  @Override
  public void exitSzn_layer2(Szn_layer2Context ctx) {
    _currentZone.setType(LAYER2);
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentZone.getInterfaceNames().add(name);
      referenceStructure(INTERFACE, name, ZONE_INTERFACE, getLine(var.start));
      // Mark reference to zone when it has an interface in it (since the zone if effectively used
      // at this point)
      // Use constructed object name so same-named refs across vsys are unique
      String zoneName = computeObjectName(_currentVsys, _currentZone.getName());
      referenceStructure(ZONE, zoneName, LAYER2_INTERFACE_ZONE, getLine(var.start));
    }
  }

  @Override
  public void exitSzn_layer3(Szn_layer3Context ctx) {
    _currentZone.setType(LAYER3);
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentZone.getInterfaceNames().add(name);
      referenceStructure(INTERFACE, name, ZONE_INTERFACE, getLine(var.start));
      // Mark reference to zone when it has an interface in it (since the zone if effectively used
      // at this point)
      // Use constructed object name so same-named refs across vsys are unique
      String zoneName = computeObjectName(_currentVsys, _currentZone.getName());
      referenceStructure(ZONE, zoneName, LAYER3_INTERFACE_ZONE, getLine(var.start));
    }
  }

  @Override
  public void exitSzn_tap(Szn_tapContext ctx) {
    _currentZone.setType(TAP);
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentZone.getInterfaceNames().add(name);
      referenceStructure(INTERFACE, name, ZONE_INTERFACE, getLine(var.start));
      // Mark reference to zone when it has an interface in it (since the zone if effectively used
      // at this point)
      // Use constructed object name so same-named refs across vsys are unique
      String zoneName = computeObjectName(_currentVsys, _currentZone.getName());
      referenceStructure(ZONE, zoneName, TAP_INTERFACE_ZONE, getLine(var.start));
    }
  }

  @Override
  public void exitSzn_virtual_wire(Szn_virtual_wireContext ctx) {
    _currentZone.setType(VIRTUAL_WIRE);
    for (Variable_list_itemContext var : variables(ctx.variable_list())) {
      String name = getText(var);
      _currentZone.getInterfaceNames().add(name);
      referenceStructure(INTERFACE, name, ZONE_INTERFACE, getLine(var.start));
      // Mark reference to zone when it has an interface in it (since the zone if effectively used
      // at this point)
      // Use constructed object name so same-named refs across vsys are unique
      String zoneName = computeObjectName(_currentVsys, _currentZone.getName());
      referenceStructure(ZONE, zoneName, VIRTUAL_WIRE_INTERFACE_ZONE, getLine(var.start));
    }
  }

  public PaloAltoConfiguration getConfiguration() {
    return _mainConfiguration;
  }

  /** Get or create Panorama vsys for current configuration. */
  private Vsys getOrCreatePanoramaVsys() {
    Vsys panorama = _currentConfiguration.getPanorama();
    if (panorama == null) {
      panorama = new Vsys(PANORAMA_VSYS_NAME, NamespaceType.PANORAMA);
      _currentConfiguration.setPanorama(panorama);
    }
    return panorama;
  }

  private Vsys getOrCreateDefaultVsys(PaloAltoConfiguration config) {
    return config.getVirtualSystems().computeIfAbsent(DEFAULT_VSYS_NAME, Vsys::new);
  }

  private static ApplicationOverrideRule.Protocol toApplicationOverrideProtocol(
      Tcp_or_udpContext ctx) {
    if (ctx.TCP() != null) {
      return Protocol.TCP;
    }
    assert ctx.UDP() != null;
    return Protocol.UDP;
  }

  private static boolean toBoolean(Yes_or_noContext ctx) {
    if (ctx.YES() != null) {
      return true;
    }
    assert ctx.NO() != null;
    return false;
  }

  private static @Nonnull ConcreteInterfaceAddress toConcreteInterfaceAddress(
      Interface_addressContext ctx) {
    if (ctx.addr != null) {
      // PAN allows implicit /32 in lots of places.
      return ConcreteInterfaceAddress.create(toIp(ctx.addr), Prefix.MAX_PREFIX_LENGTH);
    }
    return ConcreteInterfaceAddress.parse(ctx.getText());
  }

  private static @Nonnull Ip toIp(Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private static @Nonnull IpPrefix toIpPrefix(Ip_prefixContext ctx) {
    return IpPrefix.parse(ctx.getText());
  }

  private static @Nonnull Prefix toPrefix(Ip_prefixContext ctx) {
    return Prefix.parse(ctx.getText());
  }

  private @Nonnull Optional<Ip> toIp(
      ParserRuleContext ctx, Ip_address_or_slash32Context addr, String ipType) {
    ConcreteInterfaceAddress ip = toConcreteInterfaceAddress(addr.addr);
    if (ip.getNetworkBits() != Prefix.MAX_PREFIX_LENGTH) {
      warn(ctx, addr, String.format("Expecting 32-bit mask for %s, ignoring", ipType));
    }
    return Optional.of(ip.getIp());
  }

  private static @Nonnull String stripZoneIndex(String ipStr) {
    int percentIdx = ipStr.indexOf('%');
    return percentIdx != -1 ? ipStr.substring(0, percentIdx) : ipStr;
  }

  private @Nonnull Optional<Range<Ip>> toIpRange(Ip_rangeContext ctx) {
    String[] ips = getText(ctx).split("-");
    Ip lowIp = Ip.parse(ips[0]);
    Ip highIp = Ip.parse(ips[1]);
    if (lowIp.compareTo(highIp) >= 0) {
      warn(ctx, "Invalid IP address range");
      return Optional.empty();
    }
    return Optional.of(Range.closed(lowIp, highIp));
  }

  /////////////////////////////////////////
  ///// Range-aware type conversions. /////
  /////////////////////////////////////////

  private int toInteger(Ospf_interface_metricContext ctx) {
    return toInteger(ctx.uint16());
  }

  private int toInteger(Port_numberContext ctx) {
    return toInteger(ctx.uint16());
  }

  private int toInteger(Uint8Context t) {
    return Integer.parseInt(getText(t));
  }

  private int toInteger(Uint16Context t) {
    return Integer.parseInt(getText(t));
  }

  private static final IntegerSpace OSPF_AREA_DEFAULT_ROUTE_ADVERTISE_METRIC =
      IntegerSpace.of(Range.closed(1, 255));

  private static final IntegerSpace PORT_NUMBER_SPACE = IntegerSpace.of(Range.closed(0, 65535));

  private static final IntegerSpace PROTOCOL_ADMIN_DISTANCE_SPACE =
      IntegerSpace.of(Range.closed(10, 240));

  private Optional<Integer> toInteger(ParserRuleContext ctx, Ospf_area_metricContext metric) {
    return toIntegerInSpace(
        ctx,
        metric,
        OSPF_AREA_DEFAULT_ROUTE_ADVERTISE_METRIC,
        "ospf area advertise default metric");
  }

  private Optional<Integer> toInteger(ParserRuleContext ctx, Port_numberContext pn) {
    return toIntegerInSpace(ctx, pn, PORT_NUMBER_SPACE, "admin-dist");
  }

  private Optional<Integer> toInteger(ParserRuleContext ctx, Protocol_adContext ad) {
    return toIntegerInSpace(ctx, ad, PROTOCOL_ADMIN_DISTANCE_SPACE, "admin-dist");
  }

  private static final IntegerSpace VLAN_TAG_SPACE = IntegerSpace.of(Range.closed(1, 4094));

  private Optional<Integer> toInteger(ParserRuleContext ctx, Vlan_tagContext vlan) {
    return toIntegerInSpace(ctx, vlan, VLAN_TAG_SPACE, "vlan/tag");
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
      _w.addWarning(
          messageCtx,
          getFullText(messageCtx),
          _parser,
          String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private static final LongSpace BGP_2_BYTE_ASN_SPACE = LongSpace.of(Range.closed(1L, 65535L));
  private static final LongSpace BGP_4_BYTE_ASN_SPACE = LongSpace.of(Range.closed(1L, 4294967295L));

  private Optional<Long> toAsn(ParserRuleContext ctx, Bgp_asnContext asn, AsFormat fmt) {
    if (fmt == AsFormat.TWO_BYTE_AS) {
      return toLongInSpace(ctx, asn, BGP_2_BYTE_ASN_SPACE, "2-byte BGP AS number");
    } else {
      assert fmt == AsFormat.FOUR_BYTE_AS;
      return toLongInSpace(ctx, asn, BGP_4_BYTE_ASN_SPACE, "4-byte BGP AS number");
    }
  }

  private long toLong(Uint32Context t) {
    return Long.parseLong(getText(t));
  }

  private static long toLong(Bgp_local_prefContext ctx) {
    return Long.parseLong(ctx.getText());
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
      warn(
          messageCtx,
          ctx,
          String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private static final Pattern TEMPLATE_VARIABLE_NAME_PATTERN =
      Pattern.compile("^\\$[A-Za-z0-9._-]{1,62}$");

  private Optional<String> toString(
      ParserRuleContext messageCtx, PaloAltoParser.Variable_nameContext ctx) {
    return toString(
        messageCtx, ctx.variable(), "template variable name", TEMPLATE_VARIABLE_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, ParserRuleContext ctx, String type, Pattern pattern) {
    return toString(messageCtx, ctx, type, s -> pattern.matcher(s).matches());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx,
      ParserRuleContext ctx,
      String type,
      Predicate<String> predicate) {
    String text = getText(ctx);
    if (!predicate.test(text)) {
      warn(messageCtx, String.format("Illegal value for %s", type));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  private static final IntegerSpace BGP_PEER_GROUP_NAME_LENGTH_SPACE =
      IntegerSpace.of(Range.closed(1, 31));

  private @Nonnull Optional<String> toString(ParserRuleContext ctx, Bgp_peer_group_nameContext pg) {
    return toStringWithLengthInSpace(ctx, pg, BGP_PEER_GROUP_NAME_LENGTH_SPACE, "bgp peer-group");
  }

  private static final IntegerSpace BGP_PEER_NAME_LENGTH_SPACE =
      IntegerSpace.of(Range.closed(1, 31));

  private @Nonnull Optional<String> toString(ParserRuleContext ctx, Bgp_peer_nameContext peer) {
    return toStringWithLengthInSpace(ctx, peer, BGP_PEER_NAME_LENGTH_SPACE, "bgp peer");
  }

  private static final IntegerSpace CUSTOM_URL_CATEGORY_NAME_LENGTH_SPACE =
      IntegerSpace.of(Range.closed(1, 31));

  private @Nonnull Optional<String> toString(
      ParserRuleContext ctx, PaloAltoParser.Custom_url_category_nameContext name) {
    return toStringWithLengthInSpace(
        ctx, name, CUSTOM_URL_CATEGORY_NAME_LENGTH_SPACE, "custom-url-category");
  }

  /**
   * Return the text of the provided {@code ctx} if its length is within the provided {@link
   * IntegerSpace lengthSpace}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<String> toStringWithLengthInSpace(
      ParserRuleContext messageCtx, ParserRuleContext ctx, IntegerSpace lengthSpace, String name) {
    String text = ctx.getText();
    if (!lengthSpace.contains(text.length())) {
      _w.addWarning(
          messageCtx,
          getFullText(messageCtx),
          _parser,
          String.format(
              "Expected %s with length in range %s, but got '%s'", name, lengthSpace, text));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    String lineText = getText(errorNode).replace("\n", "").replace("\r", "").trim();
    int line = getLine(token);
    _mainConfiguration.setUnrecognized(true);

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

  private void fileWarningApplicationStatementIgnored(ParserRuleContext ctx) {
    if (!_filedWarningApplicationStatementIgnored) {
      _filedWarningApplicationStatementIgnored = true;
      warn(
          ctx,
          "Application definitions only affect App-ID, so Batfish ignores custom application"
              + " definitions.");
    }
  }

  private void warn(ParserRuleContext ctx, ParserRuleContext warnCtx, String message) {
    warn(ctx, warnCtx.start, message);
  }

  private void warn(ParserRuleContext ctx, Token warnToken, String message) {
    _w.addWarningOnLine(_parser.getLine(warnToken), ctx, getFullText(ctx), _parser, message);
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }
}
