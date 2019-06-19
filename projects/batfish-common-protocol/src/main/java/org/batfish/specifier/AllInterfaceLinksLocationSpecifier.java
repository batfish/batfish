package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/** A {@link LocationSpecifier} specifying all interface links in the network. */
public final class AllInterfaceLinksLocationSpecifier implements LocationSpecifier {
  public static final LocationSpecifier INSTANCE = new AllInterfaceLinksLocationSpecifier();

  private AllInterfaceLinksLocationSpecifier() {}

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs().values().stream()
        .flatMap(node -> node.getAllInterfaces().values().stream())
        .map(iface -> new InterfaceLinkLocation(iface.getOwner().getHostname(), iface.getName()))
        .collect(ImmutableSet.toImmutableSet());
  }
}
