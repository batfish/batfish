package org.batfish.vendor.sonic.representation;

import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Vrf;

public class SonicConversions {

  /** Converts physical ports found under PORT object. */
  static void convertPorts(
      Configuration c, Map<String, Port> ports, Map<String, L3Interface> interfaces) {
    for (String portName : ports.keySet()) {
      Port port = ports.get(portName);
      Interface.Builder ib =
          Interface.builder()
              .setName(portName)
              .setOwner(c)
              .setVrf(c.getDefaultVrf()) // all physical ports are in default VRF at the moment
              .setType(InterfaceType.PHYSICAL)
              .setDescription(port.getDescription().orElse(null))
              .setMtu(port.getMtu().orElse(null))
              .setActive(port.getAdminStatusUp().orElse(true)); // default is active

      if (interfaces.containsKey(portName)) {
        L3Interface l3Interface = interfaces.get(portName);
        ib.setAddress(l3Interface.getAddress());
      }

      ib.build();
    }
  }

  /** Converts management ports. T */
  static void convertMgmtPorts(
      Configuration c,
      Map<String, Port> mgmtPorts,
      Map<String, L3Interface> mgmtInterfaces,
      Vrf mgmtVrf) {
    for (String portName : mgmtPorts.keySet()) {
      Port port = mgmtPorts.get(portName);
      Interface.Builder ib =
          Interface.builder()
              .setName(portName)
              .setOwner(c)
              .setVrf(mgmtVrf)
              .setType(InterfaceType.PHYSICAL)
              .setDescription(port.getDescription().orElse(null))
              .setMtu(port.getMtu().orElse(null))
              .setActive(port.getAdminStatusUp().orElse(true)); // default is active

      if (mgmtInterfaces.containsKey(portName)) {
        L3Interface l3Interface = mgmtInterfaces.get(portName);
        ib.setAddress(l3Interface.getAddress());
      }

      ib.build();
    }
  }
}
