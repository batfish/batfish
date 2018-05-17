package org.batfish.specifier;

public class AllInterfaceLinksLocationSpecifier extends AllInterfacesLocationSpecifier {
  public static final AllInterfacesLocationSpecifier INSTANCE =
      new AllInterfaceLinksLocationSpecifier();

  private AllInterfaceLinksLocationSpecifier() {
    super();
  }

  @Override
  protected Location makeLocation(String node, String iface) {
    return new InterfaceLinkLocation(node, iface);
  }
}
