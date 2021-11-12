package org.batfish.representation.palo_alto;

/** Visitor for {@link Reference} */
public interface ReferenceVisitor<T, U> {
  default T visit(Reference reference, U arg) {
    return reference.accept(this, arg);
  }

  T visitServiceOrServiceGroupReference(ServiceOrServiceGroupReference reference, U arg);

  T visitApplicationOrApplicationGroupReference(
      ApplicationOrApplicationGroupReference reference, U arg);
}
