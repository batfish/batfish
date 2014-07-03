package batfish.util;

import org.antlr.v4.runtime.TokenStream;

import batfish.grammar.BatfishParser;

public abstract class DummyParser extends BatfishParser {

   public DummyParser(TokenStream input) {
      super(input);
   }

}
