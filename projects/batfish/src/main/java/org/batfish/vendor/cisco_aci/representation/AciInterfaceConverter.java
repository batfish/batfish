package org.batfish.vendor.cisco_aci.representation;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Vrf;

/** Converts ACI interface models to VI interfaces. */
final class AciInterfaceConverter {

  private AciInterfaceConverter() {}

  static Map<String, Interface> convertInterfaces(
      FabricNode node, AciConfiguration aciConfig, Vrf vrf, Configuration c, Warnings warnings) {

    Map<String, Interface> interfaces = new TreeMap<>();

    // Get interfaces from the fabric node (if populated)
    if (node.getInterfaces() != null) {
      for (FabricNodeInterface fvIface : node.getInterfaces().values()) {
        String ifaceName = fvIface.getName();
        if (ifaceName == null) {
          continue;
        }

        Interface.Builder ifaceBuilder =
            Interface.builder()
                .setName(ifaceName)
                .setType(toInterfaceType(fvIface.getType()))
                .setOwner(c)
                .setVrf(vrf)
                .setAdminUp(fvIface.isEnabled())
                .setMtu(AciConstants.DEFAULT_MTU)
                .setHumanName(ifaceName)
                .setDeclaredNames(ImmutableList.of(ifaceName));

        // Build description from existing description and fabric interface status
        StringBuilder description = new StringBuilder();
        if (fvIface.getDescription() != null) {
          description.append(fvIface.getDescription());
        }

        // Mark fabric interfaces
        if (isFabricInterface(ifaceName, node.getRole())) {
          if (description.length() > 0) {
            description.append(" | ");
          }
          description.append("Fabric interface (IS-IS/Overlay)");
        }

        if (description.length() > 0) {
          ifaceBuilder.setDescription(description.toString());
        }

        Interface iface = ifaceBuilder.build();
        interfaces.put(ifaceName, iface);
      }
    }

    // Also add interfaces discovered from path attachments (fvRsPathAtt)
    String nodeId = node.getNodeId();
    if (nodeId != null && aciConfig.getNodeInterfaces() != null) {
      Set<String> pathAttachmentInterfaces = aciConfig.getNodeInterfaces().get(nodeId);
      if (pathAttachmentInterfaces != null) {
        for (String ifaceName : pathAttachmentInterfaces) {
          // Only add if not already present
          if (!interfaces.containsKey(ifaceName)) {
            Interface.Builder ifaceBuilder =
                Interface.builder()
                    .setName(ifaceName)
                    .setType(InterfaceType.PHYSICAL)
                    .setOwner(c)
                    .setVrf(vrf)
                    .setAdminUp(true)
                    .setMtu(AciConstants.DEFAULT_MTU)
                    .setHumanName(ifaceName)
                    .setDeclaredNames(ImmutableList.of(ifaceName));

            // Add description from path attachment if available
            boolean hasDescription = false;
            StringBuilder description = new StringBuilder();

            if (aciConfig.getPathAttachmentMap() != null) {
              Map<String, PathAttachment> nodeAttachments =
                  aciConfig.getPathAttachmentMap().get(nodeId);
              if (nodeAttachments != null) {
                PathAttachment attachment = nodeAttachments.get(ifaceName);
                if (attachment != null) {
                  if (attachment.getDescription() != null) {
                    description.append(attachment.getDescription());
                    hasDescription = true;
                  }
                  // Add EPG information
                  if (attachment.getEpgName() != null) {
                    if (hasDescription) {
                      description.append(" | ");
                    }
                    description
                        .append("EPG: ")
                        .append(attachment.getEpgTenant())
                        .append(":")
                        .append(attachment.getEpgName());
                    hasDescription = true;
                  }
                  // Add VLAN information
                  if (attachment.getEncap() != null) {
                    if (hasDescription) {
                      description.append(" | ");
                    }
                    description.append("VLAN: ").append(attachment.getEncap());
                    hasDescription = true;
                  }
                }
              }
            }

            // Mark fabric interfaces
            if (isFabricInterface(ifaceName, node.getRole())) {
              if (hasDescription) {
                description.append(" | ");
              }
              description.append("Fabric interface (IS-IS/Overlay)");
            }

            if (hasDescription) {
              ifaceBuilder.setDescription(description.toString());
            }

            Interface iface = ifaceBuilder.build();
            interfaces.put(ifaceName, iface);
          }
        }
      }
    }

    // Add loopback interface if not present (this is the VTEP interface in ACI)
    String loopbackName = "loopback0";
    if (!interfaces.containsKey(loopbackName)) {
      Interface.Builder loopbackBuilder =
          Interface.builder()
              .setName(loopbackName)
              .setType(InterfaceType.LOOPBACK)
              .setOwner(c)
              .setVrf(vrf)
              .setAdminUp(true)
              .setHumanName("VTEP Loopback")
              .setDeclaredNames(ImmutableList.of(loopbackName));

      // Note: VTEP IP is dynamically assigned via DHCP during fabric discovery
      // and is not stored in the configuration export
      String role = node.getRole();
      if ("leaf".equalsIgnoreCase(role) || "spine".equalsIgnoreCase(role)) {
        loopbackBuilder.setDescription(
            "VTEP (VXLAN Tunnel Endpoint) - dynamically assigned IP from TEP pool");
      }

      Interface loopback = loopbackBuilder.build();
      interfaces.put(loopbackName, loopback);
    }

    // Add management interface if out-of-band management is configured
    AciManagementInfo mgmtInfo = node.getManagementInfo();
    if (mgmtInfo != null && mgmtInfo.getAddress() != null) {
      String mgmtIfaceName = "mgmt0";
      Interface.Builder mgmtBuilder =
          Interface.builder()
              .setName(mgmtIfaceName)
              .setType(InterfaceType.PHYSICAL)
              .setOwner(c)
              .setVrf(vrf)
              .setAdminUp(true)
              .setHumanName("Management Interface")
              .setDeclaredNames(ImmutableList.of(mgmtIfaceName));

      // Parse the management IP address
      // Format: "10.35.1.52/24"
      String addr = mgmtInfo.getAddress();
      try {
        ConcreteInterfaceAddress address = ConcreteInterfaceAddress.parse(addr);
        mgmtBuilder.setAddress(address);

        // Set description with management info
        StringBuilder descr = new StringBuilder("Out-of-band management interface");
        if (mgmtInfo.getGateway() != null) {
          descr.append(" | Gateway: ").append(mgmtInfo.getGateway());
        }
        mgmtBuilder.setDescription(descr.toString());
      } catch (Exception e) {
        warnings.redFlag("Failed to parse management address '" + addr + "': " + e.getMessage());
      }

      Interface mgmtIface = mgmtBuilder.build();
      interfaces.put(mgmtIfaceName, mgmtIface);
    }

    // If no interfaces were defined (only loopback), create fallback fabric interfaces
    // This ensures isolated nodes get connected in the spine-leaf topology
    if (interfaces.size() == 1) { // Only loopback
      warnings.redFlag(
          "No interfaces defined for fabric node "
              + node.getName()
              + ". Adding fallback fabric interfaces based on role.");
      createFallbackFabricInterfaces(node, c, vrf, interfaces);
    }

    return interfaces;
  }

  private static void createFallbackFabricInterfaces(
      FabricNode node, Configuration c, Vrf vrf, Map<String, Interface> interfaces) {

    String role = node.getRole();
    if (role == null) {
      return; // Can't determine what interfaces to create
    }

    String roleLower = role.toLowerCase();

    if ("spine".equals(roleLower)) {
      for (int i = 1; i <= AciConstants.SPINE_FABRIC_PORT_COUNT; i++) {
        String ifaceName = String.format("ethernet1/%d", i);
        Interface iface =
            Interface.builder()
                .setName(ifaceName)
                .setType(InterfaceType.PHYSICAL)
                .setOwner(c)
                .setVrf(vrf)
                .setAdminUp(true)
                .setMtu(AciConstants.DEFAULT_MTU)
                .setDescription(
                    String.format("Fabric interface to leaf (fallback) - Node %s", node.getName()))
                .setHumanName(ifaceName)
                .setDeclaredNames(ImmutableList.of(ifaceName))
                .build();
        interfaces.put(ifaceName, iface);
      }
    } else if ("leaf".equals(roleLower)
        || "services".equals(roleLower)
        || "service".equals(roleLower)) {
      for (int i = AciConstants.LEAF_UPLINK_START; i <= AciConstants.LEAF_UPLINK_END; i++) {
        String ifaceName = String.format("ethernet1/%d", i);
        Interface iface =
            Interface.builder()
                .setName(ifaceName)
                .setType(InterfaceType.PHYSICAL)
                .setOwner(c)
                .setVrf(vrf)
                .setAdminUp(true)
                .setMtu(AciConstants.DEFAULT_MTU)
                .setDescription(
                    String.format("Fabric uplink to spine (fallback) - Node %s", node.getName()))
                .setHumanName(ifaceName)
                .setDeclaredNames(ImmutableList.of(ifaceName))
                .build();
        interfaces.put(ifaceName, iface);
      }

      for (int i = 1; i <= AciConstants.FALLBACK_DOWNSTREAM_PORT_COUNT; i++) {
        String ifaceName = String.format("ethernet1/%d", i);
        Interface iface =
            Interface.builder()
                .setName(ifaceName)
                .setType(InterfaceType.PHYSICAL)
                .setOwner(c)
                .setVrf(vrf)
                .setAdminUp(true)
                .setMtu(AciConstants.DEFAULT_MTU)
                .setDescription(
                    String.format("Downstream port for EPGs (fallback) - Node %s", node.getName()))
                .setHumanName(ifaceName)
                .setDeclaredNames(ImmutableList.of(ifaceName))
                .build();
        interfaces.put(ifaceName, iface);
      }
    }
  }

  private static boolean isFabricInterface(String ifaceName, String role) {
    if (ifaceName == null) {
      return false;
    }

    String name = ifaceName.toLowerCase();

    if (name.matches(".*eth1/(5[3-9]|6[0-9]|[7-9][0-9]).*")) {
      return true;
    }

    if ("spine".equalsIgnoreCase(role)) {
      if (name.matches(".*eth1/([1-9]|[1-5][0-9]).*")) {
        return true;
      }
    }

    return false;
  }

  private static InterfaceType toInterfaceType(String type) {
    if (type == null) {
      return InterfaceType.PHYSICAL;
    }
    switch (type.toLowerCase()) {
      case "physical":
      case "ethernet":
        return InterfaceType.PHYSICAL;
      case "vlan":
        return InterfaceType.VLAN;
      case "loopback":
        return InterfaceType.LOOPBACK;
      case "portchannel":
      case "aggregated":
        return InterfaceType.AGGREGATED;
      default:
        return InterfaceType.PHYSICAL;
    }
  }
}
