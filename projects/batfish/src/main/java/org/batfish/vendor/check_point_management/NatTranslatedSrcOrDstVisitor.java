package org.batfish.vendor.check_point_management;

/** Visitor for {@link NatTranslatedSrcOrDst} */
public interface NatTranslatedSrcOrDstVisitor<T> extends ConcreteSrcOrDstVisitor<T> {
  default T visit(NatTranslatedSrcOrDst natTranslatedSrcOrDst) {
    return natTranslatedSrcOrDst.accept(this);
  }

  T visitOriginal(Original original);

  T visitPolicyTargets(PolicyTargets policyTargets);

  T visitUnhandledGlobal(UnhandledGlobal unhandledGlobal);
}
