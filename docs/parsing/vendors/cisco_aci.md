# Cisco ACI-Specific Parsing and Extraction

This document covers the unique aspects of parsing and extracting Cisco ACI (Application Centric Infrastructure) configurations in Batfish.

## Cisco ACI Configuration Structure

Cisco ACI configurations have several unique characteristics that distinguish them from traditional CLI-based network devices:

1. **No CLI Syntax**: ACI configurations are exported as JSON or XML, not CLI commands
2. **Policy-Based Model**: Configuration is defined through policies (contracts) between endpoint groups
3. **Hierarchical Object Model**: Uses a Management Information Tree (MIT) with `polUni` as the root
4. **Fabric-Wide Configuration**: A single configuration applies to the entire fabric (spines and leaves)
5. **Dynamic Typing**: Children objects use heterogeneous keys (e.g., `fvTenant`, `fvCtx`, `vzBrCP`)

## Jackson-Based Parsing (No ANTLR4)

Cisco ACI uses Jackson JSON/XML parsing via `BatfishObjectMapper` rather than ANTLR4 grammars for parsing. This approach is **shared with other cloud/SDN vendors** in Batfish, not unique to ACI.

### Why Jackson Instead of ANTLR4?

ACI configurations are exported from the APIC (Application Policy Infrastructure Controller) as structured JSON or XML files. These exports:
- Have a well-defined schema with attributes and children arrays
- Do not require line-by-line CLI parsing
- Are more naturally handled by a tree-based JSON/XML parser

### Similar Approaches in Other Vendors

**Cloud/SDN vendors** that use Jackson-based parsing (like ACI):
- **AWS**: JSON configuration exports parsed with Jackson (`BatfishObjectMapper`)
- **Azure**: JSON configuration exports parsed with Jackson (`BatfishObjectMapper`)
- **Sonic**: JSON (`config_db.json`) and YAML (`snmp.yml`) parsed with Jackson, plus ANTLR4 for FRR routing configuration

**Traditional network vendors** use ANTLR4 CLI parsing:
- Cisco NX-OS, IOS-XR, Juniper, Arista, etc.

### Comparison Table

| Aspect | ANTLR4 Vendors (NX-OS, IOS-XR, etc.) | Jackson Vendors (ACI, AWS, Azure, Sonic) |
|--------|--------------------------------------|------------------------------------------|
| Input Format | CLI commands (text) | JSON/XML/YAML exports |
| Parser | ANTLR4 grammar files | Jackson (BatfishObjectMapper) |
| Grammar Files | `*.g4` files (Lexer, Parser) | None (for config parsing) |
| Configuration Model | Line-based, hierarchical CLI blocks | Object tree with attributes/children |
| Extraction | Parse tree listeners | Jackson deserialization |
| Examples | Cisco NX-OS, Juniper, Arista | Cisco ACI, AWS, Azure, Sonic |

### Entry Point Comparison

| Vendor | Entry Point Method | File Types |
|--------|-------------------|------------|
| **Cisco ACI** | `AciConfiguration.fromJson/fromXml/fromFile()` | JSON, XML |
| **AWS** | `AwsConfiguration.addConfigElement(JsonNode)` | JSON |
| **Azure** | `AzureConfiguration.addConfigElement(JsonNode)` | JSON |
| **Sonic** | `SonicControlPlaneExtractor.processNonFrrFiles()` | JSON (config_db), YAML (snmp.yml) |
| **Traditional** | ControlPlaneExtractor with ANTLR4 | CLI text |

## ACI Class Naming and Package Conventions

To keep ACI parsing consistent with Batfish structured-format parser patterns, ACI classes are
split into two layers:

1. **APIC wire-format DTOs**
   - Package: `org.batfish.vendor.cisco_aci.representation.apic`
   - Naming: `Aci*` names that mirror APIC/MIT object names (for example `AciTenant`,
     `AciPolUniInternal`, `AciFabricInst`).
   - Purpose: Jackson deserialization only.

2. **Normalized internal model**
   - Package: `org.batfish.vendor.cisco_aci.representation`
   - Naming: domain model names without `Aci` prefix (for example `Tenant`, `TenantVrf`,
     `BridgeDomain`, `Contract`, `FabricLink`).
   - Purpose: conversion and analysis logic.

This separation avoids mixed naming styles in the same layer and makes parsing vs. modeling intent
explicit.

## ACI JSON Structure

ACI configurations have `polUni` (Policy Universe) as the root:

```json
{
  "polUni": {
    "attributes": {"dn": "uni", "name": "aci-fabric"},
    "children": [
      {
        "fvTenant": {
          "attributes": {"name": "tenant1", "descr": "Example Tenant"},
          "children": [
            {"fvCtx": {"attributes": {"name": "vrf1"}}},
            {"fvBD": {"attributes": {"name": "bd1"}}},
            {"vzBrCP": {"attributes": {"name": "contract1"}}
          }
        ]
      }
    ]
  }
}
```

### XML Format

ACI configurations can also be exported in XML format with equivalent structure:

```xml
<polUni name="aci-fabric">
  <fvTenant name="tenant1" descr="Example Tenant">
    <fvCtx name="vrf1"/>
    <fvBD name="bd1"/>
    <vzBrCP name="contract1"/>
  </fvTenant>
</polUni>
```

## Parsing Flow

### Snapshot Packaging and Optional `cisco_aci_configs/`

Cisco ACI parsing now supports two input styles, both valid:

1. **Legacy / single-file style (still supported)**
   Place an APIC export (`polUni` JSON or XML) under `configs/` like any other network config.

2. **ACI-specific style (optional)**
   Place ACI inputs under `cisco_aci_configs/`.

`cisco_aci_configs/` is **optional**. If it is not present, Batfish continues to parse ACI APIC
files from `configs/` as before.

When `cisco_aci_configs/` is present:

- Batfish looks for at least one **primary APIC model file** (`polUni` JSON/XML).
- Optional supplemental fabric topology payloads (APIC `fabricLink` export JSON) are detected and
  merged into the ACI model as explicit fabric links.
- If `cisco_aci_configs/` contains only supplemental `fabricLink` JSON and no primary APIC file,
  that folder is ignored for ACI parsing (no failure).

Current behavior for multiple primary APIC files in `cisco_aci_configs/`:

- The first primary file is used.
- Additional primary files are ignored with warnings.

Recommended snapshot layout for enriched ACI modeling:

```text
<snapshot>/
├── cisco_aci_configs/
│   ├── apic.json                 # primary APIC export (polUni)
│   └── outputtopology_4.json     # optional APIC fabricLink export
└── configs/
    └── ...                       # other vendors and/or legacy ACI placement
```

### 1. Root Deserialization

```java
// In AciConfiguration.java
public static AciConfiguration fromJson(String filename, String text, Warnings warnings) {
    JsonNode rootNode = BatfishObjectMapper.mapper().readTree(text);
    JsonNode polUniNode = rootNode.get("polUni");

    AciConfiguration config = new AciConfiguration();
    parsePolUni(polUniNode, config, warnings);
    return config;
}
```

### 2. polUni Processing

The `parsePolUni()` method extracts top-level children:

```java
private void parsePolUni(JsonNode polUniNode, AciConfiguration config, Warnings warnings) {
    JsonNode attributesNode = polUniNode.get("attributes");
    if (attributesNode != null && attributesNode.has("name")) {
        config.setHostname(attributesNode.get("name").asText());
    }

    JsonNode children = polUniNode.get("children");
    if (children != null && children.isArray()) {
        for (JsonNode child : children) {
            parsePolUniChild(child, config, warnings);
        }
    }
}
```

### 3. Heterogeneous Children Handling

ACI JSON uses dynamic object keys. Each child is a map with a single key indicating its type:

```java
private void parsePolUniChild(JsonNode child, AciConfiguration config, Warnings warnings) {
    Iterator<String> fieldNames = child.fieldNames();
    if (fieldNames.hasNext()) {
        String className = fieldNames.next(); // e.g., "fvTenant", "fabricInst"
        JsonNode childNode = child.get(className);

        switch (className) {
            case "fvTenant":
                parseTenant(childNode, config, warnings);
                break;
            case "fabricInst":
                parseFabricNodes(childNode, config, warnings);
                break;
            // ... more cases
        }
    }
}
```

### 4. Tenant Parsing

Tenants contain VRFs, bridge domains, EPGs, and contracts:

```java
private void parseTenant(JsonNode tenantNode, AciConfiguration config, Warnings warnings) {
    JsonNode attributes = tenantNode.get("attributes");
    String tenantName = attributes.get("name").asText();

    AciConfiguration.Tenant tenant = config.getOrCreateTenant(tenantName);

    JsonNode children = tenantNode.get("children");
    if (children != null && children.isArray()) {
        for (JsonNode child : children) {
            Iterator<String> fieldNames = child.fieldNames();
            if (fieldNames.hasNext()) {
                String childType = fieldNames.next();
                JsonNode childNode = child.get(childType);

                switch (childType) {
                    case "fvCtx":
                        parseVrf(childNode, tenantName, config, warnings);
                        break;
                    case "fvBD":
                        parseBridgeDomain(childNode, tenantName, config, warnings);
                        break;
                    case "vzBrCP":
                        parseContract(childNode, tenantName, config, warnings);
                        break;
                    // ... more cases
                }
            }
        }
    }
}
```

## Supported ACI Object Classes

| ACI Class | Description | Batfish Representation |
|-----------|-------------|------------------------|
| `fvTenant` | Tenant | Policy container |
| `fvCtx` | VRF Context | `Vrf` objects |
| `fvBD` | Bridge Domain | VLAN interfaces with subnets |
| `fvSubnet` | Subnet | IP addresses on VLAN |
| `fvAEPg` | Endpoint Group | EPG for policy application |
| `vzBrCP` | Contract | `IpAccessList` objects |
| `vzSubj` | Contract Subject | Group of filters |
| `vzRsSubjFiltAtt` | Filter Reference | ACL line entries |
| `fabricNodePEp` | Fabric Node | Individual `Configuration` objects |
| `l3ExtOut` | L3 External Connectivity | External routing (BGP, OSPF) |

## Common ACI Parsing Patterns

### Attribute Extraction

Most ACI objects have an `attributes` block containing properties:

```java
private void parseVrf(JsonNode vrfNode, String tenantName, AciConfiguration config, Warnings warnings) {
    JsonNode attributes = vrfNode.get("attributes");
    String vrfName = attributes.get("name").asText();

    AciConfiguration.Vrf vrf = new AciConfiguration.Vrf(tenantName + ":" + vrfName);
    vrf.setTenant(tenantName);

    if (attributes.has("descr")) {
        vrf.setDescription(attributes.get("descr").asText());
    }

    config.getVrfs().put(vrf.getQualifiedName(), vrf);
}
```

### Null-Safe Children Processing

Always check for null/missing children arrays:

```java
JsonNode children = node.get("children");
if (children != null && children.isArray()) {
    for (JsonNode child : children) {
        // Process child
    }
}
```

### Qualified Name Construction

ACI objects use tenant-prefixed names to avoid collisions:

```java
// VRF names become "tenant:vrf"
String qualifiedVrfName = tenantName + ":" + vrfName;

// Contract names become "tenant:contract"
String qualifiedContractName = tenantName + ":" + contractName;

// EPG names become "tenant:app:epg"
String qualifiedEpgName = tenantName + ":" + appName + ":" + epgName;
```

## Contract to ACL Conversion

Contracts define allowed communication between EPGs and are converted to ACLs:

### Conversion Logic

```java
private static IpAccessList toContractAcl(AciConfiguration.Contract contract, Configuration c) {
    String aclName = "~CONTRACT~" + contract.getName();

    ImmutableList.Builder<ExprAclLine> lines = ImmutableList.builder();

    for (AciConfiguration.Contract.Subject subject : contract.getSubjects()) {
        for (AciConfiguration.Contract.Filter filter : subject.getFilters()) {
            lines.addAll(toAclLines(filter, contract.getName(), c));
        }
    }

    // Implicit deny at end
    lines.add(new ExprAclLine(
        LineAction.DENY,
        AclLineMatchExprs.TRUE,
        "Implicit deny for contract " + contract.getName()
    ));

    return IpAccessList.builder()
        .setOwner(c)
        .setName(aclName)
        .setLines(lines.build())
        .build();
}
```

### Filter to ACL Line Mapping

| Contract Filter | ACL Match Expression |
|-----------------|---------------------|
| `ipProtocol: "tcp"` | `AclLineMatchExprs.matchIpProtocol(IpProtocol.TCP)` |
| `destinationPorts: ["80"]` | `AclLineMatchExprs.matchDstPort(IntegerSpace.builder().including(80).build())` |
| `etherT: "ip"` | `AclLineMatchExprs.matchIpProtocols(ImmutableSet.of(IpProtocol.IP))` |

## Fabric Node to Configuration Mapping

Each fabric node becomes a separate Batfish `Configuration` object:

```java
// In AciConversion.toVendorIndependentConfigurations()
SortedMap<String, Configuration> configs = new TreeMap<>();

if (aciConfig.getFabricNodes().isEmpty()) {
    // No fabric nodes - create single config
    Configuration c = convertFabricConfig(aciConfig, warnings);
    configs.put(aciConfig.getHostname(), c);
} else {
    // One config per fabric node
    for (AciConfiguration.FabricNode node : aciConfig.getFabricNodes().values()) {
        String hostname = ...; // resolved node hostname
        Configuration c = convertNode(node, aciConfig, hostname, warnings);
        configs.put(hostname, c);
    }
}

return configs;
```

## Implementation Decision Guide for ACI

When implementing a new ACI object type:

### 1. Define the Data Model

Create a static inner class in `AciConfiguration.java`:

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public static class NewObject implements Serializable {
    @JsonProperty("attributes")
    private NewObjectAttributes attributes;

    @JsonProperty("children")
    private List<Object> children;

    public static class NewObjectAttributes {
        @JsonProperty("name")
        private String name;

        @JsonProperty("descr")
        private String description;

        // Getters and setters
    }

    // Getters and setters
}
```

### 2. Add to AciConfiguration Storage

```java
private Map<String, NewObject> _newObjects = new TreeMap<>();

public Map<String, NewObject> getNewObjects() {
    return _newObjects;
}

public NewObject getOrCreateNewObject(String name) {
    return _newObjects.computeIfAbsent(name, NewObject::new);
}
```

### 3. Parse from JSON

Add a case in the appropriate switch statement:

```java
case "newObject":
    parseNewObject(childNode, tenantName, config, warnings);
    break;
```

Implement the parse method:

```java
private void parseNewObject(JsonNode objNode, String tenantName, AciConfiguration config, Warnings warnings) {
    JsonNode attributes = objNode.get("attributes");
    String name = attributes.get("name").asText();

    AciConfiguration.NewObject obj = config.getOrCreateNewObject(tenantName + ":" + name);

    if (attributes.has("descr")) {
        obj.setDescription(attributes.get("descr").asText());
    }

    // Process children if needed
    JsonNode children = objNode.get("children");
    if (children != null && children.isArray()) {
        // Process nested objects
    }
}
```

### 4. Convert to Vendor-Independent Model

Add conversion logic in `AciConversion.java`:

```java
private static void convertNewObjects(
    AciConfiguration aciConfig,
    Configuration c,
    Warnings warnings) {

    for (AciConfiguration.NewObject obj : aciConfig.getNewObjects().values()) {
        // Convert to Batfish structures
        // e.g., create ACLs, interfaces, routes, etc.
    }
}
```

### 5. Add Tests

Create tests in `AciConfigurationTest.java`:

```java
@Test
public void testParseConfig_newObject() throws IOException {
    String json = "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"name\": \"test-fabric\"},"
        + "\"children\": [{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"name\": \"tenant1\"},"
        + "\"children\": [{"
        + "\"newObject\": {"
        + "\"attributes\": {\"name\": \"obj1\", \"descr\": \"Test\"}"
        + "}"
        + "}]"
        + "}"
        + "}]"
        + "}"
        + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config.getNewObjects(), hasKey("tenant1:obj1"));
}
```

## Testing ACI Configurations

When testing ACI configurations:

1. **Create minimal test JSON** that focuses on the feature being tested
2. **Use `AciConfiguration.fromJson()`** to parse the JSON
3. **Verify object extraction** by checking the appropriate collections
4. **Test conversion** by calling `AciConversion.toVendorIndependentConfigurations()`
5. **Test edge cases** like missing attributes, null children, invalid values

### Example Test Structure

```java
@Test
public void testParseVrf() throws Exception {
    String json = "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"name\": \"test\"},"
        + "\"children\": [{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"name\": \"tenant1\"},"
        + "\"children\": [{"
        + "\"fvCtx\": {\"attributes\": {\"name\": \"vrf1\", \"descr\": \"Test VRF\"}}"
        + "}]"
        + "}"
        + "}]"
        + "}"
        + "}";

    AciConfiguration config = AciConfiguration.fromJson("test.json", json, new Warnings());

    assertThat(config.getVrfs(), hasKey("tenant1:vrf1"));
    AciConfiguration.Vrf vrf = config.getVrfs().get("tenant1:vrf1");
    assertThat(vrf.getDescription(), equalTo("Test VRF"));
}
```

## Known Limitations

The following features are partially implemented or not yet supported:

1. **L3Out Conversion**: BGP, OSPF, and static route definitions in L3Out configurations need additional conversion work
2. **Filter Entries**: Full filter entry parsing with IP ranges and complex match conditions
3. **QoS and Service Graphs**: QoS policies and service graph redirection
4. **Multicast**: Multicast policies and configurations
5. **Endpoint Learning**: Dynamic endpoint learning and IP address migration

## References

- [Parsing Documentation](../README.md)
- [Implementation Guide](../implementation_guide.md)
- [ACI README](../../../projects/batfish/src/main/java/org/batfish/vendor/cisco_aci/README.md)

### Related Vendor Implementations

For comparison with other Jackson-based parsing implementations:

- **AWS**: `/projects/batfish/src/main/java/org/batfish/representation/aws/`
  - Entry point: `AwsConfiguration.java`
  - Uses `BatfishObjectMapper.mapper().convertValue()` for JSON deserialization

- **Azure**: `/projects/batfish/src/main/java/org/batfish/representation/azure/`
  - Entry point: `AzureConfiguration.java`
  - Uses `BatfishObjectMapper` for JSON deserialization

- **Sonic**: `/projects/batfish/src/main/java/org/batfish/vendor/sonic/`
  - Entry point: `SonicControlPlaneExtractor.java`
  - Uses `BatfishObjectMapper` for `ConfigDb` JSON deserialization
  - Uses `ObjectMapper(new YAMLFactory())` for YAML files
  - Also uses ANTLR4 for FRR routing daemon CLI configs (`frr.conf`)
