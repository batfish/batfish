package org.batfish.specifier;

import java.util.regex.Pattern;

public class NodeRoleRegexInterfaceLinkLocationSpecifier
    extends NodeRoleRegexInterfaceLocationSpecifier {
  public NodeRoleRegexInterfaceLinkLocationSpecifier(String roleDimension, Pattern rolePattern) {
    super(roleDimension, rolePattern);
  }

  @Override
  protected Location makeLocation(String node, String iface) {
    return new InterfaceLinkLocation(node, iface);
  }
}
