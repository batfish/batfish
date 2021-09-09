package org.batfish.vendor.check_point_management;

/** Visitor for {@link Service} */
public interface ServiceVisitor<T> {
  default T visit(Service service) {
    return service.accept(this);
  }

  T visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject);

  T visitServiceGroup(ServiceGroup serviceGroup);

  T visitServiceIcmp(ServiceIcmp serviceIcmp);

  T visitServiceTcp(ServiceTcp serviceTcp);

  T visitServiceUdp(ServiceUdp serviceUdp);

  // TODO Add ServiceOther when created
}
