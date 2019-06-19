package org.batfish.grammar;

import static org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER;

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
    while (true) {
      try {
        return super.adaptivePredict(input, decision, outerContext);
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
}
