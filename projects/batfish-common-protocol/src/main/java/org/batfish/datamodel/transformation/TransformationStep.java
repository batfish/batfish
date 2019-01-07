package org.batfish.datamodel.transformation;

import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** A representation of an atomic transformation step. */
@ParametersAreNonnullByDefault
public interface TransformationStep {
  <T> T accept(TransformationStepVisitor<T> visitor);

  static AssignIpAddressFromPool assignDestinationIp(Ip poolStart, Ip poolEnd) {
    return new AssignIpAddressFromPool(IpField.DESTINATION, poolStart, poolEnd);
  }

  static AssignIpAddressFromPool assignSourceIp(Ip poolStart, Ip poolEnd) {
    return new AssignIpAddressFromPool(IpField.SOURCE, poolStart, poolEnd);
  }

  static ShiftIpAddressIntoSubnet shiftDestinationIp(Prefix subnet) {
    return new ShiftIpAddressIntoSubnet(IpField.DESTINATION, subnet);
  }

  static ShiftIpAddressIntoSubnet shiftSourceIp(Prefix subnet) {
    return new ShiftIpAddressIntoSubnet(IpField.SOURCE, subnet);
  }
}
