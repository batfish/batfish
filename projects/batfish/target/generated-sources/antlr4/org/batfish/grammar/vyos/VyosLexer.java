// Generated from org/batfish/grammar/vyos/VyosLexer.g4 by ANTLR 4.7.2
package org.batfish.grammar.vyos;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class VyosLexer extends org.batfish.grammar.BatfishLexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		CLOSE_BRACE=1, CLOSE_PAREN=2, LINE_COMMENT=3, MULTILINE_COMMENT=4, NEWLINE=5, 
		OPEN_BRACE=6, OPEN_PAREN=7, SEMICOLON=8, WORD=9, WS=10;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"CLOSE_BRACE", "CLOSE_PAREN", "LINE_COMMENT", "MULTILINE_COMMENT", "NEWLINE", 
			"OPEN_BRACE", "OPEN_PAREN", "SEMICOLON", "WORD", "WS", "F_NewlineChar", 
			"F_NonNewlineChar", "F_ParenString", "F_QuotedString", "F_WhitespaceChar", 
			"F_WordChar"
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



	public VyosLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "VyosLexer.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\fx\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\3\2\3"+
		"\3\3\3\3\4\3\4\7\4*\n\4\f\4\16\4-\13\4\3\4\6\4\60\n\4\r\4\16\4\61\3\4"+
		"\3\4\3\5\3\5\3\5\3\5\7\5:\n\5\f\5\16\5=\13\5\3\5\3\5\3\5\3\5\3\5\3\6\6"+
		"\6E\n\6\r\6\16\6F\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\6\nR\n\n\r\n\16"+
		"\nS\5\nV\n\n\3\13\6\13Y\n\13\r\13\16\13Z\3\13\3\13\3\f\3\f\3\r\3\r\3\16"+
		"\3\16\7\16e\n\16\f\16\16\16h\13\16\3\16\3\16\3\17\3\17\7\17n\n\17\f\17"+
		"\16\17q\13\17\3\17\3\17\3\20\3\20\3\21\3\21\3;\2\22\3\3\5\4\7\5\t\6\13"+
		"\7\r\b\17\t\21\n\23\13\25\f\27\2\31\2\33\2\35\2\37\2!\2\3\2\7\4\2\f\f"+
		"\17\17\3\2++\3\2$$\5\2\13\13\16\16\"\"\f\2\13\f\16\17\"\"$%*+==]]__}}"+
		"\177\177\2{\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2"+
		"\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\3#"+
		"\3\2\2\2\5%\3\2\2\2\7\'\3\2\2\2\t\65\3\2\2\2\13D\3\2\2\2\rH\3\2\2\2\17"+
		"J\3\2\2\2\21L\3\2\2\2\23U\3\2\2\2\25X\3\2\2\2\27^\3\2\2\2\31`\3\2\2\2"+
		"\33b\3\2\2\2\35k\3\2\2\2\37t\3\2\2\2!v\3\2\2\2#$\7\177\2\2$\4\3\2\2\2"+
		"%&\7+\2\2&\6\3\2\2\2\'+\7%\2\2(*\5\31\r\2)(\3\2\2\2*-\3\2\2\2+)\3\2\2"+
		"\2+,\3\2\2\2,/\3\2\2\2-+\3\2\2\2.\60\5\27\f\2/.\3\2\2\2\60\61\3\2\2\2"+
		"\61/\3\2\2\2\61\62\3\2\2\2\62\63\3\2\2\2\63\64\b\4\2\2\64\b\3\2\2\2\65"+
		"\66\7\61\2\2\66\67\7,\2\2\67;\3\2\2\28:\13\2\2\298\3\2\2\2:=\3\2\2\2;"+
		"<\3\2\2\2;9\3\2\2\2<>\3\2\2\2=;\3\2\2\2>?\7,\2\2?@\7\61\2\2@A\3\2\2\2"+
		"AB\b\5\2\2B\n\3\2\2\2CE\5\27\f\2DC\3\2\2\2EF\3\2\2\2FD\3\2\2\2FG\3\2\2"+
		"\2G\f\3\2\2\2HI\7}\2\2I\16\3\2\2\2JK\7*\2\2K\20\3\2\2\2LM\7=\2\2M\22\3"+
		"\2\2\2NV\5\35\17\2OV\5\33\16\2PR\5!\21\2QP\3\2\2\2RS\3\2\2\2SQ\3\2\2\2"+
		"ST\3\2\2\2TV\3\2\2\2UN\3\2\2\2UO\3\2\2\2UQ\3\2\2\2V\24\3\2\2\2WY\5\37"+
		"\20\2XW\3\2\2\2YZ\3\2\2\2ZX\3\2\2\2Z[\3\2\2\2[\\\3\2\2\2\\]\b\13\2\2]"+
		"\26\3\2\2\2^_\t\2\2\2_\30\3\2\2\2`a\n\2\2\2a\32\3\2\2\2bf\7*\2\2ce\n\3"+
		"\2\2dc\3\2\2\2eh\3\2\2\2fd\3\2\2\2fg\3\2\2\2gi\3\2\2\2hf\3\2\2\2ij\7+"+
		"\2\2j\34\3\2\2\2ko\7$\2\2ln\n\4\2\2ml\3\2\2\2nq\3\2\2\2om\3\2\2\2op\3"+
		"\2\2\2pr\3\2\2\2qo\3\2\2\2rs\7$\2\2s\36\3\2\2\2tu\t\5\2\2u \3\2\2\2vw"+
		"\n\6\2\2w\"\3\2\2\2\f\2+\61;FSUZfo\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}