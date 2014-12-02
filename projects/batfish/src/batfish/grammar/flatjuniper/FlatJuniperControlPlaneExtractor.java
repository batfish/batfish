package batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.ControlPlaneExtractor;
import batfish.representation.VendorConfiguration;
import batfish.representation.juniper.JuniperVendorConfiguration;

public class FlatJuniperControlPlaneExtractor implements ControlPlaneExtractor {

   private List<String> _warnings;
   private JuniperVendorConfiguration _configuration;

   public FlatJuniperControlPlaneExtractor(String fileText,
         BatfishCombinedParser<?, ?> combinedParser,
         Set<String> rulesWithSuppressedWarnings) {
      _warnings = new ArrayList<String>();
   }

   @Override
   public VendorConfiguration getVendorConfiguration() {
      return _configuration;
   }

   @Override
   public List<String> getWarnings() {
      return _warnings;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      ParseTreeWalker walker = new ParseTreeWalker();
      HierachyBuilder hb = new HierachyBuilder();
      walker.walk(hb, tree);
      Hierarchy hierarchy = hb.getHierarchy();
      GroupPruner gp = new GroupPruner();
      walker.walk(gp, tree);
      GroupApplicator ga = new GroupApplicator(hierarchy);
      walker.walk(ga, tree);
      ConfigurationBuilder cb = new ConfigurationBuilder();
      walker.walk(cb, tree);
      _configuration = cb.getConfiguration();
   }

}
