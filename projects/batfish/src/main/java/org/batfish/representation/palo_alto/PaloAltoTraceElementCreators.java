package org.batfish.representation.palo_alto;

import static org.batfish.representation.palo_alto.PaloAltoConfiguration.computeObjectName;

import com.google.common.annotations.VisibleForTesting;
import org.batfish.datamodel.TraceElement;
import org.batfish.vendor.VendorStructureId;

/** Collection of methods to create {@link TraceElement trace elements} for Palo Alto ACL lines. */
public final class PaloAltoTraceElementCreators {

  private PaloAltoTraceElementCreators() {}

  /**
   * Creates {@link TraceElement} for ACL line representing the given security rule in the given
   * vsys.
   */
  @VisibleForTesting
  public static TraceElement matchSecurityRuleTraceElement(
      String ruleName, String vsysName, String filename) {
    return TraceElement.builder()
        .add("Matched security rule ")
        .add(
            ruleName,
            new VendorStructureId(
                filename,
                PaloAltoStructureType.SECURITY_RULE.getDescription(),
                computeObjectName(vsysName, ruleName)))
        .build();
  }

  @VisibleForTesting
  public static TraceElement matchServiceAnyTraceElement() {
    return TraceElement.of("Matched service any");
  }

  @VisibleForTesting
  public static TraceElement matchSourceAddressTraceElement() {
    return TraceElement.of("Matched source address");
  }

  @VisibleForTesting
  public static TraceElement matchAddressAnyTraceElement() {
    return TraceElement.of("Matched address any");
  }

  @VisibleForTesting
  public static TraceElement matchAddressValueTraceElement(String value) {
    return TraceElement.of(String.format("Matched address value %s", value));
  }

  @VisibleForTesting
  public static TraceElement matchAddressObjectTraceElement(
      String name, String vsysName, String filename) {
    return TraceElement.builder()
        .add("Matched address object ")
        .add(
            name,
            new VendorStructureId(
                filename,
                PaloAltoStructureType.ADDRESS_OBJECT.getDescription(),
                computeObjectName(vsysName, name)))
        .build();
  }

  @VisibleForTesting
  public static TraceElement matchAddressGroupTraceElement(
      String name, String vsysName, String filename) {
    return TraceElement.builder()
        .add("Matched address-group ")
        .add(
            name,
            new VendorStructureId(
                filename,
                PaloAltoStructureType.ADDRESS_GROUP.getDescription(),
                computeObjectName(vsysName, name)))
        .build();
  }

  @VisibleForTesting
  public static TraceElement matchApplicationGroupTraceElement(
      String name, String vsysName, String filename) {
    return TraceElement.builder()
        .add("Matched application-group ")
        .add(
            name,
            new VendorStructureId(
                filename,
                PaloAltoStructureType.APPLICATION_GROUP.getDescription(),
                computeObjectName(vsysName, name)))
        .build();
  }

  @VisibleForTesting
  public static TraceElement matchApplicationObjectTraceElement(
      String name, String vsysName, String filename) {
    return TraceElement.builder()
        .add("Matched application object ")
        .add(
            name,
            new VendorStructureId(
                filename,
                PaloAltoStructureType.APPLICATION.getDescription(),
                computeObjectName(vsysName, name)))
        .build();
  }

  @VisibleForTesting
  public static TraceElement matchApplicationOverrideRuleTraceElement(
      String name, String vsysName, String filename) {
    return TraceElement.builder()
        .add("Matched application-override rule ")
        .add(
            name,
            new VendorStructureId(
                filename,
                PaloAltoStructureType.APPLICATION_OVERRIDE_RULE.getDescription(),
                computeObjectName(vsysName, name)))
        .build();
  }

  @VisibleForTesting
  public static TraceElement matchBuiltInApplicationTraceElement(String name) {
    return TraceElement.of(String.format("Matched built-in application %s", name));
  }

  @VisibleForTesting
  public static TraceElement matchApplicationAnyTraceElement() {
    return TraceElement.of("Matched application any");
  }

  @VisibleForTesting
  public static TraceElement matchNegatedAddressTraceElement() {
    return TraceElement.of("Matched negated address");
  }

  @VisibleForTesting
  public static TraceElement matchDestinationAddressTraceElement() {
    return TraceElement.of("Matched destination address");
  }

  @VisibleForTesting
  public static TraceElement matchServiceTraceElement() {
    return TraceElement.of("Matched a service");
  }

  @VisibleForTesting
  public static TraceElement matchBuiltInServiceTraceElement() {
    return TraceElement.of("Matched a built-in service");
  }

  @VisibleForTesting
  public static TraceElement matchServiceApplicationDefaultTraceElement() {
    return TraceElement.of("Matched service application-default");
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
   * Creates {@link TraceElement} for ACL line that rejects flows after they were not permitted by
   * an intra- or cross-zone filter
   */
  @VisibleForTesting
  public static TraceElement zoneToZoneRejectTraceElement(
      String fromZone, String toZone, String vsys) {
    String desc =
        fromZone.equals(toZone)
            // Intrazone flows are default accepted, so this trace indicates an explicit deny
            ? String.format("Denied by intrazone rules for vsys %s zone %s", vsys, fromZone)
            // Intrazone flows are default denied; this trace covers explicit and default denies
            : String.format(
                "Not permitted by cross-zone rules from zone %s to zone %s in vsys %s",
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
            "Matched rules for exiting interface %s in vsys %s zone %s", iface, vsys, zone));
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
