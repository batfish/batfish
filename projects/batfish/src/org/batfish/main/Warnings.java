package org.batfish.main;

import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warning;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ParseTreePrettyPrinter;
import org.batfish.grammar.cisco.CiscoParser;

public class Warnings extends org.batfish.common.Warnings {

   private static final String MISCELLANEOUS = "MISCELLANEOUS";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private final transient boolean _pedanticAsError;

   private final transient boolean _pedanticRecord;

   private transient boolean _printParseTree;

   private final transient boolean _redFlagAsError;

   private final transient boolean _redFlagRecord;

   private final transient boolean _unimplementedAsError;

   private final transient boolean _unimplementedRecord;

   public Warnings(boolean pedanticAsError, boolean pedanticRecord,
         boolean redFlagAsError, boolean redFlagRecord,
         boolean unimplementedAsError, boolean unimplementedRecord,
         boolean printParseTree) {
      _pedanticAsError = pedanticAsError;
      _pedanticRecord = pedanticRecord;
      _printParseTree = printParseTree;
      _redFlagAsError = redFlagAsError;
      _redFlagRecord = redFlagRecord;
      _unimplementedAsError = unimplementedAsError;
      _unimplementedRecord = unimplementedRecord;
   }

   public void pedantic(String msg) {
      pedantic(msg, MISCELLANEOUS);
   }

   public void pedantic(String msg, String tag) {
      if (_pedanticAsError) {
         throw new PedanticBatfishException(msg);
      }
      else if (_pedanticRecord) {
         _pedanticWarnings.add(new Warning(msg, tag));
      }
   }

   public void redFlag(String msg) {
      redFlag(msg, MISCELLANEOUS);
   }

   public void redFlag(String msg, String tag) {
      if (_redFlagAsError) {
         throw new RedFlagBatfishException(msg);
      }
      else if (_redFlagRecord) {
         _redFlagWarnings.add(new Warning(msg, tag));
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
      sb.append(
            prefix + "Missing implementation for top (leftmost) parser rule in stack: '"
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
         _unimplementedWarnings
               .add(new Warning(sb.toString(), "UNIMPLEMENTED"));
      }
   }

   public void unimplemented(String msg) {
      unimplemented(msg, MISCELLANEOUS);
   }

   public void unimplemented(String msg, String tag) {
      if (_unimplementedAsError) {
         throw new UnimplementedBatfishException(msg);
      }
      else if (_unimplementedRecord) {
         _unimplementedWarnings.add(new Warning(msg, tag));
      }
   }

}
