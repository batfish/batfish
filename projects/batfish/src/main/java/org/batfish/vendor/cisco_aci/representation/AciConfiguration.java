package org.batfish.vendor.cisco_aci.representation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.vendor.VendorConfiguration;

/**
 * Datamodel class representing a Cisco ACI fabric configuration.
 *
 * <p>This class stores the ACI-specific configuration elements including tenants, bridge domains,
 * VRFs, EPGs (End Point Groups), contracts, and fabric nodes. It provides conversion to the
 * vendor-independent Batfish configuration model.
 *
 * <p>This class supports parsing ACI configurations from both JSON and XML formats. Use {@link
 * #fromFile} for automatic format detection, or use {@link #fromJson} or {@link #fromXml} for
 * specific format parsing.
 *
 * <p>The native ACI JSON/XML structure is hierarchical with polUni as the root:
 *
 * <pre>
 * polUni
 * ├── attributes
 * └── children[]
 *     ├── fvTenant (tenants)
 *     │   └── children[]
 *     │       ├── fvCtx (VRFs)
 *     │       ├── fvBD (bridge domains)
 *     │       ├── fvAp (application profiles)
 *     │       │   └── children[]
 *     │       │       └── fvAEPg (EPGs)
 *     │       └── vzBrCP (contracts)
 *     └── fabricInst (fabric configuration)
 *         └── children[]
 *             └── fabricProtPol
 *                 └── children[]
 *                     └── fabricExplicitGEp
 *                         └── children[]
 *                             └── fabricNodePEp (fabric nodes)
 * </pre>
 */
@SuppressWarnings({
  "PMD.UnusedPrivateField",
  "PMD.UnusedLocalVariable",
  "PMD.UnusedFormalParameter"
})
public final class AciConfiguration extends VendorConfiguration {

  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_TENANTS = "tenants";
  private static final String PROP_BRIDGE_DOMAINS = "bridgeDomains";
  private static final String PROP_VRFS = "vrfs";
  private static final String PROP_EPGS = "epgs";
  private static final String PROP_CONTRACTS = "contracts";
  private static final String PROP_CONTRACT_INTERFACES = "contractInterfaces";
  private static final String PROP_FILTERS = "filters";
  private static final String PROP_TABOO_CONTRACTS = "tabooContracts";
  private static final String PROP_FABRIC_NODES = "fabricNodes";
  private static final String PROP_L3_OUTS = "l3Outs";
  private static final String PROP_L2_OUTS = "l2Outs";

  /**
   * Creates an {@link AciConfiguration} from JSON text.
   *
   * <p>This method parses the native ACI JSON structure with polUni as the root and extracts
   * tenants, VRFs, bridge domains, EPGs, contracts, and fabric nodes from the nested children
   * arrays.
   *
   * @param filename The filename of the configuration file
   * @param text The JSON text content
   * @param warnings Warnings object for collecting conversion warnings
   * @return A new AciConfiguration instance
   * @throws IOException If JSON deserialization fails
   */
  public static AciConfiguration fromJson(String filename, String text, Warnings warnings)
      throws IOException {
    return AciParser.fromJson(filename, text, warnings);
  }

  /**
   * Creates an {@link AciConfiguration} from XML text.
   *
   * <p>This method parses the native ACI XML structure with polUni as the root and extracts
   * tenants, VRFs, bridge domains, EPGs, contracts, and fabric nodes from the nested children
   * arrays. The XML structure is equivalent to the JSON structure but uses XML tags instead of JSON
   * keys.
   *
   * @param filename The filename of the configuration file
   * @param text The XML text content
   * @param warnings Warnings object for collecting conversion warnings
   * @return A new AciConfiguration instance
   * @throws IOException If XML deserialization fails
   */
  public static AciConfiguration fromXml(String filename, String text, Warnings warnings)
      throws IOException {
    return AciParser.fromXml(filename, text, warnings);
  }

  /**
   * Creates an {@link AciConfiguration} from file content with automatic format detection.
   *
   * <p>This method automatically detects whether the input is JSON or XML based on the first
   * non-whitespace character (JSON starts with '{', XML starts with '<'). It then calls the
   * appropriate parser method.
   *
   * @param filename The filename of the configuration file
   * @param text The file content (JSON or XML format)
   * @param warnings Warnings object for collecting conversion warnings
   * @return A new AciConfiguration instance
   * @throws IOException If deserialization fails or the format is not recognized
   */
  public static AciConfiguration fromFile(String filename, String text, Warnings warnings)
      throws IOException {
    return AciParser.fromFile(filename, text, warnings);
  }

  /** Returns {@code true} iff the provided JSON text looks like a fabricLink export. */
  public static boolean isFabricLinksJson(String text) {
    return AciParser.isFabricLinksJson(text);
  }

  /**
   * Parses APIC fabricLink export JSON into explicit Layer1 link records.
   *
   * <p>Expected format is the common APIC response shape with top-level {@code imdata[]} containing
   * {@code fabricLink} objects.
   */
  public static @Nonnull List<AciFabricLink> parseFabricLinksJson(String filename, String text)
      throws IOException {
    return AciParser.parseFabricLinksJson(filename, text);
  }

  private String _hostname;

  /** Map of tenant names to tenant configurations */
  private Map<String, Tenant> _tenants;

  /** Map of bridge domain names to bridge domain configurations */
  private Map<String, BridgeDomain> _bridgeDomains;

  /** Map of VRF names to VRF configurations */
  private Map<String, AciVrfModel> _vrfs;

  /** Map of EPG names to EPG configurations */
  private Map<String, Epg> _epgs;

  /** Map of Application Profile names to Application Profile configurations */
  private Map<String, ApplicationProfile> _applicationProfiles;

  /** Map of contract names to contract configurations */
  private Map<String, Contract> _contracts;

  /** Map of contract interface names to contract interface configurations */
  private Map<String, ContractInterface> _contractInterfaces;

  /** Map of filter names to filter configurations */
  private Map<String, FilterModel> _filters;

  /** Map of taboo contract names to taboo contract configurations */
  private Map<String, TabooContract> _tabooContracts;

  /** Map of fabric node IDs to fabric node configurations */
  private Map<String, FabricNode> _fabricNodes;

  /** Map of VPC IDs to VPC pair configurations */
  private Map<String, AciVpcPair> _vpcPairs;

  /** List of explicit fabric links (from fabric_links.json) */
  private List<AciFabricLink> _fabricLinks;

  /** Map of inter-fabric connection IDs to connection configurations */
  private Map<String, AciInterFabricConnection> _interFabricConnections;

  /** Map of (nodeId, interfaceName) to path attachment details */
  private Map<String, Map<String, org.batfish.vendor.cisco_aci.representation.PathAttachment>>
      _pathAttachmentMap;

  /**
   * Map of node IDs to interface names discovered from path attachments (LinkedHashSet for
   * uniqueness + insertion order)
   */
  private Map<String, Set<String>> _nodeInterfaces;

  /** Map of L3Out names to L3Out configurations */
  private Map<String, org.batfish.vendor.cisco_aci.representation.L3Out> _l3Outs;

  /** Map of L2Out names to L2Out configurations */
  private Map<String, org.batfish.vendor.cisco_aci.representation.L2Out> _l2Outs;

  /** The vendor format for this configuration */
  private ConfigurationFormat _vendor;

  public AciConfiguration() {
    _tenants = new TreeMap<>();
    _bridgeDomains = new TreeMap<>();
    _vrfs = new TreeMap<>();
    _epgs = new TreeMap<>();
    _applicationProfiles = new TreeMap<>();
    _contracts = new TreeMap<>();
    _contractInterfaces = new TreeMap<>();
    _filters = new TreeMap<>();
    _tabooContracts = new TreeMap<>();
    _fabricNodes = new TreeMap<>();
    _vpcPairs = new TreeMap<>();
    _fabricLinks = new ArrayList<>();
    _interFabricConnections = new TreeMap<>();
    _pathAttachmentMap = new TreeMap<>();
    _nodeInterfaces = new TreeMap<>();
    _l3Outs = new TreeMap<>();
    _l2Outs = new TreeMap<>();
  }

  /**
   * Parses the native ACI polUni structure and extracts all configuration elements.
   *
   * <p>This method traverses the hierarchical JSON structure and extracts:
   *
   * <ul>
   *   <li>fvTenant - Tenants and their contained elements
   *   <li>fvCtx - VRF contexts within tenants
   *   <li>fvBD - Bridge domains within tenants
   *   <li>fvAEPg - Endpoint Groups within application profiles
   *   <li>vzBrCP - Contracts within tenants
   *   <li>fabricNodePEp - Fabric nodes within fabricInst
   * </ul>
   *
   * @param polUni The parsed polUni structure
   * @param warnings Warnings object for collecting parsing warnings
   */
  void parsePolUni(
      org.batfish.vendor.cisco_aci.representation.AciPolUniInternal polUni, Warnings warnings) {
    if (polUni == null || polUni.getChildren() == null) {
      return;
    }

    // First pass: collect all fabric nodes from fabricInst
    parseFabricNodes(polUni, warnings);

    // Second pass: parse VPC pairs
    parseVpcPairs(polUni, warnings);

    // Third pass: process tenants and their contents
    for (AciPolUniInternal.PolUniChild child : polUni.getChildren()) {
      if (child.getFvTenant() != null) {
        parseTenant(child.getFvTenant(), warnings);
      }
    }
  }

  /** Parses fabric nodes from the fabricInst hierarchy. */
  private void parseFabricNodes(AciPolUniInternal polUni, Warnings warnings) {
    // First pass: collect all fabricNodeIdentP objects to map node IDs to names
    Map<String, String> nodeIdToName = new TreeMap<>();

    // Parse from fabricInst (fabric)
    for (AciPolUniInternal.PolUniChild child : polUni.getChildren()) {
      if (child.getFabricInst() != null) {
        AciFabricInst fabricInst = child.getFabricInst();
        if (fabricInst.getChildren() != null) {
          for (AciFabricInst.FabricInstChild instChild : fabricInst.getChildren()) {
            // Parse fabricNodeIdentPol to get node name mappings (primary source)
            if (instChild.getFabricNodeIdentPol() != null) {
              AciFabricNodeIdentPol identPol = instChild.getFabricNodeIdentPol();
              if (identPol.getChildren() != null) {
                for (AciFabricNodeIdentP nodeIdentP : identPol.getChildren()) {
                  parseFabricNodeIdentP(nodeIdentP, nodeIdToName);
                }
              }
            }
            // Parse fabricProtPol for fabricNodePEp entries
            if (instChild.getFabricProtPol() != null) {
              AciFabricProtPol protPol = instChild.getFabricProtPol();
              if (protPol.getChildren() != null) {
                for (AciFabricProtPol.FabricProtPolChild protChild : protPol.getChildren()) {
                  if (protChild.getFabricExplicitGEp() != null) {
                    AciFabricExplicitGEp explicitEp = protChild.getFabricExplicitGEp();
                    if (explicitEp.getChildren() != null) {
                      for (AciFabricExplicitGEp.FabricExplicitGEpChild expChild :
                          explicitEp.getChildren()) {
                        // Parse fabricNodeIdentP to get node names (fallback source)
                        if (expChild.getFabricNodeIdentP() != null) {
                          parseFabricNodeIdentP(expChild.getFabricNodeIdentP(), nodeIdToName);
                        }
                        // Parse fabricNodePEp
                        if (expChild.getFabricNodePEp() != null) {
                          parseFabricNode(expChild.getFabricNodePEp(), nodeIdToName, warnings);
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // ALSO parse from ctrlrInst (controller) - alternative location for node names
      if (child.getCtrlrInst() != null) {
        AciCtrlrInst ctrlrInst = child.getCtrlrInst();
        if (ctrlrInst.getChildren() != null) {
          for (AciCtrlrInst.CtrlrInstChild instChild : ctrlrInst.getChildren()) {
            if (instChild.getFabricNodeIdentPol() != null) {
              AciFabricNodeIdentPol identPol = instChild.getFabricNodeIdentPol();
              if (identPol.getChildren() != null) {
                for (AciFabricNodeIdentP nodeIdentP : identPol.getChildren()) {
                  parseFabricNodeIdentP(nodeIdentP, nodeIdToName);
                }
              }
            }
          }
        }
      }
    }
  }

  /** Parses a fabricNodeIdentP to extract node name mapping. */
  private void parseFabricNodeIdentP(
      AciFabricNodeIdentP nodeIdentP, Map<String, String> nodeIdToName) {
    if (nodeIdentP.getAttributes() == null) {
      return;
    }
    AciFabricNodeIdentP.AciFabricNodeIdentPAttributes attrs = nodeIdentP.getAttributes();
    // fabricNodeIdentP can use either "id" or "nodeId" as the identifier field
    // Prefer "nodeId" as it's the standard field, fallback to "id"
    String nodeId = attrs.getNodeId();
    if (nodeId == null || nodeId.isEmpty()) {
      nodeId = attrs.getId();
    }
    String name = attrs.getName();

    if (nodeId != null && name != null && !name.isEmpty()) {
      nodeIdToName.put(nodeId, name);
    }
  }

  /** Parses a single fabric node from fabricNodePEp. */
  private void parseFabricNode(
      AciFabricNodePEp nodePep, Map<String, String> nodeIdToName, Warnings warnings) {
    if (nodePep.getAttributes() == null) {
      return;
    }

    AciFabricNodePEp.AciFabricNodePEpAttributes attrs = nodePep.getAttributes();
    // fabricNodePEp can use either "nodeId" or "id" as the identifier field
    // Prefer "nodeId" as it's the standard field, fallback to "id"
    String nodeId = attrs.getNodeId();
    if (nodeId == null || nodeId.isEmpty()) {
      nodeId = attrs.getId();
    }
    String podId = attrs.getPodId();
    String role = attrs.getRole();

    // Determine the key to use for this fabric node
    // Use nodeId if available, otherwise fallback to name
    String key = nodeId;
    String name;

    // Get the name from fabricNodeIdentP if available, otherwise from fabricNodePEp
    if (nodeId != null) {
      name = nodeIdToName.get(nodeId);
      if (name == null || name.isEmpty()) {
        name = attrs.getName();
      }
    } else {
      name = attrs.getName();
    }

    // If still null, we can't store this node
    if (key == null || key.isEmpty()) {
      key = name;
      if (key == null || key.isEmpty()) {
        warnings.redFlagf("Fabric node missing both ID and name, skipping");
        return;
      }
    }

    FabricNode fabricNode = new FabricNode();
    fabricNode.setNodeId(nodeId);
    // Use the human-readable name if available
    // If no name is available, leave it null so AciConversion can use fabric+nodeId format
    if (name != null && !name.isEmpty()) {
      fabricNode.setName(name);
    }
    // Don't set a fallback name - let AciConversion generate unique hostname from fabric+nodeId
    fabricNode.setPodId(podId);
    // Set role: use fabricNodePEp role if specified, otherwise extract from node name
    if (role == null || role.isEmpty() || "unspecified".equalsIgnoreCase(role)) {
      role = extractRoleFromNodeName(name);
    }
    fabricNode.setRole(role);

    // Parse children for interface information
    if (nodePep.getChildren() != null) {
      for (AciFabricNodePEp.FabricNodePEpChild nodeChild : nodePep.getChildren()) {
        // Parse fabricInterface
        if (nodeChild.getFabricInterface() != null) {
          AciFabricInterface ifaceObj = nodeChild.getFabricInterface();
          if (ifaceObj.getAttributes() != null) {
            AciFabricInterface.AciFabricInterfaceAttributes ifaceAttrs = ifaceObj.getAttributes();
            String ifaceName = ifaceAttrs.getName();
            if (ifaceName != null && !ifaceName.isEmpty()) {
              FabricNodeInterface iface = new FabricNodeInterface();
              iface.setName(ifaceName);
              iface.setEnabled(true); // ACI interfaces are enabled by default
              // Store additional interface attributes if needed
              fabricNode.getInterfaces().put(ifaceName, iface);
            }
          }
        }
        // Parse l1PhysIf (layer 1 physical interface)
        if (nodeChild.getL1PhysIf() != null) {
          AciL1PhysIf l1PhysIf = nodeChild.getL1PhysIf();
          if (l1PhysIf.getAttributes() != null) {
            AciL1PhysIf.AciL1PhysIfAttributes l1Attrs = l1PhysIf.getAttributes();
            String ifaceId = l1Attrs.getId();
            if (ifaceId != null && !ifaceId.isEmpty()) {
              // Avoid duplicates - only add if not already present
              if (!fabricNode.getInterfaces().containsKey(ifaceId)) {
                FabricNodeInterface iface = new FabricNodeInterface();
                iface.setName(ifaceId);
                iface.setEnabled(true); // ACI interfaces are enabled by default
                iface.setDescription(l1Attrs.getDescription());
                fabricNode.getInterfaces().put(ifaceId, iface);
              }
            }
          }
        }
      }
    }

    _fabricNodes.put(key, fabricNode);
  }

  /**
   * Extracts the role from a node name. ACI node names typically follow patterns like:
   * "SW-DC1-Leaf-NSAB01-SET-01" or "SW-DC1-Spine-NSAA03-SET-01".
   *
   * @param nodeName The node name
   * @return The role ("leaf", "spine", or null if not found)
   */
  private @Nullable String extractRoleFromNodeName(String nodeName) {
    if (nodeName == null || nodeName.isEmpty()) {
      return null;
    }
    // Attempt to extract role from name, but be conservative.
    // User feedback indicates names can be arbitrary.
    String lowerName = nodeName.toLowerCase();

    // Check for standard ACI naming conventions if present
    if (lowerName.contains("-spine-") || lowerName.startsWith("spine-")) {
      return "spine";
    } else if (lowerName.contains("-leaf-") || lowerName.startsWith("leaf-")) {
      return "leaf";
    }

    // Default to null if no clear role indicator is found
    return null;
  }

  /** Parses a tenant and all its contained elements. */
  private void parseTenant(AciTenant tenant, Warnings warnings) {
    if (tenant == null || tenant.getAttributes() == null) {
      return;
    }

    AciTenant.AciTenantAttributes attrs = tenant.getAttributes();
    String tenantName = attrs.getName();
    if (tenantName == null || tenantName.isEmpty()) {
      return;
    }

    // Create or get the tenant
    Tenant t = getOrCreateTenant(tenantName);
    if (attrs.getDescription() != null) {
      // Note: Tenant class doesn't have description field, skip for now
    }

    // Parse tenant children for VRFs, BDs, EPGs, and contracts
    if (tenant.getChildren() == null) {
      return;
    }

    for (Object childObj : tenant.getChildren()) {
      // Children are heterogenous - need to check type
      if (childObj instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> childMap = (Map<String, Object>) childObj;

        // Check for fvCtx (VRF)
        if (childMap.containsKey("fvCtx")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> vrfMap = (Map<String, Object>) childMap.get("fvCtx");
          parseVrfFromMap(vrfMap, tenantName, warnings);
        }
        // Check for fvBD (Bridge Domain)
        else if (childMap.containsKey("fvBD")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> bdMap = (Map<String, Object>) childMap.get("fvBD");
          parseBridgeDomainFromMap(bdMap, tenantName, warnings);
        }
        // Check for fvAp (Application Profile - contains EPGs)
        else if (childMap.containsKey("fvAp")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> apMap = (Map<String, Object>) childMap.get("fvAp");
          parseApplicationProfileFromMap(apMap, tenantName, warnings);
        }
        // Check for vzFilter (Filter - contains filter entries)
        else if (childMap.containsKey("vzFilter")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> filterMap = (Map<String, Object>) childMap.get("vzFilter");
          parseFilterFromMap(filterMap, tenantName, warnings);
        }
        // Check for vzBrCP (Contract)
        else if (childMap.containsKey("vzBrCP")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> contractMap = (Map<String, Object>) childMap.get("vzBrCP");
          parseContractFromMap(contractMap, tenantName, warnings);
        }
        // Check for vzCPIf (Contract Interface)
        else if (childMap.containsKey("vzCPIf")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> contractInterfaceMap = (Map<String, Object>) childMap.get("vzCPIf");
          parseContractInterfaceFromMap(contractInterfaceMap, tenantName, warnings);
        }
        // Check for l3extOut (L3 Outside - routed external connectivity)
        else if (childMap.containsKey("l3extOut") || childMap.containsKey("l3ExtOut")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> l3outMap =
              (Map<String, Object>)
                  (childMap.containsKey("l3extOut")
                      ? childMap.get("l3extOut")
                      : childMap.get("l3ExtOut"));
          parseL3OutFromMap(l3outMap, tenantName, warnings);
        }
        // Check for l2extOut (L2 Outside - bridged external connectivity)
        else if (childMap.containsKey("l2extOut") || childMap.containsKey("l2ExtOut")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> l2outMap =
              (Map<String, Object>)
                  (childMap.containsKey("l2extOut")
                      ? childMap.get("l2extOut")
                      : childMap.get("l2ExtOut"));
          parseL2OutFromMap(l2outMap, tenantName, warnings);
        }
        // Check for fvAEPg directly under tenant (uncommon but possible)
        else if (childMap.containsKey("fvAEPg")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> epgMap = (Map<String, Object>) childMap.get("fvAEPg");
          parseEpgFromMap(epgMap, tenantName, null, warnings);
        }
        // Check for mgmtMgmtP (Management policy - contains OOB management node bindings)
        else if (childMap.containsKey("mgmtMgmtP")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> mgmtMgmtMap = (Map<String, Object>) childMap.get("mgmtMgmtP");
          parseManagementPolicyFromMap(mgmtMgmtMap, warnings);
        }
        // Check for vzTaboo (taboo contracts)
        else if (childMap.containsKey("vzTaboo")) {
          @SuppressWarnings("unchecked")
          Map<String, Object> tabooMap = (Map<String, Object>) childMap.get("vzTaboo");
          parseTabooContractFromMap(tabooMap, tenantName, warnings);
        }
        // Log warning for unrecognized tenant child types
        else {
          @SuppressWarnings("unchecked")
          Map<String, Object> childAttrs = (Map<String, Object>) childMap.get("attributes");
          if (childAttrs != null && !childMap.isEmpty()) {
            String name = (String) childAttrs.get("name");
            String unknownType = childMap.keySet().iterator().next();
            warnings.redFlagf(
                "Skipping unsupported tenant child object: %s (name: %s) in tenant %s. This"
                    + " configuration will not be analyzed.",
                unknownType, name, tenantName);
          }
        }
      }
    }
  }

  /**
   * Parses VPC (Virtual Port Channel) pairs from fabricExplicitGEp structures.
   *
   * <p>VPC pairs are identified by fabricExplicitGEp with multiple fabricNodePEp children,
   * indicating two or more nodes that are VPC peers connected via a peer-link.
   */
  private void parseVpcPairs(AciPolUniInternal polUni, Warnings warnings) {
    for (AciPolUniInternal.PolUniChild child : polUni.getChildren()) {
      if (child.getFabricInst() != null) {
        AciFabricInst fabricInst = child.getFabricInst();
        if (fabricInst.getChildren() != null) {
          for (AciFabricInst.FabricInstChild instChild : fabricInst.getChildren()) {
            if (instChild.getFabricProtPol() != null) {
              AciFabricProtPol protPol = instChild.getFabricProtPol();
              if (protPol.getChildren() != null) {
                for (AciFabricProtPol.FabricProtPolChild protChild : protPol.getChildren()) {
                  if (protChild.getFabricExplicitGEp() != null) {
                    AciFabricExplicitGEp explicitEp = protChild.getFabricExplicitGEp();
                    // Check if this fabricExplicitGEp has multiple fabricNodePEp children (VPC
                    // pair)
                    if (explicitEp.getChildren() != null) {
                      List<AciFabricExplicitGEp.FabricExplicitGEpChild> children =
                          explicitEp.getChildren();
                      List<String> nodeIds = new ArrayList<>();
                      String vpcId = null;
                      String vpcName = null;

                      // Get VPC ID and name from fabricExplicitGEp attributes
                      if (explicitEp.getAttributes() != null) {
                        AciFabricExplicitGEp.AciFabricExplicitGEpAttributes attrs =
                            explicitEp.getAttributes();
                        vpcId = attrs.getId();
                        vpcName = attrs.getName();
                      }

                      // Collect all fabricNodePEp node IDs
                      for (AciFabricExplicitGEp.FabricExplicitGEpChild expChild : children) {
                        if (expChild.getFabricNodePEp() != null) {
                          AciFabricNodePEp nodePep = expChild.getFabricNodePEp();
                          if (nodePep.getAttributes() != null) {
                            AciFabricNodePEp.AciFabricNodePEpAttributes attrs =
                                nodePep.getAttributes();
                            // Prefer nodeId over id field
                            String nodeId = attrs.getNodeId();
                            if (nodeId == null || nodeId.isEmpty()) {
                              nodeId = attrs.getId();
                            }
                            if (nodeId != null && !nodeId.isEmpty()) {
                              nodeIds.add(nodeId);
                            }
                          }
                        }
                      }

                      // If we have exactly 2 nodes, this is a VPC pair
                      if (nodeIds.size() == 2 && vpcId != null) {
                        AciVpcPair vpcPair =
                            new AciVpcPair(vpcId, vpcName, nodeIds.get(0), nodeIds.get(1));
                        _vpcPairs.put(vpcId, vpcPair);
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Parses management policy (mgmtMgmtP) to extract out-of-band management IPs.
   *
   * <p>Management policy structure: mgmtMgmtP → mgmtOoB → mgmtRsOoBStNode The mgmtRsOoBStNode
   * contains: - tDn: "topology/pod-{podId}/node-{nodeId}" - identifies the fabric node - addr:
   * management IP address with prefix (e.g., "10.35.1.52/24") - gw: default gateway (e.g.,
   * "10.35.1.1") - addr6/gateway6: IPv6 addresses (optional)
   */
  private void parseManagementPolicyFromMap(Map<String, Object> mgmtMgmtMap, Warnings warnings) {
    if (mgmtMgmtMap == null) {
      return;
    }

    // Navigate to mgmtOoB (Out-of-band management interface)
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) mgmtMgmtMap.get("attributes");
    @SuppressWarnings("unchecked")
    List<Object> children = (List<Object>) mgmtMgmtMap.get("children");

    if (children == null) {
      return;
    }

    // Look for mgmtOoB child
    for (Object childObj : children) {
      if (!(childObj instanceof Map)) {
        continue;
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> childMap = (Map<String, Object>) childObj;

      if (!childMap.containsKey("mgmtOoB")) {
        continue;
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> mgmtOoBMap = (Map<String, Object>) childMap.get("mgmtOoB");
      if (mgmtOoBMap == null) {
        continue;
      }

      // Parse mgmtOoB children for mgmtRsOoBStNode
      @SuppressWarnings("unchecked")
      List<Object> oobChildren = (List<Object>) mgmtOoBMap.get("children");
      if (oobChildren == null) {
        continue;
      }

      for (Object oobChildObj : oobChildren) {
        if (!(oobChildObj instanceof Map)) {
          continue;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> oobChildMap = (Map<String, Object>) oobChildObj;

        if (!oobChildMap.containsKey("mgmtRsOoBStNode")) {
          continue;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> oobStNodeMap = (Map<String, Object>) oobChildMap.get("mgmtRsOoBStNode");
        if (oobStNodeMap == null) {
          continue;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> stNodeAttrs = (Map<String, Object>) oobStNodeMap.get("attributes");
        if (stNodeAttrs == null) {
          continue;
        }

        // Extract management information
        String tDn = (String) stNodeAttrs.get("tDn");
        String addr = (String) stNodeAttrs.get("addr");
        String gw = (String) stNodeAttrs.get("gw");
        String addr6 = (String) stNodeAttrs.get("addr6");
        String gateway6 = (String) stNodeAttrs.get("gateway6");

        if (tDn == null || addr == null) {
          continue;
        }

        // Parse tDn to extract node ID
        // tDn format: "topology/pod-1/node-1208"
        String nodeId = null;
        String[] parts = tDn.split("/");
        for (String part : parts) {
          if (part.startsWith("node-")) {
            nodeId = part.substring(5);
            break;
          }
        }

        if (nodeId == null) {
          warnings.redFlag(String.format("Could not parse node ID from management tDn: %s", tDn));
          continue;
        }

        // Create ManagementInfo object
        AciManagementInfo mgmtInfo = new AciManagementInfo();
        mgmtInfo.setAddress(addr);
        mgmtInfo.setGateway(gw);
        mgmtInfo.setAddress6(addr6);
        mgmtInfo.setGateway6(gateway6);

        // Find the fabric node and attach management info
        FabricNode node = _fabricNodes.get(nodeId);
        if (node != null) {
          node.setManagementInfo(mgmtInfo);
        } else {
          warnings.redFlag(
              String.format(
                  "Management IP %s references unknown node ID %s (from tDn: %s)",
                  addr, nodeId, tDn));
        }
      }
    }
  }

  /** Parses a VRF from a raw map structure. */
  private void parseVrfFromMap(Map<String, Object> vrfMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) vrfMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String vrfName = (String) attrs.get("name");
    if (vrfName == null || vrfName.isEmpty()) {
      return;
    }

    // Create VRF with fully qualified name (tenant:vrf)
    String fqVrfName = tenantName + ":" + vrfName;
    AciVrfModel vrf = getOrCreateVrf(fqVrfName);
    vrf.setTenant(tenantName);
    vrf.setDescription((String) attrs.get("descr"));

    // Also add VRF to tenant's VRF map (using fully-qualified name for global uniqueness)
    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getVrfs().put(fqVrfName, vrf);
  }

  /** Parses a Bridge Domain from a raw map structure. */
  private void parseBridgeDomainFromMap(
      Map<String, Object> bdMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) bdMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String bdName = (String) attrs.get("name");
    if (bdName == null || bdName.isEmpty()) {
      return;
    }

    // Create Bridge Domain with fully qualified name
    String fqBdName = tenantName + ":" + bdName;
    BridgeDomain bd = getOrCreateBridgeDomain(fqBdName);
    bd.setTenant(tenantName);
    bd.setDescription((String) attrs.get("descr"));

    // Parse VRF association from children (fvRsCtx) and subnets (fvSubnet)
    if (bdMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) bdMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;
          if (childMap.containsKey("fvRsCtx")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsCtxMap = (Map<String, Object>) childMap.get("fvRsCtx");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsCtxAttrs = (Map<String, Object>) rsCtxMap.get("attributes");
            if (rsCtxAttrs != null) {
              String tnFvCtxName = (String) rsCtxAttrs.get("tnFvCtxName");
              if (tnFvCtxName != null) {
                // Store the fully-qualified VRF name
                bd.setVrf(tenantName + ":" + tnFvCtxName);
              }
            }
          } else if (childMap.containsKey("fvSubnet")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> subnetMap = (Map<String, Object>) childMap.get("fvSubnet");
            @SuppressWarnings("unchecked")
            Map<String, Object> subnetAttrs = (Map<String, Object>) subnetMap.get("attributes");
            if (subnetAttrs != null) {
              String ip = (String) subnetAttrs.get("ip");
              if (ip != null && !ip.isEmpty()) {
                bd.getSubnets().add(ip);
              }
            }
          } else if (childMap.containsKey("fvRsPathAtt")) {
            // Parse encapsulation from path attachment
            @SuppressWarnings("unchecked")
            Map<String, Object> pathAttMap = (Map<String, Object>) childMap.get("fvRsPathAtt");
            @SuppressWarnings("unchecked")
            Map<String, Object> pathAttAttrs = (Map<String, Object>) pathAttMap.get("attributes");
            if (pathAttAttrs != null) {
              String encap = (String) pathAttAttrs.get("encap");
              if (encap != null && !encap.isEmpty() && !encap.equals("unknown")) {
                bd.setEncapsulation(encap);
              }
            }
          }
        }
      }
    }

    // Also add BridgeDomain to tenant's BD map (using fully-qualified name for global uniqueness)
    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getBridgeDomains().put(fqBdName, bd);
  }

  /** Parses an Application Profile and its contained EPGs. */
  private void parseApplicationProfileFromMap(
      Map<String, Object> apMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) apMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String apName = (String) attrs.get("name");
    if (apName == null || apName.isEmpty()) {
      return;
    }

    // Create or get Application Profile
    String fqApName = tenantName + ":" + apName;
    ApplicationProfile appProfile = getOrCreateApplicationProfile(fqApName);
    appProfile.setTenant(tenantName);
    appProfile.setDescription((String) attrs.get("descr"));

    // Parse children for EPGs
    if (apMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) apMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;
          if (childMap.containsKey("fvAEPg")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> epgMap = (Map<String, Object>) childMap.get("fvAEPg");
            parseEpgFromMap(epgMap, tenantName, apName, warnings);

            // Add EPG name to Application Profile
            @SuppressWarnings("unchecked")
            Map<String, Object> epgAttrs = (Map<String, Object>) epgMap.get("attributes");
            if (epgAttrs != null) {
              String epgName = (String) epgAttrs.get("name");
              if (epgName != null) {
                String fqEpgName = tenantName + ":" + apName + ":" + epgName;
                appProfile.addEpg(fqEpgName);
              }
            }
          }
        }
      }
    }
  }

  /** Parses an EPG from a raw map structure. */
  private void parseEpgFromMap(
      Map<String, Object> epgMap, String tenantName, @Nullable String apName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) epgMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String epgName = (String) attrs.get("name");
    if (epgName == null || epgName.isEmpty()) {
      return;
    }

    // Create EPG with fully qualified name
    String fqEpgName = tenantName + ":" + (apName != null ? apName + ":" : "") + epgName;
    Epg epg = getOrCreateEpg(fqEpgName);
    epg.setTenant(tenantName);
    epg.setApplicationProfile(apName);
    epg.setDescription((String) attrs.get("descr"));

    // Parse children for bridge domain association and contract references
    if (epgMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) epgMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // Bridge domain association
          if (childMap.containsKey("fvRsBd")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsBdMap = (Map<String, Object>) childMap.get("fvRsBd");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsBdAttrs = (Map<String, Object>) rsBdMap.get("attributes");
            if (rsBdAttrs != null) {
              String tnFvBDName = (String) rsBdAttrs.get("tnFvBDName");
              if (tnFvBDName != null) {
                // Store the fully-qualified BD name
                epg.setBridgeDomain(tenantName + ":" + tnFvBDName);
              }
            }
          }

          // Provided contracts
          if (childMap.containsKey("fvRsProv")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvMap = (Map<String, Object>) childMap.get("fvRsProv");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvAttrs = (Map<String, Object>) rsProvMap.get("attributes");
            if (rsProvAttrs != null) {
              String tnVzBrCPName = (String) rsProvAttrs.get("tnVzBrCPName");
              if (tnVzBrCPName != null) {
                String fqContractName = tenantName + ":" + tnVzBrCPName;
                epg.getProvidedContracts().add(fqContractName);
              }
            }
          }

          // Consumed contracts
          if (childMap.containsKey("fvRsCons")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsMap = (Map<String, Object>) childMap.get("fvRsCons");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsAttrs = (Map<String, Object>) rsConsMap.get("attributes");
            if (rsConsAttrs != null) {
              String tnVzBrCPName = (String) rsConsAttrs.get("tnVzBrCPName");
              if (tnVzBrCPName != null) {
                String fqContractName = tenantName + ":" + tnVzBrCPName;
                epg.getConsumedContracts().add(fqContractName);
              }
            }
          }

          // Provided contract interfaces (ContractInterface references)
          if (childMap.containsKey("fvRsProvIf")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvIfMap = (Map<String, Object>) childMap.get("fvRsProvIf");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvIfAttrs = (Map<String, Object>) rsProvIfMap.get("attributes");
            String contractInterfaceName = firstNonEmptyValue(rsProvIfAttrs, "tnVzCPIfName");
            if (contractInterfaceName != null) {
              epg.getProvidedContractInterfaces().add(tenantName + ":" + contractInterfaceName);
            }
          }

          // Consumed contract interfaces (ContractInterface references)
          if (childMap.containsKey("fvRsConsIf")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsIfMap = (Map<String, Object>) childMap.get("fvRsConsIf");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsIfAttrs = (Map<String, Object>) rsConsIfMap.get("attributes");
            String contractInterfaceName = firstNonEmptyValue(rsConsIfAttrs, "tnVzCPIfName");
            if (contractInterfaceName != null) {
              epg.getConsumedContractInterfaces().add(tenantName + ":" + contractInterfaceName);
            }
          }

          // Taboo contracts protecting this EPG
          if (childMap.containsKey("fvRsProtBy")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProtByMap = (Map<String, Object>) childMap.get("fvRsProtBy");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProtByAttrs = (Map<String, Object>) rsProtByMap.get("attributes");
            String tabooName = firstNonEmptyValue(rsProtByAttrs, "tnVzTabooName");
            if (tabooName != null) {
              epg.getProtectedByTaboos().add(tenantName + ":" + tabooName);
            }
          }

          // Path Attachments (fvRsPathAtt) - link EPG to physical interfaces
          if (childMap.containsKey("fvRsPathAtt")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pathAttMap = (Map<String, Object>) childMap.get("fvRsPathAtt");
            @SuppressWarnings("unchecked")
            Map<String, Object> pathAttAttrs = (Map<String, Object>) pathAttMap.get("attributes");
            if (pathAttAttrs != null) {
              String tDn = (String) pathAttAttrs.get("tDn");
              if (tDn != null) {
                PathAttachment pathAtt = new PathAttachment(tDn);
                pathAtt.setEncap((String) pathAttAttrs.get("encap"));
                pathAtt.setDescription((String) pathAttAttrs.get("descr"));
                pathAtt.setEpgName(epgName);
                pathAtt.setEpgTenant(tenantName);

                // Add to path attachment map for both nodes (vPC pairs get two entries)
                String nodeId = pathAtt.getNodeId();
                String nodeId2 = pathAtt.getNodeId2();
                if (nodeId != null) {
                  _pathAttachmentMap
                      .computeIfAbsent(nodeId, k -> new TreeMap<>())
                      .put(pathAtt.getInterface(), pathAtt);

                  // Add to node interfaces list (LinkedHashSet for uniqueness + order)
                  _nodeInterfaces
                      .computeIfAbsent(nodeId, k -> new LinkedHashSet<>())
                      .add(pathAtt.getInterface());
                }
                // For vPC, also add to the secondary node
                if (nodeId2 != null) {
                  _pathAttachmentMap
                      .computeIfAbsent(nodeId2, k -> new TreeMap<>())
                      .put(pathAtt.getInterface(), pathAtt);

                  _nodeInterfaces
                      .computeIfAbsent(nodeId2, k -> new LinkedHashSet<>())
                      .add(pathAtt.getInterface());
                }
              }
            }
          }
        }
      }
    }

    // Also add EPG to tenant's EPG map (using fully-qualified name for global uniqueness)
    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getEpgs().put(fqEpgName, epg);
  }

  /** Parses a Contract from a raw map structure. */
  private void parseContractFromMap(
      Map<String, Object> contractMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) contractMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String contractName = (String) attrs.get("name");
    if (contractName == null || contractName.isEmpty()) {
      return;
    }

    // Create Contract with fully qualified name
    String fqContractName = tenantName + ":" + contractName;
    Contract contract = getOrCreateContract(fqContractName);
    contract.setTenant(tenantName);
    contract.setDescription((String) attrs.get("descr"));
    contract.setScope((String) attrs.get("scope"));

    // Parse children for subjects
    if (contractMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) contractMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;
          if (childMap.containsKey("vzSubj")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> subjMap = (Map<String, Object>) childMap.get("vzSubj");
            parseContractSubjectFromMap(subjMap, contract, warnings);
          }
        }
      }
    }

    // Also add Contract to tenant's contract map (using fully-qualified name for global uniqueness)
    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getContracts().put(fqContractName, contract);
  }

  /** Parses an L2Out (Layer 2 Outside) from a raw map structure. */
  private void parseL2OutFromMap(
      Map<String, Object> l2outMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) l2outMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String l2outName = (String) attrs.get("name");
    if (l2outName == null || l2outName.isEmpty()) {
      return;
    }

    // Create L2Out with fully qualified name
    String fqL2OutName = tenantName + ":" + l2outName;
    L2Out l2out = new L2Out(l2outName);
    l2out.setTenant(tenantName);
    l2out.setDescription((String) attrs.get("descr"));

    // Parse children for bridge domain references
    if (l2outMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) l2outMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;
          // Look for l2extRsEBd (relation to external bridge domain)
          if (childMap.containsKey("l2extRsEBd")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsMap = (Map<String, Object>) childMap.get("l2extRsEBd");
            Map<String, Object> rsAttrs = (Map<String, Object>) rsMap.get("attributes");
            if (rsAttrs != null) {
              l2out.setBridgeDomain((String) rsAttrs.get("tnFvBDName"));
              l2out.setEncapsulation((String) rsAttrs.get("encap"));
            }
          }
        }
      }
    }

    _l2Outs.put(fqL2OutName, l2out);
  }

  /** Parses a contract interface (vzCPIf) from a raw map structure. */
  private void parseContractInterfaceFromMap(
      Map<String, Object> contractInterfaceMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) contractInterfaceMap.get("attributes");
    if (attrs == null) {
      return;
    }
    String name = (String) attrs.get("name");
    if (name == null || name.isEmpty()) {
      return;
    }

    String fqName = tenantName + ":" + name;
    ContractInterface contractInterface = getOrCreateContractInterface(fqName);
    contractInterface.setTenant(tenantName);
    contractInterface.setDescription((String) attrs.get("descr"));

    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getContractInterfaces().put(fqName, contractInterface);
  }

  /** Parses a taboo contract (vzTaboo) from a raw map structure. */
  private void parseTabooContractFromMap(
      Map<String, Object> tabooMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) tabooMap.get("attributes");
    if (attrs == null) {
      return;
    }
    String name = (String) attrs.get("name");
    if (name == null || name.isEmpty()) {
      return;
    }

    String fqName = tenantName + ":" + name;
    TabooContract taboo = getOrCreateTabooContract(fqName);
    taboo.setTenant(tenantName);
    taboo.setDescription((String) attrs.get("descr"));
    taboo.setScope((String) attrs.get("scope"));

    if (tabooMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) tabooMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;
          if (childMap.containsKey("vzSubj")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> subjMap = (Map<String, Object>) childMap.get("vzSubj");
            Contract.Subject subject = parseContractSubject(subjMap);
            if (subject != null) {
              taboo.getSubjects().add(subject);
            }
          }
        }
      }
    }

    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getTabooContracts().put(fqName, taboo);
  }

  /** Parses an L3Out (Layer 3 Outside) from a raw map structure. */
  private void parseL3OutFromMap(
      Map<String, Object> l3outMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) l3outMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String l3outName = (String) attrs.get("name");
    if (l3outName == null || l3outName.isEmpty()) {
      return;
    }

    // Create L3Out with fully qualified name
    String fqL3OutName = tenantName + ":" + l3outName;
    L3Out l3out = new L3Out(l3outName);
    l3out.setTenant(tenantName);
    l3out.setDescription((String) attrs.get("descr"));
    l3out.setEnforceRouteControl((String) attrs.get("enforceRtctrl"));
    l3out.setMplsEnabled((String) attrs.get("mplsEnabled"));
    l3out.setTargetDscp((String) attrs.get("targetDscp"));

    // Parse children for VRF reference and other relationships
    if (l3outMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) l3outMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // VRF relation (l3extRsEctx)
          if (childMap.containsKey("l3extRsEctx")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsMap = (Map<String, Object>) childMap.get("l3extRsEctx");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsAttrs = (Map<String, Object>) rsMap.get("attributes");
            if (rsAttrs != null) {
              String vrfName = (String) rsAttrs.get("tnFvCtxName");
              if (vrfName != null && !vrfName.isEmpty()) {
                l3out.setVrf(tenantName + ":" + vrfName);
              }
            }
          }

          // External EPG (l3extInstP) - contains subnets and static routes
          else if (childMap.containsKey("l3extInstP") || childMap.containsKey("l3ExtInstP")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> epgMap =
                (Map<String, Object>)
                    (childMap.containsKey("l3extInstP")
                        ? childMap.get("l3extInstP")
                        : childMap.get("l3ExtInstP"));
            parseExternalEpgFromMap(epgMap, l3out, warnings);
          }

          // BGP external policy (bgpExtP)
          else if (childMap.containsKey("bgpExtP")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> bgpExtPMap = (Map<String, Object>) childMap.get("bgpExtP");
            parseBgpExtPFromMap(bgpExtPMap, l3out, warnings);
          }

          // OSPF external policy (ospfExtP)
          else if (childMap.containsKey("ospfExtP")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> ospfExtPMap = (Map<String, Object>) childMap.get("ospfExtP");
            parseOspfExtPFromMap(ospfExtPMap, l3out, warnings);
          }

          // Logical Node Profile (l3extLNodeP) - contains node and interface attachments
          else if (childMap.containsKey("l3extLNodeP")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nodePMap = (Map<String, Object>) childMap.get("l3extLNodeP");
            parseL3ExtLNodePFromMap(nodePMap, l3out, warnings);
          }
        }
      }
    }

    _l3Outs.put(fqL3OutName, l3out);
  }

  /** Parses a logical node profile (l3extLNodeP) from a raw map structure. */
  private void parseL3ExtLNodePFromMap(
      Map<String, Object> nodePMap, L3Out l3out, Warnings warnings) {
    if (nodePMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) nodePMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // Logical Interface Profile (l3extLIfP) - contains path attachments
          if (childMap.containsKey("l3extLIfP")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> lifPMap = (Map<String, Object>) childMap.get("l3extLIfP");
            parseL3ExtLIfPFromMap(lifPMap, l3out, warnings);
          }
        }
      }
    }
  }

  /** Parses a logical interface profile (l3extLIfP) from a raw map structure. */
  private void parseL3ExtLIfPFromMap(Map<String, Object> lifPMap, L3Out l3out, Warnings warnings) {
    String ifProfileName = null;
    String ifDescription = null;

    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) lifPMap.get("attributes");
    if (attrs != null) {
      ifProfileName = (String) attrs.get("name");
      ifDescription = (String) attrs.get("descr");
    }

    if (lifPMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) lifPMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // Path attachment (l3extRsPathL3OutAtt)
          if (childMap.containsKey("l3extRsPathL3OutAtt")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> pathAttMap =
                (Map<String, Object>) childMap.get("l3extRsPathL3OutAtt");
            @SuppressWarnings("unchecked")
            Map<String, Object> pathAttrs = (Map<String, Object>) pathAttMap.get("attributes");
            if (pathAttrs != null) {
              PathAttachment pathAtt = new PathAttachment();
              pathAtt.setTargetDn((String) pathAttrs.get("tDn"));
              pathAtt.setEncapsulation((String) pathAttrs.get("encap"));
              pathAtt.setAddress((String) pathAttrs.get("addr"));
              pathAtt.setMac((String) pathAttrs.get("mac"));
              pathAtt.setMode((String) pathAttrs.get("mode"));
              pathAtt.setInterfaceType((String) pathAttrs.get("ifInstT"));
              pathAtt.setDescription(ifDescription);

              // Parse tDn to extract node ID and interface name
              // Examples:
              // - topology/pod-1/paths-1255/pathep-[eth1/29] (single node)
              // - topology/pod-1/protpaths-1255-1256/pathep-[PG_VPC_FW_1] (VPC pair)
              String tDn = pathAtt.getTargetDn();
              if (tDn != null) {
                // Extract node ID from "paths-N" pattern (single node)
                Pattern singlePathPattern = Pattern.compile("paths-(\\d+)/pathep-\\[(.+?)\\]");
                Matcher singleMatcher = singlePathPattern.matcher(tDn);
                if (singleMatcher.find()) {
                  pathAtt.setNodeId(singleMatcher.group(1));
                  pathAtt.setInterfaceName(singleMatcher.group(2));
                } else {
                  // For VPC pairs (protpaths-N-M), we don't set a single nodeId
                  // but still extract the interface name
                  Pattern vpcPathPattern =
                      Pattern.compile("protpaths-([\\d-]+)/pathep-\\[(.+?)\\]");
                  Matcher vpcMatcher = vpcPathPattern.matcher(tDn);
                  if (vpcMatcher.find()) {
                    // nodeId remains null for VPC (indicates multi-node)
                    pathAtt.setInterfaceName(vpcMatcher.group(2));
                  }
                }
              }

              l3out.getPathAttachments().add(pathAtt);
            }
          }
        }
      }
    }
  }

  /** Parses an external EPG (l3extInstP) from a raw map structure. */
  private void parseExternalEpgFromMap(Map<String, Object> epgMap, L3Out l3out, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) epgMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String epgName = (String) attrs.get("name");
    if (epgName == null || epgName.isEmpty()) {
      epgName = "extepg-" + l3out.getName();
    }
    ExternalEpg epg = new ExternalEpg(epgName);
    epg.setDescription((String) attrs.get("descr"));

    // Parse children for subnets and static routes
    if (epgMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) epgMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // External subnet (l3extSubnet)
          if (childMap.containsKey("l3extSubnet") || childMap.containsKey("l3ExtSubnet")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> subnetMap =
                (Map<String, Object>)
                    (childMap.containsKey("l3extSubnet")
                        ? childMap.get("l3extSubnet")
                        : childMap.get("l3ExtSubnet"));
            @SuppressWarnings("unchecked")
            Map<String, Object> subnetAttrs = (Map<String, Object>) subnetMap.get("attributes");
            if (subnetAttrs != null) {
              String subnet = (String) subnetAttrs.get("ip");
              if (subnet != null) {
                epg.getSubnets().add(subnet);
              }
            }
          }

          // Static route (ipRouteP)
          else if (childMap.containsKey("ipRouteP")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> routeMap = (Map<String, Object>) childMap.get("ipRouteP");
            @SuppressWarnings("unchecked")
            Map<String, Object> routeAttrs = (Map<String, Object>) routeMap.get("attributes");
            if (routeAttrs != null) {
              StaticRoute route = new StaticRoute();
              route.setPrefix((String) routeAttrs.get("ip"));
              route.setNextHop((String) routeAttrs.get("nextHop"));
              route.setAdministrativeDistance((String) routeAttrs.get("pref"));
              route.setNextHopInterface((String) routeAttrs.get("ifName"));
              l3out.getStaticRoutes().add(route);
            }
          }

          // Provided contracts
          if (childMap.containsKey("fvRsProv")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvMap = (Map<String, Object>) childMap.get("fvRsProv");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvAttrs = (Map<String, Object>) rsProvMap.get("attributes");
            String contractName = firstNonEmptyValue(rsProvAttrs, "tnVzBrCPName");
            if (contractName != null) {
              epg.getProvidedContracts().add(l3out.getTenant() + ":" + contractName);
            }
          }

          // Consumed contracts
          if (childMap.containsKey("fvRsCons")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsMap = (Map<String, Object>) childMap.get("fvRsCons");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsAttrs = (Map<String, Object>) rsConsMap.get("attributes");
            String contractName = firstNonEmptyValue(rsConsAttrs, "tnVzBrCPName");
            if (contractName != null) {
              epg.getConsumedContracts().add(l3out.getTenant() + ":" + contractName);
            }
          }

          // Provided contract interfaces
          if (childMap.containsKey("fvRsProvIf")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvIfMap = (Map<String, Object>) childMap.get("fvRsProvIf");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProvIfAttrs = (Map<String, Object>) rsProvIfMap.get("attributes");
            String contractInterfaceName = firstNonEmptyValue(rsProvIfAttrs, "tnVzCPIfName");
            if (contractInterfaceName != null) {
              epg.getProvidedContractInterfaces()
                  .add(l3out.getTenant() + ":" + contractInterfaceName);
            }
          }

          // Consumed contract interfaces
          if (childMap.containsKey("fvRsConsIf")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsIfMap = (Map<String, Object>) childMap.get("fvRsConsIf");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsConsIfAttrs = (Map<String, Object>) rsConsIfMap.get("attributes");
            String contractInterfaceName = firstNonEmptyValue(rsConsIfAttrs, "tnVzCPIfName");
            if (contractInterfaceName != null) {
              epg.getConsumedContractInterfaces()
                  .add(l3out.getTenant() + ":" + contractInterfaceName);
            }
          }

          // Taboo contracts protecting this external EPG
          if (childMap.containsKey("fvRsProtBy")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProtByMap = (Map<String, Object>) childMap.get("fvRsProtBy");
            @SuppressWarnings("unchecked")
            Map<String, Object> rsProtByAttrs = (Map<String, Object>) rsProtByMap.get("attributes");
            String tabooName = firstNonEmptyValue(rsProtByAttrs, "tnVzTabooName");
            if (tabooName != null) {
              epg.getProtectedByTaboos().add(l3out.getTenant() + ":" + tabooName);
            }
          }
        }
      }
    }

    l3out.getExternalEpgs().add(epg);
  }

  private static @Nullable String firstNonEmptyValue(
      @Nullable Map<String, Object> attributes, String... keys) {
    if (attributes == null) {
      return null;
    }
    for (String key : keys) {
      Object value = attributes.get(key);
      if (value instanceof String && !((String) value).isEmpty()) {
        return (String) value;
      }
    }
    return null;
  }

  /**
   * Normalizes ACI interface names to canonical form.
   *
   * <p>ACI uses short lowercase names in path attachment tDn values (e.g., "eth1/3", "po1"). This
   * method converts them to canonical names that match how interfaces are named in Batfish (e.g.,
   * "Ethernet1/3", "port-channel1").
   *
   * @param ifaceName The interface name from ACI (may be short form like "eth1/3")
   * @return The canonical interface name (e.g., "Ethernet1/3")
   */
  public static @Nonnull String normalizeInterfaceName(@Nonnull String ifaceName) {
    String lower = ifaceName.toLowerCase();

    // Handle Ethernet interfaces: eth1/3 -> Ethernet1/3
    if (lower.startsWith("eth") && lower.matches("eth\\d+/\\d+.*")) {
      return "Ethernet" + ifaceName.substring(3);
    }

    // Handle port-channel: po1 -> port-channel1
    if (lower.startsWith("po") && lower.matches("po\\d+.*")) {
      return "port-channel" + ifaceName.substring(2);
    }

    // Handle Loopback: lo0 -> Loopback0
    if (lower.startsWith("lo") && lower.matches("lo\\d+.*")) {
      return "Loopback" + ifaceName.substring(2);
    }

    // Handle Vlan: vl100 -> Vlan100
    if (lower.startsWith("vl") && lower.matches("vl\\d+.*")) {
      return "Vlan" + ifaceName.substring(2);
    }

    // Default: capitalize first letter and return as-is (for named interfaces like PG_VPC_FW_1)
    if (!ifaceName.isEmpty()) {
      return ifaceName.substring(0, 1).toUpperCase() + ifaceName.substring(1);
    }

    return ifaceName;
  }

  /** Parses a BGP external policy (bgpExtP) from a raw map structure. */
  private void parseBgpExtPFromMap(Map<String, Object> bgpExtPMap, L3Out l3out, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) bgpExtPMap.get("attributes");
    if (attrs == null) {
      return;
    }

    // Create or update BGP process config
    BgpProcess bgpProcess = l3out.getBgpProcess();
    if (bgpProcess == null) {
      bgpProcess = new BgpProcess();
      l3out.setBgpProcess(bgpProcess);
    }

    // Parse BGP process attributes
    String routerId = (String) attrs.get("routerId");
    if (routerId != null) {
      bgpProcess.setRouterId(routerId);
    }

    String asStr = (String) attrs.get("asn");
    if (asStr != null) {
      try {
        bgpProcess.setAs(Long.parseLong(asStr));
      } catch (NumberFormatException e) {
        warnings.redFlagf(
            "Invalid AS number '%s' in BGP process, ignoring: %s", asStr, e.getMessage());
      }
    }

    // Parse children for BGP peers
    if (bgpExtPMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) bgpExtPMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // BGP peer (bgpPeerP)
          if (childMap.containsKey("bgpPeerP")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> peerMap = (Map<String, Object>) childMap.get("bgpPeerP");
            @SuppressWarnings("unchecked")
            Map<String, Object> peerAttrs = (Map<String, Object>) peerMap.get("attributes");
            if (peerAttrs != null) {
              BgpPeer peer = new BgpPeer();
              peer.setPeerAddress((String) peerAttrs.get("addr"));
              peer.setRemoteAs((String) peerAttrs.get("asn"));
              peer.setDescription((String) peerAttrs.get("descr"));

              // Peer-specific attributes
              peer.setLocalAs((String) peerAttrs.get("localAsn"));
              peer.setPassword((String) peerAttrs.get("pwd"));

              l3out.getBgpPeers().add(peer);
            }
          }
        }
      }
    }
  }

  /** Parses an OSPF external policy (ospfExtP) from a raw map structure. */
  private void parseOspfExtPFromMap(
      Map<String, Object> ospfExtPMap, L3Out l3out, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) ospfExtPMap.get("attributes");
    if (attrs == null) {
      return;
    }

    OspfConfig ospfConfig = new OspfConfig();
    ospfConfig.setName((String) attrs.get("name"));
    ospfConfig.setDescription((String) attrs.get("descr"));

    // Parse OSPF process attributes
    String areaId = (String) attrs.get("area");
    if (areaId != null) {
      ospfConfig.setAreaId(areaId);
    }

    // Parse children for OSPF interface configs
    if (ospfExtPMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) ospfExtPMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // OSPF interface policy (ospfIfP)
          if (childMap.containsKey("ospfIfP")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> ifPMap = (Map<String, Object>) childMap.get("ospfIfP");
            @SuppressWarnings("unchecked")
            Map<String, Object> ifPAttrs = (Map<String, Object>) ifPMap.get("attributes");
            if (ifPAttrs != null) {
              OspfInterface ospfInterface = new OspfInterface();
              ospfInterface.setName((String) ifPAttrs.get("name"));
              ospfInterface.setDescription((String) ifPAttrs.get("descr"));

              String ospfCost = (String) ifPAttrs.get("cost");
              if (ospfCost != null) {
                try {
                  ospfInterface.setCost(Integer.parseInt(ospfCost));
                } catch (NumberFormatException e) {
                  warnings.redFlagf(
                      "Invalid OSPF cost '%s', using default: %s", ospfCost, e.getMessage());
                }
              }

              String helloInterval = (String) ifPAttrs.get("helloIntvl");
              if (helloInterval != null) {
                try {
                  ospfInterface.setHelloInterval(Integer.parseInt(helloInterval));
                } catch (NumberFormatException e) {
                  warnings.redFlagf(
                      "Invalid OSPF hello interval '%s', using default: %s",
                      helloInterval, e.getMessage());
                }
              }

              String deadInterval = (String) ifPAttrs.get("deadIntvl");
              if (deadInterval != null) {
                try {
                  ospfInterface.setDeadInterval(Integer.parseInt(deadInterval));
                } catch (NumberFormatException e) {
                  warnings.redFlagf(
                      "Invalid OSPF dead interval '%s', using default: %s",
                      deadInterval, e.getMessage());
                }
              }

              ospfConfig.getOspfInterfaces().add(ospfInterface);
            }
          }
        }
      }
    }

    l3out.setOspfConfig(ospfConfig);
  }

  /** Parses a contract subject from a raw map structure. */

  /** Parses a contract subject from a raw map structure. */
  private void parseContractSubjectFromMap(
      Map<String, Object> subjMap, Contract contract, Warnings warnings) {
    Contract.Subject subject = parseContractSubject(subjMap);
    if (subject != null) {
      contract.getSubjects().add(subject);
    }
  }

  private @Nullable Contract.Subject parseContractSubject(Map<String, Object> subjMap) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) subjMap.get("attributes");
    if (attrs == null) {
      return null;
    }

    String subjName = (String) attrs.get("name");
    Contract.Subject subject = new Contract.Subject();
    subject.setName(subjName);

    // Parse children for filter references
    if (subjMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) subjMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;

          // Filter reference (vzRsSubjFiltAtt)
          if (childMap.containsKey("vzRsSubjFiltAtt")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> filtMap = (Map<String, Object>) childMap.get("vzRsSubjFiltAtt");
            @SuppressWarnings("unchecked")
            Map<String, Object> filtAttrs = (Map<String, Object>) filtMap.get("attributes");
            if (filtAttrs != null) {
              String tnVzFilterName = (String) filtAttrs.get("tnVzFilterName");
              String action = (String) filtAttrs.get("action");

              Contract.FilterRef filter = new Contract.FilterRef();
              filter.setName(tnVzFilterName);
              filter.setAction(action);
              subject.getFilters().add(filter);
            }
          }
        }
      }
    }

    return subject;
  }

  /** Parses a Filter (vzFilter) from a raw map structure. */
  private void parseFilterFromMap(
      Map<String, Object> filterMap, String tenantName, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) filterMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String filterName = (String) attrs.get("name");
    if (filterName == null || filterName.isEmpty()) {
      return;
    }

    // Create Filter with fully qualified name
    String fqFilterName = tenantName + ":" + filterName;
    FilterModel filter = getOrCreateFilter(fqFilterName);
    filter.setTenant(tenantName);
    filter.setDescription((String) attrs.get("descr"));

    // Parse children for filter entries
    if (filterMap.containsKey("children")) {
      @SuppressWarnings("unchecked")
      List<Object> children = (List<Object>) filterMap.get("children");
      for (Object childObj : children) {
        if (childObj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> childMap = (Map<String, Object>) childObj;
          if (childMap.containsKey("vzEntry")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> entryMap = (Map<String, Object>) childMap.get("vzEntry");
            parseEntryFromMap(entryMap, filter, warnings);
          }
        }
      }
    }

    // Also add Filter to tenant's filter map
    Tenant tenant = getOrCreateTenant(tenantName);
    tenant.getFilters().put(fqFilterName, filter);
  }

  /** Parses a filter entry (vzEntry) from a raw map structure. */
  private void parseEntryFromMap(
      Map<String, Object> entryMap, FilterModel filter, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) entryMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String entryName = (String) attrs.get("name");
    FilterModel.Entry entry = new FilterModel.Entry();
    entry.setName(entryName);

    // Parse Ethernet type
    entry.setEtherType((String) attrs.get("etherT"));

    // Parse IP protocol
    entry.setProtocol((String) attrs.get("prot"));

    // Parse ports
    entry.setDestinationPort((String) attrs.get("dPort"));
    entry.setSourcePort((String) attrs.get("sPort"));
    entry.setDestinationFromPort((String) attrs.get("dFromPort"));
    entry.setDestinationToPort((String) attrs.get("dToPort"));
    entry.setSourceFromPort((String) attrs.get("sFromPort"));
    entry.setSourceToPort((String) attrs.get("sToPort"));

    // Parse ICMP types
    entry.setIcmpv4Type((String) attrs.get("icmpv4T"));
    entry.setIcmpv4Code((String) attrs.get("icmpv4C"));
    entry.setIcmpv6Type((String) attrs.get("icmpv6T"));
    entry.setIcmpv6Code((String) attrs.get("icmpv6C"));

    // Parse ARP
    entry.setArpOpcode((String) attrs.get("arpOpc"));

    // Parse fragment and stateful flags
    String applyToFrag = (String) attrs.get("applyToFrag");
    if (applyToFrag != null) {
      entry.setApplyToFragments("yes".equalsIgnoreCase(applyToFrag));
    }

    String stateful = (String) attrs.get("stateful");
    if (stateful != null) {
      entry.setStateful("yes".equalsIgnoreCase(stateful));
    }

    // Parse TCP rules
    entry.setTcpRules((String) attrs.get("tcpRules"));

    // Parse source/destination addresses
    entry.setSourceAddress((String) attrs.get("srcAddr"));
    entry.setDestinationAddress((String) attrs.get("dstAddr"));

    filter.getEntries().add(entry);
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_HOSTNAME)
  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  /**
   * Returns the map of tenant configurations.
   *
   * @return An immutable map of tenant names to tenant configurations
   */
  public @Nonnull Map<String, Tenant> getTenants() {
    return _tenants;
  }

  @JsonProperty(PROP_TENANTS)
  public void setTenants(Map<String, Tenant> tenants) {
    _tenants = new TreeMap<>(tenants);
  }

  /**
   * Returns the map of bridge domain configurations.
   *
   * @return An immutable map of bridge domain names to bridge domain configurations
   */
  public @Nonnull Map<String, BridgeDomain> getBridgeDomains() {
    return _bridgeDomains;
  }

  @JsonProperty(PROP_BRIDGE_DOMAINS)
  public void setBridgeDomains(Map<String, BridgeDomain> bridgeDomains) {
    _bridgeDomains = new TreeMap<>(bridgeDomains);
  }

  /**
   * Returns the map of VRF configurations.
   *
   * @return An immutable map of VRF names to VRF configurations
   */
  public @Nonnull Map<String, AciVrfModel> getVrfs() {
    return _vrfs;
  }

  @JsonProperty(PROP_VRFS)
  public void setVrfs(Map<String, AciVrfModel> vrfs) {
    _vrfs = new TreeMap<>(vrfs);
  }

  /**
   * Returns the map of EPG configurations.
   *
   * @return An immutable map of EPG names to EPG configurations
   */
  public @Nonnull Map<String, Epg> getEpgs() {
    return _epgs;
  }

  @JsonProperty(PROP_EPGS)
  public void setEpgs(Map<String, Epg> epgs) {
    _epgs = new TreeMap<>(epgs);
  }

  /**
   * Returns the map of Application Profile configurations.
   *
   * @return An immutable map of Application Profile names to configurations
   */
  public @Nonnull Map<String, ApplicationProfile> getApplicationProfiles() {
    return _applicationProfiles;
  }

  public void setApplicationProfiles(Map<String, ApplicationProfile> applicationProfiles) {
    _applicationProfiles = new TreeMap<>(applicationProfiles);
  }

  /**
   * Returns the map of contract configurations.
   *
   * @return An immutable map of contract names to contract configurations
   */
  public @Nonnull Map<String, Contract> getContracts() {
    return _contracts;
  }

  @JsonProperty(PROP_CONTRACTS)
  public void setContracts(Map<String, Contract> contracts) {
    _contracts = new TreeMap<>(contracts);
  }

  /** Returns the map of contract interface configurations. */
  public @Nonnull Map<String, ContractInterface> getContractInterfaces() {
    return _contractInterfaces;
  }

  @JsonProperty(PROP_CONTRACT_INTERFACES)
  public void setContractInterfaces(Map<String, ContractInterface> contractInterfaces) {
    _contractInterfaces = new TreeMap<>(contractInterfaces);
  }

  /**
   * Returns the map of filter configurations.
   *
   * @return An immutable map of filter names to filter configurations
   */
  public @Nonnull Map<String, FilterModel> getFilters() {
    return _filters;
  }

  @JsonProperty(PROP_FILTERS)
  public void setFilters(Map<String, FilterModel> filters) {
    _filters = new TreeMap<>(filters);
  }

  /** Returns the map of taboo contract configurations. */
  public @Nonnull Map<String, TabooContract> getTabooContracts() {
    return _tabooContracts;
  }

  @JsonProperty(PROP_TABOO_CONTRACTS)
  public void setTabooContracts(Map<String, TabooContract> tabooContracts) {
    _tabooContracts = new TreeMap<>(tabooContracts);
  }

  /**
   * Returns the map of fabric node configurations.
   *
   * @return An immutable map of node IDs to fabric node configurations
   */
  public @Nonnull Map<String, FabricNode> getFabricNodes() {
    return _fabricNodes;
  }

  @JsonProperty(PROP_FABRIC_NODES)
  public void setFabricNodes(Map<String, FabricNode> fabricNodes) {
    _fabricNodes = new TreeMap<>(fabricNodes);
  }

  /** Returns explicit fabric links parsed from optional fabricLink exports. */
  public @Nonnull List<AciFabricLink> getFabricLinks() {
    return _fabricLinks;
  }

  public void setFabricLinks(List<AciFabricLink> fabricLinks) {
    _fabricLinks = new ArrayList<>(fabricLinks);
  }

  /**
   * Returns the map of VPC pairs in the ACI fabric.
   *
   * @return An immutable map of VPC IDs to VPC pair configurations
   */
  public @Nonnull Map<String, AciVpcPair> getVpcPairs() {
    return _vpcPairs;
  }

  public void setVpcPairs(Map<String, AciVpcPair> vpcPairs) {
    _vpcPairs = new TreeMap<>(vpcPairs);
  }

  /**
   * Returns the Layer 1 topology edges connecting fabric nodes.
   *
   * <p>In ACI, fabric nodes are connected via a spine-leaf topology. This method returns the
   * physical edges representing those connections.
   *
   * @return Set of Layer 1 edges between fabric nodes
   */
  @Override
  public @Nonnull Set<Layer1Edge> getLayer1Edges() {
    return AciConversion.createLayer1Edges(this);
  }

  /**
   * Returns the map of inter-fabric connections.
   *
   * @return Map of connection IDs to inter-fabric connection configurations
   */
  public @Nonnull Map<String, AciInterFabricConnection> getInterFabricConnections() {
    return _interFabricConnections;
  }

  public void setInterFabricConnections(
      Map<String, AciInterFabricConnection> interFabricConnections) {
    _interFabricConnections = new TreeMap<>(interFabricConnections);
  }

  /**
   * Returns the map of path attachments linking EPGs to physical interfaces.
   *
   * @return Map of (nodeId, interfaceName) to PathAttachment details
   */
  public @Nonnull Map<
          String, Map<String, org.batfish.vendor.cisco_aci.representation.PathAttachment>>
      getPathAttachmentMap() {
    return _pathAttachmentMap;
  }

  /**
   * Returns the map of node IDs to interface names discovered from path attachments.
   *
   * @return Map of node IDs to sets of interface names (LinkedHashSet for uniqueness + insertion
   *     order)
   */
  public @Nonnull Map<String, Set<String>> getNodeInterfaces() {
    return _nodeInterfaces;
  }

  /**
   * Returns the map of L3Out configurations.
   *
   * @return An immutable map of L3Out names to L3Out configurations
   */
  public @Nonnull Map<String, org.batfish.vendor.cisco_aci.representation.L3Out> getL3Outs() {
    return _l3Outs;
  }

  @JsonProperty(PROP_L3_OUTS)
  public void setL3Outs(Map<String, org.batfish.vendor.cisco_aci.representation.L3Out> l3Outs) {
    _l3Outs = new TreeMap<>(l3Outs);
  }

  /**
   * Returns the map of L2Out names to L2Out configurations.
   *
   * @return An immutable map of L2Out names to L2Out configurations
   */
  public @Nonnull Map<String, org.batfish.vendor.cisco_aci.representation.L2Out> getL2Outs() {
    return _l2Outs;
  }

  @JsonProperty(PROP_L2_OUTS)
  public void setL2Outs(Map<String, org.batfish.vendor.cisco_aci.representation.L2Out> l2Outs) {
    _l2Outs = new TreeMap<>(l2Outs);
  }

  /**
   * Gets or creates a tenant with the given name.
   *
   * @param name The tenant name
   * @return The existing or newly created tenant
   */
  public @Nonnull Tenant getOrCreateTenant(String name) {
    return _tenants.computeIfAbsent(name, Tenant::new);
  }

  /**
   * Gets or creates a VRF with the given name.
   *
   * @param name The VRF name
   * @return The existing or newly created VRF
   */
  public @Nonnull AciVrfModel getOrCreateVrf(String name) {
    return _vrfs.computeIfAbsent(name, AciVrfModel::new);
  }

  /**
   * Gets or creates a bridge domain with the given name.
   *
   * @param name The bridge domain name
   * @return The existing or newly created bridge domain
   */
  public @Nonnull BridgeDomain getOrCreateBridgeDomain(String name) {
    return _bridgeDomains.computeIfAbsent(name, BridgeDomain::new);
  }

  /**
   * Gets or creates an EPG with the given name.
   *
   * @param name The EPG name
   * @return The existing or newly created EPG
   */
  public @Nonnull Epg getOrCreateEpg(String name) {
    return _epgs.computeIfAbsent(name, Epg::new);
  }

  /**
   * Gets or creates an Application Profile with the given name.
   *
   * @param name The Application Profile name
   * @return The existing or newly created Application Profile
   */
  public @Nonnull ApplicationProfile getOrCreateApplicationProfile(String name) {
    return _applicationProfiles.computeIfAbsent(name, ApplicationProfile::new);
  }

  /**
   * Gets or creates a contract with the given name.
   *
   * @param name The contract name
   * @return The existing or newly created contract
   */
  public @Nonnull Contract getOrCreateContract(String name) {
    return _contracts.computeIfAbsent(name, Contract::new);
  }

  /** Gets or creates a contract interface with the given name. */
  public @Nonnull ContractInterface getOrCreateContractInterface(String name) {
    return _contractInterfaces.computeIfAbsent(name, ContractInterface::new);
  }

  /**
   * Gets or creates a filter with the given name.
   *
   * @param name The filter name
   * @return The existing or newly created filter
   */
  public @Nonnull FilterModel getOrCreateFilter(String name) {
    return _filters.computeIfAbsent(name, FilterModel::new);
  }

  /** Gets or creates a taboo contract with the given name. */
  public @Nonnull TabooContract getOrCreateTabooContract(String name) {
    return _tabooContracts.computeIfAbsent(name, TabooContract::new);
  }

  /**
   * Gets or creates an L3Out with the given name.
   *
   * @param name The L3Out name
   * @return The existing or newly created L3Out
   */
  public @Nonnull org.batfish.vendor.cisco_aci.representation.L3Out getOrCreateL3Out(String name) {
    return _l3Outs.computeIfAbsent(name, L3Out::new);
  }

  /**
   * Converts this ACI configuration to vendor-independent Batfish configurations.
   *
   * <p>This method creates the vendor-independent Configuration objects that represent the ACI
   * fabric's network topology and policies in Batfish's vendor-independent model.
   *
   * <p>The conversion is delegated to the {@link AciConversion} utility class which handles the
   * detailed mapping from ACI structures to Batfish Configuration objects.
   *
   * @return A list of Configuration objects representing this ACI fabric
   * @throws VendorConversionException If conversion fails
   */
  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    // Delegate to the AciConversion utility class
    Warnings warnings = getWarnings();
    if (warnings == null) {
      warnings = new Warnings();
      setWarnings(warnings);
    }

    // Convert to VI configurations
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(this, warnings);

    // Return as a list
    return ImmutableList.copyOf(configs.values());
  }

  /**
   * Finalizes the configuration structures after parsing is complete.
   *
   * <p>This method makes internal data structures immutable and should be called once at the end of
   * parsing and extraction.
   */
  public void finalizeStructures() {
    _tenants = ImmutableMap.copyOf(_tenants);
    _bridgeDomains = ImmutableMap.copyOf(_bridgeDomains);
    _vrfs = ImmutableMap.copyOf(_vrfs);
    _epgs = ImmutableMap.copyOf(_epgs);
    _contracts = ImmutableMap.copyOf(_contracts);
    _contractInterfaces = ImmutableMap.copyOf(_contractInterfaces);
    _filters = ImmutableMap.copyOf(_filters);
    _tabooContracts = ImmutableMap.copyOf(_tabooContracts);
    _fabricNodes = ImmutableMap.copyOf(_fabricNodes);
    _fabricLinks = ImmutableList.copyOf(_fabricLinks);
    _l3Outs = ImmutableMap.copyOf(_l3Outs);
  }
}
