package org.batfish.specifier;

import java.util.regex.Pattern;

public class NameRegexInterfaceLinkLocationSpecifier extends NameRegexInterfaceLocationSpecifier {
  public NameRegexInterfaceLinkLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Location makeLocation(String node, String iface) {
    return new InterfaceLinkLocation(node, iface);
  }
}
