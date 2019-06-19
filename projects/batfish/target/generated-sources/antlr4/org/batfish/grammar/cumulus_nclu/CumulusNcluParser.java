// Generated from org/batfish/grammar/cumulus_nclu/CumulusNcluParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.cumulus_nclu;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CumulusNcluParser extends org.batfish.grammar.cumulus_nclu.parsing.CumulusNcluBaseParser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		EXTRA_CONFIGURATION_FOOTER=1, USERNAME=2, ACCESS=3, ACTIVATE=4, ADD=5, 
		ADDRESS=6, ADDRESS_VIRTUAL=7, ADVERTISE=8, ADVERTISE_ALL_VNI=9, ADVERTISE_DEFAULT_GW=10, 
		ALERTS=11, ARP_ND_SUPPRESS=12, AUTO=13, AUTONOMOUS_SYSTEM=14, BACKUP_IP=15, 
		BGP=16, BOND=17, BPDUGUARD=18, BRIDGE=19, CLAG=20, COMMIT=21, CONNECTED=22, 
		CRITICAL=23, DATACENTER=24, DEBUGGING=25, DEFAULTS=26, DEL=27, DENY=28, 
		DNS=29, DOT1X=30, EMERGENCIES=31, ERRORS=32, EVPN=33, EXTERNAL=34, HOSTNAME=35, 
		IBURST=36, ID=37, INFORMATIONAL=38, INTEGRATED_VTYSH_CONFIG=39, INTERFACE=40, 
		INTERNAL=41, IP=42, IPV4=43, IPV6=44, L2VPN=45, LEARNING=46, LO=47, LOCAL_TUNNELIP=48, 
		LOG=49, LOOPBACK=50, MATCH=51, NAMESERVER=52, NEIGHBOR=53, NETWORK=54, 
		NET=55, NOTIFICATIONS=56, NTP=57, OFF=58, ON=59, PEER_IP=60, PERMIT=61, 
		PORTBPDUFILTER=62, PORTS=63, PRIORITY=64, PTP=65, PVID=66, REDISTRIBUTE=67, 
		REMOTE_AS=68, ROUTE=69, ROUTE_MAP=70, ROUTER_ID=71, ROUTING=72, SERVER=73, 
		SERVICE=74, SLAVES=75, SNMP_SERVER=76, SOURCE=77, STATIC=78, STP=79, SYS_MAC=80, 
		SYSLOG=81, TIME=82, UNICAST=83, VIDS=84, VLAN=85, VLAN_AWARE=86, VLAN_ID=87, 
		VLAN_RAW_DEVICE=88, VNI=89, VRF=90, VRF_TABLE=91, VXLAN=92, VXLAN_ANYCAST_IP=93, 
		WARNINGS=94, ZONE=95, EXTRA_CONFIGURATION_HEADER=96, COMMA=97, COMMENT_LINE=98, 
		COMMENT_TAIL=99, DASH=100, DEC=101, IP_ADDRESS=102, IP_PREFIX=103, IPV6_ADDRESS=104, 
		IPV6_PREFIX=105, MAC_ADDRESS=106, NEWLINE=107, NUMBERED_WORD=108, WORD=109, 
		WS=110, M_Printf_WS=111, M_Printf_EXTRA_CONFIGURATION_FOOTER=112, M_Printf_NEWLINE=113, 
		M_Printf_USERNAME=114;
	public static final int
		RULE_cumulus_nclu_configuration = 0, RULE_statement = 1, RULE_s_extra_configuration = 2, 
		RULE_s_net_add = 3, RULE_a_bond = 4, RULE_bond_bond = 5, RULE_bobo_slaves = 6, 
		RULE_bond_bridge = 7, RULE_bob_access = 8, RULE_bob_pvid = 9, RULE_bob_vids = 10, 
		RULE_bond_clag_id = 11, RULE_bond_ip_address = 12, RULE_bond_vrf = 13, 
		RULE_a_bridge = 14, RULE_bridge_bridge = 15, RULE_brbr_ports = 16, RULE_brbr_pvid = 17, 
		RULE_brbr_vids = 18, RULE_brbr_vlan_aware = 19, RULE_a_dns = 20, RULE_dns_nameserver = 21, 
		RULE_dn4 = 22, RULE_dn6 = 23, RULE_a_dot1x = 24, RULE_a_hostname = 25, 
		RULE_a_loopback = 26, RULE_l_clag = 27, RULE_lc_vxlan_anycast_ip = 28, 
		RULE_l_ip_address = 29, RULE_a_ptp = 30, RULE_a_snmp_server = 31, RULE_a_time = 32, 
		RULE_t_ntp = 33, RULE_tn_server = 34, RULE_tn_source = 35, RULE_t_zone = 36, 
		RULE_a_vlan = 37, RULE_v_ip_address = 38, RULE_v_ip_address_virtual = 39, 
		RULE_v_vlan_id = 40, RULE_v_vlan_raw_device = 41, RULE_v_vrf = 42, RULE_a_vrf = 43, 
		RULE_vrf_ip_address = 44, RULE_vrf_vni = 45, RULE_vrf_vrf_table = 46, 
		RULE_a_vxlan = 47, RULE_vx_bridge = 48, RULE_vxb_access = 49, RULE_vxb_arp_nd_suppress = 50, 
		RULE_vxb_learning = 51, RULE_vx_stp = 52, RULE_vxs_bpduguard = 53, RULE_vxs_portbpdufilter = 54, 
		RULE_vx_vxlan = 55, RULE_vxv_id = 56, RULE_vxv_local_tunnelip = 57, RULE_s_net_add_unrecognized = 58, 
		RULE_s_null = 59, RULE_glob = 60, RULE_glob_range_set = 61, RULE_glob_word = 62, 
		RULE_interface_address = 63, RULE_ip_address = 64, RULE_ip_prefix = 65, 
		RULE_ipv6_address = 66, RULE_line_action = 67, RULE_mac_address = 68, 
		RULE_null_rest_of_line = 69, RULE_numbered_word = 70, RULE_range = 71, 
		RULE_range_set = 72, RULE_uint16 = 73, RULE_uint32 = 74, RULE_vlan_id = 75, 
		RULE_vlan_range = 76, RULE_vlan_range_set = 77, RULE_vni_number = 78, 
		RULE_word = 79, RULE_a_bgp = 80, RULE_b_common = 81, RULE_b_autonomous_system = 82, 
		RULE_b_ipv4_unicast = 83, RULE_bi4_network = 84, RULE_bi4_redistribute_connected = 85, 
		RULE_bi4_redistribute_static = 86, RULE_b_l2vpn = 87, RULE_ble_advertise_all_vni = 88, 
		RULE_ble_advertise_default_gw = 89, RULE_ble_advertise_ipv4_unicast = 90, 
		RULE_ble_neighbor = 91, RULE_blen_activate = 92, RULE_b_neighbor = 93, 
		RULE_bn_interface = 94, RULE_bni_remote_as_external = 95, RULE_bni_remote_as_internal = 96, 
		RULE_bni_remote_as_number = 97, RULE_b_router_id = 98, RULE_b_vrf = 99, 
		RULE_frr_vrf = 100, RULE_frrv_ip_route = 101, RULE_frr_username = 102, 
		RULE_frr_null_rest_of_line = 103, RULE_frr_unrecognized = 104, RULE_a_interface = 105, 
		RULE_i_bridge = 106, RULE_ib_access = 107, RULE_ib_pvid = 108, RULE_ib_vids = 109, 
		RULE_i_clag = 110, RULE_ic_backup_ip = 111, RULE_ic_peer_ip = 112, RULE_ic_priority = 113, 
		RULE_ic_sys_mac = 114, RULE_i_ip_address = 115, RULE_i_vrf = 116, RULE_a_routing = 117, 
		RULE_r_defaults_datacenter = 118, RULE_r_log = 119, RULE_rl_syslog = 120, 
		RULE_r_route = 121, RULE_r_route_map = 122, RULE_rm_match = 123, RULE_rmm_interface = 124, 
		RULE_r_service_integrated_vtysh_config = 125;
	private static String[] makeRuleNames() {
		return new String[] {
			"cumulus_nclu_configuration", "statement", "s_extra_configuration", "s_net_add", 
			"a_bond", "bond_bond", "bobo_slaves", "bond_bridge", "bob_access", "bob_pvid", 
			"bob_vids", "bond_clag_id", "bond_ip_address", "bond_vrf", "a_bridge", 
			"bridge_bridge", "brbr_ports", "brbr_pvid", "brbr_vids", "brbr_vlan_aware", 
			"a_dns", "dns_nameserver", "dn4", "dn6", "a_dot1x", "a_hostname", "a_loopback", 
			"l_clag", "lc_vxlan_anycast_ip", "l_ip_address", "a_ptp", "a_snmp_server", 
			"a_time", "t_ntp", "tn_server", "tn_source", "t_zone", "a_vlan", "v_ip_address", 
			"v_ip_address_virtual", "v_vlan_id", "v_vlan_raw_device", "v_vrf", "a_vrf", 
			"vrf_ip_address", "vrf_vni", "vrf_vrf_table", "a_vxlan", "vx_bridge", 
			"vxb_access", "vxb_arp_nd_suppress", "vxb_learning", "vx_stp", "vxs_bpduguard", 
			"vxs_portbpdufilter", "vx_vxlan", "vxv_id", "vxv_local_tunnelip", "s_net_add_unrecognized", 
			"s_null", "glob", "glob_range_set", "glob_word", "interface_address", 
			"ip_address", "ip_prefix", "ipv6_address", "line_action", "mac_address", 
			"null_rest_of_line", "numbered_word", "range", "range_set", "uint16", 
			"uint32", "vlan_id", "vlan_range", "vlan_range_set", "vni_number", "word", 
			"a_bgp", "b_common", "b_autonomous_system", "b_ipv4_unicast", "bi4_network", 
			"bi4_redistribute_connected", "bi4_redistribute_static", "b_l2vpn", "ble_advertise_all_vni", 
			"ble_advertise_default_gw", "ble_advertise_ipv4_unicast", "ble_neighbor", 
			"blen_activate", "b_neighbor", "bn_interface", "bni_remote_as_external", 
			"bni_remote_as_internal", "bni_remote_as_number", "b_router_id", "b_vrf", 
			"frr_vrf", "frrv_ip_route", "frr_username", "frr_null_rest_of_line", 
			"frr_unrecognized", "a_interface", "i_bridge", "ib_access", "ib_pvid", 
			"ib_vids", "i_clag", "ic_backup_ip", "ic_peer_ip", "ic_priority", "ic_sys_mac", 
			"i_ip_address", "i_vrf", "a_routing", "r_defaults_datacenter", "r_log", 
			"rl_syslog", "r_route", "r_route_map", "rm_match", "rmm_interface", "r_service_integrated_vtysh_config"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, "'access'", "'activate'", "'add'", "'address'", "'address-virtual'", 
			"'advertise'", "'advertise-all-vni'", "'advertise-default-gw'", "'alerts'", 
			"'arp-nd-suppress'", "'auto'", "'autonomous-system'", "'backup-ip'", 
			"'bgp'", "'bond'", "'bpduguard'", "'bridge'", "'clag'", "'commit'", "'connected'", 
			"'critical'", "'datacenter'", "'debugging'", "'defaults'", "'del'", "'deny'", 
			"'dns'", "'dot1x'", "'emergencies'", "'errors'", "'evpn'", "'external'", 
			"'hostname'", "'iburst'", "'id'", "'informational'", "'integrated-vtysh-config'", 
			"'interface'", "'internal'", null, "'ipv4'", "'ipv6'", "'l2vpn'", "'learning'", 
			"'lo'", "'local-tunnelip'", "'log'", "'loopback'", "'match'", "'nameserver'", 
			"'neighbor'", "'network'", "'net'", "'notifications'", "'ntp'", "'off'", 
			"'on'", "'peer-ip'", "'permit'", "'portbpdufilter'", "'ports'", "'priority'", 
			"'ptp'", "'pvid'", "'redistribute'", "'remote-as'", null, "'route-map'", 
			"'router-id'", "'routing'", "'server'", "'service'", "'slaves'", "'snmp-server'", 
			"'source'", "'static'", "'stp'", "'sys-mac'", "'syslog'", "'time'", "'unicast'", 
			"'vids'", "'vlan'", "'vlan-aware'", "'vlan-id'", "'vlan-raw-device'", 
			"'vni'", null, "'vrf-table'", "'vxlan'", "'vxlan-anycast-ip'", "'warnings'", 
			"'zone'", "'sudo sh -c \"printf ''", "','", null, null, "'-'", null, 
			null, null, null, null, null, null, null, null, null, null, "'' >> /etc/frr/frr.conf\"'", 
			"'\\n'", "'username'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "EXTRA_CONFIGURATION_FOOTER", "USERNAME", "ACCESS", "ACTIVATE", 
			"ADD", "ADDRESS", "ADDRESS_VIRTUAL", "ADVERTISE", "ADVERTISE_ALL_VNI", 
			"ADVERTISE_DEFAULT_GW", "ALERTS", "ARP_ND_SUPPRESS", "AUTO", "AUTONOMOUS_SYSTEM", 
			"BACKUP_IP", "BGP", "BOND", "BPDUGUARD", "BRIDGE", "CLAG", "COMMIT", 
			"CONNECTED", "CRITICAL", "DATACENTER", "DEBUGGING", "DEFAULTS", "DEL", 
			"DENY", "DNS", "DOT1X", "EMERGENCIES", "ERRORS", "EVPN", "EXTERNAL", 
			"HOSTNAME", "IBURST", "ID", "INFORMATIONAL", "INTEGRATED_VTYSH_CONFIG", 
			"INTERFACE", "INTERNAL", "IP", "IPV4", "IPV6", "L2VPN", "LEARNING", "LO", 
			"LOCAL_TUNNELIP", "LOG", "LOOPBACK", "MATCH", "NAMESERVER", "NEIGHBOR", 
			"NETWORK", "NET", "NOTIFICATIONS", "NTP", "OFF", "ON", "PEER_IP", "PERMIT", 
			"PORTBPDUFILTER", "PORTS", "PRIORITY", "PTP", "PVID", "REDISTRIBUTE", 
			"REMOTE_AS", "ROUTE", "ROUTE_MAP", "ROUTER_ID", "ROUTING", "SERVER", 
			"SERVICE", "SLAVES", "SNMP_SERVER", "SOURCE", "STATIC", "STP", "SYS_MAC", 
			"SYSLOG", "TIME", "UNICAST", "VIDS", "VLAN", "VLAN_AWARE", "VLAN_ID", 
			"VLAN_RAW_DEVICE", "VNI", "VRF", "VRF_TABLE", "VXLAN", "VXLAN_ANYCAST_IP", 
			"WARNINGS", "ZONE", "EXTRA_CONFIGURATION_HEADER", "COMMA", "COMMENT_LINE", 
			"COMMENT_TAIL", "DASH", "DEC", "IP_ADDRESS", "IP_PREFIX", "IPV6_ADDRESS", 
			"IPV6_PREFIX", "MAC_ADDRESS", "NEWLINE", "NUMBERED_WORD", "WORD", "WS", 
			"M_Printf_WS", "M_Printf_EXTRA_CONFIGURATION_FOOTER", "M_Printf_NEWLINE", 
			"M_Printf_USERNAME"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "CumulusNcluParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CumulusNcluParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class Cumulus_nclu_configurationContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(CumulusNcluParser.EOF, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(CumulusNcluParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CumulusNcluParser.NEWLINE, i);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public Cumulus_nclu_configurationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cumulus_nclu_configuration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterCumulus_nclu_configuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitCumulus_nclu_configuration(this);
		}
	}

	public final Cumulus_nclu_configurationContext cumulus_nclu_configuration() throws RecognitionException {
		Cumulus_nclu_configurationContext _localctx = new Cumulus_nclu_configurationContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_cumulus_nclu_configuration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(253);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NEWLINE) {
				{
				setState(252);
				match(NEWLINE);
				}
			}

			setState(256); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(255);
				statement();
				}
				}
				setState(258); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NET || _la==EXTRA_CONFIGURATION_HEADER );
			setState(261);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NEWLINE) {
				{
				setState(260);
				match(NEWLINE);
				}
			}

			setState(263);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends ParserRuleContext {
		public S_extra_configurationContext s_extra_configuration() {
			return getRuleContext(S_extra_configurationContext.class,0);
		}
		public S_net_addContext s_net_add() {
			return getRuleContext(S_net_addContext.class,0);
		}
		public S_net_add_unrecognizedContext s_net_add_unrecognized() {
			return getRuleContext(S_net_add_unrecognizedContext.class,0);
		}
		public S_nullContext s_null() {
			return getRuleContext(S_nullContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(269);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(265);
				s_extra_configuration();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(266);
				s_net_add();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(267);
				s_net_add_unrecognized();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(268);
				s_null();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class S_extra_configurationContext extends ParserRuleContext {
		public TerminalNode EXTRA_CONFIGURATION_HEADER() { return getToken(CumulusNcluParser.EXTRA_CONFIGURATION_HEADER, 0); }
		public TerminalNode EXTRA_CONFIGURATION_FOOTER() { return getToken(CumulusNcluParser.EXTRA_CONFIGURATION_FOOTER, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Frr_usernameContext frr_username() {
			return getRuleContext(Frr_usernameContext.class,0);
		}
		public Frr_vrfContext frr_vrf() {
			return getRuleContext(Frr_vrfContext.class,0);
		}
		public Frr_unrecognizedContext frr_unrecognized() {
			return getRuleContext(Frr_unrecognizedContext.class,0);
		}
		public S_extra_configurationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_extra_configuration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterS_extra_configuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitS_extra_configuration(this);
		}
	}

	public final S_extra_configurationContext s_extra_configuration() throws RecognitionException {
		S_extra_configurationContext _localctx = new S_extra_configurationContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_s_extra_configuration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(271);
			match(EXTRA_CONFIGURATION_HEADER);
			setState(275);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(272);
				frr_username();
				}
				break;
			case 2:
				{
				setState(273);
				frr_vrf();
				}
				break;
			case 3:
				{
				setState(274);
				frr_unrecognized();
				}
				break;
			}
			setState(277);
			match(EXTRA_CONFIGURATION_FOOTER);
			setState(278);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class S_net_addContext extends ParserRuleContext {
		public TerminalNode NET() { return getToken(CumulusNcluParser.NET, 0); }
		public TerminalNode ADD() { return getToken(CumulusNcluParser.ADD, 0); }
		public A_bgpContext a_bgp() {
			return getRuleContext(A_bgpContext.class,0);
		}
		public A_bondContext a_bond() {
			return getRuleContext(A_bondContext.class,0);
		}
		public A_bridgeContext a_bridge() {
			return getRuleContext(A_bridgeContext.class,0);
		}
		public A_dnsContext a_dns() {
			return getRuleContext(A_dnsContext.class,0);
		}
		public A_dot1xContext a_dot1x() {
			return getRuleContext(A_dot1xContext.class,0);
		}
		public A_hostnameContext a_hostname() {
			return getRuleContext(A_hostnameContext.class,0);
		}
		public A_interfaceContext a_interface() {
			return getRuleContext(A_interfaceContext.class,0);
		}
		public A_loopbackContext a_loopback() {
			return getRuleContext(A_loopbackContext.class,0);
		}
		public A_ptpContext a_ptp() {
			return getRuleContext(A_ptpContext.class,0);
		}
		public A_routingContext a_routing() {
			return getRuleContext(A_routingContext.class,0);
		}
		public A_snmp_serverContext a_snmp_server() {
			return getRuleContext(A_snmp_serverContext.class,0);
		}
		public A_timeContext a_time() {
			return getRuleContext(A_timeContext.class,0);
		}
		public A_vlanContext a_vlan() {
			return getRuleContext(A_vlanContext.class,0);
		}
		public A_vrfContext a_vrf() {
			return getRuleContext(A_vrfContext.class,0);
		}
		public A_vxlanContext a_vxlan() {
			return getRuleContext(A_vxlanContext.class,0);
		}
		public S_net_addContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_net_add; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterS_net_add(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitS_net_add(this);
		}
	}

	public final S_net_addContext s_net_add() throws RecognitionException {
		S_net_addContext _localctx = new S_net_addContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_s_net_add);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(280);
			match(NET);
			setState(281);
			match(ADD);
			setState(297);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BGP:
				{
				setState(282);
				a_bgp();
				}
				break;
			case BOND:
				{
				setState(283);
				a_bond();
				}
				break;
			case BRIDGE:
				{
				setState(284);
				a_bridge();
				}
				break;
			case DNS:
				{
				setState(285);
				a_dns();
				}
				break;
			case DOT1X:
				{
				setState(286);
				a_dot1x();
				}
				break;
			case HOSTNAME:
				{
				setState(287);
				a_hostname();
				}
				break;
			case INTERFACE:
				{
				setState(288);
				a_interface();
				}
				break;
			case LOOPBACK:
				{
				setState(289);
				a_loopback();
				}
				break;
			case PTP:
				{
				setState(290);
				a_ptp();
				}
				break;
			case ROUTING:
				{
				setState(291);
				a_routing();
				}
				break;
			case SNMP_SERVER:
				{
				setState(292);
				a_snmp_server();
				}
				break;
			case TIME:
				{
				setState(293);
				a_time();
				}
				break;
			case VLAN:
				{
				setState(294);
				a_vlan();
				}
				break;
			case VRF:
				{
				setState(295);
				a_vrf();
				}
				break;
			case VXLAN:
				{
				setState(296);
				a_vxlan();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_bondContext extends ParserRuleContext {
		public WordContext name;
		public TerminalNode BOND() { return getToken(CumulusNcluParser.BOND, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Bond_bondContext bond_bond() {
			return getRuleContext(Bond_bondContext.class,0);
		}
		public Bond_bridgeContext bond_bridge() {
			return getRuleContext(Bond_bridgeContext.class,0);
		}
		public Bond_clag_idContext bond_clag_id() {
			return getRuleContext(Bond_clag_idContext.class,0);
		}
		public Bond_ip_addressContext bond_ip_address() {
			return getRuleContext(Bond_ip_addressContext.class,0);
		}
		public Bond_vrfContext bond_vrf() {
			return getRuleContext(Bond_vrfContext.class,0);
		}
		public A_bondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_bond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_bond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_bond(this);
		}
	}

	public final A_bondContext a_bond() throws RecognitionException {
		A_bondContext _localctx = new A_bondContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_a_bond);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(299);
			match(BOND);
			setState(300);
			((A_bondContext)_localctx).name = word();
			setState(306);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BOND:
				{
				setState(301);
				bond_bond();
				}
				break;
			case BRIDGE:
				{
				setState(302);
				bond_bridge();
				}
				break;
			case CLAG:
				{
				setState(303);
				bond_clag_id();
				}
				break;
			case IP:
				{
				setState(304);
				bond_ip_address();
				}
				break;
			case VRF:
				{
				setState(305);
				bond_vrf();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bond_bondContext extends ParserRuleContext {
		public TerminalNode BOND() { return getToken(CumulusNcluParser.BOND, 0); }
		public Bobo_slavesContext bobo_slaves() {
			return getRuleContext(Bobo_slavesContext.class,0);
		}
		public Bond_bondContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bond_bond; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBond_bond(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBond_bond(this);
		}
	}

	public final Bond_bondContext bond_bond() throws RecognitionException {
		Bond_bondContext _localctx = new Bond_bondContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_bond_bond);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(308);
			match(BOND);
			setState(309);
			bobo_slaves();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bobo_slavesContext extends ParserRuleContext {
		public GlobContext slaves;
		public TerminalNode SLAVES() { return getToken(CumulusNcluParser.SLAVES, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public GlobContext glob() {
			return getRuleContext(GlobContext.class,0);
		}
		public Bobo_slavesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bobo_slaves; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBobo_slaves(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBobo_slaves(this);
		}
	}

	public final Bobo_slavesContext bobo_slaves() throws RecognitionException {
		Bobo_slavesContext _localctx = new Bobo_slavesContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_bobo_slaves);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(311);
			match(SLAVES);
			setState(312);
			((Bobo_slavesContext)_localctx).slaves = glob();
			setState(313);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bond_bridgeContext extends ParserRuleContext {
		public TerminalNode BRIDGE() { return getToken(CumulusNcluParser.BRIDGE, 0); }
		public Bob_accessContext bob_access() {
			return getRuleContext(Bob_accessContext.class,0);
		}
		public Bob_pvidContext bob_pvid() {
			return getRuleContext(Bob_pvidContext.class,0);
		}
		public Bob_vidsContext bob_vids() {
			return getRuleContext(Bob_vidsContext.class,0);
		}
		public Bond_bridgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bond_bridge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBond_bridge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBond_bridge(this);
		}
	}

	public final Bond_bridgeContext bond_bridge() throws RecognitionException {
		Bond_bridgeContext _localctx = new Bond_bridgeContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_bond_bridge);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(315);
			match(BRIDGE);
			setState(319);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ACCESS:
				{
				setState(316);
				bob_access();
				}
				break;
			case PVID:
				{
				setState(317);
				bob_pvid();
				}
				break;
			case VIDS:
				{
				setState(318);
				bob_vids();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bob_accessContext extends ParserRuleContext {
		public Vlan_idContext vlan;
		public TerminalNode ACCESS() { return getToken(CumulusNcluParser.ACCESS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vlan_idContext vlan_id() {
			return getRuleContext(Vlan_idContext.class,0);
		}
		public Bob_accessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bob_access; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBob_access(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBob_access(this);
		}
	}

	public final Bob_accessContext bob_access() throws RecognitionException {
		Bob_accessContext _localctx = new Bob_accessContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_bob_access);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(321);
			match(ACCESS);
			setState(322);
			((Bob_accessContext)_localctx).vlan = vlan_id();
			setState(323);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bob_pvidContext extends ParserRuleContext {
		public Vlan_idContext id;
		public TerminalNode PVID() { return getToken(CumulusNcluParser.PVID, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vlan_idContext vlan_id() {
			return getRuleContext(Vlan_idContext.class,0);
		}
		public Bob_pvidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bob_pvid; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBob_pvid(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBob_pvid(this);
		}
	}

	public final Bob_pvidContext bob_pvid() throws RecognitionException {
		Bob_pvidContext _localctx = new Bob_pvidContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_bob_pvid);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(325);
			match(PVID);
			setState(326);
			((Bob_pvidContext)_localctx).id = vlan_id();
			setState(327);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bob_vidsContext extends ParserRuleContext {
		public Vlan_range_setContext vlans;
		public TerminalNode VIDS() { return getToken(CumulusNcluParser.VIDS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vlan_range_setContext vlan_range_set() {
			return getRuleContext(Vlan_range_setContext.class,0);
		}
		public Bob_vidsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bob_vids; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBob_vids(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBob_vids(this);
		}
	}

	public final Bob_vidsContext bob_vids() throws RecognitionException {
		Bob_vidsContext _localctx = new Bob_vidsContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_bob_vids);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(329);
			match(VIDS);
			setState(330);
			((Bob_vidsContext)_localctx).vlans = vlan_range_set();
			setState(331);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bond_clag_idContext extends ParserRuleContext {
		public Uint16Context id;
		public TerminalNode CLAG() { return getToken(CumulusNcluParser.CLAG, 0); }
		public TerminalNode ID() { return getToken(CumulusNcluParser.ID, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Uint16Context uint16() {
			return getRuleContext(Uint16Context.class,0);
		}
		public Bond_clag_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bond_clag_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBond_clag_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBond_clag_id(this);
		}
	}

	public final Bond_clag_idContext bond_clag_id() throws RecognitionException {
		Bond_clag_idContext _localctx = new Bond_clag_idContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_bond_clag_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(333);
			match(CLAG);
			setState(334);
			match(ID);
			setState(335);
			((Bond_clag_idContext)_localctx).id = uint16();
			setState(336);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bond_ip_addressContext extends ParserRuleContext {
		public Interface_addressContext address;
		public TerminalNode IP() { return getToken(CumulusNcluParser.IP, 0); }
		public TerminalNode ADDRESS() { return getToken(CumulusNcluParser.ADDRESS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Interface_addressContext interface_address() {
			return getRuleContext(Interface_addressContext.class,0);
		}
		public Bond_ip_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bond_ip_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBond_ip_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBond_ip_address(this);
		}
	}

	public final Bond_ip_addressContext bond_ip_address() throws RecognitionException {
		Bond_ip_addressContext _localctx = new Bond_ip_addressContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_bond_ip_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(338);
			match(IP);
			setState(339);
			match(ADDRESS);
			setState(340);
			((Bond_ip_addressContext)_localctx).address = interface_address();
			setState(341);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bond_vrfContext extends ParserRuleContext {
		public WordContext name;
		public TerminalNode VRF() { return getToken(CumulusNcluParser.VRF, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Bond_vrfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bond_vrf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBond_vrf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBond_vrf(this);
		}
	}

	public final Bond_vrfContext bond_vrf() throws RecognitionException {
		Bond_vrfContext _localctx = new Bond_vrfContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_bond_vrf);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(343);
			match(VRF);
			setState(344);
			((Bond_vrfContext)_localctx).name = word();
			setState(345);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_bridgeContext extends ParserRuleContext {
		public TerminalNode BRIDGE() { return getToken(CumulusNcluParser.BRIDGE, 0); }
		public Bridge_bridgeContext bridge_bridge() {
			return getRuleContext(Bridge_bridgeContext.class,0);
		}
		public A_bridgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_bridge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_bridge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_bridge(this);
		}
	}

	public final A_bridgeContext a_bridge() throws RecognitionException {
		A_bridgeContext _localctx = new A_bridgeContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_a_bridge);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(347);
			match(BRIDGE);
			setState(348);
			bridge_bridge();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bridge_bridgeContext extends ParserRuleContext {
		public TerminalNode BRIDGE() { return getToken(CumulusNcluParser.BRIDGE, 0); }
		public Brbr_portsContext brbr_ports() {
			return getRuleContext(Brbr_portsContext.class,0);
		}
		public Brbr_pvidContext brbr_pvid() {
			return getRuleContext(Brbr_pvidContext.class,0);
		}
		public Brbr_vidsContext brbr_vids() {
			return getRuleContext(Brbr_vidsContext.class,0);
		}
		public Brbr_vlan_awareContext brbr_vlan_aware() {
			return getRuleContext(Brbr_vlan_awareContext.class,0);
		}
		public Bridge_bridgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bridge_bridge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBridge_bridge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBridge_bridge(this);
		}
	}

	public final Bridge_bridgeContext bridge_bridge() throws RecognitionException {
		Bridge_bridgeContext _localctx = new Bridge_bridgeContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_bridge_bridge);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(350);
			match(BRIDGE);
			setState(355);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PORTS:
				{
				setState(351);
				brbr_ports();
				}
				break;
			case PVID:
				{
				setState(352);
				brbr_pvid();
				}
				break;
			case VIDS:
				{
				setState(353);
				brbr_vids();
				}
				break;
			case VLAN_AWARE:
				{
				setState(354);
				brbr_vlan_aware();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Brbr_portsContext extends ParserRuleContext {
		public GlobContext ports;
		public TerminalNode PORTS() { return getToken(CumulusNcluParser.PORTS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public GlobContext glob() {
			return getRuleContext(GlobContext.class,0);
		}
		public Brbr_portsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_brbr_ports; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBrbr_ports(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBrbr_ports(this);
		}
	}

	public final Brbr_portsContext brbr_ports() throws RecognitionException {
		Brbr_portsContext _localctx = new Brbr_portsContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_brbr_ports);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(357);
			match(PORTS);
			setState(358);
			((Brbr_portsContext)_localctx).ports = glob();
			setState(359);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Brbr_pvidContext extends ParserRuleContext {
		public Vlan_idContext pvid;
		public TerminalNode PVID() { return getToken(CumulusNcluParser.PVID, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vlan_idContext vlan_id() {
			return getRuleContext(Vlan_idContext.class,0);
		}
		public Brbr_pvidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_brbr_pvid; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBrbr_pvid(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBrbr_pvid(this);
		}
	}

	public final Brbr_pvidContext brbr_pvid() throws RecognitionException {
		Brbr_pvidContext _localctx = new Brbr_pvidContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_brbr_pvid);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			match(PVID);
			setState(362);
			((Brbr_pvidContext)_localctx).pvid = vlan_id();
			setState(363);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Brbr_vidsContext extends ParserRuleContext {
		public Range_setContext ids;
		public TerminalNode VIDS() { return getToken(CumulusNcluParser.VIDS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Range_setContext range_set() {
			return getRuleContext(Range_setContext.class,0);
		}
		public Brbr_vidsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_brbr_vids; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBrbr_vids(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBrbr_vids(this);
		}
	}

	public final Brbr_vidsContext brbr_vids() throws RecognitionException {
		Brbr_vidsContext _localctx = new Brbr_vidsContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_brbr_vids);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(365);
			match(VIDS);
			setState(366);
			((Brbr_vidsContext)_localctx).ids = range_set();
			setState(367);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Brbr_vlan_awareContext extends ParserRuleContext {
		public TerminalNode VLAN_AWARE() { return getToken(CumulusNcluParser.VLAN_AWARE, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Brbr_vlan_awareContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_brbr_vlan_aware; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBrbr_vlan_aware(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBrbr_vlan_aware(this);
		}
	}

	public final Brbr_vlan_awareContext brbr_vlan_aware() throws RecognitionException {
		Brbr_vlan_awareContext _localctx = new Brbr_vlan_awareContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_brbr_vlan_aware);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(369);
			match(VLAN_AWARE);
			setState(370);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_dnsContext extends ParserRuleContext {
		public TerminalNode DNS() { return getToken(CumulusNcluParser.DNS, 0); }
		public Dns_nameserverContext dns_nameserver() {
			return getRuleContext(Dns_nameserverContext.class,0);
		}
		public A_dnsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_dns; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_dns(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_dns(this);
		}
	}

	public final A_dnsContext a_dns() throws RecognitionException {
		A_dnsContext _localctx = new A_dnsContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_a_dns);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(372);
			match(DNS);
			setState(373);
			dns_nameserver();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Dns_nameserverContext extends ParserRuleContext {
		public TerminalNode NAMESERVER() { return getToken(CumulusNcluParser.NAMESERVER, 0); }
		public Dn4Context dn4() {
			return getRuleContext(Dn4Context.class,0);
		}
		public Dn6Context dn6() {
			return getRuleContext(Dn6Context.class,0);
		}
		public Dns_nameserverContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dns_nameserver; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterDns_nameserver(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitDns_nameserver(this);
		}
	}

	public final Dns_nameserverContext dns_nameserver() throws RecognitionException {
		Dns_nameserverContext _localctx = new Dns_nameserverContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_dns_nameserver);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(375);
			match(NAMESERVER);
			setState(378);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IPV4:
				{
				setState(376);
				dn4();
				}
				break;
			case IPV6:
				{
				setState(377);
				dn6();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Dn4Context extends ParserRuleContext {
		public Ip_addressContext address;
		public TerminalNode IPV4() { return getToken(CumulusNcluParser.IPV4, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ip_addressContext ip_address() {
			return getRuleContext(Ip_addressContext.class,0);
		}
		public Dn4Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dn4; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterDn4(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitDn4(this);
		}
	}

	public final Dn4Context dn4() throws RecognitionException {
		Dn4Context _localctx = new Dn4Context(_ctx, getState());
		enterRule(_localctx, 44, RULE_dn4);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(380);
			match(IPV4);
			setState(381);
			((Dn4Context)_localctx).address = ip_address();
			setState(382);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Dn6Context extends ParserRuleContext {
		public Ipv6_addressContext address6;
		public TerminalNode IPV6() { return getToken(CumulusNcluParser.IPV6, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ipv6_addressContext ipv6_address() {
			return getRuleContext(Ipv6_addressContext.class,0);
		}
		public Dn6Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dn6; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterDn6(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitDn6(this);
		}
	}

	public final Dn6Context dn6() throws RecognitionException {
		Dn6Context _localctx = new Dn6Context(_ctx, getState());
		enterRule(_localctx, 46, RULE_dn6);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(384);
			match(IPV6);
			setState(385);
			((Dn6Context)_localctx).address6 = ipv6_address();
			setState(386);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_dot1xContext extends ParserRuleContext {
		public TerminalNode DOT1X() { return getToken(CumulusNcluParser.DOT1X, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public A_dot1xContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_dot1x; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_dot1x(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_dot1x(this);
		}
	}

	public final A_dot1xContext a_dot1x() throws RecognitionException {
		A_dot1xContext _localctx = new A_dot1xContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_a_dot1x);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(388);
			match(DOT1X);
			setState(389);
			null_rest_of_line();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_hostnameContext extends ParserRuleContext {
		public WordContext hostname;
		public TerminalNode HOSTNAME() { return getToken(CumulusNcluParser.HOSTNAME, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public A_hostnameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_hostname; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_hostname(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_hostname(this);
		}
	}

	public final A_hostnameContext a_hostname() throws RecognitionException {
		A_hostnameContext _localctx = new A_hostnameContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_a_hostname);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(391);
			match(HOSTNAME);
			setState(392);
			((A_hostnameContext)_localctx).hostname = word();
			setState(393);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_loopbackContext extends ParserRuleContext {
		public TerminalNode LOOPBACK() { return getToken(CumulusNcluParser.LOOPBACK, 0); }
		public TerminalNode LO() { return getToken(CumulusNcluParser.LO, 0); }
		public L_clagContext l_clag() {
			return getRuleContext(L_clagContext.class,0);
		}
		public L_ip_addressContext l_ip_address() {
			return getRuleContext(L_ip_addressContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public A_loopbackContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_loopback; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_loopback(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_loopback(this);
		}
	}

	public final A_loopbackContext a_loopback() throws RecognitionException {
		A_loopbackContext _localctx = new A_loopbackContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_a_loopback);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(395);
			match(LOOPBACK);
			setState(396);
			match(LO);
			setState(400);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CLAG:
				{
				setState(397);
				l_clag();
				}
				break;
			case IP:
				{
				setState(398);
				l_ip_address();
				}
				break;
			case NEWLINE:
				{
				setState(399);
				match(NEWLINE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class L_clagContext extends ParserRuleContext {
		public TerminalNode CLAG() { return getToken(CumulusNcluParser.CLAG, 0); }
		public Lc_vxlan_anycast_ipContext lc_vxlan_anycast_ip() {
			return getRuleContext(Lc_vxlan_anycast_ipContext.class,0);
		}
		public L_clagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_l_clag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterL_clag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitL_clag(this);
		}
	}

	public final L_clagContext l_clag() throws RecognitionException {
		L_clagContext _localctx = new L_clagContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_l_clag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(402);
			match(CLAG);
			setState(403);
			lc_vxlan_anycast_ip();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Lc_vxlan_anycast_ipContext extends ParserRuleContext {
		public Ip_addressContext ip;
		public TerminalNode VXLAN_ANYCAST_IP() { return getToken(CumulusNcluParser.VXLAN_ANYCAST_IP, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ip_addressContext ip_address() {
			return getRuleContext(Ip_addressContext.class,0);
		}
		public Lc_vxlan_anycast_ipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lc_vxlan_anycast_ip; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterLc_vxlan_anycast_ip(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitLc_vxlan_anycast_ip(this);
		}
	}

	public final Lc_vxlan_anycast_ipContext lc_vxlan_anycast_ip() throws RecognitionException {
		Lc_vxlan_anycast_ipContext _localctx = new Lc_vxlan_anycast_ipContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_lc_vxlan_anycast_ip);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			match(VXLAN_ANYCAST_IP);
			setState(406);
			((Lc_vxlan_anycast_ipContext)_localctx).ip = ip_address();
			setState(407);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class L_ip_addressContext extends ParserRuleContext {
		public Interface_addressContext address;
		public TerminalNode IP() { return getToken(CumulusNcluParser.IP, 0); }
		public TerminalNode ADDRESS() { return getToken(CumulusNcluParser.ADDRESS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Interface_addressContext interface_address() {
			return getRuleContext(Interface_addressContext.class,0);
		}
		public L_ip_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_l_ip_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterL_ip_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitL_ip_address(this);
		}
	}

	public final L_ip_addressContext l_ip_address() throws RecognitionException {
		L_ip_addressContext _localctx = new L_ip_addressContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_l_ip_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(409);
			match(IP);
			setState(410);
			match(ADDRESS);
			setState(411);
			((L_ip_addressContext)_localctx).address = interface_address();
			setState(412);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_ptpContext extends ParserRuleContext {
		public TerminalNode PTP() { return getToken(CumulusNcluParser.PTP, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public A_ptpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_ptp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_ptp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_ptp(this);
		}
	}

	public final A_ptpContext a_ptp() throws RecognitionException {
		A_ptpContext _localctx = new A_ptpContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_a_ptp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(414);
			match(PTP);
			setState(415);
			null_rest_of_line();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_snmp_serverContext extends ParserRuleContext {
		public TerminalNode SNMP_SERVER() { return getToken(CumulusNcluParser.SNMP_SERVER, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public A_snmp_serverContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_snmp_server; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_snmp_server(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_snmp_server(this);
		}
	}

	public final A_snmp_serverContext a_snmp_server() throws RecognitionException {
		A_snmp_serverContext _localctx = new A_snmp_serverContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_a_snmp_server);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(417);
			match(SNMP_SERVER);
			setState(418);
			null_rest_of_line();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_timeContext extends ParserRuleContext {
		public TerminalNode TIME() { return getToken(CumulusNcluParser.TIME, 0); }
		public T_ntpContext t_ntp() {
			return getRuleContext(T_ntpContext.class,0);
		}
		public T_zoneContext t_zone() {
			return getRuleContext(T_zoneContext.class,0);
		}
		public A_timeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_time; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_time(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_time(this);
		}
	}

	public final A_timeContext a_time() throws RecognitionException {
		A_timeContext _localctx = new A_timeContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_a_time);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(420);
			match(TIME);
			setState(423);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NTP:
				{
				setState(421);
				t_ntp();
				}
				break;
			case ZONE:
				{
				setState(422);
				t_zone();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class T_ntpContext extends ParserRuleContext {
		public TerminalNode NTP() { return getToken(CumulusNcluParser.NTP, 0); }
		public Tn_serverContext tn_server() {
			return getRuleContext(Tn_serverContext.class,0);
		}
		public Tn_sourceContext tn_source() {
			return getRuleContext(Tn_sourceContext.class,0);
		}
		public T_ntpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_t_ntp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterT_ntp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitT_ntp(this);
		}
	}

	public final T_ntpContext t_ntp() throws RecognitionException {
		T_ntpContext _localctx = new T_ntpContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_t_ntp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(425);
			match(NTP);
			setState(428);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SERVER:
				{
				setState(426);
				tn_server();
				}
				break;
			case SOURCE:
				{
				setState(427);
				tn_source();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Tn_serverContext extends ParserRuleContext {
		public WordContext server;
		public TerminalNode SERVER() { return getToken(CumulusNcluParser.SERVER, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public TerminalNode IBURST() { return getToken(CumulusNcluParser.IBURST, 0); }
		public Tn_serverContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tn_server; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterTn_server(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitTn_server(this);
		}
	}

	public final Tn_serverContext tn_server() throws RecognitionException {
		Tn_serverContext _localctx = new Tn_serverContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_tn_server);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			match(SERVER);
			setState(431);
			((Tn_serverContext)_localctx).server = word();
			setState(433);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IBURST) {
				{
				setState(432);
				match(IBURST);
				}
			}

			setState(435);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Tn_sourceContext extends ParserRuleContext {
		public WordContext source;
		public TerminalNode SOURCE() { return getToken(CumulusNcluParser.SOURCE, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Tn_sourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tn_source; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterTn_source(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitTn_source(this);
		}
	}

	public final Tn_sourceContext tn_source() throws RecognitionException {
		Tn_sourceContext _localctx = new Tn_sourceContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_tn_source);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(437);
			match(SOURCE);
			setState(438);
			((Tn_sourceContext)_localctx).source = word();
			setState(439);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class T_zoneContext extends ParserRuleContext {
		public WordContext zone;
		public TerminalNode ZONE() { return getToken(CumulusNcluParser.ZONE, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public T_zoneContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_t_zone; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterT_zone(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitT_zone(this);
		}
	}

	public final T_zoneContext t_zone() throws RecognitionException {
		T_zoneContext _localctx = new T_zoneContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_t_zone);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(441);
			match(ZONE);
			setState(442);
			((T_zoneContext)_localctx).zone = word();
			setState(443);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_vlanContext extends ParserRuleContext {
		public Uint16Context suffix;
		public Range_setContext suffixes;
		public TerminalNode VLAN() { return getToken(CumulusNcluParser.VLAN, 0); }
		public V_vlan_idContext v_vlan_id() {
			return getRuleContext(V_vlan_idContext.class,0);
		}
		public Uint16Context uint16() {
			return getRuleContext(Uint16Context.class,0);
		}
		public Range_setContext range_set() {
			return getRuleContext(Range_setContext.class,0);
		}
		public V_ip_addressContext v_ip_address() {
			return getRuleContext(V_ip_addressContext.class,0);
		}
		public V_ip_address_virtualContext v_ip_address_virtual() {
			return getRuleContext(V_ip_address_virtualContext.class,0);
		}
		public V_vlan_raw_deviceContext v_vlan_raw_device() {
			return getRuleContext(V_vlan_raw_deviceContext.class,0);
		}
		public V_vrfContext v_vrf() {
			return getRuleContext(V_vrfContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public A_vlanContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_vlan; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_vlan(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_vlan(this);
		}
	}

	public final A_vlanContext a_vlan() throws RecognitionException {
		A_vlanContext _localctx = new A_vlanContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_a_vlan);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(445);
			match(VLAN);
			setState(457);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				{
				setState(446);
				((A_vlanContext)_localctx).suffix = uint16();
				setState(447);
				v_vlan_id();
				}
				}
				break;
			case 2:
				{
				{
				setState(449);
				((A_vlanContext)_localctx).suffixes = range_set();
				setState(455);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
				case 1:
					{
					setState(450);
					v_ip_address();
					}
					break;
				case 2:
					{
					setState(451);
					v_ip_address_virtual();
					}
					break;
				case 3:
					{
					setState(452);
					v_vlan_raw_device();
					}
					break;
				case 4:
					{
					setState(453);
					v_vrf();
					}
					break;
				case 5:
					{
					setState(454);
					match(NEWLINE);
					}
					break;
				}
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class V_ip_addressContext extends ParserRuleContext {
		public Interface_addressContext address;
		public TerminalNode IP() { return getToken(CumulusNcluParser.IP, 0); }
		public TerminalNode ADDRESS() { return getToken(CumulusNcluParser.ADDRESS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Interface_addressContext interface_address() {
			return getRuleContext(Interface_addressContext.class,0);
		}
		public V_ip_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_v_ip_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterV_ip_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitV_ip_address(this);
		}
	}

	public final V_ip_addressContext v_ip_address() throws RecognitionException {
		V_ip_addressContext _localctx = new V_ip_addressContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_v_ip_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(459);
			match(IP);
			setState(460);
			match(ADDRESS);
			setState(461);
			((V_ip_addressContext)_localctx).address = interface_address();
			setState(462);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class V_ip_address_virtualContext extends ParserRuleContext {
		public Mac_addressContext mac;
		public Interface_addressContext address;
		public TerminalNode IP() { return getToken(CumulusNcluParser.IP, 0); }
		public TerminalNode ADDRESS_VIRTUAL() { return getToken(CumulusNcluParser.ADDRESS_VIRTUAL, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Mac_addressContext mac_address() {
			return getRuleContext(Mac_addressContext.class,0);
		}
		public Interface_addressContext interface_address() {
			return getRuleContext(Interface_addressContext.class,0);
		}
		public V_ip_address_virtualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_v_ip_address_virtual; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterV_ip_address_virtual(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitV_ip_address_virtual(this);
		}
	}

	public final V_ip_address_virtualContext v_ip_address_virtual() throws RecognitionException {
		V_ip_address_virtualContext _localctx = new V_ip_address_virtualContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_v_ip_address_virtual);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(464);
			match(IP);
			setState(465);
			match(ADDRESS_VIRTUAL);
			setState(466);
			((V_ip_address_virtualContext)_localctx).mac = mac_address();
			setState(467);
			((V_ip_address_virtualContext)_localctx).address = interface_address();
			setState(468);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class V_vlan_idContext extends ParserRuleContext {
		public Vlan_idContext id;
		public TerminalNode VLAN_ID() { return getToken(CumulusNcluParser.VLAN_ID, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vlan_idContext vlan_id() {
			return getRuleContext(Vlan_idContext.class,0);
		}
		public V_vlan_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_v_vlan_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterV_vlan_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitV_vlan_id(this);
		}
	}

	public final V_vlan_idContext v_vlan_id() throws RecognitionException {
		V_vlan_idContext _localctx = new V_vlan_idContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_v_vlan_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(470);
			match(VLAN_ID);
			setState(471);
			((V_vlan_idContext)_localctx).id = vlan_id();
			setState(472);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class V_vlan_raw_deviceContext extends ParserRuleContext {
		public Token device;
		public TerminalNode VLAN_RAW_DEVICE() { return getToken(CumulusNcluParser.VLAN_RAW_DEVICE, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public TerminalNode BRIDGE() { return getToken(CumulusNcluParser.BRIDGE, 0); }
		public V_vlan_raw_deviceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_v_vlan_raw_device; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterV_vlan_raw_device(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitV_vlan_raw_device(this);
		}
	}

	public final V_vlan_raw_deviceContext v_vlan_raw_device() throws RecognitionException {
		V_vlan_raw_deviceContext _localctx = new V_vlan_raw_deviceContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_v_vlan_raw_device);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(474);
			match(VLAN_RAW_DEVICE);
			setState(475);
			((V_vlan_raw_deviceContext)_localctx).device = match(BRIDGE);
			setState(476);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class V_vrfContext extends ParserRuleContext {
		public WordContext name;
		public TerminalNode VRF() { return getToken(CumulusNcluParser.VRF, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public V_vrfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_v_vrf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterV_vrf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitV_vrf(this);
		}
	}

	public final V_vrfContext v_vrf() throws RecognitionException {
		V_vrfContext _localctx = new V_vrfContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_v_vrf);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(478);
			match(VRF);
			setState(479);
			((V_vrfContext)_localctx).name = word();
			setState(480);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_vrfContext extends ParserRuleContext {
		public GlobContext names;
		public TerminalNode VRF() { return getToken(CumulusNcluParser.VRF, 0); }
		public GlobContext glob() {
			return getRuleContext(GlobContext.class,0);
		}
		public Vrf_ip_addressContext vrf_ip_address() {
			return getRuleContext(Vrf_ip_addressContext.class,0);
		}
		public Vrf_vniContext vrf_vni() {
			return getRuleContext(Vrf_vniContext.class,0);
		}
		public Vrf_vrf_tableContext vrf_vrf_table() {
			return getRuleContext(Vrf_vrf_tableContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public A_vrfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_vrf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_vrf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_vrf(this);
		}
	}

	public final A_vrfContext a_vrf() throws RecognitionException {
		A_vrfContext _localctx = new A_vrfContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_a_vrf);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(482);
			match(VRF);
			setState(483);
			((A_vrfContext)_localctx).names = glob();
			setState(488);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IP:
				{
				setState(484);
				vrf_ip_address();
				}
				break;
			case VNI:
				{
				setState(485);
				vrf_vni();
				}
				break;
			case VRF_TABLE:
				{
				setState(486);
				vrf_vrf_table();
				}
				break;
			case NEWLINE:
				{
				setState(487);
				match(NEWLINE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vrf_ip_addressContext extends ParserRuleContext {
		public Interface_addressContext address;
		public TerminalNode IP() { return getToken(CumulusNcluParser.IP, 0); }
		public TerminalNode ADDRESS() { return getToken(CumulusNcluParser.ADDRESS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Interface_addressContext interface_address() {
			return getRuleContext(Interface_addressContext.class,0);
		}
		public Vrf_ip_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vrf_ip_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVrf_ip_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVrf_ip_address(this);
		}
	}

	public final Vrf_ip_addressContext vrf_ip_address() throws RecognitionException {
		Vrf_ip_addressContext _localctx = new Vrf_ip_addressContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_vrf_ip_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(490);
			match(IP);
			setState(491);
			match(ADDRESS);
			setState(492);
			((Vrf_ip_addressContext)_localctx).address = interface_address();
			setState(493);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vrf_vniContext extends ParserRuleContext {
		public Vni_numberContext vni;
		public TerminalNode VNI() { return getToken(CumulusNcluParser.VNI, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vni_numberContext vni_number() {
			return getRuleContext(Vni_numberContext.class,0);
		}
		public Vrf_vniContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vrf_vni; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVrf_vni(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVrf_vni(this);
		}
	}

	public final Vrf_vniContext vrf_vni() throws RecognitionException {
		Vrf_vniContext _localctx = new Vrf_vniContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_vrf_vni);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(495);
			match(VNI);
			setState(496);
			((Vrf_vniContext)_localctx).vni = vni_number();
			setState(497);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vrf_vrf_tableContext extends ParserRuleContext {
		public TerminalNode VRF_TABLE() { return getToken(CumulusNcluParser.VRF_TABLE, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public TerminalNode AUTO() { return getToken(CumulusNcluParser.AUTO, 0); }
		public TerminalNode DEC() { return getToken(CumulusNcluParser.DEC, 0); }
		public Vrf_vrf_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vrf_vrf_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVrf_vrf_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVrf_vrf_table(this);
		}
	}

	public final Vrf_vrf_tableContext vrf_vrf_table() throws RecognitionException {
		Vrf_vrf_tableContext _localctx = new Vrf_vrf_tableContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_vrf_vrf_table);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(499);
			match(VRF_TABLE);
			setState(500);
			_la = _input.LA(1);
			if ( !(_la==AUTO || _la==DEC) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(501);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_vxlanContext extends ParserRuleContext {
		public GlobContext names;
		public TerminalNode VXLAN() { return getToken(CumulusNcluParser.VXLAN, 0); }
		public GlobContext glob() {
			return getRuleContext(GlobContext.class,0);
		}
		public Vx_bridgeContext vx_bridge() {
			return getRuleContext(Vx_bridgeContext.class,0);
		}
		public Vx_stpContext vx_stp() {
			return getRuleContext(Vx_stpContext.class,0);
		}
		public Vx_vxlanContext vx_vxlan() {
			return getRuleContext(Vx_vxlanContext.class,0);
		}
		public A_vxlanContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_vxlan; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_vxlan(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_vxlan(this);
		}
	}

	public final A_vxlanContext a_vxlan() throws RecognitionException {
		A_vxlanContext _localctx = new A_vxlanContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_a_vxlan);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(503);
			match(VXLAN);
			setState(504);
			((A_vxlanContext)_localctx).names = glob();
			setState(508);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BRIDGE:
				{
				setState(505);
				vx_bridge();
				}
				break;
			case STP:
				{
				setState(506);
				vx_stp();
				}
				break;
			case VXLAN:
				{
				setState(507);
				vx_vxlan();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vx_bridgeContext extends ParserRuleContext {
		public TerminalNode BRIDGE() { return getToken(CumulusNcluParser.BRIDGE, 0); }
		public Vxb_accessContext vxb_access() {
			return getRuleContext(Vxb_accessContext.class,0);
		}
		public Vxb_arp_nd_suppressContext vxb_arp_nd_suppress() {
			return getRuleContext(Vxb_arp_nd_suppressContext.class,0);
		}
		public Vxb_learningContext vxb_learning() {
			return getRuleContext(Vxb_learningContext.class,0);
		}
		public Vx_bridgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vx_bridge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVx_bridge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVx_bridge(this);
		}
	}

	public final Vx_bridgeContext vx_bridge() throws RecognitionException {
		Vx_bridgeContext _localctx = new Vx_bridgeContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_vx_bridge);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(510);
			match(BRIDGE);
			setState(514);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ACCESS:
				{
				setState(511);
				vxb_access();
				}
				break;
			case ARP_ND_SUPPRESS:
				{
				setState(512);
				vxb_arp_nd_suppress();
				}
				break;
			case LEARNING:
				{
				setState(513);
				vxb_learning();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vxb_accessContext extends ParserRuleContext {
		public Vlan_idContext vlan;
		public TerminalNode ACCESS() { return getToken(CumulusNcluParser.ACCESS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vlan_idContext vlan_id() {
			return getRuleContext(Vlan_idContext.class,0);
		}
		public Vxb_accessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vxb_access; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVxb_access(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVxb_access(this);
		}
	}

	public final Vxb_accessContext vxb_access() throws RecognitionException {
		Vxb_accessContext _localctx = new Vxb_accessContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_vxb_access);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			match(ACCESS);
			setState(517);
			((Vxb_accessContext)_localctx).vlan = vlan_id();
			setState(518);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vxb_arp_nd_suppressContext extends ParserRuleContext {
		public TerminalNode ARP_ND_SUPPRESS() { return getToken(CumulusNcluParser.ARP_ND_SUPPRESS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public TerminalNode OFF() { return getToken(CumulusNcluParser.OFF, 0); }
		public TerminalNode ON() { return getToken(CumulusNcluParser.ON, 0); }
		public Vxb_arp_nd_suppressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vxb_arp_nd_suppress; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVxb_arp_nd_suppress(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVxb_arp_nd_suppress(this);
		}
	}

	public final Vxb_arp_nd_suppressContext vxb_arp_nd_suppress() throws RecognitionException {
		Vxb_arp_nd_suppressContext _localctx = new Vxb_arp_nd_suppressContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_vxb_arp_nd_suppress);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(520);
			match(ARP_ND_SUPPRESS);
			setState(521);
			_la = _input.LA(1);
			if ( !(_la==OFF || _la==ON) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(522);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vxb_learningContext extends ParserRuleContext {
		public TerminalNode LEARNING() { return getToken(CumulusNcluParser.LEARNING, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public TerminalNode OFF() { return getToken(CumulusNcluParser.OFF, 0); }
		public TerminalNode ON() { return getToken(CumulusNcluParser.ON, 0); }
		public Vxb_learningContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vxb_learning; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVxb_learning(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVxb_learning(this);
		}
	}

	public final Vxb_learningContext vxb_learning() throws RecognitionException {
		Vxb_learningContext _localctx = new Vxb_learningContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_vxb_learning);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(524);
			match(LEARNING);
			setState(525);
			_la = _input.LA(1);
			if ( !(_la==OFF || _la==ON) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(526);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vx_stpContext extends ParserRuleContext {
		public TerminalNode STP() { return getToken(CumulusNcluParser.STP, 0); }
		public Vxs_bpduguardContext vxs_bpduguard() {
			return getRuleContext(Vxs_bpduguardContext.class,0);
		}
		public Vxs_portbpdufilterContext vxs_portbpdufilter() {
			return getRuleContext(Vxs_portbpdufilterContext.class,0);
		}
		public Vx_stpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vx_stp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVx_stp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVx_stp(this);
		}
	}

	public final Vx_stpContext vx_stp() throws RecognitionException {
		Vx_stpContext _localctx = new Vx_stpContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_vx_stp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(528);
			match(STP);
			setState(531);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BPDUGUARD:
				{
				setState(529);
				vxs_bpduguard();
				}
				break;
			case PORTBPDUFILTER:
				{
				setState(530);
				vxs_portbpdufilter();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vxs_bpduguardContext extends ParserRuleContext {
		public TerminalNode BPDUGUARD() { return getToken(CumulusNcluParser.BPDUGUARD, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vxs_bpduguardContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vxs_bpduguard; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVxs_bpduguard(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVxs_bpduguard(this);
		}
	}

	public final Vxs_bpduguardContext vxs_bpduguard() throws RecognitionException {
		Vxs_bpduguardContext _localctx = new Vxs_bpduguardContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_vxs_bpduguard);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(533);
			match(BPDUGUARD);
			setState(534);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vxs_portbpdufilterContext extends ParserRuleContext {
		public TerminalNode PORTBPDUFILTER() { return getToken(CumulusNcluParser.PORTBPDUFILTER, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vxs_portbpdufilterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vxs_portbpdufilter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVxs_portbpdufilter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVxs_portbpdufilter(this);
		}
	}

	public final Vxs_portbpdufilterContext vxs_portbpdufilter() throws RecognitionException {
		Vxs_portbpdufilterContext _localctx = new Vxs_portbpdufilterContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_vxs_portbpdufilter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(536);
			match(PORTBPDUFILTER);
			setState(537);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vx_vxlanContext extends ParserRuleContext {
		public TerminalNode VXLAN() { return getToken(CumulusNcluParser.VXLAN, 0); }
		public Vxv_idContext vxv_id() {
			return getRuleContext(Vxv_idContext.class,0);
		}
		public Vxv_local_tunnelipContext vxv_local_tunnelip() {
			return getRuleContext(Vxv_local_tunnelipContext.class,0);
		}
		public Vx_vxlanContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vx_vxlan; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVx_vxlan(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVx_vxlan(this);
		}
	}

	public final Vx_vxlanContext vx_vxlan() throws RecognitionException {
		Vx_vxlanContext _localctx = new Vx_vxlanContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_vx_vxlan);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(539);
			match(VXLAN);
			setState(542);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				{
				setState(540);
				vxv_id();
				}
				break;
			case LOCAL_TUNNELIP:
				{
				setState(541);
				vxv_local_tunnelip();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vxv_idContext extends ParserRuleContext {
		public Vni_numberContext vni;
		public TerminalNode ID() { return getToken(CumulusNcluParser.ID, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vni_numberContext vni_number() {
			return getRuleContext(Vni_numberContext.class,0);
		}
		public Vxv_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vxv_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVxv_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVxv_id(this);
		}
	}

	public final Vxv_idContext vxv_id() throws RecognitionException {
		Vxv_idContext _localctx = new Vxv_idContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_vxv_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(544);
			match(ID);
			setState(545);
			((Vxv_idContext)_localctx).vni = vni_number();
			setState(546);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vxv_local_tunnelipContext extends ParserRuleContext {
		public Ip_addressContext ip;
		public TerminalNode LOCAL_TUNNELIP() { return getToken(CumulusNcluParser.LOCAL_TUNNELIP, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ip_addressContext ip_address() {
			return getRuleContext(Ip_addressContext.class,0);
		}
		public Vxv_local_tunnelipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vxv_local_tunnelip; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVxv_local_tunnelip(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVxv_local_tunnelip(this);
		}
	}

	public final Vxv_local_tunnelipContext vxv_local_tunnelip() throws RecognitionException {
		Vxv_local_tunnelipContext _localctx = new Vxv_local_tunnelipContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_vxv_local_tunnelip);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(548);
			match(LOCAL_TUNNELIP);
			setState(549);
			((Vxv_local_tunnelipContext)_localctx).ip = ip_address();
			setState(550);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class S_net_add_unrecognizedContext extends ParserRuleContext {
		public TerminalNode NET() { return getToken(CumulusNcluParser.NET, 0); }
		public TerminalNode ADD() { return getToken(CumulusNcluParser.ADD, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public S_net_add_unrecognizedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_net_add_unrecognized; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterS_net_add_unrecognized(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitS_net_add_unrecognized(this);
		}
	}

	public final S_net_add_unrecognizedContext s_net_add_unrecognized() throws RecognitionException {
		S_net_add_unrecognizedContext _localctx = new S_net_add_unrecognizedContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_s_net_add_unrecognized);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(552);
			match(NET);
			setState(553);
			match(ADD);
			setState(554);
			word();
			setState(555);
			null_rest_of_line();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class S_nullContext extends ParserRuleContext {
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public TerminalNode NET() { return getToken(CumulusNcluParser.NET, 0); }
		public TerminalNode COMMIT() { return getToken(CumulusNcluParser.COMMIT, 0); }
		public TerminalNode DEL() { return getToken(CumulusNcluParser.DEL, 0); }
		public S_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterS_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitS_null(this);
		}
	}

	public final S_nullContext s_null() throws RecognitionException {
		S_nullContext _localctx = new S_nullContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_s_null);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(557);
			match(NET);
			setState(561);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				{
				}
				break;
			case 2:
				{
				setState(559);
				match(COMMIT);
				}
				break;
			case 3:
				{
				setState(560);
				match(DEL);
				}
				break;
			}
			}
			setState(563);
			null_rest_of_line();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GlobContext extends ParserRuleContext {
		public List<Glob_range_setContext> glob_range_set() {
			return getRuleContexts(Glob_range_setContext.class);
		}
		public Glob_range_setContext glob_range_set(int i) {
			return getRuleContext(Glob_range_setContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CumulusNcluParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CumulusNcluParser.COMMA, i);
		}
		public GlobContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_glob; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterGlob(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitGlob(this);
		}
	}

	public final GlobContext glob() throws RecognitionException {
		GlobContext _localctx = new GlobContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_glob);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(565);
			glob_range_set();
			setState(570);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(566);
				match(COMMA);
				setState(567);
				glob_range_set();
				}
				}
				setState(572);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Glob_range_setContext extends ParserRuleContext {
		public Glob_wordContext unnumbered;
		public Numbered_wordContext base_word;
		public Uint16Context first_interval_end;
		public Range_setContext other_numeric_ranges;
		public Glob_wordContext glob_word() {
			return getRuleContext(Glob_wordContext.class,0);
		}
		public Numbered_wordContext numbered_word() {
			return getRuleContext(Numbered_wordContext.class,0);
		}
		public TerminalNode DASH() { return getToken(CumulusNcluParser.DASH, 0); }
		public TerminalNode COMMA() { return getToken(CumulusNcluParser.COMMA, 0); }
		public Uint16Context uint16() {
			return getRuleContext(Uint16Context.class,0);
		}
		public Range_setContext range_set() {
			return getRuleContext(Range_setContext.class,0);
		}
		public Glob_range_setContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_glob_range_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterGlob_range_set(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitGlob_range_set(this);
		}
	}

	public final Glob_range_setContext glob_range_set() throws RecognitionException {
		Glob_range_setContext _localctx = new Glob_range_setContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_glob_range_set);
		int _la;
		try {
			setState(583);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXTRA_CONFIGURATION_FOOTER:
			case USERNAME:
			case ACCESS:
			case ACTIVATE:
			case ADD:
			case ADDRESS:
			case ADDRESS_VIRTUAL:
			case ADVERTISE:
			case ADVERTISE_ALL_VNI:
			case ADVERTISE_DEFAULT_GW:
			case ALERTS:
			case ARP_ND_SUPPRESS:
			case AUTO:
			case AUTONOMOUS_SYSTEM:
			case BACKUP_IP:
			case BGP:
			case BOND:
			case BPDUGUARD:
			case BRIDGE:
			case CLAG:
			case COMMIT:
			case CONNECTED:
			case CRITICAL:
			case DATACENTER:
			case DEBUGGING:
			case DEFAULTS:
			case DEL:
			case DENY:
			case DNS:
			case DOT1X:
			case EMERGENCIES:
			case ERRORS:
			case EVPN:
			case EXTERNAL:
			case HOSTNAME:
			case IBURST:
			case ID:
			case INFORMATIONAL:
			case INTEGRATED_VTYSH_CONFIG:
			case INTERFACE:
			case INTERNAL:
			case IP:
			case IPV4:
			case IPV6:
			case L2VPN:
			case LEARNING:
			case LO:
			case LOCAL_TUNNELIP:
			case LOG:
			case LOOPBACK:
			case MATCH:
			case NAMESERVER:
			case NEIGHBOR:
			case NETWORK:
			case NET:
			case NOTIFICATIONS:
			case NTP:
			case OFF:
			case ON:
			case PEER_IP:
			case PERMIT:
			case PORTBPDUFILTER:
			case PORTS:
			case PRIORITY:
			case PTP:
			case PVID:
			case REDISTRIBUTE:
			case REMOTE_AS:
			case ROUTE:
			case ROUTE_MAP:
			case ROUTER_ID:
			case ROUTING:
			case SERVER:
			case SERVICE:
			case SLAVES:
			case SNMP_SERVER:
			case SOURCE:
			case STATIC:
			case STP:
			case SYS_MAC:
			case SYSLOG:
			case TIME:
			case UNICAST:
			case VIDS:
			case VLAN:
			case VLAN_AWARE:
			case VLAN_ID:
			case VLAN_RAW_DEVICE:
			case VNI:
			case VRF:
			case VRF_TABLE:
			case VXLAN:
			case VXLAN_ANYCAST_IP:
			case WARNINGS:
			case ZONE:
			case EXTRA_CONFIGURATION_HEADER:
			case COMMA:
			case COMMENT_LINE:
			case COMMENT_TAIL:
			case DASH:
			case IP_ADDRESS:
			case IP_PREFIX:
			case IPV6_ADDRESS:
			case IPV6_PREFIX:
			case MAC_ADDRESS:
			case WORD:
			case WS:
			case M_Printf_WS:
			case M_Printf_EXTRA_CONFIGURATION_FOOTER:
			case M_Printf_NEWLINE:
			case M_Printf_USERNAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(573);
				((Glob_range_setContext)_localctx).unnumbered = glob_word();
				}
				break;
			case NUMBERED_WORD:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(574);
				((Glob_range_setContext)_localctx).base_word = numbered_word();
				setState(577);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DASH) {
					{
					setState(575);
					match(DASH);
					setState(576);
					((Glob_range_setContext)_localctx).first_interval_end = uint16();
					}
				}

				setState(581);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(579);
					match(COMMA);
					setState(580);
					((Glob_range_setContext)_localctx).other_numeric_ranges = range_set();
					}
					break;
				}
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Glob_wordContext extends ParserRuleContext {
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public TerminalNode NUMBERED_WORD() { return getToken(CumulusNcluParser.NUMBERED_WORD, 0); }
		public TerminalNode DEC() { return getToken(CumulusNcluParser.DEC, 0); }
		public Glob_wordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_glob_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterGlob_word(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitGlob_word(this);
		}
	}

	public final Glob_wordContext glob_word() throws RecognitionException {
		Glob_wordContext _localctx = new Glob_wordContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_glob_word);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(585);
			_la = _input.LA(1);
			if ( _la <= 0 || (((((_la - 101)) & ~0x3f) == 0 && ((1L << (_la - 101)) & ((1L << (DEC - 101)) | (1L << (NEWLINE - 101)) | (1L << (NUMBERED_WORD - 101)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Interface_addressContext extends ParserRuleContext {
		public TerminalNode IP_PREFIX() { return getToken(CumulusNcluParser.IP_PREFIX, 0); }
		public Interface_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interface_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterInterface_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitInterface_address(this);
		}
	}

	public final Interface_addressContext interface_address() throws RecognitionException {
		Interface_addressContext _localctx = new Interface_addressContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_interface_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(587);
			match(IP_PREFIX);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ip_addressContext extends ParserRuleContext {
		public TerminalNode IP_ADDRESS() { return getToken(CumulusNcluParser.IP_ADDRESS, 0); }
		public Ip_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ip_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIp_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIp_address(this);
		}
	}

	public final Ip_addressContext ip_address() throws RecognitionException {
		Ip_addressContext _localctx = new Ip_addressContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_ip_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(589);
			match(IP_ADDRESS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ip_prefixContext extends ParserRuleContext {
		public TerminalNode IP_PREFIX() { return getToken(CumulusNcluParser.IP_PREFIX, 0); }
		public Ip_prefixContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ip_prefix; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIp_prefix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIp_prefix(this);
		}
	}

	public final Ip_prefixContext ip_prefix() throws RecognitionException {
		Ip_prefixContext _localctx = new Ip_prefixContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_ip_prefix);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(591);
			match(IP_PREFIX);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ipv6_addressContext extends ParserRuleContext {
		public TerminalNode IPV6_ADDRESS() { return getToken(CumulusNcluParser.IPV6_ADDRESS, 0); }
		public Ipv6_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ipv6_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIpv6_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIpv6_address(this);
		}
	}

	public final Ipv6_addressContext ipv6_address() throws RecognitionException {
		Ipv6_addressContext _localctx = new Ipv6_addressContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_ipv6_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(593);
			match(IPV6_ADDRESS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Line_actionContext extends ParserRuleContext {
		public TerminalNode DENY() { return getToken(CumulusNcluParser.DENY, 0); }
		public TerminalNode PERMIT() { return getToken(CumulusNcluParser.PERMIT, 0); }
		public Line_actionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_line_action; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterLine_action(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitLine_action(this);
		}
	}

	public final Line_actionContext line_action() throws RecognitionException {
		Line_actionContext _localctx = new Line_actionContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_line_action);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(595);
			_la = _input.LA(1);
			if ( !(_la==DENY || _la==PERMIT) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Mac_addressContext extends ParserRuleContext {
		public TerminalNode MAC_ADDRESS() { return getToken(CumulusNcluParser.MAC_ADDRESS, 0); }
		public Mac_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mac_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterMac_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitMac_address(this);
		}
	}

	public final Mac_addressContext mac_address() throws RecognitionException {
		Mac_addressContext _localctx = new Mac_addressContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_mac_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(597);
			match(MAC_ADDRESS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Null_rest_of_lineContext extends ParserRuleContext {
		public List<TerminalNode> NEWLINE() { return getTokens(CumulusNcluParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CumulusNcluParser.NEWLINE, i);
		}
		public Null_rest_of_lineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_null_rest_of_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterNull_rest_of_line(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitNull_rest_of_line(this);
		}
	}

	public final Null_rest_of_lineContext null_rest_of_line() throws RecognitionException {
		Null_rest_of_lineContext _localctx = new Null_rest_of_lineContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_null_rest_of_line);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(602);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << EXTRA_CONFIGURATION_FOOTER) | (1L << USERNAME) | (1L << ACCESS) | (1L << ACTIVATE) | (1L << ADD) | (1L << ADDRESS) | (1L << ADDRESS_VIRTUAL) | (1L << ADVERTISE) | (1L << ADVERTISE_ALL_VNI) | (1L << ADVERTISE_DEFAULT_GW) | (1L << ALERTS) | (1L << ARP_ND_SUPPRESS) | (1L << AUTO) | (1L << AUTONOMOUS_SYSTEM) | (1L << BACKUP_IP) | (1L << BGP) | (1L << BOND) | (1L << BPDUGUARD) | (1L << BRIDGE) | (1L << CLAG) | (1L << COMMIT) | (1L << CONNECTED) | (1L << CRITICAL) | (1L << DATACENTER) | (1L << DEBUGGING) | (1L << DEFAULTS) | (1L << DEL) | (1L << DENY) | (1L << DNS) | (1L << DOT1X) | (1L << EMERGENCIES) | (1L << ERRORS) | (1L << EVPN) | (1L << EXTERNAL) | (1L << HOSTNAME) | (1L << IBURST) | (1L << ID) | (1L << INFORMATIONAL) | (1L << INTEGRATED_VTYSH_CONFIG) | (1L << INTERFACE) | (1L << INTERNAL) | (1L << IP) | (1L << IPV4) | (1L << IPV6) | (1L << L2VPN) | (1L << LEARNING) | (1L << LO) | (1L << LOCAL_TUNNELIP) | (1L << LOG) | (1L << LOOPBACK) | (1L << MATCH) | (1L << NAMESERVER) | (1L << NEIGHBOR) | (1L << NETWORK) | (1L << NET) | (1L << NOTIFICATIONS) | (1L << NTP) | (1L << OFF) | (1L << ON) | (1L << PEER_IP) | (1L << PERMIT) | (1L << PORTBPDUFILTER) | (1L << PORTS))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (PRIORITY - 64)) | (1L << (PTP - 64)) | (1L << (PVID - 64)) | (1L << (REDISTRIBUTE - 64)) | (1L << (REMOTE_AS - 64)) | (1L << (ROUTE - 64)) | (1L << (ROUTE_MAP - 64)) | (1L << (ROUTER_ID - 64)) | (1L << (ROUTING - 64)) | (1L << (SERVER - 64)) | (1L << (SERVICE - 64)) | (1L << (SLAVES - 64)) | (1L << (SNMP_SERVER - 64)) | (1L << (SOURCE - 64)) | (1L << (STATIC - 64)) | (1L << (STP - 64)) | (1L << (SYS_MAC - 64)) | (1L << (SYSLOG - 64)) | (1L << (TIME - 64)) | (1L << (UNICAST - 64)) | (1L << (VIDS - 64)) | (1L << (VLAN - 64)) | (1L << (VLAN_AWARE - 64)) | (1L << (VLAN_ID - 64)) | (1L << (VLAN_RAW_DEVICE - 64)) | (1L << (VNI - 64)) | (1L << (VRF - 64)) | (1L << (VRF_TABLE - 64)) | (1L << (VXLAN - 64)) | (1L << (VXLAN_ANYCAST_IP - 64)) | (1L << (WARNINGS - 64)) | (1L << (ZONE - 64)) | (1L << (EXTRA_CONFIGURATION_HEADER - 64)) | (1L << (COMMA - 64)) | (1L << (COMMENT_LINE - 64)) | (1L << (COMMENT_TAIL - 64)) | (1L << (DASH - 64)) | (1L << (DEC - 64)) | (1L << (IP_ADDRESS - 64)) | (1L << (IP_PREFIX - 64)) | (1L << (IPV6_ADDRESS - 64)) | (1L << (IPV6_PREFIX - 64)) | (1L << (MAC_ADDRESS - 64)) | (1L << (NUMBERED_WORD - 64)) | (1L << (WORD - 64)) | (1L << (WS - 64)) | (1L << (M_Printf_WS - 64)) | (1L << (M_Printf_EXTRA_CONFIGURATION_FOOTER - 64)) | (1L << (M_Printf_NEWLINE - 64)) | (1L << (M_Printf_USERNAME - 64)))) != 0)) {
				{
				{
				setState(599);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==NEWLINE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(604);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(605);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Numbered_wordContext extends ParserRuleContext {
		public TerminalNode NUMBERED_WORD() { return getToken(CumulusNcluParser.NUMBERED_WORD, 0); }
		public Numbered_wordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numbered_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterNumbered_word(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitNumbered_word(this);
		}
	}

	public final Numbered_wordContext numbered_word() throws RecognitionException {
		Numbered_wordContext _localctx = new Numbered_wordContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_numbered_word);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(607);
			match(NUMBERED_WORD);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RangeContext extends ParserRuleContext {
		public Uint16Context low;
		public Uint16Context high;
		public List<Uint16Context> uint16() {
			return getRuleContexts(Uint16Context.class);
		}
		public Uint16Context uint16(int i) {
			return getRuleContext(Uint16Context.class,i);
		}
		public TerminalNode DASH() { return getToken(CumulusNcluParser.DASH, 0); }
		public RangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitRange(this);
		}
	}

	public final RangeContext range() throws RecognitionException {
		RangeContext _localctx = new RangeContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_range);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(609);
			((RangeContext)_localctx).low = uint16();
			setState(612);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DASH) {
				{
				setState(610);
				match(DASH);
				setState(611);
				((RangeContext)_localctx).high = uint16();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Range_setContext extends ParserRuleContext {
		public List<RangeContext> range() {
			return getRuleContexts(RangeContext.class);
		}
		public RangeContext range(int i) {
			return getRuleContext(RangeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CumulusNcluParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CumulusNcluParser.COMMA, i);
		}
		public Range_setContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_range_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterRange_set(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitRange_set(this);
		}
	}

	public final Range_setContext range_set() throws RecognitionException {
		Range_setContext _localctx = new Range_setContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_range_set);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(614);
			range();
			setState(619);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(615);
					match(COMMA);
					setState(616);
					range();
					}
					} 
				}
				setState(621);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Uint16Context extends ParserRuleContext {
		public Token d;
		public TerminalNode DEC() { return getToken(CumulusNcluParser.DEC, 0); }
		public Uint16Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uint16; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterUint16(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitUint16(this);
		}
	}

	public final Uint16Context uint16() throws RecognitionException {
		Uint16Context _localctx = new Uint16Context(_ctx, getState());
		enterRule(_localctx, 146, RULE_uint16);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(622);
			((Uint16Context)_localctx).d = match(DEC);
			setState(623);
			if (!(isUint16(((Uint16Context)_localctx).d))) throw new FailedPredicateException(this, "isUint16($d)");
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Uint32Context extends ParserRuleContext {
		public Token d;
		public TerminalNode DEC() { return getToken(CumulusNcluParser.DEC, 0); }
		public Uint32Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_uint32; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterUint32(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitUint32(this);
		}
	}

	public final Uint32Context uint32() throws RecognitionException {
		Uint32Context _localctx = new Uint32Context(_ctx, getState());
		enterRule(_localctx, 148, RULE_uint32);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(625);
			((Uint32Context)_localctx).d = match(DEC);
			setState(626);
			if (!(isUint32(((Uint32Context)_localctx).d))) throw new FailedPredicateException(this, "isUint32($d)");
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vlan_idContext extends ParserRuleContext {
		public Token v;
		public TerminalNode DEC() { return getToken(CumulusNcluParser.DEC, 0); }
		public Vlan_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vlan_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVlan_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVlan_id(this);
		}
	}

	public final Vlan_idContext vlan_id() throws RecognitionException {
		Vlan_idContext _localctx = new Vlan_idContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_vlan_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(628);
			((Vlan_idContext)_localctx).v = match(DEC);
			setState(629);
			if (!(isVlanId(((Vlan_idContext)_localctx).v))) throw new FailedPredicateException(this, "isVlanId($v)");
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vlan_rangeContext extends ParserRuleContext {
		public Vlan_idContext low;
		public Vlan_idContext high;
		public List<Vlan_idContext> vlan_id() {
			return getRuleContexts(Vlan_idContext.class);
		}
		public Vlan_idContext vlan_id(int i) {
			return getRuleContext(Vlan_idContext.class,i);
		}
		public TerminalNode DASH() { return getToken(CumulusNcluParser.DASH, 0); }
		public Vlan_rangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vlan_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVlan_range(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVlan_range(this);
		}
	}

	public final Vlan_rangeContext vlan_range() throws RecognitionException {
		Vlan_rangeContext _localctx = new Vlan_rangeContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_vlan_range);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(631);
			((Vlan_rangeContext)_localctx).low = vlan_id();
			setState(634);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DASH) {
				{
				setState(632);
				match(DASH);
				setState(633);
				((Vlan_rangeContext)_localctx).high = vlan_id();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vlan_range_setContext extends ParserRuleContext {
		public List<Vlan_rangeContext> vlan_range() {
			return getRuleContexts(Vlan_rangeContext.class);
		}
		public Vlan_rangeContext vlan_range(int i) {
			return getRuleContext(Vlan_rangeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CumulusNcluParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CumulusNcluParser.COMMA, i);
		}
		public Vlan_range_setContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vlan_range_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVlan_range_set(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVlan_range_set(this);
		}
	}

	public final Vlan_range_setContext vlan_range_set() throws RecognitionException {
		Vlan_range_setContext _localctx = new Vlan_range_setContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_vlan_range_set);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(636);
			vlan_range();
			setState(641);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(637);
				match(COMMA);
				setState(638);
				vlan_range();
				}
				}
				setState(643);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Vni_numberContext extends ParserRuleContext {
		public Token v;
		public TerminalNode DEC() { return getToken(CumulusNcluParser.DEC, 0); }
		public Vni_numberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vni_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterVni_number(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitVni_number(this);
		}
	}

	public final Vni_numberContext vni_number() throws RecognitionException {
		Vni_numberContext _localctx = new Vni_numberContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_vni_number);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(644);
			((Vni_numberContext)_localctx).v = match(DEC);
			setState(645);
			if (!(isVniNumber(((Vni_numberContext)_localctx).v))) throw new FailedPredicateException(this, "isVniNumber($v)");
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WordContext extends ParserRuleContext {
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public WordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitWord(this);
		}
	}

	public final WordContext word() throws RecognitionException {
		WordContext _localctx = new WordContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_word);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(647);
			_la = _input.LA(1);
			if ( _la <= 0 || (_la==NEWLINE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_bgpContext extends ParserRuleContext {
		public TerminalNode BGP() { return getToken(CumulusNcluParser.BGP, 0); }
		public B_commonContext b_common() {
			return getRuleContext(B_commonContext.class,0);
		}
		public B_vrfContext b_vrf() {
			return getRuleContext(B_vrfContext.class,0);
		}
		public A_bgpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_bgp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_bgp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_bgp(this);
		}
	}

	public final A_bgpContext a_bgp() throws RecognitionException {
		A_bgpContext _localctx = new A_bgpContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_a_bgp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(649);
			match(BGP);
			setState(652);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AUTONOMOUS_SYSTEM:
			case IPV4:
			case L2VPN:
			case NEIGHBOR:
			case ROUTER_ID:
				{
				setState(650);
				b_common();
				}
				break;
			case VRF:
				{
				setState(651);
				b_vrf();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_commonContext extends ParserRuleContext {
		public B_autonomous_systemContext b_autonomous_system() {
			return getRuleContext(B_autonomous_systemContext.class,0);
		}
		public B_ipv4_unicastContext b_ipv4_unicast() {
			return getRuleContext(B_ipv4_unicastContext.class,0);
		}
		public B_l2vpnContext b_l2vpn() {
			return getRuleContext(B_l2vpnContext.class,0);
		}
		public B_neighborContext b_neighbor() {
			return getRuleContext(B_neighborContext.class,0);
		}
		public B_router_idContext b_router_id() {
			return getRuleContext(B_router_idContext.class,0);
		}
		public B_commonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_common; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterB_common(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitB_common(this);
		}
	}

	public final B_commonContext b_common() throws RecognitionException {
		B_commonContext _localctx = new B_commonContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_b_common);
		try {
			setState(659);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case AUTONOMOUS_SYSTEM:
				enterOuterAlt(_localctx, 1);
				{
				setState(654);
				b_autonomous_system();
				}
				break;
			case IPV4:
				enterOuterAlt(_localctx, 2);
				{
				setState(655);
				b_ipv4_unicast();
				}
				break;
			case L2VPN:
				enterOuterAlt(_localctx, 3);
				{
				setState(656);
				b_l2vpn();
				}
				break;
			case NEIGHBOR:
				enterOuterAlt(_localctx, 4);
				{
				setState(657);
				b_neighbor();
				}
				break;
			case ROUTER_ID:
				enterOuterAlt(_localctx, 5);
				{
				setState(658);
				b_router_id();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_autonomous_systemContext extends ParserRuleContext {
		public Uint32Context as;
		public TerminalNode AUTONOMOUS_SYSTEM() { return getToken(CumulusNcluParser.AUTONOMOUS_SYSTEM, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Uint32Context uint32() {
			return getRuleContext(Uint32Context.class,0);
		}
		public B_autonomous_systemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_autonomous_system; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterB_autonomous_system(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitB_autonomous_system(this);
		}
	}

	public final B_autonomous_systemContext b_autonomous_system() throws RecognitionException {
		B_autonomous_systemContext _localctx = new B_autonomous_systemContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_b_autonomous_system);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(661);
			match(AUTONOMOUS_SYSTEM);
			setState(662);
			((B_autonomous_systemContext)_localctx).as = uint32();
			setState(663);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_ipv4_unicastContext extends ParserRuleContext {
		public TerminalNode IPV4() { return getToken(CumulusNcluParser.IPV4, 0); }
		public TerminalNode UNICAST() { return getToken(CumulusNcluParser.UNICAST, 0); }
		public Bi4_networkContext bi4_network() {
			return getRuleContext(Bi4_networkContext.class,0);
		}
		public Bi4_redistribute_connectedContext bi4_redistribute_connected() {
			return getRuleContext(Bi4_redistribute_connectedContext.class,0);
		}
		public Bi4_redistribute_staticContext bi4_redistribute_static() {
			return getRuleContext(Bi4_redistribute_staticContext.class,0);
		}
		public B_ipv4_unicastContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_ipv4_unicast; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterB_ipv4_unicast(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitB_ipv4_unicast(this);
		}
	}

	public final B_ipv4_unicastContext b_ipv4_unicast() throws RecognitionException {
		B_ipv4_unicastContext _localctx = new B_ipv4_unicastContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_b_ipv4_unicast);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(665);
			match(IPV4);
			setState(666);
			match(UNICAST);
			setState(670);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				{
				setState(667);
				bi4_network();
				}
				break;
			case 2:
				{
				setState(668);
				bi4_redistribute_connected();
				}
				break;
			case 3:
				{
				setState(669);
				bi4_redistribute_static();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bi4_networkContext extends ParserRuleContext {
		public Ip_prefixContext network;
		public TerminalNode NETWORK() { return getToken(CumulusNcluParser.NETWORK, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ip_prefixContext ip_prefix() {
			return getRuleContext(Ip_prefixContext.class,0);
		}
		public Bi4_networkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bi4_network; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBi4_network(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBi4_network(this);
		}
	}

	public final Bi4_networkContext bi4_network() throws RecognitionException {
		Bi4_networkContext _localctx = new Bi4_networkContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_bi4_network);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(672);
			match(NETWORK);
			setState(673);
			((Bi4_networkContext)_localctx).network = ip_prefix();
			setState(674);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bi4_redistribute_connectedContext extends ParserRuleContext {
		public WordContext rm;
		public TerminalNode REDISTRIBUTE() { return getToken(CumulusNcluParser.REDISTRIBUTE, 0); }
		public TerminalNode CONNECTED() { return getToken(CumulusNcluParser.CONNECTED, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public TerminalNode ROUTE_MAP() { return getToken(CumulusNcluParser.ROUTE_MAP, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Bi4_redistribute_connectedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bi4_redistribute_connected; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBi4_redistribute_connected(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBi4_redistribute_connected(this);
		}
	}

	public final Bi4_redistribute_connectedContext bi4_redistribute_connected() throws RecognitionException {
		Bi4_redistribute_connectedContext _localctx = new Bi4_redistribute_connectedContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_bi4_redistribute_connected);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(676);
			match(REDISTRIBUTE);
			setState(677);
			match(CONNECTED);
			setState(680);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROUTE_MAP) {
				{
				setState(678);
				match(ROUTE_MAP);
				setState(679);
				((Bi4_redistribute_connectedContext)_localctx).rm = word();
				}
			}

			setState(682);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bi4_redistribute_staticContext extends ParserRuleContext {
		public WordContext rm;
		public TerminalNode REDISTRIBUTE() { return getToken(CumulusNcluParser.REDISTRIBUTE, 0); }
		public TerminalNode STATIC() { return getToken(CumulusNcluParser.STATIC, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public TerminalNode ROUTE_MAP() { return getToken(CumulusNcluParser.ROUTE_MAP, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Bi4_redistribute_staticContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bi4_redistribute_static; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBi4_redistribute_static(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBi4_redistribute_static(this);
		}
	}

	public final Bi4_redistribute_staticContext bi4_redistribute_static() throws RecognitionException {
		Bi4_redistribute_staticContext _localctx = new Bi4_redistribute_staticContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_bi4_redistribute_static);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(684);
			match(REDISTRIBUTE);
			setState(685);
			match(STATIC);
			setState(688);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ROUTE_MAP) {
				{
				setState(686);
				match(ROUTE_MAP);
				setState(687);
				((Bi4_redistribute_staticContext)_localctx).rm = word();
				}
			}

			setState(690);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_l2vpnContext extends ParserRuleContext {
		public TerminalNode L2VPN() { return getToken(CumulusNcluParser.L2VPN, 0); }
		public TerminalNode EVPN() { return getToken(CumulusNcluParser.EVPN, 0); }
		public Ble_advertise_all_vniContext ble_advertise_all_vni() {
			return getRuleContext(Ble_advertise_all_vniContext.class,0);
		}
		public Ble_advertise_default_gwContext ble_advertise_default_gw() {
			return getRuleContext(Ble_advertise_default_gwContext.class,0);
		}
		public Ble_advertise_ipv4_unicastContext ble_advertise_ipv4_unicast() {
			return getRuleContext(Ble_advertise_ipv4_unicastContext.class,0);
		}
		public Ble_neighborContext ble_neighbor() {
			return getRuleContext(Ble_neighborContext.class,0);
		}
		public B_l2vpnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_l2vpn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterB_l2vpn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitB_l2vpn(this);
		}
	}

	public final B_l2vpnContext b_l2vpn() throws RecognitionException {
		B_l2vpnContext _localctx = new B_l2vpnContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_b_l2vpn);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(692);
			match(L2VPN);
			setState(693);
			match(EVPN);
			setState(698);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ADVERTISE_ALL_VNI:
				{
				setState(694);
				ble_advertise_all_vni();
				}
				break;
			case ADVERTISE_DEFAULT_GW:
				{
				setState(695);
				ble_advertise_default_gw();
				}
				break;
			case ADVERTISE:
				{
				setState(696);
				ble_advertise_ipv4_unicast();
				}
				break;
			case NEIGHBOR:
				{
				setState(697);
				ble_neighbor();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ble_advertise_all_vniContext extends ParserRuleContext {
		public TerminalNode ADVERTISE_ALL_VNI() { return getToken(CumulusNcluParser.ADVERTISE_ALL_VNI, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ble_advertise_all_vniContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ble_advertise_all_vni; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBle_advertise_all_vni(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBle_advertise_all_vni(this);
		}
	}

	public final Ble_advertise_all_vniContext ble_advertise_all_vni() throws RecognitionException {
		Ble_advertise_all_vniContext _localctx = new Ble_advertise_all_vniContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_ble_advertise_all_vni);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(700);
			match(ADVERTISE_ALL_VNI);
			setState(701);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ble_advertise_default_gwContext extends ParserRuleContext {
		public TerminalNode ADVERTISE_DEFAULT_GW() { return getToken(CumulusNcluParser.ADVERTISE_DEFAULT_GW, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ble_advertise_default_gwContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ble_advertise_default_gw; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBle_advertise_default_gw(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBle_advertise_default_gw(this);
		}
	}

	public final Ble_advertise_default_gwContext ble_advertise_default_gw() throws RecognitionException {
		Ble_advertise_default_gwContext _localctx = new Ble_advertise_default_gwContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_ble_advertise_default_gw);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(703);
			match(ADVERTISE_DEFAULT_GW);
			setState(704);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ble_advertise_ipv4_unicastContext extends ParserRuleContext {
		public TerminalNode ADVERTISE() { return getToken(CumulusNcluParser.ADVERTISE, 0); }
		public TerminalNode IPV4() { return getToken(CumulusNcluParser.IPV4, 0); }
		public TerminalNode UNICAST() { return getToken(CumulusNcluParser.UNICAST, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ble_advertise_ipv4_unicastContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ble_advertise_ipv4_unicast; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBle_advertise_ipv4_unicast(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBle_advertise_ipv4_unicast(this);
		}
	}

	public final Ble_advertise_ipv4_unicastContext ble_advertise_ipv4_unicast() throws RecognitionException {
		Ble_advertise_ipv4_unicastContext _localctx = new Ble_advertise_ipv4_unicastContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_ble_advertise_ipv4_unicast);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(706);
			match(ADVERTISE);
			setState(707);
			match(IPV4);
			setState(708);
			match(UNICAST);
			setState(709);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ble_neighborContext extends ParserRuleContext {
		public WordContext name;
		public TerminalNode NEIGHBOR() { return getToken(CumulusNcluParser.NEIGHBOR, 0); }
		public Blen_activateContext blen_activate() {
			return getRuleContext(Blen_activateContext.class,0);
		}
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Ble_neighborContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ble_neighbor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBle_neighbor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBle_neighbor(this);
		}
	}

	public final Ble_neighborContext ble_neighbor() throws RecognitionException {
		Ble_neighborContext _localctx = new Ble_neighborContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_ble_neighbor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(711);
			match(NEIGHBOR);
			setState(712);
			((Ble_neighborContext)_localctx).name = word();
			setState(713);
			blen_activate();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Blen_activateContext extends ParserRuleContext {
		public TerminalNode ACTIVATE() { return getToken(CumulusNcluParser.ACTIVATE, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Blen_activateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blen_activate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBlen_activate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBlen_activate(this);
		}
	}

	public final Blen_activateContext blen_activate() throws RecognitionException {
		Blen_activateContext _localctx = new Blen_activateContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_blen_activate);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(715);
			match(ACTIVATE);
			setState(716);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_neighborContext extends ParserRuleContext {
		public WordContext name;
		public TerminalNode NEIGHBOR() { return getToken(CumulusNcluParser.NEIGHBOR, 0); }
		public Bn_interfaceContext bn_interface() {
			return getRuleContext(Bn_interfaceContext.class,0);
		}
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public B_neighborContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_neighbor; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterB_neighbor(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitB_neighbor(this);
		}
	}

	public final B_neighborContext b_neighbor() throws RecognitionException {
		B_neighborContext _localctx = new B_neighborContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_b_neighbor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(718);
			match(NEIGHBOR);
			setState(719);
			((B_neighborContext)_localctx).name = word();
			setState(720);
			bn_interface();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bn_interfaceContext extends ParserRuleContext {
		public TerminalNode INTERFACE() { return getToken(CumulusNcluParser.INTERFACE, 0); }
		public Bni_remote_as_externalContext bni_remote_as_external() {
			return getRuleContext(Bni_remote_as_externalContext.class,0);
		}
		public Bni_remote_as_internalContext bni_remote_as_internal() {
			return getRuleContext(Bni_remote_as_internalContext.class,0);
		}
		public Bni_remote_as_numberContext bni_remote_as_number() {
			return getRuleContext(Bni_remote_as_numberContext.class,0);
		}
		public Bn_interfaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bn_interface; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBn_interface(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBn_interface(this);
		}
	}

	public final Bn_interfaceContext bn_interface() throws RecognitionException {
		Bn_interfaceContext _localctx = new Bn_interfaceContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_bn_interface);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(722);
			match(INTERFACE);
			setState(726);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				{
				setState(723);
				bni_remote_as_external();
				}
				break;
			case 2:
				{
				setState(724);
				bni_remote_as_internal();
				}
				break;
			case 3:
				{
				setState(725);
				bni_remote_as_number();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bni_remote_as_externalContext extends ParserRuleContext {
		public TerminalNode REMOTE_AS() { return getToken(CumulusNcluParser.REMOTE_AS, 0); }
		public TerminalNode EXTERNAL() { return getToken(CumulusNcluParser.EXTERNAL, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Bni_remote_as_externalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bni_remote_as_external; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBni_remote_as_external(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBni_remote_as_external(this);
		}
	}

	public final Bni_remote_as_externalContext bni_remote_as_external() throws RecognitionException {
		Bni_remote_as_externalContext _localctx = new Bni_remote_as_externalContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_bni_remote_as_external);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(728);
			match(REMOTE_AS);
			setState(729);
			match(EXTERNAL);
			setState(730);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bni_remote_as_internalContext extends ParserRuleContext {
		public TerminalNode REMOTE_AS() { return getToken(CumulusNcluParser.REMOTE_AS, 0); }
		public TerminalNode INTERNAL() { return getToken(CumulusNcluParser.INTERNAL, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Bni_remote_as_internalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bni_remote_as_internal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBni_remote_as_internal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBni_remote_as_internal(this);
		}
	}

	public final Bni_remote_as_internalContext bni_remote_as_internal() throws RecognitionException {
		Bni_remote_as_internalContext _localctx = new Bni_remote_as_internalContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_bni_remote_as_internal);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(732);
			match(REMOTE_AS);
			setState(733);
			match(INTERNAL);
			setState(734);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bni_remote_as_numberContext extends ParserRuleContext {
		public Uint32Context as;
		public TerminalNode REMOTE_AS() { return getToken(CumulusNcluParser.REMOTE_AS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Uint32Context uint32() {
			return getRuleContext(Uint32Context.class,0);
		}
		public Bni_remote_as_numberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bni_remote_as_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterBni_remote_as_number(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitBni_remote_as_number(this);
		}
	}

	public final Bni_remote_as_numberContext bni_remote_as_number() throws RecognitionException {
		Bni_remote_as_numberContext _localctx = new Bni_remote_as_numberContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_bni_remote_as_number);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(736);
			match(REMOTE_AS);
			setState(737);
			((Bni_remote_as_numberContext)_localctx).as = uint32();
			setState(738);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_router_idContext extends ParserRuleContext {
		public Ip_addressContext id;
		public TerminalNode ROUTER_ID() { return getToken(CumulusNcluParser.ROUTER_ID, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ip_addressContext ip_address() {
			return getRuleContext(Ip_addressContext.class,0);
		}
		public B_router_idContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_router_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterB_router_id(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitB_router_id(this);
		}
	}

	public final B_router_idContext b_router_id() throws RecognitionException {
		B_router_idContext _localctx = new B_router_idContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_b_router_id);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(740);
			match(ROUTER_ID);
			setState(741);
			((B_router_idContext)_localctx).id = ip_address();
			setState(742);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class B_vrfContext extends ParserRuleContext {
		public WordContext name;
		public TerminalNode VRF() { return getToken(CumulusNcluParser.VRF, 0); }
		public B_commonContext b_common() {
			return getRuleContext(B_commonContext.class,0);
		}
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public B_vrfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_b_vrf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterB_vrf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitB_vrf(this);
		}
	}

	public final B_vrfContext b_vrf() throws RecognitionException {
		B_vrfContext _localctx = new B_vrfContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_b_vrf);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(744);
			match(VRF);
			setState(745);
			((B_vrfContext)_localctx).name = word();
			setState(746);
			b_common();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Frr_vrfContext extends ParserRuleContext {
		public WordContext name;
		public TerminalNode VRF() { return getToken(CumulusNcluParser.VRF, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public List<Frrv_ip_routeContext> frrv_ip_route() {
			return getRuleContexts(Frrv_ip_routeContext.class);
		}
		public Frrv_ip_routeContext frrv_ip_route(int i) {
			return getRuleContext(Frrv_ip_routeContext.class,i);
		}
		public Frr_vrfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frr_vrf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterFrr_vrf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitFrr_vrf(this);
		}
	}

	public final Frr_vrfContext frr_vrf() throws RecognitionException {
		Frr_vrfContext _localctx = new Frr_vrfContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_frr_vrf);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(748);
			match(VRF);
			setState(749);
			((Frr_vrfContext)_localctx).name = word();
			setState(750);
			match(NEWLINE);
			setState(754);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IP) {
				{
				{
				setState(751);
				frrv_ip_route();
				}
				}
				setState(756);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Frrv_ip_routeContext extends ParserRuleContext {
		public Ip_prefixContext network;
		public Ip_addressContext nhip;
		public TerminalNode IP() { return getToken(CumulusNcluParser.IP, 0); }
		public TerminalNode ROUTE() { return getToken(CumulusNcluParser.ROUTE, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ip_prefixContext ip_prefix() {
			return getRuleContext(Ip_prefixContext.class,0);
		}
		public Ip_addressContext ip_address() {
			return getRuleContext(Ip_addressContext.class,0);
		}
		public Frrv_ip_routeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frrv_ip_route; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterFrrv_ip_route(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitFrrv_ip_route(this);
		}
	}

	public final Frrv_ip_routeContext frrv_ip_route() throws RecognitionException {
		Frrv_ip_routeContext _localctx = new Frrv_ip_routeContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_frrv_ip_route);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(757);
			match(IP);
			setState(758);
			match(ROUTE);
			setState(759);
			((Frrv_ip_routeContext)_localctx).network = ip_prefix();
			setState(760);
			((Frrv_ip_routeContext)_localctx).nhip = ip_address();
			setState(761);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Frr_usernameContext extends ParserRuleContext {
		public TerminalNode USERNAME() { return getToken(CumulusNcluParser.USERNAME, 0); }
		public Frr_null_rest_of_lineContext frr_null_rest_of_line() {
			return getRuleContext(Frr_null_rest_of_lineContext.class,0);
		}
		public Frr_usernameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frr_username; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterFrr_username(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitFrr_username(this);
		}
	}

	public final Frr_usernameContext frr_username() throws RecognitionException {
		Frr_usernameContext _localctx = new Frr_usernameContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_frr_username);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(763);
			match(USERNAME);
			setState(764);
			frr_null_rest_of_line();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Frr_null_rest_of_lineContext extends ParserRuleContext {
		public List<TerminalNode> EXTRA_CONFIGURATION_FOOTER() { return getTokens(CumulusNcluParser.EXTRA_CONFIGURATION_FOOTER); }
		public TerminalNode EXTRA_CONFIGURATION_FOOTER(int i) {
			return getToken(CumulusNcluParser.EXTRA_CONFIGURATION_FOOTER, i);
		}
		public Frr_null_rest_of_lineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frr_null_rest_of_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterFrr_null_rest_of_line(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitFrr_null_rest_of_line(this);
		}
	}

	public final Frr_null_rest_of_lineContext frr_null_rest_of_line() throws RecognitionException {
		Frr_null_rest_of_lineContext _localctx = new Frr_null_rest_of_lineContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_frr_null_rest_of_line);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(769);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << USERNAME) | (1L << ACCESS) | (1L << ACTIVATE) | (1L << ADD) | (1L << ADDRESS) | (1L << ADDRESS_VIRTUAL) | (1L << ADVERTISE) | (1L << ADVERTISE_ALL_VNI) | (1L << ADVERTISE_DEFAULT_GW) | (1L << ALERTS) | (1L << ARP_ND_SUPPRESS) | (1L << AUTO) | (1L << AUTONOMOUS_SYSTEM) | (1L << BACKUP_IP) | (1L << BGP) | (1L << BOND) | (1L << BPDUGUARD) | (1L << BRIDGE) | (1L << CLAG) | (1L << COMMIT) | (1L << CONNECTED) | (1L << CRITICAL) | (1L << DATACENTER) | (1L << DEBUGGING) | (1L << DEFAULTS) | (1L << DEL) | (1L << DENY) | (1L << DNS) | (1L << DOT1X) | (1L << EMERGENCIES) | (1L << ERRORS) | (1L << EVPN) | (1L << EXTERNAL) | (1L << HOSTNAME) | (1L << IBURST) | (1L << ID) | (1L << INFORMATIONAL) | (1L << INTEGRATED_VTYSH_CONFIG) | (1L << INTERFACE) | (1L << INTERNAL) | (1L << IP) | (1L << IPV4) | (1L << IPV6) | (1L << L2VPN) | (1L << LEARNING) | (1L << LO) | (1L << LOCAL_TUNNELIP) | (1L << LOG) | (1L << LOOPBACK) | (1L << MATCH) | (1L << NAMESERVER) | (1L << NEIGHBOR) | (1L << NETWORK) | (1L << NET) | (1L << NOTIFICATIONS) | (1L << NTP) | (1L << OFF) | (1L << ON) | (1L << PEER_IP) | (1L << PERMIT) | (1L << PORTBPDUFILTER) | (1L << PORTS))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (PRIORITY - 64)) | (1L << (PTP - 64)) | (1L << (PVID - 64)) | (1L << (REDISTRIBUTE - 64)) | (1L << (REMOTE_AS - 64)) | (1L << (ROUTE - 64)) | (1L << (ROUTE_MAP - 64)) | (1L << (ROUTER_ID - 64)) | (1L << (ROUTING - 64)) | (1L << (SERVER - 64)) | (1L << (SERVICE - 64)) | (1L << (SLAVES - 64)) | (1L << (SNMP_SERVER - 64)) | (1L << (SOURCE - 64)) | (1L << (STATIC - 64)) | (1L << (STP - 64)) | (1L << (SYS_MAC - 64)) | (1L << (SYSLOG - 64)) | (1L << (TIME - 64)) | (1L << (UNICAST - 64)) | (1L << (VIDS - 64)) | (1L << (VLAN - 64)) | (1L << (VLAN_AWARE - 64)) | (1L << (VLAN_ID - 64)) | (1L << (VLAN_RAW_DEVICE - 64)) | (1L << (VNI - 64)) | (1L << (VRF - 64)) | (1L << (VRF_TABLE - 64)) | (1L << (VXLAN - 64)) | (1L << (VXLAN_ANYCAST_IP - 64)) | (1L << (WARNINGS - 64)) | (1L << (ZONE - 64)) | (1L << (EXTRA_CONFIGURATION_HEADER - 64)) | (1L << (COMMA - 64)) | (1L << (COMMENT_LINE - 64)) | (1L << (COMMENT_TAIL - 64)) | (1L << (DASH - 64)) | (1L << (DEC - 64)) | (1L << (IP_ADDRESS - 64)) | (1L << (IP_PREFIX - 64)) | (1L << (IPV6_ADDRESS - 64)) | (1L << (IPV6_PREFIX - 64)) | (1L << (MAC_ADDRESS - 64)) | (1L << (NEWLINE - 64)) | (1L << (NUMBERED_WORD - 64)) | (1L << (WORD - 64)) | (1L << (WS - 64)) | (1L << (M_Printf_WS - 64)) | (1L << (M_Printf_EXTRA_CONFIGURATION_FOOTER - 64)) | (1L << (M_Printf_NEWLINE - 64)) | (1L << (M_Printf_USERNAME - 64)))) != 0)) {
				{
				{
				setState(766);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==EXTRA_CONFIGURATION_FOOTER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(771);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Frr_unrecognizedContext extends ParserRuleContext {
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Frr_null_rest_of_lineContext frr_null_rest_of_line() {
			return getRuleContext(Frr_null_rest_of_lineContext.class,0);
		}
		public Frr_unrecognizedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frr_unrecognized; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterFrr_unrecognized(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitFrr_unrecognized(this);
		}
	}

	public final Frr_unrecognizedContext frr_unrecognized() throws RecognitionException {
		Frr_unrecognizedContext _localctx = new Frr_unrecognizedContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_frr_unrecognized);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(772);
			word();
			setState(773);
			frr_null_rest_of_line();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_interfaceContext extends ParserRuleContext {
		public GlobContext interfaces;
		public TerminalNode INTERFACE() { return getToken(CumulusNcluParser.INTERFACE, 0); }
		public GlobContext glob() {
			return getRuleContext(GlobContext.class,0);
		}
		public I_bridgeContext i_bridge() {
			return getRuleContext(I_bridgeContext.class,0);
		}
		public I_clagContext i_clag() {
			return getRuleContext(I_clagContext.class,0);
		}
		public I_ip_addressContext i_ip_address() {
			return getRuleContext(I_ip_addressContext.class,0);
		}
		public I_vrfContext i_vrf() {
			return getRuleContext(I_vrfContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public A_interfaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_interface; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_interface(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_interface(this);
		}
	}

	public final A_interfaceContext a_interface() throws RecognitionException {
		A_interfaceContext _localctx = new A_interfaceContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_a_interface);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(775);
			match(INTERFACE);
			setState(776);
			((A_interfaceContext)_localctx).interfaces = glob();
			setState(782);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BRIDGE:
				{
				setState(777);
				i_bridge();
				}
				break;
			case CLAG:
				{
				setState(778);
				i_clag();
				}
				break;
			case IP:
				{
				setState(779);
				i_ip_address();
				}
				break;
			case VRF:
				{
				setState(780);
				i_vrf();
				}
				break;
			case NEWLINE:
				{
				setState(781);
				match(NEWLINE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class I_bridgeContext extends ParserRuleContext {
		public TerminalNode BRIDGE() { return getToken(CumulusNcluParser.BRIDGE, 0); }
		public Ib_accessContext ib_access() {
			return getRuleContext(Ib_accessContext.class,0);
		}
		public Ib_pvidContext ib_pvid() {
			return getRuleContext(Ib_pvidContext.class,0);
		}
		public Ib_vidsContext ib_vids() {
			return getRuleContext(Ib_vidsContext.class,0);
		}
		public I_bridgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_i_bridge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterI_bridge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitI_bridge(this);
		}
	}

	public final I_bridgeContext i_bridge() throws RecognitionException {
		I_bridgeContext _localctx = new I_bridgeContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_i_bridge);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(784);
			match(BRIDGE);
			setState(788);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ACCESS:
				{
				setState(785);
				ib_access();
				}
				break;
			case PVID:
				{
				setState(786);
				ib_pvid();
				}
				break;
			case VIDS:
				{
				setState(787);
				ib_vids();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ib_accessContext extends ParserRuleContext {
		public Vlan_idContext vlan;
		public TerminalNode ACCESS() { return getToken(CumulusNcluParser.ACCESS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vlan_idContext vlan_id() {
			return getRuleContext(Vlan_idContext.class,0);
		}
		public Ib_accessContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ib_access; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIb_access(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIb_access(this);
		}
	}

	public final Ib_accessContext ib_access() throws RecognitionException {
		Ib_accessContext _localctx = new Ib_accessContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_ib_access);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(790);
			match(ACCESS);
			setState(791);
			((Ib_accessContext)_localctx).vlan = vlan_id();
			setState(792);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ib_pvidContext extends ParserRuleContext {
		public Vlan_idContext id;
		public TerminalNode PVID() { return getToken(CumulusNcluParser.PVID, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vlan_idContext vlan_id() {
			return getRuleContext(Vlan_idContext.class,0);
		}
		public Ib_pvidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ib_pvid; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIb_pvid(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIb_pvid(this);
		}
	}

	public final Ib_pvidContext ib_pvid() throws RecognitionException {
		Ib_pvidContext _localctx = new Ib_pvidContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_ib_pvid);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(794);
			match(PVID);
			setState(795);
			((Ib_pvidContext)_localctx).id = vlan_id();
			setState(796);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ib_vidsContext extends ParserRuleContext {
		public Vlan_range_setContext vlans;
		public TerminalNode VIDS() { return getToken(CumulusNcluParser.VIDS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Vlan_range_setContext vlan_range_set() {
			return getRuleContext(Vlan_range_setContext.class,0);
		}
		public Ib_vidsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ib_vids; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIb_vids(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIb_vids(this);
		}
	}

	public final Ib_vidsContext ib_vids() throws RecognitionException {
		Ib_vidsContext _localctx = new Ib_vidsContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_ib_vids);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(798);
			match(VIDS);
			setState(799);
			((Ib_vidsContext)_localctx).vlans = vlan_range_set();
			setState(800);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class I_clagContext extends ParserRuleContext {
		public TerminalNode CLAG() { return getToken(CumulusNcluParser.CLAG, 0); }
		public Ic_backup_ipContext ic_backup_ip() {
			return getRuleContext(Ic_backup_ipContext.class,0);
		}
		public Ic_peer_ipContext ic_peer_ip() {
			return getRuleContext(Ic_peer_ipContext.class,0);
		}
		public Ic_priorityContext ic_priority() {
			return getRuleContext(Ic_priorityContext.class,0);
		}
		public Ic_sys_macContext ic_sys_mac() {
			return getRuleContext(Ic_sys_macContext.class,0);
		}
		public I_clagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_i_clag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterI_clag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitI_clag(this);
		}
	}

	public final I_clagContext i_clag() throws RecognitionException {
		I_clagContext _localctx = new I_clagContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_i_clag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(802);
			match(CLAG);
			setState(807);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BACKUP_IP:
				{
				setState(803);
				ic_backup_ip();
				}
				break;
			case PEER_IP:
				{
				setState(804);
				ic_peer_ip();
				}
				break;
			case PRIORITY:
				{
				setState(805);
				ic_priority();
				}
				break;
			case SYS_MAC:
				{
				setState(806);
				ic_sys_mac();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ic_backup_ipContext extends ParserRuleContext {
		public Ip_addressContext backup_ip;
		public WordContext vrf;
		public TerminalNode BACKUP_IP() { return getToken(CumulusNcluParser.BACKUP_IP, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ip_addressContext ip_address() {
			return getRuleContext(Ip_addressContext.class,0);
		}
		public TerminalNode VRF() { return getToken(CumulusNcluParser.VRF, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Ic_backup_ipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ic_backup_ip; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIc_backup_ip(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIc_backup_ip(this);
		}
	}

	public final Ic_backup_ipContext ic_backup_ip() throws RecognitionException {
		Ic_backup_ipContext _localctx = new Ic_backup_ipContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_ic_backup_ip);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(809);
			match(BACKUP_IP);
			setState(810);
			((Ic_backup_ipContext)_localctx).backup_ip = ip_address();
			setState(813);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VRF) {
				{
				setState(811);
				match(VRF);
				setState(812);
				((Ic_backup_ipContext)_localctx).vrf = word();
				}
			}

			setState(815);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ic_peer_ipContext extends ParserRuleContext {
		public Ip_addressContext peer_ip;
		public TerminalNode PEER_IP() { return getToken(CumulusNcluParser.PEER_IP, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ip_addressContext ip_address() {
			return getRuleContext(Ip_addressContext.class,0);
		}
		public Ic_peer_ipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ic_peer_ip; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIc_peer_ip(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIc_peer_ip(this);
		}
	}

	public final Ic_peer_ipContext ic_peer_ip() throws RecognitionException {
		Ic_peer_ipContext _localctx = new Ic_peer_ipContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_ic_peer_ip);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(817);
			match(PEER_IP);
			setState(818);
			((Ic_peer_ipContext)_localctx).peer_ip = ip_address();
			setState(819);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ic_priorityContext extends ParserRuleContext {
		public Uint16Context priority;
		public TerminalNode PRIORITY() { return getToken(CumulusNcluParser.PRIORITY, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Uint16Context uint16() {
			return getRuleContext(Uint16Context.class,0);
		}
		public Ic_priorityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ic_priority; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIc_priority(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIc_priority(this);
		}
	}

	public final Ic_priorityContext ic_priority() throws RecognitionException {
		Ic_priorityContext _localctx = new Ic_priorityContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_ic_priority);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(821);
			match(PRIORITY);
			setState(822);
			((Ic_priorityContext)_localctx).priority = uint16();
			setState(823);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Ic_sys_macContext extends ParserRuleContext {
		public Mac_addressContext mac;
		public TerminalNode SYS_MAC() { return getToken(CumulusNcluParser.SYS_MAC, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Mac_addressContext mac_address() {
			return getRuleContext(Mac_addressContext.class,0);
		}
		public Ic_sys_macContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ic_sys_mac; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterIc_sys_mac(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitIc_sys_mac(this);
		}
	}

	public final Ic_sys_macContext ic_sys_mac() throws RecognitionException {
		Ic_sys_macContext _localctx = new Ic_sys_macContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_ic_sys_mac);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(825);
			match(SYS_MAC);
			setState(826);
			((Ic_sys_macContext)_localctx).mac = mac_address();
			setState(827);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class I_ip_addressContext extends ParserRuleContext {
		public Interface_addressContext address;
		public TerminalNode IP() { return getToken(CumulusNcluParser.IP, 0); }
		public TerminalNode ADDRESS() { return getToken(CumulusNcluParser.ADDRESS, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Interface_addressContext interface_address() {
			return getRuleContext(Interface_addressContext.class,0);
		}
		public I_ip_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_i_ip_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterI_ip_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitI_ip_address(this);
		}
	}

	public final I_ip_addressContext i_ip_address() throws RecognitionException {
		I_ip_addressContext _localctx = new I_ip_addressContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_i_ip_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(829);
			match(IP);
			setState(830);
			match(ADDRESS);
			setState(831);
			((I_ip_addressContext)_localctx).address = interface_address();
			setState(832);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class I_vrfContext extends ParserRuleContext {
		public WordContext name;
		public TerminalNode VRF() { return getToken(CumulusNcluParser.VRF, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public I_vrfContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_i_vrf; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterI_vrf(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitI_vrf(this);
		}
	}

	public final I_vrfContext i_vrf() throws RecognitionException {
		I_vrfContext _localctx = new I_vrfContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_i_vrf);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(834);
			match(VRF);
			setState(835);
			((I_vrfContext)_localctx).name = word();
			setState(836);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class A_routingContext extends ParserRuleContext {
		public TerminalNode ROUTING() { return getToken(CumulusNcluParser.ROUTING, 0); }
		public R_defaults_datacenterContext r_defaults_datacenter() {
			return getRuleContext(R_defaults_datacenterContext.class,0);
		}
		public R_logContext r_log() {
			return getRuleContext(R_logContext.class,0);
		}
		public R_routeContext r_route() {
			return getRuleContext(R_routeContext.class,0);
		}
		public R_route_mapContext r_route_map() {
			return getRuleContext(R_route_mapContext.class,0);
		}
		public R_service_integrated_vtysh_configContext r_service_integrated_vtysh_config() {
			return getRuleContext(R_service_integrated_vtysh_configContext.class,0);
		}
		public A_routingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_a_routing; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterA_routing(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitA_routing(this);
		}
	}

	public final A_routingContext a_routing() throws RecognitionException {
		A_routingContext _localctx = new A_routingContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_a_routing);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(838);
			match(ROUTING);
			setState(844);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DEFAULTS:
				{
				setState(839);
				r_defaults_datacenter();
				}
				break;
			case LOG:
				{
				setState(840);
				r_log();
				}
				break;
			case ROUTE:
				{
				setState(841);
				r_route();
				}
				break;
			case ROUTE_MAP:
				{
				setState(842);
				r_route_map();
				}
				break;
			case SERVICE:
				{
				setState(843);
				r_service_integrated_vtysh_config();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class R_defaults_datacenterContext extends ParserRuleContext {
		public TerminalNode DEFAULTS() { return getToken(CumulusNcluParser.DEFAULTS, 0); }
		public TerminalNode DATACENTER() { return getToken(CumulusNcluParser.DATACENTER, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public R_defaults_datacenterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_r_defaults_datacenter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterR_defaults_datacenter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitR_defaults_datacenter(this);
		}
	}

	public final R_defaults_datacenterContext r_defaults_datacenter() throws RecognitionException {
		R_defaults_datacenterContext _localctx = new R_defaults_datacenterContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_r_defaults_datacenter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(846);
			match(DEFAULTS);
			setState(847);
			match(DATACENTER);
			setState(848);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class R_logContext extends ParserRuleContext {
		public TerminalNode LOG() { return getToken(CumulusNcluParser.LOG, 0); }
		public Rl_syslogContext rl_syslog() {
			return getRuleContext(Rl_syslogContext.class,0);
		}
		public R_logContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_r_log; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterR_log(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitR_log(this);
		}
	}

	public final R_logContext r_log() throws RecognitionException {
		R_logContext _localctx = new R_logContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_r_log);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(850);
			match(LOG);
			setState(851);
			rl_syslog();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Rl_syslogContext extends ParserRuleContext {
		public TerminalNode SYSLOG() { return getToken(CumulusNcluParser.SYSLOG, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public TerminalNode ALERTS() { return getToken(CumulusNcluParser.ALERTS, 0); }
		public TerminalNode CRITICAL() { return getToken(CumulusNcluParser.CRITICAL, 0); }
		public TerminalNode DEBUGGING() { return getToken(CumulusNcluParser.DEBUGGING, 0); }
		public TerminalNode EMERGENCIES() { return getToken(CumulusNcluParser.EMERGENCIES, 0); }
		public TerminalNode ERRORS() { return getToken(CumulusNcluParser.ERRORS, 0); }
		public TerminalNode INFORMATIONAL() { return getToken(CumulusNcluParser.INFORMATIONAL, 0); }
		public TerminalNode NOTIFICATIONS() { return getToken(CumulusNcluParser.NOTIFICATIONS, 0); }
		public TerminalNode WARNINGS() { return getToken(CumulusNcluParser.WARNINGS, 0); }
		public Rl_syslogContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rl_syslog; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterRl_syslog(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitRl_syslog(this);
		}
	}

	public final Rl_syslogContext rl_syslog() throws RecognitionException {
		Rl_syslogContext _localctx = new Rl_syslogContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_rl_syslog);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(853);
			match(SYSLOG);
			setState(855);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALERTS) | (1L << CRITICAL) | (1L << DEBUGGING) | (1L << EMERGENCIES) | (1L << ERRORS) | (1L << INFORMATIONAL) | (1L << NOTIFICATIONS))) != 0) || _la==WARNINGS) {
				{
				setState(854);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALERTS) | (1L << CRITICAL) | (1L << DEBUGGING) | (1L << EMERGENCIES) | (1L << ERRORS) | (1L << INFORMATIONAL) | (1L << NOTIFICATIONS))) != 0) || _la==WARNINGS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(857);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class R_routeContext extends ParserRuleContext {
		public Ip_prefixContext prefix;
		public Ip_addressContext nhip;
		public TerminalNode ROUTE() { return getToken(CumulusNcluParser.ROUTE, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public Ip_prefixContext ip_prefix() {
			return getRuleContext(Ip_prefixContext.class,0);
		}
		public Ip_addressContext ip_address() {
			return getRuleContext(Ip_addressContext.class,0);
		}
		public R_routeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_r_route; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterR_route(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitR_route(this);
		}
	}

	public final R_routeContext r_route() throws RecognitionException {
		R_routeContext _localctx = new R_routeContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_r_route);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(859);
			match(ROUTE);
			setState(860);
			((R_routeContext)_localctx).prefix = ip_prefix();
			setState(861);
			((R_routeContext)_localctx).nhip = ip_address();
			setState(862);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class R_route_mapContext extends ParserRuleContext {
		public WordContext name;
		public Line_actionContext action;
		public Uint16Context num;
		public TerminalNode ROUTE_MAP() { return getToken(CumulusNcluParser.ROUTE_MAP, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Line_actionContext line_action() {
			return getRuleContext(Line_actionContext.class,0);
		}
		public Uint16Context uint16() {
			return getRuleContext(Uint16Context.class,0);
		}
		public Rm_matchContext rm_match() {
			return getRuleContext(Rm_matchContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public R_route_mapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_r_route_map; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterR_route_map(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitR_route_map(this);
		}
	}

	public final R_route_mapContext r_route_map() throws RecognitionException {
		R_route_mapContext _localctx = new R_route_mapContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_r_route_map);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(864);
			match(ROUTE_MAP);
			setState(865);
			((R_route_mapContext)_localctx).name = word();
			setState(866);
			((R_route_mapContext)_localctx).action = line_action();
			setState(867);
			((R_route_mapContext)_localctx).num = uint16();
			setState(870);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MATCH:
				{
				setState(868);
				rm_match();
				}
				break;
			case NEWLINE:
				{
				setState(869);
				match(NEWLINE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Rm_matchContext extends ParserRuleContext {
		public TerminalNode MATCH() { return getToken(CumulusNcluParser.MATCH, 0); }
		public Rmm_interfaceContext rmm_interface() {
			return getRuleContext(Rmm_interfaceContext.class,0);
		}
		public Rm_matchContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rm_match; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterRm_match(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitRm_match(this);
		}
	}

	public final Rm_matchContext rm_match() throws RecognitionException {
		Rm_matchContext _localctx = new Rm_matchContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_rm_match);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(872);
			match(MATCH);
			setState(873);
			rmm_interface();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Rmm_interfaceContext extends ParserRuleContext {
		public GlobContext interfaces;
		public TerminalNode INTERFACE() { return getToken(CumulusNcluParser.INTERFACE, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public GlobContext glob() {
			return getRuleContext(GlobContext.class,0);
		}
		public Rmm_interfaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rmm_interface; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterRmm_interface(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitRmm_interface(this);
		}
	}

	public final Rmm_interfaceContext rmm_interface() throws RecognitionException {
		Rmm_interfaceContext _localctx = new Rmm_interfaceContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_rmm_interface);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(875);
			match(INTERFACE);
			setState(876);
			((Rmm_interfaceContext)_localctx).interfaces = glob();
			setState(877);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class R_service_integrated_vtysh_configContext extends ParserRuleContext {
		public TerminalNode SERVICE() { return getToken(CumulusNcluParser.SERVICE, 0); }
		public TerminalNode INTEGRATED_VTYSH_CONFIG() { return getToken(CumulusNcluParser.INTEGRATED_VTYSH_CONFIG, 0); }
		public TerminalNode NEWLINE() { return getToken(CumulusNcluParser.NEWLINE, 0); }
		public R_service_integrated_vtysh_configContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_r_service_integrated_vtysh_config; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).enterR_service_integrated_vtysh_config(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CumulusNcluParserListener ) ((CumulusNcluParserListener)listener).exitR_service_integrated_vtysh_config(this);
		}
	}

	public final R_service_integrated_vtysh_configContext r_service_integrated_vtysh_config() throws RecognitionException {
		R_service_integrated_vtysh_configContext _localctx = new R_service_integrated_vtysh_configContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_r_service_integrated_vtysh_config);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(879);
			match(SERVICE);
			setState(880);
			match(INTEGRATED_VTYSH_CONFIG);
			setState(881);
			match(NEWLINE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 73:
			return uint16_sempred((Uint16Context)_localctx, predIndex);
		case 74:
			return uint32_sempred((Uint32Context)_localctx, predIndex);
		case 75:
			return vlan_id_sempred((Vlan_idContext)_localctx, predIndex);
		case 78:
			return vni_number_sempred((Vni_numberContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean uint16_sempred(Uint16Context _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return isUint16(((Uint16Context)_localctx).d);
		}
		return true;
	}
	private boolean uint32_sempred(Uint32Context _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return isUint32(((Uint32Context)_localctx).d);
		}
		return true;
	}
	private boolean vlan_id_sempred(Vlan_idContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return isVlanId(((Vlan_idContext)_localctx).v);
		}
		return true;
	}
	private boolean vni_number_sempred(Vni_numberContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return isVniNumber(((Vni_numberContext)_localctx).v);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3t\u0376\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\4f\tf\4g\tg\4h\th\4i\ti\4j\tj\4k\t"+
		"k\4l\tl\4m\tm\4n\tn\4o\to\4p\tp\4q\tq\4r\tr\4s\ts\4t\tt\4u\tu\4v\tv\4"+
		"w\tw\4x\tx\4y\ty\4z\tz\4{\t{\4|\t|\4}\t}\4~\t~\4\177\t\177\3\2\5\2\u0100"+
		"\n\2\3\2\6\2\u0103\n\2\r\2\16\2\u0104\3\2\5\2\u0108\n\2\3\2\3\2\3\3\3"+
		"\3\3\3\3\3\5\3\u0110\n\3\3\4\3\4\3\4\3\4\5\4\u0116\n\4\3\4\3\4\3\4\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5\u012c"+
		"\n\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6\u0135\n\6\3\7\3\7\3\7\3\b\3\b\3\b"+
		"\3\b\3\t\3\t\3\t\3\t\5\t\u0142\n\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13"+
		"\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\17\3\17"+
		"\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\5\21\u0166\n\21\3\22"+
		"\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\25\3\25\3\25"+
		"\3\26\3\26\3\26\3\27\3\27\3\27\5\27\u017d\n\27\3\30\3\30\3\30\3\30\3\31"+
		"\3\31\3\31\3\31\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34"+
		"\3\34\5\34\u0193\n\34\3\35\3\35\3\35\3\36\3\36\3\36\3\36\3\37\3\37\3\37"+
		"\3\37\3\37\3 \3 \3 \3!\3!\3!\3\"\3\"\3\"\5\"\u01aa\n\"\3#\3#\3#\5#\u01af"+
		"\n#\3$\3$\3$\5$\u01b4\n$\3$\3$\3%\3%\3%\3%\3&\3&\3&\3&\3\'\3\'\3\'\3\'"+
		"\3\'\3\'\3\'\3\'\3\'\3\'\5\'\u01ca\n\'\5\'\u01cc\n\'\3(\3(\3(\3(\3(\3"+
		")\3)\3)\3)\3)\3)\3*\3*\3*\3*\3+\3+\3+\3+\3,\3,\3,\3,\3-\3-\3-\3-\3-\3"+
		"-\5-\u01eb\n-\3.\3.\3.\3.\3.\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\61\3\61"+
		"\3\61\3\61\3\61\5\61\u01ff\n\61\3\62\3\62\3\62\3\62\5\62\u0205\n\62\3"+
		"\63\3\63\3\63\3\63\3\64\3\64\3\64\3\64\3\65\3\65\3\65\3\65\3\66\3\66\3"+
		"\66\5\66\u0216\n\66\3\67\3\67\3\67\38\38\38\39\39\39\59\u0221\n9\3:\3"+
		":\3:\3:\3;\3;\3;\3;\3<\3<\3<\3<\3<\3=\3=\3=\3=\5=\u0234\n=\3=\3=\3>\3"+
		">\3>\7>\u023b\n>\f>\16>\u023e\13>\3?\3?\3?\3?\5?\u0244\n?\3?\3?\5?\u0248"+
		"\n?\5?\u024a\n?\3@\3@\3A\3A\3B\3B\3C\3C\3D\3D\3E\3E\3F\3F\3G\7G\u025b"+
		"\nG\fG\16G\u025e\13G\3G\3G\3H\3H\3I\3I\3I\5I\u0267\nI\3J\3J\3J\7J\u026c"+
		"\nJ\fJ\16J\u026f\13J\3K\3K\3K\3L\3L\3L\3M\3M\3M\3N\3N\3N\5N\u027d\nN\3"+
		"O\3O\3O\7O\u0282\nO\fO\16O\u0285\13O\3P\3P\3P\3Q\3Q\3R\3R\3R\5R\u028f"+
		"\nR\3S\3S\3S\3S\3S\5S\u0296\nS\3T\3T\3T\3T\3U\3U\3U\3U\3U\5U\u02a1\nU"+
		"\3V\3V\3V\3V\3W\3W\3W\3W\5W\u02ab\nW\3W\3W\3X\3X\3X\3X\5X\u02b3\nX\3X"+
		"\3X\3Y\3Y\3Y\3Y\3Y\3Y\5Y\u02bd\nY\3Z\3Z\3Z\3[\3[\3[\3\\\3\\\3\\\3\\\3"+
		"\\\3]\3]\3]\3]\3^\3^\3^\3_\3_\3_\3_\3`\3`\3`\3`\5`\u02d9\n`\3a\3a\3a\3"+
		"a\3b\3b\3b\3b\3c\3c\3c\3c\3d\3d\3d\3d\3e\3e\3e\3e\3f\3f\3f\3f\7f\u02f3"+
		"\nf\ff\16f\u02f6\13f\3g\3g\3g\3g\3g\3g\3h\3h\3h\3i\7i\u0302\ni\fi\16i"+
		"\u0305\13i\3j\3j\3j\3k\3k\3k\3k\3k\3k\3k\5k\u0311\nk\3l\3l\3l\3l\5l\u0317"+
		"\nl\3m\3m\3m\3m\3n\3n\3n\3n\3o\3o\3o\3o\3p\3p\3p\3p\3p\5p\u032a\np\3q"+
		"\3q\3q\3q\5q\u0330\nq\3q\3q\3r\3r\3r\3r\3s\3s\3s\3s\3t\3t\3t\3t\3u\3u"+
		"\3u\3u\3u\3v\3v\3v\3v\3w\3w\3w\3w\3w\3w\5w\u034f\nw\3x\3x\3x\3x\3y\3y"+
		"\3y\3z\3z\5z\u035a\nz\3z\3z\3{\3{\3{\3{\3{\3|\3|\3|\3|\3|\3|\5|\u0369"+
		"\n|\3}\3}\3}\3~\3~\3~\3~\3\177\3\177\3\177\3\177\3\177\2\2\u0080\2\4\6"+
		"\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRT"+
		"VXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e"+
		"\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6"+
		"\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be"+
		"\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6"+
		"\u00d8\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u00ec\u00ee"+
		"\u00f0\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\2\t\4\2\17\17gg\3\2<=\4\2g"+
		"gmn\4\2\36\36??\3\2mm\3\2\3\3\t\2\r\r\31\31\33\33!\"((::``\2\u0355\2\u00ff"+
		"\3\2\2\2\4\u010f\3\2\2\2\6\u0111\3\2\2\2\b\u011a\3\2\2\2\n\u012d\3\2\2"+
		"\2\f\u0136\3\2\2\2\16\u0139\3\2\2\2\20\u013d\3\2\2\2\22\u0143\3\2\2\2"+
		"\24\u0147\3\2\2\2\26\u014b\3\2\2\2\30\u014f\3\2\2\2\32\u0154\3\2\2\2\34"+
		"\u0159\3\2\2\2\36\u015d\3\2\2\2 \u0160\3\2\2\2\"\u0167\3\2\2\2$\u016b"+
		"\3\2\2\2&\u016f\3\2\2\2(\u0173\3\2\2\2*\u0176\3\2\2\2,\u0179\3\2\2\2."+
		"\u017e\3\2\2\2\60\u0182\3\2\2\2\62\u0186\3\2\2\2\64\u0189\3\2\2\2\66\u018d"+
		"\3\2\2\28\u0194\3\2\2\2:\u0197\3\2\2\2<\u019b\3\2\2\2>\u01a0\3\2\2\2@"+
		"\u01a3\3\2\2\2B\u01a6\3\2\2\2D\u01ab\3\2\2\2F\u01b0\3\2\2\2H\u01b7\3\2"+
		"\2\2J\u01bb\3\2\2\2L\u01bf\3\2\2\2N\u01cd\3\2\2\2P\u01d2\3\2\2\2R\u01d8"+
		"\3\2\2\2T\u01dc\3\2\2\2V\u01e0\3\2\2\2X\u01e4\3\2\2\2Z\u01ec\3\2\2\2\\"+
		"\u01f1\3\2\2\2^\u01f5\3\2\2\2`\u01f9\3\2\2\2b\u0200\3\2\2\2d\u0206\3\2"+
		"\2\2f\u020a\3\2\2\2h\u020e\3\2\2\2j\u0212\3\2\2\2l\u0217\3\2\2\2n\u021a"+
		"\3\2\2\2p\u021d\3\2\2\2r\u0222\3\2\2\2t\u0226\3\2\2\2v\u022a\3\2\2\2x"+
		"\u022f\3\2\2\2z\u0237\3\2\2\2|\u0249\3\2\2\2~\u024b\3\2\2\2\u0080\u024d"+
		"\3\2\2\2\u0082\u024f\3\2\2\2\u0084\u0251\3\2\2\2\u0086\u0253\3\2\2\2\u0088"+
		"\u0255\3\2\2\2\u008a\u0257\3\2\2\2\u008c\u025c\3\2\2\2\u008e\u0261\3\2"+
		"\2\2\u0090\u0263\3\2\2\2\u0092\u0268\3\2\2\2\u0094\u0270\3\2\2\2\u0096"+
		"\u0273\3\2\2\2\u0098\u0276\3\2\2\2\u009a\u0279\3\2\2\2\u009c\u027e\3\2"+
		"\2\2\u009e\u0286\3\2\2\2\u00a0\u0289\3\2\2\2\u00a2\u028b\3\2\2\2\u00a4"+
		"\u0295\3\2\2\2\u00a6\u0297\3\2\2\2\u00a8\u029b\3\2\2\2\u00aa\u02a2\3\2"+
		"\2\2\u00ac\u02a6\3\2\2\2\u00ae\u02ae\3\2\2\2\u00b0\u02b6\3\2\2\2\u00b2"+
		"\u02be\3\2\2\2\u00b4\u02c1\3\2\2\2\u00b6\u02c4\3\2\2\2\u00b8\u02c9\3\2"+
		"\2\2\u00ba\u02cd\3\2\2\2\u00bc\u02d0\3\2\2\2\u00be\u02d4\3\2\2\2\u00c0"+
		"\u02da\3\2\2\2\u00c2\u02de\3\2\2\2\u00c4\u02e2\3\2\2\2\u00c6\u02e6\3\2"+
		"\2\2\u00c8\u02ea\3\2\2\2\u00ca\u02ee\3\2\2\2\u00cc\u02f7\3\2\2\2\u00ce"+
		"\u02fd\3\2\2\2\u00d0\u0303\3\2\2\2\u00d2\u0306\3\2\2\2\u00d4\u0309\3\2"+
		"\2\2\u00d6\u0312\3\2\2\2\u00d8\u0318\3\2\2\2\u00da\u031c\3\2\2\2\u00dc"+
		"\u0320\3\2\2\2\u00de\u0324\3\2\2\2\u00e0\u032b\3\2\2\2\u00e2\u0333\3\2"+
		"\2\2\u00e4\u0337\3\2\2\2\u00e6\u033b\3\2\2\2\u00e8\u033f\3\2\2\2\u00ea"+
		"\u0344\3\2\2\2\u00ec\u0348\3\2\2\2\u00ee\u0350\3\2\2\2\u00f0\u0354\3\2"+
		"\2\2\u00f2\u0357\3\2\2\2\u00f4\u035d\3\2\2\2\u00f6\u0362\3\2\2\2\u00f8"+
		"\u036a\3\2\2\2\u00fa\u036d\3\2\2\2\u00fc\u0371\3\2\2\2\u00fe\u0100\7m"+
		"\2\2\u00ff\u00fe\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0102\3\2\2\2\u0101"+
		"\u0103\5\4\3\2\u0102\u0101\3\2\2\2\u0103\u0104\3\2\2\2\u0104\u0102\3\2"+
		"\2\2\u0104\u0105\3\2\2\2\u0105\u0107\3\2\2\2\u0106\u0108\7m\2\2\u0107"+
		"\u0106\3\2\2\2\u0107\u0108\3\2\2\2\u0108\u0109\3\2\2\2\u0109\u010a\7\2"+
		"\2\3\u010a\3\3\2\2\2\u010b\u0110\5\6\4\2\u010c\u0110\5\b\5\2\u010d\u0110"+
		"\5v<\2\u010e\u0110\5x=\2\u010f\u010b\3\2\2\2\u010f\u010c\3\2\2\2\u010f"+
		"\u010d\3\2\2\2\u010f\u010e\3\2\2\2\u0110\5\3\2\2\2\u0111\u0115\7b\2\2"+
		"\u0112\u0116\5\u00ceh\2\u0113\u0116\5\u00caf\2\u0114\u0116\5\u00d2j\2"+
		"\u0115\u0112\3\2\2\2\u0115\u0113\3\2\2\2\u0115\u0114\3\2\2\2\u0116\u0117"+
		"\3\2\2\2\u0117\u0118\7\3\2\2\u0118\u0119\7m\2\2\u0119\7\3\2\2\2\u011a"+
		"\u011b\79\2\2\u011b\u012b\7\7\2\2\u011c\u012c\5\u00a2R\2\u011d\u012c\5"+
		"\n\6\2\u011e\u012c\5\36\20\2\u011f\u012c\5*\26\2\u0120\u012c\5\62\32\2"+
		"\u0121\u012c\5\64\33\2\u0122\u012c\5\u00d4k\2\u0123\u012c\5\66\34\2\u0124"+
		"\u012c\5> \2\u0125\u012c\5\u00ecw\2\u0126\u012c\5@!\2\u0127\u012c\5B\""+
		"\2\u0128\u012c\5L\'\2\u0129\u012c\5X-\2\u012a\u012c\5`\61\2\u012b\u011c"+
		"\3\2\2\2\u012b\u011d\3\2\2\2\u012b\u011e\3\2\2\2\u012b\u011f\3\2\2\2\u012b"+
		"\u0120\3\2\2\2\u012b\u0121\3\2\2\2\u012b\u0122\3\2\2\2\u012b\u0123\3\2"+
		"\2\2\u012b\u0124\3\2\2\2\u012b\u0125\3\2\2\2\u012b\u0126\3\2\2\2\u012b"+
		"\u0127\3\2\2\2\u012b\u0128\3\2\2\2\u012b\u0129\3\2\2\2\u012b\u012a\3\2"+
		"\2\2\u012c\t\3\2\2\2\u012d\u012e\7\23\2\2\u012e\u0134\5\u00a0Q\2\u012f"+
		"\u0135\5\f\7\2\u0130\u0135\5\20\t\2\u0131\u0135\5\30\r\2\u0132\u0135\5"+
		"\32\16\2\u0133\u0135\5\34\17\2\u0134\u012f\3\2\2\2\u0134\u0130\3\2\2\2"+
		"\u0134\u0131\3\2\2\2\u0134\u0132\3\2\2\2\u0134\u0133\3\2\2\2\u0135\13"+
		"\3\2\2\2\u0136\u0137\7\23\2\2\u0137\u0138\5\16\b\2\u0138\r\3\2\2\2\u0139"+
		"\u013a\7M\2\2\u013a\u013b\5z>\2\u013b\u013c\7m\2\2\u013c\17\3\2\2\2\u013d"+
		"\u0141\7\25\2\2\u013e\u0142\5\22\n\2\u013f\u0142\5\24\13\2\u0140\u0142"+
		"\5\26\f\2\u0141\u013e\3\2\2\2\u0141\u013f\3\2\2\2\u0141\u0140\3\2\2\2"+
		"\u0142\21\3\2\2\2\u0143\u0144\7\5\2\2\u0144\u0145\5\u0098M\2\u0145\u0146"+
		"\7m\2\2\u0146\23\3\2\2\2\u0147\u0148\7D\2\2\u0148\u0149\5\u0098M\2\u0149"+
		"\u014a\7m\2\2\u014a\25\3\2\2\2\u014b\u014c\7V\2\2\u014c\u014d\5\u009c"+
		"O\2\u014d\u014e\7m\2\2\u014e\27\3\2\2\2\u014f\u0150\7\26\2\2\u0150\u0151"+
		"\7\'\2\2\u0151\u0152\5\u0094K\2\u0152\u0153\7m\2\2\u0153\31\3\2\2\2\u0154"+
		"\u0155\7,\2\2\u0155\u0156\7\b\2\2\u0156\u0157\5\u0080A\2\u0157\u0158\7"+
		"m\2\2\u0158\33\3\2\2\2\u0159\u015a\7\\\2\2\u015a\u015b\5\u00a0Q\2\u015b"+
		"\u015c\7m\2\2\u015c\35\3\2\2\2\u015d\u015e\7\25\2\2\u015e\u015f\5 \21"+
		"\2\u015f\37\3\2\2\2\u0160\u0165\7\25\2\2\u0161\u0166\5\"\22\2\u0162\u0166"+
		"\5$\23\2\u0163\u0166\5&\24\2\u0164\u0166\5(\25\2\u0165\u0161\3\2\2\2\u0165"+
		"\u0162\3\2\2\2\u0165\u0163\3\2\2\2\u0165\u0164\3\2\2\2\u0166!\3\2\2\2"+
		"\u0167\u0168\7A\2\2\u0168\u0169\5z>\2\u0169\u016a\7m\2\2\u016a#\3\2\2"+
		"\2\u016b\u016c\7D\2\2\u016c\u016d\5\u0098M\2\u016d\u016e\7m\2\2\u016e"+
		"%\3\2\2\2\u016f\u0170\7V\2\2\u0170\u0171\5\u0092J\2\u0171\u0172\7m\2\2"+
		"\u0172\'\3\2\2\2\u0173\u0174\7X\2\2\u0174\u0175\7m\2\2\u0175)\3\2\2\2"+
		"\u0176\u0177\7\37\2\2\u0177\u0178\5,\27\2\u0178+\3\2\2\2\u0179\u017c\7"+
		"\66\2\2\u017a\u017d\5.\30\2\u017b\u017d\5\60\31\2\u017c\u017a\3\2\2\2"+
		"\u017c\u017b\3\2\2\2\u017d-\3\2\2\2\u017e\u017f\7-\2\2\u017f\u0180\5\u0082"+
		"B\2\u0180\u0181\7m\2\2\u0181/\3\2\2\2\u0182\u0183\7.\2\2\u0183\u0184\5"+
		"\u0086D\2\u0184\u0185\7m\2\2\u0185\61\3\2\2\2\u0186\u0187\7 \2\2\u0187"+
		"\u0188\5\u008cG\2\u0188\63\3\2\2\2\u0189\u018a\7%\2\2\u018a\u018b\5\u00a0"+
		"Q\2\u018b\u018c\7m\2\2\u018c\65\3\2\2\2\u018d\u018e\7\64\2\2\u018e\u0192"+
		"\7\61\2\2\u018f\u0193\58\35\2\u0190\u0193\5<\37\2\u0191\u0193\7m\2\2\u0192"+
		"\u018f\3\2\2\2\u0192\u0190\3\2\2\2\u0192\u0191\3\2\2\2\u0193\67\3\2\2"+
		"\2\u0194\u0195\7\26\2\2\u0195\u0196\5:\36\2\u01969\3\2\2\2\u0197\u0198"+
		"\7_\2\2\u0198\u0199\5\u0082B\2\u0199\u019a\7m\2\2\u019a;\3\2\2\2\u019b"+
		"\u019c\7,\2\2\u019c\u019d\7\b\2\2\u019d\u019e\5\u0080A\2\u019e\u019f\7"+
		"m\2\2\u019f=\3\2\2\2\u01a0\u01a1\7C\2\2\u01a1\u01a2\5\u008cG\2\u01a2?"+
		"\3\2\2\2\u01a3\u01a4\7N\2\2\u01a4\u01a5\5\u008cG\2\u01a5A\3\2\2\2\u01a6"+
		"\u01a9\7T\2\2\u01a7\u01aa\5D#\2\u01a8\u01aa\5J&\2\u01a9\u01a7\3\2\2\2"+
		"\u01a9\u01a8\3\2\2\2\u01aaC\3\2\2\2\u01ab\u01ae\7;\2\2\u01ac\u01af\5F"+
		"$\2\u01ad\u01af\5H%\2\u01ae\u01ac\3\2\2\2\u01ae\u01ad\3\2\2\2\u01afE\3"+
		"\2\2\2\u01b0\u01b1\7K\2\2\u01b1\u01b3\5\u00a0Q\2\u01b2\u01b4\7&\2\2\u01b3"+
		"\u01b2\3\2\2\2\u01b3\u01b4\3\2\2\2\u01b4\u01b5\3\2\2\2\u01b5\u01b6\7m"+
		"\2\2\u01b6G\3\2\2\2\u01b7\u01b8\7O\2\2\u01b8\u01b9\5\u00a0Q\2\u01b9\u01ba"+
		"\7m\2\2\u01baI\3\2\2\2\u01bb\u01bc\7a\2\2\u01bc\u01bd\5\u00a0Q\2\u01bd"+
		"\u01be\7m\2\2\u01beK\3\2\2\2\u01bf\u01cb\7W\2\2\u01c0\u01c1\5\u0094K\2"+
		"\u01c1\u01c2\5R*\2\u01c2\u01cc\3\2\2\2\u01c3\u01c9\5\u0092J\2\u01c4\u01ca"+
		"\5N(\2\u01c5\u01ca\5P)\2\u01c6\u01ca\5T+\2\u01c7\u01ca\5V,\2\u01c8\u01ca"+
		"\7m\2\2\u01c9\u01c4\3\2\2\2\u01c9\u01c5\3\2\2\2\u01c9\u01c6\3\2\2\2\u01c9"+
		"\u01c7\3\2\2\2\u01c9\u01c8\3\2\2\2\u01ca\u01cc\3\2\2\2\u01cb\u01c0\3\2"+
		"\2\2\u01cb\u01c3\3\2\2\2\u01ccM\3\2\2\2\u01cd\u01ce\7,\2\2\u01ce\u01cf"+
		"\7\b\2\2\u01cf\u01d0\5\u0080A\2\u01d0\u01d1\7m\2\2\u01d1O\3\2\2\2\u01d2"+
		"\u01d3\7,\2\2\u01d3\u01d4\7\t\2\2\u01d4\u01d5\5\u008aF\2\u01d5\u01d6\5"+
		"\u0080A\2\u01d6\u01d7\7m\2\2\u01d7Q\3\2\2\2\u01d8\u01d9\7Y\2\2\u01d9\u01da"+
		"\5\u0098M\2\u01da\u01db\7m\2\2\u01dbS\3\2\2\2\u01dc\u01dd\7Z\2\2\u01dd"+
		"\u01de\7\25\2\2\u01de\u01df\7m\2\2\u01dfU\3\2\2\2\u01e0\u01e1\7\\\2\2"+
		"\u01e1\u01e2\5\u00a0Q\2\u01e2\u01e3\7m\2\2\u01e3W\3\2\2\2\u01e4\u01e5"+
		"\7\\\2\2\u01e5\u01ea\5z>\2\u01e6\u01eb\5Z.\2\u01e7\u01eb\5\\/\2\u01e8"+
		"\u01eb\5^\60\2\u01e9\u01eb\7m\2\2\u01ea\u01e6\3\2\2\2\u01ea\u01e7\3\2"+
		"\2\2\u01ea\u01e8\3\2\2\2\u01ea\u01e9\3\2\2\2\u01ebY\3\2\2\2\u01ec\u01ed"+
		"\7,\2\2\u01ed\u01ee\7\b\2\2\u01ee\u01ef\5\u0080A\2\u01ef\u01f0\7m\2\2"+
		"\u01f0[\3\2\2\2\u01f1\u01f2\7[\2\2\u01f2\u01f3\5\u009eP\2\u01f3\u01f4"+
		"\7m\2\2\u01f4]\3\2\2\2\u01f5\u01f6\7]\2\2\u01f6\u01f7\t\2\2\2\u01f7\u01f8"+
		"\7m\2\2\u01f8_\3\2\2\2\u01f9\u01fa\7^\2\2\u01fa\u01fe\5z>\2\u01fb\u01ff"+
		"\5b\62\2\u01fc\u01ff\5j\66\2\u01fd\u01ff\5p9\2\u01fe\u01fb\3\2\2\2\u01fe"+
		"\u01fc\3\2\2\2\u01fe\u01fd\3\2\2\2\u01ffa\3\2\2\2\u0200\u0204\7\25\2\2"+
		"\u0201\u0205\5d\63\2\u0202\u0205\5f\64\2\u0203\u0205\5h\65\2\u0204\u0201"+
		"\3\2\2\2\u0204\u0202\3\2\2\2\u0204\u0203\3\2\2\2\u0205c\3\2\2\2\u0206"+
		"\u0207\7\5\2\2\u0207\u0208\5\u0098M\2\u0208\u0209\7m\2\2\u0209e\3\2\2"+
		"\2\u020a\u020b\7\16\2\2\u020b\u020c\t\3\2\2\u020c\u020d\7m\2\2\u020dg"+
		"\3\2\2\2\u020e\u020f\7\60\2\2\u020f\u0210\t\3\2\2\u0210\u0211\7m\2\2\u0211"+
		"i\3\2\2\2\u0212\u0215\7Q\2\2\u0213\u0216\5l\67\2\u0214\u0216\5n8\2\u0215"+
		"\u0213\3\2\2\2\u0215\u0214\3\2\2\2\u0216k\3\2\2\2\u0217\u0218\7\24\2\2"+
		"\u0218\u0219\7m\2\2\u0219m\3\2\2\2\u021a\u021b\7@\2\2\u021b\u021c\7m\2"+
		"\2\u021co\3\2\2\2\u021d\u0220\7^\2\2\u021e\u0221\5r:\2\u021f\u0221\5t"+
		";\2\u0220\u021e\3\2\2\2\u0220\u021f\3\2\2\2\u0221q\3\2\2\2\u0222\u0223"+
		"\7\'\2\2\u0223\u0224\5\u009eP\2\u0224\u0225\7m\2\2\u0225s\3\2\2\2\u0226"+
		"\u0227\7\62\2\2\u0227\u0228\5\u0082B\2\u0228\u0229\7m\2\2\u0229u\3\2\2"+
		"\2\u022a\u022b\79\2\2\u022b\u022c\7\7\2\2\u022c\u022d\5\u00a0Q\2\u022d"+
		"\u022e\5\u008cG\2\u022ew\3\2\2\2\u022f\u0233\79\2\2\u0230\u0234\3\2\2"+
		"\2\u0231\u0234\7\27\2\2\u0232\u0234\7\35\2\2\u0233\u0230\3\2\2\2\u0233"+
		"\u0231\3\2\2\2\u0233\u0232\3\2\2\2\u0234\u0235\3\2\2\2\u0235\u0236\5\u008c"+
		"G\2\u0236y\3\2\2\2\u0237\u023c\5|?\2\u0238\u0239\7c\2\2\u0239\u023b\5"+
		"|?\2\u023a\u0238\3\2\2\2\u023b\u023e\3\2\2\2\u023c\u023a\3\2\2\2\u023c"+
		"\u023d\3\2\2\2\u023d{\3\2\2\2\u023e\u023c\3\2\2\2\u023f\u024a\5~@\2\u0240"+
		"\u0243\5\u008eH\2\u0241\u0242\7f\2\2\u0242\u0244\5\u0094K\2\u0243\u0241"+
		"\3\2\2\2\u0243\u0244\3\2\2\2\u0244\u0247\3\2\2\2\u0245\u0246\7c\2\2\u0246"+
		"\u0248\5\u0092J\2\u0247\u0245\3\2\2\2\u0247\u0248\3\2\2\2\u0248\u024a"+
		"\3\2\2\2\u0249\u023f\3\2\2\2\u0249\u0240\3\2\2\2\u024a}\3\2\2\2\u024b"+
		"\u024c\n\4\2\2\u024c\177\3\2\2\2\u024d\u024e\7i\2\2\u024e\u0081\3\2\2"+
		"\2\u024f\u0250\7h\2\2\u0250\u0083\3\2\2\2\u0251\u0252\7i\2\2\u0252\u0085"+
		"\3\2\2\2\u0253\u0254\7j\2\2\u0254\u0087\3\2\2\2\u0255\u0256\t\5\2\2\u0256"+
		"\u0089\3\2\2\2\u0257\u0258\7l\2\2\u0258\u008b\3\2\2\2\u0259\u025b\n\6"+
		"\2\2\u025a\u0259\3\2\2\2\u025b\u025e\3\2\2\2\u025c\u025a\3\2\2\2\u025c"+
		"\u025d\3\2\2\2\u025d\u025f\3\2\2\2\u025e\u025c\3\2\2\2\u025f\u0260\7m"+
		"\2\2\u0260\u008d\3\2\2\2\u0261\u0262\7n\2\2\u0262\u008f\3\2\2\2\u0263"+
		"\u0266\5\u0094K\2\u0264\u0265\7f\2\2\u0265\u0267\5\u0094K\2\u0266\u0264"+
		"\3\2\2\2\u0266\u0267\3\2\2\2\u0267\u0091\3\2\2\2\u0268\u026d\5\u0090I"+
		"\2\u0269\u026a\7c\2\2\u026a\u026c\5\u0090I\2\u026b\u0269\3\2\2\2\u026c"+
		"\u026f\3\2\2\2\u026d\u026b\3\2\2\2\u026d\u026e\3\2\2\2\u026e\u0093\3\2"+
		"\2\2\u026f\u026d\3\2\2\2\u0270\u0271\7g\2\2\u0271\u0272\6K\2\3\u0272\u0095"+
		"\3\2\2\2\u0273\u0274\7g\2\2\u0274\u0275\6L\3\3\u0275\u0097\3\2\2\2\u0276"+
		"\u0277\7g\2\2\u0277\u0278\6M\4\3\u0278\u0099\3\2\2\2\u0279\u027c\5\u0098"+
		"M\2\u027a\u027b\7f\2\2\u027b\u027d\5\u0098M\2\u027c\u027a\3\2\2\2\u027c"+
		"\u027d\3\2\2\2\u027d\u009b\3\2\2\2\u027e\u0283\5\u009aN\2\u027f\u0280"+
		"\7c\2\2\u0280\u0282\5\u009aN\2\u0281\u027f\3\2\2\2\u0282\u0285\3\2\2\2"+
		"\u0283\u0281\3\2\2\2\u0283\u0284\3\2\2\2\u0284\u009d\3\2\2\2\u0285\u0283"+
		"\3\2\2\2\u0286\u0287\7g\2\2\u0287\u0288\6P\5\3\u0288\u009f\3\2\2\2\u0289"+
		"\u028a\n\6\2\2\u028a\u00a1\3\2\2\2\u028b\u028e\7\22\2\2\u028c\u028f\5"+
		"\u00a4S\2\u028d\u028f\5\u00c8e\2\u028e\u028c\3\2\2\2\u028e\u028d\3\2\2"+
		"\2\u028f\u00a3\3\2\2\2\u0290\u0296\5\u00a6T\2\u0291\u0296\5\u00a8U\2\u0292"+
		"\u0296\5\u00b0Y\2\u0293\u0296\5\u00bc_\2\u0294\u0296\5\u00c6d\2\u0295"+
		"\u0290\3\2\2\2\u0295\u0291\3\2\2\2\u0295\u0292\3\2\2\2\u0295\u0293\3\2"+
		"\2\2\u0295\u0294\3\2\2\2\u0296\u00a5\3\2\2\2\u0297\u0298\7\20\2\2\u0298"+
		"\u0299\5\u0096L\2\u0299\u029a\7m\2\2\u029a\u00a7\3\2\2\2\u029b\u029c\7"+
		"-\2\2\u029c\u02a0\7U\2\2\u029d\u02a1\5\u00aaV\2\u029e\u02a1\5\u00acW\2"+
		"\u029f\u02a1\5\u00aeX\2\u02a0\u029d\3\2\2\2\u02a0\u029e\3\2\2\2\u02a0"+
		"\u029f\3\2\2\2\u02a1\u00a9\3\2\2\2\u02a2\u02a3\78\2\2\u02a3\u02a4\5\u0084"+
		"C\2\u02a4\u02a5\7m\2\2\u02a5\u00ab\3\2\2\2\u02a6\u02a7\7E\2\2\u02a7\u02aa"+
		"\7\30\2\2\u02a8\u02a9\7H\2\2\u02a9\u02ab\5\u00a0Q\2\u02aa\u02a8\3\2\2"+
		"\2\u02aa\u02ab\3\2\2\2\u02ab\u02ac\3\2\2\2\u02ac\u02ad\7m\2\2\u02ad\u00ad"+
		"\3\2\2\2\u02ae\u02af\7E\2\2\u02af\u02b2\7P\2\2\u02b0\u02b1\7H\2\2\u02b1"+
		"\u02b3\5\u00a0Q\2\u02b2\u02b0\3\2\2\2\u02b2\u02b3\3\2\2\2\u02b3\u02b4"+
		"\3\2\2\2\u02b4\u02b5\7m\2\2\u02b5\u00af\3\2\2\2\u02b6\u02b7\7/\2\2\u02b7"+
		"\u02bc\7#\2\2\u02b8\u02bd\5\u00b2Z\2\u02b9\u02bd\5\u00b4[\2\u02ba\u02bd"+
		"\5\u00b6\\\2\u02bb\u02bd\5\u00b8]\2\u02bc\u02b8\3\2\2\2\u02bc\u02b9\3"+
		"\2\2\2\u02bc\u02ba\3\2\2\2\u02bc\u02bb\3\2\2\2\u02bd\u00b1\3\2\2\2\u02be"+
		"\u02bf\7\13\2\2\u02bf\u02c0\7m\2\2\u02c0\u00b3\3\2\2\2\u02c1\u02c2\7\f"+
		"\2\2\u02c2\u02c3\7m\2\2\u02c3\u00b5\3\2\2\2\u02c4\u02c5\7\n\2\2\u02c5"+
		"\u02c6\7-\2\2\u02c6\u02c7\7U\2\2\u02c7\u02c8\7m\2\2\u02c8\u00b7\3\2\2"+
		"\2\u02c9\u02ca\7\67\2\2\u02ca\u02cb\5\u00a0Q\2\u02cb\u02cc\5\u00ba^\2"+
		"\u02cc\u00b9\3\2\2\2\u02cd\u02ce\7\6\2\2\u02ce\u02cf\7m\2\2\u02cf\u00bb"+
		"\3\2\2\2\u02d0\u02d1\7\67\2\2\u02d1\u02d2\5\u00a0Q\2\u02d2\u02d3\5\u00be"+
		"`\2\u02d3\u00bd\3\2\2\2\u02d4\u02d8\7*\2\2\u02d5\u02d9\5\u00c0a\2\u02d6"+
		"\u02d9\5\u00c2b\2\u02d7\u02d9\5\u00c4c\2\u02d8\u02d5\3\2\2\2\u02d8\u02d6"+
		"\3\2\2\2\u02d8\u02d7\3\2\2\2\u02d9\u00bf\3\2\2\2\u02da\u02db\7F\2\2\u02db"+
		"\u02dc\7$\2\2\u02dc\u02dd\7m\2\2\u02dd\u00c1\3\2\2\2\u02de\u02df\7F\2"+
		"\2\u02df\u02e0\7+\2\2\u02e0\u02e1\7m\2\2\u02e1\u00c3\3\2\2\2\u02e2\u02e3"+
		"\7F\2\2\u02e3\u02e4\5\u0096L\2\u02e4\u02e5\7m\2\2\u02e5\u00c5\3\2\2\2"+
		"\u02e6\u02e7\7I\2\2\u02e7\u02e8\5\u0082B\2\u02e8\u02e9\7m\2\2\u02e9\u00c7"+
		"\3\2\2\2\u02ea\u02eb\7\\\2\2\u02eb\u02ec\5\u00a0Q\2\u02ec\u02ed\5\u00a4"+
		"S\2\u02ed\u00c9\3\2\2\2\u02ee\u02ef\7\\\2\2\u02ef\u02f0\5\u00a0Q\2\u02f0"+
		"\u02f4\7m\2\2\u02f1\u02f3\5\u00ccg\2\u02f2\u02f1\3\2\2\2\u02f3\u02f6\3"+
		"\2\2\2\u02f4\u02f2\3\2\2\2\u02f4\u02f5\3\2\2\2\u02f5\u00cb\3\2\2\2\u02f6"+
		"\u02f4\3\2\2\2\u02f7\u02f8\7,\2\2\u02f8\u02f9\7G\2\2\u02f9\u02fa\5\u0084"+
		"C\2\u02fa\u02fb\5\u0082B\2\u02fb\u02fc\7m\2\2\u02fc\u00cd\3\2\2\2\u02fd"+
		"\u02fe\7\4\2\2\u02fe\u02ff\5\u00d0i\2\u02ff\u00cf\3\2\2\2\u0300\u0302"+
		"\n\7\2\2\u0301\u0300\3\2\2\2\u0302\u0305\3\2\2\2\u0303\u0301\3\2\2\2\u0303"+
		"\u0304\3\2\2\2\u0304\u00d1\3\2\2\2\u0305\u0303\3\2\2\2\u0306\u0307\5\u00a0"+
		"Q\2\u0307\u0308\5\u00d0i\2\u0308\u00d3\3\2\2\2\u0309\u030a\7*\2\2\u030a"+
		"\u0310\5z>\2\u030b\u0311\5\u00d6l\2\u030c\u0311\5\u00dep\2\u030d\u0311"+
		"\5\u00e8u\2\u030e\u0311\5\u00eav\2\u030f\u0311\7m\2\2\u0310\u030b\3\2"+
		"\2\2\u0310\u030c\3\2\2\2\u0310\u030d\3\2\2\2\u0310\u030e\3\2\2\2\u0310"+
		"\u030f\3\2\2\2\u0311\u00d5\3\2\2\2\u0312\u0316\7\25\2\2\u0313\u0317\5"+
		"\u00d8m\2\u0314\u0317\5\u00dan\2\u0315\u0317\5\u00dco\2\u0316\u0313\3"+
		"\2\2\2\u0316\u0314\3\2\2\2\u0316\u0315\3\2\2\2\u0317\u00d7\3\2\2\2\u0318"+
		"\u0319\7\5\2\2\u0319\u031a\5\u0098M\2\u031a\u031b\7m\2\2\u031b\u00d9\3"+
		"\2\2\2\u031c\u031d\7D\2\2\u031d\u031e\5\u0098M\2\u031e\u031f\7m\2\2\u031f"+
		"\u00db\3\2\2\2\u0320\u0321\7V\2\2\u0321\u0322\5\u009cO\2\u0322\u0323\7"+
		"m\2\2\u0323\u00dd\3\2\2\2\u0324\u0329\7\26\2\2\u0325\u032a\5\u00e0q\2"+
		"\u0326\u032a\5\u00e2r\2\u0327\u032a\5\u00e4s\2\u0328\u032a\5\u00e6t\2"+
		"\u0329\u0325\3\2\2\2\u0329\u0326\3\2\2\2\u0329\u0327\3\2\2\2\u0329\u0328"+
		"\3\2\2\2\u032a\u00df\3\2\2\2\u032b\u032c\7\21\2\2\u032c\u032f\5\u0082"+
		"B\2\u032d\u032e\7\\\2\2\u032e\u0330\5\u00a0Q\2\u032f\u032d\3\2\2\2\u032f"+
		"\u0330\3\2\2\2\u0330\u0331\3\2\2\2\u0331\u0332\7m\2\2\u0332\u00e1\3\2"+
		"\2\2\u0333\u0334\7>\2\2\u0334\u0335\5\u0082B\2\u0335\u0336\7m\2\2\u0336"+
		"\u00e3\3\2\2\2\u0337\u0338\7B\2\2\u0338\u0339\5\u0094K\2\u0339\u033a\7"+
		"m\2\2\u033a\u00e5\3\2\2\2\u033b\u033c\7R\2\2\u033c\u033d\5\u008aF\2\u033d"+
		"\u033e\7m\2\2\u033e\u00e7\3\2\2\2\u033f\u0340\7,\2\2\u0340\u0341\7\b\2"+
		"\2\u0341\u0342\5\u0080A\2\u0342\u0343\7m\2\2\u0343\u00e9\3\2\2\2\u0344"+
		"\u0345\7\\\2\2\u0345\u0346\5\u00a0Q\2\u0346\u0347\7m\2\2\u0347\u00eb\3"+
		"\2\2\2\u0348\u034e\7J\2\2\u0349\u034f\5\u00eex\2\u034a\u034f\5\u00f0y"+
		"\2\u034b\u034f\5\u00f4{\2\u034c\u034f\5\u00f6|\2\u034d\u034f\5\u00fc\177"+
		"\2\u034e\u0349\3\2\2\2\u034e\u034a\3\2\2\2\u034e\u034b\3\2\2\2\u034e\u034c"+
		"\3\2\2\2\u034e\u034d\3\2\2\2\u034f\u00ed\3\2\2\2\u0350\u0351\7\34\2\2"+
		"\u0351\u0352\7\32\2\2\u0352\u0353\7m\2\2\u0353\u00ef\3\2\2\2\u0354\u0355"+
		"\7\63\2\2\u0355\u0356\5\u00f2z\2\u0356\u00f1\3\2\2\2\u0357\u0359\7S\2"+
		"\2\u0358\u035a\t\b\2\2\u0359\u0358\3\2\2\2\u0359\u035a\3\2\2\2\u035a\u035b"+
		"\3\2\2\2\u035b\u035c\7m\2\2\u035c\u00f3\3\2\2\2\u035d\u035e\7G\2\2\u035e"+
		"\u035f\5\u0084C\2\u035f\u0360\5\u0082B\2\u0360\u0361\7m\2\2\u0361\u00f5"+
		"\3\2\2\2\u0362\u0363\7H\2\2\u0363\u0364\5\u00a0Q\2\u0364\u0365\5\u0088"+
		"E\2\u0365\u0368\5\u0094K\2\u0366\u0369\5\u00f8}\2\u0367\u0369\7m\2\2\u0368"+
		"\u0366\3\2\2\2\u0368\u0367\3\2\2\2\u0369\u00f7\3\2\2\2\u036a\u036b\7\65"+
		"\2\2\u036b\u036c\5\u00fa~\2\u036c\u00f9\3\2\2\2\u036d\u036e\7*\2\2\u036e"+
		"\u036f\5z>\2\u036f\u0370\7m\2\2\u0370\u00fb\3\2\2\2\u0371\u0372\7L\2\2"+
		"\u0372\u0373\7)\2\2\u0373\u0374\7m\2\2\u0374\u00fd\3\2\2\2\61\u00ff\u0104"+
		"\u0107\u010f\u0115\u012b\u0134\u0141\u0165\u017c\u0192\u01a9\u01ae\u01b3"+
		"\u01c9\u01cb\u01ea\u01fe\u0204\u0215\u0220\u0233\u023c\u0243\u0247\u0249"+
		"\u025c\u0266\u026d\u027c\u0283\u028e\u0295\u02a0\u02aa\u02b2\u02bc\u02d8"+
		"\u02f4\u0303\u0310\u0316\u0329\u032f\u034e\u0359\u0368";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}