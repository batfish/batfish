package batfish.grammar.juniper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.ControlPlaneExtractor;
//import batfish.grammar.juniper.JuniperGrammarParser.*;
import batfish.representation.VendorConfiguration;

public class JuniperControlPlaneExtractor implements ControlPlaneExtractor {

   private List<String> _warnings;

   public JuniperControlPlaneExtractor(String fileText,
         BatfishCombinedParser<?, ?> combinedParser,
         Set<String> rulesWithSuppressedWarnings) {
      _warnings = new ArrayList<String>();
   }

   @Override
   public VendorConfiguration getVendorConfiguration() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public List<String> getWarnings() {
      return _warnings;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      // ParseTreeWalker walker = new ParseTreeWalker();
      // TODO: everything
   }

}
