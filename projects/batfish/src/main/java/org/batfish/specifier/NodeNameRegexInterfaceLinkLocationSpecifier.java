package org.batfish.specifier;

import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifier} specifying links of interfaces belonging to nodes with names matching
 * the input regex.
 */
public final class NodeNameRegexInterfaceLinkLocationSpecifier
    extends NodeNameRegexInterfaceLocationSpecifier {
  public NodeNameRegexInterfaceLinkLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Location makeLocation(String node, String iface) {
    return new InterfaceLinkLocation(node, iface);
  }
}
