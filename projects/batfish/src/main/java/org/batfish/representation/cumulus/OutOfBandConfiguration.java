package org.batfish.representation.cumulus;

/**
 * Represents configuration outside of the FRR configuration file (e.g., in /etc/network/interfaces)
 */
public interface OutOfBandConfiguration {

  boolean hasInterface(String ifaceName);

  String getInterfaceVrf(String ifaceName);

  boolean hasVrf(String vrfName);
}
