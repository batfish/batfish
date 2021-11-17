package org.batfish.representation.frr;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/**
 * Represents configuration outside of the FRR configuration file (e.g., in /etc/network/interfaces)
 */
public interface OutOfBandConfiguration {

  /** Does the config have an interface with this name? */
  boolean hasInterface(String ifaceName);

  /** Does the config have a VRF with this name? */
  boolean hasVrf(String vrfName);

  /**
   * Return the VRF name for the specified interface name.
   *
   * @throws java.util.NoSuchElementException if the interface does not exist.
   */
  String getInterfaceVrf(String ifaceName);

  /**
   * Return the configured concrete addresses for the specified interface name.
   *
   * @throws java.util.NoSuchElementException if the interface does not exist.
   */
  @Nonnull
  List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName);

  // TODO: Simplify and unbundle what is happening in this method
  Map<String, Vxlan> getVxlans();

  // TODO: Simplify and unbundle what is happening in this method
  Optional<String> getVrfForVlan(Integer bridgeAccessVlan);
}
