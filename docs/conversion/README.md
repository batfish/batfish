# Conversion

In Batfish, "conversion" refers to the process of transforming vendor-specific network configuration objects into a unified vendor-independent model. This is a critical stage in the Batfish pipeline, as it enables analysis algorithms to work consistently across different network device vendors.

## Overview

The conversion stage sits between extraction and post-processing in the Batfish pipeline:

```
Parse Trees → [Extraction] → Vendor-Specific Objects → [Conversion] → Vendor-Independent Objects → [Post-Processing] → Data Plane
```

During conversion, Batfish:
1. Translates vendor-specific configuration concepts into generic networking concepts
2. Normalizes differences in how vendors represent similar functionality
3. Applies validation and sanity checks
4. Collects warnings and errors for incomplete or unsupported features
5. Resolves references between configuration elements

## Input

- **Vendor-specific configuration objects**: Java objects representing device configurations after extraction (e.g., `CiscoConfiguration`, `JuniperConfiguration`, `AristaConfiguration`)
- **Conversion context** (optional): Additional context needed for conversion (e.g., CheckPoint management configuration)
- **ISP configuration** (optional): Internet service provider configuration for certain network setups

## Output

- **Vendor-independent configuration objects**: `Configuration` objects in Batfish's unified vendor-independent model
- **Conversion warnings**: Notifications about unsupported features, potential issues, or limitations
- **Additional wiring information**: Special network configurations (e.g., AWS, CheckPoint, ISP configurations)

## Main Entry Point

The primary entry point for conversion is the **`ConvertConfigurationJob`** class located at:

`projects/batfish/src/main/java/org/batfish/job/ConvertConfigurationJob.java`

This job orchestrates the conversion process and handles:
- Managing conversion warnings and errors
- Parallel processing of multiple configurations
- Finalizing configurations after conversion
- Structure management and validation

### ConvertConfigurationJob

**Key responsibilities:**
- Creates `ConvertConfigurationAnswerElement` with conversion results
- Processes vendor configurations in parallel when possible
- Calls `finalizeConfiguration()` for each converted configuration
- Aggregates warnings and errors across all configurations

## VendorConfiguration Base Class

All vendor-specific configuration classes extend the abstract `VendorConfiguration` base class:

**Location:** `projects/common/src/main/java/org/batfish/vendor/VendorConfiguration.java`

**Abstract method:**
```java
public abstract List<Configuration> toVendorIndependentConfigurations()
    throws VendorConversionException;
```

**Key properties:**
- `_warnings`: Tracks conversion warnings
- `_structureManager`: Manages structure definitions and references
- `_conversionContext`: Additional context for conversion
- `_runtimeData`: Runtime data from snapshots

### Vendor-Specific Implementations

Each vendor implements `toVendorIndependentConfigurations()`:

```java
@Override
public List<Configuration> toVendorIndependentConfigurations() {
    Configuration c = Configuration.builder()
        .setHostname(_hostname)
        .setDefaultCrossZoneAction(LineAction.PERMIT)
        .setDefaultInboundAction(LineAction.PERMIT)
        .build();

    // Convert specific features
    convertStaticRoutes(this, c);
    convertInterfaces(this, c);
    convertBgp(this, c);

    return ImmutableList.of(c);
}
```

## Conversion Patterns

### Single Configuration Pattern

Most devices produce a single vendor-independent configuration:

```java
public List<Configuration> toVendorIndependentConfigurations() {
    return ImmutableList.of(toVendorIndependentConfiguration());
}
```

### Multi-Configuration Pattern

Some vendors support multiple configurations (e.g., Palo Alto Panorama managing multiple firewalls):

```java
public List<Configuration> toVendorIndependentConfigurations() {
    ImmutableList.Builder<Configuration> outputConfigurations = ImmutableList.builder();

    // Build primary config
    Configuration primaryConfig = toVendorIndependentConfiguration();
    outputConfigurations.add(primaryConfig);

    // Add managed configurations
    outputConfigurations.addAll(
        managedConfigurations.stream()
            .map(ManagedConfig::toVendorIndependentConfiguration)
            .collect(ImmutableList.toImmutableList()));

    return outputConfigurations.build();
}
```

### Conversion Utilities

Each vendor has utility classes for conversion (e.g., `CiscoConversions`, `CumulusConversions`, `FrrConversions`):

```java
public final class CiscoConversions {
    static void convertStaticRoutes(CiscoConfiguration vc, Configuration c) {
        vc.getStaticRoutes()
            .forEach((prefix, route) ->
                c.getDefaultVrf().getStaticRoutes().add(toStaticRoute(prefix, route)));
    }

    static void convertBgpProcess(CiscoBgpProcess bgpProcess, Configuration c) {
        // Convert BGP process to vendor-independent model
    }
}
```

## Common Conversion Tasks

### Interface Conversion

Converting vendor-specific interfaces to vendor-independent `Interface` objects:

```java
Interface iface = Interface.builder()
    .setName(vendorIface.getName())
    .setHostname(configuration.getHostname())
    .setVrf(vendorIface.getVrfName())
    .setAddresses(toIpStrings(vendorIface.getAddresses()))
    .setBandwidth(vendorIface.getSpeed())
    .setAdminUp(vendorIface.isEnabled())
    .build();
```

### Protocol Conversion

Converting routing protocols from vendor-specific to vendor-independent:

**BGP:**
- Convert vendor-specific BGP process to `BgpProcess` in VI model
- Normalize AS numbers, neighbor configurations, and policies
- Convert route-maps and route policies

**OSPF:**
- Convert OSPF process and areas to `OspfProcess` in VI model
- Normalize area IDs, interface costs, and neighbor configurations
- Handle vendor-specific OSPF features

**Static Routes:**
- Convert vendor-specific static routes to `StaticRoute` in VI model
- Normalize next-hop representations
- Track route dependencies (e.g., routes that depend on interface reachability)

### Access Control Lists (ACLs)

Converting vendor-specific filter syntax to vendor-independent `IpAccessList`:

```java
IpAccessList acl = IpAccessList.builder()
    .setName(vendorAcl.getName())
    .setLines(convertAclLines(vendorAcl.getLines()))
    .build();
```

## Warnings and Error Handling

### Warning Levels

Batfish uses three warning levels during conversion:

1. **PEDANTIC**: Minor issues and informational messages
   - Unusual but valid configuration patterns
   - Non-standard syntax that is correctly handled

2. **REDFLAG**: Serious issues that may affect analysis
   - Unsupported features that are partially implemented
   - Configuration inconsistencies
   - Potentially incorrect behavior

3. **UNIMPLEMENTED**: Features not yet implemented
   - Configuration lines that are recognized but not converted
   - Features that will be added in future releases

### Example Warning Usage

```java
// Red flag for unsupported feature
warnings.redFlag("NAT type %s is not supported", natType);

// Pedantic warning for unusual pattern
warnings.pedantic("Interface %s has no IP address", ifaceName);

// Unimplemented warning
warnings.unimplemented("MPLS is not yet supported");
```

### Structure Management

The `StructureManager` tracks:
- **Defined structures**: Configuration objects referenced by name (e.g., route policies, ACLs)
- **Referenced structures**: Where each structure is used
- **Undefined references**: References to structures that don't exist

```java
// Define a structure
_structureManager.defineStructure(
    StructureType.ROUTE_POLICY,
    policyName,
    filename);

// Reference a structure
_structureManager.referenceStructure(
    StructureType.ROUTE_POLICY,
    policyName,
    StructureUsage.BGP_IMPORT_POLICY,
    line);
```

### Validation During Conversion

Conversion includes several validation checks:

1. **Required properties**: Ensures all required properties are set
2. **Reference integrity**: Validates that all references point to defined structures
3. **Sanity checks**: Validates configuration consistency
4. **Default values**: Applies appropriate defaults for missing values

## Finalization

After conversion, each configuration goes through finalization:

**Location:** `ConvertConfigurationJob.finalizeConfiguration()`

**Finalization steps:**
1. **Sanity checks**: Required properties, default actions
2. **Helper structure generation**: Tenant VNI interfaces, etc.
3. **Validation**: OSPF areas, VRRP groups, interfaces
4. **Immutable conversion**: Convert maps to immutable collections
5. **Reference verification**: ACLs, routing policies

```java
private void finalizeConfiguration(
    Configuration c,
    VendorConfiguration vc,
    Warnings warnings) {

    // Sanity checks
    checkRequiredProperties(c, warnings);

    // Generate helper structures
    generateTenantVniInterfaces(c);

    // Validate configurations
    validateOspfAreas(c, warnings);
    validateVrrpGroups(c, warnings);

    // Convert to immutable
    c.setInterfaces(ImmutableMap.copyOf(c.getInterfaces()));

    // Verify references
    verifyAclReferences(c, warnings);
    verifyRoutingPolicyReferences(c, warnings);
}
```

## Conversion Result

The result of conversion is encapsulated in **`ConvertConfigurationResult`**:

**Location:** `projects/batfish/src/main/java/org/batfish/job/ConvertConfigurationResult.java`

**Contains:**
- `Map<String, Configuration> configurations`: Converted configurations by hostname
- `Map<String, Warnings> warningsByHost`: Warnings for each host
- `ConversionStatus status`: Overall status (PASSED, WARNINGS, FAILED)
- `String error`: Error details if conversion failed
- `Map<String, String> fileMappings`: Input file to output hostname mapping

## Error Handling

### VendorConversionException

Thrown when conversion fails:

```java
throw new VendorConversionException("Cannot convert configuration: " + message);
```

### Graceful Degradation

Conversion attempts to continue when possible:
- Individual features may fail with warnings while overall conversion succeeds
- Unsupported features are logged but don't halt conversion
- Partial results are returned with appropriate warnings

## Architecture Principles

1. **Vendor-Independence**: Output is standardized `Configuration` objects
2. **Extensibility**: New vendors can be added by extending `VendorConfiguration`
3. **Validation**: Comprehensive checks ensure output quality
4. **Performance**: Parallel processing for multi-device configurations
5. **Traceability**: Structure tracking maintains source-to-output mapping
6. **Error Resilience**: Conversion continues where possible with warnings

## Key Classes Summary

| Class | Location | Purpose |
|-------|----------|---------|
| `VendorConfiguration` | `projects/common/.../vendor/` | Abstract base class for all vendor-specific configurations |
| `ConvertConfigurationJob` | `projects/batfish/.../job/` | Main entry point for conversion |
| `ConvertConfigurationResult` | `projects/batfish/.../job/` | Result object containing converted configurations |
| `ConversionContext` | `projects/common/.../vendor/` | Additional conversion context |
| `VendorConversionException` | `projects/common/.../` | Exception for conversion failures |

## Related Documentation

- [Extraction](../extraction/README.md): Converting parse trees to vendor-specific objects
- [Post-processing](../post_processing/README.md): Finalizing vendor-independent configurations
- [Architecture Overview](../architecture/README.md): Overall Batfish architecture
- [Pipeline Overview](../architecture/pipeline_overview.md): Detailed pipeline description
