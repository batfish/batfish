package org.batfish.grammar.mrv;

import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.mrv.MrvParser.Mrv_configurationContext;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.mrv.MrvConfiguration;
import org.batfish.representation.mrv.MrvVendorConfiguration;

public class MrvControlPlaneExtractor extends MrvParserBaseListener
      implements ControlPlaneExtractor {

   @SuppressWarnings("unused")
   private MrvConfiguration _configuration;

   private MrvCombinedParser _parser;

   private String _text;

   private final Set<String> _unimplementedFeatures;

   private MrvVendorConfiguration _vendorConfiguration;

   private Warnings _w;

   public MrvControlPlaneExtractor(String fileText, MrvCombinedParser mrvParser,
         Warnings warnings) {
      _text = fileText;
      _parser = mrvParser;
      _w = warnings;
      _unimplementedFeatures = new TreeSet<>();
   }

   @Override
   public void enterMrv_configuration(Mrv_configurationContext ctx) {
      _vendorConfiguration = new MrvVendorConfiguration();
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
