package org.batfish.grammar;

import static org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER;

import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.batfish.grammar.BatfishANTLRErrorStrategy.BatfishRecognitionException;

/**
 * Clones an existing {@link ParserATNSimulator} used by a {@link BatfishParser} and wraps the
 * {@link #adaptivePredict} function with a recovery mechanism. Intended for use with a {@link
 * BatfishParser} employing a {@link BatfishANTLRErrorStrategy} as its error recovery strategy.
 */
public class BatfishParserATNSimulator extends ParserATNSimulator {

  private NoViableAltException _exception;
  private BatfishParser _parser;

  /**
   * Construct a {@link BatfishParserATNSimulator} from a {@link ParserATNSimulator}
   *
   * @param parent The {@link ParserATNSimulator} to emulate modulo {@link #adaptivePredict}.
   */
  public BatfishParserATNSimulator(ParserATNSimulator parent) {
    super(parent.getParser(), parent.atn, parent.decisionToDFA, parent.getSharedContextCache());
    setPredictionMode(parent.getPredictionMode());
    _parser = (BatfishParser) parser;
  }

  @Override
  public int adaptivePredict(TokenStream input, int decision, ParserRuleContext outerContext) {
    while (true) {
      _exception = null;
      try {
        int alt = super.adaptivePredict(input, decision, outerContext);
        if (_exception != null) {
          // Sometimes we want to override the adaptive prediction decision when an error has
          // occurred anyway.
          // See https://www.antlr.org/api/Java/org/antlr/v4/runtime/atn/ATNState.html
          switch (_parser.getInterpreter().atn.states.get(_parser.getState()).getStateType()) {
            case ATNState.PLUS_LOOP_BACK:
            case ATNState.STAR_LOOP_BACK:
            case ATNState.STAR_LOOP_ENTRY:
              if (alt == 2) {
                // 2 means we want to leave the loop. But perhaps we won't have to if we throw out
                // the current line.
                // So throw to trigger recovery and another call to adaptive prediction.
                NoViableAltException exception = _exception;
                _exception = null;
                throw exception;
              }
              // 1 means we can definitely parse at least one more element in the loop, and the
              // error will just occur later. We can just deal with it later anyway, so don't
              // intercede now.
              assert alt == 1;
              break;
            default:
              break;
          }
        }
        return alt;
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

  @Override
  protected NoViableAltException noViableAlt(
      TokenStream input, ParserRuleContext outerContext, ATNConfigSet configs, int startIndex) {
    _exception = super.noViableAlt(input, outerContext, configs, startIndex);
    return _exception;
  }
}
