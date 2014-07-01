package batfish.grammar;

import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.TokenStream;

public abstract class BatfishParser extends Parser {

   private final BatfishGrammarErrorListener _errorListener;
   
   public BatfishParser(TokenStream input) {
      super(input);
      _errorListener = new BatfishGrammarErrorListener(this.getClass().getSimpleName());
      removeErrorListeners();
      addErrorListener(_errorListener);
   }
   
   public List<String> getErrors() {
      return _errorListener.getErrors();
   }

}
