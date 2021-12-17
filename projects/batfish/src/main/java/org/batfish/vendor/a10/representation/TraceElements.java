package org.batfish.vendor.a10.representation;

import org.batfish.datamodel.TraceElement;
import org.batfish.vendor.VendorStructureId;

public final class TraceElements {
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
        .add(String.format("Matched %s %s", port.getType().toString(), toPortString(port)))
        .build();
  }

  public static String toPortString(VirtualServerPort port) {
    if (port.getRange() != null) {
      return String.format("ports %d-%d", port.getNumber(), port.getNumber() + port.getRange());
    }
    return String.format("port %d", port.getNumber());
  }
}
