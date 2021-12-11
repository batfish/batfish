package org.batfish.vendor.sonic.representation;

import java.util.Map;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Vrf;

public class SonicConversions {

  /** Converts physical ports found under PORT or the MGMT_PORT objects. */
  static void convertPorts(
      Configuration c, Map<String, Port> ports, Map<String, L3Interface> interfaces, Vrf vrf) {
    for (String portName : ports.keySet()) {
      Port port = ports.get(portName);
      Interface.Builder ib =
          Interface.builder()
              .setName(portName)
              .setOwner(c)
              .setVrf(vrf)
              .setType(InterfaceType.PHYSICAL)
              .setDescription(port.getDescription().orElse(null))
              .setMtu(port.getMtu().orElse(null))
              .setSpeed(port.getSpeed().map(Double::new).orElse(null))
              .setActive(port.getAdminStatusUp().orElse(true)); // default is active

      if (interfaces.containsKey(portName)) {
        L3Interface l3Interface = interfaces.get(portName);
        ib.setAddress(l3Interface.getAddress());
      }

      ib.build();
    }
  }
}
