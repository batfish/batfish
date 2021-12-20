package org.batfish.representation.frr;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.vendor.VendorConfiguration;

/**
 * Abstract class for for vendors that use FRR. Individual such vendors should extend this class.
 */
public abstract class FrrVendorConfiguration extends VendorConfiguration {

  /** Return the FRR portion of the configuration */
  public abstract FrrConfiguration getFrrConfiguration();

  /** Does the config have an interface with this name? */
  public abstract boolean hasInterface(String ifaceName);

  /** Does the config have a VRF with this name? */
  public abstract boolean hasVrf(String vrfName);

  /**
   * Return the VRF name for the specified interface name.
   *
   * @throws java.util.NoSuchElementException if the interface does not exist.
   */
  public abstract String getInterfaceVrf(String ifaceName);

  /**
   * Return the configured concrete addresses for the specified interface name.
   *
   * @throws java.util.NoSuchElementException if the interface does not exist.
   */
  @Nonnull
  public abstract List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName);

  // TODO: Simplify and unbundle what is happening in this method
  public abstract Map<String, Vxlan> getVxlans();

  /** Logs a structure reference seen during FRR file parsing. */
  public abstract void referenceStructure(
      FrrStructureType type, String name, FrrStructureUsage usage, int line);

  /** Logs a structure definition seen during FRR file parsing. */
  public abstract void defineStructure(FrrStructureType type, String name, ParserRuleContext ctx);
}
