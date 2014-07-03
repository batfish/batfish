package batfish.util;

import org.antlr.v4.runtime.CharStream;

import batfish.grammar.BatfishLexer;

public abstract class DummyLexer extends BatfishLexer {

   public DummyLexer(CharStream input) {
      super(input);
   }

}
