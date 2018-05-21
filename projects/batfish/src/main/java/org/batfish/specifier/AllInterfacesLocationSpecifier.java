package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/** A {@link LocationSpecifier} specifying all interfaces in the network. */
public class AllInterfacesLocationSpecifier implements LocationSpecifier {
  public static final LocationSpecifier INSTANCE = new AllInterfacesLocationSpecifier();

  AllInterfacesLocationSpecifier() {}

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return ctxt.getConfigs()
        .entrySet()
        .stream()
        .flatMap(
            entry -> {
              String node = entry.getKey();
              return entry
                  .getValue()
                  .getInterfaces()
                  .keySet()
                  .stream()
                  .map(iface -> makeLocation(node, iface));
            })
        .collect(ImmutableSet.toImmutableSet());
  }

  protected Location makeLocation(String node, String iface) {
    return new InterfaceLocation(node, iface);
  }
}
