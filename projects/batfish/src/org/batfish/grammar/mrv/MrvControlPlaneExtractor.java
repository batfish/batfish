package org.batfish.grammar.mrv;

import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.mrv.MrvParser.*;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.mrv.MrvConfiguration;

public class MrvControlPlaneExtractor extends MrvParserBaseListener
      implements ControlPlaneExtractor {

   private MrvConfiguration _configuration;

   private MrvCombinedParser _parser;

   private String _text;

   private final Set<String> _unimplementedFeatures;

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
      _configuration = new MrvConfiguration();
   }
   
   @Override
   public void exitA_system_systemname(A_system_systemnameContext ctx) {
      Token text = ctx.nsdecl().quoted_string().text;
      if (text != null) {
         String hostname = text.getText();
         _configuration.setHostname(hostname);
      }
   }

   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @Override
   public VendorConfiguration getVendorConfiguration() {
      return _configuration;
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
