package org.batfish.specifier;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link LocationSpecifier} that yields {@link InterfaceLocation}s based on a {@code
 * NodeSpecifier}. The {@link NodeSpecifier} helps select the nodes and locations include all
 * interfaces on those nodes.
 */
@ParametersAreNonnullByDefault
public class NodeSpecifierInterfaceLocationSpecifier implements LocationSpecifier {
  private NodeSpecifier _nodeSpecifier;

  public NodeSpecifierInterfaceLocationSpecifier(NodeSpecifier nodeSpecifier) {
    _nodeSpecifier = nodeSpecifier;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NodeSpecifierInterfaceLocationSpecifier)) {
      return false;
    }
    return Objects.equals(
        _nodeSpecifier, ((NodeSpecifierInterfaceLocationSpecifier) o)._nodeSpecifier);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_nodeSpecifier);
  }

  @Override
  public Set<Location> resolve(SpecifierContext ctxt) {
    return _nodeSpecifier.resolve(ctxt).stream()
        .map(n -> ctxt.getConfigs().get(n).getAllInterfaces().values())
        .flatMap(Collection::stream)
        .map(iface -> new InterfaceLocation(iface.getOwner().getHostname(), iface.getName()))
        .collect(ImmutableSet.toImmutableSet());
  }
}
