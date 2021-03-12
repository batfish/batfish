package org.batfish.representation.fortios;

import org.batfish.datamodel.TraceElement;
import org.batfish.vendor.VendorStructureId;

/** Collection of methods to create {@link TraceElement trace elements} for FortiOS ACL lines. */
public final class FortiosTraceElementCreators {

  private FortiosTraceElementCreators() {}

  /** Creates {@link TraceElement} for specified {@link Service}. */
  static TraceElement matchServiceTraceElement(Service service, String filename) {
    TraceElement.Builder te =
        TraceElement.builder()
            .add("Matched service ")
            .add(
                service.getName(),
                new VendorStructureId(
                    filename,
                    FortiosStructureType.SERVICE_CUSTOM.getDescription(),
                    service.getName()));
    if (service.getComment() != null) {
      te.add(String.format("(%s)", service.getComment()));
    }
    return te.build();
  }
}
