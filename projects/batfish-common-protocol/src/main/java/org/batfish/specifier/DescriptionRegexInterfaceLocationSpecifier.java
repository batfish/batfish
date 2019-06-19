package org.batfish.specifier;

import java.util.regex.Pattern;
import org.batfish.datamodel.Interface;

/**
 * A {@link LocationSpecifier} specifying all interfaces whose description matches the input regex.
 */
public final class DescriptionRegexInterfaceLocationSpecifier
    extends InterfaceDescriptionRegexLocationSpecifier {
  public DescriptionRegexInterfaceLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Location getLocation(Interface iface) {
    return new InterfaceLocation(iface.getOwner().getHostname(), iface.getName());
  }
}
