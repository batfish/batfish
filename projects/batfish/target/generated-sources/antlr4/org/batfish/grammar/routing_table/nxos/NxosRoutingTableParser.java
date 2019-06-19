// Generated from org/batfish/grammar/routing_table/nxos/NxosRoutingTableParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.routing_table.nxos;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NxosRoutingTableParser extends org.batfish.grammar.BatfishParser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ID=1, ATTACHED=2, BGP=3, DIRECT=4, ETH=5, EXTERNAL=6, INTER=7, INTERNAL=8, 
		INTRA=9, LO=10, LOCAL=11, NULL=12, OSPF=13, STATIC=14, TAG=15, TYPE_1=16, 
		TYPE_2=17, VIA=18, VRF_HEADER=19, ASTERISK=20, BRACKET_LEFT=21, BRACKET_RIGHT=22, 
		COLON=23, COMMA=24, COMMENT=25, DASH=26, DEC=27, DOUBLE_QUOTE=28, ELAPSED_TIME=29, 
		FORWARD_SLASH=30, IP_ADDRESS=31, IP_PREFIX=32, NEWLINE=33, UNICAST_MULTICAST_COUNT=34, 
		WS=35;
	public static final int
		RULE_double_quoted_string = 0, RULE_interface_name = 1, RULE_network = 2, 
		RULE_nxos_routing_table = 3, RULE_protocol = 4, RULE_route = 5, RULE_vrf_declaration = 6, 
		RULE_vrf_routing_table = 7;
	private static String[] makeRuleNames() {
		return new String[] {
			"double_quoted_string", "interface_name", "network", "nxos_routing_table", 
			"protocol", "route", "vrf_declaration", "vrf_routing_table"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, "'attached'", "'bgp'", "'direct'", "'Eth'", "'external'", 
			"'inter'", "'internal'", "'intra'", "'Lo'", "'local'", "'Null'", "'ospf'", 
			"'static'", "'tag'", "'type-1'", "'type-2'", "'via'", "'IP Route Table for VRF'", 
			"'*'", "'['", "']'", "':'", "','", null, "'-'", null, null, null, "'/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ID", "ATTACHED", "BGP", "DIRECT", "ETH", "EXTERNAL", "INTER", 
			"INTERNAL", "INTRA", "LO", "LOCAL", "NULL", "OSPF", "STATIC", "TAG", 
			"TYPE_1", "TYPE_2", "VIA", "VRF_HEADER", "ASTERISK", "BRACKET_LEFT", 
			"BRACKET_RIGHT", "COLON", "COMMA", "COMMENT", "DASH", "DEC", "DOUBLE_QUOTE", 
			"ELAPSED_TIME", "FORWARD_SLASH", "IP_ADDRESS", "IP_PREFIX", "NEWLINE", 
			"UNICAST_MULTICAST_COUNT", "WS"
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
	public String getGrammarFileName() { return "NxosRoutingTableParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }




	public NxosRoutingTableParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class Double_quoted_stringContext extends ParserRuleContext {
		public List<TerminalNode> DOUBLE_QUOTE() { return getTokens(NxosRoutingTableParser.DOUBLE_QUOTE); }
		public TerminalNode DOUBLE_QUOTE(int i) {
			return getToken(NxosRoutingTableParser.DOUBLE_QUOTE, i);
		}
		public TerminalNode ID() { return getToken(NxosRoutingTableParser.ID, 0); }
		public Double_quoted_stringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_double_quoted_string; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).enterDouble_quoted_string(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).exitDouble_quoted_string(this);
		}
	}

	public final Double_quoted_stringContext double_quoted_string() throws RecognitionException {
		Double_quoted_stringContext _localctx = new Double_quoted_stringContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_double_quoted_string);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(16);
			match(DOUBLE_QUOTE);
			setState(18);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(17);
				match(ID);
				}
			}

			setState(20);
			match(DOUBLE_QUOTE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Interface_nameContext extends ParserRuleContext {
		public List<TerminalNode> COMMA() { return getTokens(NxosRoutingTableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(NxosRoutingTableParser.COMMA, i);
		}
		public Interface_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interface_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).enterInterface_name(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).exitInterface_name(this);
		}
	}

	public final Interface_nameContext interface_name() throws RecognitionException {
		Interface_nameContext _localctx = new Interface_nameContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_interface_name);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(23); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(22);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==COMMA) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(25); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ID) | (1L << ATTACHED) | (1L << BGP) | (1L << DIRECT) | (1L << ETH) | (1L << EXTERNAL) | (1L << INTER) | (1L << INTERNAL) | (1L << INTRA) | (1L << LO) | (1L << LOCAL) | (1L << NULL) | (1L << OSPF) | (1L << STATIC) | (1L << TAG) | (1L << TYPE_1) | (1L << TYPE_2) | (1L << VIA) | (1L << VRF_HEADER) | (1L << ASTERISK) | (1L << BRACKET_LEFT) | (1L << BRACKET_RIGHT) | (1L << COLON) | (1L << COMMENT) | (1L << DASH) | (1L << DEC) | (1L << DOUBLE_QUOTE) | (1L << ELAPSED_TIME) | (1L << FORWARD_SLASH) | (1L << IP_ADDRESS) | (1L << IP_PREFIX) | (1L << NEWLINE) | (1L << UNICAST_MULTICAST_COUNT) | (1L << WS))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NetworkContext extends ParserRuleContext {
		public TerminalNode IP_PREFIX() { return getToken(NxosRoutingTableParser.IP_PREFIX, 0); }
		public List<TerminalNode> COMMA() { return getTokens(NxosRoutingTableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(NxosRoutingTableParser.COMMA, i);
		}
		public TerminalNode UNICAST_MULTICAST_COUNT() { return getToken(NxosRoutingTableParser.UNICAST_MULTICAST_COUNT, 0); }
		public TerminalNode NEWLINE() { return getToken(NxosRoutingTableParser.NEWLINE, 0); }
		public TerminalNode ATTACHED() { return getToken(NxosRoutingTableParser.ATTACHED, 0); }
		public List<RouteContext> route() {
			return getRuleContexts(RouteContext.class);
		}
		public RouteContext route(int i) {
			return getRuleContext(RouteContext.class,i);
		}
		public NetworkContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_network; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).enterNetwork(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).exitNetwork(this);
		}
	}

	public final NetworkContext network() throws RecognitionException {
		NetworkContext _localctx = new NetworkContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_network);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(27);
			match(IP_PREFIX);
			setState(28);
			match(COMMA);
			setState(29);
			match(UNICAST_MULTICAST_COUNT);
			setState(32);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(30);
				match(COMMA);
				setState(31);
				match(ATTACHED);
				}
			}

			setState(34);
			match(NEWLINE);
			setState(36); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(35);
				route();
				}
				}
				setState(38); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ASTERISK );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Nxos_routing_tableContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(NxosRoutingTableParser.EOF, 0); }
		public TerminalNode NEWLINE() { return getToken(NxosRoutingTableParser.NEWLINE, 0); }
		public List<Vrf_routing_tableContext> vrf_routing_table() {
			return getRuleContexts(Vrf_routing_tableContext.class);
		}
		public Vrf_routing_tableContext vrf_routing_table(int i) {
			return getRuleContext(Vrf_routing_tableContext.class,i);
		}
		public Nxos_routing_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nxos_routing_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).enterNxos_routing_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).exitNxos_routing_table(this);
		}
	}

	public final Nxos_routing_tableContext nxos_routing_table() throws RecognitionException {
		Nxos_routing_tableContext _localctx = new Nxos_routing_tableContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_nxos_routing_table);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NEWLINE) {
				{
				setState(40);
				match(NEWLINE);
				}
			}

			setState(44); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(43);
				vrf_routing_table();
				}
				}
				setState(46); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==VRF_HEADER );
			setState(48);
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

	public static class ProtocolContext extends ParserRuleContext {
		public TerminalNode BGP() { return getToken(NxosRoutingTableParser.BGP, 0); }
		public TerminalNode DASH() { return getToken(NxosRoutingTableParser.DASH, 0); }
		public TerminalNode DEC() { return getToken(NxosRoutingTableParser.DEC, 0); }
		public TerminalNode COMMA() { return getToken(NxosRoutingTableParser.COMMA, 0); }
		public TerminalNode EXTERNAL() { return getToken(NxosRoutingTableParser.EXTERNAL, 0); }
		public TerminalNode INTERNAL() { return getToken(NxosRoutingTableParser.INTERNAL, 0); }
		public TerminalNode DIRECT() { return getToken(NxosRoutingTableParser.DIRECT, 0); }
		public TerminalNode LOCAL() { return getToken(NxosRoutingTableParser.LOCAL, 0); }
		public TerminalNode OSPF() { return getToken(NxosRoutingTableParser.OSPF, 0); }
		public TerminalNode INTER() { return getToken(NxosRoutingTableParser.INTER, 0); }
		public TerminalNode INTRA() { return getToken(NxosRoutingTableParser.INTRA, 0); }
		public TerminalNode TYPE_1() { return getToken(NxosRoutingTableParser.TYPE_1, 0); }
		public TerminalNode TYPE_2() { return getToken(NxosRoutingTableParser.TYPE_2, 0); }
		public TerminalNode STATIC() { return getToken(NxosRoutingTableParser.STATIC, 0); }
		public ProtocolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_protocol; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).enterProtocol(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).exitProtocol(this);
		}
	}

	public final ProtocolContext protocol() throws RecognitionException {
		ProtocolContext _localctx = new ProtocolContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_protocol);
		int _la;
		try {
			setState(63);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BGP:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(50);
				match(BGP);
				setState(51);
				match(DASH);
				setState(52);
				match(DEC);
				setState(53);
				match(COMMA);
				setState(54);
				_la = _input.LA(1);
				if ( !(_la==EXTERNAL || _la==INTERNAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				break;
			case DIRECT:
				enterOuterAlt(_localctx, 2);
				{
				setState(55);
				match(DIRECT);
				}
				break;
			case LOCAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(56);
				match(LOCAL);
				}
				break;
			case OSPF:
				enterOuterAlt(_localctx, 4);
				{
				{
				setState(57);
				match(OSPF);
				setState(58);
				match(DASH);
				setState(59);
				match(DEC);
				setState(60);
				match(COMMA);
				setState(61);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << INTER) | (1L << INTRA) | (1L << TYPE_1) | (1L << TYPE_2))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				break;
			case STATIC:
				enterOuterAlt(_localctx, 5);
				{
				setState(62);
				match(STATIC);
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

	public static class RouteContext extends ParserRuleContext {
		public Token nexthop;
		public Interface_nameContext nexthopint;
		public Token admin;
		public Token cost;
		public TerminalNode ASTERISK() { return getToken(NxosRoutingTableParser.ASTERISK, 0); }
		public TerminalNode VIA() { return getToken(NxosRoutingTableParser.VIA, 0); }
		public List<TerminalNode> COMMA() { return getTokens(NxosRoutingTableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(NxosRoutingTableParser.COMMA, i);
		}
		public TerminalNode BRACKET_LEFT() { return getToken(NxosRoutingTableParser.BRACKET_LEFT, 0); }
		public TerminalNode FORWARD_SLASH() { return getToken(NxosRoutingTableParser.FORWARD_SLASH, 0); }
		public TerminalNode BRACKET_RIGHT() { return getToken(NxosRoutingTableParser.BRACKET_RIGHT, 0); }
		public TerminalNode ELAPSED_TIME() { return getToken(NxosRoutingTableParser.ELAPSED_TIME, 0); }
		public ProtocolContext protocol() {
			return getRuleContext(ProtocolContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(NxosRoutingTableParser.NEWLINE, 0); }
		public List<TerminalNode> DEC() { return getTokens(NxosRoutingTableParser.DEC); }
		public TerminalNode DEC(int i) {
			return getToken(NxosRoutingTableParser.DEC, i);
		}
		public TerminalNode IP_ADDRESS() { return getToken(NxosRoutingTableParser.IP_ADDRESS, 0); }
		public List<Interface_nameContext> interface_name() {
			return getRuleContexts(Interface_nameContext.class);
		}
		public Interface_nameContext interface_name(int i) {
			return getRuleContext(Interface_nameContext.class,i);
		}
		public TerminalNode TAG() { return getToken(NxosRoutingTableParser.TAG, 0); }
		public RouteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_route; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).enterRoute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).exitRoute(this);
		}
	}

	public final RouteContext route() throws RecognitionException {
		RouteContext _localctx = new RouteContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_route);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(65);
			match(ASTERISK);
			setState(66);
			match(VIA);
			setState(69);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(67);
				((RouteContext)_localctx).nexthop = match(IP_ADDRESS);
				}
				break;
			case 2:
				{
				setState(68);
				((RouteContext)_localctx).nexthopint = interface_name();
				}
				break;
			}
			setState(73);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				setState(71);
				match(COMMA);
				setState(72);
				((RouteContext)_localctx).nexthopint = interface_name();
				}
				break;
			}
			setState(75);
			match(COMMA);
			setState(76);
			match(BRACKET_LEFT);
			setState(77);
			((RouteContext)_localctx).admin = match(DEC);
			setState(78);
			match(FORWARD_SLASH);
			setState(79);
			((RouteContext)_localctx).cost = match(DEC);
			setState(80);
			match(BRACKET_RIGHT);
			setState(81);
			match(COMMA);
			setState(82);
			match(ELAPSED_TIME);
			setState(83);
			match(COMMA);
			setState(84);
			protocol();
			setState(88);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(85);
				match(COMMA);
				setState(86);
				match(TAG);
				setState(87);
				match(DEC);
				}
			}

			setState(90);
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

	public static class Vrf_declarationContext extends ParserRuleContext {
		public TerminalNode VRF_HEADER() { return getToken(NxosRoutingTableParser.VRF_HEADER, 0); }
		public Double_quoted_stringContext double_quoted_string() {
			return getRuleContext(Double_quoted_stringContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(NxosRoutingTableParser.NEWLINE, 0); }
		public Vrf_declarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vrf_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).enterVrf_declaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).exitVrf_declaration(this);
		}
	}

	public final Vrf_declarationContext vrf_declaration() throws RecognitionException {
		Vrf_declarationContext _localctx = new Vrf_declarationContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_vrf_declaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92);
			match(VRF_HEADER);
			setState(93);
			double_quoted_string();
			setState(94);
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

	public static class Vrf_routing_tableContext extends ParserRuleContext {
		public Vrf_declarationContext vrf_declaration() {
			return getRuleContext(Vrf_declarationContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(NxosRoutingTableParser.NEWLINE, 0); }
		public List<NetworkContext> network() {
			return getRuleContexts(NetworkContext.class);
		}
		public NetworkContext network(int i) {
			return getRuleContext(NetworkContext.class,i);
		}
		public Vrf_routing_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vrf_routing_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).enterVrf_routing_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NxosRoutingTableParserListener ) ((NxosRoutingTableParserListener)listener).exitVrf_routing_table(this);
		}
	}

	public final Vrf_routing_tableContext vrf_routing_table() throws RecognitionException {
		Vrf_routing_tableContext _localctx = new Vrf_routing_tableContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_vrf_routing_table);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			vrf_declaration();
			setState(98);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NEWLINE) {
				{
				setState(97);
				match(NEWLINE);
				}
			}

			setState(103);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IP_PREFIX) {
				{
				{
				setState(100);
				network();
				}
				}
				setState(105);
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3%m\4\2\t\2\4\3\t\3"+
		"\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\3\2\3\2\5\2\25\n\2\3"+
		"\2\3\2\3\3\6\3\32\n\3\r\3\16\3\33\3\4\3\4\3\4\3\4\3\4\5\4#\n\4\3\4\3\4"+
		"\6\4\'\n\4\r\4\16\4(\3\5\5\5,\n\5\3\5\6\5/\n\5\r\5\16\5\60\3\5\3\5\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6B\n\6\3\7\3\7\3\7"+
		"\3\7\5\7H\n\7\3\7\3\7\5\7L\n\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\5\7[\n\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\5\te\n\t\3\t\7\t"+
		"h\n\t\f\t\16\tk\13\t\3\t\2\2\n\2\4\6\b\n\f\16\20\2\5\3\2\32\32\4\2\b\b"+
		"\n\n\5\2\t\t\13\13\22\23\2s\2\22\3\2\2\2\4\31\3\2\2\2\6\35\3\2\2\2\b+"+
		"\3\2\2\2\nA\3\2\2\2\fC\3\2\2\2\16^\3\2\2\2\20b\3\2\2\2\22\24\7\36\2\2"+
		"\23\25\7\3\2\2\24\23\3\2\2\2\24\25\3\2\2\2\25\26\3\2\2\2\26\27\7\36\2"+
		"\2\27\3\3\2\2\2\30\32\n\2\2\2\31\30\3\2\2\2\32\33\3\2\2\2\33\31\3\2\2"+
		"\2\33\34\3\2\2\2\34\5\3\2\2\2\35\36\7\"\2\2\36\37\7\32\2\2\37\"\7$\2\2"+
		" !\7\32\2\2!#\7\4\2\2\" \3\2\2\2\"#\3\2\2\2#$\3\2\2\2$&\7#\2\2%\'\5\f"+
		"\7\2&%\3\2\2\2\'(\3\2\2\2(&\3\2\2\2()\3\2\2\2)\7\3\2\2\2*,\7#\2\2+*\3"+
		"\2\2\2+,\3\2\2\2,.\3\2\2\2-/\5\20\t\2.-\3\2\2\2/\60\3\2\2\2\60.\3\2\2"+
		"\2\60\61\3\2\2\2\61\62\3\2\2\2\62\63\7\2\2\3\63\t\3\2\2\2\64\65\7\5\2"+
		"\2\65\66\7\34\2\2\66\67\7\35\2\2\678\7\32\2\28B\t\3\2\29B\7\6\2\2:B\7"+
		"\r\2\2;<\7\17\2\2<=\7\34\2\2=>\7\35\2\2>?\7\32\2\2?B\t\4\2\2@B\7\20\2"+
		"\2A\64\3\2\2\2A9\3\2\2\2A:\3\2\2\2A;\3\2\2\2A@\3\2\2\2B\13\3\2\2\2CD\7"+
		"\26\2\2DG\7\24\2\2EH\7!\2\2FH\5\4\3\2GE\3\2\2\2GF\3\2\2\2HK\3\2\2\2IJ"+
		"\7\32\2\2JL\5\4\3\2KI\3\2\2\2KL\3\2\2\2LM\3\2\2\2MN\7\32\2\2NO\7\27\2"+
		"\2OP\7\35\2\2PQ\7 \2\2QR\7\35\2\2RS\7\30\2\2ST\7\32\2\2TU\7\37\2\2UV\7"+
		"\32\2\2VZ\5\n\6\2WX\7\32\2\2XY\7\21\2\2Y[\7\35\2\2ZW\3\2\2\2Z[\3\2\2\2"+
		"[\\\3\2\2\2\\]\7#\2\2]\r\3\2\2\2^_\7\25\2\2_`\5\2\2\2`a\7#\2\2a\17\3\2"+
		"\2\2bd\5\16\b\2ce\7#\2\2dc\3\2\2\2de\3\2\2\2ei\3\2\2\2fh\5\6\4\2gf\3\2"+
		"\2\2hk\3\2\2\2ig\3\2\2\2ij\3\2\2\2j\21\3\2\2\2ki\3\2\2\2\16\24\33\"(+"+
		"\60AGKZdi";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}