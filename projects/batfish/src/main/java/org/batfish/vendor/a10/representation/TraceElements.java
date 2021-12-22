package org.batfish.vendor.a10.representation;

import static org.batfish.vendor.a10.representation.A10Conversion.getEndPort;

import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.vendor.VendorStructureId;

/** Collection of methods to create {@link TraceElement trace elements} for A10 structures. */
public final class TraceElements {

  /**
   * Returns a {@link TraceElement} indicating a particular action was taken by a line in the
   * specified access-list.
   */
  public static TraceElement traceElementForAccessList(
      String aclName, String filename, boolean permitted) {
    return TraceElement.builder()
        .add(String.format("%s by access-list", permitted ? "Permitted" : "Denied"))
        .add(
            aclName,
            new VendorStructureId(filename, A10StructureType.ACCESS_LIST.getDescription(), aclName))
        .build();
  }

  public static TraceElement traceElementForSourceAddressAny() {
    return TraceElement.builder().add("Matched source address any").build();
  }

  public static TraceElement traceElementForDestAddressAny() {
    return TraceElement.builder().add("Matched destination address any").build();
  }

  public static TraceElement traceElementForSourceHost(AccessListAddressHost host) {
    return TraceElement.builder()
        .add(String.format("Matched source host %s", host.getHost()))
        .build();
  }

  public static TraceElement traceElementForDestHost(AccessListAddressHost host) {
    return TraceElement.builder()
        .add(String.format("Matched destination host %s", host.getHost()))
        .build();
  }

  public static TraceElement traceElementForVirtualServer(VirtualServer server, String filename) {
    String serverName = server.getName();
    return TraceElement.builder()
        .add("Matched virtual-server")
        .add(
            serverName,
            new VendorStructureId(
                filename, A10StructureType.VIRTUAL_SERVER.getDescription(), serverName))
        .build();
  }

  public static TraceElement traceElementForVirtualServerPort(VirtualServerPort port) {
    return TraceElement.builder()
        .add(
            String.format(
                "Matched %s %s",
                port.getType().toString(), toPortString(port.getNumber(), getEndPort(port))))
        .build();
  }

  public static TraceElement traceElementForProtocolPortRange(IpProtocol protocol, SubRange range) {
    return TraceElement.builder()
        .add(
            String.format(
                "Matched %s %s", protocol.name(), toPortString(range.getStart(), range.getEnd())))
        .build();
  }

  public static TraceElement traceElementForProtocol(IpProtocol protocol) {
    return TraceElement.builder()
        .add(String.format("Matched protocol %s", protocol.name()))
        .build();
  }

  public static String toPortString(int start, int end) {
    return start == end
        ? String.format("port %d", start)
        : String.format("ports %d-%d", start, end);
  }
}
