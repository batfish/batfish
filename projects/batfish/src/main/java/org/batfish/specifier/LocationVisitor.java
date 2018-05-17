package org.batfish.specifier;

public interface LocationVisitor<T> {
  T visitInterfaceLinkLocation(InterfaceLinkLocation interfaceLinkLocation);

  T visitInterfaceLocation(InterfaceLocation interfaceLocation);
}
