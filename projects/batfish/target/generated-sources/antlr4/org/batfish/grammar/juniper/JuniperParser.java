// Generated from org/batfish/grammar/juniper/JuniperParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.juniper;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JuniperParser extends org.batfish.grammar.BatfishParser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		REPLACE=1, CLOSE_BRACE=2, CLOSE_BRACKET=3, CLOSE_PAREN=4, INACTIVE=5, 
		LINE_COMMENT=6, MULTILINE_COMMENT=7, OPEN_BRACE=8, OPEN_BRACKET=9, OPEN_PAREN=10, 
		SEMICOLON=11, WORD=12, WS=13;
	public static final int
		RULE_braced_clause = 0, RULE_bracketed_clause = 1, RULE_juniper_configuration = 2, 
		RULE_statement = 3, RULE_terminator = 4, RULE_word = 5;
	private static String[] makeRuleNames() {
		return new String[] {
			"braced_clause", "bracketed_clause", "juniper_configuration", "statement", 
			"terminator", "word"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'replace:'", "'}'", "']'", "')'", "'inactive:'", null, null, "'{'", 
			"'['", "'('", "';'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "REPLACE", "CLOSE_BRACE", "CLOSE_BRACKET", "CLOSE_PAREN", "INACTIVE", 
			"LINE_COMMENT", "MULTILINE_COMMENT", "OPEN_BRACE", "OPEN_BRACKET", "OPEN_PAREN", 
			"SEMICOLON", "WORD", "WS"
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
	public String getGrammarFileName() { return "JuniperParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public JuniperParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class Braced_clauseContext extends ParserRuleContext {
		public TerminalNode OPEN_BRACE() { return getToken(JuniperParser.OPEN_BRACE, 0); }
		public TerminalNode CLOSE_BRACE() { return getToken(JuniperParser.CLOSE_BRACE, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public Braced_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_braced_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).enterBraced_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).exitBraced_clause(this);
		}
	}

	public final Braced_clauseContext braced_clause() throws RecognitionException {
		Braced_clauseContext _localctx = new Braced_clauseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_braced_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(12);
			match(OPEN_BRACE);
			setState(16);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << REPLACE) | (1L << INACTIVE) | (1L << WORD))) != 0)) {
				{
				{
				setState(13);
				statement();
				}
				}
				setState(18);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(19);
			match(CLOSE_BRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Bracketed_clauseContext extends ParserRuleContext {
		public TerminalNode OPEN_BRACKET() { return getToken(JuniperParser.OPEN_BRACKET, 0); }
		public TerminalNode CLOSE_BRACKET() { return getToken(JuniperParser.CLOSE_BRACKET, 0); }
		public List<WordContext> word() {
			return getRuleContexts(WordContext.class);
		}
		public WordContext word(int i) {
			return getRuleContext(WordContext.class,i);
		}
		public Bracketed_clauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bracketed_clause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).enterBracketed_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).exitBracketed_clause(this);
		}
	}

	public final Bracketed_clauseContext bracketed_clause() throws RecognitionException {
		Bracketed_clauseContext _localctx = new Bracketed_clauseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_bracketed_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			match(OPEN_BRACKET);
			setState(23); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(22);
				word();
				}
				}
				setState(25); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WORD );
			setState(27);
			match(CLOSE_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Juniper_configurationContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(JuniperParser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public Juniper_configurationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_juniper_configuration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).enterJuniper_configuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).exitJuniper_configuration(this);
		}
	}

	public final Juniper_configurationContext juniper_configuration() throws RecognitionException {
		Juniper_configurationContext _localctx = new Juniper_configurationContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_juniper_configuration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(29);
				statement();
				}
				}
				setState(32); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << REPLACE) | (1L << INACTIVE) | (1L << WORD))) != 0) );
			setState(34);
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
		public WordContext word;
		public List<WordContext> words = new ArrayList<WordContext>();
		public Braced_clauseContext braced_clause() {
			return getRuleContext(Braced_clauseContext.class,0);
		}
		public TerminatorContext terminator() {
			return getRuleContext(TerminatorContext.class,0);
		}
		public TerminalNode INACTIVE() { return getToken(JuniperParser.INACTIVE, 0); }
		public TerminalNode REPLACE() { return getToken(JuniperParser.REPLACE, 0); }
		public List<WordContext> word() {
			return getRuleContexts(WordContext.class);
		}
		public WordContext word(int i) {
			return getRuleContext(WordContext.class,i);
		}
		public Bracketed_clauseContext bracketed_clause() {
			return getRuleContext(Bracketed_clauseContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==REPLACE || _la==INACTIVE) {
				{
				setState(36);
				_la = _input.LA(1);
				if ( !(_la==REPLACE || _la==INACTIVE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(40); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(39);
				((StatementContext)_localctx).word = word();
				((StatementContext)_localctx).words.add(((StatementContext)_localctx).word);
				}
				}
				setState(42); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WORD );
			setState(49);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OPEN_BRACE:
				{
				setState(44);
				braced_clause();
				}
				break;
			case OPEN_BRACKET:
				{
				{
				setState(45);
				bracketed_clause();
				setState(46);
				terminator();
				}
				}
				break;
			case SEMICOLON:
				{
				setState(48);
				terminator();
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

	public static class TerminatorContext extends ParserRuleContext {
		public TerminalNode SEMICOLON() { return getToken(JuniperParser.SEMICOLON, 0); }
		public TerminatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_terminator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).enterTerminator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).exitTerminator(this);
		}
	}

	public final TerminatorContext terminator() throws RecognitionException {
		TerminatorContext _localctx = new TerminatorContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_terminator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			match(SEMICOLON);
			}
		}
		catch (RecognitionException re) {
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
		public TerminalNode WORD() { return getToken(JuniperParser.WORD, 0); }
		public WordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).enterWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof JuniperParserListener ) ((JuniperParserListener)listener).exitWord(this);
		}
	}

	public final WordContext word() throws RecognitionException {
		WordContext _localctx = new WordContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_word);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			match(WORD);
			}
		}
		catch (RecognitionException re) {
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\17:\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\7\2\21\n\2\f\2\16\2\24\13\2"+
		"\3\2\3\2\3\3\3\3\6\3\32\n\3\r\3\16\3\33\3\3\3\3\3\4\6\4!\n\4\r\4\16\4"+
		"\"\3\4\3\4\3\5\5\5(\n\5\3\5\6\5+\n\5\r\5\16\5,\3\5\3\5\3\5\3\5\3\5\5\5"+
		"\64\n\5\3\6\3\6\3\7\3\7\3\7\2\2\b\2\4\6\b\n\f\2\3\4\2\3\3\7\7\2:\2\16"+
		"\3\2\2\2\4\27\3\2\2\2\6 \3\2\2\2\b\'\3\2\2\2\n\65\3\2\2\2\f\67\3\2\2\2"+
		"\16\22\7\n\2\2\17\21\5\b\5\2\20\17\3\2\2\2\21\24\3\2\2\2\22\20\3\2\2\2"+
		"\22\23\3\2\2\2\23\25\3\2\2\2\24\22\3\2\2\2\25\26\7\4\2\2\26\3\3\2\2\2"+
		"\27\31\7\13\2\2\30\32\5\f\7\2\31\30\3\2\2\2\32\33\3\2\2\2\33\31\3\2\2"+
		"\2\33\34\3\2\2\2\34\35\3\2\2\2\35\36\7\5\2\2\36\5\3\2\2\2\37!\5\b\5\2"+
		" \37\3\2\2\2!\"\3\2\2\2\" \3\2\2\2\"#\3\2\2\2#$\3\2\2\2$%\7\2\2\3%\7\3"+
		"\2\2\2&(\t\2\2\2\'&\3\2\2\2\'(\3\2\2\2(*\3\2\2\2)+\5\f\7\2*)\3\2\2\2+"+
		",\3\2\2\2,*\3\2\2\2,-\3\2\2\2-\63\3\2\2\2.\64\5\2\2\2/\60\5\4\3\2\60\61"+
		"\5\n\6\2\61\64\3\2\2\2\62\64\5\n\6\2\63.\3\2\2\2\63/\3\2\2\2\63\62\3\2"+
		"\2\2\64\t\3\2\2\2\65\66\7\r\2\2\66\13\3\2\2\2\678\7\16\2\28\r\3\2\2\2"+
		"\b\22\33\"\',\63";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}