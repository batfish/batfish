package org.batfish.grammar.cisco.controlplane;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.batfish.grammar.cisco.CiscoLexer;
import org.batfish.grammar.cisco.CiscoParserBaseListener;

public class CiscoControlPlaneObfuscator extends CiscoParserBaseListener {

   private CiscoControlPlaneSensitiveDataCollector _collector;

   public CiscoControlPlaneObfuscator(
         CiscoControlPlaneSensitiveDataCollector collector) {
      _collector = collector;
   }

   @Override
   public void visitTerminal(TerminalNode t) {
      Token symbol = t.getSymbol();
      int type = symbol.getType();
      switch (type) {

      case CiscoLexer.VARIABLE:
         ParserRuleContext parent = (ParserRuleContext) t.getParent()
               .getPayload();
         int index = parent.children.indexOf(t);
         String oldText = symbol.getText();
         String newText = _collector.getObfuscatedVariable(oldText);
         CommonToken ct = new CommonToken(CiscoLexer.VARIABLE, newText);
         TerminalNodeImpl newNode = new TerminalNodeImpl(ct);
         parent.children.remove(index);
         parent.children.add(index, newNode);
         break;

      default:
         break;
      }
   }

}
