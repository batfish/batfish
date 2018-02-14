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
    Integer result = null;
    do {
      try {
        result = super.adaptivePredict(input, decision, outerContext);
      } catch (NoViableAltException e) {
        // If adaptive prediction fails, throw out current line and try again.
        try {
          _parser.createErrorNodeLine();
        } catch (BatfishRecognitionException re) {
          /*
           * This lets us exit adaptive prediction gracefully when recovery fails because of:
           * A. Rule using adaptive prediction that isn't satisfied by EOF
           * B. Transient lexer corruption due to crappy build system (during development)
           */
          return org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER;
        }
      }
    } while (result == null);
    return result;
  }
}
