package batfish.grammar;

import java.util.List;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.RecognizerSharedState;

public abstract class ConfigurationLexer extends Lexer {

   public ConfigurationLexer() {
      super();
   }
   
   public ConfigurationLexer(CharStream input) {
      super(input);
   }

   public ConfigurationLexer(CharStream input, RecognizerSharedState state) {
      super(input, state);
   }

   public abstract List<String> getErrors();

}
