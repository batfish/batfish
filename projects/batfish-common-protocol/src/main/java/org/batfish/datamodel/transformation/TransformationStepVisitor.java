package org.batfish.datamodel.transformation;

/** A visitor of {@link TransformationStep} objects */
public interface TransformationStepVisitor<T> {
  default T visit(TransformationStep step) {
    return step.accept(this);
  }

  T visitAssignIpAddressFromPool(AssignIpAddressFromPool assignIpAddressFromPool);

  T visitNoop(Noop noop);

  T visitShiftIpAddressIntoSubnet(ShiftIpAddressIntoSubnet shiftIpAddressIntoSubnet);

  T visitAssignPortFromPool(AssignPortFromPool assignPortFromPool);
}
