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

  <T> T accept(TransformationStepVisitor<T> visitor);

  static AssignIpAddressFromPool assignDestinationIp(Ip ip) {
    return assignDestinationIp(ip, ip);
  }

  static AssignIpAddressFromPool assignDestinationIp(Ip poolStart, Ip poolEnd) {
    return new AssignIpAddressFromPool(DEST_NAT, DESTINATION, poolStart, poolEnd);
  }

  static AssignIpAddressFromPool assignSourceIp(Ip ip) {
    return assignSourceIp(ip, ip);
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

  static AssignPortFromPool assignSourcePort(int port) {
    return assignSourcePort(port, port);
  }

  static AssignPortFromPool assignSourcePort(int poolStart, int poolEnd) {
    return new AssignPortFromPool(SOURCE_NAT, PortField.SOURCE, poolStart, poolEnd);
  }

  static AssignPortFromPool assignDestinationPort(int port) {
    return assignDestinationPort(port, port);
  }

  static AssignPortFromPool assignDestinationPort(int poolStart, int poolEnd) {
    return new AssignPortFromPool(DEST_NAT, PortField.DESTINATION, poolStart, poolEnd);
  }
}
