package batfish.grammar.flatjuniper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.ParseTreePrettyPrinter;
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.*;
import batfish.representation.Prefix;
import batfish.representation.juniper.Interface;
import batfish.representation.juniper.JuniperVendorConfiguration;
import batfish.representation.juniper.RoutingInformationBase;
import batfish.representation.juniper.RoutingInstance;

public class ConfigurationBuilder extends FlatJuniperGrammarParserBaseListener {

   private JuniperVendorConfiguration _configuration;

   private Interface _currentInterface;

   private Interface _currentMasterInterface;

   private RoutingInformationBase _currentRib;

   private RoutingInstance _currentRoutingInstance;

   private BatfishCombinedParser<?, ?> _parser;

   private Set<String> _rulesWithSuppressedWarnings;

   private String _text;

   private List<String> _warnings;

   public ConfigurationBuilder(BatfishCombinedParser<?, ?> parser, String text,
         Set<String> rulesWithSuppressedWarnings, List<String> warnings) {
      _parser = parser;
      _text = text;
      _rulesWithSuppressedWarnings = rulesWithSuppressedWarnings;
      _warnings = warnings;
      _configuration = new JuniperVendorConfiguration();
      _currentRoutingInstance = _configuration.getDefaultRoutingInstance();
   }

   @Override
   public void enterIt_unit(It_unitContext ctx) {
      String unit = ctx.it_unit_header().num.getText();
      Map<String, Interface> units = _currentMasterInterface.getUnits();
      _currentInterface = units.get(unit);
      if (_currentInterface == null) {
         _currentInterface = new Interface(unit);
         units.put(unit, _currentInterface);
      }
   }

   @Override
   public void enterRit_named_routing_instance(
         Rit_named_routing_instanceContext ctx) {
      String name;
      name = ctx.name.getText();
      _currentRoutingInstance = _configuration.getRoutingInstances().get(name);
      if (_currentRoutingInstance == null) {
         _currentRoutingInstance = new RoutingInstance(name);
         _configuration.getRoutingInstances()
               .put(name, _currentRoutingInstance);
      }
   }

   @Override
   public void enterRot_rib(Rot_ribContext ctx) {
      String name = ctx.name.getText();
      Map<String, RoutingInformationBase> ribs = _currentRoutingInstance
            .getRibs();
      _currentRib = ribs.get(name);
      if (_currentRib == null) {
         _currentRib = new RoutingInformationBase(name);
         ribs.put(name, _currentRib);
      }
   }

   @Override
   public void enterS_interfaces(S_interfacesContext ctx) {
      String ifaceName = ctx.s_interfaces_header().name.getText();
      Map<String, Interface> interfaces = _currentRoutingInstance
            .getInterfaces();
      _currentInterface = interfaces.get(ifaceName);
      if (_currentInterface == null) {
         _currentInterface = new Interface(ifaceName);
         interfaces.put(ifaceName, _currentInterface);
      }
      _currentMasterInterface = _currentInterface;
   }

   @Override
   public void enterS_routing_options(S_routing_optionsContext ctx) {
      _currentRib = _currentRoutingInstance.getRibs().get(
            RoutingInformationBase.RIB_IPV4_UNICAST);
   }

   @Override
   public void exitIfamt_address(Ifamt_addressContext ctx) {
      Prefix prefix = new Prefix(ctx.IP_ADDRESS_WITH_MASK().getText());
      _currentInterface.setPrefix(prefix);
   }

   @Override
   public void exitIfamt_filter(Ifamt_filterContext ctx) {
      FilterContext filter = ctx.filter();
      String name = filter.name.getText();
      DirectionContext direction = ctx.filter().direction();
      if (direction.INPUT() != null) {
         _currentInterface.setIncomingFilter(name);
      }
      else if (direction.OUTPUT() != null) {
         _currentInterface.setOutgoingFilter(name);
      }
   }

   @Override
   public void exitIt_disable(It_disableContext ctx) {
      _currentInterface.setActive(false);
   }

   @Override
   public void exitIt_unit(It_unitContext ctx) {
      _currentInterface = _currentMasterInterface;
   }

   @Override
   public void exitS_interfaces(S_interfacesContext ctx) {
      _currentInterface = null;
      _currentMasterInterface = null;
   }

   @Override
   public void exitS_routing_options(S_routing_optionsContext ctx) {
      _currentRib = null;
   }

   @Override
   public void exitSt_host_name(St_host_nameContext ctx) {
      String hostname = ctx.variable().getText();
      _currentRoutingInstance.setHostname(hostname);
   }

   public JuniperVendorConfiguration getConfiguration() {
      return _configuration;
   }

   @SuppressWarnings("unused")
   private void todo(ParserRuleContext ctx) {
      todo(ctx, "Unknown");
   }

   private void todo(ParserRuleContext ctx, String reason) {
      String ruleName = _parser.getParser().getRuleNames()[ctx.getRuleIndex()];
      if (_rulesWithSuppressedWarnings.contains(ruleName)) {
         return;
      }
      String prefix = "WARNING " + (_warnings.size() + 1) + ": ";
      StringBuilder sb = new StringBuilder();
      List<String> ruleNames = Arrays
            .asList(FlatJuniperGrammarParser.ruleNames);
      String ruleStack = ctx.toString(ruleNames);
      sb.append(prefix
            + "Missing implementation for top (leftmost) parser rule in stack: '"
            + ruleStack + "'.\n");
      sb.append(prefix + "Reason: " + reason + "\n");
      sb.append(prefix + "Rule context follows:\n");
      int start = ctx.start.getStartIndex();
      int startLine = ctx.start.getLine();
      int end = ctx.stop.getStopIndex();
      String ruleText = _text.substring(start, end + 1);
      String[] ruleTextLines = ruleText.split("\\n");
      for (int line = startLine, i = 0; i < ruleTextLines.length; line++, i++) {
         String contextPrefix = prefix + " line " + line + ": ";
         sb.append(contextPrefix + ruleTextLines[i] + "\n");
      }
      sb.append(prefix + "Parse tree follows:\n");
      String parseTreePrefix = prefix + "PARSE TREE: ";
      String parseTreeText = ParseTreePrettyPrinter.print(ctx, _parser);
      String[] parseTreeLines = parseTreeText.split("\n");
      for (String parseTreeLine : parseTreeLines) {
         sb.append(parseTreePrefix + parseTreeLine + "\n");
      }
      _warnings.add(sb.toString());
   }

}
