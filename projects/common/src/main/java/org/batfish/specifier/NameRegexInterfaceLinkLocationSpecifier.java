package org.batfish.specifier;

import java.util.regex.Pattern;
import org.batfish.datamodel.Interface;

/**
 * A {@link LocationSpecifier} specifying links of interfaces with names matching the input regex.
 */
public final class NameRegexInterfaceLinkLocationSpecifier
    extends InterfaceNameRegexLocationSpecifier {
  public NameRegexInterfaceLinkLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  public NameRegexInterfaceLinkLocationSpecifier(String pattern) {
    super(Pattern.compile(pattern));
  }

  @Override
  protected Location getLocation(Interface iface) {
    return new InterfaceLinkLocation(iface.getOwner().getHostname(), iface.getName());
  }
}
