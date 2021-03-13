package org.batfish.representation.fortios;

import static org.batfish.representation.fortios.FortiosConfiguration.computeViPolicyName;

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

  /** Creates {@link TraceElement} for specified {@link Policy}. */
  static TraceElement matchPolicyTraceElement(Policy policy, String filename) {
    String viPolicyName = computeViPolicyName(policy);
    TraceElement.Builder te =
        TraceElement.builder()
            .add("Matched policy ")
            .add(
                viPolicyName,
                new VendorStructureId(
                    filename, FortiosStructureType.POLICY.getDescription(), viPolicyName));
    if (policy.getComments() != null) {
      te.add(String.format("(%s)", policy.getComments()));
    }
    return te.build();
  }
}
