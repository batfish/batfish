// Generated from org/batfish/grammar/routing_table/nxos/NxosRoutingTableLexer.g4 by ANTLR 4.7.2
package org.batfish.grammar.routing_table.nxos;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NxosRoutingTableLexer extends org.batfish.grammar.BatfishLexer {
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
		M_DoubleQuote=1;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE", "M_DoubleQuote"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"ATTACHED", "BGP", "DIRECT", "ETH", "EXTERNAL", "INTER", "INTERNAL", 
			"INTRA", "LO", "LOCAL", "NULL", "OSPF", "STATIC", "TAG", "TYPE_1", "TYPE_2", 
			"VIA", "VRF_HEADER", "ASTERISK", "BRACKET_LEFT", "BRACKET_RIGHT", "COLON", 
			"COMMA", "COMMENT", "DASH", "DEC", "DOUBLE_QUOTE", "ELAPSED_TIME", "FORWARD_SLASH", 
			"IP_ADDRESS", "IP_PREFIX", "NEWLINE", "UNICAST_MULTICAST_COUNT", "WS", 
			"F_DecByte", "F_Digit", "F_IpAddress", "F_IpPrefix", "F_IpPrefixLength", 
			"F_Newline", "F_NonNewline", "F_PositiveDigit", "F_Whitespace", "M_DoubleQuote_ID", 
			"M_DoubleQuote_DOUBLE_QUOTE"
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


	private int lastTokenType = -1;

	@Override
	public void emit(Token token) {
	    super.emit(token);
	    if (token.getChannel() != HIDDEN) {
	       lastTokenType = token.getType();
	    }
	}

	public String printStateVariables() {
	   StringBuilder sb = new StringBuilder();
	   return sb.toString();
	}



	public NxosRoutingTableLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NxosRoutingTableLexer.g4"; }

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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2%\u017b\b\1\b\1\4"+
		"\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n"+
		"\4\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3"+
		"\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3"+
		"\b\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3"+
		"\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3"+
		"\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3"+
		"\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3"+
		"\23\3\23\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3"+
		"\31\3\31\6\31\u00e8\n\31\r\31\16\31\u00e9\3\31\3\31\3\31\3\31\3\32\3\32"+
		"\3\33\6\33\u00f3\n\33\r\33\16\33\u00f4\3\34\3\34\3\34\3\34\3\35\6\35\u00fc"+
		"\n\35\r\35\16\35\u00fd\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\5\35\u0108"+
		"\n\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\5\35\u0115"+
		"\n\35\3\36\3\36\3\37\3\37\3 \3 \3!\6!\u011e\n!\r!\16!\u011f\3\"\3\"\3"+
		"\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\6\"\u0131\n\"\r\"\16"+
		"\"\u0132\3\"\3\"\6\"\u0137\n\"\r\"\16\"\u0138\3#\6#\u013c\n#\r#\16#\u013d"+
		"\3#\3#\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\5$\u0151\n$\3%\3%"+
		"\3&\3&\3&\3&\3&\3&\3&\3&\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3(\5(\u0166\n(\3"+
		")\3)\3*\3*\3+\3+\3,\3,\3-\6-\u0171\n-\r-\16-\u0172\3-\3-\3.\3.\3.\3.\3"+
		".\2\2/\4\4\6\5\b\6\n\7\f\b\16\t\20\n\22\13\24\f\26\r\30\16\32\17\34\20"+
		"\36\21 \22\"\23$\24&\25(\26*\27,\30.\31\60\32\62\33\64\34\66\358\36:\37"+
		"< >!@\"B#D$F%H\2J\2L\2N\2P\2R\2T\2V\2X\2Z\2\\\2\4\2\3\f\3\2\62\66\3\2"+
		"\62\67\3\2\62;\3\2\63\64\3\2\65\65\3\2\62\64\4\2\f\f\17\17\3\2\63;\4\2"+
		"\13\13\"\"\3\2$$\2\u0181\2\4\3\2\2\2\2\6\3\2\2\2\2\b\3\2\2\2\2\n\3\2\2"+
		"\2\2\f\3\2\2\2\2\16\3\2\2\2\2\20\3\2\2\2\2\22\3\2\2\2\2\24\3\2\2\2\2\26"+
		"\3\2\2\2\2\30\3\2\2\2\2\32\3\2\2\2\2\34\3\2\2\2\2\36\3\2\2\2\2 \3\2\2"+
		"\2\2\"\3\2\2\2\2$\3\2\2\2\2&\3\2\2\2\2(\3\2\2\2\2*\3\2\2\2\2,\3\2\2\2"+
		"\2.\3\2\2\2\2\60\3\2\2\2\2\62\3\2\2\2\2\64\3\2\2\2\2\66\3\2\2\2\28\3\2"+
		"\2\2\2:\3\2\2\2\2<\3\2\2\2\2>\3\2\2\2\2@\3\2\2\2\2B\3\2\2\2\2D\3\2\2\2"+
		"\2F\3\2\2\2\3Z\3\2\2\2\3\\\3\2\2\2\4^\3\2\2\2\6g\3\2\2\2\bk\3\2\2\2\n"+
		"r\3\2\2\2\fv\3\2\2\2\16\177\3\2\2\2\20\u0085\3\2\2\2\22\u008e\3\2\2\2"+
		"\24\u0094\3\2\2\2\26\u0097\3\2\2\2\30\u009d\3\2\2\2\32\u00a2\3\2\2\2\34"+
		"\u00a7\3\2\2\2\36\u00ae\3\2\2\2 \u00b2\3\2\2\2\"\u00b9\3\2\2\2$\u00c0"+
		"\3\2\2\2&\u00c4\3\2\2\2(\u00db\3\2\2\2*\u00dd\3\2\2\2,\u00df\3\2\2\2."+
		"\u00e1\3\2\2\2\60\u00e3\3\2\2\2\62\u00e5\3\2\2\2\64\u00ef\3\2\2\2\66\u00f2"+
		"\3\2\2\28\u00f6\3\2\2\2:\u0114\3\2\2\2<\u0116\3\2\2\2>\u0118\3\2\2\2@"+
		"\u011a\3\2\2\2B\u011d\3\2\2\2D\u0121\3\2\2\2F\u013b\3\2\2\2H\u0150\3\2"+
		"\2\2J\u0152\3\2\2\2L\u0154\3\2\2\2N\u015c\3\2\2\2P\u0165\3\2\2\2R\u0167"+
		"\3\2\2\2T\u0169\3\2\2\2V\u016b\3\2\2\2X\u016d\3\2\2\2Z\u0170\3\2\2\2\\"+
		"\u0176\3\2\2\2^_\7c\2\2_`\7v\2\2`a\7v\2\2ab\7c\2\2bc\7e\2\2cd\7j\2\2d"+
		"e\7g\2\2ef\7f\2\2f\5\3\2\2\2gh\7d\2\2hi\7i\2\2ij\7r\2\2j\7\3\2\2\2kl\7"+
		"f\2\2lm\7k\2\2mn\7t\2\2no\7g\2\2op\7e\2\2pq\7v\2\2q\t\3\2\2\2rs\7G\2\2"+
		"st\7v\2\2tu\7j\2\2u\13\3\2\2\2vw\7g\2\2wx\7z\2\2xy\7v\2\2yz\7g\2\2z{\7"+
		"t\2\2{|\7p\2\2|}\7c\2\2}~\7n\2\2~\r\3\2\2\2\177\u0080\7k\2\2\u0080\u0081"+
		"\7p\2\2\u0081\u0082\7v\2\2\u0082\u0083\7g\2\2\u0083\u0084\7t\2\2\u0084"+
		"\17\3\2\2\2\u0085\u0086\7k\2\2\u0086\u0087\7p\2\2\u0087\u0088\7v\2\2\u0088"+
		"\u0089\7g\2\2\u0089\u008a\7t\2\2\u008a\u008b\7p\2\2\u008b\u008c\7c\2\2"+
		"\u008c\u008d\7n\2\2\u008d\21\3\2\2\2\u008e\u008f\7k\2\2\u008f\u0090\7"+
		"p\2\2\u0090\u0091\7v\2\2\u0091\u0092\7t\2\2\u0092\u0093\7c\2\2\u0093\23"+
		"\3\2\2\2\u0094\u0095\7N\2\2\u0095\u0096\7q\2\2\u0096\25\3\2\2\2\u0097"+
		"\u0098\7n\2\2\u0098\u0099\7q\2\2\u0099\u009a\7e\2\2\u009a\u009b\7c\2\2"+
		"\u009b\u009c\7n\2\2\u009c\27\3\2\2\2\u009d\u009e\7P\2\2\u009e\u009f\7"+
		"w\2\2\u009f\u00a0\7n\2\2\u00a0\u00a1\7n\2\2\u00a1\31\3\2\2\2\u00a2\u00a3"+
		"\7q\2\2\u00a3\u00a4\7u\2\2\u00a4\u00a5\7r\2\2\u00a5\u00a6\7h\2\2\u00a6"+
		"\33\3\2\2\2\u00a7\u00a8\7u\2\2\u00a8\u00a9\7v\2\2\u00a9\u00aa\7c\2\2\u00aa"+
		"\u00ab\7v\2\2\u00ab\u00ac\7k\2\2\u00ac\u00ad\7e\2\2\u00ad\35\3\2\2\2\u00ae"+
		"\u00af\7v\2\2\u00af\u00b0\7c\2\2\u00b0\u00b1\7i\2\2\u00b1\37\3\2\2\2\u00b2"+
		"\u00b3\7v\2\2\u00b3\u00b4\7{\2\2\u00b4\u00b5\7r\2\2\u00b5\u00b6\7g\2\2"+
		"\u00b6\u00b7\7/\2\2\u00b7\u00b8\7\63\2\2\u00b8!\3\2\2\2\u00b9\u00ba\7"+
		"v\2\2\u00ba\u00bb\7{\2\2\u00bb\u00bc\7r\2\2\u00bc\u00bd\7g\2\2\u00bd\u00be"+
		"\7/\2\2\u00be\u00bf\7\64\2\2\u00bf#\3\2\2\2\u00c0\u00c1\7x\2\2\u00c1\u00c2"+
		"\7k\2\2\u00c2\u00c3\7c\2\2\u00c3%\3\2\2\2\u00c4\u00c5\7K\2\2\u00c5\u00c6"+
		"\7R\2\2\u00c6\u00c7\7\"\2\2\u00c7\u00c8\7T\2\2\u00c8\u00c9\7q\2\2\u00c9"+
		"\u00ca\7w\2\2\u00ca\u00cb\7v\2\2\u00cb\u00cc\7g\2\2\u00cc\u00cd\7\"\2"+
		"\2\u00cd\u00ce\7V\2\2\u00ce\u00cf\7c\2\2\u00cf\u00d0\7d\2\2\u00d0\u00d1"+
		"\7n\2\2\u00d1\u00d2\7g\2\2\u00d2\u00d3\7\"\2\2\u00d3\u00d4\7h\2\2\u00d4"+
		"\u00d5\7q\2\2\u00d5\u00d6\7t\2\2\u00d6\u00d7\7\"\2\2\u00d7\u00d8\7X\2"+
		"\2\u00d8\u00d9\7T\2\2\u00d9\u00da\7H\2\2\u00da\'\3\2\2\2\u00db\u00dc\7"+
		",\2\2\u00dc)\3\2\2\2\u00dd\u00de\7]\2\2\u00de+\3\2\2\2\u00df\u00e0\7_"+
		"\2\2\u00e0-\3\2\2\2\u00e1\u00e2\7<\2\2\u00e2/\3\2\2\2\u00e3\u00e4\7.\2"+
		"\2\u00e4\61\3\2\2\2\u00e5\u00e7\7)\2\2\u00e6\u00e8\5T*\2\u00e7\u00e6\3"+
		"\2\2\2\u00e8\u00e9\3\2\2\2\u00e9\u00e7\3\2\2\2\u00e9\u00ea\3\2\2\2\u00ea"+
		"\u00eb\3\2\2\2\u00eb\u00ec\5R)\2\u00ec\u00ed\3\2\2\2\u00ed\u00ee\b\31"+
		"\2\2\u00ee\63\3\2\2\2\u00ef\u00f0\7/\2\2\u00f0\65\3\2\2\2\u00f1\u00f3"+
		"\5J%\2\u00f2\u00f1\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4\u00f2\3\2\2\2\u00f4"+
		"\u00f5\3\2\2\2\u00f5\67\3\2\2\2\u00f6\u00f7\7$\2\2\u00f7\u00f8\3\2\2\2"+
		"\u00f8\u00f9\b\34\3\2\u00f99\3\2\2\2\u00fa\u00fc\5J%\2\u00fb\u00fa\3\2"+
		"\2\2\u00fc\u00fd\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fd\u00fe\3\2\2\2\u00fe"+
		"\u00ff\3\2\2\2\u00ff\u0100\7y\2\2\u0100\u0101\5J%\2\u0101\u0102\7f\2\2"+
		"\u0102\u0115\3\2\2\2\u0103\u0104\5J%\2\u0104\u0105\7f\2\2\u0105\u0107"+
		"\5J%\2\u0106\u0108\5J%\2\u0107\u0106\3\2\2\2\u0107\u0108\3\2\2\2\u0108"+
		"\u0109\3\2\2\2\u0109\u010a\7j\2\2\u010a\u0115\3\2\2\2\u010b\u010c\5J%"+
		"\2\u010c\u010d\5J%\2\u010d\u010e\7<\2\2\u010e\u010f\5J%\2\u010f\u0110"+
		"\5J%\2\u0110\u0111\7<\2\2\u0111\u0112\5J%\2\u0112\u0113\5J%\2\u0113\u0115"+
		"\3\2\2\2\u0114\u00fb\3\2\2\2\u0114\u0103\3\2\2\2\u0114\u010b\3\2\2\2\u0115"+
		";\3\2\2\2\u0116\u0117\7\61\2\2\u0117=\3\2\2\2\u0118\u0119\5L&\2\u0119"+
		"?\3\2\2\2\u011a\u011b\5N\'\2\u011bA\3\2\2\2\u011c\u011e\5R)\2\u011d\u011c"+
		"\3\2\2\2\u011e\u011f\3\2\2\2\u011f\u011d\3\2\2\2\u011f\u0120\3\2\2\2\u0120"+
		"C\3\2\2\2\u0121\u0122\7w\2\2\u0122\u0123\7d\2\2\u0123\u0124\7g\2\2\u0124"+
		"\u0125\7u\2\2\u0125\u0126\7v\2\2\u0126\u0127\7\61\2\2\u0127\u0128\7o\2"+
		"\2\u0128\u0129\7d\2\2\u0129\u012a\7g\2\2\u012a\u012b\7u\2\2\u012b\u012c"+
		"\7v\2\2\u012c\u012d\7<\2\2\u012d\u012e\7\"\2\2\u012e\u0130\3\2\2\2\u012f"+
		"\u0131\5J%\2\u0130\u012f\3\2\2\2\u0131\u0132\3\2\2\2\u0132\u0130\3\2\2"+
		"\2\u0132\u0133\3\2\2\2\u0133\u0134\3\2\2\2\u0134\u0136\7\61\2\2\u0135"+
		"\u0137\5J%\2\u0136\u0135\3\2\2\2\u0137\u0138\3\2\2\2\u0138\u0136\3\2\2"+
		"\2\u0138\u0139\3\2\2\2\u0139E\3\2\2\2\u013a\u013c\5X,\2\u013b\u013a\3"+
		"\2\2\2\u013c\u013d\3\2\2\2\u013d\u013b\3\2\2\2\u013d\u013e\3\2\2\2\u013e"+
		"\u013f\3\2\2\2\u013f\u0140\b#\2\2\u0140G\3\2\2\2\u0141\u0151\5J%\2\u0142"+
		"\u0143\5V+\2\u0143\u0144\5J%\2\u0144\u0151\3\2\2\2\u0145\u0146\7\63\2"+
		"\2\u0146\u0147\5J%\2\u0147\u0148\5J%\2\u0148\u0151\3\2\2\2\u0149\u014a"+
		"\7\64\2\2\u014a\u014b\t\2\2\2\u014b\u0151\5J%\2\u014c\u014d\7\64\2\2\u014d"+
		"\u014e\7\67\2\2\u014e\u014f\3\2\2\2\u014f\u0151\t\3\2\2\u0150\u0141\3"+
		"\2\2\2\u0150\u0142\3\2\2\2\u0150\u0145\3\2\2\2\u0150\u0149\3\2\2\2\u0150"+
		"\u014c\3\2\2\2\u0151I\3\2\2\2\u0152\u0153\t\4\2\2\u0153K\3\2\2\2\u0154"+
		"\u0155\5H$\2\u0155\u0156\7\60\2\2\u0156\u0157\5H$\2\u0157\u0158\7\60\2"+
		"\2\u0158\u0159\5H$\2\u0159\u015a\7\60\2\2\u015a\u015b\5H$\2\u015bM\3\2"+
		"\2\2\u015c\u015d\5L&\2\u015d\u015e\7\61\2\2\u015e\u015f\5P(\2\u015fO\3"+
		"\2\2\2\u0160\u0166\5J%\2\u0161\u0162\t\5\2\2\u0162\u0166\5J%\2\u0163\u0164"+
		"\t\6\2\2\u0164\u0166\t\7\2\2\u0165\u0160\3\2\2\2\u0165\u0161\3\2\2\2\u0165"+
		"\u0163\3\2\2\2\u0166Q\3\2\2\2\u0167\u0168\t\b\2\2\u0168S\3\2\2\2\u0169"+
		"\u016a\n\b\2\2\u016aU\3\2\2\2\u016b\u016c\t\t\2\2\u016cW\3\2\2\2\u016d"+
		"\u016e\t\n\2\2\u016eY\3\2\2\2\u016f\u0171\n\13\2\2\u0170\u016f\3\2\2\2"+
		"\u0171\u0172\3\2\2\2\u0172\u0170\3\2\2\2\u0172\u0173\3\2\2\2\u0173\u0174"+
		"\3\2\2\2\u0174\u0175\b-\4\2\u0175[\3\2\2\2\u0176\u0177\7$\2\2\u0177\u0178"+
		"\3\2\2\2\u0178\u0179\b.\5\2\u0179\u017a\b.\6\2\u017a]\3\2\2\2\20\2\3\u00e9"+
		"\u00f4\u00fd\u0107\u0114\u011f\u0132\u0138\u013d\u0150\u0165\u0172\7\2"+
		"\3\2\7\3\2\t\3\2\t\36\2\6\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}