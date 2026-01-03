package org.batfish.specifier;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.batfish.datamodel.Configuration;

/**
 * A {@link LocationSpecifier} specifying interfaces belonging to nodes with roles matching the
 * input dimension and regex.
 */
public final class NodeRoleRegexInterfaceLocationSpecifier extends NodeRoleRegexLocationSpecifier {
  public NodeRoleRegexInterfaceLocationSpecifier(String roleDimension, Pattern rolePattern) {
    super(roleDimension, rolePattern);
  }

  @Override
  Stream<Location> getNodeLocations(Configuration node) {
    return node.getAllInterfaces().values().stream()
        .map(iface -> new InterfaceLocation(node.getHostname(), iface.getName()));
  }
}
