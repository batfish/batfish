package org.batfish.grammar.iptables;

import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.iptables.IptablesParser.CommandContext;
import org.batfish.grammar.iptables.IptablesParser.Command_tailContext;
import org.batfish.grammar.iptables.IptablesParser.Iptables_configurationContext;
import org.batfish.grammar.iptables.IptablesParser.Rule_specContext;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.iptables.IptablesConfiguration;
import org.batfish.representation.iptables.IptablesRule;
import org.batfish.representation.iptables.IptablesVendorConfiguration;

public class IptablesControlPlaneExtractor extends IptablesParserBaseListener implements
      ControlPlaneExtractor {

   @SuppressWarnings("unused")
   private IptablesConfiguration _configuration;

   private IptablesCombinedParser _parser;

   private String _text;

   private final Set<String> _unimplementedFeatures;

   private IptablesVendorConfiguration _vendorConfiguration;

   private Warnings _w;

   public IptablesControlPlaneExtractor(String fileText,
         IptablesCombinedParser iptablesParser, Warnings warnings) {
      _text = fileText;
      _parser = iptablesParser;
      _w = warnings;
      _unimplementedFeatures = new TreeSet<String>();
   }

   @Override
   public void enterIptables_configuration(Iptables_configurationContext ctx) {
      _vendorConfiguration = new IptablesVendorConfiguration();
      _configuration = _vendorConfiguration;
   }

   @Override
   public void exitCommand(CommandContext ctx) {

      //filter is the default table
      String table = "filter";
      
      if (ctx.table() == null) {
         table = ctx.table().getText();
      }
      
      Command_tailContext tailCtx = ctx.command_tail();
      
      if (tailCtx.command_append() != null) {
         String chain = tailCtx.command_append().chain().getText();
         IptablesRule rule = extractRule(tailCtx.command_append().rule_spec());
         _configuration.addRule(table, chain, rule, -1);
      } 
      else if (tailCtx.command_check() != null) {
         todo(tailCtx.command_check(), "Command Check");
      }
      else if (tailCtx.command_delete() != null) {
         todo(tailCtx.command_delete(), "Command Delete");
      }
      else if (tailCtx.command_delete_chain() != null) {
         todo(tailCtx.command_delete_chain(), "Command Delete Chain");
      }
      else if (tailCtx.command_flush() != null) {
         todo(tailCtx.command_flush(), "Command Flush");
      }
      else if (tailCtx.command_help() != null) {
         todo(tailCtx.command_help(), "Command Help");
      }
      else if (tailCtx.command_insert() != null) {
         String chain = tailCtx.command_append().chain().getText();
         int ruleNum = 1;
         if (tailCtx.command_insert().rulenum != null) {
            ruleNum = toInteger(tailCtx.command_insert().rulenum);
         }
         IptablesRule rule = extractRule(tailCtx.command_insert().rule_spec());
         _configuration.addRule(table, chain, rule, ruleNum);
      }
      else if (tailCtx.command_list() != null) {
         todo(tailCtx.command_list(), "Command List");         
      }
      else if (tailCtx.command_list_rules() != null) {
         todo(tailCtx.command_list_rules(), "Command List Rules");         
      }
      else if (tailCtx.command_new_chain() != null) {
         String chain = tailCtx.command_new_chain().chain().getText();
         _configuration.addChain(table, chain);
      }
      else if (tailCtx.command_policy() != null) {
         String chain = tailCtx.command_policy().chain().getText();
         String target = tailCtx.command_policy().target().getText();
         _configuration.setPolicy(table, chain, target);
      }
      else if (tailCtx.command_rename_chain() != null) {
         todo(tailCtx.command_rename_chain(), "Command Rename Chain");         
      }
      else if (tailCtx.command_replace() != null) {
         todo(tailCtx.command_replace(), "Command Replace");         
      }
      else if (tailCtx.command_zero() != null) {
         todo(tailCtx.command_zero(), "Command Zero");         
      }
      else {
         todo(tailCtx, "Unknown command");
      }
   }
   
   private IptablesRule extractRule(Rule_specContext rule_spec) {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @Override
   public VendorConfiguration getVendorConfiguration() {
      return _vendorConfiguration;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(this, tree);
   }

   @SuppressWarnings("unused")
   private void todo(ParserRuleContext ctx, String feature) {
      _w.todo(ctx, feature, _parser, _text);
      _unimplementedFeatures.add("Cisco: " + feature);
   }

   public static int toInteger(Token t) {
      return Integer.parseInt(t.getText());
   }
}
