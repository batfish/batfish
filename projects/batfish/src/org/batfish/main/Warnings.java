package org.batfish.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.cisco.CiscoParser;

public class Warnings {

   private final boolean _pedanticAsError;

   private final boolean _pedanticRecord;

   private final List<String> _pedanticWarnings;

   private boolean _printParseTree;

   private final boolean _redFlagAsError;

   private final boolean _redFlagRecord;

   private final List<String> _redFlagWarnings;

   private final boolean _unimplementedAsError;

   private final boolean _unimplementedRecord;

   private final List<String> _unimplementedWarnings;

   public Warnings(boolean pedanticAsError, boolean pedanticRecord,
         boolean redFlagAsError, boolean redFlagRecord,
         boolean unimplementedAsError, boolean unimplementedRecord,
         boolean printParseTree) {
      _pedanticAsError = pedanticAsError;
      _pedanticWarnings = new ArrayList<String>();
      _pedanticRecord = pedanticRecord;
      _printParseTree = printParseTree;
      _redFlagAsError = redFlagAsError;
      _redFlagRecord = redFlagRecord;
      _redFlagWarnings = new ArrayList<String>();
      _unimplementedAsError = unimplementedAsError;
      _unimplementedRecord = unimplementedRecord;
      _unimplementedWarnings = new ArrayList<String>();
   }

   public List<String> getPedanticWarnings() {
      return _pedanticWarnings;
   }

   public List<String> getRedFlagWarnings() {
      return _redFlagWarnings;
   }

   public List<String> getUnimplementedWarnings() {
      return _unimplementedWarnings;
   }

   public void pedantic(String msg) {
      if (_pedanticAsError) {
         throw new PedanticBatfishException(msg);
      }
      else if (_pedanticRecord) {
         String prefix = "WARNING " + (_pedanticWarnings.size() + 1)
               + ": PEDANTIC: ";
         String warning = prefix + msg + "\n";
         _pedanticWarnings.add(warning);
      }
   }

   public void redFlag(String msg) {
      if (_redFlagAsError) {
         throw new RedFlagBatfishException(msg);
      }
      else if (_redFlagRecord) {
         String prefix = "WARNING " + (_redFlagWarnings.size() + 1)
               + ": RED FLAG: ";
         String warning = prefix + msg + "\n";
         _redFlagWarnings.add(warning);
      }
   }

   public void todo(ParserRuleContext ctx, String feature,
         BatfishCombinedParser<?, ?> parser, String text) {
      if (!_unimplementedRecord && !_unimplementedAsError) {
         return;
      }
      String prefix = "WARNING: UNIMPLEMENTED: "
            + (_unimplementedWarnings.size() + 1) + ": ";
      StringBuilder sb = new StringBuilder();
      List<String> ruleNames = Arrays.asList(CiscoParser.ruleNames);
      String ruleStack = ctx.toString(ruleNames);
      sb.append(prefix
            + "Missing implementation for top (leftmost) parser rule in stack: '"
            + ruleStack + "'.\n");
      sb.append(prefix + "Unimplemented feature: " + feature + "\n");
      sb.append(prefix + "Rule context follows:\n");
      int start = ctx.start.getStartIndex();
      int startLine = ctx.start.getLine();
      int end = ctx.stop.getStopIndex();
      String ruleText = text.substring(start, end + 1);
      String[] ruleTextLines = ruleText.split("\\n");
      for (int line = startLine, i = 0; i < ruleTextLines.length; line++, i++) {
         String contextPrefix = prefix + " line " + line + ": ";
         sb.append(contextPrefix + ruleTextLines[i] + "\n");
      }
      if (_printParseTree) {
         sb.append(prefix + "Parse tree follows:\n");
         String parseTreePrefix = prefix + "PARSE TREE: ";
         String parseTreeText = ParseTreePrettyPrinter.print(ctx, parser);
         String[] parseTreeLines = parseTreeText.split("\n");
         for (String parseTreeLine : parseTreeLines) {
            sb.append(parseTreePrefix + parseTreeLine + "\n");
         }
      }
      String warning = sb.toString();
      if (_unimplementedAsError) {
         throw new UnimplementedBatfishException(warning);
      }
      else {
         _unimplementedWarnings.add(sb.toString());
      }
   }

   public void unimplemented(String msg) {
      if (_unimplementedAsError) {
         throw new UnimplementedBatfishException(msg);
      }
      else if (_unimplementedRecord) {
         String prefix = "WARNING " + (_unimplementedWarnings.size() + 1)
               + ": UNIMPLEMENTED: ";
         String warning = prefix + msg + "\n";
         _unimplementedWarnings.add(warning);
      }
   }

}
