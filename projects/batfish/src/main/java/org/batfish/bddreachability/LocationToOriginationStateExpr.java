package org.batfish.bddreachability;

import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.LocationVisitor;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.StateExpr;

class LocationToOriginationStateExpr implements LocationVisitor<Optional<StateExpr>> {
  private final Map<String, Configuration> _configs;

  LocationToOriginationStateExpr(Map<String, Configuration> configs) {
    _configs = configs;
  }

  @Override
  public Optional<StateExpr> visitInterfaceLinkLocation(
      @Nonnull InterfaceLinkLocation interfaceLinkLocation) {
    return Optional.of(
        new OriginateInterfaceLink(
            interfaceLinkLocation.getNodeName(), interfaceLinkLocation.getInterfaceName()));
  }

  @Override
  public Optional<StateExpr> visitInterfaceLocation(@Nonnull InterfaceLocation interfaceLocation) {
    Configuration config = _configs.get(interfaceLocation.getNodeName());
    if (config == null) {
      return Optional.empty();
    }
    Interface iface = config.getAllInterfaces().get(interfaceLocation.getInterfaceName());
    if (iface == null) {
      return Optional.empty();
    }
    String vrf = iface.getVrfName();
    return Optional.of(new OriginateVrf(interfaceLocation.getNodeName(), vrf));
  }
}
