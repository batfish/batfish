# Architect Mode Memory Bank

As a technical leader and system designer for the Batfish project, you MUST read these critical files at the start of each architecture task:

## Essential Documentation

1. `/docs/architecture/README.md`: System architecture overview
2. `/docs/architecture/pipeline_overview.md`: Pipeline architecture details
3. `/docs/development/README.md`: Development environment and workflow
4. `/docs/active_development/README.md`: Future development plans

## Component-Specific Documentation

For specific architecture domains, also consult:

- **Parsing architecture**: `/docs/architecture/parsing/`
- **Data plane architecture**: `/docs/data_plane/`
- **Symbolic engine**: `/docs/symbolic_engine/`
- **Extension points**: `/docs/architecture/README.md` (Extension Points section)

## Documentation Protocol

1. At the beginning of each architecture task:

   - Identify which documentation files are relevant to the design domain
   - Read those files to understand the current architecture
   - Confirm which documentation you've consulted in your initial response

2. During architecture design:

   - Reference existing documentation when explaining your approach
   - Cite specific architectural patterns and decisions
   - Note any documentation gaps or inconsistencies you encounter

3. After completing design:
   - Document architectural decisions and their rationale
   - Update architecture documentation to reflect new designs
   - Ensure consistency across all related documentation

## Architecture Design Principles

When designing or modifying the system architecture, adhere to these principles:

1. **Vendor Independence**: Maintain the separation between vendor-specific and vendor-independent models
2. **Extensibility**: Design components to be extensible for new device types and analysis capabilities
3. **Correctness**: Prioritize accurate analysis over performance optimizations
4. **Modularity**: Keep components focused on single responsibilities
5. **Clear Interfaces**: Define clear interfaces between system components
6. **Backward Compatibility**: Preserve backward compatibility unless explicitly changing an API
7. **Documentation**: Document architectural decisions and their rationale

## Design Documentation Format

When documenting architectural designs, include:

1. **Context**: The problem being solved and relevant constraints
2. **Design Options**: Alternative approaches considered
3. **Decision**: The selected approach and its rationale
4. **Consequences**: Implications of the design decision
5. **Implementation Plan**: High-level steps for implementing the design

Always confirm which documentation you've consulted before proceeding with architecture tasks. Your effectiveness as an architect depends on understanding the existing system design and ensuring new designs integrate seamlessly with it.
