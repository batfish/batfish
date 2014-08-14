package batfish.grammar;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

public class BatfishParserErrorListener extends BatfishGrammarErrorListener {

   public BatfishParserErrorListener(String grammarName,
         BatfishCombinedParser<?, ?> parser) {
      super(grammarName, parser);
   }

   private String printToken(Token token) {
      String rawTokenText = token.getText();
      String tokenText = rawTokenText.replace("\n", "\\n").replace("\t", "\\t");
      int tokenType = token.getType();
      String channel = token.getChannel() == Lexer.HIDDEN ? "(HIDDEN) " : "";
      String tokenName;
      int line = token.getLine();
      int col = token.getCharPositionInLine();
      if (tokenType == -1) {
         tokenName = "EOF";
      }
      else {
         tokenName = _combinedParser.getParser().getTokenNames()[tokenType];
         tokenText = "'" + tokenText + "'";
      }
      return " line " + line + ":" + col + " " + channel + " " + tokenName
            + ":" + tokenText;
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
      int endTokenIndex = tokens.size();
      StringBuilder sb = new StringBuilder();
      sb.append("parser: " + _grammarName + ": line " + line + ":"
            + charPositionInLine + ": " + msg + "\n");
      String offendingTokenText = printToken((Token) offendingSymbol);
      sb.append("Offending Token: " + offendingTokenText + "\n");
      sb.append("Error parsing top (leftmost) parser rule in stack: '"
            + ruleStack + "'.\n");
      sb.append("Unconsumed tokens:\n");
      for (int i = startTokenIndex; i < endTokenIndex; i++) {
         Token token = tokens.get(i);
         String tokenText = printToken(token);
         sb.append(tokenText + "\n");
      }
      String error = sb.toString();
      _combinedParser.getErrors().add(error);
   }

}
