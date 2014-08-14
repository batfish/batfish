package batfish.grammar;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

public abstract class BatfishCombinedParser<P extends BatfishParser, L extends BatfishLexer> {

   private final List<String> _errors;
   private L _lexer;
   protected P _parser;
   protected CommonTokenStream _tokens;
   private final List<String> _warnings;

   public BatfishCombinedParser(Class<P> pClass, Class<L> lClass, String input) {
      _warnings = new ArrayList<String>();
      _errors = new ArrayList<String>();
      ANTLRInputStream inputStream = new ANTLRInputStream(input);
      try {
         _lexer = lClass.getConstructor(CharStream.class).newInstance(
               inputStream);
      }
      catch (InstantiationException | IllegalAccessException
            | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException e) {
         throw new Error(e);
      }
      _lexer.initErrorListener(this);
      _tokens = new CommonTokenStream(_lexer);
      try {
         _parser = pClass.getConstructor(TokenStream.class)
               .newInstance(_tokens);
      }
      catch (InstantiationException | IllegalAccessException
            | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException e) {
         throw new Error(e);
      }
      _parser.initErrorListener(this);
      _parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
   }

   public List<String> getErrors() {
      return _errors;
   }

   public BatfishLexer getLexer() {
      return _lexer;
   }

   public BatfishParser getParser() {
      return _parser;
   }

   public CommonTokenStream getTokens() {
      return _tokens;
   }

   public List<String> getWarnings() {
      return _warnings;
   }

   public abstract ParserRuleContext parse();

}
