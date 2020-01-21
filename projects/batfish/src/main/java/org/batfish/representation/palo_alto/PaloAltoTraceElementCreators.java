package org.batfish.representation.palo_alto;

import com.google.common.annotations.VisibleForTesting;
import org.batfish.datamodel.TraceElement;

/** Collection of methods to create {@link TraceElement trace elements} for Palo Alto ACL lines. */
public final class PaloAltoTraceElementCreators {

  private PaloAltoTraceElementCreators() {}

  /** Creates {@link TraceElement} for ACL line representing security rule {@code ruleName} */
  @VisibleForTesting
  public static TraceElement matchRuleTraceElement(String ruleName) {
    return TraceElement.of("Matched security rule " + ruleName);
  }

  /**
   * Creates {@link TraceElement} for ACL line representing security rules for going from {@code
   * fromZone} to {@code toZone} (intrazone rules if zones are the same)
   */
  @VisibleForTesting
  public static TraceElement zoneToZoneMatchTraceElement(
      String fromZone, String toZone, String vsys) {
    String desc =
        fromZone.equals(toZone)
            ? String.format("Matched intrazone rules for vsys %s zone %s", vsys, fromZone)
            : String.format(
                "Matched cross-zone rules from zone %s to zone %s in vsys %s",
                fromZone, toZone, vsys);
    return TraceElement.of(desc);
  }

  /**
   * Creates {@link TraceElement} for ACL line that rejects flows after they failed to match intra-
   * or cross-zone security rules
   */
  @VisibleForTesting
  public static TraceElement zoneToZoneRejectTraceElement(
      String fromZone, String toZone, String vsys) {
    String desc =
        fromZone.equals(toZone)
            ? String.format("Did not match intrazone rules for vsys %s zone %s", vsys, fromZone)
            : String.format(
                "Did not match cross-zone rules from zone %s to zone %s in vsys %s",
                fromZone, toZone, vsys);
    return TraceElement.of(desc);
  }

  /**
   * Creates {@link TraceElement} for ACL line that default accepts intrazone traffic when it falls
   * through security rules
   */
  @VisibleForTesting
  public static TraceElement intrazoneDefaultAcceptTraceElement(String vsys, String zone) {
    return TraceElement.of(
        String.format("Accepted intrazone traffic in vsys %s zone %s", vsys, zone));
  }

  /** Creates {@link TraceElement} for ACL line representing rules for exiting {@code iface} */
  @VisibleForTesting
  public static TraceElement ifaceOutgoingTraceElement(String iface, String zone, String vsys) {
    return TraceElement.of(
        String.format(
            "Match rules for exiting interface %s in vsys %s zone %s", iface, vsys, zone));
  }

  /**
   * Creates {@link TraceElement} for ACL line representing default reject for flows attempting to
   * exit an unzoned interface
   */
  @VisibleForTesting
  public static TraceElement unzonedIfaceRejectTraceElement(String iface) {
    return TraceElement.of(
        String.format("Cannot exit interface %s because it is not in a zone", iface));
  }

  /** Creates {@link TraceElement} indicating that the flow originated from device */
  @VisibleForTesting
  public static TraceElement originatedFromDeviceTraceElement() {
    return TraceElement.of("Originated from the device");
  }

  /**
   * Creates {@link TraceElement} for ACL line representing default reject for flows attempting to
   * enter or exit a zone with no interfaces
   */
  @VisibleForTesting
  public static TraceElement emptyZoneRejectTraceElement(String vsys, String emptyZone) {
    return TraceElement.of(String.format("No interfaces in vsys %s zone %s", vsys, emptyZone));
  }
}
