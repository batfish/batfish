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

  /**
   * This function is called when Batfish will no longer provide any text to parse.
   *
   * <p>Implementors of {@link ControlPlaneExtractor} may override this function to cleanup or
   * otherwise post-process their configuration before it is serialized and saved.
   */
  default void doneParsing() {}

  VendorConfiguration getVendorConfiguration();
}
