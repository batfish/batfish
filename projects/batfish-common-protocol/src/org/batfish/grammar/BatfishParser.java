package org.batfish.grammar;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

public abstract class BatfishParser extends Parser {

   public BatfishParser(TokenStream input) {
      super(input);
   }

   public String getStateInfo() {
      return null;
   }

   public void initErrorListener(BatfishCombinedParser<?, ?> parser) {
      BatfishParserErrorListener errorListener = new BatfishParserErrorListener(
            this.getClass().getSimpleName(), parser);
      removeErrorListeners();
      addErrorListener(errorListener);
      parser.setParserErrorListener(errorListener);
   }

}
