package batfish.grammar;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import batfish.representation.VendorConfiguration;

public interface ControlPlaneExtractor {

   VendorConfiguration getVendorConfiguration();

   List<String> getWarnings();

   void processParseTree(ParserRuleContext tree);

}
