package org.batfish.grammar;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.util.Util;

public class ParseTreePrettyPrinter implements ParseTreeListener {

   public static String print(ParserRuleContext ctx,
         BatfishCombinedParser<?, ?> combinedParser) {
      ParseTreeWalker walker = new ParseTreeWalker();
      ParseTreePrettyPrinter printer = new ParseTreePrettyPrinter(ctx,
            combinedParser);
      walker.walk(printer, ctx);
      return printer._sb.toString();
   }

   private BatfishCombinedParser<?, ?> _combinedParser;

   private ParserRuleContext _ctx;

   private int _indent;

   private List<String> _ruleNames;

   private StringBuilder _sb;

   private Vocabulary _vocabulary;

   private ParseTreePrettyPrinter(ParserRuleContext ctx,
         BatfishCombinedParser<?, ?> combinedParser) {
      Parser grammar = combinedParser.getParser();
      List<String> ruleNames = Arrays.asList(grammar.getRuleNames());
      _vocabulary = grammar.getVocabulary();
      _combinedParser = combinedParser;
      _ruleNames = ruleNames;
      _ctx = ctx;
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
      String ruleName = _ruleNames.get(ctx.getRuleIndex());
      _sb.append("(" + ruleName);
      if (ruleName.equals("set_add_method")) {
         assert Boolean.TRUE;
      }
   }

   @Override
   public void exitEveryRule(ParserRuleContext ctx) {
      _sb.append(")");
      _indent--;
   }

   @Override
   public void visitErrorNode(ErrorNode ctx) {
      String nodeText = Util.escape(ctx.getText());
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
         tokenName = _vocabulary.getSymbolicName(tokenType);
         _sb.append("<ErrorNode>:" + tokenName + ":'" + nodeText + "'");
      }
   }

   @Override
   public void visitTerminal(TerminalNode ctx) {
      String nodeText = Util.escape(ctx.getText());
      _sb.append("\n");
      for (int i = 0; i < _indent; i++) {
         _sb.append("  ");
      }
      Token t = ctx.getSymbol();
      int tokenType = t.getType();
      int modeAsInt = _combinedParser.getTokenMode(t);
      String mode;
      if (modeAsInt == -1) {
         mode = "<MANUAL/UNKNOWN>";
      }
      else {
         mode = _combinedParser.getLexer().getModeNames()[modeAsInt];
      }
      String tokenName;
      if (tokenType == -1) {
         tokenName = "EOF";
         _sb.append(tokenName + ":" + nodeText);
      }
      else {
         tokenName = _vocabulary.getSymbolicName(tokenType);
         _sb.append(tokenName + ":'" + nodeText + "'");
      }
      _sb.append("  <== mode:" + mode);
   }
}
