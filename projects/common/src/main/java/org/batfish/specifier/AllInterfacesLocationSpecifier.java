package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/** A {@link LocationSpecifier} specifying all interfaces in the network. */
public final class AllInterfacesLocationSpecifier implements LocationSpecifier {
  public static final LocationSpecifier INSTANCE = new AllInterfacesLocationSpecifier();

  private AllInterfacesLocationSpecifier() {}

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .flatMap(node -> node.getAllInterfaces().values().stream())
        .map(iface -> new InterfaceLocation(iface.getOwner().getHostname(), iface.getName()))
        .collect(ImmutableSet.toImmutableSet());
  }
}
