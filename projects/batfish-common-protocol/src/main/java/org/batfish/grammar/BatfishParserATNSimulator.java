package org.batfish.grammar;

import static org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER;

import javax.annotation.Nonnull;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.misc.Interval;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishRecognitionException;

/**
 * Clones an existing {@link ParserATNSimulator} used by a {@link BatfishParser} and wraps the
 * {@link #adaptivePredict} function with a recovery mechanism. Intended for use with a {@link
 * BatfishParser} employing a {@link BatfishANTLRErrorStrategy} as its error recovery strategy.
 */
public class BatfishParserATNSimulator extends ParserATNSimulator {

  private BatfishParser _parser;

  /**
   * Construct a {@link BatfishParserATNSimulator} from a {@link ParserATNSimulator}
   *
   * @param parent The {@link ParserATNSimulator} to emulate modulo {@link #adaptivePredict}.
   */
  public BatfishParserATNSimulator(ParserATNSimulator parent) {
    super(parent.getParser(), parent.atn, parent.decisionToDFA, parent.getSharedContextCache());
    this.setPredictionMode(parent.getPredictionMode());
    _parser = (BatfishParser) parser;
  }

  @Override
  public int adaptivePredict(TokenStream input, int decision, ParserRuleContext outerContext) {
    while (true) {
      try {
        return super.adaptivePredict(wrap(input), decision, outerContext);
      } catch (NoViableAltException e) {
        int line = _parser.getCurrentToken().getLine();
        try {
          // Since adaptive prediction has failed, throw out current line.
          _parser.createErrorNodeLine();
        } catch (BatfishRecognitionException re) {
          // Handle adaptive prediction failure that isn't satisfied by EOF.
          break;
        }
        if (line == outerContext.getStart().getLine()) {
          // To throw out this line we should also throw out the outer context.
          break;
        }
        // Try again to save the outer context.
      }
    }

    return INVALID_ALT_NUMBER;
  }

  /**
   * Wrap {@link TokenStream} so that when {@link BatfishLexer#UNMATCHABLE_TOKEN} is returned, the
   * stream is rewound and a {@link NoViableAltException} is thrown. This allows us to remove the
   * line and try adaptive prediction again.
   *
   * <p>When adaptive prediction is used to determine whether to exit a loop, it treats <i>any</i>
   * token not matching the loop grammar as an indication that the loop should be exited - even if
   * that token would be illegal at a higher level. This behavior is undesired for {@link
   * BatfishLexer#UNMATCHABLE_TOKEN}, because we know it will always be illegal at every level.
   */
  private @Nonnull TokenStream wrap(TokenStream input) {
    return new WrappedTokenStream(input, _parser);
  }

  /** See {@link #wrap}. */
  private static class WrappedTokenStream implements TokenStream {

    @Override
    public void consume() {
      _input.consume();
    }

    @Override
    public int LA(int i) {
      // Should only be used by adaptive prediction to look at very next token
      assert i == 1;
      int nextToken = _input.LA(i);
      if (nextToken == BatfishLexer.UNMATCHABLE_TOKEN) {
        // rewind the token stream and throw so we can do custom recovery and try adaptive
        // prediction again
        _input.seek(_startIndex);
        throw new NoViableAltException(_parser);
      }
      return nextToken;
    }

    @Override
    public int mark() {
      return _input.mark();
    }

    @Override
    public void release(int marker) {
      _input.release(marker);
    }

    @Override
    public int index() {
      return _input.index();
    }

    @Override
    public void seek(int index) {
      _input.seek(index);
    }

    @Override
    public int size() {
      return _input.size();
    }

    @Override
    public String getSourceName() {
      return _input.getSourceName();
    }

    @Override
    public Token LT(int k) {
      return _input.LT(k);
    }

    @Override
    public Token get(int index) {
      return _input.get(index);
    }

    @Override
    public TokenSource getTokenSource() {
      return _input.getTokenSource();
    }

    @Override
    public String getText(Interval interval) {
      return _input.getText(interval);
    }

    @Override
    public String getText() {
      return _input.getText();
    }

    @Override
    public String getText(RuleContext ctx) {
      return _input.getText(ctx);
    }

    @Override
    public String getText(Token start, Token stop) {
      return _input.getText(start, stop);
    }

    private final @Nonnull BatfishParser _parser;
    private final @Nonnull TokenStream _input;
    private final int _startIndex;

    private WrappedTokenStream(TokenStream input, BatfishParser parser) {
      _input = input;
      _parser = parser;
      _startIndex = input.index();
    }
  }
}
