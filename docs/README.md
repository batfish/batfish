# Batfish Documentation

## Project Overview

Batfish is a network validation tool that provides correctness guarantees for security, reliability, and compliance by analyzing the configuration of network devices. It builds complete models of network behavior from device configurations and finds violations of network policies (built-in, user-defined, and best-practices).

### Core Mission

Batfish enables network engineers to validate configuration changes _before_ deployment, closing a critical gap in existing network automation workflows. By including Batfish in automation workflows, network engineers can ensure that only correct changes are deployed.

### Key Problems Solved

- **Configuration Complexity**: Modern networks involve numerous devices with complex, interdependent configurations
- **High Cost of Errors**: Misconfigurations can lead to outages, security breaches, and compliance violations
- **Limited Testing**: Traditional network testing methods are manual, time-consuming, and often incomplete
- **Vendor Diversity**: Networks typically include devices from multiple vendors with different configuration languages

## Documentation Structure

This documentation is organized into several sections:

- **[Active Development](active_development/README.md)**: Current focus areas, roadmap, and known issues
- **[Architecture](architecture/README.md)**: System design, pipeline overview, and component details
- **[Development](development/README.md)**: Setup instructions, contribution guidelines, and technical context
- **[User Guide](user_guide/README.md)**: Usage instructions, use cases, and best practices

## For Developers

This documentation is primarily aimed at developers interested in understanding and improving Batfish. If you are interested in trying out Batfish on your network, check out our [instructions for getting started](https://pybatfish.readthedocs.io/en/latest/getting_started.html) instead.

## Getting Help

If you get stuck after reading all relevant documentation, you can ask questions on the [Batfish Slack](https://join.slack.com/t/batfish-org/shared_invite/enQtMzA0Nzg2OTAzNzQ1LTcyYzY3M2Q0NWUyYTRhYjdlM2IzYzRhZGU1NWFlNGU2MzlhNDY3OTJmMDIyMjQzYmRlNjhkMTRjNWIwNTUwNTQ).
