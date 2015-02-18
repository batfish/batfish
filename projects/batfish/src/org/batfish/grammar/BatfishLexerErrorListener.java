package org.batfish.grammar;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.batfish.main.BatfishException;

public class BatfishLexerErrorListener extends BatfishGrammarErrorListener {

   public BatfishLexerErrorListener(String grammarName,
         BatfishCombinedParser<?, ?> parser) {
      super(grammarName, parser);
   }

   @Override
   public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
         int line, int charPositionInLine, String msg, RecognitionException e) {
      StringBuilder sb = new StringBuilder();
      BatfishParser parser = _combinedParser.getParser();
      BatfishLexer lexer = _combinedParser.getLexer();
      List<String> ruleNames = Arrays.asList(parser.getRuleNames());
      ParserRuleContext ctx = parser.getContext();
      String ruleStack = ctx.toString(ruleNames);
      sb.append("lexer: " + _grammarName + ": line " + line + ":"
            + charPositionInLine + ": " + msg + "\n");
      sb.append("Current rule stack: '" + ruleStack + "'.\n");
      sb.append("Current rule starts at: line: " + ctx.getStart().getLine()
            + ", col " + ctx.getStart().getCharPositionInLine() + "\n");
      sb.append("Parse tree for current rule:\n");
      sb.append(ParseTreePrettyPrinter.print(ctx, _combinedParser) + "\n");
      sb.append("Lexer mode: " + lexer.getMode() + "\n");
      sb.append("Lexer state variables:\n");
      sb.append(lexer.printStateVariables());
      String error = sb.toString();
      if (_combinedParser.getThrowOnLexerError()) {
         throw new BatfishException("\n" + error);
      }
      else {
         _combinedParser.getErrors().add(error);
      }
   }

}
