package org.batfish.specifier;

public interface LocationVisitor<T> {
  default T visit(Location location) {
    return location.accept(this);
  }

  T visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation);

  T visitInterfaceLocation(InterfaceLocation interfaceLocation);
}
