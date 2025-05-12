# Batfish Development

This section provides information about the Batfish development environment, technology stack, and contribution guidelines.

## Technology Stack

### Core Technologies

- **Java**: Primary implementation language
- **ANTLR**: Parser generator used for network configuration parsing
- **Python**: Used for the Pybatfish client library
- **Bazel**: Build system
- **Docker**: Container platform for deployment

### Key Libraries and Dependencies

- **BDD (Binary Decision Diagrams)**: Used for symbolic analysis
- **Jackson**: JSON processing
- **Jersey**: RESTful web services
- **JUnit**: Testing framework
- **Guava**: Google core libraries for Java

## Development Environment

### Prerequisites

- Java 17 or later
- [Bazelisk](https://github.com/bazelbuild/bazelisk#installation)
- Python 3.9 or later (for Pybatfish)
- Docker (for containerized deployment)

### IDE Setup

Batfish developers primarily use IntelliJ IDEA. Detailed setup instructions are available in the [IntelliJ setup guide](intellij_setup/README.md).

### Build System

Batfish uses Bazel as its build system. Key commands:

- `bazel build //...`: Build all targets
- `bazel test //...`: Run all tests
- `./tools/bazel_run.sh`: Run a local Batfish server

**IMPORTANT**: Batfish uses Bazel, NOT Maven. All build and test operations must use Bazel commands.

## Getting Started

- [Building and Running](../building_and_running/README.md): Instructions for building and running Batfish
- [Contributing](contributing/README.md): Guidelines for contributing to Batfish
- [Roo](roo.md): Overview of the Roo AI coding assistant used in this project

## Development Workflow

1. **Issue Tracking**: GitHub Issues
2. **Version Control**: Git with GitHub
3. **Code Review**: Pull requests with required reviews
4. **Continuous Integration**: GitHub Actions

## Project Principles

1. **Correctness**: Batfish prioritizes accurate analysis over speed
2. **Usability**: The tool should be accessible to network engineers without requiring programming expertise
3. **Extensibility**: The architecture should allow for easy addition of new device types and analysis capabilities
4. **Community-Driven**: Development priorities are guided by real-world network engineering needs
