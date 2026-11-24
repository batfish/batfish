package org.batfish.grammar;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Interval;
import org.batfish.common.BatfishException;
import org.batfish.grammar.flattener.FlattenerLineMap;

public abstract class BatfishCombinedParser<P extends BatfishParser, L extends BatfishLexer> {

  static {
    fillIntervalCache();
  }

  /**
   * Creates every {@link Interval} that ANTLR caches.
   *
   * <p>ANTLR 4.13.2 creates the interval {@code i..i}, for {@code i} up to {@link
   * Interval#INTERVAL_POOL_MAX_VALUE}, on first use and caches it in a static array. It stores the
   * interval without synchronization, so another thread can read a cached interval before it can
   * see its bounds, as {@code 0..0} (https://github.com/antlr/antlr4/issues/4901). The text of a
   * one-character token and the lookahead sets come from these intervals.
   *
   * <p>This runs when this class is initialized, which is before it creates a lexer or parser, so
   * every thread that creates a combined parser sees the cache full.
   *
   * <p>ANTLR removed the cache after 4.13.2. Remove this once Batfish uses a release without it;
   * {@code INTERVAL_POOL_MAX_VALUE} no longer exists there.
   */
  private static void fillIntervalCache() {
    for (int i = 0; i <= Interval.INTERVAL_POOL_MAX_VALUE; i++) {
      Interval.of(i, i);
    }
  }

  /** ATNs for which {@link #computeLookaheadSets(ATN)} has run. */
  private static final ConcurrentMap<ATN, Boolean> ATNS_WITH_LOOKAHEAD_SETS =
      new ConcurrentHashMap<>();

  /**
   * Computes the lookahead set of every state of {@code atn}, the first time this is called for
   * that ATN.
   *
   * <p>ANTLR computes each state's lookahead set on first use and caches it in the ATN, which all
   * parsers of a grammar share. It stores the set without synchronization, so a parser on another
   * thread can read a set before it can see all of its elements, and then reports a syntax error on
   * valid input (https://github.com/antlr/antlr4/issues/4999, seen on aarch64). After this method
   * returns, parsing no longer writes that cache, and every thread that has called this method sees
   * the sets complete.
   *
   * <p>Remove once Batfish uses an ANTLR release in which that issue is fixed.
   */
  private static void computeLookaheadSets(ATN atn) {
    ATNS_WITH_LOOKAHEAD_SETS.computeIfAbsent(
        atn,
        a -> {
          for (ATNState state : a.states) {
            if (state != null) {
              a.nextTokens(state);
            }
          }
          return true;
        });
  }

  private int _currentModeStart;

  private final List<String> _errors;

  private String _input;

  private String[] _inputLines;

  protected L _lexer;

  private BatfishLexerErrorListener _lexerErrorListener;

  private FlattenerLineMap _lineMap;

  protected P _parser;

  private BatfishParserErrorListener _parserErrorListener;

  private boolean _recovery;

  private GrammarSettings _settings;

  private List<Integer> _tokenModes;

  protected CommonTokenStream _tokens;

  private final List<String> _warnings;

  public BatfishCombinedParser(
      Class<P> pClass, Class<L> lClass, String input, GrammarSettings settings) {
    _settings = settings;
    _tokenModes = new ArrayList<>();
    _currentModeStart = 0;
    _warnings = new ArrayList<>();
    _errors = new ArrayList<>();
    _input = input;
    _lineMap = null;
    CharStream inputStream = CharStreams.fromString(input);
    try {
      _lexer = lClass.getConstructor(CharStream.class).newInstance(inputStream);
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new BatfishException("Error constructing lexer using reflection", e);
    }
    _lexer.initErrorListener(this);
    _tokens = new CommonTokenStream(_lexer);
    try {
      _parser = pClass.getConstructor(TokenStream.class).newInstance(_tokens);
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new Error(e);
    }
    computeLookaheadSets(_parser.getATN());
    _parser.initErrorListener(this);
    _parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
  }

  public BatfishCombinedParser(
      Class<P> pClass,
      Class<L> lClass,
      String input,
      GrammarSettings settings,
      BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory batfishANTLRErrorStrategyFactory,
      Set<Integer> separatorChars) {
    this(pClass, lClass, input, settings);
    /*
     * Do not supply recovery infrastructure with associated overhead unless recovery is actually
     * enabled.
     */
    if (!settings.getDisableUnrecognized()) {
      _parser.setInterpreter(new BatfishParserATNSimulator(_parser.getInterpreter()));
      _parser.setErrorHandler(batfishANTLRErrorStrategyFactory.build(_input));
      _lexer.setRecoveryStrategy(new BatfishLexerRecoveryStrategy(_lexer, separatorChars));
      _recovery = true;
    }
  }

  public BatfishCombinedParser(
      Class<P> pClass,
      Class<L> lClass,
      String input,
      GrammarSettings settings,
      BatfishANTLRErrorStrategy.BatfishANTLRErrorStrategyFactory batfishANTLRErrorStrategyFactor,
      Set<Integer> separatorChars,
      FlattenerLineMap lineMap) {
    this(pClass, lClass, input, settings, batfishANTLRErrorStrategyFactor, separatorChars);
    _lineMap = lineMap;
  }

  /**
   * Escapes certain whitespace {@code \n, \r, \t} in the given token text. This is typically used
   * when printing token text for debugging purposes.
   */
  public static String escape(String offendingTokenText) {
    return offendingTokenText.replace("\n", "\\n").replace("\t", "\\t").replace("\r", "\\r");
  }

  public List<String> getErrors() {
    return _errors;
  }

  public String getInput() {
    return _input;
  }

  public L getLexer() {
    return _lexer;
  }

  public BatfishLexerErrorListener getLexerErrorListener() {
    return _lexerErrorListener;
  }

  /** Get line number for a specified token, applying line mapping if applicable. */
  public int getLine(@Nonnull Token t) {
    int line = t.getLine();
    return (_lineMap == null) ? line : _lineMap.getOriginalLine(line, t.getCharPositionInLine());
  }

  public String[] getInputLines() {
    if (_inputLines == null) {
      _inputLines = _input.split("\n", -1);
    }
    return _inputLines;
  }

  public P getParser() {
    return _parser;
  }

  public BatfishParserErrorListener getParserErrorListener() {
    return _parserErrorListener;
  }

  /**
   * Returns {@code true} iff this is grammar uses custom recovery infrastructure, e.g. via {@link
   * BatfishANTLRErrorStrategy}. For non-recovery-based grammars, this should return {@code false}
   * even when unrecognized lines are allowed.
   */
  public boolean getRecovery() {
    return _recovery;
  }

  public GrammarSettings getSettings() {
    return _settings;
  }

  public int getTokenMode(Token t) {
    int tokenIndex = t.getTokenIndex();
    if (tokenIndex == -1) {
      // token probably added manually, not by parser
      return -1;
    }
    if (tokenIndex < _tokenModes.size()) {
      return _tokenModes.get(tokenIndex);
    } else {
      return _lexer._mode;
    }
  }

  public CommonTokenStream getTokens() {
    return _tokens;
  }

  public List<String> getWarnings() {
    return _warnings;
  }

  public abstract ParserRuleContext parse();

  public void setLexerErrorListener(BatfishLexerErrorListener lexerErrorListener) {
    _lexerErrorListener = lexerErrorListener;
  }

  public void setParserErrorListener(BatfishParserErrorListener parserErrorListener) {
    _parserErrorListener = parserErrorListener;
  }

  public void updateTokenModes(int mode) {
    for (int i = _currentModeStart; i <= _tokens.size(); i++) {
      _tokenModes.add(mode);
    }
    _currentModeStart = _tokens.size() + 1;
  }

  /**
   * Returns the name of the parser rule corresponding to the given context. Behavior is undefined
   * if the context was produced by a different parser.
   */
  public @Nonnull String getRuleName(ParserRuleContext ctx) {
    return _parser.getRuleNames()[ctx.getRuleIndex()];
  }

  public @Nonnull Map<Integer, Set<Integer>> getExtraLines() {
    return _lineMap != null ? _lineMap.getExtraLines() : ImmutableMap.of();
  }
}
