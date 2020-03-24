package org.batfish.specifier;

/** Converts a {@link Location} to its specifier string */
public final class ToSpecifierString implements LocationVisitor<String> {
  private static final ToSpecifierString INSTANCE = new ToSpecifierString();

  private ToSpecifierString() {}

  public static String toSpecifierString(Location location) {
    return location.accept(INSTANCE);
  }

  @Override
  public String visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation) {
    return String.format(
        "@enter(%s[%s])",
        interfaceLinkLocation.getNodeName(), interfaceLinkLocation.getInterfaceName());
  }

  @Override
  public String visitInterfaceLocation(InterfaceLocation interfaceLocation) {
    return String.format(
        "%s[%s]", interfaceLocation.getNodeName(), interfaceLocation.getInterfaceName());
  }
}
