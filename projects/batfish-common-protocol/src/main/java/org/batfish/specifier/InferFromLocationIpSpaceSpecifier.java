package org.batfish.specifier;

import java.util.Set;
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

    @Override
    public IpSpace visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation) {
      return _specifierContext.getInterfaceLinkOwnedIps(
          interfaceLinkLocation.getNodeName(), interfaceLinkLocation.getInterfaceName());
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
