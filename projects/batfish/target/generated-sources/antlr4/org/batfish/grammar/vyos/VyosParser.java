// Generated from org/batfish/grammar/vyos/VyosParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.vyos;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class VyosParser extends org.batfish.grammar.BatfishParser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		CLOSE_BRACE=1, CLOSE_PAREN=2, LINE_COMMENT=3, MULTILINE_COMMENT=4, NEWLINE=5, 
		OPEN_BRACE=6, OPEN_PAREN=7, SEMICOLON=8, WORD=9, WS=10;
	public static final int
		RULE_braced_clause = 0, RULE_statement = 1, RULE_terminator = 2, RULE_vyos_configuration = 3, 
		RULE_word = 4;
	private static String[] makeRuleNames() {
		return new String[] {
			"braced_clause", "statement", "terminator", "vyos_configuration", "word"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'}'", "')'", null, null, null, "'{'", "'('", "';'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "CLOSE_BRACE", "CLOSE_PAREN", "LINE_COMMENT", "MULTILINE_COMMENT", 
			"NEWLINE", "OPEN_BRACE", "OPEN_PAREN", "SEMICOLON", "WORD", "WS"
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
	public String getGrammarFileName() { return "VyosParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public VyosParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class Braced_clauseContext extends ParserRuleContext {
		public TerminalNode OPEN_BRACE() { return getToken(VyosParser.OPEN_BRACE, 0); }
		public List<TerminalNode> NEWLINE() { return getTokens(VyosParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(VyosParser.NEWLINE, i);
		}
		public TerminalNode CLOSE_BRACE() { return getToken(VyosParser.CLOSE_BRACE, 0); }
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
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).enterBraced_clause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).exitBraced_clause(this);
		}
	}

	public final Braced_clauseContext braced_clause() throws RecognitionException {
		Braced_clauseContext _localctx = new Braced_clauseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_braced_clause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(10);
			match(OPEN_BRACE);
			setState(11);
			match(NEWLINE);
			setState(15);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==WORD) {
				{
				{
				setState(12);
				statement();
				}
				}
				setState(17);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(18);
			match(CLOSE_BRACE);
			setState(19);
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

	public static class StatementContext extends ParserRuleContext {
		public Braced_clauseContext braced_clause() {
			return getRuleContext(Braced_clauseContext.class,0);
		}
		public TerminatorContext terminator() {
			return getRuleContext(TerminatorContext.class,0);
		}
		public List<WordContext> word() {
			return getRuleContexts(WordContext.class);
		}
		public WordContext word(int i) {
			return getRuleContext(WordContext.class,i);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(22); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(21);
				word();
				}
				}
				setState(24); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WORD );
			setState(28);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case OPEN_BRACE:
				{
				setState(26);
				braced_clause();
				}
				break;
			case NEWLINE:
				{
				setState(27);
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
		public TerminalNode NEWLINE() { return getToken(VyosParser.NEWLINE, 0); }
		public TerminatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_terminator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).enterTerminator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).exitTerminator(this);
		}
	}

	public final TerminatorContext terminator() throws RecognitionException {
		TerminatorContext _localctx = new TerminatorContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_terminator);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30);
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

	public static class Vyos_configurationContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(VyosParser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public Vyos_configurationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vyos_configuration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).enterVyos_configuration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).exitVyos_configuration(this);
		}
	}

	public final Vyos_configurationContext vyos_configuration() throws RecognitionException {
		Vyos_configurationContext _localctx = new Vyos_configurationContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_vyos_configuration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(33); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(32);
				statement();
				}
				}
				setState(35); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WORD );
			setState(37);
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

	public static class WordContext extends ParserRuleContext {
		public TerminalNode WORD() { return getToken(VyosParser.WORD, 0); }
		public WordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).enterWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VyosParserListener ) ((VyosParserListener)listener).exitWord(this);
		}
	}

	public final WordContext word() throws RecognitionException {
		WordContext _localctx = new WordContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_word);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(39);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\f,\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\2\7\2\20\n\2\f\2\16\2\23\13\2\3\2"+
		"\3\2\3\2\3\3\6\3\31\n\3\r\3\16\3\32\3\3\3\3\5\3\37\n\3\3\4\3\4\3\5\6\5"+
		"$\n\5\r\5\16\5%\3\5\3\5\3\6\3\6\3\6\2\2\7\2\4\6\b\n\2\2\2*\2\f\3\2\2\2"+
		"\4\30\3\2\2\2\6 \3\2\2\2\b#\3\2\2\2\n)\3\2\2\2\f\r\7\b\2\2\r\21\7\7\2"+
		"\2\16\20\5\4\3\2\17\16\3\2\2\2\20\23\3\2\2\2\21\17\3\2\2\2\21\22\3\2\2"+
		"\2\22\24\3\2\2\2\23\21\3\2\2\2\24\25\7\3\2\2\25\26\7\7\2\2\26\3\3\2\2"+
		"\2\27\31\5\n\6\2\30\27\3\2\2\2\31\32\3\2\2\2\32\30\3\2\2\2\32\33\3\2\2"+
		"\2\33\36\3\2\2\2\34\37\5\2\2\2\35\37\5\6\4\2\36\34\3\2\2\2\36\35\3\2\2"+
		"\2\37\5\3\2\2\2 !\7\7\2\2!\7\3\2\2\2\"$\5\4\3\2#\"\3\2\2\2$%\3\2\2\2%"+
		"#\3\2\2\2%&\3\2\2\2&\'\3\2\2\2\'(\7\2\2\3(\t\3\2\2\2)*\7\13\2\2*\13\3"+
		"\2\2\2\6\21\32\36%";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}