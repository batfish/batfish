package org.batfish.specifier;

import java.util.regex.Pattern;

public class VrfNameRegexInterfaceLinkLocationSpecifier
    extends VrfNameRegexInterfaceLocationSpecifier {
  public VrfNameRegexInterfaceLinkLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Location makeLocation(String node, String iface) {
    return new InterfaceLinkLocation(node, iface);
  }
}
