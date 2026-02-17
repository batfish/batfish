package org.batfish.vendor.cisco_aci.representation;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.ospf.StubType;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/**
 * Conversion helpers for converting VS model {@link AciConfiguration} to the VI model.
 *
 * <p>Cisco ACI (Application Centric Infrastructure) uses a different configuration model than
 * traditional Cisco devices. Key concepts:
 *
 * <ul>
 *   <li>fabricNode: Physical switches/spines in the fabric
 *   <li>fvCtx (VRF): Forwarding contexts for L3 isolation
 *   <li>fvBD (Bridge Domain): L2 domains similar to VLANs
 *   <li>fvaAEPg (EPG): Endpoint Groups - logical groupings of endpoints
 *   <li>vzBrCP (Contract): Security policies between EPGs
 *   <li>fvRsPathAtt: Path attachments linking EPGs to physical interfaces
 *   <li>l3ExtOut: L3 external connectivity (BGP, OSPF, etc.)
 * </ul>
 */
@SuppressWarnings({
  "PMD.UnusedPrivateField",
  "PMD.UnusedLocalVariable",
  "PMD.UnusedFormalParameter"
})
public final class AciConversion {

  /** Default administrative distance for EBGP routes on ACI */
  public static final int DEFAULT_EBGP_ADMIN_COST = 20;

  /** Default administrative distance for IBGP routes on ACI */
  public static final int DEFAULT_IBGP_ADMIN_COST = 200;

  /** Default local BGP weight on ACI */
  public static final int DEFAULT_LOCAL_BGP_WEIGHT = 0;

  /** Default MTU for ACI interfaces */
  public static final int DEFAULT_MTU = 9000;

  /** Prefix used for generated ACL names for contracts */
  private static final String CONTRACT_ACL_PREFIX = "~CONTRACT~";

  /** Prefix used for generated ACL names for taboo contracts */
  private static final String TABOO_ACL_PREFIX = "~TABOO~";

  /** Maximum prefix length for host routes */
  private static final int MAX_PREFIX_LENGTH = Prefix.MAX_PREFIX_LENGTH;

  /**
   * Converts an ACI configuration to vendor-independent Batfish Configuration objects.
   *
   * <p>Each fabricNode in the ACI configuration becomes a separate Batfish Configuration. This
   * allows Batfish to model the fabric as individual switches.
   *
   * <p>If no fabric nodes are present, a single configuration is created representing the entire
   * fabric.
   *
   * @param aciConfig The ACI configuration to convert
   * @param warnings Warnings container for conversion issues
   * @return Map of node hostnames to Batfish Configurations
   */
  public static @Nonnull SortedMap<String, Configuration> toVendorIndependentConfigurations(
      AciConfiguration aciConfig, Warnings warnings) {
    ImmutableSortedMap.Builder<String, Configuration> configs = ImmutableSortedMap.naturalOrder();

    // If no fabric nodes are defined, create a single configuration for the fabric
    // representing the logical ACI fabric itself
    if (aciConfig.getFabricNodes().isEmpty()) {
      Configuration c = convertFabricConfig(aciConfig, warnings);
      configs.put(aciConfig.getHostname(), c);
      warnings.redFlag(
          "No fabric nodes defined in ACI configuration. Creating single configuration for fabric: "
              + aciConfig.getHostname());
      return configs.build();
    }

    Map<String, String> nodeIdToHostname = computeNodeIdToHostnameMap(aciConfig, warnings);

    // Process each fabric node as a separate configuration
    for (AciConfiguration.FabricNode node : aciConfig.getFabricNodes().values()) {
      String nodeId = node.getNodeId();
      if (nodeId == null || nodeId.isEmpty()) {
        warnings.redFlag("Skipping fabric node without nodeId during conversion.");
        continue;
      }
      String hostname = nodeIdToHostname.get(nodeId);
      if (hostname == null) {
        warnings.redFlagf("Skipping fabric node %s due to unresolved hostname.", nodeId);
        continue;
      }
      Configuration c = convertNode(node, aciConfig, hostname, warnings);
      configs.put(hostname, c);
    }

    return configs.build();
  }

  /**
   * Converts a single fabric node to a Batfish Configuration.
   *
   * @param node The fabric node to convert
   * @param aciConfig The full ACI configuration
   * @param warnings Warnings container
   * @return A Batfish Configuration for the node
   */
  private static Configuration convertNode(
      AciConfiguration.FabricNode node,
      AciConfiguration aciConfig,
      String hostname,
      Warnings warnings) {
    // Use the actual node name from fabricNodeIdentP if available
    // Example: nodeName = "SW-DC1-Leaf-NSAB07-SET-01" from config
    String nodeId = node.getNodeId();
    String nodeName = node.getName();

    // Set humanName consistent with other vendors (e.g., NX-OS)
    // Use nodeName directly as the human-readable name, without augmenting with nodeId
    String humanName = nodeName != null && !nodeName.isEmpty() ? nodeName : hostname;

    Configuration c = new Configuration(hostname, ConfigurationFormat.CISCO_ACI);
    c.setHumanName(humanName);
    c.setDeviceModel(DeviceModel.CISCO_UNSPECIFIED);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);
    c.setExportBgpFromBgpRib(true);

    // Create VRFs (fvCtx)
    SortedMap<String, Vrf> vrfs = convertVrfs(aciConfig, c);
    c.setVrfs(vrfs);

    // Get the default VRF for the node
    Vrf defaultVrf = vrfs.get(DEFAULT_VRF_NAME);
    if (defaultVrf == null) {
      // Create default VRF if not present
      defaultVrf = new Vrf(DEFAULT_VRF_NAME);
    }

    // Convert interfaces
    Map<String, Interface> interfaces = convertInterfaces(node, aciConfig, defaultVrf, c, warnings);
    c.setInterfaces(interfaces);

    // Create VPC peer-link interface if this node is part of a VPC pair
    createVpcPeerLinkInterface(node, aciConfig, interfaces, defaultVrf, c, warnings);

    // Convert bridge domains to interface VLAN settings
    convertBridgeDomains(aciConfig, interfaces, defaultVrf, c, warnings);

    // Convert contracts to ACLs
    convertContracts(aciConfig, c, warnings);
    convertTabooContracts(aciConfig, c, warnings);

    // Convert path attachments (EPG to interface mappings)
    convertPathAttachments(aciConfig, interfaces, c, warnings);

    // Convert L3Out configurations (BGP, static routes, etc.)
    convertL3Outs(node, aciConfig, interfaces, defaultVrf, c, warnings);

    // Convert L2Out configurations (external Layer 2 connectivity)
    convertL2Outs(node, aciConfig, interfaces, defaultVrf, c, warnings);

    return c;
  }

  private static @Nonnull Map<String, String> computeNodeIdToHostnameMap(
      AciConfiguration aciConfig, @Nullable Warnings warnings) {
    Map<String, String> nodeIdToHostname = new TreeMap<>();
    Set<String> usedHostnames = new HashSet<>();
    for (AciConfiguration.FabricNode node : aciConfig.getFabricNodes().values()) {
      String nodeId = node.getNodeId();
      if (nodeId == null || nodeId.isEmpty()) {
        continue;
      }
      String baseHostname = computeNodeHostname(node, aciConfig.getHostname());
      String hostname = baseHostname;
      int suffix = 2;
      while (usedHostnames.contains(hostname)) {
        hostname = String.format("%s-%s-%d", baseHostname, nodeId, suffix++);
      }
      if (!hostname.equals(baseHostname) && warnings != null) {
        warnings.redFlagf(
            "Duplicate ACI node hostname '%s' detected; using '%s' for nodeId %s.",
            baseHostname, hostname, nodeId);
      }
      usedHostnames.add(hostname);
      nodeIdToHostname.put(nodeId, hostname);
    }
    return nodeIdToHostname;
  }

  private static @Nonnull String computeNodeHostname(
      AciConfiguration.FabricNode node, @Nullable String fabricHostname) {
    String nodeName = node.getName();
    if (nodeName != null && !nodeName.isEmpty()) {
      return nodeName;
    }
    String nodeId = node.getNodeId();
    if (nodeId != null && !nodeId.isEmpty()) {
      return computeFallbackNodeHostname(fabricHostname, nodeId);
    }
    return "aci-node-unknown";
  }

  private static @Nonnull String computeFallbackNodeHostname(
      @Nullable String fabricHostname, @Nonnull String nodeId) {
    String fabricBase = fabricHostname != null ? fabricHostname.trim() : "aci";
    if (fabricBase.isEmpty()) {
      fabricBase = "aci";
    }
    // Handle both JSON and XML filenames and collapse duplicate ACI prefixes.
    fabricBase = fabricBase.replaceAll("\\.(json|xml)$", "");
    fabricBase = fabricBase.replaceFirst("(?i)^(aci-)+", "aci-");
    fabricBase = fabricBase.replaceAll("-+$", "");
    return fabricBase + "-" + nodeId;
  }

  /**
   * Converts a fabric configuration without fabric nodes to a Batfish Configuration.
   *
   * <p>This is used when no fabric nodes are defined in the ACI configuration. A single
   * configuration is created representing the entire fabric.
   *
   * @param aciConfig The ACI configuration
   * @param warnings Warnings container
   * @return A Batfish Configuration for the fabric
   */
  private static Configuration convertFabricConfig(AciConfiguration aciConfig, Warnings warnings) {
    String hostname = aciConfig.getHostname();
    Configuration c = new Configuration(hostname, ConfigurationFormat.CISCO_ACI);
    c.setHumanName(hostname);
    c.setDeviceModel(DeviceModel.CISCO_UNSPECIFIED);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDefaultInboundAction(LineAction.PERMIT);
    c.setExportBgpFromBgpRib(true);

    // Create VRFs (fvCtx)
    SortedMap<String, Vrf> vrfs = convertVrfs(aciConfig, c);
    c.setVrfs(vrfs);

    // Get the default VRF
    Vrf defaultVrf = vrfs.get(DEFAULT_VRF_NAME);
    if (defaultVrf == null) {
      defaultVrf = new Vrf(DEFAULT_VRF_NAME);
    }

    // Create minimal interface set
    Map<String, Interface> interfaces = new TreeMap<>();

    // Add loopback interface
    Interface loopback =
        Interface.builder()
            .setName("loopback0")
            .setType(InterfaceType.LOOPBACK)
            .setOwner(c)
            .setVrf(defaultVrf)
            .setAdminUp(true)
            .setHumanName("Loopback0")
            .setDeclaredNames(ImmutableList.of("loopback0"))
            .build();
    interfaces.put("loopback0", loopback);
    c.setInterfaces(interfaces);

    // Convert bridge domains
    convertBridgeDomains(aciConfig, interfaces, defaultVrf, c, warnings);

    // Convert contracts to ACLs
    convertContracts(aciConfig, c, warnings);
    convertTabooContracts(aciConfig, c, warnings);

    return c;
  }

  /**
   * Converts ACI VRF contexts (fvCtx) to Batfish Vrf objects.
   *
   * @param aciConfig The ACI configuration
   * @param c The Batfish configuration being built
   * @return Map of VRF names to Vrf objects
   */
  private static SortedMap<String, Vrf> convertVrfs(AciConfiguration aciConfig, Configuration c) {
    SortedMap<String, Vrf> vrfs = new TreeMap<>();

    // Always create default VRF
    Vrf defaultVrf = new Vrf(DEFAULT_VRF_NAME);
    vrfs.put(DEFAULT_VRF_NAME, defaultVrf);

    // Convert each ACI VRF (fvCtx) to a Batfish VRF
    for (AciVrfModel aciVrf : aciConfig.getVrfs().values()) {
      String vrfName = aciVrf.getName();
      if (vrfName == null || vrfName.isEmpty()) {
        vrfName = DEFAULT_VRF_NAME;
      }

      // Skip if already exists (e.g., default VRF)
      if (!vrfs.containsKey(vrfName)) {
        Vrf vrf = new Vrf(vrfName);
        if (aciVrf.getDescription() != null) {
          // Note: Vrf class doesn't have description field, but we could store it elsewhere
        }
        vrfs.put(vrfName, vrf);
      }
    }

    return vrfs;
  }

  /**
   * Converts ACI interfaces to Batfish Interface objects.
   *
   * @param node The fabric node
   * @param aciConfig The ACI configuration
   * @param vrf The default VRF
   * @param c The Batfish configuration
   * @param warnings Warnings container
   * @return Map of interface names to Interface objects
   */
  private static Map<String, Interface> convertInterfaces(
      AciConfiguration.FabricNode node,
      AciConfiguration aciConfig,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {

    Map<String, Interface> interfaces = new TreeMap<>();

    // Get interfaces from the fabric node (if populated)
    if (node.getInterfaces() != null) {
      for (AciConfiguration.FabricNode.Interface fvIface : node.getInterfaces().values()) {
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
                .setMtu(DEFAULT_MTU)
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
                    .setMtu(DEFAULT_MTU)
                    .setHumanName(ifaceName)
                    .setDeclaredNames(ImmutableList.of(ifaceName));

            // Add description from path attachment if available
            boolean hasDescription = false;
            StringBuilder description = new StringBuilder();

            if (aciConfig.getPathAttachmentMap() != null) {
              Map<String, AciConfiguration.PathAttachment> nodeAttachments =
                  aciConfig.getPathAttachmentMap().get(nodeId);
              if (nodeAttachments != null) {
                AciConfiguration.PathAttachment attachment = nodeAttachments.get(ifaceName);
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
    AciConfiguration.ManagementInfo mgmtInfo = node.getManagementInfo();
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

        // Note: Static route for default gateway would be added to the VRF here
        // if needed for management traffic routing
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

  /**
   * Creates fallback fabric interfaces for a node that has no explicit interfaces defined.
   *
   * <p>This ensures isolated nodes (like those from configs without EPG path attachments) get
   * connected in the spine-leaf topology. Interface creation is based on node role:
   *
   * <ul>
   *   <li>Spine: Creates interfaces for connecting to multiple leaf switches
   *   <li>Leaf: Creates fabric uplink interfaces (typically eth1/53-54) and downstream ports
   *   <li>Services: Treated as leaf switches
   * </ul>
   *
   * @param node The fabric node
   * @param c The Batfish configuration
   * @param vrf The VRF for the interfaces
   * @param interfaces Map to add the created interfaces to
   */
  private static void createFallbackFabricInterfaces(
      AciConfiguration.FabricNode node,
      Configuration c,
      Vrf vrf,
      Map<String, Interface> interfaces) {

    String role = node.getRole();
    if (role == null) {
      return; // Can't determine what interfaces to create
    }

    String roleLower = role.toLowerCase();

    if ("spine".equals(roleLower)) {
      // Spine switches connect to all leaf switches
      // Create multiple fabric interfaces (eth1/1 through eth1/32 as typical spine ports)
      for (int i = 1; i <= 32; i++) {
        String ifaceName = String.format("ethernet1/%d", i);
        Interface iface =
            Interface.builder()
                .setName(ifaceName)
                .setType(InterfaceType.PHYSICAL)
                .setOwner(c)
                .setVrf(vrf)
                .setAdminUp(true)
                .setMtu(DEFAULT_MTU)
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
      // Leaf switches have:
      // 1. Fabric uplinks to spine (eth1/53-54 typically)
      // 2. Downstream ports for endpoints (eth1/1-52)

      // Create fabric uplink interfaces
      for (int i = 53; i <= 54; i++) {
        String ifaceName = String.format("ethernet1/%d", i);
        Interface iface =
            Interface.builder()
                .setName(ifaceName)
                .setType(InterfaceType.PHYSICAL)
                .setOwner(c)
                .setVrf(vrf)
                .setAdminUp(true)
                .setMtu(DEFAULT_MTU)
                .setDescription(
                    String.format("Fabric uplink to spine (fallback) - Node %s", node.getName()))
                .setHumanName(ifaceName)
                .setDeclaredNames(ImmutableList.of(ifaceName))
                .build();
        interfaces.put(ifaceName, iface);
      }

      // Create a few downstream interfaces for EPG connectivity
      for (int i = 1; i <= 8; i++) {
        String ifaceName = String.format("ethernet1/%d", i);
        Interface iface =
            Interface.builder()
                .setName(ifaceName)
                .setType(InterfaceType.PHYSICAL)
                .setOwner(c)
                .setVrf(vrf)
                .setAdminUp(true)
                .setMtu(DEFAULT_MTU)
                .setDescription(
                    String.format("Downstream port for EPGs (fallback) - Node %s", node.getName()))
                .setHumanName(ifaceName)
                .setDeclaredNames(ImmutableList.of(ifaceName))
                .build();
        interfaces.put(ifaceName, iface);
      }
    }
  }

  /**
   * Determines if an interface is a fabric-facing interface based on naming patterns.
   *
   * <p>In ACI, fabric interfaces are used for IS-IS and overlay traffic. Common patterns: -
   * eth1/53-54: Fabric interfaces on leaf switches (for spine connectivity) - eth1/1-52: Front
   * panel data ports (for EPGs/endpoints)
   *
   * @param ifaceName The interface name to check
   * @param role The node role (leaf/spine)
   * @return true if this appears to be a fabric interface
   */
  private static boolean isFabricInterface(String ifaceName, String role) {
    if (ifaceName == null) {
      return false;
    }

    // Convert to lowercase for pattern matching
    String name = ifaceName.toLowerCase();

    // Common fabric interface patterns
    // On leaf switches: eth1/53-54 typically connect to spines
    // On spine switches: eth1/1-52 may connect to leaves
    if (name.matches(".*eth1/(5[3-9]|6[0-9]|[7-9][0-9]).*")) {
      return true; // High-numbered eth1/X interfaces are typically fabric
    }

    // Spine switches: more interfaces are fabric-facing
    if ("spine".equalsIgnoreCase(role)) {
      if (name.matches(".*eth1/([1-9]|[1-5][0-9]).*")) {
        return true; // Most interfaces on spines are fabric-facing
      }
    }

    return false;
  }

  /**
   * Converts an ACI interface type string to Batfish InterfaceType.
   *
   * @param type The ACI interface type string
   * @return The Batfish InterfaceType
   */
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

  /**
   * Converts ACI Bridge Domains (fvBD) to interface VLAN settings.
   *
   * <p>In ACI, Bridge Domains represent L2 domains with associated subnets. This method:
   *
   * <ul>
   *   <li>Creates VLAN interfaces for bridge domains
   *   <li>Associates bridge domain subnets with those VLAN interfaces
   *   <li>Sets up routing between bridge domains
   * </ul>
   *
   * @param aciConfig The ACI configuration
   * @param interfaces Map of existing interfaces
   * @param vrf The VRF to add bridge domains to
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  private static void convertBridgeDomains(
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {

    // First pass: collect all subnets per bridge domain and create VLAN interfaces
    // We need to do this in two passes because a bridge domain can have multiple subnets
    Map<String, List<ConcreteInterfaceAddress>> bdSubnets = new TreeMap<>();

    for (AciConfiguration.BridgeDomain bd : aciConfig.getBridgeDomains().values()) {
      String bdName = bd.getName();

      // Parse all subnets for this BD
      List<ConcreteInterfaceAddress> subnets = new ArrayList<>();
      if (bd.getSubnets() != null) {
        for (String subnetStr : bd.getSubnets()) {
          Prefix subnet = parsePrefix(subnetStr);
          if (subnet != null) {
            // Convert network address to first usable host address
            Ip gatewayIp = subnet.getStartIp();
            int prefixLen = subnet.getPrefixLength();
            if (prefixLen < 31) {
              // For subnets larger than /31, add 1 to get first usable host
              gatewayIp = Ip.create(gatewayIp.asLong() + 1);
            }
            subnets.add(ConcreteInterfaceAddress.create(gatewayIp, prefixLen));
          } else {
            warnings.redFlagf("Invalid subnet in bridge domain %s: %s", bdName, subnetStr);
          }
        }
      }
      bdSubnets.put(bdName, subnets);
    }

    // Second pass: create VLAN interfaces with collected subnets
    for (AciConfiguration.BridgeDomain bd : aciConfig.getBridgeDomains().values()) {
      String bdName = bd.getName();
      String vrfName = bd.getVrf();

      // Find or create the VRF for this bridge domain
      Vrf targetVrf = vrf;
      if (vrfName != null) {
        Vrf foundVrf = c.getVrfs().get(vrfName);
        if (foundVrf != null) {
          targetVrf = foundVrf;
        } else {
          warnings.redFlagf(
              "VRF %s not found for bridge domain %s, using default VRF", vrfName, bdName);
        }
      }

      // Get the subnets for this BD
      List<ConcreteInterfaceAddress> subnets = bdSubnets.get(bdName);

      // Determine VLAN ID from encapsulation or fallback to hash-based generation
      int vlanId;
      String encap = bd.getEncapsulation();
      if (encap != null && encap.startsWith("vlan-")) {
        try {
          // Extract VLAN ID from encapsulation string (e.g., "vlan-100" -> 100)
          vlanId = Integer.parseInt(encap.substring(5));
        } catch (NumberFormatException e) {
          // Fallback to hash-based generation if parsing fails
          vlanId = Math.abs(bdName.hashCode() % 4094) + 1;
          warnings.redFlagf(
              "Invalid encapsulation '%s' for bridge domain %s, using generated VLAN ID %d",
              encap, bdName, vlanId);
        }
      } else {
        // No encapsulation found, generate VLAN ID from hash
        vlanId = Math.abs(bdName.hashCode() % 4094) + 1;
      }

      String vlanInterfaceName = "Vlan" + vlanId;

      if (!interfaces.containsKey(vlanInterfaceName)) {
        Interface.Builder vlanIfaceBuilder =
            Interface.builder()
                .setName(vlanInterfaceName)
                .setType(InterfaceType.VLAN)
                .setOwner(c)
                .setVrf(targetVrf)
                .setAdminUp(true)
                .setMtu(DEFAULT_MTU)
                .setHumanName("VLAN " + vlanId + " (" + bdName + ")")
                .setDeclaredNames(ImmutableList.of(vlanInterfaceName))
                .setVlan(vlanId);

        // Set addresses: primary is first subnet, rest are secondary
        if (subnets != null && !subnets.isEmpty()) {
          ConcreteInterfaceAddress primaryAddr = subnets.get(0);
          if (subnets.size() > 1) {
            List<InterfaceAddress> secondaryAddrs =
                new ArrayList<>(subnets.subList(1, subnets.size()));
            vlanIfaceBuilder.setAddresses(primaryAddr, secondaryAddrs);
          } else {
            vlanIfaceBuilder.setAddress(primaryAddr);
          }

          // Set connected route metadata for all addresses
          Map<ConcreteInterfaceAddress, ConnectedRouteMetadata> addrMetadata = new TreeMap<>();
          for (ConcreteInterfaceAddress addr : subnets) {
            addrMetadata.put(
                addr,
                ConnectedRouteMetadata.builder()
                    .setGenerateConnectedRoute(true)
                    .setGenerateLocalRoute(false)
                    .build());
          }
          vlanIfaceBuilder.setAddressMetadata(addrMetadata);
        }

        Interface vlanInterface = vlanIfaceBuilder.build();
        interfaces.put(vlanInterfaceName, vlanInterface);
      }
    }
  }

  /**
   * Parses a prefix string to a Prefix object.
   *
   * @param prefixStr The prefix string (e.g., "10.0.0.0/24")
   * @return The Prefix object, or null if parsing fails
   */
  private static @Nullable Prefix parsePrefix(String prefixStr) {
    if (prefixStr == null) {
      return null;
    }
    try {
      return Prefix.parse(prefixStr);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Converts ACI Contracts (vzBrCP) to Batfish ACLs.
   *
   * <p>Contracts in ACI define allowed communication between Endpoint Groups. This method:
   *
   * <ul>
   *   <li>Creates an IP access list for each contract
   *   <li>Adds permit/deny rules based on contract subjects and filters
   *   <li>Associates the ACL with the appropriate interfaces
   * </ul>
   *
   * @param aciConfig The ACI configuration
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  private static void convertContracts(
      AciConfiguration aciConfig, Configuration c, Warnings warnings) {
    for (Map.Entry<String, AciConfiguration.Contract> entry : aciConfig.getContracts().entrySet()) {
      // Use the map key which contains the fully-qualified name (tenant:contract)
      String contractName = entry.getKey();
      AciConfiguration.Contract contract = entry.getValue();
      if (contractName == null || contractName.isEmpty()) {
        continue;
      }
      String aclName = getContractAclName(contractName);
      List<ExprAclLine> aclLines =
          buildAclLinesFromSubjects(contractName, contract.getSubjects(), aciConfig, c, warnings);
      installAclIfNonEmpty(c, aclName, aclLines);
    }
  }

  private static void convertTabooContracts(
      AciConfiguration aciConfig, Configuration c, Warnings warnings) {
    for (Map.Entry<String, AciConfiguration.TabooContract> entry :
        aciConfig.getTabooContracts().entrySet()) {
      String tabooName = entry.getKey();
      AciConfiguration.TabooContract taboo = entry.getValue();
      if (tabooName == null || tabooName.isEmpty()) {
        continue;
      }
      String aclName = getTabooAclName(tabooName);
      List<ExprAclLine> aclLines =
          buildAclLinesFromSubjects(tabooName, taboo.getSubjects(), aciConfig, c, warnings);
      installAclIfNonEmpty(c, aclName, aclLines);
    }
  }

  private static @Nonnull List<ExprAclLine> buildAclLinesFromSubjects(
      String contractName,
      @Nullable List<AciConfiguration.Contract.Subject> subjects,
      AciConfiguration aciConfig,
      Configuration c,
      Warnings warnings) {
    ImmutableList.Builder<ExprAclLine> lines = ImmutableList.builder();

    // Extract tenant name from fully-qualified contract name (tenant:contract)
    String tenantName = null;
    int colonIdx = contractName.indexOf(':');
    if (colonIdx > 0) {
      tenantName = contractName.substring(0, colonIdx);
    }

    if (subjects != null) {
      for (AciConfiguration.Contract.Subject subject : subjects) {
        if (subject.getFilters() != null) {
          for (AciConfiguration.Contract.Filter filterRef : subject.getFilters()) {
            String filterName = filterRef.getName();
            if (filterName == null || filterName.isEmpty()) {
              lines.addAll(toAclLines(filterRef, contractName, c, warnings));
              continue;
            }

            String fqFilterName =
                (tenantName != null) ? (tenantName + ":" + filterName) : filterName;
            AciConfiguration.Filter fullFilter = aciConfig.getFilters().get(fqFilterName);

            if (fullFilter != null
                && fullFilter.getEntries() != null
                && !fullFilter.getEntries().isEmpty()) {
              for (AciConfiguration.Filter.Entry filterEntry : fullFilter.getEntries()) {
                List<ExprAclLine> entryLines =
                    toAclEntryLines(filterEntry, contractName, filterName, c, warnings);
                lines.addAll(entryLines);
              }
            } else {
              lines.addAll(toAclLines(filterRef, contractName, c, warnings));
            }
          }
        }
      }
    }

    if (!lines.build().isEmpty()) {
      lines.add(
          new ExprAclLine(
              LineAction.DENY,
              AclLineMatchExprs.TRUE,
              "Default deny for contract " + contractName));
    }
    return lines.build();
  }

  private static void installAclIfNonEmpty(
      Configuration c, String aclName, List<ExprAclLine> aclLines) {
    if (!aclLines.isEmpty()) {
      List<AclLine> aclLinesCasted = new ArrayList<>(aclLines);
      IpAccessList acl =
          IpAccessList.builder().setOwner(c).setName(aclName).setLines(aclLinesCasted).build();
      c.getIpAccessLists().put(aclName, acl);
    }
  }

  /**
   * Converts a contract filter to ACL lines.
   *
   * <p>This method converts ACI filter entries to Batfish ACL lines, supporting:
   *
   * <ul>
   *   <li>IP protocols - TCP, UDP, ICMP, or protocol number
   *   <li>TCP/UDP port ranges - Single ports or ranges (e.g., "80", "8080-8090")
   *   <li>IP address ranges - With wildcards (e.g., "10.0.0.0/24", "10.0.0.0/0.0.0.255")
   *   <li>ICMP types and codes - For ICMP protocol filtering
   *   <li>Non-IP traffic - ARP, MPLS via etherType field
   * </ul>
   *
   * @param filter The contract filter
   * @param contractName The contract name for trace elements
   * @param c The Batfish configuration
   * @param warnings Warnings container
   * @return List of ACL lines
   */
  private static List<ExprAclLine> toAclLines(
      AciConfiguration.Contract.Filter filter,
      String contractName,
      Configuration c,
      Warnings warnings) {

    ImmutableList.Builder<ExprAclLine> lines = ImmutableList.builder();

    // Determine action based on filter's action field (default to permit)
    LineAction action = LineAction.PERMIT;
    if (filter.getAction() != null && "deny".equalsIgnoreCase(filter.getAction())) {
      action = LineAction.DENY;
    }

    // Build match expressions based on filter criteria
    ImmutableList.Builder<AclLineMatchExpr> matchExprs = ImmutableList.builder();

    // Handle non-IP traffic via etherType (e.g., ARP, MPLS)
    if (filter.getEtherType() != null && !filter.getEtherType().isEmpty()) {
      AclLineMatchExpr etherTypeExpr =
          toEtherType(filter.getEtherType(), contractName, filter, warnings);
      if (etherTypeExpr != null) {
        matchExprs.add(etherTypeExpr);
      }
    }

    // Match on IP protocol if specified
    if (filter.getIpProtocol() != null) {
      AclLineMatchExpr protocolExpr = toIpProtocolMatchExpr(filter.getIpProtocol(), warnings);
      if (protocolExpr != null) {
        matchExprs.add(protocolExpr);

        // Handle ICMP-specific fields (type and code)
        if (filter.getIpProtocol().toLowerCase().contains("icmp")) {
          if (filter.getIcmpType() != null) {
            matchExprs.add(toIcmpTypeCode(filter.getIcmpType(), filter.getIcmpCode(), warnings));
          }
        }
      }
    }

    // Match on source IP address if specified
    if (filter.getSourceAddress() != null && !filter.getSourceAddress().isEmpty()) {
      AclLineMatchExpr srcAddrExpr = toIpMatchExpr(filter.getSourceAddress(), true, warnings);
      if (srcAddrExpr != null) {
        matchExprs.add(srcAddrExpr);
      }
    }

    // Match on destination IP address if specified
    if (filter.getDestinationAddress() != null && !filter.getDestinationAddress().isEmpty()) {
      AclLineMatchExpr dstAddrExpr = toIpMatchExpr(filter.getDestinationAddress(), false, warnings);
      if (dstAddrExpr != null) {
        matchExprs.add(dstAddrExpr);
      }
    }

    // Match on destination ports if specified (supports ranges)
    if (filter.getDestinationPorts() != null && !filter.getDestinationPorts().isEmpty()) {
      IntegerSpace portSpace =
          toPortSpace(filter.getDestinationPorts(), contractName, filter, true, warnings);
      if (!portSpace.isEmpty()) {
        matchExprs.add(AclLineMatchExprs.matchDstPort(portSpace));
      }
    }

    // Match on source ports if specified (supports ranges)
    if (filter.getSourcePorts() != null && !filter.getSourcePorts().isEmpty()) {
      IntegerSpace portSpace =
          toPortSpace(filter.getSourcePorts(), contractName, filter, false, warnings);
      if (!portSpace.isEmpty()) {
        matchExprs.add(AclLineMatchExprs.matchSrcPort(portSpace));
      }
    }

    // Handle ARP opcode if specified
    if (isMeaningfulArpOpcode(filter.getArpOpcode())) {
      // ARP is handled at L2, so we emit a warning that this filter may have limited effect
      warnings.redFlagf(
          "ARP opcode specified in contract %s filter %s: %s. ARP filtering has limited effect in"
              + " IP ACLs.",
          contractName, filter.getName(), filter.getArpOpcode());
    }

    AclLineMatchExpr matchExpr =
        matchExprs.build().isEmpty()
            ? AclLineMatchExprs.TRUE
            : AclLineMatchExprs.and(matchExprs.build());

    String filterName = filter.getName() != null ? filter.getName() : "unnamed";
    ExprAclLine line =
        new ExprAclLine(
            action, matchExpr, String.format("Contract %s filter %s", contractName, filterName));

    lines.add(line);
    return lines.build();
  }

  /**
   * Converts a filter entry to ACL lines.
   *
   * <p>This method converts ACI filter entries (vzEntry) to Batfish ACL lines, supporting:
   *
   * <ul>
   *   <li>IP protocols - TCP, UDP, ICMP, or protocol number
   *   <li>TCP/UDP ports - Single ports or ranges (dPort, sPort, dFromPort/dToPort,
   *       sFromPort/sToPort)
   *   <li>IP address ranges - srcAddr, dstAddr
   *   <li>ICMP types and codes - icmpv4T/icmpv4C, icmpv6T/icmpv6C
   *   <li>Non-IP traffic - ARP via arpOpc, etherType
   * </ul>
   *
   * @param entry The filter entry
   * @param contractName The contract name for trace elements
   * @param filterName The filter name for trace elements
   * @param c The Batfish configuration
   * @param warnings Warnings container
   * @return List of ACL lines
   */
  private static List<ExprAclLine> toAclEntryLines(
      AciConfiguration.Filter.Entry entry,
      String contractName,
      String filterName,
      Configuration c,
      Warnings warnings) {

    ImmutableList.Builder<ExprAclLine> lines = ImmutableList.builder();
    LineAction action = LineAction.PERMIT; // Default action for filter entries
    ImmutableList.Builder<AclLineMatchExpr> matchExprs = ImmutableList.builder();

    // Handle non-IP traffic via etherType (e.g., ARP, MPLS)
    if (entry.getEtherType() != null && !entry.getEtherType().isEmpty()) {
      AclLineMatchExpr etherTypeExpr =
          toEtherType(entry.getEtherType(), contractName, entry.getName(), warnings);
      if (etherTypeExpr != null) {
        matchExprs.add(etherTypeExpr);
      }
    }

    // Match on IP protocol if specified
    if (entry.getProtocol() != null) {
      AclLineMatchExpr protocolExpr = toIpProtocolMatchExpr(entry.getProtocol(), warnings);
      if (protocolExpr != null) {
        matchExprs.add(protocolExpr);

        // Handle ICMP-specific fields (type and code)
        String protocol = entry.getProtocol().toLowerCase();
        if (protocol.contains("icmpv4") || protocol.equals("icmp")) {
          if (entry.getIcmpv4Type() != null) {
            matchExprs.add(toIcmpTypeCode(entry.getIcmpv4Type(), entry.getIcmpv4Code(), warnings));
          }
        } else if (protocol.contains("icmpv6")) {
          if (entry.getIcmpv6Type() != null) {
            matchExprs.add(toIcmpTypeCode(entry.getIcmpv6Type(), entry.getIcmpv6Code(), warnings));
          }
        }
      }
    }

    // Match on source IP address if specified
    if (entry.getSourceAddress() != null && !entry.getSourceAddress().isEmpty()) {
      AclLineMatchExpr srcAddrExpr = toIpMatchExpr(entry.getSourceAddress(), true, warnings);
      if (srcAddrExpr != null) {
        matchExprs.add(srcAddrExpr);
      }
    }

    // Match on destination IP address if specified
    if (entry.getDestinationAddress() != null && !entry.getDestinationAddress().isEmpty()) {
      AclLineMatchExpr dstAddrExpr = toIpMatchExpr(entry.getDestinationAddress(), false, warnings);
      if (dstAddrExpr != null) {
        matchExprs.add(dstAddrExpr);
      }
    }

    // Match on destination ports - handle both single port and port range
    String dstPort = normalizeSinglePort(entry.getDestinationPort());
    String dstFromPort = normalizeRangeEndpoint(entry.getDestinationFromPort());
    String dstToPort = normalizeRangeEndpoint(entry.getDestinationToPort());

    if (dstPort != null || (dstFromPort != null && dstToPort != null)) {
      List<String> dstPorts = new ArrayList<>();
      if (dstPort != null) {
        dstPorts.add(dstPort);
      } else if (dstFromPort != null && dstToPort != null) {
        dstPorts.add(dstFromPort + "-" + dstToPort);
      }
      IntegerSpace portSpace = toPortSpace(dstPorts, contractName, entry.getName(), true, warnings);
      if (!portSpace.isEmpty()) {
        matchExprs.add(AclLineMatchExprs.matchDstPort(portSpace));
      }
    }

    // Match on source ports - handle both single port and port range
    String srcPort = normalizeSinglePort(entry.getSourcePort());
    String srcFromPort = normalizeRangeEndpoint(entry.getSourceFromPort());
    String srcToPort = normalizeRangeEndpoint(entry.getSourceToPort());

    if (srcPort != null || (srcFromPort != null && srcToPort != null)) {
      List<String> srcPorts = new ArrayList<>();
      if (srcPort != null) {
        srcPorts.add(srcPort);
      } else if (srcFromPort != null && srcToPort != null) {
        srcPorts.add(srcFromPort + "-" + srcToPort);
      }
      IntegerSpace portSpace =
          toPortSpace(srcPorts, contractName, entry.getName(), false, warnings);
      if (!portSpace.isEmpty()) {
        matchExprs.add(AclLineMatchExprs.matchSrcPort(portSpace));
      }
    }

    // Handle ARP opcode if specified
    if (isMeaningfulArpOpcode(entry.getArpOpcode())) {
      warnings.redFlagf(
          "ARP opcode specified in contract %s filter %s entry %s: %s. ARP filtering has limited"
              + " effect in IP ACLs.",
          contractName, filterName, entry.getName(), entry.getArpOpcode());
    }

    // Handle stateful flag - warn that this may not be fully supported
    if (Boolean.TRUE.equals(entry.getStateful())) {
      warnings.redFlagf(
          "Stateful filtering specified in contract %s filter %s entry %s. Stateful filtering"
              + " may not be fully supported in ACL conversion.",
          contractName, filterName, entry.getName());
    }

    AclLineMatchExpr matchExpr =
        matchExprs.build().isEmpty()
            ? AclLineMatchExprs.TRUE
            : AclLineMatchExprs.and(matchExprs.build());

    String entryName = entry.getName() != null ? entry.getName() : "unnamed";
    ExprAclLine line =
        new ExprAclLine(
            action,
            matchExpr,
            String.format("Contract %s filter %s entry %s", contractName, filterName, entryName));

    lines.add(line);
    return lines.build();
  }

  /**
   * Converts an ACI protocol string to a Batfish IpProtocol match expression.
   *
   * <p>Supports protocol names (tcp, udp, icmp) and protocol numbers (6, 17, 1, etc.). Any valid IP
   * protocol number from 0-255 is supported.
   *
   * @param protocol The protocol string (e.g., "tcp", "udp", "icmp", "6", "17")
   * @param warnings Warnings container
   * @return The AclLineMatchExpr for matching the protocol, or null if protocol is null
   */
  private static @Nullable AclLineMatchExpr toIpProtocolMatchExpr(
      String protocol, Warnings warnings) {
    if (protocol == null) {
      return null;
    }
    String p = protocol.toLowerCase().trim();

    // Handle common protocol names
    switch (p) {
      case "tcp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP);
      case "udp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.UDP);
      case "icmp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.ICMP);
      case "ip":
      case "ipv4":
        return null; // No protocol filtering needed for "any IP"
      case "igmp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.IGMP);
      case "ipinip":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.IPINIP);
      case "gre":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.GRE);
      case "ospf":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.OSPF);
      case "pim":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.PIM);
      case "sctp":
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.SCTP);
      default:
        // Try to parse as protocol number
        try {
          int protoNum = Integer.parseInt(p);
          if (protoNum >= 0 && protoNum <= 255) {
            return AclLineMatchExprs.matchIpProtocol(protoNum);
          } else {
            warnings.redFlagf("Invalid IP protocol number: %s (must be 0-255)", protocol);
            return null;
          }
        } catch (NumberFormatException e) {
          // Unknown protocol name
          warnings.redFlagf("Unknown IP protocol: %s", protocol);
          return null;
        }
    }
  }

  private static boolean isMeaningfulArpOpcode(@Nullable String arpOpcode) {
    if (arpOpcode == null) {
      return false;
    }
    String normalized = arpOpcode.trim().toLowerCase();
    return !normalized.isEmpty() && !normalized.equals("unspecified") && !normalized.equals("any");
  }

  private static @Nullable String normalizeRangeEndpoint(@Nullable String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim().toLowerCase();
    if (normalized.isEmpty()
        || normalized.equals("unspecified")
        || normalized.equals("any")
        || normalized.equals("0")) {
      return null;
    }
    return value.trim();
  }

  private static @Nullable String normalizeSinglePort(@Nullable String value) {
    return normalizeRangeEndpoint(value);
  }

  /**
   * Converts ACI path attachments (fvRsPathAtt) to interface assignments.
   *
   * <p>Path attachments link Endpoint Groups to physical interfaces/ports.
   *
   * @param aciConfig The ACI configuration
   * @param interfaces Map of existing interfaces
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  private static void convertPathAttachments(
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Configuration c,
      Warnings warnings) {

    // Process EPGs to find their interface associations
    for (AciConfiguration.Epg epg : aciConfig.getEpgs().values()) {
      String bridgeDomainName = epg.getBridgeDomain();
      String epgDisplayName = epg.getName();
      String tenantName = epg.getTenant();
      String appProfileName = epg.getApplicationProfile();

      // Find the bridge domain to determine VLAN
      Integer vlanId = null;
      if (bridgeDomainName != null) {
        AciConfiguration.BridgeDomain bd = aciConfig.getBridgeDomains().get(bridgeDomainName);
        if (bd != null) {
          // Generate a VLAN ID from the BD name
          vlanId = Math.abs(bd.getName().hashCode() % 4094) + 1;
        }
      }

      // For each interface in the fabric nodes, check if it belongs to this EPG
      for (AciConfiguration.FabricNode node : aciConfig.getFabricNodes().values()) {
        if (node.getInterfaces() == null) {
          continue;
        }
        for (AciConfiguration.FabricNode.Interface iface : node.getInterfaces().values()) {
          if (epg.getName().equals(iface.getEpg())) {
            // This interface belongs to the EPG
            Interface batfishIface = interfaces.get(iface.getName());
            if (batfishIface == null) {
              warnings.redFlagf(
                  "Interface %s not found for EPG %s", iface.getName(), epg.getName());
              continue;
            }

            // Build EPG metadata description
            StringBuilder epgMetadata = new StringBuilder();
            epgMetadata.append("EPG: ").append(epgDisplayName);
            if (tenantName != null) {
              epgMetadata.append(" | Tenant: ").append(tenantName);
            }
            if (appProfileName != null) {
              epgMetadata.append(" | AppProfile: ").append(appProfileName);
            }
            if (bridgeDomainName != null) {
              epgMetadata.append(" | BridgeDomain: ").append(bridgeDomainName);
            }

            // Append EPG info to existing description
            String existingDesc = batfishIface.getDescription();
            if (existingDesc != null && !existingDesc.isEmpty()) {
              batfishIface.setDescription(existingDesc + " | " + epgMetadata.toString());
            } else {
              batfishIface.setDescription(epgMetadata.toString());
            }

            // Set VLAN based on EPG's bridge domain
            if (vlanId != null) {
              IntegerSpace newVlans =
                  IntegerSpace.builder()
                      .including(batfishIface.getAllowedVlans())
                      .including(vlanId)
                      .build();
              batfishIface.setAllowedVlans(newVlans);
              batfishIface.setNativeVlan(vlanId);
              batfishIface.setSwitchportMode(SwitchportMode.TRUNK);
              batfishIface.setSwitchport(true);
            }

            // Also check for explicit VLAN on interface
            if (iface.getVlan() != null) {
              try {
                int explicitVlan = Integer.parseInt(iface.getVlan());
                IntegerSpace newVlans =
                    IntegerSpace.builder()
                        .including(batfishIface.getAllowedVlans())
                        .including(explicitVlan)
                        .build();
                batfishIface.setAllowedVlans(newVlans);
                batfishIface.setNativeVlan(explicitVlan);
                batfishIface.setSwitchportMode(SwitchportMode.TRUNK);
                batfishIface.setSwitchport(true);
              } catch (NumberFormatException e) {
                warnings.redFlagf(
                    "Invalid VLAN for interface %s: %s", iface.getName(), iface.getVlan());
              }
            }

            // Apply EPG contract policy relationships as interface ACLs.
            applyEpgPolicies(epg, batfishIface, c, warnings);
          }
        }
      }
    }
  }

  private static void applyEpgPolicies(
      AciConfiguration.Epg epg, Interface iface, Configuration c, Warnings warnings) {
    List<String> incomingAclRefs =
        resolveContractAclRefs(
            epg.getConsumedContracts(),
            epg.getConsumedContractInterfaces(),
            epg.getTenant(),
            c,
            warnings,
            epg.getName(),
            "incoming",
            AclKind.CONTRACT);
    List<String> outgoingAclRefs =
        resolveContractAclRefs(
            epg.getProvidedContracts(),
            epg.getProvidedContractInterfaces(),
            epg.getTenant(),
            c,
            warnings,
            epg.getName(),
            "outgoing",
            AclKind.CONTRACT);
    List<String> tabooAclRefs =
        resolveContractAclRefs(
            epg.getProtectedByTaboos(),
            ImmutableList.of(),
            epg.getTenant(),
            c,
            warnings,
            epg.getName(),
            "taboo",
            AclKind.TABOO);

    if (!incomingAclRefs.isEmpty() || !tabooAclRefs.isEmpty()) {
      IpAccessList incomingFilter =
          buildEpgPolicyAcl(
              c, epg.getName(), "IN", incomingAclRefs, tabooAclRefs, /* defaultPermit= */ true);
      iface.setIncomingFilter(incomingFilter);
    }
    if (!outgoingAclRefs.isEmpty() || !tabooAclRefs.isEmpty()) {
      IpAccessList outgoingFilter =
          buildEpgPolicyAcl(
              c, epg.getName(), "OUT", outgoingAclRefs, tabooAclRefs, /* defaultPermit= */ true);
      iface.setOutgoingFilter(outgoingFilter);
    }
  }

  private static @Nonnull List<String> resolveContractAclRefs(
      @Nullable List<String> contractNames,
      @Nullable List<String> contractInterfaceNames,
      @Nullable String tenantName,
      Configuration c,
      Warnings warnings,
      String epgName,
      String direction,
      AclKind aclKind) {
    Set<String> aclRefs = new HashSet<>();
    if (contractNames != null) {
      for (String name : contractNames) {
        resolveContractAclRef(
            name, tenantName, c, aclRefs, warnings, epgName, direction, "contract", aclKind);
      }
    }
    if (contractInterfaceNames != null) {
      for (String name : contractInterfaceNames) {
        resolveContractAclRef(
            name,
            tenantName,
            c,
            aclRefs,
            warnings,
            epgName,
            direction,
            "contract-interface",
            aclKind);
      }
    }
    return ImmutableList.copyOf(aclRefs);
  }

  private static void resolveContractAclRef(
      @Nullable String rawName,
      @Nullable String tenantName,
      Configuration c,
      Set<String> aclRefs,
      Warnings warnings,
      String epgName,
      String direction,
      String refType,
      AclKind aclKind) {
    if (rawName == null || rawName.isEmpty()) {
      return;
    }
    List<String> candidateContractNames = new ArrayList<>();
    candidateContractNames.add(rawName);
    if (tenantName != null && !rawName.contains(":")) {
      candidateContractNames.add(tenantName + ":" + rawName);
    }
    for (String contractName : candidateContractNames) {
      String aclName =
          aclKind == AclKind.TABOO
              ? getTabooAclName(contractName)
              : getContractAclName(contractName);
      if (c.getIpAccessLists().containsKey(aclName)) {
        aclRefs.add(aclName);
        return;
      }
    }
    warnings.redFlagf(
        "Could not resolve %s reference '%s' for EPG %s (%s direction) to a known contract ACL",
        refType, rawName, epgName, direction);
  }

  private static @Nonnull IpAccessList buildEpgPolicyAcl(
      Configuration c,
      String epgName,
      String direction,
      List<String> permitAclRefs,
      List<String> denyAclRefs,
      boolean defaultPermit) {
    String aclName =
        String.format("~EPG_POLICY~%s~%s", epgName.replaceAll("[^A-Za-z0-9:_-]", "_"), direction);

    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();
    for (String denyAclRef : denyAclRefs) {
      lines.add(
          new ExprAclLine(
              LineAction.DENY,
              new PermittedByAcl(denyAclRef),
              String.format("Denied by taboo policy ACL %s", denyAclRef)));
    }
    for (String permitAclRef : permitAclRefs) {
      lines.add(
          new ExprAclLine(
              LineAction.PERMIT,
              new PermittedByAcl(permitAclRef),
              String.format("Permitted by contract policy ACL %s", permitAclRef)));
    }
    lines.add(
        new ExprAclLine(
            defaultPermit ? LineAction.PERMIT : LineAction.DENY,
            AclLineMatchExprs.TRUE,
            defaultPermit
                ? String.format("Default permit for EPG %s %s policy", epgName, direction)
                : String.format("Default deny for EPG %s %s policy", epgName, direction)));

    IpAccessList acl =
        IpAccessList.builder().setOwner(c).setName(aclName).setLines(lines.build()).build();
    c.getIpAccessLists().put(aclName, acl);
    return acl;
  }

  private enum AclKind {
    CONTRACT,
    TABOO
  }

  /**
   * Converts ACI L3Out configurations to Batfish routing configuration.
   *
   * <p>L3Out in ACI defines external connectivity including BGP peering, static routes, OSPF, and
   * external EPGs (L3ExtEpg). This method:
   *
   * <ul>
   *   <li>Converts BGP peers defined in L3Out to Batfish BgpProcess with BgpActivePeerConfig
   *   <li>Converts static routes to Batfish StaticRoute objects
   *   <li>Converts OSPF areas to Batfish OspfProcess (when OSPF support is available)
   *   <li>Handles external EPGs (L3ExtEpg) for external endpoint connectivity
   * </ul>
   *
   * @param node The fabric node
   * @param aciConfig The ACI configuration
   * @param interfaces Map of existing interfaces
   * @param vrf The VRF to add routes to
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  private static void convertL3Outs(
      AciConfiguration.FabricNode node,
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {
    // Get L3Outs from the ACI configuration
    Map<String, AciConfiguration.L3Out> l3Outs = aciConfig.getL3Outs();
    if (l3Outs == null || l3Outs.isEmpty()) {
      return;
    }

    // Track if we've created any routing processes and which VRF should receive it
    boolean hasBgpProcess = false;
    BgpProcess.Builder bgpProcessBuilder = null;
    Vrf bgpTargetVrf = null; // Track which VRF should get the BGP process

    // Process each L3Out
    for (AciConfiguration.L3Out l3Out : l3Outs.values()) {
      String l3OutName = l3Out.getName();
      String vrfName = l3Out.getVrf();

      // Find the target VRF for this L3Out
      Vrf targetVrf = vrf;
      if (vrfName != null) {
        Vrf foundVrf = c.getVrfs().get(vrfName);
        if (foundVrf != null) {
          targetVrf = foundVrf;
        } else {
          warnings.redFlagf("VRF %s not found for L3Out %s, using default VRF", vrfName, l3OutName);
        }
      }

      // Convert BGP peers
      if (l3Out.getBgpPeers() != null && !l3Out.getBgpPeers().isEmpty()) {
        if (!hasBgpProcess) {
          bgpTargetVrf = targetVrf; // Remember the VRF for this L3Out
          bgpProcessBuilder =
              convertBgpPeers(l3Out, node, aciConfig, interfaces, targetVrf, c, warnings);
          hasBgpProcess = bgpProcessBuilder != null;
        } else {
          // Add additional peers to existing BGP process
          addBgpPeersToProcess(bgpProcessBuilder, l3Out, node, interfaces, targetVrf, c, warnings);
        }
      }

      // Convert static routes
      if (l3Out.getStaticRoutes() != null && !l3Out.getStaticRoutes().isEmpty()) {
        convertStaticRoutes(l3Out, node, interfaces, targetVrf, c, warnings);
      }

      // Convert OSPF configuration
      if (l3Out.getOspfConfig() != null) {
        convertOspfConfig(
            l3Out.getOspfConfig(), l3OutName, node, interfaces, targetVrf, c, warnings);
      }

      // Convert external EPGs (L3ExtEpg)
      if (l3Out.getExternalEpgs() != null && !l3Out.getExternalEpgs().isEmpty()) {
        convertExternalEpgs(l3Out, node, interfaces, targetVrf, c, warnings);
      }
    }

    // Set the BGP process on the VRF if we created one
    if (hasBgpProcess && bgpProcessBuilder != null && bgpTargetVrf != null) {
      BgpProcess bgpProcess = bgpProcessBuilder.build();
      bgpTargetVrf.setBgpProcess(bgpProcess);
    }
  }

  /**
   * Converts L2Out configurations to VLAN interfaces for external Layer 2 connectivity.
   *
   * <p>L2Out in ACI provides external Layer 2 connectivity. This method creates VLAN interfaces
   * representing the L2Out connections with appropriate VLAN IDs derived from the encapsulation.
   *
   * @param node The fabric node
   * @param aciConfig The ACI configuration
   * @param interfaces Map of existing interfaces
   * @param defaultVrf The default VRF
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  private static void convertL2Outs(
      AciConfiguration.FabricNode node,
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Vrf defaultVrf,
      Configuration c,
      Warnings warnings) {
    Map<String, AciConfiguration.L2Out> l2Outs = aciConfig.getL2Outs();
    if (l2Outs == null || l2Outs.isEmpty()) {
      return;
    }

    for (Map.Entry<String, AciConfiguration.L2Out> entry : l2Outs.entrySet()) {
      AciConfiguration.L2Out l2Out = entry.getValue();
      String l2OutName = l2Out.getName();
      if (l2OutName == null || l2OutName.isEmpty()) {
        continue;
      }

      // Determine the VLAN ID from encapsulation
      int vlanId = parseL2OutVlanId(l2Out, warnings);
      if (vlanId <= 0) {
        continue;
      }

      // Determine the target VRF from the bridge domain
      Vrf targetVrf = defaultVrf;
      String bdName = l2Out.getBridgeDomain();
      if (bdName != null && !bdName.isEmpty()) {
        AciConfiguration.BridgeDomain bd = aciConfig.getBridgeDomains().get(bdName);
        if (bd != null && bd.getVrf() != null) {
          Vrf foundVrf = c.getVrfs().get(bd.getVrf());
          if (foundVrf != null) {
            targetVrf = foundVrf;
          }
        }
      }

      // Create interface name
      String interfaceName = "L2Out-" + l2OutName;

      // Skip if interface already exists
      if (interfaces.containsKey(interfaceName)) {
        continue;
      }

      // Build the L2Out interface
      Interface.Builder l2OutInterface =
          Interface.builder()
              .setName(interfaceName)
              .setType(InterfaceType.VLAN)
              .setOwner(c)
              .setVrf(targetVrf)
              .setAdminUp(true)
              .setMtu(DEFAULT_MTU)
              .setVlan(vlanId)
              .setHumanName(String.format("L2Out %s (VLAN %d)", l2OutName, vlanId))
              .setDescription(
                  l2Out.getDescription() != null
                      ? l2Out.getDescription()
                      : String.format("L2Out %s for external L2 connectivity", l2OutName))
              .setDeclaredNames(ImmutableList.of(interfaceName));

      interfaces.put(interfaceName, l2OutInterface.build());
    }
  }

  /**
   * Parses the VLAN ID from an L2Out encapsulation.
   *
   * <p>Supports formats: "vlan-100", "vxlan-5000"
   *
   * @param l2Out The L2Out configuration
   * @param warnings Warnings container
   * @return The VLAN ID (1-4095), or 0 if invalid
   */
  private static int parseL2OutVlanId(AciConfiguration.L2Out l2Out, Warnings warnings) {
    String encap = l2Out.getEncapsulation();
    if (encap == null || encap.isEmpty()) {
      // Generate VLAN ID from L2Out name hash if no encapsulation
      return Math.abs(l2Out.getName().hashCode() % 4094) + 1;
    }

    encap = encap.toLowerCase();

    // Parse VLAN format: "vlan-100"
    if (encap.startsWith("vlan-")) {
      try {
        int vlan = Integer.parseInt(encap.substring(5));
        if (vlan >= 1 && vlan <= 4095) {
          return vlan;
        }
      } catch (NumberFormatException e) {
        warnings.redFlagf("Invalid VLAN encapsulation '%s' for L2Out %s", encap, l2Out.getName());
      }
    }

    // Parse VXLAN format: "vxlan-5000" -> map to VLAN (VNI % 4094 + 1)
    if (encap.startsWith("vxlan-")) {
      try {
        int vni = Integer.parseInt(encap.substring(6));
        if (vni >= 1) {
          return (vni % 4094) + 1;
        }
      } catch (NumberFormatException e) {
        warnings.redFlagf("Invalid VXLAN encapsulation '%s' for L2Out %s", encap, l2Out.getName());
      }
    }

    return 0;
  }

  /**
   * Converts BGP peers from an L3Out to a Batfish BgpProcess.
   *
   * @param l3Out The L3Out configuration
   * @param node The fabric node
   * @param aciConfig The ACI configuration
   * @param interfaces Map of existing interfaces
   * @param vrf The VRF for BGP
   * @param c The Batfish configuration
   * @param warnings Warnings container
   * @return A BgpProcess.Builder with the BGP configuration, or null if no valid BGP configuration
   */
  private static @Nullable BgpProcess.Builder convertBgpPeers(
      AciConfiguration.L3Out l3Out,
      AciConfiguration.FabricNode node,
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {
    if (l3Out.getBgpPeers() == null || l3Out.getBgpPeers().isEmpty()) {
      return null;
    }

    // Get the BGP process configuration
    AciConfiguration.BgpProcess bgpProcessConfig = l3Out.getBgpProcess();
    if (bgpProcessConfig == null) {
      bgpProcessConfig = new AciConfiguration.BgpProcess();
    }

    // Parse router ID if configured
    Ip routerId = null;
    if (bgpProcessConfig.getRouterId() != null) {
      try {
        routerId = Ip.parse(bgpProcessConfig.getRouterId());
      } catch (IllegalArgumentException e) {
        warnings.redFlagf(
            "Invalid router ID %s for BGP process in L3Out %s",
            bgpProcessConfig.getRouterId(), l3Out.getName());
      }
    }
    // Default to loopback address if no router ID configured
    if (routerId == null) {
      routerId = Ip.AUTO;
    }

    // Get the local AS from the BGP process or from the first peer
    Long localAs = bgpProcessConfig.getAs();
    if (localAs == null && l3Out.getBgpPeers() != null && !l3Out.getBgpPeers().isEmpty()) {
      String firstPeerLocalAs = l3Out.getBgpPeers().get(0).getLocalAs();
      if (firstPeerLocalAs != null) {
        try {
          localAs = Long.parseLong(firstPeerLocalAs);
        } catch (NumberFormatException e) {
          warnings.redFlagf(
              "Invalid local AS '%s' in BGP peer for L3Out %s, ignoring",
              firstPeerLocalAs, l3Out.getName());
        }
      }
    }

    // Build the BGP process
    BgpProcess.Builder bgpBuilder =
        BgpProcess.builder()
            .setRouterId(routerId)
            .setEbgpAdminCost(
                bgpProcessConfig.getEbgpAdminCost() != null
                    ? bgpProcessConfig.getEbgpAdminCost()
                    : DEFAULT_EBGP_ADMIN_COST)
            .setIbgpAdminCost(
                bgpProcessConfig.getIbgpAdminCost() != null
                    ? bgpProcessConfig.getIbgpAdminCost()
                    : DEFAULT_IBGP_ADMIN_COST)
            .setLocalAdminCost(
                bgpProcessConfig.getVrfAdminCost() != null
                    ? bgpProcessConfig.getVrfAdminCost()
                    : DEFAULT_LOCAL_BGP_WEIGHT);

    // Set the VRF for the BGP process
    bgpBuilder.setVrf(vrf);

    // Add each BGP peer
    for (AciConfiguration.BgpPeer bgpPeer : l3Out.getBgpPeers()) {
      convertBgpPeer(bgpPeer, l3Out, node, interfaces, vrf, localAs, c, warnings);
    }

    return bgpBuilder;
  }

  /**
   * Converts a single BGP peer from an L3Out to a Batfish BgpActivePeerConfig.
   *
   * <p>The peer is associated with the BGP process through the builder's setBgpProcess method.
   *
   * @param bgpPeer The BGP peer configuration
   * @param l3Out The parent L3Out
   * @param node The fabric node
   * @param interfaces Map of existing interfaces
   * @param vrf The VRF for BGP
   * @param localAs The local AS number for the peer
   * @param c The Batfish configuration
   * @param warnings Warnings container
   * @return A BgpActivePeerConfig, or null if conversion fails
   */
  private static @Nullable BgpActivePeerConfig convertBgpPeer(
      AciConfiguration.BgpPeer bgpPeer,
      AciConfiguration.L3Out l3Out,
      AciConfiguration.FabricNode node,
      Map<String, Interface> interfaces,
      Vrf vrf,
      @Nullable Long localAs,
      Configuration c,
      Warnings warnings) {
    String peerAddressStr = bgpPeer.getPeerAddress();
    if (peerAddressStr == null) {
      warnings.redFlagf("BGP peer in L3Out %s has no peer address", l3Out.getName());
      return null;
    }

    Ip peerAddress;
    try {
      peerAddress = Ip.parse(peerAddressStr);
    } catch (IllegalArgumentException e) {
      warnings.redFlagf("Invalid BGP peer address %s in L3Out %s", peerAddressStr, l3Out.getName());
      return null;
    }

    // Build the BGP peer config
    BgpActivePeerConfig.Builder peerBuilder =
        BgpActivePeerConfig.builder().setPeerAddress(peerAddress);

    // Set remote AS
    if (bgpPeer.getRemoteAs() != null) {
      try {
        long remoteAs = Long.parseLong(bgpPeer.getRemoteAs());
        peerBuilder.setRemoteAsns(LongSpace.of(remoteAs));
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid remote AS %s for BGP peer %s in L3Out %s",
            bgpPeer.getRemoteAs(), peerAddressStr, l3Out.getName());
      }
    }

    // Set local AS if specified
    if (bgpPeer.getLocalAs() != null) {
      try {
        long peerLocalAs = Long.parseLong(bgpPeer.getLocalAs());
        peerBuilder.setLocalAs(peerLocalAs);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid local AS %s for BGP peer %s in L3Out %s",
            bgpPeer.getLocalAs(), peerAddressStr, l3Out.getName());
      }
    } else if (localAs != null) {
      // Use the process-level local AS if peer doesn't override
      peerBuilder.setLocalAs(localAs);
    }

    // Determine local IP (update source)
    Ip localIp = determineBgpLocalIp(bgpPeer, l3Out, node, interfaces, vrf, warnings);
    if (localIp != null) {
      peerBuilder.setLocalIp(localIp);
    }

    // Set BGP password if configured
    if (bgpPeer.getPassword() != null) {
      // Note: Batfish stores MD5 password, but for security we just note it exists
      // Actual password validation would require additional processing
    }

    // Set description
    peerBuilder.setDescription(
        bgpPeer.getDescription() != null
            ? bgpPeer.getDescription()
            : String.format("BGP peer from L3Out %s", l3Out.getName()));

    // Set EBGP multihop if configured
    if (bgpPeer.getEbgpMultihop() != null && bgpPeer.getEbgpMultihop()) {
      peerBuilder.setEbgpMultihop(true);
    }

    // Set TTL if configured for multihop
    if (bgpPeer.getTtl() != null) {
      // TTL is typically set via ebgp-multihop ttl
    }

    // Configure address families (IPv4 unicast is the primary)
    Ipv4UnicastAddressFamily.Builder afBuilder = Ipv4UnicastAddressFamily.builder();

    // Create import policy
    String importPolicyName = createBgpImportPolicy(bgpPeer, l3Out, c, warnings);
    if (importPolicyName != null) {
      afBuilder.setImportPolicy(importPolicyName);
    }

    // Create export policy
    String exportPolicyName = createBgpExportPolicy(bgpPeer, l3Out, c, warnings);
    if (exportPolicyName != null) {
      afBuilder.setExportPolicy(exportPolicyName);
    }

    // Set route reflector client if configured
    if (bgpPeer.getRouteReflectorClient() != null && bgpPeer.getRouteReflectorClient()) {
      afBuilder.setRouteReflectorClient(true);
    }

    // Set next-hop-self if configured
    if (bgpPeer.getNextHopSelf() != null && bgpPeer.getNextHopSelf()) {
      // Next-hop-self is handled in the export policy
    }

    peerBuilder.setIpv4UnicastAddressFamily(afBuilder.build());

    // Associate the peer with the BGP process and VRF
    // The peer will be added to the BGP process when built
    BgpProcess bgpProcess = vrf.getBgpProcess();
    if (bgpProcess != null) {
      peerBuilder.setBgpProcess(bgpProcess);
    }

    return peerBuilder.build();
  }

  /**
   * Adds BGP peers from an L3Out to an existing BgpProcess.Builder.
   *
   * @param bgpProcessBuilder The existing BgpProcess.Builder
   * @param l3Out The L3Out configuration
   * @param node The fabric node
   * @param interfaces Map of existing interfaces
   * @param vrf The VRF for BGP
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  private static void addBgpPeersToProcess(
      BgpProcess.Builder bgpProcessBuilder,
      AciConfiguration.L3Out l3Out,
      AciConfiguration.FabricNode node,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {
    if (l3Out.getBgpPeers() == null || l3Out.getBgpPeers().isEmpty()) {
      return;
    }

    // Get the local AS from the BGP process configuration
    Long localAs = null;
    if (l3Out.getBgpProcess() != null && l3Out.getBgpProcess().getAs() != null) {
      localAs = l3Out.getBgpProcess().getAs();
    }

    for (AciConfiguration.BgpPeer bgpPeer : l3Out.getBgpPeers()) {
      convertBgpPeer(bgpPeer, l3Out, node, interfaces, vrf, localAs, c, warnings);
    }
  }

  /**
   * Determines the local IP address to use for BGP peering.
   *
   * @param bgpPeer The BGP peer configuration
   * @param l3Out The parent L3Out
   * @param node The fabric node
   * @param interfaces Map of existing interfaces
   * @param vrf The VRF
   * @param warnings Warnings container
   * @return The local IP address, or null if it cannot be determined
   */
  private static @Nullable Ip determineBgpLocalIp(
      AciConfiguration.BgpPeer bgpPeer,
      AciConfiguration.L3Out l3Out,
      AciConfiguration.FabricNode node,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Warnings warnings) {
    // Check if update source interface is specified
    String updateSourceInterface = bgpPeer.getUpdateSourceInterface();
    if (updateSourceInterface != null) {
      Interface iface = interfaces.get(updateSourceInterface);
      if (iface != null && iface.getConcreteAddress() != null) {
        return iface.getConcreteAddress().getIp();
      }
      warnings.redFlagf(
          "Update source interface %s not found or has no IP address for BGP peer %s in L3Out %s",
          updateSourceInterface, bgpPeer.getPeerAddress(), l3Out.getName());
      return null;
    }

    // Try to find an interface with an IP in the same subnet as the peer
    Ip peerAddress;
    try {
      peerAddress = Ip.parse(bgpPeer.getPeerAddress());
    } catch (IllegalArgumentException e) {
      // Invalid peer address - can't find matching subnet
      return null;
    }
    for (Interface iface : interfaces.values()) {
      if (iface.getConcreteAddress() != null) {
        Prefix subnet = iface.getConcreteAddress().getPrefix();
        if (subnet.containsIp(peerAddress)) {
          return iface.getConcreteAddress().getIp();
        }
      }
    }

    // Default to loopback if available
    Interface loopback = interfaces.get("loopback0");
    if (loopback != null && loopback.getConcreteAddress() != null) {
      return loopback.getConcreteAddress().getIp();
    }

    return null;
  }

  /**
   * Creates an import policy for a BGP peer.
   *
   * @param bgpPeer The BGP peer configuration
   * @param l3Out The parent L3Out
   * @param c The Batfish configuration
   * @param warnings Warnings container
   * @return The name of the import policy, or null if no special policy is needed
   */
  private static @Nullable String createBgpImportPolicy(
      AciConfiguration.BgpPeer bgpPeer,
      AciConfiguration.L3Out l3Out,
      Configuration c,
      Warnings warnings) {
    String policyName =
        String.format("~BGP_IMPORT~%s~%s", l3Out.getName(), bgpPeer.getPeerAddress());

    RoutingPolicy.Builder policyBuilder = RoutingPolicy.builder().setName(policyName).setOwner(c);

    // Set local preference if configured
    if (bgpPeer.getLocalPreference() != null) {
      try {
        int localPref = Integer.parseInt(bgpPeer.getLocalPreference());
        policyBuilder.addStatement(new SetLocalPreference(new LiteralLong(localPref)));
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid local preference %s for BGP peer %s in L3Out %s",
            bgpPeer.getLocalPreference(), bgpPeer.getPeerAddress(), l3Out.getName());
      }
    }

    // Set origin if configured (default is IGP for most cases)
    policyBuilder.addStatement(new SetOrigin(new LiteralOrigin(OriginType.IGP, null)));

    // Apply route map if specified
    if (bgpPeer.getImportRouteMap() != null) {
      // Create an If statement that calls the route map
      List<Statement> trueStatements = ImmutableList.of(Statements.ExitAccept.toStaticStatement());
      List<Statement> falseStatements = ImmutableList.of(Statements.ExitReject.toStaticStatement());
      If routeMapIf =
          new If(
              "Apply import route-map " + bgpPeer.getImportRouteMap(),
              new CallExpr(bgpPeer.getImportRouteMap()),
              trueStatements,
              falseStatements);
      policyBuilder.addStatement(routeMapIf);
    } else {
      // Default to accept if no route map
      policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());
    }

    // Always create the policy
    RoutingPolicy policy = policyBuilder.build();
    c.getRoutingPolicies().put(policyName, policy);
    return policyName;
  }

  /**
   * Creates an export policy for a BGP peer.
   *
   * @param bgpPeer The BGP peer configuration
   * @param l3Out The parent L3Out
   * @param c The Batfish configuration
   * @param warnings Warnings container
   * @return The name of the export policy, or null if no special policy is needed
   */
  private static @Nullable String createBgpExportPolicy(
      AciConfiguration.BgpPeer bgpPeer,
      AciConfiguration.L3Out l3Out,
      Configuration c,
      Warnings warnings) {
    String policyName =
        String.format("~BGP_EXPORT~%s~%s", l3Out.getName(), bgpPeer.getPeerAddress());

    RoutingPolicy.Builder policyBuilder = RoutingPolicy.builder().setName(policyName).setOwner(c);

    // Track if we have any special configurations
    boolean hasSpecialConfig = false;

    // Set next-hop-self if configured
    if (bgpPeer.getNextHopSelf() != null && bgpPeer.getNextHopSelf()) {
      policyBuilder.addStatement(new SetNextHop(SelfNextHop.getInstance()));
      hasSpecialConfig = true;
    }

    // Set communities if configured
    if (bgpPeer.getSendCommunities() != null && bgpPeer.getSendCommunities()) {
      // Communities are set via route-maps or neighbor policies
      // This is a placeholder for future community support
      hasSpecialConfig = true;
    }

    // Apply route map if specified
    if (bgpPeer.getExportRouteMap() != null) {
      hasSpecialConfig = true;
      // Create an If statement that calls the route map
      List<Statement> trueStatements = ImmutableList.of(Statements.ExitAccept.toStaticStatement());
      List<Statement> falseStatements = ImmutableList.of(Statements.ExitReject.toStaticStatement());
      If routeMapIf =
          new If(
              "Apply export route-map " + bgpPeer.getExportRouteMap(),
              new CallExpr(bgpPeer.getExportRouteMap()),
              trueStatements,
              falseStatements);
      policyBuilder.addStatement(routeMapIf);
    } else {
      // Default to accept if no route map
      policyBuilder.addStatement(Statements.ExitAccept.toStaticStatement());
    }

    // Only create the policy if we have special configurations or no route map
    RoutingPolicy policy = policyBuilder.build();
    c.getRoutingPolicies().put(policyName, policy);
    return policyName;
  }

  /**
   * Converts static routes from an L3Out to Batfish StaticRoute objects.
   *
   * <p>In Cisco ACI, static routes are configured within L3Out (Layer 3 Outside) configurations
   * using the following MO (Managed Object) classes:
   *
   * <ul>
   *   <li>fvRsPathAtt - Relation from L3Out to external EPG (defines the external interface)
   *   <li>fvStaticRoute - Static route configuration with prefix, next hop, and attributes
   *   <li>ipRouteP - IP route policy configuration (for more complex routing scenarios)
   * </ul>
   *
   * <p>This method processes each static route defined in the L3Out and converts it to a Batfish
   * StaticRoute with the following mappings:
   *
   * <ul>
   *   <li>prefix - Network prefix (e.g., "10.0.0.0/24")
   *   <li>nextHop - Next hop IP address
   *   <li>nextHopInterface - Outgoing interface (if specified)
   *   <li>administrativeDistance - Route preference (default: 1)
   *   <li>tag - Route tag for route filtering and policy
   *   <li>track - Track object for high availability monitoring
   * </ul>
   *
   * @param l3Out The L3Out configuration containing static routes
   * @param node The fabric node (for context)
   * @param interfaces Map of existing interfaces on the node
   * @param vrf The VRF to add static routes to
   * @param c The Batfish configuration
   * @param warnings Warnings container for conversion issues
   */
  private static void convertStaticRoutes(
      AciConfiguration.L3Out l3Out,
      AciConfiguration.FabricNode node,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {
    if (l3Out.getStaticRoutes() == null || l3Out.getStaticRoutes().isEmpty()) {
      return;
    }

    for (AciConfiguration.StaticRoute aciStaticRoute : l3Out.getStaticRoutes()) {
      StaticRoute staticRoute =
          convertStaticRoute(aciStaticRoute, l3Out, node, interfaces, warnings);
      if (staticRoute != null) {
        vrf.getStaticRoutes().add(staticRoute);
      }
    }
  }

  /**
   * Converts a single ACI static route to a Batfish StaticRoute.
   *
   * <p>Handles the following ACI static route attributes:
   *
   * <ul>
   *   <li>Prefix (required) - Network destination prefix
   *   <li>Next Hop IP (required if no interface) - IP address of next hop router
   *   <li>Next Hop Interface (required if no IP) - Outgoing interface name
   *   <li>Administrative Distance (optional) - Route preference, default 1
   *   <li>Tag (optional) - Route tag for policy-based routing
   *   <li>Track (optional) - SLA/track object for high availability
   * </ul>
   *
   * @param aciStaticRoute The ACI static route configuration
   * @param l3Out The parent L3Out configuration
   * @param node The fabric node (for context)
   * @param interfaces Map of existing interfaces for validation
   * @param warnings Warnings container for conversion issues
   * @return A Batfish StaticRoute, or null if conversion fails
   */
  private static @Nullable StaticRoute convertStaticRoute(
      AciConfiguration.StaticRoute aciStaticRoute,
      AciConfiguration.L3Out l3Out,
      AciConfiguration.FabricNode node,
      Map<String, Interface> interfaces,
      Warnings warnings) {
    String prefixStr = aciStaticRoute.getPrefix();
    if (prefixStr == null) {
      warnings.redFlagf("Static route in L3Out %s has no prefix", l3Out.getName());
      return null;
    }

    Prefix prefix;
    try {
      prefix = Prefix.parse(prefixStr);
    } catch (IllegalArgumentException e) {
      warnings.redFlagf(
          "Invalid prefix %s for static route in L3Out %s", prefixStr, l3Out.getName());
      return null;
    }

    StaticRoute.Builder routeBuilder = StaticRoute.builder().setNetwork(prefix);

    // Track if we have a valid next hop (either IP or interface must be specified)
    boolean hasNextHopIp = false;
    boolean hasNextHopInterface = false;

    // Set next hop IP
    String nextHopStr = aciStaticRoute.getNextHop();
    if (nextHopStr != null) {
      try {
        Ip nextHop = Ip.parse(nextHopStr);
        routeBuilder.setNextHopIp(nextHop);
        hasNextHopIp = true;
      } catch (IllegalArgumentException e) {
        warnings.redFlagf(
            "Invalid next hop %s for static route %s in L3Out %s",
            nextHopStr, prefixStr, l3Out.getName());
      }
    }

    // Set next hop interface if specified
    String nextHopInterface = aciStaticRoute.getNextHopInterface();
    if (nextHopInterface != null) {
      // Verify interface exists
      if (interfaces.containsKey(nextHopInterface)) {
        routeBuilder.setNextHopInterface(nextHopInterface);
        hasNextHopInterface = true;
      } else {
        warnings.redFlagf(
            "Next hop interface %s not found for static route %s in L3Out %s",
            nextHopInterface, prefixStr, l3Out.getName());
      }
    }

    // A static route must have at least a next hop IP or interface
    if (!hasNextHopIp && !hasNextHopInterface) {
      warnings.redFlagf(
          "Static route %s in L3Out %s has no valid next hop (missing both IP and interface)",
          prefixStr, l3Out.getName());
      return null;
    }

    // Set administrative distance (default to 1 for static routes)
    // Note: ACI default is typically 1, but can be configured per-route
    int adminDist = 1; // Default administrative distance for static routes
    if (aciStaticRoute.getAdministrativeDistance() != null) {
      try {
        adminDist = Integer.parseInt(aciStaticRoute.getAdministrativeDistance());
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid administrative distance %s for static route %s in L3Out %s, using default (1)",
            aciStaticRoute.getAdministrativeDistance(), prefixStr, l3Out.getName());
      }
    }
    routeBuilder.setAdmin(adminDist);

    // Set route tag if specified (used for route filtering and policy)
    if (aciStaticRoute.getTag() != null) {
      try {
        long tag = Long.parseLong(aciStaticRoute.getTag());
        routeBuilder.setTag(tag);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid tag %s for static route %s in L3Out %s",
            aciStaticRoute.getTag(), prefixStr, l3Out.getName());
      }
    }

    // Set track object if specified (for high availability / SLA monitoring)
    if (aciStaticRoute.getTrack() != null) {
      // Track can be a string identifier or numeric, store as-is
      routeBuilder.setTrack(aciStaticRoute.getTrack());
    }

    return routeBuilder.build();
  }

  /**
   * Converts OSPF configuration from an L3Out to Batfish OspfProcess.
   *
   * <p>Creates an OSPF process with areas and applies interface settings.
   *
   * @param ospfConfig The OSPF configuration
   * @param l3OutName The L3Out name
   * @param node The fabric node
   * @param interfaces Map of existing interfaces
   * @param vrf The VRF for OSPF
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  private static void convertOspfConfig(
      AciConfiguration.OspfConfig ospfConfig,
      String l3OutName,
      AciConfiguration.FabricNode node,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {

    String processId = ospfConfig.getProcessId();
    if (processId == null || processId.isEmpty()) {
      processId = l3OutName;
    }

    // Infer router ID from node interfaces or use a default
    Ip routerId = inferOspfRouterId(node, interfaces, l3OutName, warnings);

    // Build areas map
    ImmutableSortedMap.Builder<Long, OspfArea> areasBuilder = ImmutableSortedMap.naturalOrder();

    if (ospfConfig.getAreas() != null) {
      for (AciConfiguration.OspfArea aciArea : ospfConfig.getAreas().values()) {
        Long areaNum = parseAreaId(aciArea.getAreaId());
        if (areaNum == null) {
          warnings.redFlagf(
              "Invalid OSPF area ID %s in L3Out %s, skipping area", aciArea.getAreaId(), l3OutName);
          continue;
        }

        OspfArea.Builder areaBuilder = OspfArea.builder().setNumber(areaNum);

        // Set area type (stub, nssa, or regular)
        String areaType = aciArea.getAreaType();
        if (areaType != null) {
          switch (areaType.toLowerCase()) {
            case "stub":
              areaBuilder.setStubType(StubType.STUB);
              break;
            case "nssa":
              areaBuilder.setStubType(StubType.NSSA);
              break;
            default:
              // Regular area - no special stub settings
              areaBuilder.setNonStub();
              break;
          }
        }

        areasBuilder.put(areaNum, areaBuilder.build());
      }
    }

    // If no areas defined, create a default area 0
    if (areasBuilder.build().isEmpty()) {
      Long defaultArea = parseAreaId(ospfConfig.getAreaId());
      if (defaultArea == null) {
        defaultArea = 0L; // Default to backbone area
      }
      areasBuilder.put(defaultArea, OspfArea.builder().setNumber(defaultArea).build());
    }

    // Build the OSPF process
    OspfProcess ospfProcess =
        OspfProcess.builder()
            .setProcessId(processId)
            .setRouterId(routerId)
            .setReferenceBandwidth(100.0) // 100 Mbps default reference bandwidth
            .setAreas(areasBuilder.build())
            .build();

    // Add OSPF process to VRF
    vrf.setOspfProcesses(ImmutableList.of(ospfProcess).stream());

    // Apply OSPF interface settings to L3Out interfaces
    applyOspfInterfaceSettings(ospfConfig, l3OutName, interfaces, processId, warnings);
  }

  /**
   * Infers an OSPF router ID from node interfaces.
   *
   * @param node The fabric node
   * @param interfaces Map of interfaces
   * @param l3OutName The L3Out name for warning messages
   * @param warnings Warnings container
   * @return The inferred router ID
   */
  private static Ip inferOspfRouterId(
      AciConfiguration.FabricNode node,
      Map<String, Interface> interfaces,
      String l3OutName,
      Warnings warnings) {

    // Try to find an interface with an IP address to use as router ID
    for (Interface iface : interfaces.values()) {
      if (iface.getAllAddresses().isEmpty()) {
        continue;
      }
      InterfaceAddress addr = iface.getAllAddresses().iterator().next();
      if (addr instanceof ConcreteInterfaceAddress) {
        Ip ip = ((ConcreteInterfaceAddress) addr).getIp();
        if (!ip.equals(Ip.ZERO)) {
          return ip;
        }
      }
    }

    // Fallback: generate a pseudo-router ID based on node ID
    String nodeId = node.getNodeId();
    if (nodeId != null && !nodeId.isEmpty()) {
      try {
        int id = Integer.parseInt(nodeId.replaceAll("[^0-9]", ""));
        // Generate a pseudo-IP in the 0.0.0.x range
        return Ip.create(id & 0xFF);
      } catch (NumberFormatException e) {
        // Fall through to default
      }
    }

    warnings.redFlagf("Could not infer OSPF router ID for L3Out %s, using 0.0.0.1", l3OutName);
    return Ip.create(1);
  }

  /**
   * Applies OSPF interface settings to interfaces.
   *
   * @param ospfConfig The OSPF configuration
   * @param l3OutName The L3Out name
   * @param interfaces Map of interfaces
   * @param processId The OSPF process ID
   * @param warnings Warnings container
   */
  private static void applyOspfInterfaceSettings(
      AciConfiguration.OspfConfig ospfConfig,
      String l3OutName,
      Map<String, Interface> interfaces,
      String processId,
      Warnings warnings) {

    if (ospfConfig.getOspfInterfaces() == null) {
      return;
    }

    for (AciConfiguration.OspfInterface ospfIface : ospfConfig.getOspfInterfaces()) {
      String ifaceName = ospfIface.getName();
      Interface batfishIface = interfaces.get(ifaceName);

      if (batfishIface == null) {
        // Try to find interface with L3Out prefix
        String l3OutIfaceName = "L3Out-" + l3OutName + "-" + ifaceName;
        batfishIface = interfaces.get(l3OutIfaceName);
      }

      if (batfishIface == null) {
        warnings.redFlagf(
            "OSPF interface %s in L3Out %s not found in converted interfaces",
            ifaceName, l3OutName);
        continue;
      }

      // Determine the area for this interface
      Long areaNum = parseAreaId(ospfConfig.getAreaId());
      if (areaNum == null) {
        areaNum = 0L; // Default to backbone area
      }

      // Build OSPF interface settings
      OspfInterfaceSettings.Builder settingsBuilder =
          OspfInterfaceSettings.builder()
              .setEnabled(true)
              .setProcess(processId)
              .setAreaName(areaNum);

      // Set cost if specified
      if (ospfIface.getCost() != null) {
        settingsBuilder.setCost(ospfIface.getCost());
      }

      // Set hello interval (default 10 seconds)
      int helloInterval = ospfIface.getHelloInterval() != null ? ospfIface.getHelloInterval() : 10;
      settingsBuilder.setHelloInterval(helloInterval);

      // Set dead interval (default 40 seconds, typically 4x hello)
      int deadInterval = ospfIface.getDeadInterval() != null ? ospfIface.getDeadInterval() : 40;
      settingsBuilder.setDeadInterval(deadInterval);

      // Set network type
      OspfNetworkType networkType = convertOspfNetworkType(ospfIface.getNetworkType());
      if (networkType != null) {
        settingsBuilder.setNetworkType(networkType);
      } else {
        // Default to point-to-point for L3Out interfaces
        settingsBuilder.setNetworkType(OspfNetworkType.POINT_TO_POINT);
      }

      // Set passive mode
      boolean passive = ospfIface.getPassive() != null ? ospfIface.getPassive() : false;
      settingsBuilder.setPassive(passive);

      // Apply settings to interface
      batfishIface.setOspfSettings(settingsBuilder.build());
    }
  }

  /**
   * Converts ACI OSPF network type string to Batfish OspfNetworkType.
   *
   * @param networkTypeStr The network type string
   * @return The corresponding OspfNetworkType, or null if not recognized
   */
  @VisibleForTesting
  public static @Nullable OspfNetworkType convertOspfNetworkType(@Nullable String networkTypeStr) {
    if (networkTypeStr == null) {
      return null;
    }

    switch (networkTypeStr.toLowerCase()) {
      case "point-to-point":
      case "p2p":
        return OspfNetworkType.POINT_TO_POINT;
      case "broadcast":
      case "bcast":
        return OspfNetworkType.BROADCAST;
      case "non-broadcast":
      case "nbma":
        return OspfNetworkType.NON_BROADCAST_MULTI_ACCESS;
      case "point-to-multipoint":
      case "p2mp":
        return OspfNetworkType.POINT_TO_MULTIPOINT;
      default:
        return null;
    }
  }

  /**
   * Parses an OSPF area ID from a string.
   *
   * <p>OSPF area IDs can be either numeric (e.g., "0", "1") or in IP address format (e.g.,
   * "0.0.0.0", "0.0.0.1"). This method converts both formats to a long value.
   *
   * @param areaIdStr The area ID string to parse
   * @return The area ID as a long, or null if invalid
   */
  @VisibleForTesting
  public static @Nullable Long parseAreaId(@Nullable String areaIdStr) {
    if (areaIdStr == null || areaIdStr.isEmpty()) {
      return null;
    }

    // Try parsing as a simple numeric value first
    try {
      return Long.parseLong(areaIdStr);
    } catch (NumberFormatException e) {
      // Not a simple number, try IP address format
    }

    // Try parsing as IP address format (e.g., "0.0.0.1")
    String[] parts = areaIdStr.split("\\.");
    if (parts.length != 4) {
      return null;
    }

    try {
      long result = 0;
      for (int i = 0; i < 4; i++) {
        int octet = Integer.parseInt(parts[i]);
        if (octet < 0 || octet > 255) {
          return null;
        }
        result = (result << 8) | octet;
      }
      return result;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Converts external EPGs (L3ExtEpg) from an L3Out.
   *
   * <p>External EPGs define subnets for external connectivity. This method creates static routes or
   * associated interface configurations for those subnets.
   *
   * @param l3Out The L3Out configuration
   * @param node The fabric node
   * @param interfaces Map of existing interfaces
   * @param vrf The VRF for external EPGs
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  private static void convertExternalEpgs(
      AciConfiguration.L3Out l3Out,
      AciConfiguration.FabricNode node,
      Map<String, Interface> interfaces,
      Vrf vrf,
      Configuration c,
      Warnings warnings) {
    if (l3Out.getExternalEpgs() == null || l3Out.getExternalEpgs().isEmpty()) {
      return;
    }

    for (AciConfiguration.ExternalEpg extEpg : l3Out.getExternalEpgs()) {
      String epgName = extEpg.getName();
      if (epgName == null) {
        continue;
      }

      // Convert external EPG subnets to interface configurations or static routes
      if (extEpg.getSubnets() != null && !extEpg.getSubnets().isEmpty()) {
        for (String subnetStr : extEpg.getSubnets()) {
          Prefix subnet = parsePrefix(subnetStr);
          if (subnet != null) {
            // Create a static route for this subnet if next hop is specified
            // Otherwise, the subnet is considered directly connected
            if (extEpg.getNextHop() != null) {
              try {
                Ip nextHop = Ip.parse(extEpg.getNextHop());
                StaticRoute staticRoute =
                    StaticRoute.builder()
                        .setNetwork(subnet)
                        .setNextHopIp(nextHop)
                        .setAdministrativeCost(1)
                        .build();
                vrf.getStaticRoutes().add(staticRoute);
              } catch (IllegalArgumentException e) {
                warnings.redFlagf(
                    "Invalid next hop %s for external EPG %s in L3Out %s",
                    extEpg.getNextHop(), epgName, l3Out.getName());
              }
            }
          }
        }
      }

      // Handle external EPG to interface binding
      if (extEpg.getInterface() != null) {
        Interface iface = interfaces.get(extEpg.getInterface());
        if (iface == null) {
          warnings.redFlagf(
              "Interface %s not found for external EPG %s in L3Out %s",
              extEpg.getInterface(), epgName, l3Out.getName());
        }
      }
    }
  }

  /**
   * Gets the generated ACL name for a contract.
   *
   * @param contractName The contract name
   * @return The ACL name
   */
  @VisibleForTesting
  public static @Nonnull String getContractAclName(String contractName) {
    return CONTRACT_ACL_PREFIX + contractName;
  }

  public static @Nonnull String getTabooAclName(String tabooName) {
    return TABOO_ACL_PREFIX + tabooName;
  }

  /**
   * Creates a VPC peer-link interface on a node if it's part of a VPC pair.
   *
   * @param node The fabric node
   * @param aciConfig The ACI configuration
   * @param interfaces Map of interfaces to add the VPC interface to
   * @param defaultVrf The default VRF
   * @param c The Batfish configuration
   * @param warnings Warnings container
   */
  private static void createVpcPeerLinkInterface(
      AciConfiguration.FabricNode node,
      AciConfiguration aciConfig,
      Map<String, Interface> interfaces,
      Vrf defaultVrf,
      Configuration c,
      Warnings warnings) {
    String nodeId = node.getNodeId();
    if (nodeId == null) {
      return;
    }

    // Check if this node is part of any VPC pair
    for (AciConfiguration.VpcPair vpcPair : aciConfig.getVpcPairs().values()) {
      if (nodeId.equals(vpcPair.getPeer1NodeId()) || nodeId.equals(vpcPair.getPeer2NodeId())) {
        // This node is part of a VPC pair - create peer-link interface
        String vpcIfaceName = "port-channel1";
        String peerNodeId =
            nodeId.equals(vpcPair.getPeer1NodeId())
                ? vpcPair.getPeer2NodeId()
                : vpcPair.getPeer1NodeId();

        // Get the peer node to extract its name for description
        AciConfiguration.FabricNode peerNode = aciConfig.getFabricNodes().get(peerNodeId);
        String peerName =
            (peerNode != null && peerNode.getName() != null && !peerNode.getName().isEmpty())
                ? peerNode.getName()
                : peerNodeId;

        Interface.Builder vpcIfaceBuilder =
            Interface.builder()
                .setName(vpcIfaceName)
                .setType(InterfaceType.AGGREGATED)
                .setOwner(c)
                .setVrf(defaultVrf)
                .setAdminUp(true)
                .setHumanName(
                    String.format(
                        "VPC Peer-link (VPC %s)",
                        vpcPair.getVpcId() != null ? vpcPair.getVpcId() : ""))
                .setDescription(
                    String.format(
                        "VPC peer-link connecting to %s (VPC: %s)",
                        peerName,
                        vpcPair.getVpcName() != null ? vpcPair.getVpcName() : vpcPair.getVpcId()))
                .setDeclaredNames(ImmutableList.of(vpcIfaceName));

        Interface vpcIface = vpcIfaceBuilder.build();
        interfaces.put(vpcIfaceName, vpcIface);

        // Only create one VPC interface per node
        break;
      }
    }
  }

  /**
   * Creates Layer 1 edges between fabric nodes based on fabric topology.
   *
   * <p>In ACI, nodes are connected via a spine-leaf topology. This method creates physical Layer 1
   * edges representing those connections.
   *
   * <p>Edge hostnames use resolved node hostnames to match {@link
   * #toVendorIndependentConfigurations}.
   *
   * @param aciConfig The ACI configuration
   * @return Set of Layer 1 edges between nodes
   */
  public static @Nonnull Set<Layer1Edge> createLayer1Edges(AciConfiguration aciConfig) {
    ImmutableSet.Builder<Layer1Edge> edges = ImmutableSet.builder();
    Map<String, String> nodeIdToHostname = computeNodeIdToHostnameMap(aciConfig, null);

    // Create a basic spine-leaf topology
    List<AciConfiguration.FabricNode> spines =
        aciConfig.getFabricNodes().values().stream()
            .filter(node -> node.getRole() != null && "spine".equalsIgnoreCase(node.getRole()))
            .collect(Collectors.toList());

    // Include both "leaf" and "service"/"services" nodes as leaves
    // (service nodes are leaf switches that provide connectivity to services)
    List<AciConfiguration.FabricNode> leaves =
        aciConfig.getFabricNodes().values().stream()
            .filter(
                node ->
                    node.getRole() != null
                        && ("leaf".equalsIgnoreCase(node.getRole())
                            || "services".equalsIgnoreCase(node.getRole())
                            || "service".equalsIgnoreCase(node.getRole())))
            .collect(Collectors.toList());

    // Connect each leaf to each spine
    int spineIndex = 0;
    for (AciConfiguration.FabricNode leaf : leaves) {
      String leafNodeId = leaf.getNodeId();
      String leafHostname = leafNodeId != null ? nodeIdToHostname.get(leafNodeId) : null;
      if (leafHostname == null) {
        continue;
      }

      // Get available interfaces from leaf (merge both sources)
      // Source 1: Fabric node interfaces (l1PhysIf)
      // Source 2: Path attachment interfaces (EPG attachments)
      Set<String> leafInterfaces = new LinkedHashSet<>();
      if (leaf.getInterfaces() != null) {
        leafInterfaces.addAll(leaf.getInterfaces().keySet());
      }
      // Also add interfaces discovered from path attachments
      if (aciConfig.getNodeInterfaces() != null
          && aciConfig.getNodeInterfaces().containsKey(leafNodeId)) {
        leafInterfaces.addAll(aciConfig.getNodeInterfaces().get(leafNodeId));
      }

      // For each spine, use a different leaf interface to model realistic fabric connectivity
      spineIndex = 0;
      for (AciConfiguration.FabricNode spine : spines) {
        String spineNodeId = spine.getNodeId();
        String spineHostname = spineNodeId != null ? nodeIdToHostname.get(spineNodeId) : null;
        if (spineHostname == null) {
          spineIndex++;
          continue;
        }

        // Select leaf interface - use indexed interface if available, otherwise default
        List<String> leafIfaceList = new ArrayList<>(leafInterfaces);
        String leafIface =
            spineIndex < leafIfaceList.size()
                ? leafIfaceList.get(spineIndex)
                : "Ethernet1/" + (spineIndex + 1);

        // For spine, use interfaces from fabric node (l1PhysIf) or generate default
        String spineIface;
        List<String> spineIfaceList = new ArrayList<>();
        if (spine.getInterfaces() != null) {
          spineIfaceList.addAll(spine.getInterfaces().keySet());
        }
        spineIface =
            spineIndex < spineIfaceList.size()
                ? spineIfaceList.get(spineIndex)
                : "Ethernet1/" + (spineIndex + 1);

        edges.add(new Layer1Edge(leafHostname, leafIface, spineHostname, spineIface));
        spineIndex++;
      }
    }

    // Create VPC peer-link edges
    for (AciConfiguration.VpcPair vpcPair : aciConfig.getVpcPairs().values()) {
      String peer1NodeId = vpcPair.getPeer1NodeId();
      String peer2NodeId = vpcPair.getPeer2NodeId();

      AciConfiguration.FabricNode peer1 = aciConfig.getFabricNodes().get(peer1NodeId);
      AciConfiguration.FabricNode peer2 = aciConfig.getFabricNodes().get(peer2NodeId);

      if (peer1 != null && peer2 != null) {
        // VPC peer-link interface name
        String vpcIfaceName = "port-channel1";

        String peer1Hostname = nodeIdToHostname.get(peer1NodeId);
        String peer2Hostname = nodeIdToHostname.get(peer2NodeId);
        if (peer1Hostname != null && peer2Hostname != null) {
          edges.add(new Layer1Edge(peer1Hostname, vpcIfaceName, peer2Hostname, vpcIfaceName));
        }
      }
    }

    // Add inter-fabric connection edges
    createInterFabricLayer1Edges(aciConfig, nodeIdToHostname, edges);

    return edges.build();
  }

  /**
   * Creates Layer1Edge objects for inter-fabric connections.
   *
   * <p>Inter-fabric connections represent physical or logical links between border switches in
   * different ACI fabrics. These connections are typically established via L3Out connections with
   * BGP peering, shared external networks, or MPLS connections.
   *
   * @param aciConfig The ACI configuration containing inter-fabric connections
   * @param nodeIdToHostname Map of node IDs to hostnames for resolving node references
   * @param edges Builder to add the created edges to
   */
  @VisibleForTesting
  static void createInterFabricLayer1Edges(
      AciConfiguration aciConfig,
      Map<String, String> nodeIdToHostname,
      ImmutableSet.Builder<Layer1Edge> edges) {

    Map<String, AciConfiguration.InterFabricConnection> connections =
        aciConfig.getInterFabricConnections();
    if (connections == null || connections.isEmpty()) {
      return;
    }

    // Find border nodes (nodes that have L3Out interfaces)
    Set<String> borderNodeIds = findBorderNodeIds(aciConfig);

    // Get list of fabric nodes for selecting representative border nodes
    List<AciConfiguration.FabricNode> fabricNodes =
        new ArrayList<>(aciConfig.getFabricNodes().values());

    // Filter to get border nodes
    List<AciConfiguration.FabricNode> borderNodes =
        fabricNodes.stream()
            .filter(node -> borderNodeIds.contains(node.getNodeId()))
            .collect(Collectors.toList());

    // If no specific border nodes found, use all leaf switches as potential border nodes
    if (borderNodes.isEmpty()) {
      borderNodes =
          fabricNodes.stream()
              .filter(
                  node ->
                      node.getRole() != null
                          && ("leaf".equalsIgnoreCase(node.getRole())
                              || "services".equalsIgnoreCase(node.getRole())))
              .collect(Collectors.toList());
    }

    if (borderNodes.isEmpty()) {
      return;
    }

    // Create edges for each inter-fabric connection
    for (Map.Entry<String, AciConfiguration.InterFabricConnection> entry : connections.entrySet()) {
      String connectionId = entry.getKey();
      AciConfiguration.InterFabricConnection connection = entry.getValue();

      String fabric1 = connection.getFabric1();
      String fabric2 = connection.getFabric2();
      if (fabric1 == null || fabric2 == null) {
        continue;
      }

      // Generate interface name for the inter-fabric link
      String ifaceName = generateInterFabricInterfaceName(connectionId, connection);

      // Use first two available border nodes for the connection
      // In a real ACI deployment, the specific nodes would be determined by L3Out path attachments
      AciConfiguration.FabricNode node1 = borderNodes.size() > 0 ? borderNodes.get(0) : null;
      AciConfiguration.FabricNode node2 = borderNodes.size() > 1 ? borderNodes.get(1) : node1;

      if (node1 == null || node2 == null) {
        continue;
      }

      String hostname1 = nodeIdToHostname.get(node1.getNodeId());
      String hostname2 = nodeIdToHostname.get(node2.getNodeId());

      if (hostname1 == null || hostname2 == null) {
        continue;
      }

      // Create bidirectional edge for the inter-fabric connection
      // Each fabric side gets its own interface for the connection
      String iface1 = ifaceName + "-fabric1";
      String iface2 = ifaceName + "-fabric2";

      edges.add(new Layer1Edge(hostname1, iface1, hostname2, iface2));
    }
  }

  /**
   * Finds the node IDs of border nodes (nodes participating in L3Out connections).
   *
   * <p>Border nodes are leaf switches that have L3Out interfaces configured, making them the
   * connection points for external networks and inter-fabric links.
   *
   * @param aciConfig The ACI configuration
   * @return Set of node IDs for border nodes
   */
  private static Set<String> findBorderNodeIds(AciConfiguration aciConfig) {
    Set<String> borderNodeIds = new HashSet<>();

    // Check L3Out configurations for node references in path attachments
    for (AciConfiguration.L3Out l3out : aciConfig.getL3Outs().values()) {
      // Check path attachments for node IDs
      if (l3out.getPathAttachments() != null) {
        for (AciConfiguration.L3OutPathAttachment attachment : l3out.getPathAttachments()) {
          if (attachment.getNodeId() != null) {
            borderNodeIds.add(attachment.getNodeId());
          }
        }
      }

      // If L3Out has BGP peers, mark all leaf nodes as potential border nodes
      // BGP configuration is at the L3Out level, not per-node
      if (l3out.getBgpPeers() != null && !l3out.getBgpPeers().isEmpty()) {
        for (AciConfiguration.FabricNode node : aciConfig.getFabricNodes().values()) {
          if (node.getRole() != null
              && ("leaf".equalsIgnoreCase(node.getRole())
                  || "services".equalsIgnoreCase(node.getRole()))) {
            borderNodeIds.add(node.getNodeId());
          }
        }
      }
    }

    // Also check path attachments for nodes that host EPGs with external connectivity
    if (aciConfig.getPathAttachmentMap() != null) {
      borderNodeIds.addAll(aciConfig.getPathAttachmentMap().keySet());
    }

    return borderNodeIds;
  }

  /**
   * Generates an interface name for an inter-fabric connection.
   *
   * @param connectionId The connection identifier
   * @param connection The inter-fabric connection
   * @return A normalized interface name for the connection
   */
  private static String generateInterFabricInterfaceName(
      String connectionId, AciConfiguration.InterFabricConnection connection) {
    // Create a normalized interface name based on connection type and ID
    String connType = connection.getConnectionType();
    if (connType == null || connType.isEmpty()) {
      connType = "generic";
    }

    // Sanitize connection ID for use in interface name
    String sanitizedId = connectionId.replaceAll("[^a-zA-Z0-9_-]", "-");

    return String.format("inter-fabric-%s-%s", connType, sanitizedId);
  }

  /**
   * Detects inter-fabric connections between multiple ACI fabrics.
   *
   * <p>Analyzes L3Out configurations from multiple ACI fabrics to detect connections via:
   *
   * <ul>
   *   <li>Shared external subnets in ExternalEPGs
   *   <li>Common BGP peers
   *   <li>Overlapping L3Out configurations
   * </ul>
   *
   * @param aciConfigs Map of fabric names to ACI configurations
   * @return Map of connection IDs to detected inter-fabric connections
   */
  public static Map<String, AciConfiguration.InterFabricConnection> detectInterFabricConnections(
      Map<String, AciConfiguration> aciConfigs) {
    Map<String, AciConfiguration.InterFabricConnection> connections = new TreeMap<>();

    // Compare each pair of fabrics
    List<String> fabrics = new ArrayList<>(aciConfigs.keySet());
    for (int i = 0; i < fabrics.size(); i++) {
      for (int j = i + 1; j < fabrics.size(); j++) {
        String fabric1 = fabrics.get(i);
        String fabric2 = fabrics.get(j);
        AciConfiguration config1 = aciConfigs.get(fabric1);
        AciConfiguration config2 = aciConfigs.get(fabric2);

        // Check for shared external subnets
        detectSharedExternalConnections(fabric1, config1, fabric2, config2, connections);

        // Check for shared BGP peers
        detectSharedBgpConnections(fabric1, config1, fabric2, config2, connections);
      }
    }

    return connections;
  }

  /**
   * Detects connections via shared external subnets in ExternalEPGs.
   *
   * @param fabric1 First fabric name
   * @param config1 First fabric configuration
   * @param fabric2 Second fabric name
   * @param config2 Second fabric configuration
   * @param connections Map to store detected connections
   */
  private static void detectSharedExternalConnections(
      String fabric1,
      AciConfiguration config1,
      String fabric2,
      AciConfiguration config2,
      Map<String, AciConfiguration.InterFabricConnection> connections) {

    // Collect external subnets from fabric1
    Set<String> fabric1Subnets = new HashSet<>();
    for (AciConfiguration.L3Out l3out : config1.getL3Outs().values()) {
      for (AciConfiguration.ExternalEpg extEpg : l3out.getExternalEpgs()) {
        fabric1Subnets.addAll(extEpg.getSubnets());
      }
    }

    // Collect external subnets from fabric2
    Set<String> fabric2Subnets = new HashSet<>();
    for (AciConfiguration.L3Out l3out : config2.getL3Outs().values()) {
      for (AciConfiguration.ExternalEpg extEpg : l3out.getExternalEpgs()) {
        fabric2Subnets.addAll(extEpg.getSubnets());
      }
    }

    // Find overlapping subnets
    Set<String> sharedSubnets = new HashSet<>(fabric1Subnets);
    sharedSubnets.retainAll(fabric2Subnets);

    if (!sharedSubnets.isEmpty()) {
      String connectionId = fabric1 + "-" + fabric2 + "-external";
      AciConfiguration.InterFabricConnection connection =
          new AciConfiguration.InterFabricConnection(
              fabric1, fabric2, "shared-external", "Fabrics share external subnets");
      connection.getSharedSubnets().addAll(sharedSubnets);
      connections.put(connectionId, connection);
    }
  }

  /**
   * Detects connections via shared BGP peers.
   *
   * @param fabric1 First fabric name
   * @param config1 First fabric configuration
   * @param fabric2 Second fabric name
   * @param config2 Second fabric configuration
   * @param connections Map to store detected connections
   */
  private static void detectSharedBgpConnections(
      String fabric1,
      AciConfiguration config1,
      String fabric2,
      AciConfiguration config2,
      Map<String, AciConfiguration.InterFabricConnection> connections) {

    // Collect BGP peers from fabric1
    Set<String> fabric1BgpPeers = new HashSet<>();
    for (AciConfiguration.L3Out l3out : config1.getL3Outs().values()) {
      if (l3out.getBgpPeers() != null) {
        for (AciConfiguration.BgpPeer peer : l3out.getBgpPeers()) {
          if (peer.getPeerAddress() != null) {
            fabric1BgpPeers.add(peer.getPeerAddress());
          }
        }
      }
    }

    // Collect BGP peers from fabric2
    Set<String> fabric2BgpPeers = new HashSet<>();
    for (AciConfiguration.L3Out l3out : config2.getL3Outs().values()) {
      if (l3out.getBgpPeers() != null) {
        for (AciConfiguration.BgpPeer peer : l3out.getBgpPeers()) {
          if (peer.getPeerAddress() != null) {
            fabric2BgpPeers.add(peer.getPeerAddress());
          }
        }
      }
    }

    // Find shared BGP peers
    Set<String> sharedBgpPeers = new HashSet<>(fabric1BgpPeers);
    sharedBgpPeers.retainAll(fabric2BgpPeers);

    if (!sharedBgpPeers.isEmpty()) {
      String connectionId = fabric1 + "-" + fabric2 + "-bgp";
      AciConfiguration.InterFabricConnection connection =
          new AciConfiguration.InterFabricConnection(
              fabric1, fabric2, "bgp", "Fabrics share BGP peers");
      connection.getBgpPeers().addAll(sharedBgpPeers);
      connections.put(connectionId, connection);
    }
  }

  /**
   * Creates a Batfish Interface address from an ACI IP address and prefix length.
   *
   * @param ip The IP address
   * @param prefixLength The prefix length
   * @return The ConcreteInterfaceAddress
   */
  @VisibleForTesting
  public static @Nonnull ConcreteInterfaceAddress toInterfaceAddress(Ip ip, int prefixLength) {
    return ConcreteInterfaceAddress.create(ip, prefixLength);
  }

  /**
   * Creates an IP wildcard from prefix and wildcard mask.
   *
   * @param prefix The IP prefix
   * @param wildcard The wildcard mask
   * @return The IpWildcard
   */
  @VisibleForTesting
  public static @Nonnull IpWildcard toIpWildcard(Ip prefix, Ip wildcard) {
    return IpWildcard.ipWithWildcardMask(prefix, wildcard);
  }

  /**
   * Converts ACI etherType specification to a Batfish match expression.
   *
   * <p>Supports common etherTypes:
   *
   * <ul>
   *   <li>0x0800 - IPv4
   *   <li>0x86dd - IPv6
   *   <li>0x0806 - ARP
   *   <li>0x8847 - MPLS unicast
   *   <li>0x8848 - MPLS multicast
   *   <li>arp, ipv4, ipv6, mpls - Name aliases
   * </ul>
   *
   * <p>Note: Non-IP etherTypes are matched by excluding IP protocols from the match expression.
   *
   * @param etherType The etherType string (hex or name)
   * @param contractName The contract name for warnings
   * @param filter The filter for warnings
   * @param warnings Warnings container
   * @return A match expression for the etherType, or null if not applicable
   */
  private static @Nullable AclLineMatchExpr toEtherType(
      String etherType,
      String contractName,
      AciConfiguration.Contract.Filter filter,
      Warnings warnings) {
    if (etherType == null) {
      warnings.redFlagf("Null etherType in contract %s filter %s", contractName, filter.getName());
      return null;
    }
    String et = etherType.toLowerCase().trim();

    // Handle hex format (0x prefix or just hex digits)
    int etherTypeValue;
    if (et.startsWith("0x") || et.startsWith("0X")) {
      if (et.length() <= 2) {
        warnings.redFlagf(
            "Invalid etherType (empty hex value) in contract %s filter %s: %s",
            contractName, filter.getName(), etherType);
        return null;
      }
      try {
        etherTypeValue = Integer.parseInt(et.substring(2), 16);
        if (etherTypeValue < 0 || etherTypeValue > 0xFFFF) {
          warnings.redFlagf(
              "EtherType out of range (0x0000-0xFFFF) in contract %s filter %s: 0x%x",
              contractName, filter.getName(), etherTypeValue);
          return null;
        }
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid etherType in contract %s filter %s: %s",
            contractName, filter.getName(), etherType);
        return null;
      }
    } else if (et.matches("[0-9a-f]+")) {
      try {
        etherTypeValue = Integer.parseInt(et, 16);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid etherType in contract %s filter %s: %s",
            contractName, filter.getName(), etherType);
        return null;
      }
    } else {
      // Handle named etherTypes
      switch (et) {
        case "arp":
          etherTypeValue = 0x0806;
          break;
        case "ipv4":
        case "ip":
          etherTypeValue = 0x0800;
          break;
        case "ipv6":
          etherTypeValue = 0x86dd;
          break;
        case "mpls":
        case "mpls_unicast":
          etherTypeValue = 0x8847;
          break;
        case "mpls_multicast":
          etherTypeValue = 0x8848;
          break;
        default:
          warnings.redFlagf(
              "Unknown etherType in contract %s filter %s: %s",
              contractName, filter.getName(), etherType);
          return null;
      }
    }

    // Convert etherType to match expression
    // Since Batfish ACLs work at IP layer, non-IP etherTypes require special handling
    switch (etherTypeValue) {
      case 0x0800: // IPv4
        // No filtering needed - this is the default
        return null;
      case 0x86dd: // IPv6
        // For now, warn that IPv6 filtering is not fully supported in IPv4 ACLs
        warnings.redFlagf(
            "IPv6 etherType specified in contract %s filter %s: IPv6 filtering has limited effect"
                + " in IPv4 ACLs",
            contractName, filter.getName());
        return null;
      case 0x0806: // ARP
        // ARP is L2 - we can't match it in IP ACLs
        warnings.redFlagf(
            "ARP etherType specified in contract %s filter %s: ARP filtering has limited effect in"
                + " IP ACLs",
            contractName, filter.getName());
        return AclLineMatchExprs.FALSE; // Will not match any IP traffic
      case 0x8847: // MPLS unicast
      case 0x8848: // MPLS multicast
        // MPLS is L2.5 - warn about limited effect
        warnings.redFlagf(
            "MPLS etherType specified in contract %s filter %s: MPLS filtering has limited effect"
                + " in IP ACLs",
            contractName, filter.getName());
        return AclLineMatchExprs.FALSE; // Will not match any IP traffic
      default:
        // Other non-IP etherTypes
        warnings.redFlagf(
            "Non-IP etherType (0x%04x) specified in contract %s filter %s: This will not match IP"
                + " traffic",
            etherTypeValue, contractName, filter.getName());
        return AclLineMatchExprs.FALSE;
    }
  }

  /**
   * Converts an ACI etherType string to a match expression.
   *
   * <p>This overload is for filter entries which use entry names instead of filter objects.
   *
   * @param etherType The etherType string (e.g., "arp", "ip", "0x0806")
   * @param contractName The contract name for warnings
   * @param entryName The entry name for warnings
   * @param warnings Warnings container
   * @return A match expression for the etherType, or null if not applicable
   */
  private static @Nullable AclLineMatchExpr toEtherType(
      String etherType, String contractName, String entryName, Warnings warnings) {
    String et = etherType.toLowerCase().trim();

    // Handle hex format (0x prefix or just hex digits)
    int etherTypeValue;
    if (et.startsWith("0x") || et.startsWith("0X")) {
      try {
        etherTypeValue = Integer.parseInt(et.substring(2), 16);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid etherType in contract %s entry %s: %s", contractName, entryName, etherType);
        return null;
      }
    } else if (et.matches("[0-9a-f]+")) {
      try {
        etherTypeValue = Integer.parseInt(et, 16);
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid etherType in contract %s entry %s: %s", contractName, entryName, etherType);
        return null;
      }
    } else {
      // Handle named etherTypes
      switch (et) {
        case "arp":
          etherTypeValue = 0x0806;
          break;
        case "ipv4":
        case "ip":
          etherTypeValue = 0x0800;
          return null; // IP is the default, no special matching needed
        case "trill":
          etherTypeValue = 0x22F3;
          break;
        case "macsec":
          etherTypeValue = 0x88E5;
          break;
        case "fcoe":
          etherTypeValue = 0x8906;
          break;
        case "lldp":
          etherTypeValue = 0x88CC;
          break;
        case "mpls":
        case "mpls-unicast":
          etherTypeValue = 0x8847;
          break;
        default:
          warnings.redFlagf(
              "Unknown etherType name in contract %s entry %s: %s",
              contractName, entryName, etherType);
          return null;
      }
    }

    // Match on the etherType
    switch (etherTypeValue) {
      case 0x0800: // IPv4
        return null; // No filtering needed for IPv4 (default)
      case 0x0806: // ARP
        // ARP is L2 only, won't match IP traffic
        warnings.redFlagf(
            "ARP etherType specified in contract %s entry %s: ARP filtering has limited effect in"
                + " IP ACLs",
            contractName, entryName);
        return AclLineMatchExprs.FALSE;
      case 0x86DD: // IPv6
        return AclLineMatchExprs.matchIpProtocol(IpProtocol.IPV6);
      case 0x8847: // MPLS unicast
      case 0x8848: // MPLS multicast
        // MPLS is L2.5 - warn about limited effect
        warnings.redFlagf(
            "MPLS etherType specified in contract %s entry %s: MPLS filtering has limited effect"
                + " in IP ACLs",
            contractName, entryName);
        return AclLineMatchExprs.FALSE; // Will not match any IP traffic
      default:
        // Other non-IP etherTypes
        warnings.redFlagf(
            "Non-IP etherType (0x%04x) specified in contract %s entry %s: This will not match IP"
                + " traffic",
            etherTypeValue, contractName, entryName);
        return AclLineMatchExprs.FALSE;
    }
  }

  /**
   * Parses ICMP type and code into a match expression.
   *
   * <p>Supports:
   *
   * <ul>
   *   <li>Type only: "8" for echo request
   *   <li>Type and code: "8:0" for echo request with code 0
   *   <li>Named types: "echo-request", "echo-reply", "destination-unreachable", etc.
   * </ul>
   *
   * @param icmpType The ICMP type string
   * @param icmpCode The ICMP code string (optional)
   * @param warnings Warnings container
   * @return A match expression for ICMP type/code
   */
  private static @Nonnull AclLineMatchExpr toIcmpTypeCode(
      @Nullable String icmpType, @Nullable String icmpCode, Warnings warnings) {
    int typeValue = 0;
    int codeValue = -1; // -1 means no code specified

    // Parse ICMP type
    if (icmpType != null) {
      String typeStr = icmpType.toLowerCase().trim();
      // Handle named ICMP types
      typeValue = parseIcmpTypeName(typeStr);
      if (typeValue == -1) {
        // Not a named type, try numeric
        try {
          typeValue = Integer.parseInt(typeStr);
          if (typeValue < 0 || typeValue > 255) {
            warnings.redFlagf("Invalid ICMP type: %s (must be 0-255)", icmpType);
            typeValue = 0;
          }
        } catch (NumberFormatException e) {
          warnings.redFlagf("Invalid ICMP type: %s", icmpType);
          typeValue = 0;
        }
      }
    }

    // Parse ICMP code if present
    if (icmpCode != null) {
      String codeStr = icmpCode.toLowerCase().trim();
      try {
        codeValue = Integer.parseInt(codeStr);
        if (codeValue < 0 || codeValue > 255) {
          warnings.redFlagf("Invalid ICMP code: %s (must be 0-255)", icmpCode);
          codeValue = 0;
        }
      } catch (NumberFormatException e) {
        warnings.redFlagf("Invalid ICMP code: %s", icmpCode);
        codeValue = 0;
      }
    }

    // Build match expression
    if (codeValue >= 0) {
      return AclLineMatchExprs.and(
          AclLineMatchExprs.matchIcmpType(typeValue), AclLineMatchExprs.matchIcmpCode(codeValue));
    } else {
      return AclLineMatchExprs.matchIcmpType(typeValue);
    }
  }

  /**
   * Parses a named ICMP type to its numeric value.
   *
   * @param typeName The ICMP type name
   * @return The numeric ICMP type, or -1 if not found
   */
  private static int parseIcmpTypeName(String typeName) {
    switch (typeName) {
      case "echo-reply":
        return 0;
      case "destination-unreachable":
      case "dest-unreachable":
        return 3;
      case "source-quench":
        return 4;
      case "redirect":
        return 5;
      case "alternate-host-address":
        return 6;
      case "echo-request":
      case "echo":
        return 8;
      case "router-advertisement":
      case "router-advert":
        return 9;
      case "router-solicitation":
      case "router-solicit":
        return 10;
      case "time-exceeded":
      case "ttl-exceeded":
        return 11;
      case "parameter-problem":
      case "parameter-prob":
        return 12;
      case "timestamp-request":
      case "timestamp":
        return 13;
      case "timestamp-reply":
        return 14;
      case "information-request":
      case "info-request":
        return 15;
      case "information-reply":
      case "info-reply":
        return 16;
      case "address-mask-request":
      case "mask-request":
        return 17;
      case "address-mask-reply":
      case "mask-reply":
        return 18;
      default:
        return -1;
    }
  }

  /**
   * Converts port specifications to an IntegerSpace.
   *
   * <p>Supports:
   *
   * <ul>
   *   <li>Single ports: "80", "443"
   *   <li>Port ranges: "8080-8090"
   *   <li>Multiple specifications: ["80", "8080-8090", "443"]
   * </ul>
   *
   * @param ports List of port specification strings
   * @param contractName The contract name for warnings
   * @param filter The filter for warnings
   * @param isDestination True for destination ports, false for source ports
   * @param warnings Warnings container
   * @return An IntegerSpace containing the specified ports
   */
  private static @Nonnull IntegerSpace toPortSpace(
      List<String> ports,
      String contractName,
      AciConfiguration.Contract.Filter filter,
      boolean isDestination,
      Warnings warnings) {
    IntegerSpace.Builder portSpace = IntegerSpace.builder();
    String portType = isDestination ? "destination" : "source";

    for (String portStr : ports) {
      String ps = normalizePortSpecToken(portStr);
      if (ps == null) {
        continue;
      }

      // Check for range syntax (e.g., "8080-8090")
      if (ps.contains("-")) {
        String[] parts = ps.split("-", 2);
        if (parts.length == 2) {
          try {
            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());
            if (start < 0 || start > 65535 || end < 0 || end > 65535) {
              warnings.redFlagf(
                  "Invalid %s port range in contract %s filter %s: %s (ports must be 0-65535)",
                  portType, contractName, filter.getName(), portStr);
            } else if (start > end) {
              warnings.redFlagf(
                  "Invalid %s port range in contract %s filter %s: %s (start > end)",
                  portType, contractName, filter.getName(), portStr);
            } else {
              portSpace.including(new SubRange(start, end));
            }
          } catch (NumberFormatException e) {
            warnings.redFlagf(
                "Invalid %s port range in contract %s filter %s: %s",
                portType, contractName, filter.getName(), portStr);
          }
        } else {
          warnings.redFlagf(
              "Invalid %s port range in contract %s filter %s: %s",
              portType, contractName, filter.getName(), portStr);
        }
      } else {
        // Single port
        try {
          int port = Integer.parseInt(ps);
          if (port < 0 || port > 65535) {
            warnings.redFlagf(
                "Invalid %s port in contract %s filter %s: %s (must be 0-65535)",
                portType, contractName, filter.getName(), portStr);
          } else {
            portSpace.including(port);
          }
        } catch (NumberFormatException e) {
          warnings.redFlagf(
              "Invalid %s port in contract %s filter %s: %s",
              portType, contractName, filter.getName(), portStr);
        }
      }
    }

    return portSpace.build();
  }

  /**
   * Converts a list of port strings to an IntegerSpace.
   *
   * <p>This overload is for filter entries which use entry names instead of filter objects.
   *
   * @param ports List of port strings (single ports or ranges like "80" or "8080-8090")
   * @param contractName The contract name for warnings
   * @param entryName The entry name for warnings
   * @param isDestination True for destination ports, false for source ports
   * @param warnings Warnings container
   * @return An IntegerSpace containing the specified ports
   */
  private static @Nonnull IntegerSpace toPortSpace(
      List<String> ports,
      String contractName,
      String entryName,
      boolean isDestination,
      Warnings warnings) {
    IntegerSpace.Builder portSpace = IntegerSpace.builder();
    String portType = isDestination ? "destination" : "source";

    for (String portStr : ports) {
      String ps = normalizePortSpecToken(portStr);
      if (ps == null) {
        continue;
      }

      // Check for range syntax (e.g., "8080-8090")
      if (ps.contains("-")) {
        String[] parts = ps.split("-", 2);
        if (parts.length == 2) {
          try {
            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());
            if (start < 0 || start > 65535 || end < 0 || end > 65535) {
              warnings.redFlagf(
                  "Invalid %s port range in contract %s entry %s: %s (ports must be 0-65535)",
                  portType, contractName, entryName, portStr);
            } else if (start > end) {
              warnings.redFlagf(
                  "Invalid %s port range in contract %s entry %s: %s (start > end)",
                  portType, contractName, entryName, portStr);
            } else {
              portSpace.including(new SubRange(start, end));
            }
          } catch (NumberFormatException e) {
            warnings.redFlagf(
                "Invalid %s port range in contract %s entry %s: %s",
                portType, contractName, entryName, portStr);
          }
        } else {
          warnings.redFlagf(
              "Invalid %s port range in contract %s entry %s: %s",
              portType, contractName, entryName, portStr);
        }
      } else {
        // Single port
        try {
          int port = Integer.parseInt(ps);
          if (port < 0 || port > 65535) {
            warnings.redFlagf(
                "Invalid %s port in contract %s entry %s: %s (must be 0-65535)",
                portType, contractName, entryName, portStr);
          } else {
            portSpace.including(port);
          }
        } catch (NumberFormatException e) {
          warnings.redFlagf(
              "Invalid %s port in contract %s entry %s: %s",
              portType, contractName, entryName, portStr);
        }
      }
    }

    return portSpace.build();
  }

  private static boolean isPlaceholderPortToken(String token) {
    String normalized = token.trim().toLowerCase();
    return normalized.equals("unspecified") || normalized.equals("any") || normalized.equals("0");
  }

  private static @Nullable String normalizePortSpecToken(@Nullable String token) {
    if (token == null) {
      return null;
    }
    String trimmed = token.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    if (!trimmed.contains("-")) {
      return isPlaceholderPortToken(trimmed) ? null : trimmed;
    }
    String[] parts = trimmed.split("-", 2);
    if (parts.length != 2) {
      return trimmed;
    }
    // Treat placeholder endpoints as unset range metadata.
    if (isPlaceholderPortToken(parts[0]) || isPlaceholderPortToken(parts[1])) {
      return null;
    }
    return trimmed;
  }

  /**
   * Converts an IP address specification to a match expression.
   *
   * <p>Supports:
   *
   * <ul>
   *   <li>Single IP: "10.0.0.1"
   *   <li>Prefix: "10.0.0.0/24"
   *   <li>Wildcard: "10.0.0.0/0.0.0.255"
   *   <li>Any: "any", "0.0.0.0/0"
   * </ul>
   *
   * @param addrStr The address specification string
   * @param isSource True for source address, false for destination
   * @param warnings Warnings container
   * @return A match expression for the address, or null if invalid
   */
  private static @Nullable AclLineMatchExpr toIpMatchExpr(
      String addrStr, boolean isSource, Warnings warnings) {
    if (addrStr == null || addrStr.trim().isEmpty()) {
      return null;
    }

    String as = addrStr.trim().toLowerCase();

    // Handle "any" keyword
    if (as.equals("any") || as.equals("0.0.0.0/0") || as.equals("0.0.0.0/0.0.0.0")) {
      return null; // No filtering needed for "any"
    }

    // Try CIDR prefix notation first
    try {
      Prefix prefix = Prefix.parse(as);
      if (isSource) {
        return AclLineMatchExprs.matchSrc(prefix);
      } else {
        return AclLineMatchExprs.matchDst(prefix);
      }
    } catch (IllegalArgumentException e) {
      // Not a CIDR prefix, try wildcard notation
    }

    // Try IP/wildcard notation (e.g., "10.0.0.0/0.0.0.255")
    Pattern wildcardPattern = Pattern.compile("([\\d.]+)/([\\d.]+)");
    Matcher matcher = wildcardPattern.matcher(as);
    if (matcher.matches()) {
      try {
        Ip addr = Ip.parse(matcher.group(1));
        Ip wildcard = Ip.parse(matcher.group(2));
        IpWildcard ipWildcard = IpWildcard.ipWithWildcardMask(addr, wildcard);
        if (isSource) {
          return AclLineMatchExprs.matchSrc(ipWildcard);
        } else {
          return AclLineMatchExprs.matchDst(ipWildcard);
        }
      } catch (IllegalArgumentException e) {
        // Invalid wildcard format
      }
    }

    // Try single IP address
    try {
      Ip ip = Ip.parse(as);
      if (isSource) {
        return AclLineMatchExprs.matchSrc(ip);
      } else {
        return AclLineMatchExprs.matchDst(ip);
      }
    } catch (IllegalArgumentException e) {
      warnings.redFlagf("Invalid IP address specification: %s", addrStr);
      return null;
    }
  }

  private AciConversion() {
    // Prevent instantiation
  }
}
