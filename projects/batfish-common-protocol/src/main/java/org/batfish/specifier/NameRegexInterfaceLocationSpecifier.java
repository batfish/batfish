package org.batfish.specifier;

import java.util.regex.Pattern;
import org.batfish.datamodel.Interface;

/** A {@link LocationSpecifier} specifying interfaces with names matching the input regex. */
public final class NameRegexInterfaceLocationSpecifier extends InterfaceNameRegexLocationSpecifier {
  public NameRegexInterfaceLocationSpecifier(Pattern pattern) {
    super(pattern);
  }

  @Override
  protected Location getLocation(Interface iface) {
    return new InterfaceLocation(iface.getOwner().getHostname(), iface.getName());
  }
}
