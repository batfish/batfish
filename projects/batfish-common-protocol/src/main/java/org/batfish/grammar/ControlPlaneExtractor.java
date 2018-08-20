package org.batfish.grammar;

import org.batfish.vendor.VendorConfiguration;

public interface ControlPlaneExtractor extends BatfishExtractor {

  VendorConfiguration getVendorConfiguration();
}
