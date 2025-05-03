# Batfish Architecture

This document provides an overview of Batfish's architecture and key design decisions.

## Core Architecture

Batfish follows a pipeline architecture with distinct stages:

1. **[Parsing](parsing/README.md)**: Convert vendor-specific configurations into parse trees
2. **[Extraction](extraction/README.md)**: Extract data from parse trees into vendor-specific models
3. **[Conversion](conversion/README.md)**: Convert vendor-specific models into vendor-independent models
4. **[Post-processing](post_processing/README.md)**: Finalize and clean up the vendor-independent model
5. **[Data Plane Generation](data_plane/README.md)**: Compute routing information bases and forwarding tables
6. **[Forwarding Analysis](forwarding_analysis/README.md)**: Analyze forwarding behavior

## Key Design Decisions

### Vendor-Independent Model

Batfish converts all vendor-specific configurations into a unified vendor-independent model. This allows analysis algorithms to work consistently across different network devices.

**Rationale**: This approach enables Batfish to support multiple vendors without duplicating analysis code.

### Parse Tree-Based Extraction

Batfish uses ANTLR-generated parsers to create parse trees, which are then processed to extract configuration data.

**Rationale**: This approach provides a clean separation between syntax recognition and semantic interpretation.

### Symbolic Analysis

Batfish uses symbolic analysis techniques to efficiently reason about large sets of packets.

**Rationale**: This approach allows Batfish to analyze network behavior for all possible packets without having to enumerate them individually.

## Component Interactions

### Question Framework

Questions are the primary way users interact with Batfish. Each question is implemented as a plugin that can access the network model and data plane.

### Data Model Evolution

The data model evolves through these stages:

1. Vendor-specific text configurations
2. Parse trees
3. Vendor-specific Java objects
4. Vendor-independent Java objects
5. Data plane structures

## Extension Points

Batfish is designed to be extensible in several ways:

1. **Adding New Device Support**:

   - Implement a parser for the device's configuration language
   - Create extraction logic to populate vendor-specific models
   - Implement conversion logic to the vendor-independent model

2. **Adding New Analysis Capabilities**:

   - Implement new questions that use the existing data model
   - Extend the data model to capture new types of information
   - Add new analysis algorithms that operate on the data plane

3. **Integrating with External Systems**:
   - Use the RESTful API provided by the coordinator
   - Use the Python SDK (Pybatfish) to script interactions

## Pipeline Details

For detailed information on each pipeline stage, see the corresponding documentation:

- [Pipeline Overview](pipeline_overview.md)
- [Parsing](parsing/README.md)
- [Extraction](extraction/README.md)
- [Conversion](conversion/README.md)
- [Post-processing](post_processing/README.md)
- [Data Plane Generation](data_plane/README.md)
- [Forwarding Analysis](forwarding_analysis/README.md)
