package batfish.grammar;

import java.util.List;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RecognizerSharedState;

public abstract class TopologyLexer extends Lexer {

   public TopologyLexer() {
      super();
   }
   
   public TopologyLexer(CharStream input) {
      super(input);
   }

   public TopologyLexer(CharStream input, RecognizerSharedState state) {
      super(input, state);
   }

   public abstract List<String> getErrors();

}
