package org.batfish.vendor.check_point_management;

/** Visitor for {@link NatTranslatedService} */
public interface NatTranslatedServiceVisitor<T> extends ServiceVisitor<T> {
  T visitOriginal(Original original);

  T visitService(Service service);
}
