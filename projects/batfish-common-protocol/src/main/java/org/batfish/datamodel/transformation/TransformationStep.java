package org.batfish.datamodel.transformation;

import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.transformation.IpField.DESTINATION;
import static org.batfish.datamodel.transformation.IpField.SOURCE;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.flow.TransformationStep.TransformationType;

/**
 * A representation of an atomic transformation step. Each step has a {@link TransformationType},
 * which is included in traces. In order to build meaningful traces, the transformation must apply
 * at least one step whenever a flow would match a transformation rule in the source configs. Use
 * the {@link Noop} step for rules that match but do not transform the flow.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
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

  static TransformationStep shiftDestinationIp(Prefix subnet) {
    if (subnet.getPrefixLength() == Prefix.MAX_PREFIX_LENGTH) {
      return new AssignIpAddressFromPool(
          DEST_NAT, DESTINATION, subnet.getStartIp(), subnet.getStartIp());
    }

    return new ShiftIpAddressIntoSubnet(DEST_NAT, DESTINATION, subnet);
  }

  static TransformationStep shiftSourceIp(Prefix subnet) {
    if (subnet.getPrefixLength() == Prefix.MAX_PREFIX_LENGTH) {
      return new AssignIpAddressFromPool(
          SOURCE_NAT, SOURCE, subnet.getStartIp(), subnet.getStartIp());
    }
    return new ShiftIpAddressIntoSubnet(SOURCE_NAT, SOURCE, subnet);
  }
}
