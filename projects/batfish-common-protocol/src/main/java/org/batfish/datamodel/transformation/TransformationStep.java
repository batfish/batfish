package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/** A representation of an atomic transformation step. */
@ParametersAreNonnullByDefault
public interface TransformationStep {
  /** Which {@link TransformationType} this step is (partially) encoding. */
  TransformationType getType();

  <T> T accept(TransformationStepVisitor<T> visitor);

  static AssignIpAddressFromPool assignDestinationIp(Ip poolStart, Ip poolEnd) {
    return new AssignIpAddressFromPool(DEST_NAT, DESTINATION, poolStart, poolEnd);
  }

  static AssignIpAddressFromPool assignSourceIp(Ip poolStart, Ip poolEnd) {
    return new AssignIpAddressFromPool(SOURCE_NAT, SOURCE, poolStart, poolEnd);
  }

  static ShiftIpAddressIntoSubnet shiftDestinationIp(Prefix subnet) {
    return new ShiftIpAddressIntoSubnet(DEST_NAT, DESTINATION, subnet);
  }

  static ShiftIpAddressIntoSubnet shiftSourceIp(Prefix subnet) {
    return new ShiftIpAddressIntoSubnet(SOURCE_NAT, SOURCE, subnet);
  }
}
