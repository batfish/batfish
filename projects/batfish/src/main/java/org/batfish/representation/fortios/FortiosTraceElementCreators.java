package org.batfish.representation.fortios;

import static org.batfish.representation.fortios.InterfaceOrZoneUtils.getDefaultIntrazoneAction;
import static org.batfish.representation.fortios.InterfaceOrZoneUtils.getStructureType;

import com.google.common.annotations.VisibleForTesting;
import org.batfish.datamodel.TraceElement;
import org.batfish.vendor.VendorStructureId;

/** Collection of methods to create {@link TraceElement trace elements} for FortiOS ACL lines. */
public final class FortiosTraceElementCreators {

  private FortiosTraceElementCreators() {}

  /** Creates {@link TraceElement} for specified {@link ServiceGroupMember}. */
  static TraceElement matchServiceTraceElement(Service service, String filename) {
    TraceElement.Builder te = TraceElement.builder();

    te.add("Matched service ")
        .add(
            service.getName(),
            new VendorStructureId(
                filename, FortiosStructureType.SERVICE_CUSTOM.getDescription(), service.getName()));

    if (service.getComment() != null) {
      te.add(String.format("(%s)", service.getComment()));
    }
    return te.build();
  }

  /** Creates {@link TraceElement} for specified {@link ServiceGroupMember}. */
  static TraceElement matchServiceGroupTraceElement(ServiceGroup serviceGroup, String filename) {
    TraceElement.Builder te =
        TraceElement.builder()
            .add("Matched service group ")
            .add(
                serviceGroup.getName(),
                new VendorStructureId(
                    filename,
                    FortiosStructureType.SERVICE_GROUP.getDescription(),
                    serviceGroup.getName()));

    if (serviceGroup.getComment() != null) {
      te.add(String.format("(%s)", serviceGroup.getComment()));
    }
    return te.build();
  }

  /** Creates {@link TraceElement} for specified addrgrp member used as a source address. */
  static TraceElement matchSourceAddressTraceElement(AddrgrpMember addr, String filename) {
    if (addr instanceof Address) {
      return matchAddressTraceElement(addr.getName(), filename, true);
    }
    assert addr instanceof Addrgrp;
    return matchAddrgrpTraceElement(addr.getName(), filename, true);
  }

  /** Creates {@link TraceElement} for specified addrgrp member used as a dest address. */
  static TraceElement matchDestinationAddressTraceElement(AddrgrpMember addr, String filename) {
    if (addr instanceof Address) {
      return matchAddressTraceElement(addr.getName(), filename, false);
    }
    assert addr instanceof Addrgrp;
    return matchAddrgrpTraceElement(addr.getName(), filename, false);
  }

  /** Creates {@link TraceElement} for specified address used as a source or dest address. */
  static TraceElement matchAddressTraceElement(
      String addressName, String filename, boolean sourceAddr) {
    TraceElement.Builder te =
        TraceElement.builder()
            .add(String.format("Matched %s address ", sourceAddr ? "source" : "destination"))
            .add(
                addressName,
                new VendorStructureId(
                    filename, FortiosStructureType.ADDRESS.getDescription(), addressName));
    // TODO handle comment
    return te.build();
  }

  /** Creates {@link TraceElement} for specified addrgrp used as a source or dest address. */
  static TraceElement matchAddrgrpTraceElement(
      String addressName, String filename, boolean sourceAddr) {
    TraceElement.Builder te =
        TraceElement.builder()
            .add(String.format("Matched %s addrgrp ", sourceAddr ? "source" : "destination"))
            .add(
                addressName,
                new VendorStructureId(
                    filename, FortiosStructureType.ADDRGRP.getDescription(), addressName));
    // TODO handle comment
    return te.build();
  }

  /** Creates {@link TraceElement} for specified {@link Policy}. */
  static TraceElement matchPolicyTraceElement(Policy policy, String filename) {
    TraceElement.Builder te = TraceElement.builder().add("Matched policy ");
    if (policy.getName() != null) {
      te.add(String.format("named %s: ", policy.getName()));
    }
    te.add(
        policy.getNumber(),
        new VendorStructureId(
            filename, FortiosStructureType.POLICY.getDescription(), policy.getNumber()));
    if (policy.getComments() != null) {
      te.add(String.format("(%s)", policy.getComments()));
    }
    return te.build();
  }

  /**
   * Creates {@link TraceElement} for ACL line that applies the default action in an intra- or
   * cross-zone filter
   */
  @VisibleForTesting
  public static TraceElement zoneToZoneDefaultTraceElement(
      InterfaceOrZone from, InterfaceOrZone to, String filename) {
    if (from == to) {
      return intrazoneDefaultTraceElement(from, filename);
    }
    return TraceElement.builder()
        .add("Default denied cross-zone traffic from ")
        .add(from.getName(), getVendorStructureId(from, filename))
        .add(" to ")
        .add(to.getName(), getVendorStructureId(to, filename))
        .build();
  }

  /** Creates {@link TraceElement} for ACL line applying the default action to intrazone flows. */
  private static TraceElement intrazoneDefaultTraceElement(
      InterfaceOrZone interfaceOrZone, String filename) {
    String action =
        getDefaultIntrazoneAction(interfaceOrZone) == Zone.IntrazoneAction.ALLOW
            ? "allowed"
            : "denied";
    return TraceElement.builder()
        .add(String.format("Default %s intrazone traffic entering and exiting ", action))
        .add(interfaceOrZone.getName(), getVendorStructureId(interfaceOrZone, filename))
        .build();
  }

  private static VendorStructureId getVendorStructureId(
      InterfaceOrZone interfaceOrZone, String filename) {
    return new VendorStructureId(
        filename, getStructureType(interfaceOrZone).getDescription(), interfaceOrZone.getName());
  }
}
