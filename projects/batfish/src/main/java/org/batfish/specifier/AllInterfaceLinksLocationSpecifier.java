package org.batfish.specifier;

/** A {@link LocationSpecifier} specifying all interface links in the network. */
public final class AllInterfaceLinksLocationSpecifier extends AllInterfacesLocationSpecifier {
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
