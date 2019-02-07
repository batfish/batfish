package org.batfish.grammar;

import org.batfish.vendor.VendorConfiguration;

/**
 * Producer of a {@link VendorConfiguration}.
 *
 * <p>Typically extended by a class that consumes a parse tree from a configuration file. That
 * subclass can either extend some XXXBaseListener, or delegate to one or more of them. Delegation
 * has the advantage the the {@link ControlPlaneExtractor} is then allowed to extend any arbitrary
 * class rather than just some XXXBaseListener.
 */
public interface ControlPlaneExtractor extends BatfishExtractor {

  VendorConfiguration getVendorConfiguration();
}
