package org.batfish.specifier;

import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.IpSpace;

/**
 * An {@link IpSpaceSpecifier} that specifies the {@link IpSpace} owned by each {@link Location}.
 */
public final class InferFromLocationIpSpaceSpecifier implements IpSpaceSpecifier {
  public static final InferFromLocationIpSpaceSpecifier INSTANCE =
      new InferFromLocationIpSpaceSpecifier();

  /** A {@link LocationVisitor} that returns the {@link IpSpace} owned by that {@link Location}. */
  class IpSpaceLocationVisitor implements LocationVisitor<IpSpace> {
    private final SpecifierContext _specifierContext;

    IpSpaceLocationVisitor(SpecifierContext specifierContext) {
      _specifierContext = specifierContext;
    }

    private Set<InterfaceAddress> interfaceAddresses(String node, String iface) {
      return _specifierContext.getConfigs().get(node).getInterfaces().get(iface).getAllAddresses();
    }

    @Override
    public IpSpace visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation) {
      String node = interfaceLinkLocation.getNodeName();
      String iface = interfaceLinkLocation.getInterfaceName();
      return AclIpSpace.difference(
          AclIpSpace.union(
              interfaceAddresses(node, iface)
                  .stream()
                  .map(address -> address.getPrefix().toIpSpace())
                  .collect(Collectors.toList())),
          _specifierContext.getInterfaceOwnedIps(node, iface));
    }

    @Override
    public IpSpace visitInterfaceLocation(InterfaceLocation interfaceLocation) {
      return _specifierContext.getInterfaceOwnedIps(
          interfaceLocation.getNodeName(), interfaceLocation.getInterfaceName());
    }
  }

  private InferFromLocationIpSpaceSpecifier() {}

  @Override
  public IpSpaceAssignment resolve(Set<Location> locations, SpecifierContext ctxt) {
    IpSpaceLocationVisitor ipSpaceLocationVisitor = new IpSpaceLocationVisitor(ctxt);
    IpSpaceAssignment.Builder builder = IpSpaceAssignment.builder();
    locations.forEach(
        location -> builder.assign(location, location.accept(ipSpaceLocationVisitor)));
    return builder.build();
  }
}
