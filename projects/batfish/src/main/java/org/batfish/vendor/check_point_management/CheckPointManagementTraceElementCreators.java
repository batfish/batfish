package org.batfish.vendor.check_point_management;

import com.google.common.annotations.VisibleForTesting;
import org.batfish.datamodel.TraceElement;

public final class CheckPointManagementTraceElementCreators {

  @VisibleForTesting
  public static TraceElement addressCpmiAnyTraceElement(boolean source) {
    return TraceElement.of(
        String.format("Matched %s address CpmiAny", source ? "source" : "destination"));
  }

  @VisibleForTesting
  public static TraceElement addressRangeTraceElement(AddressRange addressRange, boolean source) {
    return TraceElement.of(
        String.format(
            "Matched %s address-range %s",
            source ? "source" : "destination", addressRange.getName()));
  }

  @VisibleForTesting
  public static TraceElement addressGatewayOrServerTraceElement(
      GatewayOrServer gatewayOrServer, boolean source) {
    return TraceElement.of(
        String.format(
            "Matched %s %s %s",
            source ? "source" : "destination",
            gatewayOrServer.getClass().getSimpleName(),
            gatewayOrServer.getName()));
  }

  @VisibleForTesting
  public static TraceElement addressGroupTraceElement(Group group, boolean source) {
    return TraceElement.of(
        String.format("Matched %s group %s", source ? "source" : "destination", group.getName()));
  }

  @VisibleForTesting
  public static TraceElement addressHostTraceElement(Host host, boolean source) {
    return TraceElement.of(
        String.format("Matched %s host %s", source ? "source" : "destination", host.getName()));
  }

  @VisibleForTesting
  public static TraceElement addressNetworkTraceElement(Network network, boolean source) {
    return TraceElement.of(
        String.format(
            "Matched %s network %s", source ? "source" : "destination", network.getName()));
  }

  @VisibleForTesting
  public static TraceElement serviceCpmiAnyTraceElement() {
    return TraceElement.of("Matched service CpmiAny");
  }

  @VisibleForTesting
  public static TraceElement serviceGroupTraceElement(ServiceGroup group) {
    return TraceElement.of(String.format("Matched service-group %s", group.getName()));
  }

  @VisibleForTesting
  public static TraceElement serviceIcmpTraceElement(ServiceIcmp service) {
    return TraceElement.of(String.format("Matched service-icmp %s", service.getName()));
  }

  @VisibleForTesting
  public static TraceElement serviceTcpTraceElement(ServiceTcp service) {
    return TraceElement.of(String.format("Matched service-tcp %s", service.getName()));
  }

  @VisibleForTesting
  public static TraceElement serviceUdpTraceElement(ServiceUdp service) {
    return TraceElement.of(String.format("Matched service-udp %s", service.getName()));
  }
}
