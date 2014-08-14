package batfish.grammar;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ParseTreePrettyPrinter implements ParseTreeListener {

   public static String print(ParserRuleContext ctx, Parser grammar) {
      List<String> ruleNameList = Arrays.asList(grammar.getRuleNames());
      List<String> tokenNameList = Arrays.asList(grammar.getTokenNames());
      ParseTreeWalker walker = new ParseTreeWalker();
      ParseTreePrettyPrinter printer = new ParseTreePrettyPrinter(ctx,
            ruleNameList, tokenNameList);
      walker.walk(printer, ctx);
      return printer._sb.toString();
   }

   private ParserRuleContext _ctx;
   private int _indent;
   private List<String> _ruleNames;
   private StringBuilder _sb;

   private List<String> _tokenNames;

   private ParseTreePrettyPrinter(ParserRuleContext ctx,
         List<String> ruleNames, List<String> tokenNames) {
      _ruleNames = ruleNames;
      _ctx = ctx;
      _tokenNames = tokenNames;
      _sb = new StringBuilder();
      _indent = 0;
   }

   @Override
   public void enterEveryRule(ParserRuleContext ctx) {
      if (ctx != _ctx) {
         _sb.append("\n");
      }
      for (int i = 0; i < _indent; i++) {
         _sb.append("  ");
      }
      _indent++;
      _sb.append("(" + _ruleNames.get(ctx.getRuleIndex()));
   }

   @Override
   public void exitEveryRule(ParserRuleContext ctx) {
      _sb.append(")");
      _indent--;
   }

   @Override
   public void visitErrorNode(ErrorNode ctx) {
      String nodeText = ctx.getText().replace("\n", "\\n");
      _sb.append("\n");
      for (int i = 0; i < _indent; i++) {
         _sb.append("  ");
      }
      int tokenType = ctx.getSymbol().getType();
      String tokenName;
      if (tokenType == -1) {
         tokenName = "EOF";
         _sb.append(tokenName + ":" + nodeText);
      }
      else {
         tokenName = _tokenNames.get(tokenType);
         _sb.append("<ErrorNode>:" + tokenName + ":'" + nodeText + "'");
      }
   }

   @Override
   public void visitTerminal(TerminalNode ctx) {
      String nodeText = ctx.getText().replace("\n", "\\n").replace("\t", "\\t");
      _sb.append("\n");
      for (int i = 0; i < _indent; i++) {
         _sb.append("  ");
      }
      int tokenType = ctx.getSymbol().getType();
      String tokenName;
      if (tokenType == -1) {
         tokenName = "EOF";
         _sb.append(tokenName + ":" + nodeText);
      }
      else {
         tokenName = _tokenNames.get(tokenType);
         _sb.append(tokenName + ":'" + nodeText + "'");
      }
   }

}
