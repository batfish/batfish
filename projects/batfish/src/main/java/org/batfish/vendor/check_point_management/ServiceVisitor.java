package org.batfish.vendor.check_point_management;

/** Visitor for {@link Service} */
public interface ServiceVisitor<T> {
  default T visit(Service service) {
    return service.accept(this);
  }

  T visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject);

  T visitServiceGroup(ServiceGroup serviceGroup);

  T visitServiceTcp(ServiceTcp serviceTcp);

  // TODO Add ServiceOther, ServiceUdp when created
}
