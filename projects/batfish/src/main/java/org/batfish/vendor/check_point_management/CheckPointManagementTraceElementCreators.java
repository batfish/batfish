package org.batfish.vendor.check_point_management;

import com.google.common.annotations.VisibleForTesting;
import org.batfish.datamodel.TraceElement;

public final class CheckPointManagementTraceElementCreators {

  @VisibleForTesting
  public static TraceElement addressCpmiAnyTraceElement(boolean source) {
    return TraceElement.of(
        String.format(
            "Matched %s address network object 'Any'", source ? "source" : "destination"));
  }

  @VisibleForTesting
  public static TraceElement addressGroupTraceElement(Group group, boolean source) {
    return TraceElement.of(
        String.format(
            "Matched %s address group '%s'", source ? "source" : "destination", group.getName()));
  }

  @VisibleForTesting
  public static TraceElement serviceCpmiAnyTraceElement() {
    return TraceElement.of("Matched service object 'Any'");
  }

  @VisibleForTesting
  public static TraceElement serviceGroupTraceElement(ServiceGroup group) {
    return TraceElement.of(String.format("Matched service-group '%s'", group.getName()));
  }

  @VisibleForTesting
  public static TraceElement serviceIcmpTraceElement(ServiceIcmp service) {
    return TraceElement.of(String.format("Matched service-icmp '%s'", service.getName()));
  }

  @VisibleForTesting
  public static TraceElement serviceOtherTraceElement(ServiceOther service) {
    return TraceElement.of(String.format("Matched service-other %s", service.getName()));
  }

  @VisibleForTesting
  public static TraceElement serviceTcpTraceElement(ServiceTcp service) {
    return TraceElement.of(String.format("Matched service-tcp '%s'", service.getName()));
  }

  @VisibleForTesting
  public static TraceElement serviceUdpTraceElement(ServiceUdp service) {
    return TraceElement.of(String.format("Matched service-udp '%s'", service.getName()));
  }
}
