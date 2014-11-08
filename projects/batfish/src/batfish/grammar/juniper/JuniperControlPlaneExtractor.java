package batfish.grammar.juniper;

import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.ControlPlaneExtractor;
import batfish.grammar.juniper.JuniperGrammarParser.Accept_then_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Accept_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Accounting_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Action_filter_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Address_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Aggregate_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Aggregated_ether_options_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Anon_term_ps_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Aop_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Apply_groups_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Apply_groups_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Apply_groups_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Area_op_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Arp_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.As_idContext;
import batfish.grammar.juniper.JuniperGrammarParser.As_path_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.As_path_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.As_path_prepend_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Authentication_order_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Autonomous_system_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Backup_router_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Bfd_liveness_detection_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Bfd_liveness_detection_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Bfd_liveness_detection_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Bfd_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Bg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Bg_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Bgp_family_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Bgp_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Bridge_domains_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Chassis_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Class_of_service_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Cluster_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Community_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Community_literalContext;
import batfish.grammar.juniper.JuniperGrammarParser.Community_literal_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Community_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Community_static_opts_sro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Community_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Connections_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Count_then_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Defaults_sro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Description_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Description_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Description_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Description_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Destination_address_from_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Destination_port_from_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Disable_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Disable_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Discard_then_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Domain_name_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Domain_search_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Double_numContext;
import batfish.grammar.juniper.JuniperGrammarParser.Dump_on_panic_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Empty_neighbor_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Enable_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Enable_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Encapsulation_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Encapsulation_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Encapsulation_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Export_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Export_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Export_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Export_op_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Fam_u_if_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Family_bg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Family_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Family_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Family_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Family_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Filter_f_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Filter_f_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Filter_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Firewall_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Flexible_vlan_tagging_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Forwarding_options_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Forwarding_table_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Framing_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.From_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.From_t_ff_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.From_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.From_t_ps_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Gbg_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Gigether_options_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Gr_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Graceful_restart_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Group_bg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Group_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Groups_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Hold_time_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Host_name_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Icmp_type_from_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.If_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.If_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Igmp_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Igmp_snooping_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Ignored_substanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Import_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Import_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Import_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Import_op_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_bg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_interface_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_sro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_term_ps_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_to_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Inactive_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Include_mp_next_hop_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Input_vlan_map_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Install_next_hop_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Instance_to_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Integer_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Interface_aop_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Interface_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Interface_mode_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Interface_routes_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Interface_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Interfaces_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Isis_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.J_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.J_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Juniper_configurationContext;
import batfish.grammar.juniper.JuniperGrammarParser.L2_circuit_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Ldp_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.License_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Link_mode_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Lldp_med_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Lldp_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Load_balance_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Local_address_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Local_address_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Local_address_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Local_as_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Local_preference_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Local_preference_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Location_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Log_then_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Log_updown_bg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Log_updown_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Log_updown_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Login_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Mac_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Martians_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Match_type_filter_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Max_configuration_rollbacks_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Max_configurations_on_flash_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Metric_out_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Metric_out_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Metric_out_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Metric_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Mld_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Mpls_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Msdp_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Mtu_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Mtu_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Mtu_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Multicast_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Multihop_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Multihop_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Multihop_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Multipath_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Multipath_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Name_server_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Native_vlan_id_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Neighbor_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Neighbor_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Network_summary_export_aop_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Next_hop_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Next_policy_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Next_term_then_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Next_term_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.No_neighbor_learn_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.No_redirects_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Not_braceContext;
import batfish.grammar.juniper.JuniperGrammarParser.Nssa_aop_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Ntp_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_aop_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_bg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_op_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_then_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Null_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Op_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Origin_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Ospf3_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Ospf_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Output_vlan_map_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.P_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Passive_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Peer_as_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Peer_as_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Peer_as_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Pim_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Po_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Policer_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Policy_options_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Policy_statement_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.PortContext;
import batfish.grammar.juniper.JuniperGrammarParser.Port_mode_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Ports_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Prefix_list_filter_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Prefix_list_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Prefix_list_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Primary_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.ProtocolContext;
import batfish.grammar.juniper.JuniperGrammarParser.Protocol_from_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Protocol_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Protocol_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Protocols_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Radius_options_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Radius_server_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Reference_bandwidth_op_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Reject_then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Remove_private_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Remove_private_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Remove_private_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Removed_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Removed_top_level_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Rib_common_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Rib_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Rib_group_sro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Rib_groups_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Rib_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Rib_to_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Root_authentication_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Route_filter_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Route_sro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Router_advertisement_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Router_id_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Routing_instances_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Routing_options_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Rpf_check_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Rstp_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Rsvp_p_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Sample_then_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Security_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Services_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Services_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Source_address_filter_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Source_address_from_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Source_port_from_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Sro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Static_opts_sro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Static_ro_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.String_in_double_quotesContext;
import batfish.grammar.juniper.JuniperGrammarParser.String_up_to_semicolonContext;
import batfish.grammar.juniper.JuniperGrammarParser.SubstanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Subterm_ps_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Syslog_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.System_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Tacplus_server_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Tag_from_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Targeted_broadcast_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Tcp_mss_ngbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Term_f_f_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Term_ps_po_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Then_t_ff_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Then_t_ff_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Then_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Then_t_ps_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Time_zone_sys_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.To_t_ps_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.To_t_ps_stanza_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Traceoptions_bg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Traceoptions_op_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Traps_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Tunnel_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Type_gbg_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.U_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Unit_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Variable_listContext;
import batfish.grammar.juniper.JuniperGrammarParser.Version_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Vlan_id_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Vlan_members_fam_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Vlan_tagging_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Vlan_tags_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Vlanid_u_if_stanzaContext;
import batfish.grammar.juniper.JuniperGrammarParser.Vstp_p_stanzaContext;
import batfish.representation.VendorConfiguration;

public class JuniperControlPlaneExtractor extends
      JuniperGrammarParserBaseListener implements ControlPlaneExtractor {

   public JuniperControlPlaneExtractor(String fileText,
         BatfishCombinedParser<?, ?> combinedParser,
         Set<String> rulesWithSuppressedWarnings) {
      // TODO Auto-generated constructor stub
   }

   @Override
   public void enterEveryRule(ParserRuleContext arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitEveryRule(ParserRuleContext arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void visitErrorNode(ErrorNode arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void visitTerminal(TerminalNode arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public VendorConfiguration getVendorConfiguration() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<String> getWarnings() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void enterDescription_if_stanza(Description_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDescription_if_stanza(Description_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInterface_mode_fam_u_if_stanza(
         Interface_mode_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInterface_mode_fam_u_if_stanza(
         Interface_mode_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRemove_private_ngbg_stanza(
         Remove_private_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRemove_private_ngbg_stanza(
         Remove_private_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterJ_stanza(J_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitJ_stanza(J_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMtu_if_stanza(Mtu_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMtu_if_stanza(Mtu_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_fam_u_if_stanza(Null_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_fam_u_if_stanza(Null_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDump_on_panic_sys_stanza(Dump_on_panic_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDump_on_panic_sys_stanza(Dump_on_panic_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTargeted_broadcast_fam_u_if_stanza(
         Targeted_broadcast_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTargeted_broadcast_fam_u_if_stanza(
         Targeted_broadcast_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_sro_stanza(Inactive_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_sro_stanza(Inactive_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInstall_next_hop_then_t_ps_stanza(
         Install_next_hop_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInstall_next_hop_then_t_ps_stanza(
         Install_next_hop_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_if_stanza(Inactive_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_if_stanza(Inactive_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterReject_then_t_ps_stanza(Reject_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitReject_then_t_ps_stanza(Reject_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDescription_ngbg_stanza(Description_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDescription_ngbg_stanza(Description_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSys_stanza(Sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSys_stanza(Sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAs_path_prepend_then_t_ps_stanza(
         As_path_prepend_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAs_path_prepend_then_t_ps_stanza(
         As_path_prepend_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPim_p_stanza(Pim_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPim_p_stanza(Pim_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterGroups_stanza(Groups_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitGroups_stanza(Groups_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTerm_ps_po_stanza(Term_ps_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTerm_ps_po_stanza(Term_ps_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNext_hop_then_t_ps_stanza(
         Next_hop_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNext_hop_then_t_ps_stanza(
         Next_hop_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRpf_check_fam_u_if_stanza(
         Rpf_check_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRpf_check_fam_u_if_stanza(
         Rpf_check_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterForwarding_options_stanza(
         Forwarding_options_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitForwarding_options_stanza(
         Forwarding_options_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPassive_ngbg_stanza(Passive_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPassive_ngbg_stanza(Passive_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRouting_options_stanza(Routing_options_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRouting_options_stanza(Routing_options_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_sys_stanza(Null_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_sys_stanza(Null_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterCommunity_static_opts_sro_stanza(
         Community_static_opts_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitCommunity_static_opts_sro_stanza(
         Community_static_opts_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterOspf3_p_stanza(Ospf3_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitOspf3_p_stanza(Ospf3_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterArea_op_stanza(Area_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitArea_op_stanza(Area_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPolicy_statement_po_stanza(
         Policy_statement_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPolicy_statement_po_stanza(
         Policy_statement_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFrom_t_ps_stanza_list(From_t_ps_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFrom_t_ps_stanza_list(From_t_ps_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInteger_list(Integer_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInteger_list(Integer_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterArp_sys_stanza(Arp_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitArp_sys_stanza(Arp_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterThen_t_ff_stanza(Then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitThen_t_ff_stanza(Then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFilter_f_stanza_list(Filter_f_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFilter_f_stanza_list(Filter_f_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRemoved_top_level_stanza(Removed_top_level_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRemoved_top_level_stanza(Removed_top_level_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAccept_then_t_ff_stanza(Accept_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAccept_then_t_ff_stanza(Accept_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterCount_then_t_ff_stanza(Count_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitCount_then_t_ff_stanza(Count_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_po_stanza(Inactive_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_po_stanza(Inactive_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterCommunity_from_t_ps_stanza(
         Community_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitCommunity_from_t_ps_stanza(
         Community_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIgmp_p_stanza(Igmp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIgmp_p_stanza(Igmp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSecurity_stanza(Security_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSecurity_stanza(Security_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterReference_bandwidth_op_stanza(
         Reference_bandwidth_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitReference_bandwidth_op_stanza(
         Reference_bandwidth_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_p_stanza(Null_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_p_stanza(Null_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBg_stanza_list(Bg_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBg_stanza_list(Bg_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTcp_mss_ngbg_stanza(Tcp_mss_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTcp_mss_ngbg_stanza(Tcp_mss_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterHost_name_sys_stanza(Host_name_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitHost_name_sys_stanza(Host_name_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_ro_stanza(Null_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_ro_stanza(Null_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPo_stanza_list(Po_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPo_stanza_list(Po_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLocation_sys_stanza(Location_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLocation_sys_stanza(Location_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNgbg_stanza(Ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNgbg_stanza(Ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRemove_private_gbg_stanza(
         Remove_private_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRemove_private_gbg_stanza(
         Remove_private_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterChassis_stanza(Chassis_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitChassis_stanza(Chassis_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTacplus_server_sys_stanza(
         Tacplus_server_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTacplus_server_sys_stanza(
         Tacplus_server_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_gbg_stanza(Inactive_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_gbg_stanza(Inactive_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterVlanid_u_if_stanza(Vlanid_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVlanid_u_if_stanza(Vlanid_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMetric_out_ngbg_stanza(Metric_out_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMetric_out_ngbg_stanza(Metric_out_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_from_t_ps_stanza(
         Inactive_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_from_t_ps_stanza(
         Inactive_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMax_configuration_rollbacks_sys_stanza(
         Max_configuration_rollbacks_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMax_configuration_rollbacks_sys_stanza(
         Max_configuration_rollbacks_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_stanza(Null_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_stanza(Null_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInterface_stanza(Interface_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInterface_stanza(Interface_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterVlan_tags_u_if_stanza(Vlan_tags_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVlan_tags_u_if_stanza(Vlan_tags_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAuthentication_order_sys_stanza(
         Authentication_order_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAuthentication_order_sys_stanza(
         Authentication_order_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRib_ro_stanza(Rib_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRib_ro_stanza(Rib_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLocal_address_gbg_stanza(Local_address_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLocal_address_gbg_stanza(Local_address_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterP_stanza(P_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitP_stanza(P_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDouble_num(Double_numContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDouble_num(Double_numContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLicense_sys_stanza(License_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLicense_sys_stanza(License_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterEmpty_neighbor_stanza(Empty_neighbor_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitEmpty_neighbor_stanza(Empty_neighbor_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterConnections_p_stanza(Connections_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitConnections_p_stanza(Connections_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFrom_t_ps_stanza(From_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFrom_t_ps_stanza(From_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterProtocols_stanza(Protocols_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitProtocols_stanza(Protocols_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterImport_op_stanza(Import_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitImport_op_stanza(Import_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNetwork_summary_export_aop_stanza(
         Network_summary_export_aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNetwork_summary_export_aop_stanza(
         Network_summary_export_aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterOp_stanza(Op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitOp_stanza(Op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterEncapsulation_u_if_stanza(
         Encapsulation_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitEncapsulation_u_if_stanza(
         Encapsulation_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAccept_then_t_ps_stanza(Accept_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAccept_then_t_ps_stanza(Accept_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInclude_mp_next_hop_ngbg_stanza(
         Include_mp_next_hop_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInclude_mp_next_hop_ngbg_stanza(
         Include_mp_next_hop_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterCommunity_literal(Community_literalContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitCommunity_literal(Community_literalContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPorts_sys_stanza(Ports_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPorts_sys_stanza(Ports_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNssa_aop_stanza(Nssa_aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNssa_aop_stanza(Nssa_aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterName_server_sys_stanza(Name_server_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitName_server_sys_stanza(Name_server_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTo_t_ps_stanza(To_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTo_t_ps_stanza(To_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterHold_time_ngbg_stanza(Hold_time_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitHold_time_ngbg_stanza(Hold_time_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAop_stanza(Aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAop_stanza(Aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfd_p_stanza(Bfd_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfd_p_stanza(Bfd_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSource_port_from_t_ff_stanza(
         Source_port_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSource_port_from_t_ff_stanza(
         Source_port_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFam_u_if_stanza(Fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFam_u_if_stanza(Fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTime_zone_sys_stanza(Time_zone_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTime_zone_sys_stanza(Time_zone_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_ngbg_stanza(Inactive_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_ngbg_stanza(Inactive_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterExport_common_stanza(Export_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitExport_common_stanza(Export_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLink_mode_if_stanza(Link_mode_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLink_mode_if_stanza(Link_mode_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNeighbor_gbg_stanza(Neighbor_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_gbg_stanza(Neighbor_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBackup_router_sys_stanza(Backup_router_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBackup_router_sys_stanza(Backup_router_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInterfaces_stanza(Interfaces_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInterfaces_stanza(Interfaces_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLldp_p_stanza(Lldp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLldp_p_stanza(Lldp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFam_u_if_stanza_list(Fam_u_if_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFam_u_if_stanza_list(Fam_u_if_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLog_updown_common_stanza(Log_updown_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLog_updown_common_stanza(Log_updown_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFrom_t_ff_stanza_list(From_t_ff_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFrom_t_ff_stanza_list(From_t_ff_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDisable_if_stanza(Disable_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDisable_if_stanza(Disable_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDomain_name_sys_stanza(Domain_name_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDomain_name_sys_stanza(Domain_name_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterThen_t_ps_stanza(Then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitThen_t_ps_stanza(Then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLogin_sys_stanza(Login_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLogin_sys_stanza(Login_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPrefix_list_from_t_ps_stanza(
         Prefix_list_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPrefix_list_from_t_ps_stanza(
         Prefix_list_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTraps_if_stanza(Traps_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTraps_if_stanza(Traps_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMtu_fam_u_if_stanza(Mtu_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMtu_fam_u_if_stanza(Mtu_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPeer_as_ngbg_stanza(Peer_as_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPeer_as_ngbg_stanza(Peer_as_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRib_group_sro_stanza(Rib_group_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRib_group_sro_stanza(Rib_group_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPort(PortContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPort(PortContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInterface_from_t_ps_stanza(
         Interface_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInterface_from_t_ps_stanza(
         Interface_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterL2_circuit_p_stanza(L2_circuit_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitL2_circuit_p_stanza(L2_circuit_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPrefix_list_po_stanza(Prefix_list_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPrefix_list_po_stanza(Prefix_list_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterImport_gbg_stanza(Import_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitImport_gbg_stanza(Import_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMultihop_ngbg_stanza(Multihop_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMultihop_ngbg_stanza(Multihop_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterThen_t_ps_stanza_list(Then_t_ps_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitThen_t_ps_stanza_list(Then_t_ps_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSyslog_sys_stanza(Syslog_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSyslog_sys_stanza(Syslog_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_bg_stanza(Inactive_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_bg_stanza(Inactive_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterStatic_opts_sro_stanza(Static_opts_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitStatic_opts_sro_stanza(Static_opts_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterType_gbg_stanza(Type_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitType_gbg_stanza(Type_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_u_if_stanza(Null_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_u_if_stanza(Null_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterThen_t_ff_stanza_list(Then_t_ff_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitThen_t_ff_stanza_list(Then_t_ff_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterVlan_id_fam_u_if_stanza(Vlan_id_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVlan_id_fam_u_if_stanza(Vlan_id_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRouter_id_ro_stanza(Router_id_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRouter_id_ro_stanza(Router_id_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_fam_u_if_stanza(Inactive_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_fam_u_if_stanza(Inactive_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterJ_stanza_list(J_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitJ_stanza_list(J_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterCommunity_literal_list(Community_literal_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitCommunity_literal_list(Community_literal_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInput_vlan_map_u_if_stanza(
         Input_vlan_map_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInput_vlan_map_u_if_stanza(
         Input_vlan_map_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLocal_preference_ngbg_stanza(
         Local_preference_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLocal_preference_ngbg_stanza(
         Local_preference_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLocal_address_ngbg_stanza(
         Local_address_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLocal_address_ngbg_stanza(
         Local_address_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRouter_advertisement_p_stanza(
         Router_advertisement_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRouter_advertisement_p_stanza(
         Router_advertisement_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLocal_as_gbg_stanza(Local_as_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLocal_as_gbg_stanza(Local_as_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterGraceful_restart_ngbg_stanza(
         Graceful_restart_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitGraceful_restart_ngbg_stanza(
         Graceful_restart_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMulticast_ro_stanza(Multicast_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMulticast_ro_stanza(Multicast_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPrefix_list_filter_from_t_ps_stanza(
         Prefix_list_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPrefix_list_filter_from_t_ps_stanza(
         Prefix_list_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterProtocol_from_t_ff_stanza(
         Protocol_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitProtocol_from_t_ff_stanza(
         Protocol_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterExport_op_stanza(Export_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitExport_op_stanza(Export_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMatch_type_filter_from_t_ps_stanza(
         Match_type_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMatch_type_filter_from_t_ps_stanza(
         Match_type_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFrom_t_ff_stanza(From_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFrom_t_ff_stanza(From_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterGbg_stanza_list(Gbg_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitGbg_stanza_list(Gbg_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPo_stanza(Po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPo_stanza(Po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterStatic_ro_stanza(Static_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitStatic_ro_stanza(Static_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAutonomous_system_ro_stanza(
         Autonomous_system_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAutonomous_system_ro_stanza(
         Autonomous_system_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDescription_common_stanza(
         Description_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDescription_common_stanza(
         Description_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPort_mode_fam_u_if_stanza(
         Port_mode_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPort_mode_fam_u_if_stanza(
         Port_mode_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfd_liveness_detection_ngbg_stanza(
         Bfd_liveness_detection_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfd_liveness_detection_ngbg_stanza(
         Bfd_liveness_detection_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_then_t_ff_stanza(Null_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_then_t_ff_stanza(Null_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTraceoptions_op_stanza(Traceoptions_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTraceoptions_op_stanza(Traceoptions_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTo_t_ps_stanza_list(To_t_ps_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTo_t_ps_stanza_list(To_t_ps_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPrimary_fam_u_if_stanza(Primary_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPrimary_fam_u_if_stanza(Primary_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIf_stanza_list(If_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIf_stanza_list(If_stanza_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMetric_then_t_ps_stanza(Metric_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMetric_then_t_ps_stanza(Metric_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterEncapsulation_if_stanza(Encapsulation_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitEncapsulation_if_stanza(Encapsulation_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInstance_to_t_ps_stanza(Instance_to_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInstance_to_t_ps_stanza(Instance_to_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNative_vlan_id_fam_u_if_stanza(
         Native_vlan_id_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNative_vlan_id_fam_u_if_stanza(
         Native_vlan_id_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRadius_server_sys_stanza(Radius_server_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRadius_server_sys_stanza(Radius_server_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterImport_ngbg_stanza(Import_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitImport_ngbg_stanza(Import_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMtu_common_stanza(Mtu_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMtu_common_stanza(Mtu_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPolicer_fam_u_if_stanza(Policer_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPolicer_fam_u_if_stanza(Policer_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterEncapsulation_common_stanza(
         Encapsulation_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitEncapsulation_common_stanza(
         Encapsulation_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterServices_sys_stanza(Services_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitServices_sys_stanza(Services_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBridge_domains_stanza(Bridge_domains_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBridge_domains_stanza(Bridge_domains_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSource_address_filter_from_t_ps_stanza(
         Source_address_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSource_address_filter_from_t_ps_stanza(
         Source_address_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFirewall_stanza(Firewall_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFirewall_stanza(Firewall_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNext_term_then_t_ps_stanza(
         Next_term_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNext_term_then_t_ps_stanza(
         Next_term_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMld_p_stanza(Mld_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMld_p_stanza(Mld_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterGroup_bg_stanza(Group_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitGroup_bg_stanza(Group_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDomain_search_sys_stanza(Domain_search_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDomain_search_sys_stanza(Domain_search_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMetric_out_gbg_stanza(Metric_out_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMetric_out_gbg_stanza(Metric_out_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAddress_fam_u_if_stanza(Address_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAddress_fam_u_if_stanza(Address_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_op_stanza(Null_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_op_stanza(Null_op_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterServices_stanza(Services_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitServices_stanza(Services_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterApply_groups_u_if_stanza(Apply_groups_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitApply_groups_u_if_stanza(Apply_groups_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLoad_balance_then_t_ps_stanza(
         Load_balance_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLoad_balance_then_t_ps_stanza(
         Load_balance_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRib_groups_ro_stanza(Rib_groups_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRib_groups_ro_stanza(Rib_groups_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBgp_family_common_stanza(Bgp_family_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBgp_family_common_stanza(Bgp_family_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAction_filter_from_t_ps_stanza(
         Action_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAction_filter_from_t_ps_stanza(
         Action_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRadius_options_sys_stanza(
         Radius_options_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRadius_options_sys_stanza(
         Radius_options_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTag_from_t_ps_stanza(Tag_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTag_from_t_ps_stanza(Tag_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_then_t_ps_stanza(
         Inactive_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_then_t_ps_stanza(
         Inactive_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterString_up_to_semicolon(String_up_to_semicolonContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitString_up_to_semicolon(String_up_to_semicolonContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMax_configurations_on_flash_sys_stanza(
         Max_configurations_on_flash_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMax_configurations_on_flash_sys_stanza(
         Max_configurations_on_flash_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBg_stanza(Bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBg_stanza(Bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPolicy_options_stanza(Policy_options_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPolicy_options_stanza(Policy_options_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRstp_p_stanza(Rstp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRstp_p_stanza(Rstp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfd_liveness_detection_common_stanza(
         Bfd_liveness_detection_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfd_liveness_detection_common_stanza(
         Bfd_liveness_detection_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNo_neighbor_learn_fam_u_if_stanza(
         No_neighbor_learn_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNo_neighbor_learn_fam_u_if_stanza(
         No_neighbor_learn_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_to_t_ps_stanza(Inactive_to_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_to_t_ps_stanza(Inactive_to_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterApply_groups_if_stanza(Apply_groups_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitApply_groups_if_stanza(Apply_groups_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLog_then_t_ff_stanza(Log_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLog_then_t_ff_stanza(Log_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterVstp_p_stanza(Vstp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVstp_p_stanza(Vstp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_gbg_stanza(Null_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_gbg_stanza(Null_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterUnit_if_stanza(Unit_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitUnit_if_stanza(Unit_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAs_path_from_t_ps_stanza(As_path_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAs_path_from_t_ps_stanza(As_path_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterVariable_list(Variable_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVariable_list(Variable_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAggregated_ether_options_if_stanza(
         Aggregated_ether_options_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAggregated_ether_options_if_stanza(
         Aggregated_ether_options_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterGbg_stanza(Gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitGbg_stanza(Gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTerm_f_f_stanza(Term_f_f_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTerm_f_f_stanza(Term_f_f_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterOutput_vlan_map_u_if_stanza(
         Output_vlan_map_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitOutput_vlan_map_u_if_stanza(
         Output_vlan_map_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAs_path_po_stanza(As_path_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAs_path_po_stanza(As_path_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRouting_instances_stanza(Routing_instances_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRouting_instances_stanza(Routing_instances_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterCommunity_then_t_ps_stanza(
         Community_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitCommunity_then_t_ps_stanza(
         Community_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLog_updown_gbg_stanza(Log_updown_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLog_updown_gbg_stanza(Log_updown_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_ngbg_stanza(Null_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_ngbg_stanza(Null_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLldp_med_p_stanza(Lldp_med_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLldp_med_p_stanza(Lldp_med_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIf_stanza(If_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIf_stanza(If_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRib_common_stanza(Rib_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRib_common_stanza(Rib_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAccounting_sys_stanza(Accounting_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAccounting_sys_stanza(Accounting_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFilter_fam_u_if_stanza(Filter_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFilter_fam_u_if_stanza(Filter_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterString_in_double_quotes(String_in_double_quotesContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitString_in_double_quotes(String_in_double_quotesContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNeighbor_from_t_ps_stanza(
         Neighbor_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_from_t_ps_stanza(
         Neighbor_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNtp_sys_stanza(Ntp_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNtp_sys_stanza(Ntp_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamily_u_if_stanza(Family_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamily_u_if_stanza(Family_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIgnored_substanza(Ignored_substanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIgnored_substanza(Ignored_substanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamily_gbg_stanza(Family_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamily_gbg_stanza(Family_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNext_policy_then_t_ps_stanza(
         Next_policy_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNext_policy_then_t_ps_stanza(
         Next_policy_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterProtocol_from_t_ps_stanza(
         Protocol_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitProtocol_from_t_ps_stanza(
         Protocol_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRo_stanza(Ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRo_stanza(Ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNo_redirects_fam_u_if_stanza(
         No_redirects_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNo_redirects_fam_u_if_stanza(
         No_redirects_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_then_t_ps_stanza(Null_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_then_t_ps_stanza(Null_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRsvp_p_stanza(Rsvp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRsvp_p_stanza(Rsvp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInterface_routes_ro_stanza(
         Interface_routes_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInterface_routes_ro_stanza(
         Interface_routes_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBfd_liveness_detection_gbg_stanza(
         Bfd_liveness_detection_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBfd_liveness_detection_gbg_stanza(
         Bfd_liveness_detection_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSro_stanza(Sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSro_stanza(Sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_u_if_stanza(Inactive_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_u_if_stanza(Inactive_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterCluster_ngbg_stanza(Cluster_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitCluster_ngbg_stanza(Cluster_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_aop_stanza(Null_aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_aop_stanza(Null_aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSample_then_t_ff_stanza(Sample_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSample_then_t_ff_stanza(Sample_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRemove_private_common_stanza(
         Remove_private_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRemove_private_common_stanza(
         Remove_private_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDefaults_sro_stanza(Defaults_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDefaults_sro_stanza(Defaults_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_if_stanza(Null_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_if_stanza(Null_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterForwarding_table_ro_stanza(
         Forwarding_table_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitForwarding_table_ro_stanza(
         Forwarding_table_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterOspf_p_stanza(Ospf_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitOspf_p_stanza(Ospf_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRoute_filter_from_t_ps_stanza(
         Route_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRoute_filter_from_t_ps_stanza(
         Route_filter_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAnon_term_ps_po_stanza(Anon_term_ps_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAnon_term_ps_po_stanza(Anon_term_ps_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMultipath_ngbg_stanza(Multipath_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMultipath_ngbg_stanza(Multipath_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFlexible_vlan_tagging_if_stanza(
         Flexible_vlan_tagging_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFlexible_vlan_tagging_if_stanza(
         Flexible_vlan_tagging_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMultihop_common_stanza(Multihop_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMultihop_common_stanza(Multihop_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterGroup_stanza(Group_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitGroup_stanza(Group_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterClass_of_service_stanza(Class_of_service_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitClass_of_service_stanza(Class_of_service_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRib_from_t_ps_stanza(Rib_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRib_from_t_ps_stanza(Rib_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLdp_p_stanza(Ldp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLdp_p_stanza(Ldp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTunnel_u_if_stanza(Tunnel_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTunnel_u_if_stanza(Tunnel_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterVlan_tagging_if_stanza(Vlan_tagging_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVlan_tagging_if_stanza(Vlan_tagging_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterExport_ngbg_stanza(Export_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitExport_ngbg_stanza(Export_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMultipath_gbg_stanza(Multipath_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMultipath_gbg_stanza(Multipath_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterApply_groups_stanza(Apply_groups_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitApply_groups_stanza(Apply_groups_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMartians_ro_stanza(Martians_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMartians_ro_stanza(Martians_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_bg_stanza(Null_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_bg_stanza(Null_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterProtocol(ProtocolContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitProtocol(ProtocolContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSubterm_ps_po_stanza(Subterm_ps_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSubterm_ps_po_stanza(Subterm_ps_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterVlan_members_fam_u_if_stanza(
         Vlan_members_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVlan_members_fam_u_if_stanza(
         Vlan_members_fam_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterGigether_options_if_stanza(
         Gigether_options_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitGigether_options_if_stanza(
         Gigether_options_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIcmp_type_from_t_ff_stanza(
         Icmp_type_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIcmp_type_from_t_ff_stanza(
         Icmp_type_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInterface_aop_stanza(Interface_aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInterface_aop_stanza(Interface_aop_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPeer_as_common_stanza(Peer_as_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPeer_as_common_stanza(Peer_as_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterEnable_u_if_stanza(Enable_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitEnable_u_if_stanza(Enable_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterJuniper_configuration(Juniper_configurationContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitJuniper_configuration(Juniper_configurationContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSubstanza(SubstanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSubstanza(SubstanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMsdp_p_stanza(Msdp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMsdp_p_stanza(Msdp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFilter_f_stanza(Filter_f_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFilter_f_stanza(Filter_f_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterTraceoptions_bg_stanza(Traceoptions_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitTraceoptions_bg_stanza(Traceoptions_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLocal_preference_then_t_ps_stanza(
         Local_preference_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLocal_preference_then_t_ps_stanza(
         Local_preference_then_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMetric_out_common_stanza(Metric_out_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMetric_out_common_stanza(Metric_out_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterVersion_stanza(Version_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVersion_stanza(Version_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMac_if_stanza(Mac_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMac_if_stanza(Mac_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamily_from_t_ps_stanza(Family_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamily_from_t_ps_stanza(Family_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterImport_common_stanza(Import_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitImport_common_stanza(Import_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLocal_address_common_stanza(
         Local_address_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLocal_address_common_stanza(
         Local_address_common_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_interface_stanza(
         Inactive_interface_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_interface_stanza(
         Inactive_interface_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterU_if_stanza(U_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitU_if_stanza(U_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAs_id(As_idContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAs_id(As_idContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDestination_address_from_t_ff_stanza(
         Destination_address_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDestination_address_from_t_ff_stanza(
         Destination_address_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterGr_stanza(Gr_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitGr_stanza(Gr_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDisable_u_if_stanza(Disable_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDisable_u_if_stanza(Disable_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRoute_sro_stanza(Route_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRoute_sro_stanza(Route_sro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterProtocol_list(Protocol_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitProtocol_list(Protocol_listContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNext_term_then_t_ff_stanza(
         Next_term_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNext_term_then_t_ff_stanza(
         Next_term_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterExport_gbg_stanza(Export_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitExport_gbg_stanza(Export_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRib_to_t_ps_stanza(Rib_to_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRib_to_t_ps_stanza(Rib_to_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNull_po_stanza(Null_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNull_po_stanza(Null_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterCommunity_po_stanza(Community_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitCommunity_po_stanza(Community_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIgmp_snooping_p_stanza(Igmp_snooping_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIgmp_snooping_p_stanza(Igmp_snooping_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterIsis_p_stanza(Isis_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitIsis_p_stanza(Isis_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRoot_authentication_sys_stanza(
         Root_authentication_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRoot_authentication_sys_stanza(
         Root_authentication_sys_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterNot_brace(Not_braceContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNot_brace(Not_braceContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterBgp_p_stanza(Bgp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitBgp_p_stanza(Bgp_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFraming_if_stanza(Framing_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFraming_if_stanza(Framing_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamily_bg_stanza(Family_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamily_bg_stanza(Family_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMultihop_gbg_stanza(Multihop_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMultihop_gbg_stanza(Multihop_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterInactive_term_ps_po_stanza(
         Inactive_term_ps_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitInactive_term_ps_po_stanza(
         Inactive_term_ps_po_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDiscard_then_t_ff_stanza(Discard_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDiscard_then_t_ff_stanza(Discard_then_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSource_address_from_t_ff_stanza(
         Source_address_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSource_address_from_t_ff_stanza(
         Source_address_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterEnable_if_stanza(Enable_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitEnable_if_stanza(Enable_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterAggregate_ro_stanza(Aggregate_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitAggregate_ro_stanza(Aggregate_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterOrigin_from_t_ps_stanza(Origin_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitOrigin_from_t_ps_stanza(Origin_from_t_ps_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterFamily_ngbg_stanza(Family_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitFamily_ngbg_stanza(Family_ngbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterLog_updown_bg_stanza(Log_updown_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitLog_updown_bg_stanza(Log_updown_bg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterSystem_stanza(System_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSystem_stanza(System_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterPeer_as_gbg_stanza(Peer_as_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPeer_as_gbg_stanza(Peer_as_gbg_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDescription_u_if_stanza(Description_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDescription_u_if_stanza(Description_u_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterMpls_p_stanza(Mpls_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMpls_p_stanza(Mpls_p_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRemoved_stanza(Removed_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRemoved_stanza(Removed_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterDestination_port_from_t_ff_stanza(
         Destination_port_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitDestination_port_from_t_ff_stanza(
         Destination_port_from_t_ff_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

}
