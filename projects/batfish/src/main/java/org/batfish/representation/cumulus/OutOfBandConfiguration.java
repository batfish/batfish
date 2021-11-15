package org.batfish.representation.cumulus;

import java.util.List;
import org.batfish.datamodel.ConcreteInterfaceAddress;

/**
 * Represents configuration outside of the FRR configuration file (e.g., in /etc/network/interfaces)
 */
public interface OutOfBandConfiguration {

  boolean hasInterface(String ifaceName);

  String getInterfaceVrf(String ifaceName);

  List<ConcreteInterfaceAddress> getInterfaceAddresses(String ifaceName);

  boolean hasVrf(String vrfName);
}
