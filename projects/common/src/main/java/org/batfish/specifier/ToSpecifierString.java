package org.batfish.specifier;

import static org.batfish.datamodel.Names.escapeNameIfNeeded;

import org.batfish.common.util.isp.IspModelingUtils;

/** Converts a {@link Location} to its specifier string */
public final class ToSpecifierString implements LocationVisitor<String> {
  private static final ToSpecifierString INSTANCE = new ToSpecifierString();

  private ToSpecifierString() {}

  public static String toSpecifierString(Location location) {
    return location.accept(INSTANCE);
  }

  @Override
  public String visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation) {
    // special-case for traffic originating from the internet.
    // TODO check the device type to be completely consistent with the location specifier
    if (interfaceLinkLocation.getNodeName().equals(IspModelingUtils.INTERNET_HOST_NAME)
        && interfaceLinkLocation
            .getInterfaceName()
            .equals(IspModelingUtils.INTERNET_OUT_INTERFACE)) {
      return IspModelingUtils.INTERNET_HOST_NAME;
    }
    return String.format(
        "@enter(%s[%s])",
        escapeNameIfNeeded(interfaceLinkLocation.getNodeName()),
        escapeNameIfNeeded(interfaceLinkLocation.getInterfaceName()));
  }

  @Override
  public String visitInterfaceLocation(InterfaceLocation interfaceLocation) {
    return String.format(
        "%s[%s]",
        escapeNameIfNeeded(interfaceLocation.getNodeName()),
        escapeNameIfNeeded(interfaceLocation.getInterfaceName()));
  }
}
