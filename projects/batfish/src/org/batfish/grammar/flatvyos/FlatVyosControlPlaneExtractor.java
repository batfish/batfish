package org.batfish.grammar.flatvyos;

import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.flatvyos.FlatVyosParser.Flat_vyos_configurationContext;
import org.batfish.grammar.flatvyos.FlatVyosParser.St_host_nameContext;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.vyos.VyosConfiguration;
import org.batfish.representation.vyos.VyosVendorConfiguration;

public class FlatVyosControlPlaneExtractor extends FlatVyosParserBaseListener
      implements ControlPlaneExtractor {

   private VyosConfiguration _configuration;

   private final FlatVyosCombinedParser _parser;

   private final String _text;

   private final Set<String> _unimplementedFeatures;

   private VyosVendorConfiguration _vendorConfiguration;

   private final Warnings _w;

   public FlatVyosControlPlaneExtractor(String text,
         FlatVyosCombinedParser parser, Warnings warnings) {
      _text = text;
      _parser = parser;
      _unimplementedFeatures = new TreeSet<String>();
      _w = warnings;
   }

   @Override
   public void enterFlat_vyos_configuration(Flat_vyos_configurationContext ctx) {
      _vendorConfiguration = new VyosVendorConfiguration();
      _configuration = _vendorConfiguration;
   }

   @Override
   public void exitSt_host_name(St_host_nameContext ctx) {
      String hostname = ctx.name.getText();
      _configuration.setHostname(hostname);
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
      _unimplementedFeatures.add("Vyos: " + feature);
   }

}
