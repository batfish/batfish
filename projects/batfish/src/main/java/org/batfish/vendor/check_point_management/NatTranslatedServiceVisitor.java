package org.batfish.vendor.check_point_management;

/** Visitor for {@link NatTranslatedService} */
public interface NatTranslatedServiceVisitor<T> extends ServiceVisitor<T> {
  default T visit(NatTranslatedService natTranslatedService) {
    return natTranslatedService.accept(this);
  }

  T visitOriginal(Original original);
}
