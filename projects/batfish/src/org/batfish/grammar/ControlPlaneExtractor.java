package org.batfish.grammar;

import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.representation.VendorConfiguration;

public interface ControlPlaneExtractor {

   Set<String> getUnimplementedFeatures();

   VendorConfiguration getVendorConfiguration();

   void processParseTree(ParserRuleContext tree);

}
