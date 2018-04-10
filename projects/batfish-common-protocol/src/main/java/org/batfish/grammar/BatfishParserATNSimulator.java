package org.batfish.grammar;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
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
    try {
      return super.adaptivePredict(input, decision, outerContext);
    } catch (NoViableAltException e) {
      /*
       * Adaptive prediction has failed. In this case, throw out the current line and then make
       * one attempt to stay in the current context. This should work if the current context is a
       * block, like interface, and the next line is still in the interface context.
       *
       * If this second attempt still fails then the current context is not valid for the next line.
       * In this case, return INVALID_ALT_NUMBER and let the caller exit the current parse context
       * and try again.
       */
      try {
        _parser.createErrorNodeLine();
        return super.adaptivePredict(input, decision, outerContext);
      } catch (BatfishRecognitionException | NoViableAltException ex) {
        /*
         * This lets us exit adaptive prediction gracefully when recovery fails because of:
         * A. Rule using adaptive prediction that isn't satisfied by EOF
         * B. Transient lexer corruption due to crappy build system (during development)
         */
        return org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER;
      }
    }
  }
}
