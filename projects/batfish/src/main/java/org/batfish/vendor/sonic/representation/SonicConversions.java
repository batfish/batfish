package org.batfish.vendor.sonic.representation;

import static org.batfish.representation.frr.FrrConversions.SPEED_CONVERSION_FACTOR;

import java.util.Map;
import java.util.Optional;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;

public class SonicConversions {

  /** Converts physical ports found under PORT or the MGMT_PORT objects. */
  static void convertPorts(
      Configuration c, Map<String, Port> ports, Map<String, L3Interface> interfaces, Vrf vrf) {

    // TODO: Set bandwidth appropriately. Factor in runtime data

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
              .setSpeed(
                  port.getSpeed()
                      .map(speed -> speed * SPEED_CONVERSION_FACTOR)
                      .orElse(null)) // TODO: default speed
              .setActive(port.getAdminStatusUp().orElse(true)); // default is active

      if (interfaces.containsKey(portName)) {
        L3Interface l3Interface = interfaces.get(portName);
        ib.setAddress(l3Interface.getAddress());
      }

      ib.build();
    }
  }

  /**
   * Converts vlan information under the VLAN, VLAN_MEMBER, and VLAN_INTERFACE tables.
   *
   * <p>It will create a new VI interface for the VLAN and put it in {@code vrf}. If the VI version
   * of a member interface does not already exist in {@code c}, it is created as well. The member
   * interfaces are configured based on the tagging mode.
   */
  static void convertVlans(
      Configuration c,
      Map<String, Vlan> vlans,
      Map<String, VlanMember> vlanMembers,
      Map<String, L3Interface> vlanInterfaces,
      Vrf vrf,
      Warnings w) {
    for (String vlanName : vlans.keySet()) {
      Vlan vlan = vlans.get(vlanName);
      Interface.builder()
          .setName(vlanName)
          .setOwner(c)
          .setVrf(vrf)
          .setType(InterfaceType.VLAN)
          .setVlan(vlan.getVlanId())
          .setActive(true)
          .setAddress(
              Optional.ofNullable(vlanInterfaces.get(vlanName))
                  .flatMap(vlanIface -> Optional.ofNullable(vlanIface.getAddress()))
                  .orElse(null))
          .build();

      for (String memberName : vlan.getMembers()) {
        Interface memberInterface = c.getAllInterfaces().get(memberName);
        if (memberInterface == null) {
          memberInterface =
              Interface.builder()
                  .setName(memberName)
                  .setOwner(c)
                  .setVrf(vrf)
                  .setType(interfaceNameToType(memberName))
                  .build();
        }

        String memberKey =
            String.format(
                "%s|%s", vlanName, memberName); // how members are encoded in configdb data
        VlanMember vlanMember = vlanMembers.get(memberKey);
        if (vlanMember == null) {
          w.redFlag(String.format("Vlan member %s is not configured", memberKey));
          continue;
        }
        if (vlanMember.getTaggingMode() == null) {
          w.redFlag(String.format("tagging_mode is not configured for vlan member %s", memberKey));
          continue;
        }
        switch (vlanMember.getTaggingMode()) {
          case "tagged":
            memberInterface.setSwitchport(true);
            memberInterface.setSwitchportMode(SwitchportMode.TRUNK);
            memberInterface.setNativeVlan(vlan.getVlanId());
            memberInterface.setAllowedVlans(
                Optional.ofNullable(vlan.getVlanId())
                    .map(IntegerSpace::of)
                    .orElse(IntegerSpace.EMPTY));
            break;
          case "untagged":
            memberInterface.setSwitchport(true);
            memberInterface.setSwitchportMode(SwitchportMode.ACCESS);
            memberInterface.setAccessVlan(vlan.getVlanId());
            break;
          default:
            w.redFlag(
                String.format(
                    "Unknown tagging_mode %s for vlan member %s",
                    vlanMember.getTaggingMode(), memberKey));
        }
      }
    }
  }

  // Super simple name to type conversion
  // TODO: expand later as needed
  private static InterfaceType interfaceNameToType(String interfaceName) {
    if (interfaceName.toLowerCase().startsWith("eth")) {
      return InterfaceType.PHYSICAL;
    }
    if (interfaceName.toLowerCase().startsWith("portchannel")) {
      return InterfaceType.AGGREGATED;
    }
    throw new IllegalArgumentException("Cannot infer type for interface name " + interfaceName);
  }
}
