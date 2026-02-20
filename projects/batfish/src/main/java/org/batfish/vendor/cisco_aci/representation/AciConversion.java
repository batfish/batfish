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
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.ospf.OspfNetworkType;

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
    for (FabricNode node : aciConfig.getFabricNodes().values()) {
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
      FabricNode node, AciConfiguration aciConfig, String hostname, Warnings warnings) {
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
    Map<String, Interface> interfaces =
        AciInterfaceConverter.convertInterfaces(node, aciConfig, defaultVrf, c, warnings);
    c.setInterfaces(interfaces);

    // Create VPC peer-link interface if this node is part of a VPC pair
    createVpcPeerLinkInterface(node, aciConfig, interfaces, defaultVrf, c, warnings);

    // Convert bridge domains to interface VLAN settings
    convertBridgeDomains(aciConfig, interfaces, defaultVrf, c, warnings);

    // Convert contracts to ACLs
    AciContractConverter.convertContracts(aciConfig, c, warnings);
    AciContractConverter.convertTabooContracts(aciConfig, c, warnings);

    // Convert path attachments (EPG to interface mappings)
    AciContractConverter.convertPathAttachments(aciConfig, interfaces, c, warnings);

    // Convert L3Out configurations (BGP, static routes, etc.)
    AciRoutingConverter.convertL3Outs(node, aciConfig, interfaces, defaultVrf, c, warnings);

    // Convert L2Out configurations (external Layer 2 connectivity)
    AciRoutingConverter.convertL2Outs(aciConfig, interfaces, defaultVrf, c, warnings);

    return c;
  }

  private static @Nonnull Map<String, String> computeNodeIdToHostnameMap(
      AciConfiguration aciConfig, @Nullable Warnings warnings) {
    Map<String, String> nodeIdToHostname = new TreeMap<>();
    Set<String> usedHostnames = new HashSet<>();
    for (FabricNode node : aciConfig.getFabricNodes().values()) {
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
      FabricNode node, @Nullable String fabricHostname) {
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
    AciContractConverter.convertContracts(aciConfig, c, warnings);
    AciContractConverter.convertTabooContracts(aciConfig, c, warnings);

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
    for (TenantVrf aciVrf : aciConfig.getVrfs().values()) {
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

    for (BridgeDomain bd : aciConfig.getBridgeDomains().values()) {
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
    for (BridgeDomain bd : aciConfig.getBridgeDomains().values()) {
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
                .setMtu(AciConstants.DEFAULT_MTU)
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

  @VisibleForTesting
  public static @Nullable OspfNetworkType convertOspfNetworkType(@Nullable String networkTypeStr) {
    return AciRoutingConverter.convertOspfNetworkType(networkTypeStr);
  }

  @VisibleForTesting
  public static @Nullable Long parseAreaId(@Nullable String areaIdStr) {
    return AciRoutingConverter.parseAreaId(areaIdStr);
  }

  /**
   * Gets the generated ACL name for a contract.
   *
   * @param contractName The contract name
   * @return The ACL name
   */
  @VisibleForTesting
  public static @Nonnull String getContractAclName(String contractName) {
    return AciContractConverter.getContractAclName(contractName);
  }

  public static @Nonnull String getTabooAclName(String tabooName) {
    return AciContractConverter.getTabooAclName(tabooName);
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
      FabricNode node,
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
    for (VpcPair vpcPair : aciConfig.getVpcPairs().values()) {
      if (nodeId.equals(vpcPair.getPeer1NodeId()) || nodeId.equals(vpcPair.getPeer2NodeId())) {
        // This node is part of a VPC pair - create peer-link interface
        String vpcIfaceName = "port-channel1";
        String peerNodeId =
            nodeId.equals(vpcPair.getPeer1NodeId())
                ? vpcPair.getPeer2NodeId()
                : vpcPair.getPeer1NodeId();

        // Get the peer node to extract its name for description
        FabricNode peerNode = aciConfig.getFabricNodes().get(peerNodeId);
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

    if (!aciConfig.getFabricLinks().isEmpty()) {
      // Prefer explicit APIC fabricLink topology when present.
      for (FabricLink link : aciConfig.getFabricLinks()) {
        String node1Hostname = nodeIdToHostname.get(link.getNode1Id());
        String node2Hostname = nodeIdToHostname.get(link.getNode2Id());
        if (node1Hostname == null || node2Hostname == null) {
          continue;
        }
        if (link.getNode1Interface() == null || link.getNode2Interface() == null) {
          continue;
        }
        edges.add(
            new Layer1Edge(
                node1Hostname, link.getNode1Interface(), node2Hostname, link.getNode2Interface()));
      }
    } else {
      // Create a basic spine-leaf topology
      List<FabricNode> spines =
          aciConfig.getFabricNodes().values().stream()
              .filter(node -> node.getRole() != null && "spine".equalsIgnoreCase(node.getRole()))
              .collect(Collectors.toList());

      // Include both "leaf" and "service"/"services" nodes as leaves
      // (service nodes are leaf switches that provide connectivity to services)
      List<FabricNode> leaves =
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
      for (FabricNode leaf : leaves) {
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
        for (FabricNode spine : spines) {
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
      for (VpcPair vpcPair : aciConfig.getVpcPairs().values()) {
        String peer1NodeId = vpcPair.getPeer1NodeId();
        String peer2NodeId = vpcPair.getPeer2NodeId();

        FabricNode peer1 = aciConfig.getFabricNodes().get(peer1NodeId);
        FabricNode peer2 = aciConfig.getFabricNodes().get(peer2NodeId);

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

    Map<String, InterFabricConnection> connections = aciConfig.getInterFabricConnections();
    if (connections == null || connections.isEmpty()) {
      return;
    }

    // Find border nodes (nodes that have L3Out interfaces)
    Set<String> borderNodeIds = findBorderNodeIds(aciConfig);

    // Get list of fabric nodes for selecting representative border nodes
    List<FabricNode> fabricNodes = new ArrayList<>(aciConfig.getFabricNodes().values());

    // Filter to get border nodes
    List<FabricNode> borderNodes =
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
    for (Map.Entry<String, InterFabricConnection> entry : connections.entrySet()) {
      String connectionId = entry.getKey();
      InterFabricConnection connection = entry.getValue();

      String fabric1 = connection.getFabric1();
      String fabric2 = connection.getFabric2();
      if (fabric1 == null || fabric2 == null) {
        continue;
      }

      // Generate interface name for the inter-fabric link
      String ifaceName = generateInterFabricInterfaceName(connectionId, connection);

      // Use first two available border nodes for the connection
      // In a real ACI deployment, the specific nodes would be determined by L3Out path attachments
      FabricNode node1 = borderNodes.get(0);
      FabricNode node2 = borderNodes.size() > 1 ? borderNodes.get(1) : node1;

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
    for (L3Out l3out : aciConfig.getL3Outs().values()) {
      // Check path attachments for node IDs
      if (l3out.getPathAttachments() != null) {
        for (PathAttachment attachment : l3out.getPathAttachments()) {
          if (attachment.getNodeId() != null) {
            borderNodeIds.add(attachment.getNodeId());
          }
        }
      }

      // If L3Out has BGP peers, mark all leaf nodes as potential border nodes
      // BGP configuration is at the L3Out level, not per-node
      if (l3out.getBgpPeers() != null && !l3out.getBgpPeers().isEmpty()) {
        for (FabricNode node : aciConfig.getFabricNodes().values()) {
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
      String connectionId, InterFabricConnection connection) {
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
  public static Map<String, InterFabricConnection> detectInterFabricConnections(
      Map<String, AciConfiguration> aciConfigs) {
    Map<String, InterFabricConnection> connections = new TreeMap<>();

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
      Map<String, InterFabricConnection> connections) {

    // Collect external subnets from fabric1
    Set<String> fabric1Subnets = new HashSet<>();
    for (L3Out l3out : config1.getL3Outs().values()) {
      for (ExternalEpg extEpg : l3out.getExternalEpgs()) {
        fabric1Subnets.addAll(extEpg.getSubnets());
      }
    }

    // Collect external subnets from fabric2
    Set<String> fabric2Subnets = new HashSet<>();
    for (L3Out l3out : config2.getL3Outs().values()) {
      for (ExternalEpg extEpg : l3out.getExternalEpgs()) {
        fabric2Subnets.addAll(extEpg.getSubnets());
      }
    }

    // Find overlapping subnets
    Set<String> sharedSubnets = new HashSet<>(fabric1Subnets);
    sharedSubnets.retainAll(fabric2Subnets);

    if (!sharedSubnets.isEmpty()) {
      String connectionId = fabric1 + "-" + fabric2 + "-external";
      InterFabricConnection connection =
          new InterFabricConnection(
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
      Map<String, InterFabricConnection> connections) {

    // Collect BGP peers from fabric1
    Set<String> fabric1BgpPeers = new HashSet<>();
    for (L3Out l3out : config1.getL3Outs().values()) {
      if (l3out.getBgpPeers() != null) {
        for (BgpPeer peer : l3out.getBgpPeers()) {
          if (peer.getPeerAddress() != null) {
            fabric1BgpPeers.add(peer.getPeerAddress());
          }
        }
      }
    }

    // Collect BGP peers from fabric2
    Set<String> fabric2BgpPeers = new HashSet<>();
    for (L3Out l3out : config2.getL3Outs().values()) {
      if (l3out.getBgpPeers() != null) {
        for (BgpPeer peer : l3out.getBgpPeers()) {
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
      InterFabricConnection connection =
          new InterFabricConnection(fabric1, fabric2, "bgp", "Fabrics share BGP peers");
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
  private AciConversion() {
    // Prevent instantiation
  }
}
