package org.batfish.vendor.check_point_management;

/** Visitor for {@link Service} */
public interface ServiceVisitor<T> extends ConcreteServiceVisitor<T> {
  default T visit(Service service) {
    return service.accept(this);
  }

  T visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject);
}
