package org.batfish.representation.fortios;

/** Visitor for {@link InterfaceOrZone} */
public interface InterfaceOrZoneVisitor<T> {

  default T visit(InterfaceOrZone interfaceOrZone) {
    return interfaceOrZone.accept(this);
  }

  T visitInterface(Interface iface);

  T visitZone(Zone zone);
}
