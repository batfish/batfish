package org.batfish.specifier;

import java.util.regex.Pattern;

/**
 * A {@link LocationSpecifier} specifying links of interfaces that belong to VRFs with names
 * matching the input regex.
 */
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
