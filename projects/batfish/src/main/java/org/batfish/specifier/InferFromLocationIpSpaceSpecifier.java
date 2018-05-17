package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.IpSpace;

public class InferFromLocationIpSpaceSpecifier implements IpSpaceSpecifier {
  public static final InferFromLocationIpSpaceSpecifier INSTANCE =
      new InferFromLocationIpSpaceSpecifier();

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
      return AclIpSpace.union(
          interfaceAddresses(
                  interfaceLinkLocation.getNodeName(), interfaceLinkLocation.getInterfaceName())
              .stream()
              .map(
                  address ->
                      // any IP within the prefix, other than the one assigned to the interface
                      AclIpSpace.difference(
                          address.getPrefix().toIpSpace(), address.getIp().toIpSpace()))
              .collect(Collectors.toSet()));
    }

    @Override
    public IpSpace visitInterfaceLocation(InterfaceLocation interfaceLocation) {
      return AclIpSpace.union(
          interfaceAddresses(interfaceLocation.getNodeName(), interfaceLocation.getInterfaceName())
              .stream()
              .map(address -> address.getIp().toIpSpace())
              .collect(Collectors.toSet()));
    }
  }

  private InferFromLocationIpSpaceSpecifier() {}

  @Override
  public Map<Set<Location>, IpSpace> resolve(Set<Location> locations, SpecifierContext ctxt) {
    IpSpaceLocationVisitor ipSpaceLocationVisitor = new IpSpaceLocationVisitor(ctxt);
    // for each location, map the singleton set of that location to its sane IP space
    return CommonUtil.toImmutableMap(
        locations, ImmutableSet::of, location -> location.accept(ipSpaceLocationVisitor));
  }
}
