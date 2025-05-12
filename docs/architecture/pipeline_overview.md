# Batfish Pipeline Overview

This document provides a detailed overview of the Batfish processing pipeline, including design decisions and implementation details.

## Pipeline Stages

### 1. Parsing

**Input**: Raw vendor-specific configuration files
**Output**: Parse trees

In this stage, Batfish uses ANTLR-generated parsers to convert vendor-specific configuration files into parse trees. Each vendor has its own grammar files that define the syntax of its configuration language.

**Design Decisions**:

- Use of ANTLR for parser generation provides a standardized approach across vendors
- Modular grammar design allows for incremental support of new configuration features
- Preprocessing steps handle vendor-specific quirks (e.g., Juniper's hierarchical format)

### 2. Extraction

**Input**: Parse trees
**Output**: Vendor-specific Java objects

In this stage, Batfish extracts data from parse trees into vendor-specific Java objects. This involves traversing the parse tree and creating objects that represent the configuration elements.

**Design Decisions**:

- Separation of parsing and extraction allows for cleaner code organization
- Vendor-specific objects capture the unique features of each vendor's configuration model
- Extraction visitors follow a consistent pattern across vendors

### 3. Conversion

**Input**: Vendor-specific Java objects
**Output**: Vendor-independent Java objects

In this stage, Batfish converts vendor-specific objects into a unified vendor-independent model. This involves mapping vendor-specific concepts to generic networking concepts.

**Design Decisions**:

- Unified model enables analysis algorithms to work across vendors
- Conversion handles vendor-specific semantics and normalizes behavior
- Extensible design allows for adding new vendors without modifying analysis code

### 4. Post-processing

**Input**: Vendor-independent Java objects
**Output**: Finalized vendor-independent model

In this stage, Batfish performs various post-processing steps to finalize the vendor-independent model. This includes resolving references, inferring missing information, and validating the model.

**Design Decisions**:

- Separation of conversion and post-processing simplifies the conversion logic
- Common post-processing steps are applied consistently across all vendors
- Validation ensures the model is complete and consistent before analysis

### 5. Data Plane Generation

**Input**: Finalized vendor-independent model
**Output**: Data plane structures (RIBs, FIBs, etc.)

In this stage, Batfish computes the routing information bases (RIBs) and forwarding information bases (FIBs) for each device in the network. This involves simulating the routing protocols and computing the forwarding tables.

**Design Decisions**:

- Modular design allows for different data plane computation engines
- Incremental computation optimizes performance for repeated analysis
- Protocol implementations follow RFC specifications for accurate simulation

### 6. Forwarding Analysis

**Input**: Data plane structures
**Output**: Analysis results

In this stage, Batfish analyzes the forwarding behavior of the network to answer user questions. This involves using the data plane structures to determine how packets would be forwarded through the network.

**Design Decisions**:

- Symbolic analysis enables efficient reasoning about large sets of packets
- Question framework provides a flexible interface for different types of analysis
- Incremental analysis allows for efficient what-if scenarios

## Cross-Cutting Concerns

### Error Handling

Batfish is designed to be robust in the face of incomplete or incorrect configurations. Each stage of the pipeline includes error handling to ensure that analysis can proceed even with partial information.

### Performance Optimization

Performance is a key concern throughout the pipeline. Batfish uses various optimization techniques, including:

- Parallel processing where possible
- Incremental computation for repeated analysis
- Efficient data structures for representing large sets of packets

### Extensibility

The pipeline is designed to be extensible in several ways:

- Adding support for new vendors
- Adding new analysis capabilities
- Integrating with external systems

## Implementation Details

### Key Classes and Interfaces

- `NetworkSnapshot`: Represents a snapshot of the network configuration
- `ParseVendorConfigurationJob`: Handles parsing of configuration files
- `Warnings`: Collects warnings and errors during processing
- `Configuration`: Represents a vendor-independent device configuration
- `DataPlane`: Represents the computed data plane
- `Question`: Base class for all analysis questions

### Processing Flow

1. User submits a snapshot of network configurations
2. Batfish parses and processes the configurations through the pipeline
3. User submits questions about the network
4. Batfish answers the questions using the processed data
