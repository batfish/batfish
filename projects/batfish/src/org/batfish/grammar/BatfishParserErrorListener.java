package org.batfish.grammar;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.batfish.common.BatfishException;
import org.batfish.util.Util;

public class BatfishParserErrorListener extends BatfishGrammarErrorListener {

   private static final int MAX_LOOKBACK = 10;

   public BatfishParserErrorListener(String grammarName,
         BatfishCombinedParser<?, ?> parser) {
      super(grammarName, parser);
   }

   private String printToken(Token token) {
      int modeAsInt = _combinedParser.getTokenMode(token);
      String mode = _combinedParser.getLexer().getModeNames()[modeAsInt];
      String rawTokenText = token.getText();
      String tokenText = Util.escape(rawTokenText);
      int tokenType = token.getType();
      String channel = token.getChannel() == Lexer.HIDDEN ? "(HIDDEN) " : "";
      String tokenName;
      int line = token.getLine();
      int col = token.getCharPositionInLine();
      if (tokenType == -1) {
         tokenName = "EOF";
      }
      else {
         tokenName = _combinedParser.getParser().getVocabulary()
               .getSymbolicName(tokenType);
         tokenText = "'" + tokenText + "'";
      }
      return " line " + line + ":" + col + " " + channel + " " + tokenName
            + ":" + tokenText + "  <== mode:" + mode;
   }

   @Override
   public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
         int line, int charPositionInLine, String msg, RecognitionException e) {
      BatfishParser parser = _combinedParser.getParser();
      List<String> ruleNames = Arrays.asList(parser.getRuleNames());
      ParserRuleContext ctx = parser.getContext();
      String ruleStack = ctx.toString(ruleNames);
      List<Token> tokens = _combinedParser.getTokens().getTokens();
      int startTokenIndex = parser.getInputStream().index();
      int lookbackIndex = Math.max(0, startTokenIndex - MAX_LOOKBACK);
      int endTokenIndex = tokens.size();
      StringBuilder sb = new StringBuilder();
      sb.append("parser: " + _grammarName + ": line " + line + ":"
            + charPositionInLine + ": " + msg + "\n");
      Token offendingToken = (Token) offendingSymbol;
      String offendingTokenText = printToken(offendingToken);
      sb.append("Offending Token: " + offendingTokenText + "\n");
      sb.append("Error parsing top (leftmost) parser rule in stack: '"
            + ruleStack + "'.\n");
      String ctxParseTree = ParseTreePrettyPrinter.print(ctx, _combinedParser);
      sb.append("Parse tree of current rule:\n" + ctxParseTree + "\n");
      sb.append("Unconsumed tokens:\n");
      for (int i = startTokenIndex; i < endTokenIndex; i++) {
         Token token = tokens.get(i);
         String tokenText = printToken(token);
         sb.append(tokenText + "\n");
      }
      if (lookbackIndex < startTokenIndex) {
         int numLookbackTokens = startTokenIndex - lookbackIndex;
         sb.append("Previous " + numLookbackTokens + " tokens:\n");
         for (int i = lookbackIndex; i < startTokenIndex; i++) {
            Token lookbackToken = tokens.get(i);
            String tokenText = printToken(lookbackToken);
            sb.append(tokenText + "\n");
         }
      }
      if (offendingToken.getType() == Token.EOF) {
         sb.append("Lexer mode at EOF: " + _combinedParser.getLexer().getMode()
               + "\n");
      }
      String error = sb.toString();
      if (_combinedParser.getThrowOnParserError()) {
         throw new BatfishException("\n" + error);
      }
      else {
         _combinedParser.getErrors().add(error);
      }
   }

}
