// Generated from org/batfish/grammar/juniper/JuniperLexer.g4 by ANTLR 4.7.2
package org.batfish.grammar.juniper;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JuniperLexer extends org.batfish.grammar.BatfishLexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		REPLACE=1, CLOSE_BRACE=2, CLOSE_BRACKET=3, CLOSE_PAREN=4, INACTIVE=5, 
		LINE_COMMENT=6, MULTILINE_COMMENT=7, OPEN_BRACE=8, OPEN_BRACKET=9, OPEN_PAREN=10, 
		SEMICOLON=11, WORD=12, WS=13;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"REPLACE", "CLOSE_BRACE", "CLOSE_BRACKET", "CLOSE_PAREN", "INACTIVE", 
			"LINE_COMMENT", "MULTILINE_COMMENT", "OPEN_BRACE", "OPEN_BRACKET", "OPEN_PAREN", 
			"SEMICOLON", "WORD", "WS", "F_NewlineChar", "F_NonNewlineChar", "F_ParenString", 
			"F_QuotedString", "F_WhitespaceChar", "F_WordChar"
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


	boolean enableIPV6_ADDRESS = true;
	boolean enableIP_ADDRESS = true;
	boolean enableDEC = true;

	@Override
	public String printStateVariables() {
	   StringBuilder sb = new StringBuilder();
	   sb.append("enableIPV6_ADDRESS: " + enableIPV6_ADDRESS + "\n");
	   sb.append("enableIP_ADDRESS: " + enableIP_ADDRESS + "\n");
	   sb.append("enableDEC: " + enableDEC + "\n");
	   return sb.toString();
	}



	public JuniperLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JuniperLexer.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\17\u0090\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3"+
		"\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\7\7E"+
		"\n\7\f\7\16\7H\13\7\3\7\6\7K\n\7\r\7\16\7L\3\7\3\7\3\b\3\b\3\b\3\b\7\b"+
		"U\n\b\f\b\16\bX\13\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f"+
		"\3\f\3\r\3\r\3\r\6\rj\n\r\r\r\16\rk\5\rn\n\r\3\16\6\16q\n\16\r\16\16\16"+
		"r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\7\21}\n\21\f\21\16\21\u0080"+
		"\13\21\3\21\3\21\3\22\3\22\7\22\u0086\n\22\f\22\16\22\u0089\13\22\3\22"+
		"\3\22\3\23\3\23\3\24\3\24\3V\2\25\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23"+
		"\13\25\f\27\r\31\16\33\17\35\2\37\2!\2#\2%\2\'\2\3\2\b\4\2##%%\4\2\f\f"+
		"\17\17\3\2++\3\2$$\5\2\13\f\16\17\"\"\f\2\13\f\16\17\"\"$%*+==]]__}}\177"+
		"\177\2\u0092\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2"+
		"\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2"+
		"\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\3)\3\2\2\2\5\62\3\2\2\2\7\64\3\2"+
		"\2\2\t\66\3\2\2\2\138\3\2\2\2\rB\3\2\2\2\17P\3\2\2\2\21^\3\2\2\2\23`\3"+
		"\2\2\2\25b\3\2\2\2\27d\3\2\2\2\31m\3\2\2\2\33p\3\2\2\2\35v\3\2\2\2\37"+
		"x\3\2\2\2!z\3\2\2\2#\u0083\3\2\2\2%\u008c\3\2\2\2\'\u008e\3\2\2\2)*\7"+
		"t\2\2*+\7g\2\2+,\7r\2\2,-\7n\2\2-.\7c\2\2./\7e\2\2/\60\7g\2\2\60\61\7"+
		"<\2\2\61\4\3\2\2\2\62\63\7\177\2\2\63\6\3\2\2\2\64\65\7_\2\2\65\b\3\2"+
		"\2\2\66\67\7+\2\2\67\n\3\2\2\289\7k\2\29:\7p\2\2:;\7c\2\2;<\7e\2\2<=\7"+
		"v\2\2=>\7k\2\2>?\7x\2\2?@\7g\2\2@A\7<\2\2A\f\3\2\2\2BF\t\2\2\2CE\5\37"+
		"\20\2DC\3\2\2\2EH\3\2\2\2FD\3\2\2\2FG\3\2\2\2GJ\3\2\2\2HF\3\2\2\2IK\5"+
		"\35\17\2JI\3\2\2\2KL\3\2\2\2LJ\3\2\2\2LM\3\2\2\2MN\3\2\2\2NO\b\7\2\2O"+
		"\16\3\2\2\2PQ\7\61\2\2QR\7,\2\2RV\3\2\2\2SU\13\2\2\2TS\3\2\2\2UX\3\2\2"+
		"\2VW\3\2\2\2VT\3\2\2\2WY\3\2\2\2XV\3\2\2\2YZ\7,\2\2Z[\7\61\2\2[\\\3\2"+
		"\2\2\\]\b\b\2\2]\20\3\2\2\2^_\7}\2\2_\22\3\2\2\2`a\7]\2\2a\24\3\2\2\2"+
		"bc\7*\2\2c\26\3\2\2\2de\7=\2\2e\30\3\2\2\2fn\5#\22\2gn\5!\21\2hj\5\'\24"+
		"\2ih\3\2\2\2jk\3\2\2\2ki\3\2\2\2kl\3\2\2\2ln\3\2\2\2mf\3\2\2\2mg\3\2\2"+
		"\2mi\3\2\2\2n\32\3\2\2\2oq\5%\23\2po\3\2\2\2qr\3\2\2\2rp\3\2\2\2rs\3\2"+
		"\2\2st\3\2\2\2tu\b\16\2\2u\34\3\2\2\2vw\t\3\2\2w\36\3\2\2\2xy\n\3\2\2"+
		"y \3\2\2\2z~\7*\2\2{}\n\4\2\2|{\3\2\2\2}\u0080\3\2\2\2~|\3\2\2\2~\177"+
		"\3\2\2\2\177\u0081\3\2\2\2\u0080~\3\2\2\2\u0081\u0082\7+\2\2\u0082\"\3"+
		"\2\2\2\u0083\u0087\7$\2\2\u0084\u0086\n\5\2\2\u0085\u0084\3\2\2\2\u0086"+
		"\u0089\3\2\2\2\u0087\u0085\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u008a\3\2"+
		"\2\2\u0089\u0087\3\2\2\2\u008a\u008b\7$\2\2\u008b$\3\2\2\2\u008c\u008d"+
		"\t\6\2\2\u008d&\3\2\2\2\u008e\u008f\n\7\2\2\u008f(\3\2\2\2\13\2FLVkmr"+
		"~\u0087\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}