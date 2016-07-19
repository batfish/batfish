package org.batfish.grammar.iptables;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.iptables.IptablesParser.CommandContext;
import org.batfish.grammar.iptables.IptablesParser.Command_tailContext;
import org.batfish.grammar.iptables.IptablesParser.Declaration_chain_policyContext;
import org.batfish.grammar.iptables.IptablesParser.Declaration_tableContext;
import org.batfish.grammar.iptables.IptablesParser.EndpointContext;
import org.batfish.grammar.iptables.IptablesParser.Iptables_configurationContext;
import org.batfish.grammar.iptables.IptablesParser.MatchContext;
import org.batfish.grammar.iptables.IptablesParser.Rule_specContext;
import org.batfish.grammar.iptables.IptablesParser.TargetContext;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.iptables.IptablesConfiguration;
import org.batfish.representation.iptables.IptablesMatch.MatchType;
import org.batfish.representation.iptables.IptablesRule;
import org.batfish.representation.iptables.IptablesRule.IptablesActionType;
import org.batfish.representation.iptables.IptablesVendorConfiguration;

public class IptablesControlPlaneExtractor extends IptablesParserBaseListener implements
      ControlPlaneExtractor {

   private IptablesConfiguration _configuration;

   private boolean _formatIptablesSave = false;

   private String _fileName;
   
   private IptablesCombinedParser _parser;

   private String _tableCurrent;
   
   private String _text;

   private final Set<String> _unimplementedFeatures;

   private IptablesVendorConfiguration _vendorConfiguration;

   private Warnings _w;

   
   
   public IptablesControlPlaneExtractor(String fileText,
         IptablesCombinedParser iptablesParser, Warnings warnings, String fileName) {
      _text = fileText;
      _parser = iptablesParser;
      _w = warnings;
      _unimplementedFeatures = new TreeSet<String>();
      _fileName = fileName;
   }

   @Override
   public void enterIptables_configuration(Iptables_configurationContext ctx) {
      _vendorConfiguration = new IptablesVendorConfiguration();
      _configuration = _vendorConfiguration;
      _vendorConfiguration.setHostname(_fileName);
   }

   @Override
   public void exitCommand(CommandContext ctx) {

      //default table if not specified in the command
      String table = (_formatIptablesSave)? _tableCurrent : "filter";
      
      if (ctx.table() != null) {
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
         _configuration.setChainTarget(table, chain, target);
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
   
   @Override
   public void exitDeclaration_chain_policy(Declaration_chain_policyContext ctx) {
      String chain = ctx.chain().getText();
      String target = ctx.target().getText();
      _configuration.setChainTarget(_tableCurrent, chain, target);
   }
   
   @Override
   public void exitDeclaration_table(Declaration_tableContext ctx) {      
      _formatIptablesSave = true;      
      _tableCurrent = ctx.table().getText();      
   }
   
   private IptablesRule extractRule(Rule_specContext ctx) {
      IptablesRule rule = new IptablesRule();
      
      List<MatchContext> matches = ctx.match_list;
      
      for (MatchContext mCtx : matches) {
         
         boolean inverted = (mCtx.NOT() != null);
         
         if (mCtx.OPTION_IPV4() != null || mCtx.OPTION_IPV6() != null) {
            todo(mCtx, "ipv4 (--4) and ipv6 (--6) options");
         }
         else if (mCtx.OPTION_DESTINATION() != null) {
            rule.addMatch(inverted, MatchType.Destination, getEndpoint(mCtx.endpoint()));
         }
         else if (mCtx.OPTION_DESTINATION_PORT() != null) {
            rule.addMatch(inverted, MatchType.DestinationPort, toInteger(mCtx.port));
         }
         else if (mCtx.OPTION_IN_INTERFACE() != null) {
            rule.addMatch(inverted, MatchType.InInterface, mCtx.interface_name.getText());
         }
         else if (mCtx.OPTION_OUT_INTERFACE() != null) {
            rule.addMatch(inverted, MatchType.OutInterface, mCtx.interface_name.getText());
         }
         else if (mCtx.OPTION_SOURCE() != null) {
            rule.addMatch(inverted, MatchType.Source, getEndpoint(mCtx.endpoint()));
         }
         else if (mCtx.OPTION_SOURCE_PORT() != null) {
            rule.addMatch(inverted, MatchType.SourcePort, toInteger(mCtx.port));
         }
         else {
            todo(mCtx, "Unknown match option");            
         }
      }
      
      if (ctx.action().OPTION_JUMP() != null) {
         TargetContext target = ctx.action().target();
         if (target.ACCEPT() != null) {
            rule.setAction(IptablesActionType.Accept, null);            
         }
         else if (target.DROP() != null) {
            rule.setAction(IptablesActionType.Drop, null);
         }
         else if (target.RETURN() != null) {
            rule.setAction(IptablesActionType.Return, null);
         }
         else if (target.chain() != null) {
            rule.setAction(IptablesActionType.Chain, target.chain().getText());
         }
      }
      else if (ctx.action().OPTION_GOTO() != null) {
         rule.setAction(IptablesActionType.Goto, ctx.action().chain().getText());
      }
      else {
         todo(ctx, "Unknown rule action");            
      }
      
      return rule;
   }

   private Object getEndpoint(EndpointContext endpoint) {
      if (endpoint.IP_ADDRESS() != null) {
         return new Ip(endpoint.IP_ADDRESS().getText());
      }
      else if (endpoint.IP_PREFIX() != null) {
         return new Prefix(endpoint.IP_PREFIX().getText());
      }
      else if (endpoint.IPV6_ADDRESS() != null) {
         return new Ip6(endpoint.IPV6_ADDRESS().getText());
      }
      else if (endpoint.IPV6_PREFIX() != null) {
         return new Prefix6(endpoint.IPV6_PREFIX().getText());
      }
      else if (endpoint.name != null) {
         return endpoint.name.getText();
      }
      else {
         todo(endpoint, "Unknown endpoint");
      }
      return null;
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
