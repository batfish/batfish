package batfish.grammar.cisco.controlplane;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.cisco.CiscoGrammar.*;
import batfish.grammar.cisco.CiscoGrammarBaseListener;
import batfish.grammar.cisco.CiscoGrammarCommonLexer;

public class CiscoControlPlanePrinter extends CiscoGrammarBaseListener {

   private StringBuilder _sb;
   private int _indent;
   private boolean _beginningOfLine;
   
   public CiscoControlPlanePrinter() {
      _sb = new StringBuilder();
      _indent = 0;
      _beginningOfLine = true;
   }
   
   @Override
   public void enterInterface_stanza_tail(Interface_stanza_tailContext ctx) {
      _indent++;
   }
   
   @Override
   public void exitInterface_stanza_tail(Interface_stanza_tailContext ctx) {
      _indent--;
   }

   @Override
   public void enterRouter_ospf_stanza_tail(Router_ospf_stanza_tailContext ctx) {
      _indent++;
   }
   
   @Override
   public void exitRouter_ospf_stanza_tail(Router_ospf_stanza_tailContext ctx) {
      _indent--;
   }

   @Override
   public void enterRouter_bgp_stanza_tail(Router_bgp_stanza_tailContext ctx) {
      _indent++;
   }
   
   @Override
   public void exitRouter_bgp_stanza_tail(Router_bgp_stanza_tailContext ctx) {
      _indent--;
   }

   @Override
   public void enterAddress_family_rb_stanza_tail(Address_family_rb_stanza_tailContext ctx) {
      _indent++;
   }
   
   @Override
   public void exitAddress_family_rb_stanza_tail(Address_family_rb_stanza_tailContext ctx) {
      _indent--;
   }

   @Override
   public void enterRoute_map_tail_tail(Route_map_tail_tailContext ctx) {
      _indent++;
   }
   
   @Override
   public void exitRoute_map_tail_tail(Route_map_tail_tailContext ctx) {
      _indent--;
   }

   @Override
   public void enterNull_block_substanza(Null_block_substanzaContext ctx) {
      _indent++;
   }
   
   @Override
   public void exitNull_block_substanza(Null_block_substanzaContext ctx) {
      _indent--;
   }

   private void indent() {
      for (int i = 0; i < _indent; i++) {
         _sb.append(" ");
      }      
   }
   
   @Override
   public void visitTerminal(TerminalNode t) {
      Token symbol = t.getSymbol();
      int type = symbol.getType();
      switch (type) {
         
      case CiscoGrammarCommonLexer.COMMENT_LINE:
         if (_beginningOfLine) {
            indent();
         }
         else {
            _sb.append(" ");
         }
         _sb.append(t.getText());
         _beginningOfLine = true;
         break;
         
      case CiscoGrammarCommonLexer.NEWLINE:
         _beginningOfLine = true;
         _sb.append("\n");
         break;
         
      default:
         if (_beginningOfLine) {
            indent();
            _beginningOfLine = false;
         }
         else {
            _sb.append(" ");
         }
         //_sb.append("(" + type + ")");
         _sb.append(t.getText());
      }
   }
   
   @Override
   public String toString() {
      return _sb.toString();
   }
   
}
