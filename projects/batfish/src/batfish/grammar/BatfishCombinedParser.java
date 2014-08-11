package batfish.grammar;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

public abstract class BatfishCombinedParser {

   private final List<String> _errors;
   private final List<String> _warnings;

   public BatfishCombinedParser() {
      _warnings = new ArrayList<String>();
      _errors = new ArrayList<String>();
   }

   public List<String> getErrors() {
      return _errors;
   }

   public abstract BatfishLexer getLexer();

   public abstract BatfishParser getParser();

   public abstract CommonTokenStream getTokens();

   public List<String> getWarnings() {
      return _warnings;
   }

   public abstract ParserRuleContext parse();

}
