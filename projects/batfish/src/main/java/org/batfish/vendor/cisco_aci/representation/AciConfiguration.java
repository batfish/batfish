package org.batfish.vendor.cisco_aci.representation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.util.BatfishObjectMapper;
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
    // Parse the native ACI JSON structure (polUni root)
    // First, read as JsonNode to handle the wrapper object
    JsonNode rootNode = BatfishObjectMapper.mapper().readTree(text);

    // Extract the polUni object if it's wrapped (common in ACI exports)
    JsonNode polUniNode = rootNode.has("polUni") ? rootNode.get("polUni") : rootNode;

    // Now deserialize the polUni object
    AciPolUniInternal polUni =
        BatfishObjectMapper.mapper().treeToValue(polUniNode, AciPolUniInternal.class);

    // Create a new AciConfiguration and populate it from the parsed structure
    AciConfiguration aciConfiguration = new AciConfiguration();
    aciConfiguration.setWarnings(warnings);
    aciConfiguration.setFilename(filename);

    // Extract hostname from polUni attributes or use default
    String hostname = extractHostname(polUni, filename);
    aciConfiguration.setHostname(hostname);

    // Parse all elements from the nested structure
    aciConfiguration.parsePolUni(polUni, warnings);

    // Finalize the structures
    aciConfiguration.finalizeStructures();

    return aciConfiguration;
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
    // Parse the native ACI XML structure (polUni root) using XmlMapper
    AciPolUniInternal polUni =
        BatfishObjectMapper.xmlMapper().readValue(text, AciPolUniInternal.class);

    // Create a new AciConfiguration and populate it from the parsed structure
    AciConfiguration aciConfiguration = new AciConfiguration();
    aciConfiguration.setWarnings(warnings);
    aciConfiguration.setFilename(filename);

    // Extract hostname from polUni attributes or use default
    String hostname = extractHostname(polUni, filename);
    aciConfiguration.setHostname(hostname);

    // Parse all elements from the nested structure
    aciConfiguration.parsePolUni(polUni, warnings);

    // Finalize the structures
    aciConfiguration.finalizeStructures();

    return aciConfiguration;
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
    String trimmed = text.trim();
    if (trimmed.isEmpty()) {
      throw new IOException("Empty configuration file: " + filename);
    }

    char firstChar = trimmed.charAt(0);
    if (firstChar == '{') {
      // JSON format
      return fromJson(filename, text, warnings);
    } else if (firstChar == '<') {
      // XML format
      return fromXml(filename, text, warnings);
    } else {
      throw new IOException(
          "Unrecognized configuration format. Expected JSON (starting with '{') or "
              + "XML (starting with '<'), but file starts with '"
              + firstChar
              + "': "
              + filename);
    }
  }

  /** Extracts a hostname from the polUni structure. Uses filename-derived name if not found. */
  private static String extractHostname(AciPolUniInternal polUni, String filename) {
    if (polUni.getAttributes() != null && polUni.getAttributes().getName() != null) {
      return polUni.getAttributes().getName();
    }
    // Use filename to generate a deterministic hostname
    // Extract base name without extension and path
    String basename = filename.substring(filename.lastIndexOf('/') + 1);
    if (basename.contains(".")) {
      basename = basename.substring(0, basename.lastIndexOf('.'));
    }
    // Use a unique prefix that won't conflict with "aci-fabric"
    return "aci-" + basename;
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
  private Map<String, Filter> _filters;

  /** Map of taboo contract names to taboo contract configurations */
  private Map<String, TabooContract> _tabooContracts;

  /** Map of fabric node IDs to fabric node configurations */
  private Map<String, FabricNode> _fabricNodes;

  /** Map of VPC IDs to VPC pair configurations */
  private Map<String, VpcPair> _vpcPairs;

  /** Map of inter-fabric connection IDs to connection configurations */
  private Map<String, InterFabricConnection> _interFabricConnections;

  /** Map of (nodeId, interfaceName) to path attachment details */
  private Map<String, Map<String, PathAttachment>> _pathAttachmentMap;

  /**
   * Map of node IDs to interface names discovered from path attachments (LinkedHashSet for
   * uniqueness + insertion order)
   */
  private Map<String, Set<String>> _nodeInterfaces;

  /** Map of L3Out names to L3Out configurations */
  private Map<String, L3Out> _l3Outs;

  /** Map of L2Out names to L2Out configurations */
  private Map<String, L2Out> _l2Outs;

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
  private void parsePolUni(AciPolUniInternal polUni, Warnings warnings) {
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
            if (instChild.getFabricProtPol() != null) {
              AciFabricProtPol protPol = instChild.getFabricProtPol();
              if (protPol.getChildren() != null) {
                for (AciFabricProtPol.FabricProtPolChild protChild : protPol.getChildren()) {
                  if (protChild.getFabricExplicitGEp() != null) {
                    AciFabricExplicitGEp explicitEp = protChild.getFabricExplicitGEp();
                    if (explicitEp.getChildren() != null) {
                      for (AciFabricExplicitGEp.FabricExplicitGEpChild expChild :
                          explicitEp.getChildren()) {
                        // Parse fabricNodeIdentP to get node names
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

      // ALSO parse from ctrlrInst (controller) - this is where the real node names are!
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
          AciFabricNodePEp.AciInterface ifaceObj = nodeChild.getFabricInterface();
          if (ifaceObj.getAttributes() != null) {
            AciFabricNodePEp.AciInterface.AciInterfaceAttributes ifaceAttrs =
                ifaceObj.getAttributes();
            String ifaceName = ifaceAttrs.getName();
            if (ifaceName != null && !ifaceName.isEmpty()) {
              FabricNode.Interface iface = new FabricNode.Interface();
              iface.setName(ifaceName);
              iface.setEnabled(true); // ACI interfaces are enabled by default
              // Store additional interface attributes if needed
              fabricNode.getInterfaces().put(ifaceName, iface);
            }
          }
        }
        // Parse l1PhysIf (layer 1 physical interface)
        if (nodeChild.getL1PhysIf() != null) {
          AciFabricNodePEp.AciL1PhysIf l1PhysIf = nodeChild.getL1PhysIf();
          if (l1PhysIf.getAttributes() != null) {
            AciFabricNodePEp.AciL1PhysIf.AciL1PhysIfAttributes l1Attrs = l1PhysIf.getAttributes();
            String ifaceId = l1Attrs.getId();
            if (ifaceId != null && !ifaceId.isEmpty()) {
              // Avoid duplicates - only add if not already present
              if (!fabricNode.getInterfaces().containsKey(ifaceId)) {
                FabricNode.Interface iface = new FabricNode.Interface();
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
    // Node names are typically in format: SW-DC1-{Role}-{NodeID}-{Set}
    // Or shorter formats like: Spine-CNAA03, Leaf227, etc.
    // Extract the role part (case-insensitive)
    String lowerName = nodeName.toLowerCase();

    // Check for SW-DC-* format (with dashes around role)
    if (lowerName.contains("-leaf-")) {
      return "leaf";
    } else if (lowerName.contains("-spine-")) {
      return "spine";
    } else if (lowerName.contains("-services-")) {
      // Services nodes are leaf switches that provide connectivity to services
      return "leaf";
    }

    // Check for shorter formats: Spine-*, Leaf-*, or prefix patterns
    if (lowerName.startsWith("spine") || lowerName.startsWith("spine-")) {
      return "spine";
    } else if (lowerName.startsWith("leaf") || lowerName.startsWith("leaf-")) {
      return "leaf";
    }

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
                        VpcPair vpcPair =
                            new VpcPair(vpcId, vpcName, nodeIds.get(0), nodeIds.get(1));
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
        ManagementInfo mgmtInfo = new ManagementInfo();
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
        }
      }
    }

    _l3Outs.put(fqL3OutName, l3out);
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

              Contract.Filter filter = new Contract.Filter();
              filter.setName(tnVzFilterName);
              if ("deny".equalsIgnoreCase(action)) {
                // Action is deny - mark on filter
              }
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
    Filter filter = getOrCreateFilter(fqFilterName);
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
  private void parseEntryFromMap(Map<String, Object> entryMap, Filter filter, Warnings warnings) {
    @SuppressWarnings("unchecked")
    Map<String, Object> attrs = (Map<String, Object>) entryMap.get("attributes");
    if (attrs == null) {
      return;
    }

    String entryName = (String) attrs.get("name");
    Filter.Entry entry = new Filter.Entry();
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
  public @Nonnull Map<String, Filter> getFilters() {
    return _filters;
  }

  @JsonProperty(PROP_FILTERS)
  public void setFilters(Map<String, Filter> filters) {
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

  /**
   * Returns the map of VPC pairs in the ACI fabric.
   *
   * @return An immutable map of VPC IDs to VPC pair configurations
   */
  public @Nonnull Map<String, VpcPair> getVpcPairs() {
    return _vpcPairs;
  }

  public void setVpcPairs(Map<String, VpcPair> vpcPairs) {
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
  public @Nonnull Map<String, InterFabricConnection> getInterFabricConnections() {
    return _interFabricConnections;
  }

  public void setInterFabricConnections(Map<String, InterFabricConnection> interFabricConnections) {
    _interFabricConnections = new TreeMap<>(interFabricConnections);
  }

  /**
   * Returns the map of path attachments linking EPGs to physical interfaces.
   *
   * @return Map of (nodeId, interfaceName) to PathAttachment details
   */
  public @Nonnull Map<String, Map<String, PathAttachment>> getPathAttachmentMap() {
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
  public @Nonnull Map<String, L3Out> getL3Outs() {
    return _l3Outs;
  }

  @JsonProperty(PROP_L3_OUTS)
  public void setL3Outs(Map<String, L3Out> l3Outs) {
    _l3Outs = new TreeMap<>(l3Outs);
  }

  /**
   * Returns the map of L2Out names to L2Out configurations.
   *
   * @return An immutable map of L2Out names to L2Out configurations
   */
  public @Nonnull Map<String, L2Out> getL2Outs() {
    return _l2Outs;
  }

  @JsonProperty(PROP_L2_OUTS)
  public void setL2Outs(Map<String, L2Out> l2Outs) {
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
  public @Nonnull Filter getOrCreateFilter(String name) {
    return _filters.computeIfAbsent(name, Filter::new);
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
  public @Nonnull L3Out getOrCreateL3Out(String name) {
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
    _l3Outs = ImmutableMap.copyOf(_l3Outs);
  }

  /**
   * ACI Tenant configuration.
   *
   * <p>A tenant is a logical container for application policies in ACI. It contains bridge domains,
   * VRFs, EPGs, and contracts.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Tenant implements Serializable {
    private final String _name;
    private Map<String, BridgeDomain> _bridgeDomains;
    private Map<String, AciVrfModel> _vrfs;
    private Map<String, Epg> _epgs;
    private Map<String, Contract> _contracts;
    private Map<String, ContractInterface> _contractInterfaces;
    private Map<String, Filter> _filters;
    private Map<String, TabooContract> _tabooContracts;

    public Tenant(String name) {
      _name = name;
      _bridgeDomains = new TreeMap<>();
      _vrfs = new TreeMap<>();
      _epgs = new TreeMap<>();
      _contracts = new TreeMap<>();
      _contractInterfaces = new TreeMap<>();
      _filters = new TreeMap<>();
      _tabooContracts = new TreeMap<>();
    }

    public String getName() {
      return _name;
    }

    public Map<String, BridgeDomain> getBridgeDomains() {
      return _bridgeDomains;
    }

    public void setBridgeDomains(Map<String, BridgeDomain> bridgeDomains) {
      _bridgeDomains = new TreeMap<>(bridgeDomains);
    }

    public Map<String, AciVrfModel> getVrfs() {
      return _vrfs;
    }

    public void setVrfs(Map<String, AciVrfModel> vrfs) {
      _vrfs = new TreeMap<>(vrfs);
    }

    public Map<String, Epg> getEpgs() {
      return _epgs;
    }

    public void setEpgs(Map<String, Epg> epgs) {
      _epgs = new TreeMap<>(epgs);
    }

    public Map<String, Contract> getContracts() {
      return _contracts;
    }

    public void setContracts(Map<String, Contract> contracts) {
      _contracts = new TreeMap<>(contracts);
    }

    public Map<String, ContractInterface> getContractInterfaces() {
      return _contractInterfaces;
    }

    public void setContractInterfaces(Map<String, ContractInterface> contractInterfaces) {
      _contractInterfaces = new TreeMap<>(contractInterfaces);
    }

    public Map<String, Filter> getFilters() {
      return _filters;
    }

    public void setFilters(Map<String, Filter> filters) {
      _filters = new TreeMap<>(filters);
    }

    public Map<String, TabooContract> getTabooContracts() {
      return _tabooContracts;
    }

    public void setTabooContracts(Map<String, TabooContract> tabooContracts) {
      _tabooContracts = new TreeMap<>(tabooContracts);
    }
  }

  /**
   * ACI Bridge Domain configuration.
   *
   * <p>A bridge domain is a Layer 2 forwarding domain within a tenant. It contains subnets and can
   * be associated with a VRF.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BridgeDomain implements Serializable {
    private final String _name;
    private String _vrf;
    private String _tenant;
    private List<String> _subnets;
    private String _description;
    private String _encapsulation; // VLAN encapsulation (e.g., "vlan-100")

    public BridgeDomain(String name) {
      _name = name;
      _subnets = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getVrf() {
      return _vrf;
    }

    public void setVrf(String vrf) {
      _vrf = vrf;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public List<String> getSubnets() {
      return _subnets;
    }

    public void setSubnets(List<String> subnets) {
      _subnets = new ArrayList<>(subnets);
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public @Nullable String getEncapsulation() {
      return _encapsulation;
    }

    public void setEncapsulation(String encapsulation) {
      _encapsulation = encapsulation;
    }
  }

  /**
   * ACI Application Profile (fvAp) configuration.
   *
   * <p>An Application Profile is a logical container for EPGs that belong to the same application
   * tier or application.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ApplicationProfile implements Serializable {
    private final String _name;
    private String _tenant;
    private String _description;
    private List<String> _epgNames;

    public ApplicationProfile(String name) {
      _name = name;
      _epgNames = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public List<String> getEpgNames() {
      return _epgNames;
    }

    public void addEpg(String epgName) {
      _epgNames.add(epgName);
    }
  }

  /**
   * ACI End Point Group (EPG) configuration.
   *
   * <p>An EPG is a collection of endpoints that share similar policy requirements. EPGs are the
   * fundamental building blocks for ACI policy application.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Epg implements Serializable {
    private final String _name;
    private String _tenant;
    private String _applicationProfile;
    private String _bridgeDomain;
    private String _description;
    private List<String> _providedContracts;
    private List<String> _consumedContracts;
    private List<String> _providedContractInterfaces;
    private List<String> _consumedContractInterfaces;
    private List<String> _protectedByTaboos;

    public Epg(String name) {
      _name = name;
      _providedContracts = new ArrayList<>();
      _consumedContracts = new ArrayList<>();
      _providedContractInterfaces = new ArrayList<>();
      _consumedContractInterfaces = new ArrayList<>();
      _protectedByTaboos = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getApplicationProfile() {
      return _applicationProfile;
    }

    public void setApplicationProfile(String applicationProfile) {
      _applicationProfile = applicationProfile;
    }

    public @Nullable String getBridgeDomain() {
      return _bridgeDomain;
    }

    public void setBridgeDomain(String bridgeDomain) {
      _bridgeDomain = bridgeDomain;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public List<String> getProvidedContracts() {
      return _providedContracts;
    }

    public void setProvidedContracts(List<String> providedContracts) {
      _providedContracts = new ArrayList<>(providedContracts);
    }

    public List<String> getConsumedContracts() {
      return _consumedContracts;
    }

    public void setConsumedContracts(List<String> consumedContracts) {
      _consumedContracts = new ArrayList<>(consumedContracts);
    }

    public List<String> getProvidedContractInterfaces() {
      return _providedContractInterfaces;
    }

    public void setProvidedContractInterfaces(List<String> providedContractInterfaces) {
      _providedContractInterfaces = new ArrayList<>(providedContractInterfaces);
    }

    public List<String> getConsumedContractInterfaces() {
      return _consumedContractInterfaces;
    }

    public void setConsumedContractInterfaces(List<String> consumedContractInterfaces) {
      _consumedContractInterfaces = new ArrayList<>(consumedContractInterfaces);
    }

    public List<String> getProtectedByTaboos() {
      return _protectedByTaboos;
    }

    public void setProtectedByTaboos(List<String> protectedByTaboos) {
      _protectedByTaboos = new ArrayList<>(protectedByTaboos);
    }
  }

  /**
   * ACI Contract configuration.
   *
   * <p>A contract defines the allowed communication between EPGs. It contains subjects and filters
   * that specify the protocols and ports for communication.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Contract implements Serializable {
    private final String _name;
    private String _tenant;
    private String _description;
    private List<Subject> _subjects;
    private String _scope;

    public Contract(String name) {
      _name = name;
      _subjects = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public List<Subject> getSubjects() {
      return _subjects;
    }

    public void setSubjects(List<Subject> subjects) {
      _subjects = new ArrayList<>(subjects);
    }

    public @Nullable String getScope() {
      return _scope;
    }

    public void setScope(String scope) {
      _scope = scope;
    }

    /** A contract subject contains filters that define specific traffic rules. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Subject implements Serializable {
      private String _name;
      private List<Filter> _filters;

      public Subject() {
        _filters = new ArrayList<>();
      }

      public @Nullable String getName() {
        return _name;
      }

      public void setName(String name) {
        _name = name;
      }

      public List<Filter> getFilters() {
        return _filters;
      }

      public void setFilters(List<Filter> filters) {
        _filters = new ArrayList<>(filters);
      }
    }

    /** A contract filter defines specific traffic matching criteria (protocols, ports). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Filter implements Serializable {
      private String _name;
      private String _etherType;
      private String _ipProtocol;
      private List<String> _sourcePorts;
      private List<String> _destinationPorts;
      private String _sourceAddress;
      private String _destinationAddress;
      private String _icmpType;
      private String _icmpCode;
      private String _arpOpcode;
      private Boolean _stateful;

      public Filter() {
        _sourcePorts = new ArrayList<>();
        _destinationPorts = new ArrayList<>();
      }

      public @Nullable String getName() {
        return _name;
      }

      public void setName(String name) {
        _name = name;
      }

      public @Nullable String getEtherType() {
        return _etherType;
      }

      public void setEtherType(String etherType) {
        _etherType = etherType;
      }

      public @Nullable String getIpProtocol() {
        return _ipProtocol;
      }

      public void setIpProtocol(String ipProtocol) {
        _ipProtocol = ipProtocol;
      }

      public List<String> getSourcePorts() {
        return _sourcePorts;
      }

      public void setSourcePorts(List<String> sourcePorts) {
        _sourcePorts = new ArrayList<>(sourcePorts);
      }

      public List<String> getDestinationPorts() {
        return _destinationPorts;
      }

      public void setDestinationPorts(List<String> destinationPorts) {
        _destinationPorts = new ArrayList<>(destinationPorts);
      }

      public @Nullable String getSourceAddress() {
        return _sourceAddress;
      }

      public void setSourceAddress(String sourceAddress) {
        _sourceAddress = sourceAddress;
      }

      public @Nullable String getDestinationAddress() {
        return _destinationAddress;
      }

      public void setDestinationAddress(String destinationAddress) {
        _destinationAddress = destinationAddress;
      }

      public @Nullable String getIcmpType() {
        return _icmpType;
      }

      public void setIcmpType(String icmpType) {
        _icmpType = icmpType;
      }

      public @Nullable String getIcmpCode() {
        return _icmpCode;
      }

      public void setIcmpCode(String icmpCode) {
        _icmpCode = icmpCode;
      }

      public @Nullable String getArpOpcode() {
        return _arpOpcode;
      }

      public void setArpOpcode(String arpOpcode) {
        _arpOpcode = arpOpcode;
      }

      public @Nullable Boolean getStateful() {
        return _stateful;
      }

      public void setStateful(Boolean stateful) {
        _stateful = stateful;
      }
    }
  }

  /** ACI Contract Interface (vzCPIf) configuration. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ContractInterface implements Serializable {
    private final String _name;
    private String _tenant;
    private String _description;

    public ContractInterface(String name) {
      _name = name;
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }
  }

  /** ACI Taboo Contract (vzTaboo) configuration. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class TabooContract implements Serializable {
    private final String _name;
    private String _tenant;
    private String _description;
    private String _scope;
    private List<Contract.Subject> _subjects;

    public TabooContract(String name) {
      _name = name;
      _subjects = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public @Nullable String getScope() {
      return _scope;
    }

    public void setScope(String scope) {
      _scope = scope;
    }

    public List<Contract.Subject> getSubjects() {
      return _subjects;
    }

    public void setSubjects(List<Contract.Subject> subjects) {
      _subjects = new ArrayList<>(subjects);
    }
  }

  /**
   * ACI Filter configuration (vzFilter).
   *
   * <p>Filters define Layer 2 to Layer 4 traffic classification rules. A filter contains one or
   * more entries that specify match criteria such as Ethernet type, IP protocol, TCP/UDP ports, and
   * ICMP types. Filters are referenced by contract subjects to define allowed traffic patterns
   * between endpoint groups.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Filter implements Serializable {
    private final String _name;
    private String _tenant;
    private String _description;
    private List<Entry> _entries;

    public Filter(String name) {
      _name = name;
      _entries = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public List<Entry> getEntries() {
      return _entries;
    }

    public void setEntries(List<Entry> entries) {
      _entries = new ArrayList<>(entries);
    }

    /** A filter entry defines specific traffic matching criteria (protocols, ports, etc.). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry implements Serializable {
      private String _name;
      private String _etherType;
      private String _protocol;
      private String _destinationPort;
      private String _sourcePort;
      private String _destinationFromPort;
      private String _destinationToPort;
      private String _sourceFromPort;
      private String _sourceToPort;
      private String _icmpv4Type;
      private String _icmpv4Code;
      private String _icmpv6Type;
      private String _icmpv6Code;
      private String _arpOpcode;
      private Boolean _applyToFragments;
      private Boolean _stateful;
      private String _tcpRules;
      private String _sourceAddress;
      private String _destinationAddress;

      public Entry() {}

      public @Nullable String getName() {
        return _name;
      }

      public void setName(String name) {
        _name = name;
      }

      public @Nullable String getEtherType() {
        return _etherType;
      }

      public void setEtherType(String etherType) {
        _etherType = etherType;
      }

      public @Nullable String getProtocol() {
        return _protocol;
      }

      public void setProtocol(String protocol) {
        _protocol = protocol;
      }

      public @Nullable String getDestinationPort() {
        return _destinationPort;
      }

      public void setDestinationPort(String destinationPort) {
        _destinationPort = destinationPort;
      }

      public @Nullable String getSourcePort() {
        return _sourcePort;
      }

      public void setSourcePort(String sourcePort) {
        _sourcePort = sourcePort;
      }

      public @Nullable String getDestinationFromPort() {
        return _destinationFromPort;
      }

      public void setDestinationFromPort(String destinationFromPort) {
        _destinationFromPort = destinationFromPort;
      }

      public @Nullable String getDestinationToPort() {
        return _destinationToPort;
      }

      public void setDestinationToPort(String destinationToPort) {
        _destinationToPort = destinationToPort;
      }

      public @Nullable String getSourceFromPort() {
        return _sourceFromPort;
      }

      public void setSourceFromPort(String sourceFromPort) {
        _sourceFromPort = sourceFromPort;
      }

      public @Nullable String getSourceToPort() {
        return _sourceToPort;
      }

      public void setSourceToPort(String sourceToPort) {
        _sourceToPort = sourceToPort;
      }

      public @Nullable String getIcmpv4Type() {
        return _icmpv4Type;
      }

      public void setIcmpv4Type(String icmpv4Type) {
        _icmpv4Type = icmpv4Type;
      }

      public @Nullable String getIcmpv4Code() {
        return _icmpv4Code;
      }

      public void setIcmpv4Code(String icmpv4Code) {
        _icmpv4Code = icmpv4Code;
      }

      public @Nullable String getIcmpv6Type() {
        return _icmpv6Type;
      }

      public void setIcmpv6Type(String icmpv6Type) {
        _icmpv6Type = icmpv6Type;
      }

      public @Nullable String getIcmpv6Code() {
        return _icmpv6Code;
      }

      public void setIcmpv6Code(String icmpv6Code) {
        _icmpv6Code = icmpv6Code;
      }

      public @Nullable String getArpOpcode() {
        return _arpOpcode;
      }

      public void setArpOpcode(String arpOpcode) {
        _arpOpcode = arpOpcode;
      }

      public @Nullable Boolean getApplyToFragments() {
        return _applyToFragments;
      }

      public void setApplyToFragments(Boolean applyToFragments) {
        _applyToFragments = applyToFragments;
      }

      public @Nullable Boolean getStateful() {
        return _stateful;
      }

      public void setStateful(Boolean stateful) {
        _stateful = stateful;
      }

      public @Nullable String getTcpRules() {
        return _tcpRules;
      }

      public void setTcpRules(String tcpRules) {
        _tcpRules = tcpRules;
      }

      public @Nullable String getSourceAddress() {
        return _sourceAddress;
      }

      public void setSourceAddress(String sourceAddress) {
        _sourceAddress = sourceAddress;
      }

      public @Nullable String getDestinationAddress() {
        return _destinationAddress;
      }

      public void setDestinationAddress(String destinationAddress) {
        _destinationAddress = destinationAddress;
      }
    }
  }

  /**
   * ACI VPC (Virtual Port Channel) pair configuration.
   *
   * <p>Represents a VPC pair that consists of two fabric nodes connected via a peer-link.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class VpcPair implements Serializable {
    private String _vpcId;
    private String _vpcName;
    private String _peer1NodeId;
    private String _peer2NodeId;

    public VpcPair() {}

    public VpcPair(String vpcId, String vpcName, String peer1NodeId, String peer2NodeId) {
      _vpcId = vpcId;
      _vpcName = vpcName;
      _peer1NodeId = peer1NodeId;
      _peer2NodeId = peer2NodeId;
    }

    public @Nullable String getVpcId() {
      return _vpcId;
    }

    public void setVpcId(String vpcId) {
      _vpcId = vpcId;
    }

    public @Nullable String getVpcName() {
      return _vpcName;
    }

    public void setVpcName(String vpcName) {
      _vpcName = vpcName;
    }

    public @Nullable String getPeer1NodeId() {
      return _peer1NodeId;
    }

    public void setPeer1NodeId(String peer1NodeId) {
      _peer1NodeId = peer1NodeId;
    }

    public @Nullable String getPeer2NodeId() {
      return _peer2NodeId;
    }

    public void setPeer2NodeId(String peer2NodeId) {
      _peer2NodeId = peer2NodeId;
    }
  }

  /**
   * ACI Inter-Fabric Connection configuration.
   *
   * <p>Represents detected connections between ACI fabrics (e.g., DC1, DC2) via shared external
   * networks, BGP peers, or L3Out configurations.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InterFabricConnection implements Serializable {
    private String _fabric1;
    private String _fabric2;
    private String _connectionType; // "l3out", "bgp", "shared-external", "mpls"
    private String _l3OutName1;
    private String _l3OutName2;
    private List<String> _sharedSubnets;
    private List<String> _bgpPeers;
    private String _description;

    public InterFabricConnection() {
      _sharedSubnets = new ArrayList<>();
      _bgpPeers = new ArrayList<>();
    }

    public InterFabricConnection(
        String fabric1, String fabric2, String connectionType, String description) {
      this();
      _fabric1 = fabric1;
      _fabric2 = fabric2;
      _connectionType = connectionType;
      _description = description;
    }

    public @Nullable String getFabric1() {
      return _fabric1;
    }

    public void setFabric1(String fabric1) {
      _fabric1 = fabric1;
    }

    public @Nullable String getFabric2() {
      return _fabric2;
    }

    public void setFabric2(String fabric2) {
      _fabric2 = fabric2;
    }

    public @Nullable String getConnectionType() {
      return _connectionType;
    }

    public void setConnectionType(String connectionType) {
      _connectionType = connectionType;
    }

    public @Nullable String getL3OutName1() {
      return _l3OutName1;
    }

    public void setL3OutName1(String l3OutName1) {
      _l3OutName1 = l3OutName1;
    }

    public @Nullable String getL3OutName2() {
      return _l3OutName2;
    }

    public void setL3OutName2(String l3OutName2) {
      _l3OutName2 = l3OutName2;
    }

    public List<String> getSharedSubnets() {
      return _sharedSubnets;
    }

    public void setSharedSubnets(List<String> sharedSubnets) {
      _sharedSubnets = new ArrayList<>(sharedSubnets);
    }

    public void addSharedSubnet(String subnet) {
      _sharedSubnets.add(subnet);
    }

    public List<String> getBgpPeers() {
      return _bgpPeers;
    }

    public void setBgpPeers(List<String> bgpPeers) {
      _bgpPeers = new ArrayList<>(bgpPeers);
    }

    public void addBgpPeer(String bgpPeer) {
      _bgpPeers.add(bgpPeer);
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }
  }

  /**
   * ACI Fabric Node configuration.
   *
   * <p>A fabric node represents a physical or virtual switch in the ACI fabric. It contains
   * interface and connectivity information.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FabricNode implements Serializable {
    private String _nodeId;
    private String _name;
    private String _role;
    private String _podId;
    private Map<String, Interface> _interfaces;

    public FabricNode() {
      _interfaces = new TreeMap<>();
    }

    public @Nullable String getNodeId() {
      return _nodeId;
    }

    public void setNodeId(String nodeId) {
      _nodeId = nodeId;
    }

    public @Nullable String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public @Nullable String getRole() {
      return _role;
    }

    public void setRole(String role) {
      _role = role;
    }

    public @Nullable String getPodId() {
      return _podId;
    }

    public void setPodId(String podId) {
      _podId = podId;
    }

    public Map<String, Interface> getInterfaces() {
      return _interfaces;
    }

    public void setInterfaces(Map<String, Interface> interfaces) {
      _interfaces = new TreeMap<>(interfaces);
    }

    /** Interface configuration on a fabric node. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interface implements Serializable {
      private String _name;
      private String _type;
      private String _description;
      private boolean _enabled;
      private String _epg;
      private String _vlan;

      public Interface() {
        _enabled = true;
      }

      public @Nullable String getName() {
        return _name;
      }

      public void setName(String name) {
        _name = name;
      }

      public @Nullable String getType() {
        return _type;
      }

      public void setType(String type) {
        _type = type;
      }

      public @Nullable String getDescription() {
        return _description;
      }

      public void setDescription(String description) {
        _description = description;
      }

      public boolean isEnabled() {
        return _enabled;
      }

      public void setEnabled(boolean enabled) {
        _enabled = enabled;
      }

      public @Nullable String getEpg() {
        return _epg;
      }

      public void setEpg(String epg) {
        _epg = epg;
      }

      public @Nullable String getVlan() {
        return _vlan;
      }

      public void setVlan(String vlan) {
        _vlan = vlan;
      }
    }

    /** Management information for the fabric node (out-of-band management). */
    private ManagementInfo _managementInfo;

    public @Nullable ManagementInfo getManagementInfo() {
      return _managementInfo;
    }

    public void setManagementInfo(@Nullable ManagementInfo managementInfo) {
      _managementInfo = managementInfo;
    }
  }

  /*
   * ========================================================================
   * Native ACI JSON POJOs
   * ========================================================================
   *
   * The following classes represent the native ACI JSON structure with polUni
   * as the root and nested children arrays. These are used by Jackson to
   * deserialize the ACI JSON export.
   */

  /**
   * Root element of the ACI JSON structure. The polUni (Policy Universe) is the top-level container
   * for all ACI configuration.
   *
   * <p>This is an internal version with PolUniChild for deserialization. The standalone {@link
   * AciPolUni} uses AciChild for a more generic structure.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonDeserialize(using = AciPolUniDeserializer.class)
  public static class AciPolUniInternal implements Serializable {
    private AciPolUniInternalAttributes _attributes;
    private List<PolUniChild> _children;

    public @Nullable AciPolUniInternalAttributes getAttributes() {
      return _attributes;
    }

    public void setAttributes(@Nullable AciPolUniInternalAttributes attributes) {
      _attributes = attributes;
    }

    public @Nullable List<PolUniChild> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<PolUniChild> children) {
      _children = children;
    }

    /** Attributes of the polUni root element. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AciPolUniInternalAttributes implements Serializable {
      @JsonProperty("annotation")
      private @Nullable String _annotation;

      @JsonProperty("dn")
      private @Nullable String _distinguishedName;

      @JsonProperty("name")
      private @Nullable String _name;

      @JsonProperty("nameAlias")
      private @Nullable String _nameAlias;

      @JsonProperty("userdom")
      private @Nullable String _userDomain;

      public @Nullable String getName() {
        return _name;
      }

      public void setName(@Nullable String name) {
        _name = name;
      }
    }

    /** Child elements at the polUni level. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PolUniChild implements Serializable {
      private @Nullable AciTenant _fvTenant;

      private @Nullable AciFabricInst _fabricInst;

      private @Nullable AciCtrlrInst _ctrlrInst;

      @JsonProperty("fvTenant")
      public @Nullable AciTenant getFvTenant() {
        return _fvTenant;
      }

      @JsonProperty("fvTenant")
      public void setFvTenant(@Nullable AciTenant fvTenant) {
        _fvTenant = fvTenant;
      }

      @JsonProperty("fabricInst")
      public @Nullable AciFabricInst getFabricInst() {
        return _fabricInst;
      }

      @JsonProperty("fabricInst")
      public void setFabricInst(@Nullable AciFabricInst fabricInst) {
        _fabricInst = fabricInst;
      }

      @JsonProperty("ctrlrInst")
      public @Nullable AciCtrlrInst getCtrlrInst() {
        return _ctrlrInst;
      }

      @JsonProperty("ctrlrInst")
      public void setCtrlrInst(@Nullable AciCtrlrInst ctrlrInst) {
        _ctrlrInst = ctrlrInst;
      }
    }
  }

  /**
   * Custom deserializer for AciPolUniInternal that handles the heterogenous children array
   * structure.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciPolUniDeserializer extends JsonDeserializer<AciPolUniInternal>
      implements Serializable {
    @Override
    public AciPolUniInternal deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      JsonNode node = p.getCodec().readTree(p);

      // Use Jackson's treeToValue to deserialize the entire structure
      // This works for attributes and most nested structures
      // Only need special handling for the heterogenous children array
      AciPolUniInternal polUni = new AciPolUniInternal();

      // Parse attributes using treeToValue
      JsonNode attributesNode = node.get("attributes");
      if (attributesNode != null) {
        polUni.setAttributes(
            p.getCodec()
                .treeToValue(attributesNode, AciPolUniInternal.AciPolUniInternalAttributes.class));
      }

      // Parse children - each child is a single-key object like {"fvTenant": {...}}
      JsonNode childrenNode = node.get("children");
      if (childrenNode != null && childrenNode.isArray()) {
        com.google.common.collect.ImmutableList.Builder<AciPolUniInternal.PolUniChild> children =
            com.google.common.collect.ImmutableList.builder();
        for (JsonNode childNode : childrenNode) {
          AciPolUniInternal.PolUniChild child = new AciPolUniInternal.PolUniChild();

          // Try each known child type and use treeToValue for deserialization
          if (childNode.has("fvTenant")) {
            child.setFvTenant(p.getCodec().treeToValue(childNode.get("fvTenant"), AciTenant.class));
          } else if (childNode.has("fabricInst")) {
            child.setFabricInst(
                p.getCodec().treeToValue(childNode.get("fabricInst"), AciFabricInst.class));
          } else if (childNode.has("ctrlrInst")) {
            // ctrlrInst has mixed-type children, so parse it manually
            child.setCtrlrInst(parseCtrlrInst(childNode.get("ctrlrInst"), p, ctxt));
          }
          // Ignore other child types (apPluginPolContainer, notifCont, etc.)

          children.add(child);
        }
        polUni.setChildren(children.build());
      }

      return polUni;
    }

    private AciCtrlrInst parseCtrlrInst(JsonNode node, JsonParser p, DeserializationContext ctxt)
        throws IOException {
      AciCtrlrInst ctrlrInst = new AciCtrlrInst();

      JsonNode childrenNode = node.get("children");
      if (childrenNode != null && childrenNode.isArray()) {
        com.google.common.collect.ImmutableList.Builder<AciCtrlrInst.CtrlrInstChild> children =
            com.google.common.collect.ImmutableList.builder();
        for (JsonNode childNode : childrenNode) {
          AciCtrlrInst.CtrlrInstChild child = new AciCtrlrInst.CtrlrInstChild();

          // Only care about fabricNodeIdentPol, ignore other child types
          if (childNode.has("fabricNodeIdentPol")) {
            child.setFabricNodeIdentPol(
                p.getCodec()
                    .treeToValue(childNode.get("fabricNodeIdentPol"), AciFabricNodeIdentPol.class));
          }

          children.add(child);
        }
        ctrlrInst.setChildren(children.build());
      }

      return ctrlrInst;
    }
  }

  /** Represents the fabricInst element containing fabric-wide configuration. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricInst implements Serializable {
    private AciFabricInstAttributes _attributes;
    private List<FabricInstChild> _children;

    public @Nullable AciFabricInstAttributes getAttributes() {
      return _attributes;
    }

    public void setAttributes(@Nullable AciFabricInstAttributes attributes) {
      _attributes = attributes;
    }

    public @Nullable List<FabricInstChild> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<FabricInstChild> children) {
      _children = children;
    }

    /** Attributes of fabricInst. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AciFabricInstAttributes implements Serializable {
      private @Nullable String _annotation;

      private @Nullable String _distinguishedName;

      private @Nullable String _name;

      private @Nullable String _nameAlias;

      @JsonProperty("annotation")
      public @Nullable String getAnnotation() {
        return _annotation;
      }

      @JsonProperty("annotation")
      public void setAnnotation(@Nullable String annotation) {
        _annotation = annotation;
      }

      @JsonProperty("dn")
      public @Nullable String getDistinguishedName() {
        return _distinguishedName;
      }

      @JsonProperty("dn")
      public void setDistinguishedName(@Nullable String distinguishedName) {
        _distinguishedName = distinguishedName;
      }

      @JsonProperty("name")
      public @Nullable String getName() {
        return _name;
      }

      @JsonProperty("name")
      public void setName(@Nullable String name) {
        _name = name;
      }

      @JsonProperty("nameAlias")
      public @Nullable String getNameAlias() {
        return _nameAlias;
      }

      @JsonProperty("nameAlias")
      public void setNameAlias(@Nullable String nameAlias) {
        _nameAlias = nameAlias;
      }
    }

    /** Child elements of fabricInst. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FabricInstChild implements Serializable {
      private @Nullable AciFabricProtPol _fabricProtPol;

      @JsonProperty("fabricProtPol")
      public @Nullable AciFabricProtPol getFabricProtPol() {
        return _fabricProtPol;
      }

      @JsonProperty("fabricProtPol")
      public void setFabricProtPol(@Nullable AciFabricProtPol fabricProtPol) {
        _fabricProtPol = fabricProtPol;
      }
    }
  }

  /** Represents the fabricProtPol element containing fabric protection policies. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricProtPol implements Serializable {
    private AciFabricProtPolAttributes _attributes;
    private List<FabricProtPolChild> _children;

    public @Nullable AciFabricProtPolAttributes getAttributes() {
      return _attributes;
    }

    public void setAttributes(@Nullable AciFabricProtPolAttributes attributes) {
      _attributes = attributes;
    }

    public @Nullable List<FabricProtPolChild> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<FabricProtPolChild> children) {
      _children = children;
    }

    /** Attributes of fabricProtPol. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AciFabricProtPolAttributes implements Serializable {
      private @Nullable String _annotation;
      private @Nullable String _distinguishedName;
      private @Nullable String _nameAlias;
      private @Nullable String _userDomain;

      @JsonProperty("annotation")
      public @Nullable String getAnnotation() {
        return _annotation;
      }

      @JsonProperty("annotation")
      public void setAnnotation(@Nullable String annotation) {
        _annotation = annotation;
      }

      @JsonProperty("dn")
      public @Nullable String getDistinguishedName() {
        return _distinguishedName;
      }

      @JsonProperty("dn")
      public void setDistinguishedName(@Nullable String distinguishedName) {
        _distinguishedName = distinguishedName;
      }

      @JsonProperty("nameAlias")
      public @Nullable String getNameAlias() {
        return _nameAlias;
      }

      @JsonProperty("nameAlias")
      public void setNameAlias(@Nullable String nameAlias) {
        _nameAlias = nameAlias;
      }

      @JsonProperty("userdom")
      public @Nullable String getUserDomain() {
        return _userDomain;
      }

      @JsonProperty("userdom")
      public void setUserDomain(@Nullable String userDomain) {
        _userDomain = userDomain;
      }
    }

    /** Child elements of fabricProtPol. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FabricProtPolChild implements Serializable {
      private @Nullable AciFabricExplicitGEp _fabricExplicitGEp;

      @JsonProperty("fabricExplicitGEp")
      public @Nullable AciFabricExplicitGEp getFabricExplicitGEp() {
        return _fabricExplicitGEp;
      }

      @JsonProperty("fabricExplicitGEp")
      public void setFabricExplicitGEp(@Nullable AciFabricExplicitGEp fabricExplicitGEp) {
        _fabricExplicitGEp = fabricExplicitGEp;
      }
    }
  }

  /** Represents the fabricExplicitGEp element containing explicit fabric endpoints. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricExplicitGEp implements Serializable {
    private AciFabricExplicitGEpAttributes _attributes;
    private List<FabricExplicitGEpChild> _children;

    public @Nullable AciFabricExplicitGEpAttributes getAttributes() {
      return _attributes;
    }

    public void setAttributes(@Nullable AciFabricExplicitGEpAttributes attributes) {
      _attributes = attributes;
    }

    public @Nullable List<FabricExplicitGEpChild> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<FabricExplicitGEpChild> children) {
      _children = children;
    }

    /** Attributes of fabricExplicitGEp (VPC ID and name). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AciFabricExplicitGEpAttributes implements Serializable {
      private @Nullable String _annotation;
      private @Nullable String _description;
      private @Nullable String _distinguishedName;
      private @Nullable String _id;
      private @Nullable String _name;
      private @Nullable String _userDomain;

      @JsonProperty("annotation")
      public @Nullable String getAnnotation() {
        return _annotation;
      }

      @JsonProperty("annotation")
      public void setAnnotation(@Nullable String annotation) {
        _annotation = annotation;
      }

      @JsonProperty("descr")
      public @Nullable String getDescription() {
        return _description;
      }

      @JsonProperty("descr")
      public void setDescription(@Nullable String description) {
        _description = description;
      }

      @JsonProperty("dn")
      public @Nullable String getDistinguishedName() {
        return _distinguishedName;
      }

      @JsonProperty("dn")
      public void setDistinguishedName(@Nullable String distinguishedName) {
        _distinguishedName = distinguishedName;
      }

      @JsonProperty("id")
      public @Nullable String getId() {
        return _id;
      }

      @JsonProperty("id")
      public void setId(@Nullable String id) {
        _id = id;
      }

      @JsonProperty("name")
      public @Nullable String getName() {
        return _name;
      }

      @JsonProperty("name")
      public void setName(@Nullable String name) {
        _name = name;
      }

      @JsonProperty("userdom")
      public @Nullable String getUserDomain() {
        return _userDomain;
      }

      @JsonProperty("userdom")
      public void setUserDomain(@Nullable String userDomain) {
        _userDomain = userDomain;
      }
    }

    /** Child elements of fabricExplicitGEp. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FabricExplicitGEpChild implements Serializable {
      private @Nullable AciFabricNodePEp _fabricNodePEp;
      private @Nullable AciFabricNodeIdentP _fabricNodeIdentP;

      @JsonProperty("fabricNodePEp")
      public @Nullable AciFabricNodePEp getFabricNodePEp() {
        return _fabricNodePEp;
      }

      @JsonProperty("fabricNodePEp")
      public void setFabricNodePEp(@Nullable AciFabricNodePEp fabricNodePEp) {
        _fabricNodePEp = fabricNodePEp;
      }

      @JsonProperty("fabricNodeIdentP")
      public @Nullable AciFabricNodeIdentP getFabricNodeIdentP() {
        return _fabricNodeIdentP;
      }

      @JsonProperty("fabricNodeIdentP")
      public void setFabricNodeIdentP(@Nullable AciFabricNodeIdentP fabricNodeIdentP) {
        _fabricNodeIdentP = fabricNodeIdentP;
      }
    }
  }

  /** Represents the fabricNodeIdentP element containing fabric node identification. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricNodeIdentP implements Serializable {
    private AciFabricNodeIdentPAttributes _attributes;

    public @Nullable AciFabricNodeIdentPAttributes getAttributes() {
      return _attributes;
    }

    public void setAttributes(@Nullable AciFabricNodeIdentPAttributes attributes) {
      _attributes = attributes;
    }

    /** Attributes of fabric node identification. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AciFabricNodeIdentPAttributes implements Serializable {
      private @Nullable String _annotation;
      private @Nullable String _description;
      private @Nullable String _distinguishedName;
      private @Nullable String _id;
      private @Nullable String _name;
      private @Nullable String _nodeId;
      private @Nullable String _podId;
      private @Nullable String _role;
      private @Nullable String _serial;
      private @Nullable String _userDomain;

      @JsonProperty("annotation")
      public @Nullable String getAnnotation() {
        return _annotation;
      }

      @JsonProperty("annotation")
      public void setAnnotation(@Nullable String annotation) {
        _annotation = annotation;
      }

      @JsonProperty("descr")
      public @Nullable String getDescription() {
        return _description;
      }

      @JsonProperty("descr")
      public void setDescription(@Nullable String description) {
        _description = description;
      }

      @JsonProperty("dn")
      public @Nullable String getDistinguishedName() {
        return _distinguishedName;
      }

      @JsonProperty("dn")
      public void setDistinguishedName(@Nullable String distinguishedName) {
        _distinguishedName = distinguishedName;
      }

      @JsonProperty("id")
      public @Nullable String getId() {
        return _id;
      }

      @JsonProperty("id")
      public void setId(@Nullable String id) {
        _id = id;
      }

      @JsonProperty("name")
      public @Nullable String getName() {
        return _name;
      }

      @JsonProperty("name")
      public void setName(@Nullable String name) {
        _name = name;
      }

      @JsonProperty("nodeId")
      public @Nullable String getNodeId() {
        return _nodeId;
      }

      @JsonProperty("nodeId")
      public void setNodeId(@Nullable String nodeId) {
        _nodeId = nodeId;
      }

      @JsonProperty("podId")
      public @Nullable String getPodId() {
        return _podId;
      }

      @JsonProperty("podId")
      public void setPodId(@Nullable String podId) {
        _podId = podId;
      }

      @JsonProperty("role")
      public @Nullable String getRole() {
        return _role;
      }

      @JsonProperty("role")
      public void setRole(@Nullable String role) {
        _role = role;
      }

      @JsonProperty("serial")
      public @Nullable String getSerial() {
        return _serial;
      }

      @JsonProperty("serial")
      public void setSerial(@Nullable String serial) {
        _serial = serial;
      }

      @JsonProperty("userdom")
      public @Nullable String getUserDomain() {
        return _userDomain;
      }

      @JsonProperty("userdom")
      public void setUserDomain(@Nullable String userDomain) {
        _userDomain = userDomain;
      }
    }
  }

  /** Represents a fabric node endpoint (fabricNodePEp). */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricNodePEp implements Serializable {
    private AciFabricNodePEpAttributes _attributes;
    private List<FabricNodePEpChild> _children;

    public @Nullable AciFabricNodePEpAttributes getAttributes() {
      return _attributes;
    }

    public void setAttributes(@Nullable AciFabricNodePEpAttributes attributes) {
      _attributes = attributes;
    }

    public @Nullable List<FabricNodePEpChild> getChildren() {
      return _children;
    }

    @JsonProperty("children")
    public void setChildren(@Nullable List<FabricNodePEpChild> children) {
      _children = children;
    }

    /** Child elements of a fabric node. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FabricNodePEpChild implements Serializable {
      private AciInterface _fabricInterface;
      private AciL1PhysIf _l1PhysIf;

      @JsonProperty("fabricInterface")
      public @Nullable AciInterface getFabricInterface() {
        return _fabricInterface;
      }

      @JsonProperty("fabricInterface")
      public void setFabricInterface(@Nullable AciInterface fabricInterface) {
        _fabricInterface = fabricInterface;
      }

      @JsonProperty("l1PhysIf")
      public @Nullable AciL1PhysIf getL1PhysIf() {
        return _l1PhysIf;
      }

      @JsonProperty("l1PhysIf")
      public void setL1PhysIf(@Nullable AciL1PhysIf l1PhysIf) {
        _l1PhysIf = l1PhysIf;
      }
    }

    /** Interface configuration in ACI. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AciInterface implements Serializable {
      private AciInterfaceAttributes _attributes;

      public @Nullable AciInterfaceAttributes getAttributes() {
        return _attributes;
      }

      @JsonProperty("attributes")
      public void setAttributes(@Nullable AciInterfaceAttributes attributes) {
        _attributes = attributes;
      }

      @JsonInclude(JsonInclude.Include.NON_NULL)
      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class AciInterfaceAttributes implements Serializable {
        private @Nullable String _annotation;
        private @Nullable String _description;
        private @Nullable String _distinguishedName;
        private @Nullable String _id;
        private @Nullable String _name;
        private @Nullable String _nameAlias;
        private @Nullable String _userDomain;

        @JsonProperty("annotation")
        public @Nullable String getAnnotation() {
          return _annotation;
        }

        @JsonProperty("annotation")
        public void setAnnotation(@Nullable String annotation) {
          _annotation = annotation;
        }

        @JsonProperty("descr")
        public @Nullable String getDescription() {
          return _description;
        }

        @JsonProperty("descr")
        public void setDescription(@Nullable String description) {
          _description = description;
        }

        @JsonProperty("dn")
        public @Nullable String getDistinguishedName() {
          return _distinguishedName;
        }

        @JsonProperty("dn")
        public void setDistinguishedName(@Nullable String distinguishedName) {
          _distinguishedName = distinguishedName;
        }

        @JsonProperty("id")
        public @Nullable String getId() {
          return _id;
        }

        @JsonProperty("id")
        public void setId(@Nullable String id) {
          _id = id;
        }

        @JsonProperty("name")
        public @Nullable String getName() {
          return _name;
        }

        @JsonProperty("name")
        public void setName(@Nullable String name) {
          _name = name;
        }

        @JsonProperty("nameAlias")
        public @Nullable String getNameAlias() {
          return _nameAlias;
        }

        @JsonProperty("nameAlias")
        public void setNameAlias(@Nullable String nameAlias) {
          _nameAlias = nameAlias;
        }

        @JsonProperty("userdom")
        public @Nullable String getUserDomain() {
          return _userDomain;
        }

        @JsonProperty("userdom")
        public void setUserDomain(@Nullable String userDomain) {
          _userDomain = userDomain;
        }
      }
    }

    /** Physical layer 1 interface configuration in ACI. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AciL1PhysIf implements Serializable {
      private AciL1PhysIfAttributes _attributes;

      public @Nullable AciL1PhysIfAttributes getAttributes() {
        return _attributes;
      }

      @JsonProperty("attributes")
      public void setAttributes(@Nullable AciL1PhysIfAttributes attributes) {
        _attributes = attributes;
      }

      /** Attributes of a physical layer 1 interface. */
      @JsonInclude(JsonInclude.Include.NON_NULL)
      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class AciL1PhysIfAttributes implements Serializable {
        private @Nullable String _annotation;
        private @Nullable String _description;
        private @Nullable String _distinguishedName;
        private @Nullable String _id;

        @JsonProperty("annotation")
        public @Nullable String getAnnotation() {
          return _annotation;
        }

        @JsonProperty("annotation")
        public void setAnnotation(@Nullable String annotation) {
          _annotation = annotation;
        }

        @JsonProperty("descr")
        public @Nullable String getDescription() {
          return _description;
        }

        @JsonProperty("descr")
        public void setDescription(@Nullable String description) {
          _description = description;
        }

        @JsonProperty("dn")
        public @Nullable String getDistinguishedName() {
          return _distinguishedName;
        }

        @JsonProperty("dn")
        public void setDistinguishedName(@Nullable String distinguishedName) {
          _distinguishedName = distinguishedName;
        }

        @JsonProperty("id")
        public @Nullable String getId() {
          return _id;
        }

        @JsonProperty("id")
        public void setId(@Nullable String id) {
          _id = id;
        }
      }
    }

    /** Attributes of a fabric node. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AciFabricNodePEpAttributes implements Serializable {
      private @Nullable String _annotation;
      private @Nullable String _description;
      private @Nullable String _distinguishedName;
      private @Nullable String _id;
      private @Nullable String _name;
      private @Nullable String _nameAlias;
      private @Nullable String _nodeId;
      private @Nullable String _podId;
      private @Nullable String _role;
      private @Nullable String _userDomain;

      @JsonProperty("annotation")
      public @Nullable String getAnnotation() {
        return _annotation;
      }

      @JsonProperty("annotation")
      public void setAnnotation(@Nullable String annotation) {
        _annotation = annotation;
      }

      @JsonProperty("descr")
      public @Nullable String getDescription() {
        return _description;
      }

      @JsonProperty("descr")
      public void setDescription(@Nullable String description) {
        _description = description;
      }

      @JsonProperty("dn")
      public @Nullable String getDistinguishedName() {
        return _distinguishedName;
      }

      @JsonProperty("dn")
      public void setDistinguishedName(@Nullable String distinguishedName) {
        _distinguishedName = distinguishedName;
      }

      @JsonProperty("id")
      public @Nullable String getId() {
        return _id;
      }

      @JsonProperty("id")
      public void setId(@Nullable String id) {
        _id = id;
      }

      @JsonProperty("name")
      public @Nullable String getName() {
        return _name;
      }

      @JsonProperty("name")
      public void setName(@Nullable String name) {
        _name = name;
      }

      @JsonProperty("nameAlias")
      public @Nullable String getNameAlias() {
        return _nameAlias;
      }

      @JsonProperty("nameAlias")
      public void setNameAlias(@Nullable String nameAlias) {
        _nameAlias = nameAlias;
      }

      @JsonProperty("nodeId")
      public @Nullable String getNodeId() {
        return _nodeId;
      }

      @JsonProperty("nodeId")
      public void setNodeId(@Nullable String nodeId) {
        _nodeId = nodeId;
      }

      @JsonProperty("podId")
      public @Nullable String getPodId() {
        return _podId;
      }

      @JsonProperty("podId")
      public void setPodId(@Nullable String podId) {
        _podId = podId;
      }

      @JsonProperty("role")
      public @Nullable String getRole() {
        return _role;
      }

      @JsonProperty("role")
      public void setRole(@Nullable String role) {
        _role = role;
      }

      @JsonProperty("userdom")
      public @Nullable String getUserDomain() {
        return _userDomain;
      }

      @JsonProperty("userdom")
      public void setUserDomain(@Nullable String userDomain) {
        _userDomain = userDomain;
      }
    }
  }

  /**
   * ACI L2Out (Layer 2 Outside) configuration.
   *
   * <p>An L2Out defines Layer 2 external connectivity through a bridge domain, using encapsulation
   * like VLAN or VXLAN rather than IP routing.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class L2Out implements Serializable {
    private final String _name;
    private String _tenant;
    private String _description;
    private String _bridgeDomain;
    private String _encapsulation;

    public L2Out(String name) {
      _name = name;
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public @Nullable String getBridgeDomain() {
      return _bridgeDomain;
    }

    public void setBridgeDomain(String bridgeDomain) {
      _bridgeDomain = bridgeDomain;
    }

    public @Nullable String getEncapsulation() {
      return _encapsulation;
    }

    public void setEncapsulation(String encapsulation) {
      _encapsulation = encapsulation;
    }
  }

  /**
   * ACI L3Out (Layer 3 Outside) configuration.
   *
   * <p>An L3Out defines external connectivity for a tenant, including BGP peering, static routes,
   * OSPF configuration, and external EPGs (L3ExtEpg).
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class L3Out implements Serializable {
    private final String _name;
    private String _tenant;
    private String _vrf;
    private String _description;
    private String _enforceRouteControl;
    private String _mplsEnabled;
    private String _targetDscp;
    private BgpProcess _bgpProcess;
    private List<BgpPeer> _bgpPeers;
    private List<StaticRoute> _staticRoutes;
    private OspfConfig _ospfConfig;
    private List<ExternalEpg> _externalEpgs;

    public L3Out(String name) {
      _name = name;
      _bgpPeers = new ArrayList<>();
      _staticRoutes = new ArrayList<>();
      _externalEpgs = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public @Nullable String getTenant() {
      return _tenant;
    }

    public void setTenant(String tenant) {
      _tenant = tenant;
    }

    public @Nullable String getVrf() {
      return _vrf;
    }

    public void setVrf(String vrf) {
      _vrf = vrf;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public @Nullable String getEnforceRouteControl() {
      return _enforceRouteControl;
    }

    public void setEnforceRouteControl(String enforceRouteControl) {
      _enforceRouteControl = enforceRouteControl;
    }

    public @Nullable String getMplsEnabled() {
      return _mplsEnabled;
    }

    public void setMplsEnabled(String mplsEnabled) {
      _mplsEnabled = mplsEnabled;
    }

    public @Nullable String getTargetDscp() {
      return _targetDscp;
    }

    public void setTargetDscp(String targetDscp) {
      _targetDscp = targetDscp;
    }

    public @Nullable BgpProcess getBgpProcess() {
      return _bgpProcess;
    }

    public void setBgpProcess(BgpProcess bgpProcess) {
      _bgpProcess = bgpProcess;
    }

    public List<BgpPeer> getBgpPeers() {
      return _bgpPeers;
    }

    public void setBgpPeers(List<BgpPeer> bgpPeers) {
      _bgpPeers = new ArrayList<>(bgpPeers);
    }

    public List<StaticRoute> getStaticRoutes() {
      return _staticRoutes;
    }

    public void setStaticRoutes(List<StaticRoute> staticRoutes) {
      _staticRoutes = new ArrayList<>(staticRoutes);
    }

    public @Nullable OspfConfig getOspfConfig() {
      return _ospfConfig;
    }

    public void setOspfConfig(OspfConfig ospfConfig) {
      _ospfConfig = ospfConfig;
    }

    public List<ExternalEpg> getExternalEpgs() {
      return _externalEpgs;
    }

    public void setExternalEpgs(List<ExternalEpg> externalEpgs) {
      _externalEpgs = new ArrayList<>(externalEpgs);
    }
  }

  /**
   * BGP process configuration for L3Out.
   *
   * <p>Defines BGP process-level settings for an L3Out including AS number, router ID,
   * administrative distances, and BGP timers.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BgpProcess implements Serializable {
    private Long _as;
    private String _routerId;
    private Integer _ebgpAdminCost;
    private Integer _ibgpAdminCost;
    private Integer _vrfAdminCost;
    private Integer _keepalive;
    private Integer _holdTime;

    public @Nullable Long getAs() {
      return _as;
    }

    public void setAs(Long as) {
      _as = as;
    }

    public @Nullable String getRouterId() {
      return _routerId;
    }

    public void setRouterId(String routerId) {
      _routerId = routerId;
    }

    public @Nullable Integer getEbgpAdminCost() {
      return _ebgpAdminCost;
    }

    public void setEbgpAdminCost(Integer ebgpAdminCost) {
      _ebgpAdminCost = ebgpAdminCost;
    }

    public @Nullable Integer getIbgpAdminCost() {
      return _ibgpAdminCost;
    }

    public void setIbgpAdminCost(Integer ibgpAdminCost) {
      _ibgpAdminCost = ibgpAdminCost;
    }

    public @Nullable Integer getVrfAdminCost() {
      return _vrfAdminCost;
    }

    public void setVrfAdminCost(Integer vrfAdminCost) {
      _vrfAdminCost = vrfAdminCost;
    }

    public @Nullable Integer getKeepalive() {
      return _keepalive;
    }

    public void setKeepalive(Integer keepalive) {
      _keepalive = keepalive;
    }

    public @Nullable Integer getHoldTime() {
      return _holdTime;
    }

    public void setHoldTime(Integer holdTime) {
      _holdTime = holdTime;
    }
  }

  /**
   * BGP peer configuration for L3Out.
   *
   * <p>Defines a BGP peer within an L3Out including peer address, AS numbers, policies, and route
   * target (route-map) configurations.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BgpPeer implements Serializable {
    private String _peerAddress;
    private String _remoteAs;
    private String _localAs;
    private String _updateSourceInterface;
    private String _password;
    private String _description;
    private Boolean _ebgpMultihop;
    private Integer _ttl;
    private Boolean _routeReflectorClient;
    private Boolean _nextHopSelf;
    private Boolean _sendCommunities;
    private String _localPreference;
    private String _med;
    private String _importRouteMap;
    private String _exportRouteMap;
    private List<String> _routeTargets;
    private Integer _keepalive;
    private Integer _holdTime;

    public BgpPeer() {
      _routeTargets = new ArrayList<>();
    }

    public @Nullable String getPeerAddress() {
      return _peerAddress;
    }

    public void setPeerAddress(String peerAddress) {
      _peerAddress = peerAddress;
    }

    public @Nullable String getRemoteAs() {
      return _remoteAs;
    }

    public void setRemoteAs(String remoteAs) {
      _remoteAs = remoteAs;
    }

    public @Nullable String getLocalAs() {
      return _localAs;
    }

    public void setLocalAs(String localAs) {
      _localAs = localAs;
    }

    public @Nullable String getUpdateSourceInterface() {
      return _updateSourceInterface;
    }

    public void setUpdateSourceInterface(String updateSourceInterface) {
      _updateSourceInterface = updateSourceInterface;
    }

    public @Nullable String getPassword() {
      return _password;
    }

    public void setPassword(String password) {
      _password = password;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public @Nullable Boolean getEbgpMultihop() {
      return _ebgpMultihop;
    }

    public void setEbgpMultihop(Boolean ebgpMultihop) {
      _ebgpMultihop = ebgpMultihop;
    }

    public @Nullable Integer getTtl() {
      return _ttl;
    }

    public void setTtl(Integer ttl) {
      _ttl = ttl;
    }

    public @Nullable Boolean getRouteReflectorClient() {
      return _routeReflectorClient;
    }

    public void setRouteReflectorClient(Boolean routeReflectorClient) {
      _routeReflectorClient = routeReflectorClient;
    }

    public @Nullable Boolean getNextHopSelf() {
      return _nextHopSelf;
    }

    public void setNextHopSelf(Boolean nextHopSelf) {
      _nextHopSelf = nextHopSelf;
    }

    public @Nullable Boolean getSendCommunities() {
      return _sendCommunities;
    }

    public void setSendCommunities(Boolean sendCommunities) {
      _sendCommunities = sendCommunities;
    }

    public @Nullable String getLocalPreference() {
      return _localPreference;
    }

    public void setLocalPreference(String localPreference) {
      _localPreference = localPreference;
    }

    public @Nullable String getMed() {
      return _med;
    }

    public void setMed(String med) {
      _med = med;
    }

    public @Nullable String getImportRouteMap() {
      return _importRouteMap;
    }

    public void setImportRouteMap(String importRouteMap) {
      _importRouteMap = importRouteMap;
    }

    public @Nullable String getExportRouteMap() {
      return _exportRouteMap;
    }

    public void setExportRouteMap(String exportRouteMap) {
      _exportRouteMap = exportRouteMap;
    }

    /**
     * Returns the list of route targets (RTs) configured for this BGP peer. Route targets are used
     * in BGP route-maps to control route import/export.
     *
     * @return List of route target strings (e.g., "route-target:65000:1")
     */
    public List<String> getRouteTargets() {
      return _routeTargets;
    }

    public void setRouteTargets(List<String> routeTargets) {
      _routeTargets = new ArrayList<>(routeTargets);
    }

    public void addRouteTarget(String routeTarget) {
      if (_routeTargets == null) {
        _routeTargets = new ArrayList<>();
      }
      _routeTargets.add(routeTarget);
    }

    public @Nullable Integer getKeepalive() {
      return _keepalive;
    }

    public void setKeepalive(Integer keepalive) {
      _keepalive = keepalive;
    }

    public @Nullable Integer getHoldTime() {
      return _holdTime;
    }

    public void setHoldTime(Integer holdTime) {
      _holdTime = holdTime;
    }
  }

  /**
   * Static route configuration for L3Out.
   *
   * <p>Defines a static route within an L3Out including prefix, next hop, and associated
   * parameters.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class StaticRoute implements Serializable {
    private String _prefix;
    private String _nextHop;
    private String _nextHopInterface;
    private String _administrativeDistance;
    private String _tag;
    private String _track;

    public @Nullable String getPrefix() {
      return _prefix;
    }

    public void setPrefix(String prefix) {
      _prefix = prefix;
    }

    public @Nullable String getNextHop() {
      return _nextHop;
    }

    public void setNextHop(String nextHop) {
      _nextHop = nextHop;
    }

    public @Nullable String getNextHopInterface() {
      return _nextHopInterface;
    }

    public void setNextHopInterface(String nextHopInterface) {
      _nextHopInterface = nextHopInterface;
    }

    public @Nullable String getAdministrativeDistance() {
      return _administrativeDistance;
    }

    public void setAdministrativeDistance(String administrativeDistance) {
      _administrativeDistance = administrativeDistance;
    }

    public @Nullable String getTag() {
      return _tag;
    }

    public void setTag(String tag) {
      _tag = tag;
    }

    public @Nullable String getTrack() {
      return _track;
    }

    public void setTrack(String track) {
      _track = track;
    }
  }

  /**
   * OSPF configuration for L3Out.
   *
   * <p>Defines OSPF process settings, areas, and interfaces for an L3Out.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class OspfConfig implements Serializable {
    private String _name;
    private String _description;
    private String _processId;
    private String _areaId;
    private Map<String, OspfArea> _areas;
    private List<OspfInterface> _ospfInterfaces;

    public OspfConfig() {
      _areas = new TreeMap<>();
      _ospfInterfaces = new ArrayList<>();
    }

    public @Nullable String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public @Nullable String getProcessId() {
      return _processId;
    }

    public void setProcessId(String processId) {
      _processId = processId;
    }

    public @Nullable String getAreaId() {
      return _areaId;
    }

    public void setAreaId(String areaId) {
      _areaId = areaId;
    }

    public Map<String, OspfArea> getAreas() {
      return _areas;
    }

    public void setAreas(Map<String, OspfArea> areas) {
      _areas = new TreeMap<>(areas);
    }

    public List<OspfInterface> getOspfInterfaces() {
      return _ospfInterfaces;
    }

    public void setOspfInterfaces(List<OspfInterface> ospfInterfaces) {
      _ospfInterfaces = new ArrayList<>(ospfInterfaces);
    }
  }

  /**
   * OSPF area configuration for L3Out.
   *
   * <p>Defines an OSPF area within an L3Out OSPF configuration.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class OspfArea implements Serializable {
    private String _areaId;
    private List<String> _networks;
    private String _areaType;

    public OspfArea() {
      _networks = new ArrayList<>();
    }

    public @Nullable String getAreaId() {
      return _areaId;
    }

    public void setAreaId(String areaId) {
      _areaId = areaId;
    }

    public List<String> getNetworks() {
      return _networks;
    }

    public void setNetworks(List<String> networks) {
      _networks = new ArrayList<>(networks);
    }

    public @Nullable String getAreaType() {
      return _areaType;
    }

    public void setAreaType(String areaType) {
      _areaType = areaType;
    }
  }

  /**
   * OSPF interface configuration for L3Out.
   *
   * <p>Defines OSPF interface-specific settings for an L3Out OSPF configuration.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class OspfInterface implements Serializable {
    private String _name;
    private String _description;
    private Integer _cost;
    private Integer _helloInterval;
    private Integer _deadInterval;
    private String _networkType;
    private Boolean _passive;

    public OspfInterface() {}

    public @Nullable String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }

    public @Nullable Integer getCost() {
      return _cost;
    }

    public void setCost(Integer cost) {
      _cost = cost;
    }

    public @Nullable Integer getHelloInterval() {
      return _helloInterval;
    }

    public void setHelloInterval(Integer helloInterval) {
      _helloInterval = helloInterval;
    }

    public @Nullable Integer getDeadInterval() {
      return _deadInterval;
    }

    public void setDeadInterval(Integer deadInterval) {
      _deadInterval = deadInterval;
    }

    public @Nullable String getNetworkType() {
      return _networkType;
    }

    public void setNetworkType(String networkType) {
      _networkType = networkType;
    }

    public @Nullable Boolean getPassive() {
      return _passive;
    }

    public void setPassive(Boolean passive) {
      _passive = passive;
    }
  }

  /**
   * External EPG (L3ExtEpg) configuration for L3Out.
   *
   * <p>Defines an external endpoint group for external connectivity, including subnets and
   * associated interfaces.
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ExternalEpg implements Serializable {
    private final String _name;
    private List<String> _subnets;
    private List<String> _providedContracts;
    private List<String> _consumedContracts;
    private List<String> _providedContractInterfaces;
    private List<String> _consumedContractInterfaces;
    private List<String> _protectedByTaboos;
    private String _nextHop;
    private String _interface;
    private String _description;

    public ExternalEpg(String name) {
      _name = name;
      _subnets = new ArrayList<>();
      _providedContracts = new ArrayList<>();
      _consumedContracts = new ArrayList<>();
      _providedContractInterfaces = new ArrayList<>();
      _consumedContractInterfaces = new ArrayList<>();
      _protectedByTaboos = new ArrayList<>();
    }

    public String getName() {
      return _name;
    }

    public List<String> getSubnets() {
      return _subnets;
    }

    public void setSubnets(List<String> subnets) {
      _subnets = new ArrayList<>(subnets);
    }

    public List<String> getProvidedContracts() {
      return _providedContracts;
    }

    public void setProvidedContracts(List<String> providedContracts) {
      _providedContracts = new ArrayList<>(providedContracts);
    }

    public List<String> getConsumedContracts() {
      return _consumedContracts;
    }

    public void setConsumedContracts(List<String> consumedContracts) {
      _consumedContracts = new ArrayList<>(consumedContracts);
    }

    public List<String> getProvidedContractInterfaces() {
      return _providedContractInterfaces;
    }

    public void setProvidedContractInterfaces(List<String> providedContractInterfaces) {
      _providedContractInterfaces = new ArrayList<>(providedContractInterfaces);
    }

    public List<String> getConsumedContractInterfaces() {
      return _consumedContractInterfaces;
    }

    public void setConsumedContractInterfaces(List<String> consumedContractInterfaces) {
      _consumedContractInterfaces = new ArrayList<>(consumedContractInterfaces);
    }

    public List<String> getProtectedByTaboos() {
      return _protectedByTaboos;
    }

    public void setProtectedByTaboos(List<String> protectedByTaboos) {
      _protectedByTaboos = new ArrayList<>(protectedByTaboos);
    }

    public @Nullable String getNextHop() {
      return _nextHop;
    }

    public void setNextHop(String nextHop) {
      _nextHop = nextHop;
    }

    public @Nullable String getInterface() {
      return _interface;
    }

    public void setInterface(String iface) {
      _interface = iface;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(String description) {
      _description = description;
    }
  }

  /** Management information for out-of-band management. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ManagementInfo implements Serializable {
    private String _address;
    private String _gateway;
    private String _gateway6;
    private String _address6;

    public @Nullable String getAddress() {
      return _address;
    }

    public void setAddress(@Nullable String address) {
      _address = address;
    }

    public @Nullable String getGateway() {
      return _gateway;
    }

    public void setGateway(@Nullable String gateway) {
      _gateway = gateway;
    }

    public @Nullable String getGateway6() {
      return _gateway6;
    }

    public void setGateway6(@Nullable String gateway6) {
      _gateway6 = gateway6;
    }

    public @Nullable String getAddress6() {
      return _address6;
    }

    public void setAddress6(@Nullable String address6) {
      _address6 = address6;
    }
  }

  /**
   * Path attachment information linking EPGs to physical interfaces.
   *
   * <p>Path attachments (fvRsPathAtt) contain:
   *
   * <ul>
   *   <li>tDn: Target distinguished name identifying the physical interface
   *   <li>encap: VLAN encapsulation (e.g., "vlan-2717")
   *   <li>descr: Description of the attachment
   * </ul>
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PathAttachment implements Serializable {
    private final String _tdn;
    private String _podId;
    private String _nodeId;
    private String _nodeId2; // Secondary node ID for vPC
    private String _interface;
    private String _encap;
    private String _description;
    private String _epgName;
    private String _epgTenant;

    public PathAttachment(String tdn) {
      _tdn = tdn;
      parseTdn(tdn);
    }

    private void parseTdn(String tdn) {
      // tDn formats:
      // - Single node: topology/pod-{podId}/paths-{nodeId}/pathep-[{interface}]
      // - vPC pair: topology/pod-{podId}/protpaths-{nodeId1}-{nodeId2}/pathep-[{interface}]
      if (tdn == null) {
        return;
      }

      // Extract podId
      int podIdx = tdn.indexOf("/pod-");
      if (podIdx >= 0) {
        int podStart = podIdx + 5; // Skip "/pod-"
        int podEnd = tdn.indexOf('/', podStart);
        if (podEnd > podStart) {
          _podId = tdn.substring(podStart, podEnd);
        }
      }

      // Extract nodeId (paths- or protpaths-)
      int pathsIdx = tdn.indexOf("/paths-");
      int protpathsIdx = tdn.indexOf("/protpaths-");

      if (protpathsIdx >= 0) {
        // vPC format: protpaths-{nodeId1}-{nodeId2}
        int nodeStart = protpathsIdx + 11; // Skip "/protpaths-"
        int slashIdx = tdn.indexOf('/', nodeStart);
        if (slashIdx > nodeStart) {
          String nodePair = tdn.substring(nodeStart, slashIdx);
          String[] nodes = nodePair.split("-");
          if (nodes.length >= 1) {
            _nodeId = nodes[0];
          }
          if (nodes.length >= 2) {
            _nodeId2 = nodes[1];
          }
        }
      } else if (pathsIdx >= 0) {
        // Single node: paths-{nodeId}
        int nodeStart = pathsIdx + 7; // Skip "/paths-"
        int slashIdx = tdn.indexOf('/', nodeStart);
        if (slashIdx > nodeStart) {
          _nodeId = tdn.substring(nodeStart, slashIdx);
        }
      }

      // Extract interface name (pathep-[{interface}])
      int pathepIdx = tdn.indexOf("/pathep-[");
      if (pathepIdx >= 0) {
        int ifStart = pathepIdx + 9; // Skip "/pathep-["
        int ifEnd = tdn.indexOf(']', ifStart);
        if (ifEnd > ifStart) {
          _interface = tdn.substring(ifStart, ifEnd);
        }
      }
    }

    public String getTdn() {
      return _tdn;
    }

    public @Nullable String getPodId() {
      return _podId;
    }

    public @Nullable String getNodeId() {
      return _nodeId;
    }

    public @Nullable String getNodeId2() {
      return _nodeId2;
    }

    public boolean isVpc() {
      return _nodeId2 != null;
    }

    public @Nullable String getInterface() {
      return _interface;
    }

    public @Nullable String getEncap() {
      return _encap;
    }

    public void setEncap(@Nullable String encap) {
      _encap = encap;
    }

    public @Nullable String getDescription() {
      return _description;
    }

    public void setDescription(@Nullable String description) {
      _description = description;
    }

    public @Nullable String getEpgName() {
      return _epgName;
    }

    public void setEpgName(@Nullable String epgName) {
      _epgName = epgName;
    }

    public @Nullable String getEpgTenant() {
      return _epgTenant;
    }

    public void setEpgTenant(@Nullable String epgTenant) {
      _epgTenant = epgTenant;
    }
  }

  /** Controller instance (ctrlrInst) in ACI fabric. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciCtrlrInst implements Serializable {
    private List<CtrlrInstChild> _children;

    public @Nullable List<CtrlrInstChild> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<CtrlrInstChild> children) {
      _children = children;
    }

    /** Child elements of ctrlrInst. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CtrlrInstChild implements Serializable {
      private @Nullable AciFabricNodeIdentPol _fabricNodeIdentPol;

      @JsonProperty("fabricNodeIdentPol")
      public @Nullable AciFabricNodeIdentPol getFabricNodeIdentPol() {
        return _fabricNodeIdentPol;
      }

      @JsonProperty("fabricNodeIdentPol")
      public void setFabricNodeIdentPol(@Nullable AciFabricNodeIdentPol fabricNodeIdentPol) {
        _fabricNodeIdentPol = fabricNodeIdentPol;
      }
    }
  }

  /** Fabric node identity policy (fabricNodeIdentPol) containing node identities. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AciFabricNodeIdentPol implements Serializable {
    private List<AciFabricNodeIdentP> _children;

    public @Nullable List<AciFabricNodeIdentP> getChildren() {
      return _children;
    }

    public void setChildren(@Nullable List<AciFabricNodeIdentP> children) {
      _children = children;
    }
  }
}
