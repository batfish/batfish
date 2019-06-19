// Generated from org/batfish/grammar/recovery/RecoveryLexer.g4 by ANTLR 4.7.2
package org.batfish.grammar.recovery;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class RecoveryLexer extends org.batfish.grammar.BatfishLexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		BLOCK=1, INNER=2, OTHER=3, SIMPLE=4, BLANK_LINE=5, COMMENT_LINE=6, COMMENT_TAIL=7, 
		NEWLINE=8, WS=9, ENTER_BAD_MODE=10, M_BadMode_nonexistent=11;
	public static final int
		M_BadMode=1;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "M_BadMode"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"BLOCK", "INNER", "OTHER", "SIMPLE", "BLANK_LINE", "COMMENT_LINE", "COMMENT_TAIL", 
			"NEWLINE", "WS", "F_Newline", "F_NonNewline", "F_Whitespace", "ENTER_BAD_MODE", 
			"M_BadMode_nonexistent"
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


	private int lastTokenType = -1;

	@Override
	public void emit(Token token) {
	    super.emit(token);
	    if (token.getChannel() != HIDDEN) {
	       lastTokenType = token.getType();
	    }
	}


	public RecoveryLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "RecoveryLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	@Override
	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 4:
			return BLANK_LINE_sempred((RuleContext)_localctx, predIndex);
		case 5:
			return COMMENT_LINE_sempred((RuleContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean BLANK_LINE_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return lastTokenType == NEWLINE;
		}
		return true;
	}
	private boolean COMMENT_LINE_sempred(RuleContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return lastTokenType == NEWLINE || lastTokenType == EOF;
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\r\u0096\b\1\b\1\4"+
		"\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n"+
		"\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\2\3\2\3\2\3\2"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\6\7\6;\n\6\f\6\16\6>\13\6\3\6\3\6\3\6\7\6C\n\6\f\6\16\6F\13\6"+
		"\3\6\3\6\3\7\7\7K\n\7\f\7\16\7N\13\7\3\7\3\7\3\7\7\7S\n\7\f\7\16\7V\13"+
		"\7\3\7\6\7Y\n\7\r\7\16\7Z\3\7\3\7\3\b\3\b\7\ba\n\b\f\b\16\bd\13\b\3\b"+
		"\3\b\3\t\6\ti\n\t\r\t\16\tj\3\n\6\nn\n\n\r\n\16\no\3\n\3\n\3\13\3\13\3"+
		"\f\3\f\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\2\2\20\4\3\6\4\b\5\n\6\f\7\16\b\20\t\22\n\24\13\26"+
		"\2\30\2\32\2\34\f\36\r\4\2\3\4\4\2\f\f\17\17\4\2\13\13\"\"\2\u0099\2\4"+
		"\3\2\2\2\2\6\3\2\2\2\2\b\3\2\2\2\2\n\3\2\2\2\2\f\3\2\2\2\2\16\3\2\2\2"+
		"\2\20\3\2\2\2\2\22\3\2\2\2\2\24\3\2\2\2\2\34\3\2\2\2\3\36\3\2\2\2\4 \3"+
		"\2\2\2\6&\3\2\2\2\b,\3\2\2\2\n\62\3\2\2\2\f<\3\2\2\2\16L\3\2\2\2\20^\3"+
		"\2\2\2\22h\3\2\2\2\24m\3\2\2\2\26s\3\2\2\2\30u\3\2\2\2\32w\3\2\2\2\34"+
		"y\3\2\2\2\36\u008a\3\2\2\2 !\7d\2\2!\"\7n\2\2\"#\7q\2\2#$\7e\2\2$%\7m"+
		"\2\2%\5\3\2\2\2&\'\7k\2\2\'(\7p\2\2()\7p\2\2)*\7g\2\2*+\7t\2\2+\7\3\2"+
		"\2\2,-\7q\2\2-.\7v\2\2./\7j\2\2/\60\7g\2\2\60\61\7t\2\2\61\t\3\2\2\2\62"+
		"\63\7u\2\2\63\64\7k\2\2\64\65\7o\2\2\65\66\7r\2\2\66\67\7n\2\2\678\7g"+
		"\2\28\13\3\2\2\29;\5\32\r\2:9\3\2\2\2;>\3\2\2\2<:\3\2\2\2<=\3\2\2\2=?"+
		"\3\2\2\2><\3\2\2\2?@\5\26\13\2@D\6\6\2\2AC\5\26\13\2BA\3\2\2\2CF\3\2\2"+
		"\2DB\3\2\2\2DE\3\2\2\2EG\3\2\2\2FD\3\2\2\2GH\b\6\2\2H\r\3\2\2\2IK\5\32"+
		"\r\2JI\3\2\2\2KN\3\2\2\2LJ\3\2\2\2LM\3\2\2\2MO\3\2\2\2NL\3\2\2\2OP\7%"+
		"\2\2PT\6\7\3\2QS\5\30\f\2RQ\3\2\2\2SV\3\2\2\2TR\3\2\2\2TU\3\2\2\2UX\3"+
		"\2\2\2VT\3\2\2\2WY\5\26\13\2XW\3\2\2\2YZ\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2["+
		"\\\3\2\2\2\\]\b\7\2\2]\17\3\2\2\2^b\7%\2\2_a\5\30\f\2`_\3\2\2\2ad\3\2"+
		"\2\2b`\3\2\2\2bc\3\2\2\2ce\3\2\2\2db\3\2\2\2ef\b\b\2\2f\21\3\2\2\2gi\5"+
		"\26\13\2hg\3\2\2\2ij\3\2\2\2jh\3\2\2\2jk\3\2\2\2k\23\3\2\2\2ln\5\32\r"+
		"\2ml\3\2\2\2no\3\2\2\2om\3\2\2\2op\3\2\2\2pq\3\2\2\2qr\b\n\2\2r\25\3\2"+
		"\2\2st\t\2\2\2t\27\3\2\2\2uv\n\2\2\2v\31\3\2\2\2wx\t\3\2\2x\33\3\2\2\2"+
		"yz\7g\2\2z{\7p\2\2{|\7v\2\2|}\7g\2\2}~\7t\2\2~\177\7/\2\2\177\u0080\7"+
		"d\2\2\u0080\u0081\7c\2\2\u0081\u0082\7f\2\2\u0082\u0083\7/\2\2\u0083\u0084"+
		"\7o\2\2\u0084\u0085\7q\2\2\u0085\u0086\7f\2\2\u0086\u0087\7g\2\2\u0087"+
		"\u0088\3\2\2\2\u0088\u0089\b\16\3\2\u0089\35\3\2\2\2\u008a\u008b\7p\2"+
		"\2\u008b\u008c\7q\2\2\u008c\u008d\7p\2\2\u008d\u008e\7g\2\2\u008e\u008f"+
		"\7z\2\2\u008f\u0090\7k\2\2\u0090\u0091\7u\2\2\u0091\u0092\7v\2\2\u0092"+
		"\u0093\7g\2\2\u0093\u0094\7p\2\2\u0094\u0095\7v\2\2\u0095\37\3\2\2\2\f"+
		"\2\3<DLTZbjo\4\2\3\2\7\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}