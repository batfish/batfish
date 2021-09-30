package org.batfish.vendor.check_point_management;

/** Visitor for {@link Service} that takes a generic argument and returns a generic value. */
public interface ServiceVisitor<T, U> {
  default T visit(Service service, U arg) {
    return service.accept(this, arg);
  }

  T visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject, U arg);

  T visitServiceGroup(ServiceGroup serviceGroup, U arg);

  T visitServiceIcmp(ServiceIcmp serviceIcmp, U arg);

  T visitServiceOther(ServiceOther serviceOther, U arg);

  T visitServiceTcp(ServiceTcp serviceTcp, U arg);

  T visitServiceUdp(ServiceUdp serviceUdp, U arg);
}
