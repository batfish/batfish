// Generated from /home/arifogel/git/batfish/projects/batfish/src/batfish/grammar/z3/DatalogQueryResultLexer.g4 by ANTLR 4.4

package batfish.grammar.z3;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class DatalogQueryResultLexer extends batfish.grammar.BatfishLexer {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		AND=1, EXTRACT=2, FALSE=3, LET=4, NOT=5, OR=6, SAT=7, TRUE=8, UNSAT=9, 
		VAR=10, COMMENT=11, BIN=12, DEC=13, EQUALS=14, HEX=15, LEFT_PAREN=16, 
		RIGHT_PAREN=17, UNDERSCORE=18, VARIABLE=19, WS=20, M_COMMENT_NEWLINE=21, 
		M_COMMENT_NON_NEWLINE=22;
	public static final int M_COMMENT = 1;
	public static String[] modeNames = {
		"DEFAULT_MODE", "M_COMMENT"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'", 
		"'\r'", "'\\u000E'", "'\\u000F'", "'\\u0010'", "'\\u0011'", "'\\u0012'", 
		"'\\u0013'", "'\\u0014'", "'\\u0015'", "'\\u0016'"
	};
	public static final String[] ruleNames = {
		"AND", "EXTRACT", "FALSE", "LET", "NOT", "OR", "SAT", "TRUE", "UNSAT", 
		"VAR", "COMMENT", "BIN", "DEC", "EQUALS", "HEX", "LEFT_PAREN", "RIGHT_PAREN", 
		"UNDERSCORE", "VARIABLE", "WS", "NEWLINE_CHAR", "F_HexDigit", "F_Digit", 
		"F_NewlineChar", "F_NonNewlineChar", "F_WhitespaceChar", "M_COMMENT_NEWLINE", 
		"M_COMMENT_NON_NEWLINE"
	};


	public DatalogQueryResultLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "DatalogQueryResultLexer.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\30\u00b9\b\1\b\1"+
		"\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t"+
		"\n\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4"+
		"\22\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4"+
		"\31\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\3\2\3\2\3\2\3\2\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6"+
		"\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3"+
		"\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3"+
		"\r\3\r\6\rw\n\r\r\r\16\rx\3\16\6\16|\n\16\r\16\16\16}\3\17\3\17\3\20\3"+
		"\20\3\20\3\20\6\20\u0086\n\20\r\20\16\20\u0087\3\21\3\21\3\22\3\22\3\23"+
		"\3\23\3\24\3\24\3\24\3\24\6\24\u0094\n\24\r\24\16\24\u0095\3\25\6\25\u0099"+
		"\n\25\r\25\16\25\u009a\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3"+
		"\31\3\32\3\32\3\33\3\33\3\34\6\34\u00ac\n\34\r\34\16\34\u00ad\3\34\3\34"+
		"\3\34\3\35\6\35\u00b4\n\35\r\35\16\35\u00b5\3\35\3\35\2\2\36\4\3\6\4\b"+
		"\5\n\6\f\7\16\b\20\t\22\n\24\13\26\f\30\r\32\16\34\17\36\20 \21\"\22$"+
		"\23&\24(\25*\26,\2.\2\60\2\62\2\64\2\66\28\27:\30\4\2\3\5\5\2\62;CHch"+
		"\4\2\f\f\17\17\5\2\13\f\16\17\"\"\u00b8\2\4\3\2\2\2\2\6\3\2\2\2\2\b\3"+
		"\2\2\2\2\n\3\2\2\2\2\f\3\2\2\2\2\16\3\2\2\2\2\20\3\2\2\2\2\22\3\2\2\2"+
		"\2\24\3\2\2\2\2\26\3\2\2\2\2\30\3\2\2\2\2\32\3\2\2\2\2\34\3\2\2\2\2\36"+
		"\3\2\2\2\2 \3\2\2\2\2\"\3\2\2\2\2$\3\2\2\2\2&\3\2\2\2\2(\3\2\2\2\2*\3"+
		"\2\2\2\38\3\2\2\2\3:\3\2\2\2\4<\3\2\2\2\6@\3\2\2\2\bH\3\2\2\2\nN\3\2\2"+
		"\2\fR\3\2\2\2\16V\3\2\2\2\20Y\3\2\2\2\22]\3\2\2\2\24b\3\2\2\2\26h\3\2"+
		"\2\2\30m\3\2\2\2\32r\3\2\2\2\34{\3\2\2\2\36\177\3\2\2\2 \u0081\3\2\2\2"+
		"\"\u0089\3\2\2\2$\u008b\3\2\2\2&\u008d\3\2\2\2(\u008f\3\2\2\2*\u0098\3"+
		"\2\2\2,\u009e\3\2\2\2.\u00a0\3\2\2\2\60\u00a2\3\2\2\2\62\u00a4\3\2\2\2"+
		"\64\u00a6\3\2\2\2\66\u00a8\3\2\2\28\u00ab\3\2\2\2:\u00b3\3\2\2\2<=\7c"+
		"\2\2=>\7p\2\2>?\7f\2\2?\5\3\2\2\2@A\7g\2\2AB\7z\2\2BC\7v\2\2CD\7t\2\2"+
		"DE\7c\2\2EF\7e\2\2FG\7v\2\2G\7\3\2\2\2HI\7h\2\2IJ\7c\2\2JK\7n\2\2KL\7"+
		"u\2\2LM\7g\2\2M\t\3\2\2\2NO\7n\2\2OP\7g\2\2PQ\7v\2\2Q\13\3\2\2\2RS\7p"+
		"\2\2ST\7q\2\2TU\7v\2\2U\r\3\2\2\2VW\7q\2\2WX\7t\2\2X\17\3\2\2\2YZ\7u\2"+
		"\2Z[\7c\2\2[\\\7v\2\2\\\21\3\2\2\2]^\7v\2\2^_\7t\2\2_`\7w\2\2`a\7g\2\2"+
		"a\23\3\2\2\2bc\7w\2\2cd\7p\2\2de\7u\2\2ef\7c\2\2fg\7v\2\2g\25\3\2\2\2"+
		"hi\7<\2\2ij\7x\2\2jk\7c\2\2kl\7t\2\2l\27\3\2\2\2mn\7=\2\2no\3\2\2\2op"+
		"\b\f\2\2pq\b\f\3\2q\31\3\2\2\2rs\7%\2\2st\7d\2\2tv\3\2\2\2uw\5\60\30\2"+
		"vu\3\2\2\2wx\3\2\2\2xv\3\2\2\2xy\3\2\2\2y\33\3\2\2\2z|\5\60\30\2{z\3\2"+
		"\2\2|}\3\2\2\2}{\3\2\2\2}~\3\2\2\2~\35\3\2\2\2\177\u0080\7?\2\2\u0080"+
		"\37\3\2\2\2\u0081\u0082\7%\2\2\u0082\u0083\7z\2\2\u0083\u0085\3\2\2\2"+
		"\u0084\u0086\5.\27\2\u0085\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0085"+
		"\3\2\2\2\u0087\u0088\3\2\2\2\u0088!\3\2\2\2\u0089\u008a\7*\2\2\u008a#"+
		"\3\2\2\2\u008b\u008c\7+\2\2\u008c%\3\2\2\2\u008d\u008e\7a\2\2\u008e\'"+
		"\3\2\2\2\u008f\u0090\7c\2\2\u0090\u0091\7#\2\2\u0091\u0093\3\2\2\2\u0092"+
		"\u0094\5\60\30\2\u0093\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095\u0093\3"+
		"\2\2\2\u0095\u0096\3\2\2\2\u0096)\3\2\2\2\u0097\u0099\5\66\33\2\u0098"+
		"\u0097\3\2\2\2\u0099\u009a\3\2\2\2\u009a\u0098\3\2\2\2\u009a\u009b\3\2"+
		"\2\2\u009b\u009c\3\2\2\2\u009c\u009d\b\25\3\2\u009d+\3\2\2\2\u009e\u009f"+
		"\7\f\2\2\u009f-\3\2\2\2\u00a0\u00a1\t\2\2\2\u00a1/\3\2\2\2\u00a2\u00a3"+
		"\4\62;\2\u00a3\61\3\2\2\2\u00a4\u00a5\t\3\2\2\u00a5\63\3\2\2\2\u00a6\u00a7"+
		"\n\3\2\2\u00a7\65\3\2\2\2\u00a8\u00a9\t\4\2\2\u00a9\67\3\2\2\2\u00aa\u00ac"+
		"\5\62\31\2\u00ab\u00aa\3\2\2\2\u00ac\u00ad\3\2\2\2\u00ad\u00ab\3\2\2\2"+
		"\u00ad\u00ae\3\2\2\2\u00ae\u00af\3\2\2\2\u00af\u00b0\b\34\4\2\u00b0\u00b1"+
		"\b\34\3\2\u00b19\3\2\2\2\u00b2\u00b4\5\64\32\2\u00b3\u00b2\3\2\2\2\u00b4"+
		"\u00b5\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\u00b7\3\2"+
		"\2\2\u00b7\u00b8\b\35\3\2\u00b8;\3\2\2\2\13\2\3x}\u0087\u0095\u009a\u00ad"+
		"\u00b5\5\7\3\2\2\3\2\6\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}