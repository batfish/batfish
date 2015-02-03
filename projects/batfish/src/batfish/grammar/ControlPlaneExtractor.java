package batfish.grammar;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.representation.VendorConfiguration;

public interface ControlPlaneExtractor {

   List<String> getPedanticWarnings();

   List<String> getRedFlagWarnings();

   List<String> getUnimplementedWarnings();

   VendorConfiguration getVendorConfiguration();

   void processParseTree(ParserRuleContext tree);

}
