package org.batfish.vendor.check_point_management;

/** Visitor for {@link Service} */
public interface ServiceVisitor<T> {
  T visitCpmiAnyObject(CpmiAnyObject cpmiAnyObject);

  T visitServiceGroup(ServiceGroup serviceGroup);

  T visitServiceTcp(ServiceTcp serviceTcp);

  T visitServiceUdp(ServiceUdp serviceUdp);

  // TODO Add ServiceOther when created
}
