package org.batfish.vendor.sonic.representation;

import static org.batfish.representation.frr.FrrConversions.SPEED_CONVERSION_FACTOR;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
              .setHumanName(port.getAlias().orElse(null))
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
    warnMissingVlans(vlans.keySet(), vlanInterfaces.keySet(), w);
    for (String vlanName : vlans.keySet()) {
      Vlan vlan = vlans.get(vlanName);
      if (!checkVlanId(vlanName, vlan.getVlanId().orElse(null), w)) {
        continue;
      }
      int vlanId = vlan.getVlanId().get(); // must exist since checkVlanId passed
      if (vlanInterfaces.containsKey(vlanName)) {
        Interface.builder()
            .setName(vlanName)
            .setOwner(c)
            .setVrf(vrf)
            .setType(InterfaceType.VLAN)
            .setVlan(vlanId)
            .setActive(true)
            .setAddress(vlanInterfaces.get(vlanName).getAddress())
            .build();
      }

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
        if (!vlanMember.getTaggingMode().isPresent()) {
          w.redFlag(String.format("tagging_mode is not configured for vlan member %s", memberKey));
          continue;
        }
        switch (vlanMember.getTaggingMode().get()) {
          case TAGGED:
            memberInterface.setSwitchport(true);
            memberInterface.setSwitchportMode(SwitchportMode.TRUNK);
            memberInterface.setNativeVlan(vlanId);
            memberInterface.setAllowedVlans(IntegerSpace.of(vlanId));
            break;
          case UNTAGGED:
            memberInterface.setSwitchport(true);
            memberInterface.setSwitchportMode(SwitchportMode.ACCESS);
            memberInterface.setAccessVlan(vlanId);
            break;
          default:
            w.redFlag(
                String.format(
                    "Unhandled tagging_mode %s for vlan member %s",
                    vlanMember.getTaggingMode().get(), memberKey));
        }
      }
    }
  }

  private static void warnMissingVlans(Set<String> vlans, Set<String> vlanInterfaces, Warnings w) {
    Set<String> missingVlans = Sets.difference(vlanInterfaces, vlans);
    if (!missingVlans.isEmpty()) {
      w.redFlag(
          String.format(
              "Ignoring VLAN_INTERFACEs %s because they don't have VLANs defined.", missingVlans));
    }
  }

  @VisibleForTesting
  static boolean checkVlanId(String vlanName, @Nullable Integer vlanId, Warnings w) {
    if (vlanId == null) {
      w.redFlag(String.format("%s ignored: vlanid is not configured.", vlanName));
      return false;
    }
    if (vlanId < 1 || vlanId > 4094) {
      w.redFlag(
          String.format(
              "%s ignored: It has invalid vlan id %d. Vlan ids should be between 1 and 4094.",
              vlanName, vlanId));
      return false;
    }
    try {
      int vlanIdFromName = Integer.parseInt(vlanName.replaceFirst("^Vlan", ""));
      if (vlanIdFromName != vlanId) {
        w.redFlag(
            String.format(
                "%s ignored: Vlan id in the name does not match configured vlan id %d",
                vlanName, vlanId));
        return false;
      }
    } catch (NumberFormatException e) {
      w.redFlag(
          String.format(
              "%s ignored: Unexpected name format. Vlan names should be like 'Vlan1', with 'Vlan'"
                  + " followed by its numerical id.",
              vlanName));
      return false;
    }
    return true;
  }

  // Super simple name to type conversion
  // TODO: expand later as needed
  private @Nonnull static InterfaceType interfaceNameToType(String interfaceName) {
    if (interfaceName.toLowerCase().startsWith("eth")) {
      return InterfaceType.PHYSICAL;
    }
    if (interfaceName.toLowerCase().startsWith("portchannel")) {
      return InterfaceType.AGGREGATED;
    }
    throw new IllegalArgumentException("Cannot infer type for interface name " + interfaceName);
  }
}
