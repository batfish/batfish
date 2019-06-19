// Generated from org/batfish/grammar/recovery/RecoveryParser.g4 by ANTLR 4.7.2
package org.batfish.grammar.recovery;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class RecoveryParser extends org.batfish.grammar.BatfishParser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		BLOCK=1, INNER=2, OTHER=3, SIMPLE=4, BLANK_LINE=5, COMMENT_LINE=6, COMMENT_TAIL=7, 
		NEWLINE=8, WS=9, ENTER_BAD_MODE=10, M_BadMode_nonexistent=11;
	public static final int
		RULE_block_statement = 0, RULE_inner_statement = 1, RULE_statement = 2, 
		RULE_recovery = 3, RULE_simple_statement = 4, RULE_tail_word = 5;
	private static String[] makeRuleNames() {
		return new String[] {
			"block_statement", "inner_statement", "statement", "recovery", "simple_statement", 
			"tail_word"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'block'", "'inner'", "'other'", "'simple'", null, null, null, 
			null, null, "'enter-bad-mode'", "'nonexistent'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "BLOCK", "INNER", "OTHER", "SIMPLE", "BLANK_LINE", "COMMENT_LINE", 
			"COMMENT_TAIL", "NEWLINE", "WS", "ENTER_BAD_MODE", "M_BadMode_nonexistent"
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
	public String getGrammarFileName() { return "RecoveryParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public RecoveryParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class Block_statementContext extends ParserRuleContext {
		public TerminalNode BLOCK() { return getToken(RecoveryParser.BLOCK, 0); }
		public TerminalNode NEWLINE() { return getToken(RecoveryParser.NEWLINE, 0); }
		public List<Tail_wordContext> tail_word() {
			return getRuleContexts(Tail_wordContext.class);
		}
		public Tail_wordContext tail_word(int i) {
			return getRuleContext(Tail_wordContext.class,i);
		}
		public List<Inner_statementContext> inner_statement() {
			return getRuleContexts(Inner_statementContext.class);
		}
		public Inner_statementContext inner_statement(int i) {
			return getRuleContext(Inner_statementContext.class,i);
		}
		public Block_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).enterBlock_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).exitBlock_statement(this);
		}
	}

	public final Block_statementContext block_statement() throws RecognitionException {
		Block_statementContext _localctx = new Block_statementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_block_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(12);
			match(BLOCK);
			setState(16);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BLOCK) | (1L << INNER) | (1L << SIMPLE))) != 0)) {
				{
				{
				setState(13);
				tail_word();
				}
				}
				setState(18);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(19);
			match(NEWLINE);
			setState(23);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==INNER) {
				{
				{
				setState(20);
				inner_statement();
				}
				}
				setState(25);
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

	public static class Inner_statementContext extends ParserRuleContext {
		public TerminalNode INNER() { return getToken(RecoveryParser.INNER, 0); }
		public TerminalNode NEWLINE() { return getToken(RecoveryParser.NEWLINE, 0); }
		public List<Tail_wordContext> tail_word() {
			return getRuleContexts(Tail_wordContext.class);
		}
		public Tail_wordContext tail_word(int i) {
			return getRuleContext(Tail_wordContext.class,i);
		}
		public Inner_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inner_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).enterInner_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).exitInner_statement(this);
		}
	}

	public final Inner_statementContext inner_statement() throws RecognitionException {
		Inner_statementContext _localctx = new Inner_statementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_inner_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(26);
			match(INNER);
			setState(30);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BLOCK) | (1L << INNER) | (1L << SIMPLE))) != 0)) {
				{
				{
				setState(27);
				tail_word();
				}
				}
				setState(32);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(33);
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
		public Block_statementContext block_statement() {
			return getRuleContext(Block_statementContext.class,0);
		}
		public Simple_statementContext simple_statement() {
			return getRuleContext(Simple_statementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_statement);
		try {
			setState(37);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BLOCK:
				enterOuterAlt(_localctx, 1);
				{
				setState(35);
				block_statement();
				}
				break;
			case SIMPLE:
				enterOuterAlt(_localctx, 2);
				{
				setState(36);
				simple_statement();
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

	public static class RecoveryContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(RecoveryParser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public RecoveryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_recovery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).enterRecovery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).exitRecovery(this);
		}
	}

	public final RecoveryContext recovery() throws RecognitionException {
		RecoveryContext _localctx = new RecoveryContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_recovery);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(42);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==BLOCK || _la==SIMPLE) {
				{
				{
				setState(39);
				statement();
				}
				}
				setState(44);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(45);
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

	public static class Simple_statementContext extends ParserRuleContext {
		public TerminalNode SIMPLE() { return getToken(RecoveryParser.SIMPLE, 0); }
		public TerminalNode NEWLINE() { return getToken(RecoveryParser.NEWLINE, 0); }
		public List<Tail_wordContext> tail_word() {
			return getRuleContexts(Tail_wordContext.class);
		}
		public Tail_wordContext tail_word(int i) {
			return getRuleContext(Tail_wordContext.class,i);
		}
		public Simple_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simple_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).enterSimple_statement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).exitSimple_statement(this);
		}
	}

	public final Simple_statementContext simple_statement() throws RecognitionException {
		Simple_statementContext _localctx = new Simple_statementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_simple_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			match(SIMPLE);
			setState(51);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BLOCK) | (1L << INNER) | (1L << SIMPLE))) != 0)) {
				{
				{
				setState(48);
				tail_word();
				}
				}
				setState(53);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(54);
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

	public static class Tail_wordContext extends ParserRuleContext {
		public TerminalNode BLOCK() { return getToken(RecoveryParser.BLOCK, 0); }
		public TerminalNode INNER() { return getToken(RecoveryParser.INNER, 0); }
		public TerminalNode SIMPLE() { return getToken(RecoveryParser.SIMPLE, 0); }
		public Tail_wordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tail_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).enterTail_word(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof RecoveryParserListener ) ((RecoveryParserListener)listener).exitTail_word(this);
		}
	}

	public final Tail_wordContext tail_word() throws RecognitionException {
		Tail_wordContext _localctx = new Tail_wordContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_tail_word);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BLOCK) | (1L << INNER) | (1L << SIMPLE))) != 0)) ) {
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\r=\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\7\2\21\n\2\f\2\16\2\24\13\2"+
		"\3\2\3\2\7\2\30\n\2\f\2\16\2\33\13\2\3\3\3\3\7\3\37\n\3\f\3\16\3\"\13"+
		"\3\3\3\3\3\3\4\3\4\5\4(\n\4\3\5\7\5+\n\5\f\5\16\5.\13\5\3\5\3\5\3\6\3"+
		"\6\7\6\64\n\6\f\6\16\6\67\13\6\3\6\3\6\3\7\3\7\3\7\2\2\b\2\4\6\b\n\f\2"+
		"\3\4\2\3\4\6\6\2<\2\16\3\2\2\2\4\34\3\2\2\2\6\'\3\2\2\2\b,\3\2\2\2\n\61"+
		"\3\2\2\2\f:\3\2\2\2\16\22\7\3\2\2\17\21\5\f\7\2\20\17\3\2\2\2\21\24\3"+
		"\2\2\2\22\20\3\2\2\2\22\23\3\2\2\2\23\25\3\2\2\2\24\22\3\2\2\2\25\31\7"+
		"\n\2\2\26\30\5\4\3\2\27\26\3\2\2\2\30\33\3\2\2\2\31\27\3\2\2\2\31\32\3"+
		"\2\2\2\32\3\3\2\2\2\33\31\3\2\2\2\34 \7\4\2\2\35\37\5\f\7\2\36\35\3\2"+
		"\2\2\37\"\3\2\2\2 \36\3\2\2\2 !\3\2\2\2!#\3\2\2\2\" \3\2\2\2#$\7\n\2\2"+
		"$\5\3\2\2\2%(\5\2\2\2&(\5\n\6\2\'%\3\2\2\2\'&\3\2\2\2(\7\3\2\2\2)+\5\6"+
		"\4\2*)\3\2\2\2+.\3\2\2\2,*\3\2\2\2,-\3\2\2\2-/\3\2\2\2.,\3\2\2\2/\60\7"+
		"\2\2\3\60\t\3\2\2\2\61\65\7\6\2\2\62\64\5\f\7\2\63\62\3\2\2\2\64\67\3"+
		"\2\2\2\65\63\3\2\2\2\65\66\3\2\2\2\668\3\2\2\2\67\65\3\2\2\289\7\n\2\2"+
		"9\13\3\2\2\2:;\t\2\2\2;\r\3\2\2\2\b\22\31 \',\65";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}