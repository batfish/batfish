package org.batfish.grammar;

import java.util.Set;

import org.batfish.representation.VendorConfiguration;

public interface ControlPlaneExtractor extends BatfishExtractor {

   Set<String> getUnimplementedFeatures();

   VendorConfiguration getVendorConfiguration();

}
