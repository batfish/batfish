// Generated from org/batfish/grammar/routing_table/eos/EosRoutingTableParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.routing_table.eos;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class EosRoutingTableParser extends org.batfish.grammar.BatfishParser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		CODES=1, GATEWAY=2, IS_DIRECTLY_CONNECTED=3, NAME=4, VIA=5, VRF=6, BRACKET_LEFT=7, 
		BRACKET_RIGHT=8, COLON=9, COMMA=10, COMMENT=11, DASH=12, DEC=13, FORWARD_SLASH=14, 
		IP_ADDRESS=15, IP_PREFIX=16, NEWLINE=17, WORD=18, WS=19;
	public static final int
		RULE_code = 0, RULE_codes_declaration = 1, RULE_eos_routing_table = 2, 
		RULE_gateway_header = 3, RULE_identifier = 4, RULE_protocol = 5, RULE_route = 6, 
		RULE_vrf_declaration = 7, RULE_vrf_routing_table = 8;
	private static String[] makeRuleNames() {
		return new String[] {
			"code", "codes_declaration", "eos_routing_table", "gateway_header", "identifier", 
			"protocol", "route", "vrf_declaration", "vrf_routing_table"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'Codes'", "'Gateway'", "'is directly connected'", "'name'", "'via'", 
			"'VRF'", "'['", "']'", "':'", "','", null, "'-'", null, "'/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "CODES", "GATEWAY", "IS_DIRECTLY_CONNECTED", "NAME", "VIA", "VRF", 
			"BRACKET_LEFT", "BRACKET_RIGHT", "COLON", "COMMA", "COMMENT", "DASH", 
			"DEC", "FORWARD_SLASH", "IP_ADDRESS", "IP_PREFIX", "NEWLINE", "WORD", 
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
	public String getGrammarFileName() { return "EosRoutingTableParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }




	public EosRoutingTableParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class CodeContext extends ParserRuleContext {
		public Token WORD;
		public List<Token> code_parts = new ArrayList<Token>();
		public List<Token> description = new ArrayList<Token>();
		public Token DEC;
		public TerminalNode DASH() { return getToken(EosRoutingTableParser.DASH, 0); }
		public List<TerminalNode> WORD() { return getTokens(EosRoutingTableParser.WORD); }
		public TerminalNode WORD(int i) {
			return getToken(EosRoutingTableParser.WORD, i);
		}
		public List<TerminalNode> DEC() { return getTokens(EosRoutingTableParser.DEC); }
		public TerminalNode DEC(int i) {
			return getToken(EosRoutingTableParser.DEC, i);
		}
		public CodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_code; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).enterCode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).exitCode(this);
		}
	}

	public final CodeContext code() throws RecognitionException {
		CodeContext _localctx = new CodeContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_code);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(19); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(18);
				((CodeContext)_localctx).WORD = match(WORD);
				((CodeContext)_localctx).code_parts.add(((CodeContext)_localctx).WORD);
				}
				}
				setState(21); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WORD );
			setState(23);
			match(DASH);
			setState(26); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(26);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case WORD:
					{
					setState(24);
					((CodeContext)_localctx).WORD = match(WORD);
					((CodeContext)_localctx).description.add(((CodeContext)_localctx).WORD);
					}
					break;
				case DEC:
					{
					setState(25);
					((CodeContext)_localctx).DEC = match(DEC);
					((CodeContext)_localctx).description.add(((CodeContext)_localctx).DEC);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(28); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==DEC || _la==WORD );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Codes_declarationContext extends ParserRuleContext {
		public TerminalNode CODES() { return getToken(EosRoutingTableParser.CODES, 0); }
		public TerminalNode COLON() { return getToken(EosRoutingTableParser.COLON, 0); }
		public List<CodeContext> code() {
			return getRuleContexts(CodeContext.class);
		}
		public CodeContext code(int i) {
			return getRuleContext(CodeContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(EosRoutingTableParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(EosRoutingTableParser.NEWLINE, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(EosRoutingTableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(EosRoutingTableParser.COMMA, i);
		}
		public Codes_declarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_codes_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).enterCodes_declaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).exitCodes_declaration(this);
		}
	}

	public final Codes_declarationContext codes_declaration() throws RecognitionException {
		Codes_declarationContext _localctx = new Codes_declarationContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_codes_declaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
			match(CODES);
			setState(31);
			match(COLON);
			setState(32);
			code();
			setState(38); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(33);
				match(COMMA);
				setState(35);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NEWLINE) {
					{
					setState(34);
					match(NEWLINE);
					}
				}

				setState(37);
				code();
				}
				}
				setState(40); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==COMMA );
			setState(42);
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

	public static class Eos_routing_tableContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(EosRoutingTableParser.EOF, 0); }
		public TerminalNode NEWLINE() { return getToken(EosRoutingTableParser.NEWLINE, 0); }
		public List<Vrf_routing_tableContext> vrf_routing_table() {
			return getRuleContexts(Vrf_routing_tableContext.class);
		}
		public Vrf_routing_tableContext vrf_routing_table(int i) {
			return getRuleContext(Vrf_routing_tableContext.class,i);
		}
		public Eos_routing_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eos_routing_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).enterEos_routing_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).exitEos_routing_table(this);
		}
	}

	public final Eos_routing_tableContext eos_routing_table() throws RecognitionException {
		Eos_routing_tableContext _localctx = new Eos_routing_tableContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_eos_routing_table);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NEWLINE) {
				{
				setState(44);
				match(NEWLINE);
				}
			}

			setState(48); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(47);
				vrf_routing_table();
				}
				}
				setState(50); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==CODES || _la==VRF );
			setState(52);
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

	public static class Gateway_headerContext extends ParserRuleContext {
		public TerminalNode GATEWAY() { return getToken(EosRoutingTableParser.GATEWAY, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(EosRoutingTableParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(EosRoutingTableParser.NEWLINE, i);
		}
		public List<TerminalNode> WORD() { return getTokens(EosRoutingTableParser.WORD); }
		public TerminalNode WORD(int i) {
			return getToken(EosRoutingTableParser.WORD, i);
		}
		public Gateway_headerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gateway_header; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).enterGateway_header(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).exitGateway_header(this);
		}
	}

	public final Gateway_headerContext gateway_header() throws RecognitionException {
		Gateway_headerContext _localctx = new Gateway_headerContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_gateway_header);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			match(GATEWAY);
			setState(56); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(55);
					match(WORD);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(58); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(63);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CODES) | (1L << GATEWAY) | (1L << IS_DIRECTLY_CONNECTED) | (1L << NAME) | (1L << VIA) | (1L << VRF) | (1L << BRACKET_LEFT) | (1L << BRACKET_RIGHT) | (1L << COLON) | (1L << COMMA) | (1L << COMMENT) | (1L << DASH) | (1L << DEC) | (1L << FORWARD_SLASH) | (1L << IP_ADDRESS) | (1L << IP_PREFIX) | (1L << WORD) | (1L << WS))) != 0)) {
				{
				{
				setState(60);
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
				setState(65);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(66);
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

	public static class IdentifierContext extends ParserRuleContext {
		public List<TerminalNode> NEWLINE() { return getTokens(EosRoutingTableParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(EosRoutingTableParser.NEWLINE, i);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).exitIdentifier(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(68);
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
				setState(71); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CODES) | (1L << GATEWAY) | (1L << IS_DIRECTLY_CONNECTED) | (1L << NAME) | (1L << VIA) | (1L << VRF) | (1L << BRACKET_LEFT) | (1L << BRACKET_RIGHT) | (1L << COLON) | (1L << COMMA) | (1L << COMMENT) | (1L << DASH) | (1L << DEC) | (1L << FORWARD_SLASH) | (1L << IP_ADDRESS) | (1L << IP_PREFIX) | (1L << WORD) | (1L << WS))) != 0) );
			}
		}
		catch (RecognitionException re) {
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
		public List<TerminalNode> WORD() { return getTokens(EosRoutingTableParser.WORD); }
		public TerminalNode WORD(int i) {
			return getToken(EosRoutingTableParser.WORD, i);
		}
		public ProtocolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_protocol; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).enterProtocol(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).exitProtocol(this);
		}
	}

	public final ProtocolContext protocol() throws RecognitionException {
		ProtocolContext _localctx = new ProtocolContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_protocol);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(73);
				match(WORD);
				}
				}
				setState(76); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WORD );
			}
		}
		catch (RecognitionException re) {
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
		public Token admin;
		public Token cost;
		public Token IP_ADDRESS;
		public List<Token> nexthops = new ArrayList<Token>();
		public IdentifierContext identifier;
		public List<IdentifierContext> nexthopifaces = new ArrayList<IdentifierContext>();
		public ProtocolContext protocol() {
			return getRuleContext(ProtocolContext.class,0);
		}
		public TerminalNode IP_PREFIX() { return getToken(EosRoutingTableParser.IP_PREFIX, 0); }
		public TerminalNode BRACKET_LEFT() { return getToken(EosRoutingTableParser.BRACKET_LEFT, 0); }
		public TerminalNode FORWARD_SLASH() { return getToken(EosRoutingTableParser.FORWARD_SLASH, 0); }
		public TerminalNode BRACKET_RIGHT() { return getToken(EosRoutingTableParser.BRACKET_RIGHT, 0); }
		public TerminalNode IS_DIRECTLY_CONNECTED() { return getToken(EosRoutingTableParser.IS_DIRECTLY_CONNECTED, 0); }
		public List<TerminalNode> COMMA() { return getTokens(EosRoutingTableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(EosRoutingTableParser.COMMA, i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(EosRoutingTableParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(EosRoutingTableParser.NEWLINE, i);
		}
		public List<TerminalNode> DEC() { return getTokens(EosRoutingTableParser.DEC); }
		public TerminalNode DEC(int i) {
			return getToken(EosRoutingTableParser.DEC, i);
		}
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> VIA() { return getTokens(EosRoutingTableParser.VIA); }
		public TerminalNode VIA(int i) {
			return getToken(EosRoutingTableParser.VIA, i);
		}
		public List<TerminalNode> IP_ADDRESS() { return getTokens(EosRoutingTableParser.IP_ADDRESS); }
		public TerminalNode IP_ADDRESS(int i) {
			return getToken(EosRoutingTableParser.IP_ADDRESS, i);
		}
		public RouteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_route; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).enterRoute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).exitRoute(this);
		}
	}

	public final RouteContext route() throws RecognitionException {
		RouteContext _localctx = new RouteContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_route);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			protocol();
			setState(79);
			match(IP_PREFIX);
			setState(100);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BRACKET_LEFT:
				{
				{
				setState(80);
				match(BRACKET_LEFT);
				setState(81);
				((RouteContext)_localctx).admin = match(DEC);
				setState(82);
				match(FORWARD_SLASH);
				setState(83);
				((RouteContext)_localctx).cost = match(DEC);
				setState(84);
				match(BRACKET_RIGHT);
				setState(91); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(85);
					match(VIA);
					setState(86);
					((RouteContext)_localctx).IP_ADDRESS = match(IP_ADDRESS);
					((RouteContext)_localctx).nexthops.add(((RouteContext)_localctx).IP_ADDRESS);
					setState(87);
					match(COMMA);
					setState(88);
					((RouteContext)_localctx).identifier = identifier();
					((RouteContext)_localctx).nexthopifaces.add(((RouteContext)_localctx).identifier);
					setState(89);
					match(NEWLINE);
					}
					}
					setState(93); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==VIA );
				}
				}
				break;
			case IS_DIRECTLY_CONNECTED:
				{
				{
				setState(95);
				match(IS_DIRECTLY_CONNECTED);
				setState(96);
				match(COMMA);
				setState(97);
				((RouteContext)_localctx).identifier = identifier();
				((RouteContext)_localctx).nexthopifaces.add(((RouteContext)_localctx).identifier);
				setState(98);
				match(NEWLINE);
				}
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

	public static class Vrf_declarationContext extends ParserRuleContext {
		public TerminalNode VRF() { return getToken(EosRoutingTableParser.VRF, 0); }
		public TerminalNode NAME() { return getToken(EosRoutingTableParser.NAME, 0); }
		public TerminalNode COLON() { return getToken(EosRoutingTableParser.COLON, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(EosRoutingTableParser.NEWLINE, 0); }
		public Vrf_declarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vrf_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).enterVrf_declaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).exitVrf_declaration(this);
		}
	}

	public final Vrf_declarationContext vrf_declaration() throws RecognitionException {
		Vrf_declarationContext _localctx = new Vrf_declarationContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_vrf_declaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			match(VRF);
			setState(103);
			match(NAME);
			setState(104);
			match(COLON);
			setState(105);
			identifier();
			setState(106);
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
		public Codes_declarationContext codes_declaration() {
			return getRuleContext(Codes_declarationContext.class,0);
		}
		public Vrf_declarationContext vrf_declaration() {
			return getRuleContext(Vrf_declarationContext.class,0);
		}
		public Gateway_headerContext gateway_header() {
			return getRuleContext(Gateway_headerContext.class,0);
		}
		public List<RouteContext> route() {
			return getRuleContexts(RouteContext.class);
		}
		public RouteContext route(int i) {
			return getRuleContext(RouteContext.class,i);
		}
		public Vrf_routing_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vrf_routing_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).enterVrf_routing_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof EosRoutingTableParserListener ) ((EosRoutingTableParserListener)listener).exitVrf_routing_table(this);
		}
	}

	public final Vrf_routing_tableContext vrf_routing_table() throws RecognitionException {
		Vrf_routing_tableContext _localctx = new Vrf_routing_tableContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_vrf_routing_table);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VRF) {
				{
				setState(108);
				vrf_declaration();
				}
			}

			setState(111);
			codes_declaration();
			setState(113);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GATEWAY) {
				{
				setState(112);
				gateway_header();
				}
			}

			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WORD) {
				{
				{
				setState(115);
				route();
				}
				}
				setState(120);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\25|\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\6\2\26"+
		"\n\2\r\2\16\2\27\3\2\3\2\3\2\6\2\35\n\2\r\2\16\2\36\3\3\3\3\3\3\3\3\3"+
		"\3\5\3&\n\3\3\3\6\3)\n\3\r\3\16\3*\3\3\3\3\3\4\5\4\60\n\4\3\4\6\4\63\n"+
		"\4\r\4\16\4\64\3\4\3\4\3\5\3\5\6\5;\n\5\r\5\16\5<\3\5\7\5@\n\5\f\5\16"+
		"\5C\13\5\3\5\3\5\3\6\6\6H\n\6\r\6\16\6I\3\7\6\7M\n\7\r\7\16\7N\3\b\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\6\b^\n\b\r\b\16\b_\3\b\3"+
		"\b\3\b\3\b\3\b\5\bg\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\n\5\np\n\n\3\n\3\n\5"+
		"\nt\n\n\3\n\7\nw\n\n\f\n\16\nz\13\n\3\n\2\2\13\2\4\6\b\n\f\16\20\22\2"+
		"\3\3\2\23\23\2\u0082\2\25\3\2\2\2\4 \3\2\2\2\6/\3\2\2\2\b8\3\2\2\2\nG"+
		"\3\2\2\2\fL\3\2\2\2\16P\3\2\2\2\20h\3\2\2\2\22o\3\2\2\2\24\26\7\24\2\2"+
		"\25\24\3\2\2\2\26\27\3\2\2\2\27\25\3\2\2\2\27\30\3\2\2\2\30\31\3\2\2\2"+
		"\31\34\7\16\2\2\32\35\7\24\2\2\33\35\7\17\2\2\34\32\3\2\2\2\34\33\3\2"+
		"\2\2\35\36\3\2\2\2\36\34\3\2\2\2\36\37\3\2\2\2\37\3\3\2\2\2 !\7\3\2\2"+
		"!\"\7\13\2\2\"(\5\2\2\2#%\7\f\2\2$&\7\23\2\2%$\3\2\2\2%&\3\2\2\2&\'\3"+
		"\2\2\2\')\5\2\2\2(#\3\2\2\2)*\3\2\2\2*(\3\2\2\2*+\3\2\2\2+,\3\2\2\2,-"+
		"\7\23\2\2-\5\3\2\2\2.\60\7\23\2\2/.\3\2\2\2/\60\3\2\2\2\60\62\3\2\2\2"+
		"\61\63\5\22\n\2\62\61\3\2\2\2\63\64\3\2\2\2\64\62\3\2\2\2\64\65\3\2\2"+
		"\2\65\66\3\2\2\2\66\67\7\2\2\3\67\7\3\2\2\28:\7\4\2\29;\7\24\2\2:9\3\2"+
		"\2\2;<\3\2\2\2<:\3\2\2\2<=\3\2\2\2=A\3\2\2\2>@\n\2\2\2?>\3\2\2\2@C\3\2"+
		"\2\2A?\3\2\2\2AB\3\2\2\2BD\3\2\2\2CA\3\2\2\2DE\7\23\2\2E\t\3\2\2\2FH\n"+
		"\2\2\2GF\3\2\2\2HI\3\2\2\2IG\3\2\2\2IJ\3\2\2\2J\13\3\2\2\2KM\7\24\2\2"+
		"LK\3\2\2\2MN\3\2\2\2NL\3\2\2\2NO\3\2\2\2O\r\3\2\2\2PQ\5\f\7\2Qf\7\22\2"+
		"\2RS\7\t\2\2ST\7\17\2\2TU\7\20\2\2UV\7\17\2\2V]\7\n\2\2WX\7\7\2\2XY\7"+
		"\21\2\2YZ\7\f\2\2Z[\5\n\6\2[\\\7\23\2\2\\^\3\2\2\2]W\3\2\2\2^_\3\2\2\2"+
		"_]\3\2\2\2_`\3\2\2\2`g\3\2\2\2ab\7\5\2\2bc\7\f\2\2cd\5\n\6\2de\7\23\2"+
		"\2eg\3\2\2\2fR\3\2\2\2fa\3\2\2\2g\17\3\2\2\2hi\7\b\2\2ij\7\6\2\2jk\7\13"+
		"\2\2kl\5\n\6\2lm\7\23\2\2m\21\3\2\2\2np\5\20\t\2on\3\2\2\2op\3\2\2\2p"+
		"q\3\2\2\2qs\5\4\3\2rt\5\b\5\2sr\3\2\2\2st\3\2\2\2tx\3\2\2\2uw\5\16\b\2"+
		"vu\3\2\2\2wz\3\2\2\2xv\3\2\2\2xy\3\2\2\2y\23\3\2\2\2zx\3\2\2\2\22\27\34"+
		"\36%*/\64<AIN_fosx";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}