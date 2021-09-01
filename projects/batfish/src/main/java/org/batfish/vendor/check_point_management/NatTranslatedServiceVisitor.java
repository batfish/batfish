package org.batfish.vendor.check_point_management;

/** Visitor for {@link NatTranslatedService} */
public interface NatTranslatedServiceVisitor<T> extends ConcreteServiceVisitor<T> {
  default T visit(NatTranslatedService natTranslatedService) {
    return natTranslatedService.accept(this);
  }

  T visitOriginal(Original original);

  T visitPolicyTargets(PolicyTargets policyTargets);

  T visitUnhandledGlobal(UnhandledGlobal unhandledGlobal);
}
