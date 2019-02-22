package org.batfish.datamodel.transformation;

/** A visitor of {@link TransformationStep} objects */
public interface TransformationStepVisitor<T> {
  default T visit(TransformationStep step) {
    return step.accept(this);
  }

  T visitApplyAll(ApplyAll applyAll);

  T visitApplyOne(ApplyOne applyOne);

  T visitAssignIpAddressFromPool(AssignIpAddressFromPool assignIpAddressFromPool);

  T visitAssignPortFromPool(AssignPortFromPool assignPortFromPool);

  T visitNoop(Noop noop);

  T visitShiftIpAddressIntoSubnet(ShiftIpAddressIntoSubnet shiftIpAddressIntoSubnet);
}
