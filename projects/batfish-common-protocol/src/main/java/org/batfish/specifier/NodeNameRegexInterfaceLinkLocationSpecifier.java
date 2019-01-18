package org.batfish.specifier;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.batfish.datamodel.Configuration;

/**
 * A {@link LocationSpecifier} specifying links of interfaces belonging to nodes with names matching
 * the input regex.
 */
public final class NodeNameRegexInterfaceLinkLocationSpecifier
    extends NodeNameRegexLocationSpecifier {
  public NodeNameRegexInterfaceLinkLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Stream<Location> getNodeLocations(Configuration node) {
    return node.getAllInterfaces().values().stream()
        .map(iface -> new InterfaceLinkLocation(node.getHostname(), iface.getName()));
  }
}
