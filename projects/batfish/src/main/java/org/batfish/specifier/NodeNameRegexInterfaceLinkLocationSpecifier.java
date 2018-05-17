package org.batfish.specifier;

import java.util.regex.Pattern;

public class NodeNameRegexInterfaceLinkLocationSpecifier
    extends NodeNameRegexInterfaceLocationSpecifier {
  public NodeNameRegexInterfaceLinkLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Location makeLocation(String node, String iface) {
    return new InterfaceLinkLocation(node, iface);
  }
}
