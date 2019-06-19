// Generated from org/batfish/grammar/palo_alto/PaloAltoParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.palo_alto;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class PaloAltoParser extends org.batfish.grammar.BatfishParser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ACTION=1, ADDRESS=2, ADDRESS_GROUP=3, ADMIN_DIST=4, AES_128_CBC=5, AES_128_GCM=6, 
		AES_192_CBC=7, AES_256_CBC=8, AES_256_GCM=9, ALLOW=10, ANY=11, APPLICATION=12, 
		APPLICATION_GROUP=13, AUTHENTICATION=14, AUTHENTICATION_TYPE=15, AUTO=16, 
		BGP=17, BOTNET=18, CATEGORY=19, CLOSE_BRACKET=20, COMMENT=21, CONFIG=22, 
		CRYPTO_PROFILES=23, DAMPENING_PROFILE=24, DAYS=25, DEFAULT_GATEWAY=26, 
		DENY=27, DES=28, DESCRIPTION=29, DESTINATION=30, DEVICES=31, DEVICECONFIG=32, 
		DH_GROUP=33, DISABLED=34, DISPLAY_NAME=35, DNS=36, DNS_SETTING=37, DOWN=38, 
		DROP=39, DYNAMIC=40, ENABLE=41, ENCRYPTION=42, ESP=43, EXTERNAL=44, ETHERNET=45, 
		FQDN=46, FROM=47, GATEWAY=48, GLOBAL_PROTECT_APP_CRYPTO_PROFILES=49, GROUP1=50, 
		GROUP2=51, GROUP5=52, GROUP14=53, GROUP19=54, GROUP20=55, HASH=56, HIP_PROFILES=57, 
		HOSTNAME=58, HOURS=59, ICMP=60, IKE=61, IKE_CRYPTO_PROFILES=62, IMPORT=63, 
		INTERFACE=64, IP=65, IP_ADDRESS_LITERAL=66, IP_NETMASK=67, IP_RANGE_LITERAL=68, 
		IPSEC_CRYPTO_PROFILES=69, IPV6=70, LAYER2=71, LAYER3=72, LIFETIME=73, 
		LINK_STATE=74, LLDP=75, LOG_SETTINGS=76, LOOPBACK=77, MD5=78, MINUTES=79, 
		MEMBERS=80, METRIC=81, MGT_CONFIG=82, MTU=83, NDP_PROXY=84, NEGATE_DESTINATION=85, 
		NEGATE_SOURCE=86, NETMASK=87, NETWORK=88, NEXT_VR=89, NEXTHOP=90, NO=91, 
		NONE=92, NTP_SERVER_ADDRESS=93, NTP_SERVERS=94, NULL=95, OPEN_BRACKET=96, 
		PANORAMA=97, PANORAMA_SERVER=98, POLICY=99, PORT=100, POST_RULEBASE=101, 
		PRE_RULEBASE=102, PRIMARY=103, PRIMARY_NTP_SERVER=104, PROFILES=105, PROTOCOL=106, 
		QOS=107, RESET_BOTH=108, RESET_CLIENT=109, RESET_SERVER=110, ROUTING_TABLE=111, 
		RULEBASE=112, RULES=113, SCTP=114, SECONDARY=115, SECONDARY_NTP_SERVER=116, 
		SECONDS=117, SECURITY=118, SERVER=119, SERVERS=120, SERVICE=121, SERVICE_GROUP=122, 
		SET=123, SETTING=124, SHA1=125, SHA256=126, SHA384=127, SHA512=128, SHARED=129, 
		SHARED_GATEWAY=130, SOURCE=131, SOURCE_PORT=132, SOURCE_USER=133, STATIC=134, 
		STATIC_ROUTE=135, SYSLOG=136, SYSTEM=137, TAG=138, TAP=139, TCP=140, THREE_DES=141, 
		TIMEZONE=142, TO=143, TUNNEL=144, TYPE=145, UDP=146, UNITS=147, UP=148, 
		UPDATE_SCHEDULE=149, UPDATE_SERVER=150, VIRTUAL_ROUTER=151, VIRTUAL_WIRE=152, 
		VISIBLE_VSYS=153, VLAN=154, VSYS=155, YES=156, ZONE=157, COMMA=158, DASH=159, 
		DEC=160, DOUBLE_QUOTED_STRING=161, IP_ADDRESS=162, IP_PREFIX=163, IP_RANGE=164, 
		LINE_COMMENT=165, NEWLINE=166, RANGE=167, SINGLE_QUOTED_STRING=168, VARIABLE=169, 
		WS=170;
	public static final int
		RULE_palo_alto_configuration = 0, RULE_newline = 1, RULE_s_null = 2, RULE_set_line_config_devices = 3, 
		RULE_set_line_config_general = 4, RULE_statement_config_devices = 5, RULE_statement_config_general = 6, 
		RULE_set_line = 7, RULE_set_line_tail = 8, RULE_s_policy = 9, RULE_s_policy_panorama = 10, 
		RULE_s_policy_shared = 11, RULE_panorama_post_rulebase = 12, RULE_panorama_pre_rulebase = 13, 
		RULE_s_address = 14, RULE_s_address_definition = 15, RULE_sa_description = 16, 
		RULE_sa_fqdn = 17, RULE_sa_ip_netmask = 18, RULE_sa_ip_range = 19, RULE_sa_null = 20, 
		RULE_null_rest_of_line = 21, RULE_src_or_dst_list = 22, RULE_src_or_dst_list_item = 23, 
		RULE_port_or_range = 24, RULE_variable_port_list = 25, RULE_variable_list = 26, 
		RULE_variable_list_item = 27, RULE_variable = 28, RULE_s_address_group = 29, 
		RULE_s_address_group_definition = 30, RULE_sag_description = 31, RULE_sag_dynamic = 32, 
		RULE_sag_null = 33, RULE_sag_static = 34, RULE_s_application = 35, RULE_s_application_definition = 36, 
		RULE_sapp_description = 37, RULE_s_application_group = 38, RULE_sappg_definition = 39, 
		RULE_sappg_members = 40, RULE_s_deviceconfig = 41, RULE_sd_null = 42, 
		RULE_sd_system = 43, RULE_sds_default_gateway = 44, RULE_sds_dns_setting = 45, 
		RULE_sds_hostname = 46, RULE_sds_ip_address = 47, RULE_sds_netmask = 48, 
		RULE_sds_ntp_servers = 49, RULE_sds_null = 50, RULE_sdsd_servers = 51, 
		RULE_sdsn_ntp_server_address = 52, RULE_sn_interface = 53, RULE_if_common = 54, 
		RULE_if_comment = 55, RULE_if_tag = 56, RULE_sni_ethernet = 57, RULE_sni_ethernet_definition = 58, 
		RULE_sni_loopback = 59, RULE_sni_tunnel = 60, RULE_sni_vlan = 61, RULE_snie_layer2 = 62, 
		RULE_snie_layer3 = 63, RULE_snie_link_state = 64, RULE_snie_tap = 65, 
		RULE_snie_virtual_wire = 66, RULE_sniel2_unit = 67, RULE_sniel2_units = 68, 
		RULE_sniel3_common = 69, RULE_sniel3_ip = 70, RULE_sniel3_mtu = 71, RULE_sniel3_null = 72, 
		RULE_sniel3_unit = 73, RULE_sniel3_units = 74, RULE_snil_unit = 75, RULE_snil_units = 76, 
		RULE_snit_unit = 77, RULE_snit_units = 78, RULE_sniv_unit = 79, RULE_sniv_units = 80, 
		RULE_cp_authentication = 81, RULE_cp_dh_group = 82, RULE_cp_encryption = 83, 
		RULE_cp_encryption_algo = 84, RULE_cp_hash = 85, RULE_cp_lifetime = 86, 
		RULE_s_network = 87, RULE_sn_ike = 88, RULE_sn_ike_crypto_profiles = 89, 
		RULE_sn_ike_gateway = 90, RULE_sn_profiles = 91, RULE_sn_qos = 92, RULE_sn_shared_gateway = 93, 
		RULE_sn_shared_gateway_definition = 94, RULE_snsg_display_name = 95, RULE_snsg_import = 96, 
		RULE_snsgi_interface = 97, RULE_snsg_zone = 98, RULE_snsg_zone_definition = 99, 
		RULE_snsgz_network = 100, RULE_snsgzn_layer3 = 101, RULE_sn_virtual_router = 102, 
		RULE_sn_virtual_router_definition = 103, RULE_snicp_global_protect = 104, 
		RULE_snicp_ike_crypto_profiles = 105, RULE_snicp_ipsec_crypto_profiles = 106, 
		RULE_snvr_interface = 107, RULE_snvr_protocol = 108, RULE_snvr_routing_table = 109, 
		RULE_snvrp_bgp = 110, RULE_snvrp_bgp_enable = 111, RULE_snvrp_bgp_null = 112, 
		RULE_snvrrt_admin_dist = 113, RULE_snvrrt_destination = 114, RULE_snvrrt_interface = 115, 
		RULE_snvrrt_metric = 116, RULE_snvrrt_nexthop = 117, RULE_snvrrtn_ip = 118, 
		RULE_snvrrtn_next_vr = 119, RULE_s_rulebase = 120, RULE_rulebase_inner = 121, 
		RULE_sr_security = 122, RULE_sr_security_rules = 123, RULE_srs_definition = 124, 
		RULE_srs_action = 125, RULE_srs_application = 126, RULE_srs_category = 127, 
		RULE_srs_description = 128, RULE_srs_destination = 129, RULE_srs_disabled = 130, 
		RULE_srs_from = 131, RULE_srs_hip_profiles = 132, RULE_srs_negate_destination = 133, 
		RULE_srs_negate_source = 134, RULE_srs_service = 135, RULE_srs_source = 136, 
		RULE_srs_source_user = 137, RULE_srs_to = 138, RULE_s_service = 139, RULE_s_service_definition = 140, 
		RULE_sserv_description = 141, RULE_sserv_port = 142, RULE_sserv_protocol = 143, 
		RULE_sserv_source_port = 144, RULE_s_service_group = 145, RULE_s_service_group_definition = 146, 
		RULE_sservgrp_members = 147, RULE_s_shared = 148, RULE_ss_common = 149, 
		RULE_ss_log_settings = 150, RULE_ss_null = 151, RULE_ssl_syslog = 152, 
		RULE_ssls_server = 153, RULE_sslss_server = 154, RULE_s_vsys = 155, RULE_s_vsys_definition = 156, 
		RULE_sv_import = 157, RULE_svi_network = 158, RULE_svi_visible_vsys = 159, 
		RULE_svin_interface = 160, RULE_s_zone = 161, RULE_s_zone_definition = 162, 
		RULE_sz_network = 163, RULE_szn_external = 164, RULE_szn_layer2 = 165, 
		RULE_szn_layer3 = 166, RULE_szn_tap = 167, RULE_szn_virtual_wire = 168;
	private static String[] makeRuleNames() {
		return new String[] {
			"palo_alto_configuration", "newline", "s_null", "set_line_config_devices", 
			"set_line_config_general", "statement_config_devices", "statement_config_general", 
			"set_line", "set_line_tail", "s_policy", "s_policy_panorama", "s_policy_shared", 
			"panorama_post_rulebase", "panorama_pre_rulebase", "s_address", "s_address_definition", 
			"sa_description", "sa_fqdn", "sa_ip_netmask", "sa_ip_range", "sa_null", 
			"null_rest_of_line", "src_or_dst_list", "src_or_dst_list_item", "port_or_range", 
			"variable_port_list", "variable_list", "variable_list_item", "variable", 
			"s_address_group", "s_address_group_definition", "sag_description", "sag_dynamic", 
			"sag_null", "sag_static", "s_application", "s_application_definition", 
			"sapp_description", "s_application_group", "sappg_definition", "sappg_members", 
			"s_deviceconfig", "sd_null", "sd_system", "sds_default_gateway", "sds_dns_setting", 
			"sds_hostname", "sds_ip_address", "sds_netmask", "sds_ntp_servers", "sds_null", 
			"sdsd_servers", "sdsn_ntp_server_address", "sn_interface", "if_common", 
			"if_comment", "if_tag", "sni_ethernet", "sni_ethernet_definition", "sni_loopback", 
			"sni_tunnel", "sni_vlan", "snie_layer2", "snie_layer3", "snie_link_state", 
			"snie_tap", "snie_virtual_wire", "sniel2_unit", "sniel2_units", "sniel3_common", 
			"sniel3_ip", "sniel3_mtu", "sniel3_null", "sniel3_unit", "sniel3_units", 
			"snil_unit", "snil_units", "snit_unit", "snit_units", "sniv_unit", "sniv_units", 
			"cp_authentication", "cp_dh_group", "cp_encryption", "cp_encryption_algo", 
			"cp_hash", "cp_lifetime", "s_network", "sn_ike", "sn_ike_crypto_profiles", 
			"sn_ike_gateway", "sn_profiles", "sn_qos", "sn_shared_gateway", "sn_shared_gateway_definition", 
			"snsg_display_name", "snsg_import", "snsgi_interface", "snsg_zone", "snsg_zone_definition", 
			"snsgz_network", "snsgzn_layer3", "sn_virtual_router", "sn_virtual_router_definition", 
			"snicp_global_protect", "snicp_ike_crypto_profiles", "snicp_ipsec_crypto_profiles", 
			"snvr_interface", "snvr_protocol", "snvr_routing_table", "snvrp_bgp", 
			"snvrp_bgp_enable", "snvrp_bgp_null", "snvrrt_admin_dist", "snvrrt_destination", 
			"snvrrt_interface", "snvrrt_metric", "snvrrt_nexthop", "snvrrtn_ip", 
			"snvrrtn_next_vr", "s_rulebase", "rulebase_inner", "sr_security", "sr_security_rules", 
			"srs_definition", "srs_action", "srs_application", "srs_category", "srs_description", 
			"srs_destination", "srs_disabled", "srs_from", "srs_hip_profiles", "srs_negate_destination", 
			"srs_negate_source", "srs_service", "srs_source", "srs_source_user", 
			"srs_to", "s_service", "s_service_definition", "sserv_description", "sserv_port", 
			"sserv_protocol", "sserv_source_port", "s_service_group", "s_service_group_definition", 
			"sservgrp_members", "s_shared", "ss_common", "ss_log_settings", "ss_null", 
			"ssl_syslog", "ssls_server", "sslss_server", "s_vsys", "s_vsys_definition", 
			"sv_import", "svi_network", "svi_visible_vsys", "svin_interface", "s_zone", 
			"s_zone_definition", "sz_network", "szn_external", "szn_layer2", "szn_layer3", 
			"szn_tap", "szn_virtual_wire"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'action'", "'address'", "'address-group'", "'admin-dist'", "'aes-128-cbc'", 
			"'aes-128-gcm'", "'aes-192-cbc'", "'aes-256-cbc'", "'aes-256-gcm'", "'allow'", 
			"'any'", "'application'", "'application-group'", "'authentication'", 
			"'authentication-type'", "'auto'", "'bgp'", "'botnet'", "'category'", 
			"']'", "'comment'", "'config'", "'crypto-profiles'", "'dampening-profile'", 
			"'days'", "'default-gateway'", "'deny'", "'des'", "'description'", "'destination'", 
			"'devices'", "'deviceconfig'", "'dh-group'", "'disabled'", "'display-name'", 
			"'dns'", "'dns-setting'", "'down'", "'drop'", "'dynamic'", "'enable'", 
			"'encryption'", "'esp'", "'external'", "'ethernet'", "'fqdn'", "'from'", 
			"'gateway'", "'global-protect-app-crypto-profiles'", "'group1'", "'group2'", 
			"'group5'", "'group14'", "'group19'", "'group20'", "'hash'", "'hip-profiles'", 
			"'hostname'", "'hours'", "'icmp'", "'ike'", "'ike-crypto-profiles'", 
			"'import'", "'interface'", "'ip'", "'ip-address'", "'ip-netmask'", "'ip-range'", 
			"'ipsec-crypto-profiles'", "'ipv6'", "'layer2'", "'layer3'", "'lifetime'", 
			"'link-state'", "'lldp'", "'log-settings'", "'loopback'", "'md5'", "'minutes'", 
			"'members'", "'metric'", "'mgt-config'", "'mtu'", "'ndp-proxy'", "'negate-destination'", 
			"'negate-source'", "'netmask'", "'network'", "'next-vr'", "'nexthop'", 
			"'no'", "'none'", "'ntp-server-address'", "'ntp-servers'", "'null'", 
			"'['", "'panorama'", "'panorama-server'", "'policy'", "'port'", "'post-rulebase'", 
			"'pre-rulebase'", "'primary'", "'primary-ntp-server'", "'profiles'", 
			"'protocol'", "'qos'", "'reset-both'", "'reset-client'", "'reset-server'", 
			"'routing-table'", "'rulebase'", "'rules'", "'sctp'", "'secondary'", 
			"'secondary-ntp-server'", "'seconds'", "'security'", "'server'", "'servers'", 
			"'service'", "'service-group'", "'set'", "'setting'", "'sha1'", "'sha256'", 
			"'sha384'", "'sha512'", "'shared'", "'shared-gateway'", "'source'", "'source-port'", 
			"'source-user'", "'static'", "'static-route'", "'syslog'", "'system'", 
			"'tag'", "'tap'", "'tcp'", "'3des'", "'timezone'", "'to'", "'tunnel'", 
			"'type'", "'udp'", "'units'", "'up'", "'update-schedule'", "'update-server'", 
			"'virtual-router'", "'virtual-wire'", "'visible-vsys'", "'vlan'", "'vsys'", 
			"'yes'", "'zone'", "','", "'-'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ACTION", "ADDRESS", "ADDRESS_GROUP", "ADMIN_DIST", "AES_128_CBC", 
			"AES_128_GCM", "AES_192_CBC", "AES_256_CBC", "AES_256_GCM", "ALLOW", 
			"ANY", "APPLICATION", "APPLICATION_GROUP", "AUTHENTICATION", "AUTHENTICATION_TYPE", 
			"AUTO", "BGP", "BOTNET", "CATEGORY", "CLOSE_BRACKET", "COMMENT", "CONFIG", 
			"CRYPTO_PROFILES", "DAMPENING_PROFILE", "DAYS", "DEFAULT_GATEWAY", "DENY", 
			"DES", "DESCRIPTION", "DESTINATION", "DEVICES", "DEVICECONFIG", "DH_GROUP", 
			"DISABLED", "DISPLAY_NAME", "DNS", "DNS_SETTING", "DOWN", "DROP", "DYNAMIC", 
			"ENABLE", "ENCRYPTION", "ESP", "EXTERNAL", "ETHERNET", "FQDN", "FROM", 
			"GATEWAY", "GLOBAL_PROTECT_APP_CRYPTO_PROFILES", "GROUP1", "GROUP2", 
			"GROUP5", "GROUP14", "GROUP19", "GROUP20", "HASH", "HIP_PROFILES", "HOSTNAME", 
			"HOURS", "ICMP", "IKE", "IKE_CRYPTO_PROFILES", "IMPORT", "INTERFACE", 
			"IP", "IP_ADDRESS_LITERAL", "IP_NETMASK", "IP_RANGE_LITERAL", "IPSEC_CRYPTO_PROFILES", 
			"IPV6", "LAYER2", "LAYER3", "LIFETIME", "LINK_STATE", "LLDP", "LOG_SETTINGS", 
			"LOOPBACK", "MD5", "MINUTES", "MEMBERS", "METRIC", "MGT_CONFIG", "MTU", 
			"NDP_PROXY", "NEGATE_DESTINATION", "NEGATE_SOURCE", "NETMASK", "NETWORK", 
			"NEXT_VR", "NEXTHOP", "NO", "NONE", "NTP_SERVER_ADDRESS", "NTP_SERVERS", 
			"NULL", "OPEN_BRACKET", "PANORAMA", "PANORAMA_SERVER", "POLICY", "PORT", 
			"POST_RULEBASE", "PRE_RULEBASE", "PRIMARY", "PRIMARY_NTP_SERVER", "PROFILES", 
			"PROTOCOL", "QOS", "RESET_BOTH", "RESET_CLIENT", "RESET_SERVER", "ROUTING_TABLE", 
			"RULEBASE", "RULES", "SCTP", "SECONDARY", "SECONDARY_NTP_SERVER", "SECONDS", 
			"SECURITY", "SERVER", "SERVERS", "SERVICE", "SERVICE_GROUP", "SET", "SETTING", 
			"SHA1", "SHA256", "SHA384", "SHA512", "SHARED", "SHARED_GATEWAY", "SOURCE", 
			"SOURCE_PORT", "SOURCE_USER", "STATIC", "STATIC_ROUTE", "SYSLOG", "SYSTEM", 
			"TAG", "TAP", "TCP", "THREE_DES", "TIMEZONE", "TO", "TUNNEL", "TYPE", 
			"UDP", "UNITS", "UP", "UPDATE_SCHEDULE", "UPDATE_SERVER", "VIRTUAL_ROUTER", 
			"VIRTUAL_WIRE", "VISIBLE_VSYS", "VLAN", "VSYS", "YES", "ZONE", "COMMA", 
			"DASH", "DEC", "DOUBLE_QUOTED_STRING", "IP_ADDRESS", "IP_PREFIX", "IP_RANGE", 
			"LINE_COMMENT", "NEWLINE", "RANGE", "SINGLE_QUOTED_STRING", "VARIABLE", 
			"WS"
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
	public String getGrammarFileName() { return "PaloAltoParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PaloAltoParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class Palo_alto_configurationContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(PaloAltoParser.EOF, 0); }
		public List<Set_lineContext> set_line() {
			return getRuleContexts(Set_lineContext.class);
		}
		public Set_lineContext set_line(int i) {
			return getRuleContext(Set_lineContext.class,i);
		}
		public List<NewlineContext> newline() {
			return getRuleContexts(NewlineContext.class);
		}
		public NewlineContext newline(int i) {
			return getRuleContext(NewlineContext.class,i);
		}
		public Palo_alto_configurationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_palo_alto_configuration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterPalo_alto_configuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitPalo_alto_configuration(this);
		}
	}

	public final Palo_alto_configurationContext palo_alto_configuration() throws RecognitionException {
		Palo_alto_configurationContext _localctx = new Palo_alto_configurationContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_palo_alto_configuration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(340); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(340);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SET:
					{
					setState(338);
					set_line();
					}
					break;
				case NEWLINE:
					{
					setState(339);
					newline();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(342); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==SET || _la==NEWLINE );
			setState(344);
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

	public static class NewlineContext extends ParserRuleContext {
		public TerminalNode NEWLINE() { return getToken(PaloAltoParser.NEWLINE, 0); }
		public NewlineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_newline; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterNewline(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitNewline(this);
		}
	}

	public final NewlineContext newline() throws RecognitionException {
		NewlineContext _localctx = new NewlineContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_newline);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(346);
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

	public static class S_nullContext extends ParserRuleContext {
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public TerminalNode MGT_CONFIG() { return getToken(PaloAltoParser.MGT_CONFIG, 0); }
		public TerminalNode TAG() { return getToken(PaloAltoParser.TAG, 0); }
		public S_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_null(this);
		}
	}

	public final S_nullContext s_null() throws RecognitionException {
		S_nullContext _localctx = new S_nullContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_s_null);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(348);
			_la = _input.LA(1);
			if ( !(_la==MGT_CONFIG || _la==TAG) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(349);
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

	public static class Set_line_config_devicesContext extends ParserRuleContext {
		public VariableContext name;
		public Statement_config_devicesContext statement_config_devices() {
			return getRuleContext(Statement_config_devicesContext.class,0);
		}
		public TerminalNode CONFIG() { return getToken(PaloAltoParser.CONFIG, 0); }
		public TerminalNode DEVICES() { return getToken(PaloAltoParser.DEVICES, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Set_line_config_devicesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set_line_config_devices; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSet_line_config_devices(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSet_line_config_devices(this);
		}
	}

	public final Set_line_config_devicesContext set_line_config_devices() throws RecognitionException {
		Set_line_config_devicesContext _localctx = new Set_line_config_devicesContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_set_line_config_devices);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONFIG) {
				{
				setState(351);
				match(CONFIG);
				setState(352);
				match(DEVICES);
				setState(353);
				((Set_line_config_devicesContext)_localctx).name = variable();
				}
			}

			setState(356);
			statement_config_devices();
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

	public static class Set_line_config_generalContext extends ParserRuleContext {
		public Statement_config_generalContext statement_config_general() {
			return getRuleContext(Statement_config_generalContext.class,0);
		}
		public TerminalNode CONFIG() { return getToken(PaloAltoParser.CONFIG, 0); }
		public Set_line_config_generalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set_line_config_general; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSet_line_config_general(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSet_line_config_general(this);
		}
	}

	public final Set_line_config_generalContext set_line_config_general() throws RecognitionException {
		Set_line_config_generalContext _localctx = new Set_line_config_generalContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_set_line_config_general);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(359);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CONFIG) {
				{
				setState(358);
				match(CONFIG);
				}
			}

			setState(361);
			statement_config_general();
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

	public static class Statement_config_devicesContext extends ParserRuleContext {
		public S_addressContext s_address() {
			return getRuleContext(S_addressContext.class,0);
		}
		public S_address_groupContext s_address_group() {
			return getRuleContext(S_address_groupContext.class,0);
		}
		public S_applicationContext s_application() {
			return getRuleContext(S_applicationContext.class,0);
		}
		public S_deviceconfigContext s_deviceconfig() {
			return getRuleContext(S_deviceconfigContext.class,0);
		}
		public S_networkContext s_network() {
			return getRuleContext(S_networkContext.class,0);
		}
		public S_nullContext s_null() {
			return getRuleContext(S_nullContext.class,0);
		}
		public S_rulebaseContext s_rulebase() {
			return getRuleContext(S_rulebaseContext.class,0);
		}
		public S_serviceContext s_service() {
			return getRuleContext(S_serviceContext.class,0);
		}
		public S_service_groupContext s_service_group() {
			return getRuleContext(S_service_groupContext.class,0);
		}
		public S_vsysContext s_vsys() {
			return getRuleContext(S_vsysContext.class,0);
		}
		public S_zoneContext s_zone() {
			return getRuleContext(S_zoneContext.class,0);
		}
		public Statement_config_devicesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement_config_devices; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterStatement_config_devices(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitStatement_config_devices(this);
		}
	}

	public final Statement_config_devicesContext statement_config_devices() throws RecognitionException {
		Statement_config_devicesContext _localctx = new Statement_config_devicesContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_statement_config_devices);
		try {
			setState(374);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ADDRESS:
				enterOuterAlt(_localctx, 1);
				{
				setState(363);
				s_address();
				}
				break;
			case ADDRESS_GROUP:
				enterOuterAlt(_localctx, 2);
				{
				setState(364);
				s_address_group();
				}
				break;
			case APPLICATION:
				enterOuterAlt(_localctx, 3);
				{
				setState(365);
				s_application();
				}
				break;
			case DEVICECONFIG:
				enterOuterAlt(_localctx, 4);
				{
				setState(366);
				s_deviceconfig();
				}
				break;
			case NETWORK:
				enterOuterAlt(_localctx, 5);
				{
				setState(367);
				s_network();
				}
				break;
			case MGT_CONFIG:
			case TAG:
				enterOuterAlt(_localctx, 6);
				{
				setState(368);
				s_null();
				}
				break;
			case RULEBASE:
				enterOuterAlt(_localctx, 7);
				{
				setState(369);
				s_rulebase();
				}
				break;
			case SERVICE:
				enterOuterAlt(_localctx, 8);
				{
				setState(370);
				s_service();
				}
				break;
			case SERVICE_GROUP:
				enterOuterAlt(_localctx, 9);
				{
				setState(371);
				s_service_group();
				}
				break;
			case VSYS:
				enterOuterAlt(_localctx, 10);
				{
				setState(372);
				s_vsys();
				}
				break;
			case ZONE:
				enterOuterAlt(_localctx, 11);
				{
				setState(373);
				s_zone();
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

	public static class Statement_config_generalContext extends ParserRuleContext {
		public S_sharedContext s_shared() {
			return getRuleContext(S_sharedContext.class,0);
		}
		public Statement_config_generalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement_config_general; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterStatement_config_general(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitStatement_config_general(this);
		}
	}

	public final Statement_config_generalContext statement_config_general() throws RecognitionException {
		Statement_config_generalContext _localctx = new Statement_config_generalContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_statement_config_general);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(376);
			s_shared();
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

	public static class Set_lineContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(PaloAltoParser.SET, 0); }
		public Set_line_tailContext set_line_tail() {
			return getRuleContext(Set_line_tailContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(PaloAltoParser.NEWLINE, 0); }
		public Set_lineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSet_line(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSet_line(this);
		}
	}

	public final Set_lineContext set_line() throws RecognitionException {
		Set_lineContext _localctx = new Set_lineContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_set_line);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(378);
			match(SET);
			setState(379);
			set_line_tail();
			setState(380);
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

	public static class Set_line_tailContext extends ParserRuleContext {
		public Set_line_config_devicesContext set_line_config_devices() {
			return getRuleContext(Set_line_config_devicesContext.class,0);
		}
		public Set_line_config_generalContext set_line_config_general() {
			return getRuleContext(Set_line_config_generalContext.class,0);
		}
		public S_policyContext s_policy() {
			return getRuleContext(S_policyContext.class,0);
		}
		public Set_line_tailContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set_line_tail; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSet_line_tail(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSet_line_tail(this);
		}
	}

	public final Set_line_tailContext set_line_tail() throws RecognitionException {
		Set_line_tailContext _localctx = new Set_line_tailContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_set_line_tail);
		try {
			setState(385);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(382);
				set_line_config_devices();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(383);
				set_line_config_general();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(384);
				s_policy();
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

	public static class S_policyContext extends ParserRuleContext {
		public TerminalNode POLICY() { return getToken(PaloAltoParser.POLICY, 0); }
		public S_policy_panoramaContext s_policy_panorama() {
			return getRuleContext(S_policy_panoramaContext.class,0);
		}
		public S_policy_sharedContext s_policy_shared() {
			return getRuleContext(S_policy_sharedContext.class,0);
		}
		public S_policyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_policy; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_policy(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_policy(this);
		}
	}

	public final S_policyContext s_policy() throws RecognitionException {
		S_policyContext _localctx = new S_policyContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_s_policy);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(387);
			match(POLICY);
			setState(390);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PANORAMA:
				{
				setState(388);
				s_policy_panorama();
				}
				break;
			case SHARED:
				{
				setState(389);
				s_policy_shared();
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

	public static class S_policy_panoramaContext extends ParserRuleContext {
		public TerminalNode PANORAMA() { return getToken(PaloAltoParser.PANORAMA, 0); }
		public Ss_commonContext ss_common() {
			return getRuleContext(Ss_commonContext.class,0);
		}
		public Panorama_post_rulebaseContext panorama_post_rulebase() {
			return getRuleContext(Panorama_post_rulebaseContext.class,0);
		}
		public Panorama_pre_rulebaseContext panorama_pre_rulebase() {
			return getRuleContext(Panorama_pre_rulebaseContext.class,0);
		}
		public S_policy_panoramaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_policy_panorama; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_policy_panorama(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_policy_panorama(this);
		}
	}

	public final S_policy_panoramaContext s_policy_panorama() throws RecognitionException {
		S_policy_panoramaContext _localctx = new S_policy_panoramaContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_s_policy_panorama);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(392);
			match(PANORAMA);
			setState(396);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ADDRESS:
			case ADDRESS_GROUP:
			case APPLICATION:
			case APPLICATION_GROUP:
			case LOG_SETTINGS:
			case SERVICE:
			case SERVICE_GROUP:
				{
				setState(393);
				ss_common();
				}
				break;
			case POST_RULEBASE:
				{
				setState(394);
				panorama_post_rulebase();
				}
				break;
			case PRE_RULEBASE:
				{
				setState(395);
				panorama_pre_rulebase();
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

	public static class S_policy_sharedContext extends ParserRuleContext {
		public TerminalNode SHARED() { return getToken(PaloAltoParser.SHARED, 0); }
		public S_policy_sharedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_policy_shared; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_policy_shared(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_policy_shared(this);
		}
	}

	public final S_policy_sharedContext s_policy_shared() throws RecognitionException {
		S_policy_sharedContext _localctx = new S_policy_sharedContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_s_policy_shared);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(398);
			match(SHARED);
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

	public static class Panorama_post_rulebaseContext extends ParserRuleContext {
		public TerminalNode POST_RULEBASE() { return getToken(PaloAltoParser.POST_RULEBASE, 0); }
		public Rulebase_innerContext rulebase_inner() {
			return getRuleContext(Rulebase_innerContext.class,0);
		}
		public Panorama_post_rulebaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_panorama_post_rulebase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterPanorama_post_rulebase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitPanorama_post_rulebase(this);
		}
	}

	public final Panorama_post_rulebaseContext panorama_post_rulebase() throws RecognitionException {
		Panorama_post_rulebaseContext _localctx = new Panorama_post_rulebaseContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_panorama_post_rulebase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(400);
			match(POST_RULEBASE);
			setState(401);
			rulebase_inner();
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

	public static class Panorama_pre_rulebaseContext extends ParserRuleContext {
		public TerminalNode PRE_RULEBASE() { return getToken(PaloAltoParser.PRE_RULEBASE, 0); }
		public Rulebase_innerContext rulebase_inner() {
			return getRuleContext(Rulebase_innerContext.class,0);
		}
		public Panorama_pre_rulebaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_panorama_pre_rulebase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterPanorama_pre_rulebase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitPanorama_pre_rulebase(this);
		}
	}

	public final Panorama_pre_rulebaseContext panorama_pre_rulebase() throws RecognitionException {
		Panorama_pre_rulebaseContext _localctx = new Panorama_pre_rulebaseContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_panorama_pre_rulebase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(403);
			match(PRE_RULEBASE);
			setState(404);
			rulebase_inner();
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

	public static class S_addressContext extends ParserRuleContext {
		public TerminalNode ADDRESS() { return getToken(PaloAltoParser.ADDRESS, 0); }
		public S_address_definitionContext s_address_definition() {
			return getRuleContext(S_address_definitionContext.class,0);
		}
		public S_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_address(this);
		}
	}

	public final S_addressContext s_address() throws RecognitionException {
		S_addressContext _localctx = new S_addressContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_s_address);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(406);
			match(ADDRESS);
			setState(408);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(407);
				s_address_definition();
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

	public static class S_address_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sa_descriptionContext sa_description() {
			return getRuleContext(Sa_descriptionContext.class,0);
		}
		public Sa_fqdnContext sa_fqdn() {
			return getRuleContext(Sa_fqdnContext.class,0);
		}
		public Sa_ip_netmaskContext sa_ip_netmask() {
			return getRuleContext(Sa_ip_netmaskContext.class,0);
		}
		public Sa_ip_rangeContext sa_ip_range() {
			return getRuleContext(Sa_ip_rangeContext.class,0);
		}
		public Sa_nullContext sa_null() {
			return getRuleContext(Sa_nullContext.class,0);
		}
		public S_address_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_address_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_address_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_address_definition(this);
		}
	}

	public final S_address_definitionContext s_address_definition() throws RecognitionException {
		S_address_definitionContext _localctx = new S_address_definitionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_s_address_definition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(410);
			((S_address_definitionContext)_localctx).name = variable();
			setState(416);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DESCRIPTION:
				{
				setState(411);
				sa_description();
				}
				break;
			case FQDN:
				{
				setState(412);
				sa_fqdn();
				}
				break;
			case IP_NETMASK:
				{
				setState(413);
				sa_ip_netmask();
				}
				break;
			case IP_RANGE_LITERAL:
				{
				setState(414);
				sa_ip_range();
				}
				break;
			case TAG:
				{
				setState(415);
				sa_null();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Sa_descriptionContext extends ParserRuleContext {
		public VariableContext description;
		public TerminalNode DESCRIPTION() { return getToken(PaloAltoParser.DESCRIPTION, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sa_descriptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sa_description; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSa_description(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSa_description(this);
		}
	}

	public final Sa_descriptionContext sa_description() throws RecognitionException {
		Sa_descriptionContext _localctx = new Sa_descriptionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_sa_description);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(418);
			match(DESCRIPTION);
			setState(419);
			((Sa_descriptionContext)_localctx).description = variable();
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

	public static class Sa_fqdnContext extends ParserRuleContext {
		public TerminalNode FQDN() { return getToken(PaloAltoParser.FQDN, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Sa_fqdnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sa_fqdn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSa_fqdn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSa_fqdn(this);
		}
	}

	public final Sa_fqdnContext sa_fqdn() throws RecognitionException {
		Sa_fqdnContext _localctx = new Sa_fqdnContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_sa_fqdn);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(421);
			match(FQDN);
			setState(422);
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

	public static class Sa_ip_netmaskContext extends ParserRuleContext {
		public TerminalNode IP_NETMASK() { return getToken(PaloAltoParser.IP_NETMASK, 0); }
		public TerminalNode IP_ADDRESS() { return getToken(PaloAltoParser.IP_ADDRESS, 0); }
		public TerminalNode IP_PREFIX() { return getToken(PaloAltoParser.IP_PREFIX, 0); }
		public Sa_ip_netmaskContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sa_ip_netmask; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSa_ip_netmask(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSa_ip_netmask(this);
		}
	}

	public final Sa_ip_netmaskContext sa_ip_netmask() throws RecognitionException {
		Sa_ip_netmaskContext _localctx = new Sa_ip_netmaskContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_sa_ip_netmask);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(424);
			match(IP_NETMASK);
			setState(425);
			_la = _input.LA(1);
			if ( !(_la==IP_ADDRESS || _la==IP_PREFIX) ) {
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

	public static class Sa_ip_rangeContext extends ParserRuleContext {
		public TerminalNode IP_RANGE_LITERAL() { return getToken(PaloAltoParser.IP_RANGE_LITERAL, 0); }
		public TerminalNode IP_RANGE() { return getToken(PaloAltoParser.IP_RANGE, 0); }
		public Sa_ip_rangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sa_ip_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSa_ip_range(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSa_ip_range(this);
		}
	}

	public final Sa_ip_rangeContext sa_ip_range() throws RecognitionException {
		Sa_ip_rangeContext _localctx = new Sa_ip_rangeContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_sa_ip_range);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(427);
			match(IP_RANGE_LITERAL);
			setState(428);
			match(IP_RANGE);
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

	public static class Sa_nullContext extends ParserRuleContext {
		public TerminalNode TAG() { return getToken(PaloAltoParser.TAG, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Sa_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sa_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSa_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSa_null(this);
		}
	}

	public final Sa_nullContext sa_null() throws RecognitionException {
		Sa_nullContext _localctx = new Sa_nullContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_sa_null);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			match(TAG);
			setState(431);
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

	public static class Null_rest_of_lineContext extends ParserRuleContext {
		public List<TerminalNode> NEWLINE() { return getTokens(PaloAltoParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(PaloAltoParser.NEWLINE, i);
		}
		public Null_rest_of_lineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_null_rest_of_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterNull_rest_of_line(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitNull_rest_of_line(this);
		}
	}

	public final Null_rest_of_lineContext null_rest_of_line() throws RecognitionException {
		Null_rest_of_lineContext _localctx = new Null_rest_of_lineContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_null_rest_of_line);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(436);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				{
				setState(433);
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
				setState(438);
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

	public static class Src_or_dst_listContext extends ParserRuleContext {
		public List<Src_or_dst_list_itemContext> src_or_dst_list_item() {
			return getRuleContexts(Src_or_dst_list_itemContext.class);
		}
		public Src_or_dst_list_itemContext src_or_dst_list_item(int i) {
			return getRuleContext(Src_or_dst_list_itemContext.class,i);
		}
		public TerminalNode OPEN_BRACKET() { return getToken(PaloAltoParser.OPEN_BRACKET, 0); }
		public TerminalNode CLOSE_BRACKET() { return getToken(PaloAltoParser.CLOSE_BRACKET, 0); }
		public Src_or_dst_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_src_or_dst_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrc_or_dst_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrc_or_dst_list(this);
		}
	}

	public final Src_or_dst_listContext src_or_dst_list() throws RecognitionException {
		Src_or_dst_listContext _localctx = new Src_or_dst_listContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_src_or_dst_list);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(448);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(439);
				src_or_dst_list_item();
				}
				break;
			case 2:
				{
				{
				setState(440);
				match(OPEN_BRACKET);
				setState(444);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(441);
						src_or_dst_list_item();
						}
						} 
					}
					setState(446);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				}
				setState(447);
				match(CLOSE_BRACKET);
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

	public static class Src_or_dst_list_itemContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode ANY() { return getToken(PaloAltoParser.ANY, 0); }
		public TerminalNode IP_ADDRESS() { return getToken(PaloAltoParser.IP_ADDRESS, 0); }
		public TerminalNode IP_PREFIX() { return getToken(PaloAltoParser.IP_PREFIX, 0); }
		public TerminalNode IP_RANGE() { return getToken(PaloAltoParser.IP_RANGE, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Src_or_dst_list_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_src_or_dst_list_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrc_or_dst_list_item(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrc_or_dst_list_item(this);
		}
	}

	public final Src_or_dst_list_itemContext src_or_dst_list_item() throws RecognitionException {
		Src_or_dst_list_itemContext _localctx = new Src_or_dst_list_itemContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_src_or_dst_list_item);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(455);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				{
				setState(450);
				match(ANY);
				}
				break;
			case 2:
				{
				setState(451);
				match(IP_ADDRESS);
				}
				break;
			case 3:
				{
				setState(452);
				match(IP_PREFIX);
				}
				break;
			case 4:
				{
				setState(453);
				match(IP_RANGE);
				}
				break;
			case 5:
				{
				setState(454);
				((Src_or_dst_list_itemContext)_localctx).name = variable();
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

	public static class Port_or_rangeContext extends ParserRuleContext {
		public Token range;
		public Token port;
		public TerminalNode RANGE() { return getToken(PaloAltoParser.RANGE, 0); }
		public TerminalNode DEC() { return getToken(PaloAltoParser.DEC, 0); }
		public Port_or_rangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_port_or_range; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterPort_or_range(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitPort_or_range(this);
		}
	}

	public final Port_or_rangeContext port_or_range() throws RecognitionException {
		Port_or_rangeContext _localctx = new Port_or_rangeContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_port_or_range);
		try {
			setState(459);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case RANGE:
				enterOuterAlt(_localctx, 1);
				{
				setState(457);
				((Port_or_rangeContext)_localctx).range = match(RANGE);
				}
				break;
			case DEC:
				enterOuterAlt(_localctx, 2);
				{
				setState(458);
				((Port_or_rangeContext)_localctx).port = match(DEC);
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

	public static class Variable_port_listContext extends ParserRuleContext {
		public List<Port_or_rangeContext> port_or_range() {
			return getRuleContexts(Port_or_rangeContext.class);
		}
		public Port_or_rangeContext port_or_range(int i) {
			return getRuleContext(Port_or_rangeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PaloAltoParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PaloAltoParser.COMMA, i);
		}
		public Variable_port_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable_port_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterVariable_port_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitVariable_port_list(this);
		}
	}

	public final Variable_port_listContext variable_port_list() throws RecognitionException {
		Variable_port_listContext _localctx = new Variable_port_listContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_variable_port_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(461);
			port_or_range();
			setState(466);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(462);
				match(COMMA);
				setState(463);
				port_or_range();
				}
				}
				setState(468);
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

	public static class Variable_listContext extends ParserRuleContext {
		public List<Variable_list_itemContext> variable_list_item() {
			return getRuleContexts(Variable_list_itemContext.class);
		}
		public Variable_list_itemContext variable_list_item(int i) {
			return getRuleContext(Variable_list_itemContext.class,i);
		}
		public TerminalNode OPEN_BRACKET() { return getToken(PaloAltoParser.OPEN_BRACKET, 0); }
		public TerminalNode CLOSE_BRACKET() { return getToken(PaloAltoParser.CLOSE_BRACKET, 0); }
		public Variable_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterVariable_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitVariable_list(this);
		}
	}

	public final Variable_listContext variable_list() throws RecognitionException {
		Variable_listContext _localctx = new Variable_listContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_variable_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(478);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(469);
				variable_list_item();
				}
				break;
			case 2:
				{
				{
				setState(470);
				match(OPEN_BRACKET);
				setState(474);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
					{
					{
					setState(471);
					variable_list_item();
					}
					}
					setState(476);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(477);
				match(CLOSE_BRACKET);
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

	public static class Variable_list_itemContext extends ParserRuleContext {
		public TerminalNode CLOSE_BRACKET() { return getToken(PaloAltoParser.CLOSE_BRACKET, 0); }
		public TerminalNode NEWLINE() { return getToken(PaloAltoParser.NEWLINE, 0); }
		public Variable_list_itemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable_list_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterVariable_list_item(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitVariable_list_item(this);
		}
	}

	public final Variable_list_itemContext variable_list_item() throws RecognitionException {
		Variable_list_itemContext _localctx = new Variable_list_itemContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_variable_list_item);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(480);
			_la = _input.LA(1);
			if ( _la <= 0 || (_la==CLOSE_BRACKET || _la==NEWLINE) ) {
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

	public static class VariableContext extends ParserRuleContext {
		public TerminalNode NEWLINE() { return getToken(PaloAltoParser.NEWLINE, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitVariable(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_variable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(482);
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

	public static class S_address_groupContext extends ParserRuleContext {
		public TerminalNode ADDRESS_GROUP() { return getToken(PaloAltoParser.ADDRESS_GROUP, 0); }
		public S_address_group_definitionContext s_address_group_definition() {
			return getRuleContext(S_address_group_definitionContext.class,0);
		}
		public S_address_groupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_address_group; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_address_group(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_address_group(this);
		}
	}

	public final S_address_groupContext s_address_group() throws RecognitionException {
		S_address_groupContext _localctx = new S_address_groupContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_s_address_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(484);
			match(ADDRESS_GROUP);
			setState(486);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(485);
				s_address_group_definition();
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

	public static class S_address_group_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sag_descriptionContext sag_description() {
			return getRuleContext(Sag_descriptionContext.class,0);
		}
		public Sag_dynamicContext sag_dynamic() {
			return getRuleContext(Sag_dynamicContext.class,0);
		}
		public Sag_nullContext sag_null() {
			return getRuleContext(Sag_nullContext.class,0);
		}
		public Sag_staticContext sag_static() {
			return getRuleContext(Sag_staticContext.class,0);
		}
		public S_address_group_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_address_group_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_address_group_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_address_group_definition(this);
		}
	}

	public final S_address_group_definitionContext s_address_group_definition() throws RecognitionException {
		S_address_group_definitionContext _localctx = new S_address_group_definitionContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_s_address_group_definition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(488);
			((S_address_group_definitionContext)_localctx).name = variable();
			setState(493);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DESCRIPTION:
				{
				setState(489);
				sag_description();
				}
				break;
			case DYNAMIC:
				{
				setState(490);
				sag_dynamic();
				}
				break;
			case TAG:
				{
				setState(491);
				sag_null();
				}
				break;
			case STATIC:
				{
				setState(492);
				sag_static();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Sag_descriptionContext extends ParserRuleContext {
		public VariableContext description;
		public TerminalNode DESCRIPTION() { return getToken(PaloAltoParser.DESCRIPTION, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sag_descriptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sag_description; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSag_description(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSag_description(this);
		}
	}

	public final Sag_descriptionContext sag_description() throws RecognitionException {
		Sag_descriptionContext _localctx = new Sag_descriptionContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_sag_description);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(495);
			match(DESCRIPTION);
			setState(496);
			((Sag_descriptionContext)_localctx).description = variable();
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

	public static class Sag_dynamicContext extends ParserRuleContext {
		public TerminalNode DYNAMIC() { return getToken(PaloAltoParser.DYNAMIC, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Sag_dynamicContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sag_dynamic; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSag_dynamic(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSag_dynamic(this);
		}
	}

	public final Sag_dynamicContext sag_dynamic() throws RecognitionException {
		Sag_dynamicContext _localctx = new Sag_dynamicContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_sag_dynamic);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(498);
			match(DYNAMIC);
			setState(499);
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

	public static class Sag_nullContext extends ParserRuleContext {
		public TerminalNode TAG() { return getToken(PaloAltoParser.TAG, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Sag_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sag_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSag_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSag_null(this);
		}
	}

	public final Sag_nullContext sag_null() throws RecognitionException {
		Sag_nullContext _localctx = new Sag_nullContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_sag_null);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(501);
			match(TAG);
			setState(502);
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

	public static class Sag_staticContext extends ParserRuleContext {
		public TerminalNode STATIC() { return getToken(PaloAltoParser.STATIC, 0); }
		public List<VariableContext> variable() {
			return getRuleContexts(VariableContext.class);
		}
		public VariableContext variable(int i) {
			return getRuleContext(VariableContext.class,i);
		}
		public TerminalNode OPEN_BRACKET() { return getToken(PaloAltoParser.OPEN_BRACKET, 0); }
		public TerminalNode CLOSE_BRACKET() { return getToken(PaloAltoParser.CLOSE_BRACKET, 0); }
		public Sag_staticContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sag_static; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSag_static(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSag_static(this);
		}
	}

	public final Sag_staticContext sag_static() throws RecognitionException {
		Sag_staticContext _localctx = new Sag_staticContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_sag_static);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(504);
			match(STATIC);
			setState(514);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				{
				setState(505);
				variable();
				}
				break;
			case 2:
				{
				{
				setState(506);
				match(OPEN_BRACKET);
				setState(510);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(507);
						variable();
						}
						} 
					}
					setState(512);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				}
				setState(513);
				match(CLOSE_BRACKET);
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

	public static class S_applicationContext extends ParserRuleContext {
		public TerminalNode APPLICATION() { return getToken(PaloAltoParser.APPLICATION, 0); }
		public S_application_definitionContext s_application_definition() {
			return getRuleContext(S_application_definitionContext.class,0);
		}
		public S_applicationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_application; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_application(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_application(this);
		}
	}

	public final S_applicationContext s_application() throws RecognitionException {
		S_applicationContext _localctx = new S_applicationContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_s_application);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(516);
			match(APPLICATION);
			setState(518);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(517);
				s_application_definition();
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

	public static class S_application_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sapp_descriptionContext sapp_description() {
			return getRuleContext(Sapp_descriptionContext.class,0);
		}
		public S_application_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_application_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_application_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_application_definition(this);
		}
	}

	public final S_application_definitionContext s_application_definition() throws RecognitionException {
		S_application_definitionContext _localctx = new S_application_definitionContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_s_application_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(520);
			((S_application_definitionContext)_localctx).name = variable();
			setState(522);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DESCRIPTION) {
				{
				setState(521);
				sapp_description();
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

	public static class Sapp_descriptionContext extends ParserRuleContext {
		public VariableContext description;
		public TerminalNode DESCRIPTION() { return getToken(PaloAltoParser.DESCRIPTION, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sapp_descriptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sapp_description; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSapp_description(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSapp_description(this);
		}
	}

	public final Sapp_descriptionContext sapp_description() throws RecognitionException {
		Sapp_descriptionContext _localctx = new Sapp_descriptionContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_sapp_description);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(524);
			match(DESCRIPTION);
			setState(525);
			((Sapp_descriptionContext)_localctx).description = variable();
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

	public static class S_application_groupContext extends ParserRuleContext {
		public TerminalNode APPLICATION_GROUP() { return getToken(PaloAltoParser.APPLICATION_GROUP, 0); }
		public Sappg_definitionContext sappg_definition() {
			return getRuleContext(Sappg_definitionContext.class,0);
		}
		public S_application_groupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_application_group; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_application_group(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_application_group(this);
		}
	}

	public final S_application_groupContext s_application_group() throws RecognitionException {
		S_application_groupContext _localctx = new S_application_groupContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_s_application_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(527);
			match(APPLICATION_GROUP);
			setState(529);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(528);
				sappg_definition();
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

	public static class Sappg_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sappg_membersContext sappg_members() {
			return getRuleContext(Sappg_membersContext.class,0);
		}
		public Sappg_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sappg_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSappg_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSappg_definition(this);
		}
	}

	public final Sappg_definitionContext sappg_definition() throws RecognitionException {
		Sappg_definitionContext _localctx = new Sappg_definitionContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_sappg_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(531);
			((Sappg_definitionContext)_localctx).name = variable();
			setState(533);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MEMBERS) {
				{
				setState(532);
				sappg_members();
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

	public static class Sappg_membersContext extends ParserRuleContext {
		public TerminalNode MEMBERS() { return getToken(PaloAltoParser.MEMBERS, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Sappg_membersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sappg_members; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSappg_members(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSappg_members(this);
		}
	}

	public final Sappg_membersContext sappg_members() throws RecognitionException {
		Sappg_membersContext _localctx = new Sappg_membersContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_sappg_members);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(535);
			match(MEMBERS);
			setState(537);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(536);
				variable_list();
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

	public static class S_deviceconfigContext extends ParserRuleContext {
		public TerminalNode DEVICECONFIG() { return getToken(PaloAltoParser.DEVICECONFIG, 0); }
		public Sd_nullContext sd_null() {
			return getRuleContext(Sd_nullContext.class,0);
		}
		public Sd_systemContext sd_system() {
			return getRuleContext(Sd_systemContext.class,0);
		}
		public S_deviceconfigContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_deviceconfig; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_deviceconfig(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_deviceconfig(this);
		}
	}

	public final S_deviceconfigContext s_deviceconfig() throws RecognitionException {
		S_deviceconfigContext _localctx = new S_deviceconfigContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_s_deviceconfig);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(539);
			match(DEVICECONFIG);
			setState(542);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SETTING:
				{
				setState(540);
				sd_null();
				}
				break;
			case SYSTEM:
				{
				setState(541);
				sd_system();
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

	public static class Sd_nullContext extends ParserRuleContext {
		public TerminalNode SETTING() { return getToken(PaloAltoParser.SETTING, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Sd_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sd_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSd_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSd_null(this);
		}
	}

	public final Sd_nullContext sd_null() throws RecognitionException {
		Sd_nullContext _localctx = new Sd_nullContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_sd_null);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(544);
			match(SETTING);
			setState(545);
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

	public static class Sd_systemContext extends ParserRuleContext {
		public TerminalNode SYSTEM() { return getToken(PaloAltoParser.SYSTEM, 0); }
		public Sds_default_gatewayContext sds_default_gateway() {
			return getRuleContext(Sds_default_gatewayContext.class,0);
		}
		public Sds_dns_settingContext sds_dns_setting() {
			return getRuleContext(Sds_dns_settingContext.class,0);
		}
		public Sds_hostnameContext sds_hostname() {
			return getRuleContext(Sds_hostnameContext.class,0);
		}
		public Sds_ip_addressContext sds_ip_address() {
			return getRuleContext(Sds_ip_addressContext.class,0);
		}
		public Sds_netmaskContext sds_netmask() {
			return getRuleContext(Sds_netmaskContext.class,0);
		}
		public Sds_ntp_serversContext sds_ntp_servers() {
			return getRuleContext(Sds_ntp_serversContext.class,0);
		}
		public Sds_nullContext sds_null() {
			return getRuleContext(Sds_nullContext.class,0);
		}
		public Sd_systemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sd_system; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSd_system(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSd_system(this);
		}
	}

	public final Sd_systemContext sd_system() throws RecognitionException {
		Sd_systemContext _localctx = new Sd_systemContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_sd_system);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(547);
			match(SYSTEM);
			setState(555);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DEFAULT_GATEWAY:
				{
				setState(548);
				sds_default_gateway();
				}
				break;
			case DNS_SETTING:
				{
				setState(549);
				sds_dns_setting();
				}
				break;
			case HOSTNAME:
				{
				setState(550);
				sds_hostname();
				}
				break;
			case IP_ADDRESS_LITERAL:
				{
				setState(551);
				sds_ip_address();
				}
				break;
			case NETMASK:
				{
				setState(552);
				sds_netmask();
				}
				break;
			case NTP_SERVERS:
				{
				setState(553);
				sds_ntp_servers();
				}
				break;
			case PANORAMA_SERVER:
			case SERVICE:
			case TIMEZONE:
			case TYPE:
			case UPDATE_SCHEDULE:
			case UPDATE_SERVER:
				{
				setState(554);
				sds_null();
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

	public static class Sds_default_gatewayContext extends ParserRuleContext {
		public TerminalNode DEFAULT_GATEWAY() { return getToken(PaloAltoParser.DEFAULT_GATEWAY, 0); }
		public TerminalNode IP_ADDRESS() { return getToken(PaloAltoParser.IP_ADDRESS, 0); }
		public Sds_default_gatewayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sds_default_gateway; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSds_default_gateway(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSds_default_gateway(this);
		}
	}

	public final Sds_default_gatewayContext sds_default_gateway() throws RecognitionException {
		Sds_default_gatewayContext _localctx = new Sds_default_gatewayContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_sds_default_gateway);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(557);
			match(DEFAULT_GATEWAY);
			setState(558);
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

	public static class Sds_dns_settingContext extends ParserRuleContext {
		public TerminalNode DNS_SETTING() { return getToken(PaloAltoParser.DNS_SETTING, 0); }
		public Sdsd_serversContext sdsd_servers() {
			return getRuleContext(Sdsd_serversContext.class,0);
		}
		public Sds_dns_settingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sds_dns_setting; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSds_dns_setting(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSds_dns_setting(this);
		}
	}

	public final Sds_dns_settingContext sds_dns_setting() throws RecognitionException {
		Sds_dns_settingContext _localctx = new Sds_dns_settingContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_sds_dns_setting);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(560);
			match(DNS_SETTING);
			{
			setState(561);
			sdsd_servers();
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

	public static class Sds_hostnameContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode HOSTNAME() { return getToken(PaloAltoParser.HOSTNAME, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sds_hostnameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sds_hostname; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSds_hostname(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSds_hostname(this);
		}
	}

	public final Sds_hostnameContext sds_hostname() throws RecognitionException {
		Sds_hostnameContext _localctx = new Sds_hostnameContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_sds_hostname);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(563);
			match(HOSTNAME);
			setState(564);
			((Sds_hostnameContext)_localctx).name = variable();
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

	public static class Sds_ip_addressContext extends ParserRuleContext {
		public TerminalNode IP_ADDRESS_LITERAL() { return getToken(PaloAltoParser.IP_ADDRESS_LITERAL, 0); }
		public TerminalNode IP_ADDRESS() { return getToken(PaloAltoParser.IP_ADDRESS, 0); }
		public Sds_ip_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sds_ip_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSds_ip_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSds_ip_address(this);
		}
	}

	public final Sds_ip_addressContext sds_ip_address() throws RecognitionException {
		Sds_ip_addressContext _localctx = new Sds_ip_addressContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_sds_ip_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(566);
			match(IP_ADDRESS_LITERAL);
			setState(567);
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

	public static class Sds_netmaskContext extends ParserRuleContext {
		public TerminalNode NETMASK() { return getToken(PaloAltoParser.NETMASK, 0); }
		public TerminalNode IP_ADDRESS() { return getToken(PaloAltoParser.IP_ADDRESS, 0); }
		public Sds_netmaskContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sds_netmask; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSds_netmask(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSds_netmask(this);
		}
	}

	public final Sds_netmaskContext sds_netmask() throws RecognitionException {
		Sds_netmaskContext _localctx = new Sds_netmaskContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_sds_netmask);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(569);
			match(NETMASK);
			setState(570);
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

	public static class Sds_ntp_serversContext extends ParserRuleContext {
		public TerminalNode NTP_SERVERS() { return getToken(PaloAltoParser.NTP_SERVERS, 0); }
		public TerminalNode PRIMARY_NTP_SERVER() { return getToken(PaloAltoParser.PRIMARY_NTP_SERVER, 0); }
		public TerminalNode SECONDARY_NTP_SERVER() { return getToken(PaloAltoParser.SECONDARY_NTP_SERVER, 0); }
		public Sdsn_ntp_server_addressContext sdsn_ntp_server_address() {
			return getRuleContext(Sdsn_ntp_server_addressContext.class,0);
		}
		public Sds_ntp_serversContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sds_ntp_servers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSds_ntp_servers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSds_ntp_servers(this);
		}
	}

	public final Sds_ntp_serversContext sds_ntp_servers() throws RecognitionException {
		Sds_ntp_serversContext _localctx = new Sds_ntp_serversContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_sds_ntp_servers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(572);
			match(NTP_SERVERS);
			setState(573);
			_la = _input.LA(1);
			if ( !(_la==PRIMARY_NTP_SERVER || _la==SECONDARY_NTP_SERVER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			{
			setState(574);
			sdsn_ntp_server_address();
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

	public static class Sds_nullContext extends ParserRuleContext {
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public TerminalNode PANORAMA_SERVER() { return getToken(PaloAltoParser.PANORAMA_SERVER, 0); }
		public TerminalNode SERVICE() { return getToken(PaloAltoParser.SERVICE, 0); }
		public TerminalNode TIMEZONE() { return getToken(PaloAltoParser.TIMEZONE, 0); }
		public TerminalNode TYPE() { return getToken(PaloAltoParser.TYPE, 0); }
		public TerminalNode UPDATE_SCHEDULE() { return getToken(PaloAltoParser.UPDATE_SCHEDULE, 0); }
		public TerminalNode UPDATE_SERVER() { return getToken(PaloAltoParser.UPDATE_SERVER, 0); }
		public Sds_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sds_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSds_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSds_null(this);
		}
	}

	public final Sds_nullContext sds_null() throws RecognitionException {
		Sds_nullContext _localctx = new Sds_nullContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_sds_null);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(576);
			_la = _input.LA(1);
			if ( !(((((_la - 98)) & ~0x3f) == 0 && ((1L << (_la - 98)) & ((1L << (PANORAMA_SERVER - 98)) | (1L << (SERVICE - 98)) | (1L << (TIMEZONE - 98)) | (1L << (TYPE - 98)) | (1L << (UPDATE_SCHEDULE - 98)) | (1L << (UPDATE_SERVER - 98)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(577);
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

	public static class Sdsd_serversContext extends ParserRuleContext {
		public Token primary_name;
		public Token secondary_name;
		public TerminalNode SERVERS() { return getToken(PaloAltoParser.SERVERS, 0); }
		public TerminalNode PRIMARY() { return getToken(PaloAltoParser.PRIMARY, 0); }
		public TerminalNode SECONDARY() { return getToken(PaloAltoParser.SECONDARY, 0); }
		public TerminalNode IP_ADDRESS() { return getToken(PaloAltoParser.IP_ADDRESS, 0); }
		public Sdsd_serversContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sdsd_servers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSdsd_servers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSdsd_servers(this);
		}
	}

	public final Sdsd_serversContext sdsd_servers() throws RecognitionException {
		Sdsd_serversContext _localctx = new Sdsd_serversContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_sdsd_servers);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(579);
			match(SERVERS);
			setState(584);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PRIMARY:
				{
				setState(580);
				match(PRIMARY);
				setState(581);
				((Sdsd_serversContext)_localctx).primary_name = match(IP_ADDRESS);
				}
				break;
			case SECONDARY:
				{
				setState(582);
				match(SECONDARY);
				setState(583);
				((Sdsd_serversContext)_localctx).secondary_name = match(IP_ADDRESS);
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

	public static class Sdsn_ntp_server_addressContext extends ParserRuleContext {
		public VariableContext address;
		public TerminalNode NTP_SERVER_ADDRESS() { return getToken(PaloAltoParser.NTP_SERVER_ADDRESS, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sdsn_ntp_server_addressContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sdsn_ntp_server_address; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSdsn_ntp_server_address(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSdsn_ntp_server_address(this);
		}
	}

	public final Sdsn_ntp_server_addressContext sdsn_ntp_server_address() throws RecognitionException {
		Sdsn_ntp_server_addressContext _localctx = new Sdsn_ntp_server_addressContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_sdsn_ntp_server_address);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(586);
			match(NTP_SERVER_ADDRESS);
			setState(587);
			((Sdsn_ntp_server_addressContext)_localctx).address = variable();
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

	public static class Sn_interfaceContext extends ParserRuleContext {
		public TerminalNode INTERFACE() { return getToken(PaloAltoParser.INTERFACE, 0); }
		public Sni_ethernetContext sni_ethernet() {
			return getRuleContext(Sni_ethernetContext.class,0);
		}
		public Sni_loopbackContext sni_loopback() {
			return getRuleContext(Sni_loopbackContext.class,0);
		}
		public Sni_tunnelContext sni_tunnel() {
			return getRuleContext(Sni_tunnelContext.class,0);
		}
		public Sni_vlanContext sni_vlan() {
			return getRuleContext(Sni_vlanContext.class,0);
		}
		public Sn_interfaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_interface; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_interface(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_interface(this);
		}
	}

	public final Sn_interfaceContext sn_interface() throws RecognitionException {
		Sn_interfaceContext _localctx = new Sn_interfaceContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_sn_interface);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(589);
			match(INTERFACE);
			setState(594);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ETHERNET:
				{
				setState(590);
				sni_ethernet();
				}
				break;
			case LOOPBACK:
				{
				setState(591);
				sni_loopback();
				}
				break;
			case TUNNEL:
				{
				setState(592);
				sni_tunnel();
				}
				break;
			case VLAN:
				{
				setState(593);
				sni_vlan();
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

	public static class If_commonContext extends ParserRuleContext {
		public If_commentContext if_comment() {
			return getRuleContext(If_commentContext.class,0);
		}
		public If_commonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_if_common; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterIf_common(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitIf_common(this);
		}
	}

	public final If_commonContext if_common() throws RecognitionException {
		If_commonContext _localctx = new If_commonContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_if_common);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(596);
			if_comment();
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

	public static class If_commentContext extends ParserRuleContext {
		public VariableContext text;
		public TerminalNode COMMENT() { return getToken(PaloAltoParser.COMMENT, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public If_commentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_if_comment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterIf_comment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitIf_comment(this);
		}
	}

	public final If_commentContext if_comment() throws RecognitionException {
		If_commentContext _localctx = new If_commentContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_if_comment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(598);
			match(COMMENT);
			setState(599);
			((If_commentContext)_localctx).text = variable();
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

	public static class If_tagContext extends ParserRuleContext {
		public Token tag;
		public TerminalNode TAG() { return getToken(PaloAltoParser.TAG, 0); }
		public TerminalNode DEC() { return getToken(PaloAltoParser.DEC, 0); }
		public If_tagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_if_tag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterIf_tag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitIf_tag(this);
		}
	}

	public final If_tagContext if_tag() throws RecognitionException {
		If_tagContext _localctx = new If_tagContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_if_tag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(601);
			match(TAG);
			setState(602);
			((If_tagContext)_localctx).tag = match(DEC);
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

	public static class Sni_ethernetContext extends ParserRuleContext {
		public TerminalNode ETHERNET() { return getToken(PaloAltoParser.ETHERNET, 0); }
		public Sni_ethernet_definitionContext sni_ethernet_definition() {
			return getRuleContext(Sni_ethernet_definitionContext.class,0);
		}
		public Sni_ethernetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sni_ethernet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSni_ethernet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSni_ethernet(this);
		}
	}

	public final Sni_ethernetContext sni_ethernet() throws RecognitionException {
		Sni_ethernetContext _localctx = new Sni_ethernetContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_sni_ethernet);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(604);
			match(ETHERNET);
			setState(606);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(605);
				sni_ethernet_definition();
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

	public static class Sni_ethernet_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public If_commonContext if_common() {
			return getRuleContext(If_commonContext.class,0);
		}
		public Snie_layer2Context snie_layer2() {
			return getRuleContext(Snie_layer2Context.class,0);
		}
		public Snie_layer3Context snie_layer3() {
			return getRuleContext(Snie_layer3Context.class,0);
		}
		public Snie_link_stateContext snie_link_state() {
			return getRuleContext(Snie_link_stateContext.class,0);
		}
		public Snie_tapContext snie_tap() {
			return getRuleContext(Snie_tapContext.class,0);
		}
		public Snie_virtual_wireContext snie_virtual_wire() {
			return getRuleContext(Snie_virtual_wireContext.class,0);
		}
		public Sni_ethernet_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sni_ethernet_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSni_ethernet_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSni_ethernet_definition(this);
		}
	}

	public final Sni_ethernet_definitionContext sni_ethernet_definition() throws RecognitionException {
		Sni_ethernet_definitionContext _localctx = new Sni_ethernet_definitionContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_sni_ethernet_definition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(608);
			((Sni_ethernet_definitionContext)_localctx).name = variable();
			setState(615);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case COMMENT:
				{
				setState(609);
				if_common();
				}
				break;
			case LAYER2:
				{
				setState(610);
				snie_layer2();
				}
				break;
			case LAYER3:
				{
				setState(611);
				snie_layer3();
				}
				break;
			case LINK_STATE:
				{
				setState(612);
				snie_link_state();
				}
				break;
			case TAP:
				{
				setState(613);
				snie_tap();
				}
				break;
			case VIRTUAL_WIRE:
				{
				setState(614);
				snie_virtual_wire();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Sni_loopbackContext extends ParserRuleContext {
		public TerminalNode LOOPBACK() { return getToken(PaloAltoParser.LOOPBACK, 0); }
		public If_commonContext if_common() {
			return getRuleContext(If_commonContext.class,0);
		}
		public Snil_unitsContext snil_units() {
			return getRuleContext(Snil_unitsContext.class,0);
		}
		public Sni_loopbackContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sni_loopback; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSni_loopback(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSni_loopback(this);
		}
	}

	public final Sni_loopbackContext sni_loopback() throws RecognitionException {
		Sni_loopbackContext _localctx = new Sni_loopbackContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_sni_loopback);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(617);
			match(LOOPBACK);
			setState(620);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case COMMENT:
				{
				setState(618);
				if_common();
				}
				break;
			case UNITS:
				{
				setState(619);
				snil_units();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Sni_tunnelContext extends ParserRuleContext {
		public TerminalNode TUNNEL() { return getToken(PaloAltoParser.TUNNEL, 0); }
		public If_commonContext if_common() {
			return getRuleContext(If_commonContext.class,0);
		}
		public Snit_unitsContext snit_units() {
			return getRuleContext(Snit_unitsContext.class,0);
		}
		public Sni_tunnelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sni_tunnel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSni_tunnel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSni_tunnel(this);
		}
	}

	public final Sni_tunnelContext sni_tunnel() throws RecognitionException {
		Sni_tunnelContext _localctx = new Sni_tunnelContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_sni_tunnel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(622);
			match(TUNNEL);
			setState(625);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case COMMENT:
				{
				setState(623);
				if_common();
				}
				break;
			case UNITS:
				{
				setState(624);
				snit_units();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Sni_vlanContext extends ParserRuleContext {
		public TerminalNode VLAN() { return getToken(PaloAltoParser.VLAN, 0); }
		public If_commonContext if_common() {
			return getRuleContext(If_commonContext.class,0);
		}
		public Sniv_unitsContext sniv_units() {
			return getRuleContext(Sniv_unitsContext.class,0);
		}
		public Sni_vlanContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sni_vlan; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSni_vlan(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSni_vlan(this);
		}
	}

	public final Sni_vlanContext sni_vlan() throws RecognitionException {
		Sni_vlanContext _localctx = new Sni_vlanContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_sni_vlan);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(627);
			match(VLAN);
			setState(630);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case COMMENT:
				{
				setState(628);
				if_common();
				}
				break;
			case UNITS:
				{
				setState(629);
				sniv_units();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Snie_layer2Context extends ParserRuleContext {
		public TerminalNode LAYER2() { return getToken(PaloAltoParser.LAYER2, 0); }
		public Sniel2_unitsContext sniel2_units() {
			return getRuleContext(Sniel2_unitsContext.class,0);
		}
		public Snie_layer2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snie_layer2; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnie_layer2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnie_layer2(this);
		}
	}

	public final Snie_layer2Context snie_layer2() throws RecognitionException {
		Snie_layer2Context _localctx = new Snie_layer2Context(_ctx, getState());
		enterRule(_localctx, 124, RULE_snie_layer2);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(632);
			match(LAYER2);
			setState(634);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==UNITS) {
				{
				setState(633);
				sniel2_units();
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

	public static class Snie_layer3Context extends ParserRuleContext {
		public TerminalNode LAYER3() { return getToken(PaloAltoParser.LAYER3, 0); }
		public Sniel3_commonContext sniel3_common() {
			return getRuleContext(Sniel3_commonContext.class,0);
		}
		public Sniel3_unitsContext sniel3_units() {
			return getRuleContext(Sniel3_unitsContext.class,0);
		}
		public Snie_layer3Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snie_layer3; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnie_layer3(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnie_layer3(this);
		}
	}

	public final Snie_layer3Context snie_layer3() throws RecognitionException {
		Snie_layer3Context _localctx = new Snie_layer3Context(_ctx, getState());
		enterRule(_localctx, 126, RULE_snie_layer3);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(636);
			match(LAYER3);
			setState(639);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IP:
			case IPV6:
			case LLDP:
			case MTU:
			case NDP_PROXY:
				{
				setState(637);
				sniel3_common();
				}
				break;
			case UNITS:
				{
				setState(638);
				sniel3_units();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Snie_link_stateContext extends ParserRuleContext {
		public TerminalNode LINK_STATE() { return getToken(PaloAltoParser.LINK_STATE, 0); }
		public TerminalNode AUTO() { return getToken(PaloAltoParser.AUTO, 0); }
		public TerminalNode DOWN() { return getToken(PaloAltoParser.DOWN, 0); }
		public TerminalNode UP() { return getToken(PaloAltoParser.UP, 0); }
		public Snie_link_stateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snie_link_state; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnie_link_state(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnie_link_state(this);
		}
	}

	public final Snie_link_stateContext snie_link_state() throws RecognitionException {
		Snie_link_stateContext _localctx = new Snie_link_stateContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_snie_link_state);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(641);
			match(LINK_STATE);
			setState(642);
			_la = _input.LA(1);
			if ( !(_la==AUTO || _la==DOWN || _la==UP) ) {
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

	public static class Snie_tapContext extends ParserRuleContext {
		public TerminalNode TAP() { return getToken(PaloAltoParser.TAP, 0); }
		public Snie_tapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snie_tap; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnie_tap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnie_tap(this);
		}
	}

	public final Snie_tapContext snie_tap() throws RecognitionException {
		Snie_tapContext _localctx = new Snie_tapContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_snie_tap);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(644);
			match(TAP);
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

	public static class Snie_virtual_wireContext extends ParserRuleContext {
		public TerminalNode VIRTUAL_WIRE() { return getToken(PaloAltoParser.VIRTUAL_WIRE, 0); }
		public Snie_virtual_wireContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snie_virtual_wire; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnie_virtual_wire(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnie_virtual_wire(this);
		}
	}

	public final Snie_virtual_wireContext snie_virtual_wire() throws RecognitionException {
		Snie_virtual_wireContext _localctx = new Snie_virtual_wireContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_snie_virtual_wire);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(646);
			match(VIRTUAL_WIRE);
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

	public static class Sniel2_unitContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public If_commonContext if_common() {
			return getRuleContext(If_commonContext.class,0);
		}
		public If_tagContext if_tag() {
			return getRuleContext(If_tagContext.class,0);
		}
		public Sniel2_unitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniel2_unit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniel2_unit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniel2_unit(this);
		}
	}

	public final Sniel2_unitContext sniel2_unit() throws RecognitionException {
		Sniel2_unitContext _localctx = new Sniel2_unitContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_sniel2_unit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(648);
			((Sniel2_unitContext)_localctx).name = variable();
			setState(651);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case COMMENT:
				{
				setState(649);
				if_common();
				}
				break;
			case TAG:
				{
				setState(650);
				if_tag();
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

	public static class Sniel2_unitsContext extends ParserRuleContext {
		public TerminalNode UNITS() { return getToken(PaloAltoParser.UNITS, 0); }
		public Sniel2_unitContext sniel2_unit() {
			return getRuleContext(Sniel2_unitContext.class,0);
		}
		public Sniel2_unitsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniel2_units; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniel2_units(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniel2_units(this);
		}
	}

	public final Sniel2_unitsContext sniel2_units() throws RecognitionException {
		Sniel2_unitsContext _localctx = new Sniel2_unitsContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_sniel2_units);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(653);
			match(UNITS);
			setState(655);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(654);
				sniel2_unit();
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

	public static class Sniel3_commonContext extends ParserRuleContext {
		public Sniel3_ipContext sniel3_ip() {
			return getRuleContext(Sniel3_ipContext.class,0);
		}
		public Sniel3_mtuContext sniel3_mtu() {
			return getRuleContext(Sniel3_mtuContext.class,0);
		}
		public Sniel3_nullContext sniel3_null() {
			return getRuleContext(Sniel3_nullContext.class,0);
		}
		public Sniel3_commonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniel3_common; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniel3_common(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniel3_common(this);
		}
	}

	public final Sniel3_commonContext sniel3_common() throws RecognitionException {
		Sniel3_commonContext _localctx = new Sniel3_commonContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_sniel3_common);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(660);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IP:
				{
				setState(657);
				sniel3_ip();
				}
				break;
			case MTU:
				{
				setState(658);
				sniel3_mtu();
				}
				break;
			case IPV6:
			case LLDP:
			case NDP_PROXY:
				{
				setState(659);
				sniel3_null();
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

	public static class Sniel3_ipContext extends ParserRuleContext {
		public Token address;
		public TerminalNode IP() { return getToken(PaloAltoParser.IP, 0); }
		public TerminalNode IP_PREFIX() { return getToken(PaloAltoParser.IP_PREFIX, 0); }
		public TerminalNode IP_ADDRESS() { return getToken(PaloAltoParser.IP_ADDRESS, 0); }
		public Sniel3_ipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniel3_ip; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniel3_ip(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniel3_ip(this);
		}
	}

	public final Sniel3_ipContext sniel3_ip() throws RecognitionException {
		Sniel3_ipContext _localctx = new Sniel3_ipContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_sniel3_ip);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(662);
			match(IP);
			setState(663);
			((Sniel3_ipContext)_localctx).address = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==IP_ADDRESS || _la==IP_PREFIX) ) {
				((Sniel3_ipContext)_localctx).address = (Token)_errHandler.recoverInline(this);
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

	public static class Sniel3_mtuContext extends ParserRuleContext {
		public Token mtu;
		public TerminalNode MTU() { return getToken(PaloAltoParser.MTU, 0); }
		public TerminalNode DEC() { return getToken(PaloAltoParser.DEC, 0); }
		public Sniel3_mtuContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniel3_mtu; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniel3_mtu(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniel3_mtu(this);
		}
	}

	public final Sniel3_mtuContext sniel3_mtu() throws RecognitionException {
		Sniel3_mtuContext _localctx = new Sniel3_mtuContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_sniel3_mtu);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(665);
			match(MTU);
			setState(666);
			((Sniel3_mtuContext)_localctx).mtu = match(DEC);
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

	public static class Sniel3_nullContext extends ParserRuleContext {
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public TerminalNode LLDP() { return getToken(PaloAltoParser.LLDP, 0); }
		public TerminalNode IPV6() { return getToken(PaloAltoParser.IPV6, 0); }
		public TerminalNode NDP_PROXY() { return getToken(PaloAltoParser.NDP_PROXY, 0); }
		public Sniel3_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniel3_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniel3_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniel3_null(this);
		}
	}

	public final Sniel3_nullContext sniel3_null() throws RecognitionException {
		Sniel3_nullContext _localctx = new Sniel3_nullContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_sniel3_null);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(668);
			_la = _input.LA(1);
			if ( !(((((_la - 70)) & ~0x3f) == 0 && ((1L << (_la - 70)) & ((1L << (IPV6 - 70)) | (1L << (LLDP - 70)) | (1L << (NDP_PROXY - 70)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(669);
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

	public static class Sniel3_unitContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public If_commonContext if_common() {
			return getRuleContext(If_commonContext.class,0);
		}
		public Sniel3_commonContext sniel3_common() {
			return getRuleContext(Sniel3_commonContext.class,0);
		}
		public If_tagContext if_tag() {
			return getRuleContext(If_tagContext.class,0);
		}
		public Sniel3_unitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniel3_unit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniel3_unit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniel3_unit(this);
		}
	}

	public final Sniel3_unitContext sniel3_unit() throws RecognitionException {
		Sniel3_unitContext _localctx = new Sniel3_unitContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_sniel3_unit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(671);
			((Sniel3_unitContext)_localctx).name = variable();
			setState(675);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case COMMENT:
				{
				setState(672);
				if_common();
				}
				break;
			case IP:
			case IPV6:
			case LLDP:
			case MTU:
			case NDP_PROXY:
				{
				setState(673);
				sniel3_common();
				}
				break;
			case TAG:
				{
				setState(674);
				if_tag();
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

	public static class Sniel3_unitsContext extends ParserRuleContext {
		public TerminalNode UNITS() { return getToken(PaloAltoParser.UNITS, 0); }
		public Sniel3_unitContext sniel3_unit() {
			return getRuleContext(Sniel3_unitContext.class,0);
		}
		public Sniel3_unitsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniel3_units; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniel3_units(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniel3_units(this);
		}
	}

	public final Sniel3_unitsContext sniel3_units() throws RecognitionException {
		Sniel3_unitsContext _localctx = new Sniel3_unitsContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_sniel3_units);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(677);
			match(UNITS);
			setState(679);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(678);
				sniel3_unit();
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

	public static class Snil_unitContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public If_commonContext if_common() {
			return getRuleContext(If_commonContext.class,0);
		}
		public Snil_unitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snil_unit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnil_unit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnil_unit(this);
		}
	}

	public final Snil_unitContext snil_unit() throws RecognitionException {
		Snil_unitContext _localctx = new Snil_unitContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_snil_unit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(681);
			((Snil_unitContext)_localctx).name = variable();
			setState(683);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(682);
				if_common();
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

	public static class Snil_unitsContext extends ParserRuleContext {
		public TerminalNode UNITS() { return getToken(PaloAltoParser.UNITS, 0); }
		public Snil_unitContext snil_unit() {
			return getRuleContext(Snil_unitContext.class,0);
		}
		public Snil_unitsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snil_units; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnil_units(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnil_units(this);
		}
	}

	public final Snil_unitsContext snil_units() throws RecognitionException {
		Snil_unitsContext _localctx = new Snil_unitsContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_snil_units);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(685);
			match(UNITS);
			setState(687);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(686);
				snil_unit();
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

	public static class Snit_unitContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public If_commonContext if_common() {
			return getRuleContext(If_commonContext.class,0);
		}
		public Snit_unitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snit_unit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnit_unit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnit_unit(this);
		}
	}

	public final Snit_unitContext snit_unit() throws RecognitionException {
		Snit_unitContext _localctx = new Snit_unitContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_snit_unit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(689);
			((Snit_unitContext)_localctx).name = variable();
			setState(691);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(690);
				if_common();
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

	public static class Snit_unitsContext extends ParserRuleContext {
		public TerminalNode UNITS() { return getToken(PaloAltoParser.UNITS, 0); }
		public Snit_unitContext snit_unit() {
			return getRuleContext(Snit_unitContext.class,0);
		}
		public Snit_unitsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snit_units; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnit_units(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnit_units(this);
		}
	}

	public final Snit_unitsContext snit_units() throws RecognitionException {
		Snit_unitsContext _localctx = new Snit_unitsContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_snit_units);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(693);
			match(UNITS);
			setState(695);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(694);
				snit_unit();
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

	public static class Sniv_unitContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public If_commonContext if_common() {
			return getRuleContext(If_commonContext.class,0);
		}
		public Sniv_unitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniv_unit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniv_unit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniv_unit(this);
		}
	}

	public final Sniv_unitContext sniv_unit() throws RecognitionException {
		Sniv_unitContext _localctx = new Sniv_unitContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_sniv_unit);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(697);
			((Sniv_unitContext)_localctx).name = variable();
			setState(699);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMENT) {
				{
				setState(698);
				if_common();
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

	public static class Sniv_unitsContext extends ParserRuleContext {
		public TerminalNode UNITS() { return getToken(PaloAltoParser.UNITS, 0); }
		public Sniv_unitContext sniv_unit() {
			return getRuleContext(Sniv_unitContext.class,0);
		}
		public Sniv_unitsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sniv_units; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSniv_units(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSniv_units(this);
		}
	}

	public final Sniv_unitsContext sniv_units() throws RecognitionException {
		Sniv_unitsContext _localctx = new Sniv_unitsContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_sniv_units);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(701);
			match(UNITS);
			setState(703);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(702);
				sniv_unit();
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

	public static class Cp_authenticationContext extends ParserRuleContext {
		public TerminalNode AUTHENTICATION() { return getToken(PaloAltoParser.AUTHENTICATION, 0); }
		public TerminalNode MD5() { return getToken(PaloAltoParser.MD5, 0); }
		public TerminalNode NONE() { return getToken(PaloAltoParser.NONE, 0); }
		public TerminalNode SHA1() { return getToken(PaloAltoParser.SHA1, 0); }
		public TerminalNode SHA256() { return getToken(PaloAltoParser.SHA256, 0); }
		public TerminalNode SHA384() { return getToken(PaloAltoParser.SHA384, 0); }
		public TerminalNode SHA512() { return getToken(PaloAltoParser.SHA512, 0); }
		public Cp_authenticationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cp_authentication; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterCp_authentication(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitCp_authentication(this);
		}
	}

	public final Cp_authenticationContext cp_authentication() throws RecognitionException {
		Cp_authenticationContext _localctx = new Cp_authenticationContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_cp_authentication);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(705);
			match(AUTHENTICATION);
			setState(706);
			_la = _input.LA(1);
			if ( !(((((_la - 78)) & ~0x3f) == 0 && ((1L << (_la - 78)) & ((1L << (MD5 - 78)) | (1L << (NONE - 78)) | (1L << (SHA1 - 78)) | (1L << (SHA256 - 78)) | (1L << (SHA384 - 78)) | (1L << (SHA512 - 78)))) != 0)) ) {
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

	public static class Cp_dh_groupContext extends ParserRuleContext {
		public TerminalNode DH_GROUP() { return getToken(PaloAltoParser.DH_GROUP, 0); }
		public TerminalNode GROUP1() { return getToken(PaloAltoParser.GROUP1, 0); }
		public TerminalNode GROUP2() { return getToken(PaloAltoParser.GROUP2, 0); }
		public TerminalNode GROUP5() { return getToken(PaloAltoParser.GROUP5, 0); }
		public TerminalNode GROUP14() { return getToken(PaloAltoParser.GROUP14, 0); }
		public TerminalNode GROUP19() { return getToken(PaloAltoParser.GROUP19, 0); }
		public TerminalNode GROUP20() { return getToken(PaloAltoParser.GROUP20, 0); }
		public Cp_dh_groupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cp_dh_group; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterCp_dh_group(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitCp_dh_group(this);
		}
	}

	public final Cp_dh_groupContext cp_dh_group() throws RecognitionException {
		Cp_dh_groupContext _localctx = new Cp_dh_groupContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_cp_dh_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(708);
			match(DH_GROUP);
			setState(709);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20))) != 0)) ) {
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

	public static class Cp_encryptionContext extends ParserRuleContext {
		public Cp_encryption_algoContext cp_encryption_algo;
		public List<Cp_encryption_algoContext> algo = new ArrayList<Cp_encryption_algoContext>();
		public TerminalNode ENCRYPTION() { return getToken(PaloAltoParser.ENCRYPTION, 0); }
		public TerminalNode OPEN_BRACKET() { return getToken(PaloAltoParser.OPEN_BRACKET, 0); }
		public TerminalNode CLOSE_BRACKET() { return getToken(PaloAltoParser.CLOSE_BRACKET, 0); }
		public List<Cp_encryption_algoContext> cp_encryption_algo() {
			return getRuleContexts(Cp_encryption_algoContext.class);
		}
		public Cp_encryption_algoContext cp_encryption_algo(int i) {
			return getRuleContext(Cp_encryption_algoContext.class,i);
		}
		public Cp_encryptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cp_encryption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterCp_encryption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitCp_encryption(this);
		}
	}

	public final Cp_encryptionContext cp_encryption() throws RecognitionException {
		Cp_encryptionContext _localctx = new Cp_encryptionContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_cp_encryption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(711);
			match(ENCRYPTION);
			setState(713);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPEN_BRACKET) {
				{
				setState(712);
				match(OPEN_BRACKET);
				}
			}

			setState(716); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(715);
				((Cp_encryptionContext)_localctx).cp_encryption_algo = cp_encryption_algo();
				((Cp_encryptionContext)_localctx).algo.add(((Cp_encryptionContext)_localctx).cp_encryption_algo);
				}
				}
				setState(718); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << DES))) != 0) || _la==NULL || _la==THREE_DES );
			setState(721);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==CLOSE_BRACKET) {
				{
				setState(720);
				match(CLOSE_BRACKET);
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

	public static class Cp_encryption_algoContext extends ParserRuleContext {
		public TerminalNode DES() { return getToken(PaloAltoParser.DES, 0); }
		public TerminalNode THREE_DES() { return getToken(PaloAltoParser.THREE_DES, 0); }
		public TerminalNode AES_128_CBC() { return getToken(PaloAltoParser.AES_128_CBC, 0); }
		public TerminalNode AES_192_CBC() { return getToken(PaloAltoParser.AES_192_CBC, 0); }
		public TerminalNode AES_256_CBC() { return getToken(PaloAltoParser.AES_256_CBC, 0); }
		public TerminalNode AES_128_GCM() { return getToken(PaloAltoParser.AES_128_GCM, 0); }
		public TerminalNode AES_256_GCM() { return getToken(PaloAltoParser.AES_256_GCM, 0); }
		public TerminalNode NULL() { return getToken(PaloAltoParser.NULL, 0); }
		public Cp_encryption_algoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cp_encryption_algo; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterCp_encryption_algo(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitCp_encryption_algo(this);
		}
	}

	public final Cp_encryption_algoContext cp_encryption_algo() throws RecognitionException {
		Cp_encryption_algoContext _localctx = new Cp_encryption_algoContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_cp_encryption_algo);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(723);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << DES))) != 0) || _la==NULL || _la==THREE_DES) ) {
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

	public static class Cp_hashContext extends ParserRuleContext {
		public TerminalNode HASH() { return getToken(PaloAltoParser.HASH, 0); }
		public TerminalNode MD5() { return getToken(PaloAltoParser.MD5, 0); }
		public TerminalNode SHA1() { return getToken(PaloAltoParser.SHA1, 0); }
		public TerminalNode SHA256() { return getToken(PaloAltoParser.SHA256, 0); }
		public TerminalNode SHA384() { return getToken(PaloAltoParser.SHA384, 0); }
		public TerminalNode SHA512() { return getToken(PaloAltoParser.SHA512, 0); }
		public Cp_hashContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cp_hash; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterCp_hash(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitCp_hash(this);
		}
	}

	public final Cp_hashContext cp_hash() throws RecognitionException {
		Cp_hashContext _localctx = new Cp_hashContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_cp_hash);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(725);
			match(HASH);
			setState(726);
			_la = _input.LA(1);
			if ( !(((((_la - 78)) & ~0x3f) == 0 && ((1L << (_la - 78)) & ((1L << (MD5 - 78)) | (1L << (SHA1 - 78)) | (1L << (SHA256 - 78)) | (1L << (SHA384 - 78)) | (1L << (SHA512 - 78)))) != 0)) ) {
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

	public static class Cp_lifetimeContext extends ParserRuleContext {
		public Token val;
		public TerminalNode LIFETIME() { return getToken(PaloAltoParser.LIFETIME, 0); }
		public TerminalNode DAYS() { return getToken(PaloAltoParser.DAYS, 0); }
		public TerminalNode HOURS() { return getToken(PaloAltoParser.HOURS, 0); }
		public TerminalNode MINUTES() { return getToken(PaloAltoParser.MINUTES, 0); }
		public TerminalNode SECONDS() { return getToken(PaloAltoParser.SECONDS, 0); }
		public TerminalNode DEC() { return getToken(PaloAltoParser.DEC, 0); }
		public Cp_lifetimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cp_lifetime; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterCp_lifetime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitCp_lifetime(this);
		}
	}

	public final Cp_lifetimeContext cp_lifetime() throws RecognitionException {
		Cp_lifetimeContext _localctx = new Cp_lifetimeContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_cp_lifetime);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(728);
			match(LIFETIME);
			setState(729);
			_la = _input.LA(1);
			if ( !(_la==DAYS || _la==HOURS || _la==MINUTES || _la==SECONDS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(730);
			((Cp_lifetimeContext)_localctx).val = match(DEC);
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

	public static class S_networkContext extends ParserRuleContext {
		public TerminalNode NETWORK() { return getToken(PaloAltoParser.NETWORK, 0); }
		public Sn_ikeContext sn_ike() {
			return getRuleContext(Sn_ikeContext.class,0);
		}
		public Sn_interfaceContext sn_interface() {
			return getRuleContext(Sn_interfaceContext.class,0);
		}
		public Sn_profilesContext sn_profiles() {
			return getRuleContext(Sn_profilesContext.class,0);
		}
		public Sn_qosContext sn_qos() {
			return getRuleContext(Sn_qosContext.class,0);
		}
		public Sn_shared_gatewayContext sn_shared_gateway() {
			return getRuleContext(Sn_shared_gatewayContext.class,0);
		}
		public Sn_virtual_routerContext sn_virtual_router() {
			return getRuleContext(Sn_virtual_routerContext.class,0);
		}
		public S_networkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_network; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_network(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_network(this);
		}
	}

	public final S_networkContext s_network() throws RecognitionException {
		S_networkContext _localctx = new S_networkContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_s_network);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(732);
			match(NETWORK);
			setState(739);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IKE:
				{
				setState(733);
				sn_ike();
				}
				break;
			case INTERFACE:
				{
				setState(734);
				sn_interface();
				}
				break;
			case PROFILES:
				{
				setState(735);
				sn_profiles();
				}
				break;
			case QOS:
				{
				setState(736);
				sn_qos();
				}
				break;
			case SHARED_GATEWAY:
				{
				setState(737);
				sn_shared_gateway();
				}
				break;
			case VIRTUAL_ROUTER:
				{
				setState(738);
				sn_virtual_router();
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

	public static class Sn_ikeContext extends ParserRuleContext {
		public TerminalNode IKE() { return getToken(PaloAltoParser.IKE, 0); }
		public Sn_ike_crypto_profilesContext sn_ike_crypto_profiles() {
			return getRuleContext(Sn_ike_crypto_profilesContext.class,0);
		}
		public Sn_ike_gatewayContext sn_ike_gateway() {
			return getRuleContext(Sn_ike_gatewayContext.class,0);
		}
		public Sn_ikeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_ike; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_ike(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_ike(this);
		}
	}

	public final Sn_ikeContext sn_ike() throws RecognitionException {
		Sn_ikeContext _localctx = new Sn_ikeContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_sn_ike);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(741);
			match(IKE);
			setState(744);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CRYPTO_PROFILES:
				{
				setState(742);
				sn_ike_crypto_profiles();
				}
				break;
			case GATEWAY:
				{
				setState(743);
				sn_ike_gateway();
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

	public static class Sn_ike_crypto_profilesContext extends ParserRuleContext {
		public TerminalNode CRYPTO_PROFILES() { return getToken(PaloAltoParser.CRYPTO_PROFILES, 0); }
		public Snicp_global_protectContext snicp_global_protect() {
			return getRuleContext(Snicp_global_protectContext.class,0);
		}
		public Snicp_ike_crypto_profilesContext snicp_ike_crypto_profiles() {
			return getRuleContext(Snicp_ike_crypto_profilesContext.class,0);
		}
		public Snicp_ipsec_crypto_profilesContext snicp_ipsec_crypto_profiles() {
			return getRuleContext(Snicp_ipsec_crypto_profilesContext.class,0);
		}
		public Sn_ike_crypto_profilesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_ike_crypto_profiles; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_ike_crypto_profiles(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_ike_crypto_profiles(this);
		}
	}

	public final Sn_ike_crypto_profilesContext sn_ike_crypto_profiles() throws RecognitionException {
		Sn_ike_crypto_profilesContext _localctx = new Sn_ike_crypto_profilesContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_sn_ike_crypto_profiles);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(746);
			match(CRYPTO_PROFILES);
			setState(750);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GLOBAL_PROTECT_APP_CRYPTO_PROFILES:
				{
				setState(747);
				snicp_global_protect();
				}
				break;
			case IKE_CRYPTO_PROFILES:
				{
				setState(748);
				snicp_ike_crypto_profiles();
				}
				break;
			case IPSEC_CRYPTO_PROFILES:
				{
				setState(749);
				snicp_ipsec_crypto_profiles();
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

	public static class Sn_ike_gatewayContext extends ParserRuleContext {
		public TerminalNode GATEWAY() { return getToken(PaloAltoParser.GATEWAY, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Sn_ike_gatewayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_ike_gateway; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_ike_gateway(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_ike_gateway(this);
		}
	}

	public final Sn_ike_gatewayContext sn_ike_gateway() throws RecognitionException {
		Sn_ike_gatewayContext _localctx = new Sn_ike_gatewayContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_sn_ike_gateway);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(752);
			match(GATEWAY);
			setState(753);
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

	public static class Sn_profilesContext extends ParserRuleContext {
		public TerminalNode PROFILES() { return getToken(PaloAltoParser.PROFILES, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Sn_profilesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_profiles; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_profiles(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_profiles(this);
		}
	}

	public final Sn_profilesContext sn_profiles() throws RecognitionException {
		Sn_profilesContext _localctx = new Sn_profilesContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_sn_profiles);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(755);
			match(PROFILES);
			setState(756);
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

	public static class Sn_qosContext extends ParserRuleContext {
		public TerminalNode QOS() { return getToken(PaloAltoParser.QOS, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Sn_qosContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_qos; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_qos(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_qos(this);
		}
	}

	public final Sn_qosContext sn_qos() throws RecognitionException {
		Sn_qosContext _localctx = new Sn_qosContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_sn_qos);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(758);
			match(QOS);
			setState(759);
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

	public static class Sn_shared_gatewayContext extends ParserRuleContext {
		public TerminalNode SHARED_GATEWAY() { return getToken(PaloAltoParser.SHARED_GATEWAY, 0); }
		public Sn_shared_gateway_definitionContext sn_shared_gateway_definition() {
			return getRuleContext(Sn_shared_gateway_definitionContext.class,0);
		}
		public Sn_shared_gatewayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_shared_gateway; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_shared_gateway(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_shared_gateway(this);
		}
	}

	public final Sn_shared_gatewayContext sn_shared_gateway() throws RecognitionException {
		Sn_shared_gatewayContext _localctx = new Sn_shared_gatewayContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_sn_shared_gateway);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(761);
			match(SHARED_GATEWAY);
			setState(763);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(762);
				sn_shared_gateway_definition();
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

	public static class Sn_shared_gateway_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Snsg_display_nameContext snsg_display_name() {
			return getRuleContext(Snsg_display_nameContext.class,0);
		}
		public Snsg_importContext snsg_import() {
			return getRuleContext(Snsg_importContext.class,0);
		}
		public Snsg_zoneContext snsg_zone() {
			return getRuleContext(Snsg_zoneContext.class,0);
		}
		public Sn_shared_gateway_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_shared_gateway_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_shared_gateway_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_shared_gateway_definition(this);
		}
	}

	public final Sn_shared_gateway_definitionContext sn_shared_gateway_definition() throws RecognitionException {
		Sn_shared_gateway_definitionContext _localctx = new Sn_shared_gateway_definitionContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_sn_shared_gateway_definition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(765);
			((Sn_shared_gateway_definitionContext)_localctx).name = variable();
			setState(769);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DISPLAY_NAME:
				{
				setState(766);
				snsg_display_name();
				}
				break;
			case IMPORT:
				{
				setState(767);
				snsg_import();
				}
				break;
			case ZONE:
				{
				setState(768);
				snsg_zone();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Snsg_display_nameContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode DISPLAY_NAME() { return getToken(PaloAltoParser.DISPLAY_NAME, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Snsg_display_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snsg_display_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnsg_display_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnsg_display_name(this);
		}
	}

	public final Snsg_display_nameContext snsg_display_name() throws RecognitionException {
		Snsg_display_nameContext _localctx = new Snsg_display_nameContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_snsg_display_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(771);
			match(DISPLAY_NAME);
			setState(772);
			((Snsg_display_nameContext)_localctx).name = variable();
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

	public static class Snsg_importContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(PaloAltoParser.IMPORT, 0); }
		public Snsgi_interfaceContext snsgi_interface() {
			return getRuleContext(Snsgi_interfaceContext.class,0);
		}
		public Snsg_importContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snsg_import; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnsg_import(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnsg_import(this);
		}
	}

	public final Snsg_importContext snsg_import() throws RecognitionException {
		Snsg_importContext _localctx = new Snsg_importContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_snsg_import);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(774);
			match(IMPORT);
			setState(776);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NETWORK) {
				{
				setState(775);
				snsgi_interface();
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

	public static class Snsgi_interfaceContext extends ParserRuleContext {
		public TerminalNode NETWORK() { return getToken(PaloAltoParser.NETWORK, 0); }
		public TerminalNode INTERFACE() { return getToken(PaloAltoParser.INTERFACE, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Snsgi_interfaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snsgi_interface; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnsgi_interface(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnsgi_interface(this);
		}
	}

	public final Snsgi_interfaceContext snsgi_interface() throws RecognitionException {
		Snsgi_interfaceContext _localctx = new Snsgi_interfaceContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_snsgi_interface);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(778);
			match(NETWORK);
			setState(779);
			match(INTERFACE);
			setState(780);
			variable_list();
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

	public static class Snsg_zoneContext extends ParserRuleContext {
		public TerminalNode ZONE() { return getToken(PaloAltoParser.ZONE, 0); }
		public Snsg_zone_definitionContext snsg_zone_definition() {
			return getRuleContext(Snsg_zone_definitionContext.class,0);
		}
		public Snsg_zoneContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snsg_zone; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnsg_zone(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnsg_zone(this);
		}
	}

	public final Snsg_zoneContext snsg_zone() throws RecognitionException {
		Snsg_zoneContext _localctx = new Snsg_zoneContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_snsg_zone);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(782);
			match(ZONE);
			setState(783);
			snsg_zone_definition();
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

	public static class Snsg_zone_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Snsgz_networkContext snsgz_network() {
			return getRuleContext(Snsgz_networkContext.class,0);
		}
		public Snsg_zone_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snsg_zone_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnsg_zone_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnsg_zone_definition(this);
		}
	}

	public final Snsg_zone_definitionContext snsg_zone_definition() throws RecognitionException {
		Snsg_zone_definitionContext _localctx = new Snsg_zone_definitionContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_snsg_zone_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(785);
			((Snsg_zone_definitionContext)_localctx).name = variable();
			setState(787);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NETWORK) {
				{
				setState(786);
				snsgz_network();
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

	public static class Snsgz_networkContext extends ParserRuleContext {
		public TerminalNode NETWORK() { return getToken(PaloAltoParser.NETWORK, 0); }
		public Snsgzn_layer3Context snsgzn_layer3() {
			return getRuleContext(Snsgzn_layer3Context.class,0);
		}
		public Snsgz_networkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snsgz_network; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnsgz_network(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnsgz_network(this);
		}
	}

	public final Snsgz_networkContext snsgz_network() throws RecognitionException {
		Snsgz_networkContext _localctx = new Snsgz_networkContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_snsgz_network);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(789);
			match(NETWORK);
			setState(791);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LAYER3) {
				{
				setState(790);
				snsgzn_layer3();
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

	public static class Snsgzn_layer3Context extends ParserRuleContext {
		public TerminalNode LAYER3() { return getToken(PaloAltoParser.LAYER3, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Snsgzn_layer3Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snsgzn_layer3; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnsgzn_layer3(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnsgzn_layer3(this);
		}
	}

	public final Snsgzn_layer3Context snsgzn_layer3() throws RecognitionException {
		Snsgzn_layer3Context _localctx = new Snsgzn_layer3Context(_ctx, getState());
		enterRule(_localctx, 202, RULE_snsgzn_layer3);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(793);
			match(LAYER3);
			setState(794);
			variable_list();
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

	public static class Sn_virtual_routerContext extends ParserRuleContext {
		public TerminalNode VIRTUAL_ROUTER() { return getToken(PaloAltoParser.VIRTUAL_ROUTER, 0); }
		public Sn_virtual_router_definitionContext sn_virtual_router_definition() {
			return getRuleContext(Sn_virtual_router_definitionContext.class,0);
		}
		public Sn_virtual_routerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_virtual_router; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_virtual_router(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_virtual_router(this);
		}
	}

	public final Sn_virtual_routerContext sn_virtual_router() throws RecognitionException {
		Sn_virtual_routerContext _localctx = new Sn_virtual_routerContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_sn_virtual_router);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(796);
			match(VIRTUAL_ROUTER);
			setState(798);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(797);
				sn_virtual_router_definition();
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

	public static class Sn_virtual_router_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Snvr_interfaceContext snvr_interface() {
			return getRuleContext(Snvr_interfaceContext.class,0);
		}
		public Snvr_protocolContext snvr_protocol() {
			return getRuleContext(Snvr_protocolContext.class,0);
		}
		public Snvr_routing_tableContext snvr_routing_table() {
			return getRuleContext(Snvr_routing_tableContext.class,0);
		}
		public Sn_virtual_router_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sn_virtual_router_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSn_virtual_router_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSn_virtual_router_definition(this);
		}
	}

	public final Sn_virtual_router_definitionContext sn_virtual_router_definition() throws RecognitionException {
		Sn_virtual_router_definitionContext _localctx = new Sn_virtual_router_definitionContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_sn_virtual_router_definition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(800);
			((Sn_virtual_router_definitionContext)_localctx).name = variable();
			setState(804);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTERFACE:
				{
				setState(801);
				snvr_interface();
				}
				break;
			case PROTOCOL:
				{
				setState(802);
				snvr_protocol();
				}
				break;
			case ROUTING_TABLE:
				{
				setState(803);
				snvr_routing_table();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Snicp_global_protectContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode GLOBAL_PROTECT_APP_CRYPTO_PROFILES() { return getToken(PaloAltoParser.GLOBAL_PROTECT_APP_CRYPTO_PROFILES, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Cp_encryptionContext cp_encryption() {
			return getRuleContext(Cp_encryptionContext.class,0);
		}
		public Cp_authenticationContext cp_authentication() {
			return getRuleContext(Cp_authenticationContext.class,0);
		}
		public Snicp_global_protectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snicp_global_protect; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnicp_global_protect(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnicp_global_protect(this);
		}
	}

	public final Snicp_global_protectContext snicp_global_protect() throws RecognitionException {
		Snicp_global_protectContext _localctx = new Snicp_global_protectContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_snicp_global_protect);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(806);
			match(GLOBAL_PROTECT_APP_CRYPTO_PROFILES);
			setState(807);
			((Snicp_global_protectContext)_localctx).name = variable();
			setState(810);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ENCRYPTION:
				{
				setState(808);
				cp_encryption();
				}
				break;
			case AUTHENTICATION:
				{
				setState(809);
				cp_authentication();
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

	public static class Snicp_ike_crypto_profilesContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode IKE_CRYPTO_PROFILES() { return getToken(PaloAltoParser.IKE_CRYPTO_PROFILES, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Cp_dh_groupContext cp_dh_group() {
			return getRuleContext(Cp_dh_groupContext.class,0);
		}
		public Cp_encryptionContext cp_encryption() {
			return getRuleContext(Cp_encryptionContext.class,0);
		}
		public Cp_hashContext cp_hash() {
			return getRuleContext(Cp_hashContext.class,0);
		}
		public Cp_lifetimeContext cp_lifetime() {
			return getRuleContext(Cp_lifetimeContext.class,0);
		}
		public Snicp_ike_crypto_profilesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snicp_ike_crypto_profiles; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnicp_ike_crypto_profiles(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnicp_ike_crypto_profiles(this);
		}
	}

	public final Snicp_ike_crypto_profilesContext snicp_ike_crypto_profiles() throws RecognitionException {
		Snicp_ike_crypto_profilesContext _localctx = new Snicp_ike_crypto_profilesContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_snicp_ike_crypto_profiles);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(812);
			match(IKE_CRYPTO_PROFILES);
			setState(813);
			((Snicp_ike_crypto_profilesContext)_localctx).name = variable();
			setState(818);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DH_GROUP:
				{
				setState(814);
				cp_dh_group();
				}
				break;
			case ENCRYPTION:
				{
				setState(815);
				cp_encryption();
				}
				break;
			case HASH:
				{
				setState(816);
				cp_hash();
				}
				break;
			case LIFETIME:
				{
				setState(817);
				cp_lifetime();
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

	public static class Snicp_ipsec_crypto_profilesContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode IPSEC_CRYPTO_PROFILES() { return getToken(PaloAltoParser.IPSEC_CRYPTO_PROFILES, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Cp_dh_groupContext cp_dh_group() {
			return getRuleContext(Cp_dh_groupContext.class,0);
		}
		public Cp_lifetimeContext cp_lifetime() {
			return getRuleContext(Cp_lifetimeContext.class,0);
		}
		public TerminalNode ESP() { return getToken(PaloAltoParser.ESP, 0); }
		public Cp_authenticationContext cp_authentication() {
			return getRuleContext(Cp_authenticationContext.class,0);
		}
		public Cp_encryptionContext cp_encryption() {
			return getRuleContext(Cp_encryptionContext.class,0);
		}
		public Snicp_ipsec_crypto_profilesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snicp_ipsec_crypto_profiles; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnicp_ipsec_crypto_profiles(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnicp_ipsec_crypto_profiles(this);
		}
	}

	public final Snicp_ipsec_crypto_profilesContext snicp_ipsec_crypto_profiles() throws RecognitionException {
		Snicp_ipsec_crypto_profilesContext _localctx = new Snicp_ipsec_crypto_profilesContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_snicp_ipsec_crypto_profiles);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(820);
			match(IPSEC_CRYPTO_PROFILES);
			setState(821);
			((Snicp_ipsec_crypto_profilesContext)_localctx).name = variable();
			setState(829);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ESP:
				{
				{
				setState(822);
				match(ESP);
				setState(825);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case AUTHENTICATION:
					{
					setState(823);
					cp_authentication();
					}
					break;
				case ENCRYPTION:
					{
					setState(824);
					cp_encryption();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				break;
			case DH_GROUP:
				{
				setState(827);
				cp_dh_group();
				}
				break;
			case LIFETIME:
				{
				setState(828);
				cp_lifetime();
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

	public static class Snvr_interfaceContext extends ParserRuleContext {
		public TerminalNode INTERFACE() { return getToken(PaloAltoParser.INTERFACE, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Snvr_interfaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvr_interface; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvr_interface(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvr_interface(this);
		}
	}

	public final Snvr_interfaceContext snvr_interface() throws RecognitionException {
		Snvr_interfaceContext _localctx = new Snvr_interfaceContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_snvr_interface);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(831);
			match(INTERFACE);
			setState(832);
			variable_list();
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

	public static class Snvr_protocolContext extends ParserRuleContext {
		public TerminalNode PROTOCOL() { return getToken(PaloAltoParser.PROTOCOL, 0); }
		public Snvrp_bgpContext snvrp_bgp() {
			return getRuleContext(Snvrp_bgpContext.class,0);
		}
		public Snvr_protocolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvr_protocol; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvr_protocol(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvr_protocol(this);
		}
	}

	public final Snvr_protocolContext snvr_protocol() throws RecognitionException {
		Snvr_protocolContext _localctx = new Snvr_protocolContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_snvr_protocol);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(834);
			match(PROTOCOL);
			setState(835);
			snvrp_bgp();
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

	public static class Snvr_routing_tableContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode ROUTING_TABLE() { return getToken(PaloAltoParser.ROUTING_TABLE, 0); }
		public TerminalNode IP() { return getToken(PaloAltoParser.IP, 0); }
		public TerminalNode STATIC_ROUTE() { return getToken(PaloAltoParser.STATIC_ROUTE, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Snvrrt_admin_distContext snvrrt_admin_dist() {
			return getRuleContext(Snvrrt_admin_distContext.class,0);
		}
		public Snvrrt_destinationContext snvrrt_destination() {
			return getRuleContext(Snvrrt_destinationContext.class,0);
		}
		public Snvrrt_interfaceContext snvrrt_interface() {
			return getRuleContext(Snvrrt_interfaceContext.class,0);
		}
		public Snvrrt_metricContext snvrrt_metric() {
			return getRuleContext(Snvrrt_metricContext.class,0);
		}
		public Snvrrt_nexthopContext snvrrt_nexthop() {
			return getRuleContext(Snvrrt_nexthopContext.class,0);
		}
		public Snvr_routing_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvr_routing_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvr_routing_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvr_routing_table(this);
		}
	}

	public final Snvr_routing_tableContext snvr_routing_table() throws RecognitionException {
		Snvr_routing_tableContext _localctx = new Snvr_routing_tableContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_snvr_routing_table);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(837);
			match(ROUTING_TABLE);
			setState(838);
			match(IP);
			setState(839);
			match(STATIC_ROUTE);
			setState(840);
			((Snvr_routing_tableContext)_localctx).name = variable();
			setState(846);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ADMIN_DIST:
				{
				setState(841);
				snvrrt_admin_dist();
				}
				break;
			case DESTINATION:
				{
				setState(842);
				snvrrt_destination();
				}
				break;
			case INTERFACE:
				{
				setState(843);
				snvrrt_interface();
				}
				break;
			case METRIC:
				{
				setState(844);
				snvrrt_metric();
				}
				break;
			case NEXTHOP:
				{
				setState(845);
				snvrrt_nexthop();
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

	public static class Snvrp_bgpContext extends ParserRuleContext {
		public TerminalNode BGP() { return getToken(PaloAltoParser.BGP, 0); }
		public Snvrp_bgp_enableContext snvrp_bgp_enable() {
			return getRuleContext(Snvrp_bgp_enableContext.class,0);
		}
		public Snvrp_bgp_nullContext snvrp_bgp_null() {
			return getRuleContext(Snvrp_bgp_nullContext.class,0);
		}
		public Snvrp_bgpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrp_bgp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrp_bgp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrp_bgp(this);
		}
	}

	public final Snvrp_bgpContext snvrp_bgp() throws RecognitionException {
		Snvrp_bgpContext _localctx = new Snvrp_bgpContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_snvrp_bgp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(848);
			match(BGP);
			setState(851);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ENABLE:
				{
				setState(849);
				snvrp_bgp_enable();
				}
				break;
			case DAMPENING_PROFILE:
				{
				setState(850);
				snvrp_bgp_null();
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

	public static class Snvrp_bgp_enableContext extends ParserRuleContext {
		public TerminalNode ENABLE() { return getToken(PaloAltoParser.ENABLE, 0); }
		public TerminalNode NO() { return getToken(PaloAltoParser.NO, 0); }
		public Snvrp_bgp_enableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrp_bgp_enable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrp_bgp_enable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrp_bgp_enable(this);
		}
	}

	public final Snvrp_bgp_enableContext snvrp_bgp_enable() throws RecognitionException {
		Snvrp_bgp_enableContext _localctx = new Snvrp_bgp_enableContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_snvrp_bgp_enable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(853);
			match(ENABLE);
			setState(854);
			match(NO);
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

	public static class Snvrp_bgp_nullContext extends ParserRuleContext {
		public TerminalNode DAMPENING_PROFILE() { return getToken(PaloAltoParser.DAMPENING_PROFILE, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Snvrp_bgp_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrp_bgp_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrp_bgp_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrp_bgp_null(this);
		}
	}

	public final Snvrp_bgp_nullContext snvrp_bgp_null() throws RecognitionException {
		Snvrp_bgp_nullContext _localctx = new Snvrp_bgp_nullContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_snvrp_bgp_null);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(856);
			match(DAMPENING_PROFILE);
			setState(857);
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

	public static class Snvrrt_admin_distContext extends ParserRuleContext {
		public Token distance;
		public TerminalNode ADMIN_DIST() { return getToken(PaloAltoParser.ADMIN_DIST, 0); }
		public TerminalNode DEC() { return getToken(PaloAltoParser.DEC, 0); }
		public Snvrrt_admin_distContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrrt_admin_dist; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrrt_admin_dist(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrrt_admin_dist(this);
		}
	}

	public final Snvrrt_admin_distContext snvrrt_admin_dist() throws RecognitionException {
		Snvrrt_admin_distContext _localctx = new Snvrrt_admin_distContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_snvrrt_admin_dist);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(859);
			match(ADMIN_DIST);
			setState(860);
			((Snvrrt_admin_distContext)_localctx).distance = match(DEC);
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

	public static class Snvrrt_destinationContext extends ParserRuleContext {
		public Token destination;
		public TerminalNode DESTINATION() { return getToken(PaloAltoParser.DESTINATION, 0); }
		public TerminalNode IP_PREFIX() { return getToken(PaloAltoParser.IP_PREFIX, 0); }
		public Snvrrt_destinationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrrt_destination; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrrt_destination(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrrt_destination(this);
		}
	}

	public final Snvrrt_destinationContext snvrrt_destination() throws RecognitionException {
		Snvrrt_destinationContext _localctx = new Snvrrt_destinationContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_snvrrt_destination);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(862);
			match(DESTINATION);
			setState(863);
			((Snvrrt_destinationContext)_localctx).destination = match(IP_PREFIX);
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

	public static class Snvrrt_interfaceContext extends ParserRuleContext {
		public VariableContext iface;
		public TerminalNode INTERFACE() { return getToken(PaloAltoParser.INTERFACE, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Snvrrt_interfaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrrt_interface; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrrt_interface(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrrt_interface(this);
		}
	}

	public final Snvrrt_interfaceContext snvrrt_interface() throws RecognitionException {
		Snvrrt_interfaceContext _localctx = new Snvrrt_interfaceContext(_ctx, getState());
		enterRule(_localctx, 230, RULE_snvrrt_interface);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(865);
			match(INTERFACE);
			setState(866);
			((Snvrrt_interfaceContext)_localctx).iface = variable();
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

	public static class Snvrrt_metricContext extends ParserRuleContext {
		public Token metric;
		public TerminalNode METRIC() { return getToken(PaloAltoParser.METRIC, 0); }
		public TerminalNode DEC() { return getToken(PaloAltoParser.DEC, 0); }
		public Snvrrt_metricContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrrt_metric; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrrt_metric(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrrt_metric(this);
		}
	}

	public final Snvrrt_metricContext snvrrt_metric() throws RecognitionException {
		Snvrrt_metricContext _localctx = new Snvrrt_metricContext(_ctx, getState());
		enterRule(_localctx, 232, RULE_snvrrt_metric);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(868);
			match(METRIC);
			setState(869);
			((Snvrrt_metricContext)_localctx).metric = match(DEC);
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

	public static class Snvrrt_nexthopContext extends ParserRuleContext {
		public TerminalNode NEXTHOP() { return getToken(PaloAltoParser.NEXTHOP, 0); }
		public Snvrrtn_ipContext snvrrtn_ip() {
			return getRuleContext(Snvrrtn_ipContext.class,0);
		}
		public Snvrrtn_next_vrContext snvrrtn_next_vr() {
			return getRuleContext(Snvrrtn_next_vrContext.class,0);
		}
		public Snvrrt_nexthopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrrt_nexthop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrrt_nexthop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrrt_nexthop(this);
		}
	}

	public final Snvrrt_nexthopContext snvrrt_nexthop() throws RecognitionException {
		Snvrrt_nexthopContext _localctx = new Snvrrt_nexthopContext(_ctx, getState());
		enterRule(_localctx, 234, RULE_snvrrt_nexthop);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(871);
			match(NEXTHOP);
			setState(874);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IP_ADDRESS_LITERAL:
				{
				setState(872);
				snvrrtn_ip();
				}
				break;
			case NEXT_VR:
				{
				setState(873);
				snvrrtn_next_vr();
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

	public static class Snvrrtn_ipContext extends ParserRuleContext {
		public Token address;
		public TerminalNode IP_ADDRESS_LITERAL() { return getToken(PaloAltoParser.IP_ADDRESS_LITERAL, 0); }
		public TerminalNode IP_ADDRESS() { return getToken(PaloAltoParser.IP_ADDRESS, 0); }
		public Snvrrtn_ipContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrrtn_ip; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrrtn_ip(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrrtn_ip(this);
		}
	}

	public final Snvrrtn_ipContext snvrrtn_ip() throws RecognitionException {
		Snvrrtn_ipContext _localctx = new Snvrrtn_ipContext(_ctx, getState());
		enterRule(_localctx, 236, RULE_snvrrtn_ip);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(876);
			match(IP_ADDRESS_LITERAL);
			setState(877);
			((Snvrrtn_ipContext)_localctx).address = match(IP_ADDRESS);
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

	public static class Snvrrtn_next_vrContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode NEXT_VR() { return getToken(PaloAltoParser.NEXT_VR, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Snvrrtn_next_vrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snvrrtn_next_vr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSnvrrtn_next_vr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSnvrrtn_next_vr(this);
		}
	}

	public final Snvrrtn_next_vrContext snvrrtn_next_vr() throws RecognitionException {
		Snvrrtn_next_vrContext _localctx = new Snvrrtn_next_vrContext(_ctx, getState());
		enterRule(_localctx, 238, RULE_snvrrtn_next_vr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(879);
			match(NEXT_VR);
			setState(880);
			((Snvrrtn_next_vrContext)_localctx).name = variable();
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

	public static class S_rulebaseContext extends ParserRuleContext {
		public TerminalNode RULEBASE() { return getToken(PaloAltoParser.RULEBASE, 0); }
		public Rulebase_innerContext rulebase_inner() {
			return getRuleContext(Rulebase_innerContext.class,0);
		}
		public S_rulebaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_rulebase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_rulebase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_rulebase(this);
		}
	}

	public final S_rulebaseContext s_rulebase() throws RecognitionException {
		S_rulebaseContext _localctx = new S_rulebaseContext(_ctx, getState());
		enterRule(_localctx, 240, RULE_s_rulebase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(882);
			match(RULEBASE);
			setState(883);
			rulebase_inner();
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

	public static class Rulebase_innerContext extends ParserRuleContext {
		public Sr_securityContext sr_security() {
			return getRuleContext(Sr_securityContext.class,0);
		}
		public Rulebase_innerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rulebase_inner; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterRulebase_inner(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitRulebase_inner(this);
		}
	}

	public final Rulebase_innerContext rulebase_inner() throws RecognitionException {
		Rulebase_innerContext _localctx = new Rulebase_innerContext(_ctx, getState());
		enterRule(_localctx, 242, RULE_rulebase_inner);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(885);
			sr_security();
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

	public static class Sr_securityContext extends ParserRuleContext {
		public TerminalNode SECURITY() { return getToken(PaloAltoParser.SECURITY, 0); }
		public Sr_security_rulesContext sr_security_rules() {
			return getRuleContext(Sr_security_rulesContext.class,0);
		}
		public Sr_securityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sr_security; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSr_security(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSr_security(this);
		}
	}

	public final Sr_securityContext sr_security() throws RecognitionException {
		Sr_securityContext _localctx = new Sr_securityContext(_ctx, getState());
		enterRule(_localctx, 244, RULE_sr_security);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(887);
			match(SECURITY);
			setState(889);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RULES) {
				{
				setState(888);
				sr_security_rules();
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

	public static class Sr_security_rulesContext extends ParserRuleContext {
		public TerminalNode RULES() { return getToken(PaloAltoParser.RULES, 0); }
		public Srs_definitionContext srs_definition() {
			return getRuleContext(Srs_definitionContext.class,0);
		}
		public Sr_security_rulesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sr_security_rules; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSr_security_rules(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSr_security_rules(this);
		}
	}

	public final Sr_security_rulesContext sr_security_rules() throws RecognitionException {
		Sr_security_rulesContext _localctx = new Sr_security_rulesContext(_ctx, getState());
		enterRule(_localctx, 246, RULE_sr_security_rules);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(891);
			match(RULES);
			setState(893);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(892);
				srs_definition();
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

	public static class Srs_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Srs_actionContext srs_action() {
			return getRuleContext(Srs_actionContext.class,0);
		}
		public Srs_applicationContext srs_application() {
			return getRuleContext(Srs_applicationContext.class,0);
		}
		public Srs_categoryContext srs_category() {
			return getRuleContext(Srs_categoryContext.class,0);
		}
		public Srs_descriptionContext srs_description() {
			return getRuleContext(Srs_descriptionContext.class,0);
		}
		public Srs_destinationContext srs_destination() {
			return getRuleContext(Srs_destinationContext.class,0);
		}
		public Srs_disabledContext srs_disabled() {
			return getRuleContext(Srs_disabledContext.class,0);
		}
		public Srs_fromContext srs_from() {
			return getRuleContext(Srs_fromContext.class,0);
		}
		public Srs_hip_profilesContext srs_hip_profiles() {
			return getRuleContext(Srs_hip_profilesContext.class,0);
		}
		public Srs_negate_destinationContext srs_negate_destination() {
			return getRuleContext(Srs_negate_destinationContext.class,0);
		}
		public Srs_negate_sourceContext srs_negate_source() {
			return getRuleContext(Srs_negate_sourceContext.class,0);
		}
		public Srs_serviceContext srs_service() {
			return getRuleContext(Srs_serviceContext.class,0);
		}
		public Srs_sourceContext srs_source() {
			return getRuleContext(Srs_sourceContext.class,0);
		}
		public Srs_source_userContext srs_source_user() {
			return getRuleContext(Srs_source_userContext.class,0);
		}
		public Srs_toContext srs_to() {
			return getRuleContext(Srs_toContext.class,0);
		}
		public Srs_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_definition(this);
		}
	}

	public final Srs_definitionContext srs_definition() throws RecognitionException {
		Srs_definitionContext _localctx = new Srs_definitionContext(_ctx, getState());
		enterRule(_localctx, 248, RULE_srs_definition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(895);
			((Srs_definitionContext)_localctx).name = variable();
			setState(910);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ACTION:
				{
				setState(896);
				srs_action();
				}
				break;
			case APPLICATION:
				{
				setState(897);
				srs_application();
				}
				break;
			case CATEGORY:
				{
				setState(898);
				srs_category();
				}
				break;
			case DESCRIPTION:
				{
				setState(899);
				srs_description();
				}
				break;
			case DESTINATION:
				{
				setState(900);
				srs_destination();
				}
				break;
			case DISABLED:
				{
				setState(901);
				srs_disabled();
				}
				break;
			case FROM:
				{
				setState(902);
				srs_from();
				}
				break;
			case HIP_PROFILES:
				{
				setState(903);
				srs_hip_profiles();
				}
				break;
			case NEGATE_DESTINATION:
				{
				setState(904);
				srs_negate_destination();
				}
				break;
			case NEGATE_SOURCE:
				{
				setState(905);
				srs_negate_source();
				}
				break;
			case SERVICE:
				{
				setState(906);
				srs_service();
				}
				break;
			case SOURCE:
				{
				setState(907);
				srs_source();
				}
				break;
			case SOURCE_USER:
				{
				setState(908);
				srs_source_user();
				}
				break;
			case TO:
				{
				setState(909);
				srs_to();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Srs_actionContext extends ParserRuleContext {
		public TerminalNode ACTION() { return getToken(PaloAltoParser.ACTION, 0); }
		public TerminalNode ALLOW() { return getToken(PaloAltoParser.ALLOW, 0); }
		public TerminalNode DENY() { return getToken(PaloAltoParser.DENY, 0); }
		public TerminalNode DROP() { return getToken(PaloAltoParser.DROP, 0); }
		public TerminalNode RESET_BOTH() { return getToken(PaloAltoParser.RESET_BOTH, 0); }
		public TerminalNode RESET_CLIENT() { return getToken(PaloAltoParser.RESET_CLIENT, 0); }
		public TerminalNode RESET_SERVER() { return getToken(PaloAltoParser.RESET_SERVER, 0); }
		public Srs_actionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_action; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_action(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_action(this);
		}
	}

	public final Srs_actionContext srs_action() throws RecognitionException {
		Srs_actionContext _localctx = new Srs_actionContext(_ctx, getState());
		enterRule(_localctx, 250, RULE_srs_action);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(912);
			match(ACTION);
			setState(913);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ALLOW) | (1L << DENY) | (1L << DROP))) != 0) || ((((_la - 108)) & ~0x3f) == 0 && ((1L << (_la - 108)) & ((1L << (RESET_BOTH - 108)) | (1L << (RESET_CLIENT - 108)) | (1L << (RESET_SERVER - 108)))) != 0)) ) {
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

	public static class Srs_applicationContext extends ParserRuleContext {
		public TerminalNode APPLICATION() { return getToken(PaloAltoParser.APPLICATION, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Srs_applicationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_application; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_application(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_application(this);
		}
	}

	public final Srs_applicationContext srs_application() throws RecognitionException {
		Srs_applicationContext _localctx = new Srs_applicationContext(_ctx, getState());
		enterRule(_localctx, 252, RULE_srs_application);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(915);
			match(APPLICATION);
			setState(916);
			variable_list();
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

	public static class Srs_categoryContext extends ParserRuleContext {
		public TerminalNode CATEGORY() { return getToken(PaloAltoParser.CATEGORY, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Srs_categoryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_category; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_category(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_category(this);
		}
	}

	public final Srs_categoryContext srs_category() throws RecognitionException {
		Srs_categoryContext _localctx = new Srs_categoryContext(_ctx, getState());
		enterRule(_localctx, 254, RULE_srs_category);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(918);
			match(CATEGORY);
			setState(919);
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

	public static class Srs_descriptionContext extends ParserRuleContext {
		public VariableContext description;
		public TerminalNode DESCRIPTION() { return getToken(PaloAltoParser.DESCRIPTION, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Srs_descriptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_description; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_description(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_description(this);
		}
	}

	public final Srs_descriptionContext srs_description() throws RecognitionException {
		Srs_descriptionContext _localctx = new Srs_descriptionContext(_ctx, getState());
		enterRule(_localctx, 256, RULE_srs_description);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(921);
			match(DESCRIPTION);
			setState(922);
			((Srs_descriptionContext)_localctx).description = variable();
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

	public static class Srs_destinationContext extends ParserRuleContext {
		public TerminalNode DESTINATION() { return getToken(PaloAltoParser.DESTINATION, 0); }
		public Src_or_dst_listContext src_or_dst_list() {
			return getRuleContext(Src_or_dst_listContext.class,0);
		}
		public Srs_destinationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_destination; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_destination(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_destination(this);
		}
	}

	public final Srs_destinationContext srs_destination() throws RecognitionException {
		Srs_destinationContext _localctx = new Srs_destinationContext(_ctx, getState());
		enterRule(_localctx, 258, RULE_srs_destination);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(924);
			match(DESTINATION);
			setState(925);
			src_or_dst_list();
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

	public static class Srs_disabledContext extends ParserRuleContext {
		public TerminalNode DISABLED() { return getToken(PaloAltoParser.DISABLED, 0); }
		public TerminalNode NO() { return getToken(PaloAltoParser.NO, 0); }
		public TerminalNode YES() { return getToken(PaloAltoParser.YES, 0); }
		public Srs_disabledContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_disabled; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_disabled(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_disabled(this);
		}
	}

	public final Srs_disabledContext srs_disabled() throws RecognitionException {
		Srs_disabledContext _localctx = new Srs_disabledContext(_ctx, getState());
		enterRule(_localctx, 260, RULE_srs_disabled);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(927);
			match(DISABLED);
			setState(928);
			_la = _input.LA(1);
			if ( !(_la==NO || _la==YES) ) {
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

	public static class Srs_fromContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(PaloAltoParser.FROM, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Srs_fromContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_from; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_from(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_from(this);
		}
	}

	public final Srs_fromContext srs_from() throws RecognitionException {
		Srs_fromContext _localctx = new Srs_fromContext(_ctx, getState());
		enterRule(_localctx, 262, RULE_srs_from);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(930);
			match(FROM);
			setState(931);
			variable_list();
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

	public static class Srs_hip_profilesContext extends ParserRuleContext {
		public TerminalNode HIP_PROFILES() { return getToken(PaloAltoParser.HIP_PROFILES, 0); }
		public TerminalNode ANY() { return getToken(PaloAltoParser.ANY, 0); }
		public Srs_hip_profilesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_hip_profiles; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_hip_profiles(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_hip_profiles(this);
		}
	}

	public final Srs_hip_profilesContext srs_hip_profiles() throws RecognitionException {
		Srs_hip_profilesContext _localctx = new Srs_hip_profilesContext(_ctx, getState());
		enterRule(_localctx, 264, RULE_srs_hip_profiles);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(933);
			match(HIP_PROFILES);
			setState(934);
			match(ANY);
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

	public static class Srs_negate_destinationContext extends ParserRuleContext {
		public TerminalNode NEGATE_DESTINATION() { return getToken(PaloAltoParser.NEGATE_DESTINATION, 0); }
		public TerminalNode YES() { return getToken(PaloAltoParser.YES, 0); }
		public TerminalNode NO() { return getToken(PaloAltoParser.NO, 0); }
		public Srs_negate_destinationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_negate_destination; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_negate_destination(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_negate_destination(this);
		}
	}

	public final Srs_negate_destinationContext srs_negate_destination() throws RecognitionException {
		Srs_negate_destinationContext _localctx = new Srs_negate_destinationContext(_ctx, getState());
		enterRule(_localctx, 266, RULE_srs_negate_destination);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(936);
			match(NEGATE_DESTINATION);
			setState(937);
			_la = _input.LA(1);
			if ( !(_la==NO || _la==YES) ) {
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

	public static class Srs_negate_sourceContext extends ParserRuleContext {
		public TerminalNode NEGATE_SOURCE() { return getToken(PaloAltoParser.NEGATE_SOURCE, 0); }
		public TerminalNode YES() { return getToken(PaloAltoParser.YES, 0); }
		public TerminalNode NO() { return getToken(PaloAltoParser.NO, 0); }
		public Srs_negate_sourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_negate_source; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_negate_source(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_negate_source(this);
		}
	}

	public final Srs_negate_sourceContext srs_negate_source() throws RecognitionException {
		Srs_negate_sourceContext _localctx = new Srs_negate_sourceContext(_ctx, getState());
		enterRule(_localctx, 268, RULE_srs_negate_source);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(939);
			match(NEGATE_SOURCE);
			setState(940);
			_la = _input.LA(1);
			if ( !(_la==NO || _la==YES) ) {
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

	public static class Srs_serviceContext extends ParserRuleContext {
		public TerminalNode SERVICE() { return getToken(PaloAltoParser.SERVICE, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Srs_serviceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_service; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_service(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_service(this);
		}
	}

	public final Srs_serviceContext srs_service() throws RecognitionException {
		Srs_serviceContext _localctx = new Srs_serviceContext(_ctx, getState());
		enterRule(_localctx, 270, RULE_srs_service);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(942);
			match(SERVICE);
			setState(943);
			variable_list();
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

	public static class Srs_sourceContext extends ParserRuleContext {
		public TerminalNode SOURCE() { return getToken(PaloAltoParser.SOURCE, 0); }
		public Src_or_dst_listContext src_or_dst_list() {
			return getRuleContext(Src_or_dst_listContext.class,0);
		}
		public Srs_sourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_source; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_source(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_source(this);
		}
	}

	public final Srs_sourceContext srs_source() throws RecognitionException {
		Srs_sourceContext _localctx = new Srs_sourceContext(_ctx, getState());
		enterRule(_localctx, 272, RULE_srs_source);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(945);
			match(SOURCE);
			setState(946);
			src_or_dst_list();
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

	public static class Srs_source_userContext extends ParserRuleContext {
		public TerminalNode SOURCE_USER() { return getToken(PaloAltoParser.SOURCE_USER, 0); }
		public TerminalNode ANY() { return getToken(PaloAltoParser.ANY, 0); }
		public Srs_source_userContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_source_user; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_source_user(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_source_user(this);
		}
	}

	public final Srs_source_userContext srs_source_user() throws RecognitionException {
		Srs_source_userContext _localctx = new Srs_source_userContext(_ctx, getState());
		enterRule(_localctx, 274, RULE_srs_source_user);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(948);
			match(SOURCE_USER);
			setState(949);
			match(ANY);
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

	public static class Srs_toContext extends ParserRuleContext {
		public TerminalNode TO() { return getToken(PaloAltoParser.TO, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Srs_toContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_srs_to; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSrs_to(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSrs_to(this);
		}
	}

	public final Srs_toContext srs_to() throws RecognitionException {
		Srs_toContext _localctx = new Srs_toContext(_ctx, getState());
		enterRule(_localctx, 276, RULE_srs_to);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(951);
			match(TO);
			setState(952);
			variable_list();
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

	public static class S_serviceContext extends ParserRuleContext {
		public TerminalNode SERVICE() { return getToken(PaloAltoParser.SERVICE, 0); }
		public S_service_definitionContext s_service_definition() {
			return getRuleContext(S_service_definitionContext.class,0);
		}
		public S_serviceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_service; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_service(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_service(this);
		}
	}

	public final S_serviceContext s_service() throws RecognitionException {
		S_serviceContext _localctx = new S_serviceContext(_ctx, getState());
		enterRule(_localctx, 278, RULE_s_service);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(954);
			match(SERVICE);
			setState(956);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(955);
				s_service_definition();
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

	public static class S_service_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public List<Sserv_descriptionContext> sserv_description() {
			return getRuleContexts(Sserv_descriptionContext.class);
		}
		public Sserv_descriptionContext sserv_description(int i) {
			return getRuleContext(Sserv_descriptionContext.class,i);
		}
		public List<Sserv_portContext> sserv_port() {
			return getRuleContexts(Sserv_portContext.class);
		}
		public Sserv_portContext sserv_port(int i) {
			return getRuleContext(Sserv_portContext.class,i);
		}
		public List<Sserv_protocolContext> sserv_protocol() {
			return getRuleContexts(Sserv_protocolContext.class);
		}
		public Sserv_protocolContext sserv_protocol(int i) {
			return getRuleContext(Sserv_protocolContext.class,i);
		}
		public List<Sserv_source_portContext> sserv_source_port() {
			return getRuleContexts(Sserv_source_portContext.class);
		}
		public Sserv_source_portContext sserv_source_port(int i) {
			return getRuleContext(Sserv_source_portContext.class,i);
		}
		public S_service_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_service_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_service_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_service_definition(this);
		}
	}

	public final S_service_definitionContext s_service_definition() throws RecognitionException {
		S_service_definitionContext _localctx = new S_service_definitionContext(_ctx, getState());
		enterRule(_localctx, 280, RULE_s_service_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(958);
			((S_service_definitionContext)_localctx).name = variable();
			setState(965);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DESCRIPTION || ((((_la - 100)) & ~0x3f) == 0 && ((1L << (_la - 100)) & ((1L << (PORT - 100)) | (1L << (PROTOCOL - 100)) | (1L << (SOURCE_PORT - 100)))) != 0)) {
				{
				setState(963);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case DESCRIPTION:
					{
					setState(959);
					sserv_description();
					}
					break;
				case PORT:
					{
					setState(960);
					sserv_port();
					}
					break;
				case PROTOCOL:
					{
					setState(961);
					sserv_protocol();
					}
					break;
				case SOURCE_PORT:
					{
					setState(962);
					sserv_source_port();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(967);
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

	public static class Sserv_descriptionContext extends ParserRuleContext {
		public VariableContext description;
		public TerminalNode DESCRIPTION() { return getToken(PaloAltoParser.DESCRIPTION, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sserv_descriptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sserv_description; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSserv_description(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSserv_description(this);
		}
	}

	public final Sserv_descriptionContext sserv_description() throws RecognitionException {
		Sserv_descriptionContext _localctx = new Sserv_descriptionContext(_ctx, getState());
		enterRule(_localctx, 282, RULE_sserv_description);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(968);
			match(DESCRIPTION);
			setState(969);
			((Sserv_descriptionContext)_localctx).description = variable();
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

	public static class Sserv_portContext extends ParserRuleContext {
		public TerminalNode PORT() { return getToken(PaloAltoParser.PORT, 0); }
		public Variable_port_listContext variable_port_list() {
			return getRuleContext(Variable_port_listContext.class,0);
		}
		public Sserv_portContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sserv_port; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSserv_port(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSserv_port(this);
		}
	}

	public final Sserv_portContext sserv_port() throws RecognitionException {
		Sserv_portContext _localctx = new Sserv_portContext(_ctx, getState());
		enterRule(_localctx, 284, RULE_sserv_port);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(971);
			match(PORT);
			setState(972);
			variable_port_list();
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

	public static class Sserv_protocolContext extends ParserRuleContext {
		public TerminalNode PROTOCOL() { return getToken(PaloAltoParser.PROTOCOL, 0); }
		public TerminalNode SCTP() { return getToken(PaloAltoParser.SCTP, 0); }
		public TerminalNode TCP() { return getToken(PaloAltoParser.TCP, 0); }
		public TerminalNode UDP() { return getToken(PaloAltoParser.UDP, 0); }
		public Sserv_protocolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sserv_protocol; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSserv_protocol(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSserv_protocol(this);
		}
	}

	public final Sserv_protocolContext sserv_protocol() throws RecognitionException {
		Sserv_protocolContext _localctx = new Sserv_protocolContext(_ctx, getState());
		enterRule(_localctx, 286, RULE_sserv_protocol);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(974);
			match(PROTOCOL);
			setState(975);
			_la = _input.LA(1);
			if ( !(((((_la - 114)) & ~0x3f) == 0 && ((1L << (_la - 114)) & ((1L << (SCTP - 114)) | (1L << (TCP - 114)) | (1L << (UDP - 114)))) != 0)) ) {
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

	public static class Sserv_source_portContext extends ParserRuleContext {
		public TerminalNode SOURCE_PORT() { return getToken(PaloAltoParser.SOURCE_PORT, 0); }
		public Variable_port_listContext variable_port_list() {
			return getRuleContext(Variable_port_listContext.class,0);
		}
		public Sserv_source_portContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sserv_source_port; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSserv_source_port(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSserv_source_port(this);
		}
	}

	public final Sserv_source_portContext sserv_source_port() throws RecognitionException {
		Sserv_source_portContext _localctx = new Sserv_source_portContext(_ctx, getState());
		enterRule(_localctx, 288, RULE_sserv_source_port);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(977);
			match(SOURCE_PORT);
			setState(978);
			variable_port_list();
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

	public static class S_service_groupContext extends ParserRuleContext {
		public TerminalNode SERVICE_GROUP() { return getToken(PaloAltoParser.SERVICE_GROUP, 0); }
		public S_service_group_definitionContext s_service_group_definition() {
			return getRuleContext(S_service_group_definitionContext.class,0);
		}
		public S_service_groupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_service_group; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_service_group(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_service_group(this);
		}
	}

	public final S_service_groupContext s_service_group() throws RecognitionException {
		S_service_groupContext _localctx = new S_service_groupContext(_ctx, getState());
		enterRule(_localctx, 290, RULE_s_service_group);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(980);
			match(SERVICE_GROUP);
			setState(982);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(981);
				s_service_group_definition();
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

	public static class S_service_group_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sservgrp_membersContext sservgrp_members() {
			return getRuleContext(Sservgrp_membersContext.class,0);
		}
		public S_service_group_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_service_group_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_service_group_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_service_group_definition(this);
		}
	}

	public final S_service_group_definitionContext s_service_group_definition() throws RecognitionException {
		S_service_group_definitionContext _localctx = new S_service_group_definitionContext(_ctx, getState());
		enterRule(_localctx, 292, RULE_s_service_group_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(984);
			((S_service_group_definitionContext)_localctx).name = variable();
			setState(986);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MEMBERS) {
				{
				setState(985);
				sservgrp_members();
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

	public static class Sservgrp_membersContext extends ParserRuleContext {
		public TerminalNode MEMBERS() { return getToken(PaloAltoParser.MEMBERS, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Sservgrp_membersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sservgrp_members; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSservgrp_members(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSservgrp_members(this);
		}
	}

	public final Sservgrp_membersContext sservgrp_members() throws RecognitionException {
		Sservgrp_membersContext _localctx = new Sservgrp_membersContext(_ctx, getState());
		enterRule(_localctx, 294, RULE_sservgrp_members);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(988);
			match(MEMBERS);
			setState(989);
			variable_list();
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

	public static class S_sharedContext extends ParserRuleContext {
		public TerminalNode SHARED() { return getToken(PaloAltoParser.SHARED, 0); }
		public Ss_commonContext ss_common() {
			return getRuleContext(Ss_commonContext.class,0);
		}
		public Ss_nullContext ss_null() {
			return getRuleContext(Ss_nullContext.class,0);
		}
		public S_sharedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_shared; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_shared(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_shared(this);
		}
	}

	public final S_sharedContext s_shared() throws RecognitionException {
		S_sharedContext _localctx = new S_sharedContext(_ctx, getState());
		enterRule(_localctx, 296, RULE_s_shared);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(991);
			match(SHARED);
			setState(994);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ADDRESS:
			case ADDRESS_GROUP:
			case APPLICATION:
			case APPLICATION_GROUP:
			case LOG_SETTINGS:
			case SERVICE:
			case SERVICE_GROUP:
				{
				setState(992);
				ss_common();
				}
				break;
			case BOTNET:
				{
				setState(993);
				ss_null();
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

	public static class Ss_commonContext extends ParserRuleContext {
		public S_addressContext s_address() {
			return getRuleContext(S_addressContext.class,0);
		}
		public S_address_groupContext s_address_group() {
			return getRuleContext(S_address_groupContext.class,0);
		}
		public S_applicationContext s_application() {
			return getRuleContext(S_applicationContext.class,0);
		}
		public S_application_groupContext s_application_group() {
			return getRuleContext(S_application_groupContext.class,0);
		}
		public S_serviceContext s_service() {
			return getRuleContext(S_serviceContext.class,0);
		}
		public S_service_groupContext s_service_group() {
			return getRuleContext(S_service_groupContext.class,0);
		}
		public Ss_log_settingsContext ss_log_settings() {
			return getRuleContext(Ss_log_settingsContext.class,0);
		}
		public Ss_commonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ss_common; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSs_common(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSs_common(this);
		}
	}

	public final Ss_commonContext ss_common() throws RecognitionException {
		Ss_commonContext _localctx = new Ss_commonContext(_ctx, getState());
		enterRule(_localctx, 298, RULE_ss_common);
		try {
			setState(1003);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ADDRESS:
				enterOuterAlt(_localctx, 1);
				{
				setState(996);
				s_address();
				}
				break;
			case ADDRESS_GROUP:
				enterOuterAlt(_localctx, 2);
				{
				setState(997);
				s_address_group();
				}
				break;
			case APPLICATION:
				enterOuterAlt(_localctx, 3);
				{
				setState(998);
				s_application();
				}
				break;
			case APPLICATION_GROUP:
				enterOuterAlt(_localctx, 4);
				{
				setState(999);
				s_application_group();
				}
				break;
			case SERVICE:
				enterOuterAlt(_localctx, 5);
				{
				setState(1000);
				s_service();
				}
				break;
			case SERVICE_GROUP:
				enterOuterAlt(_localctx, 6);
				{
				setState(1001);
				s_service_group();
				}
				break;
			case LOG_SETTINGS:
				enterOuterAlt(_localctx, 7);
				{
				setState(1002);
				ss_log_settings();
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

	public static class Ss_log_settingsContext extends ParserRuleContext {
		public TerminalNode LOG_SETTINGS() { return getToken(PaloAltoParser.LOG_SETTINGS, 0); }
		public Ssl_syslogContext ssl_syslog() {
			return getRuleContext(Ssl_syslogContext.class,0);
		}
		public Ss_log_settingsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ss_log_settings; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSs_log_settings(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSs_log_settings(this);
		}
	}

	public final Ss_log_settingsContext ss_log_settings() throws RecognitionException {
		Ss_log_settingsContext _localctx = new Ss_log_settingsContext(_ctx, getState());
		enterRule(_localctx, 300, RULE_ss_log_settings);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1005);
			match(LOG_SETTINGS);
			{
			setState(1006);
			ssl_syslog();
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

	public static class Ss_nullContext extends ParserRuleContext {
		public TerminalNode BOTNET() { return getToken(PaloAltoParser.BOTNET, 0); }
		public Null_rest_of_lineContext null_rest_of_line() {
			return getRuleContext(Null_rest_of_lineContext.class,0);
		}
		public Ss_nullContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ss_null; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSs_null(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSs_null(this);
		}
	}

	public final Ss_nullContext ss_null() throws RecognitionException {
		Ss_nullContext _localctx = new Ss_nullContext(_ctx, getState());
		enterRule(_localctx, 302, RULE_ss_null);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1008);
			match(BOTNET);
			setState(1009);
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

	public static class Ssl_syslogContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode SYSLOG() { return getToken(PaloAltoParser.SYSLOG, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Ssls_serverContext ssls_server() {
			return getRuleContext(Ssls_serverContext.class,0);
		}
		public Ssl_syslogContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ssl_syslog; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSsl_syslog(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSsl_syslog(this);
		}
	}

	public final Ssl_syslogContext ssl_syslog() throws RecognitionException {
		Ssl_syslogContext _localctx = new Ssl_syslogContext(_ctx, getState());
		enterRule(_localctx, 304, RULE_ssl_syslog);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1011);
			match(SYSLOG);
			setState(1012);
			((Ssl_syslogContext)_localctx).name = variable();
			{
			setState(1013);
			ssls_server();
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

	public static class Ssls_serverContext extends ParserRuleContext {
		public VariableContext name;
		public TerminalNode SERVER() { return getToken(PaloAltoParser.SERVER, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sslss_serverContext sslss_server() {
			return getRuleContext(Sslss_serverContext.class,0);
		}
		public Ssls_serverContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ssls_server; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSsls_server(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSsls_server(this);
		}
	}

	public final Ssls_serverContext ssls_server() throws RecognitionException {
		Ssls_serverContext _localctx = new Ssls_serverContext(_ctx, getState());
		enterRule(_localctx, 306, RULE_ssls_server);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1015);
			match(SERVER);
			setState(1016);
			((Ssls_serverContext)_localctx).name = variable();
			{
			setState(1017);
			sslss_server();
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

	public static class Sslss_serverContext extends ParserRuleContext {
		public VariableContext address;
		public TerminalNode SERVER() { return getToken(PaloAltoParser.SERVER, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sslss_serverContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sslss_server; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSslss_server(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSslss_server(this);
		}
	}

	public final Sslss_serverContext sslss_server() throws RecognitionException {
		Sslss_serverContext _localctx = new Sslss_serverContext(_ctx, getState());
		enterRule(_localctx, 308, RULE_sslss_server);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1019);
			match(SERVER);
			setState(1020);
			((Sslss_serverContext)_localctx).address = variable();
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

	public static class S_vsysContext extends ParserRuleContext {
		public TerminalNode VSYS() { return getToken(PaloAltoParser.VSYS, 0); }
		public S_vsys_definitionContext s_vsys_definition() {
			return getRuleContext(S_vsys_definitionContext.class,0);
		}
		public S_vsysContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_vsys; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_vsys(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_vsys(this);
		}
	}

	public final S_vsysContext s_vsys() throws RecognitionException {
		S_vsysContext _localctx = new S_vsysContext(_ctx, getState());
		enterRule(_localctx, 310, RULE_s_vsys);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1022);
			match(VSYS);
			setState(1024);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(1023);
				s_vsys_definition();
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

	public static class S_vsys_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public S_rulebaseContext s_rulebase() {
			return getRuleContext(S_rulebaseContext.class,0);
		}
		public S_zoneContext s_zone() {
			return getRuleContext(S_zoneContext.class,0);
		}
		public Ss_commonContext ss_common() {
			return getRuleContext(Ss_commonContext.class,0);
		}
		public Sv_importContext sv_import() {
			return getRuleContext(Sv_importContext.class,0);
		}
		public S_vsys_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_vsys_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_vsys_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_vsys_definition(this);
		}
	}

	public final S_vsys_definitionContext s_vsys_definition() throws RecognitionException {
		S_vsys_definitionContext _localctx = new S_vsys_definitionContext(_ctx, getState());
		enterRule(_localctx, 312, RULE_s_vsys_definition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1026);
			((S_vsys_definitionContext)_localctx).name = variable();
			setState(1031);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case RULEBASE:
				{
				setState(1027);
				s_rulebase();
				}
				break;
			case ZONE:
				{
				setState(1028);
				s_zone();
				}
				break;
			case ADDRESS:
			case ADDRESS_GROUP:
			case APPLICATION:
			case APPLICATION_GROUP:
			case LOG_SETTINGS:
			case SERVICE:
			case SERVICE_GROUP:
				{
				setState(1029);
				ss_common();
				}
				break;
			case IMPORT:
				{
				setState(1030);
				sv_import();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Sv_importContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(PaloAltoParser.IMPORT, 0); }
		public Svi_networkContext svi_network() {
			return getRuleContext(Svi_networkContext.class,0);
		}
		public Svi_visible_vsysContext svi_visible_vsys() {
			return getRuleContext(Svi_visible_vsysContext.class,0);
		}
		public Sv_importContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sv_import; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSv_import(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSv_import(this);
		}
	}

	public final Sv_importContext sv_import() throws RecognitionException {
		Sv_importContext _localctx = new Sv_importContext(_ctx, getState());
		enterRule(_localctx, 314, RULE_sv_import);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1033);
			match(IMPORT);
			setState(1036);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NETWORK:
				{
				setState(1034);
				svi_network();
				}
				break;
			case VISIBLE_VSYS:
				{
				setState(1035);
				svi_visible_vsys();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Svi_networkContext extends ParserRuleContext {
		public TerminalNode NETWORK() { return getToken(PaloAltoParser.NETWORK, 0); }
		public Svin_interfaceContext svin_interface() {
			return getRuleContext(Svin_interfaceContext.class,0);
		}
		public Svi_networkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_svi_network; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSvi_network(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSvi_network(this);
		}
	}

	public final Svi_networkContext svi_network() throws RecognitionException {
		Svi_networkContext _localctx = new Svi_networkContext(_ctx, getState());
		enterRule(_localctx, 316, RULE_svi_network);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1038);
			match(NETWORK);
			setState(1040);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INTERFACE) {
				{
				setState(1039);
				svin_interface();
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

	public static class Svi_visible_vsysContext extends ParserRuleContext {
		public TerminalNode VISIBLE_VSYS() { return getToken(PaloAltoParser.VISIBLE_VSYS, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Svi_visible_vsysContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_svi_visible_vsys; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSvi_visible_vsys(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSvi_visible_vsys(this);
		}
	}

	public final Svi_visible_vsysContext svi_visible_vsys() throws RecognitionException {
		Svi_visible_vsysContext _localctx = new Svi_visible_vsysContext(_ctx, getState());
		enterRule(_localctx, 318, RULE_svi_visible_vsys);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1042);
			match(VISIBLE_VSYS);
			setState(1044);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(1043);
				variable_list();
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

	public static class Svin_interfaceContext extends ParserRuleContext {
		public TerminalNode INTERFACE() { return getToken(PaloAltoParser.INTERFACE, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Svin_interfaceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_svin_interface; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSvin_interface(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSvin_interface(this);
		}
	}

	public final Svin_interfaceContext svin_interface() throws RecognitionException {
		Svin_interfaceContext _localctx = new Svin_interfaceContext(_ctx, getState());
		enterRule(_localctx, 320, RULE_svin_interface);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1046);
			match(INTERFACE);
			setState(1048);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(1047);
				variable_list();
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

	public static class S_zoneContext extends ParserRuleContext {
		public TerminalNode ZONE() { return getToken(PaloAltoParser.ZONE, 0); }
		public S_zone_definitionContext s_zone_definition() {
			return getRuleContext(S_zone_definitionContext.class,0);
		}
		public S_zoneContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_zone; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_zone(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_zone(this);
		}
	}

	public final S_zoneContext s_zone() throws RecognitionException {
		S_zoneContext _localctx = new S_zoneContext(_ctx, getState());
		enterRule(_localctx, 322, RULE_s_zone);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1050);
			match(ZONE);
			setState(1052);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << CLOSE_BRACKET) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(1051);
				s_zone_definition();
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

	public static class S_zone_definitionContext extends ParserRuleContext {
		public VariableContext name;
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public Sz_networkContext sz_network() {
			return getRuleContext(Sz_networkContext.class,0);
		}
		public S_zone_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_s_zone_definition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterS_zone_definition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitS_zone_definition(this);
		}
	}

	public final S_zone_definitionContext s_zone_definition() throws RecognitionException {
		S_zone_definitionContext _localctx = new S_zone_definitionContext(_ctx, getState());
		enterRule(_localctx, 324, RULE_s_zone_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1054);
			((S_zone_definitionContext)_localctx).name = variable();
			setState(1056);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NETWORK) {
				{
				setState(1055);
				sz_network();
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

	public static class Sz_networkContext extends ParserRuleContext {
		public TerminalNode NETWORK() { return getToken(PaloAltoParser.NETWORK, 0); }
		public Szn_externalContext szn_external() {
			return getRuleContext(Szn_externalContext.class,0);
		}
		public Szn_layer2Context szn_layer2() {
			return getRuleContext(Szn_layer2Context.class,0);
		}
		public Szn_layer3Context szn_layer3() {
			return getRuleContext(Szn_layer3Context.class,0);
		}
		public Szn_tapContext szn_tap() {
			return getRuleContext(Szn_tapContext.class,0);
		}
		public Szn_virtual_wireContext szn_virtual_wire() {
			return getRuleContext(Szn_virtual_wireContext.class,0);
		}
		public Sz_networkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sz_network; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSz_network(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSz_network(this);
		}
	}

	public final Sz_networkContext sz_network() throws RecognitionException {
		Sz_networkContext _localctx = new Sz_networkContext(_ctx, getState());
		enterRule(_localctx, 326, RULE_sz_network);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1058);
			match(NETWORK);
			setState(1064);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXTERNAL:
				{
				setState(1059);
				szn_external();
				}
				break;
			case LAYER2:
				{
				setState(1060);
				szn_layer2();
				}
				break;
			case LAYER3:
				{
				setState(1061);
				szn_layer3();
				}
				break;
			case TAP:
				{
				setState(1062);
				szn_tap();
				}
				break;
			case VIRTUAL_WIRE:
				{
				setState(1063);
				szn_virtual_wire();
				}
				break;
			case NEWLINE:
				break;
			default:
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

	public static class Szn_externalContext extends ParserRuleContext {
		public TerminalNode EXTERNAL() { return getToken(PaloAltoParser.EXTERNAL, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Szn_externalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_szn_external; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSzn_external(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSzn_external(this);
		}
	}

	public final Szn_externalContext szn_external() throws RecognitionException {
		Szn_externalContext _localctx = new Szn_externalContext(_ctx, getState());
		enterRule(_localctx, 328, RULE_szn_external);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1066);
			match(EXTERNAL);
			setState(1068);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(1067);
				variable_list();
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

	public static class Szn_layer2Context extends ParserRuleContext {
		public TerminalNode LAYER2() { return getToken(PaloAltoParser.LAYER2, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Szn_layer2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_szn_layer2; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSzn_layer2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSzn_layer2(this);
		}
	}

	public final Szn_layer2Context szn_layer2() throws RecognitionException {
		Szn_layer2Context _localctx = new Szn_layer2Context(_ctx, getState());
		enterRule(_localctx, 330, RULE_szn_layer2);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1070);
			match(LAYER2);
			setState(1072);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(1071);
				variable_list();
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

	public static class Szn_layer3Context extends ParserRuleContext {
		public TerminalNode LAYER3() { return getToken(PaloAltoParser.LAYER3, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Szn_layer3Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_szn_layer3; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSzn_layer3(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSzn_layer3(this);
		}
	}

	public final Szn_layer3Context szn_layer3() throws RecognitionException {
		Szn_layer3Context _localctx = new Szn_layer3Context(_ctx, getState());
		enterRule(_localctx, 332, RULE_szn_layer3);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1074);
			match(LAYER3);
			setState(1076);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(1075);
				variable_list();
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

	public static class Szn_tapContext extends ParserRuleContext {
		public TerminalNode TAP() { return getToken(PaloAltoParser.TAP, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Szn_tapContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_szn_tap; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSzn_tap(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSzn_tap(this);
		}
	}

	public final Szn_tapContext szn_tap() throws RecognitionException {
		Szn_tapContext _localctx = new Szn_tapContext(_ctx, getState());
		enterRule(_localctx, 334, RULE_szn_tap);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1078);
			match(TAP);
			setState(1080);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(1079);
				variable_list();
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

	public static class Szn_virtual_wireContext extends ParserRuleContext {
		public TerminalNode VIRTUAL_WIRE() { return getToken(PaloAltoParser.VIRTUAL_WIRE, 0); }
		public Variable_listContext variable_list() {
			return getRuleContext(Variable_listContext.class,0);
		}
		public Szn_virtual_wireContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_szn_virtual_wire; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).enterSzn_virtual_wire(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PaloAltoParserListener ) ((PaloAltoParserListener)listener).exitSzn_virtual_wire(this);
		}
	}

	public final Szn_virtual_wireContext szn_virtual_wire() throws RecognitionException {
		Szn_virtual_wireContext _localctx = new Szn_virtual_wireContext(_ctx, getState());
		enterRule(_localctx, 336, RULE_szn_virtual_wire);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1082);
			match(VIRTUAL_WIRE);
			setState(1084);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ACTION) | (1L << ADDRESS) | (1L << ADDRESS_GROUP) | (1L << ADMIN_DIST) | (1L << AES_128_CBC) | (1L << AES_128_GCM) | (1L << AES_192_CBC) | (1L << AES_256_CBC) | (1L << AES_256_GCM) | (1L << ALLOW) | (1L << ANY) | (1L << APPLICATION) | (1L << APPLICATION_GROUP) | (1L << AUTHENTICATION) | (1L << AUTHENTICATION_TYPE) | (1L << AUTO) | (1L << BGP) | (1L << BOTNET) | (1L << CATEGORY) | (1L << COMMENT) | (1L << CONFIG) | (1L << CRYPTO_PROFILES) | (1L << DAMPENING_PROFILE) | (1L << DAYS) | (1L << DEFAULT_GATEWAY) | (1L << DENY) | (1L << DES) | (1L << DESCRIPTION) | (1L << DESTINATION) | (1L << DEVICES) | (1L << DEVICECONFIG) | (1L << DH_GROUP) | (1L << DISABLED) | (1L << DISPLAY_NAME) | (1L << DNS) | (1L << DNS_SETTING) | (1L << DOWN) | (1L << DROP) | (1L << DYNAMIC) | (1L << ENABLE) | (1L << ENCRYPTION) | (1L << ESP) | (1L << EXTERNAL) | (1L << ETHERNET) | (1L << FQDN) | (1L << FROM) | (1L << GATEWAY) | (1L << GLOBAL_PROTECT_APP_CRYPTO_PROFILES) | (1L << GROUP1) | (1L << GROUP2) | (1L << GROUP5) | (1L << GROUP14) | (1L << GROUP19) | (1L << GROUP20) | (1L << HASH) | (1L << HIP_PROFILES) | (1L << HOSTNAME) | (1L << HOURS) | (1L << ICMP) | (1L << IKE) | (1L << IKE_CRYPTO_PROFILES) | (1L << IMPORT))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (INTERFACE - 64)) | (1L << (IP - 64)) | (1L << (IP_ADDRESS_LITERAL - 64)) | (1L << (IP_NETMASK - 64)) | (1L << (IP_RANGE_LITERAL - 64)) | (1L << (IPSEC_CRYPTO_PROFILES - 64)) | (1L << (IPV6 - 64)) | (1L << (LAYER2 - 64)) | (1L << (LAYER3 - 64)) | (1L << (LIFETIME - 64)) | (1L << (LINK_STATE - 64)) | (1L << (LLDP - 64)) | (1L << (LOG_SETTINGS - 64)) | (1L << (LOOPBACK - 64)) | (1L << (MD5 - 64)) | (1L << (MINUTES - 64)) | (1L << (MEMBERS - 64)) | (1L << (METRIC - 64)) | (1L << (MGT_CONFIG - 64)) | (1L << (MTU - 64)) | (1L << (NDP_PROXY - 64)) | (1L << (NEGATE_DESTINATION - 64)) | (1L << (NEGATE_SOURCE - 64)) | (1L << (NETMASK - 64)) | (1L << (NETWORK - 64)) | (1L << (NEXT_VR - 64)) | (1L << (NEXTHOP - 64)) | (1L << (NO - 64)) | (1L << (NONE - 64)) | (1L << (NTP_SERVER_ADDRESS - 64)) | (1L << (NTP_SERVERS - 64)) | (1L << (NULL - 64)) | (1L << (OPEN_BRACKET - 64)) | (1L << (PANORAMA - 64)) | (1L << (PANORAMA_SERVER - 64)) | (1L << (POLICY - 64)) | (1L << (PORT - 64)) | (1L << (POST_RULEBASE - 64)) | (1L << (PRE_RULEBASE - 64)) | (1L << (PRIMARY - 64)) | (1L << (PRIMARY_NTP_SERVER - 64)) | (1L << (PROFILES - 64)) | (1L << (PROTOCOL - 64)) | (1L << (QOS - 64)) | (1L << (RESET_BOTH - 64)) | (1L << (RESET_CLIENT - 64)) | (1L << (RESET_SERVER - 64)) | (1L << (ROUTING_TABLE - 64)) | (1L << (RULEBASE - 64)) | (1L << (RULES - 64)) | (1L << (SCTP - 64)) | (1L << (SECONDARY - 64)) | (1L << (SECONDARY_NTP_SERVER - 64)) | (1L << (SECONDS - 64)) | (1L << (SECURITY - 64)) | (1L << (SERVER - 64)) | (1L << (SERVERS - 64)) | (1L << (SERVICE - 64)) | (1L << (SERVICE_GROUP - 64)) | (1L << (SET - 64)) | (1L << (SETTING - 64)) | (1L << (SHA1 - 64)) | (1L << (SHA256 - 64)) | (1L << (SHA384 - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (SHA512 - 128)) | (1L << (SHARED - 128)) | (1L << (SHARED_GATEWAY - 128)) | (1L << (SOURCE - 128)) | (1L << (SOURCE_PORT - 128)) | (1L << (SOURCE_USER - 128)) | (1L << (STATIC - 128)) | (1L << (STATIC_ROUTE - 128)) | (1L << (SYSLOG - 128)) | (1L << (SYSTEM - 128)) | (1L << (TAG - 128)) | (1L << (TAP - 128)) | (1L << (TCP - 128)) | (1L << (THREE_DES - 128)) | (1L << (TIMEZONE - 128)) | (1L << (TO - 128)) | (1L << (TUNNEL - 128)) | (1L << (TYPE - 128)) | (1L << (UDP - 128)) | (1L << (UNITS - 128)) | (1L << (UP - 128)) | (1L << (UPDATE_SCHEDULE - 128)) | (1L << (UPDATE_SERVER - 128)) | (1L << (VIRTUAL_ROUTER - 128)) | (1L << (VIRTUAL_WIRE - 128)) | (1L << (VISIBLE_VSYS - 128)) | (1L << (VLAN - 128)) | (1L << (VSYS - 128)) | (1L << (YES - 128)) | (1L << (ZONE - 128)) | (1L << (COMMA - 128)) | (1L << (DASH - 128)) | (1L << (DEC - 128)) | (1L << (DOUBLE_QUOTED_STRING - 128)) | (1L << (IP_ADDRESS - 128)) | (1L << (IP_PREFIX - 128)) | (1L << (IP_RANGE - 128)) | (1L << (LINE_COMMENT - 128)) | (1L << (RANGE - 128)) | (1L << (SINGLE_QUOTED_STRING - 128)) | (1L << (VARIABLE - 128)) | (1L << (WS - 128)))) != 0)) {
				{
				setState(1083);
				variable_list();
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u00ac\u0441\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
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
		"w\tw\4x\tx\4y\ty\4z\tz\4{\t{\4|\t|\4}\t}\4~\t~\4\177\t\177\4\u0080\t\u0080"+
		"\4\u0081\t\u0081\4\u0082\t\u0082\4\u0083\t\u0083\4\u0084\t\u0084\4\u0085"+
		"\t\u0085\4\u0086\t\u0086\4\u0087\t\u0087\4\u0088\t\u0088\4\u0089\t\u0089"+
		"\4\u008a\t\u008a\4\u008b\t\u008b\4\u008c\t\u008c\4\u008d\t\u008d\4\u008e"+
		"\t\u008e\4\u008f\t\u008f\4\u0090\t\u0090\4\u0091\t\u0091\4\u0092\t\u0092"+
		"\4\u0093\t\u0093\4\u0094\t\u0094\4\u0095\t\u0095\4\u0096\t\u0096\4\u0097"+
		"\t\u0097\4\u0098\t\u0098\4\u0099\t\u0099\4\u009a\t\u009a\4\u009b\t\u009b"+
		"\4\u009c\t\u009c\4\u009d\t\u009d\4\u009e\t\u009e\4\u009f\t\u009f\4\u00a0"+
		"\t\u00a0\4\u00a1\t\u00a1\4\u00a2\t\u00a2\4\u00a3\t\u00a3\4\u00a4\t\u00a4"+
		"\4\u00a5\t\u00a5\4\u00a6\t\u00a6\4\u00a7\t\u00a7\4\u00a8\t\u00a8\4\u00a9"+
		"\t\u00a9\4\u00aa\t\u00aa\3\2\3\2\6\2\u0157\n\2\r\2\16\2\u0158\3\2\3\2"+
		"\3\3\3\3\3\4\3\4\3\4\3\5\3\5\3\5\5\5\u0165\n\5\3\5\3\5\3\6\5\6\u016a\n"+
		"\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7\u0179\n\7\3"+
		"\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\n\5\n\u0184\n\n\3\13\3\13\3\13\5\13\u0189"+
		"\n\13\3\f\3\f\3\f\3\f\5\f\u018f\n\f\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3"+
		"\17\3\20\3\20\5\20\u019b\n\20\3\21\3\21\3\21\3\21\3\21\3\21\5\21\u01a3"+
		"\n\21\3\22\3\22\3\22\3\23\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\25\3\26"+
		"\3\26\3\26\3\27\7\27\u01b5\n\27\f\27\16\27\u01b8\13\27\3\30\3\30\3\30"+
		"\7\30\u01bd\n\30\f\30\16\30\u01c0\13\30\3\30\5\30\u01c3\n\30\3\31\3\31"+
		"\3\31\3\31\3\31\5\31\u01ca\n\31\3\32\3\32\5\32\u01ce\n\32\3\33\3\33\3"+
		"\33\7\33\u01d3\n\33\f\33\16\33\u01d6\13\33\3\34\3\34\3\34\7\34\u01db\n"+
		"\34\f\34\16\34\u01de\13\34\3\34\5\34\u01e1\n\34\3\35\3\35\3\36\3\36\3"+
		"\37\3\37\5\37\u01e9\n\37\3 \3 \3 \3 \3 \5 \u01f0\n \3!\3!\3!\3\"\3\"\3"+
		"\"\3#\3#\3#\3$\3$\3$\3$\7$\u01ff\n$\f$\16$\u0202\13$\3$\5$\u0205\n$\3"+
		"%\3%\5%\u0209\n%\3&\3&\5&\u020d\n&\3\'\3\'\3\'\3(\3(\5(\u0214\n(\3)\3"+
		")\5)\u0218\n)\3*\3*\5*\u021c\n*\3+\3+\3+\5+\u0221\n+\3,\3,\3,\3-\3-\3"+
		"-\3-\3-\3-\3-\3-\5-\u022e\n-\3.\3.\3.\3/\3/\3/\3\60\3\60\3\60\3\61\3\61"+
		"\3\61\3\62\3\62\3\62\3\63\3\63\3\63\3\63\3\64\3\64\3\64\3\65\3\65\3\65"+
		"\3\65\3\65\5\65\u024b\n\65\3\66\3\66\3\66\3\67\3\67\3\67\3\67\3\67\5\67"+
		"\u0255\n\67\38\38\39\39\39\3:\3:\3:\3;\3;\5;\u0261\n;\3<\3<\3<\3<\3<\3"+
		"<\3<\5<\u026a\n<\3=\3=\3=\5=\u026f\n=\3>\3>\3>\5>\u0274\n>\3?\3?\3?\5"+
		"?\u0279\n?\3@\3@\5@\u027d\n@\3A\3A\3A\5A\u0282\nA\3B\3B\3B\3C\3C\3D\3"+
		"D\3E\3E\3E\5E\u028e\nE\3F\3F\5F\u0292\nF\3G\3G\3G\5G\u0297\nG\3H\3H\3"+
		"H\3I\3I\3I\3J\3J\3J\3K\3K\3K\3K\5K\u02a6\nK\3L\3L\5L\u02aa\nL\3M\3M\5"+
		"M\u02ae\nM\3N\3N\5N\u02b2\nN\3O\3O\5O\u02b6\nO\3P\3P\5P\u02ba\nP\3Q\3"+
		"Q\5Q\u02be\nQ\3R\3R\5R\u02c2\nR\3S\3S\3S\3T\3T\3T\3U\3U\5U\u02cc\nU\3"+
		"U\6U\u02cf\nU\rU\16U\u02d0\3U\5U\u02d4\nU\3V\3V\3W\3W\3W\3X\3X\3X\3X\3"+
		"Y\3Y\3Y\3Y\3Y\3Y\3Y\5Y\u02e6\nY\3Z\3Z\3Z\5Z\u02eb\nZ\3[\3[\3[\3[\5[\u02f1"+
		"\n[\3\\\3\\\3\\\3]\3]\3]\3^\3^\3^\3_\3_\5_\u02fe\n_\3`\3`\3`\3`\5`\u0304"+
		"\n`\3a\3a\3a\3b\3b\5b\u030b\nb\3c\3c\3c\3c\3d\3d\3d\3e\3e\5e\u0316\ne"+
		"\3f\3f\5f\u031a\nf\3g\3g\3g\3h\3h\5h\u0321\nh\3i\3i\3i\3i\5i\u0327\ni"+
		"\3j\3j\3j\3j\5j\u032d\nj\3k\3k\3k\3k\3k\3k\5k\u0335\nk\3l\3l\3l\3l\3l"+
		"\5l\u033c\nl\3l\3l\5l\u0340\nl\3m\3m\3m\3n\3n\3n\3o\3o\3o\3o\3o\3o\3o"+
		"\3o\3o\5o\u0351\no\3p\3p\3p\5p\u0356\np\3q\3q\3q\3r\3r\3r\3s\3s\3s\3t"+
		"\3t\3t\3u\3u\3u\3v\3v\3v\3w\3w\3w\5w\u036d\nw\3x\3x\3x\3y\3y\3y\3z\3z"+
		"\3z\3{\3{\3|\3|\5|\u037c\n|\3}\3}\5}\u0380\n}\3~\3~\3~\3~\3~\3~\3~\3~"+
		"\3~\3~\3~\3~\3~\3~\3~\5~\u0391\n~\3\177\3\177\3\177\3\u0080\3\u0080\3"+
		"\u0080\3\u0081\3\u0081\3\u0081\3\u0082\3\u0082\3\u0082\3\u0083\3\u0083"+
		"\3\u0083\3\u0084\3\u0084\3\u0084\3\u0085\3\u0085\3\u0085\3\u0086\3\u0086"+
		"\3\u0086\3\u0087\3\u0087\3\u0087\3\u0088\3\u0088\3\u0088\3\u0089\3\u0089"+
		"\3\u0089\3\u008a\3\u008a\3\u008a\3\u008b\3\u008b\3\u008b\3\u008c\3\u008c"+
		"\3\u008c\3\u008d\3\u008d\5\u008d\u03bf\n\u008d\3\u008e\3\u008e\3\u008e"+
		"\3\u008e\3\u008e\7\u008e\u03c6\n\u008e\f\u008e\16\u008e\u03c9\13\u008e"+
		"\3\u008f\3\u008f\3\u008f\3\u0090\3\u0090\3\u0090\3\u0091\3\u0091\3\u0091"+
		"\3\u0092\3\u0092\3\u0092\3\u0093\3\u0093\5\u0093\u03d9\n\u0093\3\u0094"+
		"\3\u0094\5\u0094\u03dd\n\u0094\3\u0095\3\u0095\3\u0095\3\u0096\3\u0096"+
		"\3\u0096\5\u0096\u03e5\n\u0096\3\u0097\3\u0097\3\u0097\3\u0097\3\u0097"+
		"\3\u0097\3\u0097\5\u0097\u03ee\n\u0097\3\u0098\3\u0098\3\u0098\3\u0099"+
		"\3\u0099\3\u0099\3\u009a\3\u009a\3\u009a\3\u009a\3\u009b\3\u009b\3\u009b"+
		"\3\u009b\3\u009c\3\u009c\3\u009c\3\u009d\3\u009d\5\u009d\u0403\n\u009d"+
		"\3\u009e\3\u009e\3\u009e\3\u009e\3\u009e\5\u009e\u040a\n\u009e\3\u009f"+
		"\3\u009f\3\u009f\5\u009f\u040f\n\u009f\3\u00a0\3\u00a0\5\u00a0\u0413\n"+
		"\u00a0\3\u00a1\3\u00a1\5\u00a1\u0417\n\u00a1\3\u00a2\3\u00a2\5\u00a2\u041b"+
		"\n\u00a2\3\u00a3\3\u00a3\5\u00a3\u041f\n\u00a3\3\u00a4\3\u00a4\5\u00a4"+
		"\u0423\n\u00a4\3\u00a5\3\u00a5\3\u00a5\3\u00a5\3\u00a5\3\u00a5\5\u00a5"+
		"\u042b\n\u00a5\3\u00a6\3\u00a6\5\u00a6\u042f\n\u00a6\3\u00a7\3\u00a7\5"+
		"\u00a7\u0433\n\u00a7\3\u00a8\3\u00a8\5\u00a8\u0437\n\u00a8\3\u00a9\3\u00a9"+
		"\5\u00a9\u043b\n\u00a9\3\u00aa\3\u00aa\5\u00aa\u043f\n\u00aa\3\u00aa\2"+
		"\2\u00ab\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668"+
		":<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a"+
		"\u008c\u008e\u0090\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2"+
		"\u00a4\u00a6\u00a8\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba"+
		"\u00bc\u00be\u00c0\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2"+
		"\u00d4\u00d6\u00d8\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea"+
		"\u00ec\u00ee\u00f0\u00f2\u00f4\u00f6\u00f8\u00fa\u00fc\u00fe\u0100\u0102"+
		"\u0104\u0106\u0108\u010a\u010c\u010e\u0110\u0112\u0114\u0116\u0118\u011a"+
		"\u011c\u011e\u0120\u0122\u0124\u0126\u0128\u012a\u012c\u012e\u0130\u0132"+
		"\u0134\u0136\u0138\u013a\u013c\u013e\u0140\u0142\u0144\u0146\u0148\u014a"+
		"\u014c\u014e\u0150\u0152\2\22\4\2TT\u008c\u008c\3\2\u00a4\u00a5\3\2\u00a8"+
		"\u00a8\4\2\26\26\u00a8\u00a8\4\2jjvv\7\2dd{{\u0090\u0090\u0093\u0093\u0097"+
		"\u0098\5\2\22\22((\u0096\u0096\5\2HHMMVV\5\2PP^^\177\u0082\3\2\649\6\2"+
		"\7\13\36\36aa\u008f\u008f\4\2PP\177\u0082\6\2\33\33==QQww\6\2\f\f\35\35"+
		"))np\4\2]]\u009e\u009e\5\2tt\u008e\u008e\u0094\u0094\2\u0446\2\u0156\3"+
		"\2\2\2\4\u015c\3\2\2\2\6\u015e\3\2\2\2\b\u0164\3\2\2\2\n\u0169\3\2\2\2"+
		"\f\u0178\3\2\2\2\16\u017a\3\2\2\2\20\u017c\3\2\2\2\22\u0183\3\2\2\2\24"+
		"\u0185\3\2\2\2\26\u018a\3\2\2\2\30\u0190\3\2\2\2\32\u0192\3\2\2\2\34\u0195"+
		"\3\2\2\2\36\u0198\3\2\2\2 \u019c\3\2\2\2\"\u01a4\3\2\2\2$\u01a7\3\2\2"+
		"\2&\u01aa\3\2\2\2(\u01ad\3\2\2\2*\u01b0\3\2\2\2,\u01b6\3\2\2\2.\u01c2"+
		"\3\2\2\2\60\u01c9\3\2\2\2\62\u01cd\3\2\2\2\64\u01cf\3\2\2\2\66\u01e0\3"+
		"\2\2\28\u01e2\3\2\2\2:\u01e4\3\2\2\2<\u01e6\3\2\2\2>\u01ea\3\2\2\2@\u01f1"+
		"\3\2\2\2B\u01f4\3\2\2\2D\u01f7\3\2\2\2F\u01fa\3\2\2\2H\u0206\3\2\2\2J"+
		"\u020a\3\2\2\2L\u020e\3\2\2\2N\u0211\3\2\2\2P\u0215\3\2\2\2R\u0219\3\2"+
		"\2\2T\u021d\3\2\2\2V\u0222\3\2\2\2X\u0225\3\2\2\2Z\u022f\3\2\2\2\\\u0232"+
		"\3\2\2\2^\u0235\3\2\2\2`\u0238\3\2\2\2b\u023b\3\2\2\2d\u023e\3\2\2\2f"+
		"\u0242\3\2\2\2h\u0245\3\2\2\2j\u024c\3\2\2\2l\u024f\3\2\2\2n\u0256\3\2"+
		"\2\2p\u0258\3\2\2\2r\u025b\3\2\2\2t\u025e\3\2\2\2v\u0262\3\2\2\2x\u026b"+
		"\3\2\2\2z\u0270\3\2\2\2|\u0275\3\2\2\2~\u027a\3\2\2\2\u0080\u027e\3\2"+
		"\2\2\u0082\u0283\3\2\2\2\u0084\u0286\3\2\2\2\u0086\u0288\3\2\2\2\u0088"+
		"\u028a\3\2\2\2\u008a\u028f\3\2\2\2\u008c\u0296\3\2\2\2\u008e\u0298\3\2"+
		"\2\2\u0090\u029b\3\2\2\2\u0092\u029e\3\2\2\2\u0094\u02a1\3\2\2\2\u0096"+
		"\u02a7\3\2\2\2\u0098\u02ab\3\2\2\2\u009a\u02af\3\2\2\2\u009c\u02b3\3\2"+
		"\2\2\u009e\u02b7\3\2\2\2\u00a0\u02bb\3\2\2\2\u00a2\u02bf\3\2\2\2\u00a4"+
		"\u02c3\3\2\2\2\u00a6\u02c6\3\2\2\2\u00a8\u02c9\3\2\2\2\u00aa\u02d5\3\2"+
		"\2\2\u00ac\u02d7\3\2\2\2\u00ae\u02da\3\2\2\2\u00b0\u02de\3\2\2\2\u00b2"+
		"\u02e7\3\2\2\2\u00b4\u02ec\3\2\2\2\u00b6\u02f2\3\2\2\2\u00b8\u02f5\3\2"+
		"\2\2\u00ba\u02f8\3\2\2\2\u00bc\u02fb\3\2\2\2\u00be\u02ff\3\2\2\2\u00c0"+
		"\u0305\3\2\2\2\u00c2\u0308\3\2\2\2\u00c4\u030c\3\2\2\2\u00c6\u0310\3\2"+
		"\2\2\u00c8\u0313\3\2\2\2\u00ca\u0317\3\2\2\2\u00cc\u031b\3\2\2\2\u00ce"+
		"\u031e\3\2\2\2\u00d0\u0322\3\2\2\2\u00d2\u0328\3\2\2\2\u00d4\u032e\3\2"+
		"\2\2\u00d6\u0336\3\2\2\2\u00d8\u0341\3\2\2\2\u00da\u0344\3\2\2\2\u00dc"+
		"\u0347\3\2\2\2\u00de\u0352\3\2\2\2\u00e0\u0357\3\2\2\2\u00e2\u035a\3\2"+
		"\2\2\u00e4\u035d\3\2\2\2\u00e6\u0360\3\2\2\2\u00e8\u0363\3\2\2\2\u00ea"+
		"\u0366\3\2\2\2\u00ec\u0369\3\2\2\2\u00ee\u036e\3\2\2\2\u00f0\u0371\3\2"+
		"\2\2\u00f2\u0374\3\2\2\2\u00f4\u0377\3\2\2\2\u00f6\u0379\3\2\2\2\u00f8"+
		"\u037d\3\2\2\2\u00fa\u0381\3\2\2\2\u00fc\u0392\3\2\2\2\u00fe\u0395\3\2"+
		"\2\2\u0100\u0398\3\2\2\2\u0102\u039b\3\2\2\2\u0104\u039e\3\2\2\2\u0106"+
		"\u03a1\3\2\2\2\u0108\u03a4\3\2\2\2\u010a\u03a7\3\2\2\2\u010c\u03aa\3\2"+
		"\2\2\u010e\u03ad\3\2\2\2\u0110\u03b0\3\2\2\2\u0112\u03b3\3\2\2\2\u0114"+
		"\u03b6\3\2\2\2\u0116\u03b9\3\2\2\2\u0118\u03bc\3\2\2\2\u011a\u03c0\3\2"+
		"\2\2\u011c\u03ca\3\2\2\2\u011e\u03cd\3\2\2\2\u0120\u03d0\3\2\2\2\u0122"+
		"\u03d3\3\2\2\2\u0124\u03d6\3\2\2\2\u0126\u03da\3\2\2\2\u0128\u03de\3\2"+
		"\2\2\u012a\u03e1\3\2\2\2\u012c\u03ed\3\2\2\2\u012e\u03ef\3\2\2\2\u0130"+
		"\u03f2\3\2\2\2\u0132\u03f5\3\2\2\2\u0134\u03f9\3\2\2\2\u0136\u03fd\3\2"+
		"\2\2\u0138\u0400\3\2\2\2\u013a\u0404\3\2\2\2\u013c\u040b\3\2\2\2\u013e"+
		"\u0410\3\2\2\2\u0140\u0414\3\2\2\2\u0142\u0418\3\2\2\2\u0144\u041c\3\2"+
		"\2\2\u0146\u0420\3\2\2\2\u0148\u0424\3\2\2\2\u014a\u042c\3\2\2\2\u014c"+
		"\u0430\3\2\2\2\u014e\u0434\3\2\2\2\u0150\u0438\3\2\2\2\u0152\u043c\3\2"+
		"\2\2\u0154\u0157\5\20\t\2\u0155\u0157\5\4\3\2\u0156\u0154\3\2\2\2\u0156"+
		"\u0155\3\2\2\2\u0157\u0158\3\2\2\2\u0158\u0156\3\2\2\2\u0158\u0159\3\2"+
		"\2\2\u0159\u015a\3\2\2\2\u015a\u015b\7\2\2\3\u015b\3\3\2\2\2\u015c\u015d"+
		"\7\u00a8\2\2\u015d\5\3\2\2\2\u015e\u015f\t\2\2\2\u015f\u0160\5,\27\2\u0160"+
		"\7\3\2\2\2\u0161\u0162\7\30\2\2\u0162\u0163\7!\2\2\u0163\u0165\5:\36\2"+
		"\u0164\u0161\3\2\2\2\u0164\u0165\3\2\2\2\u0165\u0166\3\2\2\2\u0166\u0167"+
		"\5\f\7\2\u0167\t\3\2\2\2\u0168\u016a\7\30\2\2\u0169\u0168\3\2\2\2\u0169"+
		"\u016a\3\2\2\2\u016a\u016b\3\2\2\2\u016b\u016c\5\16\b\2\u016c\13\3\2\2"+
		"\2\u016d\u0179\5\36\20\2\u016e\u0179\5<\37\2\u016f\u0179\5H%\2\u0170\u0179"+
		"\5T+\2\u0171\u0179\5\u00b0Y\2\u0172\u0179\5\6\4\2\u0173\u0179\5\u00f2"+
		"z\2\u0174\u0179\5\u0118\u008d\2\u0175\u0179\5\u0124\u0093\2\u0176\u0179"+
		"\5\u0138\u009d\2\u0177\u0179\5\u0144\u00a3\2\u0178\u016d\3\2\2\2\u0178"+
		"\u016e\3\2\2\2\u0178\u016f\3\2\2\2\u0178\u0170\3\2\2\2\u0178\u0171\3\2"+
		"\2\2\u0178\u0172\3\2\2\2\u0178\u0173\3\2\2\2\u0178\u0174\3\2\2\2\u0178"+
		"\u0175\3\2\2\2\u0178\u0176\3\2\2\2\u0178\u0177\3\2\2\2\u0179\r\3\2\2\2"+
		"\u017a\u017b\5\u012a\u0096\2\u017b\17\3\2\2\2\u017c\u017d\7}\2\2\u017d"+
		"\u017e\5\22\n\2\u017e\u017f\7\u00a8\2\2\u017f\21\3\2\2\2\u0180\u0184\5"+
		"\b\5\2\u0181\u0184\5\n\6\2\u0182\u0184\5\24\13\2\u0183\u0180\3\2\2\2\u0183"+
		"\u0181\3\2\2\2\u0183\u0182\3\2\2\2\u0184\23\3\2\2\2\u0185\u0188\7e\2\2"+
		"\u0186\u0189\5\26\f\2\u0187\u0189\5\30\r\2\u0188\u0186\3\2\2\2\u0188\u0187"+
		"\3\2\2\2\u0189\25\3\2\2\2\u018a\u018e\7c\2\2\u018b\u018f\5\u012c\u0097"+
		"\2\u018c\u018f\5\32\16\2\u018d\u018f\5\34\17\2\u018e\u018b\3\2\2\2\u018e"+
		"\u018c\3\2\2\2\u018e\u018d\3\2\2\2\u018f\27\3\2\2\2\u0190\u0191\7\u0083"+
		"\2\2\u0191\31\3\2\2\2\u0192\u0193\7g\2\2\u0193\u0194\5\u00f4{\2\u0194"+
		"\33\3\2\2\2\u0195\u0196\7h\2\2\u0196\u0197\5\u00f4{\2\u0197\35\3\2\2\2"+
		"\u0198\u019a\7\4\2\2\u0199\u019b\5 \21\2\u019a\u0199\3\2\2\2\u019a\u019b"+
		"\3\2\2\2\u019b\37\3\2\2\2\u019c\u01a2\5:\36\2\u019d\u01a3\5\"\22\2\u019e"+
		"\u01a3\5$\23\2\u019f\u01a3\5&\24\2\u01a0\u01a3\5(\25\2\u01a1\u01a3\5*"+
		"\26\2\u01a2\u019d\3\2\2\2\u01a2\u019e\3\2\2\2\u01a2\u019f\3\2\2\2\u01a2"+
		"\u01a0\3\2\2\2\u01a2\u01a1\3\2\2\2\u01a2\u01a3\3\2\2\2\u01a3!\3\2\2\2"+
		"\u01a4\u01a5\7\37\2\2\u01a5\u01a6\5:\36\2\u01a6#\3\2\2\2\u01a7\u01a8\7"+
		"\60\2\2\u01a8\u01a9\5,\27\2\u01a9%\3\2\2\2\u01aa\u01ab\7E\2\2\u01ab\u01ac"+
		"\t\3\2\2\u01ac\'\3\2\2\2\u01ad\u01ae\7F\2\2\u01ae\u01af\7\u00a6\2\2\u01af"+
		")\3\2\2\2\u01b0\u01b1\7\u008c\2\2\u01b1\u01b2\5,\27\2\u01b2+\3\2\2\2\u01b3"+
		"\u01b5\n\4\2\2\u01b4\u01b3\3\2\2\2\u01b5\u01b8\3\2\2\2\u01b6\u01b4\3\2"+
		"\2\2\u01b6\u01b7\3\2\2\2\u01b7-\3\2\2\2\u01b8\u01b6\3\2\2\2\u01b9\u01c3"+
		"\5\60\31\2\u01ba\u01be\7b\2\2\u01bb\u01bd\5\60\31\2\u01bc\u01bb\3\2\2"+
		"\2\u01bd\u01c0\3\2\2\2\u01be\u01bc\3\2\2\2\u01be\u01bf\3\2\2\2\u01bf\u01c1"+
		"\3\2\2\2\u01c0\u01be\3\2\2\2\u01c1\u01c3\7\26\2\2\u01c2\u01b9\3\2\2\2"+
		"\u01c2\u01ba\3\2\2\2\u01c3/\3\2\2\2\u01c4\u01ca\7\r\2\2\u01c5\u01ca\7"+
		"\u00a4\2\2\u01c6\u01ca\7\u00a5\2\2\u01c7\u01ca\7\u00a6\2\2\u01c8\u01ca"+
		"\5:\36\2\u01c9\u01c4\3\2\2\2\u01c9\u01c5\3\2\2\2\u01c9\u01c6\3\2\2\2\u01c9"+
		"\u01c7\3\2\2\2\u01c9\u01c8\3\2\2\2\u01ca\61\3\2\2\2\u01cb\u01ce\7\u00a9"+
		"\2\2\u01cc\u01ce\7\u00a2\2\2\u01cd\u01cb\3\2\2\2\u01cd\u01cc\3\2\2\2\u01ce"+
		"\63\3\2\2\2\u01cf\u01d4\5\62\32\2\u01d0\u01d1\7\u00a0\2\2\u01d1\u01d3"+
		"\5\62\32\2\u01d2\u01d0\3\2\2\2\u01d3\u01d6\3\2\2\2\u01d4\u01d2\3\2\2\2"+
		"\u01d4\u01d5\3\2\2\2\u01d5\65\3\2\2\2\u01d6\u01d4\3\2\2\2\u01d7\u01e1"+
		"\58\35\2\u01d8\u01dc\7b\2\2\u01d9\u01db\58\35\2\u01da\u01d9\3\2\2\2\u01db"+
		"\u01de\3\2\2\2\u01dc\u01da\3\2\2\2\u01dc\u01dd\3\2\2\2\u01dd\u01df\3\2"+
		"\2\2\u01de\u01dc\3\2\2\2\u01df\u01e1\7\26\2\2\u01e0\u01d7\3\2\2\2\u01e0"+
		"\u01d8\3\2\2\2\u01e1\67\3\2\2\2\u01e2\u01e3\n\5\2\2\u01e39\3\2\2\2\u01e4"+
		"\u01e5\n\4\2\2\u01e5;\3\2\2\2\u01e6\u01e8\7\5\2\2\u01e7\u01e9\5> \2\u01e8"+
		"\u01e7\3\2\2\2\u01e8\u01e9\3\2\2\2\u01e9=\3\2\2\2\u01ea\u01ef\5:\36\2"+
		"\u01eb\u01f0\5@!\2\u01ec\u01f0\5B\"\2\u01ed\u01f0\5D#\2\u01ee\u01f0\5"+
		"F$\2\u01ef\u01eb\3\2\2\2\u01ef\u01ec\3\2\2\2\u01ef\u01ed\3\2\2\2\u01ef"+
		"\u01ee\3\2\2\2\u01ef\u01f0\3\2\2\2\u01f0?\3\2\2\2\u01f1\u01f2\7\37\2\2"+
		"\u01f2\u01f3\5:\36\2\u01f3A\3\2\2\2\u01f4\u01f5\7*\2\2\u01f5\u01f6\5,"+
		"\27\2\u01f6C\3\2\2\2\u01f7\u01f8\7\u008c\2\2\u01f8\u01f9\5,\27\2\u01f9"+
		"E\3\2\2\2\u01fa\u0204\7\u0088\2\2\u01fb\u0205\5:\36\2\u01fc\u0200\7b\2"+
		"\2\u01fd\u01ff\5:\36\2\u01fe\u01fd\3\2\2\2\u01ff\u0202\3\2\2\2\u0200\u01fe"+
		"\3\2\2\2\u0200\u0201\3\2\2\2\u0201\u0203\3\2\2\2\u0202\u0200\3\2\2\2\u0203"+
		"\u0205\7\26\2\2\u0204\u01fb\3\2\2\2\u0204\u01fc\3\2\2\2\u0205G\3\2\2\2"+
		"\u0206\u0208\7\16\2\2\u0207\u0209\5J&\2\u0208\u0207\3\2\2\2\u0208\u0209"+
		"\3\2\2\2\u0209I\3\2\2\2\u020a\u020c\5:\36\2\u020b\u020d\5L\'\2\u020c\u020b"+
		"\3\2\2\2\u020c\u020d\3\2\2\2\u020dK\3\2\2\2\u020e\u020f\7\37\2\2\u020f"+
		"\u0210\5:\36\2\u0210M\3\2\2\2\u0211\u0213\7\17\2\2\u0212\u0214\5P)\2\u0213"+
		"\u0212\3\2\2\2\u0213\u0214\3\2\2\2\u0214O\3\2\2\2\u0215\u0217\5:\36\2"+
		"\u0216\u0218\5R*\2\u0217\u0216\3\2\2\2\u0217\u0218\3\2\2\2\u0218Q\3\2"+
		"\2\2\u0219\u021b\7R\2\2\u021a\u021c\5\66\34\2\u021b\u021a\3\2\2\2\u021b"+
		"\u021c\3\2\2\2\u021cS\3\2\2\2\u021d\u0220\7\"\2\2\u021e\u0221\5V,\2\u021f"+
		"\u0221\5X-\2\u0220\u021e\3\2\2\2\u0220\u021f\3\2\2\2\u0221U\3\2\2\2\u0222"+
		"\u0223\7~\2\2\u0223\u0224\5,\27\2\u0224W\3\2\2\2\u0225\u022d\7\u008b\2"+
		"\2\u0226\u022e\5Z.\2\u0227\u022e\5\\/\2\u0228\u022e\5^\60\2\u0229\u022e"+
		"\5`\61\2\u022a\u022e\5b\62\2\u022b\u022e\5d\63\2\u022c\u022e\5f\64\2\u022d"+
		"\u0226\3\2\2\2\u022d\u0227\3\2\2\2\u022d\u0228\3\2\2\2\u022d\u0229\3\2"+
		"\2\2\u022d\u022a\3\2\2\2\u022d\u022b\3\2\2\2\u022d\u022c\3\2\2\2\u022e"+
		"Y\3\2\2\2\u022f\u0230\7\34\2\2\u0230\u0231\7\u00a4\2\2\u0231[\3\2\2\2"+
		"\u0232\u0233\7\'\2\2\u0233\u0234\5h\65\2\u0234]\3\2\2\2\u0235\u0236\7"+
		"<\2\2\u0236\u0237\5:\36\2\u0237_\3\2\2\2\u0238\u0239\7D\2\2\u0239\u023a"+
		"\7\u00a4\2\2\u023aa\3\2\2\2\u023b\u023c\7Y\2\2\u023c\u023d\7\u00a4\2\2"+
		"\u023dc\3\2\2\2\u023e\u023f\7`\2\2\u023f\u0240\t\6\2\2\u0240\u0241\5j"+
		"\66\2\u0241e\3\2\2\2\u0242\u0243\t\7\2\2\u0243\u0244\5,\27\2\u0244g\3"+
		"\2\2\2\u0245\u024a\7z\2\2\u0246\u0247\7i\2\2\u0247\u024b\7\u00a4\2\2\u0248"+
		"\u0249\7u\2\2\u0249\u024b\7\u00a4\2\2\u024a\u0246\3\2\2\2\u024a\u0248"+
		"\3\2\2\2\u024bi\3\2\2\2\u024c\u024d\7_\2\2\u024d\u024e\5:\36\2\u024ek"+
		"\3\2\2\2\u024f\u0254\7B\2\2\u0250\u0255\5t;\2\u0251\u0255\5x=\2\u0252"+
		"\u0255\5z>\2\u0253\u0255\5|?\2\u0254\u0250\3\2\2\2\u0254\u0251\3\2\2\2"+
		"\u0254\u0252\3\2\2\2\u0254\u0253\3\2\2\2\u0255m\3\2\2\2\u0256\u0257\5"+
		"p9\2\u0257o\3\2\2\2\u0258\u0259\7\27\2\2\u0259\u025a\5:\36\2\u025aq\3"+
		"\2\2\2\u025b\u025c\7\u008c\2\2\u025c\u025d\7\u00a2\2\2\u025ds\3\2\2\2"+
		"\u025e\u0260\7/\2\2\u025f\u0261\5v<\2\u0260\u025f\3\2\2\2\u0260\u0261"+
		"\3\2\2\2\u0261u\3\2\2\2\u0262\u0269\5:\36\2\u0263\u026a\5n8\2\u0264\u026a"+
		"\5~@\2\u0265\u026a\5\u0080A\2\u0266\u026a\5\u0082B\2\u0267\u026a\5\u0084"+
		"C\2\u0268\u026a\5\u0086D\2\u0269\u0263\3\2\2\2\u0269\u0264\3\2\2\2\u0269"+
		"\u0265\3\2\2\2\u0269\u0266\3\2\2\2\u0269\u0267\3\2\2\2\u0269\u0268\3\2"+
		"\2\2\u0269\u026a\3\2\2\2\u026aw\3\2\2\2\u026b\u026e\7O\2\2\u026c\u026f"+
		"\5n8\2\u026d\u026f\5\u009aN\2\u026e\u026c\3\2\2\2\u026e\u026d\3\2\2\2"+
		"\u026e\u026f\3\2\2\2\u026fy\3\2\2\2\u0270\u0273\7\u0092\2\2\u0271\u0274"+
		"\5n8\2\u0272\u0274\5\u009eP\2\u0273\u0271\3\2\2\2\u0273\u0272\3\2\2\2"+
		"\u0273\u0274\3\2\2\2\u0274{\3\2\2\2\u0275\u0278\7\u009c\2\2\u0276\u0279"+
		"\5n8\2\u0277\u0279\5\u00a2R\2\u0278\u0276\3\2\2\2\u0278\u0277\3\2\2\2"+
		"\u0278\u0279\3\2\2\2\u0279}\3\2\2\2\u027a\u027c\7I\2\2\u027b\u027d\5\u008a"+
		"F\2\u027c\u027b\3\2\2\2\u027c\u027d\3\2\2\2\u027d\177\3\2\2\2\u027e\u0281"+
		"\7J\2\2\u027f\u0282\5\u008cG\2\u0280\u0282\5\u0096L\2\u0281\u027f\3\2"+
		"\2\2\u0281\u0280\3\2\2\2\u0281\u0282\3\2\2\2\u0282\u0081\3\2\2\2\u0283"+
		"\u0284\7L\2\2\u0284\u0285\t\b\2\2\u0285\u0083\3\2\2\2\u0286\u0287\7\u008d"+
		"\2\2\u0287\u0085\3\2\2\2\u0288\u0289\7\u009a\2\2\u0289\u0087\3\2\2\2\u028a"+
		"\u028d\5:\36\2\u028b\u028e\5n8\2\u028c\u028e\5r:\2\u028d\u028b\3\2\2\2"+
		"\u028d\u028c\3\2\2\2\u028e\u0089\3\2\2\2\u028f\u0291\7\u0095\2\2\u0290"+
		"\u0292\5\u0088E\2\u0291\u0290\3\2\2\2\u0291\u0292\3\2\2\2\u0292\u008b"+
		"\3\2\2\2\u0293\u0297\5\u008eH\2\u0294\u0297\5\u0090I\2\u0295\u0297\5\u0092"+
		"J\2\u0296\u0293\3\2\2\2\u0296\u0294\3\2\2\2\u0296\u0295\3\2\2\2\u0297"+
		"\u008d\3\2\2\2\u0298\u0299\7C\2\2\u0299\u029a\t\3\2\2\u029a\u008f\3\2"+
		"\2\2\u029b\u029c\7U\2\2\u029c\u029d\7\u00a2\2\2\u029d\u0091\3\2\2\2\u029e"+
		"\u029f\t\t\2\2\u029f\u02a0\5,\27\2\u02a0\u0093\3\2\2\2\u02a1\u02a5\5:"+
		"\36\2\u02a2\u02a6\5n8\2\u02a3\u02a6\5\u008cG\2\u02a4\u02a6\5r:\2\u02a5"+
		"\u02a2\3\2\2\2\u02a5\u02a3\3\2\2\2\u02a5\u02a4\3\2\2\2\u02a6\u0095\3\2"+
		"\2\2\u02a7\u02a9\7\u0095\2\2\u02a8\u02aa\5\u0094K\2\u02a9\u02a8\3\2\2"+
		"\2\u02a9\u02aa\3\2\2\2\u02aa\u0097\3\2\2\2\u02ab\u02ad\5:\36\2\u02ac\u02ae"+
		"\5n8\2\u02ad\u02ac\3\2\2\2\u02ad\u02ae\3\2\2\2\u02ae\u0099\3\2\2\2\u02af"+
		"\u02b1\7\u0095\2\2\u02b0\u02b2\5\u0098M\2\u02b1\u02b0\3\2\2\2\u02b1\u02b2"+
		"\3\2\2\2\u02b2\u009b\3\2\2\2\u02b3\u02b5\5:\36\2\u02b4\u02b6\5n8\2\u02b5"+
		"\u02b4\3\2\2\2\u02b5\u02b6\3\2\2\2\u02b6\u009d\3\2\2\2\u02b7\u02b9\7\u0095"+
		"\2\2\u02b8\u02ba\5\u009cO\2\u02b9\u02b8\3\2\2\2\u02b9\u02ba\3\2\2\2\u02ba"+
		"\u009f\3\2\2\2\u02bb\u02bd\5:\36\2\u02bc\u02be\5n8\2\u02bd\u02bc\3\2\2"+
		"\2\u02bd\u02be\3\2\2\2\u02be\u00a1\3\2\2\2\u02bf\u02c1\7\u0095\2\2\u02c0"+
		"\u02c2\5\u00a0Q\2\u02c1\u02c0\3\2\2\2\u02c1\u02c2\3\2\2\2\u02c2\u00a3"+
		"\3\2\2\2\u02c3\u02c4\7\20\2\2\u02c4\u02c5\t\n\2\2\u02c5\u00a5\3\2\2\2"+
		"\u02c6\u02c7\7#\2\2\u02c7\u02c8\t\13\2\2\u02c8\u00a7\3\2\2\2\u02c9\u02cb"+
		"\7,\2\2\u02ca\u02cc\7b\2\2\u02cb\u02ca\3\2\2\2\u02cb\u02cc\3\2\2\2\u02cc"+
		"\u02ce\3\2\2\2\u02cd\u02cf\5\u00aaV\2\u02ce\u02cd\3\2\2\2\u02cf\u02d0"+
		"\3\2\2\2\u02d0\u02ce\3\2\2\2\u02d0\u02d1\3\2\2\2\u02d1\u02d3\3\2\2\2\u02d2"+
		"\u02d4\7\26\2\2\u02d3\u02d2\3\2\2\2\u02d3\u02d4\3\2\2\2\u02d4\u00a9\3"+
		"\2\2\2\u02d5\u02d6\t\f\2\2\u02d6\u00ab\3\2\2\2\u02d7\u02d8\7:\2\2\u02d8"+
		"\u02d9\t\r\2\2\u02d9\u00ad\3\2\2\2\u02da\u02db\7K\2\2\u02db\u02dc\t\16"+
		"\2\2\u02dc\u02dd\7\u00a2\2\2\u02dd\u00af\3\2\2\2\u02de\u02e5\7Z\2\2\u02df"+
		"\u02e6\5\u00b2Z\2\u02e0\u02e6\5l\67\2\u02e1\u02e6\5\u00b8]\2\u02e2\u02e6"+
		"\5\u00ba^\2\u02e3\u02e6\5\u00bc_\2\u02e4\u02e6\5\u00ceh\2\u02e5\u02df"+
		"\3\2\2\2\u02e5\u02e0\3\2\2\2\u02e5\u02e1\3\2\2\2\u02e5\u02e2\3\2\2\2\u02e5"+
		"\u02e3\3\2\2\2\u02e5\u02e4\3\2\2\2\u02e6\u00b1\3\2\2\2\u02e7\u02ea\7?"+
		"\2\2\u02e8\u02eb\5\u00b4[\2\u02e9\u02eb\5\u00b6\\\2\u02ea\u02e8\3\2\2"+
		"\2\u02ea\u02e9\3\2\2\2\u02eb\u00b3\3\2\2\2\u02ec\u02f0\7\31\2\2\u02ed"+
		"\u02f1\5\u00d2j\2\u02ee\u02f1\5\u00d4k\2\u02ef\u02f1\5\u00d6l\2\u02f0"+
		"\u02ed\3\2\2\2\u02f0\u02ee\3\2\2\2\u02f0\u02ef\3\2\2\2\u02f1\u00b5\3\2"+
		"\2\2\u02f2\u02f3\7\62\2\2\u02f3\u02f4\5,\27\2\u02f4\u00b7\3\2\2\2\u02f5"+
		"\u02f6\7k\2\2\u02f6\u02f7\5,\27\2\u02f7\u00b9\3\2\2\2\u02f8\u02f9\7m\2"+
		"\2\u02f9\u02fa\5,\27\2\u02fa\u00bb\3\2\2\2\u02fb\u02fd\7\u0084\2\2\u02fc"+
		"\u02fe\5\u00be`\2\u02fd\u02fc\3\2\2\2\u02fd\u02fe\3\2\2\2\u02fe\u00bd"+
		"\3\2\2\2\u02ff\u0303\5:\36\2\u0300\u0304\5\u00c0a\2\u0301\u0304\5\u00c2"+
		"b\2\u0302\u0304\5\u00c6d\2\u0303\u0300\3\2\2\2\u0303\u0301\3\2\2\2\u0303"+
		"\u0302\3\2\2\2\u0303\u0304\3\2\2\2\u0304\u00bf\3\2\2\2\u0305\u0306\7%"+
		"\2\2\u0306\u0307\5:\36\2\u0307\u00c1\3\2\2\2\u0308\u030a\7A\2\2\u0309"+
		"\u030b\5\u00c4c\2\u030a\u0309\3\2\2\2\u030a\u030b\3\2\2\2\u030b\u00c3"+
		"\3\2\2\2\u030c\u030d\7Z\2\2\u030d\u030e\7B\2\2\u030e\u030f\5\66\34\2\u030f"+
		"\u00c5\3\2\2\2\u0310\u0311\7\u009f\2\2\u0311\u0312\5\u00c8e\2\u0312\u00c7"+
		"\3\2\2\2\u0313\u0315\5:\36\2\u0314\u0316\5\u00caf\2\u0315\u0314\3\2\2"+
		"\2\u0315\u0316\3\2\2\2\u0316\u00c9\3\2\2\2\u0317\u0319\7Z\2\2\u0318\u031a"+
		"\5\u00ccg\2\u0319\u0318\3\2\2\2\u0319\u031a\3\2\2\2\u031a\u00cb\3\2\2"+
		"\2\u031b\u031c\7J\2\2\u031c\u031d\5\66\34\2\u031d\u00cd\3\2\2\2\u031e"+
		"\u0320\7\u0099\2\2\u031f\u0321\5\u00d0i\2\u0320\u031f\3\2\2\2\u0320\u0321"+
		"\3\2\2\2\u0321\u00cf\3\2\2\2\u0322\u0326\5:\36\2\u0323\u0327\5\u00d8m"+
		"\2\u0324\u0327\5\u00dan\2\u0325\u0327\5\u00dco\2\u0326\u0323\3\2\2\2\u0326"+
		"\u0324\3\2\2\2\u0326\u0325\3\2\2\2\u0326\u0327\3\2\2\2\u0327\u00d1\3\2"+
		"\2\2\u0328\u0329\7\63\2\2\u0329\u032c\5:\36\2\u032a\u032d\5\u00a8U\2\u032b"+
		"\u032d\5\u00a4S\2\u032c\u032a\3\2\2\2\u032c\u032b\3\2\2\2\u032d\u00d3"+
		"\3\2\2\2\u032e\u032f\7@\2\2\u032f\u0334\5:\36\2\u0330\u0335\5\u00a6T\2"+
		"\u0331\u0335\5\u00a8U\2\u0332\u0335\5\u00acW\2\u0333\u0335\5\u00aeX\2"+
		"\u0334\u0330\3\2\2\2\u0334\u0331\3\2\2\2\u0334\u0332\3\2\2\2\u0334\u0333"+
		"\3\2\2\2\u0335\u00d5\3\2\2\2\u0336\u0337\7G\2\2\u0337\u033f\5:\36\2\u0338"+
		"\u033b\7-\2\2\u0339\u033c\5\u00a4S\2\u033a\u033c\5\u00a8U\2\u033b\u0339"+
		"\3\2\2\2\u033b\u033a\3\2\2\2\u033c\u0340\3\2\2\2\u033d\u0340\5\u00a6T"+
		"\2\u033e\u0340\5\u00aeX\2\u033f\u0338\3\2\2\2\u033f\u033d\3\2\2\2\u033f"+
		"\u033e\3\2\2\2\u0340\u00d7\3\2\2\2\u0341\u0342\7B\2\2\u0342\u0343\5\66"+
		"\34\2\u0343\u00d9\3\2\2\2\u0344\u0345\7l\2\2\u0345\u0346\5\u00dep\2\u0346"+
		"\u00db\3\2\2\2\u0347\u0348\7q\2\2\u0348\u0349\7C\2\2\u0349\u034a\7\u0089"+
		"\2\2\u034a\u0350\5:\36\2\u034b\u0351\5\u00e4s\2\u034c\u0351\5\u00e6t\2"+
		"\u034d\u0351\5\u00e8u\2\u034e\u0351\5\u00eav\2\u034f\u0351\5\u00ecw\2"+
		"\u0350\u034b\3\2\2\2\u0350\u034c\3\2\2\2\u0350\u034d\3\2\2\2\u0350\u034e"+
		"\3\2\2\2\u0350\u034f\3\2\2\2\u0351\u00dd\3\2\2\2\u0352\u0355\7\23\2\2"+
		"\u0353\u0356\5\u00e0q\2\u0354\u0356\5\u00e2r\2\u0355\u0353\3\2\2\2\u0355"+
		"\u0354\3\2\2\2\u0356\u00df\3\2\2\2\u0357\u0358\7+\2\2\u0358\u0359\7]\2"+
		"\2\u0359\u00e1\3\2\2\2\u035a\u035b\7\32\2\2\u035b\u035c\5,\27\2\u035c"+
		"\u00e3\3\2\2\2\u035d\u035e\7\6\2\2\u035e\u035f\7\u00a2\2\2\u035f\u00e5"+
		"\3\2\2\2\u0360\u0361\7 \2\2\u0361\u0362\7\u00a5\2\2\u0362\u00e7\3\2\2"+
		"\2\u0363\u0364\7B\2\2\u0364\u0365\5:\36\2\u0365\u00e9\3\2\2\2\u0366\u0367"+
		"\7S\2\2\u0367\u0368\7\u00a2\2\2\u0368\u00eb\3\2\2\2\u0369\u036c\7\\\2"+
		"\2\u036a\u036d\5\u00eex\2\u036b\u036d\5\u00f0y\2\u036c\u036a\3\2\2\2\u036c"+
		"\u036b\3\2\2\2\u036d\u00ed\3\2\2\2\u036e\u036f\7D\2\2\u036f\u0370\7\u00a4"+
		"\2\2\u0370\u00ef\3\2\2\2\u0371\u0372\7[\2\2\u0372\u0373\5:\36\2\u0373"+
		"\u00f1\3\2\2\2\u0374\u0375\7r\2\2\u0375\u0376\5\u00f4{\2\u0376\u00f3\3"+
		"\2\2\2\u0377\u0378\5\u00f6|\2\u0378\u00f5\3\2\2\2\u0379\u037b\7x\2\2\u037a"+
		"\u037c\5\u00f8}\2\u037b\u037a\3\2\2\2\u037b\u037c\3\2\2\2\u037c\u00f7"+
		"\3\2\2\2\u037d\u037f\7s\2\2\u037e\u0380\5\u00fa~\2\u037f\u037e\3\2\2\2"+
		"\u037f\u0380\3\2\2\2\u0380\u00f9\3\2\2\2\u0381\u0390\5:\36\2\u0382\u0391"+
		"\5\u00fc\177\2\u0383\u0391\5\u00fe\u0080\2\u0384\u0391\5\u0100\u0081\2"+
		"\u0385\u0391\5\u0102\u0082\2\u0386\u0391\5\u0104\u0083\2\u0387\u0391\5"+
		"\u0106\u0084\2\u0388\u0391\5\u0108\u0085\2\u0389\u0391\5\u010a\u0086\2"+
		"\u038a\u0391\5\u010c\u0087\2\u038b\u0391\5\u010e\u0088\2\u038c\u0391\5"+
		"\u0110\u0089\2\u038d\u0391\5\u0112\u008a\2\u038e\u0391\5\u0114\u008b\2"+
		"\u038f\u0391\5\u0116\u008c\2\u0390\u0382\3\2\2\2\u0390\u0383\3\2\2\2\u0390"+
		"\u0384\3\2\2\2\u0390\u0385\3\2\2\2\u0390\u0386\3\2\2\2\u0390\u0387\3\2"+
		"\2\2\u0390\u0388\3\2\2\2\u0390\u0389\3\2\2\2\u0390\u038a\3\2\2\2\u0390"+
		"\u038b\3\2\2\2\u0390\u038c\3\2\2\2\u0390\u038d\3\2\2\2\u0390\u038e\3\2"+
		"\2\2\u0390\u038f\3\2\2\2\u0390\u0391\3\2\2\2\u0391\u00fb\3\2\2\2\u0392"+
		"\u0393\7\3\2\2\u0393\u0394\t\17\2\2\u0394\u00fd\3\2\2\2\u0395\u0396\7"+
		"\16\2\2\u0396\u0397\5\66\34\2\u0397\u00ff\3\2\2\2\u0398\u0399\7\25\2\2"+
		"\u0399\u039a\5,\27\2\u039a\u0101\3\2\2\2\u039b\u039c\7\37\2\2\u039c\u039d"+
		"\5:\36\2\u039d\u0103\3\2\2\2\u039e\u039f\7 \2\2\u039f\u03a0\5.\30\2\u03a0"+
		"\u0105\3\2\2\2\u03a1\u03a2\7$\2\2\u03a2\u03a3\t\20\2\2\u03a3\u0107\3\2"+
		"\2\2\u03a4\u03a5\7\61\2\2\u03a5\u03a6\5\66\34\2\u03a6\u0109\3\2\2\2\u03a7"+
		"\u03a8\7;\2\2\u03a8\u03a9\7\r\2\2\u03a9\u010b\3\2\2\2\u03aa\u03ab\7W\2"+
		"\2\u03ab\u03ac\t\20\2\2\u03ac\u010d\3\2\2\2\u03ad\u03ae\7X\2\2\u03ae\u03af"+
		"\t\20\2\2\u03af\u010f\3\2\2\2\u03b0\u03b1\7{\2\2\u03b1\u03b2\5\66\34\2"+
		"\u03b2\u0111\3\2\2\2\u03b3\u03b4\7\u0085\2\2\u03b4\u03b5\5.\30\2\u03b5"+
		"\u0113\3\2\2\2\u03b6\u03b7\7\u0087\2\2\u03b7\u03b8\7\r\2\2\u03b8\u0115"+
		"\3\2\2\2\u03b9\u03ba\7\u0091\2\2\u03ba\u03bb\5\66\34\2\u03bb\u0117\3\2"+
		"\2\2\u03bc\u03be\7{\2\2\u03bd\u03bf\5\u011a\u008e\2\u03be\u03bd\3\2\2"+
		"\2\u03be\u03bf\3\2\2\2\u03bf\u0119\3\2\2\2\u03c0\u03c7\5:\36\2\u03c1\u03c6"+
		"\5\u011c\u008f\2\u03c2\u03c6\5\u011e\u0090\2\u03c3\u03c6\5\u0120\u0091"+
		"\2\u03c4\u03c6\5\u0122\u0092\2\u03c5\u03c1\3\2\2\2\u03c5\u03c2\3\2\2\2"+
		"\u03c5\u03c3\3\2\2\2\u03c5\u03c4\3\2\2\2\u03c6\u03c9\3\2\2\2\u03c7\u03c5"+
		"\3\2\2\2\u03c7\u03c8\3\2\2\2\u03c8\u011b\3\2\2\2\u03c9\u03c7\3\2\2\2\u03ca"+
		"\u03cb\7\37\2\2\u03cb\u03cc\5:\36\2\u03cc\u011d\3\2\2\2\u03cd\u03ce\7"+
		"f\2\2\u03ce\u03cf\5\64\33\2\u03cf\u011f\3\2\2\2\u03d0\u03d1\7l\2\2\u03d1"+
		"\u03d2\t\21\2\2\u03d2\u0121\3\2\2\2\u03d3\u03d4\7\u0086\2\2\u03d4\u03d5"+
		"\5\64\33\2\u03d5\u0123\3\2\2\2\u03d6\u03d8\7|\2\2\u03d7\u03d9\5\u0126"+
		"\u0094\2\u03d8\u03d7\3\2\2\2\u03d8\u03d9\3\2\2\2\u03d9\u0125\3\2\2\2\u03da"+
		"\u03dc\5:\36\2\u03db\u03dd\5\u0128\u0095\2\u03dc\u03db\3\2\2\2\u03dc\u03dd"+
		"\3\2\2\2\u03dd\u0127\3\2\2\2\u03de\u03df\7R\2\2\u03df\u03e0\5\66\34\2"+
		"\u03e0\u0129\3\2\2\2\u03e1\u03e4\7\u0083\2\2\u03e2\u03e5\5\u012c\u0097"+
		"\2\u03e3\u03e5\5\u0130\u0099\2\u03e4\u03e2\3\2\2\2\u03e4\u03e3\3\2\2\2"+
		"\u03e5\u012b\3\2\2\2\u03e6\u03ee\5\36\20\2\u03e7\u03ee\5<\37\2\u03e8\u03ee"+
		"\5H%\2\u03e9\u03ee\5N(\2\u03ea\u03ee\5\u0118\u008d\2\u03eb\u03ee\5\u0124"+
		"\u0093\2\u03ec\u03ee\5\u012e\u0098\2\u03ed\u03e6\3\2\2\2\u03ed\u03e7\3"+
		"\2\2\2\u03ed\u03e8\3\2\2\2\u03ed\u03e9\3\2\2\2\u03ed\u03ea\3\2\2\2\u03ed"+
		"\u03eb\3\2\2\2\u03ed\u03ec\3\2\2\2\u03ee\u012d\3\2\2\2\u03ef\u03f0\7N"+
		"\2\2\u03f0\u03f1\5\u0132\u009a\2\u03f1\u012f\3\2\2\2\u03f2\u03f3\7\24"+
		"\2\2\u03f3\u03f4\5,\27\2\u03f4\u0131\3\2\2\2\u03f5\u03f6\7\u008a\2\2\u03f6"+
		"\u03f7\5:\36\2\u03f7\u03f8\5\u0134\u009b\2\u03f8\u0133\3\2\2\2\u03f9\u03fa"+
		"\7y\2\2\u03fa\u03fb\5:\36\2\u03fb\u03fc\5\u0136\u009c\2\u03fc\u0135\3"+
		"\2\2\2\u03fd\u03fe\7y\2\2\u03fe\u03ff\5:\36\2\u03ff\u0137\3\2\2\2\u0400"+
		"\u0402\7\u009d\2\2\u0401\u0403\5\u013a\u009e\2\u0402\u0401\3\2\2\2\u0402"+
		"\u0403\3\2\2\2\u0403\u0139\3\2\2\2\u0404\u0409\5:\36\2\u0405\u040a\5\u00f2"+
		"z\2\u0406\u040a\5\u0144\u00a3\2\u0407\u040a\5\u012c\u0097\2\u0408\u040a"+
		"\5\u013c\u009f\2\u0409\u0405\3\2\2\2\u0409\u0406\3\2\2\2\u0409\u0407\3"+
		"\2\2\2\u0409\u0408\3\2\2\2\u0409\u040a\3\2\2\2\u040a\u013b\3\2\2\2\u040b"+
		"\u040e\7A\2\2\u040c\u040f\5\u013e\u00a0\2\u040d\u040f\5\u0140\u00a1\2"+
		"\u040e\u040c\3\2\2\2\u040e\u040d\3\2\2\2\u040e\u040f\3\2\2\2\u040f\u013d"+
		"\3\2\2\2\u0410\u0412\7Z\2\2\u0411\u0413\5\u0142\u00a2\2\u0412\u0411\3"+
		"\2\2\2\u0412\u0413\3\2\2\2\u0413\u013f\3\2\2\2\u0414\u0416\7\u009b\2\2"+
		"\u0415\u0417\5\66\34\2\u0416\u0415\3\2\2\2\u0416\u0417\3\2\2\2\u0417\u0141"+
		"\3\2\2\2\u0418\u041a\7B\2\2\u0419\u041b\5\66\34\2\u041a\u0419\3\2\2\2"+
		"\u041a\u041b\3\2\2\2\u041b\u0143\3\2\2\2\u041c\u041e\7\u009f\2\2\u041d"+
		"\u041f\5\u0146\u00a4\2\u041e\u041d\3\2\2\2\u041e\u041f\3\2\2\2\u041f\u0145"+
		"\3\2\2\2\u0420\u0422\5:\36\2\u0421\u0423\5\u0148\u00a5\2\u0422\u0421\3"+
		"\2\2\2\u0422\u0423\3\2\2\2\u0423\u0147\3\2\2\2\u0424\u042a\7Z\2\2\u0425"+
		"\u042b\5\u014a\u00a6\2\u0426\u042b\5\u014c\u00a7\2\u0427\u042b\5\u014e"+
		"\u00a8\2\u0428\u042b\5\u0150\u00a9\2\u0429\u042b\5\u0152\u00aa\2\u042a"+
		"\u0425\3\2\2\2\u042a\u0426\3\2\2\2\u042a\u0427\3\2\2\2\u042a\u0428\3\2"+
		"\2\2\u042a\u0429\3\2\2\2\u042a\u042b\3\2\2\2\u042b\u0149\3\2\2\2\u042c"+
		"\u042e\7.\2\2\u042d\u042f\5\66\34\2\u042e\u042d\3\2\2\2\u042e\u042f\3"+
		"\2\2\2\u042f\u014b\3\2\2\2\u0430\u0432\7I\2\2\u0431\u0433\5\66\34\2\u0432"+
		"\u0431\3\2\2\2\u0432\u0433\3\2\2\2\u0433\u014d\3\2\2\2\u0434\u0436\7J"+
		"\2\2\u0435\u0437\5\66\34\2\u0436\u0435\3\2\2\2\u0436\u0437\3\2\2\2\u0437"+
		"\u014f\3\2\2\2\u0438\u043a\7\u008d\2\2\u0439\u043b\5\66\34\2\u043a\u0439"+
		"\3\2\2\2\u043a\u043b\3\2\2\2\u043b\u0151\3\2\2\2\u043c\u043e\7\u009a\2"+
		"\2\u043d\u043f\5\66\34\2\u043e\u043d\3\2\2\2\u043e\u043f\3\2\2\2\u043f"+
		"\u0153\3\2\2\2_\u0156\u0158\u0164\u0169\u0178\u0183\u0188\u018e\u019a"+
		"\u01a2\u01b6\u01be\u01c2\u01c9\u01cd\u01d4\u01dc\u01e0\u01e8\u01ef\u0200"+
		"\u0204\u0208\u020c\u0213\u0217\u021b\u0220\u022d\u024a\u0254\u0260\u0269"+
		"\u026e\u0273\u0278\u027c\u0281\u028d\u0291\u0296\u02a5\u02a9\u02ad\u02b1"+
		"\u02b5\u02b9\u02bd\u02c1\u02cb\u02d0\u02d3\u02e5\u02ea\u02f0\u02fd\u0303"+
		"\u030a\u0315\u0319\u0320\u0326\u032c\u0334\u033b\u033f\u0350\u0355\u036c"+
		"\u037b\u037f\u0390\u03be\u03c5\u03c7\u03d8\u03dc\u03e4\u03ed\u0402\u0409"+
		"\u040e\u0412\u0416\u041a\u041e\u0422\u042a\u042e\u0432\u0436\u043a\u043e";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}