package org.batfish.specifier;

import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifier} specifying links of interfaces belonging to nodes with roles matching
 * the input dimension and regex.
 */
public final class NodeRoleRegexInterfaceLinkLocationSpecifier
    extends NodeRoleRegexInterfaceLocationSpecifier {
  public NodeRoleRegexInterfaceLinkLocationSpecifier(String roleDimension, Pattern rolePattern) {
    super(roleDimension, rolePattern);
  }

  @Override
  protected Location makeLocation(String node, String iface) {
    return new InterfaceLinkLocation(node, iface);
  }
}
