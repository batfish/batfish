package batfish.grammar.juniper;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognizerSharedState;

import batfish.grammar.ConfigurationLexer;

public abstract class Lexer extends ConfigurationLexer {

   public Lexer() {
      super();
   }

   public Lexer(CharStream input) {
      super(input);
   }

   public Lexer(CharStream input, RecognizerSharedState state) {
      super(input, state);
   }

}
