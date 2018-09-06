package org.batfish.z3;

import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.LocationVisitor;

/**
 * A {@link LocationVisitor} that converts to {@link IngressLocation}s, mapping {@link
 * InterfaceLinkLocation}s to {@link IngressLocation.Type#INTERFACE_LINK} ingress locations, and
 * {@link InterfaceLocation}s to {@link IngressLocation.Type#VRF} ingress locations.
 */
public final class LocationToIngressLocation implements LocationVisitor<IngressLocation> {
  private final Map<String, Configuration> _configs;

  public LocationToIngressLocation(Map<String, Configuration> configs) {
    _configs = configs;
  }

  @Override
  public IngressLocation visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation) {
    return IngressLocation.interfaceLink(
        interfaceLinkLocation.getNodeName(), interfaceLinkLocation.getInterfaceName());
  }

  @Override
  public IngressLocation visitInterfaceLocation(InterfaceLocation interfaceLocation) {
    String node = interfaceLocation.getNodeName();
    String iface = interfaceLocation.getInterfaceName();
    return IngressLocation.vrf(node, _configs.get(node).getAllInterfaces().get(iface).getVrfName());
  }
}
