package org.batfish.grammar;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.batfish.main.BatfishException;

public abstract class BatfishCombinedParser<P extends BatfishParser, L extends BatfishLexer> {

   private int _currentModeStart;
   private final List<String> _errors;
   private String _input;
   private L _lexer;
   protected P _parser;
   private boolean _throwOnLexerError;
   private boolean _throwOnParserError;
   private List<Integer> _tokenModes;
   protected CommonTokenStream _tokens;
   private final List<String> _warnings;

   public BatfishCombinedParser(Class<P> pClass, Class<L> lClass, String input,
         boolean throwOnParserError, boolean throwOnLexerError) {
      _throwOnParserError = throwOnParserError;
      _throwOnLexerError = throwOnLexerError;
      _tokenModes = new ArrayList<Integer>();
      _currentModeStart = 0;
      _warnings = new ArrayList<String>();
      _errors = new ArrayList<String>();
      _input = input;
      ANTLRInputStream inputStream = new ANTLRInputStream(input);
      try {
         _lexer = lClass.getConstructor(CharStream.class).newInstance(
               inputStream);
      }
      catch (InstantiationException | IllegalAccessException
            | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException e) {
         throw new BatfishException(
               "Error constructing lexer using reflection", e);
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

   public String getInput() {
      return _input;
   }

   public L getLexer() {
      return _lexer;
   }

   public P getParser() {
      return _parser;
   }

   public boolean getThrowOnLexerError() {
      return _throwOnLexerError;
   }

   public boolean getThrowOnParserError() {
      return _throwOnParserError;
   }

   public int getTokenMode(Token t) {
      int tokenIndex = t.getTokenIndex();
      if (tokenIndex == -1) {
         // token probably added manually, not by parser
         return -1;
      }
      if (tokenIndex < _tokenModes.size()) {
         return _tokenModes.get(tokenIndex);
      }
      else {
         return _lexer._mode;
      }
   }

   public CommonTokenStream getTokens() {
      return _tokens;
   }

   public List<String> getWarnings() {
      return _warnings;
   }

   public abstract ParserRuleContext parse();

   public void updateTokenModes(int mode) {
      for (int i = _currentModeStart; i <= _tokens.size(); i++) {
         _tokenModes.add(mode);
      }
      _currentModeStart = _tokens.size() + 1;
   }

}
