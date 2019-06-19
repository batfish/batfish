// Generated from org/batfish/grammar/routing_table/ios/IosRoutingTableParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.routing_table.ios;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class IosRoutingTableParser extends org.batfish.grammar.BatfishParser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		CODES=1, GATEWAY=2, IS_DIRECTLY_CONNECTED=3, IS_VARIABLY_SUBNETTED=4, 
		NAME=5, VIA=6, VRF=7, ASTERISK=8, BRACKET_LEFT=9, BRACKET_RIGHT=10, COLON=11, 
		COMMA=12, COMMENT=13, DASH=14, PERCENT=15, PLUS=16, DEC=17, FORWARD_SLASH=18, 
		IP_ADDRESS=19, IP_PREFIX=20, NEWLINE=21, WORD=22, WS=23;
	public static final int
		RULE_code = 0, RULE_codes_declaration = 1, RULE_code_line = 2, RULE_ios_routing_table = 3, 
		RULE_gateway_header = 4, RULE_identifier = 5, RULE_info = 6, RULE_protocol = 7, 
		RULE_route = 8, RULE_time = 9, RULE_vrf_declaration = 10, RULE_vrf_routing_table = 11;
	private static String[] makeRuleNames() {
		return new String[] {
			"code", "codes_declaration", "code_line", "ios_routing_table", "gateway_header", 
			"identifier", "info", "protocol", "route", "time", "vrf_declaration", 
			"vrf_routing_table"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'Codes'", "'Gateway'", "'is directly connected'", "'is variably subnetted'", 
			"'name'", "'via'", "'VRF'", "'*'", "'['", "']'", "':'", "','", null, 
			"'-'", "'%'", "'+'", null, "'/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "CODES", "GATEWAY", "IS_DIRECTLY_CONNECTED", "IS_VARIABLY_SUBNETTED", 
			"NAME", "VIA", "VRF", "ASTERISK", "BRACKET_LEFT", "BRACKET_RIGHT", "COLON", 
			"COMMA", "COMMENT", "DASH", "PERCENT", "PLUS", "DEC", "FORWARD_SLASH", 
			"IP_ADDRESS", "IP_PREFIX", "NEWLINE", "WORD", "WS"
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
	public String getGrammarFileName() { return "IosRoutingTableParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }




	public IosRoutingTableParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class CodeContext extends ParserRuleContext {
		public Token ASTERISK;
		public List<Token> code_parts = new ArrayList<Token>();
		public Token PERCENT;
		public Token PLUS;
		public Token WORD;
		public Token _tset40;
		public List<Token> description = new ArrayList<Token>();
		public Token DEC;
		public TerminalNode DASH() { return getToken(IosRoutingTableParser.DASH, 0); }
		public List<TerminalNode> WORD() { return getTokens(IosRoutingTableParser.WORD); }
		public TerminalNode WORD(int i) {
			return getToken(IosRoutingTableParser.WORD, i);
		}
		public List<TerminalNode> DEC() { return getTokens(IosRoutingTableParser.DEC); }
		public TerminalNode DEC(int i) {
			return getToken(IosRoutingTableParser.DEC, i);
		}
		public List<TerminalNode> ASTERISK() { return getTokens(IosRoutingTableParser.ASTERISK); }
		public TerminalNode ASTERISK(int i) {
			return getToken(IosRoutingTableParser.ASTERISK, i);
		}
		public List<TerminalNode> PERCENT() { return getTokens(IosRoutingTableParser.PERCENT); }
		public TerminalNode PERCENT(int i) {
			return getToken(IosRoutingTableParser.PERCENT, i);
		}
		public List<TerminalNode> PLUS() { return getTokens(IosRoutingTableParser.PLUS); }
		public TerminalNode PLUS(int i) {
			return getToken(IosRoutingTableParser.PLUS, i);
		}
		public CodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_code; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterCode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitCode(this);
		}
	}

	public final CodeContext code() throws RecognitionException {
		CodeContext _localctx = new CodeContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_code);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(24);
				((CodeContext)_localctx)._tset40 = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ASTERISK) | (1L << PERCENT) | (1L << PLUS) | (1L << WORD))) != 0)) ) {
					((CodeContext)_localctx)._tset40 = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				((CodeContext)_localctx).code_parts.add(((CodeContext)_localctx)._tset40);
				}
				}
				setState(27); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ASTERISK) | (1L << PERCENT) | (1L << PLUS) | (1L << WORD))) != 0) );
			setState(29);
			match(DASH);
			setState(32); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(32);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case WORD:
					{
					setState(30);
					((CodeContext)_localctx).WORD = match(WORD);
					((CodeContext)_localctx).description.add(((CodeContext)_localctx).WORD);
					}
					break;
				case DEC:
					{
					setState(31);
					((CodeContext)_localctx).DEC = match(DEC);
					((CodeContext)_localctx).description.add(((CodeContext)_localctx).DEC);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(34); 
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
		public TerminalNode CODES() { return getToken(IosRoutingTableParser.CODES, 0); }
		public TerminalNode COLON() { return getToken(IosRoutingTableParser.COLON, 0); }
		public List<Code_lineContext> code_line() {
			return getRuleContexts(Code_lineContext.class);
		}
		public Code_lineContext code_line(int i) {
			return getRuleContext(Code_lineContext.class,i);
		}
		public Codes_declarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_codes_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterCodes_declaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitCodes_declaration(this);
		}
	}

	public final Codes_declarationContext codes_declaration() throws RecognitionException {
		Codes_declarationContext _localctx = new Codes_declarationContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_codes_declaration);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			match(CODES);
			setState(37);
			match(COLON);
			setState(39); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(38);
					code_line();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(41); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Code_lineContext extends ParserRuleContext {
		public List<CodeContext> code() {
			return getRuleContexts(CodeContext.class);
		}
		public CodeContext code(int i) {
			return getRuleContext(CodeContext.class,i);
		}
		public TerminalNode NEWLINE() { return getToken(IosRoutingTableParser.NEWLINE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(IosRoutingTableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IosRoutingTableParser.COMMA, i);
		}
		public Code_lineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_code_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterCode_line(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitCode_line(this);
		}
	}

	public final Code_lineContext code_line() throws RecognitionException {
		Code_lineContext _localctx = new Code_lineContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_code_line);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(46); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(43);
					code();
					setState(44);
					match(COMMA);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(48); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(50);
			code();
			setState(51);
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

	public static class Ios_routing_tableContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(IosRoutingTableParser.EOF, 0); }
		public TerminalNode NEWLINE() { return getToken(IosRoutingTableParser.NEWLINE, 0); }
		public List<Vrf_routing_tableContext> vrf_routing_table() {
			return getRuleContexts(Vrf_routing_tableContext.class);
		}
		public Vrf_routing_tableContext vrf_routing_table(int i) {
			return getRuleContext(Vrf_routing_tableContext.class,i);
		}
		public Ios_routing_tableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ios_routing_table; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterIos_routing_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitIos_routing_table(this);
		}
	}

	public final Ios_routing_tableContext ios_routing_table() throws RecognitionException {
		Ios_routing_tableContext _localctx = new Ios_routing_tableContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_ios_routing_table);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(54);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NEWLINE) {
				{
				setState(53);
				match(NEWLINE);
				}
			}

			setState(57); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(56);
				vrf_routing_table();
				}
				}
				setState(59); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==CODES || _la==VRF );
			setState(61);
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
		public TerminalNode GATEWAY() { return getToken(IosRoutingTableParser.GATEWAY, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(IosRoutingTableParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(IosRoutingTableParser.NEWLINE, i);
		}
		public List<TerminalNode> WORD() { return getTokens(IosRoutingTableParser.WORD); }
		public TerminalNode WORD(int i) {
			return getToken(IosRoutingTableParser.WORD, i);
		}
		public Gateway_headerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gateway_header; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterGateway_header(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitGateway_header(this);
		}
	}

	public final Gateway_headerContext gateway_header() throws RecognitionException {
		Gateway_headerContext _localctx = new Gateway_headerContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_gateway_header);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			match(GATEWAY);
			setState(65); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(64);
					match(WORD);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(67); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(72);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CODES) | (1L << GATEWAY) | (1L << IS_DIRECTLY_CONNECTED) | (1L << IS_VARIABLY_SUBNETTED) | (1L << NAME) | (1L << VIA) | (1L << VRF) | (1L << ASTERISK) | (1L << BRACKET_LEFT) | (1L << BRACKET_RIGHT) | (1L << COLON) | (1L << COMMA) | (1L << COMMENT) | (1L << DASH) | (1L << PERCENT) | (1L << PLUS) | (1L << DEC) | (1L << FORWARD_SLASH) | (1L << IP_ADDRESS) | (1L << IP_PREFIX) | (1L << WORD) | (1L << WS))) != 0)) {
				{
				{
				setState(69);
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
				setState(74);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(75);
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
		public List<TerminalNode> NEWLINE() { return getTokens(IosRoutingTableParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(IosRoutingTableParser.NEWLINE, i);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitIdentifier(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_identifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(77);
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
				setState(80); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CODES) | (1L << GATEWAY) | (1L << IS_DIRECTLY_CONNECTED) | (1L << IS_VARIABLY_SUBNETTED) | (1L << NAME) | (1L << VIA) | (1L << VRF) | (1L << ASTERISK) | (1L << BRACKET_LEFT) | (1L << BRACKET_RIGHT) | (1L << COLON) | (1L << COMMA) | (1L << COMMENT) | (1L << DASH) | (1L << PERCENT) | (1L << PLUS) | (1L << DEC) | (1L << FORWARD_SLASH) | (1L << IP_ADDRESS) | (1L << IP_PREFIX) | (1L << WORD) | (1L << WS))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class InfoContext extends ParserRuleContext {
		public TerminalNode IP_PREFIX() { return getToken(IosRoutingTableParser.IP_PREFIX, 0); }
		public TerminalNode IS_VARIABLY_SUBNETTED() { return getToken(IosRoutingTableParser.IS_VARIABLY_SUBNETTED, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(IosRoutingTableParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(IosRoutingTableParser.NEWLINE, i);
		}
		public InfoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_info; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterInfo(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitInfo(this);
		}
	}

	public final InfoContext info() throws RecognitionException {
		InfoContext _localctx = new InfoContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_info);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			match(IP_PREFIX);
			setState(83);
			match(IS_VARIABLY_SUBNETTED);
			setState(87);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CODES) | (1L << GATEWAY) | (1L << IS_DIRECTLY_CONNECTED) | (1L << IS_VARIABLY_SUBNETTED) | (1L << NAME) | (1L << VIA) | (1L << VRF) | (1L << ASTERISK) | (1L << BRACKET_LEFT) | (1L << BRACKET_RIGHT) | (1L << COLON) | (1L << COMMA) | (1L << COMMENT) | (1L << DASH) | (1L << PERCENT) | (1L << PLUS) | (1L << DEC) | (1L << FORWARD_SLASH) | (1L << IP_ADDRESS) | (1L << IP_PREFIX) | (1L << WORD) | (1L << WS))) != 0)) {
				{
				{
				setState(84);
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
				setState(89);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	public static class ProtocolContext extends ParserRuleContext {
		public List<TerminalNode> WORD() { return getTokens(IosRoutingTableParser.WORD); }
		public TerminalNode WORD(int i) {
			return getToken(IosRoutingTableParser.WORD, i);
		}
		public ProtocolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_protocol; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterProtocol(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitProtocol(this);
		}
	}

	public final ProtocolContext protocol() throws RecognitionException {
		ProtocolContext _localctx = new ProtocolContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_protocol);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(92);
				match(WORD);
				}
				}
				setState(95); 
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
		public TerminalNode IP_PREFIX() { return getToken(IosRoutingTableParser.IP_PREFIX, 0); }
		public TerminalNode IS_DIRECTLY_CONNECTED() { return getToken(IosRoutingTableParser.IS_DIRECTLY_CONNECTED, 0); }
		public List<TerminalNode> COMMA() { return getTokens(IosRoutingTableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IosRoutingTableParser.COMMA, i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(IosRoutingTableParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(IosRoutingTableParser.NEWLINE, i);
		}
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public List<TerminalNode> BRACKET_LEFT() { return getTokens(IosRoutingTableParser.BRACKET_LEFT); }
		public TerminalNode BRACKET_LEFT(int i) {
			return getToken(IosRoutingTableParser.BRACKET_LEFT, i);
		}
		public List<TerminalNode> FORWARD_SLASH() { return getTokens(IosRoutingTableParser.FORWARD_SLASH); }
		public TerminalNode FORWARD_SLASH(int i) {
			return getToken(IosRoutingTableParser.FORWARD_SLASH, i);
		}
		public List<TerminalNode> BRACKET_RIGHT() { return getTokens(IosRoutingTableParser.BRACKET_RIGHT); }
		public TerminalNode BRACKET_RIGHT(int i) {
			return getToken(IosRoutingTableParser.BRACKET_RIGHT, i);
		}
		public List<TerminalNode> VIA() { return getTokens(IosRoutingTableParser.VIA); }
		public TerminalNode VIA(int i) {
			return getToken(IosRoutingTableParser.VIA, i);
		}
		public List<TimeContext> time() {
			return getRuleContexts(TimeContext.class);
		}
		public TimeContext time(int i) {
			return getRuleContext(TimeContext.class,i);
		}
		public List<TerminalNode> DEC() { return getTokens(IosRoutingTableParser.DEC); }
		public TerminalNode DEC(int i) {
			return getToken(IosRoutingTableParser.DEC, i);
		}
		public List<TerminalNode> IP_ADDRESS() { return getTokens(IosRoutingTableParser.IP_ADDRESS); }
		public TerminalNode IP_ADDRESS(int i) {
			return getToken(IosRoutingTableParser.IP_ADDRESS, i);
		}
		public RouteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_route; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterRoute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitRoute(this);
		}
	}

	public final RouteContext route() throws RecognitionException {
		RouteContext _localctx = new RouteContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_route);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			protocol();
			setState(98);
			match(IP_PREFIX);
			setState(123);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BRACKET_LEFT:
				{
				{
				setState(114); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(99);
					match(BRACKET_LEFT);
					setState(100);
					((RouteContext)_localctx).admin = match(DEC);
					setState(101);
					match(FORWARD_SLASH);
					setState(102);
					((RouteContext)_localctx).cost = match(DEC);
					setState(103);
					match(BRACKET_RIGHT);
					setState(104);
					match(VIA);
					setState(105);
					((RouteContext)_localctx).IP_ADDRESS = match(IP_ADDRESS);
					((RouteContext)_localctx).nexthops.add(((RouteContext)_localctx).IP_ADDRESS);
					setState(106);
					match(COMMA);
					setState(107);
					time();
					setState(110);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(108);
						match(COMMA);
						setState(109);
						((RouteContext)_localctx).identifier = identifier();
						((RouteContext)_localctx).nexthopifaces.add(((RouteContext)_localctx).identifier);
						}
					}

					setState(112);
					match(NEWLINE);
					}
					}
					setState(116); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==BRACKET_LEFT );
				}
				}
				break;
			case IS_DIRECTLY_CONNECTED:
				{
				{
				setState(118);
				match(IS_DIRECTLY_CONNECTED);
				setState(119);
				match(COMMA);
				setState(120);
				((RouteContext)_localctx).identifier = identifier();
				((RouteContext)_localctx).nexthopifaces.add(((RouteContext)_localctx).identifier);
				setState(121);
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

	public static class TimeContext extends ParserRuleContext {
		public List<TerminalNode> COMMA() { return getTokens(IosRoutingTableParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IosRoutingTableParser.COMMA, i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(IosRoutingTableParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(IosRoutingTableParser.NEWLINE, i);
		}
		public TimeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_time; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterTime(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitTime(this);
		}
	}

	public final TimeContext time() throws RecognitionException {
		TimeContext _localctx = new TimeContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_time);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(128);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << CODES) | (1L << GATEWAY) | (1L << IS_DIRECTLY_CONNECTED) | (1L << IS_VARIABLY_SUBNETTED) | (1L << NAME) | (1L << VIA) | (1L << VRF) | (1L << ASTERISK) | (1L << BRACKET_LEFT) | (1L << BRACKET_RIGHT) | (1L << COLON) | (1L << COMMENT) | (1L << DASH) | (1L << PERCENT) | (1L << PLUS) | (1L << DEC) | (1L << FORWARD_SLASH) | (1L << IP_ADDRESS) | (1L << IP_PREFIX) | (1L << WORD) | (1L << WS))) != 0)) {
				{
				{
				setState(125);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==COMMA || _la==NEWLINE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(130);
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

	public static class Vrf_declarationContext extends ParserRuleContext {
		public TerminalNode VRF() { return getToken(IosRoutingTableParser.VRF, 0); }
		public TerminalNode NAME() { return getToken(IosRoutingTableParser.NAME, 0); }
		public TerminalNode COLON() { return getToken(IosRoutingTableParser.COLON, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(IosRoutingTableParser.NEWLINE, 0); }
		public Vrf_declarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vrf_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterVrf_declaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitVrf_declaration(this);
		}
	}

	public final Vrf_declarationContext vrf_declaration() throws RecognitionException {
		Vrf_declarationContext _localctx = new Vrf_declarationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_vrf_declaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			match(VRF);
			setState(132);
			match(NAME);
			setState(133);
			match(COLON);
			setState(134);
			identifier();
			setState(135);
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
		public List<InfoContext> info() {
			return getRuleContexts(InfoContext.class);
		}
		public InfoContext info(int i) {
			return getRuleContext(InfoContext.class,i);
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
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).enterVrf_routing_table(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IosRoutingTableParserListener ) ((IosRoutingTableParserListener)listener).exitVrf_routing_table(this);
		}
	}

	public final Vrf_routing_tableContext vrf_routing_table() throws RecognitionException {
		Vrf_routing_tableContext _localctx = new Vrf_routing_tableContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_vrf_routing_table);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==VRF) {
				{
				setState(137);
				vrf_declaration();
				}
			}

			setState(140);
			codes_declaration();
			setState(142);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GATEWAY) {
				{
				setState(141);
				gateway_header();
				}
			}

			setState(148);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IP_PREFIX || _la==WORD) {
				{
				setState(146);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case IP_PREFIX:
					{
					setState(144);
					info();
					}
					break;
				case WORD:
					{
					setState(145);
					route();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(150);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\31\u009a\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\3\2\6\2\34\n\2\r\2\16\2\35\3\2\3\2\3\2\6\2#\n\2"+
		"\r\2\16\2$\3\3\3\3\3\3\6\3*\n\3\r\3\16\3+\3\4\3\4\3\4\6\4\61\n\4\r\4\16"+
		"\4\62\3\4\3\4\3\4\3\5\5\59\n\5\3\5\6\5<\n\5\r\5\16\5=\3\5\3\5\3\6\3\6"+
		"\6\6D\n\6\r\6\16\6E\3\6\7\6I\n\6\f\6\16\6L\13\6\3\6\3\6\3\7\6\7Q\n\7\r"+
		"\7\16\7R\3\b\3\b\3\b\7\bX\n\b\f\b\16\b[\13\b\3\b\3\b\3\t\6\t`\n\t\r\t"+
		"\16\ta\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\nq\n\n\3"+
		"\n\3\n\6\nu\n\n\r\n\16\nv\3\n\3\n\3\n\3\n\3\n\5\n~\n\n\3\13\7\13\u0081"+
		"\n\13\f\13\16\13\u0084\13\13\3\f\3\f\3\f\3\f\3\f\3\f\3\r\5\r\u008d\n\r"+
		"\3\r\3\r\5\r\u0091\n\r\3\r\3\r\7\r\u0095\n\r\f\r\16\r\u0098\13\r\3\r\2"+
		"\2\16\2\4\6\b\n\f\16\20\22\24\26\30\2\5\5\2\n\n\21\22\30\30\3\2\27\27"+
		"\4\2\16\16\27\27\2\u00a1\2\33\3\2\2\2\4&\3\2\2\2\6\60\3\2\2\2\b8\3\2\2"+
		"\2\nA\3\2\2\2\fP\3\2\2\2\16T\3\2\2\2\20_\3\2\2\2\22c\3\2\2\2\24\u0082"+
		"\3\2\2\2\26\u0085\3\2\2\2\30\u008c\3\2\2\2\32\34\t\2\2\2\33\32\3\2\2\2"+
		"\34\35\3\2\2\2\35\33\3\2\2\2\35\36\3\2\2\2\36\37\3\2\2\2\37\"\7\20\2\2"+
		" #\7\30\2\2!#\7\23\2\2\" \3\2\2\2\"!\3\2\2\2#$\3\2\2\2$\"\3\2\2\2$%\3"+
		"\2\2\2%\3\3\2\2\2&\'\7\3\2\2\')\7\r\2\2(*\5\6\4\2)(\3\2\2\2*+\3\2\2\2"+
		"+)\3\2\2\2+,\3\2\2\2,\5\3\2\2\2-.\5\2\2\2./\7\16\2\2/\61\3\2\2\2\60-\3"+
		"\2\2\2\61\62\3\2\2\2\62\60\3\2\2\2\62\63\3\2\2\2\63\64\3\2\2\2\64\65\5"+
		"\2\2\2\65\66\7\27\2\2\66\7\3\2\2\2\679\7\27\2\28\67\3\2\2\289\3\2\2\2"+
		"9;\3\2\2\2:<\5\30\r\2;:\3\2\2\2<=\3\2\2\2=;\3\2\2\2=>\3\2\2\2>?\3\2\2"+
		"\2?@\7\2\2\3@\t\3\2\2\2AC\7\4\2\2BD\7\30\2\2CB\3\2\2\2DE\3\2\2\2EC\3\2"+
		"\2\2EF\3\2\2\2FJ\3\2\2\2GI\n\3\2\2HG\3\2\2\2IL\3\2\2\2JH\3\2\2\2JK\3\2"+
		"\2\2KM\3\2\2\2LJ\3\2\2\2MN\7\27\2\2N\13\3\2\2\2OQ\n\3\2\2PO\3\2\2\2QR"+
		"\3\2\2\2RP\3\2\2\2RS\3\2\2\2S\r\3\2\2\2TU\7\26\2\2UY\7\6\2\2VX\n\3\2\2"+
		"WV\3\2\2\2X[\3\2\2\2YW\3\2\2\2YZ\3\2\2\2Z\\\3\2\2\2[Y\3\2\2\2\\]\7\27"+
		"\2\2]\17\3\2\2\2^`\7\30\2\2_^\3\2\2\2`a\3\2\2\2a_\3\2\2\2ab\3\2\2\2b\21"+
		"\3\2\2\2cd\5\20\t\2d}\7\26\2\2ef\7\13\2\2fg\7\23\2\2gh\7\24\2\2hi\7\23"+
		"\2\2ij\7\f\2\2jk\7\b\2\2kl\7\25\2\2lm\7\16\2\2mp\5\24\13\2no\7\16\2\2"+
		"oq\5\f\7\2pn\3\2\2\2pq\3\2\2\2qr\3\2\2\2rs\7\27\2\2su\3\2\2\2te\3\2\2"+
		"\2uv\3\2\2\2vt\3\2\2\2vw\3\2\2\2w~\3\2\2\2xy\7\5\2\2yz\7\16\2\2z{\5\f"+
		"\7\2{|\7\27\2\2|~\3\2\2\2}t\3\2\2\2}x\3\2\2\2~\23\3\2\2\2\177\u0081\n"+
		"\4\2\2\u0080\177\3\2\2\2\u0081\u0084\3\2\2\2\u0082\u0080\3\2\2\2\u0082"+
		"\u0083\3\2\2\2\u0083\25\3\2\2\2\u0084\u0082\3\2\2\2\u0085\u0086\7\t\2"+
		"\2\u0086\u0087\7\7\2\2\u0087\u0088\7\r\2\2\u0088\u0089\5\f\7\2\u0089\u008a"+
		"\7\27\2\2\u008a\27\3\2\2\2\u008b\u008d\5\26\f\2\u008c\u008b\3\2\2\2\u008c"+
		"\u008d\3\2\2\2\u008d\u008e\3\2\2\2\u008e\u0090\5\4\3\2\u008f\u0091\5\n"+
		"\6\2\u0090\u008f\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u0096\3\2\2\2\u0092"+
		"\u0095\5\16\b\2\u0093\u0095\5\22\n\2\u0094\u0092\3\2\2\2\u0094\u0093\3"+
		"\2\2\2\u0095\u0098\3\2\2\2\u0096\u0094\3\2\2\2\u0096\u0097\3\2\2\2\u0097"+
		"\31\3\2\2\2\u0098\u0096\3\2\2\2\26\35\"$+\628=EJRYapv}\u0082\u008c\u0090"+
		"\u0094\u0096";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}