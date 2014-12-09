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

   private JuniperVendorConfiguration _configuration;
   private BatfishCombinedParser<?, ?> _parser;
   private Set<String> _rulesWithSuppressedWarnings;
   private String _text;
   private List<String> _warnings;

   public FlatJuniperControlPlaneExtractor(String fileText,
         BatfishCombinedParser<?, ?> combinedParser,
         Set<String> rulesWithSuppressedWarnings) {
      _text = fileText;
      _parser = combinedParser;
      _rulesWithSuppressedWarnings = rulesWithSuppressedWarnings;
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
      Hierarchy hierarchy = new Hierarchy();
      ParseTreeWalker walker = new ParseTreeWalker();
      InitialTreeBuilder tb = new InitialTreeBuilder(hierarchy);
      walker.walk(tb, tree);
      ApplyGroupsApplicator hb = new ApplyGroupsApplicator(_parser, hierarchy);
      walker.walk(hb, tree);
      GroupPruner gp = new GroupPruner();
      walker.walk(gp, tree);
      WildcardApplicator wa = new WildcardApplicator(hierarchy);
      walker.walk(wa, tree);
      WildcardPruner wp = new WildcardPruner();
      walker.walk(wp, tree);
      ConfigurationBuilder cb = new ConfigurationBuilder(_parser, _text,
            _rulesWithSuppressedWarnings, _warnings);
      walker.walk(cb, tree);
      _configuration = cb.getConfiguration();
   }

}
