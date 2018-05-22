package org.batfish.specifier;

import java.util.regex.Pattern;
import org.batfish.datamodel.Interface;

/**
 * A {@link LocationSpecifier} specifying all links of interfaces whose description matches the
 * input regex.
 */
public final class DescriptionRegexInterfaceLinkLocationSpecifier
    extends InterfaceDescriptionRegexLocationSpecifier {
  public DescriptionRegexInterfaceLinkLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Location getLocation(Interface iface) {
    return new InterfaceLinkLocation(iface.getOwner().getHostname(), iface.getName());
  }
}
