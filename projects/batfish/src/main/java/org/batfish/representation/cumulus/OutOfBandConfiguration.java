package org.batfish.representation.cumulus;

import java.util.List;
import java.util.Map;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/**
 * Represents configuration outside of the FRR configuration file (e.g., in /etc/network/interfaces)
 */
public interface OutOfBandConfiguration {

  boolean hasInterface(String ifaceName);

  String getInterfaceVrf(String ifaceName);

  List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName);

  boolean hasVrf(String vrfName);

  // TODO: Simplify and unbundle what is happening in this method
  Map<String, Vxlan> getVxlans();
}
