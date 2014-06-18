package batfish.grammar.topology;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognizerSharedState;

import batfish.grammar.TopologyLexer;

public abstract class Lexer extends TopologyLexer {

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
