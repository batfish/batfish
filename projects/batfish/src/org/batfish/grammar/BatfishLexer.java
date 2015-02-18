package org.batfish.grammar;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public abstract class BatfishLexer extends Lexer {

   private BatfishCombinedParser<?, ?> _parser;

   public BatfishLexer(CharStream input) {
      super(input);
   }

   public String getMode() {
      return this.getModeNames()[_mode];
   }

   public void initErrorListener(BatfishCombinedParser<?, ?> parser) {
      _parser = parser;
      ANTLRErrorListener errorListener = new BatfishLexerErrorListener(this
            .getClass().getSimpleName(), parser);
      removeErrorListeners();
      addErrorListener(errorListener);
   }

   @Override
   public void mode(int m) {
      _parser.updateTokenModes(_mode);
      super.mode(m);
   };

   /**
    * Print custom lexer state (should be overridden)
    *
    * @return Should print custom lexer state variables and their values
    */
   public String printStateVariables() {
      return "";
   }

}
