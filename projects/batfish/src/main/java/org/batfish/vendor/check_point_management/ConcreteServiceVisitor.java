package org.batfish.vendor.check_point_management;

/** Visitor for {@link ConcreteService} */
public interface ConcreteServiceVisitor<T> {
  default T visit(ConcreteService concreteService) {
    return concreteService.accept(this);
  }

  T visitServiceGroup(ServiceGroup serviceGroup);

  T visitServiceTcp(ServiceTcp serviceTcp);

  // TODO Add ServiceOther, ServiceUdp when created
}
