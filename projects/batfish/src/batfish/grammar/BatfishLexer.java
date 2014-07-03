package batfish.grammar;

import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

public abstract class BatfishLexer extends Lexer {

   private final BatfishGrammarErrorListener _errorListener;
   
   public BatfishLexer(CharStream input) {
      super(input);
      _errorListener = new BatfishGrammarErrorListener(this.getClass().getSimpleName());
      removeErrorListeners();
      addErrorListener(_errorListener);
   }
   
   public List<String> getErrors() {
      return _errorListener.getErrors();
   }

}
