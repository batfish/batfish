// Generated from org/batfish/grammar/routing_table/ios/IosRoutingTableLexer.g4 by ANTLR 4.7.2
package org.batfish.grammar.routing_table.ios;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class IosRoutingTableLexer extends org.batfish.grammar.BatfishLexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		CODES=1, GATEWAY=2, IS_DIRECTLY_CONNECTED=3, IS_VARIABLY_SUBNETTED=4, 
		NAME=5, VIA=6, VRF=7, ASTERISK=8, BRACKET_LEFT=9, BRACKET_RIGHT=10, COLON=11, 
		COMMA=12, COMMENT=13, DASH=14, PERCENT=15, PLUS=16, DEC=17, FORWARD_SLASH=18, 
		IP_ADDRESS=19, IP_PREFIX=20, NEWLINE=21, WORD=22, WS=23;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"CODES", "GATEWAY", "IS_DIRECTLY_CONNECTED", "IS_VARIABLY_SUBNETTED", 
			"NAME", "VIA", "VRF", "ASTERISK", "BRACKET_LEFT", "BRACKET_RIGHT", "COLON", 
			"COMMA", "COMMENT", "DASH", "PERCENT", "PLUS", "DEC", "FORWARD_SLASH", 
			"IP_ADDRESS", "IP_PREFIX", "NEWLINE", "WORD", "WS", "F_DecByte", "F_Digit", 
			"F_IpAddress", "F_IpPrefix", "F_IpPrefixLength", "F_Newline", "F_NonNewline", 
			"F_PositiveDigit", "F_Whitespace"
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



	public IosRoutingTableLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "IosRoutingTableLexer.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\31\u010e\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\b"+
		"\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\6\16"+
		"\u0097\n\16\r\16\16\16\u0098\3\16\6\16\u009c\n\16\r\16\16\16\u009d\3\16"+
		"\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\6\22\u00a9\n\22\r\22\16\22\u00aa"+
		"\3\23\3\23\3\24\3\24\3\25\3\25\3\26\6\26\u00b4\n\26\r\26\16\26\u00b5\3"+
		"\27\6\27\u00b9\n\27\r\27\16\27\u00ba\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\5\27\u00d8\n\27\3\30\6\30\u00db\n\30\r"+
		"\30\16\30\u00dc\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31"+
		"\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u00f0\n\31\3\32\3\32\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\35"+
		"\5\35\u0105\n\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\2\2\"\3\3\5\4\7\5\t\6"+
		"\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24"+
		"\'\25)\26+\27-\30/\31\61\2\63\2\65\2\67\29\2;\2=\2?\2A\2\3\2\f\5\2\62"+
		";C\\c|\3\2\62\66\3\2\62\67\3\2\62;\3\2\63\64\3\2\65\65\3\2\62\64\4\2\f"+
		"\f\17\17\3\2\63;\4\2\13\13\"\"\2\u0114\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2"+
		"\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2"+
		"\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3"+
		"\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3"+
		"\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\3C\3\2\2\2\5I\3\2\2\2\7Q\3\2\2"+
		"\2\tg\3\2\2\2\13}\3\2\2\2\r\u0082\3\2\2\2\17\u0086\3\2\2\2\21\u008a\3"+
		"\2\2\2\23\u008c\3\2\2\2\25\u008e\3\2\2\2\27\u0090\3\2\2\2\31\u0092\3\2"+
		"\2\2\33\u0094\3\2\2\2\35\u00a1\3\2\2\2\37\u00a3\3\2\2\2!\u00a5\3\2\2\2"+
		"#\u00a8\3\2\2\2%\u00ac\3\2\2\2\'\u00ae\3\2\2\2)\u00b0\3\2\2\2+\u00b3\3"+
		"\2\2\2-\u00d7\3\2\2\2/\u00da\3\2\2\2\61\u00ef\3\2\2\2\63\u00f1\3\2\2\2"+
		"\65\u00f3\3\2\2\2\67\u00fb\3\2\2\29\u0104\3\2\2\2;\u0106\3\2\2\2=\u0108"+
		"\3\2\2\2?\u010a\3\2\2\2A\u010c\3\2\2\2CD\7E\2\2DE\7q\2\2EF\7f\2\2FG\7"+
		"g\2\2GH\7u\2\2H\4\3\2\2\2IJ\7I\2\2JK\7c\2\2KL\7v\2\2LM\7g\2\2MN\7y\2\2"+
		"NO\7c\2\2OP\7{\2\2P\6\3\2\2\2QR\7k\2\2RS\7u\2\2ST\7\"\2\2TU\7f\2\2UV\7"+
		"k\2\2VW\7t\2\2WX\7g\2\2XY\7e\2\2YZ\7v\2\2Z[\7n\2\2[\\\7{\2\2\\]\7\"\2"+
		"\2]^\7e\2\2^_\7q\2\2_`\7p\2\2`a\7p\2\2ab\7g\2\2bc\7e\2\2cd\7v\2\2de\7"+
		"g\2\2ef\7f\2\2f\b\3\2\2\2gh\7k\2\2hi\7u\2\2ij\7\"\2\2jk\7x\2\2kl\7c\2"+
		"\2lm\7t\2\2mn\7k\2\2no\7c\2\2op\7d\2\2pq\7n\2\2qr\7{\2\2rs\7\"\2\2st\7"+
		"u\2\2tu\7w\2\2uv\7d\2\2vw\7p\2\2wx\7g\2\2xy\7v\2\2yz\7v\2\2z{\7g\2\2{"+
		"|\7f\2\2|\n\3\2\2\2}~\7p\2\2~\177\7c\2\2\177\u0080\7o\2\2\u0080\u0081"+
		"\7g\2\2\u0081\f\3\2\2\2\u0082\u0083\7x\2\2\u0083\u0084\7k\2\2\u0084\u0085"+
		"\7c\2\2\u0085\16\3\2\2\2\u0086\u0087\7X\2\2\u0087\u0088\7T\2\2\u0088\u0089"+
		"\7H\2\2\u0089\20\3\2\2\2\u008a\u008b\7,\2\2\u008b\22\3\2\2\2\u008c\u008d"+
		"\7]\2\2\u008d\24\3\2\2\2\u008e\u008f\7_\2\2\u008f\26\3\2\2\2\u0090\u0091"+
		"\7<\2\2\u0091\30\3\2\2\2\u0092\u0093\7.\2\2\u0093\32\3\2\2\2\u0094\u0096"+
		"\7#\2\2\u0095\u0097\5=\37\2\u0096\u0095\3\2\2\2\u0097\u0098\3\2\2\2\u0098"+
		"\u0096\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u009b\3\2\2\2\u009a\u009c\5;"+
		"\36\2\u009b\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009b\3\2\2\2\u009d"+
		"\u009e\3\2\2\2\u009e\u009f\3\2\2\2\u009f\u00a0\b\16\2\2\u00a0\34\3\2\2"+
		"\2\u00a1\u00a2\7/\2\2\u00a2\36\3\2\2\2\u00a3\u00a4\7\'\2\2\u00a4 \3\2"+
		"\2\2\u00a5\u00a6\7-\2\2\u00a6\"\3\2\2\2\u00a7\u00a9\5\63\32\2\u00a8\u00a7"+
		"\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00a8\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab"+
		"$\3\2\2\2\u00ac\u00ad\7\61\2\2\u00ad&\3\2\2\2\u00ae\u00af\5\65\33\2\u00af"+
		"(\3\2\2\2\u00b0\u00b1\5\67\34\2\u00b1*\3\2\2\2\u00b2\u00b4\5;\36\2\u00b3"+
		"\u00b2\3\2\2\2\u00b4\u00b5\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b5\u00b6\3\2"+
		"\2\2\u00b6,\3\2\2\2\u00b7\u00b9\t\2\2\2\u00b8\u00b7\3\2\2\2\u00b9\u00ba"+
		"\3\2\2\2\u00ba\u00b8\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb\u00d8\3\2\2\2\u00bc"+
		"\u00bd\7K\2\2\u00bd\u00be\7U\2\2\u00be\u00bf\7/\2\2\u00bf\u00c0\7K\2\2"+
		"\u00c0\u00d8\7U\2\2\u00c1\u00c2\7n\2\2\u00c2\u00c3\7g\2\2\u00c3\u00c4"+
		"\7x\2\2\u00c4\u00c5\7g\2\2\u00c5\u00c6\7n\2\2\u00c6\u00c7\7/\2\2\u00c7"+
		"\u00d8\7\63\2\2\u00c8\u00c9\7n\2\2\u00c9\u00ca\7g\2\2\u00ca\u00cb\7x\2"+
		"\2\u00cb\u00cc\7g\2\2\u00cc\u00cd\7n\2\2\u00cd\u00ce\7/\2\2\u00ce\u00d8"+
		"\7\64\2\2\u00cf\u00d0\7r\2\2\u00d0\u00d1\7g\2\2\u00d1\u00d2\7t\2\2\u00d2"+
		"\u00d3\7/\2\2\u00d3\u00d4\7w\2\2\u00d4\u00d5\7u\2\2\u00d5\u00d6\7g\2\2"+
		"\u00d6\u00d8\7t\2\2\u00d7\u00b8\3\2\2\2\u00d7\u00bc\3\2\2\2\u00d7\u00c1"+
		"\3\2\2\2\u00d7\u00c8\3\2\2\2\u00d7\u00cf\3\2\2\2\u00d8.\3\2\2\2\u00d9"+
		"\u00db\5A!\2\u00da\u00d9\3\2\2\2\u00db\u00dc\3\2\2\2\u00dc\u00da\3\2\2"+
		"\2\u00dc\u00dd\3\2\2\2\u00dd\u00de\3\2\2\2\u00de\u00df\b\30\2\2\u00df"+
		"\60\3\2\2\2\u00e0\u00f0\5\63\32\2\u00e1\u00e2\5? \2\u00e2\u00e3\5\63\32"+
		"\2\u00e3\u00f0\3\2\2\2\u00e4\u00e5\7\63\2\2\u00e5\u00e6\5\63\32\2\u00e6"+
		"\u00e7\5\63\32\2\u00e7\u00f0\3\2\2\2\u00e8\u00e9\7\64\2\2\u00e9\u00ea"+
		"\t\3\2\2\u00ea\u00f0\5\63\32\2\u00eb\u00ec\7\64\2\2\u00ec\u00ed\7\67\2"+
		"\2\u00ed\u00ee\3\2\2\2\u00ee\u00f0\t\4\2\2\u00ef\u00e0\3\2\2\2\u00ef\u00e1"+
		"\3\2\2\2\u00ef\u00e4\3\2\2\2\u00ef\u00e8\3\2\2\2\u00ef\u00eb\3\2\2\2\u00f0"+
		"\62\3\2\2\2\u00f1\u00f2\t\5\2\2\u00f2\64\3\2\2\2\u00f3\u00f4\5\61\31\2"+
		"\u00f4\u00f5\7\60\2\2\u00f5\u00f6\5\61\31\2\u00f6\u00f7\7\60\2\2\u00f7"+
		"\u00f8\5\61\31\2\u00f8\u00f9\7\60\2\2\u00f9\u00fa\5\61\31\2\u00fa\66\3"+
		"\2\2\2\u00fb\u00fc\5\65\33\2\u00fc\u00fd\7\61\2\2\u00fd\u00fe\59\35\2"+
		"\u00fe8\3\2\2\2\u00ff\u0105\5\63\32\2\u0100\u0101\t\6\2\2\u0101\u0105"+
		"\5\63\32\2\u0102\u0103\t\7\2\2\u0103\u0105\t\b\2\2\u0104\u00ff\3\2\2\2"+
		"\u0104\u0100\3\2\2\2\u0104\u0102\3\2\2\2\u0105:\3\2\2\2\u0106\u0107\t"+
		"\t\2\2\u0107<\3\2\2\2\u0108\u0109\n\t\2\2\u0109>\3\2\2\2\u010a\u010b\t"+
		"\n\2\2\u010b@\3\2\2\2\u010c\u010d\t\13\2\2\u010dB\3\2\2\2\f\2\u0098\u009d"+
		"\u00aa\u00b5\u00ba\u00d7\u00dc\u00ef\u0104\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}