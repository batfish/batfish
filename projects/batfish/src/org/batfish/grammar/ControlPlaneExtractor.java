package org.batfish.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.representation.VendorConfiguration;

public interface ControlPlaneExtractor {

   VendorConfiguration getVendorConfiguration();

   void processParseTree(ParserRuleContext tree);

}
