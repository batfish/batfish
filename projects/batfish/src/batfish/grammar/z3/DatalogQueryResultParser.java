// Generated from /home/arifogel/git/batfish/projects/batfish/src/batfish/grammar/z3/DatalogQueryResultParser.g4 by ANTLR 4.4

package batfish.grammar.z3;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DatalogQueryResultParser extends batfish.grammar.BatfishParser {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		M_COMMENT_NEWLINE=21, RIGHT_PAREN=17, DEC=13, WS=20, VARIABLE=19, HEX=15, 
		UNSAT=9, OR=6, LEFT_PAREN=16, UNDERSCORE=18, EQUALS=14, VAR=10, M_COMMENT_NON_NEWLINE=22, 
		NOT=5, SAT=7, AND=1, EXTRACT=2, COMMENT=11, FALSE=3, BIN=12, TRUE=8, LET=4;
	public static final String[] tokenNames = {
		"<INVALID>", "'and'", "'extract'", "'false'", "'let'", "'not'", "'or'", 
		"'sat'", "'true'", "'unsat'", "':var'", "COMMENT", "BIN", "DEC", "'='", 
		"HEX", "'('", "')'", "'_'", "VARIABLE", "WS", "M_COMMENT_NEWLINE", "M_COMMENT_NON_NEWLINE"
	};
	public static final int
		RULE_and_expr = 0, RULE_boolean_expr = 1, RULE_eq_expr = 2, RULE_extract_expr = 3, 
		RULE_int_expr = 4, RULE_lit_int_expr = 5, RULE_or_expr = 6, RULE_result = 7, 
		RULE_var_int_expr = 8;
	public static final String[] ruleNames = {
		"and_expr", "boolean_expr", "eq_expr", "extract_expr", "int_expr", "lit_int_expr", 
		"or_expr", "result", "var_int_expr"
	};

	@Override
	public String getGrammarFileName() { return "DatalogQueryResultParser.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DatalogQueryResultParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class And_exprContext extends ParserRuleContext {
		public Boolean_exprContext boolean_expr;
		public List<Boolean_exprContext> conjuncts = new ArrayList<Boolean_exprContext>();
		public TerminalNode AND() { return getToken(DatalogQueryResultParser.AND, 0); }
		public List<Boolean_exprContext> boolean_expr() {
			return getRuleContexts(Boolean_exprContext.class);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(DatalogQueryResultParser.RIGHT_PAREN, 0); }
		public Boolean_exprContext boolean_expr(int i) {
			return getRuleContext(Boolean_exprContext.class,i);
		}
		public TerminalNode LEFT_PAREN() { return getToken(DatalogQueryResultParser.LEFT_PAREN, 0); }
		public And_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).enterAnd_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).exitAnd_expr(this);
		}
	}

	public final And_exprContext and_expr() throws RecognitionException {
		And_exprContext _localctx = new And_exprContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_and_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(18); match(LEFT_PAREN);
			setState(19); match(AND);
			setState(21); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(20); ((And_exprContext)_localctx).boolean_expr = boolean_expr();
				((And_exprContext)_localctx).conjuncts.add(((And_exprContext)_localctx).boolean_expr);
				}
				}
				setState(23); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FALSE) | (1L << TRUE) | (1L << LEFT_PAREN))) != 0) );
			setState(25); match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Boolean_exprContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(DatalogQueryResultParser.TRUE, 0); }
		public Eq_exprContext eq_expr() {
			return getRuleContext(Eq_exprContext.class,0);
		}
		public And_exprContext and_expr() {
			return getRuleContext(And_exprContext.class,0);
		}
		public Or_exprContext or_expr() {
			return getRuleContext(Or_exprContext.class,0);
		}
		public TerminalNode FALSE() { return getToken(DatalogQueryResultParser.FALSE, 0); }
		public Boolean_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).enterBoolean_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).exitBoolean_expr(this);
		}
	}

	public final Boolean_exprContext boolean_expr() throws RecognitionException {
		Boolean_exprContext _localctx = new Boolean_exprContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_boolean_expr);
		try {
			setState(32);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(27); and_expr();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(28); eq_expr();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(29); or_expr();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(30); match(TRUE);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(31); match(FALSE);
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

	public static class Eq_exprContext extends ParserRuleContext {
		public Int_exprContext lhs;
		public Int_exprContext rhs;
		public List<Int_exprContext> int_expr() {
			return getRuleContexts(Int_exprContext.class);
		}
		public TerminalNode EQUALS() { return getToken(DatalogQueryResultParser.EQUALS, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(DatalogQueryResultParser.RIGHT_PAREN, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(DatalogQueryResultParser.LEFT_PAREN, 0); }
		public Int_exprContext int_expr(int i) {
			return getRuleContext(Int_exprContext.class,i);
		}
		public Eq_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_eq_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).enterEq_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).exitEq_expr(this);
		}
	}

	public final Eq_exprContext eq_expr() throws RecognitionException {
		Eq_exprContext _localctx = new Eq_exprContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_eq_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(34); match(LEFT_PAREN);
			setState(35); match(EQUALS);
			setState(36); ((Eq_exprContext)_localctx).lhs = int_expr();
			setState(37); ((Eq_exprContext)_localctx).rhs = int_expr();
			setState(38); match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Extract_exprContext extends ParserRuleContext {
		public Token high;
		public Token low;
		public TerminalNode UNDERSCORE() { return getToken(DatalogQueryResultParser.UNDERSCORE, 0); }
		public TerminalNode DEC(int i) {
			return getToken(DatalogQueryResultParser.DEC, i);
		}
		public Var_int_exprContext var_int_expr() {
			return getRuleContext(Var_int_exprContext.class,0);
		}
		public TerminalNode LEFT_PAREN(int i) {
			return getToken(DatalogQueryResultParser.LEFT_PAREN, i);
		}
		public TerminalNode EXTRACT() { return getToken(DatalogQueryResultParser.EXTRACT, 0); }
		public List<TerminalNode> DEC() { return getTokens(DatalogQueryResultParser.DEC); }
		public List<TerminalNode> RIGHT_PAREN() { return getTokens(DatalogQueryResultParser.RIGHT_PAREN); }
		public List<TerminalNode> LEFT_PAREN() { return getTokens(DatalogQueryResultParser.LEFT_PAREN); }
		public TerminalNode RIGHT_PAREN(int i) {
			return getToken(DatalogQueryResultParser.RIGHT_PAREN, i);
		}
		public Extract_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_extract_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).enterExtract_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).exitExtract_expr(this);
		}
	}

	public final Extract_exprContext extract_expr() throws RecognitionException {
		Extract_exprContext _localctx = new Extract_exprContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_extract_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40); match(LEFT_PAREN);
			setState(41); match(LEFT_PAREN);
			setState(42); match(UNDERSCORE);
			setState(43); match(EXTRACT);
			setState(44); ((Extract_exprContext)_localctx).high = match(DEC);
			setState(45); ((Extract_exprContext)_localctx).low = match(DEC);
			setState(46); match(RIGHT_PAREN);
			setState(47); var_int_expr();
			setState(48); match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Int_exprContext extends ParserRuleContext {
		public Var_int_exprContext var_int_expr() {
			return getRuleContext(Var_int_exprContext.class,0);
		}
		public Extract_exprContext extract_expr() {
			return getRuleContext(Extract_exprContext.class,0);
		}
		public Lit_int_exprContext lit_int_expr() {
			return getRuleContext(Lit_int_exprContext.class,0);
		}
		public Int_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_int_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).enterInt_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).exitInt_expr(this);
		}
	}

	public final Int_exprContext int_expr() throws RecognitionException {
		Int_exprContext _localctx = new Int_exprContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_int_expr);
		try {
			setState(53);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(50); lit_int_expr();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(51); extract_expr();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(52); var_int_expr();
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

	public static class Lit_int_exprContext extends ParserRuleContext {
		public TerminalNode BIN() { return getToken(DatalogQueryResultParser.BIN, 0); }
		public TerminalNode HEX() { return getToken(DatalogQueryResultParser.HEX, 0); }
		public Lit_int_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lit_int_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).enterLit_int_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).exitLit_int_expr(this);
		}
	}

	public final Lit_int_exprContext lit_int_expr() throws RecognitionException {
		Lit_int_exprContext _localctx = new Lit_int_exprContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_lit_int_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(55);
			_la = _input.LA(1);
			if ( !(_la==BIN || _la==HEX) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Or_exprContext extends ParserRuleContext {
		public Boolean_exprContext boolean_expr;
		public List<Boolean_exprContext> disjuncts = new ArrayList<Boolean_exprContext>();
		public List<Boolean_exprContext> boolean_expr() {
			return getRuleContexts(Boolean_exprContext.class);
		}
		public TerminalNode OR() { return getToken(DatalogQueryResultParser.OR, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(DatalogQueryResultParser.RIGHT_PAREN, 0); }
		public Boolean_exprContext boolean_expr(int i) {
			return getRuleContext(Boolean_exprContext.class,i);
		}
		public TerminalNode LEFT_PAREN() { return getToken(DatalogQueryResultParser.LEFT_PAREN, 0); }
		public Or_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).enterOr_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).exitOr_expr(this);
		}
	}

	public final Or_exprContext or_expr() throws RecognitionException {
		Or_exprContext _localctx = new Or_exprContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_or_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(57); match(LEFT_PAREN);
			setState(58); match(OR);
			setState(60); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(59); ((Or_exprContext)_localctx).boolean_expr = boolean_expr();
				((Or_exprContext)_localctx).disjuncts.add(((Or_exprContext)_localctx).boolean_expr);
				}
				}
				setState(62); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FALSE) | (1L << TRUE) | (1L << LEFT_PAREN))) != 0) );
			setState(64); match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ResultContext extends ParserRuleContext {
		public TerminalNode SAT() { return getToken(DatalogQueryResultParser.SAT, 0); }
		public Boolean_exprContext boolean_expr() {
			return getRuleContext(Boolean_exprContext.class,0);
		}
		public TerminalNode UNSAT() { return getToken(DatalogQueryResultParser.UNSAT, 0); }
		public ResultContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_result; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).enterResult(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).exitResult(this);
		}
	}

	public final ResultContext result() throws RecognitionException {
		ResultContext _localctx = new ResultContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_result);
		try {
			setState(69);
			switch (_input.LA(1)) {
			case SAT:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(66); match(SAT);
				setState(67); boolean_expr();
				}
				}
				break;
			case UNSAT:
				enterOuterAlt(_localctx, 2);
				{
				setState(68); match(UNSAT);
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

	public static class Var_int_exprContext extends ParserRuleContext {
		public TerminalNode VAR() { return getToken(DatalogQueryResultParser.VAR, 0); }
		public TerminalNode DEC() { return getToken(DatalogQueryResultParser.DEC, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(DatalogQueryResultParser.RIGHT_PAREN, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(DatalogQueryResultParser.LEFT_PAREN, 0); }
		public Var_int_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var_int_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).enterVar_int_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DatalogQueryResultParserListener ) ((DatalogQueryResultParserListener)listener).exitVar_int_expr(this);
		}
	}

	public final Var_int_exprContext var_int_expr() throws RecognitionException {
		Var_int_exprContext _localctx = new Var_int_exprContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_var_int_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71); match(LEFT_PAREN);
			setState(72); match(VAR);
			setState(73); match(DEC);
			setState(74); match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\30O\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\3\2\3\2"+
		"\6\2\30\n\2\r\2\16\2\31\3\2\3\2\3\3\3\3\3\3\3\3\3\3\5\3#\n\3\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\5\6"+
		"8\n\6\3\7\3\7\3\b\3\b\3\b\6\b?\n\b\r\b\16\b@\3\b\3\b\3\t\3\t\3\t\5\tH"+
		"\n\t\3\n\3\n\3\n\3\n\3\n\3\n\2\2\13\2\4\6\b\n\f\16\20\22\2\3\4\2\16\16"+
		"\21\21N\2\24\3\2\2\2\4\"\3\2\2\2\6$\3\2\2\2\b*\3\2\2\2\n\67\3\2\2\2\f"+
		"9\3\2\2\2\16;\3\2\2\2\20G\3\2\2\2\22I\3\2\2\2\24\25\7\22\2\2\25\27\7\3"+
		"\2\2\26\30\5\4\3\2\27\26\3\2\2\2\30\31\3\2\2\2\31\27\3\2\2\2\31\32\3\2"+
		"\2\2\32\33\3\2\2\2\33\34\7\23\2\2\34\3\3\2\2\2\35#\5\2\2\2\36#\5\6\4\2"+
		"\37#\5\16\b\2 #\7\n\2\2!#\7\5\2\2\"\35\3\2\2\2\"\36\3\2\2\2\"\37\3\2\2"+
		"\2\" \3\2\2\2\"!\3\2\2\2#\5\3\2\2\2$%\7\22\2\2%&\7\20\2\2&\'\5\n\6\2\'"+
		"(\5\n\6\2()\7\23\2\2)\7\3\2\2\2*+\7\22\2\2+,\7\22\2\2,-\7\24\2\2-.\7\4"+
		"\2\2./\7\17\2\2/\60\7\17\2\2\60\61\7\23\2\2\61\62\5\22\n\2\62\63\7\23"+
		"\2\2\63\t\3\2\2\2\648\5\f\7\2\658\5\b\5\2\668\5\22\n\2\67\64\3\2\2\2\67"+
		"\65\3\2\2\2\67\66\3\2\2\28\13\3\2\2\29:\t\2\2\2:\r\3\2\2\2;<\7\22\2\2"+
		"<>\7\b\2\2=?\5\4\3\2>=\3\2\2\2?@\3\2\2\2@>\3\2\2\2@A\3\2\2\2AB\3\2\2\2"+
		"BC\7\23\2\2C\17\3\2\2\2DE\7\t\2\2EH\5\4\3\2FH\7\13\2\2GD\3\2\2\2GF\3\2"+
		"\2\2H\21\3\2\2\2IJ\7\22\2\2JK\7\f\2\2KL\7\17\2\2LM\7\23\2\2M\23\3\2\2"+
		"\2\7\31\"\67@G";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}