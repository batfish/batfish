package org.batfish.grammar.iptables;

import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.iptables.IptablesParser.Iptables_configurationContext;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.iptables.IptablesConfiguration;
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

}
